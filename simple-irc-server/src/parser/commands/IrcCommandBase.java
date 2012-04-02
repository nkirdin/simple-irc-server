/*
 * 
 * IrcCommandBase 
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
import java.util.regex.*;

/**
 * IrcCommandBase - класс-родитель всех исполнителей команд IRC.
 *
 * @version 0.5.2 2012-03-29
 * @author  Nikolay Kirdin
 */
public class IrcCommandBase implements IrcParamRegex {

    /** Название команды. */    
    public static final String commandName = "UNKNOWNCOMMAND";

    /** Формализованный результат исполнения команды. */
    protected Reply response = null;

    /** Список параметров. */
    protected LinkedList<String> pList;

    /** Признак наличия секции "trailing" в команде. */
    protected boolean trailing;

    /** Репозитарий. */
    protected DB db;

    /** Клиент - источник команды. */
    protected User client = null;

    /** 
     * Признак исполнимости комманды с данным набором параметров.
     * true - объект с командой можно исполнять (все параметры 
     * соответствуют спецификациям). false - объект с командой 
     * исполнять нельзя (было обнаружено нарушении спецификаций).   
     */
    protected boolean executable = false;
    
    /** Список с результатами исполнения команды. */
    protected LinkedList<IrcCommandReport> reportList = 
            new LinkedList<IrcCommandReport>();

