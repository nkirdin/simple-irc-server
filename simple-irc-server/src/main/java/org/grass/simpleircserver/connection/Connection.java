package org.grass.simpleircserver.connection;
/*
 * 
 * Connection 
 *  * is part of Simple Irc Server
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

import java.util.logging.*;

import org.grass.simpleircserver.base.Constants;
import org.grass.simpleircserver.base.Globals;
import org.grass.simpleircserver.parser.IrcCommandReport;
import org.grass.simpleircserver.parser.IrcIncomingMessage;
import org.grass.simpleircserver.talker.IrcTalker;
import org.grass.simpleircserver.tools.IrcAvgMeter;

import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.io.*;
import java.nio.charset.*;

/**
 * Класс, который хранит информацию о соединении. Объект этого класса 
 * характеризуется состояниями, которые определены в 
 * {@link ConnectionState}. 
 * 
 * Используются следующие поля интерфейсов {@link Constants} и 
 * {@link Globals}:
 * {@link Constants#MIN_FREE_MEMORY};
 * {@link Globals#minAvgReadPeriod};
 * {@link Globals#logger};
 *
 * @version 0.5.1 2012-03-27
 * @version 0.5.3 2015-11-05 Program units were moved from default package into packages with names. Unit tests were added.
 * @author  Nikolay Kirdin
 * 
 */
public class Connection implements Runnable, Comparable {
    
    /** Счетчик генератора уникальных идентификаторов класса. */
    private static AtomicLong seqNew = new AtomicLong();
    
    /** 
     * Счетчик количества операций чтения по всем объектам
     * класса.
     */
    public static AtomicLong totalReadCount = new AtomicLong();
    
    /** 
     * Счетчик количества операций записи по всем объектам
     * класса.
     */
    public static AtomicLong totalWriteCount = new AtomicLong();
    
    /** Уникальный идентификатор объекта. */
    public final long id;
    
    /** Ссылка на объект с информаций о клиенте. */
    public AtomicReference<IrcTalker> ircTalker =
            new AtomicReference<IrcTalker>();
        
    /** Буферированный поток ввода. */
    public AtomicReference<BufferedReader> br =
            new AtomicReference<BufferedReader>();
            
    /** Буферированный поток вывода. */            
    public AtomicReference<BufferedWriter> bw =
            new AtomicReference<BufferedWriter>();
        
    /** Кодировка, которая используется для этого соединения.  */            
    public AtomicReference<Charset> charset =
            new AtomicReference<Charset>(Charset.forName("UTF-8"));

    /** Состояние соединения. */
    private ConnectionState connectionState;
    
    /** Время перехода в текущее состояние. */            
    public AtomicLong connectionStateTime = new AtomicLong();
    
    /** ReadWrite Lock для ConnectionState*/
    private final ReentrantReadWriteLock connectionStateRWLock = 
            new ReentrantReadWriteLock();
    
    /** ReadLock для connectionState. */
    private final Lock connectionStateRLock = 
            connectionStateRWLock.readLock();
    
    /** WriteLock для connectionState. */
    private final Lock connectionStateWLock = 
            connectionStateRWLock.writeLock();

    /** 
     * Управление исполнением процессов, связанных с этим соединением.  
     */            
    public AtomicBoolean running = new AtomicBoolean(true);
    
    /** Общее количество операций чтения для этого объекта. */
    public AtomicLong readCount = new AtomicLong();
    
    /**
     * Количество операций чтения, во время последнего обращения к 
     * {@link #br}. 
     */
    public AtomicLong readCountDelta = new AtomicLong();
    
    /** 
     * Средний период между операциями чтения. 
     */    
    public AtomicLong avgInputPeriod = new AtomicLong();
    
    /** Общее количество операций записи для этого объекта. */
    public AtomicLong writeCount = new AtomicLong();
    
    /**
     * Количество операций записи, во время последнего обращения к 
     * {@link #bw}. 
     */
    public AtomicLong writeCountDelta = new AtomicLong();
    
    /** Максимально допустимая скорость вывода сообщений. */
    public AtomicInteger maxOutputRate = new AtomicInteger(10);
    
