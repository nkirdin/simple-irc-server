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

package org.grass.simpleircserver.tests.server;

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
import java.util.concurrent.BlockingQueue;
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
import org.grass.simpleircserver.processor.NetworkConnectionProcessor;
import org.grass.simpleircserver.talker.user.User;

/**
 * ServerIrcTalkerProcessorTest
 * @version 0.5.3.1 2015-11-06 
 * @author  Nikolay Kirdin
 */
public class ServerIrcTalkerProcessorTest extends ServerBaseTest { 
	//FIXME
	//@Test
    public void serverIrcTalkerProcessorTest() throws IOException {
        System.out.println("--IrcTalkerProcessor-----------------------------");
        //networkConnectionProcessor
        if (Globals.serverSocket.get() != null) Globals.serverSocket.get().close();
        
        ParameterInitialization parameterInitialization;
        IncomingConnectionListener incomingConnectionListener;
        NetworkConnectionProcessor networkConnectionProcessor;
        IrcTalkerProcessor ircTalkerProcessor;
        
        
        parameterInitialization = new ParameterInitialization();
        parameterInitialization.run();
        
        
        try {
            Thread.sleep(sleepTO.get() * 2);
        } catch (InterruptedException e) {}
        
        DB db = Globals.db.get();
        incomingConnectionListener = new IncomingConnectionListener();
        
                
        incomingConnectionListener.thread.set(new Thread(incomingConnectionListener));
        
        ircTalkerProcessor = new IrcTalkerProcessor();
        ircTalkerProcessor.thread.set(new Thread(ircTalkerProcessor));
        ircTalkerProcessor.running.set(false);
        ircTalkerProcessor.thread.get().start();
        
        networkConnectionProcessor = new NetworkConnectionProcessor();
        networkConnectionProcessor.thread.set(new Thread(networkConnectionProcessor));
        networkConnectionProcessor.running.set(false);
        networkConnectionProcessor.thread.get().start();
        
        
        try {
            Thread.sleep(sleepTO.get() * 2);
        } catch (InterruptedException e) {}
        
        Globals.logger.get().log(Level.INFO, "Start");
        Globals.logger.get().log(Level.FINEST, "--IrcTalkerProcessor----------------------------");
        incomingConnectionListener.running.set(true);
        incomingConnectionListener.thread.get().start();
        try {
            Thread.sleep(sleepTO.get() * 2);
        } catch (InterruptedException e) {}

        Globals.logger.get().log(Level.FINEST, "--IrcTalkerProcessor--Socket-closing------------");
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
                break;
            }
        }
        
        assertTrue("Connection is created and in OPERATIONAL state", client[0].c.getConnectionState() == ConnectionState.OPERATIONAL);
        
        LinkedHashSet<User> userSet = db.getUserSet();
        
        assertTrue("New user is created and stored in DB", userSet.contains(client[0].c.ircTalker.get()));
                
        ircTalkerProcessor.running.set(true);
        networkConnectionProcessor.running.set(true);
        
        client[0].c.setConnectionState(ConnectionState.BROKEN);
        
        
        try {
            Thread.sleep(sleepTO.get() * 2);
        } catch (InterruptedException e) {}
        connectionList = db.getConnectionList();
        
        assertFalse("Connection is removed", connectionList.contains(client[0].c));        
        assertTrue("Socket is closed",((NetworkConnection) client[0].c).getSocket().isClosed());
        
        userSet = db.getUserSet();
        assertFalse("User is removed", userSet.contains(client[0].c.ircTalker.get()));
        
        Globals.logger.get().log(Level.FINEST, "--IrcTalkerProcessor--Socket-closing---------OK--");

        Globals.logger.get().log(Level.FINEST, "--IrcTalkerProcessor--Ping-Timeout---------------");
        client[0] = new Client();
        socket = new Socket(Globals.thisIrcServer.get().getNetworkId(), Globals.serverPortNumber.get());
        client[0].s = socket;
        
        client[0].bw = new BufferedWriter(new OutputStreamWriter(client[0].s.getOutputStream()));
        client[0].br = new BufferedReader(new InputStreamReader(client[0].s.getInputStream()));
        
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
        
        userSet = db.getUserSet();
        assertTrue("New user is created and stored in DB", userSet.contains(client[0].c.ircTalker.get()));
        long oldPingPeriod = Globals.pingSendingPeriod.get();
        long newPingPeriod = 10000;
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
        
        client[0].bw = new BufferedWriter(new OutputStreamWriter(client[0].s.getOutputStream()));
        client[0].br = new BufferedReader(new InputStreamReader(client[0].s.getInputStream()));
        
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
        
        userSet = db.getUserSet();
        assertTrue("New user is created and stored in DB", userSet.contains(client[0].c.ircTalker.get()));
        BlockingQueue<IrcCommandReport> reply;
        int i = 0;
        int j = 0;
        while (j < 4) {
            reply = client[0].c.ircTalker.get().getOutputQueue();
            if (reply != null && !reply.isEmpty() && reply.poll().getReport().matches(".*PING.*")) {
                client[0].c.ircTalker.get().receivePong();
                i++;
            }
            try {
                Thread.sleep(newPingPeriod);
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

