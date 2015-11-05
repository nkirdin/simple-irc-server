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

public class TestServerListener extends TestServerBase { 
    public void run() throws IOException {
        System.out.println("--Listener---------------------------------------");
        //incomingConnectionListener        
        if (Globals.serverSocket.get() != null) Globals.serverSocket.get().close();
        ParameterInitialization parameterInitialization;
        IncomingConnectionListener incomingConnectionListener;
        
        parameterInitialization = new ParameterInitialization();
        parameterInitialization.run();
        
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

