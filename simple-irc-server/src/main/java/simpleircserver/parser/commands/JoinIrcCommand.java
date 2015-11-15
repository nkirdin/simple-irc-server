package simpleircserver.parser.commands;
/*
 * 
 * JoinIrcCommand 
 * is part of Simple Irc Server
 *
 *
 * Copyright (С) 2012, 2015, Nikolay Kirdin
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
import java.util.logging.*;

import simpleircserver.base.DB;
import simpleircserver.base.Globals;
import simpleircserver.channel.ChannelMode;
import simpleircserver.channel.IrcChannel;
import simpleircserver.parser.IrcCommandReport;
import simpleircserver.parser.IrcSyntaxException;
import simpleircserver.parser.Reply;
import simpleircserver.talker.IrcTalker;
import simpleircserver.talker.user.User;

/**
 * JoinIrcCommand - класс, который проверяет параметры команды IRC 
 * JOIN и исполняет ее. 
 *
 *    <P>Command: JOIN
 * <P>Parameters: &lt;channel&gt;{,&lt;channel&gt;}
 *  [&lt;key&gt;{,&lt;key&gt;}]
 *
 * @version 0.5 2012-02-20
 * @version 0.5.3 2015-11-05 Program units were moved from default package into packages with names. Unit tests were added.
 * @author  Nikolay Kirdin
 */
public class JoinIrcCommand extends IrcCommandBase {

    /** Название команды. */    
    public static final String commandName = "JOIN";

    /** Параметр: &lt;channel&gt;{,&lt;channel&gt;}. */
    private LinkedList<String> channelList = null;
    
    /** Параметр: &lt;key&gt;{,&lt;key&gt;}. */
    private LinkedList<String> keyList = null;

    public JoinIrcCommand() {}

    /** 
     * Создатель объекта команды без проверки параметров.
     * @param db репозитарий.
     * @param client источник команды.
     * @param channelList параметр &lt;channel&gt;{,&lt;channel&gt;}.
     * @param keyList параметр &lt;key&gt;{,&lt;key&gt;}.
     * @return объект команды.
     */
    public JoinIrcCommand create(DB db,
            User client,
            LinkedList<String> channelList,
            LinkedList<String> keyList) {

        JoinIrcCommand joinIrcCommand = new JoinIrcCommand();
        joinIrcCommand.db = db;
        joinIrcCommand.client = client;
        joinIrcCommand.channelList = channelList;
        joinIrcCommand.keyList = keyList;
        joinIrcCommand.setExecutable(true);
        return joinIrcCommand;
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


        if (pList.get(index).matches("0")) {}
        else {
            channelList = new LinkedList<String>(Arrays.asList(
                    pList.get(index++).split(",")));
            for (String item : channelList) {
                check(item, channelPattern);
            }
            
            if (index != pList.size()) {
                keyList = new LinkedList<String>(Arrays.asList(
                        pList.get(index++).split(",")));
                for (String item : keyList) {
                    check(item, keyPattern);
                }
                
            } else {
                keyList = new LinkedList<String>();
            }
        }

        setExecutable(true);
    }