    /** Средняя скорость вывода сообщений. */
    public IrcAvgMeter avgOutputRate = new IrcAvgMeter(300);
        
    /** Минимальный средней период (ms) поступления  входящих сообщений
     * для соединения  ({@link Connection}).
     */       
    public AtomicLong minAvgInputPeriod = 
            new AtomicLong(Globals.minAvgReadPeriod.get());
    
    /** Средняя скорость ввода сообщений. */
    public IrcAvgMeter avgInputPeriodMeter = new IrcAvgMeter(300);

    /** Время отправки клиенту последнего сообщения IRC PING. */
    public AtomicLong pingTime = new AtomicLong();
    
    /** Время приема от клиента последнего сообщения IRC PONG. */
    public AtomicLong pongTime = new AtomicLong();
    
    /** Максимальный размер входной очереди для клиента. */
    private int maxInputQueueSize = 1;
    
    /** Входная очередь. */
    public AtomicReference<IrcIncomingMessage> inputQueue;

    /** Максимальный размер выходной очереди для клиента. */
    private int maxOutputQueueSize = 5020;
    
    /** Выходная очередь. */
    public BlockingQueue<IrcCommandReport> outputQueue;
     
    /** Поток метода run(). */            
    public AtomicReference<Thread> thread = new AtomicReference<Thread>();

    /**
     * Конструктор по умолчанию. Этот конструктор создает уникальный 
     * идентификатор и задает начальные состояния следущим полям: 
     * <UL>
     *      <LI> {@link #connectionState}, начальное значение - 
     *      {@link ConnectionState#NEW};</LI>
     *      <LI> {@link #connectionStateTime}, начальное значение - 
     *      текущее время;</LI>
     *      <LI> {@link #pingTime}, начальное значение - текущее время;
     *      </LI>
     *      <LI> {@link #pongTime}, начальное значение - текущее время.
     *      </LI>
     * </UL>
     */
    protected Connection() {
        this(ConnectionState.NEW);
    }

    /**
     * Конструктор с параметром класса {@link ConnectionState}. Этот
     * конструктор создает уникальный идентификатор и задает 
     * начальные состояния следущим полям: 
     * <UL>
     *      <LI> {@link #connectionState}, этому полю присваивается 
     *      значение параметра connectionState;</LI>
     *      <LI> {@link #connectionStateTime}, начальное значение - 
     *      текущее время;</LI>
     *      <LI> {@link #pingTime}, начальное значение - текущее время;
     *      </LI>
     *      <LI> {@link #pongTime}, начальное значение - текущее время.
     *      </LI>
     * </UL>
     * @param connectionState начальное состояние объекта.
     */
    protected Connection (ConnectionState connectionState) {
        id = seqNew.getAndIncrement();
        this.connectionState = connectionState;
        long currentTime = System.currentTimeMillis();
        connectionStateTime.set(currentTime);
        pingTime.set(currentTime);
        pongTime.set(currentTime);
        avgInputPeriodMeter.setValue(currentTime - 10000);
        inputQueue = new AtomicReference<IrcIncomingMessage>();
        outputQueue = new ArrayBlockingQueue<IrcCommandReport>(
                maxOutputQueueSize);
    }

    /**
     * Создатель объекта без параметров. Проверяется объем свободной 
     * памяти, если этот объем меньше {@link Constants#MIN_FREE_MEMORY}, 
     * то будет вызван сборщик мусора. Если после прцедуры сборки мусора, 
     * памяти будет недостаточно, то объект создаваться не будет.
     * @return новый объект класса IrcServer.
     */
    public static Connection create() {
        Connection result = null;
        long freeMemory = Runtime.getRuntime().freeMemory();
        if (freeMemory < Constants.MIN_FREE_MEMORY) {
            System.gc();
        }
        freeMemory = Runtime.getRuntime().freeMemory();
        if (freeMemory >= Constants.MIN_FREE_MEMORY) {
            result = new Connection();
        } else {
            Globals.logger.get().log(Level.SEVERE, 
                    "Insufficient free memory (b): " + freeMemory);
        }
        return result;
    }
    
