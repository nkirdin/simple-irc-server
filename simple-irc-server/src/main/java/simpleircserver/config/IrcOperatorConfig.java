package simpleircserver.config;
/*
 * 
 * IrcOperatorConfig 
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
 * Класс, служащий для хранения учетных записей операторов.
 * В полях класса хранятся &lt;username&gt; и &lt;password&gt;.  
 *
 * @version 0.5 2012-02-11
 * @version 0.5.3 2015-11-05 Program units were moved from default package into packages with names. Unit tests were added.
 * @author  Nikolay Kirdin
 */
public class IrcOperatorConfig {
    
    /** &lt;username&gt; */
    private String name;
    
    /** &lt;password&gt; */
    private String password;
    
    /**
     * Получение &lt;username&gt;.
     * @return &lt;username&gt;.
     */
    public String getName() {
        return name;
    }

    /**
     * Задание &lt;username&gt;.
     * @param name &lt;username&gt;.
     */
     public void setName(String name) {
        this.name = name;
    }

    /**
     * Получение &lt;password&gt;.
     * @return &lt;password&gt;.
     */
    public String getPassword()
    {
        return password;
    }

    /**
     * Задание &lt;password&gt;.
     * @param password &lt;password&gt;.
     */
    public void setPassword(String password) {
        this.password = password;
    }

    
    /**
     * Конструктор.
     * @param name &lt;username&gt;.
     * @param password &lt;password&gt;
     */
    public IrcOperatorConfig(String name,
            String password) {
        this.name = name;
        this.password = password;
    }
}
