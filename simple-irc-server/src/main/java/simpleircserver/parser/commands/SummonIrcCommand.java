package simpleircserver.parser.commands;
/*
 * 
 * SummonIrcCommand 
 * is part of Simple Irc Server
 *
 *
 * Copyright (С) 2012, 2015, Nikolay Kirdin
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
import simpleircserver.base.Globals;
import simpleircserver.parser.IrcCommandReport;
import simpleircserver.parser.IrcSyntaxException;
import simpleircserver.parser.Reply;
import simpleircserver.talker.IrcTalker;
import simpleircserver.talker.user.User;

/**
 * SummonIrcCommand - класс, который проверяет параметры команды IRC 
 * SUMMON и исполняет ее. 
 *
 *    <P>Command: SUMMON
 * <P>Parameters: user> [ &lt;target&gt; [ &lt;channel&gt; ] ]
 *
 * @version 0.5 2012-02-07
 * @version 0.5.3 2015-11-05 Program units were moved from default package into packages with names. Unit tests were added.
 * @author  Nikolay Kirdin
 */
public class SummonIrcCommand extends IrcCommandBase {

    /** Название команды. */    
    public static final String commandName = "SUMMON";

    /**Источник команды. */
    private IrcTalker client = null;

    public SummonIrcCommand() {}

    /** 
     * Создатель объекта команды без проверки параметров.
     * @param db репозитарий.
     * @param client источник команды.
     * @return объект команды.
     */
    public static SummonIrcCommand create(DB db, User client) {
        SummonIrcCommand summonIrcCommand = new SummonIrcCommand();
        summonIrcCommand.db = db;
        summonIrcCommand.client = client;
        return summonIrcCommand;
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

        //int index = 0;
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

        setExecutable(true);
    }

    /** Исполнитель команды. */
    public void run() {

        if (!executable) {
            return;
        }
        client.send(errSummonDisabled(client));
    }
    
    /** 
     * Создает сообщение соответствующее  формализованному сообщению 
     * {@link Reply#ERR_SUMMONDISABLED}. 
     * @param requestor источник команды.
     * @return объект с сообщением.
     */        
    private IrcCommandReport errSummonDisabled(IrcTalker requestor) {
        
        String remark = Reply.makeText(
                Reply.ERR_SUMMONDISABLED,
                requestor.getNickname());

        return new IrcCommandReport(remark, requestor,
                Globals.thisIrcServer.get());
    }
    
}
