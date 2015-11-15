package simpleircserver.parser.commands;
/*
 * 
 * AdminIrcCommand 
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
import simpleircserver.parser.IrcCommandReport;
import simpleircserver.parser.IrcSyntaxException;
import simpleircserver.parser.Reply;
import simpleircserver.talker.IrcTalker;
import simpleircserver.talker.user.User;

/**
 * AdminIrcCommand - класс, который проверяет параметры команды IRC 
 * ADMIN и исполняет ее. 
 *
 *    <P>Command: ADMIN
 * <P>Parameters: [&lt;server&gt;]
 *
 * @version 0.5 2012-02-21
 * @version 0.5.3 2015-11-05 Program units were moved from default package into packages with names. Unit tests were added.
 * @author  Nikolay Kirdin
 */
public class AdminIrcCommand extends IrcCommandBase {

    /** Название команды. */    
    public static final String commandName = "ADMIN";
        
    /** Параметр: &lt;server&gt;. */
    private String servernameMask = null;
    
    public AdminIrcCommand() {}
    
    /** 
     * Создатель объекта команды без проверки параметров.
     * @param db репозитарий.
     * @param client источник команды.
     * @param servernameMask параметр &lt;server&gt;.
     * @return объект команды.
     */
    public static AdminIrcCommand create(DB db, User client, 
            String servernameMask) {

        AdminIrcCommand adminIrcCommand = new AdminIrcCommand();
        adminIrcCommand.db = db;
        adminIrcCommand.client = client;
        adminIrcCommand.servernameMask = servernameMask;
        adminIrcCommand.setExecutable(true);

        return adminIrcCommand;
    }

    /** 
     * Создатель объекта команды с проверкой параметров.
     * @param pList список параметров команды.
     * @param trailing true, если в последним в списке параметров
     * находится секция "trailing".
     * @param requestor источник команды.
     * @param db репозитарий.
     */
    public void checking(LinkedList<String> pList, boolean trailing, 
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
            if (index != pList.size()) {
                servernameMask = check(pList.get(index++), 
                        servernameMaskRegex);
            } else {
                servernameMask = "";
            }
            setExecutable(true);
        } catch (IrcSyntaxException e) {
            requestor.send(errUnknownCommand(requestor, commandName));
        }
    }

    /** Исполнитель команды. */
    public void run() {

        if (!isExecutable()) {
            return;
        }
        
        if (servernameMask != null && !servernameMask.isEmpty()) {
            String content = ":" + client.getNickname() + " " + 
                    commandName;
            forwardWithMask(db, client, content, servernameMask);
            return;
        } 
        
        client.send(rplAdminMe(client));
        client.send(rplAdminLoc1(client, 
                db.getIrcAdminConfig().getLocation()));
        client.send(rplAdminLoc2(client,
                db.getIrcAdminConfig().getLocation2()));
        client.send(rplAdminMail(client, 
                db.getIrcAdminConfig().getEmail(), 
                db.getIrcAdminConfig().getName(),
                db.getIrcAdminConfig().getInfo()));
    }
    
    /**
     * Создание формализованного ответа типа
     * {@link Reply#RPL_ADMINME}.
     * @param ircTalker отправитель.
     * @return объект класса {@link IrcCommandReport} с формализованным 
     * ответом.
     */
    private IrcCommandReport rplAdminMe(IrcTalker ircTalker) {
        String remark = Reply.makeText(Reply.RPL_ADMINME, 
                ircTalker.getNickname(),
                client.getIrcServer().getHostname());
        return new IrcCommandReport(remark, ircTalker,
                Globals.thisIrcServer.get());
    }
    
    /**
     * Создание формализованного ответа типа
     * {@link Reply#RPL_ADMINLOC1}.
     * @param ircTalker отправитель.
     * @param location 
     * @return объект класса {@link IrcCommandReport} с формализованным 
     * ответом.
     */
    private IrcCommandReport rplAdminLoc1(IrcTalker ircTalker, 
            String location) {
        String remark = Reply.makeText(Reply.RPL_ADMINLOC1, 
                client.getNickname(),
                location);
        return new IrcCommandReport(remark, ircTalker,
                Globals.thisIrcServer.get());
    }
    
    /**
     * Создание формализованного ответа типа
     * {@link Reply#RPL_ADMINLOC2}.
     * @param ircTalker отправитель.
     * @param location2 
     * @return объект класса {@link IrcCommandReport} с формализованным 
     * ответом.
     */
    private IrcCommandReport rplAdminLoc2(IrcTalker ircTalker, 
            String location2) {
        String remark = Reply.makeText(Reply.RPL_ADMINLOC2, 
                client.getNickname(),
                location2);
        return new IrcCommandReport(remark, ircTalker,
                Globals.thisIrcServer.get());
    }
    
    /**
     * Создание формализованного ответа типа
     * {@link Reply#RPL_ADMINEMAIL}.
     * @param ircTalker отправитель.
     * @param email 
     * @param name 
     * @param other 
     * @return объект класса {@link IrcCommandReport} с формализованным 
     * ответом.
     */
    private IrcCommandReport rplAdminMail(IrcTalker ircTalker, 
            String email, String name, String other) {
        String remark = Reply.makeText(Reply.RPL_ADMINEMAIL, 
                client.getNickname(),
                email + ", " + name + ", " + other);
        return new IrcCommandReport(remark, ircTalker,
                Globals.thisIrcServer.get());
    }
}
