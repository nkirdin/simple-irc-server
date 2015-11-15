package simpleircserver.parser.commands;
/*
 * 
 * IsonIrcCommand 
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

import simpleircserver.base.Constants;
import simpleircserver.base.DB;
import simpleircserver.base.Globals;
import simpleircserver.parser.IrcCommandReport;
import simpleircserver.parser.IrcSyntaxException;
import simpleircserver.parser.Reply;
import simpleircserver.talker.IrcTalker;
import simpleircserver.talker.user.User;

/**
 * IsonIrcCommand - класс, который проверяет параметры команды IRC 
 * ISON и исполняет ее. 
 *
 *    <P>Command: ISON
 * <P>Parameters: nickname{&lt;space&gt;&lt;nickname&gt;}
 *
 * @version 0.5 2012-02-07
 * @version 0.5.3 2015-11-05 Program units were moved from default package into packages with names. Unit tests were added.
 * @author  Nikolay Kirdin
 */
public class IsonIrcCommand extends IrcCommandBase {

    /** Название команды. */    
    public static final String commandName = "ISON";

    /** Параметр nickname{&lt;space&gt;&lt;nickname&gt;}. */
    private LinkedList<String> nicknameList = new LinkedList<String>();

    public IsonIrcCommand() {}

    /** 
     * Создатель объекта команды без проверки параметров.
     * @param db репозитарий.
     * @param client источник команды.
     * @param nicknameList параметр 
     * nickname{&lt;space&gt;&lt;nickname&gt;}.
     * @return объект команды.
     */
    public IsonIrcCommand create(DB db,
            User client,
            LinkedList<String> nicknameList) {

        IsonIrcCommand isonIrcCommand = new IsonIrcCommand();
        isonIrcCommand.db = db;
        isonIrcCommand.client = client;
        isonIrcCommand.nicknameList = nicknameList;
        isonIrcCommand.setExecutable(true);
        return isonIrcCommand;
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

        int itemCount = 0;
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
        if (((User) requestor).getIrcServer() != 
                Globals.thisIrcServer.get()) {
            return;
        } 
         
        client = (User) requestor;
        while (index != pList.size() && itemCount++ <
                Constants.MAX_ISON_LIST_SIZE) {
            try {
                String item = check(pList.get(index++), nickNamePattern);
                        nicknameList.offer(item);
            } catch (IrcSyntaxException e) {
                reportList.offer(errUnknownCommand(requestor, 
                        commandName));
            } catch (IndexOutOfBoundsException e) {
                reportList.offer(errNeedMoreParams(requestor, 
                        commandName));
            }
        }
        
        if (!nicknameList.isEmpty()) {
            setExecutable(true);
        } else {
            requestor.send(errNeedMoreParams(client, commandName));
        }
    }

    /** Исполнитель команды. */
    public void run() {
        if (!isExecutable()) {
            return;
        }
        
        String outputString = "";
        for (String userNickname : nicknameList) {
            User user = db.getUser(userNickname);
            if (user != null && user.isRegistered() && 
                    user.isVisible(client)) {
                outputString = outputString + user.getNickname() + " ";
            }
        }
        if (outputString.endsWith(" ")) {
            outputString = outputString.substring(0, 
                    outputString.length() - 1);
        }
        client.send(rplIson(client, outputString));
    }

    /**
     * Создание формализованного ответа типа
     * {@link Reply#RPL_ISON}.
     * @param ircTalker отправитель.
     * @param outputString 
     * @return объект класса {@link IrcCommandReport} с формализованным 
     * ответом.
     */
    private IrcCommandReport rplIson(IrcTalker ircTalker,
            String outputString) {
        String remark = Reply.makeText(Reply.RPL_ISON,  
                ircTalker.getNickname(), outputString);
        return new IrcCommandReport(remark, ircTalker,
                Globals.thisIrcServer.get());
    }
    
}
