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

public class TestPrefixUserConnection extends TestIrcCommand {
    public void run() {
        
        System.out.println("--TestPrefixUserConnection-----------------------");
        dropAll();
        serverInit();
        userInit(); 
        operatorInit();
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

        
        int i;
        
        IrcCommandParser icp = new IrcCommandParser();
        
        // 1. Prefix is known for user, Connection for User, Prefix belongs to connection. Prefix allowed, Message allowed, Connection allowed.
        icp.setRequestor(db.getUser(userNickname[0]));
        ircCommand = "VERSION";
        incomingPrefix = userNickname[0];
        icp.setParsingString(":" + incomingPrefix + " " + ircCommand);
        assertTrue("#1 connection accepted", db.isRegistered(icp.getRequestor().getConnection()));
        icp.ircParse();
        prefix = Globals.thisIrcServer.get().getHostname();
        response = "351" + " " + userNickname[0] + " " + ".*";
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertTrue("#1 prefix allowed", icp.getRequestor().getNickname().equals(incomingPrefix));
        assertTrue("#1 message allowed", reply.matches(":" + prefix + " " + response));
        assertTrue("#1 connection allowed", db.isRegistered(icp.getRequestor().getConnection()));
        

        // 2. Prefix is unknown for user, Connection for User, Prefix not belongs to connection. Prefix discarding, Message discarding, Connection dropping.
        
        dropAll();
        serverInit();
        userInit(); operatorInit();
        serviceInit();

        icp.setRequestor(db.getUser(userNickname[0]));
        ircCommand = "VERSION";
        String noSuchNick = "NosUcH";
        incomingPrefix = noSuchNick;
        icp.setParsingString(":" + incomingPrefix + " " + ircCommand);
        assertTrue("#2 connection accepted", db.isRegistered(icp.getRequestor().getConnection()));
        icp.ircParse();
        assertFalse("#2 prefix discarded", icp.getRequestor().getNickname().equals(incomingPrefix));
        //assertTrue("#2 message discarded", (icp.getRequestor().getOutputQueue() == null || icp.getRequestor().getOutputQueue().isEmpty()));
        assertFalse("#2 connection dropped", icp.getRequestor().getConnection().getConnectionState() == ConnectionState.OPERATIONAL);
                
        
        // 3. Prefix is known for user, Connection for User, Prefix not belongs to connection. Prefix discarding, Message discarding, Connection dropping.
        dropAll();
        serverInit();
        userInit(); operatorInit();
        serviceInit();

        icp.setRequestor(db.getUser(userNickname[0]));
                
        ircCommand = "VERSION";
        incomingPrefix = userNickname[1];
        icp.setParsingString(":" + incomingPrefix + " " + ircCommand);
        assertTrue("#3 connection accepted", db.isRegistered(icp.getRequestor().getConnection()));
        icp.ircParse();
        assertFalse("#3 prefix discarded", icp.getRequestor().getNickname().equals(incomingPrefix));
        //assertTrue("#3 message discarded", (icp.getRequestor().getOutputQueue() == null || icp.getRequestor().getOutputQueue().isEmpty()));
        assertFalse("#3 connection dropped", icp.getRequestor().getConnection().getConnectionState() == ConnectionState.OPERATIONAL);
        
       
        // 4. Prefix is unknown for IrcServer, Connection for User, Prefix not belongs to connection. Prefix discarding, Message discarding, Connection dropping.
        dropAll();
        serverInit();
        userInit(); operatorInit();
        serviceInit();

        icp.setRequestor(db.getUser(userNickname[0]));
        
        ircCommand = "VERSION";
        incomingPrefix = "no.such.server";
        icp.setParsingString(":" + incomingPrefix + " " + ircCommand);
        assertTrue("#4 connection accepted", db.isRegistered(icp.getRequestor().getConnection()));
        icp.ircParse();
        assertFalse("#4 prefix discarded", icp.getRequestor().getNickname().equals(incomingPrefix));
        //assertTrue("#4 message discarded", (icp.getRequestor().getOutputQueue() == null || icp.getRequestor().getOutputQueue().isEmpty()));
        assertFalse("#4 connection dropped", icp.getRequestor().getConnection().getConnectionState() == ConnectionState.OPERATIONAL);
        
       
        // 5. Prefix is known for IrcServer, Connection for User, Prefix not belongs to connection. Prefix discarding, Message discarding, Connection dropping.
        dropAll();
        serverInit();
        userInit(); operatorInit();
        serviceInit();

        icp.setRequestor(db.getUser(userNickname[0]));
        
        ircCommand = "VERSION";
        incomingPrefix = ircServerHostname[0];
        icp.setParsingString(":" + incomingPrefix + " " + ircCommand);
        assertTrue("#5 connection accepted", db.isRegistered(icp.getRequestor().getConnection()));
        icp.ircParse();
        assertFalse("#5 prefix discarded", icp.getRequestor().getNickname().equals(incomingPrefix));
        //assertTrue("#5 message discarded", (icp.getRequestor().getOutputQueue() == null || icp.getRequestor().getOutputQueue().isEmpty()));
        assertFalse("#5 connection dropped", icp.getRequestor().getConnection().getConnectionState() == ConnectionState.OPERATIONAL);
        
       
        // 6. Extended Prefix is unknown for User, Connection for User, Prefix not belongs to connection. Prefix discarding, Message discarding, Connection dropping.
        dropAll();
        serverInit();
        userInit(); operatorInit();
        serviceInit();

        icp.setRequestor(db.getUser(userNickname[0]));
        
        ircCommand = "VERSION";
        incomingPrefix = userNickname[1] + "!" + userUsername[1] + "@" + Globals.thisIrcServer.get().getHostname();
        icp.setParsingString(":" + incomingPrefix + " " + ircCommand);
        assertTrue("#6 connection accepted", db.isRegistered(icp.getRequestor().getConnection()));
        icp.ircParse();
        assertFalse("#6 prefix discarded", icp.getRequestor().getNickname().equals(incomingPrefix));
        //assertTrue("#6 message discarded", (icp.getRequestor().getOutputQueue() == null || icp.getRequestor().getOutputQueue().isEmpty()));
        assertFalse("#6 connection dropped", icp.getRequestor().getConnection().getConnectionState() == ConnectionState.OPERATIONAL);
        
       
        // 7. Extended Prefix is known for User, Connection for User, Prefix  not belongs to connection. Prefix discarding, Message discarding, Connection dropping.
        dropAll();
        serverInit();
        userInit(); operatorInit();
        serviceInit();
        
        icp.setRequestor(db.getUser(userNickname[0]));
        
        ircCommand = "VERSION";
        incomingPrefix = userNickname[0] + "!" + userUsername[1] + "@" + Globals.thisIrcServer.get().getHostname();
        icp.setParsingString(":" + incomingPrefix + " " + ircCommand);
        assertTrue("#7 connection accepted", db.isRegistered(icp.getRequestor().getConnection()));
        icp.ircParse();
        prefix = Globals.thisIrcServer.get().getHostname();
        response = "351" + " " + userNickname[0] + " " + ".*";
//        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertFalse("#7 prefix discarded", (icp.getRequestor().getNickname()
            + "!" + ((User) icp.getRequestor()).getUsername()
            + "@" + ((User) icp.getRequestor()).getIrcServer().getHostname()).equals(incomingPrefix));
