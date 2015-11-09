/*
 * 
 * DieCommandTest
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.grass.simpleircserver.base.Globals;
import org.grass.simpleircserver.connection.Connection;
import org.grass.simpleircserver.parser.IrcCommandParser;
import org.grass.simpleircserver.talker.IrcTalker;
import org.grass.simpleircserver.talker.user.User;
import org.grass.simpleircserver.tests.IrcCommandTest;
import org.junit.Test;

/**
 * DieCommandTest
 * @version 0.5.3.1 2015-11-06 
 * @author  Nikolay Kirdin
 */
public class DieCommandTest extends IrcCommandTest {
	
	@Test
    public void dieCommandTest() {
        System.out.println("--DIE--------------------------------------------");
        
        
        dropAll();
        serverInit();
        serviceInit();
        operatorInit();
        
        IrcCommandParser icp = new IrcCommandParser();

        IrcTalker[] requestor = new IrcTalker[3];
        String[] userNickname = {"neeck1", "neeck2", "nick3"};
        String[] userUsername = {"user1", "user2he", "user3he"};
        String[] userRealname = {"Real Name 1", "Real Name 2A", "Real Name 3A"};
        String[] userHost = {"HostName1", "HostName2", "HostName2"};
        String[] userMode = {"0", "0", "0"};
        
        String reply;
        String prefix = Globals.thisIrcServer.get().getHostname();
        String ircCommand;
        String response;

        int i;
        
        icp.setRequestor(User.create());
        ((User) icp.getRequestor()).setIrcServer(Globals.thisIrcServer.get());
        icp.getRequestor().setConnection(Connection.create());

        ircCommand = "DIE";
        icp.setParsingString(ircCommand);
        icp.ircParse();
        reply = icp.getRequestor().getOutputQueue().poll().getReport();

        response = "451" + " " + "" + " " + ":" + "You have not registered";
        assertEquals("Not registered", ":" + prefix + " " + response, reply);
        
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

        ((User) requestor[0]).setIrcServer(ircServer[0]);
        ircCommand = "DIE";
        icp.setParsingString(ircCommand);
        icp.ircParse();
        assertTrue("no reply, only local users accepted", icp.getRequestor().getOutputQueue().isEmpty());        
        
        ((User) requestor[0]).setIrcServer(Globals.thisIrcServer.get());        
        ircCommand = "DIE";
        icp.setParsingString(ircCommand);
        // 481    ERR_NOPRIVILEGES ":Permission Denied- You're not an IRC operator"
        icp.ircParse();
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        
        response = "481" + " " + userNickname[0] + " " + ":" + "Permission Denied- You're not an IRC operator";
        assertEquals("ERR_NOPRIVILEGES", ":" + prefix + " " + response, reply);
        
        ircCommand = "OPER";
        icp.setParsingString(ircCommand + " " + "operatorname1" + " " + "operatorpassword1");
        icp.ircParse();
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        
        ircCommand = "DIE";
        icp.setParsingString(ircCommand);
        icp.ircParse();
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        
        response = "NOTICE" + " " + userNickname[0] + " " + ":" + userNickname[0] + " " + ircCommand + " " + Globals.thisIrcServer.get().getHostname();
        assertEquals("DIE", ":" + prefix + " " + response, reply);
        
        System.out.println("**DIE****************************************OK**");
    }   
}    

