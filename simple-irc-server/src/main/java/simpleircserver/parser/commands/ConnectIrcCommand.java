package simpleircserver.parser.commands;
/*
 * 
 * ConnectIrcCommand 
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
import simpleircserver.talker.IrcTalker;
import simpleircserver.talker.server.IrcServer;
import simpleircserver.talker.user.User;
import simpleircserver.tools.IrcMatcher;

/**
 * ConnectIrcCommand - класс, который проверяет параметры команды IRC 
 * CONNECT и исполняет ее.
 * 
 *      <P>Command: CONNECT
 *   <P>Parameters: &lt;target server&gt; 
 * [&lt;port&gt; [&lt;remote server&gt;]]
 *
 * @version 0.5 2012-02-07
 * @version 0.5.3 2015-11-05 Program units were moved from default package into packages with names. Unit tests were added.
 * @author  Nikolay Kirdin
 */
public class ConnectIrcCommand extends IrcCommandBase {

    /** Название команды. */    
    public static final String commandName = "CONNECT";

    /** Параметр: &lt;target server&gt;. */
    private String servername;
    
    /** Параметр: &lt;port&gt;. */
    private int port;
    
    /** Параметр: &lt;remote server&gt;. */
    private String remoteServernameMask;

    public ConnectIrcCommand() {}

    /** 
     * Создатель объекта команды без проверки параметров.
     * @param db репозитарий.
     * @param client источник команды.
     * @param servername параметр &lt;target server&gt;.
     * @param port параметр &lt;port&gt;.
     * @param remoteServernameMask параметр &lt;remote server&gt;.
     * @return объект команды.
     */    
    public static ConnectIrcCommand create(DB db,
            User client,
            String servername,
            int port,
            String remoteServernameMask) {

        ConnectIrcCommand connectIrcCommand = new ConnectIrcCommand();
        connectIrcCommand.db = db;
        connectIrcCommand.client = client;
        connectIrcCommand.servername = servername;
        connectIrcCommand.port = port;
        connectIrcCommand.remoteServernameMask = remoteServernameMask;
        connectIrcCommand.setExecutable(true);
        return connectIrcCommand;
    }

    /** 
     * Создатель объекта команды с проверкой параметров.
     * @param pList список параметров команды.
     * @param trailing true, если в последним в списке параметров
     * находится секция "trailing".
     * @param requestor источник команды.
     * @param db репозитарий.
     */
    public void checking(LinkedList<String> pList,
            boolean trailing,
            IrcTalker requestor,
            DB db) {

        int index = 0;
        this.db = db;
        this.pList = pList;
        this.trailing = trailing;

        if (!(requestor instanceof User)) {
            requestor.send(errUnknownCommand(requestor, commandName));
            return;
        } 
        if (!requestor.isRegistered()) {
            requestor.send(errNotRegistered(requestor));
            return;
        } 
        
        client = (User) requestor;
        try {
            servername = check(pList.get(index++), serverNameRegex);
            port = Integer.parseInt(check(pList.get(index++), 
                    numberRegex));
            if (index != pList.size()) {
                remoteServernameMask = check(pList.get(index++), 
                        servernameMaskRegex);
            } else {
                remoteServernameMask = "";
            }
            setExecutable(true);
        } catch (NumberFormatException e) {
            requestor.send(errUnknownCommand(requestor, commandName));
        } catch (IrcSyntaxException e) {
            requestor.send(errUnknownCommand(requestor, commandName));
        } catch (IndexOutOfBoundsException e) {
            requestor.send(errNeedMoreParams(requestor, commandName));
        }
    }

    /** Исполнитель команды. */
    public void run() {
        IrcServer ircServer = null;
        IrcServer remoteServer = null;
        
        if (isExecutable()) {
            ircServer = db.getIrcServer(servername);
            if (!client.isOperator()) {
                client.send(errNoPrivileges(client));
            } else if (ircServer == null) {
                client.send(errNoSuchServer(client, servername));
            } else if (remoteServernameMask != null && 
                    !remoteServernameMask.isEmpty() &&
                    !IrcMatcher.match(remoteServernameMask,
                    Globals.thisIrcServer.get().getHostname())) {

                LinkedHashSet<IrcServer> serverSet = db.getIrcServerSet();
                for (IrcServer server : serverSet) {
                    if (IrcMatcher.match(remoteServernameMask, 
                            server.getHostname())) {
                        remoteServer = server;
                        break;
                    }
                }
                if (remoteServer == null) {
                    client.send(errNoSuchServer(client, 
                            remoteServernameMask));
                } else {
                //Forwarding
                    remoteServer.send(new IrcCommandReport(
                            commandName + " " + 
                            makeParameterString(pList, trailing),
                            remoteServer,
                            client));
                }
            } else {
                Globals.thisIrcServer.get().connect(ircServer, port);

                WallopsIrcCommand.create(db, Globals.thisIrcServer.get(), 
                        client.getNickname() + " " + commandName + " "
                        + makeParameterString(pList, trailing)).run();
            }
        }
    }
}
