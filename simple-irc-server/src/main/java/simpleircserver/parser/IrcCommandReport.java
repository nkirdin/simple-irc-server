package simpleircserver.parser;
/*
 * 
 * IrcCommandReport 
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

import java.util.concurrent.atomic.*;

import simpleircserver.base.Recipient;
import simpleircserver.talker.IrcTalker;

/**
 * Класс, использующийся для хранения и транспортировки сообщений. 
 *
 * @version 0.5 2012-02-10
 * @version 0.5.3 2015-11-05 Program units were moved from default package into packages with names. Unit tests were added.
 * @author  Nikolay Kirdin
 * 
 */
public class IrcCommandReport {
    
    /** Счетчик генератора уникальных идентификаторов. */
    static private AtomicLong seq = new AtomicLong();
    
    /** Сообщение. */
    private String report;
    
    /** Уникальный идентификатор. */
    public AtomicLong id = new AtomicLong();
    
    /** Получатель сообщения. */
    private Recipient destination;
    
    /** Отправитель сообщения. */
    private IrcTalker sender;
    
    /** 
     * Конструктор. При создании объекта генерируется уникальный 
     * идентификатор.
     * @param report Сообщение.
     * @param destination Получатель сообщения.
     * @param sender Отправитель сообщения.
     */
    public IrcCommandReport(String report, 
        Recipient destination, 
        IrcTalker sender) {
        id.set(seq.incrementAndGet());
        this.report = report;
        this.destination = destination;
        this.sender = sender;
    }
    
    /** 
     * Задание сообщение.
     * @param report сообщение.
     */
    public void setReport(String report) {
        this.report = report;
    }
    
    /** 
     * Получение сообщения.
     * @return report сообщение.
     */
    public String getReport() {
        return report;
    }
    
    /** 
     * Получение адресата.
     * @return destination адресат.
     */
    public Recipient getDestination() {
        return destination;
    }
    
    /** 
     * Задание адресата.
     * @param destination адресат.
     */
    public void setDestination(Recipient destination) {
        this.destination = destination;
    }

    /** 
     * Получение источника сообщения.
     * @return sender источник сообщения.
     */
    public IrcTalker getSender() {
        return sender;
    }
    
    /** 
     * Задание источника сообщения.
     * @param sender источник сообщения.
     */
    public void setSender(IrcTalker sender) {
        this.sender = sender;
    }
    
    /** 
     * Текстовое представление объекта. Оно имеет следущий вид:
     * <P>
     * <code>
     * "From: <Источник>  To: <Адресат> <">"> <Сообщение>"
     * </code>
     * @return текстовое представление.
     */
    public String toString() {
        return "From:" + sender.getNickname() + 
        " To:" + destination.getNickname() + 
        " >" + report + "\n";
    }
}
