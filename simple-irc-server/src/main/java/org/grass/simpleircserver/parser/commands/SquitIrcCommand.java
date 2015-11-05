package org.grass.simpleircserver.parser.commands;
/*
 * 
 * SquitIrcCommand 
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
import org.grass.simpleircserver.parser.IrcSyntaxException;
import org.grass.simpleircserver.talker.IrcTalker;
import org.grass.simpleircserver.talker.server.IrcServer;
import org.grass.simpleircserver.talker.user.User;

/**
 * SquitIrcCommand - класс, который проверяет параметры команды IRC 
 * SQUIT и исполняет ее. 
 *
 *    <P>Command: SQUIT
 * <P>Parameters: &lt;server&gt; &lt;comment&gt;
 *
 * @version 0.5 2012-02-07
 * @version 0.5.3 2015-11-05 Program units were moved from default package into packages with names. Unit tests were added.
 * @author  Nikolay Kirdin
 */
public class SquitIrcCommand extends IrcCommandBase {

    /** Название команды. */    
    public static final String commandName = "SQUIT";
    
    /** Параметр: &lt;server&gt;. */
    private String servername = null;
    
    /** Параметр: &lt;comment&gt;. */
    private String comment = null;
    
    public SquitIrcCommand() {}

    /** 
     * Создатель объекта команды без проверки параметров.
     * @param db репозитарий.
     * @param client источник команды.
     * @param servername параметр &lt;server&gt;.
     * @param comment параметр &lt;comment&gt;.
     * @return объект команды.
     */
    public static SquitIrcCommand create(DB db, User client, 
            String servername, String comment) {
        SquitIrcCommand squitIrcCommand = new SquitIrcCommand();
        squitIrcCommand.db = db;
        squitIrcCommand.client = client;
        squitIrcCommand.servername = servername;
        squitIrcCommand.comment = comment;
        squitIrcCommand.setExecutable(true);
        return squitIrcCommand;
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
        
        servername = check(pList.get(index++), serverNameRegex); 
        comment = check(pList.get(index++), stringRegex);
        
        setExecutable(true);
    }
    
    /** Исполнитель команды. */
    public void run() {
        
        IrcServer ircServer = null;
        
        if (!isExecutable()) {
            return;
        }
        
        if (!client.isOperator()) {
            client.send(errNoPrivileges(client));
            return;
        } 

        ircServer = db.getIrcServer(servername);
                    
        if (ircServer == null) {            
            client.send(errNoSuchServer(client, servername));
        } else {
            WallopsIrcCommand.create(db, Globals.thisIrcServer.get(), 
                    client.getNickname() + " " + commandName + " " +
                    servername + " " + ":" + comment).run();
            ircServer.close();                                    
        }
    }
}
