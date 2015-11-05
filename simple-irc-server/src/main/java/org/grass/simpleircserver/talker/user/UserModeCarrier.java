package org.grass.simpleircserver.talker.user;
import org.grass.simpleircserver.parser.ModeOperation;

/*
 * 
 * UserModeCarrier
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
 * Enum, служащий для обмена информацие о режиме клиента и операцией,
 * которую надо выполнить с этим режимом.
 *
 * @version 0.5 2012-02-12
 * @version 0.5.3 2015-11-05 Program units were moved from default package into packages with names. Unit tests were added.
 * @author  Nikolay Kirdin
 */
public class UserModeCarrier {
    
    /** Режим клиента.*/
    private UserMode mode;
    
    /** Операция. */
    private ModeOperation operation;
    
    /**
     * Конструктор.
     * @param mode режим клиента.
     * @param operation операция.
     */
    public UserModeCarrier(UserMode mode, ModeOperation operation) {
        this.mode = mode;
        this.operation = operation;
    }
    
    /**
     * Получение режима клиента.
     * @return режим клиента.
     */
    public UserMode getMode() {
        return mode;
    }
    /**
     * Получение операции.
     * @return операция.
     */
    public ModeOperation getOperation() {
        return operation;
    }
    
    /**
     * Получение текстового представления объекта. Режимы представлены в
     * виде строки следующего вида:
     * <P><code>
     * "&gt;операция {@link ModeOperation}&lt;&gt;режим {@link UserMode}&lt;"
     * </code>
     *
     * @return текстовое представление типа операции и режима клиента
     */
    public String toString() {
        return String.valueOf(operation.getOperation()) +
                String.valueOf(mode.getMode());
    }
}