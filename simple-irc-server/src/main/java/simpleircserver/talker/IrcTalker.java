package simpleircserver.talker;
/*
 * 
 * IrcTalker 
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
 *  License Version 3 along with this program.  If not, see 
 *  <http://www.gnu.org/licenses/>.
 *
 */

import java.util.logging.*;

import simpleircserver.base.Globals;
import simpleircserver.base.Recipient;
import simpleircserver.connection.Connection;
import simpleircserver.parser.IrcCommandReport;
import simpleircserver.parser.IrcIncomingMessage;
import simpleircserver.tools.IrcAvgMeter;

import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.net.*;

/**
 * Класс, являющийся родителем классов, хранящих информацию о 
 * клиентах данного сервера IRC.
 *
 * Объект этого класса характеризуется состояниями, которые определены в 
 * {@link IrcTalkerState}.
 *
 * @version 0.5 2012-02-12
 * @version 0.5.3 2015-11-05 Program units were moved from default package into packages with names. Unit tests were added.
 * @author  Nikolay Kirdin
 */
    
public abstract class IrcTalker implements Recipient {
    
    /** Счетчик генератора уникальных идентификаторов. */
    private static AtomicLong seq = new AtomicLong(0);
    
    /** Состояние соединения. */
    private IrcTalkerState state;
    
    /** Время перехода в текущее состояние {@link #state}. */
    public AtomicLong stateTime = new AtomicLong();    
    
    /** ReadWrite Lock для IrcTalkerState. */
    private final ReentrantReadWriteLock stateRWLock = 
            new ReentrantReadWriteLock();
    
    /** ReadLock для state. */
    private final Lock stateRLock = stateRWLock.readLock();
    
    /** WriteLock для state. */
    private final Lock stateWLock = stateRWLock.writeLock();
    
    /** Уникальный идентификатор. */
    private final long id;  
    
    /** Текстовое представление уникального идентификатора в 
     * 36-ричной системе счисления. Представление создается с помощью
     * метода {@link #genCanonicalId}.
     */
    private String idString;
    
    /** Никнэйм. */
    private String nickname = "";
    
    /** Сетевой идентификатор. */
    private InetAddress networkId;
    
    /** FQDN хоста. */
    private String hostname = "";
    
    /** Количество хопов. */
    private int hopcount;
    
    /** Признак регистрации. */
    private boolean registered = false;
    
    /** Пароль клиента, который используется в команде PASS. */
    private String password;
    
    /** 
     * Соединение - объект класса Connection, который обслуживает 
     * сетевой сокет, к которому подключен этот клиент.
     */
    private Connection connection;    
        
    /** Время чтения последнего сообщения.*/
    private long lastMessageTime;
    
    /** 
     * Максимально допустимая сокорость вывода сообщений (сообщение/секунда). 
     */
    public AtomicInteger maxOutputRate = new AtomicInteger(10);
    
    /** Средняя скорость вывода сообщений. */
    public IrcAvgMeter avgOutputRate = new IrcAvgMeter(300);
    
    /** 
     * Максимально допустимая средняя скорость ввода сообщений  в 
     * (сообщение/секунда). 
     */
    public AtomicInteger maxInputRate = new AtomicInteger(1);
    
    /** Средняя скорость вывода сообщений. */
    public IrcAvgMeter avgInputRate = new IrcAvgMeter(300);
    
    /**
     * Конструктор.
     * При создании объекта, создается уникальный идентификатор и его 
     * 36-ричное представление, Представление создается с помощью
     * метода {@link #genCanonicalId}. Состояние объекта устанавливается 
     * в состояние {@link IrcTalkerState#NEW}, сохраняется время 
     * установки состояния. Соединением объекта устанавливается 
     * служебное псевдосоединение {@link Globals#nullConnection}.
     */
    public IrcTalker() {
        id = seq.getAndIncrement();
        idString = genCanonicalId(id);
        setState(IrcTalkerState.NEW);
        stateTime.set(System.currentTimeMillis());
        connection = Globals.nullConnection.get();
        avgInputRate.setValue(stateTime.get());
        avgOutputRate.setValue(stateTime.get());
    } 
    
