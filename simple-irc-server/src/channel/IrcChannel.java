/*
 * 
 * IrcChannel 
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
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

/**
 * Класс, который хранит информацию о канале IRC.
 *
 * @version 0.5 2012-02-20
 * @author  Nikolay Kirdin
 * 
 */
public class IrcChannel implements Comparable, Recipient {
    
    /** 
     * Максмальное количество отсчетов для вычисления средней скорости 
     * поступления сообщений {@link #avgRate}. 
     */
    public static final int NUMBER_RATE_POINTS = 300;
    
   /** 
     * Максимальное среднее количество передач сообщений канала за 10 
     * секунд. По умолчанию устанавливается равным 
     * {@link Constants#MAX_CHANNEL_RATE_10}
     */
    public AtomicInteger maxChannelRate = 
            new AtomicInteger(Constants.MAX_CHANNEL_RATE_10);
            
    /** Средняя скорость сообщений канала. */            
    private IrcAvgMeter avgRate;            

    /** Текущие режимы канала. */
    protected EnumSet<ChannelMode> modeSet =
            EnumSet.noneOf(ChannelMode.class);
            
    /** Максимальное количество членов канала. */    
    protected int maximumMemberNumber = 1000;

    /** Ассоциативный массив членов канала и их режимов. */
    protected ConcurrentSkipListMap <User, EnumSet <ChannelMode>> 
    		memberMap = new 
    		ConcurrentSkipListMap <User, EnumSet <ChannelMode>> ();

    /** Имя канала. */
    protected String nickname;     
    
    /** Ключ (пароль) канала. */
    protected String channelKey = null;
            
    /** Топик канала. */
    protected String topic = "";
    
    /** Репозитарий. */
    protected DB db;
    
    /** 
     * Максимальный размер множества масок для учетных записей клиентов, 
     * запрещающих членство в канале. 
     */
    protected int maxBanMaskSetSize = Constants.MIN_LIMIT;
    
    /** 
     * Множество масок для учетных записей клиентов, запрещающих 
     * членство в канале. 
     */ 
    protected ConcurrentSkipListSet<String> banMaskSet = 
            new ConcurrentSkipListSet<String>();
            
    /** 
     * Максимальный размер множества масок исключений для учетных 
     * записей клиентов.
     */
    protected int maxExceptionBanMaskSetSize = Constants.MIN_LIMIT;
    
    /** 
     * Множество масок исключений для учетных записей клиентов.
     * Используется для клиентов, учетные записи, которых удовлетворяют 
     * маске запретов на членство в канале, но которые имеют право 
     * подключаться к каналу.  
     */ 
    protected ConcurrentSkipListSet<String> exceptionBanMaskSet =
            new ConcurrentSkipListSet<String>();
    
    /** 
     * Максимальный размер множества масок для автоматического 
     * подключения к каналу "invite only" ({@link ChannelMode#i}). 
     */
    protected int maxInviteMaskSetSize = Constants.MIN_LIMIT;
            
    /** 
     * Множество масок для автоматического подключения к каналу 
     * "invite only" ({@link ChannelMode#i}). 
     */
    protected ConcurrentSkipListSet<String> inviteMaskSet = 
            new ConcurrentSkipListSet<String>();

    /** 
     * Получение репозитария.
     * @return DB репозитарий. 
     */
    public DB getDb() {
        return db;
    }

    /** 
     * Задание репозитария.
     * @param db репозитарий. 
     */
    public void setDb(DB db) {
        this.db = db;
    }

    /** 
     * Конструктор.
     * @param nickname имя канала.
     * @param db репозитарий.
     */
    protected IrcChannel(String nickname, DB db) {
        this.nickname = nickname;
        this.db = db;
        avgRate = new IrcAvgMeter(NUMBER_RATE_POINTS);
        avgRate.setValue(System.currentTimeMillis() - 60000);
        avgRate.setValue(System.currentTimeMillis());
    }

    /** 
     * Конструктор.
     * @param nickname имя канала.
     * @param topic топик канала.
     * @param db репозитарий.
     */
    protected IrcChannel(String nickname, String topic, DB db) {
        this(nickname, db);
        this.topic = topic;
    }
    
