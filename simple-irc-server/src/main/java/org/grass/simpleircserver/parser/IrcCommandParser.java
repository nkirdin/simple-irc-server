package org.grass.simpleircserver.parser;
/*
 * 
 * IrcCommandParser 
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
import java.util.logging.*;

import org.grass.simpleircserver.base.Constants;
import org.grass.simpleircserver.base.Globals;
import org.grass.simpleircserver.config.IrcTranscriptConfig;
import org.grass.simpleircserver.parser.commands.AdminIrcCommand;
import org.grass.simpleircserver.parser.commands.AwayIrcCommand;
import org.grass.simpleircserver.parser.commands.ConnectIrcCommand;
import org.grass.simpleircserver.parser.commands.DieIrcCommand;
import org.grass.simpleircserver.parser.commands.ErrorIrcCommand;
import org.grass.simpleircserver.parser.commands.InfoIrcCommand;
import org.grass.simpleircserver.parser.commands.InviteIrcCommand;
import org.grass.simpleircserver.parser.commands.IrcCommandBase;
import org.grass.simpleircserver.parser.commands.IsonIrcCommand;
import org.grass.simpleircserver.parser.commands.JoinIrcCommand;
import org.grass.simpleircserver.parser.commands.KickIrcCommand;
import org.grass.simpleircserver.parser.commands.KillIrcCommand;
import org.grass.simpleircserver.parser.commands.LinksIrcCommand;
import org.grass.simpleircserver.parser.commands.ListIrcCommand;
import org.grass.simpleircserver.parser.commands.LusersIrcCommand;
import org.grass.simpleircserver.parser.commands.ModeIrcCommand;
import org.grass.simpleircserver.parser.commands.MotdIrcCommand;
import org.grass.simpleircserver.parser.commands.NamesIrcCommand;
import org.grass.simpleircserver.parser.commands.NickIrcCommand;
import org.grass.simpleircserver.parser.commands.NoticeIrcCommand;
import org.grass.simpleircserver.parser.commands.OperIrcCommand;
import org.grass.simpleircserver.parser.commands.PartIrcCommand;
import org.grass.simpleircserver.parser.commands.PassIrcCommand;
import org.grass.simpleircserver.parser.commands.PingIrcCommand;
import org.grass.simpleircserver.parser.commands.PongIrcCommand;
import org.grass.simpleircserver.parser.commands.PrivmsgIrcCommand;
import org.grass.simpleircserver.parser.commands.QuitIrcCommand;
import org.grass.simpleircserver.parser.commands.RehashIrcCommand;
import org.grass.simpleircserver.parser.commands.RestartIrcCommand;
import org.grass.simpleircserver.parser.commands.ServiceIrcCommand;
import org.grass.simpleircserver.parser.commands.ServlistIrcCommand;
import org.grass.simpleircserver.parser.commands.SqueryIrcCommand;
import org.grass.simpleircserver.parser.commands.SquitIrcCommand;
import org.grass.simpleircserver.parser.commands.StatsIrcCommand;
import org.grass.simpleircserver.parser.commands.SummonIrcCommand;
import org.grass.simpleircserver.parser.commands.TimeIrcCommand;
import org.grass.simpleircserver.parser.commands.TopicIrcCommand;
import org.grass.simpleircserver.parser.commands.TraceIrcCommand;
import org.grass.simpleircserver.parser.commands.UserIrcCommand;
import org.grass.simpleircserver.parser.commands.UserhostIrcCommand;
import org.grass.simpleircserver.parser.commands.UsersIrcCommand;
import org.grass.simpleircserver.parser.commands.VersionIrcCommand;
import org.grass.simpleircserver.parser.commands.WallopsIrcCommand;
import org.grass.simpleircserver.parser.commands.WhoIrcCommand;
import org.grass.simpleircserver.parser.commands.WhoisIrcCommand;
import org.grass.simpleircserver.parser.commands.WhowasIrcCommand;
import org.grass.simpleircserver.talker.IrcTalker;
import org.grass.simpleircserver.talker.server.IrcServer;
import org.grass.simpleircserver.talker.service.Service;
import org.grass.simpleircserver.talker.user.User;
import org.grass.simpleircserver.tools.IrcAvgMeter;

/**
 * Класс с помощью, которого проводится интерпретация сообщений клиента.   
 *
 * @version 0.5 2012-02-14
 * @version 0.5.3 2015-11-05 Program units were moved from default package into packages with names. Unit tests were added.
 * @author  Nikolay Kirdin
 * 
 */
