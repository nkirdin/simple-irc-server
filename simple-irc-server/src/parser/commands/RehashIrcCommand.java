/*
 * 
 * RehashIrcCommand 
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
 * RehashIrcCommand - класс, который проверяет параметры команды IRC 
 * REHASH и исполняет ее. 
 *
 *    <P>Command: REHASH
 * <P>Parameters: None
 *
 * @version 0.5 2012-02-07
 * @author  Nikolay Kirdin
 */
public class RehashIrcCommand extends IrcCommandBase {

    /** Название команды. */    
    public static final String commandName = "REHASH";
    
    public RehashIrcCommand() {}

    /** 
     * Создатель объекта команды без проверки параметров.
     * @param db репозитарий.
     * @param client источник команды.
     * @return объект команды.
     */
    public static RehashIrcCommand create(DB db, User client) {
        RehashIrcCommand rehashIrcCommand = new RehashIrcCommand();
        rehashIrcCommand.db = db;
        rehashIrcCommand.client = client;
        return rehashIrcCommand;
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
    
        //int index = 0;
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
        setExecutable(true);
    }
        
    /** Исполнитель команды. */
    public void run() {
        
        if (!executable) { 
            return;
        }

        if (client.getIrcServer() != Globals.thisIrcServer.get()) {
            return;
        }
        
        if (!client.isOperator()){
            client.send(errNoPrivileges(client));
            return;
        }
        
       client.send(rplRehashing(client, Globals.configFilename.get()));

       Globals.serverReconfigure.set(true);
    }
    
    /** 
     * Создает сообщение соответствующее  формализованному сообщению 
     * {@link Reply#RPL_REHASHING}. 
     * @param requestor источник команды.
     * @param filename имя файла.
     * @return объект с сообщением.
     */        
    private IrcCommandReport rplRehashing(IrcTalker requestor,
            String filename) {
        
        String remark = Reply.makeText(Reply.RPL_REHASHING, 
                requestor.getNickname(),
                filename);

        return new IrcCommandReport(remark, requestor,
                Globals.thisIrcServer.get());
    }
}
