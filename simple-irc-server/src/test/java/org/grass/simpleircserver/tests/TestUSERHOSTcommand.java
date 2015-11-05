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

public class TestUSERHOSTcommand extends TestIrcCommand {
    public void run() {
        System.out.println("--USERHOST---------------------------------------");
        
        IrcCommandParser icp = new IrcCommandParser();
        
        dropAll();
        serverInit();
        serviceInit();

        IrcTalker[] requestor = new IrcTalker[6];
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

        ircCommand = "USERHOST";
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
            
            icp.getRequestor().getOutputQueue().clear();
        }
        
        //461    ERR_NEEDMOREPARAMS "<command> :Not enough parameters"
        
        icp.setRequestor(requestor[0]);
        prefix = Globals.thisIrcServer.get().getHostname();
        servername = Globals.thisIrcServer.get().getHostname();
        
        ircCommand = "USERHOST";
        icp.setParsingString(ircCommand);
        prefix = servername;
        response = "461" + " " + userNickname[0] + " " + ircCommand + " " + ":" + "Not enough parameters";
        icp.ircParse();
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertTrue("ERR_NEEDMOREPARAMS", reply.equals(":" + prefix + " " + response));
        
        for (i = 0; i < 3; i++) {
            icp.setRequestor(requestor[i]);
            
            ircCommand = "OPER";
            icp.setParsingString(ircCommand + " " + "user" +" " +"test");
            icp.ircParse();
            icp.getRequestor().getOutputQueue().clear();
        }
        
        for (i = 2; i < 5; i++) {
            icp.setRequestor(requestor[i]);
            
            ircCommand = "AWAY";
            content = "Away text.";
            icp.setParsingString(ircCommand + " " + ":" + content);
            icp.ircParse();
        }
        icp.getRequestor().getOutputQueue().clear();

//       302    RPL_USERHOST ":*1<reply> *( " " <reply> )"

//         - Reply format used by USERHOST to list replies to
//           the query list.  The reply string is composed as
//           follows:

//           reply = nickname [ "*" ] "=" ( "+" / "-" ) hostname

//           The '*' indicates whether the client has registered
//           as an Operator.  The '-' or '+' characters represent
//           whether the client has set an AWAY message or not
//           respectively.

        icp.setRequestor(requestor[0]);
        
        ircCommand = "USERHOST";
        String userList = "alpha beta gamma";
        icp.setParsingString(ircCommand + " " + userList);
        prefix = Globals.thisIrcServer.get().getHostname();
        icp.ircParse();
        response = "302" + " " + userNickname[0] + " " + ":";
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertTrue("USERHOST nicks not found", reply.equals(":" + prefix + " " + response));

        ircCommand = "USERHOST";
        userList = "";
        for (i = 0; i < userNickname.length; i++ ) {
            userList = userList + userNickname[i] + " ";
        }
        icp.setParsingString(ircCommand + " " + userList);
        prefix = Globals.thisIrcServer.get().getHostname();
        
        icp.ircParse();
        
        response = "302" + " " + userNickname[0] + " " + ":";
        for (i = 0; i < 2; i++) {
            response = response + userNickname[i] + "*" + "=" + "+" + userHost[i] + " ";
        }
        i = 2;
        response = response + userNickname[i] + "*" + "=" + "-" + userHost[i];
        for (i = 3; i < 5; i++) {
            response = response + " " + userNickname[i] + "" + "=" + "-" + userHost[i];
        }
        
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertTrue("USERHOST found 5 nicks", reply.equals(":" + prefix + " " + response));
        
        System.out.println("**USERHOST***********************************OK**");
    }   
}    

