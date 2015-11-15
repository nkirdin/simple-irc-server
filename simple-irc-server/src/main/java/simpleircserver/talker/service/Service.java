package simpleircserver.talker.service;
/*
 * 
 * Service 
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

import java.util.logging.Level;

import simpleircserver.base.Constants;
import simpleircserver.base.Globals;
import simpleircserver.talker.IrcTalker;
import simpleircserver.talker.server.IrcServer;
import simpleircserver.talker.user.User;

/**
 * Класс, служащий для хранения информации о клиентах-сервисах IRC.
 *
 * @version 0.5 2012-02-13
 * @version 0.5.3 2015-11-05 Program units were moved from default package into packages with names. Unit tests were added.
 * @author  Nikolay Kirdin
 */
public class Service extends IrcTalker {
        
    /** 
     * Область обслуживания.  Область обслуживания задается с 
     * помощью регулярного выражения, составленного по правилам 
     * регулярных выражений для IRC. Это выражение должно удовлетворять 
     * регулярному выражению {@link IrcParamRegex#servernameMaskRegex}.
     * С помощью этого параметра определяется домен, в котором серверы 
     * IRC будут получать и распространять информацию об этом сервисе. 
     */
    protected String distribution;
    
    /** Параметры доступа к учетной информации клиента. */
    protected String type;
    
    /** Краткая информация о сервисе. */
    protected String info;
    
    /** Сервер, на котором установлен этот сервис. */
    protected IrcServer ircServer;
    
    /** Конструктор. */
    private Service() {}
    
    /**
     * Создатель объекта без параметров. Проверяется объем свободной 
     * памяти, если этот объем меньше {@link Constants#MIN_FREE_MEMORY}, 
     * то будет вызван сборщик мусора. Если после прцедуры сборки мусора, 
     * памяти будет недостаточно, то объект создаваться не будет.
     * @return новый объект класса Service, или null, если достигнуто 
     * ограничение по доступной памяти.
     */
    public static Service create() {
        Service result = null;
        long freeMemory = Runtime.getRuntime().freeMemory();
        if (freeMemory < Constants.MIN_FREE_MEMORY) {
            System.gc();
        }
        freeMemory = Runtime.getRuntime().freeMemory();
        if (freeMemory >= Constants.MIN_FREE_MEMORY) {
            result = new Service();
        } else {
            Globals.logger.get().log(Level.SEVERE, 
                    "Insufficient free memory (b): " + freeMemory);
        }
        return result;
    }
    
     /** 
      * Создатель объекта на основе существующего объекта класса 
      * IrcTalker. 
      * @param ircTalker объект с использованием данных, которого будет 
      * создан новый объект Service. 
      * @return новый объект класса Service, или null, если достигнуто 
      * ограничение по доступной памяти.
      */
    public static Service create(IrcTalker ircTalker) {
        Service service = Service.create();
        if (service != null) {
            service.setNickname(ircTalker.getNickname());
            service.setNetworkId(ircTalker.getNetworkId());
            service.setHostname(ircTalker.getHostname());
            service.setHopcount(ircTalker.getHopcount());
            service.setRegistered(ircTalker.isRegistered());
            service.setConnection(ircTalker.getConnection());
        }
        return service;
    }
    
     /** 
      * Создатель объекта на основе существующего объекта класса 
      * IrcServer. 
      * @param ircServer объект с использованием данных, которого будет 
      * создан новый объект Service. 
      * @return новый объект класса Service, или null, если достигнуто 
      * ограничение по доступной памяти.
      */
    public static Service create(IrcServer ircServer) {
        Service service = Service.create();
        if (service != null) {
            service.setNickname(ircServer.getNickname());
            service.setNetworkId(ircServer.getNetworkId());
            service.setHostname(ircServer.getHostname());
            service.setHopcount(ircServer.getHopcount());
            service.setRegistered(ircServer.isRegistered());
            service.setConnection(ircServer.getConnection());
            service.setIrcServer(ircServer);
        }
        return service;
    }

     /** 
      * Создатель объекта на основе существующего объекта класса 
      * User. 
      * @param user объект с использованием данных, которого будет 
      * создан новый объект Service. 
      * @return новый объект класса Service, или null, если достигнуто 
      * ограничение по доступной памяти.
      */
    public static Service create(User user) {
        Service service = Service.create();
        if (service != null) {
            service.setNickname(user.getNickname());
            service.setNetworkId(user.getNetworkId());
            service.setHostname(user.getHostname());
            service.setHopcount(user.getHopcount());
            service.setRegistered(user.isRegistered());
            service.setIrcServer(user.getIrcServer());
            service.setConnection(user.getConnection());
        }
        return service;
    }
    
    /** 
     * Задание области обслуживания.   
     * @param servernameMask область обслуживания.
     */
    public synchronized void setDistribution(String servernameMask) {
        distribution = servernameMask;
    }
    
    /** 
     * Получение области обслуживания.
     * @return область обслуживания.
     */
    public synchronized String getDistribution() {
        return distribution;
    }

    /** 
     * Задание краткой информации о сервисе.
     * @param info краткая информация о сервисе.
     */
    public synchronized void setInfo(String info) {
        this.info = info;
    }
    
    /** 
     * Получение краткой информации о сервисе.
     * @return краткая информация о сервисе.
     */
    public synchronized String getInfo() {
        return info;
    }

    /** 
     * Задание параметров доступа к учетной информации клиента. 
     * @param type параметры доступа к учетной информации клиента.
     */
    public synchronized void setType(String type) {
        this.type = type;
    }
    
    /** 
     * Получение параметров доступа к учетной информации клиента. 
     * @return параметры доступа к учетной информации клиента.
     */
    public synchronized String getType() {
        return type;
    }
    
    /** 
     * Получение объекта класса IrcServer, описывающий IRC сервер, 
     * на котором установлен этот клиент-сервис.
     * @return объект класса IrcServer.
     */
    public synchronized IrcServer getIrcServer() {
        return ircServer;
    }

    /** 
     * Задание объекта класса IrcServer, описывающий IRC сервер, 
     * на котором установлен этот клиент-сервис.
     *@param ircServer объект класса IrcServer.
     */
    public synchronized void setIrcServer(IrcServer ircServer) {
        this.ircServer = ircServer;
    }
        
    /**
     * Текстовое представление объекта. Объект представляется следующим 
     * образом:
     * <P><code> 
     * "&lt;уникальный идентификатор&gt; &lt;никнэйм&gt; &lt;область обслуживания&gt; 
     * &lt;FQDN сервера этого сервиса&gt; Registered: &lt;признак регистрации&gt;" 
     * </code>
     * <P>Поля разделены пробелом.
     */
    public String toString() {
        return String.valueOf(getId()) +  
        " " + getNickname() + 
        " " + distribution + 
        " " + getIrcServer().getHostname() + 
        " " + "Registered: " + isRegistered();

    }
}
