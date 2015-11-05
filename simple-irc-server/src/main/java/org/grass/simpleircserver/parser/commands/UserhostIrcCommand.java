package org.grass.simpleircserver.parser.commands;
/*
 * 
 * UserhostIrcCommand 
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

import org.grass.simpleircserver.base.Constants;
import org.grass.simpleircserver.base.DB;
import org.grass.simpleircserver.base.Globals;
import org.grass.simpleircserver.parser.IrcCommandReport;
import org.grass.simpleircserver.parser.IrcSyntaxException;
import org.grass.simpleircserver.parser.Reply;
import org.grass.simpleircserver.talker.IrcTalker;
import org.grass.simpleircserver.talker.user.User;

/**
 * UserhostIrcCommand - класс, который проверяет параметры команды IRC 
 * USERHOST и исполняет ее. 
 *
 *    <P>Command: USERHOST
 * <P>Parameters: nickname{&lt;space&gt;&lt;nickname&gt;}
 *
 * @version 0.5 2012-02-07
 * @version 0.5.3 2015-11-05 Program units were moved from default package into packages with names. Unit tests were added.
 * @author  Nikolay Kirdin
 */
public class UserhostIrcCommand extends IrcCommandBase {

    /** Название команды. */    
    public static final String commandName = "USERHOST";

    /** Параметр: nickname{&lt;space&gt;&lt;nickname&gt;}. */
    private LinkedList<String> nicknameList = new LinkedList<String>();

    public UserhostIrcCommand() {}

    /** 
     * Создатель объекта команды без проверки параметров.
     * @param db репозитарий.
     * @param client источник команды.
     * @param nicknameList параметр 
     * nickname{&lt;space&gt;&lt;nickname&gt;}.
     * @return объект команды.
     */
    public static UserhostIrcCommand create(DB db,
            User client,
            LinkedList<String> nicknameList) {
        UserhostIrcCommand userhostIrcCommand = new UserhostIrcCommand();
        userhostIrcCommand.db = db;
        userhostIrcCommand.client = client;
        userhostIrcCommand.nicknameList = nicknameList;
        userhostIrcCommand.setExecutable(true);
        return userhostIrcCommand;
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

        int itemCount = 0;


        while (index != pList.size()) {
            if (itemCount++ == Constants.MAX_USERHOST_LIST_SIZE) {
                break;
            }
            nicknameList.offer(check(pList.get(index++), 
                    nickNameRegex));
        }

        if (nicknameList.isEmpty()) {
            client.send(errNeedMoreParams(client, commandName));
            return;
        }

        setExecutable(true);
    }

    /** Исполнитель команды. */
    public void run() {

        if (!isExecutable()) {
            return;
        }

        LinkedHashSet<User> outputUserSet = new LinkedHashSet<User>();
        for (String userNickname : nicknameList) {
            User user = db.getUser(userNickname);
            if (user != null && user.isVisible(client) 
                    && user.isRegistered()) {
                outputUserSet.add(user);
            }
        }

        String outputString = "";
        for (User user : outputUserSet) {
            outputString = outputString + userHostString(user) + " ";
        }

        if (outputString.endsWith(" ")) {
            outputString = outputString.substring(0, 
                    outputString.length() - 1);
        }

        client.send(rplUserhost(client, outputString));
    }
    
    private String userHostString(User user) {
        String result = user.getNickname();
        if (user.isOperator()) {
            result = result + "*";
        }
        result = result + "=";
        if (user.hasAwayText()) {
            result = result + "-";
        } else {
            result = result + "+";
        }
        return result + user.getHostname();
    }
    
    /**
     * Создание формализованного ответа типа
     * {@link Reply#RPL_USERHOST}.
     * @param ircTalker отправитель.
     * @param outputString 
     * @return объект класса {@link IrcCommandReport} с формализованным 
     * ответом.
     */
    private IrcCommandReport rplUserhost(IrcTalker ircTalker, 
            String outputString) {
        String remark = Reply.makeText(Reply.RPL_USERHOST,
                ircTalker.getNickname(),
                outputString);
        return new IrcCommandReport(remark, ircTalker,
                Globals.thisIrcServer.get());
    }
}
