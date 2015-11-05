package org.grass.simpleircserver.parser.commands;
/*
 * 
 * TraceIrcCommand 
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
import org.grass.simpleircserver.parser.IrcSyntaxException;
import org.grass.simpleircserver.talker.IrcTalker;
import org.grass.simpleircserver.talker.user.User;

/**
 * TraceIrcCommand - класс, который проверяет параметры команды IRC 
 * TRACE и исполняет ее. 
 *
 *    <P>Command: TRACE
 * <P>Parameters: [ &lt;target&gt; ]
 *
 * Функционал не реализован.
 *
 * @version 0.5 2012-02-07
 * @version 0.5.3 2015-11-05 Program units were moved from default package into packages with names. Unit tests were added.
 * @author  Nikolay Kirdin
 */
public class TraceIrcCommand extends IrcCommandBase {

    /** Название команды. */    
    public static final String commandName = "TRACE";

    /* * Параметр: &lt;target&gt;. */
    //private String servernameMask = null;

    public TraceIrcCommand() {}

    /** 
     * Создатель объекта команды без проверки параметров.
     * @param db репозитарий.
     * @param client источник команды.
     * @param servernameMask параметр &lt;target&gt;.
     * @return объект команды.
     */
    public static TraceIrcCommand create(DB db, User client, 
            String servernameMask) {
        TraceIrcCommand traceIrcCommand = new TraceIrcCommand();
        traceIrcCommand.db = db;
        traceIrcCommand.client = client;
        //traceIrcCommand.servernameMask = servernameMask;
        traceIrcCommand.setExecutable(false);
        return traceIrcCommand;
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

        if (true /*!client.isOperator()*/) {
            client.send(errNoPrivileges(client));
            return;
        }
        /*
        if (index != pList.size()) {
            servernameMask = check(pList.get(index++), 
            servernameMaskRegex);
        } else {
            servernameMask = "";
        }
        
        setExecutable(false);
        */
    }

    /** Исполнитель команды. */
    public void run() {

        if (!isExecutable()) {
            return;
        }

        //client.send(errUnknownCommand(client, commandName));
    }
}
