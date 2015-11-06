/*
 * 
 * ConnectCommandTest
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
 * ConnectCommandTest
 * @version 0.5.3.1 2015-11-06 
 * @author  Nikolay Kirdin
 */
public class ConnectCommandTest extends TestIrcCommand {
	
	@Test
    public void connectCommandTest() {
        System.out.println("--CONNECT----------------------------------------");
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

        ircCommand = "CONNECT";
        icp.setParsingString(ircCommand);
        prefix = Globals.thisIrcServer.get().getHostname();
        response = "451" + " " + "" + " " + ":" + "You have not registered";
        icp.ircParse();
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertEquals("CONNECT: Not registered reply", reply, ":" + prefix + " " + response);
        
        icp.setRequestor(requestor[0]);
        
        // 481    ERR_NOPRIVILEGES ":Permission Denied- You're not an IRC operator"
        
        ircCommand = "CONNECT";
        icp.setParsingString(ircCommand + " " + targetServer + " " + port + " " + remoteServer);
        prefix = Globals.thisIrcServer.get().getHostname();
        response = "481" + " " + icp.getRequestor().getNickname() + " " + ":" + "Permission Denied- You're not an IRC operator";
        icp.ircParse();
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertEquals("ERR_NOPRIVILEGES", reply, ":" + prefix + " " + response);
        
        ircCommand = "OPER";
        icp.setParsingString(ircCommand + " " + "user" + " " + "test");
        prefix = Globals.thisIrcServer.get().getHostname();
        icp.ircParse();
        icp.getRequestor().getOutputQueue().clear();
        
        //461    ERR_NEEDMOREPARAMS "<command> :Not enough parameters"
        ircCommand = "CONNECT";
        icp.setParsingString(ircCommand);
        prefix = Globals.thisIrcServer.get().getHostname();
        response = "461" + " " + icp.getRequestor().getNickname() + " " + ircCommand + " " + ":" + "Not enough parameters";
        icp.ircParse();
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertEquals("ERR_NEEDMOREPARAMS", reply, ":" + prefix + " " + response);
        
        ircCommand = "CONNECT";
        icp.setParsingString(ircCommand + " " + targetServer);
        prefix = Globals.thisIrcServer.get().getHostname();
        response = "461" + " " + icp.getRequestor().getNickname() + " " + ircCommand + " " + ":" + "Not enough parameters";
        icp.ircParse();
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertEquals("ERR_NEEDMOREPARAMS 2", reply, ":" + prefix + " " + response);
        
        // 402    ERR_NOSUCHSERVER "<server name> :No such server"

        ircCommand = "CONNECT";
        icp.setParsingString(ircCommand + " " + targetServer + " " + port);// + " " + remoteServer;
        prefix = Globals.thisIrcServer.get().getHostname();
        //402    ERR_NOSUCHSERVER "<server name> :No such server"
        icp.ircParse();
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        response = "402" + " " + icp.getRequestor().getNickname() + " " + targetServer + " " + ":" + "No such server";
        assertEquals("ERR_NOSUCHSERVER", reply, ":" + prefix + " " + response);
        
        ircCommand = "CONNECT";
        icp.setParsingString(ircCommand + " " + ircServer[0].getHostname() + " " + port + " " + remoteServer);
        prefix = Globals.thisIrcServer.get().getHostname();
        //402    ERR_NOSUCHSERVER "<server name> :No such server"
        icp.ircParse();
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        response = "402" + " " + icp.getRequestor().getNickname() + " " + remoteServer + " " + ":" + "No such server";
        assertEquals("ERR_NOSUCHSERVER 2", reply, ":" + prefix + " " + response);
        
        //Local executing.
        ircCommand = "CONNECT";
        icp.setParsingString(ircCommand + " " + Globals.thisIrcServer.get().getHostname() + " " + port);// + " " + remoteServer;
        prefix = Globals.thisIrcServer.get().getHostname();
        icp.ircParse();
        response = "WALLOPS" + " " + ":" + icp.getRequestor().getNickname() + " " + ircCommand + " " + Globals.thisIrcServer.get().getHostname() + " " + port;
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertEquals("Local executing. WALLOPS", reply, ":" + prefix + " " + response);
         
        //Local executing remote request.
        icp.setRequestor(requestor[1]);
        
        ircCommand = "MODE";
        icp.setParsingString(ircCommand + " " + icp.getRequestor().getNickname() + " " + "+w");
        icp.ircParse();        
        icp.getRequestor().getOutputQueue().clear();
        
        icp.setRequestor(requestor[0]);
        
        ircCommand = "CONNECT";
        icp.setParsingString(ircCommand + " " + ircServer[0].getHostname() + " " + port + " " + Globals.thisIrcServer.get().getHostname());
        prefix = Globals.thisIrcServer.get().getHostname();
        icp.ircParse();
        // reply = icp.getRequestor().getOutputQueue().poll().getReport();
        response = "WALLOPS" + " " + ":" + icp.getRequestor().getNickname() + " " + ircCommand + " " + ircServer[0].getHostname() + " " + port + " " + Globals.thisIrcServer.get().getHostname();
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertEquals("Local executing 2. Get WALLOPS", reply, ":" + prefix + " " + response);
        
        icp.setRequestor(requestor[1]);
        
        // Check WALLOPS
        prefix = Globals.thisIrcServer.get().getHostname();
        response = "WALLOPS" + " " + ":" + requestor[0].getNickname() + " " + ircCommand + " " + ircServer[0].getHostname() + " " + port + " " + Globals.thisIrcServer.get().getHostname();
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertEquals("Receiving WALLOPS", reply, ":" + prefix + " " + response);
        
        ircCommand = "QUIT";
        icp.setParsingString(ircCommand);
        icp.ircParse();
                
        icp.setRequestor(requestor[0]);
        icp.getRequestor().getOutputQueue().clear();
        
        //Forwarding.
        ircCommand = "CONNECT";
        icp.setParsingString(ircCommand + " " + ircServer[0].getHostname() + " " + port + " " + ircServer[1].getHostname());
        prefix = userNickname[0];
        icp.ircParse();
        //reply = icp.getRequestor().getOutputQueue().poll().getReport();
        response = ircCommand + " " + ircServer[0].getHostname() + " " + port + " " + ircServer[1].getHostname();
        assertTrue("Forwarding. No reply", icp.getRequestor().getOutputQueue().isEmpty());

        ircCommand = "QUIT";
        icp.setParsingString(ircCommand);
        icp.ircParse();
        
        System.out.println("**CONNECT************************************OK**");
    }   
}    

