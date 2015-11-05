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

public class TestServerOutputQueueProcessor extends TestServerBase { 
    public void run() throws IOException {
        System.out.println("--OutputQueueProcessor---------------------------");
        //networkConnectionProcessor
        if (Globals.serverSocket.get() != null) Globals.serverSocket.get().close();
        
        ParameterInitialization parameterInitialization;
        IncomingConnectionListener incomingConnectionListener;
        OutputQueueProcessor outputQueueProcessor;
        IrcTalkerProcessor ircTalkerProcessor;
        
        
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

