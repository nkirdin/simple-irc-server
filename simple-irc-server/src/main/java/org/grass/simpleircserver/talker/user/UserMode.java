package org.grass.simpleircserver.talker.user;
/*
 * 
 * UserMode
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
 * Класс, служащий для хранения и индикации режимов клиента.
 *
 * @version 0.5 2012-02-12
 * @version 0.5.3 2015-11-05 Program units were moved from default package into packages with names. Unit tests were added.
 * @author  Nikolay Kirdin
*/
public enum UserMode {
    
    /**  У клиента установлен AWAY &lt;message&gt;. */
    a('a'),
    
    /** Информация о клиенте не предоставляется другим клиентам. */
    i('i'),
    
    /** Клиент является получателем WALLOPS сообщений. */
    w('w'),
    
    /** Клиенту ограничен функционал некоторых команд IRC. */
    r('r'),
    
    /** Клиент является оператором. */
    o('o'),
    
    /** Клиент является локальным оператором. (Не реализовано). */
    O('O'),
    
    /** Клиент является получателем служебных сообщений сервера. */
    s('s');
    
    /** Символьное представление режима. */
    private char op;
    
    /**
     * Конструктор.
     * @param c символьное представление режима.
     */
    private UserMode(char c) {
        op = c;
    }
    
    /**
     * Получение символьного представления режима.
     * @return символьное представление режима.
     */
    public char getMode() {
        return op;
    }
}