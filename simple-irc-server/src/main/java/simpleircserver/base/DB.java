package simpleircserver.base;
/*
 * 
 * DB 
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
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

import simpleircserver.channel.IrcChannel;
import simpleircserver.channel.MonitorIrcChannel;
import simpleircserver.config.IrcAdminConfig;
import simpleircserver.config.IrcInterfaceConfig;
import simpleircserver.config.IrcOperatorConfig;
import simpleircserver.config.IrcServerConfig;
import simpleircserver.connection.Connection;
import simpleircserver.parser.Reply;
import simpleircserver.talker.IrcTalker;
import simpleircserver.talker.server.IrcServer;
import simpleircserver.talker.service.Service;
import simpleircserver.talker.user.DriedUser;
import simpleircserver.talker.user.User;

/**
 * Репозитарий в котором хранятся разделяемые данные (очереди, 
 * ассоциативные массивы и т.д.). 
 *  
 * @version 0.5.1 2012-03-27
 * @version 0.5.3 2015-11-05 Program units were moved from default package into packages with names. Unit tests were added.
 * @author  Nikolay Kirdin
 */

public class DB {
    
    /** 
     * Максимальный размер ассоциативного массива, хранящего данные об 
     * обычных клиентах IRC.
     */
    public AtomicInteger maxUserMapSize = 
            new AtomicInteger(Constants.MAX_SERVER_CLIENTS);
    
    /** 
     * Максимальный размер ассоциативного массива, хранящего данные обо 
     * всех каналах IRC.
     */
    public AtomicInteger maxChannelMapSize = 
            new AtomicInteger(Constants.MAX_CHANNEL_NUMBER);
    
    /** 
     * Максимальный размер ассоциативного массива, хранящего данные об 
     * всех клиентах - сервисах IRC.
     */
    public AtomicInteger maxServiceMapSize = 
            new AtomicInteger(Constants.MAX_SERVER_CLIENTS);
    
    /** 
     * Максимальный размер ассоциативного массива, хранящего данные об 
     * всех клиентах - серверах IRC.
     */
    public AtomicInteger maxIrcServerMapSize = 
            new AtomicInteger(Constants.MAX_SERVER_CLIENTS);
    
    /** 
     * Максимальный размер ассоциативного массива, хранящего данные обо 
     * всех подключениях IRC.
     */
    public AtomicInteger maxConnectionListSize = 
            new AtomicInteger(Constants.MAX_SERVER_CLIENTS);
    
    /** 
     * Максимальный размер ассоциативного массива, хранящий 
     * истории никнэймов. 
     */
    public AtomicInteger maxNickHistoryMapSize = 
            new AtomicInteger(2 * Constants.MAX_SERVER_CLIENTS);

    /** 
     * Максимальный размер списка истории конкретного никнэйма.
     */
    public AtomicInteger maxNickHistorySize = new AtomicInteger(100);

    /** 
     * Максимальный размер ассоциативного массива, хранящий 
     * учетную информацию операторов. 
     */
    public AtomicInteger maxIrcOperatorConfigMapSize = 
            new AtomicInteger(Constants.MAX_OPERATOR_NUMBER);

    /** 
     * Ассоциативный массив, хранящий данные об обычных клиентах IRC.
     * Ключем является никнэйм.
     */
    protected ConcurrentSkipListMap <String, User> userMap =
            new ConcurrentSkipListMap <String, User>();

    /** 
     * Ассоциативный массив, хранящий данные об всех клиентах IRC.
     * Ключем является никнэйм.
     */
    protected LinkedHashMap<String, IrcTalker> ircTalkerMap =
            new LinkedHashMap<String, IrcTalker>();

    /** 
     * Ассоциативный массив, хранящий данные о каналах IRC. 
     * Ключем является имя канала.
     */
    protected ConcurrentSkipListMap <String, IrcChannel> channelMap =
            new ConcurrentSkipListMap <String, IrcChannel>();

    /** 
     * Ассоциативный массив, хранящий данные о клиентах - сервисах IRC. 
     * Ключем является имя сервиса.
     */
    protected LinkedHashMap<String, Service> serviceMap =
            new LinkedHashMap<String, Service>();

    /** 
     * Ассоциативный массив, хранящий данные о клиентах - серверах IRC. 
     * Ключем является имя доменное имя сервера.
     */
    protected LinkedHashMap<String, IrcServer> ircServerMap =
            new LinkedHashMap<String, IrcServer>();

