/*
 * 
 * OutputQueueProcessor 
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
 */

import java.util.*; 
import java.util.logging.*;
import java.util.concurrent.atomic.*;
import java.io.*;

/**
 * Программный процессор, просматривающий выходные очереди сетевого 
 * соединения. В основном цикле просматриваются выходные очереди и из них 
 * извлекаются результаты исполнения интерпретатора команд IRC. Эти 
 * результаты передаются клиенту с помощью буферированных потоков вывода. 
 *
 * @version 0.5 2012-02-13
 * @author  Nikolay Kirdin
 */
public class OutputQueueProcessor implements Runnable, 
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
	public AtomicReference<Thread>  thread = 
	        new AtomicReference<Thread>();
    
    /** Стандартная длительность таймаутов. */
    public AtomicLong sleepTO = new AtomicLong(100);

    /** Минимальная длительность таймаута основного цикла. */
    public AtomicLong limitingTO = new AtomicLong(10);

    /** 
     * Планируемая длительность одного цикла. Определяет нижнюю границу
     * максимальной длительности выполнения цикла, она включает в себя 
     * таймаут цикла.
     */
    public AtomicLong plannedDuration = new AtomicLong(90);
	
	/** Признак высокой загруженности процессора. */
	private boolean highLoad;	
	
    /** Конструктор. */
	public OutputQueueProcessor() {} 
	
    /**
     * Метод, просматривающий выходные очереди сетевых соединений.
     *
     * <P>В основном цикле этого метода производится просмотр выходных 
     * очередей сетевых соединений. Если в выходной очереди будет 
     * находится сообщение, то это сообщение будет извлечено из очереди 
     * и передано клиенту. 
	 * <P> С помощью средней длительности основного цикла, 
	 * запланированной длительности основного цикла, минимальной 
	 * продолжительности таймаута и средней продолжительности 
	 * таймаута определяется степень нагруженности этого программного 
	 * процессора. При выполнении любого из следующих условий этот 
	 * программный процессор признается сильно нагруженным:
	 * <OL>
	 * 	<LI>Средняя длительность основного цикла составляе 70% от
	 *  запланированной длительности основного цикла более.</LI>
	 *  <LI>Средняя продолжительность таймаута на 5% больше 
	 *  вычисленной продолжительности таймаута.</LI> 
	 * </OL>
	 * Режим высокой нагруженности сбрасывается при выполнении всех 
	 * следующих условий:
	 * <OL>
	 * 	<LI>Средняя длительность основного цикла на 50%  меньше
	 *  запланированной длительности основного цикла более.</LI>
	 *  <LI>Средняя продолжительность таймаута меньше или равна 
	 *  вычисленной продолжительности таймаута.</LI> 
	 * </OL> 
	 * <P> Каждые {@link Globals#monitoringPeriod} (ms) в канал 
	 * {@link Globals#monitorIrcChannel} выводятся диагностические 
	 * сообщения содержащие следующую информацию:
	 * <UL>
	 * 		<LI>длину списка соединений IRC;</LI>
	 * 		<LI>среднее время (ms) выполнения основного цикла 
	 * 		(без таймаута);</LI>
	 * 		<LI>средняя длительность (ms) таймаута;</LI>
	 * 		<LI>средняя планируемая длительность (ms) таймаутов.</LI>
	 * </UL>
     */
    public void run() {
    	
    	Logger logger = Globals.logger.get();    	

    	Iterator<Connection> connectionListIterator = null;
        int connectionListSize = 0;
        BufferedWriter bw = null;
        
        String outputString = null;
        
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
            
            if(down.get()) {
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
                
                try {
                    int counter = 0;
                    bw = connection.bw.get();                            
                    if (bw == null) { 
                        throw new IOException("Output stream broken."); 
                    }
                    while (!connection.getOutputQueue().isEmpty() 
                            && counter++ < 
                            connection.getMaxOutputQueueSize()) {  
                        outputString = 
                        		connection.getOutputQueue(
                        				).poll().getReport();
                        synchronized (bw) {
                            bw.write(outputString + "\r\n");
                        }
                        connection.writeCountDelta.getAndIncrement();
                    }
                    synchronized (bw) {
                        bw.flush();
                    }
                } catch (IOException e) {
                   connection.setBroken();
                   break;
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
            		(avgActualTO / waitingTO * 10 > 105))) {
            	
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
            		if (isp instanceof OutputQueueProcessor) {
            			Globals.ircServerProcessorSet.get().remove(isp);
            			break;
            		}
            	}
            }

            if ((System.currentTimeMillis() - startMonitorTime) >= 
            		Globals.monitoringPeriod.get()) {
                String monitoringString = "OutputQueueProcessor:" + 
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
        }
        logger.log(Level.FINEST, "Ended");
    }

    /**
     * Метод используется для записи в буферированный выходной поток
     * {@link Connection#bw} сообщения, предназначенного для клиента. В
     * случае возникновения ошибки вывода, состояние объекта будет
     * переведено в состояние {@link ConnectionState#BROKEN}.
     * @param connection соединение.
     * @param outputString сообщение.
    */
    public static void process(Connection connection,
        String outputString) {
        BufferedWriter bw = null;
        try {
            bw = connection.bw.get();
            if (bw == null) {
                throw new IOException("Output stream broken");
            }
            synchronized (bw) {
                connection.bw.get().write(outputString + "\r\n");
                connection.bw.get().flush();
            }
            connection.writeCountDelta.getAndIncrement();
        } catch (IOException e) {
        	connection.setBroken();
        }
    }
}
