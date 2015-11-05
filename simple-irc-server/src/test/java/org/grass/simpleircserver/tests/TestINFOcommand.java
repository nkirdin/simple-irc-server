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

public class TestINFOcommand extends TestIrcCommand {
    public void run() {
        System.out.println("--INFO-------------------------------------------");
        IrcCommandParser icp = new IrcCommandParser();
        
        dropAll();
        serverInit();
        userInit(); 
        operatorInit();
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

        ircCommand = "INFO";
        icp.setParsingString(ircCommand);
        prefix = Globals.thisIrcServer.get().getHostname();
        response = "451" + " " + "" + " " + ":" + "You have not registered";
        icp.ircParse();
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertTrue("Not registered reply", reply.equals(":" + prefix + " " + response));
        //String infoFilename = Globals.infoFilename.get();
        
        icp.setRequestor(requestor[0]);

//           371    RPL_INFO ":<string>"
//           374    RPL_ENDOFINFO ":End of INFO list"

        ircCommand = "INFO";
        String serverMask ="";
        icp.setParsingString(ircCommand + " " + serverMask);
        prefix = Globals.thisIrcServer.get().getHostname();
        icp.ircParse();
        
        prefix = Globals.thisIrcServer.get().getHostname();
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        response = "371" + " " + requestor[0].getNickname() + " " + ":" +".*";
        while (reply.matches(":" + prefix + " " + response)) {
            assertTrue("RPL_INFO", reply.matches(":" + prefix + " " + response));
            reply = icp.getRequestor().getOutputQueue().poll().getReport();
        }  
        response = "374" + " " + requestor[0].getNickname() + " " + ":" + "End of INFO list";
        assertTrue("RPL_ENDOFINFO", reply.equals(":" + prefix + " " + response));
        
        ircCommand = "INFO";
        serverMask = "*.irc.example.com";
        icp.setParsingString(ircCommand + " " + serverMask);
        prefix = Globals.thisIrcServer.get().getHostname();
        //402    ERR_NOSUCHSERVER "<server name> :No such server"
        icp.ircParse();
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        response = "402" + " " + requestor[0].getNickname() + " " + serverMask + " " + ":" + "No such server";
        assertTrue("ERR_NOSUCHSERVER", reply.equals(":" + prefix + " " + response));
        
        System.out.println("**INFO***************************************OK**");
    }   
}    

