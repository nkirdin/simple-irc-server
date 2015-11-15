package simpleircserver.parser.commands;
/*
 * 
 * PrivmsgIrcCommand 
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

import simpleircserver.base.DB;
import simpleircserver.base.Globals;
import simpleircserver.channel.IrcChannel;
import simpleircserver.parser.IrcCommandReport;
import simpleircserver.parser.IrcSyntaxException;
import simpleircserver.parser.Reply;
import simpleircserver.talker.IrcTalker;
import simpleircserver.talker.user.User;

/**
 * PrivmsgIrcCommand - класс, который проверяет параметры команды IRC 
 * PRIVMSG и исполняет ее. 
 *
 *    <P>Command: PRIVMSG
 * <P>Parameters: &lt;receiver&gt;{,&lt;receiver&gt;} 
 * &lt;text to be sent&gt;
 *
 * @version 0.5 2012-02-20
 * @version 0.5.3 2015-11-05 Program units were moved from default package into packages with names. Unit tests were added.
 * @author  Nikolay Kirdin
 */
public class PrivmsgIrcCommand extends IrcCommandBase {

    /** Название команды. */    
    public static final String commandName = "PRIVMSG";

    /** Параметр: &lt;receiver&gt;{,&lt;receiver&gt;}. */
    private LinkedList<String> targetList = null;
    
    /** Параметр: &lt;text to be sent&gt;. */
    private String message = null;

    public PrivmsgIrcCommand() {}

    /** 
     * Создатель объекта команды без проверки параметров.
     * @param db репозитарий.
     * @param client источник команды.
     * @param targetList параметр &lt;receiver&gt;{,&lt;receiver&gt;}.
     * @param message параметр &lt;text to be sent&gt;.
     * @return объект команды.
     */
    public static PrivmsgIrcCommand create(DB db,
            User client,
            LinkedList<String> targetList,
            String message) {

        PrivmsgIrcCommand privmsgIrcCommand = new PrivmsgIrcCommand();
        privmsgIrcCommand.db = db;
        privmsgIrcCommand.client = client;
        privmsgIrcCommand.targetList = targetList;
        privmsgIrcCommand.message = message;
        privmsgIrcCommand.setExecutable(true);
        return privmsgIrcCommand;
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

            targetList = new LinkedList<String>(Arrays.asList(
                        pList.get(index++).split(",")));
            
            for (String s : targetList) {
                check(s, msgToPattern);
            }

            if (index != pList.size()) {
                message = pList.get(index++);
            } else {
                client.send(errNoTextToSend(client));
                return;
            }

        } else {
            client.send(errNoRecipient(client, commandName));
            return;
        }
        setExecutable(true);
    }

    /** Исполнитель команды. */
    public void run() {

        if (!isExecutable()) {
            return;
        }

        if (trailing) {
            message = ":" + message;
        }
                
        for (String target: targetList) {
            if (isIt(target, channelPattern)) {
                IrcChannel ch = db.getChannel(target);

                if (ch == null || !ch.isVisible(client)) {
                    client.send(errNoSuchChannel(client, target));
                } else if (!ch.canReceive(client)) {
                    client.send(errCannotSendToChan(client, target));
                } else {
                    ch.send(client, 
                            commandName + " " + target + " " + message);
                }

            } else if (isIt(target, nickNamePattern)) {
                User user = db.getUser(target);
                boolean sended = true;
                if (user == null || !user.isVisible(client) 
                        || !user.isRegistered()) {
                    client.send(errNoSuchNick(client, target));
                    continue;
                }

                if (user.hasAwayText() && client.getIrcServer() == 
                        Globals.thisIrcServer.get()) {
                    client.send(rplAway(client, target, 
                            user.getAwayText()));
                    continue;
                }

                sended = user.send(client, 
                        ":" + client.getNickname() + " " + 
                        commandName + " " + target + " " + message);
                if (!sended) {
                    String remark = Reply.makeText(
                        Reply. ERR_FILEERROR,
                        "Send to user error",
                        target);
                    client.send(Globals.thisIrcServer.get(), remark);
                }
                
            } else {
                client.send(errUnknownCommand(client, commandName));
                return;
            }
        }
    }
    
    /** 
     * Создает сообщение соответствующее  формализованному сообщению 
     * {@link Reply#RPL_AWAY}. 
     * @param requestor источник команды.
     * @param target клиент с установленным Away текстом.
     * @param text Away текст.
     * @return объект с сообщением.
     */        
    private IrcCommandReport rplAway(IrcTalker requestor, String target, 
            String text) {
        
        String remark = Reply.makeText(Reply.RPL_AWAY, 
                requestor.getNickname(),
                target,
                text);

        return new IrcCommandReport(remark, requestor,
                Globals.thisIrcServer.get());
    }
    
}
