package org.grass.simpleircserver.talker.user;
/*
 * 
 * User
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
import java.util.logging.Level;

import org.grass.simpleircserver.base.Constants;
import org.grass.simpleircserver.base.Globals;
import org.grass.simpleircserver.channel.IrcChannel;
import org.grass.simpleircserver.parser.ModeOperation;
import org.grass.simpleircserver.parser.Reply;
import org.grass.simpleircserver.talker.IrcTalker;
import org.grass.simpleircserver.talker.server.IrcServer;
/**
 * Этот класс используется для хранения информации об обычных клиентах
 * IRC.
 *
 * @version 0.5 2012-02-20
 * @version 0.5.3 2015-11-05 Program units were moved from default package into packages with names. Unit tests were added.
 * @author  Nikolay Kirdin
 */
public class User extends IrcTalker implements Comparable {
    
    /** 
     * Параметр &lt;username&gt; команды IRC NICK. 
     * Аккаунт клиента на его хосте. 
     */
    private String username = "";
    
    /** 
     * Параметр &lt;realname&gt; команды IRC NICK. 
     * "Реальное" имя пользователя. 
     */
    private String realname = "";
    
    /** IRC сервер, к которому подключен клиент. */
    private IrcServer ircServer = null;
    
    /** Параметр &lt;message&gt; команды IRC AWAY.*/
    private String awayText = "";
    
    /**  Режимы клиента. */
    private EnumSet<UserMode> modeSet = EnumSet.noneOf(UserMode.class);
    
    /** Множество каналов, к которым подключен данный клиент. */
    private ConcurrentSkipListSet<IrcChannel> channelSet =
            new ConcurrentSkipListSet<IrcChannel>();
    
    /**
     * Максимальное количество каналов, к которым допускается
     * подключение клиента.
     */
    private int maximumChannelNumber = Constants.CHANLIMIT;
    
    private User() {}
    
    /**
     * Создатель объекта без параметров. Проверяется объем свободной 
     * памяти, если этот объем меньше {@link Constants#MIN_FREE_MEMORY}, 
     * то будет вызван сборщик мусора. Если после прцедуры сборки мусора, 
     * памяти будет недостаточно, то объект создаваться не будет.
     * @return новый объект класса {@link User} или null, если 
     * достигнуто ограничение по объему доступной памяти.
     */
    public static User create() {
        User result = null;
        long freeMemory = Runtime.getRuntime().freeMemory();
        if (freeMemory < Constants.MIN_FREE_MEMORY) {
            System.gc();
        }
        freeMemory = Runtime.getRuntime().freeMemory();
        if (freeMemory >= Constants.MIN_FREE_MEMORY) {
            result = new User();
        } else {
            Globals.logger.get().log(Level.SEVERE, 
                    "Insufficient free memory (B): " + freeMemory);
        }
        return result;
    }
    
    /**
     * Создатель объекта на основе объекта класса {@link IrcTalker}.
     * @param ircTalker объект класса  {@link IrcTalker}, на основе 
     * которого создается объект класса {@link User}.
     * @return новый объект класса {@link User} или null, если 
     * достигнуто ограничение по объему доступной памяти.
     */
    public static User create(IrcTalker ircTalker) {
        User user = User.create();
        if (user != null) {
            user.setNickname(ircTalker.getNickname());
            user.setNetworkId(ircTalker.getNetworkId());
            user.setHostname(ircTalker.getHostname());
            user.setHopcount(ircTalker.getHopcount());
            user.setRegistered(ircTalker.isRegistered());
            user.setConnection(ircTalker.getConnection());
        }
        return user;
    }
    
    /**
     * Создатель объекта на основе объекта класса {@link IrcServer}.
     * @param ircServer объект класса {@link IrcServer}, на основе 
     * которого создается объект класса {@link User}.
     * @return новый объект класса {@link User} или null, если 
     * достигнуто ограничение по объему доступной памяти.
     */
    public static User create(IrcServer ircServer) {
        User user = User.create();
        if (user != null) {
            user.setNickname(ircServer.getNickname());
            user.setNetworkId(ircServer.getNetworkId());
            user.setHostname(ircServer.getHostname());
            user.setHopcount(ircServer.getHopcount());
            user.setRegistered(ircServer.isRegistered());
            user.setConnection(ircServer.getConnection());
            user.setIrcServer(ircServer);
        }
        return user;
    }
    
