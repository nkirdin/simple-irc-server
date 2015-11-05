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

public class TestPINGcommand extends TestIrcCommand {
    public void run() {
        System.out.println("--PING-------------------------------------------");
        
        IrcCommandParser icp = new IrcCommandParser();
        
        dropAll();
        serverInit();
        serviceInit();

        IrcTalker[] requestor = new IrcTalker[3];
        String[] userNickname = {"neeck1", "neeck2", "nick3"};
        String[] userUsername = {"user1", "user2he", "user3he"};
        String[] userRealname = {"Real Name 1", "Real Name 2A", "Real Name 3A"};
        String[] userHost = {"HostName1", "HostName2", "HostName2"};
        String[] userMode = {"0", "0", "0"};
        String[] channelName = {"#channel1", "#channel2", "#channel3", "#channel4", "#channel5"};
        String[] channelMode = {"", "+n", "+m", "+b nick2", "+be nick? nick3"};
        
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
        
        icp.setRequestor(User.create());
        ((User) icp.getRequestor()).setIrcServer(Globals.thisIrcServer.get());
        Connection conn = Connection.create();
        
        conn.ircTalker.set(icp.getRequestor());
        icp.getRequestor().setConnection(conn);
        db.register(conn);


        ircCommand = "PING";
        icp.setParsingString(ircCommand + " " + ircServer[0].getHostname());
        icp.ircParse();
        assertTrue("If sender not registered, then no reply", icp.getRequestor().getOutputQueue().isEmpty());
        
        for (i = 0; i < requestor.length - 1 ; i++) {
            icp.setRequestor(User.create());
            ((User) icp.getRequestor()).setIrcServer(Globals.thisIrcServer.get());
            conn = Connection.create();
            
            conn.ircTalker.set(icp.getRequestor());
            icp.getRequestor().setConnection(conn);
            db.register(conn);


            
            ((User) icp.getRequestor()).setIrcServer(ircServer[i]);
            ((User) icp.getRequestor()).setHostname(userHost[i]);
            ircCommand = "NICK";
            icp.setParsingString(ircCommand + " " + userNickname[i]);
            icp.ircParse();
            requestor[i] = db.getUser(userNickname[i]);
            
        
            ircCommand = "USER";
            icp.setParsingString(ircCommand + " " + userUsername[i] + " " + userMode[i] + " " + "*" + " " + ":" + userRealname[i]);
            icp.ircParse();
            icp.getRequestor().getOutputQueue().clear();
        }
        
        icp.setRequestor(requestor[0]);
        prefix = Globals.thisIrcServer.get().getHostname();
/*
        ircCommand = "PING";
        icp.setParsingString(ircCommand + " " + channelName[0]);
        prefix = Globals.thisIrcServer.get().getHostname();
        //421    ERR_UNKNOWNCOMMAND  "<command> :Unknown command"
        response = "421" + " " + userNickname[0] + " " + ircCommand + " " + ":" + "Unknown command";
        icp.ircParse();
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertTrue("ERR_UNKNOWNCOMMAND", reply.equals(":" + prefix + " " + response));
*/
//      409    ERR_NOORIGIN ":No origin specified"
//      402    ERR_NOSUCHSERVER "<server name> :No such server"

        ircCommand = "PING";
        icp.setParsingString(ircCommand);
        prefix = Globals.thisIrcServer.get().getHostname();
        response = "409" + " " + userNickname[0] + " " + ":" + "No origin specified";
        icp.ircParse();
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertTrue("ERR_NOORIGIN", reply.equals(":" + prefix + " " + response));
/*        
        ircCommand = "PING";
        String tgt = "no.such.server";
        icp.setParsingString(ircCommand  + " " + tgt);
        prefix = Globals.thisIrcServer.get().getHostname();
        response = "402" + " " + userNickname[0] + " " + tgt  + " " + ":" + "No such server";
        icp.ircParse();
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertTrue("ERR_NOSUCHSERVER", reply.equals(":" + prefix + " " + response));
        
        ircCommand = "PING";
        icp.setParsingString(ircCommand + " " + ircServer[1].getHostname() + " " + tgt);
        prefix = Globals.thisIrcServer.get().getHostname();        
        response = "402" + " " + userNickname[0] + " " + tgt  + " " + ":" + "No such server";
        icp.ircParse();
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertTrue("ERR_NOSUCHSERVER <server2>", reply.equals(":" + prefix + " " + response));
*/        
        ircCommand = "PING";
        icp.setParsingString(ircCommand  + " " + Globals.thisIrcServer.get().getHostname());
        prefix = Globals.thisIrcServer.get().getHostname();        
        response ="PONG" + " " + Globals.thisIrcServer.get().getHostname() + " " + ":" + Globals.thisIrcServer.get().getHostname();
        icp.ircParse();
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertTrue("PONG reply to origin", reply.equals(":" + prefix + " " + response));
        
        icp.setRequestor(User.create());
        ((User) icp.getRequestor()).setIrcServer(Globals.thisIrcServer.get());
        icp.getRequestor().setConnection(Connection.create());

        
        ircCommand = "NICK";
        icp.setParsingString(ircCommand + " " + userNickname[i]);
        icp.ircParse();
        
        requestor[i] = icp.getRequestor();
        ((User) icp.getRequestor()).setHostname(userHost[i]);
        ((User) icp.getRequestor()).setIrcServer(ircServer[i]);
        
        ircCommand = "USER";
        icp.setParsingString(ircCommand + " " + userUsername[i] + " " + userMode[i] + " " + "*" + " " + ":" + userRealname[i]);
        icp.ircParse();
                        
        icp.getRequestor().getOutputQueue().clear();
        
        icp.setRequestor(ircServer[1]);
        
        ircCommand = "PING";
        icp.setParsingString(ircCommand + " " + ircServer[1].getHostname() + " " + ircServer[2].getHostname());
        prefix = ircServer[1].getHostname();        
        response = ircCommand + " " + ircServer[1].getHostname() + " " + ircServer[2].getHostname() + " " + ":" + Globals.thisIrcServer.get().getHostname();
        icp.ircParse();
        reply = ircServer[2].getOutputQueue().poll().getReport();
        assertTrue("Forward PING to destination", reply.equals(":" + prefix + " " + response));
        
        System.out.println("**PING***************************************OK**");
    }   
}    

