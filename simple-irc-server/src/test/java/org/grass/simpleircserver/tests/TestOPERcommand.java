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

public class TestOPERcommand extends TestIrcCommand {
    public void run() {
        System.out.println("--OPER-------------------------------------------");
        
        String [] xmlText = {"<?xml version=\"1.0\" encoding=\"UTF-8\"?>",
                            "<accounts>",
                            "<account name=\"root\" password=\"password\"></account>", 
                            "<account name=\"user\" password=\"test\"></account>",
                            "<account name=\"first\" password=\"first\"></account>",
                            "<account name=\"second\" password=\"second\"></account>",
                            "<account name=\"third\" password=\"third\"></account>",
                            "</accounts>"};
        
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

        IrcCommandParser icp = new IrcCommandParser();

        dropUser();
        userInit(); operatorInit();
        
        
        icp.setRequestor(User.create());
        ((User) icp.getRequestor()).setIrcServer(Globals.thisIrcServer.get());
        icp.getRequestor().setConnection(Connection.create());
        
        String unregisteredNick = "nOSuch";
        
        ircCommand = "OPER";
        icp.setParsingString(ircCommand + " " + unregisteredNick + " " + "userPassword");
        prefix = Globals.thisIrcServer.get().getHostname();
        response = "451" + " " + "" + " " + ":" + "You have not registered";
        icp.ircParse();
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertTrue(reply.equals(":" + prefix + " " + response));
        
        
   

        icp.setRequestor(db.getUser(userNickname[0]));  
        ircCommand = "OPER";
        prefix = Globals.thisIrcServer.get().getHostname();
        icp.setParsingString(ircCommand);
        //461    ERR_NEEDMOREPARAMS  "<command> :Not enough parameters"
        response = "461" + " " + userNickname[0] + " " + ircCommand + " " + ":" + "Not enough parameters";
        icp.ircParse();
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertTrue("ERR_NEEDMOREPARAMS", reply.equals(":" + prefix + " " + response));
        
        ircCommand = "OPER";
        prefix = Globals.thisIrcServer.get().getHostname();
        icp.setParsingString(ircCommand + " " + userNickname[0]);
        //461    ERR_NEEDMOREPARAMS  "<command> :Not enough parameters"
        response = "461" + " " + userNickname[0] + " " + ircCommand + " " + ":" + "Not enough parameters";
        icp.ircParse();
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertTrue("ERR_NEEDMOREPARAMS", reply.equals(":" + prefix + " " + response));
        
        String errouneous = "A2345678901234567890123456789";
        
        ircCommand = "OPER";
        prefix = Globals.thisIrcServer.get().getHostname();
        icp.setParsingString(ircCommand + " " + userUsername[0] + " " + errouneous);
        response = "421" + " " + userNickname[0] + " " + ircCommand + " " + ":Unknown command";
        icp.ircParse();
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertTrue(reply.equals(":" + prefix + " " + response));

        String correctous = "0";
        
        ircCommand = "OPER";
        prefix = Globals.thisIrcServer.get().getHostname();
        icp.setParsingString(ircCommand + " " + userUsername[0] + " " + correctous);
        //464    ERR_PASSWDMISMATCH     ":Password incorrect"
        response = "464" + " " + userNickname[0] + " " + ":Password incorrect";
        icp.ircParse();
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertTrue(reply.equals(":" + prefix + " " + response));
            
        ircCommand = "OPER";
        prefix = Globals.thisIrcServer.get().getHostname();
        icp.setParsingString(ircCommand + " " + "user" + " " + "test");
        //381    RPL_YOUREOPER   ":You are now an IRC operator"
        response = "381" + " " + userNickname[0] + " " + ":You are now an IRC operator";
        icp.ircParse();
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertTrue(reply.equals(":" + prefix + " " + response));
        
        ircCommand = "MODE";
        icp.setParsingString(ircCommand + " " + userNickname[0] + " " + "-o");
        icp.ircParse();
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        
        


        System.out.println("**OPER***************************************OK**");        
        
    }
}    

