/*
 * 
 * PrefixIrcServerConnectionTest
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

/**
 * PrefixIrcServerConnectionTest
 * @version 0.5.3.1 2015-11-06 
 * @author  Nikolay Kirdin
 */
public class PrefixIrcServerConnectionTest extends IrcCommandTest {
	
	@Test
    public void prefixIrcServerConnectionTest() {
        System.out.println("--TestPrefixIrcServerConnection------------------");

        dropAll();
        serverInit();
        userInit(); operatorInit();
        serviceInit();        
        
        String reply;
        String servername;
        String prefix;
        String incomingPrefix;
        String ircCommand;
        String response;
        String userPassword;
        String responseCode;
        String responseMsg;
        String content;
        String mask;
        Connection connection;
        String noSuchNick = "nOsUch";

        int i;
        
        IrcCommandParser icp = new IrcCommandParser();
        // 1. Prefix is known for user, Connection for IrcServer, Prefix belongs to connection. Prefix allowed, Message allowed, Connection allowed.
        serverInit();
        userInit(); operatorInit();
        serviceInit();
        
        connection = Connection.create();
        
        icp.setRequestor(db.getIrcServer(ircServerHostname[0]));
        icp.getRequestor().setConnection(connection);
        
        icp.getRequestor().getConnection().ircTalker.set(icp.getRequestor());
        db.register(connection);
        db.getUser(userNickname[0]).setIrcServer(db.getIrcServer(ircServerHostname[0]));
        db.getUser(userNickname[0]).setConnection(icp.getRequestor().getConnection());

        ircCommand = "VERSION";
        incomingPrefix = userNickname[0];
        icp.setParsingString(":" + incomingPrefix + " " + ircCommand);
        assertTrue("#1 connection accepted", db.isRegistered(icp.getRequestor().getConnection()));
        icp.ircParse();
        prefix = Globals.thisIrcServer.get().getHostname();
        response = "351" + " " + userNickname[0] + " " + ".*";
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertTrue("#1 prefix allowed",icp.getRequestor().getNickname().equals(incomingPrefix));
        assertTrue("#1 message allowed", reply.matches(":" + prefix + " " + response));
        assertTrue("#1 connection allowed", db.isRegistered(icp.getRequestor().getConnection()));

        dropAll();

        // 2. Prefix is known for IrcServer, Connection for IrcServer, Prefix  belongs to connection. Prefix allowed, Message allowed, Connection allowed.
        serverInit();
        userInit(); operatorInit();
        serviceInit();
        
        connection = Connection.create();
        
        icp.setRequestor(db.getIrcServer(ircServerHostname[0]));
        icp.getRequestor().setConnection(connection);
        
        icp.getRequestor().getConnection().ircTalker.set(icp.getRequestor());
        db.register(connection);

        ircCommand = "VERSION";
        incomingPrefix = ircServerHostname[0];
        icp.setParsingString(":" + incomingPrefix + " " + ircCommand);
        assertTrue("#2 connection accepted", db.isRegistered(icp.getRequestor().getConnection()));
        icp.ircParse();
        prefix = Globals.thisIrcServer.get().getHostname();
//        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertTrue("#2 prefix allowed",icp.getRequestor().getNickname().equals(incomingPrefix));
        //assertTrue("#2 message allowed", reply.matches(":" + prefix + " " + response));
        assertTrue("#2 connection allowed", db.isRegistered(icp.getRequestor().getConnection()));
        dropAll();

        // 3. Prefix is known for IrcServer, Connection for IrcServer, Prefix  belongs to connection. Prefix allowed, Message allowed, Connection allowed.
        serverInit();
        userInit(); operatorInit();
        serviceInit();
        
        connection = Connection.create();
        
        icp.setRequestor(db.getIrcServer(ircServerHostname[0]));
        icp.getRequestor().setConnection(connection);
        
        icp.getRequestor().getConnection().ircTalker.set(icp.getRequestor());
        db.register(connection);
        db.getIrcServer(ircServerHostname[1]).setConnection(connection);

        ircCommand = "VERSION";
        incomingPrefix = ircServerHostname[1];
        icp.setParsingString(":" + incomingPrefix + " " + ircCommand);
        assertTrue("#3 connection accepted", db.isRegistered(icp.getRequestor().getConnection()));
        icp.ircParse();
        prefix = Globals.thisIrcServer.get().getHostname();
//        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertTrue("#3 prefix allowed",icp.getRequestor().getNickname().equals(incomingPrefix));
        //assertTrue("#3 message allowed", reply.matches(":" + prefix + " " + response));
        assertTrue("#3 connection allowed", db.isRegistered(icp.getRequestor().getConnection()));
        dropAll();

        // 4. Prefix is known for IrcServer, Connection for IrcServer, Prefix not belongs to connection. Prefix discarding, Message discarding, Connection dropping.
        serverInit();
        userInit(); operatorInit();
        serviceInit();
        
        connection = Connection.create();
        
        icp.setRequestor(db.getIrcServer(ircServerHostname[0]));
        icp.getRequestor().setConnection(connection);
        
        icp.getRequestor().getConnection().ircTalker.set(icp.getRequestor());
        db.register(connection);

        ircCommand = "VERSION";
        incomingPrefix = ircServerHostname[1];
        icp.setParsingString(":" + incomingPrefix + " " + ircCommand);
        assertTrue("#4 connection accepted", db.isRegistered(icp.getRequestor().getConnection()));
        icp.ircParse();
        prefix = Globals.thisIrcServer.get().getHostname();
//        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertFalse("#4 prefix discarding",icp.getRequestor().getNickname().equals(incomingPrefix));
        //assertFalse("#4 message allowed", reply.matches(":" + prefix + " " + response));
        assertFalse("#4 connection discarding", icp.getRequestor().getConnection().getConnectionState() == ConnectionState.OPERATIONAL);
        dropAll();

        // 5. Prefix is unknown for user, Connection for IrcServer, Prefix not belongs to connection. Prefix discarding, Message discarding, Connection dropping.
        serverInit();
        userInit(); operatorInit();
        serviceInit();
        
        connection = Connection.create();
        
        icp.setRequestor(db.getIrcServer(ircServerHostname[0]));
        icp.getRequestor().setConnection(connection);
        
        icp.getRequestor().getConnection().ircTalker.set(icp.getRequestor());
        db.register(connection);

        ircCommand = "VERSION";
        incomingPrefix = noSuchNick;
        icp.setParsingString(":" + incomingPrefix + " " + ircCommand);
        assertTrue("#5 connection accepted", db.isRegistered(icp.getRequestor().getConnection()));
        icp.ircParse();
        prefix = Globals.thisIrcServer.get().getHostname();
//        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertFalse("#5 prefix discarding",icp.getRequestor().getNickname().equals(incomingPrefix));
        //assertFalse("#5 message allowed", reply.matches(":" + prefix + " " + response));
        assertFalse("#5 connection discarding", icp.getRequestor().getConnection().getConnectionState() == ConnectionState.OPERATIONAL);
        dropAll();

        // 6. Prefix is unknown for IrcServer, Connection for IrcServer, Prefix not belongs to connection. Prefix discarding, Message discarding, Connection dropping.
        serverInit();
        userInit(); operatorInit();
        serviceInit();
        
        connection = Connection.create();
        
        icp.setRequestor(db.getIrcServer(ircServerHostname[0]));
        icp.getRequestor().setConnection(connection);
        
        icp.getRequestor().getConnection().ircTalker.set(icp.getRequestor());
        db.register(connection);

        ircCommand = "VERSION";
        incomingPrefix = "no.such.server";
        icp.setParsingString(":" + incomingPrefix + " " + ircCommand);
        assertTrue("#6 connection accepted", db.isRegistered(icp.getRequestor().getConnection()));
        icp.ircParse();
        prefix = Globals.thisIrcServer.get().getHostname();
//        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertFalse("#6 prefix discarding",icp.getRequestor().getNickname().equals(incomingPrefix));
        //assertFalse("#6 message allowed", reply.matches(":" + prefix + " " + response));
        assertFalse("#6 connection discarding", icp.getRequestor().getConnection().getConnectionState() == ConnectionState.OPERATIONAL);
        dropAll();

        // 7. Prefix is known for User, Connection for IrcServer, Prefix not belongs to connection. Prefix discarding, Message discarding, Connection dropping.
        serverInit();
        userInit(); operatorInit();
        serviceInit();
        
        connection = Connection.create();
        
        icp.setRequestor(db.getIrcServer(ircServerHostname[0]));
        icp.getRequestor().setConnection(connection);
        
        icp.getRequestor().getConnection().ircTalker.set(icp.getRequestor());
        db.register(connection);
        db.getUser(userNickname[1]).setIrcServer(db.getIrcServer(ircServerHostname[1]));

        ircCommand = "VERSION";
        incomingPrefix = userNickname[1];
        icp.setParsingString(":" + incomingPrefix + " " + ircCommand);
        assertTrue("#7 connection accepted", db.isRegistered(icp.getRequestor().getConnection()));
        icp.ircParse();
        prefix = Globals.thisIrcServer.get().getHostname();
//        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertFalse("#7 prefix discarding",icp.getRequestor().getNickname().equals(incomingPrefix));
        //assertFalse("#7 message allowed", reply.matches(":" + prefix + " " + response));
        assertFalse("#7 connection discarding", icp.getRequestor().getConnection().getConnectionState() == ConnectionState.OPERATIONAL);
        dropAll();

        // 8. Extended prefix is known for User, Connection for IrcServer, Prefix not belongs to connection. Prefix discarding, Message discarding, Connection dropping.
        serverInit();
        userInit(); operatorInit();
        serviceInit();
        
        connection = Connection.create();
        
        icp.setRequestor(db.getIrcServer(ircServerHostname[0]));
        icp.getRequestor().setConnection(connection);
        
        icp.getRequestor().getConnection().ircTalker.set(icp.getRequestor());
        db.register(connection);
        db.getUser(userNickname[1]).setIrcServer(db.getIrcServer(ircServerHostname[1]));
        db.getUser(userNickname[1]).setConnection(db.getIrcServer(ircServerHostname[1]).getConnection());

        ircCommand = "VERSION";
        incomingPrefix = incomingPrefix = userNickname[1] + "!" + userUsername[1] + "@" + ircServerHostname[1];
        icp.setParsingString(":" + incomingPrefix + " " + ircCommand);
        assertTrue("#8 connection accepted", db.isRegistered(icp.getRequestor().getConnection()));
        icp.ircParse();
        prefix = Globals.thisIrcServer.get().getHostname();
//        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertFalse("#8 prefix discarding",icp.getRequestor().getNickname().equals(incomingPrefix));
        //assertFalse("#8 message allowed", reply.matches(":" + prefix + " " + response));
        assertFalse("#8 connection discarding", icp.getRequestor().getConnection().getConnectionState() == ConnectionState.OPERATIONAL);
        dropAll();

        // 9. Extended prefix is known for User, Connection for IrcServer, Prefix belongs to connection. Prefix discarding, Message discarding, Connection dropping.
        serverInit();
        userInit(); operatorInit();
        serviceInit();
        
        connection = Connection.create();
        
        icp.setRequestor(db.getIrcServer(ircServerHostname[0]));
        icp.getRequestor().setConnection(connection);
        
        icp.getRequestor().getConnection().ircTalker.set(icp.getRequestor());
        db.register(connection);
        db.getUser(userNickname[0]).setIrcServer(db.getIrcServer(ircServerHostname[0]));
        db.getUser(userNickname[0]).setConnection(connection);

        ircCommand = "VERSION";
        incomingPrefix = incomingPrefix = userNickname[0] + "!" + userUsername[0] + "@" + ircServerHostname[0];
        icp.setParsingString(":" + incomingPrefix + " " + ircCommand);
        assertTrue("#9 connection accepted", db.isRegistered(icp.getRequestor().getConnection()));
        icp.ircParse();
        prefix = Globals.thisIrcServer.get().getHostname();
//        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertFalse("#9 prefix discarding",icp.getRequestor().getNickname().equals(incomingPrefix));
        //assertFalse("#9 message allowed", reply.matches(":" + prefix + " " + response));
        assertFalse("#9 connection discarding", icp.getRequestor().getConnection().getConnectionState() == ConnectionState.OPERATIONAL);
        dropAll();
        
        // 10. Prefix is known for Service, Connection for IrcServer, Prefix not belongs to connection. Prefix discarding, Message discarding, Connection dropping.
        serverInit();
        userInit(); operatorInit();
        serviceInit();
        
        connection = Connection.create();
        
        icp.setRequestor(db.getIrcServer(ircServerHostname[0]));
        icp.getRequestor().setConnection(connection);
        
        icp.getRequestor().getConnection().ircTalker.set(icp.getRequestor());
        db.register(connection);

        ircCommand = "VERSION";
        incomingPrefix = serviceNickname[0];
        icp.setParsingString(":" + incomingPrefix + " " + ircCommand);
        assertTrue("#10 connection accepted", db.isRegistered(icp.getRequestor().getConnection()));
        icp.ircParse();
        prefix = Globals.thisIrcServer.get().getHostname();
//        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertFalse("#10 prefix discarding",icp.getRequestor().getNickname().equals(incomingPrefix));
        //assertFalse("#10 message allowed", reply.matches(":" + prefix + " " + response));
        assertFalse("#10 connection discarding", icp.getRequestor().getConnection().getConnectionState() == ConnectionState.OPERATIONAL);
        dropAll();

        // 11. Prefix is known for Service, Connection for IrcServer, Prefix belongs to connection. Prefix allowed, Message allowed, Connection allowed.
        serverInit();
        userInit(); operatorInit();
        serviceInit();
        
        connection = Connection.create();
        
        icp.setRequestor(db.getIrcServer(ircServerHostname[0]));
        icp.getRequestor().setConnection(connection);
        
        icp.getRequestor().getConnection().ircTalker.set(icp.getRequestor());
        db.register(connection);
        db.getService(serviceNickname[0]).setIrcServer(db.getIrcServer(ircServerHostname[0]));
        db.getService(serviceNickname[0]).setConnection(icp.getRequestor().getConnection());

        ircCommand = "VERSION";
        incomingPrefix = serviceNickname[0];
        icp.setParsingString(":" + incomingPrefix + " " + ircCommand);
        assertTrue("#11 connection accepted", db.isRegistered(icp.getRequestor().getConnection()));
        icp.ircParse();
        prefix = Globals.thisIrcServer.get().getHostname();
//        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertTrue("#11 prefix allowed",icp.getRequestor().getNickname().equals(incomingPrefix));
        //assertTrue("#11 message allowed", reply.matches(":" + prefix + " " + response));
        assertTrue("#11 connection allowed", db.isRegistered(icp.getRequestor().getConnection()));
        dropAll();
       
        System.out.println("**TestPrefixIrcServerConnection**************OK**");
    }
}    

