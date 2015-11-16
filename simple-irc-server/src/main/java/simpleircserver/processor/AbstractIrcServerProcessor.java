/*
 * 
 * AbstractIrcServerProcessor 
 * is part of Simple Irc Server
 *
 *
 * Copyright (С) 2015, Nikolay Kirdin
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

package simpleircserver.processor;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;

import simpleircserver.base.Globals;
import simpleircserver.parser.commands.NoticeIrcCommand;
import simpleircserver.tools.IrcAvgMeter;

/**
 * Server - класс, который служит для управления запуском, остановом, и 
 * перезапуском основных компонентов сервера IRC. 
 *
 * @version 0.5.4 2015-11-13
 * @author  Nikolay Kirdin
 */
public abstract class AbstractIrcServerProcessor implements IrcServerProcessor, Runnable {
    
    /** Длинна выборки для обработки статистики */
    public final static int STATISTIC_DATA_LENGTH_DEFAULT = 100;
    
    /** Длительность таймаута по умолчанию (ms) */
    public final static int TIMEOUT_DURTION_STANDARD_DEFAULT = 90;
    
    /** Минимально допустимая длительность таймаута (ms)*/
    public final static int TIMEOUT_DURTION_MINIMAL_DEFAULT = 10;
    
    /** Стандартная длительность таймаутов  */
    public AtomicLong durationOfTimeout = new AtomicLong(TIMEOUT_DURTION_STANDARD_DEFAULT);

    /** Минимальная длительность таймаута основного цикла. */
    public AtomicLong minimalDurationOfTimeout = new AtomicLong(TIMEOUT_DURTION_MINIMAL_DEFAULT);

    /** 
     * Планируемая длительность одного цикла. Определяет нижнюю границу
     * максимальной длительности выполнения цикла, она включает в себя 
     * таймаут цикла.
     */
    public AtomicLong plannedDurationOfCycle = new AtomicLong(TIMEOUT_DURTION_STANDARD_DEFAULT);
    
    /** Признак высокой загруженности процессора. */
    protected volatile boolean processorIsHighLoaded;    
    
    protected volatile Logger logger = Globals.logger.get();  
    
    /** Обработчик статистических данных для рабочей части цикла.  */ 
    protected volatile IrcAvgMeter avgDurationOfWorkingPartOfCycle = new IrcAvgMeter(STATISTIC_DATA_LENGTH_DEFAULT);
    
    /** Обработчик статистических данных для холостой части цикла.  */
    protected volatile IrcAvgMeter avgDurationOfTimeout = new IrcAvgMeter(STATISTIC_DATA_LENGTH_DEFAULT);

    /** Обработчик статистических данных для вычисления планируемого таймеаута.  */
    protected volatile IrcAvgMeter avgPlannedDurationOfTimeout = new IrcAvgMeter(STATISTIC_DATA_LENGTH_DEFAULT);

    /** 
     * Управление выполнением/остановом основного цикла.
     * true - цикл выполняется, false - цикл приостановлен. 
     */ 
    public AtomicBoolean running = new AtomicBoolean(true);

    /** 
     * Управление выполнением/завершением основного цикла.
     * true - цикл завершается, false - цикл может выполнятся.
     */ 
    public AtomicBoolean down = new AtomicBoolean();
        
    /** Поток метода run этого объекта. */ 
    public AtomicReference<Thread> thread = new AtomicReference<Thread>();
    
    private volatile long timeOfLastMonitoring = System.currentTimeMillis();

    /** 
     * Инициализация процесса обработки ссобщений клиентов. 
     * @return true - инициализация успешно завершена, 
     * false - инициализация завершена с ошибками. 
     */
    public boolean processorStart() {
        thread.set(new Thread(this));        
        running.set(true);
        thread.get().start();
        Globals.logger.get().log(Level.INFO, getClass().getName()+ ": " + thread.get());
        
        try {
            Thread.sleep(Globals.sleepTO.get());
        } catch (InterruptedException e) {}

        return !(thread.get().getState() == Thread.State.NEW  || thread.get().getState() == Thread.State.TERMINATED);
    }