    /** 
     * Ассоциативный массив, хранящий истории никнэймов. 
     * Ключем является никнэйм.
     */
    @SuppressWarnings("serial")
    protected LinkedHashMap<String, ArrayList<DriedUser>> 
        nicknameHistoryMap =
            new LinkedHashMap <String, ArrayList<DriedUser>> () {
        protected boolean removeEldestEntry(Map.Entry
                <String, ArrayList<DriedUser>> eldest) {
            return size() > Math.min(Math.max(Constants.MIN_LIMIT,
                    maxNickHistoryMapSize.get()),
                    Constants.HARD_LIMIT) - 1;
        }
    };
    
    /** 
     * Список, хранящий данные данные о соединении. 
     */
    protected CopyOnWriteArrayList<Connection> 
            connectionList = new CopyOnWriteArrayList <Connection>();

    /** 
     * Ассоциативный массив, хранящий учетные данные операторов. 
     * Ключем является {@code <username>} оператора.
     */            
    protected LinkedHashMap<String, IrcOperatorConfig> 
            ircOperatorConfigMap =
            new LinkedHashMap<String, IrcOperatorConfig>();

    /** 
     * Объект, хранящий общую информацию о данном сервере IRC  и его 
     * администраторе.
     */            
    protected IrcAdminConfig ircAdminConfig = null;
    
    /** 
     * Объект, хранящий конфигурационные данные данного сервера IRC.
     */            
    protected IrcServerConfig ircServerConfig = null;
    
    /** 
     * Объект, хранящий конфигурационные данные сетевого интерфейса 
     * данного сервера IRC.
     */            
    protected IrcInterfaceConfig ircInterfaceConfig = null;

    /** 
     * Объект, хранящий конфигурируемые параметры для протоколирования 
     * сообщений клиентов.
                 
    protected IrcTranscriptConfig ircTranscriptConfig = null;
    */
    /** 
     * Метод, помещающий информацию об обычном клиенте IRC в 
     * ассоциативный массив. Помещение произойдет, если размер 
     * ассоциативного массива меньше максимального размера  
     * {@link #maxUserMapSize} и в массиве нет объекта с таким-же 
     * никнэймом. Ключами служат никнэймы, символы которых, приведены
     * к нижниму регистру.
     * @param requestor информация об обычном клиенте.
     * @return {@link Reply#RPL_OK} признак успеха выполнения 
     * метода, {@link Reply#ERR_FILEERROR} - превышено 
     * ограничение на количество клиентов, 
     * {@link Reply#ERR_NICKNAMEINUSE} - клиент с таким именем 
     * существует.
     */            
    public Reply register(User requestor) {
        Reply responseReply = null;        
        
        if (userMap.size() >= Math.min(Math.max(
                Constants.MIN_LIMIT,  maxUserMapSize.get()),
                Constants.HARD_LIMIT)) {
            responseReply = Reply.ERR_FILEERROR;
        } else {
            String key = 
                    requestor.getNickname().toLowerCase(Locale.ENGLISH);
            User value = userMap.putIfAbsent(key, requestor);
            if (value == null) {
                responseReply = Reply.RPL_OK;
            } else {
                responseReply = Reply.ERR_NICKNAMEINUSE;
            }
        }
        return responseReply;
    }

    /** 
     * Метод, помещающий краткую информацию об обычном клиенте IRC в 
     * ассоциативный массив историй. При помещении этой информации 
     * проверятся длина истории, если она длиннее, чем  
     * {@link #maxNickHistoryMapSize}, то старейшие записи удаляются. 
     * Ключами служат никнэймы, символы которых, приведены к нижниму 
     * регистру.
     * @param driedUser информация об обычном клиенте.
     */            
    public void register(DriedUser driedUser) {
        String key = driedUser.nickname.toLowerCase(Locale.ENGLISH);
        synchronized(nicknameHistoryMap) {
            ArrayList<DriedUser> value = nicknameHistoryMap.get(key);
            if (value == null) {
                value = new ArrayList<DriedUser>();
            }
            synchronized (value) {
                while (value.size() >= maxNickHistorySize.get()) {
                    value.remove(0);
                }    
                value.add(driedUser);
                nicknameHistoryMap.put(key, value);
            }
        }
    }
    
