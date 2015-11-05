package org.grass.simpleircserver.config;
/*
 * 
 * IrcServerConfig 
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

import java.util.*;
import java.util.logging.*;

/**
 * Класс, хранящий конфигурируемые параметры для сервера в целом. 
 *
 * @version 0.5.2 2012-03-30
 * @version 0.5.3 2015-11-05 Program units were moved from default package into packages with names. Unit tests were added.
 * @author  Nikolay Kirdin
 */
public class IrcServerConfig {
    
    /** TimeZone сервера. */
    private TimeZone timeZone;

    /** Уровень журналирования. */
    private Level debugLevel;
    
    /** Путь к файлу MOTD*/
    private String motdFilename;
    
	/**
     * Конструктор.
     * @param timeZone TimeZone сервера.
     * @param debugLevel уровень журналирования.
     */
    public IrcServerConfig(TimeZone timeZone, Level debugLevel, 
    		String motdFilename) {
        this.timeZone = timeZone;
        this.debugLevel = debugLevel;
        this.motdFilename = motdFilename;
    }

    /** Получение уровня журналирования. */
    public Level getDebugLevel() {
        return debugLevel;
    }

    /** Задание уровня журналирования. */
    public void setDebugLevel(Level debugLevel) {
        this.debugLevel = debugLevel;
    }
    
    /** Получение TimeZone сервера. */
    public TimeZone getTimeZone() {
        return timeZone;
    }

    /** Задание TimeZone сервера. */
    public void setTimeZone(TimeZone timeZone) {
        this.timeZone = timeZone;
    }

    /** Получение пути к motd файлу. */
    public String getMotdFilename() {
		return motdFilename;
	}

    /** Задание пути к motd файлу. */
	public void setMotdFilename(String motdFilename) {
		this.motdFilename = motdFilename;
	}

}
