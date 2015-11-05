package org.grass.simpleircserver.parser.commands;
/*
 * 
 * MotdIrcCommand 
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

import org.grass.simpleircserver.base.Constants;
import org.grass.simpleircserver.base.DB;
import org.grass.simpleircserver.base.Globals;
import org.grass.simpleircserver.parser.IrcCommandReport;
import org.grass.simpleircserver.parser.IrcSyntaxException;
import org.grass.simpleircserver.parser.Reply;
import org.grass.simpleircserver.talker.IrcTalker;
import org.grass.simpleircserver.talker.user.User;
import org.grass.simpleircserver.tools.IrcFileFormat;

/**
 * MotdIrcCommand - класс, который проверяет параметры команды IRC 
 * MOTD и исполняет ее. 
 *
 *    <P>Command: MOTD
 * <P>Parameters: [&lt;target&gt;]
 *
 * @version 0.5 2012-02-07
 * @version 0.5.3 2015-11-05 Program units were moved from default package into packages with names. Unit tests were added.
 * @author  Nikolay Kirdin
 */
public class MotdIrcCommand extends IrcCommandBase {

    /** Название команды. */    
    public static final String commandName = "MOTD";

    /** Параметр: &lt;target&gt;. */
    private String servernameMask = null;

    public MotdIrcCommand() {}

    /** 
     * Создатель объекта команды без проверки параметров.
     * @param db репозитарий.
     * @param client источник команды.
     * @param servernameMask параметр &lt;target&gt;.
     * @return объект команды.
     */
    public static MotdIrcCommand create(DB db,
            User client,
            String servernameMask) {
        MotdIrcCommand motdIrcCommand = new MotdIrcCommand();
        motdIrcCommand.db = db;
        motdIrcCommand.client = client;
        motdIrcCommand.servernameMask = servernameMask;
        motdIrcCommand.setExecutable(true);
        return motdIrcCommand;
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

        if (index != pList.size()) {
            servernameMask = check(pList.get(index++), 
                    servernameMaskRegex);
        } else {
            servernameMask = "";
        }

        setExecutable(true);
    }

    /** Исполнитель команды. */
    public void run() {

        if (!isExecutable()) {
            return;
        }

        if (servernameMask != null && !servernameMask.isEmpty()) {
            String content = ":" + client.getNickname() + " " + 
                    commandName;
            forwardWithMask(servernameMask, content, client, db);
            return;
        }

        String remark = Reply.makeText(Reply.RPL_MOTD, 
                client.getNickname());

        List<String> stringList = IrcFileFormat.getFormattedText(
                Globals.motdFilename.get(),
                remark.length(),
                Constants.MAX_OUTPUT_LINE_NUMBER,
                Constants.MAX_OUTPUT_LINE_CHARS);

        if (stringList == null) {
            client.send(errNoMotd(client));
            return;
        }

        client.send(rplMotdStart(client));

        for (String s : stringList) {
            client.send(new IrcCommandReport(remark + s, client,
                    Globals.thisIrcServer.get()));
        }

        client.send(rplEndMotd(client));
    }
    
    /** 
     * Создает сообщение соответствующее  формализованному сообщению 
     * {@link Reply#ERR_NOMOTD}. 
     * @param requestor источник команды.
     * @return объект с сообщением.
     */        
    private IrcCommandReport errNoMotd(IrcTalker requestor) {
        
        String remark = Reply.makeText(Reply.ERR_NOMOTD, 
                client.getNickname());

        return new IrcCommandReport(remark, requestor,
                Globals.thisIrcServer.get());
    }

    /** 
     * Создает сообщение соответствующее  формализованному сообщению 
     * {@link Reply#RPL_MOTDSTART}. 
     * @param requestor источник команды.
     * @return объект с сообщением.
     */        
    private IrcCommandReport rplMotdStart(IrcTalker requestor) {
        
        String remark = Reply.makeText(Reply.RPL_MOTDSTART, 
                client.getNickname(),
                client.getIrcServer().getHostname());

        return new IrcCommandReport(remark, requestor,
                Globals.thisIrcServer.get());
    }

    /** 
     * Создает сообщение соответствующее  формализованному сообщению 
     * {@link Reply#RPL_ENDOFMOTD}. 
     * @param requestor источник команды.
     * @return объект с сообщением.
     */        
    private IrcCommandReport rplEndMotd(IrcTalker requestor) {
        
        String remark = Reply.makeText(Reply.RPL_ENDOFMOTD, 
                client.getNickname());

        return new IrcCommandReport(remark, requestor,
                Globals.thisIrcServer.get());
    }

}
