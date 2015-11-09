/*
 * 
 * ServerBaseTest
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

package org.grass.simpleircserver.tests.server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import org.grass.simpleircserver.connection.Connection;

/**
 * ServerBaseTest
 * @version 0.5.3.1 2015-11-06 
 * @author  Nikolay Kirdin
 */
public class ServerBaseTest {
	public AtomicBoolean running = new AtomicBoolean(true);
	public AtomicBoolean down = new AtomicBoolean(false);
    public AtomicBoolean closeAll = new AtomicBoolean(false);
    
    public AtomicBoolean restart = new AtomicBoolean(false);
    public AtomicLong sleepTO = new AtomicLong(100);
    

    public void setRunning(boolean running){
        this.running.set(running);
    }
    public void setDown(boolean down){
        this.down.set(down);
    }
    public void setCloseAll(boolean closeAll){
        this.closeAll.set(closeAll);
    }
    
    protected Socket socket;

    public class Client {
        public Socket s;
        public Connection c;
        public BufferedReader br;
        public BufferedWriter bw;
        public String nickname;
    }
    
    protected Client[] client = new Client[4];
    public void run() throws Exception {}
}
    