    /**
     * Текстовое представление объекта. Он представляется строкой 
     * следующего вида: 
     * <code>"
     * &lt;"Connection-"&gt;&lt;уникальный идентификатор&gt;"
     * </code>
     * @return строка с текстовым представлением объекта.
     */
    public String toString() {
        return String.format("Connection-%09d", getId());
    }

    /**
     * Метод закрывает буферированные потоки ввода/вывода.
     */
    public void delete() {
        running.set(false);
        try {
            Thread.sleep(5);
        } catch (InterruptedException e) {}

        try {
            if (bw.get() != null) {
                bw.get().close();
            }
        } catch (IOException e) {
            Globals.logger.get().log(Level.INFO, "Closing:" + bw.get() 
            		+ " " + e);
        }
        try {
            if (br.get() != null) {
                br.get().close();
            }
        } catch (IOException e) {
            Globals.logger.get().log(Level.INFO, "Closing:" + br.get() 
            		+ " " + e);
        }
    }

    /** 
     * Установка connectionState.
     * @param connectionState
     */
    public void setConnectionState(ConnectionState connectionState) {
        connectionStateWLock.lock();
        try {
            this.connectionState = connectionState;
        } finally {
            connectionStateWLock.unlock();
        }
    }
    
    /**
     * Получение connectionState.
     * @return connectionState.
     */
    public ConnectionState getConnectionState() {
        ConnectionState result = null;
        connectionStateRLock.lock();
        try {
            result = connectionState;
        } finally {
            connectionStateRLock.unlock();
        }
        return result;
    }
    
    /**
     * Метод устанавливает состояние объекта в состояние 
     * {@link ConnectionState#CLOSE}.  
     */
    public void close() {
        connectionStateWLock.lock();
        try {
            if (getConnectionState() != ConnectionState.CLOSE &&
                    getConnectionState() != ConnectionState.CLOSING &&
                    getConnectionState() !=    ConnectionState.CLOSED &&
                    getConnectionState() !=    ConnectionState.BROKEN) {
                setConnectionState(ConnectionState.CLOSE);
                connectionStateTime.set(System.currentTimeMillis());
                Globals.logger.get().log(Level.FINER, "connection:" + 
                        Connection.this + " ircTalker:" + ircTalker.get() 
                        + " connection set CLOSE");
            }            
        } finally {
            connectionStateWLock.unlock();
        }
     }

    /**
     * Метод устанавливает состояние объекта в состояние 
     * {@link ConnectionState#BROKEN}.  
     */
    public void setBroken() {
        connectionStateWLock.lock();
        try {
            if (getConnectionState() != ConnectionState.CLOSE &&
                    getConnectionState() != ConnectionState.CLOSING &&
                    getConnectionState() != ConnectionState.CLOSED &&
                    getConnectionState() !=    ConnectionState.BROKEN) {
                setConnectionState(ConnectionState.BROKEN);
                connectionStateTime.set(System.currentTimeMillis());
                Globals.logger.get().log(Level.FINER, "connection:" + 
                        Connection.this + " ircTalker:" + ircTalker.get() 
                        + " connection set BROKEN");
            }            
        } finally {
            connectionStateWLock.unlock();
        }
    }

    /**
     * Метод возвращает максимальный размер входной очереди для клиента.
     * @return максимальный размер входной очереди для клиента.
     */
    public int getMaxInputQueueSize() {
        return maxInputQueueSize;
    }

    /**
     * Метод возвращает максимальный размер выходной очереди для 
     * клиента.
     * @return максимальный размер выходной очереди для клиента.
     */
    public int getMaxOutputQueueSize() {
        return maxOutputQueueSize;
    }
    

