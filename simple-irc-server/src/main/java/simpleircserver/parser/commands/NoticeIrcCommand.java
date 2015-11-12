package simpleircserver.parser.commands;
/*
 * 
 * NoticeIrcCommand 
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

import simpleircserver.base.DB;
import simpleircserver.channel.IrcChannel;
import simpleircserver.parser.IrcSyntaxException;
import simpleircserver.talker.IrcTalker;
import simpleircserver.talker.service.Service;
import simpleircserver.talker.user.User;

/**
 * NoticeIrcCommand - класс, который проверяет параметры команды IRC 
 * NOTICE и исполняет ее. 
 *
 *    <P>Command: PRIVMSG
 * <P>Parameters: &lt;receiver&gt;{,&lt;receiver&gt;} 
 * &lt;text to be sent&gt;
 *
 * @version 0.5 2012-02-20
 * @version 0.5.3 2015-11-05 Program units were moved from default package into packages with names. Unit tests were added.
 * @author  Nikolay Kirdin
 */
public class NoticeIrcCommand extends IrcCommandBase {

    /** Название команды. */    
    public static final String commandName = "NOTICE";

    /** Параметр:  &lt;receiver&gt;{,&lt;receiver&gt;}. */
    private List<String> targetList;
    
    /** Параметр: &lt;text to be sent&gt;. */
    private String message = "";

    /** Отправитель сообщения. */
    private IrcTalker requestor;
    
    public NoticeIrcCommand() {}

    /** 
     * Создатель объекта команды без проверки параметров. Параметр 
     * message всегда размещается в секции trailing сообщения IRC.
     * @param db репозитарий.
     * @param requestor источник команды.
     * @param targetList параметр &lt;receiver&gt;{,&lt;receiver&gt;}
     * @param message параметр &lt;text to be sent&gt;.
     * @return объект команды.
     */
    public static NoticeIrcCommand create(DB db,
            IrcTalker requestor,
            List<String> targetList,
            String message) {

        NoticeIrcCommand noticeIrcCommand = new NoticeIrcCommand();
        noticeIrcCommand.db = db;
        noticeIrcCommand.requestor = requestor;
        noticeIrcCommand.targetList = targetList;
        noticeIrcCommand.message = message;
        noticeIrcCommand.trailing = true;
        noticeIrcCommand.setExecutable(true);
        return noticeIrcCommand;
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
        this.requestor = requestor;

        if (!requestor.isRegistered()) {
            return;
        }

        if (!(requestor instanceof User) && 
                !(requestor instanceof Service)) {
            return;
        }

        if (index == pList.size()) {
            return;
        }

        targetList = new ArrayList<String>(Arrays.asList(
                pList.get(index++).split(",")));

        for (String s : targetList) {
            if (!isIt(s, msgToRegex)) {
                return;
            }
        }

        if (index != pList.size()) {
            message = pList.get(index);
        } else {
            return;
        }

        setExecutable(true);
    }

    /** Исполнитель команды. */
    public void run() {
        
        if (!isExecutable()) {
            return;
        }
        
        if (trailing) {
            message = ":" + message;
        }
        
        for (String target: targetList) {
            if (isIt(target, channelPattern)) {
                
                if (!(requestor instanceof User)) {
                    continue;
                }
                
                User client = (User) requestor;
                
                IrcChannel ch = db.getChannel(target);

                if (ch != null && ch.isVisible(client) && 
                        ch.canReceive(client)) {
                    ch.send(client, 
                            commandName + " " + target + " " + message);
                }
                
            } else if (isIt(target, nickNamePattern)) {
                
                User user = db.getUser(target);
                if (user == null || !user.isVisible(requestor) 
                        || !user.isRegistered()) {
                    continue;
                }
                user.send(requestor, 
                        commandName + " " + target + " " + message);
            }
        }
    }
}