    /**
     * Задание никнэйма (параметр &lt;nickname&gt; команды IRC NICK).
     * @param name никнэйм.
     */
    public synchronized void setNickname(String name) {
        if (name != null) {
            super.setNickname(name);
            if (!username.isEmpty() && !realname.isEmpty()) {
                setRegistered(true);
            }
        }
    }
    
    /**
     * Задание аккаунта хоста клиента (параметр &lt;username&gt;
     * команды IRC USER).
     * @param name аккаунта хоста клиента.
     */
    public synchronized void setUsername(String name) {
        if (name != null) {
            username = name;
            if (!getNickname().isEmpty() && !realname.isEmpty()) {
                setRegistered(true);
            }
        }
    }
    
    /**
     * Получение аккаунта хоста клиента (параметр &lt;username&gt;
     * команды IRC USER).
     * @return аккаунт хоста клиента.
     */
    public synchronized String getUsername() {
        return username;
    }
    
    /**
     * Задание "реального" имени клиента (параметр &lt;realname&gt;
     * команды IRC USER).
     * @param name "реальное" имя клиента.
     */
    public synchronized void setRealname(String name) {
        if (name != null) {
            realname = name;
            if (!getNickname().isEmpty() && !username.isEmpty()) {
                setRegistered(true);
            }
        }
    }
    
    /**
     * Получение "реального" имени клиента (параметр &lt;realname&gt;
     * команды IRC USER).
     * @return "реальное" имя клиента.
     */
    public synchronized String getRealname() {
        return realname;
    }
    
    /**
     * Получение описателя сервера к которому подключен клиент.
     * @return описатель сервера к которому подключен клиент.
     */
    public synchronized IrcServer getIrcServer() {
        return ircServer;
    }
    /**
     * Задание описателя сервера к которому подключен клиент.
     * @param ircServer описатель сервера к которому подключен клиент.
     */
    public synchronized void setIrcServer(IrcServer ircServer) {
        this.ircServer = ircServer;
    }
    
    /**
     * Изменение режимов пользователя.
     * @param modeCarrier информации о режиме и операции над режимом.
     */
    public void updateUsermode(UserModeCarrier modeCarrier) {
        synchronized (modeSet) {
            switch(modeCarrier.getOperation()) {
            case ADD:
                modeSet.add(modeCarrier.getMode());
                break;
            case REMOVE:
                modeSet.remove(modeCarrier.getMode());
                break;
            default:
                throw new Error("User updateUsermode(): Internal error");
            }
        }
    }
    
    /**
     * Получение текстового представления режимов пользователя. Режимы 
     * представлены в виде строки следующего вида:
     * <P><code>
     * "+[&lt;режим {@link UserMode}&gt;[&lt;режим {@link UserMode}&gt;]...]"
     * </code>
     * @return строка с текстовым представлением режимов пользователя.
     */
    public String listUsermode() {
        String modeString = "+";
        for (UserMode usermode : modeSet) {
            modeString = modeString + usermode.getMode();
        }
        return modeString;
    }
    
    /**
     * Получение EnumSet&lt;{@link UserMode}&gt; с режимами пользователя.
     * @return EnumSet&lt;{@link UserMode}&gt; с режимами пользователя.
     */
    public EnumSet<UserMode> getModeSet() {
        return modeSet;
    }
    
    /**
     * Проверка того, что у клиента установлен режим "restricted" - 
     * {@link UserMode#r}.
     * @return true если у клиента установлен этот режим.
     */
    public boolean isRestricted() {
        return modeSet.contains(UserMode.r);
    }
    
    /**
     * Проверка того, что у клиента установлен режим "operator" - 
     * {@link UserMode#o}.
     * @return true если у клиента установлен этот режим.
     */
    public boolean isOperator() {
        return modeSet.contains(UserMode.o);
    }
    
    /**
     * Проверка того, что у клиента установлен режим "wallops" -
     * {@link UserMode#w}.
     * @return true если у клиента установлен этот режим.
     */
    public boolean isWallops() {
        return modeSet.contains(UserMode.w);
    }
    
