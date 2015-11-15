/*
 * 
 * ServerStartRestartStopTest
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

package simpleircserver.tests.server;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.Before;
import org.junit.Test;

import simpleircserver.ParameterInitialization;
import simpleircserver.Server;
import simpleircserver.base.Constants;
import simpleircserver.base.DB;
import simpleircserver.base.Globals;
import simpleircserver.tests.IrcCommandTest;
import simpleircserver.tests.ServerTestUtils;
import simpleircserver.tests.server.ServerIrcTalkerProcessorTest.Client;

/**
 * ServerStartRestartStopTest
 * 
 * @version 0.5.3.1 2015-11-06
 * @author Nikolay Kirdin
 */
public class ServerStartRestartStopTest {

    Server server;
    Thread serverThread;

    @Before
    public void setUp() throws Exception {
        if (Globals.serverSocket.get() != null) Globals.serverSocket.get().close();
        ServerTestUtils.restoreGlobals();
        String configFilePath = ServerTestUtils.buildResourceFilePath(Constants.CONFIG_FILE_PATH);
       
        String logFilePath = ServerTestUtils.buildResourceFilePath(Constants.LOG_FILE_PATH);
        Globals.configFilename.set(configFilePath);

        Globals.logFileHandlerFileName.set(logFilePath);  

        ParameterInitialization.configSetup();
        assertTrue("Normal Initialisation", ParameterInitialization.networkComponentsSetup());
        ParameterInitialization.loggerSetup();

        Globals.logFileHandler.get().setLevel(Level.ALL);
        Globals.logger.get().setLevel(Level.ALL);
        ParameterInitialization.loggerLevelSetup();
        Globals.serverDown.set(false);

    }

    @Test
    public void serverStartStopTest() throws Exception {
        System.out.println("--Server--Start test-----------------------------");
        Globals.logger.get().log(Level.FINEST, "--Server--Start----------------------------------");
        
        Globals.serverDown.set(false);
        server = new Server();
        serverThread = new Thread(server);

        serverStart();
 
        Globals.logger.get().log(Level.FINEST, "**Server**Start******************************OK**");
        Globals.logger.get().log(Level.FINEST, "--Server--Down-----------------------------------");
        
        serverStop();
        
        Globals.logger.get().log(Level.FINEST, "**Server**Down********************************OK*");
        System.out.println("**Server**Start test*************************OK**");

    }

    @Test
    public void serverRestartTest() throws Exception {
    
        System.out.println("--Server--Start/Restart/Stop---------------------");

        Globals.serverDown.set(false);
        server = new Server();
        serverThread = new Thread(server);
        
        serverStart();
                
        Globals.logger.get().log(Level.FINEST, "--Server--Restart--------------------------------");

        long serverStartTime = Globals.serverStartTime.get();
        
        // Restart server
        Globals.serverRestart.set(true);
        
        long restartTO = 6000;
        Thread.sleep(restartTO);
        
        assertNotEquals("Server restarted", Thread.State.NEW, serverThread.getState());
        assertNotEquals("Server not terminated after restart", Thread.State.TERMINATED, serverThread.getState());
        assertTrue("Server restarted", Globals.serverStartTime.get() > serverStartTime);
        Globals.logger.get().log(Level.FINEST, "**Server**Restart****************************OK**");
        Globals.logger.get().log(Level.FINEST, "--Server--Down-----------------------------------");
        serverStop();
        System.out.println("**Server**Start/Restart/Stop*****************OK**");

    }

    private void serverStart() throws Exception {
        
        long serverStartTime = System.currentTimeMillis();
        
        long predStartTO = 10;
        Thread.sleep(predStartTO);

        serverThread.start();
        
        long startTO = 1000;
        Thread.sleep(startTO);

        assertNotEquals("Server started", Thread.State.NEW, serverThread.getState());
        assertNotEquals("Server not terminated", Thread.State.TERMINATED, serverThread.getState());
        assertNotEquals("Start time is changed", serverStartTime, Globals.serverStartTime.get());
    }
    
    private void serverStop() throws Exception {
        Globals.serverDown.set(true);
        Globals.logger.get().log(Level.INFO, "Down");

        long stopTO = 5000;
        Thread.sleep(stopTO);

        assertEquals("Server terminated", Thread.State.TERMINATED, serverThread.getState());
    }



}