public class IrcCommandParser {
    
    /**
     * Массив, в котором хранятся объекты типа class, которые являются 
     * исполнителями каждой конкретной команды IRC. 
     */
    private static Class[] commandClass = {
            //Connection Registration
            PassIrcCommand.class, NickIrcCommand.class,
            UserIrcCommand.class, OperIrcCommand.class, 
            ModeIrcCommand.class, ServiceIrcCommand.class, 
            QuitIrcCommand.class, SquitIrcCommand.class,
            //Channel operation  
            JoinIrcCommand.class, PartIrcCommand.class, 
            TopicIrcCommand.class, NamesIrcCommand.class, 
            ListIrcCommand.class, InviteIrcCommand.class, 
            KickIrcCommand.class,
            //Sending Messages
            PrivmsgIrcCommand.class, NoticeIrcCommand.class,
            //Server queries and commands
            MotdIrcCommand.class, LusersIrcCommand.class,
            VersionIrcCommand.class, StatsIrcCommand.class, 
            LinksIrcCommand.class, TimeIrcCommand.class,
            ConnectIrcCommand.class, TraceIrcCommand.class, 
            AdminIrcCommand.class, InfoIrcCommand.class,
            //Service Query and Commands
            ServlistIrcCommand.class, SqueryIrcCommand.class,
            //User based queries
            WhoIrcCommand.class, WhoisIrcCommand.class, 
            WhowasIrcCommand.class,
            //Miscellaneous messages
            KillIrcCommand.class, PingIrcCommand.class, 
            PongIrcCommand.class, ErrorIrcCommand.class,
            //Optional features
            AwayIrcCommand.class, RehashIrcCommand.class, 
            DieIrcCommand.class, RestartIrcCommand.class, 
            SummonIrcCommand.class, UsersIrcCommand.class,
            WallopsIrcCommand.class, UserhostIrcCommand.class, 
            IsonIrcCommand.class
            };

    /**
     * Ассоциативный массив классов-исполнителей, ключем которого 
     * является название команды. 
     */
    private static LinkedHashMap<String, Class> commandSet =
            new LinkedHashMap<String, Class>();
            
    /**
     * Ассоциативный массив, который служит для хранения информации о 
     * количестве обращений к командам. 
     */
    private static LinkedHashMap<String, IrcAvgMeter> commandStats =
            new LinkedHashMap<String, IrcAvgMeter>();
            
    /** 
     * Признак ошибки на этапе инициализации ассосиативного массива с 
     * исполнителями команд.
     */
    private static boolean initError;
    
    /** "Причина" порождения исключения. */
    private static Throwable cause;

    static {
        try {
            for (Class classObj : commandClass) {
                String command = (String)
                        classObj.getDeclaredField("commandName").get(
                            classObj.newInstance());
                commandSet.put(command.toLowerCase(Locale.ENGLISH), 
                        classObj);
                commandStats.put(command.toLowerCase(Locale.ENGLISH), 
                    new IrcAvgMeter(10000));
            }
        } catch (NoSuchFieldException e) {
            initError = true;
            cause = e;
        }
        catch (IllegalAccessException e) {
            initError = true;
            cause = e;
        }
        catch (InstantiationException e) {
            initError = true;
            cause = e;
        }
        if (initError) {
            String message = "IrcParser internal error: " + cause;
            Globals.logger.get().log(Level.SEVERE, message);
            throw new Error(message);
        }

    }

    /** Описатель клиента, который является источником сообщения. */
    private IrcTalker requestor;
    
    /** Сообщение. */
    private String parsingString;
    
    /** Сообщение. */
    private IrcIncomingMessage ircIncomingMessage;
    
    /** Хранилище параметров файла-протокола клиентских сообщений.*/
    private IrcTranscriptConfig ircTranscriptConfig;
    
    /** Префикс. */
    private String prefix;
    
    /** Цифровое сообщение. */
    private String ircReply;
    
    /** Название команды. */
    private String commandName;
    
    /** Список параметров команды. */
    private LinkedList<String> parameterList;
    
    /** 
     * Признак присутствия в параметрах команды секции "trailing". 
     * true, если такая секция присутствует. 
     */
    private boolean trailing;
    
