package simpleircserver.parser.commands;
/*
 * 
 * OperIrcCommand 
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
import simpleircserver.base.Globals;
import simpleircserver.parser.IrcCommandReport;
import simpleircserver.parser.IrcSyntaxException;
import simpleircserver.parser.Reply;
import simpleircserver.talker.IrcTalker;
import simpleircserver.talker.user.User;

/**
 * OperIrcCommand - класс, который проверяет параметры команды IRC 
 * OPER и исполняет ее.
 *
 *    <P>Command: OPER
 * <P>Parameters: &lt;username&gt; &lt;password&gt;
 *
 * @version 0.5 2012-02-07
 * @version 0.5.3 2015-11-05 Program units were moved from default package into packages with names. Unit tests were added.
 * @author  Nikolay Kirdin
 */
public class OperIrcCommand extends IrcCommandBase {

    /** Название команды. */    
    public static final String commandName = "OPER";

    /** Параметр: &lt;username&gt;. */
    private String username = null;
    
    /** Параметр: &lt;password&gt;. */
    private String password = null;

    public OperIrcCommand() {}

    /** 
     * Создатель объекта команды без проверки параметров.
     * @param db репозитарий.
     * @param client источник команды.
     * @param username параметр &lt;username&gt;.
     * @param password параметр &lt;password&gt;.
     * @return объект команды.
     */
    public static OperIrcCommand create(DB db,
            User client,
            String username,
            String password) {

        OperIrcCommand operIrcCommand = new OperIrcCommand();
        operIrcCommand.db = db;
        operIrcCommand.client = client;
        operIrcCommand.username = username;
        operIrcCommand.password = password;
        operIrcCommand.setExecutable(true);
        return operIrcCommand;
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

        username = check(pList.get(index++), nickNameRegex);
        password = check(pList.get(index++), keyRegex);

        setExecutable(true);
    }

    /** Исполнитель команды. */
    public void run() {
        
        if (!isExecutable()) {
            return;
        }
        if (db.checkOperator(username, password)) {
            client.setOperator();
            client.send(rplYoureOper(client));
        } else {
            client.send(errPasswdMismatch(client));
        }
    }
    
    /** 
     * Создает сообщение соответствующее  формализованному сообщению 
     * {@link Reply#RPL_YOUREOPER}. 
     * @param requestor источник команды.
     * @return объект с сообщением.
     */        
    private IrcCommandReport rplYoureOper(IrcTalker requestor) {
        
        String remark = Reply.makeText(Reply.RPL_YOUREOPER,
                    requestor.getNickname());

        return new IrcCommandReport(remark, requestor,
                Globals.thisIrcServer.get());
    }
    
    /** 
     * Создает сообщение соответствующее  формализованному сообщению 
     * {@link Reply#ERR_PASSWDMISMATCH}. 
     * @param requestor источник команды.
     * @return объект с сообщением.
     */        
    private IrcCommandReport errPasswdMismatch(IrcTalker requestor) {
        
        String remark = Reply.makeText(
                    Reply.ERR_PASSWDMISMATCH,
                    requestor.getNickname());

        return new IrcCommandReport(remark, requestor,
                Globals.thisIrcServer.get());
    }
    
}
