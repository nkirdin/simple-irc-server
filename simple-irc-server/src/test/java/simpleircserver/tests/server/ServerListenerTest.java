/*
 * 
 * ServerListenerTest
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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;

import org.junit.Before;
import org.junit.Test;

import simpleircserver.ParameterInitialization;
import simpleircserver.base.Constants;
import simpleircserver.base.DB;
import simpleircserver.base.Globals;
import simpleircserver.connection.Connection;
import simpleircserver.connection.NetworkConnection;
import simpleircserver.processor.IncomingConnectionListener;
import simpleircserver.tests.IrcCommandTest;
import simpleircserver.tests.ServerTestUtils;
import simpleircserver.tests.server.ServerIrcTalkerProcessorTest.Client;

/**
 * ServerListenerTest
 * @version 0.5.3.1 2015-11-06 
 * @author  Nikolay Kirdin
 */
public class ServerListenerTest { 
	
	private AtomicLong sleepTO;
	private Socket socket;
	
    public class Client {
        public Socket s;
        public Connection c;
        public BufferedReader br;
        public BufferedWriter bw;
        public String nickname;
    }
    
    private Client[] client ;

	
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
        sleepTO = new AtomicLong(100);
        client = new Client[4];

    }

	@Test
    public void serverListenerTest() throws IOException {
        System.out.println("--Listener---------------------------------------");
        //incomingConnectionListener        
        IncomingConnectionListener incomingConnectionListener;
        
        incomingConnectionListener = new IncomingConnectionListener();
        incomingConnectionListener.thread.set(new Thread(incomingConnectionListener));
        
        try {
            Thread.sleep(sleepTO.get() * 2);
        } catch (InterruptedException e) {}
        DB db = Globals.db.get();
        Globals.logger.get().log(Level.INFO, "Start");
        Globals.logger.get().log(Level.FINEST, "--Listener---------------------------------------");
        
        
        incomingConnectionListener.running.set(true);
        incomingConnectionListener.thread.get().start();
        try {
            Thread.sleep(sleepTO.get() * 2);
        } catch (InterruptedException e) {}

        client[0] = new Client();
        socket = new Socket(Globals.thisIrcServer.get().getNetworkId(), Globals.serverPortNumber.get());
        client[0].s = socket;
        
//        client[0].bw = new BufferedWriter(new OutputStreamWriter(client[0].s.getOutputStream()));
//        client[0].br = new BufferedReader(new InputStreamReader(client[0].s.getInputStream()));
        
        
        NetworkConnection connection = null;
        assertTrue("Successfull connection", socket != null);
        try {
            Thread.sleep(sleepTO.get() * 5);
        } catch (InterruptedException e) {}
        List<Connection> connectionList = db.getConnectionList();
        for (Connection conn: connectionList) {
            if (((NetworkConnection) conn).getSocket().getRemoteSocketAddress().equals(socket.getLocalSocketAddress())) {
                connection = (NetworkConnection) conn;
                break;
            }
        }
        assertTrue("Successfull connection registration", connection != null);
        assertTrue("Successfull user creation", connection.ircTalker.get().getConnection() == connection);

        incomingConnectionListener.down.set(true);
        Globals.logger.get().log(Level.FINEST, "**Listener***********************************OK**");
        Globals.logger.get().log(Level.INFO, "Down");
        try {
            Thread.sleep(sleepTO.get() * 2);
        } catch (InterruptedException e) {}
        
        try {
            Thread.sleep(sleepTO.get() * 2);
        } catch (InterruptedException e) {}
        
        System.out.println("**Listener***********************************OK**");        
    }
}