    /** Указатель на объект-исполнитель команды. */
    private IrcCommandBase ircCommandCarrier;
    
    /** Сбор статистической информации о команде.*/
    private IrcAvgMeter ircAvgMeter;
    
    /** Время начала разбора строки. */
    private long startTime;
    
    /** Конструктор по умолчанию. */
    public IrcCommandParser() {}

    /**
     * Задание сообщения.
     * @param string сообщение.
     */
    public void setParsingString(String string) {
        parsingString = string;
    }

    /**
     * Получение сообщения.
     * @return string сообщение.
     */
    public String getParsingString() {
        return parsingString;
    }

    /**
     * Задание источника сообщения.
     * @param requestor клиент.
     */
    public void setRequestor(IrcTalker requestor) {
        this.requestor = requestor;
    }

    /**
     * Получение источника сообщения.
     * @return клиент.
     */
    public IrcTalker getRequestor() {
        return requestor;
    }
    
    /**
     * Получение статистики исполнения команд IRC.
     * @return ассоциативный массив со статистикой по исполнению команд 
     * IRC.
     */
    public static LinkedHashMap<String, IrcAvgMeter> getCommandStats() {
        synchronized (IrcCommandParser.commandStats) {
            return new LinkedHashMap<String, IrcAvgMeter>(commandStats);
        }
    }

    /**
     * Метод, в котором производится разбор строки и исполнение команды. 
     * В процессе разбора строки производится выделение префикса, 
     * цифрового ответа, команды и параметров. Затем эти компоненты 
     * команды передаются конкретному исполнителю для исполнения.   
     */
    public void ircParse() {
        
        
        int firstParameter = 0;
        String [] withTrailing = null;
        String [] withoutTrailing = null;
        String command = null;
        
        if (parsingString.isEmpty()) {
            return ;
        }
        
        startTime = System.nanoTime();
        
        Globals.logger.get().log(Level.FINER, requestor.getNickname()
            + "::" + parsingString);
        
        reset();
        
        try {

            if (parsingString.length() > 
                    Constants.MAX_PARSING_STRING_LENGTH - 2) {
                throw new IrcSyntaxException("ERR_UNKNOWNCOMMAND");
            }

            
            withTrailing = parsingString.split("\\s+:", 2);
            withoutTrailing = withTrailing[0].split("\\s+");
            if (withoutTrailing[0].charAt(0) == ':') {
                prefix = withoutTrailing[0].substring(1, 
                        withoutTrailing[0].length());
                checkPrefix();
                firstParameter++;
            }
            command = withoutTrailing[firstParameter++];
            commandName = command.toLowerCase(Locale.ENGLISH);
            parameterList.addAll(Arrays.asList(Arrays.copyOfRange(
                    withoutTrailing, firstParameter, 
                    withoutTrailing.length)));
            if (withTrailing.length == 2) {
                parameterList.offer(withTrailing[1]);
                trailing = true;
            }

            if (commandName.matches("\\d{3}")) {
                reply();
                commandName = "";
            } else if (!commandSet.containsKey(commandName)) {
                throw new IrcSyntaxException("ERR_UNKNOWNCOMMAND");
            }        
            
            
            if (!commandName.isEmpty()) {
                boolean isOperator = (requestor instanceof User) && 
                        ((User) requestor).isOperator(); 
                boolean isApprovedOrdinaryClientCommand = 
                        Arrays.asList(
                                new String[] {"ping", "pong", "admin", 
                                        "time", "oper"}
                                ).contains(commandName);
                boolean highLoad = !Globals.ircServerProcessorSet.get(
                        ).isEmpty();
                boolean isDroppable = highLoad && !isOperator 
                        && !isApprovedOrdinaryClientCommand; 
                
                boolean isOperCommand = commandName.equals("oper");
                
                if (Globals.ircTranscriptConfig.get() != null) {

                    String firstPart = ircIncomingMessage.incomingTime +
                            " " + ircIncomingMessage.id +
                            " " + ircIncomingMessage.getSource() +
                            " " + ircIncomingMessage.sender.getNickname();
                    if (isDroppable) {
                        firstPart = firstPart + " " + 
                                Reply.ERR_FILEERROR.code;
                    } else {
                        firstPart = firstPart + " " + "---";
                    }
                    
                    String lastPart = null;
                    if (isOperCommand) {
                        lastPart = (prefix.isEmpty() ? 
                                "" : ": " + prefix + " " ) 
                                + "OPER ******** ********";
                    } else {
                        lastPart = ircIncomingMessage.message;
                    }
                    
                    String transcript = firstPart + " " + lastPart; 
                                    
                    Globals.ircTranscriptConfig.get().offerToQueue(
                            transcript);
                }
                
                if (isDroppable) {
                    requestor.send(IrcCommandBase.errFileError(
                            requestor, commandName, "SERVER"));
                } else {
                    checkAndExecute();
                }
            }

        } catch (IndexOutOfBoundsException e) {
            requestor.send(
                    IrcCommandBase.errNeedMoreParams(requestor, 
                    command));
        } catch (IrcSyntaxException e) {
            if (e.getMessage().equals("ERR_UMODEUNKNOWNFLAG")) {
                requestor.send(
                        IrcCommandBase.errUModeUnknownFlag(requestor));
            } else if (e.getMessage().equals("ERR_NEEDMOREPARAMS")) {
                requestor.send(
                        IrcCommandBase.errNeedMoreParams(requestor, 
                        command));
            } else if (e.getMessage().equals("SILENCE")) {
            } else {
                requestor.send(
                        IrcCommandBase.errUnknownCommand(requestor, 
                        command));
            }
        } catch (IrcExecutionException e) {
            requestor.send(
                    new IrcCommandReport(e.getMessage(),
                            requestor,
                            Globals.thisIrcServer.get()));
        }
        
        synchronized (commandStats) {
            ircAvgMeter = commandStats.get(commandName);
            if (ircAvgMeter != null) {
                ircAvgMeter.setValue(System.nanoTime() - startTime);
                commandStats.put(commandName, ircAvgMeter);
            }
        }
    }
    
