/*
 * 
 * ServerIrcTalkerProcessorTest
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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import org.junit.Before;
import org.junit.Test;

import simpleircserver.base.Constants;
import simpleircserver.base.DB;
import simpleircserver.base.Globals;
import simpleircserver.config.ParameterInitialization;
import simpleircserver.connection.Connection;
import simpleircserver.connection.ConnectionState;
import simpleircserver.connection.NetworkConnection;
import simpleircserver.parser.IrcCommandReport;
import simpleircserver.processor.IncomingConnectionListener;
import simpleircserver.processor.IrcTalkerProcessor;
import simpleircserver.processor.NetworkConnectionProcessor;
import simpleircserver.processor.OutputQueueProcessor;
import simpleircserver.talker.user.User;
import simpleircserver.tests.IrcCommandTest;
import simpleircserver.tests.ServerTestUtils;
import simpleircserver.tests.server.ServerListenerTest.Client;

/**
 * ServerIrcTalkerProcessorTest
 * @version 0.5.3.1 2015-11-06 
 * @author  Nikolay Kirdin
 */
public class ServerIrcTalkerProcessorTest {
	
	private AtomicLong sleepTO;
	private Socket socket;
	
    public class Client {
        public Socket s;
        public Connection c;// = Connection.create();
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
        ParameterInitialization parameterInitialization;       
        parameterInitialization = new ParameterInitialization();
        parameterInitialization.configSetup();
        parameterInitialization.run();
        parameterInitialization.loggerSetup();

        Globals.logFileHandler.get().setLevel(Level.ALL);
        Globals.logger.get().setLevel(Level.ALL);
        parameterInitialization.loggerLevelSetup();
        Globals.serverDown.set(false);
        sleepTO = new AtomicLong(100);
        client = new Client[4];

    }
	

