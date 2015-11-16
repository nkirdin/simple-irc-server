package simpleircserver.processor;
/*
 * 
 * NetworkConnectionProcessor 
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
 * License Version 3 along with this program.  If not, see 
 * <http://www.gnu.org/licenses/>.
 *
 */

import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import simpleircserver.base.Globals;
import simpleircserver.connection.Connection;
import simpleircserver.connection.ConnectionState;
import simpleircserver.connection.NetworkConnection;
import simpleircserver.parser.Reply;
import simpleircserver.talker.IrcTalker;
import simpleircserver.talker.IrcTalkerState;

/**
 * Программный процессор, который обследует объекты, хранящие информацию о
 * сетевых соединениях. Он проверяет их состояние и состояние соответствующего
 * объекта класса {@link IrcTalker}. В случае обнаружения ошибочного состояния
 * или состояния завершения жизненного цикла, будут выполнены завершающие
 * действия для обследуемого объекта.
 *
 * @version 0.5 2012-02-13
 * @version 0.5.3 2015-11-05 Program units were moved from default package into
 *          packages with names. Unit tests were added.
 * @author Nikolay Kirdin
 */
public class NetworkConnectionProcessor extends AbstractIrcServerProcessor {

    private volatile long oldTotalReadCount;

    private volatile long oldTotalWriteCount;

    /** Конструктор по умолчанию. */
    public NetworkConnectionProcessor() {}

    /**
     * Метод, обследующий объекты класса {@link NetworkConnection}.
     * 
     * <P>
     * В основном цикле этого метода проверяется состояние объекта и
     * соответствующего объекта класса {@link IrcTalker}. В том случае, если
     * объект класса {@link NetworkConnection} должен завершить свой жизненый
     * цикл или объект класса {@link IrcTalker} находится в состоянии ошибки или
     * завершил свой жизненый цикл, то будут проведены завершающие действия для
     * обследуемого объекта. Состояние объекта определяется переменной
     * {@link Connection#connectionState}. Состояния объект класса
     * {@link IrcTalker} определяется переменной {@link IrcTalker#state}.
     *
     * <P>
     * Для проверки функционирования сетевого соединения, клиентам периодически
     * посылается сообщение IRC PING. Если ответ на это сообщение не будет
     * получен в заданный временной интервал, то состояние обследуемого объекта
     * будет признано ошибочным и будет переведено в состояние
     * {@link ConnectionState#BROKEN}. Минимальный период посылки сообщений
     * определяется переменной {@link Globals#pingSendingPeriod}.
     *
     * <P>
     * В том случае, если значение переменной {@link Connection#connectionState}
     * обследуемых объектов будет {@link ConnectionState#CLOSE}, то этот объект
     * будет удален из репозитария и его состояние будет изменено на состояние
     * {@link ConnectionState#CLOSED}. Если у этого объекта есть открытый сокет,
     * то этот сокет будет закрыт.
     *
     * <P>
     * Если будет обнаружено, что у обследуемого объекта нет соответствующего
     * объекта класса {@link IrcTalker} или у соответвующее ему объекта класса
     * {@link IrcTalker} находится в одном из следующих состояний:
     * {@link IrcTalkerState#CLOSED} или {@link IrcTalkerState#BROKEN}, то
     * состояние этого объекта будет изменено на состояние
     * {@link ConnectionState#BROKEN}.
     *
     * <P>
     * В основном цикле вычисляются среднее количество операций ввода,
     * вычисленное значение сохраняется в поле {@link Connection#avgInputPeriod}
     * обследуемого объекта.
     *
     * <P>
     * В поля {@link Connection#readCount}, {@link Connection#writeCount}
     * помещаются общее количество операций ввода и вывода для обследуемого
     * объекта. В поля {@link Connection#totalReadCount} и
     * {@link Connection#totalWriteCount} помещаются суммарное количество
     * операций ввода и операций вывода по всем сетевым соединениям.
     * 
     * <P>
     * Каждые {@link Globals#monitoringPeriod} (ms) в канал
     * {@link Globals#monitorIrcChannel} выводятся диагностические сообщения
     * содержащие следующую информацию:
     * <UL>
     * <LI>общее количество операций чтения;</LI>
     * <LI>среднее количество операций чтения в секунду;</LI>
     * <LI>общее количество операций записи;</LI>
     * <LI>осреднее количество операций записи в секунду.</LI>
     * </UL>
     */
    public void performProcessorOperation() {

        Iterator<Connection> connectionListIterator = Globals.db.get().getConnectionListIterator();

        while (connectionListIterator.hasNext()) {

            Connection conn = connectionListIterator.next();
            long currentTime = System.currentTimeMillis();
            IrcTalker itcTalker = conn.ircTalker.get();

            if (itcTalker == null || itcTalker.getState() == IrcTalkerState.BROKEN) {
                conn.setBroken();
            } else if (itcTalker.getState() == IrcTalkerState.CLOSED) {
                conn.close();
            }

            long avgInterval = conn.avgInputPeriodMeter.getAvgInterval(currentTime);

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

            ConnectionState connectionState = conn.getConnectionState();

            if (connectionState != ConnectionState.BROKEN && connectionState != ConnectionState.CLOSE
                    && (currentTime - conn.pingTime.get() >= Globals.pingSendingPeriod.get())) {
                if (conn.pongTime.get() < conn.pingTime.get()) {
                    String outString = "ERROR: " + "Ping timeout.";
                    OutputQueueProcessor.process(conn, outString);
                    logger.log(Level.FINER,
                            "Ping timeout:" + Math.abs(conn.pongTime.get() - conn.pingTime.get()) + " :" + conn);
                    conn.setBroken();
                } else {
                    conn.pingTime.set(currentTime);
                    logger.log(Level.FINER, "Ping send: " + conn);

                    String outString = "PING: " + Globals.thisIrcServer.get().getHostname();
                    OutputQueueProcessor.process(conn, outString);
                }
            }

            switch (connectionState) {
            case NEW:
            case INITIALIZED:
                break;
            case OPERATIONAL:
                break;
            case BROKEN:
                logger.log(Level.FINER, "Closing broken: " + conn);
            case CLOSE:
            case CLOSING:
                conn.delete();
                conn.setConnectionState(ConnectionState.CLOSED);
                conn.connectionStateTime.set(System.currentTimeMillis());
                Reply responseReply = Globals.db.get().unRegister(conn);
                logger.log(Level.FINER, "Unregistering and Deleting closed: " + conn + " " + responseReply);
                break;
            case CLOSED:
                break;
            default:
                String remark = "NetworkConnectionProcessor. Internal error. Unknown connection state: " + conn + " "
                        + connectionState;
                logger.log(Level.SEVERE, remark);
                // ** Останов сервера*/
                System.err.println(remark);
                down.set(true);
                Globals.serverDown.set(true);
            }
        }

    }

