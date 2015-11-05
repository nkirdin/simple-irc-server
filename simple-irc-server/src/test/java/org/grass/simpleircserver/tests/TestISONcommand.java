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

public class TestISONcommand extends TestIrcCommand {
    public void run() {
        System.out.println("--ISON-------------------------------------------");
        
        IrcCommandParser icp = new IrcCommandParser();
        
        dropAll();
        serverInit();
        serviceInit();

        IrcTalker [] requestor = new User[6];
        String[] userNickname = {"neeck1", "neeck2", "nick3", "nick4", "nick5", "nick6"};
        String[] userUsername = {"user1", "user2he", "user3he", "user1", "user2he", "user3he"};
        String[] userRealname = {"Real Name 1", "Real Name 2A", "Real Name 3A", "Real Name 1", "Real Name 2A", "Real Name 3A"};
        String[] userHost = {"HostName1", "HostName2", "HostName2", "HostName1", "HostName2", "HostName2"};
        String[] userMode = {"0", "0", "0", "0", "0", "0"};
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
        icp.getRequestor().setConnection(Connection.create());

        ircCommand = "ISON";
        icp.setParsingString(ircCommand);
        prefix = Globals.thisIrcServer.get().getHostname();
        response = "451" + " " + "" + " " + ":" + "You have not registered";
        icp.ircParse();
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertTrue("Not registered", reply.equals(":" + prefix + " " + response));
        
        for (i = 0; i < requestor.length; i++) {
            icp.setRequestor(User.create());
            ((User) icp.getRequestor()).setIrcServer(Globals.thisIrcServer.get());
            icp.getRequestor().setConnection(Connection.create());
            
            ircCommand = "NICK";
            icp.setParsingString(ircCommand + " " + userNickname[i]);
            icp.ircParse();
            requestor[i] = icp.getRequestor();
            ((User) icp.getRequestor()).setHostname(userHost[i]);
        
            ircCommand = "USER";
            icp.setParsingString(ircCommand + " " + userUsername[i] + " " + userMode[i] + " " + "*" + " " + ":" + userRealname[i]);
            icp.ircParse();
            
        }
        
        dropHistory();
        for (i = 0; i < 20; i++) {
            icp.setRequestor(User.create());
            ((User) icp.getRequestor()).setIrcServer(Globals.thisIrcServer.get());
            icp.getRequestor().setConnection(Connection.create());
            
            ircCommand = "NICK";
            icp.setParsingString(ircCommand + " " + "nick" + i);
            icp.ircParse();
            ((User) icp.getRequestor()).setHostname("host" + i);
        
            ircCommand = "USER";
            icp.setParsingString(ircCommand + " " + "user" + i + " " + 0 + " " + "*" + " " + ":" + "User Usrson" + i);
            icp.ircParse();
            icp.getRequestor().getOutputQueue().clear();
        }
        
//       303    RPL_ISON ":*1<nick> *( " " <nick> )"
//         - Reply format used by ISON to list replies to the query list.

        i = 0;
        icp.setRequestor(db.getUser("nick" + i));
        ((User) icp.getRequestor()).setIrcServer(Globals.anonymousIrcServer.get());
        icp.getRequestor().getOutputQueue().clear();
        
        ircCommand = "ISON";
        String userList = "alpha beta gamma";
        icp.setParsingString(ircCommand + " " + userList);
        icp.ircParse();
        assertTrue("No reply, remote client", icp.getRequestor().getOutputQueue().isEmpty());
        
        icp.setRequestor(db.getUser("neeck1"));
        icp.getRequestor().getOutputQueue().clear();
        ((User) icp.getRequestor()).setIrcServer(Globals.thisIrcServer.get());
        ircCommand = "ISON";
        userList = "alpha beta gamma";
        icp.setParsingString(ircCommand + " " + userList);
        response = "303" + " " + userNickname[0] + " " + ":";
        icp.ircParse();
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertTrue("RPL_ISON nicks not found", reply.equals(":" + prefix + " " + response));
        
        icp.setRequestor(db.getUser("neeck1"));
        icp.getRequestor().getOutputQueue().clear();
        ircCommand = "ISON";
        
        userList = "";
        for (i = 0; i < 10; i++) { 
            userList = userList + "nick" + i + " ";
        }
        userList = userList.substring(0, userList.length() - 1);
        icp.setParsingString(ircCommand + " " + userList);
        response = "303" + " " + userNickname[0] + " " + ":" + userList;
        icp.ircParse();
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertTrue("RPL_ISON found first 10 nicks", reply.equals(":" + prefix + " " + response));
        
        //461    ERR_NEEDMOREPARAMS "<command> :Not enough parameters"
        
        icp.setRequestor(requestor[0]);
        icp.getRequestor().getOutputQueue().clear();
        prefix = Globals.thisIrcServer.get().getHostname();
        servername = Globals.thisIrcServer.get().getHostname();
        ircCommand = "ISON";
        icp.setParsingString(ircCommand);
        prefix = servername;
        response = "461" + " " + userNickname[0] + " " + ircCommand + " " + ":" + "Not enough parameters";
        icp.ircParse();
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertTrue("ERR_NEEDMOREPARAMS", reply.equals(":" + prefix + " " + response));
        
        System.out.println("**ISON***************************************OK**");
    }   
}    