    /** 
     * Метод, помещающий информацию об канале IRC в ассоциативный 
     * массив. Помещение будет успешным, если размер массива меньше 
     * максимального размера массива {@link #maxChannelMapSize} и в 
     * массиве нет объекта с таким-же именем канала. Ключами служат 
     * имена каналов, символы которых, приведены к нижниму регистру.
     * @param channel информация о канале.
     * @return {@link Reply#RPL_OK} признак успеха выполнения 
     * метода, {@link Reply#ERR_FILEERROR} - превышено 
     * ограничение на количество каналов, 
     * {@link Reply#ERR_NICKNAMEINUSE} - канал с таким именем 
     * существует.
     */            
    public Reply register(IrcChannel channel) {
        Reply responseReply = null;        
        
        if (channelMap.size() >= Math.min(Math.max(
                Constants.MIN_LIMIT,  maxChannelMapSize.get()),
                Constants.MAX_CHANNEL_NUMBER)) {
            responseReply = Reply.ERR_FILEERROR;
        } else {
            String key = 
                    channel.getNickname().toLowerCase(Locale.ENGLISH);
            IrcChannel value = channelMap.putIfAbsent(key, channel);
            if (value == null) {
                responseReply = Reply.RPL_OK;
            } else {
                responseReply = Reply.ERR_NICKNAMEINUSE;
            }
        }
        return responseReply;
    }

    /** 
     * Метод, помещающий информацию об клиенте-сервисе IRC в 
     * ассоциативный массив. Помещение будет успешным, если размер 
     * массива меньше максимального размера массива 
     * {@link #maxServiceMapSize} и в массиве нет объекта с таким-же 
     * никнэймом. Ключами служат никнэймы, символы которых, приведены к 
     * нижниму регистру.
     * @param service информация о сервисе.
     * @return {@link Reply#RPL_OK} признак успеха выполнения 
     * метода, {@link Reply#ERR_FILEERROR} поместить объект в 
     * массив не удалось, {@link Reply#ERR_NICKNAMEINUSE} такой
     * объект уже находится в массиве. 
     */            
    public Reply register(Service service) {
        Reply responseReply = null;
        String key = service.getNickname().toLowerCase(Locale.ENGLISH);
        synchronized (service) {
            synchronized (serviceMap) {
                if (serviceMap.size() >= Math.min(Math.max(
                        Constants.MIN_LIMIT, maxServiceMapSize.get()), 
                        Constants.HARD_LIMIT)) {
                    responseReply = Reply.ERR_FILEERROR;
                } else if (serviceMap.containsKey(key)) {
                    responseReply = Reply.ERR_NICKNAMEINUSE;
                } else {
                    serviceMap.put(key, service);
                    responseReply = Reply.RPL_OK;
                }
            }
        }
        return responseReply;
    }

    /** 
     * Метод, помещающий информацию об клиенте-сервере IRC в 
     * ассоциативный массив. Помещение будет успешным, если размер 
     * массива меньше максимального размера массива 
     * {@link #maxIrcServerMapSize} и в массиве нет объекта с таким-же 
     * никнэймом. Ключами служат FQDN серверов, символы которых, 
     * приведены к нижниму регистру.
     * @param ircServer информация о клиенте-сервере IRC.
     * @return {@link Reply#RPL_OK} признак успеха выполнения 
     * метода, {@link Reply#ERR_FILEERROR} поместить объект в 
     * массив не удалось, {@link Reply#ERR_NOTOK} такой
     * объект уже находится в массиве. 
     */            
    public Reply register(IrcServer ircServer) {
        Reply responseReply = null;
        String key = ircServer.getHostname().toLowerCase(Locale.ENGLISH);
        synchronized (ircServer) {
            synchronized (ircServerMap) {
                if (ircServerMap.size() >= Math.min(Math.max(
                        Constants.MIN_LIMIT, maxIrcServerMapSize.get()), 
                        Constants.HARD_LIMIT)) {
                    responseReply = Reply.ERR_FILEERROR;
                } else if (ircServerMap.containsKey(key)) {
                    responseReply = Reply.ERR_NOTOK;
                } else {
                ircServerMap.put(key, ircServer);
                responseReply = Reply.RPL_OK;
                }
            }
        }
        return responseReply;
    }

