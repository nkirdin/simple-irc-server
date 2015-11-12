/*
 * 
 * PrefixTest
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

package simpleircserver.tests;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import simpleircserver.base.Globals;
import simpleircserver.connection.Connection;
import simpleircserver.parser.IrcCommandParser;
import simpleircserver.talker.IrcTalker;
import simpleircserver.talker.server.IrcServer;
import simpleircserver.talker.user.User;

/**
 * PrefixTest
 * @version 0.5.3.1 2015-11-06 
 * @author  Nikolay Kirdin
 */
public class PrefixTest extends IrcCommandTest {
	
	@Test
    public void prefixTest() {
        System.out.println("--Prefix-----------------------------------------");

        dropAll();
        serverInit();
        userInit(); 
        operatorInit();
        serviceInit();        
        
        String reply;
        String prefix;
        String incomingPrefix;
        String ircCommand;
        String response;
        Connection connection;
        IrcTalker requestor;
        
        IrcCommandParser icp = new IrcCommandParser();
        
        // 1. Prefix is known for user, Connection for User, Prefix belongs to connection. Prefix allowed, Message allowed, Connection allowed.
        
        icp.setRequestor(db.getUser(userNickname[0]));
        
        
        new Thread(icp.getRequestor().getConnection()).start();
        try {Thread.sleep(1);} catch(InterruptedException e){}

        ircCommand = "VERSION";
        incomingPrefix = userNickname[0];
        icp.setParsingString(":" + incomingPrefix + " " + ircCommand);
        assertTrue("#1 connection accepted", db.isRegistered(icp.getRequestor().getConnection()));
        icp.ircParse();
        try {Thread.sleep(100);} catch(InterruptedException e){}
        prefix = Globals.thisIrcServer.get().getHostname();
        response = "351" + " " + userNickname[0] + " " + ".*";
        System.out.println(icp.getRequestor().getNickname() + " " + icp.getRequestor().getOutputQueue());
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertTrue("#1 prefix allowed", icp.getRequestor().getNickname().equals(incomingPrefix));
        assertTrue("#1 message allowed", reply.matches(":" + prefix + " " + response));
        assertTrue("#1 connection allowed", db.isRegistered(icp.getRequestor().getConnection()));
        dropAll();

        // 2. Prefix is unknown for user, Connection for User, Prefix not belongs to connection. Prefix discarding, Message discarding, Connection dropping.
        
        serverInit();
        userInit(); 
        operatorInit();
        serviceInit();

        icp.setRequestor(db.getUser(userNickname[0]));
        
        
        new Thread(icp.getRequestor().getConnection()).start();
        try {Thread.sleep(1);} catch(InterruptedException e){}
        
        ircCommand = "VERSION";
        String noSuchNick = "NosUcH";
        incomingPrefix = noSuchNick;
        requestor = icp.getRequestor();
        icp.setParsingString(":" + incomingPrefix + " " + ircCommand);
        assertTrue("#2 connection accepted", db.isRegistered(icp.getRequestor().getConnection()));
        icp.ircParse();
        try {Thread.sleep(100);} catch(InterruptedException e){}
        System.out.println(icp.getRequestor().getNickname() + " " + icp.getRequestor().getOutputQueue());
        assertFalse("#2 prefix discarded", icp.getRequestor().getNickname().equals(incomingPrefix));
        assertTrue("#2 message discarded", (requestor.getOutputQueue() == null || requestor.getOutputQueue().isEmpty()));
        //assertFalse("#2 connection dropped", db.isRegistered(icp.getRequestor().getConnection()));
        dropAll();        
        
        // 3. Prefix is known for user, Connection for User, Prefix not belongs to connection. Prefix discarding, Message discarding, Connection dropping.
        serverInit();
        userInit(); 
        operatorInit();
        serviceInit();

        icp.setRequestor(db.getUser(userNickname[0]));
        
        
        new Thread(icp.getRequestor().getConnection()).start();
        try {Thread.sleep(1);} catch(InterruptedException e){}
        
        ircCommand = "VERSION";
        incomingPrefix = userNickname[1];
        icp.setParsingString(":" + incomingPrefix + " " + ircCommand);
        requestor = icp.getRequestor();
        assertTrue("#3 connection accepted", db.isRegistered(icp.getRequestor().getConnection()));
        icp.ircParse();
        System.out.println(icp.getRequestor().getNickname() + " " + icp.getRequestor().getOutputQueue());
        try {Thread.sleep(100);} catch(InterruptedException e){}
        assertFalse("#3 prefix discarded", icp.getRequestor().getNickname().equals(incomingPrefix));
        assertTrue("#3 message discarded", (requestor.getOutputQueue() == null || requestor.getOutputQueue().isEmpty()));
        //assertFalse("#3 connection dropped", db.isRegistered(icp.getRequestor().getConnection()));
        dropAll();
       
        // 4. Prefix is unknown for IrcServer, Connection for User, Prefix not belongs to connection. Prefix discarding, Message discarding, Connection dropping.
        serverInit();
        userInit(); 
        operatorInit();
        serviceInit();

        icp.setRequestor(db.getUser(userNickname[0]));
        
        
        new Thread(icp.getRequestor().getConnection()).start();
        try {Thread.sleep(1);} catch(InterruptedException e){}
        
        ircCommand = "VERSION";
        incomingPrefix = "no.such.server";
        icp.setParsingString(":" + incomingPrefix + " " + ircCommand);
        requestor = icp.getRequestor();
        assertTrue("#4 connection accepted", db.isRegistered(icp.getRequestor().getConnection()));
        icp.ircParse();
        System.out.println(icp.getRequestor().getNickname() + " " + icp.getRequestor().getOutputQueue());
        try {Thread.sleep(100);} catch(InterruptedException e){}
        assertFalse("#4 prefix discarded", icp.getRequestor().getNickname().equals(incomingPrefix));
        assertTrue("#4 message discarded", (requestor.getOutputQueue() == null || requestor.getOutputQueue().isEmpty()));
        //assertFalse("#4 connection dropped", db.isRegistered(icp.getRequestor().getConnection()));
        dropAll();
       
        // 5. Prefix is known for IrcServer, Connection for User, Prefix not belongs to connection. Prefix discarding, Message discarding, Connection dropping.
        serverInit();
        userInit(); 
        operatorInit();
        serviceInit();

        icp.setRequestor(db.getUser(userNickname[0]));
        
        
        new Thread(icp.getRequestor().getConnection()).start();
        try {Thread.sleep(1);} catch(InterruptedException e){}
        
        ircCommand = "VERSION";
        incomingPrefix = ircServerHostname[0];
        icp.setParsingString(":" + incomingPrefix + " " + ircCommand);
        requestor = icp.getRequestor();
        assertTrue("#5 connection accepted", db.isRegistered(icp.getRequestor().getConnection()));
        icp.ircParse();
        System.out.println(icp.getRequestor().getNickname() + " " + icp.getRequestor().getOutputQueue());
        try {Thread.sleep(200);} catch(InterruptedException e){}
        assertFalse("#5 prefix discarded", icp.getRequestor().getNickname().equals(incomingPrefix));
        assertTrue("#5 message discarded", (requestor.getOutputQueue() == null || requestor.getOutputQueue().isEmpty()));
        //assertFalse("#5 connection dropped", db.isRegistered(icp.getRequestor().getConnection()));
        dropAll();
       
        // 6. Extended Prefix is unknown for User, Connection for User, Prefix not belongs to connection. Prefix discarding, Message discarding, Connection dropping.
        serverInit();
        userInit(); 
        operatorInit();
        serviceInit();

        icp.setRequestor(db.getUser(userNickname[0]));
        
        
        new Thread(icp.getRequestor().getConnection()).start();
        try {Thread.sleep(1);} catch(InterruptedException e){}
        
        ircCommand = "VERSION";
        incomingPrefix = userNickname[1] + "!" + userUsername[1] + "@" + Globals.thisIrcServer.get().getHostname();
        icp.setParsingString(":" + incomingPrefix + " " + ircCommand);
        requestor = icp.getRequestor();
        assertTrue("#6 connection accepted", db.isRegistered(icp.getRequestor().getConnection()));
        icp.ircParse();
        System.out.println(icp.getRequestor().getNickname() + " " + icp.getRequestor().getOutputQueue());
        try {Thread.sleep(100);} catch(InterruptedException e){}
        assertFalse("#6 prefix discarded", icp.getRequestor().getNickname().equals(incomingPrefix));
        assertTrue("#6 message discarded", (requestor.getOutputQueue() == null || requestor.getOutputQueue().isEmpty()));
        //assertFalse("#6 connection dropped", db.isRegistered(icp.getRequestor().getConnection()));
        dropAll();
       
        // 7. Extended Prefix is known for User, Connection for User, Prefix  not belongs to connection. Prefix discarding, Message discarding, Connection dropping.
        serverInit();
        userInit(); 
        operatorInit();
        serviceInit();
        
        icp.setRequestor(db.getUser(userNickname[0]));
        
        
        new Thread(icp.getRequestor().getConnection()).start();
        try {Thread.sleep(1);} catch(InterruptedException e){}
        
        ircCommand = "VERSION";
        incomingPrefix = userNickname[0] + "!" + userUsername[1] + "@" + Globals.thisIrcServer.get().getHostname();
        System.out.println(userNickname[0] + "!" + userUsername[1] + "@" + Globals.thisIrcServer.get().getHostname());
        icp.setParsingString(":" + incomingPrefix + " " + ircCommand);
        assertTrue("#7 connection accepted", db.isRegistered(icp.getRequestor().getConnection()));
        requestor = icp.getRequestor();
        icp.ircParse();
        try {Thread.sleep(100);} catch(InterruptedException e){}    
        assertFalse("#7 prefix discarded", icp.getRequestor().getNickname().equals(incomingPrefix));
        assertTrue("#7 message discarded", (requestor.getOutputQueue() == null || requestor.getOutputQueue().isEmpty()));
        /*
         * FIXME
        prefix = Globals.thisIrcServer.get().getHostname();
        response = "351" + " " + userNickname[0] + " " + ".*";
        reply = requestor.getOutputQueue().poll().getReport();
        System.out.println( icp.getRequestor().getNickname()
            + "!" + ((User) icp.getRequestor()).getUsername()
            + "@" + ((User) icp.getRequestor()).getIrcServer().getHostname());
        assertFalse("#7 prefix discarded", (icp.getRequestor().getNickname()
            + "!" + ((User) icp.getRequestor()).getUsername()
            + "@" + ((User) icp.getRequestor()).getIrcServer().getHostname()).equals(incomingPrefix));
        assertFalse("#7 message discarded", reply.matches(":" + prefix + " " + response));
        //assertFalse("#7 connection dropped", db.isRegistered(icp.getRequestor().getConnection()));
         * 
         */
        dropAll();
       
        // 8. Extended Prefix is known for User, Connection for User, Prefix  belongs to connection. Prefix allowed, Message allowed, Connection holded.
        serverInit();
        userInit(); 
        operatorInit();
        serviceInit();
        
        icp.setRequestor(db.getUser(userNickname[0]));
        
        
        new Thread(icp.getRequestor().getConnection()).start();
        try {Thread.sleep(1);} catch(InterruptedException e){}
        
        ircCommand = "VERSION";
        incomingPrefix = userNickname[0] + "!" + userUsername[0] + "@" + Globals.thisIrcServer.get().getHostname();
        System.out.println(userNickname[0] + "!" + userUsername[0] + "@" + Globals.thisIrcServer.get().getHostname());
        icp.setParsingString(":" + incomingPrefix + " " + ircCommand);
        assertTrue("#8 connection accepted", db.isRegistered(icp.getRequestor().getConnection()));
        icp.ircParse();
        try {Thread.sleep(100);} catch(InterruptedException e){}    
        prefix = Globals.thisIrcServer.get().getHostname();
        response = "351" + " " + userNickname[0] + " " + ".*";
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        System.out.println( icp.getRequestor().getNickname()
            + "!" + ((User) icp.getRequestor()).getUsername()
            + "@" + ((User) icp.getRequestor()).getIrcServer().getHostname());
        assertTrue("#8 prefix accepted", (icp.getRequestor().getNickname()
            + "!" + ((User) icp.getRequestor()).getUsername()
            + "@" + ((User) icp.getRequestor()).getIrcServer().getHostname()).equals(incomingPrefix));
        assertTrue("#8 message accepted", reply.matches(":" + prefix + " " + response));
        assertTrue("#8 connection holded", db.isRegistered(icp.getRequestor().getConnection()));
        dropAll();
        
        // 9. Prefix is known for user, Connection for IrcServer, Prefix belongs to connection. Prefix allowed, Message allowed, Connection allowed.
        serverInit();
        userInit(); 
        operatorInit();
        serviceInit();
        
        connection = Connection.create();
        icp.setRequestor(db.getIrcServer(ircServerHostname[0]));
        icp.getRequestor().setConnection(connection);
        
        
        icp.getRequestor().getConnection().ircTalker.set(icp.getRequestor());
        db.register(connection);
        db.getUser(userNickname[0]).setIrcServer(db.getIrcServer(ircServerHostname[0]));
        System.out.println(userNickname[0] +" " + db.getUser(userNickname[0]).getConnection());
        db.getUser(userNickname[0]).setConnection(icp.getRequestor().getConnection());
        System.out.println(userNickname[0] +" " + db.getUser(userNickname[0]).getConnection());
        System.out.println(icp.getRequestor() + " " + (icp.getRequestor() instanceof IrcServer));
        new Thread(icp.getRequestor().getConnection()).start();
        try {Thread.sleep(1);} catch(InterruptedException e){}

        ircCommand = "VERSION";
        incomingPrefix = userNickname[0];
        icp.setParsingString(":" + incomingPrefix + " " + ircCommand);
        assertTrue("#9 connection accepted", db.isRegistered(icp.getRequestor().getConnection()));
        icp.ircParse();
        try {Thread.sleep(100);} catch(InterruptedException e){}
        prefix = Globals.thisIrcServer.get().getHostname();
        response = "351" + " " + userNickname[0] + " " + ".*";
        System.out.println(icp.getRequestor().getNickname() + " " + icp.getRequestor().getOutputQueue());
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertTrue("#9 prefix allowed",icp.getRequestor().getNickname().equals(incomingPrefix));
        assertTrue("#9 message allowed", reply.matches(":" + prefix + " " + response));
        assertTrue("#9 connection allowed", db.isRegistered(icp.getRequestor().getConnection()));
        try {Thread.sleep(100);} catch(InterruptedException e){}
        dropAll();

        // 10. Prefix is known for IrcServer, Connection for IrcServer, Prefix  belongs to connection. Prefix allowed, Message allowed, Connection allowed.
        serverInit();
        userInit(); 
        operatorInit();
        serviceInit();
        
        connection = Connection.create();
        icp.setRequestor(db.getIrcServer(ircServerHostname[0]));
        icp.getRequestor().setConnection(connection);
        
        
        icp.getRequestor().getConnection().ircTalker.set(icp.getRequestor());
        db.register(connection);
        System.out.println(ircServerHostname[0] +" " + db.getIrcServer(ircServerHostname[0]).getConnection());
        System.out.println(icp.getRequestor() + " " + icp.getRequestor().getConnection());
        new Thread(icp.getRequestor().getConnection()).start();
        try {Thread.sleep(1);} catch(InterruptedException e){}

        ircCommand = "VERSION";
        incomingPrefix = ircServerHostname[0];
        icp.setParsingString(":" + incomingPrefix + " " + ircCommand);
        assertTrue("#10 connection accepted", db.isRegistered(icp.getRequestor().getConnection()));
        icp.ircParse();
        try {Thread.sleep(100);} catch(InterruptedException e){}
        prefix = Globals.thisIrcServer.get().getHostname();
        System.out.println(icp.getRequestor().getNickname() + " " + icp.getRequestor().getOutputQueue());
//        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertTrue("#10 prefix allowed",icp.getRequestor().getNickname().equals(incomingPrefix));
        //assertTrue("#10 message allowed", reply.matches(":" + prefix + " " + response));
        assertTrue("#10 connection allowed", db.isRegistered(icp.getRequestor().getConnection()));
        try {Thread.sleep(100);} catch(InterruptedException e){}
        dropAll();

        // 11. Prefix is known for IrcServer, Connection for IrcServer, Prefix  belongs to connection. Prefix allowed, Message allowed, Connection allowed.
        serverInit();
        userInit(); 
        operatorInit();
        serviceInit();
        
        connection = Connection.create();
        icp.setRequestor(db.getIrcServer(ircServerHostname[0]));
        icp.getRequestor().setConnection(connection);
        
        
        icp.getRequestor().getConnection().ircTalker.set(icp.getRequestor());
        db.register(connection);
        db.getIrcServer(ircServerHostname[1]).setConnection(connection);
        System.out.println(ircServerHostname[1] +" " + db.getIrcServer(ircServerHostname[1]).getConnection());
        System.out.println(icp.getRequestor() + " " + icp.getRequestor().getConnection());
        new Thread(icp.getRequestor().getConnection()).start();
        try {Thread.sleep(1);} catch(InterruptedException e){}

        ircCommand = "VERSION";
        incomingPrefix = ircServerHostname[1];
        icp.setParsingString(":" + incomingPrefix + " " + ircCommand);
        assertTrue("#11 connection accepted", db.isRegistered(icp.getRequestor().getConnection()));
        icp.ircParse();
        try {Thread.sleep(100);} catch(InterruptedException e){}
        prefix = Globals.thisIrcServer.get().getHostname();
        System.out.println(icp.getRequestor().getNickname() + " " + icp.getRequestor().getOutputQueue());
//        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertTrue("#11 prefix allowed",icp.getRequestor().getNickname().equals(incomingPrefix));
        //assertTrue("#11 message allowed", reply.matches(":" + prefix + " " + response));
        assertTrue("#11 connection allowed", db.isRegistered(icp.getRequestor().getConnection()));
        try {Thread.sleep(100);} catch(InterruptedException e){}
        dropAll();

        // 12. Prefix is known for IrcServer, Connection for IrcServer, Prefix not belongs to connection. Prefix discarding, Message discarding, Connection dropping.
        serverInit();
        userInit(); 
        operatorInit();
        serviceInit();
        
        connection = Connection.create();
        icp.setRequestor(db.getIrcServer(ircServerHostname[0]));
        icp.getRequestor().setConnection(connection);
        
        
        icp.getRequestor().getConnection().ircTalker.set(icp.getRequestor());
        db.register(connection);
        System.out.println(ircServerHostname[1] +" " + db.getIrcServer(ircServerHostname[1]).getConnection());
        System.out.println(icp.getRequestor() + " " + icp.getRequestor().getConnection());
        new Thread(icp.getRequestor().getConnection()).start();
        try {Thread.sleep(1);} catch(InterruptedException e){}

        ircCommand = "VERSION";
        incomingPrefix = ircServerHostname[1];
        icp.setParsingString(":" + incomingPrefix + " " + ircCommand);
        assertTrue("#12 connection accepted", db.isRegistered(icp.getRequestor().getConnection()));
        icp.ircParse();
        try {Thread.sleep(100);} catch(InterruptedException e){}
        prefix = Globals.thisIrcServer.get().getHostname();
        System.out.println(icp.getRequestor().getNickname() + " " + icp.getRequestor().getOutputQueue());
//        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertFalse("#12 prefix discarding",icp.getRequestor().getNickname().equals(incomingPrefix));
        assertFalse("#12 prefix discarded", icp.getRequestor().getNickname().equals(incomingPrefix));
        assertTrue("#12 message discarded", (icp.getRequestor().getOutputQueue() == null || icp.getRequestor().getOutputQueue().isEmpty()));
        try {Thread.sleep(100);} catch(InterruptedException e){}
        dropAll();

        // 13. Prefix is unknown for user, Connection for IrcServer, Prefix not belongs to connection. Prefix discarding, Message discarding, Connection dropping.
        serverInit();
        userInit(); 
        operatorInit();
        serviceInit();
        
        connection = Connection.create();
        icp.setRequestor(db.getIrcServer(ircServerHostname[0]));
        icp.getRequestor().setConnection(connection);
        
        
        icp.getRequestor().getConnection().ircTalker.set(icp.getRequestor());
        db.register(connection);
        System.out.println(icp.getRequestor() + " " + icp.getRequestor().getConnection());
        new Thread(icp.getRequestor().getConnection()).start();
        try {Thread.sleep(1);} catch(InterruptedException e){}

        ircCommand = "VERSION";
        incomingPrefix = noSuchNick;
        icp.setParsingString(":" + incomingPrefix + " " + ircCommand);
        assertTrue("#13 connection accepted", db.isRegistered(icp.getRequestor().getConnection()));
        icp.ircParse();
        try {Thread.sleep(100);} catch(InterruptedException e){}
        prefix = Globals.thisIrcServer.get().getHostname();
        System.out.println(icp.getRequestor().getNickname() + " " + icp.getRequestor().getOutputQueue());
//        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertFalse("#13 prefix discarded", icp.getRequestor().getNickname().equals(incomingPrefix));
        assertTrue("#13 message discarded", (icp.getRequestor().getOutputQueue() == null || icp.getRequestor().getOutputQueue().isEmpty()));
        try {Thread.sleep(100);} catch(InterruptedException e){}
        dropAll();

        // 14. Prefix is unknown for IrcServer, Connection for IrcServer, Prefix not belongs to connection. Prefix discarding, Message discarding, Connection dropping.
        serverInit();
        userInit(); 
        operatorInit();
        serviceInit();
        
        connection = Connection.create();
        icp.setRequestor(db.getIrcServer(ircServerHostname[0]));
        icp.getRequestor().setConnection(connection);
        
        
        icp.getRequestor().getConnection().ircTalker.set(icp.getRequestor());
        db.register(connection);
        System.out.println(icp.getRequestor() + " " + icp.getRequestor().getConnection());
        new Thread(icp.getRequestor().getConnection()).start();
        try {Thread.sleep(1);} catch(InterruptedException e){}

        ircCommand = "VERSION";
        incomingPrefix = "no.such.server";
        icp.setParsingString(":" + incomingPrefix + " " + ircCommand);
        assertTrue("#14 connection accepted", db.isRegistered(icp.getRequestor().getConnection()));
        icp.ircParse();
        try {Thread.sleep(100);} catch(InterruptedException e){}
        prefix = Globals.thisIrcServer.get().getHostname();
        System.out.println(icp.getRequestor().getNickname() + " " + icp.getRequestor().getOutputQueue());
//        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertFalse("#14 prefix discarding",icp.getRequestor().getNickname().equals(incomingPrefix));
        assertTrue("#14 message discarded", (icp.getRequestor().getOutputQueue() == null || icp.getRequestor().getOutputQueue().isEmpty()));
        try {Thread.sleep(100);} catch(InterruptedException e){}
        dropAll();

        // 15. Prefix is known for User, Connection for IrcServer, Prefix not belongs to connection. Prefix discarding, Message discarding, Connection dropping.
        serverInit();
        userInit(); 
        operatorInit();
        serviceInit();
        
        connection = Connection.create();
        icp.setRequestor(db.getIrcServer(ircServerHostname[0]));
        icp.getRequestor().setConnection(connection);
        
        
        icp.getRequestor().getConnection().ircTalker.set(icp.getRequestor());
        db.register(connection);
        db.getUser(userNickname[1]).setIrcServer(db.getIrcServer(ircServerHostname[1]));
        System.out.println(userNickname[1] +" " + db.getUser(userNickname[1]).getConnection());        
        System.out.println(icp.getRequestor() + " " + icp.getRequestor().getConnection());
        new Thread(icp.getRequestor().getConnection()).start();
        try {Thread.sleep(1);} catch(InterruptedException e){}

        ircCommand = "VERSION";
        incomingPrefix = userNickname[1];
        icp.setParsingString(":" + incomingPrefix + " " + ircCommand);
        assertTrue("#15 connection accepted", db.isRegistered(icp.getRequestor().getConnection()));
        icp.ircParse();
        try {Thread.sleep(100);} catch(InterruptedException e){}
        prefix = Globals.thisIrcServer.get().getHostname();
        System.out.println(icp.getRequestor().getNickname() + " " + icp.getRequestor().getOutputQueue());
//        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertFalse("#15 prefix discarding",icp.getRequestor().getNickname().equals(incomingPrefix));
        assertTrue("#15 message discarded", (icp.getRequestor().getOutputQueue() == null || icp.getRequestor().getOutputQueue().isEmpty()));
        try {Thread.sleep(100);} catch(InterruptedException e){}
        dropAll();

        // 16. Extended prefix is known for User, Connection for IrcServer, Prefix not belongs to connection. Prefix discarding, Message discarding, Connection dropping.
        serverInit();
        userInit(); 
        operatorInit();
        serviceInit();
        
        connection = Connection.create();
        icp.setRequestor(db.getIrcServer(ircServerHostname[0]));
        icp.getRequestor().setConnection(connection);
        
        
        icp.getRequestor().getConnection().ircTalker.set(icp.getRequestor());
        db.register(connection);
        db.getUser(userNickname[1]).setIrcServer(db.getIrcServer(ircServerHostname[1]));
        db.getUser(userNickname[1]).setConnection(db.getIrcServer(ircServerHostname[1]).getConnection());
        System.out.println(userNickname[1] +" " + db.getUser(userNickname[1]).getConnection());        
        System.out.println(icp.getRequestor() + " " + icp.getRequestor().getConnection());
        new Thread(icp.getRequestor().getConnection()).start();
        try {Thread.sleep(1);} catch(InterruptedException e){}

        ircCommand = "VERSION";
        incomingPrefix = userNickname[1] + "!" + userUsername[1] + "@" + ircServerHostname[1];
        icp.setParsingString(":" + incomingPrefix + " " + ircCommand);
        assertTrue("#16 connection accepted", db.isRegistered(icp.getRequestor().getConnection()));
        icp.ircParse();
        try {Thread.sleep(100);} catch(InterruptedException e){}
        prefix = Globals.thisIrcServer.get().getHostname();
        System.out.println(icp.getRequestor().getNickname() + " " + icp.getRequestor().getOutputQueue());
//        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertFalse("#16 prefix discarding",icp.getRequestor().getNickname().equals(incomingPrefix));
        assertTrue("#16 message discarded", (icp.getRequestor().getOutputQueue() == null || icp.getRequestor().getOutputQueue().isEmpty()));
        try {Thread.sleep(100);} catch(InterruptedException e){}
        dropAll();

        // 17. Extended prefix is known for User, Connection for IrcServer, Prefix belongs to connection. Prefix discarding, Message discarding, Connection dropping.
        serverInit();
        userInit(); 
        operatorInit();
        serviceInit();
        
        connection = Connection.create();
        icp.setRequestor(db.getIrcServer(ircServerHostname[0]));
        icp.getRequestor().setConnection(connection);
        
        
        icp.getRequestor().getConnection().ircTalker.set(icp.getRequestor());
        db.register(connection);
        db.getUser(userNickname[0]).setIrcServer(db.getIrcServer(ircServerHostname[0]));
        db.getUser(userNickname[0]).setConnection(connection);
        System.out.println(userNickname[0] +" " + db.getUser(userNickname[0]).getConnection());        
        System.out.println(icp.getRequestor() + " " + icp.getRequestor().getConnection());
        new Thread(icp.getRequestor().getConnection()).start();
        try {Thread.sleep(1);} catch(InterruptedException e){}

        ircCommand = "VERSION";
        incomingPrefix = userNickname[0] + "!" + userUsername[0] + "@" + ircServerHostname[0];
        icp.setParsingString(":" + incomingPrefix + " " + ircCommand);
        assertTrue("#17 connection accepted", db.isRegistered(icp.getRequestor().getConnection()));
        icp.ircParse();
        try {Thread.sleep(100);} catch(InterruptedException e){}
        prefix = Globals.thisIrcServer.get().getHostname();
        System.out.println(icp.getRequestor().getNickname() + " " + icp.getRequestor().getOutputQueue());
//        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertFalse("#17 prefix discarding",icp.getRequestor().getNickname().equals(incomingPrefix));
        assertTrue("#17 message discarded", (icp.getRequestor().getOutputQueue() == null || icp.getRequestor().getOutputQueue().isEmpty()));
        try {Thread.sleep(100);} catch(InterruptedException e){}
        dropAll();

       
        System.out.println("**Prefix*************************************OK**");
    }
}    

