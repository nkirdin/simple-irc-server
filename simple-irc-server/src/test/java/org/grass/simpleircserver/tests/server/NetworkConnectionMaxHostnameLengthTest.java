/*
 * 
 * NetworkConnectionMaxHostnameLengthTest
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
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.List;
import java.util.logging.Level;

import org.grass.simpleircserver.base.DB;
import org.grass.simpleircserver.base.Globals;
import org.grass.simpleircserver.config.ParameterInitialization;
import org.grass.simpleircserver.connection.Connection;
import org.grass.simpleircserver.connection.ConnectionState;
import org.grass.simpleircserver.connection.NetworkConnection;
import org.grass.simpleircserver.processor.IncomingConnectionListener;
import org.grass.simpleircserver.processor.NetworkConnectionProcessor;

/**
 * NetworkConnectionMaxHostnameLengthTest
 * @version 0.5.3.1 2015-11-06 
 * @author  Nikolay Kirdin
 */
public class NetworkConnectionMaxHostnameLengthTest extends ServerBaseTest {
	//FIXME
	//@Test
    public void networkConnectionMaxHostnameLengthTest() throws IOException {
        
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