    /** 
     * Метод, помещающий информацию об соединении в список. Помещение 
     * будет успешным, если размер списка меньше максимального размера 
     * {@link #maxConnectionListSize} и в списке нет объекта с таким-же
     * никнэймом.
     * @param connection информация о соединении IRC.
     * @return {@link Reply#RPL_OK} признак успеха выполнения 
     * метода,  {@link Reply#ERR_FILEERROR} превышено 
     * ограничение на количество соединений, 
     * {@link Reply#ERR_NOTOK} поместить объект в массив не 
     * удалось. 
     */            
    public Reply register(Connection connection) {
        Reply responseReply = null;        
        
        if (connectionList.size() >= Math.min(Math.max(
                Constants.MIN_LIMIT,  maxConnectionListSize.get()),
                Constants.HARD_LIMIT)) {
            responseReply = Reply.ERR_FILEERROR;
        } else {
            if (connectionList.addIfAbsent(connection)) {
                responseReply = Reply.RPL_OK;
            } else {
                responseReply = Reply.ERR_NOTOK;
            }
        }
        return responseReply;
    }


    /** 
     * Метод, удаляющий информацию об обычном клиенте IRC из 
     * ассоциативного массива. 
     * @param user информация об обычном клиенте IRC.
     * @return {@link Reply#RPL_OK} признак успеха выполнения 
     * метода, {@link Reply#ERR_NOTOK} такого объекта в массиве
     * нет. 
     */            
    public Reply unRegister(User user) {
        Reply responseReply = null;
        String key = user.getNickname().toLowerCase(Locale.ENGLISH);
        responseReply = unRegisterUser(key);
        return responseReply;
    }

    /** 
     * Метод, удаляющий информацию об обычном клиенте IRC из 
     * ассоциативного массива. 
     * @param nickname никнэйм обычного клиента IRC.
     * @return {@link Reply#RPL_OK} признак успеха выполнения 
     * метода, {@link Reply#ERR_NOTOK} такого объекта в массиве
     * нет. 
     */            
    public Reply unRegisterUser(String nickname) {
        Reply responseReply = null;
        String key = nickname.toLowerCase(Locale.ENGLISH);
        User user = getUser(key);
        if (user == null) {
            responseReply = Reply.ERR_NOTOK;
        } else {
            synchronized (user) {
                if (user == Globals.anonymousUser.get()) {
                    responseReply = Reply.ERR_NOTOK;
                } else if (userMap.remove(key) != null) {
                    responseReply = Reply.RPL_OK;
                } else {
                    responseReply = Reply.ERR_NOTOK;
                }
            }
        }
        return responseReply;
    }

    /** 
     * Метод, удаляющий информацию о канале IRC из ассоциативного 
     * массива. 
     * @param channel канале IRC.
     * @return {@link Reply#RPL_OK} признак успеха выполнения 
     * метода, {@link Reply#ERR_NOTOK} если объект удалить 
     * нельзя, {@link Reply#ERR_NOSUCHCHANNEL}, если такого 
     * канала в массиве нет. 
     */            
    public Reply unRegister(IrcChannel channel) {
        Reply responseReply = null;
        String key = channel.getNickname().toLowerCase(Locale.ENGLISH);
        if (channel instanceof MonitorIrcChannel) {
            responseReply = Reply.RPL_OK;
        } else if (channelMap.containsKey(key)) {
            synchronized (channel) {
                if (channel.isUserSetEmpty()) {
                    channelMap.remove(key);
                    responseReply = Reply.RPL_OK;
                } else {
                    responseReply = Reply.ERR_NOTOK;
                }
            }
        } else {
            responseReply = Reply.ERR_NOSUCHCHANNEL;
        }
        return responseReply;
    }

    /** 
     * Метод, удаляющий информацию о клиенте-сервисе IRC из 
     * ассоциативного массива. 
     * @param service клиент-сервис IRC.
     * @return {@link Reply#RPL_OK} признак успеха выполнения 
     * метода, {@link Reply#ERR_NOTOK} такого объекта в массиве
     * нет. 
     */            
    public Reply unRegister(Service service) {
        Reply responseReply = null;
        String key = service.getNickname().toLowerCase(Locale.ENGLISH);
        synchronized (service) {
            synchronized (serviceMap) {
                if (serviceMap.remove(key) != null){
                    responseReply = Reply.RPL_OK;
                } else {
                    responseReply = Reply.ERR_NOTOK;
                }
            }
        }
        return responseReply;
    }


