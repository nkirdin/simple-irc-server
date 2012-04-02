/*
 * 
 * ServiceIrcCommand 
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
 * ServiceIrcCommand - класс, который проверяет параметры команды IRC 
 * SERVICE и исполняет ее. 
 *
 *    <P>Command: SERVICE
 * <P>Parameters: &lt;nickname&gt; &lt;reserved&gt; &lt;distribution&gt; 
 * &lt;type&gt; &lt;reserved&gt; &lt;info&gt;
 *
 * @version 0.5 2012-02-07
 * @author  Nikolay Kirdin
 */
public class ServiceIrcCommand extends IrcCommandBase {

    /** Название команды. */    
    public static final String commandName = "SERVICE";

    /** Параметр: &lt;nickname&gt;. */
    private String nickname = null;
    
    /** Параметр: &lt;distribution&gt;. */
    private String servernameMask = null;
    
    /** Параметр: &lt;type&gt;. */
    private String type = null;
    
    /** Параметр: &lt;info&gt;. */
    private String info = null;
    
    /** Объект класса Service. */
    private Service service = null;

    public ServiceIrcCommand() {}

    /** 
     * Создатель объекта команды без проверки параметров.
     * @param db репозитарий.
     * @param service источник команды.
     * @param nickname параметр &lt;nickname&gt;.
     * @param servernameMask параметр &lt;distribution&gt;.
     * @param type параметр &lt;type&gt;.
     * @param info параметр &lt;info&gt;.
     * @return объект команды.
     */
    public static ServiceIrcCommand create(DB db,
            Service service,
            String nickname,
            String servernameMask,
            String type,
            String info) {
        ServiceIrcCommand serviceIrcCommand = new ServiceIrcCommand();
        serviceIrcCommand.db = db;
        serviceIrcCommand.service = service;
        serviceIrcCommand.nickname = nickname;
        serviceIrcCommand.servernameMask = servernameMask;
        serviceIrcCommand.type = type;
        serviceIrcCommand.info = info;
        serviceIrcCommand.setExecutable(true);
        return serviceIrcCommand;
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

        if (requestor instanceof Service) {
            service = (Service) requestor;
        } else if (requestor instanceof IrcServer) {
            service = Service.create((IrcServer) requestor);
        } else if (requestor instanceof User) {
            service = Service.create((User) requestor);
        } else {
            requestor.send(errUnknownCommand(requestor, commandName));
            return;
        }

        try {
            nickname = check(pList.get(index++), serviceNamePattern);
        } catch (IrcSyntaxException e) {
            requestor.send(
                    errOnUsingNickname(requestor, pList.get(0)));
            return;
        }

        check(pList.get(index++), wordPattern);
        boolean error = false;

        String[] sample = pList.get(index).split("\\.");
        for (String s : sample) {
            if (isIt(s, shortNamePattern) || isIt(s, "\\*") || 
                 isIt(s, "\\?")) {
                continue;
            }
            error = true;
            break;
        }
        if (error || !isIt(sample[sample.length - 1], shortNamePattern)) {
            requestor.send(errUnknownCommand(requestor, commandName));
            return;
        }
        servernameMask = pList.get(index++);

        type = check(pList.get(index++), wordPattern);
        check(pList.get(index++), wordPattern);
        info = check(pList.get(index++), stringPattern);

        if (service.isRegistered() || db.isRegistered(
                db.getService(nickname))) {
            requestor.send(errAlreadyRegistered(service));
            return;
        }
        setExecutable(true);
    }

    /** Исполнитель команды. */
    public void run() {

        Reply responseReply = null;
        if (!isExecutable()) {
            return;
        }

        service.setNickname(nickname);
        service.setDistribution(servernameMask);
        service.setType(type);
        service.setInfo(info);
        responseReply = db.register(service);

        if (responseReply == Reply.RPL_OK) {
            service.setRegistered(true);
            service.send(rplYoureService(service, nickname));
        } else {
            service.send(errAlreadyRegistered(service));
            service.close();
        }
    }

    /** 
     * Создает сообщение соответствующее  формализованному сообщению 
     * {@link Reply#RPL_YOURESERVICE}. 
     * @param requestor источник команды.
     * @param nickname никнэйм.
     * @return объект с сообщением.
     */        
    private IrcCommandReport rplYoureService(IrcTalker requestor,
                String nickname) {
        
        String remark = Reply.makeText(
                 Reply.RPL_YOURESERVICE,
                requestor.getNickname(),
                nickname);

        return new IrcCommandReport(remark, requestor,
                Globals.thisIrcServer.get());
    }
        
}
