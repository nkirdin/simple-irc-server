/*
 * 
 * InputQueueProcessor 
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
 * InputQueueProcessor - программный процессор, который просматривает 
 * входные очереди сетевого соединения. В основном цикле метода 
 * {@link #run} просматриваются входные очереди и из них извлекаются 
 * сообщения клиентов. Затем эти сообщения передаются на исполнение 
 * интерпретатору команд IRC. 
 *
 * @version 0.5 2012-02-13
 * @author  Nikolay Kirdin
 */
public class InputQueueProcessor implements Runnable, 
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
	public AtomicLong limitingTO = new AtomicLong(20);

	/** Стандартная длительность таймаутов. */
	public AtomicLong sleepTO = new AtomicLong(100);

	/** 
	 * Планируемая длительность (ms) выполнения основного цикла 
	 * (без таймаута). 
	 */
	public AtomicLong plannedDuration = new AtomicLong(80);

	/** Конструктор по умолчанию. */
	public InputQueueProcessor() {}

	/**
	 * Метод, просматривающий входные очереди сетевых соединений.
	 *
	 * <P>В основном цикле этого метода производится просмотр входных 
	 * очередей сетевых соединений. Если во входной очереди будет 
	 * находится сообщение клиента, то это сообщение будет извлечено из 
	 * очереди и передано интерпретатору команд IRC. 
	 *
	 * <P> С помощью средней длительности основного цикла, 
	 * запланированной длительности основного цикла, минимальной 
	 * продолжительности таймаута и средней продолжительности 
	 * таймаута определяется степень нагруженности этого программного 
	 * процессора. При выполнении любого из следующих условий этот 
	 * программный процессор признается сильно нагруженным:
	 * <OL>
	 * 	<LI>Средняя длительность основного цикла составляет 70% от
	 *  запланированной длительности основного цикла более.</LI>
	 *  <LI>Средняя продолжительность таймаута на 10% больше 
	 *  запланированной продолжительности таймаута.</LI> 
	 * </OL>
	 * <P>Режим высокой нагруженности сбрасывается при выполнении всех 
	 * следующих условий:
	 * <OL>
	 * 	<LI>Средняя длительность основного цикла на 50%  меньше
	 *  запланированной длительности основного цикла более.</LI>
	 *  <LI>Средняя продолжительность таймаута меньше или равна 
	 *  запланированной продолжительности таймаута.</LI> 
	 * </OL> 
	 * 
	 * <P>Интерпретация команд производится с помощью метода 
	 * {@link IrcCommandParser#ircParse}. 
	 *
	 * <P>Если во время выполнения команды будет вызвано исключение, то 
	 * этот факт будет занесет в журнал, выполнение будет продолжено.
	 * 
	 * <P> Каждые {@link Globals#monitoringPeriod} (ms) в канал 
	 * {@link Globals#monitorIrcChannel} выводятся диагностические 
	 * сообщения содержащие следующую информацию:
	 * <UL>
	 * 		<LI>длину списка соединений;</LI>
	 * 		<LI>среднее время (ms) выполнения основного цикла 
	 * 		(без таймаута);</LI>
	 * 		<LI>среднее время (ms) выполнения команд;</LI>
	 * 		<LI>средняя длительность (ms) таймаута;</LI>
	 * 		<LI>средняя планируемая длительность (ms) таймаутов.</LI>
	 * </UL>
	 */
	public void run() {
		Logger logger = Globals.logger.get();

		Iterator<Connection> connectionListIterator = null;
		int connectionListSize = 0;
		IrcIncomingMessage ircIncomingMessage = null;
		IrcCommandParser icp = new IrcCommandParser();

		int procTimeLength = 100;
		long avgWorkingTime = 0;
		long waitingTO = limitingTO.get();
		IrcAvgMeter avgWaitingTO = new IrcAvgMeter(procTimeLength);
		avgWaitingTO.setValue(waitingTO);
		long startMonitorTime = System.currentTimeMillis();

		IrcAvgMeter avgWork = new IrcAvgMeter(procTimeLength);
		IrcAvgMeter avgChExe = new IrcAvgMeter(procTimeLength);

		long avgActualTO = 0;
		IrcAvgMeter avgTO = new IrcAvgMeter(procTimeLength);
		avgTO.setValue(limitingTO.get());

		long awto = 0;

		boolean highLoad = false;

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

				ircIncomingMessage = 
						connection.inputQueue.getAndSet(null);

				if (ircIncomingMessage == null) {
					continue;
				}

				avgChExe.intervalStart(System.nanoTime());    
				try {
					icp.setIncomingMessage(ircIncomingMessage);
					icp.ircParse();
				} catch (Throwable e) {

					IrcServer ircServer = null;
					IrcTalker ircTalker = connection.ircTalker.get();
					long id = ircIncomingMessage.id;
					String message = ircIncomingMessage.message;

					if (ircTalker instanceof User) {
						ircServer = 
								((User) ircTalker).getIrcServer();
					} else if (ircTalker instanceof Service) {
						ircServer = 
								((Service) ircTalker).getIrcServer();
					} 

					logger.log(Level.SEVERE, 
							"Exception. ircCommandReport id: " + id 
							+ " Connection: " + connection + 
							" Requestor: " + ircTalker +
							" " + ircTalker.getHostname()
							+ " " + ircTalker.getNetworkId() 
							+ " " + ircServer );
					logger.log(Level.SEVERE, 
							"Exception. ircCommandReport id: " + id 
							+ " " + e + " Message: " + 
							e.getMessage());
					logger.log(Level.SEVERE, 
							"Exception. ircCommandReport id: " + id 
							+ " StackTrace: " +
							Arrays.toString(e.getStackTrace()));
					logger.log(Level.SEVERE, 
							"Exception. ircCommandReport id: " + id 
							+ " Parsing String: " + message);
					ircTalker.send(
							IrcCommandBase.errFileError(
							ircTalker, String.valueOf(id), message));
				}
				avgChExe.intervalEnd(System.nanoTime());
			}

			avgWork.intervalEnd(System.currentTimeMillis());
			avgWorkingTime = avgWork.getAvgValue();

			avgActualTO = avgTO.getAvgValue();
			avgTO.intervalStart(System.currentTimeMillis());

			waitingTO = Math.max(limitingTO.get(), Math.min(
					plannedDuration.get(),
					plannedDuration.get() - avgWorkingTime));
			
			avgWaitingTO.setValue(waitingTO);
			awto = avgWaitingTO.getAvgValue();

			if (!highLoad && (
					(avgWorkingTime * 7 >= plannedDuration.get() * 10) ||
					(avgActualTO * 10 > waitingTO * 11))) {
				
				highLoad = true;
				
				/** Индикация высокой загруженности. */
				Globals.ircServerProcessorSet.get().add(this);
				
				logger.log(Level.INFO, "Set highLoad." + 
						" avgWorkingTime (ms):" + avgWorkingTime +
						" plannedDuration (ms):" + plannedDuration.get()
						+ " avgActualTO (ms):" + avgActualTO +
						" avgWaitingTO (ms):" + awto);
				
				
				
			} else if (highLoad && 
					(avgWorkingTime * 2 <= plannedDuration.get()) &&
					(avgActualTO <= waitingTO)) {
				
				highLoad = false;
				
				logger.log(Level.INFO, "UnSet highLoad." + 
						" avgWorkingTime (ms):" + avgWorkingTime +
						" plannedDuration (ms):" + plannedDuration.get() 
						+ " avgActualTO (ms):" + avgActualTO +
						" avgWaitingTO (ms):" + awto);
				
				/** Сброс индикации высокой загруженности. */
				for (IrcServerProcessor isp: 
					Globals.ircServerProcessorSet.get()) {
					if (isp instanceof InputQueueProcessor) {
						Globals.ircServerProcessorSet.get().remove(isp);
						break;
					}
				}
			}

			if ((System.currentTimeMillis() - startMonitorTime) >=
					Globals.monitoringPeriod.get()) {

				String monitoringString = "InputQueueProcessor:" +  
						" size:" + connectionListSize +
						" avgWorkingTime (ms):" + avgWorkingTime +
						" avgChExe (ns):" + avgChExe.getAvgValue() +
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
}
