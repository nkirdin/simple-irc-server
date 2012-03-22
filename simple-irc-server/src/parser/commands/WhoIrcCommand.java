/*
 * 
 * WhoIrcCommand 
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
import java.util.regex.*;

/**
 * WhoIrcCommand - класс, который проверяет параметры команды IRC 
 * WHO и исполняет ее. 
 *
 *    <P>Command: WHO
 * <P>Parameters: [&lt;mask&gt; [&lt;o&gt;]]
 *
 * @version 0.5 2012-02-07
 * @author  Nikolay Kirdin
 */
public class WhoIrcCommand extends IrcCommandBase {

    /** Название команды. */    
    public static final String commandName = "WHO";

    /** Параметр: &lt;mask&gt;. */
    private String mask = null;
    
    /** Параметр: &lt;o&gt;. */
    private boolean operMode = false;
    
    /** Скомпилированный паттерн для поиска по маске. */
    private Pattern maskPattern = null; 

    public WhoIrcCommand() {}

    /** 
     * Создатель объекта команды без проверки параметров.
     * @param db репозитарий.
     * @param client источник команды.
     * @param mask параметр &lt;mask&gt;.
     * @param operMode параметр &lt;o&gt;.
     * @return объект команды.
     */
    public static WhoIrcCommand create(DB db, User client,
            String mask,
            boolean operMode) {
        WhoIrcCommand whoIrcCommand = new WhoIrcCommand();
        whoIrcCommand.db = db;
        whoIrcCommand.client = client;
        whoIrcCommand.mask = mask;
        whoIrcCommand.operMode = operMode;
        whoIrcCommand.setExecutable(true);
        return whoIrcCommand;
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
        
        int i = 0;

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
            if (pList.get(index).equals("0")) {
                mask = "*";
                index++;
            } else if (isIt(pList.get(index), wordPattern)) {
                mask = pList.get(index++);
                
            } else {
                client.send(errUnknownCommand(client, commandName));
                return;
            }

            if (index != pList.size()) {
                if (!pList.get(index).matches("o")) {
                    client.send(errUnknownCommand(client, commandName));
                    return;
                }
                operMode = true;
            }
        } else {
            mask = "*";
        }
        
        String javaMask = "";
        char starChar = (char) 0X2A;
        char questChar = (char) 0X3F;
        char backSlashChar = (char) 0X5C;
        char dotChar = (char) 0X2E;
        
