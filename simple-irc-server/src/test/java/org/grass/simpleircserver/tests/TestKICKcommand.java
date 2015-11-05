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

public class TestKICKcommand extends TestIrcCommand {
    public void run() {
        System.out.println("--KICK-------------------------------------------");
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
        serviceInit();
        userInit(); operatorInit();

        icp.setRequestor(User.create());
        ((User) icp.getRequestor()).setIrcServer(Globals.thisIrcServer.get());
        icp.getRequestor().setConnection(Connection.create());

        // 451    ERR_NOTREGISTERED ":You have not registered"

        ircCommand = "KICK";
        icp.setParsingString(ircCommand + " " + channelName[0] + " " + userNickname[0]);
        prefix = Globals.thisIrcServer.get().getHostname();
        response = "451" + " " + "" + " " + ":" + "You have not registered";
        icp.ircParse();
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertTrue("Not registered", reply.equals(":" + prefix + " " + response));
        
        icp.setRequestor(db.getUser(userNickname[0]));        
        
        ircCommand = "JOIN";
        icp.setParsingString(ircCommand + " " + channelName[0]);
        icp.ircParse();
        icp.getRequestor().getOutputQueue().clear();
         
        for (IrcTalker ircTalker : requestor) {
            if (ircTalker.getOutputQueue() != null) ircTalker.getOutputQueue().clear();
        }
                
        ircCommand = "OPER";
        icp.setParsingString(ircCommand + " " + "user" + " " + "test");
        icp.ircParse();
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        
        // 461    ERR_NEEDMOREPARAMS "<command> :Not enough parameters"
        ircCommand = "KICK";
        icp.setParsingString(ircCommand);
        icp.ircParse();
        prefix = Globals.thisIrcServer.get().getHostname();
        responseCode = "461";
        response = responseCode + " " + userNickname[0] + " " + ircCommand + " " + ":" + "Not enough parameters";
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertTrue("ERR_NEEDMOREPARAMS", reply.equals(":" + prefix + " " + response));
        
        // 461    ERR_NEEDMOREPARAMS "<command> :Not enough parameters"
        ircCommand = "KICK";
        icp.setParsingString(ircCommand + " " + channelName[0]);
        icp.ircParse();
        prefix = Globals.thisIrcServer.get().getHostname();
        responseCode = "461";
        response = responseCode + " " + userNickname[0] + " " + ircCommand + " " + ":" + "Not enough parameters";
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertTrue("ERR_NEEDMOREPARAMS", reply.equals(":" + prefix + " " + response));
        
        ircCommand = "KICK";
        icp.setParsingString(ircCommand + " " + channelName[1] + " " + userNickname[1] );
        icp.ircParse();
        prefix = Globals.thisIrcServer.get().getHostname();
        //403    ERR_NOSUCHCHANNEL "<channel name> :No such channel"
        responseCode = "403";
        response = responseCode + " " + userNickname[0] + " " + channelName[1] + " " + ":" + "No such channel";
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertTrue("check ERR_NOSUCHCHANNEL", reply.equals(":" + prefix + " " + response));
        
        ircCommand = "KICK";
        String name = "nOsUch";
        icp.setParsingString(ircCommand + " " + channelName[0] + " " + name);
        icp.ircParse();
        prefix = Globals.thisIrcServer.get().getHostname();
        //401    ERR_NOSUCHNICK "<nick> :No such nick/channel"
        responseCode = "401";
        response = responseCode + " " + userNickname[0] + " " + name + " " + ":" + "No such nick/channel";
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertTrue("check ERR_NOSUCHNICK", reply.equals(":" + prefix + " " + response));
        
        ircCommand = "MODE";
        icp.setParsingString(ircCommand + " " + userNickname[0] + " " + "-o");
        icp.ircParse();
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        
        ircCommand = "KICK";
        icp.setParsingString(ircCommand + " " + channelName[0] + " " + userNickname[1]);
        icp.ircParse();
        prefix = Globals.thisIrcServer.get().getHostname();
        //441    ERR_USERNOTINCHANNEL "<nick> <channel> :They aren't on that channel"
        responseCode = "441";
        response = responseCode + " " + userNickname[0] + " " + userNickname[1] + " " + channelName[0] + " " + ":" + "They aren't on that channel";
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertTrue("check ERR_USERNOTINCHANNEL", reply.equals(":" + prefix + " " + response));
 
        for (i = 1; i < requestor.length; i++ ) {
            icp.setRequestor(requestor[i]);
            ircCommand = "JOIN";
            icp.setParsingString(ircCommand + " " + channelName[0]);
            icp.ircParse();
        }
        
        icp.setRequestor(db.getUser(userNickname[1]));
        ircCommand = "JOIN";
        icp.setParsingString(ircCommand + " " + channelName[1]);
        icp.ircParse();

        for (IrcTalker ircTalker : requestor) {
            if (ircTalker.getOutputQueue() != null) ircTalker.getOutputQueue().clear();
        }
        
        icp.setRequestor(db.getUser(userNickname[0]));
        // 442    ERR_NOTONCHANNEL "<channel> :You're not on that channel"
        ircCommand = "KICK";
        icp.setParsingString(ircCommand + " " + channelName[1] + " " + userNickname[1]);
        icp.ircParse();
        prefix = Globals.thisIrcServer.get().getHostname();
        responseCode = "442";
        response = responseCode + " " + userNickname[0] + " " + channelName[1] + " " + ":" + "You're not on that channel";
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertTrue("ERR_NOTONCHANNEL", reply.equals(":" + prefix + " " + response));
        
        icp.setRequestor(db.getUser(userNickname[1]));
        ircCommand = "PART";
        icp.setParsingString(ircCommand + " " + channelName[1]);
        icp.ircParse();
        
        for (IrcTalker ircTalker : requestor) {
            if (ircTalker.getOutputQueue() != null) ircTalker.getOutputQueue().clear();
        }
        
        ircCommand = "KICK";
        icp.setParsingString(ircCommand + " " + channelName[0] + " " + userNickname[1]);
        icp.ircParse();
        prefix = Globals.thisIrcServer.get().getHostname();
        //482    ERR_CHANOPRIVSNEEDED "<channel> :You're not channel operator"
        responseCode = "482";
        response = responseCode + " " + userNickname[1] + " " + channelName[0] + " " + ":" + "You're not channel operator";
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertTrue("check ERR_CHANOPRIVSNEEDED for selfkicking", reply.equals(":" + prefix + " " + response));
        
        ircCommand = "KICK";
        icp.setParsingString(ircCommand + " " + channelName[0] + " " + userNickname[2]);
        icp.ircParse();
        prefix = Globals.thisIrcServer.get().getHostname();
        //482    ERR_CHANOPRIVSNEEDED "<channel> :You're not channel operator"
        responseCode = "482";
        response = responseCode + " " + userNickname[1] + " " + channelName[0] + " " + ":" + "You're not channel operator";
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertTrue("check ERR_CHANOPRIVSNEEDED", reply.equals(":" + prefix + " " + response));
        
        icp.setRequestor(db.getUser(userNickname[0]));
        ircCommand = "KICK";
        icp.setParsingString(ircCommand + " " + channelName[0] + " " + userNickname[1] + "," + userNickname[2]);
        icp.ircParse();
        prefix = userNickname[0];
        response = ircCommand + " " + channelName[0] + " " + userNickname[1];
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertTrue("Reply to first user (about first kicked).", reply.equals(":" + prefix + " " + response));
        
        icp.setRequestor(db.getUser(userNickname[1]));
        response = ircCommand + " " + channelName[0] + " " + userNickname[1];
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertTrue("Reply to second user (about first kicked).", reply.equals(":" + prefix + " " + response));
        
        icp.setRequestor(db.getUser(userNickname[2]));
        response = ircCommand + " " + channelName[0] + " " + userNickname[1];
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertTrue("Reply to third user (about first kicked).", reply.equals(":" + prefix + " " + response));
        
        icp.setRequestor(db.getUser(userNickname[0]));
        response = ircCommand + " " + channelName[0] + " " + userNickname[2];
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertTrue("Reply to first user (about second kicked).", reply.equals(":" + prefix + " " + response));
        
        icp.setRequestor(db.getUser(userNickname[2]));
        response = ircCommand + " " + channelName[0] + " " + userNickname[2];
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertTrue("Reply to second user (about second kicked).", reply.equals(":" + prefix + " " + response));

        icp.setRequestor(db.getUser(userNickname[0]));
        ircCommand = "JOIN";
        icp.setParsingString(ircCommand + " " + channelName[1]);
        icp.ircParse();
        
        icp.setRequestor(db.getUser(userNickname[1]));
        ircCommand = "JOIN";
        icp.setParsingString(ircCommand + " " + channelName[0]);
        icp.ircParse();
        
        icp.setRequestor(db.getUser(userNickname[2]));
        ircCommand = "JOIN";
        icp.setParsingString(ircCommand + " " + channelName[1]);
        icp.ircParse();
        
        for (IrcTalker ircTalker : requestor) {
            if (ircTalker.getOutputQueue() != null) ircTalker.getOutputQueue().clear();
        }
        
        icp.setRequestor(db.getUser(userNickname[0]));
        
        ircCommand = "KICK";
        icp.setParsingString(ircCommand + " " + channelName[0] + "," + channelName[1] + " " + userNickname[1] + "," + userNickname[2]);
        icp.ircParse();
        prefix = userNickname[0];
        response = ircCommand + " " + channelName[0] + " " + userNickname[1];
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertTrue("Reply to first user (about first kicked from first channel).", reply.equals(":" + prefix + " " + response));
        
        icp.setRequestor(db.getUser(userNickname[1]));
        response = ircCommand + " " + channelName[0] + " " + userNickname[1];
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertTrue("Reply to second user (about first kicked from first channel).", reply.equals(":" + prefix + " " + response));

        icp.setRequestor(db.getUser(userNickname[0]));
        response = ircCommand + " " + channelName[1] + " " + userNickname[2];
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertTrue("Reply to first user (about second kicked from second channel).", reply.equals(":" + prefix + " " + response));
        
        icp.setRequestor(db.getUser(userNickname[2]));
        response = ircCommand + " " + channelName[1] + " " + userNickname[2];
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertTrue("Reply to second user (about second kicked from second channel).", reply.equals(":" + prefix + " " + response));
        
        icp.setRequestor(db.getUser(userNickname[0]));
        ircCommand = "KICK";
        icp.setParsingString(ircCommand + " " + channelName[0] + " " + userNickname[0]);
        icp.ircParse();
        prefix = userNickname[0];
        response = ircCommand + " " + channelName[0] + " " + userNickname[0];
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertTrue("for chopers selfkicking is allowed", reply.equals(":" + prefix + " " + response));
        
        icp.setRequestor(db.getService(serviceNickname[0]));                
        ircCommand = "KICK";
        icp.setParsingString(ircCommand + " " + channelName[0] + " " + userNickname[0]);
        prefix = Globals.thisIrcServer.get().getHostname();
        response = "421" + " " + service[0].getNickname() + " " + ircCommand + " " + ":Unknown command";
        icp.ircParse();
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertTrue(reply.equals(":" + prefix + " " + response));
                
        System.out.println("**KICK***************************************OK**");
    }   
}    

