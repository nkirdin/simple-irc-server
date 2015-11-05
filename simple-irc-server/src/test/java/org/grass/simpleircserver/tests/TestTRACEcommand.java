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

public class TestTRACEcommand extends TestIrcCommand {
    public void run() {
        System.out.println("--TRACE------------------------------------------");
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
        String targetServer = "irc.example.local";
        String port = "6667";
        String remoteServer = "irc2.example.local";

        int i;
        
        icp.setRequestor(User.create());
        ((User) icp.getRequestor()).setIrcServer(Globals.thisIrcServer.get());
        icp.getRequestor().setConnection(Connection.create());

        ircCommand = "TRACE";
        icp.setParsingString(ircCommand);
        prefix = Globals.thisIrcServer.get().getHostname();
        response = "451" + " " + "" + " " + ":" + "You have not registered";
        icp.ircParse();
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertTrue("Not registered reply", reply.equals(":" + prefix + " " + response));
        
        icp.setRequestor(requestor[0]);
        
        ircCommand = "OPER";
        icp.setParsingString(ircCommand + " " + "user" + " " + "test");
        icp.ircParse();
        icp.getRequestor().getOutputQueue().clear();
        
        // 206    RPL_TRACESERVER "Serv <class> <int>S <int>C <server>  <nick!user|*!*>@<host|server> V<protocol version>"
        // 207    RPL_TRACESERVICE "Service <class> <name> <type> <active type>"
        // 204    RPL_TRACEOPERATOR "Oper <class> <nick>"
        
        ircCommand = "TRACE";
        icp.setParsingString(ircCommand);
        prefix = Globals.thisIrcServer.get().getHostname();
        icp.ircParse();
        response = "481" + " " + requestor[0].getNickname() + " " + ":" + "Permission Denied- You're not an IRC operator";
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertTrue("ERR_NOPRIVILEGES", reply.equals(":" + prefix + " " + response));

        System.out.println("**TRACE**************************************OK**");
    }   
}    