        while(i < mask.length()) {
            char charCode = mask.charAt(i++);
            
            if (charCode == backSlashChar ) {
                javaMask = javaMask + charCode + mask.charAt(i++);
            } else if (charCode == starChar) {
                javaMask = javaMask + dotChar + charCode;
            } else if (charCode == questChar) {
                javaMask = javaMask + dotChar + charCode;
            } else if (charCode == dotChar) {
                javaMask = javaMask + backSlashChar + charCode;
            } else {
                javaMask = javaMask + charCode;
            }
        }
        maskPattern = Pattern.compile(
                javaMask.toLowerCase(Locale.ENGLISH));
        setExecutable(true);
    }

    /** Исполнитель команды. */
    public void run() {
        

        if (!isExecutable()) {
            return;
        }

        Matcher tokenMatcher = maskPattern.matcher("");

        if (isIt(mask, channelNicknameMaskPattern)) {

            Iterator<Map.Entry<String, IrcChannel>> channelIterator = 
                    db.getChannelEntrySetIterator();

            while (channelIterator.hasNext()) {
                Map.Entry<String, IrcChannel> channelEntry = 
                        channelIterator.next();
                String token = channelEntry.getKey();
                IrcChannel ch = channelEntry.getValue();
                
                tokenMatcher.reset(token);
                
                if (!tokenMatcher.matches()) {
                    continue;
                }

                if (!ch.isVisible(client)) {
                    continue;
                }

                Iterator<Map.Entry<User, EnumSet <ChannelMode>>> 
                userSetIterator = ch.getUserEntrySetIterator();
                while (userSetIterator.hasNext()) {
                    Map.Entry<User, EnumSet <ChannelMode>> userEntry = 
                            userSetIterator.next();
                            User user = userEntry.getKey();
                    if (!user.isVisible(client) || 
                            !user.isRegistered() || 
                            (operMode && !user.isOperator())) {
                        continue;
                    }
                    client.send(rplWhoReply(client, ch, user,
                            userEntry.getValue()));
                }
                client.send(rplEndOfWho(client, ch.getNickname()));
            }

        } else {
            LinkedHashSet<User> userSet = null;
            Iterator<User> userSetIterator = null;
            User user = null;
            String token = null;
            
            if (!mask.equals("*")) {
                userSetIterator = db.getUserSetIterator(); 
            } else {
                userSet = db.getUserSet();
                for (IrcChannel ch : client.getChannelSet()) {
                    userSet.removeAll(ch.getUserSet());
                }
                userSetIterator = userSet.iterator(); 
            }

            while (userSetIterator.hasNext()) {
                user = userSetIterator.next();
                tokenMatcher.reset(user.getHostname().toLowerCase(
                        Locale.ENGLISH));
                 token = mask;
                if (!mask.equals("*")) {
                    if (tokenMatcher.matches()) {
                        //token = user.getHostname();
                    } else if (tokenMatcher.reset(user.getIrcServer(
                            ).getHostname().toLowerCase(
                            Locale.ENGLISH)).matches()) {
                        //token = user.getIrcServer().getHostname();
                    } else if (tokenMatcher.reset(user.getRealname()
                            ).matches()) {
                        //token = user.getRealname();
                    } else if (tokenMatcher.reset(user.getNickname(
                            ).toLowerCase(Locale.ENGLISH)).matches()) {
                        //token = user.getNickname();
                    } else {
                        continue;
                    }
                } 

                if (!user.isVisible(client) ||
                        !user.isRegistered() || 
                        (operMode && !user.isOperator())) {
                    continue;
                }
                String status = (user.hasAwayText() ? "G" : "H")
                            + (user.isOperator() ? "*" : "");
                client.send(rplWhoReply(client, "*",
                        user.getUsername(),
                        user.getHostname(),
                        user.getIrcServer().getHostname(),
                        user.getNickname(),
                        status,
                        user.getIrcServer().getHopcount(),
                        user.getRealname()));
                
            }
            client.send(rplEndOfWho(client, token));
        }
    }
    
    /**
     * Создание формализованного ответа типа
     * {@link Response.Reply#RPL_WHOREPLY}.
     * @param ircTalker отправитель.
     * @param channelNickname
     * @param userUsername
     * @param userHostname
     * @param userServername
     * @param userNickname
     * @param status
     * @param userHopcount
     * @param userRealname
     * @return объект класса {@link IrcCommandReport} с формализованным 
     * ответом.
     */
    private IrcCommandReport rplWhoReply(IrcTalker ircTalker,
            String channelNickname, String userUsername, 
            String userHostname, String userServername, 
            String userNickname, String status, int userHopcount, 
            String userRealname) {
                    String remark = Response.makeText(
                            Response.Reply.RPL_WHOREPLY,
                            ircTalker.getNickname(),
                            channelNickname,
                            userUsername,
                            userHostname,
                            userServername,
                            userNickname,
                            status,
                            String.valueOf(userHopcount),
                            userRealname);
        return new IrcCommandReport(remark, ircTalker,
                Globals.thisIrcServer.get());
    }

    /**
     * Создание формализованного ответа типа
     * {@link Response.Reply#RPL_ENDOFWHO}.
     * @param ircTalker отправитель.
     * @param token
     * @return объект класса {@link IrcCommandReport} с формализованным 
     * ответом.
     */
    private IrcCommandReport rplEndOfWho(IrcTalker ircTalker,
            String token) {
        String remark = Response.makeText(Response.Reply.RPL_ENDOFWHO,
                ircTalker.getNickname(),
                token);
        return new IrcCommandReport(remark, ircTalker,
                Globals.thisIrcServer.get());
    }
    
    /**
     * Создание формализованного ответа типа
     * {@link Response.Reply#RPL_WHOREPLY}.
     * @param ircTalker отправитель.
     * @param channel канал
     * @param user исследуемый клиент.
     * @param userChannelModeSet режимы клиента для этого канала.
     * @return объект класса {@link IrcCommandReport} с формализованным 
     * ответом.
     */
    private IrcCommandReport rplWhoReply(IrcTalker ircTalker,
            IrcChannel channel, User user, 
            EnumSet<ChannelMode> userChannelModeSet) {
    
            String userNickname = user.getNickname();
            String userUsername = user.getUsername();
            String userHostname = user.getHostname();
            String userServername = user.getIrcServer().getHostname();
            String userRealname = user.getRealname();
            int userHopcount = user.getIrcServer().getHopcount();
                    
            String status = (user.hasAwayText() ? "G" : "H")
                    + (user.isOperator() ? "*" : "");
            if (channel.isAnonymous()) {
                String nick = Globals.anonymousUser.get().getNickname();
                userNickname = nick;
                userUsername = nick;
                userHostname = Globals.anonymousIrcServer.get(
                        ).getHostname();
                userServername = userHostname;
                userHopcount = 0;
                userRealname = nick;
            }   
            status = status + getUserStatus(userChannelModeSet);  
            String remark = Response.makeText(Response.Reply.RPL_WHOREPLY,
                    ircTalker.getNickname(),
                    channel.getNickname(),
                    userUsername,
                    userHostname,
                    userServername,
                    userNickname,
                    status,
                    String.valueOf(userHopcount),
                    userRealname);
        return new IrcCommandReport(remark, ircTalker,
                Globals.thisIrcServer.get());
    }
}
