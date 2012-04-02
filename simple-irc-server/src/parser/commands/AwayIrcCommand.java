/*
 * 
 * AwayIrcCommand 
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
 * AwayIrcCommand - класс, который проверяет параметры команды IRC 
 * AWAY и исполняет ее.
 * 
 *    <P>Command: AWAY
 * <P>Parameters: [&lt;message&gt;]
 *
 * @version 0.5 2012-02-21
 * @author  Nikolay Kirdin
 */
public class AwayIrcCommand extends IrcCommandBase {

    /** Название команды. */    
    public static final String commandName = "AWAY";

    /** Параметр: &lt;message&gt;. */
    private String message = null;
    
    public AwayIrcCommand() {}

    /** 
     * Создатель объекта команды без проверки параметров.
     * @param db репозитарий.
     * @param client источник команды.
     * @param message параметр &lt;message&gt;.
     * @return объект команды.
     */
    public static AwayIrcCommand create(DB db, User client, 
            String message) {
        AwayIrcCommand awayIrcCommand = new AwayIrcCommand();
        awayIrcCommand.db = db;
        awayIrcCommand.client = client;
        awayIrcCommand.message = message;
        awayIrcCommand.setExecutable(true);
        return awayIrcCommand;
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
                message = check(pList.get(index++), stringPattern);
            } else {
                message = "";
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
        client.setAwayText(message);
        if (message.isEmpty()) {
            client.send(rplAnAway(client));
        } else {
            client.send(rplNoAway(client));
        }
    }
    
    /**
     * Создание формализованного ответа типа
     * {@link Reply#RPL_UNAWAY}.
     * @param ircTalker отправитель.
     * @return объект класса {@link IrcCommandReport} с формализованным 
     * ответом.
     */
    private IrcCommandReport rplAnAway(IrcTalker ircTalker) {
        String remark = Reply.makeText(Reply.RPL_UNAWAY, 
                client.getNickname());
        return new IrcCommandReport(remark, ircTalker, 
                Globals.thisIrcServer.get());
    }
    
    /**
     * Создание формализованного ответа типа
     * {@link Reply#RPL_NOWAWAY}.
     * @param ircTalker отправитель.
     * @return объект класса {@link IrcCommandReport} с формализованным 
     * ответом.
     */
    private IrcCommandReport rplNoAway(IrcTalker ircTalker) {
        String remark = Reply.makeText(Reply.RPL_NOWAWAY, 
                client.getNickname());
        return new IrcCommandReport(remark, ircTalker, 
                Globals.thisIrcServer.get());
    }
}
