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

public class TestMOTDcommand extends TestIrcCommand {
    public void run() {
        System.out.println("--MOTD-------------------------------------------");
        IrcCommandParser icp = new IrcCommandParser();
        
        dropAll();
        serverInit();
        userInit(); operatorInit();
        serviceInit();

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
        
        ircCommand = "MOTD";
        icp.setParsingString(ircCommand);
        prefix = Globals.thisIrcServer.get().getHostname();
        response = "451" + " " + "" + " " + ":" + "You have not registered";
        icp.ircParse();
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertTrue("Not registered reply", reply.equals(":" + prefix + " " + response));
        
        icp.setRequestor(requestor[0]);
        String motdFilename = Globals.motdFilename.get();
        Globals.motdFilename.set("");
        
        // 422    ERR_NOMOTD ":MOTD File is missing"
        ircCommand = "MOTD";
        icp.setParsingString(ircCommand);
        prefix = Globals.thisIrcServer.get().getHostname();
        response = "422" + " " + requestor[0].getNickname() + " " + ":" + "MOTD File is missing";
        icp.ircParse();
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertTrue("ERR_NOMOTD", icp.getRequestor().getOutputQueue().isEmpty());

        Globals.motdFilename.set(motdFilename);
        
        ircCommand = "MOTD";
        icp.setParsingString(ircCommand);
        prefix = Globals.thisIrcServer.get().getHostname();
        servername = Globals.thisIrcServer.get().getHostname();
        //375    RPL_MOTDSTART ":- <server> Message of the day - "
        icp.ircParse();
        response = "375" + " " + requestor[0].getNickname() + " " + ":-" + " " + servername + " " +"Message of the day - ";
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertTrue("RPL_MOTDSTART", reply.equals(":" + prefix + " " + response));
        //372    RPL_MOTD ":- <text>"
        response = "372" + " " + requestor[0].getNickname() + " " + ":" + "-" +".*";
        
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        while (reply.matches(":" + prefix + " " + response)) {
            assertTrue("RPL_MOTD", reply.matches(":" + prefix + " " + response));
            reply = icp.getRequestor().getOutputQueue().poll().getReport();
        }  
        //376    RPL_ENDOFMOTD ":End of MOTD command"
        response = "376" + " " + requestor[0].getNickname() + " " + ":" + "End of MOTD command";
        assertTrue("RPL_MOTD", reply.equals(":" + prefix + " " + response));
                
        System.out.println("**MOTD***************************************OK**");
    }   
}    

