package simpleircserver.parser;
/*
 * 
 * IrcIncomingMessage 
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

import java.util.concurrent.atomic.*;

import simpleircserver.connection.NetworkConnection;
import simpleircserver.talker.IrcTalker;

/**
 * Класс, использующийся для учета и хранения поступающих сообщений. 
 *
 * @version 0.5 2012-03-11
 * @version 0.5.3 2015-11-05 Program units were moved from default package into packages with names. Unit tests were added.
 * @author  Nikolay Kirdin
 * 
 */
public class IrcIncomingMessage {
    
    /** Счетчик генератора уникальных идентификаторов. */
    static private AtomicLong seq = new AtomicLong();
    
    /** Сообщение. */
    public final String message;
    
    /** Уникальный идентификатор. */
    public final long id;
    
    /** Отправитель сообщения. */
    public final IrcTalker sender;
    
    /** Время поступления сообщения.*/
    public final long incomingTime;
    /** 
     * Конструктор. При создании объекта генерируется уникальный 
     * идентификатор.
     * @param message сообщение.
     * @param sender отправитель сообщения.
     */
    public IrcIncomingMessage(String message, 
        IrcTalker sender) {
        id = seq.getAndIncrement();
        this.message = message;
        this.sender = sender;
        incomingTime = System.currentTimeMillis();
    }
    
    public String getSource() {
        String source = null;
        if (sender.getConnection() instanceof NetworkConnection) {
            NetworkConnection nc = ((NetworkConnection) 
                    sender.getConnection());
            source = nc.getSocket().getRemoteSocketAddress().toString();
        } else {
            source = sender.getConnection().toString();
        }
        return source;
    }
    
    /** 
     * Текстовое представление объекта. 
     * @return текстовое представление.
     */
    public String toString() {
        return String.valueOf(incomingTime) + " " + 
        id + " " + getSource() + " " + sender.getNickname() + " " + message;
    }
}
