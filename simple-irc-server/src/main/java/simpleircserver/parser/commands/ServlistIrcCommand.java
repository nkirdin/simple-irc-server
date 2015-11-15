package simpleircserver.parser.commands;
/*
 * 
 * ServlistIrcCommand 
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
import simpleircserver.talker.service.Service;
import simpleircserver.talker.user.User;
import simpleircserver.tools.IrcMatcher;

/**
 * ServlistIrcCommand - класс, который проверяет параметры команды IRC 
 * SERVLIST и исполняет ее. 
 *
 *    <P>Command: SERVLIST
 * <P>Parameters: [&lt;mask&gt; [&lt;type&gt;]]
 *
 * @version 0.5 2012-02-07
 * @version 0.5.3 2015-11-05 Program units were moved from default package into packages with names. Unit tests were added.
 * @author  Nikolay Kirdin
 */
public class ServlistIrcCommand extends IrcCommandBase {
    
    /** Название команды. */ 
    public static final String commandName = "SERVLIST";

    /** Параметр: &lt;mask&gt;. */
    private String servicenameMask = null;
    
    /** Параметр: &lt;type&gt;. */
    private String type = null;

    public ServlistIrcCommand() {}

    /** 
     * Создатель объекта команды без проверки параметров.
     * @param db репозитарий.
     * @param client источник команды.
     * @param servicenameMask параметр &lt;mask&gt;.
     * @param type параметр &lt;type&gt;.
     * @return объект команды.
     */
    public static ServlistIrcCommand create(DB db,
            User client,
            String servicenameMask,
            String type) {
        ServlistIrcCommand servlistIrcCommand = new ServlistIrcCommand();
        servlistIrcCommand.db = db;
        servlistIrcCommand.client = client;
        servlistIrcCommand.servicenameMask = servicenameMask;
        servlistIrcCommand.type = type;
        servlistIrcCommand.setExecutable(true);
        return servlistIrcCommand;
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

        if (index != pList.size()) {
            servicenameMask = check(pList.get(index++), nickNameRegex);
            if (index != pList.size()) {
                type = check(pList.get(index++), wordRegex);
            } else {
                type = "";
            }
        } else {
            servicenameMask = "";
            type = "";
        }
        setExecutable(true);
    }

    /** Исполнитель команды. */
    public void run() {

        if (!isExecutable()) {
            return;
        }

        LinkedHashSet<Service> serviceSet = new LinkedHashSet<Service>();
        if (servicenameMask != null && !servicenameMask.isEmpty()) {

            LinkedHashSet<String> ircServiceNameSet = 
                    db.getServiceNameSet();
            for (String name : ircServiceNameSet) {
                if (IrcMatcher.match(servicenameMask, name)
                        && IrcMatcher.match(type, 
                        db.getService(name).getType())) {
                    serviceSet.add(db.getService(name));
                }
            }
        } else {
            serviceSet = db.getServiceSet();
        }

        for (Service ircService: serviceSet) {
            client.send(rplServlist(client, ircService, 
                    servicenameMask, type));
        }
        client.send(rplServlistEnd(client, servicenameMask, type));
    }
    
    /** 
     * Создает сообщение соответствующее  формализованному сообщению 
     * {@link Reply#RPL_SERVLIST}. 
     * @param requestor источник команды.
     * @param service сервис.
     * @param servicenameMask область распространения.
     * @param type тип.
     * @return объект с сообщением.
     */        
    private IrcCommandReport rplServlist(IrcTalker requestor,
            Service service, String servicenameMask, String type) {
        
        String remark = Reply.makeText(Reply.RPL_SERVLIST, 
                client.getNickname(),
                service.getNickname(),
                service.getIrcServer().getHostname(),
                servicenameMask,
                type,
                String.valueOf(
                        service.getIrcServer().getHopcount()),
                service.getInfo());

        return new IrcCommandReport(remark, requestor,
                Globals.thisIrcServer.get());
    }
    
    /** 
     * Создает сообщение соответствующее  формализованному сообщению 
     * {@link Reply#RPL_SERVLISTEND}. 
     * @param requestor источник команды.
     * @param servicenameMask область распространения.
     * @param type тип.
     * @return объект с сообщением.
     */        
    private IrcCommandReport rplServlistEnd(IrcTalker requestor,
            String servicenameMask, String type) {
        
        String remark = Reply.makeText(Reply.RPL_SERVLISTEND, 
                client.getNickname(),
                servicenameMask,
                type);

        return new IrcCommandReport(remark, requestor,
                Globals.thisIrcServer.get());
    }
}
