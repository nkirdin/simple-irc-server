package simpleircserver.parser.commands;
/*
 * 
 * KickIrcCommand 
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

import simpleircserver.base.DB;
import simpleircserver.channel.IrcChannel;
import simpleircserver.parser.IrcSyntaxException;
import simpleircserver.parser.Reply;
import simpleircserver.talker.IrcTalker;
import simpleircserver.talker.user.User;

/**
 * KickIrcCommand - класс, который проверяет параметры команды IRC 
 * KICK и исполняет ее. 
 *
 *    <P>Command: KICK
 * <P>Parameters: &lt;channel&gt; &lt;user&gt; [&lt;comment&gt;]
 *
 * @version 0.5 2012-02-20
 * @version 0.5.3 2015-11-05 Program units were moved from default package into packages with names. Unit tests were added.
 * @author  Nikolay Kirdin
 */
public class KickIrcCommand extends IrcCommandBase {

    /** Название команды. */    
    public static final String commandName = "KICK";
    
    /** Параметр: &lt;channel&gt;. */
    private LinkedList<String> channelList = null;
    
    /** Параметр: &lt;user&gt;. */
    private LinkedList<String> userList = null;
    
    /** Параметр: &lt;comment&gt;. */
    private String comment = null;

    public KickIrcCommand() {}

    /** 
     * Создатель объекта команды без проверки параметров.
     * @param db репозитарий.
     * @param client источник команды.
     * @param channelList параметр  &lt;channel&gt;.
     * @param userList параметр &lt;user&gt;.
     * @param comment параметр &lt;comment&gt;.
     * @return объект команды.
     */
    public KickIrcCommand create(DB db,
            User client,
            LinkedList<String> channelList,
            LinkedList<String> userList,
            String comment) {

        KickIrcCommand kickIrcCommand = new KickIrcCommand();
        kickIrcCommand.db = db;
        kickIrcCommand.client = client;
        kickIrcCommand.channelList = channelList;
        kickIrcCommand.userList = userList;
        kickIrcCommand.comment = comment;
        kickIrcCommand.setExecutable(true);
        return kickIrcCommand;
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

        if (client.isRestricted()) {
            client.send(errUserRestricted(client));
            return;
        }

        channelList = new LinkedList<String>(Arrays.asList(
                pList.get(index++).split(",")));
        for (String item : channelList) {
            check(item, channelPattern);
        } 

        userList = new LinkedList<String>(Arrays.asList(
                pList.get(index++).split(",")));
        for (String item : userList) {
            check(item, nickNamePattern);
        } 

        if (!(channelList.size() == 1 || channelList.size() == 
                userList.size())) {
            requestor.send(errUnknownCommand(requestor, commandName));
            return;
        }

        if (index != pList.size()) {
            comment = " " + ":" + check(pList.get(index++), stringPattern);
        } else {
            comment = "";
        }

        setExecutable(true);
    }

    /** Исполнитель команды. */
    public void run() {
        Reply responseReply = null;

        if (!isExecutable()) {
            return;
        }

        if (channelList.size() == 1 ) {
            String channelName = channelList.poll();
            IrcChannel ch = db.getChannel(channelName);

            if (ch == null) {
                client.send(errNoSuchChannel(client, channelName));
                return;
            }

            if (!client.isOperator()) {
                if (!client.isMember(ch)) {
                    client.send(errNotOnChannel(client, channelName));
                    return;
                }

                if (!ch.checkChannelOperator(client)) {
                    client.send(errNotChannelOp(client, channelName));
                    return;
                }
            }

            for (String s : userList) {
                User user = db.getUser(s);

                if (user == null || !user.isRegistered() ) {
                    client.send(errNoSuchNick(client, s));
                    return;
                }

                if (!user.isMember(ch)) {
                    client.send(errNotInChannel(client, s, channelName));
                    continue;
                }

                if (user.isOperator() && ch.checkChannelOperator(user)) {
                    client.send(errFileError(client, "kick " + s, 
                            channelName));
                    continue;
                }
                String message = commandName + " " + 
                        channelName + " " + s + comment;
                client.send(client, message);

                
                ch.send(client,    message);
                
                user.remove(ch);
                ch.remove(user);
                if (ch.isUserSetEmpty()) {
                    responseReply = db.unRegister(ch);
                    if (responseReply == Reply.RPL_OK) {
                        ch.delete();
                    }
                }
            }
        } else {
            for (String channelName : channelList) {
                IrcChannel ch = db.getChannel(channelName);

                if (ch == null) {
                    client.send(errNoSuchChannel(client, channelName));
                    return;
                }

                if (!client.isOperator() && 
                            !ch.checkChannelOperator(client)) {
                    client.send(errNotChannelOp(client, channelName));
                    return;
                }

                String userNickname = userList.poll();

                User user = db.getUser(userNickname);

                if (user == null || !user.isRegistered() ) {
                    client.send(errNoSuchNick(client, userNickname));
                    return;
                }

                if (!user.isMember(ch)) {
                    client.send(errNotInChannel(client, userNickname,
                            channelName));
                    continue;
                }

                if (user.isOperator() && ch.checkChannelOperator(user)) {
                    
                    client.send(errFileError(client, "kick " + 
                            userNickname, channelName));
                    continue;
                }

                String message = commandName + " " + 
                        channelName + " " + userNickname + comment;
                
                client.send(client, message);
                ch.send(client, message);
                
                user.remove(ch);
                ch.remove(user);
                if (ch.isUserSetEmpty()) {
                    responseReply = db.unRegister(ch);
                    if (responseReply == Reply.RPL_OK) {
                        ch.delete();
                    }
                }
            }
        }
    }
}
