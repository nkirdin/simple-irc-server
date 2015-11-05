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

public class TestUSERcommand extends TestIrcCommand {
    public void run() {
        System.out.println("--USER-------------------------------------------");
        
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
        
        dropAll();
        serverInit();
        
        icp.setRequestor(User.create());
        ((User) icp.getRequestor()).setIrcServer(Globals.thisIrcServer.get());
        icp.getRequestor().setConnection(Connection.create());
        
        ircCommand = "NICK";
        icp.setParsingString(ircCommand + " " + userNickname[0]);
        icp.ircParse();
        
        ircCommand = "USER";
        
        // 421    ERR_UNKNOWNCOMMAND "<command> :Unknown command"
        String errouneous = "A1234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789";
        
        prefix = Globals.thisIrcServer.get().getHostname();
        icp.setParsingString(ircCommand + " " + errouneous + " " + "0" + " " + "*" + " " + ":" + userRealname[0]);
        response = "421" + " " + userNickname[0] + " " + ircCommand + " " + ":Unknown command";
        icp.ircParse();
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertTrue("ERR_UNKNOWNCOMMAND", reply.equals(":" + prefix + " " + response));

/*        
        icp.setParsingString(ircCommand + " " +  "*" + " " + "*" + " " + ":" + userRealname[0]);
        response = "421" + " " + userNickname[0] + " " + ircCommand + " " + ":Unknown command";
        icp.ircParse();
        System.out.println(icp.getRequestor().getOutputQueue());
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertTrue("ERR_UNKNOWNCOMMAND", reply.equals(":" + prefix + " " + response));
*/

        //461    ERR_NEEDMOREPARAMS "<command> :Not enough parameters"
        icp.setParsingString(ircCommand + " " + userUsername[0] + " " + "0" + " " + "*");// + " " + ":";// + userRealname;
        response = "461" + " " + userNickname[0] + " " + ircCommand + " " + ":Not enough parameters";
        icp.ircParse();
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertTrue("ERR_NEEDMOREPARAMS", reply.equals(":" + prefix + " " + response));
/*        
        icp.setParsingString(ircCommand + " " + userUsername[0] + " " + "0" +  ":");// + userRealname;
        response = "421" + " " + userNickname[0] + " " + ircCommand + " " + ":Unknown command";
        icp.ircParse();
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertTrue("ERR_UNKNOWNCOMMAND", reply.equals(":" + prefix + " " + response));
        
        icp.setParsingString(ircCommand + " " + userUsername[0] + " " + "0" +  ":");// + userRealname;
        response = "421" + " " + userNickname[0] + " " + ircCommand + " " + ":Unknown command";
        icp.ircParse();
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertTrue("ERR_UNKNOWNCOMMAND", reply.equals(":" + prefix + " " + response));
*/

        ircCommand = "QUIT";
        icp.setParsingString(ircCommand);
        icp.ircParse();
        icp.getRequestor().getOutputQueue().clear();
       
        dropUser();
        
        icp.setRequestor(User.create());
        ((User) icp.getRequestor()).setIrcServer(Globals.thisIrcServer.get());
        icp.getRequestor().setConnection(Connection.create());

        ircCommand = "NICK";
        icp.setParsingString(ircCommand + " " + userNickname[0]);
        icp.ircParse();
           
        ircCommand = "USER";
        prefix = Globals.thisIrcServer.get().getHostname();
        icp.setParsingString(ircCommand + " " + userUsername[0] + " " + userMode[0] + " " + "*" + " " + ":" + userRealname[0]);
        icp.ircParse();
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        //001    RPL_WELCOME "Welcome to the Internet Relay Network <nick>!<user>@<host>" 
        response = "001" + " " + userNickname[0] + " " + ":" + "Welcome to the Internet Relay Network " + ".*";
        assertTrue("RPL_WELCOME", reply.matches(":" + prefix + " " + response));
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        //002    RPL_YOURHOST "Your host is <servername>, running version <ver>" 
        response = "002" + " " + userNickname[0] + " " + ":" + "Your host is " + ".*";
        assertTrue("RPL_YOURHOST", reply.matches(":" + prefix + " " + response));
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        //003    RPL_CREATED "This server was created <date>" 
        response = "003" + " " + userNickname[0] + " " + ":" + "This server was created " + ".*";
        assertTrue("RPL_CREATED", reply.matches(":" + prefix + " " + response));
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        //004    RPL_MYINFO "<servername> <version> <available user modes> <available channel modes>" 
        response = "004" + " " + userNickname[0] + " " + Globals.thisIrcServer.get().getHostname()
                         + " " + Constants.SERVER_VERSION 
                         + " " + Constants.USER_MODES
                         + " " + Constants.CHANNEL_MODES;
        assertTrue("RPL_MYINFO", reply.matches(":" + prefix + " " + response));
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        //005   RPL_ISUPPORT
        response = "005" + " " + userNickname[0] 
                         + " " + "PREFIX=" + Constants.PREFIX
                         + " " + "CHANTYPES=" + Constants.CHANTYPES
                         + " " + "MODES=" + Constants.MODES
                         + " " + "CHANLIMIT=" + Constants.CHANTYPES + ":" + Constants.CHANLIMIT
                         + " " + "NICKLEN=" + Constants.NICKLEN
                         + " " + "TOPIC_LEN=" + Constants.TOPIC_LEN
                         + " " + "KICKLEN=" + Constants.KICKLEN
                         + " " + "MAXLIST=" + "beI" + ":" + Constants.MAXLIST
                         + " " + "CHANNELLEN=" + Constants.CHANNELLEN
                         + " " + "CHANMODES=" + Constants.CHANMODES
                         + " " + "EXCEPTS=" + Constants.EXCEPTS
                         + " " + "INVEX=" + Constants.INVEX
                         + " " + "CASEMAPPING=" + Constants.CASEMAPPING
                         + " " + ":" + "are supported by this server";
        assertTrue("RPL_ISUPPORT", reply.equals(":" + prefix + " " + response));

        response = "375" + " " + icp.getRequestor().getNickname() + " " + ":-" + " " + Globals.thisIrcServer.get().getHostname() + " " +"Message of the day - ";
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertTrue("RPL_MOTDSTART prefix: " + prefix + " response: " + response + "reply: " + reply , reply.equals(":" + prefix + " " + response));
        //372    RPL_MOTD ":- <text>"
        response = "372" + " " + icp.getRequestor().getNickname() + " " + ":" + "-" +".*";
        
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        while (reply.matches(":" + prefix + " " + response)) {
            assertTrue("RPL_MOTD", reply.matches(":" + prefix + " " + response));
            reply = icp.getRequestor().getOutputQueue().poll().getReport();
        }  
        //376    RPL_ENDOFMOTD ":End of MOTD command"
        response = "376" + " " + icp.getRequestor().getNickname() + " " + ":" + "End of MOTD command";
        assertTrue("RPL_MOTD", reply.equals(":" + prefix + " " + response));


        
        // 462    ERR_ALREADYREGISTRED ":Unauthorized command (already registered)"
        ircCommand = "USER";
        prefix = Globals.thisIrcServer.get().getHostname();
        icp.setParsingString(ircCommand + " " + userUsername[0] + " " + userMode[0] + " " + "*" + " " + ":" + userRealname[0]);
        icp.ircParse();
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        response = "462" + " " + userNickname[0] + " " + ":Unauthorized command (already registered)";
        assertTrue("ERR_ALREADYREGISTRED", reply.equals(":" + prefix + " " + response));
        
        dropUser();        
        icp.setRequestor(User.create());
        ((User) icp.getRequestor()).setIrcServer(Globals.thisIrcServer.get());
        icp.getRequestor().setConnection(Connection.create());
        icp.getRequestor().setHostname(Globals.thisIrcServer.get().getHostname());

        ircCommand = "NICK";
        icp.setParsingString(ircCommand + " " + userNickname[0]);
        icp.ircParse();
           
        ircCommand = "USER";
        prefix = Globals.thisIrcServer.get().getHostname();
        
        icp.setParsingString(ircCommand + " " + userUsername[0] + " " + icp.getRequestor().getHostname() + " " + "127.0.0.1" + " " + ":" + userRealname[0]);
        icp.ircParse();
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        //001    RPL_WELCOME "Welcome to the Internet Relay Network <nick>!<user>@<host>" 
        response = "001" + " " + userNickname[0] + " " + ":" + "Welcome to the Internet Relay Network " + ".*";
        assertTrue("RPL_WELCOME", reply.matches(":" + prefix + " " + response));
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        //002    RPL_YOURHOST "Your host is <servername>, running version <ver>" 
        response = "002" + " " + userNickname[0] + " " + ":" + "Your host is " + ".*";
        assertTrue("RPL_YOURHOST", reply.matches(":" + prefix + " " + response));
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        //003    RPL_CREATED "This server was created <date>" 
        response = "003" + " " + userNickname[0] + " " + ":" + "This server was created " + ".*";
        assertTrue("RPL_CREATED", reply.matches(":" + prefix + " " + response));
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        //004    RPL_MYINFO "<servername> <version> <available user modes> <available channel modes>" 
        response = "004" + " " + userNickname[0] + " " + Globals.thisIrcServer.get().getHostname()
                         + " " + Constants.SERVER_VERSION 
                         + " " + Constants.USER_MODES
                         + " " + Constants.CHANNEL_MODES;
        assertTrue("RPL_MYINFO", reply.matches(":" + prefix + " " + response));
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        //005   RPL_ISUPPORT
        response = "005" + " " + userNickname[0] 
                         + " " + "PREFIX=" + Constants.PREFIX
                         + " " + "CHANTYPES=" + Constants.CHANTYPES
                         + " " + "MODES=" + Constants.MODES
                         + " " + "CHANLIMIT=" + Constants.CHANTYPES + ":" + Constants.CHANLIMIT
                         + " " + "NICKLEN=" + Constants.NICKLEN
                         + " " + "TOPIC_LEN=" + Constants.TOPIC_LEN
                         + " " + "KICKLEN=" + Constants.KICKLEN
                         + " " + "MAXLIST=" + "beI" + ":" + Constants.MAXLIST
                         + " " + "CHANNELLEN=" + Constants.CHANNELLEN
                         + " " + "CHANMODES=" + Constants.CHANMODES
                         + " " + "EXCEPTS=" + Constants.EXCEPTS
                         + " " + "INVEX=" + Constants.INVEX
                         + " " + "CASEMAPPING=" + Constants.CASEMAPPING
                         + " " + ":" + "are supported by this server";
        assertTrue("RPL_ISUPPORT", reply.equals(":" + prefix + " " + response));

        response = "375" + " " + icp.getRequestor().getNickname() + " " + ":-" + " " + Globals.thisIrcServer.get().getHostname() + " " +"Message of the day - ";
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertTrue("RPL_MOTDSTART", reply.equals(":" + prefix + " " + response));
        //372    RPL_MOTD ":- <text>"
        response = "372" + " " + icp.getRequestor().getNickname() + " " + ":" + "-" +".*";
        
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        while (reply.matches(":" + prefix + " " + response)) {
            assertTrue("RPL_MOTD", reply.matches(":" + prefix + " " + response));
            reply = icp.getRequestor().getOutputQueue().poll().getReport();
        }  
        //376    RPL_ENDOFMOTD ":End of MOTD command"
        response = "376" + " " + icp.getRequestor().getNickname() + " " + ":" + "End of MOTD command";
        assertTrue("RPL_MOTD", reply.equals(":" + prefix + " " + response));

        System.out.println("**USER***************************************OK**");
    }
}    

