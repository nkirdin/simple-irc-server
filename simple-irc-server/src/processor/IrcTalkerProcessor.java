/*
 * 
 * IrcTalkerProcessor 
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

/**
 * Программный процессор, который обследует объекты, хранящие информацию
 * о клиентах сервера. Он проверяет их состояние и состояние 
 * соответствующего соединения. В случае обнаружения ошибочного 
 * состояния или этапа завершения жизненного цикла этого объекта, будут 
 * выполнены завершающие действия для этого объекта. 
 *
 * @version 0.5 2012-02-12
 * @author  Nikolay Kirdin
 */
public class IrcTalkerProcessor implements Runnable, IrcServerProcessor {

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

    /** Стандартная длительность таймаутов. */
    public AtomicLong sleepTO = new AtomicLong(100);

    /** Минимальная длительность таймаута основного цикла. */
    public AtomicLong limitingTO = new AtomicLong(100);

    /** 
     * Планируемая длительность одного цикла. Определяет нижнюю границу
     * максимальной длительности выполнения цикла, она включает в себя 
     * таймаут цикла.
     */
    public AtomicLong plannedDuration = new AtomicLong(900);

    /** Признак высокой загруженности процессора. */
    private boolean highLoad;    

    /** Конструктор. */
    public IrcTalkerProcessor() {}

