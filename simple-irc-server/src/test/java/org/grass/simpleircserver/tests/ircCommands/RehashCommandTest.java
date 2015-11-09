/*
 * 
 * RehashCommandTest
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

import org.grass.simpleircserver.base.Globals;
import org.grass.simpleircserver.connection.Connection;
import org.grass.simpleircserver.parser.IrcCommandParser;
import org.grass.simpleircserver.talker.IrcTalker;
import org.grass.simpleircserver.talker.user.User;
import org.grass.simpleircserver.tests.IrcCommandTest;
import org.junit.Test;

/**
 * RehashCommandTest
 * @version 0.5.3.1 2015-11-06 
 * @author  Nikolay Kirdin
 */
public class RehashCommandTest extends IrcCommandTest {
	
	@Test
    public void rehashCommandTest() {
        System.out.println("--REHASH-----------------------------------------");
        
        IrcCommandParser icp = new IrcCommandParser();
        
        dropAll();
        serverInit();
        serviceInit();
        operatorInit();

        IrcTalker[] requestor = new IrcTalker[3];
        String[] userNickname = {"neeck1", "neeck2", "nick3"};
        String[] userUsername = {"user1", "user2he", "user3he"};
        String[] userRealname = {"Real Name 1", "Real Name 2A", "Real Name 3A"};
        String[] userHost = {"HostName1", "HostName2", "HostName2"};
        String[] userMode = {"0", "0", "0"};
        
        String reply;
        String prefix;
        String ircCommand;
        String response;

        int i;
        
        icp.setRequestor(User.create());
        ((User) icp.getRequestor()).setIrcServer(Globals.thisIrcServer.get());
        icp.getRequestor().setConnection(Connection.create());

        ircCommand = "REHASH";
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
        
        ((User) requestor[0]).setIrcServer(ircServer[0]);
        ircCommand = "REHASH";
        icp.setParsingString(ircCommand);
        prefix = Globals.thisIrcServer.get().getHostname();
        icp.ircParse();
        assertTrue("no reply, only local users accepted", icp.getRequestor().getOutputQueue().isEmpty());        
        
        ((User) requestor[0]).setIrcServer(Globals.thisIrcServer.get());
        ircCommand = "REHASH";
        icp.setParsingString(ircCommand);
        prefix = Globals.thisIrcServer.get().getHostname();
        // 481    ERR_NOPRIVILEGES ":Permission Denied- You're not an IRC operator"
        response = "481" + " " + userNickname[0] + " " + ":" + "Permission Denied- You're not an IRC operator";
        icp.ircParse();
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertTrue("ERR_NOPRIVILEGES", reply.equals(":" + prefix + " " + response));
        
        ircCommand = "OPER";
        icp.setParsingString(ircCommand + " " + "operatorname1" + " " + "operatorpassword1");
        prefix = Globals.thisIrcServer.get().getHostname();
        icp.ircParse();
        reply = icp.getRequestor().getOutputQueue().poll().getReport();

        ircCommand = "REHASH";
        icp.setParsingString(ircCommand);
        prefix = Globals.thisIrcServer.get().getHostname();
        //382    RPL_REHASHING "<config file> :Rehashing"
        response = "382" + " " + userNickname[0] + " " + Globals.configFilename.get() + " " + ":" + "Rehashing";
        icp.ircParse();
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertTrue("RPL_REHASHING", reply.equals(":" + prefix + " " + response));
        
        System.out.println("**REHASH*************************************OK**");
    }   
}    

