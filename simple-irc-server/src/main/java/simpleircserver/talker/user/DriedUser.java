package simpleircserver.talker.user;
/*
 * 
 * DriedUser 
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
 *  License Version 3 along with this program.  If not, see 
 *  <http://www.gnu.org/licenses/>.
 *
 */

/**
 * Класс, служащий для хранении краткой информации о владельце никнэйма.  
 * Эта информация используется при выполнении команды IRC WHOWAS.
 *
 * @version 0.5 2012-03-16
 * @version 0.5.3 2015-11-05 Program units were moved from default package into packages with names. Unit tests were added.
 * @author  Nikolay Kirdin
 */

public class DriedUser {
    
    /** Id клиента. */
    public final long id;
    
    /** Никнэйм клиента. */
    public final String nickname;
    
    /** Аккоунт пользователя на его хосте. */
    public final String username;
    
    /** FQDN хоста пользователя. */
    public final String hostname;
    
    /** "Реальное" имя пользователя. */
    public final String realname; 
    
    /** FQDN сервера, к которому был подключен пользователь. */
    public final String serverHostname;

    /**
     * Конструктор.
     * @param nickname никнэйм клиента.
     * @param username аккоунт пользователя на его хосте.
     * @param hostname FQDN хоста пользователя.
     * @param realname "реальное" имя пользователя.
     * @param serverHostname FQDN сервера, к которому был подключен 
     * пользователь.
     * @param id Id клиента.
     */
    public DriedUser (String nickname, String username, String hostname, 
            String realname, String serverHostname, long id) {
        this.nickname = nickname;
        this.username = username;
        this.hostname = hostname;
        this.realname = realname;
        this.serverHostname = serverHostname;
        this.id = id;
    }
}
