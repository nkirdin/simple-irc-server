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


public class TestServerConnectionProcessor extends TestServerBase { 
    public void run() throws IOException {
        System.out.println("--ConnectionProcessor----------------------------");
        //networkConnectionProcessor
        if (Globals.serverSocket.get() != null) Globals.serverSocket.get().close();
        
        ParameterInitialization parameterInitialization;
        IncomingConnectionListener incomingConnectionListener;
        NetworkConnectionProcessor networkConnectionProcessor;
        
        parameterInitialization = new ParameterInitialization();
        parameterInitialization.run();
        
        incomingConnectionListener = new IncomingConnectionListener();
        incomingConnectionListener.thread.set(new Thread(incomingConnectionListener));
        incomingConnectionListener.running.set(false);
        
        networkConnectionProcessor = new NetworkConnectionProcessor();
        networkConnectionProcessor.thread.set(new Thread(networkConnectionProcessor));
        networkConnectionProcessor.running.set(false);
        
        
        
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
            Thread.sleep(sleepTO.get() * 10);
        } catch (InterruptedException e) {}
        connectionList = db.getConnectionList();
        assertFalse("Connection 4 is removed", connectionList.contains(client[0].c));   
        
        assertTrue("Socket 4 is closed",((NetworkConnection) client[0].c).getSocket().isClosed());
        Globals.logger.get().log(Level.FINEST, "--ConnectionProcessor--QUIT-command----------OK--");


        incomingConnectionListener.down.set(true);
        networkConnectionProcessor.down.set(true);
        Globals.logger.get().log(Level.INFO, "Down");
        Globals.logger.get().log(Level.FINEST, "**ConnectionProcessor************************OK**"); 

        try {
            Thread.sleep(sleepTO.get() * 2);
        } catch (InterruptedException e) {}
        
        System.out.println("**ConnectionProcessor************************OK**");        
    }
}