    /** "Сброс" парсера.*/
    private void reset() {
        ircAvgMeter = null;
        prefix = "";
        ircReply = "";
        commandName = "";
        parameterList = new LinkedList<String>();
        trailing = false;
        ircCommandCarrier = new IrcCommandBase();
    }
    
    /** Анализ и реакция на цифровой ответ. (Не реализовано.) */
    private void reply() {}

    /** Проверка префикса сообщения. */
    private void checkPrefix() throws IrcSyntaxException {

        String workingPrefix = prefix.toLowerCase(Locale.ENGLISH);
        String prefixIrcServer = null;
        String prefixHost = null;
        String prefixUser = null;
        String prefixNickname = null;

        int ircServerIndex = workingPrefix.indexOf("@");
        if (ircServerIndex != -1) {
            prefixIrcServer = workingPrefix.substring(ircServerIndex + 1,
                    workingPrefix.length());
            workingPrefix = workingPrefix.substring(0, ircServerIndex);
        }

        int ircHostIndex = workingPrefix.indexOf("%");
        if (ircHostIndex != -1) {
            prefixHost = workingPrefix.substring(ircHostIndex + 1,
                    workingPrefix.length());
            workingPrefix = workingPrefix.substring(0, ircHostIndex);
        }

        int ircUserIndex = workingPrefix.indexOf("!");
        if (ircUserIndex != -1) {
            prefixUser = workingPrefix.substring(ircUserIndex + 1,
                    workingPrefix.length());
            workingPrefix = workingPrefix.substring(0, ircUserIndex);
        }
        prefixNickname = workingPrefix;

        if (requestor instanceof User) {
            User user = (User) requestor;
            if (prefixNickname.equals(
                    requestor.getNickname().toLowerCase(Locale.ENGLISH))
                && (prefixUser == null ||
                    prefixUser.equals(user.getUsername().toLowerCase(
                                Locale.ENGLISH)))
                && (prefixHost == null ||
                    prefixHost.equals(user.getHostname().toLowerCase(
                                Locale.ENGLISH)))
                && (prefixIrcServer == null ||
                    prefixIrcServer.equals(
                        user.getIrcServer().getHostname().toLowerCase(
                        Locale.ENGLISH)))) {
                user.setLastMessageTime(System.currentTimeMillis());
           } else {

                QuitIrcCommand.create(Globals.db.get(), requestor,
                    "Wrong prefix:" + prefix).run();
                requestor.getConnection().close();
                throw new IrcSyntaxException("SILENCE");
            }
        } else if (requestor instanceof IrcServer) {
            if (prefixNickname != null
                && !prefixNickname.isEmpty()
                && (prefixUser == null)
                && (prefixHost == null)
                && (prefixIrcServer == null)) {
                if (Globals.db.get().getUser(prefixNickname) != null &&
                        Globals.db.get().getUser(prefixNickname).getConnection() ==
                    requestor.getConnection()) {
                    requestor = Globals.db.get().getUser(prefixNickname);
                } else if (Globals.db.get().getIrcServer(prefixNickname) != null &&
                        Globals.db.get().getIrcServer(prefixNickname).getConnection() 
                                == requestor.getConnection()) {
                    requestor = Globals.db.get().getIrcServer(prefixNickname);
                } else if (Globals.db.get().getService(prefixNickname) != null &&
                        Globals.db.get().getService(prefixNickname).getConnection() ==
                        requestor.getConnection()) {
                    requestor = Globals.db.get().getService(prefixNickname);
                } else {
                    /* FIXME
                      Have to kill User.
                      KillIrcCommand killIrcCommand = 
                      KillIrcCommand.create();
                      killIrcCommand.run();
                    */
                    QuitIrcCommand.create(Globals.db.get(), requestor,
                            "Wrong prefix:" + prefix).run();
                    requestor.getConnection().close();
                    throw new IrcSyntaxException("SILENCE");
                }
            } else {
                QuitIrcCommand.create(Globals.db.get(), requestor,
                    "Wrong prefix:" + prefix).run();
                requestor.getConnection().close();
                throw new IrcSyntaxException("SILENCE");
            }
        } else if (requestor instanceof Service) {
            if (prefixNickname != null
                && !prefixNickname.isEmpty()
                && (prefixUser == null)
                && (prefixHost == null)
                && (prefixIrcServer == null)) {
                if (Globals.db.get().getService(prefixNickname) != null &&
                        Globals.db.get().getService(prefixNickname) ==
                    requestor) {
                    requestor = Globals.db.get().getService(prefixNickname);
                } else {
                    /* FIXME
                    Have to kill User.
                    KillIrcCommand killIrcCommand = 
                    KillIrcCommand.create();
                    killIrcCommand.run();
                  */
                    QuitIrcCommand.create(Globals.db.get(), requestor,
                            "Wrong prefix:" + prefix).run();
                    requestor.getConnection().close();
                    throw new IrcSyntaxException("SILENCE");
                }
            } else {
                QuitIrcCommand.create(Globals.db.get(), requestor,
                    "Wrong prefix:" + prefix).run();
                requestor.getConnection().close();
                throw new IrcSyntaxException("SILENCE");
            }
        } else {
            QuitIrcCommand.create(Globals.db.get(), requestor,
                    "Wrong prefix:" + prefix).run();
            requestor.getConnection().close();
            throw new IrcSyntaxException("SILENCE");
        }

    }