    /**
     * Получение 36-ричного представления уникального идентификатора.
     * @return 36-ричное представление уникального идентификатора.
     */
    public String getIdString() {
        return idString;
    }
    
    /**
     * Получение уникального идентификатора.
     * @return уникальный идентификатор.
    */
    public long getId() {
        return id;
    }
    
    /**
     * Получение никнэйма.
     * @return никнэйм. 
     */
    public synchronized String getNickname() {
        return nickname;
    }
        
    /**
     * Задание никнэйма.
     * @param name никнэйм.
     */
    public synchronized void setNickname(String name) {
        nickname = name;
    }

    /**
     * Задание сетевого идентификатора.
     * @param networkId сетевой идентификатор.
    */
    public synchronized void setNetworkId(InetAddress networkId) {
        this.networkId = networkId;
    }
    
    /**
     * Получение сетевого идентификатора.
     * @return сетевой идентификатор.
     */
    public synchronized InetAddress getNetworkId() {
        return networkId;
    }
    
    /**
     * Задание FQDN хоста.
     * @param hostname FQDN хоста.
     */
    public synchronized void setHostname(String hostname) {
        this.hostname = hostname;
    }
    
    /**
     * Получение FQDN хоста.
     * @return hostname FQDN хоста.
     */
    public synchronized String getHostname() {
        return hostname;
    }
    
    /**
     * Получение количества хопов.
     * @return количество хопов.
     */
    public synchronized int getHopcount() {
        return hopcount;
    }

    /**
     * Задание количества хопов.
     * @param hopcount количество хопов.
     */
    public synchronized void setHopcount(int hopcount) {
        this.hopcount = hopcount;
    }
        
    /**
     * Получение признака регистрации клиента.
     * @return true если клиент зарегистрирован.
     */
    public boolean isRegistered() {
        return getState() == IrcTalkerState.OPERATIONAL;
    }

    /**
     * Задание признака регистрации клиента.
     * @param registered признак регистрации клиента.
     */
    public void setRegistered(boolean registered) {
        if (registered) {
            setState(IrcTalkerState.OPERATIONAL);
        } else {
            setState(IrcTalkerState.REGISTERING);
        }
        stateTime.set(System.currentTimeMillis());
    }
    
    /**
     * Задание пароля клиента.
     * @param password пароль клиента.
     */
    public synchronized void setPassword(String password) {
        this.password = password;
    }
    
    /**
     * Получение пароля клиента.
     * @return пароль клиента.
     */
    public synchronized String getPassword() {
       return password;
    }
    
    /**
     * Проверка пароля клиента (Не реализовано).
     * @param password пароль предоставляемый для проверки.
     * @return всегда возвращается false.
     */
    public synchronized boolean checkPassword(String password) {
        boolean result = false;
        return result;
    }
     
    /**
     * Задание соединения.
     * @param connection соединение.
     */
    public synchronized void setConnection(Connection connection) {
        this.connection = connection;
    } 
    
    /**
     * Получение соединения
     * @return соединение.
     */
    public synchronized Connection getConnection() {
        return connection;
    } 

    /**
     * Получение длительности промежутка времени с момента получения 
     * последнего сообщения клиента.
     * @return длительность промежутка времени с момента получения 
     * последнего сообщения клиента.
     */
    public synchronized long getIdle() {
        return (System.currentTimeMillis() - lastMessageTime) / 1000;
    }

    /**
     * Задание времени получения последнего сообщения клиента.
     * @param time длительность промежутка времени с момента получения 
     * последнего сообщения клиента.
     */
    public synchronized void setLastMessageTime(long time) {
        lastMessageTime = time;
    }
     
