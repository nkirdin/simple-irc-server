/*
 * 
 * NickIrcCommand 
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
 * NickIrcCommand - класс, который проверяет параметры команды IRC 
 * NICK и исполняет ее. 
 *
 *    <P>Command: NICK
 * <P>Parameters: &lt;nickname&gt; [&lt;hopcount&gt;]
 *
 * <P>Парметер &lt;hopcount&gt; - не используется.
 *
 * @version 0.5 2012-02-20
 * @author  Nikolay Kirdin
 */
public class NickIrcCommand extends IrcCommandBase {

    /** Название команды. */    
    public static final String commandName = "NICK";

    /** Параметр: &lt;nickname&gt;. */
    private String nickname = null;

    public NickIrcCommand() {}

    /** 
     * Создатель объекта команды без проверки параметров.
     * @param db репозитарий.
     * @param client источник команды.
     * @param nickname параметр &lt;nickname&gt;.
     * @return объект команды.
     */
    public static NickIrcCommand create(DB db, User client, 
            String nickname) {
        NickIrcCommand nickIrcCommand = new NickIrcCommand();
        nickIrcCommand.db = db;
        nickIrcCommand.client = client;
        nickIrcCommand.nickname = nickname;
        nickIrcCommand.setExecutable(true);
        return nickIrcCommand;
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
            DB db) {

        int index = 0;
        this.db = db;
        this.pList = pList;
        this.trailing = trailing;

        if (requestor instanceof User) {
            client = (User) requestor;
        } else if (requestor instanceof IrcServer) {
            client = User.create((IrcServer) requestor);
        } else {
            requestor.send(errUnknownCommand(requestor, commandName));
            return;
        }

        if (index == pList.size()) {
            client.send(errNoNickname(client));
            return;
        }

        if (!isIt(pList.get(index), nickNamePattern)) {
            client.send(errOnUsingNickname(client, pList.get(0)));
            return;
        }

        nickname = pList.get(index++);

        setExecutable(true);
    }

    /** Исполнитель команды. */
    public void run() {
        Reply responseReply = null;
        String oldNickname = null;
        boolean wasNamed = false;
        boolean rename = false;

        if (!isExecutable()) {
            return;
        }

        if (client.isRegistered() && client.isRestricted()) {
            client.send(errUserRestricted(client));
            return;
        }

        oldNickname = client.getNickname();
        wasNamed = db.getUser(oldNickname) != null;
        rename = client.isRegistered();
        client.setNickname(nickname);
        responseReply = db.register(client);
                
        if (responseReply != Reply.RPL_OK) {
            client.setNickname(oldNickname);
            if (responseReply == Reply.ERR_NICKNAMEINUSE) {
                client.send(errNicknameInUse(client, nickname));
            } else {
                throw new Error("Internal error. Wrong reply: " + 
                        responseReply);
            }
            return;
        }

        if (wasNamed) {
            // Successfull rename
            db.unRegisterUser(oldNickname);
        }

        if (rename) {
            String remark = ":" + oldNickname + " " +
                    commandName + " " + client.getNickname();

            client.send(Globals.thisIrcServer.get(), remark);

            for (Iterator<IrcChannel> iterator = 
                    client.getChannelSetIterator();
                    iterator.hasNext();) {
                IrcChannel ch = iterator.next();
                if (ch.isAnonymous()) {
                    continue;
                }
                ch.send(client, remark);
            }
            return;
        }
        
        //Successfull registraition.
        if (client.isRegistered()) {
            welcomeMsg(db, client);
            MotdIrcCommand.create(db, client, "").run();
        }
    }
    
    /** 
     * Создает сообщение соответствующее  формализованному сообщению 
     * {@link Reply#ERR_NONICKNAMEGIVEN}. 
     * @param requestor источник команды.
     * @return объект с сообщением.
     */        
    private IrcCommandReport errNoNickname(IrcTalker requestor) {
        
        String remark = Reply.makeText(
                Reply.ERR_NONICKNAMEGIVEN,
                requestor.getNickname());

        return new IrcCommandReport(remark, requestor,
                Globals.thisIrcServer.get());
    }
    
}
