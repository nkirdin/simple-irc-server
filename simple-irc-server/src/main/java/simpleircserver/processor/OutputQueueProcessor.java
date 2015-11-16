package simpleircserver.processor;
/*
 * 
 * OutputQueueProcessor 
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
 */

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Iterator;

import simpleircserver.base.Globals;
import simpleircserver.connection.Connection;
import simpleircserver.connection.ConnectionState;

/**
 * Программный процессор, просматривающий выходные очереди сетевого 
 * соединения. В основном цикле просматриваются выходные очереди и из них 
 * извлекаются результаты исполнения интерпретатора команд IRC. Эти 
 * результаты передаются клиенту с помощью буферированных потоков вывода. 
 *
 * @version 0.5 2012-02-13
 * @version 0.5.3 2015-11-05 Program units were moved from default package into packages with names. Unit tests were added.
 * @author  Nikolay Kirdin
 */
public class OutputQueueProcessor extends AbstractIrcServerProcessor {

    /** Конструктор. */
    public OutputQueueProcessor() {} 
    
    /**
     * Выполнение основной функции процессора.
     * @return connectionListSize - количество обработанных элементов очереди
     */
    @Override
    public void performProcessorOperation() {

        Iterator<Connection> connectionListIterator = Globals.db.get().getConnectionListIterator();
        
        while (connectionListIterator.hasNext()) {

            Connection connection = connectionListIterator.next();
            
            if (connection.getConnectionState() != ConnectionState.OPERATIONAL) {
                continue;
            }
            
            try {
                int counter = 0;
                BufferedWriter bw = connection.bw.get();                            
                if (bw == null) { 
                    throw new IOException("Output stream broken."); 
                }
                while (!connection.getOutputQueue().isEmpty() 
                        && counter++ < connection.getMaxOutputQueueSize()) {  
                    String outputString = connection.getOutputQueue().poll().getReport();
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
        
    }

    /** Сброс индикации высокой загруженности. */
    @Override
    public void removeProcessorFromHighLoadSet() {
        for (IrcServerProcessor isp: Globals.ircServerProcessorSet.get()) {
            if (isp instanceof OutputQueueProcessor) {
                Globals.ircServerProcessorSet.get().remove(isp);
                break;
            }
        }
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
    
    /**
     * Действия выполняемые перед остановкой процесса. просесса
     * @return true - действия успешно выполнены.
     */
    @Override
    public boolean processorPredstop() {
        /* Уменьшие периода опроса выходных очередей */
        plannedDurationOfCycle.set(1);
        durationOfTimeout.set(1);
        minimalDurationOfTimeout.set(1);
        return true;
    }
    
}
