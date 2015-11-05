package org.grass.simpleircserver.tests;

import org.junit.*;
import static org.junit.Assert.*;
import java.io.*;
import java.util.*;
import java.util.logging.*;
import java.net.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

import org.grass.simpleircserver.*;
import org.grass.simpleircserver.base.*;
import org.grass.simpleircserver.channel.*;
import org.grass.simpleircserver.config.*;
import org.grass.simpleircserver.connection.*;
import org.grass.simpleircserver.parser.*;
import org.grass.simpleircserver.parser.commands.*;
import org.grass.simpleircserver.processor.*;
import org.grass.simpleircserver.talker.*;
import org.grass.simpleircserver.talker.server.*;
import org.grass.simpleircserver.talker.service.*;
import org.grass.simpleircserver.talker.user.*;
import org.grass.simpleircserver.tools.*;

public class TestServerIrcTalkerProcessor extends TestServerBase { 
    public void run() throws IOException {
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

