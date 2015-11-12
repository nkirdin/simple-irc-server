package simpleircserver.parser.commands;
/*
 * 
 * KillIrcCommand 
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
import simpleircserver.talker.user.DriedUser;
import simpleircserver.talker.user.User;

/**
 * KillIrcCommand - класс, который проверяет параметры команды IRC 
 * KILL и исполняет ее. 
 *
 *    <P>Command: KILL
 * <P>Parameters: &lt;nickname&gt; &lt;comment&gt;
 *
 * @version 0.5 2012-02-20
 * @version 0.5.3 2015-11-05 Program units were moved from default package into packages with names. Unit tests were added.
 * @author  Nikolay Kirdin
 */
public class KillIrcCommand extends IrcCommandBase {

    /** Название команды. */    
    public static final String commandName = "KILL";

    /** Параметр &lt;nickname&gt;. */
    private String nickname = null;
    
    /** Параметр &lt;comment&gt;. */
    private String comment = null;

    public KillIrcCommand() {}

    /** 
     * Создатель объекта команды без проверки параметров.
     * @param db репозитарий.
     * @param client источник команды.
     * @param nickname параметр &lt;nickname&gt;.
     * @param comment параметр &lt;comment&gt;.
     * @return объект команды.
     */
    public KillIrcCommand create(DB db,
            User client,
            String nickname,
            String comment) {

        KillIrcCommand killIrcCommand = new KillIrcCommand();
        killIrcCommand.db = db;
        killIrcCommand.client = client;
        killIrcCommand.nickname = nickname;
        killIrcCommand.comment = comment;
        killIrcCommand.setExecutable(true);
        return killIrcCommand;
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

        if (index != pList.size() && !pList.get(index).isEmpty()) {
            comment = check(pList.get(index++), stringPattern);
        } else {
            client.send(errNeedMoreParams(client, commandName));
            return;
        }
        setExecutable(true);
    }

    /** Исполнитель команды. */
    public void run() {
        Reply responseReply = null; 

        if (!isExecutable()) {
            return;
        }

        User user = db.getUser(nickname);

        if (user != null && client != user && (!client.isOperator()
                || user.isOperator())) {
            client.send(errNoPrivileges(client));
            return;
        }

        if (user == null) {
            client.send(errNoSuchNick(client, nickname));
            return;
        }
        
        comment = commandName + " " + nickname + " " + ":" + comment
                + " " + client.getNickname() + "@"
                + client.getIrcServer().getHostname();
               
        user.send(client, comment);

        for (Iterator<IrcChannel> iterator = 
                user.getChannelSetIterator();
            iterator.hasNext();) {
            IrcChannel ch = iterator.next();
            ch.send(client, comment);
            user.remove(ch);
            ch.remove(user);
            if (ch.isUserSetEmpty()) {
                responseReply = db.unRegister(ch);
                if (responseReply == Reply.RPL_OK) {
                    ch.delete();
                }
            }
        }
        
        //XXX It needs to make a small delay before deleting the user. The user should have a opportunity to get the message.
        DriedUser driedUser = new DriedUser(user.getNickname(), 
                user.getUsername(), user.getHostname(), 
                user.getRealname(),    user.getIrcServer().getHostname(), 
                user.getId());
        db.register(driedUser);
        user.close();
    }
}