    /**
     * Реконфигурирование просесса
     * @return true - действия успешно выполнены.
     */
    public boolean processorReconfigure() {return true;}
    
    
    /**
     * Действия выполняемые перед остановкой процесса. просесса
     * @return true - действия успешно выполнены.
     */
    public boolean processorPredstop() {return true;}
    

    /** Завершение основного цикла.
     * @return true - действия успешно выполнены.
     */
    public boolean processorStop() {
        boolean result = true;
        if (thread.get() != null) {
            down.set(true);
            result = stopProcess(thread.get());
        }
        return result;
    }

    
    /** 
     * Останов потока. 
     * @param thread поток, который необходимо остановить.
     * @return true - действия успешно выполнены.
     */
    public boolean stopProcess(Thread thread) {
        
        try {
            Thread.sleep(Globals.sleepTO.get() * 2);
        } catch (InterruptedException e) {}
        
        if (thread.getState() != Thread.State.NEW
            && thread.getState() != Thread.State.RUNNABLE
            && thread.getState() != Thread.State.TERMINATED) {
            thread.interrupt();
        }
        
        try {
            Thread.sleep(Globals.sleepTO.get() * 2);
        } catch (InterruptedException e) {}
        
        return thread.getState() == Thread.State.TERMINATED;
    }



    
    /**
     * Метод, поддерживающий выполнение операций процессоров.
     *
     * <P> С помощью средней длительности основного цикла, 
     * запланированной длительности основного цикла, минимальной 
     * продолжительности таймаута и средней продолжительности 
     * таймаута определяется степень нагруженности этого программного 
     * процессора. При выполнении любого из следующих условий этот 
     * программный процессор признается сильно нагруженным:
     * <OL>
     *     <LI>Средняя длительность основного цикла составляе 70% от
     *  запланированной длительности основного цикла более.</LI>
     *  <LI>Средняя продолжительность таймаута на 5% больше 
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
     *         <LI>среднее время (ms) выполнения основного цикла 
     *         (без таймаута);</LI>
     *         <LI>средняя длительность (ms) таймаута;</LI>
     *         <LI>средняя планируемая длительность (ms) таймаутов.</LI>
     * </UL>
     */
    public void run() {        
                
        avgDurationOfTimeout.setValue(minimalDurationOfTimeout.get());

        avgPlannedDurationOfTimeout.setValue(minimalDurationOfTimeout.get());
        
        logger.log(Level.FINEST, "Running: " + this.getClass().getSimpleName());
        
        while (!down.get()) {

            while (!running.get() && !down.get()) {
                try {
                    Thread.sleep(durationOfTimeout.get());
                } catch (InterruptedException e) {}
            }
           
            if(down.get()) {
                break;
            }
            
            avgDurationOfWorkingPartOfCycle.intervalStart(System.currentTimeMillis());

            
            performProcessorOperation();
            
            avgDurationOfWorkingPartOfCycle.intervalEnd(System.currentTimeMillis());
            
            long actualAvgDurationOfTimeout = avgDurationOfTimeout.getAvgValue();
            avgDurationOfTimeout.intervalStart(System.currentTimeMillis());
            long actualAvgDurationOfWorkingPartOfCycle = avgDurationOfWorkingPartOfCycle.getAvgValue();

            
            long plannedDurationOfTimeout = Math.max(minimalDurationOfTimeout.get(), Math.min(
                plannedDurationOfCycle.get(),
                plannedDurationOfCycle.get() - actualAvgDurationOfWorkingPartOfCycle));
                
            avgPlannedDurationOfTimeout.setValue(plannedDurationOfTimeout);
            long actualAvgPlannedDurationOfTimeout = avgPlannedDurationOfTimeout.getAvgValue();
            
            checkLoadOfProcessor(actualAvgDurationOfTimeout, actualAvgDurationOfWorkingPartOfCycle,
                    plannedDurationOfTimeout, actualAvgPlannedDurationOfTimeout);

            sendStatisticsToMonitoringChannel(actualAvgDurationOfWorkingPartOfCycle, actualAvgDurationOfTimeout, 
                    actualAvgPlannedDurationOfTimeout);
            
            

            try {
                Thread.sleep(plannedDurationOfTimeout);
            } catch (InterruptedException e) {}
            
            avgDurationOfTimeout.intervalEnd(System.currentTimeMillis());
        }
        logger.log(Level.FINEST, "Ended: " + this.getClass().getSimpleName());
    }

