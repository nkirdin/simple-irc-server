/*
 * 
 * IrcAdminConfig 
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
 * Класс, который хранит административную информацию о IRC сервере и 
 * администраторе этого сервера. 
 *
 * @version 0.5 2012-02-10
 * @author  Nikolay Kirdin
 * 
 */
public class IrcAdminConfig {
    
    /** Имя и фамилия администратора сервера. */
    private String name;
    
    /** Адрес расположения сервера. */
    private String location;
    
    /** Наименование и адрес организации, которой принадлежит сервер. */
    private String location2;
    
    /** Адрес электронной почты администратора сервера. */
    private String email;
    
    /** Любая дополнительная информация о сервере и/или администраторе. */
    private String other;

    /**
     * Основной конструктор.
     * @param name имя и фамилия администратора сервера.
     * @param location адрес расположения сервера.
     * @param location2 наименование и адрес организации, которой 
     * принадлежит сервер.
     * @param email адрес электронной почты администратора сервера. 
     * @param other любая дополнительная информация о сервере и/или 
     * администраторе. 
     */
    public IrcAdminConfig(String name,
            String location,
            String location2,
            String email,
            String other) {
        this.name = name;
        this.location = location;
        this.location2 = location2;
        this.email = email;
        this.other = other;
    }
    
    /**
     * Получение имени и фамилии администратора сервера.
     * @return имя и фамилия администратора сервера.
     */
    public String getName() {
        return name;
    }

    /**
     * Задание имени и фамилии администратора сервера.
     * @param name имя и фамилия администратора сервера.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Получение адреса расположения сервера.
     * @return адрес расположения сервера.
     */
    public String getLocation() {
        return location;
    }

    /**
     * Задание адреса расположения сервера. 
     * @param location адрес расположения сервера.
     */
    public void setLocation(String location) {
        this.location = location;
    }

    /**
     * Получение наименования и адреса организации, которой принадлежит 
     * сервер.
     * @return наименование и адрес организации, которой принадлежит 
     * сервер.
     */
    public String getLocation2() {
        return location2;
    }

    /**
     * Задание наименования и адреса организации, которой принадлежит 
     * сервер.
     * @param location2 наименование и адрес организации, которой 
     * принадлежит сервер.
     */
    public void setLocation2(String location2) {
        this.location2 = location2;
    }

    /**
     * Получение адреса электронной почты администратора сервера. 
     * @return адрес электронной почты администратора сервера. 
     */
    public String getEmail() {
        return email;
    }

    /**
     * Задание адреса электронной почты администратора сервера.
     * @param email адрес электронной почты администратора сервера.
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Получение любой дополнительной информации о сервере и/или 
     * администраторе.
     * @return любая дополнительная информация о сервере и/или 
     * администраторе. 
     */
    public String getInfo() {
        return other;
    }

    /**
     * Задание любой дополнительной информации о сервере и/или 
     * администраторе. 
     * @param other любая дополнительная информация о сервере и/или 
     * администраторе.
     */
    public void setInfo(String other) {
        this.other = other;
    }
}
