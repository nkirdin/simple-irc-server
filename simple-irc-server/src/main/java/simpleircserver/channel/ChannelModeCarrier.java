package simpleircserver.channel;
import simpleircserver.parser.ModeOperation;

/*
 * 
 * ChannelModeCarrier 
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
 * Этот класс служит для обмена информацией о режиме каналов IRC, типе
 * операции, которую необходимо выполнить с этим режимом и параметре 
 * режима. Информация о режиме хранится с помощью объекта класса 
 * {@link ChannelMode}, информация о типе операции хранится с помощью 
 * объекта класса {@link ModeOperation}, параметр режима хранится в 
 * текстовой строке.
 *
 * @version 0.5 2012-02-10
 * @version 0.5.3 2015-11-05 Program units were moved from default package into packages with names. Unit tests were added.
 * @author  Nikolay Kirdin
 */
public class ChannelModeCarrier {
    
    /** Режим канала IRC. */
    private ChannelMode mode;
    
    /** Действие, которое надо выполнить с указанным режимом. */
    private ModeOperation operation;
    
    /** Параметер режима. */
    private String parameter;
    
    /**
     * Конструктор.
     * @param mode режим канала IRC.
     * @param operation действие, которое надо выполнить с указанным
     * режимом.
     * @param parameter параметер режима.
     */
    public ChannelModeCarrier(ChannelMode mode,
            ModeOperation operation,
            String parameter) {
        this.mode = mode;
        this.operation = operation;
        this.parameter = parameter;
    }
    
    /** 
     * Получение информации о режиме канала.
     * @return режим канала.
     */
    public synchronized ChannelMode getMode() {
        return mode;
    }
    
    /** 
     * Сохранение информации о режиме канала. 
     * @param mode режим канала. 
     */
    public synchronized void setMode(ChannelMode mode) {
        this.mode = mode;
    }

    /** 
     * Получение информации о типе операции.
     * @return тип операции. 
     */
    public synchronized ModeOperation getOperation() {
        return operation;
    }
    
    /** 
     * Сохранение информации о типе операции.
     * @param operation тип операции. 
     */
    public synchronized void setOperation(ModeOperation operation) {
        this.operation = operation;
    }
    
    /** 
     * Получение параметра режима.
     * @return параметр режима.
     */
    public synchronized String getParameter() {
        return parameter;
    }
    
    /**
     * Сохранение параметра режима.
     * @param parameter параметр режима.
     */
    public synchronized void setParameter(String parameter) {
        this.parameter = parameter;
    }
    
    /**
     * Получение текстового представления объекта. Он представляется в 
     * виде строки следующего вида:
     * <P><code>
     * "&lt;операция {@link ModeOperation}&gt;&lt;режим {@link ChannelMode}&gt;[&lt;" "&gt;&lt;параметер&gt;]" 
     * </code>
     * @return текстовое представление типа операции, режима канала и
     * параметра режима канала
     */
    public String toString() {
        return String.valueOf(operation.getOperation()) + 
                String.valueOf(mode.op) +
               ((parameter == null || parameter.isEmpty()) ? "" : " " + 
               parameter);
    }
}
