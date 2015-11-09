/*
 * 
 * ServerOutputQueueProcessorTest
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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.logging.Level;

import org.grass.simpleircserver.base.DB;
import org.grass.simpleircserver.base.Globals;
import org.grass.simpleircserver.config.ParameterInitialization;
import org.grass.simpleircserver.connection.Connection;
import org.grass.simpleircserver.connection.ConnectionState;
import org.grass.simpleircserver.connection.NetworkConnection;
import org.grass.simpleircserver.parser.IrcCommandReport;
import org.grass.simpleircserver.processor.IncomingConnectionListener;
import org.grass.simpleircserver.processor.IrcTalkerProcessor;
import org.grass.simpleircserver.processor.OutputQueueProcessor;
import org.grass.simpleircserver.talker.user.User;

/**
 * ServerOutputQueueProcessorTest
 * @version 0.5.3.1 2015-11-06 
 * @author  Nikolay Kirdin
 */
public class ServerOutputQueueProcessorTest extends ServerBaseTest { 
	//FIXME
	//@Test
    public void serverOutputQueueProcessorTest() throws IOException {
        System.out.println("--OutputQueueProcessor---------------------------");
        //networkConnectionProcessor
        if (Globals.serverSocket.get() != null) Globals.serverSocket.get().close();
        
        ParameterInitialization parameterInitialization;
        IncomingConnectionListener incomingConnectionListener;
        OutputQueueProcessor outputQueueProcessor;        
        
        parameterInitialization = new ParameterInitialization();
        parameterInitialization.run();
        
        try {
            Thread.sleep(sleepTO.get() * 2);
        } catch (InterruptedException e) {}
        
        DB db = Globals.db.get();
        incomingConnectionListener = new IncomingConnectionListener();
                
        incomingConnectionListener.thread.set(new Thread(incomingConnectionListener));
        
        outputQueueProcessor = new OutputQueueProcessor();
        outputQueueProcessor.thread.set(new Thread(outputQueueProcessor));
        outputQueueProcessor.running.set(true);
        outputQueueProcessor.thread.get().start();
        
        try {
            Thread.sleep(sleepTO.get() * 2);
        } catch (InterruptedException e) {}
        
        Globals.logger.get().log(Level.INFO, "Start");
        Globals.logger.get().log(Level.FINEST, "--OutputQueueProcessor---------------------------");
        incomingConnectionListener.running.set(true);
        incomingConnectionListener.thread.get().start();
        try {
            Thread.sleep(sleepTO.get() * 2);
        } catch (InterruptedException e) {}

        Globals.logger.get().log(Level.FINEST, "--OutputQueueProcessor--Socket-closing-----------");
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
        client[0].c.ircTalker.get().offerToOutputQueue(new IrcCommandReport(testString, client[0].c.ircTalker.get(), Globals.thisIrcServer.get()));
        try {
            Thread.sleep(sleepTO.get() * 2);
        } catch (InterruptedException e) {}    
        readString = client[0].br.readLine();
        assertTrue("Successfull reading:", testString.equals(readString));      
        
        client[0].br.close(); 
        
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
        
        Globals.logger.get().log(Level.FINEST, "--OutputQueueProcessor--Socket-closing-------OK--");

        incomingConnectionListener.down.set(true);
        outputQueueProcessor.down.set(true);
        Globals.logger.get().log(Level.INFO, "Down");
        Globals.logger.get().log(Level.FINEST, "**OutputQueueProcessor************************OK**"); 

        try {
            Thread.sleep(sleepTO.get() * 2);
        } catch (InterruptedException e) {}
        
        System.out.println("**OutputQueueProcessor***********************OK**");        
    }
}

