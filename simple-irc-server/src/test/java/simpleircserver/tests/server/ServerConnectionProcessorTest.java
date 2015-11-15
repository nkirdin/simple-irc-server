/*
 * 
 * ServerConnectionProcessorTest
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
import simpleircserver.parser.commands.QuitIrcCommand;
import simpleircserver.processor.IncomingConnectionListener;
import simpleircserver.processor.IrcTalkerProcessor;
import simpleircserver.processor.NetworkConnectionProcessor;
import simpleircserver.tests.IrcCommandTest;
import simpleircserver.tests.ServerTestUtils;
import simpleircserver.tests.server.ServerIrcTalkerProcessorTest.Client;

/**
 * ServerConnectionProcessorTest
 * @version 0.5.3.1 2015-11-06 
 * @author  Nikolay Kirdin
 */
public class ServerConnectionProcessorTest {
	
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
    public void serverConnectionProcessorTest() throws IOException {
        System.out.println("--ConnectionProcessor----------------------------");
        //networkConnectionProcessor

        IncomingConnectionListener incomingConnectionListener;
        NetworkConnectionProcessor networkConnectionProcessor;
        IrcTalkerProcessor ircTalkerProcessor;
        
        incomingConnectionListener = new IncomingConnectionListener();
        incomingConnectionListener.thread.set(new Thread(incomingConnectionListener));
        incomingConnectionListener.running.set(false);
        
        networkConnectionProcessor = new NetworkConnectionProcessor();
        networkConnectionProcessor.thread.set(new Thread(networkConnectionProcessor));
        networkConnectionProcessor.running.set(false);
        
        ircTalkerProcessor = new IrcTalkerProcessor();
        ircTalkerProcessor.thread.set(new Thread(ircTalkerProcessor));
        ircTalkerProcessor.running.set(false);
        
        try {
            Thread.sleep(sleepTO.get() * 2);
        } catch (InterruptedException e) {}
        DB db = Globals.db.get();
        Globals.logger.get().log(Level.INFO, "Start");
        Globals.logger.get().log(Level.FINEST, "--ConnectionProcessor----------------------------");
        
        
        incomingConnectionListener.running.set(true);
        incomingConnectionListener.thread.get().start();
        try {
            Thread.sleep(sleepTO.get() * 2);
        } catch (InterruptedException e) {}

        Globals.logger.get().log(Level.FINEST, "--ConnectionProcessor--Socket-closing------------");
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
        ((NetworkConnection) client[0].c).setConnectionState(ConnectionState.BROKEN);
        Globals.logger.get().log(Level.FINEST, "Connection:" + client[0].c + " set:" + ((NetworkConnection) client[0].c).getConnectionState());
        for (Connection conn: connectionList) {
            if (((NetworkConnection) conn).getSocket().getRemoteSocketAddress().equals(socket.getLocalSocketAddress())) {
                assertTrue("Connection is BROKEN", conn.getConnectionState() == ConnectionState.BROKEN);
                break;
            }
        }
        
        ircTalkerProcessor.running.set(true);
        ircTalkerProcessor.thread.get().start();
        networkConnectionProcessor.running.set(true);
        networkConnectionProcessor.thread.get().start();
        try {
            Thread.sleep(sleepTO.get() * 2);
        } catch (InterruptedException e) {}
        connectionList = db.getConnectionList();
        assertFalse("Connection is removed", connectionList.contains(client[0].c));        
        assertTrue("Socket is closed",((NetworkConnection) client[0].c).getSocket().isClosed());
        Globals.logger.get().log(Level.FINEST, "--ConnectionProcessor--Socket-closing--------OK--");
/*
        networkConnectionProcessor.running.set(false);
        try {
            Thread.sleep(sleepTO.get() * 2);
        } catch (InterruptedException e) {}     
        Globals.logger.get().log(Level.FINEST, "--ConnectionProcessor--Writer-closing------------");
        client[0] = new Client();
        socket = new Socket(Globals.thisIrcServer.get().getNetworkId(), Globals.serverPortNumber.get());
        client[0].s = socket;
        
        client[0].bw = new BufferedWriter(new OutputStreamWriter(client[0].s.getOutputStream()));
        client[0].br = new BufferedReader(new InputStreamReader(client[0].s.getInputStream()));
        try {
            Thread.sleep(sleepTO.get() * 2);
        } catch (InterruptedException e) {}                
        assertTrue("Successfull connection 2", client[0].s != null);
        connectionList = db.getConnectionList();
        for (Connection conn: connectionList) {
            if (((NetworkConnection) conn).getSocket().getRemoteSocketAddress().equals(socket.getLocalSocketAddress())) {
                client[0].c = conn;
                assertTrue("Connection 2 is OPERATIONAL", conn.getConnectionState() == ConnectionState.OPERATIONAL);
                break;
            }
            assertTrue("No OPERATIONAL connection 2 ", false);
        }
        client[0].bw.close();
        try {
            client[0].c.br.get().readLine();
        } catch (IOException e) {
            System.out.println("Connection 2: " + e);
        }
        try {
            Thread.sleep(sleepTO.get() * 2);
        } catch (InterruptedException e) {}
        connectionList = db.getConnectionList();
        System.out.println(connectionList);
        for (Connection conn: connectionList) {
            if (((NetworkConnection) conn).getSocket().getRemoteSocketAddress().equals(socket.getLocalSocketAddress())) {
                assertTrue("Connection 2 is BROKEN", conn.getConnectionState() == ConnectionState.BROKEN);
                break;
            }
            assertTrue("No BROKEN connection 2 ",false);
        }
        networkConnectionProcessor.running.set(true);
        try {
            Thread.sleep(sleepTO.get() * 2);
        } catch (InterruptedException e) {}        
        connectionList = db.getConnectionList();
        assertFalse("Connection 2 is removed", connectionList.contains(client[0].c));        
        assertTrue("Socket 2 is closed",((NetworkConnection) client[0].c).getSocket().isClosed());
        Globals.logger.get().log(Level.FINEST, "--ConnectionProcessor--Writer-closing--------OK--");
        
        networkConnectionProcessor.running.set(false);
        try {
            Thread.sleep(sleepTO.get() * 2);
        } catch (InterruptedException e) {}        
        Globals.logger.get().log(Level.FINEST, "--ConnectionProcessor--Reader-closing------------");
        client[0] = new Client();
        socket = new Socket(Globals.thisIrcServer.get().getNetworkId(), Globals.serverPortNumber.get());
        client[0].s = socket;
        
        client[0].bw = new BufferedWriter(new OutputStreamWriter(client[0].s.getOutputStream()));
        client[0].br = new BufferedReader(new InputStreamReader(client[0].s.getInputStream()));
        try {
            Thread.sleep(sleepTO.get() * 2);
        } catch (InterruptedException e) {}                
        assertTrue("Successfull connection 3", client[0].s != null);
        connectionList = db.getConnectionList();
        for (Connection conn: connectionList) {
            if (((NetworkConnection) conn).getSocket().getRemoteSocketAddress().equals(socket.getLocalSocketAddress())) {
                client[0].c = conn;
                assertTrue("Connection 3 is OPERATIONAL", conn.getConnectionState() == ConnectionState.OPERATIONAL);
                break;
            }
        }
        client[0].br.close();
        try {
            Thread.sleep(sleepTO.get() * 2);
        } catch (InterruptedException e) {}
        connectionList = db.getConnectionList();
        for (Connection conn: connectionList) {
            if (((NetworkConnection) conn).getSocket().getRemoteSocketAddress().equals(socket.getLocalSocketAddress())) {
                assertTrue("Connection 3 is BROKEN", conn.getConnectionState() == ConnectionState.BROKEN);
                break;
            }
        }
        networkConnectionProcessor.running.set(true);
        try {
            Thread.sleep(sleepTO.get() * 2);
        } catch (InterruptedException e) {}        
        connectionList = db.getConnectionList();
        assertFalse("Connection 3 is removed", connectionList.contains(client[0].c));        
        assertTrue("Socket 3 is closed",((NetworkConnection) client[0].c).getSocket().isClosed());
        Globals.logger.get().log(Level.FINEST, "--ConnectionProcessor--Reader-closing--------OK--");
*/        
        Globals.logger.get().log(Level.FINEST, "--ConnectionProcessor--QUIT-command--------------");
        client[0] = new Client();
        socket = new Socket(Globals.thisIrcServer.get().getNetworkId(), Globals.serverPortNumber.get());
        client[0].s = socket;
        
        client[0].bw = new BufferedWriter(new OutputStreamWriter(client[0].s.getOutputStream()));
        client[0].br = new BufferedReader(new InputStreamReader(client[0].s.getInputStream()));
        try {
            Thread.sleep(sleepTO.get() * 2);
        } catch (InterruptedException e) {}                
        assertTrue("Successfull connection 4", client[0].s != null);
        connectionList = db.getConnectionList();
        for (Connection conn: connectionList) {
            if (((NetworkConnection) conn).getSocket().getRemoteSocketAddress().equals(socket.getLocalSocketAddress())) {
                client[0].c = conn;
                assertTrue("Connection 4 is OPERATIONAL", conn.getConnectionState() == ConnectionState.OPERATIONAL);
                break;
            }
        }
        Globals.logger.get().log(Level.FINEST, "--ConnectionProcessor--QUIT-executing-------------");
        QuitIrcCommand quitIrcCommand = QuitIrcCommand.create(db, client[0].c.ircTalker.get(), "Closing connection with Quit");
        quitIrcCommand.run();
        try {
            Thread.sleep(sleepTO.get() * 15);
        } catch (InterruptedException e) {}
        connectionList = db.getConnectionList();
        assertFalse("Connection 4 is removed", connectionList.contains(client[0].c));   
        
        assertTrue("Socket 4 is closed",((NetworkConnection) client[0].c).getSocket().isClosed());
        Globals.logger.get().log(Level.FINEST, "--ConnectionProcessor--QUIT-command----------OK--");


        incomingConnectionListener.down.set(true);
        networkConnectionProcessor.down.set(true);
        ircTalkerProcessor.down.set(true);
        Globals.logger.get().log(Level.INFO, "Down");
        Globals.logger.get().log(Level.FINEST, "**ConnectionProcessor************************OK**"); 

        try {
            Thread.sleep(sleepTO.get() * 2);
        } catch (InterruptedException e) {}
        
        System.out.println("**ConnectionProcessor************************OK**");        
    }
}

