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

public class TestTOPICcommand extends TestIrcCommand {
    public void run() {
        System.out.println("--TOPIC------------------------------------------");
        
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
        String [] errouneous;
        String [] correctous;

        int i;

        IrcCommandParser icp = new IrcCommandParser();
        
        dropAll();
        serverInit();
        userInit(); operatorInit();
        serviceInit();

        icp.setRequestor(User.create());
        ((User) icp.getRequestor()).setIrcServer(Globals.thisIrcServer.get());
        icp.getRequestor().setConnection(Connection.create());

        // 451    ERR_NOTREGISTERED ":You have not registered"

        ircCommand = "TOPIC";
        icp.setParsingString(ircCommand + " " + channelName[0]);
        prefix = Globals.thisIrcServer.get().getHostname();
        response = "451" + " " + "" + " " + ":" + "You have not registered";
        icp.ircParse();
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertTrue(reply.equals(":" + prefix + " " + response));
        
        i = 0;
                
        icp.setRequestor(db.getUser(userNickname[0]));
        ircCommand = "TOPIC";
        icp.setParsingString(ircCommand);
        prefix = Globals.thisIrcServer.get().getHostname();
        response = "461" + " " + userNickname[0] + " " + ircCommand + " " + ":" + "Not enough parameters";
        icp.ircParse();
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertTrue("ERR_NEEDMOREPARAMS", reply.equals(":" + prefix + " " + response));
         
        for (IrcTalker ircTalker : requestor) {
            icp.setRequestor((User) ircTalker);
            ircCommand = "JOIN";
            icp.setParsingString(ircCommand + " " + channelName[0]);
            icp.ircParse();
        }

        for (IrcTalker ircTalker : requestor) ircTalker.getOutputQueue().clear();
        
        icp.setRequestor(db.getUser(userNickname[0]));
        //331    RPL_NOTOPIC "<channel> :No topic is set"
        ircCommand = "TOPIC";
        icp.setParsingString(ircCommand + " " + channelName[0]);
        prefix = Globals.thisIrcServer.get().getHostname();
        response = "331" + " " + userNickname[0] + " " + channelName[0] + " " + ":" + "No topic is set";
        icp.ircParse();
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertTrue("RPL_NOTOPIC", reply.equals(":" + prefix + " " + response));
        
        ircCommand = "TOPIC";
        String topic = "This is a topic.";
        icp.setParsingString(ircCommand + " " + channelName[0] + " " + ":" + topic);
        prefix = userNickname[0];
        response = ircCommand + " " + channelName[0] + " " + ":" + topic;
        icp.ircParse();
        
        for (IrcTalker ircTalker : requestor) {
            reply = ircTalker.getOutputQueue().poll().getReport();
            assertTrue("TOPIC", reply.equals(":" + prefix + " " + response));
        }
        
        icp.setRequestor(db.getUser(userNickname[0]));
        ircCommand = "TOPIC";
        icp.setParsingString(ircCommand + " " + channelName[0]);
        prefix = Globals.thisIrcServer.get().getHostname();
        //332    RPL_TOPIC "<channel> :<topic>"
        response = "332" + " " + userNickname[0] + " " + channelName[0] + " " + ":" + topic;
        icp.ircParse();
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertTrue("RPL_TOPIC", reply.equals(":" + prefix + " " + response));        
                
        ircCommand = "TOPIC";
        icp.setParsingString(ircCommand + " " + channelName[0] + " " + ":");
        prefix = userNickname[0];
        response = ircCommand + " " + channelName[0] + " " + ":";
        icp.ircParse();

        for (IrcTalker ircTalker : requestor) {
            reply = ircTalker.getOutputQueue().poll().getReport();
            assertTrue(reply.equals(":" + prefix + " " + response));
        }
        
        icp.setRequestor(db.getUser(userNickname[0]));
        
        ircCommand = "TOPIC";
        icp.setParsingString(ircCommand + " " + channelName[0]);
        prefix = Globals.thisIrcServer.get().getHostname();
        response = "331" + " " + userNickname[0] + " " + channelName[0] + " " + ":" + "No topic is set";
        icp.ircParse();
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertTrue("RPL_NOTOPIC after set/uset", reply.equals(":" + prefix + " " + response));
        
        icp.setRequestor(db.getUser(userNickname[1]));
        ircCommand = "TOPIC";
        topic = "This is a topic.";
        icp.setParsingString(ircCommand + " " + channelName[0] + " " + ":" + topic);
        prefix = userNickname[1];
        response = ircCommand + " " + channelName[0] + " " + ":" + topic;
        icp.ircParse();
        
        for (IrcTalker ircTalker : requestor) {
            reply = ircTalker.getOutputQueue().poll().getReport();
            assertTrue("TOPIC check channel broadcasting", reply.equals(":" + prefix + " " + response));
        }
        
        icp.setRequestor(db.getUser(userNickname[2]));
        ircCommand = "TOPIC";
        icp.setParsingString(ircCommand + " " + channelName[0] + " " + ":");
        prefix = userNickname[2];
        response = ircCommand + " " + channelName[0] + " " + ":";
        icp.ircParse();
        
        for (IrcTalker ircTalker : requestor) {
            reply = ircTalker.getOutputQueue().poll().getReport();
            assertTrue(reply.equals(":" + prefix + " " + response));
        }
        
        // 442    ERR_NOTONCHANNEL "<channel> :You're not on that channel"
        icp.setRequestor(db.getUser(userNickname[2]));
        ircCommand = "JOIN";
        icp.setParsingString(ircCommand + " " + "0");
        icp.ircParse();
        icp.getRequestor().getOutputQueue().clear();
        
        icp.setRequestor(db.getUser(userNickname[2]));
        ircCommand = "TOPIC";
        icp.setParsingString(ircCommand + " " + channelName[0] + " " + ":" + topic);
        prefix = Globals.thisIrcServer.get().getHostname();
        response = "442" + " " + userNickname[2] + " " + channelName[0] + " " + ":" + "You're not on that channel";
        icp.ircParse();
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertTrue("ERR_NOTONCHANNEL", reply.equals(":" + prefix + " " + response));
       
        for (IrcTalker ircTalker : requestor) ircTalker.getOutputQueue().clear();        
 
        icp.setRequestor(db.getUser(userNickname[0]));
        ircCommand = "MODE";
        icp.setParsingString(ircCommand + " " + channelName[0] + " " + "+t");
        prefix = Globals.thisIrcServer.get().getHostname();
        icp.ircParse();
        for (IrcTalker ircTalker : requestor) ircTalker.getOutputQueue().clear();
        
        icp.setRequestor(db.getUser(userNickname[1]));
        ircCommand = "TOPIC";
        topic = "This is a topic.";
        icp.setParsingString(ircCommand + " " + channelName[0] + " " + ":" + topic);
        prefix = Globals.thisIrcServer.get().getHostname();
        //482    ERR_CHANOPRIVSNEEDED "<channel> :You're not channel operator"
        response = "482" + " " + userNickname[1] + " " + channelName[0] + " " + ":" + "You're not channel operator";
        icp.ircParse();
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertTrue("ERR_CHANOPRIVSNEEDED", reply.equals(":" + prefix + " " + response));
        

        for (IrcTalker ircTalker : requestor) {
            icp.setRequestor((User) ircTalker);
            ircCommand = "QUIT";
            icp.setParsingString(ircCommand);
            icp.ircParse();
        }
                
        icp.setRequestor(db.getService(serviceNickname[0]));
        ircCommand = "TOPIC";
        icp.setParsingString(ircCommand + " " + channelName[0]);
        prefix = Globals.thisIrcServer.get().getHostname();
        response = "421" + " " + serviceNickname[0] + " " + ircCommand + " " + ":Unknown command";
        icp.ircParse();
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertTrue(reply.equals(":" + prefix + " " + response));

        System.out.println("**TOPIC**************************************OK**");
    }   
}    

