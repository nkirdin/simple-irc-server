package org.grass.simpleircserver.parser.commands;
/*
 * 
 * NamesIrcCommand 
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

import org.grass.simpleircserver.base.DB;
import org.grass.simpleircserver.channel.ChannelMode;
import org.grass.simpleircserver.channel.IrcChannel;
import org.grass.simpleircserver.parser.IrcSyntaxException;
import org.grass.simpleircserver.talker.IrcTalker;
import org.grass.simpleircserver.talker.user.User;

/**
 * NamesIrcCommand - класс, который проверяет параметры команды IRC 
 * NAMES и исполняет ее. 
 *
 *    <P>Command: NAMES
 * <P>Parameters: [&lt;channel&gt;{,&lt;channel&gt;} [&lt;target&gt;]]
 *
 * @version 0.5 2012-02-07
 * @version 0.5.3 2015-11-05 Program units were moved from default package into packages with names. Unit tests were added.
 * @author  Nikolay Kirdin
 */
public class NamesIrcCommand extends IrcCommandBase {

    /** Название команды. */    
    public static final String commandName = "NAMES";

    /** Параметр: &lt;channel&gt;{,&lt;channel&gt;}. */
    private LinkedHashSet<String> channelStringSet;
    
    /** Параметр: &lt;target&gt;. */
    private String servernameMask;

    public NamesIrcCommand() {}

    /** 
     * Создатель объекта команды без проверки параметров.
     * @param db репозитарий.
     * @param client источник команды.
     * @param channelStringSet параметр &lt;channel&gt;{,&lt;channel&gt;}.
     * @param servernameMask параметр &lt;target&gt;.
     * @return объект команды.
     */
    public static NamesIrcCommand create(DB db,
            User client,
            LinkedHashSet<String> channelStringSet,
            String servernameMask) {

        NamesIrcCommand namesIrcCommand = new NamesIrcCommand();
        namesIrcCommand.db = db;
        namesIrcCommand.client = client;
        namesIrcCommand.channelStringSet = channelStringSet;
        namesIrcCommand.servernameMask = servernameMask;
        namesIrcCommand.setExecutable(true);
        return namesIrcCommand;
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

            String content = ":" + client.getNickname()
                    + " " + commandName
                    + " " + channelString;

            forwardWithMask(servernameMask, content, client, db);
            return;
        }

        if (channelStringSet == null || channelStringSet.isEmpty()) {
            channelSetIterator = db.getChannelSetIterator();
        } else {
            for (String s : channelStringSet) {
                IrcChannel ch = db.getChannel(s);
                if (ch != null) {
                   channelSet.add(ch); 
                } else {
                    client.send(rplEndOfNames(client, s));
                }
            }
            channelSetIterator = channelSet.iterator();
        }

        String nick = client.getNickname();
        
        for (Iterator<IrcChannel> iterator = channelSetIterator;
                iterator.hasNext();) {
            
            IrcChannel ch = iterator.next();
            //visibleUserSet.removeAll(ch.getUserSet());
            if (ch.isVisible(client) && !ch.isAnonymous()) {
                
                for (Iterator<Map.Entry<User, EnumSet <ChannelMode>>>
                        userMapEntrySetIterator = 
                        ch.getUserEntrySetIterator(); 
                        userMapEntrySetIterator.hasNext();) {
                    Map.Entry<User, EnumSet <ChannelMode>> 
                            channelUserEntry = 
                            userMapEntrySetIterator.next();
                    User user = channelUserEntry.getKey();
                    EnumSet <ChannelMode> channelMode = 
                            channelUserEntry.getValue();
                    
                    if (!user.isVisible(client)) {
                        continue;
                    }
                    //visibleUserSet.add(user);
                    
                    client.send(rplNameReply(client,
                            nick,
                            ch.getStatus(),
                            ch.getNickname(),
                            getUserStatus(channelMode),
                            user.getNickname()));
                }
                client.send(rplEndOfNames(client, ch.getNickname()));
            }
        }

        if (channelStringSet == null || channelStringSet.isEmpty()) {
            //for (User user : visibleUserSet) {
            userLoop:
            for (Iterator<User> iterator = db.getUserSetIterator();
                    iterator.hasNext();) {
            
                User user = iterator.next();
                
                if (!user.isVisible(client) || !user.isRegistered()) {
                    continue;
                }
                
                for (Iterator<IrcChannel> 
                        channelIterator = user.getChannelSetIterator();
                        channelIterator.hasNext();) {
                    IrcChannel ch = channelIterator.next();
                    if (ch.isVisible(user)) {
                        continue userLoop;
                    }
                }
           
                client.send(rplNameReply(
                        client,
                        client.getNickname(),
                        "",
                        "*",
                        "",
                        user.getNickname()));
            }
            client.send(rplEndOfNames(client, "*"));
        }
    }
}
