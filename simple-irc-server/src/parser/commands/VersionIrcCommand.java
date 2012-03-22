/*
 * 
 * VersionIrcCommand 
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
 * VersionIrcCommand - класс, который выполняет синтаксический разбор,
 * проверку аргументов и исполняет команду VERSION. 
 *
 *    <P>Command: VERSION
 * <P>Parameters: [&lt;server&gt;]
 *
 * @version 0.5 2012-02-02
 * @author  Nikolay Kirdin
 */
public class VersionIrcCommand extends IrcCommandBase {

    /** Название команды. */    
    public static final String commandName = "VERSION";

    /** Параметр: &lt;server&gt;. */
    private String servernameMask = "";

    public VersionIrcCommand() {}

    /** 
     * Создатель объекта команды без проверки параметров.
     * @param db репозитарий.
     * @param client источник команды.
     * @param servernameMask параметр &lt;server&gt;.
     * @return объект команды.
     */
    public static VersionIrcCommand create(DB db, User client, 
            String servernameMask) {
        VersionIrcCommand versionIrcCommand = new VersionIrcCommand();
        versionIrcCommand.db = db;
        versionIrcCommand.client = client;
        versionIrcCommand.servernameMask = servernameMask;
        versionIrcCommand.setExecutable(true);
        return versionIrcCommand;
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

        if (!servernameMask.isEmpty()) {
            String content = ":" + client.getNickname() + " " + 
                    commandName;
            forwardWithMask(servernameMask, content, client, db);
            return;
        }

        client.send(rplVersion(client, Constants.SERVER_VERSION, 
                Globals.logger.get().getLevel().toString(),
                client.getIrcServer().getHostname(),
                Constants.VERSION_COMMENT));
    }

    /**
     * Создание формализованного ответа типа
     * {@link Response.Reply#RPL_VERSION}.
     * @param ircTalker отправитель.
     * @param version
     * @param debugLevel
     * @param servername
     * @param comment
     * @return объект класса {@link IrcCommandReport} с формализованным 
     * ответом.
     */
    private IrcCommandReport rplVersion(IrcTalker ircTalker,
            String version, String debugLevel, String servername,
            String comment) {
        String remark = Response.makeText(Response.Reply.RPL_VERSION, 
                ircTalker.getNickname(),
                Constants.SERVER_VERSION,
                Globals.logger.get().getLevel().toString(),
                ((User) ircTalker).getIrcServer().getHostname(),
                Constants.VERSION_COMMENT);
        return new IrcCommandReport(remark, ircTalker,
                Globals.thisIrcServer.get());
    }
}