    /** Создатель канала. Проверяется объем свободной памяти, если этот
     * объем меньше {@link Constants#MIN_FREE_MEMORY}, то будет вызван 
     * сборщик мусора. Если после прцедуры сборки мусора, памяти будет 
     * недостаточно, то объект создаваться не будет.
     * @param nickname имя канала.
     * @param topic топик канала.
     * @param db репозитарий.
     * @return новый объект класса IrcChannel или null, если нарушено 
     * ограничение по памяти.
     */
    public static IrcChannel create(String nickname, String topic, DB db) {
    	IrcChannel result = null;
    	long freeMemory = Runtime.getRuntime().freeMemory();
    	if (freeMemory < Constants.MIN_FREE_MEMORY) {
    		System.gc();
    	}
    	freeMemory = Runtime.getRuntime().freeMemory();
    	if (freeMemory >= Constants.MIN_FREE_MEMORY) {
    		result = new IrcChannel(nickname, topic, db);
    	} else {
    		Globals.logger.get().log(Level.SEVERE, 
    				"Insufficient free memory (b): " + freeMemory);
    	}
    	
    	return result;
    }

    /** 
     * Завершающие действия при удалении канала.
     * (Действия выполняемые при удалении канала: 
     * <UL>
     * <LI> удаление имени канала;
     * <LI> удаление топика канала; 
     * <LI> удаление ключа канала; 
     * <LI> восстановление первоначального значения всех 
     * ограничителей максимального количества членов ассоциативных 
     * массивов и множеств; 
     * <LI> очистка режимов канала; очистка всех ассоциативных массивов 
     * и множеств. - не реализовано)
     * </UL>
     */
    public void delete() {
        /*
        nickname = "";
        topic = "";
        channelKey = null;
        avgRate = null;
        memberMap = 
        	new ConcurrentSkipListMap < User, EnumSet <ChannelMode>> ();
        maximumMemberNumber = Constants.MIN_LIMIT;
        synchronized (modeSet) {
            modeSet = EnumSet.noneOf(ChannelMode.class);
        }

        banMaskSet = new LinkedHashSet<String>();
        maxBanMaskSetSize = Constants.MIN_LIMIT;
        exceptionBanMaskSet = new LinkedHashSet<String>();
        maxExceptionBanMaskSetSize = Constants.MIN_LIMIT;
        inviteMaskSet = new LinkedHashSet<String>();
        maxInviteMaskSetSize = Constants.MIN_LIMIT;
        */
    }

    /** 
     * Получение множества членов канала.
     * @return множество членов канала. 
     */
    public LinkedHashSet<User> getUserSet() {
        /*unmodifiableSet*/
        return new LinkedHashSet<User>(memberMap.keySet());
    }
    
    /** 
     * Получение списка членов канала.
     * @return список членов канала. 
     */
    public LinkedList<User> getUserList() {
        return new LinkedList<User>(memberMap.keySet());
    }

    /**
     * Проверка того, что у канала нет членов. 
     * @return true если у канала нет членов.
     */
     public boolean isUserSetEmpty() {
         return memberMap.isEmpty();
     }
    
    /** 
     * Получение итератора EntrySet членов канала.
     * @return Iterator<Map.Entry<User, EnumSet <ChannelMode>>. 
     */
    public Iterator<Map.Entry<User, EnumSet <ChannelMode>>>
        getUserEntrySetIterator() {
        return memberMap.entrySet().iterator();
    }

    /** 
     * Получение итератора членов канала.
     * @return Iterator<User>. 
     */
    public Iterator<User> getUserSetIterator() {
        return memberMap.keySet().iterator();
    }

    /**
     * Задание имени канала
     * @param nickname имя канала. 
     */
    public synchronized void setNickname(String nickname) {
        this.nickname = nickname;
    }

    /** 
     * Получение имени канала.
     * @return имя канала. 
     */
    public synchronized String getNickname() {
        return nickname;
    }

    /** 
     * Задание топика канала.
     * @param topic топик канала. 
     */
    public synchronized void setTopic(String topic) {
        this.topic = topic;
    }

    /** 
     * Получение топика канала.
     * @return топик канала. 
     */
    public synchronized String getTopic() {
        return topic;
    }

    /** 
     * Проверка того, что обычный пользователь может изменить топик 
     * канала.
     * @return true если обычный пользователь может изменить топик 
     * канала. 
     */
    public boolean isTopicable() {
        return !modeSet.contains(ChannelMode.t);
    }
    
    /** 
     * Добавление клиента в ассоциативный массив членов канала. 
     * Добавление будет произведено, если размер ассоциативного массива 
     * меньше максимального значения размера и режимы канала позволяют 
     * добавить этого клиента. 
     * @param requestor клиент.
     * @param channelKey ключ (пароль) канала.
     * @return {@link Response.Reply#RPL_OK} если клиент был добавлен в
     * ассоциативный массив, {@link Response.Reply#ERR_BADCHANNELKEY},
     * если указан неправильный пароль, 
     * {@link Response.Reply#ERR_CHANNELISFULL}, если превышено 
     * ограничение на количество членов канала.
     */
    public Response.Reply add(User requestor, String channelKey) {
        
        Response.Reply response = null;
        
        if ((this.channelKey == null && channelKey != null)
                || (this.channelKey != null && 
                !this.channelKey.equals(channelKey))
                && (!requestor.isOperator())) {
            response = Response.Reply.ERR_BADCHANNELKEY;
        } else {
        	if (memberMap.size() < maximumMemberNumber) {
        		memberMap.put(requestor, 
        				EnumSet.noneOf(ChannelMode.class));
        		response = Response.Reply.RPL_OK;
        	} else {
        		response = Response.Reply.ERR_CHANNELISFULL;
        	}
        }
        return response;
    }

