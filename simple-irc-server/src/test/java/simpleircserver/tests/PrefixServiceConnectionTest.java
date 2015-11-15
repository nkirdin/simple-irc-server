/*
 * 
 * PrefixServiceConnectionTest
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

package simpleircserver.tests;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import simpleircserver.base.Globals;
import simpleircserver.connection.Connection;
import simpleircserver.connection.ConnectionState;
import simpleircserver.parser.IrcCommandParser;
import simpleircserver.talker.service.Service;

/**
 * PrefixServiceConnectionTest
 * @version 0.5.3.1 2015-11-06 
 * @author  Nikolay Kirdin
 */
public class PrefixServiceConnectionTest extends IrcCommandTest {
	
	@Test
    public void prefixServiceConnectionTest() {
        System.out.println("--TestPrefixServiceConnection--------------------");

        dropAll();
        serverInit();
        userInit(); operatorInit();
        serviceInit();        
        
        String prefix;
        String incomingPrefix;
        String ircCommand;
        String response;
        
        IrcCommandParser icp = new IrcCommandParser();
        
        // 1. Prefix is known for Service, Connection for Service, Prefix belongs to connection. Prefix allowed, Message allowed, Connection allowed.
        
        icp.setRequestor(db.getService(serviceNickname[0]));

        ircCommand = "VERSION";
        incomingPrefix = serviceNickname[0];
        icp.setParsingString(":" + incomingPrefix + " " + ircCommand);
        assertTrue("#1 connection accepted", db.isRegistered(icp.getRequestor().getConnection()));
        icp.ircParse();
        prefix = Globals.thisIrcServer.get().getHostname();
        response = "351" + " " + userNickname[0] + " " + ".*";
//        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertTrue("#1 prefix allowed", icp.getRequestor().getNickname().equals(incomingPrefix));
        //assertTrue("#1 message allowed", reply.matches(":" + prefix + " " + response));
        assertTrue("#1 connection allowed", db.isRegistered(icp.getRequestor().getConnection()));
        dropAll();

        // 2. Prefix is unknown for Service, Connection for Service, Prefix not belongs to connection. Prefix discarding, Message discarding, Connection dropping.
        
        serverInit();
        userInit(); operatorInit();
        serviceInit();

        icp.setRequestor(db.getService(serviceNickname[0]));
        
        ircCommand = "VERSION";
        String noSuchNick = "NosUcH";
        incomingPrefix = noSuchNick;
        icp.setParsingString(":" + incomingPrefix + " " + ircCommand);
        assertTrue("#2 connection accepted", db.isRegistered(icp.getRequestor().getConnection()));
        icp.ircParse();
        assertFalse("#2 prefix discarded", icp.getRequestor().getNickname().equals(incomingPrefix));
        //assertTrue("#2 message discarded", (icp.getRequestor().getOutputQueue() == null || icp.getRequestor().getOutputQueue().isEmpty()));
        assertFalse("#2 connection dropped", icp.getRequestor().getConnection().getConnectionState() == ConnectionState.OPERATIONAL);
        dropAll();        
        
        // 3. Prefix is known for Service, Connection for Service, Prefix not belongs to connection. Prefix discarding, Message discarding, Connection dropping.
        serverInit();
        userInit(); operatorInit();
        serviceInit();

        icp.setRequestor(db.getService(serviceNickname[0]));
        
        ircCommand = "VERSION";
        incomingPrefix = serviceNickname[1];
        icp.setParsingString(":" + incomingPrefix + " " + ircCommand);
        assertTrue("#3 connection accepted", db.isRegistered(icp.getRequestor().getConnection()));
        icp.ircParse();
        assertFalse("#3 prefix discarded", icp.getRequestor().getNickname().equals(incomingPrefix));
        //assertTrue("#3 message discarded", (icp.getRequestor().getOutputQueue() == null || icp.getRequestor().getOutputQueue().isEmpty()));
        assertFalse("#3 connection dropped", icp.getRequestor().getConnection().getConnectionState() == ConnectionState.OPERATIONAL);
        dropAll();
       
        // 4. Prefix is known for User, Connection for Service, Prefix not belongs to connection. Prefix discarding, Message discarding, Connection dropping.
        serverInit();
        userInit(); operatorInit();
        serviceInit();

        icp.setRequestor(db.getService(serviceNickname[0]));
        
        ircCommand = "VERSION";
        incomingPrefix = userNickname[1];
        icp.setParsingString(":" + incomingPrefix + " " + ircCommand);
        assertTrue("#4 connection accepted", db.isRegistered(icp.getRequestor().getConnection()));
        icp.ircParse();
        assertFalse("#4 prefix discarded", icp.getRequestor().getNickname().equals(incomingPrefix));
        //assertTrue("#4 message discarded", (icp.getRequestor().getOutputQueue() == null || icp.getRequestor().getOutputQueue().isEmpty()));
        assertFalse("#4 connection dropped", icp.getRequestor().getConnection().getConnectionState() == ConnectionState.OPERATIONAL);
        dropAll();
       
        // 5. Prefix is known for IrcServer, Connection for Service, Prefix not belongs to connection. Prefix discarding, Message discarding, Connection dropping.
        serverInit();
        userInit(); operatorInit();
        serviceInit();

        icp.setRequestor(db.getService(serviceNickname[0]));
        
        ircCommand = "VERSION";
        incomingPrefix = ircServerHostname[1];
        icp.setParsingString(":" + incomingPrefix + " " + ircCommand);
        assertTrue("#5 connection accepted", db.isRegistered(icp.getRequestor().getConnection()));
        icp.ircParse();
        assertFalse("#5 prefix discarded", icp.getRequestor().getNickname().equals(incomingPrefix));
        //assertTrue("#5 message discarded", (icp.getRequestor().getOutputQueue() == null || icp.getRequestor().getOutputQueue().isEmpty()));
        assertFalse("#5 connection dropped", icp.getRequestor().getConnection().getConnectionState() == ConnectionState.OPERATIONAL);
        dropAll();
       
        // 6. Prefix is unknown for IrcServer, Connection for Service, Prefix not belongs to connection. Prefix discarding, Message discarding, Connection dropping.
        serverInit();
        userInit(); operatorInit();
        serviceInit();

        icp.setRequestor(db.getService(serviceNickname[0]));
        
        ircCommand = "VERSION";
        incomingPrefix = "no.such.server";
        icp.setParsingString(":" + incomingPrefix + " " + ircCommand);
        assertTrue("#5 connection accepted", db.isRegistered(icp.getRequestor().getConnection()));
        icp.ircParse();
        assertFalse("#5 prefix discarded", icp.getRequestor().getNickname().equals(incomingPrefix));
        //assertTrue("#5 message discarded", (icp.getRequestor().getOutputQueue() == null || icp.getRequestor().getOutputQueue().isEmpty()));
        assertFalse("#5 connection dropped", icp.getRequestor().getConnection().getConnectionState() == ConnectionState.OPERATIONAL);
        dropAll();
       
        // 7. Extended Prefix is unknown for User, Connection for Service, Prefix not belongs to connection. Prefix discarding, Message discarding, Connection dropping.
        serverInit();
        userInit(); operatorInit();
        serviceInit();

        icp.setRequestor(db.getService(serviceNickname[0]));
        
        ircCommand = "VERSION";
        incomingPrefix = userNickname[1] + "!" + userUsername[2] + "@" + ircServerHostname[2];
        icp.setParsingString(":" + incomingPrefix + " " + ircCommand);
        assertTrue("#6 connection accepted", db.isRegistered(icp.getRequestor().getConnection()));
        icp.ircParse();
        assertFalse("#6 prefix discarded", icp.getRequestor().getNickname().equals(incomingPrefix));
        //assertTrue("#6 message discarded", (icp.getRequestor().getOutputQueue() == null || icp.getRequestor().getOutputQueue().isEmpty()));
        assertFalse("#6 connection dropped", icp.getRequestor().getConnection().getConnectionState() == ConnectionState.OPERATIONAL);
        dropAll();
       
        // 7. Extended Prefix is known for User, Connection for Service, Prefix  not belongs to connection. Prefix discarding, Message discarding, Connection dropping.
        serverInit();
        userInit(); operatorInit();
        serviceInit();
        
        icp.setRequestor(db.getService(serviceNickname[0]));
        
        ircCommand = "VERSION";
        incomingPrefix = userNickname[0] + "@" + ircServerHostname[0];
        icp.setParsingString(":" + incomingPrefix + " " + ircCommand);
        assertTrue("#7 connection accepted", db.isRegistered(icp.getRequestor().getConnection()));
        icp.ircParse();
        prefix = Globals.thisIrcServer.get().getHostname();
        //response = "351" + " " + userNickname[0] + " " + ".*";
        //reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertFalse("#7 prefix discarded", (icp.getRequestor().getNickname()
            + "@" + ((Service) icp.getRequestor()).getIrcServer().getHostname()).equals(incomingPrefix));
        //assertFalse("#7 message discarded", reply.matches(":" + prefix + " " + response));
        assertFalse("#7 connection dropped", icp.getRequestor().getConnection().getConnectionState() == ConnectionState.OPERATIONAL);
        dropAll();
       
        System.out.println("**TestPrefixServiceConnection****************OK**");
    }
}    

