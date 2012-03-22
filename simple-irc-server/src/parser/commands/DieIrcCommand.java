/*
 * 
 * DieIrcCommand 
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

/**
 * DieIrcCommand - класс, который проверяет параметры команды IRC 
 * DIE и исполняет ее. 
 *
 *      <P>Command: DIE
 *   <P>Parameters: None
 *
 * @version 0.5 2012-02-21
 * @author  Nikolay Kirdin
 */
public class DieIrcCommand extends IrcCommandBase {

    /** Название команды. */    
    public static final String commandName = "DIE";

    /** 
     * Создатель объекта команды без проверки параметров.
     * @param db репозитарий.
     * @param client источник команды.
     * @return объект команды.
     */
    public static DieIrcCommand create(DB db, User client) {
        DieIrcCommand dieIrcCommand = new DieIrcCommand();
        dieIrcCommand.db = db;
        dieIrcCommand.client = client;
        return dieIrcCommand;
    }
    
    public DieIrcCommand() {}

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
         setExecutable(true);
    }

    /** Исполнитель команды. */
    public void run() {

        if (!isExecutable()) {
            return;
        } 
        
        if (client.getIrcServer() != Globals.thisIrcServer.get()) {
            return;
        } 
        if (!client.isOperator()) {
            client.send(errNoPrivileges(client));
            return;
        } 
        
        LinkedList<String> targetList = new LinkedList<String>();
        for (Iterator<User> userSetIterator = db.getUserSetIterator(); 
                userSetIterator.hasNext();) {
            User user = userSetIterator.next();
            if (user.getIrcServer() == Globals.thisIrcServer.get()
                    && user.isRegistered()) { 
                targetList.offer(user.getNickname());
            }
        }

        String content = client.getNickname() + " " + commandName
        		+ " " + Globals.thisIrcServer.get().getHostname();

        NoticeIrcCommand.create(db, Globals.thisIrcServer.get(),
                targetList, content).run();

        Globals.serverDown.set(true);
    }
}