    /** 
     * Метод, удаляющий информацию о клиенте-сервере IRC из 
     * ассоциативного массива. 
     * @param ircServer клиент-сервер IRC.
     * @return {@link Reply#RPL_OK} признак успеха выполнения 
     * метода, {@link Reply#ERR_NOTOK} такого объекта в массиве
     * нет. 
     */            
    public Reply unRegister(IrcServer ircServer) {
        Reply responseReply = null;
        String key = ircServer.getHostname().toLowerCase(Locale.ENGLISH);
        if (ircServer == Globals.anonymousIrcServer.get()) {
            responseReply = Reply.ERR_NOTOK;
        } else {
            synchronized (ircServer) {
                synchronized (ircServerMap) {
                    if (ircServerMap.remove(key) != null){
                        responseReply = Reply.RPL_OK;
                    } else {
                        responseReply = Reply.ERR_NOTOK;
                    }
                }
            }
        }
        return responseReply;
    }

    /** 
     * Метод, удаляющий информацию о соединении IRC из списка. 
     * @param connection соединение IRC.
     * @return {@link Reply#RPL_OK} признак успеха выполнения 
     * метода, {@link Reply#ERR_NOTOK} такого объекта в списке
     * нет. 
     */            
    public Reply unRegister(Connection connection) {
        Reply response = null;
        if (connection == Globals.nullConnection.get()) {
            response =  Reply.RPL_OK;
        } else if (connectionList.remove(connection) == true) {
            response = Reply.RPL_OK;
        } else {
            response = Reply.ERR_NOTOK;
        }
        return response;
    }

    /**
     * Проверка нахождения обычного клиента в массиве.
     * @param user обычный клиент.
     * @return true если этот объект находится в массиве.
     */
    public boolean isRegistered(User user) {
        return userMap.containsValue(user);
    }

    /**
     * Проверка нахождения обычного клиента-сервера IRC в массиве.
     * @param ircServer клиент-сервер IRC.
     * @return true если этот объект находится в массиве.
     */
    public boolean isRegistered(IrcServer ircServer) {
        synchronized (ircServerMap) {
            return ircServerMap.containsValue(ircServer);
        }
    }

    /**
     * Проверка нахождения обычного клиента-сервиса IRC в массиве.
     * @param service клиент-сервис IRC.
     * @return true если этот объект находится в массиве.
     */
    public boolean isRegistered(Service service) {
        synchronized (serviceMap) {
            return serviceMap.containsValue(service);
        }
    }

    /**
     * Проверка нахождения канала IRC в массиве.
     * @param channel канал IRC.
     * @return true если этот объект находится в массиве.
     */
    public boolean isRegistered(IrcChannel channel) {
        return channelMap.containsValue(channel);
    }

    /**
     * Проверка нахождения соединения IRC в списке.
     * @param connection соединение IRC.
     * @return true если этот объект находится в списке.
     */
    public boolean isRegistered(Connection connection) {
        boolean result = false;
        if (connection == Globals.nullConnection.get()) {
            result =  true;
        } else {
            result = connectionList.contains(connection);
        }
        return result;
    }

    /**
     * Получение клиента-сервера IRC из массива.
     * @param ircServername доменное имя клиента-сервера IRC.
     * @return IrcServer если этот объект находится в массиве, 
     * null - если объекта с таким доменном имени в массиве нет.
     */
    public IrcServer getIrcServer(String ircServername) {
        String key = ircServername.toLowerCase(Locale.ENGLISH);
        synchronized (ircServerMap) {
            return ircServerMap.get(key);
        }
    }

    /**
     * Получение множества клиентов-серверов IRC из массива.
     * @return LinkedHashSet<IrcServer> все клиенты-серверы IRC, 
     * находящиеся в массиве, кроме служебного псевдосервера 
     * {@link Globals#anonymousIrcServer}. 
     */
    public LinkedHashSet<IrcServer> getIrcServerSet() {
        LinkedHashSet<IrcServer> ircServerSet = null;
        synchronized (ircServerMap) {
            ircServerSet = 
                    new LinkedHashSet<IrcServer>(ircServerMap.values());
        }
        ircServerSet.remove(Globals.anonymousIrcServer.get());
        return ircServerSet;
    }


    /**
     * Получение множества доменных имен клиентов-серверов IRC из массива.
     * @return LinkedHashSet<String> все доменные имена 
     * клиентов-серверов IRC, находящиеся в массиве, кроме служебного 
     * псевдосервера {@link Globals#anonymousIrcServer}. 
     */
    public LinkedHashSet<String> getIrcServernameSet() {
        LinkedList<IrcServer> elementList = new LinkedList<IrcServer>();
        synchronized (ircServerMap) {
            elementList.addAll(ircServerMap.values());
        }
        elementList.remove(Globals.anonymousIrcServer.get());
        LinkedHashSet<String> nicknameSet = new LinkedHashSet<String>();
        for (IrcServer element : elementList) {
            synchronized (element) {
                nicknameSet.add(element.getNickname());
            }
        }
        return nicknameSet;
    }