    /** Проверка и исполнение команды IRC. */
    private void checkAndExecute() throws IrcExecutionException, 
        IrcSyntaxException {
             
        try {
            ircCommandCarrier = (IrcCommandBase) commandSet.get(
                    commandName).newInstance();
        } catch (InstantiationException e) {
            throw new Error("ircParse: Internal error " + e);
        } catch (IllegalAccessException e) {
            throw new Error("ircParse: Internal error " + e);
        }

        ircCommandCarrier.checking(parameterList, trailing, requestor, 
                Globals.db.get());
        ircCommandCarrier.run();
        
    }

    /** 
     * Задание входящего сообщения клиента. 
     * @param ircIncomingMessage входящее сообщение клиента.
     */
    public void setIncomingMessage(IrcIncomingMessage 
            ircIncomingMessage) {
        this.ircIncomingMessage = ircIncomingMessage;
        setParsingString(ircIncomingMessage.message);
        setRequestor(ircIncomingMessage.sender); 
    }

    /**
     * Получение входящего сообщения клиента. 
     * @return входящее сообщение клиента.
     */
    public IrcTranscriptConfig getIrcTranscriptConfig() {
        return ircTranscriptConfig;
    }

    /**
     * Задание параметров файла-протокола клиентских сообщений.
     * @param ircTranscriptConfig объект-хранилище параметров 
     * файла-протокола клиентских сообщений.
     */
    public void setIrcTranscriptConfig(IrcTranscriptConfig 
            ircTranscriptConfig) {
        this.ircTranscriptConfig = ircTranscriptConfig;
    }
}