    /** 
     * Удаление клиента из ассоциативного массива членов канала. 
     * @param user клиент.
     */
    public void remove(User user) {
    	memberMap.remove(user);
    }
    
    /** 
     * Предоставление символов, обозначающих режимы канала.
     * Режимы канала обозначаются следующим образом:
     * <UL>
     * 		<LI> секретный канала обозначается символом "@" 
     * 		({@link ChannelMode#s});</LI>
     * 		<LI> приватный канала обозначается символом "*" 
     * 		({@link ChannelMode#p});</LI>
     * 		<LI> публичный канал обозначается символом "=".</LI>
     * </UL>
     * @return строка с одним из этих символов.
     */
    public String getStatus() {
        String result = null;
        if (modeSet.contains(ChannelMode.s)) {
            result = "@";
        } else if (modeSet.contains(ChannelMode.p)) {
            result = "*";
        } else {
            result = "=";
        }
        return result;
    }

    /** 
     * Предоставление символов, обозначающих режимы члена канала.
     * Режимы члена канала обозначаются следующим образом:
     * <UL>
     * 		<LI> оператор канала  ({@link ChannelMode#o}) или создатель 
     * 		канала ({@link ChannelMode#O}) обозначается символом "@";</LI>
     * 		<LI> член канала, обладающий правом посылать сообщения в 
     * 		модерируемый канал ({@link ChannelMode#v}), обозначается 
     * 		символом "+";</LI>
     * </UL>
     * @return строка с одним из этих символов.
     */
    public String getStatus(User requestor) {
        String result = null;
        
        EnumSet<ChannelMode> userChannelModeSet = null;
            userChannelModeSet = memberMap.get(requestor);    
        if (userChannelModeSet.contains(ChannelMode.o) ||
                userChannelModeSet.contains(ChannelMode.O)) {
            result = "@";
        } else if (userChannelModeSet.contains(ChannelMode.v)) {
            result = "+";
        } else {
            result = "";
        }
        return result;
    }

    /** 
     * Установка режима "создатель канала" {@link ChannelMode#O} клиенту.  
     * @param requestor клиент.
     */
    public void setCreator(User requestor) {
        EnumSet<ChannelMode> userChannelModeSet = 
        		memberMap.get(requestor);    
        synchronized (userChannelModeSet) {
            userChannelModeSet.add(ChannelMode.O);
        }
    }

    /** 
     * Установка режима "оператор канала" {@link ChannelMode#o} клиенту.  
     * @param requestor клиент.
     */
    public void setChannelOperator(User requestor) {
        EnumSet<ChannelMode> userChannelModeSet = 
        		memberMap.get(requestor);    
        synchronized (userChannelModeSet) {
            userChannelModeSet.add(ChannelMode.o);
        }
    }

    /** 
     * Проверка возможности доступа к каналу (получения информации о 
     * канале).  
     * @param requestor клиент.
     * @return true если канал доступен клиенту.
     */
    public boolean isVisible(User requestor) {
        boolean result = false;
        if (requestor.isOperator()) {
            result = true;
        } else if (!(modeSet.contains(ChannelMode.s) ||
                modeSet.contains(ChannelMode.p))) {
            result = true;
        } else {
            result = checkMember(requestor);
        }
        return result;
    }

    /** 
     * Проверка того, что клиент является оператором канала.  
     * @param requestor клиент.
     * @return true если клиент является оператором канала.
     */
    public boolean checkChannelOperator(User requestor) {
        boolean result = false;
        EnumSet<ChannelMode> userChannelModeSet = 
        		memberMap.get(requestor);
        if (userChannelModeSet != null) {
            result = userChannelModeSet.contains(ChannelMode.o); 
        }
        return result;
    }

    /** 
     * Проверка того, что клиент является создателем канала.  
     * @param requestor клиент.
     * @return true если клиент является создателем канала.
     */
    public boolean checkChannelCreator(User requestor) {
        boolean result = false;
        EnumSet<ChannelMode> userChannelModeSet = 
        		memberMap.get(requestor);
        if (userChannelModeSet != null) {
            result = userChannelModeSet.contains(ChannelMode.O); 
        }
        return result;
    }

