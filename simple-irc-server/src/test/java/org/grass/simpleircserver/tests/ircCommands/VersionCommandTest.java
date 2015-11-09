/*
 * 
 * VersionCommandTest
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

package org.grass.simpleircserver.tests.ircCommands;

import static org.junit.Assert.assertTrue;

import org.grass.simpleircserver.base.Constants;
import org.grass.simpleircserver.base.Globals;
import org.grass.simpleircserver.connection.Connection;
import org.grass.simpleircserver.parser.IrcCommandParser;
import org.grass.simpleircserver.talker.IrcTalker;
import org.grass.simpleircserver.talker.user.User;
import org.grass.simpleircserver.tests.IrcCommandTest;
import org.junit.Test;

/**
 * VersionCommandTest
 * @version 0.5.3.1 2015-11-06 
 * @author  Nikolay Kirdin
 */
public class VersionCommandTest extends IrcCommandTest {
	
	@Test
    public void versionCommandTest() {
        System.out.println("--VERSION----------------------------------------");
        IrcCommandParser icp = new IrcCommandParser();
        
        dropAll();
        serverInit();
        serviceInit();

        IrcTalker[] requestor = new IrcTalker[3];
        String[] userNickname = {"nick1", "nick2", "nick3"};
        String[] userUsername = {"user1", "user2", "user3"};
        String[] userRealname = {"Real Name 1", "Real Name 2", "Real Name 3"};
        String[] userMode = {"0", "0", "0"};

        String reply;
        String servername;
        String prefix;
        String ircCommand;
        String response;
        
        icp.setRequestor(User.create());
        ((User) icp.getRequestor()).setIrcServer(Globals.thisIrcServer.get());
        icp.getRequestor().setConnection(Connection.create());
        icp.getRequestor().setConnection(Connection.create());
        
        ircCommand = "VERSION";
        icp.setParsingString(ircCommand);
        prefix = Globals.thisIrcServer.get().getHostname();
        response = "451" + " " + "" + " " + ":" + "You have not registered";
        icp.ircParse();
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertTrue("Not registered reply", reply.equals(":" + prefix + " " + response));
        
        ircCommand = "NICK";
        icp.setParsingString(ircCommand + " " + userNickname[0]);
        icp.ircParse();
        requestor[0] = icp.getRequestor();
        
        ircCommand = "USER";
        icp.setParsingString(ircCommand + " " + userUsername[0] + " " + userMode[0] + " " + "*" + " " + ":" + userRealname[0]);
        icp.ircParse();
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        icp.getRequestor().getOutputQueue().clear();
        
        ircCommand = "VERSION";
        icp.setParsingString(ircCommand);
        prefix = Globals.thisIrcServer.get().getHostname();
        servername = Globals.thisIrcServer.get().getHostname();
        //351    RPL_VERSION "<version>.<debuglevel> <server> :<comments>"
        icp.ircParse();
        response = "351" + " " + userNickname[0] + " " + Constants.SERVER_VERSION + "." + Globals.logger.get().getLevel() + " " + servername + " " + ":" + Constants.VERSION_COMMENT;
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertTrue("RPL_VERSION", reply.equals(":" + prefix + " " + response));

        ircCommand = "VERSION";
        String serverMask = "irc.example.com";
        icp.setParsingString(ircCommand + " " + serverMask);
        prefix = Globals.thisIrcServer.get().getHostname();
        //402    ERR_NOSUCHSERVER "<server name> :No such server"
        icp.ircParse();
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        response = "402" + " " + userNickname[0] + " " + serverMask + " " + ":" + "No such server";
        assertTrue("ERR_NOSUCHSERVER", reply.equals(":" + prefix + " " + response));
        
        ircCommand = "QUIT";
        icp.setParsingString(ircCommand);
        icp.ircParse();
        
        System.out.println("**VERSION************************************OK**");
    }   
}    

