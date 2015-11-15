package simpleircserver.parser;
import simpleircserver.channel.ChannelMode;
import simpleircserver.talker.user.UserMode;

/*
 * 
 * ModeOperation 
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
 * Enum, с помощью которого передается информация об операциях, которые 
 * необходимо произвести с режимами клиента ({@link UserMode}) или 
 * канала ({@link ChannelMode}). 
 *
 * @version 0.5 2012-02-12
 * @version 0.5.3 2015-11-05 Program units were moved from default package into packages with names. Unit tests were added.
 * @author  Nikolay Kirdin
 */
public enum ModeOperation {
    
    /** 
     * Операция добавления режима или добавления параметра к 
     * установленному режиму.
     */
    ADD('+'), 
    
    /** 
     * Операция удаления режима или удаления параметра у 
     * установленного режима.
     */
    REMOVE('-'), 
    
    /** 
     * Операция индикации состояния (установлен/сброшен) режима и его
     * параметров.
     */
    LIST(' ');
    
    /** Символьное представление операции. */
    private char op;
    
    /**
     * Конструктор.
     * @param с символьное представление операции.
     */
    private ModeOperation(char c) {
        op = c;
    }
    
    /**
     * Получение символьного представления операции.
     * @return символьное представление операции.
     */
    public char getOperation() {
        return op;
    }
}