    /** 
     * Проверка членства в канале.  
     * @param requestor клиент.
     * @return true если клиент является членом канала.
     */
    public boolean checkMember(User requestor) {
        return memberMap.containsKey(requestor);
    }

    /** 
     * Проверка того, что канал является каналом "invite only" 
     * {@link ChannelMode#i}.  
     * @return true если канал является каналом "invite only"
     * {@link ChannelMode#i}.
     */
    public boolean isInviteOnly() {
        return modeSet.contains(ChannelMode.i);
    }

    /** 
     * Проверка того, что клиент может стать членом "invite only" канала
     * {@link ChannelMode#i}, на основании совпадения его учетных данных 
     * с элементом множества масок {@link #inviteMaskSet}.  
     * @param requestor клиент.
     * @return true Если есть совпадения его учетных данных с элементом 
     * множества масок {@link #inviteMaskSet}.
     */
    public boolean checkInvited(User requestor) {
        String requestorNetId = requestor.getNickname() + "!" +
                requestor.getUsername() + "@" + requestor.getHostname();
        for (String mask : inviteMaskSet) {
            if (IrcMatcher.match(mask, requestorNetId)) {
                return true;
            }
        }
        return false;
    }

    /** 
     * Проверка того, что клиент не может стать членом этого канала,
     * на основании совпадения его учетных данных с элементами 
     * множества масок {@link #banMaskSet}.  
     * @param requestor клиент.
     * @return true если нет совпадения его учетных данных с элементами 
     * множества масок {@link #banMaskSet}.
     */
    public boolean checkBanned(User requestor) {
        String requestorNetId = requestor.getNickname() + "!" +
                requestor.getUsername() + "@" + requestor.getHostname();
        for (String mask : banMaskSet) {
            if (IrcMatcher.match(mask, requestorNetId)) {
                return true;
            }
        }
        return false;
    }

    /** 
     * Проверка того, что клиент  может стать членом этого канала,
     * на основании совпадения его учетных данных с элементами 
     * множества масок {@link #exceptionBanMaskSet}.  
     * @param requestor клиент.
     * @return true если есть совпадение его учетных данных с элементами 
     * множества масок {@link #exceptionBanMaskSet}.
     */
    public boolean checkExcepted(User requestor) {
        String requestorNetId = requestor.getNickname() + "!" +
                requestor.getUsername() + "@" + requestor.getHostname();
        for (String mask : exceptionBanMaskSet) {
            if (IrcMatcher.match(mask, requestorNetId)) {
                return true;
            }
        }
        return false;
    }

    /** 
     * Проверка того, что клиент может послать сообщение в этот канал,
     * на основании его полномочий и режимов этого канала и того, что
     * средняя скорость сообщений канала не превышает величины
     * {@link #maxChannelRate}. Канал примет сообщение 
     * от клиента в следующих случаях:
     * <UL>
     *      <LI> при любых режимах канала или канале с режимом
     *      {@link ChannelMode#q}:
     *      <UL>
     *           <LI> клиент является оператором {@link UserMode#o};</LI>
     *           <LI> клиент является оператором канала 
     *           {@link ChannelMode#o};</LI>
     *      </UL>
     *      </LI>
     *      <LI> модерируемый канал {@link ChannelMode#m} и если средняя 
     *      скорость сообщений канала не превышает величины
     *      {@link #maxChannelRate}:
     *          <UL>
     *              <LI> клиент с режимом {@link ChannelMode#v};</LI>
     *          </UL>
     *      </LI>
     *      <LI> канал с режимом {@link ChannelMode#n} и если средняя 
     *      скорость сообщений канала не превышает величины
     *      {@link #maxChannelRate}:
     *          <UL>
     *              <LI> клиент является членом канала;</LI>
     *          </UL>
     *          </LI>
     *      <LI> канал без установленного режима или со всеми остальными
     *      режимами и если средняя скорость сообщений канала не 
     *      превышает величины {@link #maxChannelRate}: 
     *          <UL>
     *              <LI> любой клиент подключенный к серверу.</LI>
     *          </UL>
     *      </LI>
     * </UL>
     * 
     * @param requestor клиент.
     * @return true если клиент может послать сообщение в этот канал.
     */
    public boolean canReceive(User requestor) {
        boolean result = false;
        long currentTime = System.currentTimeMillis();
        long avgChannelInterval = avgRate.getAvgInterval(currentTime);
        if (requestor.isOperator() || checkChannelOperator(requestor)) {
            result = true;
        } else if (avgChannelInterval * maxChannelRate.get() < 10000) {
            result = false;
        } else if (modeSet.contains(ChannelMode.q)) {
            result = false;
        } else if ((modeSet.contains(ChannelMode.n) || 
                modeSet.contains(ChannelMode.m)) && 
                !checkMember(requestor)) {
            result = false;
        } else if (modeSet.contains(ChannelMode.m) 
            && !memberMap.get(requestor).contains(ChannelMode.v)) {
            result = false;
        } else {
            result = true;
        }
        return result;
    }

