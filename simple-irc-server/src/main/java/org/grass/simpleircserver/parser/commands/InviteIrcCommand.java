package org.grass.simpleircserver.parser.commands;
/*
 * 
 * InviteIrcCommand 
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
import java.util.logging.*;

import org.grass.simpleircserver.base.DB;
import org.grass.simpleircserver.base.Globals;
import org.grass.simpleircserver.channel.IrcChannel;
import org.grass.simpleircserver.parser.IrcCommandReport;
import org.grass.simpleircserver.parser.IrcSyntaxException;
import org.grass.simpleircserver.parser.Reply;
import org.grass.simpleircserver.talker.IrcTalker;
import org.grass.simpleircserver.talker.user.User;

/**
 * InviteIrcCommand - класс, который проверяет параметры команды IRC 
 * INVITE и исполняет ее. 
 *
 *     <P>Command: INVITE
 *  <P>Parameters: &lt;nickname&gt; &lt;channel&gt;
 *
 * @version 0.5 2012-02-21
 * @version 0.5.3 2015-11-05 Program units were moved from default package into packages with names. Unit tests were added.
 * @author  Nikolay Kirdin
 */
public class InviteIrcCommand extends IrcCommandBase {

    /** Название команды. */    
    public static final String commandName = "INVITE";

    /**Параметр: &lt;nickname&gt;. */
    private String nickname = null;

    /**Параметр: &lt;channel&gt;. */
    private String channelName = null;
    
    public InviteIrcCommand() {}

    /** 
     * Создатель объекта команды без проверки параметров.
     * @param db репозитарий.
     * @param client источник команды.
     * @param nickname параметр &lt;nickname&gt;.
     * @param channelName параметр &lt;channel&gt;.
     * @return объект команды.
     */
    public static InviteIrcCommand create(DB db,
            User client,
            String nickname,
            String channelName) {

        InviteIrcCommand inviteIrcCommand = new InviteIrcCommand();
        inviteIrcCommand.db = db;
        inviteIrcCommand.client = client;
        inviteIrcCommand.nickname = nickname;
        inviteIrcCommand.channelName = channelName;
        inviteIrcCommand.setExecutable(true);
        return inviteIrcCommand;
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

        nickname = check(pList.get(index++), nickNamePattern);
        channelName = check(pList.get(index++), channelPattern);

        setExecutable(true);
    }

    /** Исполнитель команды. */
    public void run() {
        Reply responseReply = null;

        if (!isExecutable()) {
            return;
        }

        User user = db.getUser(nickname);
        IrcChannel ch = db.getChannel(channelName);

        if (ch == null) {
            client.send(errNoSuchChannel(client, channelName));
            return;
        }

        if (!client.isMember(ch) && !client.isOperator()) {
            client.send(errNotOnChannel(client, channelName));        
            return;
        }

        if (ch.isInviteOnly() && !client.isOperator()) {
            if (ch.checkChannelOperator(client)) {
                if (client.isRestricted()) {
                    client.send(errUserRestricted(client));        
                    return;
                }
            } else {
                client.send(errNotChannelOp(client, channelName));        
                return;
            }
        }

        if (user == null || !user.isRegistered()) {
            client.send(errNoSuchNick(client, nickname));
            return;
        }

        if (user.isMember(ch)) {
            client.send(errOnChannel(client, nickname, channelName));
            return;
        }

        responseReply = ch.add(user, null);
        if (responseReply != Reply.RPL_OK) {
            String remark = null;
            if (responseReply != Reply.ERR_BADCHANNELKEY) {
            remark = Reply.makeText(responseReply, 
                    client.getNickname(), ch.getNickname());
            } else if (responseReply != Reply.ERR_CHANNELISFULL){
            remark = Reply.makeText(responseReply, 
                    client.getNickname(), ch.getNickname());
            } else {
                remark = "WRONG Reply:" + responseReply +
                " Channel: " + ch.getNickname() +
                " User: " + client.getNickname();
                Globals.logger.get().log(Level.SEVERE, remark);
                remark = Reply.makeText(Reply.ERR_FILEERROR, 
                    "WRONG Reply:" + responseReply, 
                    client.getNickname() + " " + ch.getNickname());
            }
            client.send(Globals.thisIrcServer.get(), remark);
            return;
        }

        responseReply = user.add(ch);
        if (responseReply != Reply.RPL_OK) {
            ch.remove(user);
            if (ch.isUserSetEmpty()) {
                responseReply = db.unRegister(ch);
                if (responseReply == Reply.RPL_OK) {
                    ch.delete();
                } else {
                    throw new Error("INVITE: db.unRegister(channel)"
                            + " Internal error");
                }
            }
            client.send(errTooManyChannels(client, channelName));
            return;
        }
        client.send(rplInviting(client, channelName, nickname));
        user.send(rplInviting(client, channelName, nickname));
    }
    
    /**
     * Создание формализованного ответа типа
     * {@link Reply#RPL_INVITING}.
     * @param ircTalker отправитель.
     * @return объект класса {@link IrcCommandReport} с формализованным 
     * ответом.
     */
    private IrcCommandReport rplInviting(IrcTalker ircTalker, 
            String channelName, String nickname) {
        String remark = Reply.makeText(Reply.RPL_INVITING,  
                client.getNickname(), channelName, nickname);
        
        return new IrcCommandReport(remark, ircTalker,
                Globals.thisIrcServer.get());
    }
}