//        assertFalse("#7 message discarded", reply.matches(":" + prefix + " " + response));
        assertFalse("#7 connection dropped", icp.getRequestor().getConnection().getConnectionState() == ConnectionState.OPERATIONAL);
       
        // 8. Extended Prefix is known for User, Connection for User, Prefix  belongs to connection. Prefix allowed, Message allowed, Connection holded.
        dropAll();
        serverInit();
        userInit(); operatorInit();
        serviceInit();
        
        icp.setRequestor(db.getUser(userNickname[0]));
        
        ircCommand = "VERSION";
        incomingPrefix = userNickname[0] + "!" + userUsername[0] + "@" + Globals.thisIrcServer.get().getHostname();
        icp.setParsingString(":" + incomingPrefix + " " + ircCommand);
        assertTrue("#8 connection accepted", db.isRegistered(icp.getRequestor().getConnection()));
        icp.ircParse();
        prefix = Globals.thisIrcServer.get().getHostname();
        response = "351" + " " + userNickname[0] + " " + ".*";
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertTrue("#8 prefix accepted", (icp.getRequestor().getNickname()
            + "!" + ((User) icp.getRequestor()).getUsername()
            + "@" + ((User) icp.getRequestor()).getIrcServer().getHostname()).equals(incomingPrefix));
        assertTrue("#8 message accepted", reply.matches(":" + prefix + " " + response));
        assertTrue("#8 connection holded", db.isRegistered(icp.getRequestor().getConnection()));
        
        // 9. Prefix is known for Service, Connection for User, Prefix not belongs to connection. Prefix discarding, Message discarding, Connection dropping.
        dropAll();
        serverInit();
        userInit(); operatorInit();
        serviceInit();

        icp.setRequestor(db.getUser(userNickname[0]));
        
        ircCommand = "VERSION";
        incomingPrefix = serviceNickname[0];
        icp.setParsingString(":" + incomingPrefix + " " + ircCommand);
        assertTrue("#9 connection accepted", db.isRegistered(icp.getRequestor().getConnection()));
        icp.ircParse();
        assertFalse("#9 prefix discarded", icp.getRequestor().getNickname().equals(incomingPrefix));
        //assertTrue("#9 message discarded", (icp.getRequestor().getOutputQueue() == null || icp.getRequestor().getOutputQueue().isEmpty()));
        assertFalse("#9 connection dropped", icp.getRequestor().getConnection().getConnectionState() == ConnectionState.OPERATIONAL);
      
        System.out.println("**TestPrefixUserConnection*******************OK**");
    }
}    