    /**
     * Получение канала IRC из массива.
     * @param channelNickname  имя канала IRC.
     * @return IrcChannel если этот объект находится в массиве, 
     * null - если объекта с таким имени в массиве нет.
     */
    public IrcChannel getChannel(String channelNickname) {
        String key = channelNickname.toLowerCase(Locale.ENGLISH);
        return channelMap.get(key);
    }

    /**
     * Получение множества каналов IRC из массива.
     * @return LinkedHashSet<IrcServer> все каналы IRC. 
     */
    public LinkedHashSet<IrcChannel> getChannelSet() {
         return new LinkedHashSet<IrcChannel>(channelMap.values());
    }

    /**
     * Получение множества имен каналов IRC из массива.
     * @return LinkedHashSet<String> все имена каналов IRC, 
     * находящиеся в массиве. 
     */
    public LinkedHashSet<String> getChannelNicknameSet() {
        LinkedHashSet<String> nicknameSet = new LinkedHashSet<String>();
        for (IrcChannel element : channelMap.values()) {
            nicknameSet.add(element.getNickname());
        }
        return nicknameSet;
    }

    /**
     * Получение итератора канала IRC для массива,
     * {@link Globals#anonymousUser}.
     * @return Iterator<IrcChannel> 
     */
    public Iterator<IrcChannel> getChannelSetIterator() {
        return channelMap.values().iterator();
    }
    
    /**
     * Получение итератора EntrySet пользователей IRC для массива.
     * @return Iterator<String> 
     */
     
    public Iterator<Map.Entry<String, IrcChannel>> 
            getChannelEntrySetIterator() {
        return channelMap.entrySet().iterator();
    }
    
    /**
     * Получение итератора никнэймов пользователей IRC для массива.
     * @return Iterator<String> 
     */
     
    public Iterator<String> getChannelNicknameIterator() {
        return channelMap.keySet().iterator();
    }
    
    /**
     * Получение обычного клиента IRC из массива, за исключением 
     * служебного псевдопользователя {@link Globals#anonymousUser}.
     * @param userNickname никнэйм.
     * @return User если этот объект находится в массиве, 
     * null - если объекта с таким никнэймом в массиве нет.
     */
    public User getUser(String userNickname) {
        String key = userNickname.toLowerCase(Locale.ENGLISH);
        User user = userMap.get(key);
        if (user == Globals.anonymousUser.get()) {
            user = null;
        }
        return user;
    }

    /**
     * Получение множества обычных пользователей IRC из массива.
     * @return LinkedHashSet< User > - множество обычных 
     * пользователей IRC. 
     */
    public LinkedHashSet<User> getUserSet() {
        LinkedHashSet<User> userSet = 
                new LinkedHashSet<User>(userMap.values());
        return userSet;
    }

    /**
     * Получение списка обычных пользователей IRC из массива.
     * @return LinkedList< User > - множество обычных 
     * пользователей IRC. 
     */
    public LinkedList<User> getUserList() {
        LinkedList<User> userList = 
                new LinkedList<User>(userMap.values());
        return userList;
    }

    /**
     * Получение множества никнэймов обычных пользователей IRC из массива.
     * @return LinkedHashSet< String > - все никнэймы обычных 
     * пользователей IRC, находящиеся в массиве. 
     */
    public LinkedHashSet<String> getUserNicknameSet() {
        LinkedList<User> elementList = 
                new LinkedList<User>(userMap.values());
        LinkedHashSet<String> nicknameSet = new LinkedHashSet<String>();
        for (User element : elementList) {
            nicknameSet.add(element.getNickname());
        }
        return nicknameSet;
    }
    
    /**
     * Получение итератора пользователей IRC для массива.
     * @return Iterator<User> 
     */
    public Iterator<User> getUserSetIterator() {
        return userMap.values().iterator();
    }
    
    /**
     * Получение итератора никнэймов пользователей IRC для массива.
     * @return Iterator<String> 
     */
    public Iterator<String> getUserNicknameIterator() {
        return userMap.keySet().iterator();
    }
    
