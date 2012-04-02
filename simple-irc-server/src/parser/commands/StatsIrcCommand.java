/*
 * 
 * StatsIrcCommand 
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
 * StatsIrcCommand - класс, который проверяет параметры команды IRC 
 * STATS и исполняет ее.
 *  
 *    <P>Command: STATS
 * <P>Parameters: [&lt;query&gt; [&lt;server&gt;]]
 *
 * @version 0.5 2012-02-07
 * @author  Nikolay Kirdin
 */
public class StatsIrcCommand extends IrcCommandBase {

    /** Название команды. */    
    public static final String commandName = "STATS";
    
    /** Параметр: &lt;query&gt;. */
    private String queryList = null;
    
    /** Параметр: &lt;server&gt;. */
    private String servernameMask = null;
       
    public StatsIrcCommand() {}

    /** 
     * Создатель объекта команды без проверки параметров.
     * @param db репозитарий.
     * @param client источник команды.
     * @param queryList - параметр &lt;query&gt;.
     * @param servernameMask параметр &lt;server&gt;.
     * @return объект команды.
     */
    public static StatsIrcCommand create(DB db, 
            User client, 
            String queryList, 
            String servernameMask) {
        StatsIrcCommand statsIrcCommand = new StatsIrcCommand();
        statsIrcCommand.db = db;
        statsIrcCommand.client = client;
        statsIrcCommand.queryList = queryList;
        statsIrcCommand.servernameMask = servernameMask;
        statsIrcCommand.setExecutable(true);
        return statsIrcCommand;
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
            queryList = check(pList.get(index++), queryListRegex);
            if (index != pList.size()) {
                servernameMask = check(pList.get(index++), 
                        servernameMaskRegex);
            } else {
                servernameMask = "";
            }
        } else {
            queryList = "";
            servernameMask = "";
        }
        setExecutable(true);
    }
        
    /** Исполнитель команды. */
    public void run() {
        
        if (!isExecutable()) {
            return;
        }
        
        if (servernameMask != null && !servernameMask.isEmpty()) {
            String content = ":" + client.getNickname() + " " + 
                    commandName + " " + queryList;
            forwardWithMask(servernameMask, content, client, db);
            return;
        }      
        
        if(queryList.contains("l")) {
        //211    RPL_STATSLINKINFO "<linkname> <sendq> <sent messages> 
        // <sent Kbytes> <received messages> <received Kbytes> <time open>"
            client.send(rplStatsLinkInfo(client));
        }
                    
        if(queryList.contains("m")) {
            //212    RPL_STATSCOMMANDS "<command> <count> <byte count>
            //<remote count>"
            LinkedHashMap<String, IrcAvgMeter> commandStats = 
                    IrcCommandParser.getCommandStats();
            
            for (String command : commandStats.keySet()) {
                IrcAvgMeter ircAvgMeter = commandStats.get(command);
                client.send(rplStatsCommands(client, command, 
                        ircAvgMeter.counter.get(),
                        0,
                        0,
                        ircAvgMeter.getAvgValue()));
            }
        }
                    
        if(queryList.contains("u")) {
        //242    RPL_STATSUPTIME  ":Server Up %d days %d:%02d:%02d"
            long upTime = System.currentTimeMillis() - 
                    Globals.serverStartTime.get();            
            client.send(rplStatsUptime(client, upTime));
        }
                    
        if(queryList.contains("o")) {
        //243    RPL_STATSOLINE "O <hostmask> * <name>"
            client.send(rplStatsOLine(client));
        }
       client.send(rplEndOfStats(client, queryList));
    }
    
    /** 
     * Создает сообщение соответствующее  формализованному сообщению 
     * {@link Reply#RPL_STATSLINKINFO}. 
     * @param requestor источник команды.
     * @return объект с сообщением.
     */        
    private IrcCommandReport rplStatsLinkInfo(IrcTalker requestor) {
        
        String remark = Reply.makeText(Reply.RPL_STATSLINKINFO,
                requestor.getNickname(), "0", "0", "0", "0", "0", "0", 
                "0");

        return new IrcCommandReport(remark, requestor,
                Globals.thisIrcServer.get());
    }
    
    /** 
     * Создает сообщение соответствующее  формализованному сообщению 
     * {@link Reply#RPL_STATSCOMMANDS}. 
     * @param requestor источник команды.
     * @param commandName имя команды.
     * @param times количество обращений к команде в сообщениях клиентов.
     * @return объект с сообщением.
     */        
    private IrcCommandReport rplStatsCommands(IrcTalker requestor, 
            String commandName, 
            long times, 
            long bytes, 
            long remoteBytes,
            long avgDuration
            ) {
        
        String remark = Reply.makeText(Reply.RPL_STATSCOMMANDS,
                requestor.getNickname(),
                commandName, 
                 String.valueOf(times),
                String.valueOf(bytes), 
                String.valueOf(remoteBytes), 
                String.valueOf(avgDuration));

        return new IrcCommandReport(remark, requestor,
                Globals.thisIrcServer.get());
    }
    
    /** 
     * Создает сообщение соответствующее  формализованному сообщению 
     * {@link Reply#RPL_STATSUPTIME}. 
     * @param requestor источник команды.
     * @param upTime аптайм.
     * @return объект с сообщением.
     */        
    private IrcCommandReport rplStatsUptime(IrcTalker requestor, 
            long upTime) {

        int days = (int) (upTime / (24 * 60 * 60 * 1000L));
        int hours = (int) ((upTime % (24 * 60 * 60 * 1000L)) / 
                (60 * 60 * 1000));
        int minutes = (int) ((upTime % (24 * 60 * 60 * 1000L) % 
                (60 * 60 * 1000))/ (60 * 1000));
        int seconds = (int) ((upTime % (24 * 60 * 60 * 1000L) % 
                (60 * 60 * 1000) % (60 * 1000))/ 1000);
    
        String remark = Reply.makeText(Reply.RPL_STATSUPTIME,
                requestor.getNickname(),
                String.valueOf(days),
                String.valueOf(hours),
                String.valueOf(minutes),
                String.valueOf(seconds));

        return new IrcCommandReport(remark, requestor,
                Globals.thisIrcServer.get());
    }

    /** 
     * Создает сообщение соответствующее  формализованному сообщению 
     * {@link Reply#RPL_STATSOLINE}. 
     * @param requestor источник команды.
     * @return объект с сообщением.
     */        
    private IrcCommandReport rplStatsOLine(IrcTalker requestor) {

        String remark = Reply.makeText(Reply.RPL_STATSOLINE,
                requestor.getNickname(),
                "",
                "");

        return new IrcCommandReport(remark, requestor,
                Globals.thisIrcServer.get());
    }

    /** 
     * Создает сообщение соответствующее  формализованному сообщению 
     * {@link Reply#RPL_ENDOFSTATS}. 
     * @param requestor источник команды.
     * @param queryList ключи запроса.
     * @return объект с сообщением.
     */        
    private IrcCommandReport rplEndOfStats(IrcTalker requestor,
            String queryList) {

        String remark = Reply.makeText(Reply.RPL_ENDOFSTATS,
                requestor.getNickname(),
                queryList);

        return new IrcCommandReport(remark, requestor,
                Globals.thisIrcServer.get());
    }

}
