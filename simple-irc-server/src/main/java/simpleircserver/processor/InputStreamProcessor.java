package simpleircserver.processor;
/*
 * 
 * InputStreamProcessor 
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

import java.io.BufferedReader;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.Iterator;
import java.util.logging.Level;

import simpleircserver.base.Globals;
import simpleircserver.connection.Connection;
import simpleircserver.connection.ConnectionState;
import simpleircserver.connection.NetworkConnection;
import simpleircserver.parser.IrcIncomingMessage;
import simpleircserver.parser.Reply;
import simpleircserver.talker.IrcTalker;
import simpleircserver.talker.IrcTalkerState;

/**
 * InputStreamProcessor - программный процессор, который обслуживает
 * буферированные потоки ввода. В основном цикле просматриваются потоки и из них
 * считываются сообщения клиентов. Затем эти сообщения помещаются во входные
 * очереди сетевого соединения.
 *
 * @version 0.5 2012-02-13
 * @version 0.5.3 2015-11-05 Program units were moved from default package into
 *          packages with names. Unit tests were added.
 * @author Nikolay Kirdin
 */
public class InputStreamProcessor extends AbstractIrcServerProcessor {

    /** Конструктор по умолчанию. */
    public InputStreamProcessor() {
    }

    /**
     * Метод performProcessorOperation() - это метод, который просматриваются входные потоки сетевых
     * соединений.
     *
     * <P>
     * В основном цикле этого метода производится просмотр потоков ввода сетевых
     * соединений. Если в буферированом потоке будет находится строка текста, то
     * эта строка будет извлечена из буфера и помещена во входные очереди
     * сетевого соединения. Время блокировки операций чтения ограничено временем
     * блокировки соответствующего сокета.
     *
     * <P>
     * Если во время выполнения основного цикла, будут обнаружены какие-либо
     * ошибки подсистемы ввода, то состояние соответствующего объекта класса
     * {@link NetworkConnection} будет переведено в состояние
     * {@link ConnectionState#BROKEN}.
     *
     * <P>
     * Чтения из потока ввода производится только в том случае, если состояние
     * соответствующего объекта {@link NetworkConnection} является
     * {@link IrcTalkerState#OPERATIONAL}.
     *
     * <P>
     * Чтения из потока ввода производится только в том случае, если средние
     * значения периода между входящими сообщениями больше чем в
     * {@link Globals#minAvgReadPeriod}.
     *
     * <P>
     * После успешной операции ввода значение переменной
     * {@link NetworkConnection#readCountDelta} для этого объекта увеличивается
     * на 1.
     * 
     */
    @Override
    public void performProcessorOperation() {

        Iterator<Connection> connectionListIterator = Globals.db.get().getConnectionListIterator();
        
        while (connectionListIterator.hasNext()) {

            Connection connection = connectionListIterator.next();

            if (connection.getConnectionState() != ConnectionState.OPERATIONAL) {
                continue;
            }
            if (connection.inputQueue.get() != null) {
                continue;
            }

            BufferedReader br = connection.br.get();
            if (br == null) {
                connection.setBroken();
                continue;
            }

            if (connection.avgInputPeriod.get() < connection.minAvgInputPeriod.get()) {
                continue;
            }

            try {
                String inputString;
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

                IrcTalker ircTalker = connection.ircTalker.get();
                ircTalker.setLastMessageTime(currentTime);
                connection.avgInputPeriodMeter.setValue(currentTime);

                boolean result = connection.offerToInputQueue(new IrcIncomingMessage(inputString, ircTalker));

                if (!result) {
                    String remark = Reply.makeText(Reply.ERR_FILEERROR, ircTalker.getNickname(),
                            "offer to input queue ", connection.toString());
                    OutputQueueProcessor.process(connection, remark);
                    connection.setBroken();
                }

            } catch (SocketTimeoutException e) {
            } catch (IOException e) {
                connection.setBroken();
                logger.log(Level.INFO, "Connection:" + connection + " " + e);
            }
        }

    }

    /** Сброс индикации высокой загруженности. */
    @Override
    public void removeProcessorFromHighLoadSet() {
        for (IrcServerProcessor isp : Globals.ircServerProcessorSet.get()) {
            if (isp instanceof InputStreamProcessor) {
                Globals.ircServerProcessorSet.get().remove(isp);
                break;
            }
        }
    }

}