    /**
     * Получение итератора EntrySet пользователей IRC для массива.
     * @return Iterator<String> 
     */
    public Iterator<Map.Entry<String, User>> getUserEntrySetIterator() {
        return userMap.entrySet().iterator();
    }
    
    
    /**
     * Получение размера массива пользователей IRC.
     * @return размер массива.
     */
    public int getUserMapSize() {
        return userMap.size();
    }
    

    /**
     * Получение клиента-сервиса IRC из массива.
     * @param serviceName никнэйм клиента-сервиса IRC.
     * @return объект класса {@link Service}, если объект с таким 
     * никнэймом находится в массиве, null - если объекта с таким 
     * никнэймом в массиве нет.
     */
    public Service getService(String serviceName) {
        String key = serviceName.toLowerCase(Locale.ENGLISH);
        synchronized (serviceMap) {
            return serviceMap.get(key);
        }
    }

    /**
     * Получение множества клиентов-сервисов IRC из массива.
     * @return LinkedHashSet< Service > - множество обычных 
     * пользователей 
     * IRC. 
     */
    public LinkedHashSet<Service> getServiceSet() {
        synchronized (serviceMap) {
            return new LinkedHashSet<Service>(serviceMap.values());
        }
    }

    /**
     * Получение множества никнэймов клиентов-сервисов IRC из массива.
     * @return LinkedHashSet< String > - все никнэймы клиентов-сервисов 
     * IRC, находящиеся в массиве. 
     */
    public LinkedHashSet<String> getServiceNameSet() {
        LinkedList<Service> elementList = new LinkedList<Service>();
        synchronized (serviceMap) {
            elementList.addAll(serviceMap.values());
        }
        LinkedHashSet<String> nicknameSet = new LinkedHashSet<String>();
        for (Service element : elementList) {
            synchronized (element) {
                nicknameSet.add(element.getNickname());
            }
        }
        return nicknameSet;
    }

    /**
     * Получение итератора соединений IRC для списка.
     * @return Iterator<List<Connection>> 
     */
    public Iterator<Connection> getConnectionListIterator() {
        return connectionList.iterator();
    }
    
    /**
     * Получение размера списка соединений IRC.
     * @return размер массива.
     */
    public int getConnectionListSize() {
        return connectionList.size();
    }
    
    /**
     * Получение копии списка соединений IRC.
     * @return LinkedHashSet< Connection > - множество соединений 
     * IRC.
     */
    public List<Connection> getConnectionList() {
        return new LinkedList<Connection>(connectionList);
    }


    /**
     * Получение списка никнэймов обычных пользователей IRC из массива
     * историй никнэймов.
     * @return LinkedList< String > - список никнэймов IRC с историями.
     */
    public LinkedList<String> getNicknameHistoryList() {
        synchronized (nicknameHistoryMap) {
            return new LinkedList<String>(nicknameHistoryMap.keySet());
        }
    }

    /**
     * Получение истории обычного пользователя IRC из массива историй 
     * никнэймов.
     * @param nickname никнэйм
     * @return LinkedList<DriedUser> - список никнэймов пользователкй с 
     * историями.
     */
    public ArrayList<DriedUser> getHistoryList(String nickname) {
        String key = nickname.toLowerCase(Locale.ENGLISH);
        ArrayList<DriedUser> result = null;
        synchronized (nicknameHistoryMap) {
            result = nicknameHistoryMap.get(key);
            if (result != null) {
                synchronized (result) {
                    result = new ArrayList<DriedUser>(result);
                }
            } else {
                result = new ArrayList<DriedUser>();
            }
        }
        return result;
    }

    /**
     * Очистка массива историй никнэймов.
     */
    public void dropHistory() {
        nicknameHistoryMap = 
                new LinkedHashMap <String, ArrayList<DriedUser>>();
    }
    
    /**
     * Метод dropAll используется для удаления данных. Данные удаляются
     * из следущих объектов репозитария:
     * <UL>
     * <LI> {@link #ircTalkerMap} ассоциативный массив, хранящий 
     * данные об всех клиентах IRC;</LI>
     * <LI> {@link #userMap} ассоциативный массив, хранящий данные об 
     * обычных клиентах IRC.;</LI>
     * <LI> {@link #channelMap} ассоциативный массив, хранящий данные 
     * о каналах IRC;</LI>
     * <LI> {@link #serviceMap} ассоциативный массив, хранящий данные 
     * о клиентах-сервисах IRC;</LI>
     * <LI> {@link #ircServerMap} ассоциативный массив, хранящий данные 
     * о клиентах-серверах IRC;</LI>
     * <LI> {@link #nicknameHistoryMap} ассоциативный массив, хранящий  
     * истории никнэймов.</LI>
     * <LI> {@link #connectionList} список, хранящий истории 
     * никнэймов.</LI>
     * </UL>
     */
    public void dropAll() {
        ircTalkerMap = new LinkedHashMap<String, IrcTalker>();

        userMap = new ConcurrentSkipListMap<String, User>();

        channelMap = new ConcurrentSkipListMap <String, IrcChannel>();

        serviceMap = new LinkedHashMap<String, Service>();

        ircServerMap = new LinkedHashMap<String, IrcServer>();

        nicknameHistoryMap = 
                new LinkedHashMap <String, ArrayList<DriedUser>>();
        
        connectionList = new CopyOnWriteArrayList <Connection>();
    }


