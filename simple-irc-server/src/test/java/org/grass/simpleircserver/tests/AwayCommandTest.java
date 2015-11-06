/*
 * 
 * AwayCommandTest
 * is part of Simple Irc Server
 *
 *
 * Copyright (С) 2012, Nikolay Kirdin
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License Version 3.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License Version 3 for more details.
 *
 * You should have received a copy of the GNU Lesser General Public 
 * License Version 3 along with this program.  If not, see 
 * <http://www.gnu.org/licenses/>.
 *
 */

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


/**
 * AwayCommandTest
 * @version 0.5.3.1 2015-11-06 
 * @author  Nikolay Kirdin
 */
public class AwayCommandTest extends TestIrcCommand {
	
	@Test
    public void awayCommandTest() {
        System.out.println("--AWAY-------------------------------------------");
        
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


        ircCommand = "AWAY";
        icp.setParsingString(ircCommand);
        prefix = Globals.thisIrcServer.get().getHostname();
        response = "451" + " " + "" + " " + ":" + "You have not registered";
        icp.ircParse();
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertEquals("Not registered", reply, ":" + prefix + " " + response);
        
        for (i = 0; i < requestor.length; i++) {
            icp.setRequestor(User.create());
            ((User) icp.getRequestor()).setIrcServer(Globals.thisIrcServer.get());
            conn = Connection.create();
            
            conn.ircTalker.set(icp.getRequestor());
            icp.getRequestor().setConnection(conn);
            db.register(conn);


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
        }
        
        icp.setRequestor(requestor[1]);
        ((User) icp.getRequestor()).setIrcServer(Globals.thisIrcServer.get());   
        
        prefix = Globals.thisIrcServer.get().getHostname();

        ircCommand = "AWAY";
        icp.setParsingString(ircCommand);
        prefix = Globals.thisIrcServer.get().getHostname();
        //305    RPL_UNAWAY ":You are no longer marked as being away"
        response = "305" + " " + userNickname[1] + " " + ":" + "You are no longer marked as being away";
        icp.ircParse();
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertEquals("RPL_UNAWAY", reply,":" + prefix + " " + response);
        
        ircCommand = "AWAY";
        content = "This is a sample away text.";
        icp.setParsingString(ircCommand + " " + ":" + content);
        prefix = Globals.thisIrcServer.get().getHostname();
        //306    RPL_NOWAWAY ":You have been marked as being away"
        response = "306" + " " + userNickname[1] + " " + ":" + "You have been marked as being away";
        icp.ircParse();
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertEquals("RPL_AWAY", reply, ":" + prefix + " " + response);
        
        ((User) requestor[0]).setIrcServer(Globals.thisIrcServer.get());
        icp.setRequestor(requestor[0]);
        ircCommand = "PRIVMSG";
        String msg = "msg msg msg";
        icp.setParsingString(ircCommand + " " + userNickname[1] + " " + ":" + msg);
        prefix = Globals.thisIrcServer.get().getHostname();
        //301    RPL_AWAY "<nick> :<away message>"
        response = "301" + " " + userNickname[0] + " " + userNickname[1] + " " + ":" + content;
        icp.ircParse();
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertEquals("RPL_AWAY local requestor", reply, ":" + prefix + " " + response);
        
        ircCommand = "PRIVMSG";
        ((User) requestor[0]).setIrcServer(ircServer[0]);
        msg = "msg msg msg";
        icp.setParsingString(ircCommand + " " + userNickname[1] + " " + ":" + msg);
        prefix = userNickname[0];
        response = ircCommand + " " + userNickname[1] + " " + ":" + msg;
        icp.ircParse();
        icp.setRequestor(requestor[1]);
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertEquals("remote requestor no RPL_AWAY", reply, ":" + prefix + " " + response);
        
        ircCommand = "AWAY";
        icp.setParsingString(ircCommand);
        prefix = Globals.thisIrcServer.get().getHostname();
        //305    RPL_UNAWAY ":You are no longer marked as being away"
        response = "305" + " " + userNickname[1] + " " + ":" + "You are no longer marked as being away";
        icp.ircParse();
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertEquals("RPL_UNAWAY", reply, ":" + prefix + " " + response);
        
        icp.setRequestor(requestor[0]);
        ircCommand = "PRIVMSG";
        msg = "msg msg msg";
        icp.setParsingString(ircCommand + " " + userNickname[1] + " " + ":" + msg);
        prefix = userNickname[0];
        response = ircCommand + " " + userNickname[1] + " " + ":" + msg;
        icp.ircParse();
        icp.setRequestor(requestor[1]);
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertEquals("no RPL_AWAY", reply, ":" + prefix + " " + response);
        
        System.out.println("**AWAY***************************************OK**");
    }   
}    

