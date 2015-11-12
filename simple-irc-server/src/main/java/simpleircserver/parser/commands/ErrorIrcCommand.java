package simpleircserver.parser.commands;
/*
 * 
 * ErrorIrcCommand 
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

import simpleircserver.base.DB;
import simpleircserver.base.Globals;
import simpleircserver.parser.IrcSyntaxException;
import simpleircserver.talker.IrcTalker;
import simpleircserver.talker.server.IrcServer;

/**
 * ErrorIrcCommand - класс, который проверяет параметры команды IRC 
 * ERROR и исполняет ее. 
 *
 *    <P>Command: ERROR
 * <P>Parameters: &lt;error message&gt;
 *
 * @version 0.5 2012-02-07
 * @version 0.5.3 2015-11-05 Program units were moved from default package into packages with names. Unit tests were added.
 * @author  Nikolay Kirdin
 */
public class ErrorIrcCommand extends IrcCommandBase {

    /** Название команды. */    
    public static final String commandName = "ERROR";

    /** Отправитель сообщения. */
    private IrcTalker requestor = null;
    
    /** Параметр: &lt;error message&gt;. */
    private String message = null;

    public ErrorIrcCommand() {}

    /** 
     * Создатель объекта команды без проверки параметров.
     * @param db репозитарий.
     * @param requestor источник команды.
     * @param message параметр &lt;error message&gt;.
     * @return объект команды.
     */    
    public static ErrorIrcCommand create(DB db,
            IrcTalker requestor,
            String message) {

        ErrorIrcCommand errorIrcCommand = new ErrorIrcCommand();
        errorIrcCommand.db = db;
        errorIrcCommand.requestor = requestor;
        errorIrcCommand.message = message;
        errorIrcCommand.setExecutable(false);
        return errorIrcCommand;
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
        this.requestor = requestor;        

        if (!(requestor instanceof IrcServer)) {
            requestor.send(errUnknownCommand(requestor, commandName));
            return;
        } 
        if (!requestor.isRegistered()) {
            requestor.send(errNotRegistered(requestor));
            return;
        } 
        try {
            message = check(pList.get(index++), stringRegex);
            setExecutable(true);
        } catch (IrcSyntaxException e) {
            requestor.send(errUnknownCommand(requestor, commandName));
        } catch (IndexOutOfBoundsException e) {
            requestor.send(errNeedMoreParams(requestor, commandName));
        }
    }

    /** Исполнитель команды. */
    public void run() {
        String content = null;
        if (!isExecutable()) {
            return;
        }
         
        content = ":" + requestor.getNickname() + " " + 
                commandName + " " + message;
        Globals.logger.get().log(Level.WARNING, content);
        WallopsIrcCommand.create(db,  Globals.thisIrcServer.get(),
                content).run();
        requestor.close();
    }
}