    /**
     * Сохраняет в репозитарии объект класса {@link IrcAdminConfig}.
     * @param ircAdminConfig . 
     */
    public void setIrcAdminConfig(IrcAdminConfig ircAdminConfig) {
        this.ircAdminConfig = ircAdminConfig;
    }

    /**
     * Возвращает объект класса {@link IrcAdminConfig}.
     * @return {@link IrcAdminConfig} .
     */
    public IrcAdminConfig getIrcAdminConfig() {
        return ircAdminConfig;
    }

    /**
     * Сохраняет в репозитарии объект класса {@link IrcServerConfig}.
     * @param ircServerConfig .
     */
    public void setIrcServerConfig(IrcServerConfig ircServerConfig) {
        this.ircServerConfig = ircServerConfig;
    }

    /**
     * Возвращает объект класса {@link IrcTranscriptConfig}.
     * @return {@link IrcTranscriptConfig} .
     
    public IrcTranscriptConfig getIrcTranscriptConfig() {
        return ircTranscriptConfig;
    }

    /**
     * Сохраняет в репозитарии объект класса {@link IrcTranscriptConfig}.
     * @param ircTranscriptConfig . 
     
    public void setIrcTranscriptConfig(
            IrcTranscriptConfig ircTranscriptConfig) {
        this.ircTranscriptConfig = ircTranscriptConfig;
    }
    */
    /**
     * Возвращает объект класса {@link IrcServerConfig}.
     * @return {@link IrcServerConfig} .
     */
    public IrcServerConfig getIrcServerConfig() {
        return ircServerConfig;
    }

    /**
     * Сохраняет в репозитарии объект класса {@link IrcInterfaceConfig}.
     * @param ircInterfaceConfig .
     */
    public void setIrcInterfaceConfig(
            IrcInterfaceConfig ircInterfaceConfig) {
        this.ircInterfaceConfig = ircInterfaceConfig;
    }

    /**
     * Возвращает объект класса {@link IrcInterfaceConfig}.
     * @return {@link IrcInterfaceConfig} .
     */
    public IrcInterfaceConfig getIrcInterfaceConfig() {
        return ircInterfaceConfig;
    }

    /**
     * Сохраняет в репозитарии ассоциативный массив с учетными записями
     * операторов LinkedHashMap< String, IrcOperatorConfig >.
     * @param ircOperatorConfigMap
     */
    public void setIrcOperatorConfigMap(
            LinkedHashMap<String, IrcOperatorConfig>
            ircOperatorConfigMap) {
        this.ircOperatorConfigMap = ircOperatorConfigMap;
    }

    /**
     * Возвращает ассоциативный массив с учетными записями операторов
     * LinkedHashMap< String, IrcOperatorConfig >.
     * @return LinkedHashMap< String, IrcOperatorConfig >.
     */
    public LinkedHashMap<String, IrcOperatorConfig> 
            getIrcOperatorConfigMap() {
        return ircOperatorConfigMap;
    }

    /**
     * Метод используется для проверки полномочий оператора сервера. 
     * Проверка производится путем сравнения имени и пароля, которые 
     * указывает клиент с именем и паролем которые хранятся в 
     * ассоциативном массиве учетных записей операторов   
     * LinkedHashMap < String, IrcOperatorConfig  >.
     * @param name имя.
     * @param password пароль.  
     * @return true если указанные пары совпадают.
     */
    public boolean checkOperator(String name, String password) {
        boolean result = false;
        synchronized (ircOperatorConfigMap) {
            IrcOperatorConfig ioc = ircOperatorConfigMap.get(name);
            if (ioc != null) {
                result = ioc.getPassword().equals(password);
            } else {
                result = false;
            }
        }
        return result;
    }
}
