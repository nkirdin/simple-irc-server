/*
 * 
 * PartIrcCommand 
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
 * PartIrcCommand - класс, который проверяет параметры команды IRC 
 * PART и исполняет ее. 
 *
 *      <P>Command: PART
 *  <P>Parameters: &lt;channel&gt;{,&lt;channel&gt;} [&lt;Part Message&gt;]
 *
 * @version 0.5 2012-02-20
 * @author  Nikolay Kirdin
 */
public class PartIrcCommand extends IrcCommandBase {

    /** Название команды. */    
    public static final String commandName = "PART";

    /** Параметр: &lt;channel&gt;{,&lt;channel&gt;}. */
    private LinkedList<String> channelList = null;
    
    /** Параметр: &lt;Part Message&gt;. */
    private String message = null;

    public PartIrcCommand() {}

    /** 
     * Создатель объекта команды без проверки параметров.
     * @param db репозитарий.
     * @param client источник команды.
     * @return объект команды.
     */
    public static PartIrcCommand create(DB db, User client) {
        PartIrcCommand partIrcCommand = new PartIrcCommand();
        partIrcCommand.db = db;
        partIrcCommand.client = client;
        partIrcCommand.channelList = new LinkedList<String>();
        Set<IrcChannel> channelSet = client.getChannelSet();
        for (IrcChannel ch : channelSet) {
            partIrcCommand.channelList.offer(ch.getNickname());
        }
        partIrcCommand.setExecutable(true);
        partIrcCommand.message = "";        
        return partIrcCommand;
    }

    /** 
     * Создатель объекта команды без проверки параметров.
     * @param db репозитарий.
     * @param client источник команды.
     * @param channelList параметр &lt;channel&gt;{,&lt;channel&gt;}.
     * @param message параметр &lt;Part Message&gt;.
     * @return объект команды.
     */
    public static PartIrcCommand create(DB db,
            User client,
            LinkedList<String> channelList,
            String message) {

        PartIrcCommand partIrcCommand = new PartIrcCommand();
        partIrcCommand.db = db;
        partIrcCommand.client = client;
        partIrcCommand.channelList = channelList;
        partIrcCommand.message = message;
        partIrcCommand.setExecutable(true);
        return partIrcCommand;
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

        channelList = new LinkedList<String>(Arrays.asList(
                pList.get(index++).split(",")));
        for (String item : channelList) {
            check(item, channelPattern);
        }
        
        if (index != pList.size()) {
            message = check(pList.get(index++), stringPattern);
        } else {
            message = "";
        }
        
        setExecutable(true);
    }

    /** Исполнитель команды. */
    public void run() {
        Response.Reply responseReply = null;

        if (!isExecutable()) {
            return;
        }

        if (!message.isEmpty()) {
            message = " :" + message;
        }

        for (String s : channelList) {
            IrcChannel ch = db.getChannel(s);
            if (ch != null && ch.checkMember(client)) {
                String content = commandName + " " + s + message;
                client.send(client, content);
                ch.send(client, content);
                client.remove(ch);
                ch.remove(client);
                if (ch.isUserSetEmpty()) {
                    responseReply = db.unRegister(ch);
                    if (responseReply == Response.Reply.RPL_OK) {
                        ch.delete();
                    } else {
                        throw new Error("PART: db.unRegister(channel)" +
                                " Internal error");
                    }
                }
            } else {
                client.send(errNotOnChannel(client, s));
            }
        }
    }
}
