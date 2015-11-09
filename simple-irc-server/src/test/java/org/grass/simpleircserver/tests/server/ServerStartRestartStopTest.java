/*
 * 
 * ServerStartRestartStopTest
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

import static org.junit.Assert.assertTrue;

import java.util.logging.Level;

import org.grass.simpleircserver.Server;
import org.grass.simpleircserver.base.Globals;

/**
 * ServerStartRestartStopTest
 * @version 0.5.3.1 2015-11-06 
 * @author  Nikolay Kirdin
 */
public class ServerStartRestartStopTest extends ServerBaseTest { 
	//FIXME
	//@Test
    public void serverStartRestartStopTest() {
        System.out.println("--Server--Start/Restart/Stop---------------------");
        //Server start/restart/stop
        Server server = new Server();
        Thread serverThread = new Thread(server);
        serverThread.start();
        
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {}
        Globals.logger.get().log(Level.INFO, "Start");
        Globals.logger.get().log(Level.FINEST, "--Server--Start/Restart/Stop---------------------");
        assertTrue("Server started",serverThread.getState() != Thread.State.NEW && serverThread.getState() != Thread.State.TERMINATED);
        //Restart server
        long serverStartTime1 = Globals.serverStartTime.get();
        Globals.serverRestart.set(true);
        long restartTO = 5000;
        try {
            Thread.sleep(restartTO);
        } catch (InterruptedException e) {}
        assertTrue("Server running",serverThread.getState() != Thread.State.NEW && serverThread.getState() != Thread.State.TERMINATED);
        assertTrue("Server restarted", Globals.serverStartTime.get() > serverStartTime1);
        Globals.serverDown.set(true);
        Globals.logger.get().log(Level.INFO, "Down");
        Globals.logger.get().log(Level.FINEST, "**Server**Start/Restart/Stop*****************OK**");
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {}
        assertTrue("Server terminated", serverThread.getState() == Thread.State.TERMINATED);
        System.out.println("**Server**Start/Restart/Stop*****************OK**");
        
    }
}