	@Test
    public void serverIrcTalkerProcessorTest() throws IOException {
        System.out.println("--IrcTalkerProcessor-----------------------------");
        //networkConnectionProcessor
        ByteArrayOutputStream clientOutputStream;
        String clientInput;
        
        IncomingConnectionListener incomingConnectionListener;
        NetworkConnectionProcessor networkConnectionProcessor;
        IrcTalkerProcessor ircTalkerProcessor;
        OutputQueueProcessor outputQueueProcessor;
              
              
        try {
            Thread.sleep(sleepTO.get() * 2);
        } catch (InterruptedException e) {}
        
        DB db = Globals.db.get();
        
        incomingConnectionListener = new IncomingConnectionListener();                      
        incomingConnectionListener.thread.set(new Thread(incomingConnectionListener));
        incomingConnectionListener.running.set(false);
        incomingConnectionListener.thread.get().start();

        
        ircTalkerProcessor = new IrcTalkerProcessor();
        ircTalkerProcessor.thread.set(new Thread(ircTalkerProcessor));
        ircTalkerProcessor.running.set(false);
        ircTalkerProcessor.thread.get().start();
        
        networkConnectionProcessor = new NetworkConnectionProcessor();
        networkConnectionProcessor.thread.set(new Thread(networkConnectionProcessor));
        networkConnectionProcessor.running.set(false);
        networkConnectionProcessor.thread.get().start();
        
//        outputQueueProcessor = new OutputQueueProcessor();
//        outputQueueProcessor.thread.set(new Thread(outputQueueProcessor));
//        outputQueueProcessor.running.set(false);
//        outputQueueProcessor.thread.get().start();

//        outputQueueProcessor.running.set(true);
        incomingConnectionListener.running.set(true);
        networkConnectionProcessor.running.set(true);
        ircTalkerProcessor.running.set(true);
        
        
        try {
            Thread.sleep(sleepTO.get() * 5);
        } catch (InterruptedException e) {}
        
        Globals.logger.get().log(Level.INFO, "Start");
        Globals.logger.get().log(Level.FINEST, "--IrcTalkerProcessor----------------------------");
        
        Globals.logger.get().log(Level.FINEST, "--IrcTalkerProcessor--Socket-closing------------");
        client[0] = new Client();
        socket = new Socket(Globals.thisIrcServer.get().getNetworkId(), Globals.serverPortNumber.get());
        client[0].s = socket;
        
        
        try {
            Thread.sleep(sleepTO.get() * 2);
        } catch (InterruptedException e) {}        
        
        assertTrue("Successfull connection", client[0].s != null);
        List<Connection> connectionList = db.getConnectionList();
        for (Connection conn: connectionList) {
            if (((NetworkConnection) conn).getSocket().getRemoteSocketAddress().equals(socket.getLocalSocketAddress())) {
                client[0].c = conn;
                break;
            }
        }
        
        assertTrue("Connection is created and in OPERATIONAL state", client[0].c.getConnectionState() == ConnectionState.OPERATIONAL);
        clientOutputStream = new ByteArrayOutputStream();
        client[0].c.bw.set(new BufferedWriter(new OutputStreamWriter(clientOutputStream)));
        client[0].c.br.set(new BufferedReader(new InputStreamReader(client[0].s.getInputStream())));
        
        LinkedHashSet<User> userSet = db.getUserSet();
        
        assertTrue("New user is created and stored in DB", userSet.contains(client[0].c.ircTalker.get()));
                    
        client[0].c.setConnectionState(ConnectionState.BROKEN);
               
        try {
            Thread.sleep(sleepTO.get() * 5);
        } catch (InterruptedException e) {}
        connectionList = db.getConnectionList();
        
        assertEquals("Connection is closed", ConnectionState.CLOSED, client[0].c.getConnectionState());
        assertFalse("Connection is removed", connectionList.contains(client[0].c));        
        assertTrue("Socket is closed",((NetworkConnection) client[0].c).getSocket().isClosed());
        
        userSet = db.getUserSet();
        assertFalse("User is removed", userSet.contains(client[0].c.ircTalker.get()));
       
        Globals.logger.get().log(Level.FINEST, "--IrcTalkerProcessor--Socket-closing---------OK--");

        
        
        Globals.logger.get().log(Level.FINEST, "--IrcTalkerProcessor--Ping-Timeout---------------");
        client[0] = new Client();
        socket = new Socket(Globals.thisIrcServer.get().getNetworkId(), Globals.serverPortNumber.get());
        client[0].s = socket;
        
        try {
            Thread.sleep(sleepTO.get() * 2);
        } catch (InterruptedException e) {}        
        
        assertTrue("Successfull connection", client[0].s != null);
        connectionList = db.getConnectionList();
        for (Connection conn: connectionList) {
            if (((NetworkConnection) conn).getSocket().getRemoteSocketAddress().equals(socket.getLocalSocketAddress())) {
                client[0].c = conn;
                break;
            }
        }
        
        assertTrue("Connection is OPERATIONAL", client[0].c.getConnectionState() == ConnectionState.OPERATIONAL);
        clientOutputStream = new ByteArrayOutputStream();
        client[0].c.bw.set(new BufferedWriter(new OutputStreamWriter(clientOutputStream)));
        client[0].c.br.set(new BufferedReader(new InputStreamReader(client[0].s.getInputStream())));
        
        userSet = db.getUserSet();
        assertTrue("New user is created and stored in DB", userSet.contains(client[0].c.ircTalker.get()));
        long oldPingPeriod = Globals.pingSendingPeriod.get();
        long newPingPeriod = 3000;
        Globals.pingSendingPeriod.set(newPingPeriod);
        try {
            Thread.sleep(newPingPeriod * 3);
        } catch (InterruptedException e) {}
        connectionList = db.getConnectionList();
        userSet = db.getUserSet();
        assertFalse("User is removed from DB", userSet.contains(client[0].c.ircTalker.get()));
        
        assertFalse("Connection is removed", connectionList.contains(client[0].c));        
        assertTrue("Socket is closed",((NetworkConnection) client[0].c).getSocket().isClosed());
       
        Globals.logger.get().log(Level.FINEST, "--IrcTalkerProcessor--Ping-Timeout-----------OK--");
        
        
        Globals.logger.get().log(Level.FINEST, "--IrcTalkerProcessor--Ping-Processing------------");
        client[0] = new Client();
        socket = new Socket(Globals.thisIrcServer.get().getNetworkId(), Globals.serverPortNumber.get());
        client[0].s = socket;
                
        try {
            Thread.sleep(sleepTO.get() * 2);
        } catch (InterruptedException e) {}        
        
        assertTrue("Successfull connection", client[0].s != null);
        connectionList = db.getConnectionList();
        for (Connection conn: connectionList) {
            if (((NetworkConnection) conn).getSocket().getRemoteSocketAddress().equals(socket.getLocalSocketAddress())) {
                client[0].c = conn;
                break;
            }
        }
        assertTrue("Connection is OPERATIONAL", client[0].c.getConnectionState() == ConnectionState.OPERATIONAL);
        clientOutputStream = new ByteArrayOutputStream();
        client[0].c.bw.set(new BufferedWriter(new OutputStreamWriter(clientOutputStream)));
        client[0].c.br.set(new BufferedReader(new InputStreamReader(client[0].s.getInputStream())));
        
        userSet = db.getUserSet();
        assertTrue("New user is created and stored in DB", userSet.contains(client[0].c.ircTalker.get()));

        newPingPeriod = 1000;
        Globals.pingSendingPeriod.set(newPingPeriod);
        int i = 0;
        int j = 0;
    	client[0].c.pongTime.set(client[0].c.pingTime.get());
        while (j < 20) {
            clientInput = clientOutputStream.toString();
            clientOutputStream.reset();

            if (clientInput != null && Pattern.matches("(?ms).*PING.*", clientInput)) {

                client[0].c.ircTalker.get().receivePong();
                i++;
            }
            try {
                Thread.sleep(newPingPeriod / 2);
            } catch (InterruptedException e) {}
            j++;
        }
        
        assertTrue("Processing pong reply", i >= 2);
        Globals.pingSendingPeriod.set(oldPingPeriod);
        Globals.logger.get().log(Level.FINEST, "--IrcTalkerProcessor--Ping-Processing--------OK--");
        incomingConnectionListener.down.set(true);
        networkConnectionProcessor.down.set(true);
        ircTalkerProcessor.down.set(true);
        Globals.logger.get().log(Level.INFO, "Down");
        Globals.logger.get().log(Level.FINEST, "**IrcTalkerProcessor*************************OK**"); 

        try {
            Thread.sleep(sleepTO.get() * 2);
        } catch (InterruptedException e) {}
        
        System.out.println("**IrcTalkerProcessor************************OK**");        
    }
}

