/*
 * 
 * WhoisIrcCommand 
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
 * WhoisIrcCommand - класс, который проверяет параметры команды IRC 
 * WHOIS и исполняет ее. 
 *
 *    <P>Command: WHOIS
 * <P>Parameters: [&lt;server&gt;] 
 * &lt;nickmask&gt;[,&lt;nickmask&gt;[,...]]
 *
 * @version 0.5 2012-02-07
 * @author  Nikolay Kirdin
 */
public class WhoisIrcCommand extends IrcCommandBase {

    /** Название команды. */    
    public static final String commandName = "WHOIS";

    /** Параметр: &lt;server&gt;. */
    private String servernameMask = null;
    
    /** Параметр:  &lt;nickmask&gt;[,&lt;nickmask&gt;[,...]]. */
    private LinkedHashSet<String> nicknameMaskList =
            new LinkedHashSet<String>();

    public WhoisIrcCommand() {}

    /** 
     * Создатель объекта команды без проверки параметров.
     * @param db репозитарий.
     * @param client источник команды.
     * @param servernameMask параметр &lt;nickmask&gt;.
     * @param nicknameMaskList параметр
     * &lt;nickmask&gt;[,&lt;nickmask&gt;[,...]].
     * @return объект команды.
     */
    public static WhoisIrcCommand create(DB db,
            User client,
            String servernameMask,
            LinkedHashSet<String> nicknameMaskList) {
        WhoisIrcCommand whoisIrcCommand = new WhoisIrcCommand();
        whoisIrcCommand.db = db;
        whoisIrcCommand.client = client;
        whoisIrcCommand.servernameMask = servernameMask;
        whoisIrcCommand.nicknameMaskList = nicknameMaskList;
        whoisIrcCommand.setExecutable(true);
        return whoisIrcCommand;
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
        if (pList.size() == 2) {
            servernameMask = check(pList.get(index++), 
                    servernameMaskPattern);
        } else if (pList.size() != 1) {
            client.send(errUnknownCommand(client, commandName));
            return;
        }
        nicknameString = pList.get(index++);
        nicknameMaskList = new LinkedHashSet<String>(Arrays.asList(
                nicknameString.split(",")));
        for (String s : nicknameMaskList) {
            if (!isIt(s, userNicknameMask)) {
                client.send(errUnknownCommand(client, commandName));
                return;
            }
        }

        setExecutable(true);
    }

    /** Исполнитель команды. */
    public void run() {

        if (!isExecutable()) {
            return;
        }

        String nicknameString = "";

        if (servernameMask != null && !servernameMask.isEmpty()) {

            String content = "";

            for (String s : nicknameMaskList) {
                nicknameString = nicknameString + s + ",";
            }
            if (nicknameString.endsWith(",")) {
                nicknameString = nicknameString.substring(0, 
                        nicknameString.length() - 1);
            }
            content = ":" + client.getNickname() + " " + commandName
                    + " " + nicknameString;

            forwardWithMask(servernameMask, content, client, db);

            return;
        }

        LinkedHashSet<User> outputUserSet = new LinkedHashSet<User>();
        
        for (String nicknameMask : nicknameMaskList) {
            nicknameString = nicknameString + nicknameMask + ",";
            for (Iterator<String> iterator = db.getUserNicknameIterator();
                iterator.hasNext();) {
                String userNickname = iterator.next();
                if (!IrcMatcher.match(nicknameMask, userNickname)) {
                    continue;
                }
                User user = db.getUser(userNickname);
                if (user != null && user.isVisible(client)
                    && user.isRegistered()) {
                    outputUserSet.add(user);
                }
            }
        }
        if (nicknameString.endsWith(",")) {
            nicknameString = nicknameString.substring(0, 
                    nicknameString.length() - 1);
        }

        if (outputUserSet.isEmpty()) {
            client.send(errNoSuchNick(client, nicknameString));
            return;
        }

        for (User user : outputUserSet) {
            client.send(rplWhoIsUser(client, user));
            client.send(rplWhoIsServer(client, 
            		user.getNickname(),
            		user.getIrcServer().getHostname(),
            		user.getIrcServer().getInfo()));
            if (user.isOperator()) {
                client.send(rplWhoIsOperator(client, user));
            }
            client.send(rplWhoIsIdle(client, user));

            //RPL_WHOISCHANNELS "<nick> :*( ( "@" / "+" ) <channel> " " )"                    

            for (Iterator<IrcChannel> iterator = 
                    user.getChannelSetIterator(); iterator.hasNext();) {
                IrcChannel ch = iterator.next();
                if (ch.isVisible(client) && !ch.isAnonymous()) {
                    String chanString = "";
                    if (ch.checkChannelOperator(user)) {
                        chanString = "@";
                    } else if (ch.checkVote(client)) {
                        chanString = "+";
                    }
                    chanString = chanString + ch.getNickname();
                    client.send(rplWhoIsChannels(client, user, 
                            chanString));
                }
            }
            //318    RPL_ENDOFWHOIS "<nick> :End of WHOIS list"
            client.send(rplEndOfWhoIs(client, user));
        }
    }
    
