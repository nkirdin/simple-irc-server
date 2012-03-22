/*
 * 
 * ModeIrcCommand 
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
 * ModeIrcCommand - класс, который проверяет параметры команды IRC 
 * MODE и исполняет ее. 
 *
 *    <P>Command: MODE 
 * <P>Parameters: &lt;channel&gt; {[+|-]|o|p|s|i|t|n|b|v} [&lt;limit&gt;] 
 * [&lt;user&gt;] [&lt;ban mask&gt;]
 *
 * <P>Parameters: &lt;nickname&gt; {[+|-]|i|w|s|o}
 *
 * @version 0.5 2012-02-20
 * @author  Nikolay Kirdin
 */
public class ModeIrcCommand extends IrcCommandBase {

    /** Название команды. */    
    public static final String commandName = "MODE";

    /** Параметр:  &lt;channel&gt;. */
    private String channelName = null;
    
    /** 
     * Параметр: {[+|-]|o|p|s|i|t|n|b|v} [&lt;limit&gt;] [&lt;user&gt;] 
     * [&lt;ban mask&gt;]. 
     */
    private LinkedList<ChannelModeCarrier> channelModeList = null;
    
    /** Параметр: &lt;nickname&gt;. */
    private String nickname = null;
    
    /** Параметр: {[+|-]|i|w|s|o}. */
    private LinkedList<UserModeCarrier> userModeList = null;

    public ModeIrcCommand() {}

    /** 
     *  Создатель объекта команды без проверки параметров.
     * @param db репозитарий.
     * @param client источник команды.
     * @param nickname параметр &lt;nickname&gt;.
     * @param userModeList параметр {[+|-]|i|w|s|o}.
     * @param channelName параметр  &lt;channel&gt;.
     * @param channelModeList параметр {[+|-]|o|p|s|i|t|n|b|v} 
     * [&lt;limit&gt;] [&lt;user&gt;] [&lt;ban mask&gt;].
     * @return объект команды.
     */
    public ModeIrcCommand create(DB db
            , User client
            , String nickname
            , LinkedList<UserModeCarrier> userModeList
            , String channelName
            , LinkedList<ChannelModeCarrier> channelModeList) {

        ModeIrcCommand modeIrcCommand = new ModeIrcCommand();
        modeIrcCommand.db = db;
        modeIrcCommand.client = client;
        modeIrcCommand.nickname = nickname;
        modeIrcCommand.userModeList = userModeList;
        modeIrcCommand.channelName = channelName;
        modeIrcCommand.channelModeList = channelModeList;
        modeIrcCommand.setExecutable(true);
        return modeIrcCommand;
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

        userModeList = new LinkedList<UserModeCarrier>();

        if (isIt(pList.get(index), nickNamePattern)) {

            nickname = pList.get(index++);

            char lastFlag = 0, flag = 0;

            if (index != pList.size()) {
                String s = pList.get(index++);
                int i = 0;
                while (i < s.length()) {
                    lastFlag = flag;
                    flag = 0;
                    if (s.charAt(i) == '+' || s.charAt(i) == '-') {
                        flag = s.charAt(i++);
                    }
                    if (flag == 0) {
                        flag = lastFlag;
                    }
                    userModeList.offer(check(s.charAt(i++), flag));
                }
            }
        } else if (isIt(pList.get(index), channelPattern)) {

            boolean channelModeError = false;

            channelName = pList.get(index++);
            channelModeList = new LinkedList<ChannelModeCarrier>();


            if (index != pList.size()) {

                String s = pList.get(index++);
                LinkedList<String> parameterStringList = 
                        new LinkedList<String>();
                LinkedList <LinkedList<String>> parameterList = 
                        new LinkedList<LinkedList<String>> ();

                while (index != pList.size()) {
                    parameterStringList = new LinkedList<String>(
                            Arrays.asList(pList.get(index++).split(",")));
                    parameterList.offer(parameterStringList);
                }

                char lastFlag = 0, flag = 0;
                int i = 0;
                while (i < s.length()) {
                    lastFlag = flag;
                    flag = 0;
                    if (s.charAt(i) == '+' || s.charAt(i) == '-') {
                        flag = s.charAt(i++);
                    }
                    if (flag == 0) {
                        flag = (lastFlag == 0) ? ' ' : lastFlag;
                    }

                    if (!parameterList.isEmpty()) {
                        parameterStringList = parameterList.poll();
                    } else {
                        parameterStringList = new LinkedList<String>();
                        parameterStringList.offer("");
                    }

                    do {
                        try {
                            channelModeList.offer(check(s.charAt(i),
                                    flag,
                                    parameterStringList.poll()));

                        } catch (IrcSyntaxException e) {
                            channelModeError = true;
                            client.send(errUnknownMode(client,
                                    s.charAt(i), channelName));
                         }
                    } while (!parameterStringList.isEmpty());
                    i++;
                }

            }
            if (channelModeError) {
                return;
            }

        } else {
            client.send(errUnknownCommand(client, commandName));
            return;
        }

        setExecutable(true);

    }
    /** Исполнитель команды. */
    public void run() throws IrcExecutionException {
        LinkedList<Response> responseList = null;

        if (!isExecutable()) {
            return;
        }

        if (nickname != null && userModeList != null) {

            User user = db.getUser(nickname);

            if (user == null || !user.isRegistered()) {
                client.send(errNoSuchNick(client, nickname));
                return;
            }

            if (!client.isOperator() && user != client) {
                client.send(errUserDontMatch(client));
                return;
            }

            if (!userModeList.isEmpty()) {
                for (UserModeCarrier usermode : userModeList) {
                    user.updateUsermode(usermode);
                    client.send(client, 
                        commandName + " " + usermode.toString());
                }
            } else {
                client.send(rplUmodeIs(client, user));                
            }

        } else if (channelName != null && channelModeList != null) {

            IrcChannel ch = db.getChannel(channelName);

            if (ch == null) {
                client.send(errNoSuchChannel(client, channelName));
                return;
            }

            if (channelModeList.isEmpty()) {
                response = ch.listChannelmode(client);
                client.send(Globals.thisIrcServer.get(), response);
                return;
            }

            for (ChannelModeCarrier channelmode : channelModeList) {
                if (channelmode.getOperation() == ModeOperation.ADD
                        || channelmode.getOperation() == 
                        ModeOperation.REMOVE) {
                    if (client.isRestricted()) {
                        client.send(errUserRestricted(client));
                        continue;
                    }

                    if (!client.isOperator() && 
                            !ch.checkChannelOperator(client)) {
                        if (!(channelmode.getOperation() == 
                                ModeOperation.REMOVE
                                && Arrays.asList(new ChannelMode[] {
                                ChannelMode.o,
                                ChannelMode.O,
                                ChannelMode.v
                                }).contains(channelmode.getMode())
                                && channelmode.getParameter().equals(
                                client.getNickname()))) {
                            client.send(errNotChannelOp(client, 
                                    channelName));
                            continue;
                        }
                    }
                        
                    response = ch.updateChannelmode(channelmode, client);
                    if (response instanceof ResponseSuccess) {
                        client.send(client, commandName + " " 
                                + channelName
                                + " " + channelmode.toString());

                        
                        ch.send(client, 
                                commandName + " " + channelName
                                + " " + channelmode.toString());
                    } else {
                        client.send(Globals.thisIrcServer.get(), 
                                response);
                    }
                } else if (channelmode.getOperation() == 
                        ModeOperation.LIST) {
                    responseList = ch.listChannelmode(
                            channelmode.getMode(), client);
                    for (Response resp : responseList) {
                        client.send(Globals.thisIrcServer.get(), 
                                resp);
                    }
                } else {
                    throw new Error("MODE channel: Internal error");
                }
            }
        } else {
            throw new Error("MODE: Internal error");
        }
    }
    