    protected IrcCommandBase() {}

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
            DB db) throws IrcSyntaxException {}

    /** Исполнитель команды. 
     * @throws IrcExecutionException если во время исполнения команды
     * произойдет ошибка.
     */
    public void run() throws IrcExecutionException {}

    /**
     * Получение названия команды.
     * @return название команды.
     */
    public String getCommandName() {
        return commandName;
    }

    /**
     * Задание репозитария.
     * @param db репозитарий.
     */
    public void setDb(DB db) {
        this.db = db;
    }

    /**
     * Задание источника команды.
     * @param client источник команды.
     */
    public void setClient(User client) {
        this.client = client;
    }

    /**
     * Получение источника команды.
     * @return источник команды.
     */
    public User getClient() {
        return client;
    }

    /**
     * Установка признака выполняемоемости команды.
     * @param executable признак выполняемоемости команды.
     */
    public void setExecutable(boolean executable) {
        this.executable = executable;
    }

    /**
     * Получение признака выполняемоемости команды.
     * @return признак выполняемоемости команды.
     */
    public boolean isExecutable() {
        return executable;
    }
    
    public LinkedList<IrcCommandReport> getReportList() {
        return reportList;
    } 
    
    /** 
     * Восстанавливает строку параметров команды на основе списка 
     * параметров.
     * @param parameterList список параметров.
     * @param trailing признак присутвися в команди секции "trailing". 
     */
    public static String makeParameterString(
            LinkedList<String> parameterList, boolean trailing) {
    
        String result = "";
        
        String lastElement = parameterList.get(parameterList.size() - 1);

        for (int i = 0; i < parameterList.size() - 1; i++) {
            result = result + parameterList.get(i) + " ";
        }

        if (trailing) {
            result = result + ":" + lastElement;
        } else {
            result = result + lastElement;
        }

        return result;
    }
    
    /** 
     * Проверяет строку на соответствие заданному регулярному выражению.
     * @param s проверяемая строка.
     * @param regex строка с регулярным выражением.
     * @throws IrcSyntaxException в том случае, если параметр являеется 
     * null, либо строка не соответствует регулярному выражению.
     * Если параметр являеется null, то аргументом исключения является 
     * строка "ERR_NEEDMOREPARAMS", если строка не соответствует 
     * регулярному выражению, то аргументом исключения является строка 
     * "ERR_UNKNOWNCOMMAND".
     * @return s проверяемая строка.
     */
    public static String check(String s, String regex) 
            throws IrcSyntaxException {
        if (s == null) {
            throw new IrcSyntaxException("ERR_NEEDMOREPARAMS");
        }
        if (!s.matches(regex)) {
            throw new IrcSyntaxException("ERR_UNKNOWNCOMMAND");
        }
        return s;
    }
    
    /** 
     * Проверяет строку на соответствие заданному регулярному выражению.
     * @param s проверяемая строка.
     * @param regex скомпилированное регулярное выражение.
     * @throws IrcSyntaxException в том случае, если параметр являеется 
     * null, либо строка не соответствует регулярному выражению.
     * Если параметр являеется null, то аргументом исключения является 
     * строка "ERR_NEEDMOREPARAMS", если строка не соответствует 
     * регулярному выражению, то аргументом исключения является строка 
     * "ERR_UNKNOWNCOMMAND".
     * @return проверяемая строка.
     */
    public static String check(String s, Pattern regex)
            throws IrcSyntaxException {
        if (s == null) {
            throw new IrcSyntaxException("ERR_NEEDMOREPARAMS");
        }
        if (!regex.matcher(s).matches()) {
            throw new IrcSyntaxException("ERR_UNKNOWNCOMMAND");
        }
        return s;
    }

    /** 
     * Проверяет символы режима клиента и операции на допустимость. 
     * @param op режим ({@link UserMode}).
     * @param flag операция ({@link ModeOperation}).
     * @throws IrcSyntaxException с аргументом "ERR_UMODEUNKNOWNFLAG" - 
     * в том случае, если используются недопустимые комбинации для 
     * режима и операции. 
     * @return объект, хранящий в себе проверенную комбинацию режима и 
     * операции.
     */
    public static UserModeCarrier check(char op, char flag) 
            throws IrcSyntaxException {

        UserModeCarrier result = null;

        EnumSet<UserMode> approveADD = EnumSet.of(UserMode.i
                , UserMode.w
                , UserMode.r
                , UserMode.s);

        EnumSet<UserMode> approveREMOVE = EnumSet.of(UserMode.i
                , UserMode.w
                , UserMode.o
                , UserMode.O
                , UserMode.s);

        UserMode currentUserMode = null;

        if (!(flag == '+' || flag == '-')) {
            throw new IrcSyntaxException("ERR_UMODEUNKNOWNFLAG");
        }

        for (UserMode usm : UserMode.values()) {
            if (op == usm.getMode()) {
                currentUserMode = usm;
                break;
            }
        }

        if (currentUserMode == null || !(
                flag == '+' && approveADD.contains(currentUserMode) || 
                flag == '-' && approveREMOVE.contains(currentUserMode))) {
            throw new IrcSyntaxException("ERR_UMODEUNKNOWNFLAG");
        }

        if (flag == '+' && approveADD.contains(currentUserMode)) {
            result = new UserModeCarrier(
                    currentUserMode, ModeOperation.ADD);
        } else if (flag == '-' && approveREMOVE.contains(
                currentUserMode)) {
            result = new UserModeCarrier(
                    currentUserMode, ModeOperation.REMOVE);
        } else {
            throw new Error("ERR_UMODEUNKNOWNFLAG") ;
        }

        return result;
    }

    /**
     * Проверяет символы режима канала и операции на допустимость. 
     * @param op режим ({@link ChannelMode}).
     * @param flag операция ({@link ModeOperation}).
     * @param parameter -  строка с аргументами.
     * @throws IrcSyntaxException с аргументом "ERR_UNKNOWNMODE:" - 
     * в том случае, если используются недопустимые комбинации для 
     * режима и операции. 
     * @return объект, хранящий проверенную комбинацию режима, операции
     * и строку аргументов.
     */
    public static ChannelModeCarrier check(char op, char flag,
            String parameter) throws IrcSyntaxException {

        ChannelModeCarrier result = null;

        ChannelMode currentChannelMode = null;

        for (ChannelMode chm : ChannelMode.values()) {
            if (op == chm.op) {
                currentChannelMode = chm;
                break;
            }
        }

        if (currentChannelMode == null) {
            throw new IrcSyntaxException("ERR_UNKNOWNMODE:" + op);
        }

        switch (flag) {

        case ' ':
            result = new ChannelModeCarrier(
                    currentChannelMode, ModeOperation.LIST, "");
            break;

        case '+':

            switch (op) {

            case 'O':
                throw new IrcSyntaxException("ERR_UNKNOWNMODE:" + op);
                //             break;

            case 'o':
                result = new ChannelModeCarrier(currentChannelMode, 
                        ModeOperation.ADD,
                        check(parameter, nickNameRegex));
                break;

            case 'v':
                result = new ChannelModeCarrier(currentChannelMode,
                        ModeOperation.ADD,
                        check(parameter, nickNameRegex));
                break;

            case 'k':
                result = new ChannelModeCarrier(currentChannelMode,
                        ModeOperation.ADD,
                        check(parameter, userPassword));
                break;

            case 'l':
                result = new ChannelModeCarrier(currentChannelMode,
                        ModeOperation.ADD,
                        check(parameter, numberRegex));
                break;

            case 'b':
                result = new ChannelModeCarrier(currentChannelMode,
                        ModeOperation.ADD,
                        check(parameter, userMaskRegex));
                break;

            case 'e':
                result = new ChannelModeCarrier(currentChannelMode,
                        ModeOperation.ADD,
                        check(parameter, userMaskRegex));
                break;

            case 'I':
                result = new ChannelModeCarrier(currentChannelMode,
                        ModeOperation.ADD,
                        check(parameter, userMaskRegex));
                break;

            default:
                result = new ChannelModeCarrier(currentChannelMode,
                        ModeOperation.ADD, null);
                break;
            }

            break;

        case '-':

            switch (op) {

            case 'O':
                throw new IrcSyntaxException("ERR_UNKNOWNMODE:" + op);
                //             break;

            case 'o':
                result = new ChannelModeCarrier(currentChannelMode,
                        ModeOperation.REMOVE,
                        check(parameter, nickNameRegex));
                break;

            case 'v':
                result = new ChannelModeCarrier(currentChannelMode,
                        ModeOperation.REMOVE,
                        check(parameter, nickNameRegex));
                break;

            case 'b':
                result = new ChannelModeCarrier(currentChannelMode,
                        ModeOperation.REMOVE,
                        check(parameter, userMaskRegex));
                break;

            case 'e':
                result = new ChannelModeCarrier(currentChannelMode,
                        ModeOperation.REMOVE,
                        check(parameter, userMaskRegex));
                break;

            case 'I':
                result = new ChannelModeCarrier(currentChannelMode,
                        ModeOperation.REMOVE,
                        check(parameter, userMaskRegex));
                break;

            default:
                result = new ChannelModeCarrier(currentChannelMode,
                        ModeOperation.REMOVE, null);
                break;
            }

            break;

        default:
            throw new IrcSyntaxException("ERR_UNKNOWNMODE:" + op);
        }
        return result;
    }

    /** 
     * Проверяет строку на соответствие заданному регулярному выражению.
     * @param s проверяемая строка.
     * @param regex строка с регулярным выражением.
     * @return true если проверяемая строка соответствует регулярному 
     * выражению.
     */
    public static boolean isIt(String s, String regex) {
        return s != null && s.matches(regex);
    }

    /** 
     * Проверяет строку на соответствие заданному регулярному выражению.
     * @param s проверяемая строка.
     * @param regex скомпилированное регулярное выражение.
     * @return true если проверяемая строка соответствует регулярному 
     * выражению.
     */
    public static boolean isIt(String s, Pattern regex) {
        return s != null && regex.matcher(s).matches();
    }

    /** 
     * Создает набор сообщений после успешной регистрации клиента.
     * @param db репозитарий.
     * @param client клиент.
     */    
    protected void welcomeMsg(DB db, User client) {

        String remark = null;

        remark = Reply.makeText(Reply.RPL_WELCOME, 
                client.getNickname(),
                client.getNickname(),
                client.getUsername(),
                client.getIrcServer().getHostname());
        client.send(Globals.thisIrcServer.get(), remark);

        remark = Reply.makeText(Reply.RPL_YOURHOST, 
                client.getNickname(),
                Globals.thisIrcServer.get().getHostname(),
                Constants.SERVER_VERSION);
        client.send(Globals.thisIrcServer.get(), remark);

        remark = Reply.makeText(Reply.RPL_CREATED, 
                client.getNickname(),
                Constants.DATE_CREATED);
        client.send(Globals.thisIrcServer.get(), remark);

        remark = Reply.makeText(Reply.RPL_MYINFO, 
                client.getNickname(),
                Globals.thisIrcServer.get().getHostname(),
                Constants.SERVER_VERSION,
                Constants.USER_MODES,
                Constants.CHANNEL_MODES);
        client.send(Globals.thisIrcServer.get(), remark);

        remark = Reply.makeText(Reply.RPL_ISUPPORT, 
                client.getNickname(),
                "PREFIX=" + Constants.PREFIX +
                " " + "CHANTYPES=" + Constants.CHANTYPES +
                " " + "MODES=" + Constants.MODES +
                " " + "CHANLIMIT=" + Constants.CHANTYPES + ":" + 
                Constants.CHANLIMIT +
                " " + "NICKLEN=" + Constants.NICKLEN +
                " " + "TOPIC_LEN=" + Constants.TOPIC_LEN +
                " " + "KICKLEN=" + Constants.KICKLEN +
                " " + "MAXLIST=" + "beI" + ":" + Constants.MAXLIST +
                " " + "CHANNELLEN=" + Constants.CHANNELLEN +
                " " + "CHANMODES=" + Constants.CHANMODES +
                " " + "EXCEPTS=" + Constants.EXCEPTS +
                " " + "INVEX=" + Constants.INVEX +
                " " + "CASEMAPPING=" + Constants.CASEMAPPING);
        client.send(Globals.thisIrcServer.get(), remark);
    }
    
    
    /**
     * Форвардинг сообщения. (Не реализовано.)
     * @param servernameMask маска для целевых серверов.
     * @param content форвардируемое сообщение.
     * @param client источник сообщения.
     * @param db репозитарий.
     */
    protected void forwardWithMask(String servernameMask,
            String content,
            User client,
            DB db) {

        LinkedHashSet<IrcServer> ircServerSet = 
                new LinkedHashSet<IrcServer>();

        LinkedHashSet<String> ircServerNicknameSet = 
                db.getIrcServernameSet();

        for (String name : ircServerNicknameSet) {
            if (IrcMatcher.match(servernameMask, name)) {
                ircServerSet.add(db.getIrcServer(name));
            }
        }

        //ircServerSet.remove(Globals.thisIrcServer.get());

        if (ircServerSet.isEmpty()) {
            String remark = Reply.makeText(
                    Reply.ERR_NOSUCHSERVER, 
                    client.getNickname(), 
                    servernameMask);
            client.send(
                    new IrcCommandReport(remark,
                    client, Globals.thisIrcServer.get()));
        } else {
            for (IrcServer ircServer : ircServerSet ) {
                ircServer.send(
                        new IrcCommandReport(content, ircServer, client));
            }
        }
    }

    /**
     * Форвардинг сообщения. (Не реализовано.)
     * @param servernameMask маска для целевых серверов.
     * @param content форвардируемое сообщение.
     * @param client источник сообщения.
     * @param db репозитарий.
     */
    protected void forwardWithMask(DB db, User client, String content, 
                String servernameMask) {

        LinkedHashSet<IrcServer> ircServerSet = 
                new LinkedHashSet<IrcServer>();

        LinkedHashSet<String> ircServerNicknameSet = 
                db.getIrcServernameSet();

        for (String name : ircServerNicknameSet) {
            if (IrcMatcher.match(servernameMask, name)) {
                ircServerSet.add(db.getIrcServer(name));
            }
        }
        if (ircServerSet.isEmpty()) {
            client.send(errNoSuchServer(client, servernameMask));
        } else {
            for (IrcServer ircServer : ircServerSet ) {
                client.send(
                        new IrcCommandReport(content, ircServer, client));
            }
        }
    }
    
    /** 
     * Создает сообщение соответствующее  формализованному сообщению 
     * {@link Reply#ERR_NEEDMOREPARAMS}. 
     * @param requestor источник команды.
     * @param commandName название команды.
     * @return объект с сообщением.
     */    
    protected static IrcCommandReport errNeedMoreParams(
            IrcTalker requestor, String commandName) {

        String remark = Reply.makeText(
                Reply.ERR_NEEDMOREPARAMS,
                requestor.getNickname(),
                commandName);

        return new IrcCommandReport(remark, requestor,
                Globals.thisIrcServer.get());
    }

    /** 
     * Создает сообщение соответствующее  формализованному сообщению 
     * {@link Reply#ERR_UNKNOWNCOMMAND}. 
     * @param requestor источник команды.
     * @param commandName название команды.
     * @return объект с сообщением.
     */        
    protected static IrcCommandReport errUnknownCommand(
            IrcTalker requestor, String commandName) {

        String remark = Reply.makeText(
                Reply.ERR_UNKNOWNCOMMAND,
                requestor.getNickname(),
                commandName);

        return new IrcCommandReport(remark, requestor,
                Globals.thisIrcServer.get());
    }

    /** 
     * Создает сообщение соответствующее  формализованному сообщению 
     * {@link Reply#ERR_UMODEUNKNOWNFLAG}. 
     * @param requestor источник команды.
     * @return объект с сообщением.
     */        
    protected static IrcCommandReport errUModeUnknownFlag(
            IrcTalker requestor) {

        String remark = Reply.makeText(
                Reply.ERR_UMODEUNKNOWNFLAG,
                requestor.getNickname());

        return new IrcCommandReport(remark, requestor,
                Globals.thisIrcServer.get());
    }

    /** 
     * Создает сообщение соответствующее  формализованному сообщению 
     * {@link Reply#ERR_NOSUCHNICK}. 
     * @param requestor источник команды.
     * @param nickname никнэйм.
     * @return с сообщением.
     */        
    protected static IrcCommandReport errNoSuchNick(IrcTalker requestor, 
            String nickname) {
    
        String remark = Reply.makeText(Reply.ERR_NOSUCHNICK, 
                requestor.getNickname(),
                nickname);

        return new IrcCommandReport(remark, requestor,
                Globals.thisIrcServer.get());
    }

    /** 
     * Создает сообщение соответствующее  формализованному сообщению 
     * {@link Reply#ERR_NOTREGISTERED}. 
     * @param requestor источник команды.
     * @return объект с сообщением.
     */        
    protected static IrcCommandReport errNotRegistered(
            IrcTalker requestor) {
        
        String remark = Reply.makeText(
                Reply.ERR_NOTREGISTERED, 
                requestor.getNickname());

        return new IrcCommandReport(remark, requestor,
                Globals.thisIrcServer.get());
    }
    
    /** 
     * Создает сообщение соответствующее  формализованному сообщению 
     * {@link Reply#ERR_NOSUCHCHANNEL}. 
     * @param requestor источник команды.
     * @param channelName имя канала.
     * @return объект с сообщением.
     */        
    protected static IrcCommandReport errNoSuchChannel(
            IrcTalker requestor,
            String channelName) {
        
        String remark = Reply.makeText(
                Reply.ERR_NOSUCHCHANNEL, 
                requestor.getNickname(), 
                channelName);

        return new IrcCommandReport(remark, requestor,
                Globals.thisIrcServer.get());
    }
    
    /** 
     * Создает сообщение соответствующее  формализованному сообщению 
     * {@link Reply#ERR_NOTONCHANNEL}. 
     * @param requestor источник команды.
     * @param channelName имя канала.
     * @return объект с сообщением.
     */        
    protected static IrcCommandReport errNotOnChannel(
            IrcTalker requestor,
            String channelName) {
        
        String remark = Reply.makeText(
                Reply.ERR_NOTONCHANNEL, 
                requestor.getNickname(), 
                channelName);

        return new IrcCommandReport(remark, requestor,
                Globals.thisIrcServer.get());
    }
    
    /** 
     * Создает сообщение соответствующее  формализованному сообщению 
     * {@link Reply#ERR_RESTRICTED}. 
     * @param requestor источник команды.
     * @return объект с сообщением.
     */        
    protected static IrcCommandReport errUserRestricted(
            IrcTalker requestor) {
        
        String remark = Reply.makeText(Reply.ERR_RESTRICTED, 
                requestor.getNickname());

        return new IrcCommandReport(remark, requestor,
                Globals.thisIrcServer.get());
    }
    
    /** 
     * Создает сообщение соответствующее  формализованному сообщению 
     * {@link Reply#ERR_CHANOPRIVSNEEDED}. 
     * @param requestor источник команды.
     * @param channelName имя канала.
     * @return объект с сообщением.
     */        
    protected static IrcCommandReport errNotChannelOp(
            IrcTalker requestor,
            String channelName) {
        
        String remark = Reply.makeText(
                Reply.ERR_CHANOPRIVSNEEDED, 
                requestor.getNickname(),
                channelName);

        return new IrcCommandReport(remark, requestor,
                Globals.thisIrcServer.get());
    }
    
    /** 
     * Создает сообщение соответствующее  формализованному сообщению 
     * {@link Reply#ERR_USERONCHANNEL}. 
     * @param requestor источник команды.
     * @param nickname никнэйм.
     * @param channelName имя канала.
     * @return объект с сообщением.
     */        
    protected static IrcCommandReport errOnChannel(IrcTalker requestor,
            String nickname,  String channelName) {
        
        String remark = Reply.makeText(
                Reply.ERR_USERONCHANNEL, 
                requestor.getNickname(),
                nickname,
                channelName);

        return new IrcCommandReport(remark, requestor,
                Globals.thisIrcServer.get());
    }

    /** 
     * Создает сообщение соответствующее  формализованному сообщению 
     * {@link Reply#ERR_FILEERROR}. 
     * @param requestor источник команды.
     * @param arg1 первый аргумент.
     * @param arg2 второй аргумент.
     * @return объект с сообщением.
     */        
    protected static IrcCommandReport errFileError(IrcTalker requestor,
            String arg1, String arg2) {
        
        String remark = Reply.makeText(Reply.ERR_FILEERROR, 
                requestor.getNickname(),
                arg1,
                arg2);

        return new IrcCommandReport(remark, requestor,
                Globals.thisIrcServer.get());
    }

    /** 
     * Создает сообщение соответствующее  формализованному сообщению 
     * {@link Reply#ERR_NOPRIVILEGES}. 
     * @param requestor источник команды.
     * @return объект с сообщением.
     */        
    protected static IrcCommandReport errNoPrivileges(
            IrcTalker requestor) {
        
        String remark = Reply.makeText(
                Reply.ERR_NOPRIVILEGES, 
                requestor.getNickname());

        return new IrcCommandReport(remark, requestor,
                Globals.thisIrcServer.get());
    }

    /** 
     * Создает сообщение соответствующее  формализованному сообщению 
     * {@link Reply#ERR_NOSUCHSERVER}. 
     * @param requestor источник команды.
     * @param servername имя сервера.
     * @return объект с сообщением.
     */        
    protected static IrcCommandReport errNoSuchServer(
            IrcTalker requestor, 
            String servername) {
        
        String remark = Reply.makeText(
                Reply.ERR_NOSUCHSERVER, 
                requestor.getNickname(), 
                servername);

        return new IrcCommandReport(remark, requestor,
                Globals.thisIrcServer.get());
    }
    
    /** 
     * Создает сообщение соответствующее  формализованному сообщению 
     * {@link Reply#ERR_ERRONEUSNICKNAME}. 
     * @param requestor источник команды.
     * @return объект с сообщением.
     */        
    protected static IrcCommandReport errOnUsingNickname(
            IrcTalker requestor, 
            String nickname) {
        
        String remark = Reply.makeText(
                Reply.ERR_ERRONEUSNICKNAME,
                requestor.getNickname(),
                nickname);

        return new IrcCommandReport(remark, requestor,
                Globals.thisIrcServer.get());
    }
    
    /** 
     * Создает сообщение соответствующее  формализованному сообщению 
     * {@link Reply#ERR_NOORIGIN}. 
     * @param requestor источник команды.
     * @return объект с сообщением.
     */        
    protected static IrcCommandReport errNoOrigin(IrcTalker requestor) {
        
        String remark = Reply.makeText(Reply.ERR_NOORIGIN,
                requestor.getNickname());

        return new IrcCommandReport(remark, requestor,
                Globals.thisIrcServer.get());
    }
        
    /** 
     * Создает сообщение соответствующее  формализованному сообщению 
     * {@link Reply#ERR_NOTEXTTOSEND}. 
     * @param requestor источник команды.
     * @return объект с сообщением.
     */        
    protected static IrcCommandReport errNoTextToSend(
            IrcTalker requestor) {
        
        String remark = Reply.makeText(Reply.ERR_NOTEXTTOSEND,
                requestor.getNickname());

        return new IrcCommandReport(remark, requestor,
                Globals.thisIrcServer.get());
    }
    
    /** 
     * Создает сообщение соответствующее  формализованному сообщению 
     * {@link Reply#ERR_NORECIPIENT}. 
     * @param requestor источник команды.
     * @param commandName название команды.
     * @return объект с сообщением.
     */        
    protected static IrcCommandReport errNoRecipient(IrcTalker requestor, 
            String commandName) {
        
        String remark = Reply.makeText(Reply.ERR_NORECIPIENT,
                requestor.getNickname(),
                commandName);

        return new IrcCommandReport(remark, requestor,
                Globals.thisIrcServer.get());
    }
    
    /** 
     * Создает сообщение соответствующее  формализованному сообщению 
     * {@link Reply#ERR_ALREADYREGISTRED}. 
     * @param requestor источник команды.
     * @return объект с сообщением.
     */        
    protected static IrcCommandReport errAlreadyRegistered(IrcTalker
            requestor) {
        
            String remark = Reply.makeText(
                    Reply.ERR_ALREADYREGISTRED,
                    requestor.getNickname());

        return new IrcCommandReport(remark, requestor,
                Globals.thisIrcServer.get());
    }
    /**
     * Создание формализованного ответа типа
     * {@link Reply#RPL_WHOISSERVER}.
     * @param ircTalker отправитель.
     * @param nickname
     * @param serverHostname
     * @param serverInfo 
     * @return объект класса {@link IrcCommandReport} с формализованным 
     * ответом.
     */
    protected static IrcCommandReport rplWhoIsServer(IrcTalker ircTalker, 
            String nickname, String serverHostname, String serverInfo) {
        String remark = Reply.makeText(Reply.RPL_WHOISSERVER,
                ircTalker.getNickname(),
                nickname,
                serverHostname,
                serverInfo);
        return new IrcCommandReport(remark, ircTalker,
                Globals.thisIrcServer.get());
    }
    
    /**
     * Создание формализованного ответа типа
     * {@link Reply#RPL_NOTOPIC}.
     * @param ircTalker отправитель.
     * @param channel
     * @return объект класса {@link IrcCommandReport} с формализованным 
     * ответом.
     */
    public static IrcCommandReport rplNoTopic(IrcTalker ircTalker, 
            IrcChannel channel) {
        String remark = Reply.makeText(Reply.RPL_NOTOPIC,
                ircTalker.getNickname(),
                channel.getNickname());
        return new IrcCommandReport(remark, ircTalker,
                Globals.thisIrcServer.get());
    }
    /**
     * Создание формализованного ответа типа
     * {@link Reply#RPL_TOPIC}.
     * @param ircTalker отправитель.
     * @param channel
     * @param topic
     * @return объект класса {@link IrcCommandReport} с формализованным 
     * ответом.
     */
    public static IrcCommandReport rplTopic(IrcTalker ircTalker, 
            IrcChannel channel, String topic) {
        String remark = Reply.makeText(Reply.RPL_TOPIC,
                ircTalker.getNickname(),
                channel.getNickname(),
                topic);
        return new IrcCommandReport(remark, ircTalker,
                Globals.thisIrcServer.get());
    }
    /** 
     * Создает сообщение соответствующее  формализованному сообщению 
     * {@link Reply#RPL_NAMREPLY}. 
     * @param requestor источник команды.
     * @param nick никнэйм.
     * @param channelStatus статус канала.
     * @param channelName имя канала.
     * @param userStatus статус пользователя.
     * @param userNickname никнэйм пользователя.
     * @return объект с сообщением.
     */        
    public static IrcCommandReport rplNameReply(IrcTalker requestor,
            String nick, String channelStatus, String channelName, 
            String userStatus, String userNickname) {
        
        String remark = Reply.makeText(Reply.RPL_NAMREPLY,
                nick,
                channelStatus,
                channelName,
                userStatus,
                userNickname);

        return new IrcCommandReport(remark, requestor,
                Globals.thisIrcServer.get());
    }
    
    /** 
     * Создает сообщение соответствующее  формализованному сообщению 
     * {@link Reply#RPL_ENDOFNAMES}. 
     * @param requestor источник команды.
     * @param channelName имя канала.
     * @return объект с сообщением.
     */        
    public static IrcCommandReport rplEndOfNames(IrcTalker requestor,
            String channelName) {
        
        String remark = Reply.makeText(Reply.RPL_ENDOFNAMES,
                requestor.getNickname(),
                channelName);

        return new IrcCommandReport(remark, requestor,
                Globals.thisIrcServer.get());
    }
    
    /** 
     * Предоставление символов, обозначающих режимы члена канала.
     * Режимы члена канала обозначаются следующим образом:
     * <UL>
     * <LI> оператор канала  ({@link ChannelMode#o}) или создатель 
     * канала ({@link ChannelMode#O}) обозначается символом "@";
     * <LI> член канала, обладающий правом посылать сообщения в 
     * модерируемый канал ({@link ChannelMode#v}), обозначается 
     * символом "+"; </UL>
     * @return строка с одним из этих символов.
     */
    public static String getUserStatus(
            EnumSet <ChannelMode> channelMode) {
        String result = null;
        
        if (channelMode.contains(ChannelMode.o) ||
                channelMode.contains(ChannelMode.O)) {
            result = "@";
        } else if (channelMode.contains(ChannelMode.v)) {
            result = "+";
        } else {
            result = "";
        }
        return result;
    }
    /** 
     * Создает сообщение соответствующее  формализованному сообщению 
     * {@link Reply#ERR_NICKNAMEINUSE}. 
     * @param requestor источник команды.
     * @param nickname никнэйм.
     * @return объект с сообщением.
     */        
    public static IrcCommandReport errNicknameInUse(IrcTalker requestor, 
            String nickname) {
        
        String remark = Reply.makeText(
                Reply.ERR_NICKNAMEINUSE,
                requestor.getNickname(),
                nickname);

        return new IrcCommandReport(remark, requestor,
                Globals.thisIrcServer.get());
    }
    
    /**
     * Создание формализованного ответа типа
     * {@link Reply#ERR_TOOMANYCHANNELS}.
     * @param ircTalker отправитель.
     * @param channelName имя канала.
     * @return объект класса {@link IrcCommandReport} с формализованным 
     * ответом.
     */
    public static IrcCommandReport errTooManyChannels(
            IrcTalker ircTalker, 
            String channelName) {
        String remark = Reply.makeText(
                Reply.ERR_TOOMANYCHANNELS,  
                ircTalker.getNickname(), channelName);
        
        return new IrcCommandReport(remark, ircTalker,
                Globals.thisIrcServer.get());
    }
        
    /**
     * Создание формализованного ответа типа
     * {@link Reply#ERR_TOOMANYCHANNELS}.
     * @param ircTalker отправитель.
     * @param channelName имя канала.
     * @return объект класса {@link IrcCommandReport} с формализованным 
     * ответом.
     */
    public static IrcCommandReport errCannotSendToChan(
            IrcTalker ircTalker, 
            String channelName) {
        String remark = Reply.makeText(
                Reply. ERR_CANNOTSENDTOCHAN,
                ircTalker.getNickname(),
                channelName);
        
        return new IrcCommandReport(remark, ircTalker,
                Globals.thisIrcServer.get());
    }
    /** 
     * Создает сообщение соответствующее  формализованному сообщению 
     * {@link Reply#ERR_USERNOTINCHANNEL}. 
     * @param requestor источник команды.
     * @param nickname никнэйм.
     * @param channelName имя канала.
     * @return объект с сообщением.
     */        
    public static IrcCommandReport errNotInChannel(IrcTalker requestor,
            String nickname, String channelName) {
        
        String remark = Reply.makeText(
                Reply.ERR_USERNOTINCHANNEL, 
                requestor.getNickname(),
                nickname, channelName);

        return new IrcCommandReport(remark, requestor,
                Globals.thisIrcServer.get());
    }
    
}
