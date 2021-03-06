package simpleircserver.parser.commands;
/*
 * 
 * PassIrcCommand 
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
import simpleircserver.parser.IrcSyntaxException;
import simpleircserver.talker.IrcTalker;
import simpleircserver.talker.server.IrcServer;
import simpleircserver.talker.service.Service;
import simpleircserver.talker.user.User;

/**
 * PassIrcCommand - класс, который проверяет параметры команды IRC 
 * PASS и исполняет ее.
 *
 *    <P>Command: PASS
 * <P>Parameters: &lt;password&gt;
 *
 * Функционал не реализован.
 *
 * @version 0.5 2012-02-07
 * @version 0.5.3 2015-11-05 Program units were moved from default package into packages with names. Unit tests were added.
 * @author  Nikolay Kirdin
 */
public class PassIrcCommand extends IrcCommandBase {

    /** Название команды. */    
    public static final String commandName = "PASS";
    
    /** Параметр: &lt;password&gt;. */
    private String password;
    
    /** Источник команды. */
    private IrcTalker requestor;

    public PassIrcCommand() {}

    /** 
     * Создатель объекта команды без проверки параметров.
     * @param db репозитарий.
     * @param requestor источник команды.
     * @param password параметр &lt;password&gt;.
     * @return объект команды.
     */
    public static PassIrcCommand create(DB db,
            IrcTalker requestor,
            String password) {

        PassIrcCommand passIrcCommand = new PassIrcCommand();
        passIrcCommand.db = db;
        passIrcCommand.requestor = requestor;
        passIrcCommand.password = password;
        passIrcCommand.setExecutable(true);
        return passIrcCommand;
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
        this.requestor = requestor;
        this.db = db;
        this.pList = pList;
        this.trailing = trailing;

        if (!(requestor instanceof User)
            && !(requestor instanceof IrcServer)
            && !(requestor instanceof Service)) {

            requestor.send(errUnknownCommand(requestor, commandName));
            return;
        }

        password = check(pList.get(index++), keyRegex);

        setExecutable(true);
    }

    /** Исполнитель команды. */
    public void run() {

        if (!isExecutable()) {
            return;
        }

        requestor.checkPassword(password);

        //requestor.send(requestor, commandName);
        
    }
}
