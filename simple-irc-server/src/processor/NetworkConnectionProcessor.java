/*
 * 
 * NetworkConnectionProcessor 
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
 * о сетевых соединениях. Он проверяет их состояние и состояние 
 * соответствующего объекта класса {@link IrcTalker}. В случае 
 * обнаружения ошибочного состояния или состояния завершения жизненного 
 * цикла, будут выполнены завершающие действия для обследуемого объекта. 
 *
 * @version 0.5 2012-02-13
 * @author  Nikolay Kirdin
 */
public class NetworkConnectionProcessor implements Runnable, 
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
    public AtomicReference<Thread> thread = new AtomicReference<Thread>();
    
    /** Минимальная длительность таймаутов. */
    public AtomicLong limitingTO = new AtomicLong(100);
    
    /** Стандартная длительность таймаутов. */
    public AtomicLong sleepTO = new AtomicLong(100);
    
    /** 
     * Примерная максимальная длительность выполнения цикла, она 
     * включает в себя таймаут цикла.
     */
    public AtomicLong plannedDuration = new AtomicLong(900);
    
	/** Признак высокой загруженности процессора. */
	private boolean highLoad;    
    
	/** Конструктор по умолчанию. */
    public NetworkConnectionProcessor() {}

    /**
     * Метод, обследующий объекты класса {@link NetworkConnection}.    
     * 
     * <P>В основном цикле этого метода проверяется состояние объекта и 
     * соответствующего объекта класса {@link IrcTalker}. В том случае, 
     * если объект класса {@link NetworkConnection} должен завершить 
     * свой жизненый цикл или объект класса {@link IrcTalker} находится 
     * в состоянии ошибки или завершил свой жизненый цикл, то будут 
     * проведены завершающие действия для обследуемого объекта. 
     * Состояние объекта определяется переменной 
     * {@link Connection#connectionState}. Состояния объект класса 
     * {@link IrcTalker} определяется переменной {@link IrcTalker#state}. 
     *
     * <P>Для проверки функционирования сетевого соединения, клиентам 
     * периодически посылается сообщение IRC PING. Если ответ на это 
     * сообщение не будет получен в заданный временной интервал, то 
     * состояние обследуемого объекта будет признано ошибочным и будет 
     * переведено в состояние {@link ConnectionState#BROKEN}. 
     * Минимальный период посылки сообщений определяется переменной
     * {@link Globals#pingSendingPeriod}.
     *
     * <P>В том случае, если значение переменной 
     * {@link Connection#connectionState} обследуемых объектов будет 
     * {@link ConnectionState#CLOSE}, то этот объект будет удален из 
     * репозитария и его состояние будет изменено на состояние 
     * {@link ConnectionState#CLOSED}. Если у этого объекта есть 
     * открытый сокет, то этот сокет будет закрыт.  
     *
     * <P>Если будет обнаружено, что у обследуемого объекта нет 
     * соответствующего объекта класса {@link IrcTalker} или у 
     * соответвующее ему объекта класса {@link IrcTalker} находится в 
     * одном из следующих состояний: {@link IrcTalkerState#CLOSED} или 
     * {@link IrcTalkerState#BROKEN}, то состояние этого объекта будет 
     * изменено на состояние {@link ConnectionState#BROKEN}.
     *
     * <P>В основном цикле вычисляются среднее количество операций ввода, 
     * вычисленное значение сохраняется в поле 
     * {@link Connection#avgInputPeriod} обследуемого объекта. 
     *
     * <P>В поля {@link Connection#readCount}, 
     * {@link Connection#writeCount} помещаются общее количество 
     * операций ввода и вывода для обследуемого объекта. В поля 
     * {@link Connection#totalReadCount} и 
     * {@link Connection#totalWriteCount} помещаются суммарное 
     * количество операций ввода и операций вывода по всем сетевым 
     * соединениям.
     * 
	 * <P> С помощью средней длительности основного цикла, 
	 * запланированной длительности основного цикла, минимальной 
	 * продолжительности таймаута и средней продолжительности 
	 * таймаута определяется степень нагруженности этого программного 
	 * процессора. При выполнении любого из следующих условий этот 
	 * программный процессор признается сильно нагруженным:
	 * <OL>
	 * 	<LI>Средняя длительность основного цикла составляе 70% от
	 *  запланированной длительности основного цикла более.</LI>
	 *  <LI>Средняя продолжительность таймаута на 10% больше 
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
    	logger.log(Level.FINEST, "Running");

    	Iterator<Connection> connectionListIterator = null;
    	int procTimeLength = 100;
    	long avgWorkingTime = 0;
    	long waitingTO = limitingTO.get();
    	long startMonitorTime = System.currentTimeMillis();

    	IrcAvgMeter avgWork = new IrcAvgMeter(procTimeLength);

    	long avgActualTO = 0;
    	IrcAvgMeter avgTO = new IrcAvgMeter(procTimeLength);
    	avgTO.setValue(limitingTO.get());

    	int connectionListSize = 0;

    	long oldTotalReadCount = 0;
    	long oldTotalWriteCount = 0;
    	long currentTime = System.currentTimeMillis();

    	IrcAvgMeter avgWaitingTO = new IrcAvgMeter(procTimeLength);
    	avgWaitingTO.setValue(waitingTO);        

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
    			Connection conn = connectionListIterator.next();
    			currentTime = System.currentTimeMillis();
    			IrcTalker itcTalker = conn.ircTalker.get();

    			if (itcTalker == null || itcTalker.getState() == 
    					IrcTalkerState.BROKEN) {
    				conn.setBroken();
    			} else if (itcTalker.getState() == 
    					IrcTalkerState.CLOSED) {
    				conn.close();
    			}

    			long avgInterval = 
    					conn.avgInputPeriodMeter.getAvgInterval(currentTime);

    			conn.avgInputPeriod.set(avgInterval);
    			
    			if (conn.readCountDelta.get() != 0) {
    				long readCnt = conn.readCountDelta.getAndSet(0);
    				conn.readCount.getAndAdd(readCnt);
    				Connection.totalReadCount.getAndAdd(readCnt);
    			}

    			if (conn.writeCountDelta.get() != 0) {
    				long writeCnt = conn.writeCountDelta.getAndSet(0);
    				conn.writeCount.getAndAdd(writeCnt);
    				Connection.totalWriteCount.getAndAdd(writeCnt);
    			}

    			ConnectionState connectionState = 
    					conn.getConnectionState();
    			
    			if (connectionState != ConnectionState.BROKEN
    					&& connectionState != ConnectionState.CLOSE
    					&& (currentTime - conn.pingTime.get()
    							>= Globals.pingSendingPeriod.get())) {
    				if (conn.pongTime.get() < conn.pingTime.get()) {
    					String outString = "ERROR" + " " + ":" +
    							"Ping timeout.";
    					OutputQueueProcessor.process(
    							conn,
    							outString);
    					logger.log(Level.FINER, 
    							"Ping timeout:" +
    							Math.abs(conn.pongTime.get() -
    							conn.pingTime.get()) + " :"
    							+ conn);
    					conn.setBroken();
    				} else {
    					conn.pingTime.set(currentTime);
    					logger.log(Level.FINER, "Ping send:" + conn);

    					String outString = "PING" + " " + ":" +
    							Globals.thisIrcServer.get(
    									).getHostname();
    					OutputQueueProcessor.process(
    							conn,
    							outString);
    				}
    			}

    			switch (connectionState) {
    			case NEW:
    			case INITIALIZED:
    				break;
    			case OPERATIONAL:
    				break;
    			case BROKEN:
    				logger.log(Level.FINER, 
    						"Closing broken:" + conn);
    			case CLOSE:
    			case CLOSING:
    				conn.delete();
    				conn.setConnectionState(ConnectionState.CLOSED);  
    				conn.connectionStateTime.set(
    						System.currentTimeMillis());
    				Response.Reply responseReply =
    						Globals.db.get().unRegister(conn);
    				logger.log(Level.FINER, 
    						"Unregistering and Deleting closed: " +
    								conn + " " + responseReply);  
    				break;
    			case CLOSED:
    				break;
    			default:
    				String remark = "NetworkConnectionProcessor." +
    						" Internal error." +
    						" Unknown connection state: " +
    						conn + " " + connectionState;
    				logger.log(Level.SEVERE, remark);
    				throw new Error(remark);
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
    		if (!highLoad && ((avgWorkingTime * 7 >= 
    				plannedDuration.get() * 10) ||
    				(avgActualTO * 10 > waitingTO * 11))) {
    			highLoad = true;
    			logger.log(Level.INFO, "Set highLoad." + 
    					" avgWorkingTime (ms):" + avgWorkingTime +
    					" plannedDuration (ms):" + plannedDuration.get() +
    					" avgActualTO (ms):" + avgActualTO +
    					" avgWaitingTO (ms):" + awto
    					);
    			Globals.ircServerProcessorSet.get().add(
    					NetworkConnectionProcessor.this);
    		} else if (highLoad && (avgWorkingTime * 2 <= 
    				plannedDuration.get()) &&
    				(avgActualTO <= waitingTO)) {
    			highLoad = false;
    			logger.log(Level.INFO, "UnSet highLoad." + 
    					" avgWorkingTime (ms):" + avgWorkingTime +
    					" plannedDuration (ms):" + plannedDuration.get() +
    					" avgActualTO (ms):" + avgActualTO +
    					" avgWaitingTO (ms):" + awto
    					);
    			for (IrcServerProcessor isp: 
    				Globals.ircServerProcessorSet.get()) {
    				if (isp instanceof NetworkConnectionProcessor) {
    					Globals.ircServerProcessorSet.get().remove(isp);
    					break;
    				}
    			}
    		}

    		currentTime = System.currentTimeMillis();
    		if ((currentTime - startMonitorTime) >= 
    				Globals.monitoringPeriod.get()) {
    			String monitoringString = "NetworkConnectionProcessor:" 
    					+ " size:" + connectionListSize +
    					" avgWorkingTime (ms):" + avgWorkingTime +
    					" avgActualTO (ms):" + avgActualTO +
    					" avgWaitingTO (ms):" + awto;

    			long totalReadCountRate = 0;
    			long totalWriteCountRate = 0;
    			long period = currentTime - startMonitorTime;
    			
    			if (period > 0) {
    				totalReadCountRate = (
    						Connection.totalReadCount.get() - 
    						oldTotalReadCount) * 1000 / period;
    				totalWriteCountRate = 
    						(Connection.totalWriteCount.get() - 
        					oldTotalWriteCount) * 1000 / period;
    			} else {
        			totalReadCountRate = 0;
        			totalWriteCountRate = 0;
    			}

    			oldTotalReadCount = Connection.totalReadCount.get();
    			oldTotalWriteCount = Connection.totalWriteCount.get();                

    			String monitoringString2 = "NetworkConnectionProcessor:" 
    					+ " totalReadCount:" + 
    					Connection.totalReadCount.get() +
    					" totalReadCountRate:" + totalReadCountRate +
    					" totalWriteCount:" + 
    					Connection.totalWriteCount.get() +
    					" totalWriteCountRate:" + totalWriteCountRate;
    			logger.log(Level.FINEST, monitoringString);  
    			logger.log(Level.FINEST, monitoringString2);

    			if (Globals.monitorIrcChannel.get() != null ) {
    				List<String> targetList = new ArrayList<String>();
    				String channelname = 
    						Globals.monitorIrcChannel.get().getNickname();
    				targetList.add(channelname);
    				NoticeIrcCommand.create(Globals.db.get(), 
    						Globals.anonymousUser.get(), 
    						targetList,
    						monitoringString).run();
    				NoticeIrcCommand.create(Globals.db.get(), 
    						Globals.anonymousUser.get(), 
    						targetList,
    						monitoringString2).run();
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
     * объектов порожденных от {@link NetworkConnection} будут выполнены
     * следующие действия:
     * <UL>
     *      <LI> состояние объекта будет установлено в состояние 
     *      {@link ConnectionState#CLOSED};</LI>
     *      <LI> сокет объекта будет закрыт;</LI>
     *      <LI> объект будет удален из репозитария.</LI>
     * </UL>
     */
    public void termination() {
    	Logger logger = Globals.logger.get();
        logger.log(Level.FINEST, "Start");
        Iterator<Connection> connectionListIterator = null;
        connectionListIterator =
        		Globals.db.get().getConnectionListIterator();
        
        while (connectionListIterator.hasNext()) {
            Connection conn = connectionListIterator.next();
            conn.setConnectionState(ConnectionState.CLOSED);
            conn.connectionStateTime.set(System.currentTimeMillis());
            Globals.db.get().unRegister(conn);
            conn.delete();
            logger.log(Level.FINER, 
            		"Unregistering and Deleting:" + conn);
        }
        logger.log(Level.FINEST, "Ended");
    }
}