    /** 
     * Проверка того, что клиент имеет полномочие на посылку сообщений в 
     * модерируемый канал {@link ChannelMode#m}.
     * @param requestor клиент.
     * @return true если клиент имеет полномочие на посылку сообщений в 
     * модерируемый канал {@link ChannelMode#m}.
     */
    public boolean checkVote(User requestor) {
        return memberMap.get(requestor).contains(ChannelMode.v);
    }

    /** 
     * Проверка того, канал является анонимным {@link ChannelMode#a}.
     * @return true если канал является анонимным {@link ChannelMode#a}.
     */
    public boolean isAnonymous() {
        return modeSet.contains(ChannelMode.a);
    }

    /** 
     * Изменение режимов канала.
     * @param modeCarrier объект, содержащий флаг, тип операции и 
     * параметры.
     * @param requestor источник запроса на изменение.
     * @return ResponseSuccess если запрос на изменение был успешно 
     * выполнен, ResponseFailure если запрос выполнить не удалось.
     */
    public Response updateChannelmode(ChannelModeCarrier modeCarrier,
            User requestor) {
        Response response = null;
        ModeOperation modeOperation = modeCarrier.getOperation();
        ChannelMode channelMode = modeCarrier.getMode();
        String parameter = modeCarrier.getParameter();
        synchronized (modeSet) {
            switch (modeOperation) {
            case ADD:
                switch (channelMode) {
                case O:
                case o:
                case v:

                    User user = db.getUser(parameter);

                    if (user == null) {
                        response = ResponseFailure.create(
                                Response.Reply.ERR_NOSUCHNICK,
                                requestor.getNickname(),
                                parameter);
                        break;
                    }

                    if (user.isRestricted()) {
                        response = ResponseFailure.create(
                                Response.Reply.ERR_RESTRICTED,
                                requestor.getNickname());
                        break;
                    }

                    EnumSet <ChannelMode> userChannelMode = 
                    		memberMap.get(user);
                    	
                        if (userChannelMode == null) {
                            response = ResponseFailure.create(
                                    Response.Reply.ERR_USERNOTINCHANNEL,
                                    requestor.getNickname(),
                                    parameter,
                                    getNickname());
                            break;
                        } else {
                        	synchronized (userChannelMode) {
                        		userChannelMode.add(channelMode);
                        	}
                        }
                    
                    response = new ResponseSuccess(Response.Reply.RPL_OK);
                    break;

                case p:
                case s:
                    if (!((modeSet.contains(ChannelMode.p)
                           || modeSet.contains(ChannelMode.s)))) {
                        modeSet.add(channelMode);
                    }
                    response = new ResponseSuccess(Response.Reply.RPL_OK);
                    break;
                case k:
                    if (channelKey != null && !channelKey.isEmpty()) {
                        response = ResponseFailure.create(
                                Response.Reply.ERR_KEYSET,
                                
                                requestor.getNickname(),
                                getNickname());
                        break;
                                
                    }
                    if (parameter != null &&
                        !parameter.isEmpty()) {
                        channelKey = parameter;
                        modeSet.add(channelMode);
                        response = new ResponseSuccess(
                                    Response.Reply.RPL_OK);
                    } else {
                        response = ResponseFailure.create(
                                Response.Reply.ERR_UNKNOWNCOMMAND,
                                requestor.getNickname(),
                                "MODE +k *********");
                        break;
                    }
                    break;
                case l:
                    int i = 0;
                    try {
                        i = Integer.parseInt(parameter);
                    } catch (NumberFormatException e) {
                        response = ResponseFailure.create(
                                Response.Reply.ERR_UNKNOWNCOMMAND,
                                requestor.getNickname(),
                                "MODE +l " + parameter);
                        break;
                    }
                    if (i <= Constants.MAX_CHANNEL_MEMBER) {
                        maximumMemberNumber = i;
                        modeSet.add(channelMode);
                        response = new ResponseSuccess(
                            Response.Reply.RPL_OK);
                    } else {
                        response = ResponseFailure.create(
                                Response.Reply.ERR_UNKNOWNCOMMAND,
                                requestor.getNickname(),
                                "MODE +l " + parameter);
                    }
                    break;
                case b:
                    if (banMaskSet.size() < maxBanMaskSetSize) {
                        banMaskSet.add(parameter);
                        modeSet.add(channelMode);
                        response = new ResponseSuccess(
                                    Response.Reply.RPL_OK);
                    } else {
                        response = ResponseFailure.create(
                                Response.Reply.ERR_BANLISTFULL,
                                requestor.getNickname(),
                                getNickname(),
                                "*");
                        break;
                    }
                    break;
                case e:
                    if (exceptionBanMaskSet.size() < 
                    		maxExceptionBanMaskSetSize) {
                        exceptionBanMaskSet.add(parameter);
                        modeSet.add(channelMode);
                        response = new ResponseSuccess(
                                    Response.Reply.RPL_OK);
                    } else {
                        /* Отсутствует код для индикации переполнения
                           списка исключений из запрета на членство. 
                           Использую ERR_BANLISTFULL.
                        */
                        response = ResponseFailure.create(
                                Response.Reply.ERR_BANLISTFULL,
                                requestor.getNickname(),
                                getNickname(),
                                "*");
                        break;        
                    }
                    break;
                case I:
                    if (inviteMaskSet.size() < maxInviteMaskSetSize) {
                        inviteMaskSet.add(parameter);
                        modeSet.add(channelMode);
                        response = new ResponseSuccess(
                                Response.Reply.RPL_OK);
                    } else {
                        /* Отсутствует код для индикации переполнения
                           списка на автоматическое включение в члены 
                           канала "invite only". Использую ERR_BANLISTFULL.
                        */
                        response = ResponseFailure.create(
                                Response.Reply.ERR_BANLISTFULL,
                                requestor.getNickname(),
                                getNickname(),
                                "*");
                        break;
                    }
                    break;
                default:
                    modeSet.add(channelMode);
                    response = new ResponseSuccess(Response.Reply.RPL_OK);
                    break;
                }

                break;
            case REMOVE:
                switch (channelMode) {
                case O:
                case o:
                case v:
                    User user = db.getUser(parameter);

                    if (user == null) {
                        response = ResponseFailure.create(
                                Response.Reply.ERR_NOSUCHNICK,
                                requestor.getNickname(),
                                parameter);
                        break;
                    }
                    EnumSet <ChannelMode> userChannelMode = 
                    		memberMap.get(user);
                    if (userChannelMode == null) {
                    	response = ResponseFailure.create(
                    			Response.Reply.ERR_USERNOTINCHANNEL,
                    			requestor.getNickname(),
                    			parameter,
                    			getNickname());
                    	break;
                    } else {
                    	synchronized (userChannelMode) {
                    		userChannelMode.remove(channelMode);
                    	}
                    }
                    
                    response = new ResponseSuccess(Response.Reply.RPL_OK);
                    break;
                case k:
                    channelKey = null;
                    modeSet.remove(channelMode);
                    response = new ResponseSuccess(Response.Reply.RPL_OK);
                    break;
                case l:
                    maximumMemberNumber = Constants.MIN_LIMIT;
                    response = new ResponseSuccess(Response.Reply.RPL_OK);
                    break;
                case b:
                	banMaskSet.remove(parameter);
                	if (banMaskSet.isEmpty()) {
                		modeSet.remove(channelMode);
                	}
                    response = new ResponseSuccess(Response.Reply.RPL_OK);
                    break;
                case e:
                	exceptionBanMaskSet.remove(parameter);
                	if (exceptionBanMaskSet.isEmpty()) {
                		modeSet.remove(channelMode);
                	}
                    response = new ResponseSuccess(Response.Reply.RPL_OK);
                    break;
                case I:
                	inviteMaskSet.remove(parameter);
                	if (inviteMaskSet.isEmpty()) {
                		modeSet.remove(channelMode);
                	}
                    response = new ResponseSuccess(Response.Reply.RPL_OK);
                    break;
                default:
                    modeSet.remove(channelMode);
                    response = new ResponseSuccess(Response.Reply.RPL_OK);
                    break;
                }
                break;
            default:
                throw new Error(
                        "Channel updateChannelmode(): Internal error");
            }
        }
        Globals.logger.get().log(Level.FINEST, nickname + " " +
                modeSet);
        return response;
    }