    private void checkLoadOfProcessor(long actualAvgDurationOfTimeout, long actualAvgDurationOfWorkingPartOfCycle,
            long plannedDurationOfTimeout, long actualAvgPlannedDurationOfTimeout) {
        if (!processorIsHighLoaded && 
                ((actualAvgDurationOfWorkingPartOfCycle * 7 >= plannedDurationOfCycle.get() * 10) ||
                (actualAvgDurationOfTimeout / plannedDurationOfTimeout * 10 > 105))) {
            
            processorIsHighLoaded = true;
            
            logger.log(Level.INFO, "Set highLoad." + 
            " avgWorkingTime (ms):" + actualAvgDurationOfWorkingPartOfCycle +
            " plannedDuration (ms):" + plannedDurationOfCycle.get() +
            " avgActualTO (ms):" + actualAvgDurationOfTimeout +
            " avgWaitingTO (ms):" + actualAvgPlannedDurationOfTimeout);
            
            /** Индикация высокой загруженности. */
            addProcessorToHighLoadSet(this);
            
        } else if (processorIsHighLoaded && 
                (actualAvgDurationOfWorkingPartOfCycle * 2 <= plannedDurationOfCycle.get()) && 
                (actualAvgDurationOfTimeout <= plannedDurationOfTimeout)) {
            
            processorIsHighLoaded = false;
            
            logger.log(Level.INFO, "UnSet highLoad." + 
            " avgWorkingTime (ms):" + actualAvgDurationOfWorkingPartOfCycle +
            " plannedDuration (ms):" + plannedDurationOfCycle.get() +
            " avgActualTO (ms):" + actualAvgDurationOfTimeout +
            " avgWaitingTO (ms):" + actualAvgPlannedDurationOfTimeout);
            
            /** Сброс индикации высокой загруженности. */
            removeProcessorFromHighLoadSet();
        }
    }
    /** Индикация высокой загруженности. 
     * @parametr processor - процессор на котором наблюдается высокая нагрузка
     */
    public void addProcessorToHighLoadSet(AbstractIrcServerProcessor processor) {
        Globals.ircServerProcessorSet.get().add(processor);
    }
    
    /** Сброс индикации высокой загруженности. */
    public void removeProcessorFromHighLoadSet() {}

    protected void sendStatisticsToMonitoringChannel(long avgWorkingTime, long avgActualTO, long awto) {
        if ((System.currentTimeMillis() - timeOfLastMonitoring) >= Globals.monitoringPeriod.get()) {
            
            String monitoringString = this.getClass().getSimpleName() + 
                    " avgWorkingTime (ms):" + avgWorkingTime +
                    " avgActualTO (ms):" + avgActualTO +
                    " avgWaitingTO (ms):" + awto;
            
            monitoringString += getMonitoringstring();
            
            logger.log(Level.FINEST, monitoringString);
            
            if (Globals.monitorIrcChannel.get() != null ) {
                List<String> targetList = new ArrayList<String>();
                String channelname = Globals.monitorIrcChannel.get().getNickname();
                targetList.add(channelname);
                NoticeIrcCommand.create(Globals.db.get(), 
                        Globals.anonymousUser.get(), 
                        targetList,
                        monitoringString).run();
            }
            
            timeOfLastMonitoring = System.currentTimeMillis();
            
        }
    }

    
    public void performProcessorOperation() {}
    
    public String getMonitoringstring() {
         return "";
    }

    public long getTimeOfLastMonitoring() {
        return timeOfLastMonitoring;
    }
    
    
}
