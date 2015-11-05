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

public class TestSQUITcommand extends TestIrcCommand {
    public void run() {
        System.out.println("--SQUIT------------------------------------------");
        User requestor;
        String reply;
        String servername;
        String prefix;
        String ircCommand;
        String [] errouneous;
        String [] correctous;
        String response;
        String userMode;
        String userPassword;
        String comment = "Regular shutdown.";

        IrcCommandParser icp = new IrcCommandParser();
        
        dropAll();
        serverInit();
        userInit(); 
        operatorInit();

        String target = ircServer[1].getHostname();
        
        icp.setRequestor(User.create());
        ((User) icp.getRequestor()).setIrcServer(Globals.thisIrcServer.get());
        icp.getRequestor().setConnection(Connection.create());
        
        ircCommand = "SQUIT";
        icp.setParsingString(ircCommand + " " + target + " " + ":" + comment);
        prefix = Globals.thisIrcServer.get().getHostname();
        response = "451" + " " + "" + " " + ":" + "You have not registered";
        icp.ircParse();
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertTrue("ERR_NOTREGISTERED",reply.equals(":" + prefix + " " + response));

        icp.setRequestor(db.getUser(userNickname[0]));        
        ircCommand = "SQUIT";
        icp.setParsingString(ircCommand);
        prefix = Globals.thisIrcServer.get().getHostname();
        response = "461" + " " + userNickname[0] + " " + ircCommand + " " + ":" + "Not enough parameters";
        icp.ircParse();
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertTrue("ERR_NEEDMOREPARAMS, no target, no comment", reply.equals(":" + prefix + " " + response));
                
        ircCommand = "SQUIT";
        icp.setParsingString(ircCommand + " " + target);
        prefix = Globals.thisIrcServer.get().getHostname();
        response = "461" + " " + userNickname[0] + " " + ircCommand + " " + ":" + "Not enough parameters";
        icp.ircParse();
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertTrue("ERR_NEEDMOREPARAMS, no comment", reply.equals(":" + prefix + " " + response));
        
        ircCommand = "SQUIT";
        icp.setParsingString(ircCommand + " " + target + " " + ":" + comment);
        prefix = Globals.thisIrcServer.get().getHostname();
        //481    ERR_NOPRIVILEGES  ":Permission Denied- You're not an IRC operator"
        response = "481" + " " + userNickname[0] + " " + ":Permission Denied- You're not an IRC operator";
        icp.ircParse();
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertTrue("ERR_NOPRIVILEGES", reply.equals(":" + prefix + " " + response));
        
        icp.setParsingString("OPER" + " " + "user" + " " + "test");
        icp.ircParse();
        reply = icp.getRequestor().getOutputQueue().poll().getReport();

        String badTarget = "no.such.server";
        ircCommand = "SQUIT";
        icp.setParsingString(ircCommand + " " + badTarget + " " + ":" + comment);
        prefix = Globals.thisIrcServer.get().getHostname();
        //402    ERR_NOSUCHSERVER "<server name> :No such server"
        response = "402" + " " + userNickname[0] + " " + badTarget + " " + ":No such server";
        icp.ircParse();
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertTrue("ERR_NOSUCHSERVER", reply.equals(":" + prefix + " " + response));
        
        ircCommand = "SQUIT";
        icp.setParsingString(ircCommand + " " + target + " " + ":" + comment);
        prefix = Globals.thisIrcServer.get().getHostname();;
        icp.ircParse();
        // Check WALLOPS
        prefix = Globals.thisIrcServer.get().getHostname();
        response = "WALLOPS" + " " + ":" + userNickname[0] + " " + ircCommand + " " + target + " " + ":" + comment;
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertTrue("Receiving WALLOPS", reply.equals(":" + prefix + " " + response));

        System.out.println("**SQUIT**************************************OK**");
    }   
}    

