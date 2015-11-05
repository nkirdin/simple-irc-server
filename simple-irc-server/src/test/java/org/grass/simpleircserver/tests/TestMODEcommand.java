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

public class TestMODEcommand extends TestIrcCommand {
    public void run() {
        System.out.println("--MODE-------------------------------------------");
        
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
        
        ircCommand = "MODE";
        icp.setParsingString(ircCommand + " " + userUsername[0]);
        prefix = Globals.thisIrcServer.get().getHostname();
        response = "451" + " " + "" + " " + ":" + "You have not registered";
        icp.ircParse();
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertTrue(reply.equals(":" + prefix + " " + response));

        icp.setRequestor(db.getUser(userNickname[0]));
        
        ircCommand = "MODE";
        prefix = Globals.thisIrcServer.get().getHostname();
        icp.setParsingString(ircCommand);
        //461    ERR_NEEDMOREPARAMS  "<command> :Not enough parameters"
        response = "461" + " " + userNickname[0] + " " + ircCommand + " " + ":" + "Not enough parameters";
        icp.ircParse();
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertTrue("ERR_NEEDMOREPARAMS", reply.equals(":" + prefix + " " + response));
        
        ircCommand = "MODE";
        prefix = Globals.thisIrcServer.get().getHostname();
        icp.setParsingString(ircCommand + " " + userNickname[0] + " " + "+");
        //461    ERR_NEEDMOREPARAMS  "<command> :Not enough parameters"
        response = "461" + " " + userNickname[0] + " " + ircCommand + " " + ":" + "Not enough parameters";
        icp.ircParse();
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertTrue("ERR_NEEDMOREPARAMS", reply.equals(":" + prefix + " " + response));
        
        ircCommand = "MODE";
        prefix = Globals.thisIrcServer.get().getHostname();
        icp.setParsingString(ircCommand + " " + userNickname[0] + " " + "-");
        //461    ERR_NEEDMOREPARAMS  "<command> :Not enough parameters"
        response = "461" + " " + userNickname[0] + " " + ircCommand + " " + ":" + "Not enough parameters";
        icp.ircParse();
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertTrue("ERR_NEEDMOREPARAMS", reply.equals(":" + prefix + " " + response));

        ircCommand = "MODE";
        prefix = Globals.thisIrcServer.get().getHostname();
        icp.setParsingString(ircCommand + " " + userNickname[1]);
        // 502    ERR_USERSDONTMATCH ":Cannot change mode for other users"
        response = "502" + " " + userNickname[0] + " " + ":Cannot change mode for other users";
        icp.ircParse();
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertTrue("ERR_USERSDONTMATCH", reply.equals(":" + prefix + " " + response));
       
        errouneous= new String[] {"+o", "+O", "+a", "+oOa", "o", "O", "i", "w"
        , "r", "a", "s", "-a", "-r", "-ar", "+g", "-fm"};
        
        for (String userModeString : errouneous) {
            ircCommand = "MODE";
            prefix = Globals.thisIrcServer.get().getHostname();
            icp.setParsingString(ircCommand + " " + userNickname[0] + " " + userModeString);
            // 501    ERR_UMODEUNKNOWNFLAG ":Unknown MODE flag"
            response = "501" + " " + userNickname[0] + " " + ":Unknown MODE flag";
            icp.ircParse();
            reply = icp.getRequestor().getOutputQueue().poll().getReport();
            assertTrue("ERR_UMODEUNKNOWNFLAG", reply.equals(":" + prefix + " " + response));
        }
        
        correctous= new String[] {"+i", "-i", "+w", "-w", "+r", "+iwr", "-o", "-O", "-oO", "-i", "-w", "-iwoO"};
        
        for (String userModeString : correctous) {
            ircCommand = "MODE";
            prefix = userNickname[0];
            icp.setParsingString(ircCommand + " " + userNickname[0] + " " + userModeString);
            icp.ircParse();
            assertTrue((userModeString.length() - 1) == icp.getRequestor().getOutputQueue().size());
            for (i = 1; i < userModeString.length(); i++) {
                reply = icp.getRequestor().getOutputQueue().poll().getReport();
                response = ircCommand + " " + userModeString.substring(0,1) + userModeString.substring(i, i + 1);
                assertTrue(reply.equals(":" + prefix + " " + response));
            }
            //221    RPL_UMODEIS "<user mode string>"

            ircCommand = "MODE";
            prefix = Globals.thisIrcServer.get().getHostname();
            icp.setParsingString(ircCommand + " " + userNickname[0]);
            icp.ircParse();
            reply = icp.getRequestor().getOutputQueue().poll().getReport();
            switch(userModeString.charAt(0)) {
            case '-':
                response = "221" + " " + userNickname[0] + " " + "\\+" + "[^" + userModeString.substring(1, userModeString.length()) + "]*";
                assertTrue(reply.matches(":" + prefix + " " + response));
                break;
            case '+':
                response = "221" + " " + userNickname[0] + " " + "\\+" + "[" + userModeString.substring(1, userModeString.length()) + "]+";
                assertTrue(reply.matches(":" + prefix + " " + response));
                break;
            default:
                assertTrue(false);
                break;
            } 
            
        }
        System.out.println("**MODE***************************************OK**");
    }
}    