    /** 
     * Предоставление информации о всех режимах канала.
     * @param requestor источник запроса.
     * @return ResponseSuccess с информацией о режимах канала.
     */
    public Response listChannelmode(User requestor) /*throws 
            IrcExecutionException*/ {
        Response response = null;
//        LinkedHashMap <User, EnumSet <ChannelMode>> memberMapClone = null;
        String modeString = "+";
        String paramString = "";
        for (ChannelMode channelmode : modeSet) {
            modeString = modeString + channelmode.op;
        }

        for (ChannelMode channelmode : modeSet) {
            switch (channelmode) {
            case l:
                paramString = (paramString.isEmpty()) ? 
                        paramString : paramString + " ";
                paramString = paramString + maximumMemberNumber;
                break;
            case b:
                paramString = (paramString.isEmpty()) ? 
                        paramString : paramString + " ";
                    for (String mask : banMaskSet) {
                        paramString = paramString + mask + ",";
                    }
                if (paramString.endsWith(",")) {
                    paramString = paramString.substring(0,
                            paramString.length() - 1);
                }
                if (paramString.endsWith(" ")) {
                    paramString = paramString.substring(0,
                            paramString.length() - 1);
                }
                break;
            case e:
                paramString = (paramString.isEmpty()) ?
                        paramString : paramString + " ";
                    for (String mask : exceptionBanMaskSet) {
                        paramString = paramString + mask + ",";
                    }
                if (paramString.endsWith(",")) {
                        paramString = paramString.substring(0,
                    paramString.length() - 1);
                }
                if (paramString.endsWith(" ")) {
                    paramString = paramString.substring(0,
                        paramString.length() - 1);
                }
                break;
            case I:
                paramString = (paramString.isEmpty()) ?
                        paramString : paramString + " ";
                for (String mask : inviteMaskSet) {
                    paramString = paramString + mask + ",";
                }
                if (paramString.endsWith(",")) {
                    paramString = paramString.substring(0,
                            paramString.length() - 1);
                }
                if (paramString.endsWith(" ")) {
                    paramString = paramString.substring(0,
                            paramString.length() - 1);
                }
                break;
            default:
                break;
            }
        }
        modeString = (paramString.isEmpty()) ?
                modeString : modeString + " " + paramString;
        response = ResponseSuccess.create(
                Response.Reply.RPL_CHANNELMODEIS,
                requestor.getNickname(),
                getNickname(),
                modeString);
        return response;
    }

