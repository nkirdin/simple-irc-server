package simpleircserver.channel;
/*
 * 
 * ChannelMode 
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
 * Enum, который служит для установки и индикации режимов каналов IRC. 
 *
 * @version 0.5 2012-02-07
 * @version 0.5.3 2015-11-05 Program units were moved from default package into packages with names. Unit tests were added.
 * @author  Nikolay Kirdin
 */
public enum ChannelMode {
    
    /** Cоздатель канала (указывается только для членов канала). */
    O('O'),
    
    /** Оператор канала (указывается только для членов канала). */
    o('o'), 
    
    /** 
     * Член канала "с правом голоса" ("vote") т.е. член канала, который 
     * имеет право посылать сообщения в модерирумый канал (режим канала 
     * {@link #m}) (указывается только для членов канала).
     */
    v('v'),
    
    /** 
     * Анонимный ("anonymous") канал т.е. канал который заменяет учетную 
     * информацию об участнике канала на никнэйм "anonymous" 
     * (указывается только для каналов).
     */
    a('a'), 
    
    /**
     * Канал, стать членом, которого можно только по приглашению 
     * ("invite only") (указывается только для каналов).
     */
    i('i'),
    
    /** 
     * Модерируемый канал, т.е. канал который принимает сообщения только 
     * от членов с "с правом голоса" (режим члена {@link #v}) 
     * (указывается только для каналов).
     */
    m('m'),
    
    /** 
     * Канал, который принимает сообщения только от своих членов  
     * (указывается только для каналов).
     */
    n('n'),
    
    /** 
     * "Тихий" ("quiet") канал, т.е. канал, который принимает сообщения 
     * только от оператора канала (режим члена {@link #o}) (указывается 
     * только для каналов).
     */
    q('q'),
    
    /** 
     * "Приватный" ("private") канал т.е. канал, информация о котором
     * доступна только его членам (указывается только для каналов).
     */
    p('p'),
    
    /** 
     * "Секретный" ("secret") канал - полный аналог приватного канала 
     * (режим канала {@link #p}) (указывается только для каналов).
     */
    s('s'),
    
    /** 
     * Server reop flag (не реализован) (указывается только для 
     * каналов).
     */
    r('r'),
    
    /** 
     * Канал, топик которого может изменить только оператор канала 
     * (режим члена {@link #o}) (указывается только для каналов).
     */
    t('t'),
    
    /** 
     * Наличие ключа (пароля) у канала (указывается только для каналов).
     */
    k('k'),
    
    /** 
     * Наличие ограничения на максимальное количество членов канала 
     * (указывается только для каналов).
     */
    l('l'),
    
    /** 
     * Наличие списка учетных записей клиентов, которым запрещено 
     * подключаться к каналу (указывается только для каналов).
     */
    b('b'),
    
    /** 
     * Наличие списка исключений из списка запрета на подключение для 
     * канала с режимом {@link #b} (указывается только для каналов).
     */
    e('e'), 
    
    /**
     * Наличие списка учетных записей клиентов, которым разрешено 
     * автоматическое подключение к каналу с режимом {@link #i}
     * (указывается только для каналов).
     */
    I('I');
    
    /** Символьное представление режима канала. */
    final char op;
    
    private ChannelMode(char c) {
        op = c;
    }
    
    public char getOp() {
    	return op;
    }
    
}