    /** Исполнитель команды. */
    public void run() {
        Reply responseReply = null;

        if (!isExecutable()) {
            return;
        }

        if (channelList == null) {
            // JOIN 0
            PartIrcCommand.create(db, client).run();

        } else {
            for (String s : channelList) {
                IrcChannel ch = db.getChannel(s);

                if (ch != null) {

                    if (!ch.isVisible(client)) {
                        client.send(errNoSuchChannel(client, s));
                        continue;
                    }

                    if (ch.checkMember(client)) {
                        continue;
                    }

                    if (ch.isInviteOnly() && !ch.checkInvited(client)) {
                        client.send(errInviteOnly(client, s));
                        continue;
                    }

                    if (ch.checkBanned(client) && 
                            !ch.checkExcepted(client)) {
                        client.send(errBannedFromChan(client, s));
                        continue;
                    }

                    responseReply = ch.add(client, keyList.isEmpty()
                            ? null : keyList.poll());
                    if (responseReply != Reply.RPL_OK) {
                        IrcCommandReport ircCommandReport = null;
                        if (responseReply == 
                                Reply.ERR_BADCHANNELKEY) {
                            ircCommandReport = errBadChannelKey(client, 
                                    ch.getNickname());
                        } else if (responseReply == 
                                Reply.ERR_CHANNELISFULL) {
                            ircCommandReport = errChannelListFull(client, 
                                    ch.getNickname());
                        } else {
                            String remark = "WRONG Reply:" + 
                                    responseReply +
                                    " Channel: " + ch.getNickname() +
                                    " User: " + client.getNickname();
                            Globals.logger.get().log(
                                    Level.SEVERE, remark);
                            ircCommandReport = errFileError(client, 
                                    "WRONG Reply:" + 
                                    responseReply, 
                                    client.getNickname() + " " + 
                                    ch.getNickname());
                        }
                        
                        client.send(ircCommandReport);
                        continue;
                    }

                    responseReply = client.add(ch);
                    if (responseReply != Reply.RPL_OK) {
                        client.send(errTooManyChannels(client, s));

                        ch.remove(client);
                        if (ch.isUserSetEmpty()) {
                            responseReply = db.unRegister(ch);
                            if (responseReply == Reply.RPL_OK) {
                                ch.delete();
                            } else {
                                throw new Error(
                                        "JOIN: db.unRegister(client)" +
                                        " Internal error");
                            }
                        }
                        continue;
                    }
                } else {
                    ch = IrcChannel.create(s, "");
                    
                    if (ch == null) {
                        IrcCommandReport ircCommandReport = null;
                        long freeMemory = 
                            Runtime.getRuntime().freeMemory();
                        ircCommandReport = errFileError(client,
                            commandName, "MEMORY");
                        client.send(ircCommandReport);
                        
                        Globals.logger.get().log(Level.SEVERE, 
                                "Insufficient free memory(B)" + 
                                freeMemory);
                        continue;
                    }
                    responseReply = db.register(ch);

                    if (responseReply != Reply.RPL_OK) {
                        IrcCommandReport ircCommandReport = null;
                        String remark = null;
                        if (responseReply != 
                                Reply.ERR_NICKNAMEINUSE) {
                            ircCommandReport = errNicknameInUse(client,
                                    ch.getNickname());
                        } else if (responseReply != 
                                Reply.ERR_FILEERROR) {
                            ircCommandReport = errFileError(client,
                                    "Maximum channel number was reached.", 
                                    ch.getNickname());
                        } else {
                            remark = "WRONG Reply:" + 
                                    responseReply +
                                    " Channel: " + ch.getNickname() +
                                    " User: " + client.getNickname();
                            Globals.logger.get().log(
                                    Level.SEVERE, remark);
                            ircCommandReport = errFileError( client,
                                    "WRONG Reply:" + 
                                    responseReply, 
                                    client.getNickname() + " " + 
                                    ch.getNickname());
                        }
                        client.send(ircCommandReport);
                        continue;
                    }
                    responseReply = ch.add(client, null);
                    if (responseReply != Reply.RPL_OK) {
                        IrcCommandReport ircCommandReport = null;
                        if (responseReply == 
                                Reply.ERR_BADCHANNELKEY) {
                            ircCommandReport = errBadChannelKey(client, 
                                    ch.getNickname());
                        } else if (responseReply == 
                                Reply.ERR_CHANNELISFULL) {
                            ircCommandReport = errChannelListFull(client, 
                                    ch.getNickname());
                        } else {
                            String remark = "WRONG Reply:" + 
                                    responseReply +
                                    " Channel: " + ch.getNickname() +
                                    " User: " + client.getNickname();
                            Globals.logger.get().log(
                                    Level.SEVERE, remark);
                            ircCommandReport = errFileError(client, 
                                    "WRONG Reply:" + 
                                    responseReply, 
                                    client.getNickname() + " " + 
                                    ch.getNickname());
                        }
                        ch.delete();
                        client.send(ircCommandReport);
                        continue;
                    }
                        
                    ch.setCreator(client);
                    ch.setChannelOperator(client);
                    responseReply = client.add(ch);
                    if (responseReply != Reply.RPL_OK) {
                        client.send(errTooManyChannels(client, s));

                        ch.remove(client);
                        if (ch.isUserSetEmpty()) {
                            responseReply = db.unRegister(ch);
                            if (responseReply == Reply.RPL_OK) {
                                ch.delete();
                            } else {
                                throw new Error(
                                        "JOIN: db.unRegister(client)" +
                                        " Internal error");
                            }
                        }
                        continue;
                    }

                }
                
                client.send(client, commandName + " " + s);

                ch.send(client, commandName + " " + s);
                
                // Send TOPIC
                String topic = ch.getTopic();
                if (topic == null || topic.isEmpty()) {
                    client.send(rplNoTopic(client, ch));
                } else {
                    client.send(rplTopic(client, ch, topic));
                }

                // Send NAMES
                if (!ch.isAnonymous()) {
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
                    
                       client.send(rplNameReply(client,
                                user.getNickname(),
                                ch.getStatus(),
                                ch.getNickname(),
                                getUserStatus(channelMode),
                                user.getNickname()));
                    }
                }
                client.send(rplEndOfNames(client, ch.getNickname()));
            }            
        }
    }
        
    /** 
     * Создает сообщение соответствующее  формализованному сообщению 
     * {@link Reply#ERR_INVITEONLYCHAN}. 
     * @param requestor источник команды.
     * @param channelName имя канала.
     * @return объект с сообщением.
     */        
    private IrcCommandReport errInviteOnly(IrcTalker requestor,
            String channelName) {
        
        String remark = Reply.makeText(
                Reply.ERR_INVITEONLYCHAN, 
                requestor.getNickname(),
                channelName);

        return new IrcCommandReport(remark, requestor,
                Globals.thisIrcServer.get());
    }
    
    /** 
     * Создает сообщение соответствующее  формализованному сообщению 
     * {@link Reply#ERR_BANNEDFROMCHAN}. 
     * @param requestor источник команды.
     * @param channelName имя канала.
     * @return объект с сообщением.
     */        
    private IrcCommandReport errBannedFromChan(IrcTalker requestor,
            String channelName) {
        
        String remark = Reply.makeText(
                Reply.ERR_BANNEDFROMCHAN, 
                requestor.getNickname(),
                channelName);

        return new IrcCommandReport(remark, requestor,
                Globals.thisIrcServer.get());
    }
    
    /** 
     * Создает текст сообщения соответствующие формализованным 
     * сообщениям {@link Reply#ERR_BADCHANNELKEY}
     * @param requestor источник команды.
     * @param channelName имя канала.
     * @return объект с сообщением.
     */
     public IrcCommandReport errBadChannelKey(IrcTalker requestor,
            String channelName) {
        
        String remark = Reply.makeText(
                Reply.ERR_BADCHANNELKEY, 
                requestor.getNickname(), channelName);
        return new IrcCommandReport(remark, requestor,
                Globals.thisIrcServer.get());
    }
    
    /** 
     * Создает текст сообщения соответствующие формализованным 
     * сообщениям {@link Reply#ERR_BADCHANNELKEY}
     * @param requestor источник команды.
     * @param channelName имя канала.
     * @return объект с сообщением.
     */
     public IrcCommandReport errChannelListFull(IrcTalker requestor,
            String channelName) {
        
        String remark = Reply.makeText(
                Reply.ERR_CHANNELISFULL, 
                requestor.getNickname(), channelName);
        
        return new IrcCommandReport(remark, requestor,
                Globals.thisIrcServer.get());
    }

}