    /**
     * Создание формализованного ответа типа
     * {@link Response.Reply#RPL_WHOISUSER}.
     * @param ircTalker отправитель.
     * @param user 
     * @return объект класса {@link IrcCommandReport} с формализованным 
     * ответом.
     */
    private IrcCommandReport rplWhoIsUser(IrcTalker ircTalker, 
            User user) {
        String remark = Response.makeText(Response.Reply.RPL_WHOISUSER,
                ircTalker.getNickname(),
                user.getNickname(),
                user.getUsername(),
                user.getHostname(),
                user.getRealname());
        return new IrcCommandReport(remark, ircTalker,
                Globals.thisIrcServer.get());
    }
    
    /**
     * Создание формализованного ответа типа
     * {@link Response.Reply#RPL_WHOISOPERATOR}.
     * @param ircTalker отправитель.
     * @param user 
     * @return объект класса {@link IrcCommandReport} с формализованным 
     * ответом.
     */
    private IrcCommandReport rplWhoIsOperator(IrcTalker ircTalker, 
            User user) {
        String remark = Response.makeText(Response.Reply.RPL_WHOISOPERATOR,
                ircTalker.getNickname(),
                user.getNickname());
        return new IrcCommandReport(remark, ircTalker,
                Globals.thisIrcServer.get());
    }
    
    /**
     * Создание формализованного ответа типа
     * {@link Response.Reply#RPL_WHOISIDLE}.
     * @param ircTalker отправитель.
     * @param user 
     * @return объект класса {@link IrcCommandReport} с формализованным 
     * ответом.
     */
    private IrcCommandReport rplWhoIsIdle(IrcTalker ircTalker, 
            User user) {
        String remark = Response.makeText(Response.Reply.RPL_WHOISIDLE,
                client.getNickname(),
                user.getNickname(),
                String.valueOf(user.getIdle()));
        return new IrcCommandReport(remark, ircTalker,
                Globals.thisIrcServer.get());
    }
    
    /**
     * Создание формализованного ответа типа
     * {@link Response.Reply#RPL_WHOISCHANNELS}.
     * @param ircTalker отправитель.
     * @param user 
     * @param chanString 
     * @return объект класса {@link IrcCommandReport} с формализованным 
     * ответом.
     */
    private IrcCommandReport rplWhoIsChannels(IrcTalker ircTalker, 
            User user, String chanString) {
        String remark = Response.makeText(Response.Reply.RPL_WHOISCHANNELS,
                client.getNickname(),
                user.getNickname(),
                chanString);
        return new IrcCommandReport(remark, ircTalker,
                Globals.thisIrcServer.get());
    }
    
    /**
     * Создание формализованного ответа типа
     * {@link Response.Reply#RPL_ENDOFWHOIS}.
     * @param ircTalker отправитель.
     * @param user 
     * @return объект класса {@link IrcCommandReport} с формализованным 
     * ответом.
     */
    private IrcCommandReport rplEndOfWhoIs(IrcTalker ircTalker, 
            User user) {
        String remark = Response.makeText(Response.Reply.RPL_ENDOFWHOIS,
                client.getNickname(),
                user.getNickname());
        return new IrcCommandReport(remark, ircTalker,
                Globals.thisIrcServer.get());
    }
}
