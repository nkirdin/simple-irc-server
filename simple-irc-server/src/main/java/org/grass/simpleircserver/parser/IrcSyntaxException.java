package org.grass.simpleircserver.parser;
/*
 * 
 * IrcSyntaxException 
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


/**
 * Класс, с помощью которого исполнители команд IRC сигнализируют об 
 * обнаружении синтаксической ошибке в строке команды. 
 *
 * @version 0.5 2012-02-14
 * @version 0.5.3 2015-11-05 Program units were moved from default package into packages with names. Unit tests were added.
 * @author  Nikolay Kirdin
 * 
 */
public class IrcSyntaxException extends Exception {
    
    /** Конструктор без параметров. */
    public IrcSyntaxException() {
        super();
    }
    
    /** 
     * Конструктор.
     * @param s сообщение.
     */
    public IrcSyntaxException(String s) {
        super(s);
    }
        
    /** 
     * Конструктор.
     * @param e сообщение.
     */
    public IrcSyntaxException(Throwable e) {
        super(e);
    }
}
