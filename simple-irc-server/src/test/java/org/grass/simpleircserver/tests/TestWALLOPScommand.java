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

public class TestWALLOPScommand extends TestIrcCommand {
    public void run() {
        System.out.println("--WALLOPS----------------------------------------");
        
        IrcCommandParser icp = new IrcCommandParser();
        
        dropAll();
        serverInit();
        serviceInit();

        IrcTalker[] requestor = new IrcTalker[3];
        String[] userNickname = {"neeck1", "neeck2", "nick3"};
        String[] userUsername = {"user1", "user2he", "user3he"};
        String[] userRealname = {"Real Name 1", "Real Name 2A", "Real Name 3A"};
        String[] userHost = {"HostName1", "HostName2", "HostName2"};
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
        String mask;

        int i;
        
        icp.setRequestor(User.create());
        ((User) icp.getRequestor()).setIrcServer(Globals.thisIrcServer.get());
        icp.getRequestor().setConnection(Connection.create());

        ircCommand = "WALLOPS";
        icp.setParsingString(ircCommand);
        prefix = Globals.thisIrcServer.get().getHostname();
        response = "451" + " " + "" + " " + ":" + "You have not registered";
        icp.ircParse();
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertTrue("Not registered", reply.equals(":" + prefix + " " + response));
        
        for (i = 0; i < requestor.length; i++) {
            icp.setRequestor(User.create());
            ((User) icp.getRequestor()).setIrcServer(Globals.thisIrcServer.get());
            icp.getRequestor().setConnection(Connection.create());

            ircCommand = "NICK";
            icp.setParsingString(ircCommand + " " + userNickname[i]);
            icp.ircParse();
            requestor[i] = icp.getRequestor();
            ((User) icp.getRequestor()).setHostname(userHost[i]);
            ((User) icp.getRequestor()).setIrcServer(ircServer[i]);
        
            ircCommand = "USER";
            icp.setParsingString(ircCommand + " " + userUsername[i] + " " + userMode[i] + " " + "*" + " " + ":" + userRealname[i]);
            icp.ircParse();
         
            icp.getRequestor().getOutputQueue().clear();
        }
        
        icp.setRequestor(requestor[0]);
        prefix = Globals.thisIrcServer.get().getHostname();
        servername = Globals.thisIrcServer.get().getHostname();
        
        //461    ERR_NEEDMOREPARAMS "<command> :Not enough parameters"

        ircCommand = "WALLOPS";
        icp.setParsingString(ircCommand);
        prefix = servername;
        response = "461" + " " + userNickname[0] + " " + ircCommand + " " + ":" + "Not enough parameters";
        icp.ircParse();
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertTrue("ERR_NEEDMOREPARAMS", reply.equals(":" + prefix + " " + response));
        
        ircCommand = "WALLOPS";
        icp.setParsingString(ircCommand + " " + ":");
        prefix = servername;
        response = "461" + " " + userNickname[0] + " " + ircCommand + " " + ":" + "Not enough parameters";
        icp.ircParse();
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertTrue("ERR_NEEDMOREPARAMS 2", reply.equals(":" + prefix + " " + response));
        
        ircCommand = "WALLOPS";
        content = "Message to all users who set the 'w' flag.";
        icp.setParsingString(ircCommand + " " + ":" + content);
        prefix = userNickname[0];
        icp.ircParse();
        assertTrue("Nothing, no such users", icp.getRequestor().getOutputQueue().isEmpty());
        
        for (i = 0; i < requestor.length; i++) {
            icp.setRequestor(requestor[i]);
            ircCommand = "MODE";
            icp.setParsingString(ircCommand + " " + userNickname[i] + " " + "+w");
            icp.ircParse();
            icp.getRequestor().getOutputQueue().clear();
        }
        
        icp.setRequestor(requestor[0]);
        
        ircCommand = "WALLOPS";
        icp.setParsingString(ircCommand + " " + ":" + content);
        response = ircCommand + " " + ":" + content;
        prefix = userNickname[0];
        icp.ircParse();
        
        icp.setRequestor(requestor[1]);
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertTrue("WALLOPS to second user", reply.equals(":" + prefix + " " + response));
        icp.setRequestor(requestor[2]);
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertTrue("WALLOPS to third user", reply.equals(":" + prefix + " " + response));
        
        System.out.println("**WALLOPS************************************OK**");
    }   
}    