    /**
     * Действия выполняемые при разрыве связи с клиентом.
     */
    public void disconnect() {
//        getConnection().running.set(false);
    }
    /**
     * Текстовое представление объекта. Объект представляется следующим 
     * образом:
     * <P><code> 
     * "&lt;уникальный идентификатор&gt; &lt;никнэйм&gt; &lt;сетевой идентификатор&gt; Registered: &lt;признак регистрации&gt;" 
     * </code>
     * <P>Поля разделены пробелом.
     */
    public String toString() {
        return "id: " + String.valueOf(id) +  
        " nickname: " + nickname + 
        " networkId: " + networkId + 
        " Registered: " + registered;
    }  
    

    /**
     * Преобразователь целого положительного числа в 36-ричное 
     * представление. Символы в порядке возрастания: 
     * "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789".
     * @param numb число для преобразования.
     * @return 36-ричное представление параметра.
     */
    public static String genCanonicalId(long numb) {

        String symbs = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        int sLen = symbs.length();
        StringBuilder sb = new StringBuilder();

        do {
            sb.append(symbs.charAt((int)(numb % sLen)));
            numb /= sLen;

        } while (numb != 0);

        sb.append("AAAAA");

        sb.reverse();

        sb.substring(sb.length() - 5);

        return sb.toString();

    }
    
    /**
     * Сохранение времени приема сообщения IRC PONG от клиента.
     */
    public void receivePong() {
        getConnection().pongTime.set(System.currentTimeMillis());
    } 

     /**
      * Отправка сообщения в клиенту.    
      * @param ircTalker отправитель сообщения.
      * @param message сообщение.
      * @return результат отправки сообщения.
      */
      public boolean send(IrcTalker ircTalker, String message) {
          boolean result = false;
          IrcCommandReport ircCommandReport = null;
          ircCommandReport = new IrcCommandReport(message, 
                 IrcTalker.this, ircTalker);
          result = send(ircCommandReport);
          return result;
      }

    /**
     * Отправка сообщения в клиенту.    
     * @param ircCommandReport сообщение.
     * @return результат отправки сообщения.
     */
     public boolean send(IrcCommandReport ircCommandReport) {
         boolean result = false;
         String message = ircCommandReport.getReport();
         String nick = ircCommandReport.getSender().getNickname();
         if (message.charAt(0) != ':') {
             message = ":" + nick + " " + message;
             ircCommandReport.setReport(message);       
         }
         result = IrcTalker.this.offerToOutputQueue(ircCommandReport);
         return result;
     }
     
    /**
     * Метод используется для помещения сообщения во  входную очередь 
     * клиента IRC. Сообщение будет помещено в очередь в том случае, 
     * если размер входной очереди меньше максимальной длины очереди 
     * {@link Connection#maxInputQueueSize}. 
     * @param ircIncomingMessage сообщение.
     * @return true признак успеха выполнения метода,
     * false поместить сообщение в очередь не удалось. 
     */
    public boolean offerToInputQueue(IrcIncomingMessage ircIncomingMessage) {
        return getConnection().offerToInputQueue(ircIncomingMessage);
    }
    
    /**
     * Метод используется для очистки входной очереди клиента IRC.
     */
    public void dropInputQueue() {
        getConnection().dropInputQueue();
    }

    /**
     * Метод используется для получения доступа к входной очереди 
     * клиента IRC.
     * @return BlockingQueue<IrcCommandReport> входная очередь 
     * клиента IRC.
     
    public BlockingQueue<IrcIncomingMessage> getInputQueue() {
        return  getConnection().getInputQueue();
    }
    */
    /**
     * Метод используется для получения количества элементов во входной 
     * очереди.
     * @return количества элементов во входной очереди.
     */
    public int getInputQueueSize() {
        return  getConnection().getInputQueueSize();
    }
    
    /**
     * Метод используется для помещения сообщения в  выходную очередь 
     * клиента IRC. Сообщение будет помещено в очередь в том случае, 
     * если размер выходной очереди меньше максимальной длины очереди 
     * {@link Connection#maxOutputQueueSize}. 
     * @param ircCommandReport сообщение.
     * @return true признак успеха выполнения метода,
     * false поместить сообщение в очередь не удалось. 
     */
    public boolean offerToOutputQueue(IrcCommandReport ircCommandReport) {
        return  getConnection().offerToOutputQueue(ircCommandReport);
    }
    
