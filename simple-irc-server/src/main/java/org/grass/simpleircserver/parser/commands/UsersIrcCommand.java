package org.grass.simpleircserver.parser.commands;
/*
 * 
 * UsersIrcCommand 
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
import org.grass.simpleircserver.base.Globals;
import org.grass.simpleircserver.parser.IrcCommandReport;
import org.grass.simpleircserver.parser.IrcSyntaxException;
import org.grass.simpleircserver.parser.Reply;
import org.grass.simpleircserver.talker.IrcTalker;
import org.grass.simpleircserver.talker.user.User;

/**
 * UsersIrcCommand - класс, который проверяет параметры команды IRC 
 * USERS и исполняет ее. 
 *
 *    <P>Command: USERS
 * <P>Parameters: [&lt;server&gt;]
 *
 * @version 0.5 2012-02-07
 * @version 0.5.3 2015-11-05 Program units were moved from default package into packages with names. Unit tests were added.
 * @author  Nikolay Kirdin
 */
public class UsersIrcCommand extends IrcCommandBase {

    /** Название команды. */    
    public static final String commandName = "USERS";
    /** Источник команды. */
    private IrcTalker client = null;

    public UsersIrcCommand() {}

    /** 
     * Создатель объекта команды без проверки параметров.
     * @param db репозитарий.
     * @param client источник команды.
     * @return объект команды.
     */
    public static UsersIrcCommand create(DB db, User client) {
        UsersIrcCommand usersIrcCommand = new UsersIrcCommand();
        usersIrcCommand.db = db;
        usersIrcCommand.client = client;
        return usersIrcCommand;
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

        //int index = 0;
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

        setExecutable(true);
    }

    /** Исполнитель команды. */
    public void run() {

        if (!isExecutable()) {
            return;
        }
        client.send(errUsersDisabled(client));
    }
    
    /**
     * Создание формализованного ответа типа
     * {@link Reply#ERR_USERSDISABLED}.
     * @param ircTalker отправитель.
     * @return объект класса {@link IrcCommandReport} с формализованным 
     * ответом.
     */
    private IrcCommandReport errUsersDisabled(IrcTalker ircTalker) {
        String remark = Reply.makeText(
                Reply.ERR_USERSDISABLED,
                ircTalker.getNickname());
        return new IrcCommandReport(remark, ircTalker,
                Globals.thisIrcServer.get());
    }
}
