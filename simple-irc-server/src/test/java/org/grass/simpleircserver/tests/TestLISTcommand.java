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

public class TestLISTcommand extends TestIrcCommand {
    public void run() {
        System.out.println("--LIST-------------------------------------------");
        User requestor;
        String reply;
        String servername;
        String prefix;
        String ircCommand;
        String [] errouneous;
        String [] correctous;
        String userNickname;
        String userUsername;
        String userRealname;
        String response;
        String userMode;
        String userPassword;
        String responseCode;
        String responseMsg;

        String[] channelName = {"#channel", "#channel2", "#channel3", "#channel4"};
        int i;

        IrcCommandParser icp = new IrcCommandParser();

        dropAll();
        serverInit();
//        userInit(); operatorInit();
        serviceInit();

        icp.setRequestor(User.create());
        ((User) icp.getRequestor()).setIrcServer(Globals.thisIrcServer.get());
        icp.getRequestor().setConnection(Connection.create());

        userNickname = "nick";
        ircCommand = "USER";
        userUsername = "user";
        userRealname = "User Userson";
        userMode = "0";
        // 451    ERR_NOTREGISTERED ":You have not registered"

        ircCommand = "LIST";
        icp.setParsingString(ircCommand + " " + channelName[0]);
        prefix = Globals.thisIrcServer.get().getHostname();
        response = "451" + " " + "" + " " + ":" + "You have not registered";
        icp.ircParse();
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertTrue(reply.equals(":" + prefix + " " + response));
        
        ircCommand = "NICK";
        icp.setParsingString(ircCommand + " " + userNickname);
        icp.ircParse();
        
        ircCommand = "USER";
        icp.setParsingString(ircCommand + " " + userUsername + " " + userMode + " " + "*" + " " + ":" + userRealname);
        icp.ircParse();
        icp.getRequestor().getOutputQueue().clear();

        ircCommand = "LIST";
        icp.setParsingString(ircCommand);
        prefix = Globals.thisIrcServer.get().getHostname();
        responseCode = "323";
        responseMsg = "End of LIST";
        response = responseCode + " " + userNickname + " " + ":" + responseMsg;
        icp.ircParse();
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertTrue("RPL_LISTEND", reply.equals(":" + prefix + " " + response));
        
        ircCommand = "JOIN";
        icp.setParsingString(ircCommand + " " + channelName[0]);
        prefix = userNickname;
        response = ircCommand + " " + channelName[0];
        icp.ircParse();
        icp.getRequestor().getOutputQueue().clear();
        
        ircCommand = "LIST";
        icp.setParsingString(ircCommand);
        prefix = Globals.thisIrcServer.get().getHostname();
        responseCode = "322";
        response = responseCode + " " + userNickname + " " + channelName[0] + " " + "1" + " " + ":";
        icp.ircParse();
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertTrue("RPL_LIST", reply.equals(":" + prefix + " " + response));
        
        responseCode = "323";
        responseMsg = "End of LIST";
        response = responseCode + " " + userNickname + " " + ":" + responseMsg;
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertTrue("RPL_LISTEND", reply.equals(":" + prefix + " " + response));

        icp.setRequestor(User.create());
        ((User) icp.getRequestor()).setIrcServer(Globals.thisIrcServer.get());
        icp.getRequestor().setConnection(Connection.create());

        String userNickname2 = "john"; 
        ircCommand = "NICK";
        icp.setParsingString(ircCommand + " " + userNickname2);
        icp.ircParse();
        
        ircCommand = "USER";
        icp.setParsingString(ircCommand + " " + userUsername + " " + userMode + " " + "*" + " " + ":" + userRealname);
        icp.ircParse();
        icp.getRequestor().getOutputQueue().clear();
        
        ircCommand = "JOIN";
        icp.setParsingString(ircCommand + " " + channelName[1]);
        icp.ircParse();
        icp.getRequestor().getOutputQueue().clear();

        ircCommand = "LIST";
        icp.setParsingString(ircCommand);
        icp.ircParse();
        prefix = Globals.thisIrcServer.get().getHostname();
        responseCode = "322";
        response = responseCode + " " + userNickname2 + " " + channelName[0] + " " + "1" + " " + ":";
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertTrue("RPL_LIST", reply.equals(":" + prefix + " " + response));
        
        prefix = Globals.thisIrcServer.get().getHostname();
        responseCode = "322";
        response = responseCode + " " + userNickname2 + " " + channelName[1] + " " + "1" + " " + ":";
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertTrue("RPL_LIST", reply.equals(":" + prefix + " " + response));
        
        responseCode = "323";
        responseMsg = "End of LIST";
        response = responseCode + " " + userNickname2 + " " + ":" + responseMsg;
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertTrue("RPL_LISTEND", reply.equals(":" + prefix + " " + response));

        icp.setRequestor(db.getUser(userNickname));
        
        ircCommand = "MODE";
        String channelMode = "+p";
        icp.setParsingString(ircCommand + " " + channelName[0] + " " + channelMode);
        icp.ircParse();
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        
        icp.setRequestor(db.getUser(userNickname2));
        
        ircCommand = "LIST";
        icp.setParsingString(ircCommand);
        icp.ircParse();
        prefix = Globals.thisIrcServer.get().getHostname();
        responseCode = "322";
        response = responseCode + " " + userNickname2 + " " + channelName[1] + " " + "1" + " " + ":";
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertTrue("RPL_LIST", reply.equals(":" + prefix + " " + response));

        responseCode = "323";
        responseMsg = "End of LIST";
        response = responseCode + " " + userNickname2 + " " + ":" + responseMsg;
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertTrue("RPL_LISTEND", reply.equals(":" + prefix + " " + response));
        
        icp.setRequestor(db.getUser(userNickname));

        ircCommand = "MODE";
        userMode = "-p";
        icp.setParsingString(ircCommand + " " + channelName[0] + " " + userMode);
        icp.ircParse();
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        
        icp.setRequestor(db.getUser(userNickname2));
        
        ircCommand = "LIST";
        icp.setParsingString(ircCommand);
        icp.ircParse();
        prefix = Globals.thisIrcServer.get().getHostname();
        responseCode = "322";
        response = responseCode + " " + userNickname2 + " " + channelName[0] + " " + "1" + " " + ":";
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertTrue("RPL_LIST", reply.equals(":" + prefix + " " + response));

        responseCode = "322";
        response = responseCode + " " + userNickname2 + " " + channelName[1] + " " + "1" + " " + ":";
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertTrue("RPL_LIST", reply.equals(":" + prefix + " " + response));

        responseCode = "323";
        responseMsg = "End of LIST";
        response = responseCode + " " + userNickname2 + " " + ":" + responseMsg;
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertTrue("RPL_LISTEND", reply.equals(":" + prefix + " " + response));
        
        icp.setRequestor(db.getUser(userNickname));

        ircCommand = "MODE";
        userMode = "+s";
        icp.setParsingString(ircCommand + " " + channelName[0] + " " + userMode);
        icp.ircParse();
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        
        icp.setRequestor(db.getUser(userNickname2));
        
        ircCommand = "LIST";
        icp.setParsingString(ircCommand);
        icp.ircParse();
        prefix = Globals.thisIrcServer.get().getHostname();
        responseCode = "322";
        response = responseCode + " " + userNickname2 + " " + channelName[1] + " " + "1" + " " + ":";
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertTrue("RPL_LIST", reply.equals(":" + prefix + " " + response));
        
        responseCode = "323";
        responseMsg = "End of LIST";
        response = responseCode + " " + userNickname2 + " " + ":" + responseMsg;
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertTrue("RPL_LISTEND", reply.equals(":" + prefix + " " + response));
        
        icp.setRequestor(db.getUser(userNickname));
        
        ircCommand = "MODE";
        channelMode = "-s";
        icp.setParsingString(ircCommand + " " + channelName[0] + " " + channelMode);
        icp.ircParse();
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        
        icp.setRequestor(db.getUser(userNickname2));
        
        ircCommand = "LIST";
        icp.setParsingString(ircCommand);
        icp.ircParse();
        prefix = Globals.thisIrcServer.get().getHostname();
        responseCode = "322";
        response = responseCode + " " + userNickname2 + " " + channelName[0] + " " + "1" + " " + ":";
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertTrue("RPL_LIST", reply.equals(":" + prefix + " " + response));

        responseCode = "322";
        response = responseCode + " " + userNickname2 + " " + channelName[1] + " " + "1" + " " + ":";
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertTrue("RPL_LIST", reply.equals(":" + prefix + " " + response));

        responseCode = "323";
        responseMsg = "End of LIST";
        response = responseCode + " " + userNickname2 + " " + ":" + responseMsg;
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertTrue("RPL_LISTEND", reply.equals(":" + prefix + " " + response));

        icp.setRequestor(db.getUser(userNickname));
        
        ircCommand = "QUIT";
        icp.setParsingString(ircCommand);
        icp.ircParse();
        
        icp.setRequestor(db.getUser(userNickname2));
        
        ircCommand = "LIST";
        icp.setParsingString(ircCommand);
        icp.ircParse();
        prefix = Globals.thisIrcServer.get().getHostname();
        responseCode = "322";
        response = responseCode + " " + userNickname2 + " " + channelName[1] + " " + "1" + " " + ":";
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertTrue("RPL_LIST", reply.equals(":" + prefix + " " + response));

        responseCode = "323";
        responseMsg = "End of LIST";
        response = responseCode + " " + userNickname2 + " " + ":" + responseMsg;
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertTrue("RPL_LISTEND", reply.equals(":" + prefix + " " + response));
        
        ircCommand = "LIST";
        String serverMask = "example.com";
        icp.setParsingString(ircCommand + " " + channelName[1] + " " + serverMask);
        icp.ircParse();
        prefix = Globals.thisIrcServer.get().getHostname();
        //402    ERR_NOSUCHSERVER "<server name> :No such server"
        responseCode = "402";
        response = responseCode + " " + userNickname2 + " " + serverMask + " " + ":" + "No such server";
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertTrue("ERR_NOSUCHSERVER", reply.equals(":" + prefix + " " + response));
        
        ircCommand = "QUIT";
        icp.setParsingString(ircCommand);
        icp.ircParse();
        
        icp.setRequestor(service[0]);
        ircCommand = "LIST";
        icp.setParsingString(ircCommand + " " + channelName[0]);
        prefix = Globals.thisIrcServer.get().getHostname();
        response = "421" + " " + service[0].getNickname() + " " + ircCommand + " " + ":Unknown command";
        icp.ircParse();
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertTrue(reply.equals(":" + prefix + " " + response));
                
        System.out.println("**LIST***************************************OK**");
    }   
}    

