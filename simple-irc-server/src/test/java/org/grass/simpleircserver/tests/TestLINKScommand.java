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

public class TestLINKScommand extends TestIrcCommand {
    public void run() {
        System.out.println("--LINKS------------------------------------------");
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

        ircCommand = "LINKS";
        icp.setParsingString(ircCommand);
        prefix = Globals.thisIrcServer.get().getHostname();
        response = "451" + " " + "" + " " + ":" + "You have not registered";
        icp.ircParse();
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertTrue("Not registered reply", reply.equals(":" + prefix + " " + response));
                
        //364    RPL_LINKS "<mask> <server> :<hopcount> <server info>"
        //365    RPL_ENDOFLINKS "<mask> :End of LINKS list"
        
        icp.setRequestor(requestor[0]);
        
        ircCommand = "LINKS";
        String tgt = "no.such.server";
        String serverMask = "*.irc.example.com";
        icp.setParsingString(ircCommand + " " + tgt + " " + serverMask);
        prefix = Globals.thisIrcServer.get().getHostname();
        //402    ERR_NOSUCHSERVER "<server name> :No such server"
        icp.ircParse();
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        response = "402" + " " + requestor[0].getNickname() + " " + tgt + " " + ":" + "No such server";
        assertTrue("ERR_NOSUCHSERVER", reply.equals(":" + prefix + " " + response));
        
        db.register(Globals.thisIrcServer.get());
        ircCommand = "LINKS";
        serverMask ="";
        icp.setParsingString(ircCommand + " " + serverMask);
        prefix = Globals.thisIrcServer.get().getHostname();
        servername = Globals.thisIrcServer.get().getHostname();
        icp.ircParse();
        LinkedHashSet<IrcServer> serverSet = db.getIrcServerSet();
        for (IrcServer server : serverSet) {
            response = "364" + " " + requestor[0].getNickname() + " " + serverMask + " " + server.getHostname() + " " + ":" + server.getHopcount() + " " + server.getInfo();
            reply = icp.getRequestor().getOutputQueue().poll().getReport();
            assertTrue("RPL_LINKS", reply.equals(":" + prefix + " " + response));
        }
        response = "365" + " " + requestor[0].getNickname() + " " + serverMask + " " + ":" + "End of LINKS list";
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertTrue("RPL_ENDOFLINKS", reply.equals(":" + prefix + " " + response));
        
        ircCommand = "QUIT";
        icp.setParsingString(ircCommand);
        icp.ircParse();
        
        System.out.println("**LINKS**************************************OK**");
    }   
}    

