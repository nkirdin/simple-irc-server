/*
 * 
 * TimeIrcCommand 
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
import java.text.*;

/**
 * TimeIrcCommand - класс, который проверяет параметры команды IRC 
 * TIME и исполняет ее. 
 *
 *    <P>Command: TIME
 * <P>Parameters: [&lt;server&gt;]
 *
 * @version 0.5 2012-02-07
 * @author  Nikolay Kirdin
 */
public class TimeIrcCommand extends IrcCommandBase {

    /** Название команды. */    
    public static final String commandName = "TIME";
    
    /** Формат вывода времени.*/
    private SimpleDateFormat sdf = new SimpleDateFormat(
            "EEEEEEE MMMMMMM dd yyyy -- kk:mm ZZZZZ", Locale.ENGLISH);


    /** Параметр: &lt;server&gt;. */
    private String servernameMask = "";

    public TimeIrcCommand() {}

    /** 
     * Создатель объекта команды без проверки параметров.
     * @param db репозитарий.
     * @param client источник команды.
     * @param servernameMask параметр &lt;server&gt;.
     * @return объект команды.
     */
    public static TimeIrcCommand create(DB db, User client, 
            String servernameMask) {
        TimeIrcCommand timeIrcCommand = new TimeIrcCommand();
        timeIrcCommand.db = db;
        timeIrcCommand.client = client;
        timeIrcCommand.servernameMask = servernameMask;
        timeIrcCommand.setExecutable(true);
        return timeIrcCommand;
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
                    servernameMaskPattern);
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

        if (!servernameMask.isEmpty()) {
            String content = ":" + client.getNickname() + " " + 
                    commandName;
            forwardWithMask(servernameMask, content, client, db);
            return;
        }

        client.send(rplTime(client, sdf.format(new Date())));
    }
    /**
     * Создание формализованного ответа типа
     * {@link Reply#RPL_TIME}.
     * @param ircTalker отправитель.
     * @param dateString 
     * @return объект класса {@link IrcCommandReport} с формализованным 
     * ответом.
     */
    private IrcCommandReport rplTime(IrcTalker ircTalker, 
            String dateString) {
        String remark = Reply.makeText(Reply.RPL_TIME, 
                ircTalker.getNickname(),
                ((User) ircTalker).getIrcServer().getHostname(),
                dateString);
        return new IrcCommandReport(remark, ircTalker,
                Globals.thisIrcServer.get());
    }
}
