package simpleircserver.parser.commands;
/*
 * 
 * QuitIrcCommand 
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
import simpleircserver.channel.IrcChannel;
import simpleircserver.parser.IrcSyntaxException;
import simpleircserver.parser.Reply;
import simpleircserver.processor.OutputQueueProcessor;
import simpleircserver.talker.IrcTalker;
import simpleircserver.talker.user.DriedUser;
import simpleircserver.talker.user.User;

/**
 * QuitIrcCommand - класс, который проверяет параметры команды IRC 
 * QUIT и исполняет ее. 
 *
 *    <P>Command: QUIT
 * <P>Parameters: [&lt;Quit message&gt;]
 *
 * @version 0.5 2012-02-20
 * @version 0.5.3 2015-11-05 Program units were moved from default package into packages with names. Unit tests were added.
 * @author  Nikolay Kirdin
 */
public class QuitIrcCommand extends IrcCommandBase {

    /** Название команды. */    
    public static final String commandName = "QUIT";

    /** Параметр: &lt;Quit message&gt;. */
    private String message = null;
    
    /** Источник команды. */
    private IrcTalker requestor = null;

    public QuitIrcCommand() {}

    /** 
     * Создатель объекта команды без проверки параметров.
     * @param db репозитарий.
     * @param requestor источник команды.
     * @param message параметр &lt;Quit message&gt;
     * @return объект команды.
     */
    public static QuitIrcCommand create(DB db, IrcTalker requestor, 
            String message) {
        QuitIrcCommand quitIrcCommand = new QuitIrcCommand();
        quitIrcCommand.db = db;
        quitIrcCommand.requestor = requestor;
        quitIrcCommand.message = message;
        quitIrcCommand.setExecutable(true);
        return quitIrcCommand;
    }
    
    /** 
     * Создатель объекта команды с проверкой параметров.
     * @param pList список параметров команды.
     * @param trailing true, если в последним в списке параметров
     * находится секция "trailing".
     * @param ircTalker - источник команды.
     * @param db репозитарий.
     * @throws IrcSyntaxException если будет обнаружена синтаксическая 
     * ошибка.
     */
    public void checking(LinkedList<String> pList,
            boolean trailing,
            IrcTalker ircTalker,
            DB db) throws IrcSyntaxException {

        int index = 0;
        this.db = db;
        this.pList = pList;
        this.trailing = trailing;

        requestor = ircTalker;

        if (index != pList.size() && isIt(pList.get(index), stringRegex)) {
            message = pList.get(index++);
        } else {
            message = "";
        }
        setExecutable(true);
    }

    /** Исполнитель команды. */
    public void run() {
        Reply responseReply = null;
        if (!isExecutable()) {
            return;
        }

        char quote = (char) 34;

        String closingString = "ERROR" + " " + ":Closing Link:" + " "
                + requestor.getNickname() + " " + "(" + quote + message 
                + quote + ")";
        if (requestor instanceof User) {
            User client = (User) requestor;

            for (Iterator<IrcChannel> iterator = 
                    client.getChannelSetIterator(); 
                    iterator.hasNext();) {
                IrcChannel ch = iterator.next();
                
                ch.send(client, commandName + 
                        (message.isEmpty() ? "" : " :" + message));

                client.remove(ch);
                ch.remove(client);
                if (ch.isUserSetEmpty()) {
                    responseReply = db.unRegister(ch);
                    if (responseReply == Reply.RPL_OK) {
                        ch.delete();
                    } else {
                        throw new Error("JOIN: db.unRegister(client)" +
                                " Internal error");
                    }
                }
            }
            DriedUser driedUser = new DriedUser(client.getNickname(), 
                    client.getUsername(), client.getHostname(), 
                    client.getRealname(), client.getIrcServer().getHostname(), 
                    client.getId());
            db.register(driedUser);
        }

        OutputQueueProcessor.process(requestor.getConnection(),
                closingString);

        requestor.close();
    }
}
