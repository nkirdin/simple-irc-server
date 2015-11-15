package simpleircserver.parser.commands;
/*
 * 
 * PongIrcCommand 
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
import simpleircserver.base.Globals;
import simpleircserver.parser.IrcSyntaxException;
import simpleircserver.talker.IrcTalker;
import simpleircserver.talker.server.IrcServer;
import simpleircserver.talker.user.User;

/**
 * PongIrcCommand - класс, который проверяет параметры команды IRC 
 * PONG и исполняет ее. 
 *
 *    <P>Command: PONG
 * <P>Parameters: &lt;server&gt; [&lt;server2&gt;]
 *
 * @version 0.5 2012-02-07
 * @version 0.5.3 2015-11-05 Program units were moved from default package into packages with names. Unit tests were added.
 * @author  Nikolay Kirdin
 */
public class PongIrcCommand extends IrcCommandBase {

    /** Название команды. */    
    public static final String commandName = "PONG";

    /** Параметр: &lt;server&gt;. */
    private String servername = null;
    
    /** Параметр: &lt;server2&gt;. */
    private String servername2 = null;
    
    /** Источник команды. */
    private IrcTalker client;

    public PongIrcCommand() {}

    /** 
     * Создатель объекта команды без проверки параметров.
     * @param db репозитарий.
     * @param client источник команды.
     * @param servername параметр &lt;server&gt;.
     * @param servername2 параметр &lt;server2&gt;.
     * @return объект команды.
     */
    public static PongIrcCommand create(DB db,
            IrcTalker client,
            String servername,
            String servername2) {

        PongIrcCommand pongIrcCommand = new PongIrcCommand();
        pongIrcCommand.db = db;
        pongIrcCommand.client = client;
        pongIrcCommand.servername = servername;
        pongIrcCommand.servername2 = servername2;
        pongIrcCommand.setExecutable(true);
        return pongIrcCommand;
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

        if (!requestor.isRegistered()) {
            return;
        }

        if (!(requestor instanceof User) && 
                !(requestor instanceof IrcServer)){
            return;
        }

        client = requestor;

        if (index != pList.size()) {
            servername = check(pList.get(index++), wordRegex);

            if (index != pList.size()) {
                servername2 = check(pList.get(index++), serverNameRegex);
            } else {
                servername2 = "";
            }

        } else {
            servername = "";
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

        if (servername2 == null || servername2.isEmpty() ||
                db.getIrcServer(servername2) ==
                Globals.thisIrcServer.get()) {
            client.receivePong();
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
        } else {
            
            String remark = ":" + client.getHostname() + " " +
                    "PONG" + " " + servername + " " + servername2
                    + " " + ":"
                    + (trailing ? pList.get(pList.size() - 1) + " " : "")
                    + Globals.thisIrcServer.get().getHostname();
            ircServerDestination.send(client, remark);
            return;
        }
    }
}
