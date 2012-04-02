/*
 * 
 * LinksIrcCommand 
 * is part of Simple Irc Server
 *
 *
 * Copyright (С) 2012, Nikolay Kirdin
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License Version 3.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License Version 3 for more details.
 *
 * You should have received a copy of the GNU Lesser General Public 
 * License Version 3 along with this program.  If not, see 
 * <http://www.gnu.org/licenses/>.
 *
 */

import java.util.*;

/**
 * LinksIrcCommand - класс, который проверяет параметры команды IRC 
 * LINKS и исполняет ее. 
 *
 *    <P>Command: LINKS
 * <P>Parameters: [[&lt;remote server&gt;] &lt;server mask&gt;]
 *
 * @version 0.5 2012-02-07
 * @author  Nikolay Kirdin
 */
public class LinksIrcCommand extends IrcCommandBase {

    /** Название команды. */    
    public static final String commandName = "LINKS";

    /** Параметр: &lt;remote server&gt;. */
    private String remoteServer = null;
    
    /** Параметр: &lt;server mask&gt;. */
    private String servernameMask = null;

    public LinksIrcCommand() {}

    /** 
     * Создатель объекта команды без проверки параметров.
     * @param db репозитарий.
     * @param client источник команды.
     * @param remoteServer - параметр &lt;remote server&gt;.
     * @param servernameMask параметр &lt;server mask&gt;.
     * @return объект команды.
     */
    public LinksIrcCommand create(DB db,
            User client,
            String remoteServer,
            String servernameMask) {

        LinksIrcCommand linksIrcCommand = new LinksIrcCommand();
        linksIrcCommand.db = db;
        linksIrcCommand.client = client;
        linksIrcCommand.remoteServer = remoteServer;
        linksIrcCommand.servernameMask = servernameMask;
        linksIrcCommand.setExecutable(true);
        return linksIrcCommand;
    }
    
    /** 
     * Создатель объекта команды с проверкой параметров.
     * @param pList список параметров команды.
     * @param trailing true, если в последним в списке параметров
     * находится секция "trailing".
     * @param requestor источник команды.
     * @param db репозитарий.
     * @throws IrcSyntaxException если будет обнаружена синтаксическая 
     * ошибка.
     */
    public void checking(LinkedList<String> pList,
            boolean trailing,
            IrcTalker requestor,
            DB db) throws IrcSyntaxException {

        int index = 0;
        this.db = db;
        this.pList = pList;
        this.trailing = trailing;

        if (!(requestor instanceof User)) {
            requestor.send(errUnknownCommand(requestor, commandName));
            return;
        }

        client = (User) requestor;

        if (!client.isRegistered()) {
            client.send(errNotRegistered(client));
            return;
        }

        if (index != pList.size()) {
            servernameMask = check(pList.get(index++), 
                    servernameMaskRegex);
            if (index != pList.size()) {
                remoteServer = servernameMask;
                servernameMask = check(pList.get(index++), 
                        servernameMaskRegex);
            }
        } else {
            servernameMask = "";
        }
        setExecutable(true);
    }

    /** Исполнитель команды. */
    public void run() {

        if (!isExecutable()) {
            return;
        }

        if (remoteServer != null && !remoteServer.isEmpty()) {
            String content = ":" + client.getNickname() + " " + 
                    commandName + " " + servernameMask;
            boolean notFound = true;
            LinkedHashSet<IrcServer> ircServerSet = 
                    db.getIrcServerSet();
            for (IrcServer ircServer : ircServerSet) {
                if (IrcMatcher.match(remoteServer, 
                        ircServer.getNickname())) {
                    notFound = false;
                    ircServer.send(client, content);
                }
            }
            if (notFound) {
                client.send(errNoSuchServer(client, remoteServer));
            } 
            return;
        }

        LinkedHashSet<IrcServer> ircServerSet = 
                new LinkedHashSet<IrcServer>();
        if (servernameMask != null && !servernameMask.isEmpty()) {
            LinkedHashSet<String> ircServerNicknameSet = 
                    db.getIrcServernameSet();
            for (String name : ircServerNicknameSet) {
                if (IrcMatcher.match(servernameMask, name)) {
                    ircServerSet.add(db.getIrcServer(name));
                }
            }
        } else {
            ircServerSet = db.getIrcServerSet();
        }

        for (IrcServer ircServerLink : ircServerSet) {
            client.send(rplLinks(client, 
                    servernameMask, 
                    ircServerLink.getHostname(), 
                    ircServerLink.getHopcount(), 
                    ircServerLink.getInfo()));
        }
        client.send(rplEndOfLinks(client, servernameMask));
    }

    /** 
     * Создает сообщение соответствующее  формализованному сообщению 
     * {@link Reply#RPL_LINKS}. 
     * @param requestor источник команды.
     * @param servernameMask маска поиска..
     * @param hostname имя хоста.
     * @param hopcount количество хопов.
     * @param info краткая информация.
     * @return объект с сообщением.
     */        
    private IrcCommandReport rplLinks(IrcTalker requestor,
            String servernameMask, String hostname, int hopcount,
            String info) {
         String remark = Reply.makeText(Reply.RPL_LINKS, 
                requestor.getNickname(),
                servernameMask,
                hostname,
                String.valueOf(hopcount),
                info);

        return new IrcCommandReport(remark, requestor,
                Globals.thisIrcServer.get());
    }
    
    /** 
     * Создает сообщение соответствующее  формализованному сообщению 
     * {@link Reply#RPL_ENDOFLINKS}. 
     * @param requestor источник команды.
     * @param servernameMask маска для поиска.
     * @return объект с сообщением.
     */        
    private IrcCommandReport rplEndOfLinks(IrcTalker requestor,
            String servernameMask) {
        String remark = Reply.makeText(Reply.RPL_ENDOFLINKS,  
                client.getNickname(),
                servernameMask);

        return new IrcCommandReport(remark, requestor,
                Globals.thisIrcServer.get());
    }
}
