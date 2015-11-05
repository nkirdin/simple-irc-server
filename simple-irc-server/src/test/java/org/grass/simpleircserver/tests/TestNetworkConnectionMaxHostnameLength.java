package org.grass.simpleircserver.tests;

import org.junit.*;
import static org.junit.Assert.*;
import java.io.*;
import java.util.*;
import java.util.logging.*;
import java.net.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import java.text.*;

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

public class TestNetworkConnectionMaxHostnameLength extends TestServerBase {
    public void run() throws IOException {
        
        System.out.println("--NetworkConnection--MaximumHostnameLength-------");

        if (Globals.serverSocket.get() != null) Globals.serverSocket.get().close();
        
        ParameterInitialization parameterInitialization;
        IncomingConnectionListener incomingConnectionListener;
        NetworkConnectionProcessor networkConnectionProcessor;
        
        parameterInitialization = new ParameterInitialization();
        parameterInitialization.run();
        
        incomingConnectionListener = new IncomingConnectionListener();
        incomingConnectionListener.thread.set(new Thread(incomingConnectionListener));
        
        networkConnectionProcessor = new NetworkConnectionProcessor();
        networkConnectionProcessor.thread.set(new Thread(networkConnectionProcessor));
        networkConnectionProcessor.running.set(false);
        
        
        
        
        try {
            Thread.sleep(sleepTO.get() * 2);
        } catch (InterruptedException e) {}
        DB db = Globals.db.get();
        Globals.logger.get().log(Level.INFO, "Start");
        Globals.logger.get().log(Level.FINEST, "--NetworkConnection--MaximumHostnameLength-------");
        
        
        incomingConnectionListener.running.set(true);
        incomingConnectionListener.thread.get().start();
        try {
            Thread.sleep(sleepTO.get() * 2);
        } catch (InterruptedException e) {}
        
        // Test record mast be prepared in /etc/hosts or in DNS Server.
        InetAddress inetAddress = InetAddress.getByAddress(new byte[]{127,1,0,104});
        
        assertTrue("Hostname length is greater than 63 symbols", inetAddress.getCanonicalHostName().length() > 63);
        InetSocketAddress inetSocketAddress = new InetSocketAddress(inetAddress, 2345); 
        socket = new Socket();
        socket.bind(inetSocketAddress);
        InetAddress localInetAddress = InetAddress.getByAddress(new byte[]{127,0,0,1});
        client[0] = new Client();
        SocketAddress localSocketAddress = new InetSocketAddress(localInetAddress, Globals.serverPortNumber.get());
        socket.connect(localSocketAddress);
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
        assertTrue("Hostname length is less than 63 symbols", client[0].c.ircTalker.get().getHostname().length() <= 63);
        ((NetworkConnection) client[0].c).setConnectionState(ConnectionState.BROKEN);
        Globals.logger.get().log(Level.FINEST, "Connection:" + client[0].c + " set:" + ((NetworkConnection) client[0].c).getConnectionState());
        for (Connection conn: connectionList) {
            if (((NetworkConnection) conn).getSocket().getRemoteSocketAddress().equals(socket.getLocalSocketAddress())) {
                assertTrue("Connection is BROKEN", conn.getConnectionState() == ConnectionState.BROKEN);
                break;
            }
        }
        networkConnectionProcessor.running.set(true);
        networkConnectionProcessor.thread.get().start();
        try {
            Thread.sleep(sleepTO.get() * 2);
        } catch (InterruptedException e) {}
        connectionList = db.getConnectionList();
        assertFalse("Connection is removed", connectionList.contains(client[0].c));        
        assertTrue("Socket is closed",((NetworkConnection) client[0].c).getSocket().isClosed());
        Globals.logger.get().log(Level.FINEST, "--NetworkConnection--MaximumHostnameLength---OK--");


        incomingConnectionListener.down.set(true);
        networkConnectionProcessor.down.set(true);
        Globals.logger.get().log(Level.INFO, "Down");
        Globals.logger.get().log(Level.FINEST, "**NetworkConnection**MaximumHostnameLength**OK**"); 

        try {
            Thread.sleep(sleepTO.get() * 2);
        } catch (InterruptedException e) {}
        
        
    }   
}    

