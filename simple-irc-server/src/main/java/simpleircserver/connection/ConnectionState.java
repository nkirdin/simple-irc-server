package simpleircserver.connection;
/*
 * 
 * ConnectionState 
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


/**
 * Enum, с помощью которого моделируется жизненный цикл соединения IRC.
 *
 * Этот жизненный цикл разделен на следующие этапы:
 * <UL>
 *      <LI> Инициализация.</LI>
 *      <LI> Эксплуатация.</LI>
 *      <LI> Вывод из эксплуатации.</LI>
 *</UL>
 *
 * Этот жизненный цикл моделируется с помощью нижеперечисленных 
 * состояний, некоторые этапы моделируются, с помощью нескольких 
 * состояний. Дополнительно введены состояния, отражающее состояние 
 * объекта после завершения последнего этапа и состояние ошибки, 
 * когда объект не может перейти (или быть переведенным) в целевое 
 * состояние.
 * 
 * @version 0.5 2012-02-13
 * @version 0.5.3 2015-11-05 Program units were moved from default package into packages with names. Unit tests were added.
 * @author  Nikolay Kirdin 
 */
public enum ConnectionState {
    
    /** Вновь созданное соединение. */
    NEW,
    
    /** Начало этапа инициализации. Инициализация. */
    INITIALIZING,
    
    /** Инициализация успешно завершена. */
    INITIALIZED,
    
    /** Эксплуатация. */
    OPERATIONAL,
    
    /** Начало этапа вывода из эксплуатации. */
    CLOSE,
    
    /** Состояние нахождения в выводе из эксплуатации. */
    CLOSING,
    
    /** Жизненный цикл объекта завершен. */
    CLOSED,
    
    /** Состояние ошибки. */
    BROKEN
}