    /**
     * Метод, предназначенный для проведения тестирования. После запуска 
     * объект переводится в состояние 
     * {@link ConnectionState#OPERATIONAL}. Метод продолжает 
     * выполнять бесконечный цикл до тех пор, пока значение поля 
     * {@link #running} остается равным true. После выхода из цикла
     * закрываются буферированные потоки ввода/вывода, Объект 
     * последовательно переводится в состояния 
     * {@link ConnectionState#CLOSING}, {@link ConnectionState#CLOSED}. 
     * После этого поток завершается.
     */
    public void run() {

        setConnectionState(ConnectionState.OPERATIONAL);

        while (running.get()) {
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {}
        }

        setConnectionState(ConnectionState.CLOSING);

        try {
            if (bw.get() != null)
                bw.get().close();
            Globals.logger.get().log(Level.FINEST, 
            		"Buffered Writer closed:" + bw.get());
        } catch (IOException e) {
            Globals.logger.get().log(Level.INFO, "Closing error:" + 
            		bw.get() + " " + e);
        }

        try {
            if (br.get() != null)
                br.get().close();
            Globals.logger.get().log(Level.FINEST, 
            		"Buffered Reader closed:" + br.get());
        } catch (IOException e) {
            Globals.logger.get().log(Level.INFO, "Closing error:" + 
            		br.get() + " " + e);
        }

        setConnectionState(ConnectionState.CLOSED);
        Globals.logger.get().log(Level.FINEST, "Ended.");
    }
        
    /**
     * Метод возвращает уникальный идентификатор этого объекта.
     * @return уникальный идентификатор этого объекта.
     */
    public long getId() {
        return id;
    }
    
    /**
     * Метод используется для помещения сообщения во  входную очередь 
     * клиента IRC. Сообщение будет помещено в очередь в том случае, 
     * если размер входной очереди меньше максимальной длины очереди 
     * {@link #maxInputQueueSize}. 
     * @param ircIncomingMessage сообщение.
     * @return true признак успеха выполнения метода,
     * false поместить сообщение в очередь не удалось. 
     */
    public boolean offerToInputQueue(
            IrcIncomingMessage ircIncomingMessage) {
        return inputQueue.compareAndSet(null, ircIncomingMessage);
    }
    
    /**
     * Метод используется для очистки входной очереди клиента IRC.
     */
    public void dropInputQueue() {
        inputQueue.set(null);
    }

    /**
     * Метод используется для получения количества элементов во входной 
     * очереди.
     * @return длина входной очереди. 
     * клиента IRC.
     */
    public int getInputQueueSize() {
        return 1;
    }
    
    /**
     * Метод используется для помещения сообщения в  выходную очередь 
     * клиента IRC. Сообщение будет помещено в очередь в том случае, 
     * если размер выходной очереди меньше максимальной длины очереди 
     * {@link #maxOutputQueueSize}. 
     * @param ircCommandReport сообщение.
     * @return true признак успеха выполнения метода, false - признак 
     * неудачи выполнения метода. 
     */
    public boolean offerToOutputQueue(IrcCommandReport ircCommandReport) {
        return outputQueue.offer(ircCommandReport);
    }
    
    /**
     * Метод используется для очистки выходной очереди клиента IRC.
     */
    public void dropOutputQueue() {
        outputQueue.clear();
    }

    /**
     * Метод используется для получения доступа к выходной очереди 
     * клиента IRC.
     * @return BlockingQueue<IrcCommandReport> выходная очередь 
     * клиента IRC.
     */
    public BlockingQueue<IrcCommandReport> getOutputQueue() {
        return outputQueue;
    }

    /**
     * Метод используется для получения количества элементов в выходной 
     * очереди.
     * @return BlockingQueue<IrcCommandReport> - выходная очередь 
     * клиента IRC.
     */
    public int getOutputQueueSize() {
        return outputQueue.size();
    }
   
    /** 
     * Метод реализующий функцию сравнения объектов, объекты 
     * сравниваются по их хэш-кодам. 
     */
    public int compareTo(Object connection) {
        int result = 0;
        if (!(connection instanceof Connection)) {
            throw new ClassCastException();
        }
        if (this.hashCode() < connection.hashCode()) {
            result = -1;
        } else if (this.hashCode() > connection.hashCode()) {
            result = 1;
        } else {
            result = 0;
        }
        return result;
    }
    
}
