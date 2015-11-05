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
public class TestQUITcommand extends TestIrcCommand {
    public void run() {
        System.out.println("--QUIT-------------------------------------------");
        
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
        userInit(); operatorInit();
        serviceInit();

        icp.setRequestor(db.getUser(userNickname[0]));
        
        ircCommand = "QUIT";
        icp.setParsingString(ircCommand);
        prefix = userNickname[0];
        response = "ERROR" + " " + ":Closing Link:" + " .*";
        assertTrue("IrcTalker registered", db.getUser(userNickname[0]) != null);
        assertTrue("IrcTalker operational", icp.getRequestor().getState() == IrcTalkerState.OPERATIONAL);
        icp.ircParse();
        assertTrue("IrcTalker close", icp.getRequestor().getState() == IrcTalkerState.CLOSE);
        //assertTrue("IrcTalker unregistered", db.getUser(userNickname[0]) == null);

        dropUser();
        userInit(); operatorInit();
        icp.setRequestor(db.getUser(userNickname[0]));
        
        ircCommand = "JOIN";
        icp.setParsingString(ircCommand + " " + channelName[0]);
        icp.ircParse();
        
        for (i = 1; i < requestor.length; i++) {
            icp.setRequestor(db.getUser(userNickname[i]));
        
            ircCommand = "JOIN";
            icp.setParsingString(ircCommand + " " + channelName[0]);
            icp.ircParse();
            
        } 
        
        for (IrcTalker ircTalker : requestor) {
            if (ircTalker.getOutputQueue() != null) ircTalker.getOutputQueue().clear();
        }
        
        icp.setRequestor(db.getUser(userNickname[0]));
        
        ircCommand = "QUIT";
        String quitInfo = "I'll be back.";
        icp.setParsingString(ircCommand + " " + ":" + quitInfo);
        icp.ircParse();
        
        prefix = userNickname[0];
        response = ircCommand + " " + ":" + quitInfo;
        
        for (i = 1; i < requestor.length; i++) {
            reply = requestor[i].getOutputQueue().poll().getReport();
            assertTrue(reply.equals(":" + prefix + " " + response));
        }
        
        icp.setRequestor(service[0]);

        ircCommand = "QUIT";
        quitInfo = "I'll serve you later.";
        icp.setParsingString(ircCommand + " " + ":" + quitInfo);
        
        assertTrue("IrcTalker registered", db.getService(icp.getRequestor().getNickname()) != null);
        assertTrue("IrcTalker operational", icp.getRequestor().getState() == IrcTalkerState.OPERATIONAL);
        icp.ircParse();
        assertTrue("IrcTalker close", icp.getRequestor().getState() == IrcTalkerState.CLOSE);
        //assertTrue("IrcTalker unregistered", db.getUser(userNickname[0]) == null);
        
        System.out.println("**QUIT***************************************OK**");

    }   
}    

