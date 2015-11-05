package org.grass.simpleircserver.parser.commands;
/*
 * 
 * UserIrcCommand 
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
import org.grass.simpleircserver.parser.ModeOperation;
import org.grass.simpleircserver.talker.IrcTalker;
import org.grass.simpleircserver.talker.user.User;
import org.grass.simpleircserver.talker.user.UserMode;
import org.grass.simpleircserver.talker.user.UserModeCarrier;

/**
 * UserIrcCommand - класс, который проверяет параметры команды IRC 
 * USER и исполняет ее. 
 *
 *    <P>Command: USER
 * <P>Parameters: &lt;username&gt; &lt;hostname&gt; &lt;servername&gt; 
 * &lt;realname&gt;
 *
 * <P>Parameters: &lt;username&gt; &lt;mode&gt; &lt;unused&gt; 
 * &lt;realname&gt;
 *
 * @version 0.5 2012-02-07
 * @version 0.5.3 2015-11-05 Program units were moved from default package into packages with names. Unit tests were added.
 * @author  Nikolay Kirdin
 */
public class UserIrcCommand extends IrcCommandBase {

    /** Название команды. */    
    public static final String commandName = "USER";

    /** Параметр: &lt;username&gt;. */
    private String username = null;
    
    /** Параметр: &lt;realname&gt;. */
    private String realname = null;
    
    private boolean registered = false;
    
    private LinkedList<UserModeCarrier> modeList = 
            new LinkedList<UserModeCarrier>();

    public UserIrcCommand() {}

    /** 
     * Создатель объекта команды без проверки параметров.
     * @param db репозитарий.
     * @param client источник команды.
     * @param username параметр &lt;username&gt;.
     * @param mode - параметр &lt;mode&gt;.
     * @param realname параметр &lt;realname&gt;.
     * @return объект команды.
     */
    public static UserIrcCommand create(DB db,
            User client,
            String username,
            int mode,
            String realname) {
        UserIrcCommand userIrcCommand = new UserIrcCommand();
        userIrcCommand.db = db;
        userIrcCommand.client = client;
        userIrcCommand.username = username;
        if ((mode & 0x04) != 0) {
            userIrcCommand.modeList.offer(new UserModeCarrier(
                        UserMode.w, ModeOperation.ADD));
        }
        if ((mode & 0x08) != 0) {
            userIrcCommand.modeList.offer(new UserModeCarrier(
                        UserMode.i, ModeOperation.ADD));
        }
        userIrcCommand.realname = realname;
        userIrcCommand.setExecutable(true);
        return userIrcCommand;
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

        registered = client.isRegistered();

        username = check(pList.get(index++), userRegex);

        if (isIt(pList.get(index), numberRegex)) {
            try {
                int mode = Integer.parseInt(pList.get(index++));
                //            char op = 0, flag = 0;
                if ((mode & 0x04) != 0) {
                    modeList.offer(check('w', '+'));
                }
                if ((mode & 0x08) != 0) {
                    modeList.offer(check('i', '+'));
                }
            } catch (NumberFormatException e) {
                requestor.send(errUnknownCommand(requestor, commandName));
                return;
            }
            check(pList.get(index++), wildManyRegex);
        } else {
            pList.get(index++);
            pList.get(index++);
        }
        
        realname = check(pList.get(index++), stringPattern);

        setExecutable(true);
    }

    /** Исполнитель команды. */
    public void run() {

        if (!isExecutable()) {
            return;
        }

        if (registered) {
            client.send(errAlreadyRegistered(client));
            return;
        }

        client.setUsername(username);
        for (UserModeCarrier umc : modeList) {
            client.updateUsermode(umc);
        }
        client.setRealname(realname);

        if (client.isRegistered() && !registered) {
            welcomeMsg(db, client);
            MotdIrcCommand.create(db, client, "").run();
        } else {
            String remark = commandName + " " + 
                    makeParameterString(pList, trailing);
            client.send(Globals.thisIrcServer.get(), remark);
        }
    }
}
