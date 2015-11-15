package simpleircserver.parser.commands;
/*
 * 
 * WallopsIrcCommand 
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
import simpleircserver.talker.user.User;

/**
 * WallopsIrcCommand - класс, который проверяет параметры команды IRC 
 * WALLOPS и исполняет ее. 
 *
 *    <P>Command: WALLOPS
 * <P>Parameters: &lt;Text to be sent&gt;
 *
 * @version 0.5 2012-02-07
 * @version 0.5.3 2015-11-05 Program units were moved from default package into packages with names. Unit tests were added.
 * @author  Nikolay Kirdin
 */
public class WallopsIrcCommand extends IrcCommandBase {

    /** Название команды. */    
    public static final String commandName = "WALLOPS";

    /** Параметр: &lt;Text to be sent&gt;. */
    private String text = null;
    
    /** Источник команды. */
    private IrcTalker client;
    
    public WallopsIrcCommand() {}

    /** 
     * Создатель объекта команды без проверки параметров.
     * @param db репозитарий.
     * @param client источник команды.
     * @param text параметр &lt;Text to be sent&gt;.
     * @return объект команды.
     */
    public static WallopsIrcCommand create(DB db, IrcTalker client, 
            String text) {
        WallopsIrcCommand wallopsIrcCommand = new WallopsIrcCommand();
        wallopsIrcCommand.db = db;
        wallopsIrcCommand.client = client;
        wallopsIrcCommand.text = text;
        wallopsIrcCommand.setExecutable(true);
        return wallopsIrcCommand;
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

        if (index != pList.size() && !pList.get(index).isEmpty()) {
            text = check(pList.get(index++), stringPattern);
        } else {
            client.send(errNeedMoreParams(client, commandName));
            return;
        }

        setExecutable(true);
    }

    /** Исполнитель команды. */
    public void run() {
        
        if (!isExecutable()) {
            return;
        }

        String remark = commandName + " " + ":" + text;

        for (Iterator<User> userSetIterator = db.getUserSetIterator(); 
                userSetIterator.hasNext();) {
            User recipient = userSetIterator.next();
            if (recipient != client && recipient.isRegistered()
                    && ( recipient.isOperator() || 
                        recipient.isWallops())) {
                recipient.send(client, remark);
            }
        }
    }
}
