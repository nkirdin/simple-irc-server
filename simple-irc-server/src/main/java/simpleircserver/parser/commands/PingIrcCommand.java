package simpleircserver.parser.commands;
/*
 * 
 * PingIrcCommand 
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
import simpleircserver.parser.IrcSyntaxException;
import simpleircserver.talker.IrcTalker;
import simpleircserver.talker.server.IrcServer;
import simpleircserver.talker.user.User;

/**
 * PingIrcCommand - класс, который проверяет параметры команды IRC 
 * PING и исполняет ее. 
 *
 *    <P>Command: PING
 * <P>Parameters: &lt;server1&gt; [&lt;server2&gt;]
 *
 * @version 0.5 2012-02-07
 * @version 0.5.3 2015-11-05 Program units were moved from default package into packages with names. Unit tests were added.
 * @author  Nikolay Kirdin
 */
public class PingIrcCommand extends IrcCommandBase {

    /** Название команды. */    
    public static final String commandName = "PING";

    /** Параметр: &lt;server1&gt;. */
    private String servername = null;
    
    /** Параметр: &lt;server2&gt;. */
    private String servername2 = null;
    
    /** Источник команды. */
    private IrcTalker client;

    public PingIrcCommand() {}

    /** 
     * Создатель объекта команды без проверки параметров.
     * @param db репозитарий.
     * @param client источник команды.
     * @param servername параметр &lt;server1&gt;.
     * @param servername2 параметр &lt;server2&gt;.
     * @return объект команды.
     */
    public static PingIrcCommand create(DB db,
            IrcTalker client,
            String servername,
            String servername2) {

        PingIrcCommand pingIrcCommand = new PingIrcCommand();
        pingIrcCommand.db = db;
        pingIrcCommand.client = client;
        pingIrcCommand.servername = servername;
        pingIrcCommand.servername2 = servername2;
        pingIrcCommand.setExecutable(true);
        return pingIrcCommand;
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

        if (!(requestor instanceof User) && 
                !(requestor instanceof IrcServer)) {
            requestor.send(errUnknownCommand(requestor, commandName));
            return;
        }

        client = requestor;

        if (!client.isRegistered()) {
            return;
        }

        if (index != pList.size()) {
            servername = check(pList.get(index++), wordRegex);
        } else {
            client.send(errNoOrigin(client));
            return;
        }

        if (index != pList.size()) {
            servername2 = check(pList.get(index++), serverNameRegex);
        } else {
            servername2 = "";
        }

        setExecutable(true);
    }

    /** Исполнитель команды. */
    public void run() {

        if (!isExecutable()) {
            return;
        }

        IrcServer ircServerOrigin = null;
        IrcServer ircServerDestination = null;

        if (servername2 == null || servername2.isEmpty()) {
            String remark = ":" + 
                    Globals.thisIrcServer.get().getHostname() + " " +
                    "PONG" + " " + 
                    Globals.thisIrcServer.get().getHostname() 
                    + " " + ":" 
                    + (trailing ? trailing : "") + servername;

            client.send(Globals.thisIrcServer.get(), remark);
            return;
        }

        ircServerOrigin = db.getIrcServer(servername);
        if (ircServerOrigin == null) {
            client.send(errNoSuchServer(client, servername));
            return;
         }                
        
         ircServerDestination = db.getIrcServer(servername2);
         if (ircServerDestination == null) {
             client.send(errNoSuchServer(client, servername2));
             return;
         } 
         
         if (ircServerDestination == Globals.thisIrcServer.get()) {
             PongIrcCommand.create(db, Globals.thisIrcServer.get(),
                    servername2, servername).run();
             return;
         } 
                                
         String remark = ":" + client.getHostname() + " " + 
                "PING" + " " + servername + " " + servername2 
                + " " + ":" 
                + (trailing ? pList.get(pList.size() - 1) + " "  : "")
                + Globals.thisIrcServer.get().getHostname();

         ircServerDestination.send(client, remark);
    }
    
}
