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

public class TestLUSERScommand extends TestIrcCommand {
    public void run() {
        System.out.println("--LUSERS-----------------------------------------");
        IrcCommandParser icp = new IrcCommandParser();
        
        dropAll();
        serverInit();
        serviceInit();

        IrcTalker[] requestor = new IrcTalker[3];
        String[] userNickname = {"nick1", "nick2", "nick3"};
        String[] userUsername = {"user1", "user2", "user3"};
        String[] userRealname = {"Real Name 1", "Real Name 2", "Real Name 3"};
        String[] userMode = {"0", "0", "0"};
        String[] channelName = {"#channel1", "#channel2", "#channel3", "#channel4", "#channel5"};
        String[] channelMode = {"", "+n", "+m", "+b nick2", "+be nick? nick3"};

        String reply;
        String servername;
        String prefix;
        String ircCommand;
        String response;
        String userPassword;
        String responseCode;
        String responseMsg;
        String content;

        int i;
        
        icp.setRequestor(User.create());
        ((User) icp.getRequestor()).setIrcServer(Globals.thisIrcServer.get());
        icp.getRequestor().setConnection(Connection.create());
        
        ircCommand = "LUSERS";
        icp.setParsingString(ircCommand);
        prefix = Globals.thisIrcServer.get().getHostname();
        response = "451" + " " + "" + " " + ":" + "You have not registered";
        icp.ircParse();
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertTrue("Not registered reply", reply.equals(":" + prefix + " " + response));
        
        ircCommand = "NICK";
        icp.setParsingString(ircCommand + " " + userNickname[0]);
        icp.ircParse();
        requestor[0] = icp.getRequestor();
        
        ircCommand = "USER";
        icp.setParsingString(ircCommand + " " + userUsername[0] + " " + userMode[0] + " " + "*" + " " + ":" + userRealname[0]);
        icp.ircParse();
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        icp.getRequestor().getOutputQueue().clear();
        
        ircCommand = "JOIN";
        icp.setParsingString(ircCommand + " " + channelName[0]);
        icp.ircParse();
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        icp.getRequestor().getOutputQueue().clear();
        
        ircCommand = "OPER";
        prefix = Globals.thisIrcServer.get().getHostname();
        icp.setParsingString(ircCommand + " " + "user" + " " + "test");
        //381    RPL_YOUREOPER   ":You are now an IRC operator"
        response = "381" + " " + userNickname[0] + " " + ":You are now an IRC operator";
        icp.ircParse();
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {}
        
        int numOfUsers = 0, numOfServers = 0, numOfOperators = 0, numOfServices = 0, numOfChannels = 0;
                            
        numOfServers = db.getIrcServerSet().size();    
        
        numOfServices = db.getServiceSet().size();    
        
        numOfChannels = db.getChannelSet().size();    
                    
        for (Iterator<User> userSetIterator = db.getUserSetIterator(); userSetIterator.hasNext();) {
            User user = userSetIterator.next();
            numOfUsers++;
            if (user.isOperator()) {
                numOfOperators++;
            }
        }
        
        ircCommand = "LUSERS";
        icp.setParsingString(ircCommand);
        prefix = Globals.thisIrcServer.get().getHostname();
        //251    RPL_LUSERCLIENT ":There are <integer> users and <integer> services on <integer> servers"
        icp.ircParse();
        response = "251" + " " + userNickname[0] + " " + ":There are " + String.format("%d", numOfUsers) 
                + " users and " + String.format("%d", numOfServices) + " services on " 
                + String.format("%d", numOfServers) + " servers";
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertTrue("RPL_LUSERCLIENT", reply.equals(":" + prefix + " " + response));
        //252    RPL_LUSEROP "<integer> :operator(s) online"
        response = "252" + " " + userNickname[0] + " " + String.format("%d", numOfOperators) + " " + ":" + "operator(s) online";
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertTrue("RPL_LUSEROP", reply.equals(":" + prefix + " " + response));  
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        //254    RPL_LUSERCHANNELS "<integer> :channels formed"
        response = "254" + " " + userNickname[0] + " " + String.format("%d", numOfChannels) + " " + ":" + "channels formed";
        assertTrue("RPL_LUSERCHANNELS", reply.equals(":" + prefix + " " + response));
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        //255    RPL_LUSERME ":I have <integer> clients and <integer> servers"
        response = "255" + " " + userNickname[0] + " " + ":" + "I have " + String.format("%d", numOfUsers) + " clients and " + String.format("%d", numOfServers) + " servers";
        assertTrue("RPL_LUSERME", reply.equals(":" + prefix + " " + response));
        
//       253    RPL_LUSERUNKNOWN "<integer> :unknown connection(s)"

        ircCommand = "LUSERS";
        String mask = "*.irc.example.com";
        String serverMask = "irc.example.com";
        icp.setParsingString(ircCommand + " " + mask + " " + serverMask);
        prefix = Globals.thisIrcServer.get().getHostname();
        //402    ERR_NOSUCHSERVER "<server name> :No such server"
        icp.ircParse();
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        response = "402" + " " + userNickname[0] + " " + serverMask + " " + ":" + "No such server";
        assertTrue("ERR_NOSUCHSERVER", reply.equals(":" + prefix + " " + response));
        
        ircCommand = "QUIT";
        icp.setParsingString(ircCommand);
        icp.ircParse();
        
        System.out.println("**LUSERS*************************************OK**");
    }   
}    