    /**
     * Метод используется для очистки выходной очереди клиента IRC.
     */
    public void dropOutputQueue() {
        getConnection().dropOutputQueue();
    }

    /**
     * Метод используется для получения доступа к выходной очереди 
     * клиента IRC.
     * @return BlockingQueue<IrcCommandReport> выходная очередь 
     * клиента IRC.
     */
    public BlockingQueue<IrcCommandReport> getOutputQueue() {
        return getConnection().getOutputQueue();
    }

    /**
     * Метод используется для получения количества элементов в выходной 
     * очереди.
     * @return количество элементов в выходной очереди.
     */
    public int getOutputQueueSize() {
        return getConnection().getOutputQueueSize();
    }
   
    public int compareTo(Object object) {
        int result = 0;
        if (!(object instanceof IrcTalker)) {
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
    
    /** 
     * Проверка того, что клиент может послать сообщение в этому клиенту,
     * на основании его полномочий и режимов этого клиента и того, (что
     * средняя скорость сообщений передаваемых клиенту не превышает 
     * величину {@link #maxOutputRate} - не реализовано). Этот клиент 
     * примет сообщение от клиента в следующих случаях:
     * <UL>
     *      <LI> клиент является оператором {@link UserMode#o};</LI>
     *      <LI> клиент является этим сервером 
     *      {@link Globals#thisIrcServer};</LI>
     *      <LI> если средняя скорость сообщений клиента не превышает 
     *      величину {@link #maxOutputRate}.</LI>
     * </UL>
     * 
     * @param requestor клиент.
     * @return true если клиент может послать сообщение в этому клиенту.
     
    public boolean canReceive(IrcTalker requestor) {
        boolean result = false;
        if (requestor instanceof IrcServer && 
                ((IrcServer) requestor) == Globals.thisIrcServer.get()) {
            result = true;
        } else if (requestor instanceof User && 
                ((User) requestor).isOperator()) {
            result = true;
        } else if (avgOutputRate.getAvgInterval() < 10000 / maxOutputRate.get()) {
            result = false;
        } else {
            result = true;
        }
        return result;
    }
    */
    
    /** 
     * Установка state.
     * @param state
     */
    public void setState(IrcTalkerState state) {
        stateWLock.lock();
        try {
            this.state = state;
        } finally {
            stateWLock.unlock();
        }
    }

    /** Получение state. 
     * @return state
     */
    public IrcTalkerState getState() {
        IrcTalkerState result = null;
        stateRLock.lock();
        try {
            result = state;
        } finally {
            stateRLock.unlock();
        }
        return result;
    }
    
    /** Перевод клиента в состояние {@link IrcTalkerState#CLOSE}. */
    public void close() {
        stateWLock.lock();
        try {
            if (getState() != IrcTalkerState.CLOSE &&
                getState() != IrcTalkerState.CLOSING &&
                getState() != IrcTalkerState.CLOSED &&
                getState() != IrcTalkerState.BROKEN) {
                setState(IrcTalkerState.CLOSE);
                stateTime.set(System.currentTimeMillis());
                Globals.logger.get().log(Level.FINER, "ircTalker:" + 
                        IrcTalker.this + " connection:" + getConnection() 
                        + " ircTalker set CLOSE");
            }
        } finally {
            stateWLock.unlock();
        }
    }

    /** Перевод клиента в состояние {@link IrcTalkerState#BROKEN}. */
    public void setBroken() {
        stateWLock.lock();
        try {
            if (getState() != IrcTalkerState.CLOSE &&
                getState() != IrcTalkerState.CLOSING &&
                getState() != IrcTalkerState.CLOSED &&
                getState() != IrcTalkerState.BROKEN) {
                setState(IrcTalkerState.BROKEN);
                stateTime.set(System.currentTimeMillis());
                Globals.logger.get().log(Level.FINER, "ircTalker:" + 
                        IrcTalker.this + " connection:" + getConnection() 
                        + " ircTalker set BROKEN");
            }
        } finally {
            stateWLock.unlock();
        }
    }
}
