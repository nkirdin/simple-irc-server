package simpleircserver.talker.server;
/*
 * 
 * IrcServer 
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

import java.net.*;
import java.util.logging.Level;

import simpleircserver.base.Constants;
import simpleircserver.base.Globals;
import simpleircserver.talker.IrcTalker;

/**
 * Класс, хранящий информацию о серверах IRC.
 *
 * @version 0.5 2012-02-11
 * @version 0.5.3 2015-11-05 Program units were moved from default package into packages with names. Unit tests were added.
 * @author  Nikolay Kirdin
 */
public class IrcServer extends IrcTalker {
    
    /** Краткое описание сервера. */
    private String info;
    
    /** Коструктор по умолчанию. */
    private IrcServer() {}        
        
    /** 
     * Конструктор.
     * @param networkId сетевой идентификатор сервера.
     * @param hostname FQDN сервера.
     * @param info краткое описание сервера.
     */
    private IrcServer(InetAddress networkId, String hostname, String info) {
        setNetworkId(networkId);
        setHostname(hostname);
        setInfo(info);
    } 
    
    /**
     * Создатель объекта без параметров. Проверяется объем свободной 
     * памяти, если этот объем меньше {@link Constants#MIN_FREE_MEMORY}, 
     * то будет вызван сборщик мусора. Если после прцедуры сборки мусора, 
     * памяти будет недостаточно, то объект создаваться не будет.
     * @return новый объект класса IrcServer или null, если достигнуто
     * ограничение по доступной памяти.
     */
    public static IrcServer create() {
        IrcServer result = null;
        long freeMemory = Runtime.getRuntime().freeMemory();
        if (freeMemory < Constants.MIN_FREE_MEMORY) {
            System.gc();
        }
        freeMemory = Runtime.getRuntime().freeMemory();
        if (freeMemory >= Constants.MIN_FREE_MEMORY) {
            result = new IrcServer();
        } else {
            Globals.logger.get().log(Level.SEVERE, 
                    "Insufficient free memory (b): " + freeMemory);
        }
        return result;
    }
    

    /**
     * Создатель объекта на основе объекта класса IrcTalker. 
     * @param ircTalker объект класса IrcTalker, на основе которого 
     * создается объект класса IrcServer.
     * @return новый объект класса IrcServer или null, если достигнуто
     * ограничение по доступной памяти.
     */
    public static IrcServer create(IrcTalker ircTalker) {
        IrcServer ircServer = IrcServer.create();
        if (ircServer != null) {
            ircServer.setNickname(ircTalker.getNickname());
            ircServer.setNetworkId(ircTalker.getNetworkId());
            ircServer.setHostname(ircTalker.getHostname());
            ircServer.setHopcount(ircTalker.getHopcount());        
            ircServer.setRegistered(ircTalker.isRegistered());
            ircServer.setConnection(ircTalker.getConnection());
        }
        return ircServer;
    }
    /**
     * Создатель объекта.
     * @param networkId сетевой идентификатор сервера.
     * @param hostname FQDN сервера.
     * @param info краткое описание сервера.
     * @return новый объект класса IrcServer или null, если достигнуто
     * ограничение по доступной памяти.
     */
    public static IrcServer create(InetAddress networkId, 
            String hostname, String info) {
        IrcServer ircServer = IrcServer.create();
        if (ircServer != null) {
            ircServer.setNetworkId(networkId);
            ircServer.setHostname(hostname);
            ircServer.setInfo(info);
        }
        return ircServer;
    }
    
    /** 
     * Получение FQDN сервера.
     * @return FQDN сервера.
     */
    public String getNickname() {
        return getHostname();
    }
     
    /**
     * Задание краткого описания сервера.
     * @param info краткое описание сервера.
     */
    public synchronized void setInfo(String info) {
        this.info = info;
    }
    
    /**
     * Получение краткого описания сервера.
     * @return info краткое описание сервера.
     */
    public synchronized String getInfo() {
        return info;
    }
    
    /** 
     * Действия, выполняемые при установлении связи с удаленным сервером. 
     * (Не реализовано.)
     * @param port порт.
     * @param ircServer сервер.
     */
    public void connect(IrcServer ircServer, int port) {
    }
}
