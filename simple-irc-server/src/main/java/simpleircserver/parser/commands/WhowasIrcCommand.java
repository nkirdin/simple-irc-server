package simpleircserver.parser.commands;
/*
 * 
 * WhowasIrcCommand 
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

import simpleircserver.base.Constants;
import simpleircserver.base.DB;
import simpleircserver.base.Globals;
import simpleircserver.parser.IrcCommandReport;
import simpleircserver.parser.IrcSyntaxException;
import simpleircserver.parser.Reply;
import simpleircserver.talker.IrcTalker;
import simpleircserver.talker.user.DriedUser;
import simpleircserver.talker.user.User;

/**
 * WhowasIrcCommand - класс, который проверяет параметры команды IRC 
 * WHOWAS и исполняет ее. 
 *
 *    <P>Command: WHOWAS
 * <P>Parameters: &lt;nickmask&gt;[,&lt;nickmask&gt;[,...]] 
 * [&lt;count&gt; [&lt;server&gt;]]
 *
 *
 * @version 0.5 2012-02-07
 * @version 0.5.3 2015-11-05 Program units were moved from default package into packages with names. Unit tests were added.
 * @author  Nikolay Kirdin
 */
public class WhowasIrcCommand extends IrcCommandBase {

    /** Название команды. */    
    public static final String commandName = "WHOWAS";

    /** Параметр: &lt;nickmask&gt;[,&lt;nickmask&gt;[,...]]. */
    private LinkedHashSet<String> nicknameList = null;
    
    /** Параметр: &lt;count&gt;. */
    int maxCount = Constants.HARD_LIMIT;
    
    /** Параметр: &lt;server&gt;. */
    private String servernameMask = null;

    public WhowasIrcCommand() {}

    /** 
     * Создатель объекта команды без проверки параметров.
     * @param db репозитарий.
     * @param client источник команды.
     * @param nicknameList параметр.
     * &lt;nickmask&gt;[,&lt;nickmask&gt;[,...]].
     * @param maxCount - параметр &lt;count&gt;.
     * @param servernameMask параметр &lt;server&gt;.
     * @return объект команды.
     */
    public static WhowasIrcCommand create(DB db,
            User client,
            LinkedHashSet<String> nicknameList,
            int maxCount,
            String servernameMask) {

        WhowasIrcCommand whowasIrcCommand = new WhowasIrcCommand();
        whowasIrcCommand.db = db;
        whowasIrcCommand.client = client;
        whowasIrcCommand.nicknameList = nicknameList;
        whowasIrcCommand.maxCount = maxCount;
        whowasIrcCommand.servernameMask = servernameMask;
        whowasIrcCommand.setExecutable(true);
        return whowasIrcCommand;
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

        String nicknameString = null;

        nicknameString = pList.get(index++);
        nicknameList = new LinkedHashSet<String>(Arrays.asList(
                nicknameString.split(",")));
        for (String s : nicknameList) {
            if (!isIt(s, nickNamePattern)) {
                client.send(errUnknownCommand(client, commandName));
                return;
            }
        }

        if (index != pList.size()) {
            String count = check(pList.get(index++), numberRegex);
            try {
                maxCount = Integer.parseInt(count);
            } catch (NumberFormatException e) {
                client.send(errUnknownCommand(client, commandName));
                return;
            }

            if (index != pList.size()) {
                servernameMask = check(pList.get(index++), 
                        servernameMaskRegex);
            } else {
                servernameMask = "";
            }
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
            String nicknameString = "";
            String content = "";

            for (String s : nicknameList) {
                nicknameString = nicknameString + s + ",";
            }
            if (nicknameString.endsWith(",")) {
                nicknameString = nicknameString.substring(0, 
                        nicknameString.length() - 1);
            }
            content = ":" + client.getNickname() + " " + commandName + 
                    " " + nicknameString;

            forwardWithMask(servernameMask, content, client, db);

            return;
        }

        for (String name : nicknameList) {
            ArrayList<DriedUser> historyList = db.getHistoryList(name);
            int historyCount = 0;
            if ( historyList != null && !historyList.isEmpty()) {
                for (ListIterator<DriedUser> 
                    idu = historyList.listIterator(historyList.size());
                        idu.hasPrevious();) {
                    if (++historyCount > maxCount) {
                        break;
                    }
                    DriedUser formerUser = idu.previous();
                    client.send(rplWhoWasUser(client, formerUser));
                    client.send(rplWhoIsServer(client,
                            formerUser.nickname,
                            formerUser.serverHostname,
                            ""));
                }
            }
            if (historyCount == 0 ) {
                 client.send(errWasNoSuchNick(client, name));
            } else {
                 client.send(rplEndOfWhoWas(client, name));
            }
           
        }
    }
    /**
     * Создание формализованного ответа типа
     * {@link Reply#RPL_WHOWASUSER}.
     * @param ircTalker отправитель.
     * @param driedUser историческая информация о клиенте
     * @return объект класса {@link IrcCommandReport} с формализованным 
     * ответом.
     */
    private IrcCommandReport rplWhoWasUser(IrcTalker ircTalker, 
            DriedUser driedUser) {
        String remark = Reply.makeText(Reply.RPL_WHOWASUSER,
                ircTalker.getNickname(),
                driedUser.nickname,
                driedUser.username,
                driedUser.hostname,
                driedUser.realname);
        return new IrcCommandReport(remark, ircTalker,
                Globals.thisIrcServer.get());
    }
        
    /**
     * Создание формализованного ответа типа
     * {@link Reply#ERR_WASNOSUCHNICK}.
     * @param ircTalker отправитель.
     * @param name 
     * @return объект класса {@link IrcCommandReport} с формализованным 
     * ответом.
     */
    private IrcCommandReport errWasNoSuchNick(IrcTalker ircTalker, 
            String name) {
        String remark = Reply.makeText(Reply.ERR_WASNOSUCHNICK,
                ircTalker.getNickname(),
                name);
        return new IrcCommandReport(remark, ircTalker,
                Globals.thisIrcServer.get());
    }
        
    /**
     * Создание формализованного ответа типа
     * {@link Reply#RPL_ENDOFWHOWAS}.
     * @param ircTalker отправитель.
     * @param name 
     * @return объект класса {@link IrcCommandReport} с формализованным 
     * ответом.
     */
    private IrcCommandReport rplEndOfWhoWas(IrcTalker ircTalker, 
            String name) {
        String remark = Reply.makeText(Reply.RPL_ENDOFWHOWAS,
                ircTalker.getNickname(),
                name);
        return new IrcCommandReport(remark, ircTalker,
                Globals.thisIrcServer.get());
    }
}