    /**
     * Выполнение завершающих действий при останове сервера. Для всех объектов
     * порожденных от {@link NetworkConnection} будут выполнены следующие
     * действия:
     * <UL>
     * <LI>состояние объекта будет установлено в состояние
     * {@link ConnectionState#CLOSED};</LI>
     * <LI>сокет объекта будет закрыт;</LI>
     * <LI>объект будет удален из репозитария.</LI>
     * </UL>
     */
    public void termination() {
        Logger logger = Globals.logger.get();
        logger.log(Level.FINEST, "NetworkConnectionProcessor: Start termination");
        Iterator<Connection> connectionListIterator = null;
        connectionListIterator = Globals.db.get().getConnectionListIterator();

        while (connectionListIterator.hasNext()) {
            Connection conn = connectionListIterator.next();
            conn.setConnectionState(ConnectionState.CLOSED);
            conn.connectionStateTime.set(System.currentTimeMillis());
            Globals.db.get().unRegister(conn);
            conn.delete();
            logger.log(Level.FINER, "Unregistering and Deleting: " + conn);
        }
        logger.log(Level.FINEST, "NetworkConnectionProcessor: Ended termination");
    }

    /**
     * Завершение процесса проверки состояния соединений клиентов сервера.
     * 
     * @return true - действия успешно выполнены.
     */
    @Override
    public boolean processorStop() {
        boolean result = super.processorStop();
        termination();
        return result;
    }

    /** Сброс индикации высокой загруженности. */
    @Override
    public void removeProcessorFromHighLoadSet() {
        for (IrcServerProcessor isp : Globals.ircServerProcessorSet.get()) {
            if (isp instanceof NetworkConnectionProcessor) {
                Globals.ircServerProcessorSet.get().remove(isp);
                break;
            }
        }
    }

    @Override
    public String getMonitoringstring() {
        return getAdditionalMonitoringString();
    }

    // getTimeOfLastMonitoring
    private String getAdditionalMonitoringString() {
        long totalReadCountRate = 0;
        long totalWriteCountRate = 0;
        long period = System.currentTimeMillis() - getTimeOfLastMonitoring();

        if (period > 0) {
            totalReadCountRate = (Connection.totalReadCount.get() - oldTotalReadCount) * 1000 / period;
            totalWriteCountRate = (Connection.totalWriteCount.get() - oldTotalWriteCount) * 1000 / period;
        } else {
            totalReadCountRate = 0;
            totalWriteCountRate = 0;
        }

        oldTotalReadCount = Connection.totalReadCount.get();
        oldTotalWriteCount = Connection.totalWriteCount.get();

        String result = " totalReadCount: " + Connection.totalReadCount.get() 
                + " totalReadCountRate: " + totalReadCountRate 
                + " totalWriteCount:" + Connection.totalWriteCount.get() 
                + " totalWriteCountRate: "    + totalWriteCountRate;        
        return result;
    }

}
