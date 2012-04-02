/*
 * 
 * InputStreamProcessor 
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
import java.util.concurrent.atomic.*;
import java.net.*;
import java.io.*;

/**
 * InputStreamProcessor - программный процессор, который обслуживает 
 * буферированные потоки ввода. В основном цикле просматриваются потоки 
 * и из них считываются сообщения клиентов. Затем эти сообщения 
 * помещаются во входные очереди сетевого соединения. 
 *
 * @version 0.5 2012-02-13
 * @author  Nikolay Kirdin
 */
public class InputStreamProcessor implements Runnable, 
IrcServerProcessor {

    /** 
     * Управление выполнением/остановом основного цикла.
     * true - цикл выполняется, false - цикл приостановлен. 
     */ 
    public AtomicBoolean running = new AtomicBoolean(true);

    /** 
     * Управление выполнением/завершением основного цикла.
     * true - цикл завершается, false - цикл может выполнятся.
     */ 
    public AtomicBoolean down = new AtomicBoolean(false);

    /** Поток метода run этого объекта. */ 
    public AtomicReference<Thread> thread = 
            new AtomicReference<Thread>();

    /** Минимальная длительность таймаутов. */
    public AtomicLong limitingTO = new AtomicLong(5);

    /** Стандартная длительность таймаутов. */
    public AtomicLong sleepTO = new AtomicLong(100);

    /** 
     * Планируемая длительность (ms) выполнения основного цикла 
     * (без таймаута). 
     */
    public AtomicLong plannedDuration = new AtomicLong(45);

    /** Признак высокой загруженности процессора. */
    private boolean highLoad;    

    /** Конструктор по умолчанию. */
    public InputStreamProcessor() {}

    /**
     * Метод run() - это метод, который просматриваются входные потоки 
     * сетевых соединений.
     *
     * <P>В основном цикле этого метода производится просмотр потоков 
     * ввода сетевых соединений. Если в буферированом потоке будет 
     * находится строка текста, то эта строка будет извлечена из буфера 
     * и помещена во входные очереди сетевого соединения. Время 
     * блокировки операций чтения ограничено временем блокировки 
     * соответствующего сокета.
     *
     * <P>Для того чтобы ограничить частоту подключений в цикл введен 
     * таймаут. Длительность этого таймаута ограничено снизу величиной 
     * {@link #limitingTO}, в тех случаях, когда необходим более 
     * продолжительный таймаут ее значение автоматически увеличивается.
     *
     * <P>Если во время выполнения основного цикла, будут обнаружены 
     * какие-либо ошибки подсистемы ввода, то состояние соответствующего 
     * объекта класса {@link NetworkConnection} будет переведено в 
     * состояние {@link ConnectionState#BROKEN}.
     *
     * <P>Чтения из потока ввода производится только в том случае, если   
     * состояние соответствующего объекта {@link NetworkConnection} 
     * является {@link IrcTalkerState#OPERATIONAL}. 
     *
     * <P>Чтения из потока ввода производится только в том случае, если
     * средние значения периода между входящими сообщениями больше чем  
     * в {@link Globals#minAvgReadPeriod}.
     *
     * <P>После успешной операции ввода значение переменной 
     * {@link NetworkConnection#readCountDelta} для этого объекта  
     * увеличивается на 1.  
     * 
     * <P> С помощью средней длительности основного цикла, 
     * запланированной длительности основного цикла, минимальной 
     * продолжительности таймаута и средней продолжительности 
     * таймаута определяется степень нагруженности этого программного 
     * процессора. При выполнении любого из следующих условий этот 
     * программный процессор признается сильно нагруженным:
     * <OL>
     *     <LI>Средняя длительность основного цикла составляет 70% от
     *  запланированной длительности основного цикла более.</LI>
     *  <LI>Средняя продолжительность таймаута на 10% больше 
     *  запланированной продолжительности таймаута.</LI> 
     * </OL>
     * <P>Режим высокой нагруженности сбрасывается при выполнении всех 
     * следующих условий:
     * <OL>
     *     <LI>Средняя длительность основного цикла на 50%  меньше
     *  запланированной длительности основного цикла более.</LI>
     *  <LI>Средняя продолжительность таймаута меньше или равна 
     *  запланированной продолжительности таймаута.</LI> 
     * </OL> 
     * 
     * <P> Каждые {@link Globals#monitoringPeriod} (ms) в канал 
     * {@link Globals#monitorIrcChannel} выводятся диагностические 
     * сообщения содержащие следующую информацию:
     * <UL>
     *         <LI>длину списка соединений;</LI>
     *         <LI>среднее время (ms) выполнения основного цикла 
     *         (без таймаута);</LI>
     *         <LI>средняя длительность (ms) таймаута;</LI>
     *         <LI>средняя планируемая длительность (ms) таймаутов.</LI>
     * </UL>
     */
    public void run() {
        Logger logger = Globals.logger.get();
        BufferedReader br = null;
        IrcTalker ircTalker = null;
        Iterator<Connection> connectionListIterator = null;
        int connectionListSize = 0;

        String inputString;
        long avgWorkingTime = 0;
        int procTimeLength = 100;
        long waitingTO = limitingTO.get();
        long startMonitorTime = System.currentTimeMillis();
        IrcAvgMeter avgWork = new IrcAvgMeter(procTimeLength);

        IrcAvgMeter avgWaitingTO = new IrcAvgMeter(procTimeLength);
        avgWaitingTO.setValue(waitingTO);

        long avgActualTO = 0;
        IrcAvgMeter avgTO = new IrcAvgMeter(procTimeLength);
        avgTO.setValue(limitingTO.get());
        logger.log(Level.FINEST, "Running");


        while (!down.get()) {

            avgWork.intervalStart(System.currentTimeMillis());
            while (!running.get() && !down.get()) {
                try {
                    Thread.sleep(sleepTO.get());
                } catch (InterruptedException e) {}
                avgWork.intervalStart(System.currentTimeMillis());
            }

            if (down.get()) {
                break;
            }

            connectionListSize = 0;
            connectionListIterator = 
                    Globals.db.get().getConnectionListIterator();

            while (connectionListIterator.hasNext()) {
                connectionListSize++;
                Connection connection = connectionListIterator.next();
                

                if (connection.getConnectionState() != 
                        ConnectionState.OPERATIONAL) {
                    continue;
                }
                if (connection.inputQueue.get() != null) {
                    continue;
                }
                
                br = connection.br.get();
                if (br == null ) {
                    connection.setBroken();
                    continue;
                }

                if (connection.avgInputPeriod.get() <
                        connection.minAvgInputPeriod.get()) {
                    continue;
                }

                try {
                    synchronized (br) {
                        if (!br.ready()) {
                            continue;
                        }
                        inputString = br.readLine();
                    }

                    if (inputString == null) {
                        connection.close();
                        continue;
                    }

                    connection.readCountDelta.getAndIncrement();
                    long currentTime = System.currentTimeMillis();
                    
                    ircTalker = connection.ircTalker.get();
                    ircTalker.setLastMessageTime(currentTime);
                    connection.avgInputPeriodMeter.setValue(currentTime);

                    boolean result = connection.offerToInputQueue(new 
                            IrcIncomingMessage(inputString, ircTalker));

                    if (!result) {
                        String remark = Reply.makeText(
                                Reply.ERR_FILEERROR, 
                                ircTalker.getNickname(),
                                "offer to input queue " ,
                                connection.toString());
                        OutputQueueProcessor.process(connection, remark);
                        connection.setBroken();
                    }

                } catch (SocketTimeoutException e) {
                } catch (IOException e) {
                    connection.setBroken();
                    logger.log(Level.INFO, "Connection:" + 
                            connection + " " + e);
                }                
            }

            avgWork.intervalEnd(System.currentTimeMillis());

            avgActualTO = avgTO.getAvgValue();
            avgTO.intervalStart(System.currentTimeMillis());
            avgWorkingTime = avgWork.getAvgValue();

            waitingTO = Math.max(limitingTO.get(), Math.min(
                    plannedDuration.get(),
                    plannedDuration.get() - avgWorkingTime));

            avgWaitingTO.setValue(waitingTO);
            long awto = avgWaitingTO.getAvgValue();

            if (!highLoad && (
                    (avgWorkingTime * 7 >= plannedDuration.get() * 10) ||
                    (avgActualTO * 10 > waitingTO * 11))) {
                
                highLoad = true;
                
                logger.log(Level.INFO, 
                        "Set highLoad." + 
                        " avgWorkingTime (ms):" + avgWorkingTime +
                        " plannedDuration (ms):" + plannedDuration.get()
                        + " avgActualTO (ms):" + avgActualTO +
                        " avgWaitingTO (ms):" + awto);
                
                /** Индикация высокой загруженности. */
                Globals.ircServerProcessorSet.get().add(this);
                
            } else if (highLoad && 
                    (avgWorkingTime * 2 <= plannedDuration.get()) &&
                    (avgActualTO <= waitingTO)) {
                
                highLoad = false;
                
                logger.log(Level.INFO, 
                        "UnSet highLoad." + 
                        " avgWorkingTime (ms):" + avgWorkingTime +
                        " plannedDuration (ms):" + plannedDuration.get()
                        + " avgActualTO (ms):" + avgActualTO +
                        " avgWaitingTO (ms):" + awto);
                
                /** Сброс индикации высокой загруженности. */
                for (IrcServerProcessor isp: 
                    Globals.ircServerProcessorSet.get()) {
                    if (isp instanceof InputStreamProcessor) {
                        Globals.ircServerProcessorSet.get().remove(isp);
                        break;
                    }
                }
            }

            if ((System.currentTimeMillis() - startMonitorTime) >= 
                    Globals.monitoringPeriod.get()) {

                String monitoringString = "InputStreamProcessor:" +  
                        " size:" + connectionListSize +
                        " avgWorkingTime (ms):" + avgWorkingTime +
                        " avgActualTO (ms):" + avgActualTO +
                        " avgWaitingTO (ms):" + awto;
                logger.log(Level.FINEST, monitoringString);        
                
                if (Globals.monitorIrcChannel.get() != null ) {
                    List<String> targetList = new ArrayList<String>();
                    String channelname = 
                            Globals.monitorIrcChannel.get().getNickname();
                    targetList.add(channelname);
                    NoticeIrcCommand.create(Globals.db.get(), 
                            Globals.anonymousUser.get(), 
                            targetList,
                            monitoringString).run();
                }
                startMonitorTime = System.currentTimeMillis();
            }

            try {
                Thread.sleep(waitingTO);
            } catch (InterruptedException e) {}
            avgTO.intervalEnd(System.currentTimeMillis());

        }logger.log(Level.FINEST, "Ended");
    }
}