    /** 
     * Предоставление информации о конкретном режиме канала.
     * @param channelMode режим, информацию о котором нужно предоставить.
     * @param requestor источник запроса на индикацию.
     * @return ResponseSuccess с информацией о режимах канала.
     */
    public LinkedList<Response> listChannelmode(ChannelMode channelMode,
            User requestor) {
        Response response = null;
        LinkedList<Response> result = new LinkedList<Response>();
        ConcurrentSkipListMap <User, EnumSet <ChannelMode>> memberMapClone = null;
        String modeString = (modeSet.contains(channelMode)) ?
                "+" + channelMode.op : "+";
        String paramString = "";


        switch (channelMode) {
        case O:
        case o:
        case v:

            memberMapClone = new
                    ConcurrentSkipListMap <User, EnumSet <ChannelMode>> 
                    (memberMap);

            for (User user : memberMapClone.keySet()) {
                if (memberMapClone.get(user).contains(channelMode)) {
                    paramString = paramString + user.getNickname() + ",";
                }
            }
            if (paramString.endsWith(",")) {
                paramString = paramString.substring(0,
                        paramString.length() - 1);
            }
            modeString = (paramString.isEmpty()) ? "+" :
                    "+" + channelMode.op + " " + paramString;
            response = ResponseSuccess.create(
                     Response.Reply.RPL_CHANNELMODEIS,
                     requestor.getNickname(),
                     getNickname(),
                     modeString);
            result.offer(response);
            break;
        case l:
            paramString = String.valueOf(maximumMemberNumber);
            modeString = (paramString.isEmpty()) ? "+" :
                    "+" + channelMode.op + " " + paramString;
            response = ResponseSuccess.create(
                    Response.Reply.RPL_CHANNELMODEIS,
                    requestor.getNickname(),
                    getNickname(),
                    modeString);
            result.offer(response);
            break;
        case b:
            for (String mask : banMaskSet) {
                response = ResponseSuccess.create(
                        Response.Reply.RPL_BANLIST,
                        requestor.getNickname(),
                        getNickname(),
                        mask);
                result.offer(response);
            }
                
            response = ResponseSuccess.create(
                    Response.Reply.RPL_ENDOFBANLIST,
                    requestor.getNickname(),
                    getNickname());
            result.offer(response);
            break;
        case e:
            for (String mask : exceptionBanMaskSet) {
                response = ResponseSuccess.create(
                        Response.Reply.RPL_EXCEPTLIST,
                        requestor.getNickname(),
                        getNickname(),
                        mask);
                result.offer(response);
            }
            response = ResponseSuccess.create(
                    Response.Reply.RPL_ENDOFEXCEPTLIST,
                    requestor.getNickname(),
                    getNickname());
            result.offer(response);
            break;
        case I:
            for (String mask : inviteMaskSet) {
                response = ResponseSuccess.create(
                        Response.Reply.RPL_INVITELIST,
                        requestor.getNickname(),
                        getNickname(),
                        mask);
                result.offer(response);
            }
            response = ResponseSuccess.create(
                    Response.Reply.RPL_ENDOFINVITELIST,
                    requestor.getNickname(),
                    getNickname());
            result.offer(response);
            break;
        default:
            response = ResponseSuccess.create(
                    Response.Reply.RPL_CHANNELMODEIS,
                    requestor.getNickname(),
                    getNickname(),
                    modeString);
            result.offer(response);
            break;
        }
        
        return result;
    }
    
    
    /**
     * Отправка сообщения в канал. Сообщение будет отправлено всем 
     * членам канала, за исключением отправителя. Если отправителю 
     * необходимо получить отправляемое сообщение, то необходимо 
     * использовать методы {@link IrcTalker#send}.   
     * Сообщение будет отправляено в том случае, если параметры режимов 
     * клиента и параметры режимов канала позволяют это сделать и 
     * средняя скорость сообщений канала не превышает величины 
     * {@link #maxChannelRate}. 
     * Если у сообщения нет префикса, то он будет добавлен. Для 
     * анонимных каналов {@link ChannelMode#a} префикс будет изменен на
     * никнэйм anonymous.
     * 
     * @param client отправитель сообщения.
     * @param message сообщение.
     * @return true если сообщение было успешно отправлено в 
     * канал, false если сообщение отправить в канал не 
     * удалось. 
     */
     public boolean send(User client, String message) {
        boolean result = false;
        String nick = null;
        
        avgRate.setValue(System.currentTimeMillis());

        nick = client.getNickname();

        if (isAnonymous()) {
        	nick = Globals.anonymousUser.get().getNickname();
        }

        if (message.charAt(0) != ':') {
        	message = ":" + nick + " " + message;
        } else if (isAnonymous()) {
        	message = message.replaceFirst(":\\S+", ":" + nick);
        }

        for (Iterator<User> userIterator = getUserSetIterator();
        		userIterator.hasNext();) {
        	User recipient = userIterator.next();
        	if (recipient != client) {
        		recipient.offerToOutputQueue(
        				new IrcCommandReport(message, 
        						recipient, client));
        	}
        } 
        result = true;
        return result;
     }

