/*
 * 
 * InfoIrcCommand 
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
 * InfoIrcCommand - класс, который проверяет параметры команды IRC 
 * INFO и исполняет ее. 
 * 
 *    <P>Command: INFO
 * <P>Parameters: [&lt;server&gt;]
 *
 * @version 0.5.2 2012-03-29
 * @author  Nikolay Kirdin
 */
public class InfoIrcCommand extends IrcCommandBase {

    /** Название команды. */    
    public static final String commandName = "INFO";
    
    /** Параметр: &lt;server&gt;. */
    private String servernameMask = null;
    
    public InfoIrcCommand() {}

    /** 
     * Создатель объекта команды без проверки параметров.
     * @param db репозитарий.
     * @param client источник команды.
     * @param servernameMask параметр &lt;server&gt;.
     * @return объект команды.
     */
    public static InfoIrcCommand create(DB db
            , User client
            , String servernameMask) {
        InfoIrcCommand infoIrcCommand = new InfoIrcCommand();
        infoIrcCommand.db = db;
        infoIrcCommand.client = client;
        infoIrcCommand.servernameMask = servernameMask;
        infoIrcCommand.setExecutable(true);
        return infoIrcCommand;
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
            if (index != pList.size()) {
                servernameMask = check(pList.get(index++), 
                        servernameMaskRegex);
            } else {
                    servernameMask = "";
            }
            setExecutable(true);
        } catch (IrcSyntaxException e) {
            requestor.send(errUnknownCommand(requestor, commandName));
        } catch (IndexOutOfBoundsException e) {
            requestor.send(errNeedMoreParams(requestor, commandName));
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

        String remark = Reply.makeText(Reply.RPL_INFO,  
                client.getNickname());

        for (String s: Constants.INFO_TEXT) {
        	client.send(rplInfo(client, remark + s));
        }
        client.send(rplEndOfInfo(client));     
    }

    /**
     * Создание формализованного ответа типа
     * {@link Reply#RPL_INFO}.
     * @param ircTalker отправитель.
     * @param s выводимая строка.
     * @return объект класса {@link IrcCommandReport} с формализованным 
     * ответом.
     */
    private IrcCommandReport rplInfo(IrcTalker ircTalker, String s) {
         return new IrcCommandReport(s, ircTalker,
                Globals.thisIrcServer.get());
    }
    
    /**
     * Создание формализованного ответа типа
     * {@link Reply#RPL_ENDOFINFO}.
     * @param ircTalker отправитель.
     * @return объект класса {@link IrcCommandReport} с формализованным 
     * ответом.
     */
    private IrcCommandReport rplEndOfInfo(IrcTalker ircTalker) {
        String remark = Reply.makeText(Reply.RPL_ENDOFINFO, 
                client.getNickname());
        
        return new IrcCommandReport(remark, ircTalker, 
                Globals.thisIrcServer.get());
    }
    
}