    /**
     * Метод, который обследует объекты классов User, Service и 
     * IrcServer.    
     * 
     * <P>В основном цикле этого метода проверяется состояние объекта и 
     * соответствующего соединения. В том случае, если объект должен 
     * завершить свой жизненый цикл или соединение завершает или 
     * завершило свой жизненый цикл, будут проведены завершающие 
     * действия для обследуемого объекта. Состояние объекта определяется
     * полем {@link IrcTalker#state}. Состояние соединения определяется 
     * полем {@link Connection#connectionState}.   
     *
     * <P>В том случае, если значение поля {@link IrcTalker#state} 
     * обследуемого объекта будет {@link IrcTalkerState#BROKEN}, то для 
     * этого объекта будет выполнена команда IRC QUIT с параметром 
     * <code>"Broken state: " + (new Date()).toString()</code>.
     *
     * <P>В том случае, если значение поля {@link IrcTalker#state} 
     * обследуемого объекта будет {@link IrcTalkerState#CLOSE}, то этот 
     * объект будет удален из репозитария и его состояние будет изменено
     * на состояние {@link IrcTalkerState#CLOSED}.
     *
     * <P>Если будет обнаружено, что у обследуемого объекта нет  
     * соединения или соответвующее ему соединение находится в одном из 
     * следующих состояний: {@link ConnectionState#CLOSED} или 
     * {@link ConnectionState#BROKEN}, то состояние этого объекта 
     * будет изменено на состояние {@link IrcTalkerState#BROKEN}.
     * 
     * <P> С помощью средней длительности основного цикла, 
     * запланированной длительности основного цикла, минимальной 
     * продолжительности таймаута и средней продолжительности 
     * таймаута определяется степень нагруженности этого программного 
     * процессора. При выполнении любого из следующих условий этот 
     * программный процессор признается сильно нагруженным:
     * <OL>
     *     <LI>Средняя длительность основного цикла составляе более 70% от
     *  запланированной длительности основного цикла.</LI>
     *  <LI>Средняя продолжительность таймаута на 10% больше 
     *  вычисленной продолжительности таймаута.</LI> 
     * </OL>
     * Режим высокой нагруженности сбрасывается при выполнении всех 
     * следующих условий:
     * <OL>
     *     <LI>Средняя длительность основного цикла на 50%  меньше
     *  запланированной длительности основного цикла более.</LI>
     *  <LI>Средняя продолжительность таймаута меньше или равна 
     *  вычисленной продолжительности таймаута.</LI> 
     * </OL> 
     * <P> Каждые {@link Globals#monitoringPeriod} (ms) в канал 
     * {@link Globals#monitorIrcChannel} выводятся диагностические 
     * сообщения содержащие следующую информацию:
     * <UL>
     *         <LI>длину списка клиентов IRC;</LI>
     *         <LI>среднее время (ms) выполнения основного цикла 
     *         (без таймаута);</LI>
     *         <LI>средняя длительность (ms) таймаута;</LI>
     *         <LI>средняя планируемая длительность (ms) таймаутов.</LI>
     * </UL>
     */
    public void run() {

        Logger logger = Globals.logger.get();        

        Iterator<User> ircTalkerSetIterator = null;
        int ircTalkerSetSize = 0;
        int procTimeLength = 100;
        long avgWorkingTime = 0;
        long waitingTO = limitingTO.get();
        long startMonitorTime = System.currentTimeMillis();
        IrcAvgMeter avgWork = new IrcAvgMeter(procTimeLength);

        long avgActualTO = 0;
        IrcAvgMeter avgTO = new IrcAvgMeter(procTimeLength);
        avgTO.setValue(limitingTO.get());

        IrcAvgMeter avgWaitingTO = new IrcAvgMeter(procTimeLength);
        avgWaitingTO.setValue(waitingTO);

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

            ircTalkerSetSize = 0;
            ircTalkerSetIterator = Globals.db.get().getUserSetIterator();

            while (ircTalkerSetIterator.hasNext()) {
                IrcTalker ircTalker = ircTalkerSetIterator.next();
                ircTalkerSetSize ++;

                Connection connection = ircTalker.getConnection();

                if (connection == null ||
                        connection.getConnectionState() ==
                        ConnectionState.CLOSED ||
                        connection.getConnectionState() ==
                        ConnectionState.BROKEN) {
                    ircTalker.setBroken();
                }

                IrcTalkerState ircTalkerState = ircTalker.getState();

                switch (ircTalkerState) {
                case NEW:
                    break;
                case REGISTERING:
                case REGISTERED:                        
                    break;                    
                case BROKEN:
                    QuitIrcCommand.create(Globals.db.get(), ircTalker, 
                            "Broken state: " +               
                            (new Date()).toString()).run();
                case CLOSE:
                case CLOSING:
                    ircTalker.setState(IrcTalkerState.CLOSED);
                    ircTalker.stateTime.set(
                            System.currentTimeMillis());
                    if (ircTalker instanceof User) {
                        Globals.db.get().unRegister((User) ircTalker);
                    } else if (ircTalker instanceof Service) {
                        Globals.db.get().unRegister((Service) ircTalker);
                    } else if (ircTalker instanceof IrcServer) {
                        Globals.db.get().unRegister((IrcServer) ircTalker);
                    } else {
                        logger.log(Level.SEVERE,
                                "IrcTalkerProcessor. Internal error." +
                                        " Unknown IrcTalker:" + ircTalker);
                        throw new Error("IrcTalkerProcessor. " +
                                "Internal error." +
                                "Unknown IrcTalker:" + ircTalker);
                    }
                    logger.log(Level.FINEST, "ircTalker:" + 
                            ircTalker + " deleted and unregistered.");
                    logger.log(Level.FINEST, "ircTalker:" + 
                            ircTalker + ircTalker.getConnection() + 
                            " ircTalker set CLOSED");

                    break;
                case OPERATIONAL:
                    break;
                case CLOSED:
                    break;
                default:
                    logger.log(Level.SEVERE,
                            "IrcTalkerProcessor. Internal error." +
                            " Unknown IrcTalker state:" + ircTalker + 
                            " " + ircTalker.getState());
                    throw new Error("IrcTalkerProcessor. Internal " 
                            + "error." + "Unknown IrcTalker state:" + 
                            ircTalker + " " + ircTalker.getState());
                    //                        break;
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

            if (!highLoad && 
                    ((avgWorkingTime * 7 >= plannedDuration.get() * 10) ||
                    (avgActualTO * 10 > waitingTO * 11))) {

                highLoad = true;

                logger.log(Level.INFO, "Set highLoad." + 
                        " avgWorkingTime (ms):" + avgWorkingTime +
                        " plannedDuration (ms):" + plannedDuration.get() +
                        " avgActualTO (ms):" + avgActualTO +
                        " avgWaitingTO (ms):" + awto);

                /** Индикация высокой загруженности. */
                Globals.ircServerProcessorSet.get().add(this);

            } else if (highLoad && 
                    (avgWorkingTime * 2 <= plannedDuration.get()) &&
                    (avgActualTO <= waitingTO)) {

                highLoad = false;

                logger.log(Level.INFO, "UnSet highLoad." + 
                        " avgWorkingTime (ms):" + avgWorkingTime +
                        " plannedDuration (ms):" + plannedDuration.get() +
                        " avgActualTO (ms):" + avgActualTO +
                        " avgWaitingTO (ms):" + awto);

                /** Сброс индикации высокой загруженности. */
                for (IrcServerProcessor isp: 
                    Globals.ircServerProcessorSet.get()) {
                    if (isp instanceof IrcTalkerProcessor) {
                        Globals.ircServerProcessorSet.get().remove(isp);
                        break;
                    }
                }
            }

            if ((System.currentTimeMillis() - startMonitorTime) >= 
                    Globals.monitoringPeriod.get()) {
                String monitoringString = "IrcTalkerProcessor:" +  
                        " size:" + ircTalkerSetSize +
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
        }

        logger.log(Level.FINEST, "Ended");
    }

    /**
     * Выполнение завершающих действий при останове сервера. Для всех 
     * объектов классов порожденных от {@link IrcTalker} будет выполнена 
     * команда IRC QUIT с параметром <code>"Termination."</code>.
     */
    public void termination() {

        Logger logger = Globals.logger.get();        

        logger.log(Level.FINEST, "Started");

        Iterator<User> ircTalkerSetIterator = null;
        ircTalkerSetIterator = Globals.db.get().getUserSetIterator(); 

        while (ircTalkerSetIterator.hasNext()) {
            IrcTalker ircTalker = ircTalkerSetIterator.next();
            logger.log(Level.FINER, "ircTalker:" + ircTalker + 
                    ircTalker.getConnection() + " Termination.");

            QuitIrcCommand.create(Globals.db.get(), ircTalker, 
                    "Termination.").run();
        }

        LinkedHashSet<IrcTalker> processingIrcTalkerSet =
                new LinkedHashSet<IrcTalker>();

        LinkedHashSet<Service> serviceSet = 
                Globals.db.get().getServiceSet();
        processingIrcTalkerSet.addAll(serviceSet);

        LinkedHashSet<IrcServer> ircServerSet = 
                Globals.db.get().getIrcServerSet();
        processingIrcTalkerSet.addAll(ircServerSet);

        processingIrcTalkerSet.remove(Globals.thisIrcServer.get());

        for (IrcTalker ircTalker: processingIrcTalkerSet) {
            logger.log(Level.FINER, "ircTalker:" + ircTalker + 
                    ircTalker.getConnection() + " Termination.");

            QuitIrcCommand.create(Globals.db.get(), 
                    ircTalker, "Termination.").run();
        }

        logger.log(Level.FINEST, "Ended");
    }
}
