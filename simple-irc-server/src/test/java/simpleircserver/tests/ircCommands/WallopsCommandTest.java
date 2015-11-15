/*
 * 
 * WallopsCommandTest
 * is part of Simple Irc Server
 *
 *
 * Copyright (С) 2012, 2015, Nikolay Kirdin
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

package simpleircserver.tests.ircCommands;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import simpleircserver.base.Globals;
import simpleircserver.connection.Connection;
import simpleircserver.parser.IrcCommandParser;
import simpleircserver.talker.IrcTalker;
import simpleircserver.talker.user.User;
import simpleircserver.tests.IrcCommandTest;

/**
 * WallopsCommandTest
 * @version 0.5.3.1 2015-11-06 
 * @author  Nikolay Kirdin
 */
public class WallopsCommandTest extends IrcCommandTest {
	
    @Test
    public void wallopsCommandTest() {
        System.out.println("--WALLOPS----------------------------------------");
        
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
        
        String reply;
        String servername;
        String prefix;
        String ircCommand;
        String response;
        String content;

        int i;
        
        icp.setRequestor(User.create());
        ((User) icp.getRequestor()).setIrcServer(Globals.thisIrcServer.get());
        icp.getRequestor().setConnection(Connection.create());

        ircCommand = "WALLOPS";
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
            ((User) icp.getRequestor()).setIrcServer(ircServer[i]);
        
            ircCommand = "USER";
            icp.setParsingString(ircCommand + " " + userUsername[i] + " " + userMode[i] + " " + "*" + " " + ":" + userRealname[i]);
            icp.ircParse();
         
            icp.getRequestor().getOutputQueue().clear();
        }
        
        icp.setRequestor(requestor[0]);
        prefix = Globals.thisIrcServer.get().getHostname();
        servername = Globals.thisIrcServer.get().getHostname();
        
        //461    ERR_NEEDMOREPARAMS "<command> :Not enough parameters"

        ircCommand = "WALLOPS";
        icp.setParsingString(ircCommand);
        prefix = servername;
        response = "461" + " " + userNickname[0] + " " + ircCommand + " " + ":" + "Not enough parameters";
        icp.ircParse();
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertTrue("ERR_NEEDMOREPARAMS", reply.equals(":" + prefix + " " + response));
        
        ircCommand = "WALLOPS";
        icp.setParsingString(ircCommand + " " + ":");
        prefix = servername;
        response = "461" + " " + userNickname[0] + " " + ircCommand + " " + ":" + "Not enough parameters";
        icp.ircParse();
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertTrue("ERR_NEEDMOREPARAMS 2", reply.equals(":" + prefix + " " + response));
        
        ircCommand = "WALLOPS";
        content = "Message to all users who set the 'w' flag.";
        icp.setParsingString(ircCommand + " " + ":" + content);
        prefix = userNickname[0];
        icp.ircParse();
        assertTrue("Nothing, no such users", icp.getRequestor().getOutputQueue().isEmpty());
        
        for (i = 0; i < requestor.length; i++) {
            icp.setRequestor(requestor[i]);
            ircCommand = "MODE";
            icp.setParsingString(ircCommand + " " + userNickname[i] + " " + "+w");
            icp.ircParse();
            icp.getRequestor().getOutputQueue().clear();
        }
        
        icp.setRequestor(requestor[0]);
        
        ircCommand = "WALLOPS";
        icp.setParsingString(ircCommand + " " + ":" + content);
        response = ircCommand + " " + ":" + content;
        prefix = userNickname[0];
        icp.ircParse();
        
        icp.setRequestor(requestor[1]);
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertTrue("WALLOPS to second user", reply.equals(":" + prefix + " " + response));
        icp.setRequestor(requestor[2]);
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertTrue("WALLOPS to third user", reply.equals(":" + prefix + " " + response));
        
        System.out.println("**WALLOPS************************************OK**");
    }   
}    

