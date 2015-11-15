package simpleircserver.connection;
/*
 * 
 * NullConnection 
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

import java.util.concurrent.*;

import simpleircserver.parser.IrcCommandReport;
import simpleircserver.parser.IrcIncomingMessage;

/**
 * NullConnection - класс, который служит для создания 
 * служебного псевдосоединения. Помещение объектов в очередь соединения 
 * всегда подтверждается. При чтении из очереди возвращается 
 * пустой объект {@link IrcCommandReport}. Длина очереди равна 0.
 *
 * @version 0.5 2012-02-27
 * @version 0.5.3 2015-11-05 Program units were moved from default package into packages with names. Unit tests were added.
 * @author  Nikolay Kirdin
 */
public class NullConnection extends Connection {
    
    /**
     * Метод используется для помещения сообщения во  входную очередь 
     * клиента IRC.
     * @return true признак успеха выполнения метода,
     */
    public boolean offerToInputQueue(IrcCommandReport ircCommandReport) {
        return true;
    }
    /**
     * Метод используется для очистки входной очереди клиента IRC.
     */
    public void dropInputQueue() {
    }

    /**
     * Метод используется для получения доступа к входной очереди 
     * клиента IRC.
     * @return BlockingQueue< IrcCommandReport > входная очередь 
     * клиента IRC.
     */
    public BlockingQueue<IrcIncomingMessage> getInputQueue() {
        return new ArrayBlockingQueue<IrcIncomingMessage>(1);
    }

    /**
     * Метод используется для получения количества элементов во входной 
     * очереди.
     * @param connection источник сообщения.
     * @return LinkedList< IrcCommandReport > - входная очередь 
     * клиента IRC.
     */
    public int getInputQueueSize(Connection connection) {
        return 0;
    }
    
    /**
     * Метод используется для помещения сообщения в  выходную очередь 
     * клиента IRC.
     * @return true признак успеха выполнения метода
     */
    public boolean offerToOutputQueue(IrcCommandReport ircCommandReport) {
        return true;
    }
    /**
     * Метод используется для очистки выходной очереди клиента IRC.
     */
    public void dropOutputQueue() {
    }

    /**
     * Метод используется для получения доступа к выходной очереди 
     * клиента IRC.
     * @return LinkedList< IrcCommandReport > выходная очередь 
     * клиента IRC.
     */
    public BlockingQueue<IrcCommandReport> getOutputQueue() {
        return new ArrayBlockingQueue<IrcCommandReport>(1);
    }

    /**
     * Метод используется для получения количества элементов в выходной 
     * очереди.
     * @param connection источник сообщения.
     * @return LinkedList< IrcCommandReport > - выходная очередь 
     * клиента IRC.
     */
    public int getOutputQueueSize(Connection connection) {
        return 0;
    }
}