    /**
     * Отправка сообщения в канал. Сообщение будет отправлено всем 
     * членам канала, за исключением отправителя. Если отправителю 
     * необходимо получить отправляемое сообщение, то необходимо 
     * использовать методы {@link IrcTalker#send}.   
     * Сообщение будет отправляено в том случае, если параметры режимов 
     * клиента и параметры режимов канала позволяют это сделать и 
     * средняя скорость сообщений канала не превышает величины 
     * {@link #maxChannelRate}.
     * Если у сообщения нет префикса, то он будет добавлен. Для 
     * анонимных каналов {@link ChannelMode#a} префикс будет изменен на
     * никнэйм anonymous.
     *  
     * @param ircCommandReport формализованное сообщение.
     * @return true если сообщение было успешно отправлено в 
     * канал, false если сообщение отправить в канал не 
     * удалось. 
     */
     
     public boolean send(IrcCommandReport ircCommandReport) {
         boolean result = false;
         String nick = null;
         String message = null;
         User client = null;

         IrcTalker ircTalker = ircCommandReport.getSender();
         client = (User) ircTalker;
         message = ircCommandReport.getReport();
         /*
         avgRate.setValue(System.currentTimeMillis());
         nick = client.getNickname();

         if (isAnonymous()) {
        	 nick = Globals.anonymousUser.get().getNickname();
         }
		*/
         
         /*
         if (message.charAt(0) != ':') {
        	 message = ":" + nick + " " + message;
         } else if (isAnonymous()) {
        	 message = message.replaceFirst(":\\S+", ":" + nick);
         }

         ircCommandReport.setReport(message);

         for (Iterator<User> userIterator = getUserSetIterator();
        		 userIterator.hasNext();) {
        	 User recipient = userIterator.next();
        	 if (recipient != client) {
        		 ircCommandReport.setDestination(recipient);
        		 recipient.offerToOutputQueue(ircCommandReport);
        	 }
         }  
		*/
         result = send(client, message);
         return result;
     }
     
    public int compareTo(Object object) {
        int result = 0;
        if (!(object instanceof IrcChannel)) {
            throw new ClassCastException();
        }
        if (this.hashCode() < object.hashCode()) {
            result = -1;
        } else if (this.hashCode() > object.hashCode()) {
            result = 1;
        } else {
            result = 0;
        }
        return result;
    }
     
}
