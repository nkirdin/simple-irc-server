/*
 * 
 * ServerListenerTest
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

import java.io.IOException;
import java.net.Socket;
import java.util.List;
import java.util.logging.Level;

import org.grass.simpleircserver.base.DB;
import org.grass.simpleircserver.base.Globals;
import org.grass.simpleircserver.config.ParameterInitialization;
import org.grass.simpleircserver.connection.Connection;
import org.grass.simpleircserver.connection.NetworkConnection;
import org.grass.simpleircserver.processor.IncomingConnectionListener;

/**
 * ServerListenerTest
 * @version 0.5.3.1 2015-11-06 
 * @author  Nikolay Kirdin
 */
public class ServerListenerTest extends ServerBaseTest {
	//FIXME
	//@Test
    public void serverListenerTest() throws IOException {
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

