/*
 * 
 * LusersIrcCommand 
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
 * LusersIrcCommand - класс, который проверяет параметры команды IRC 
 * LUSERS и исполняет ее. 
 *
 *    <P>Command: LUSERS
 * <P>Parameters: [&lt;mask&gt; [&lt;target&gt;]]
 *
 * Обработка запроса по маске не реализована.
 *
 * @version 0.5 2012-02-07
 * @author  Nikolay Kirdin
 */
public class LusersIrcCommand extends IrcCommandBase {

    /** Название команды. */    
    public static final String commandName = "LUSERS";
    
    /** Параметр: &lt;mask&gt;. */
    String mask = null;
    
    /** Параметр: &lt;target&gt;. */
    private String servernameMask = null;
       
    public LusersIrcCommand() {}

    /** 
     * Создатель объекта команды без проверки параметров.
     * @param db репозитарий.
     * @param client источник команды.
     * @param mask параметр &lt;mask&gt;.
     * @param servernameMask параметр &lt;target&gt;.
     * @return объект команды.
     */
    public LusersIrcCommand create(DB db, 
        User client, 
        String mask, 
        String servernameMask) {
    
        LusersIrcCommand lusersIrcCommand = new LusersIrcCommand();
        lusersIrcCommand.db = db;
        lusersIrcCommand.client = client;
        lusersIrcCommand.mask = mask;
        lusersIrcCommand.servernameMask = servernameMask;
        lusersIrcCommand.setExecutable(true);
        return lusersIrcCommand;
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
            mask = check(pList.get(index++), servernameMaskRegex);
            if (index != pList.size()) {
                servernameMask = check(pList.get(index++), 
                        servernameMaskRegex);
            } else {
                servernameMask = "";
            }
        } else {
            mask = "";
        }
        setExecutable(true);
    }
        
    /** Исполнитель команды. */
    public void run() {
        
        int numOfUsers = 0;
        int numOfServers = 0;
        int numOfOperators = 0;
        int numOfServices = 0;
        int numOfChannels = 0;
                
        if (!isExecutable()) {
            return;
        }
        if (servernameMask != null && !servernameMask.isEmpty()) {
            String content = ":" + client.getNickname() + " " + 
                    commandName + " " + mask;
            forwardWithMask(servernameMask, content, client, db);
            return;
        }      
        // Обработка запроса по маске не реализована.        
        
        numOfServers = db.getIrcServerSet().size();    
        numOfServices = db.getServiceSet().size();    
        numOfChannels = db.getChannelSet().size();    
                    
        for (Iterator<User> userSetIterator = db.getUserSetIterator(); 
                userSetIterator.hasNext();) {
            User user = userSetIterator.next();
            if (!user.isRegistered()) {
                continue;
            }
            numOfUsers++;
            if (user.isOperator()) {
                numOfOperators++;
            }
        }
                
        client.send(rplLuserClient(client, numOfUsers, 
                numOfServices, numOfServers));
        
        if (numOfOperators > 0) {
            client.send(rplLuserOp(client, numOfOperators));
        }
                    
        if (numOfChannels > 0) {
            client.send(rplLuserChannels(client, numOfChannels));
        }
        
        client.send(rplLuserMe(client, numOfUsers, numOfServers));
    }
    
    /** 
     * Создает сообщение соответствующее  формализованному сообщению 
     * {@link Response.Reply#RPL_LUSERCLIENT}. 
     * @param requestor источник команды.
     * @param numOfUsers количество пользователей.
     * @param numOfServices количество сервисов.
     * @param numOfServers количество серверов.
     * @return объект с сообщением.
     */        
    private IrcCommandReport rplLuserClient(IrcTalker requestor,
            int numOfUsers, int numOfServices, int numOfServers) {
        
        String remark = Response.makeText(Response.Reply.RPL_LUSERCLIENT,  
                requestor.getNickname(), 
                String.valueOf(numOfUsers), 
                String.valueOf(numOfServices), 
                String.valueOf(numOfServers));

        return new IrcCommandReport(remark, requestor,
                Globals.thisIrcServer.get());
    }
    
    /** 
     * Создает сообщение соответствующее  формализованному сообщению 
     * {@link Response.Reply#RPL_LUSEROP}. 
     * @param requestor источник команды.
     * @param numOfOperators количество операторов.
     * @return объект с сообщением.
     */        
    private IrcCommandReport rplLuserOp(IrcTalker requestor,
            int numOfOperators) {
        
        String remark = Response.makeText(Response.Reply.RPL_LUSEROP,  
                requestor.getNickname(), 
                String.valueOf(numOfOperators));

        return new IrcCommandReport(remark, requestor,
                Globals.thisIrcServer.get());
    }
    
    /** 
     * Создает сообщение соответствующее  формализованному сообщению 
     * {@link Response.Reply#RPL_LUSERCHANNELS}. 
     * @param requestor источник команды.
     * @param numOfChannels количество каналов.
     * @return объект с сообщением.
     */        
    private IrcCommandReport rplLuserChannels(IrcTalker requestor,
            int numOfChannels) {
        
        String remark = Response.makeText(
        		Response.Reply.RPL_LUSERCHANNELS, 
                requestor.getNickname(), 
                String.valueOf(numOfChannels));

        return new IrcCommandReport(remark, requestor,
                Globals.thisIrcServer.get());
    }
    
    /** 
     * Создает сообщение соответствующее  формализованному сообщению 
     * {@link Response.Reply#RPL_LUSERME}. 
     * @param requestor источник команды.
     * @param numOfUsers количество пользователей.
     * @param numOfServers количество серверов.
     * @return объект с сообщением.
     */        
    private IrcCommandReport rplLuserMe(IrcTalker requestor,
            int numOfUsers, int numOfServers) {
        
        String remark = Response.makeText(Response.Reply.RPL_LUSERME,  
                requestor.getNickname(), 
                String.valueOf(numOfUsers), 
                String.valueOf(numOfServers));

        return new IrcCommandReport(remark, requestor,
                Globals.thisIrcServer.get());
    }
    
}