    /**
     * Проверка того, что информация об этом клиенте доступна клиенту,
     * указанному в качестве аргумента.
     * Если у этого клиента режим "invisible" {@link UserMode#i} не 
     * установлен, то информация предоставляется.
     * Если у этого клиента установлен режим "invisible", информация о 
     * нем предоставляется в следущих случаях:
     * <UL>
     *         <LI> Источник запроса является оператором.</LI>
     *         <LI> Источник запроса и этот клиент - это один и тот же 
     *         объект.</LI>
     * </UL>
     * @param requestor клиент, которому нужен доступ к информации об
     * этом клиенте.
     * @return true если клиенту, указанному в качестве аргумента
     * доступна информация об этом клиенте.
     */
    public boolean isVisible(IrcTalker requestor) {
        boolean result = false;
        if (!modeSet.contains(UserMode.i)) {
            result = true;
        } else if (!(requestor instanceof User)) {
            result = false;
        } else {
            result = (((User) requestor).isOperator() ||
                    ((User) requestor) == User.this);
        }
        return result;
    }
    
    /**
     * Проверка того, что этот клиент является членом канала, указанного
     * в качестве аргумента.
     * @param channel проверяемый канал.
     * @return true если этот клиент является членом канала.
     */
    public boolean isMember(IrcChannel channel) {
        return channelSet.contains(channel);
    }
    
    /**
     * Получение копии множества каналов, членом которых является
     * клиент.
     * @return множество каналов.
     */
    public ConcurrentSkipListSet<IrcChannel> getChannelSet() {
            return new ConcurrentSkipListSet<IrcChannel>(channelSet);
    }
    
    /**
     * Получение итератора каналов, членом которых является клиент.
     * @return множество каналов.
     */
    public Iterator<IrcChannel> getChannelSetIterator() {
        return channelSet.iterator();
    }
    
    /**
     * Удаление канала, указанного в качестве аргумента из множества
     * каналов, членом которых является клиент.
     * @param channel удаляемый канал.
     */
    public void remove(IrcChannel channel) {
        channelSet.remove(channel);
    }
    
    /**
     * Добавление канала, указанного в качестве аргумента во множество
     * каналов, членом которых является клиент.
     * @param channel добавляемый канал.
     * @return {@link Reply#RPL_OK} если добавление было успешно
     * завершено, {@link Reply#ERR_TOOMANYCHANNELS}, если
     * превышено максимально допустимое количество каналов.
     */
    public Reply add(IrcChannel channel) {
        Reply responseReply = null;
        if (channelSet.size() < maximumChannelNumber) {
            channelSet.add(channel);
            responseReply = Reply.RPL_OK;
        } else {
            responseReply = Reply.ERR_TOOMANYCHANNELS;
        }
        return responseReply;
    }
    
    /**
     * Задание максимального количества каналов, к которым допускается
     * подключение клиента.
     * @param maxNumber целое положительное число.
     */
    public void setMaximumChannelNumber(int maxNumber) {
        maximumChannelNumber = maxNumber;
    }
    
    /**
     * Получение максимального количества каналов, к которым допускается
     * подключение клиента.
     * @return  целое положительное число.
     */
    public int getMaximumChannelNumber() {
        return maximumChannelNumber;
    }
    
    /**
     * Проверка того, что у клиента установлен AWAY &lt;message&gt;.
     * @return true если у клиента установлен AWAY &lt;message&gt;.
     */
    public synchronized boolean hasAwayText() {
        return !awayText.isEmpty();
    }
    
    /**
     * Получение  AWAY &lt;message&gt;.
     * @return AWAY &lt;message&gt;.
     */
    public synchronized String getAwayText() {
        return awayText;
    }
    
    /**
     * Задание  AWAY &lt;message&gt;.
     * @param text AWAY &lt;message&gt;.
     */
    public synchronized void setAwayText(String text) {
        synchronized (modeSet) {
            if (text.isEmpty()) {
                modeSet.remove(UserMode.a);
            } else {
                modeSet.add(UserMode.a);
            }
        }
        awayText = text;
    }
    /** Установка режима "оператор" - {@link UserMode#o}. */
    public void setOperator() {
        updateUsermode(new UserModeCarrier(UserMode.o, ModeOperation.ADD));
    }
    
    /**
     * Текстовое представление объекта. Объект представляется следующим
     * образом:
     * <P><code> 
     * "&lt;id&gt; &lt;nickname&gt; &lt;username&gt; &lt;realname&gt; Registered: &lt;признак регистрации&gt;"
     * </code>
     * <P>Поля разделены пробелом. 
     */
    public String toString() {
        return String.valueOf(getId()) + " " + getNickname() + " " + username +
                " " + realname + " " + "Registered: " + isRegistered() ;
    }
}