    /** 
     * Создает сообщение соответствующее  формализованному сообщению 
     * {@link Response.Reply#ERR_USERSDONTMATCH}. 
     * @param requestor источник команды.
     * @return объект с сообщением.
     */        
    private IrcCommandReport errUserDontMatch(IrcTalker requestor) {
        
        String remark = Response.makeText(
        		Response.Reply.ERR_USERSDONTMATCH, 
        		requestor.getNickname());

        return new IrcCommandReport(remark, requestor,
                Globals.thisIrcServer.get());
    }
    
    /** 
     * Создает сообщение соответствующее  формализованному сообщению 
     * {@link Response.Reply#ERR_UNKNOWNMODE}. 
     * @param requestor источник команды.
     * @param mode символ параметра.
     * @param channelName имя канала.
     * @return объект с сообщением.
     */        
    private IrcCommandReport errUnknownMode(IrcTalker requestor, 
        char mode, String channelName) {
        
        String remark = Response.makeText(Response.Reply.ERR_UNKNOWNMODE,
                 requestor.getNickname(),
                 String.valueOf(mode),
                 channelName);

        return new IrcCommandReport(remark, requestor,
                Globals.thisIrcServer.get());
    }
    /** 
     * Создает сообщение соответствующее  формализованному сообщению 
     * {@link Response.Reply#RPL_UMODEIS}. 
     * @param requestor источник команды.
     * @param user тестируемый пользователь.
     * @return объект с сообщением.
     */        
    private IrcCommandReport rplUmodeIs(IrcTalker requestor, 
            User user) {
        
        String remark = Response.makeText(Response.Reply.RPL_UMODEIS,
                 requestor.getNickname(),
                 user.listUsermode());

        return new IrcCommandReport(remark, requestor,
                Globals.thisIrcServer.get());
    }

}
