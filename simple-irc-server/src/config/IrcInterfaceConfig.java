/*
 * 
 * IrcInterfaceConfig 
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

import java.net.*;
import java.nio.charset.*;

/**
 * Класс, служащий для хранения информации о параметрах сетевого 
 * интерфейса.
 * Хранится IP-адрес и номер порта интерфейса, и ожидаемая кодировка 
 * сообщений.  
 *
 * @version 0.5 2012-02-11
 * @author  Nikolay Kirdin
 */
public class IrcInterfaceConfig {
    
    /** IP-адрес интерфейса. */
    private InetAddress inetAddress;
    
    /** Номер порта.*/
    private int port;
    
    /** Кодировка. */
    private Charset charset;

    /**
     * Конструктор.
     * @param inetAddress IP-адрес интерфейса.
     * @param port номер порта.
     * @param charset кодировка.
     */
    public IrcInterfaceConfig(InetAddress inetAddress, int port, 
            Charset charset) {
        this.inetAddress=inetAddress;
        this.port = port;
        this.charset = charset;
    }
    
    /**
     * Получение IP-адреса интерфейса.
     * @return IP-адрес интерфейса.
     */
    public InetAddress getInetAddress() {
        return inetAddress;
    }

    /**
     * Задание IP-адреса интерфейса.
     * @param inetAddress IP-адрес интерфейса.
     */
    public void setInetAddress(InetAddress inetAddress) {
        this.inetAddress = inetAddress;
    }
    
    /**
     * Получение номера порта.
     * @return номер порта.
     */
    public int getPort() {
        return port;
    }

    /**
     * Задание номера порта.
     * @param port номер порта.
     */
    public void setPort(int port) {
        this.port = port;
    }
    
    /**
     * Получение кодировки.
     * @return кодировка.
     */
    public Charset getCharset() {
        return charset;
    }

    /**
     * Задание кодировки.
     * @param charset кодировка.
     */
    public void setCharset(Charset charset)    {
        this.charset = charset;
    }
    
}
