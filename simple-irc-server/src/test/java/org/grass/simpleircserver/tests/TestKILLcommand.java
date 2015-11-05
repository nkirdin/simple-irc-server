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

public class TestKILLcommand extends TestIrcCommand {
    public void run() {
        System.out.println("--KILL-------------------------------------------");
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
        String info;

        int i;
        
        icp.setRequestor(User.create());
        ((User) icp.getRequestor()).setIrcServer(Globals.thisIrcServer.get());
        Connection conn = Connection.create();
        
        conn.ircTalker.set(icp.getRequestor());
        icp.getRequestor().setConnection(conn);
        db.register(conn);

        
        ircCommand = "KILL";
        String comment = "You too stupid."; 
        icp.setParsingString(ircCommand);
        prefix = Globals.thisIrcServer.get().getHostname();
        response = "451" + " " + "" + " " + ":" + "You have not registered";
        icp.ircParse();
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertTrue("Not registered", reply.equals(":" + prefix + " " + response));
        
        for (i = 0; i < requestor.length - 1 ; i++) {
            icp.setRequestor(User.create());
            ((User) icp.getRequestor()).setIrcServer(Globals.thisIrcServer.get());
            conn = Connection.create();
            
            conn.ircTalker.set(icp.getRequestor());
            icp.getRequestor().setConnection(conn);
            db.register(conn);

            ((User) icp.getRequestor()).setIrcServer(ircServer[i]);
            
            ircCommand = "NICK";
            icp.setParsingString(ircCommand + " " + userNickname[i]);
            icp.ircParse();
            requestor[i] = icp.getRequestor();
            ((User) icp.getRequestor()).setHostname(userHost[i]);
        
            ircCommand = "USER";
            icp.setParsingString(ircCommand + " " + userUsername[i] + " " + userMode[i] + " " + "*" + " " + ":" + userRealname[i]);
            icp.ircParse();
            icp.getRequestor().getOutputQueue().clear();
        }
        
        icp.setRequestor(db.getUser(userNickname[0]));
        prefix = Globals.thisIrcServer.get().getHostname();
        
        //481    ERR_NOPRIVILEGES ":Permission Denied- You're not an IRC operator"

        ircCommand = "KILL";
        icp.setParsingString(ircCommand + " " + userNickname[1] + " " + ":" + comment);
        prefix = Globals.thisIrcServer.get().getHostname();
        response = "481" + " " + userNickname[0] + " " + ":" + "Permission Denied- You're not an IRC operator";
        icp.ircParse();
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertTrue("ERR_NOPRIVILEGES", reply.equals(":" + prefix + " " + response));
        
        ircCommand = "OPER";
        icp.setParsingString(ircCommand + " " + "user" + " " + "test");
        prefix = Globals.thisIrcServer.get().getHostname();
        icp.ircParse();
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        
        // 461    ERR_NEEDMOREPARAMS "<command> :Not enough parameters"
        
        ircCommand = "KILL";
        icp.setParsingString(ircCommand);
        prefix = Globals.thisIrcServer.get().getHostname();        
        response = "461" + " " + userNickname[0] + " " + ircCommand  + " " +":" + "Not enough parameters";
        icp.ircParse();
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertTrue("ERR_NEEDMOREPARAMS", reply.equals(":" + prefix + " " + response));
        
        ircCommand = "KILL";
        icp.setParsingString(ircCommand + " " + userNickname[1] + " " + ":");
        prefix = Globals.thisIrcServer.get().getHostname();        
        response = "461" + " " + userNickname[0] + " " + ircCommand  + " " +":" + "Not enough parameters";
        icp.ircParse();
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertTrue("ERR_NEEDMOREPARAMS 2", reply.equals(":" + prefix + " " + response));
        
        ircCommand = "KILL";
        icp.setParsingString(ircCommand + " " + userNickname[2] + " " + ":" + comment);
        prefix = Globals.thisIrcServer.get().getHostname();        
        response = "401" + " " + userNickname[0] + " " + userNickname[2] + " " +":" + "No such nick/channel";
        icp.ircParse();
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertTrue("ERR_NOSUCHNICK", reply.equals(":" + prefix + " " + response));
        
        i = 2;
        icp.setRequestor(User.create());
        ((User) icp.getRequestor()).setIrcServer(Globals.thisIrcServer.get());
        icp.getRequestor().setConnection(Connection.create());
        
        ((User) icp.getRequestor()).setIrcServer(ircServer[i]);
            
        ircCommand = "NICK";
        icp.setParsingString(ircCommand + " " + userNickname[i]);
        icp.ircParse();
        requestor[i] = icp.getRequestor();
        ((User) icp.getRequestor()).setHostname(userHost[i]);
        
        ircCommand = "USER";
        icp.setParsingString(ircCommand + " " + userUsername[i] + " " + userMode[i] + " " + "*" + " " + ":" + userRealname[i]);
        icp.ircParse();
                       
        icp.getRequestor().getOutputQueue().clear();
        requestor[2] = icp.getRequestor();
        
        icp.setRequestor(db.getUser(userNickname[0]));
        icp.getRequestor().getOutputQueue().clear();
        
        ircCommand = "KILL";
        icp.setParsingString(ircCommand + " " + userNickname[2] + " " + ":" + comment);
        prefix = userNickname[0];        
        info = comment + " " + icp.getRequestor().getNickname() + "@" + ((User)icp.getRequestor()).getIrcServer().getHostname();
        response = ircCommand + " " + userNickname[2] + " " +":" + info;
        icp.ircParse();
        icp.setRequestor(requestor[2]);
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertTrue("Kill message to killing user", reply.equals(":" + prefix + " " + response));
        
        System.out.println("**KILL***************************************OK**");
    }   
}    

