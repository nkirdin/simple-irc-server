/*
 * 
 * ListIrcCommand 
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
 * ListIrcCommand - класс, который проверяет параметры команды IRC 
 * LIST и исполняет ее. 
 *
 *    <P>Command: LIST
 * <P>Parameters: [&lt;channel&gt;{,&lt;channel&gt;} [&lt;server&gt;]]
 *
 * @version 0.5 2012-02-07
 * @author  Nikolay Kirdin
 */
public class ListIrcCommand extends IrcCommandBase {

    /** Название команды. */    
    public static final String commandName = "LIST";

    /** Параметр: &lt;channel&gt;{,&lt;channel&gt;}. */
    private LinkedHashSet<String> channelStringSet = null;
    
    /** Параметр: &lt;server&gt;. */
    private String servernameMask = null;

    public ListIrcCommand() {}

    /** 
     * Создатель объекта команды без проверки параметров.
     * @param db репозитарий.
     * @param client источник команды.
     * @param channelStringSet параметр &lt;channel&gt;{,&lt;channel&gt;}.
     * @param servernameMask параметр &lt;server&gt;.
     * @return объект команды.
     */
    public ListIrcCommand create(DB db,
            User client,
            LinkedHashSet<String> channelStringSet,
            String servernameMask) {

        ListIrcCommand listIrcCommand = new ListIrcCommand();
        listIrcCommand.db = db;
        listIrcCommand.client = client;
        listIrcCommand.channelStringSet = channelStringSet;
        listIrcCommand.servernameMask = servernameMask;
        listIrcCommand.setExecutable(true);
        return listIrcCommand;
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
            channelStringSet = new LinkedHashSet<String>(Arrays.asList(
                    pList.get(index++).split(",")));
            for (String channelString : channelStringSet) {
                check(channelString, channelPattern);
            }

            if (index != pList.size()) {
                servernameMask = check(pList.get(index++), 
                        servernameMaskRegex);
            } else {
                servernameMask = "";
            }
        }
        setExecutable(true);
    }

    /** Исполнитель команды. */
    public void run() {

        String topic = "";
        LinkedHashSet<IrcChannel> channelSet = 
                new LinkedHashSet<IrcChannel>();
        Iterator<IrcChannel> channelSetIterator = null;

        if (!isExecutable()) {
            return;
        }
        if (servernameMask != null && !servernameMask.isEmpty()) {

            String channelString = "";
            for (String s : channelStringSet) {
                channelString = channelString + s + ",";
            }
            if (channelString.endsWith(",")) {
                channelString = channelString.substring(0, 
                        channelString.length() - 1);
            }
            String content = ":" + client.getNickname() + " " + 
                    commandName + " " + channelString;
            forwardWithMask(servernameMask, content, client, db);
            return;
        }

        if (channelStringSet == null || channelStringSet.isEmpty()) {
            channelSetIterator = db.getChannelSetIterator();
        } else {
            for (String channelName : channelStringSet) {
                IrcChannel ch = db.getChannel(channelName);
                if (ch != null) {
                    channelSet.add(ch);
                }
            }
            channelSetIterator = channelSet.iterator();
        }
        for (Iterator<IrcChannel> iterator = channelSetIterator;
                    channelSetIterator.hasNext();) {
            IrcChannel ch = iterator.next();
            int visibleUserCount = 0;
            
            if (ch == null || !ch.isVisible(client)) {
                continue;
            }
            
            for (Iterator<User> userSetIterator = ch.getUserSetIterator();
                    userSetIterator.hasNext();) {
                User user = userSetIterator.next();
                if (user.isVisible(client) && user.isRegistered()) {
                    visibleUserCount++;
                }
            }
            topic = ch.getTopic();
            if (topic == null) {
                topic = "";
            }
            client.send(rplList(client, ch.getNickname(), 
                    visibleUserCount, topic));
        }

        client.send(rplListEnd(client));
        
    }
    
    /** 
     * Создает сообщение соответствующее  формализованному сообщению 
     * {@link Response.Reply#RPL_LIST}. 
     * @param requestor источник команды.
     * @param channelName имя канала.
     * @param visibleUserCount количество видимых членов канала.
     * @param topic топик канала.
     * @return объект с сообщением.
     */        
    private IrcCommandReport rplList(IrcTalker requestor, 
            String channelName, int visibleUserCount, String topic) {
        
        String remark = Response.makeText(Response.Reply.RPL_LIST, 
                requestor.getNickname(),
                channelName + " " + visibleUserCount + " :" + topic);

        return new IrcCommandReport(remark, requestor,
                Globals.thisIrcServer.get());
    }

    /** 
     * Создает сообщение соответствующее  формализованному сообщению 
     * {@link Response.Reply#RPL_LISTEND}. 
     * @param requestor источник команды.
     * @return объект с сообщением.
     */        
    private IrcCommandReport rplListEnd(IrcTalker requestor) {
        
        String remark = Response.makeText(Response.Reply.RPL_LISTEND, 
                requestor.getNickname());

        return new IrcCommandReport(remark, requestor,
                Globals.thisIrcServer.get());
    }
}
