/*
 * 
 * ServerInputStreamProcessorTest
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
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.LinkedHashSet;
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
import simpleircserver.connection.ConnectionState;
import simpleircserver.connection.NetworkConnection;
import simpleircserver.parser.IrcCommandReport;
import simpleircserver.processor.IncomingConnectionListener;
import simpleircserver.processor.InputStreamProcessor;
import simpleircserver.processor.IrcTalkerProcessor;
import simpleircserver.talker.user.User;
import simpleircserver.tests.IrcCommandTest;
import simpleircserver.tests.ServerTestUtils;
import simpleircserver.tests.server.ServerConnectionProcessorTest.Client;

/**
 * ServerInputStreamProcessorTest
 * @version 0.5.3.1 2015-11-06 
 * @author  Nikolay Kirdin
 */
public class ServerInputStreamProcessorTest { 
	
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
    public void serverInputStreamProcessorTest() throws IOException {
        System.out.println("--InputStreamProcessor---------------------------");
        //networkConnectionProcessor
        
        IncomingConnectionListener incomingConnectionListener;
        InputStreamProcessor inputStreamProcessor;        
        
        try {
            Thread.sleep(sleepTO.get() * 2);
        } catch (InterruptedException e) {}
        
        DB db = Globals.db.get();
        incomingConnectionListener = new IncomingConnectionListener();
                
        incomingConnectionListener.thread.set(new Thread(incomingConnectionListener));
        
        inputStreamProcessor = new InputStreamProcessor();
        inputStreamProcessor.thread.set(new Thread(inputStreamProcessor));
        inputStreamProcessor.running.set(true);
        inputStreamProcessor.thread.get().start();
        
        try {
            Thread.sleep(sleepTO.get() * 2);
        } catch (InterruptedException e) {}
        
        Globals.logger.get().log(Level.INFO, "Start");
        Globals.logger.get().log(Level.FINEST, "--InputStreamProcessor---------------------------");
        incomingConnectionListener.running.set(true);
        incomingConnectionListener.thread.get().start();
        try {
            Thread.sleep(sleepTO.get() * 2);
        } catch (InterruptedException e) {}

        Globals.logger.get().log(Level.FINEST, "--InputStreamProcessor-Write-to-input-stream-----");
        client[0] = new Client();
        socket = new Socket(Globals.thisIrcServer.get().getNetworkId(), Globals.serverPortNumber.get());
        client[0].s = socket;
        
        client[0].bw = new BufferedWriter(new OutputStreamWriter(client[0].s.getOutputStream()));
        client[0].br = new BufferedReader(new InputStreamReader(client[0].s.getInputStream()));
        
        try {
            Thread.sleep(sleepTO.get() * 2);
        } catch (InterruptedException e) {}        
        
        assertTrue("Successfull connection", client[0].s != null);
        List<Connection> connectionList = db.getConnectionList();
        for (Connection conn: connectionList) {
            if (((NetworkConnection) conn).getSocket().getRemoteSocketAddress().equals(socket.getLocalSocketAddress())) {
                client[0].c = conn;
                assertTrue("Connection is OPERATIONAL", conn.getConnectionState() == ConnectionState.OPERATIONAL);
                break;
            }
        }
        
        LinkedHashSet<User> userSet = db.getUserSet();
        assertTrue("New user is created and stored in DB", userSet.contains(client[0].c.ircTalker.get()));
        String readString = client[0].br.readLine();
        String testString = "Test String.";
        client[0].bw.write(testString);
        client[0].bw.newLine();
        client[0].bw.flush();
        client[0].c.avgInputPeriod.set(client[0].c.minAvgInputPeriod.get());
        
        try {
            Thread.sleep(sleepTO.get() * 2);
        } catch (InterruptedException e) {}    
        readString = client[0].c.inputQueue.get().message;
        System.out.println(readString);
        assertTrue("Successfull reading:", testString.equals(readString));      
        
        client[0].bw.close(); 
        
        Globals.logger.get().log(Level.FINEST, "Connection:" + client[0].c + " BufferedReader closed");
        client[0].c.ircTalker.get().offerToOutputQueue(new IrcCommandReport("Test String.", client[0].c.ircTalker.get(), Globals.thisIrcServer.get()));
        
        try {
            Thread.sleep(sleepTO.get() * 2);
        } catch (InterruptedException e) {}        
                
        for (Connection conn: connectionList) {
            if (((NetworkConnection) conn).getSocket().getRemoteSocketAddress().equals(socket.getLocalSocketAddress())) {
                assertTrue("Connection is BROKEN", conn.getConnectionState() == ConnectionState.BROKEN);
                break;
            }
        }
        
        Globals.logger.get().log(Level.FINEST, "--InputStreamProcessor-Write-to-input-stream-OK--");        

        incomingConnectionListener.down.set(true);
        inputStreamProcessor.down.set(true);
        Globals.logger.get().log(Level.INFO, "Down");
        Globals.logger.get().log(Level.FINEST, "**InputStreamProcessor***********************OK**"); 

        try {
            Thread.sleep(sleepTO.get() * 2);
        } catch (InterruptedException e) {}
        
        System.out.println("**InputStreamProcessor***********************OK**");        
    }
}

