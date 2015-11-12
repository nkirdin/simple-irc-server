package simpleircserver.parser.commands;
/*
 * 
 * SqueryIrcCommand 
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
import simpleircserver.parser.IrcSyntaxException;
import simpleircserver.talker.IrcTalker;
import simpleircserver.talker.service.Service;
import simpleircserver.talker.user.User;

/**
 * SqueryIrcCommand - класс, который проверяет параметры команды IRC 
 * SQUERY и исполняет ее. 
 *
 *       <P>Command: SQUERY
 *  <P>Parameters: &lt;servicename&gt; &lt;text&gt;
 *
 * @version 0.5 2012-02-07
 * @version 0.5.3 2015-11-05 Program units were moved from default package into packages with names. Unit tests were added.
 * @author  Nikolay Kirdin
 */
public class SqueryIrcCommand extends IrcCommandBase {

    /** Название команды. */
    public static final String commandName = "SQUERY";

    /** Параметр: &lt;servicename&gt;. */
    private String servicename = null;
    
    /** Параметр: &lt;text&gt;. */
    private String message = null;

    public SqueryIrcCommand() {}

    /** 
     * Создатель объекта команды без проверки параметров.
     * @param db репозитарий.
     * @param client источник команды.
     * @param servicename параметр &lt;servicename&gt;.
     * @param message параметр &lt;text&gt;.
     * @return объект команды.
     */
    public static SqueryIrcCommand create(DB db,
            User client,
            String servicename,
            String message) {
        SqueryIrcCommand squeryIrcCommand = new SqueryIrcCommand();
        squeryIrcCommand.db = db;
        squeryIrcCommand.client = client;
        squeryIrcCommand.servicename = servicename;
        squeryIrcCommand.message = message;
        squeryIrcCommand.setExecutable(true);
        return squeryIrcCommand;
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
            servicename = check(pList.get(index++), nickNameRegex + "(" 
                    + "@" + hostNameRegex + ")?");

            if (index != pList.size()) {
                message = pList.get(index++);
            } else {
                client.send(errNoTextToSend(client));
                return;
            }

        } else {
            client.send(errNoRecipient(client, commandName));
            return;
        }
        setExecutable(true);
    }

    /** Исполнитель команды. */
    public void run() {

        if (!isExecutable()) {
            return;
        }
        Service service = db.getService(servicename);
        if (service == null) {
            client.send(errNoSuchNick(client, servicename));
            return;
        }
        
        if (trailing) {
            message = ":" + message;
        }
        
        String remark = commandName + " " + message;
        service.send(client, remark);
    }
}
