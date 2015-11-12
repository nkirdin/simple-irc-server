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

package simpleircserver.tests.server;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.Before;
import org.junit.Test;

import simpleircserver.Server;
import simpleircserver.base.Constants;
import simpleircserver.base.DB;
import simpleircserver.base.Globals;
import simpleircserver.config.ParameterInitialization;
import simpleircserver.tests.IrcCommandTest;

/**
 * ServerStartRestartStopTest
 * @version 0.5.3.1 2015-11-06 
 * @author  Nikolay Kirdin
 */
public class ServerStartRestartStopTest { 
	
	@Before
	public void setUp() {
        
        String configFilePath = IrcCommandTest.buildResourceFilePath(Constants.CONFIG_FILE_PATH);
        Globals.configFilename.set(configFilePath);
       
        String logFilePath = IrcCommandTest.buildResourceFilePath(Constants.LOG_FILE_PATH);
        Globals.logFileHandlerFileName.set(logFilePath);        
		Globals.logFileHandler.get().setLevel(Level.ALL);
		Globals.logger.get().setLevel(Level.ALL);
		Globals.serverDown.set(false);
	}

	@Test
    public void serverStartRestartStopTest() throws Exception {
        System.out.println("--Server--Start/Restart/Stop---------------------");
        //Server start/restart/stop

        Server server = new Server();
        Thread serverThread = new Thread(server);
        serverThread.start();
        
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {}
        Globals.logger.get().log(Level.FINEST, "--Server--Start----------------------------------");
        assertNotEquals("Server started",Thread.State.NEW, serverThread.getState());
        assertNotEquals("Server not terminated",Thread.State.TERMINATED, serverThread.getState());
        Globals.logger.get().log(Level.FINEST, "**Server**Start******************************OK**");

        //Restart server
        Globals.logger.get().log(Level.FINEST, "--Server--Restart--------------------------------");

        long serverStartTime1 = Globals.serverStartTime.get();
        Globals.serverRestart.set(true);
        long restartTO = 5000;
        try {
            Thread.sleep(restartTO);
        } catch (InterruptedException e) {}
        assertNotEquals("Server restarted", Thread.State.NEW, serverThread.getState());
        assertNotEquals("Server not terminated after restart", Thread.State.TERMINATED, serverThread.getState());
        assertTrue("Server restarted", Globals.serverStartTime.get() > serverStartTime1);
        Globals.logger.get().log(Level.FINEST, "**Server**Restart****************************OK**");
        Globals.logger.get().log(Level.FINEST, "--Server--Down-----------------------------------");
        Globals.serverDown.set(true);
        Globals.logger.get().log(Level.INFO, "Down");
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {}
        assertEquals("Server terminated", Thread.State.TERMINATED, serverThread.getState());
        System.out.println("**Server**Start/Restart/Stop*****************OK**");
        
    }
}

