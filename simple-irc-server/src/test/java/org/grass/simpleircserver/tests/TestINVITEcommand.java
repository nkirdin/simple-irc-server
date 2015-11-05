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

public class TestINVITEcommand extends TestIrcCommand {
    public void run() {
        System.out.println("--INVITE-----------------------------------------");
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
        String channelMode;
        String userPassword;
        String responseCode;
        String responseMsg;

        String[] channelName2 = {"#channel", "#channel2", "#channel3", "#channel4"};
        int i;

        IrcCommandParser icp = new IrcCommandParser();
        
        dropUser();

        icp.setRequestor(User.create());
        ((User) icp.getRequestor()).setIrcServer(Globals.thisIrcServer.get());
        icp.getRequestor().setConnection(Connection.create());

        userNickname = "nick";
        ircCommand = "USER";
        userUsername = "user";
        userRealname = "User Userson";
        userMode = "0";
        // 451    ERR_NOTREGISTERED ":You have not registered"

        ircCommand = "INVITE";
        String userNickname2 = "john";
        icp.setParsingString(ircCommand + " " + userNickname2 + " " + channelName2[0]);
        prefix = Globals.thisIrcServer.get().getHostname();
        response = "451" + " " + "" + " " + ":" + "You have not registered";
        icp.ircParse();
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertTrue("Not registered", reply.equals(":" + prefix + " " + response));
        
        ircCommand = "NICK";
        icp.setParsingString(ircCommand + " " + userNickname);
        icp.ircParse();
        
        ircCommand = "USER";
        icp.setParsingString(ircCommand + " " + userUsername + " " + userMode + " " + "*" + " " + ":" + userRealname);
        icp.ircParse();
        icp.getRequestor().getOutputQueue().clear();
        
        ircCommand = "JOIN";
        icp.setParsingString(ircCommand + " " + channelName2[0]);
        icp.ircParse();
        icp.getRequestor().getOutputQueue().clear();
        
        ircCommand = "MODE";
        channelMode = "+i";
        icp.setParsingString(ircCommand + " " + channelName2[0] + " " + channelMode);
        icp.ircParse();
        reply = icp.getRequestor().getOutputQueue().poll().getReport();

        icp.setRequestor(User.create());
        ((User) icp.getRequestor()).setIrcServer(Globals.thisIrcServer.get());
        icp.getRequestor().setConnection(Connection.create());

        ircCommand = "NICK";
        icp.setParsingString(ircCommand + " " + userNickname2);
        icp.ircParse();
        
        ircCommand = "USER";
        icp.setParsingString(ircCommand + " " + userUsername + " " + userMode + " " + "*" + " " + ":" + userRealname);
        icp.ircParse();
        icp.getRequestor().getOutputQueue().clear();
        
        ircCommand = "JOIN";
        icp.setParsingString(ircCommand + " " + channelName2[0]);
        icp.ircParse();
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        prefix = Globals.thisIrcServer.get().getHostname();
        //473    ERR_INVITEONLYCHAN "<channel> :Cannot join channel (+i)"
        responseCode = "473";
        response = responseCode + " " + userNickname2 + " " + channelName2[0] + " " + ":" + "Cannot join channel (+i)";
        assertTrue("Check ERR_INVITEONLYCHAN", reply.equals(":" + prefix + " " + response));

        icp.setRequestor(db.getUser(userNickname));

        // 461    ERR_NEEDMOREPARAMS "<command> :Not enough parameters"
        ircCommand = "INVITE";
        icp.setParsingString(ircCommand + " " + userNickname2);
        icp.ircParse();
        prefix = Globals.thisIrcServer.get().getHostname();
        responseCode = "461";
        response = responseCode + " " + userNickname + " " + ircCommand + " " + ":" + "Not enough parameters";
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertTrue("ERR_NEEDMOREPARAMS", reply.equals(":" + prefix + " " + response));
        
        ircCommand = "INVITE";
        icp.setParsingString(ircCommand + " " + userNickname2 + " " + channelName2[0]);
        icp.ircParse();
        prefix = Globals.thisIrcServer.get().getHostname();
        //341    RPL_INVITING "<channel> <nick>"
        responseCode = "341";
        response = responseCode + " " + userNickname + " " + channelName2[0] + " " + userNickname2;
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertTrue("check RPL_INVITING for first user", reply.equals(":" + prefix + " " + response));
        
        icp.setRequestor(db.getUser(userNickname2));
        responseCode = "341";
        response = responseCode + " " + userNickname + " " + channelName2[0] + " " + userNickname2;
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertTrue("check RPL_INVITING for second user", reply.equals(":" + prefix + " " + response));
        
        icp.setRequestor(db.getUser(userNickname));
        ircCommand = "INVITE";
        icp.setParsingString(ircCommand + " " + userNickname2 + " " + channelName2[0]);
        icp.ircParse();
        prefix = Globals.thisIrcServer.get().getHostname();
        //443    ERR_USERONCHANNEL "<user> <channel> :is already on channel"
        responseCode = "443";
        response = responseCode + " " + userNickname + " " + userNickname2 + " " + channelName2[0] + " " + ":" + "is already on channel";
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertTrue("check ERR_USERONCHANNEL", reply.equals(":" + prefix + " " + response));
       
        ircCommand = "INVITE";
        String userNickname3 = "paul";
        icp.setParsingString(ircCommand + " " + userNickname3 + " " + channelName2[0]);
        icp.ircParse();
        prefix = Globals.thisIrcServer.get().getHostname();
        //401    ERR_NOSUCHNICK "<nickname> :No such nick/channel"
        responseCode = "401";
        response = responseCode + " " + userNickname + " " + userNickname3 + " " + ":" + "No such nick/channel";
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertTrue("check ERR_NOSUCHNICK", reply.equals(":" + prefix + " " + response));

        icp.setRequestor(User.create());
        ((User) icp.getRequestor()).setIrcServer(Globals.thisIrcServer.get());
        icp.getRequestor().setConnection(Connection.create());

        userNickname3 = "paul"; 
        ircCommand = "NICK";
        icp.setParsingString(ircCommand + " " + userNickname3);
        icp.ircParse();
        
        ircCommand = "USER";
        icp.setParsingString(ircCommand + " " + userUsername + " " + userMode + " " + "*" + " " + ":" + userRealname);
        icp.ircParse();
        icp.getRequestor().getOutputQueue().clear();
        
        icp.setRequestor(db.getUser(userNickname2));
                
        ircCommand = "INVITE";
        icp.setParsingString(ircCommand + " " + userNickname3 + " " + channelName2[0]);
        icp.ircParse();
        prefix = Globals.thisIrcServer.get().getHostname();
        //482    ERR_CHANOPRIVSNEEDED "<channel> :You're not channel operator"
        responseCode = "482";
        response = responseCode + " " + userNickname2 + " " + channelName2[0] + " " + ":" + "You're not channel operator";
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertTrue("check ERR_CHANOPRIVSNEEDED", reply.equals(":" + prefix + " " + response));

        ircCommand = "JOIN";
        icp.setParsingString(ircCommand + " " + "0");
        icp.ircParse();
        icp.getRequestor().getOutputQueue().clear();
        
        icp.setRequestor(db.getUser(userNickname));
        icp.getRequestor().getOutputQueue().clear();

        ircCommand = "MODE";
        channelMode = "-i";
        icp.setParsingString(ircCommand + " " + channelName2[0] + " " + channelMode);
        icp.ircParse();
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        
        icp.setRequestor(db.getUser(userNickname2));
        icp.getRequestor().getOutputQueue().clear();

        ircCommand = "INVITE";
        icp.setParsingString(ircCommand + " " + userNickname3 + " " + channelName2[0]);
        icp.ircParse();
        prefix = Globals.thisIrcServer.get().getHostname();
        //442    ERR_NOTONCHANNEL "<channel> :You're not on that channel"
        responseCode = "442";
        response = responseCode + " " + userNickname2 + " " + channelName2[0] + " " + ":" + "You're not on that channel";
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertTrue("check ERR_NOTONCHANNEL", reply.equals(":" + prefix + " " + response));

        icp.setRequestor(db.getUser(userNickname));
        
        ircCommand = "QUIT";
        icp.setParsingString(ircCommand);
        icp.ircParse();
        
        icp.setRequestor(db.getUser(userNickname2));
        
        ircCommand = "QUIT";
        icp.setParsingString(ircCommand);
        icp.ircParse();
        
        icp.setRequestor(db.getUser(userNickname3));
        
        ircCommand = "QUIT";
        icp.setParsingString(ircCommand);
        icp.ircParse();
        
        icp.setRequestor(service[0]);
        ircCommand = "INVITE";
        icp.setParsingString(ircCommand + " " + userNickname + " " + channelName2[0]);
        prefix = Globals.thisIrcServer.get().getHostname();
        response = "421" + " " + service[0].getNickname() + " " + ircCommand + " " + ":Unknown command";
        icp.ircParse();
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertTrue(reply.equals(":" + prefix + " " + response));
                
        System.out.println("**INVITE*************************************OK**");
    }   
}    

