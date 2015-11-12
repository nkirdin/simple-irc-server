/*
 * 
 * IsonCommandTest
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
 * IsonCommandTest
 * @version 0.5.3.1 2015-11-06 
 * @author  Nikolay Kirdin
 */
public class IsonCommandTest extends IrcCommandTest {
	
    @Test
    public void isonCommandTest() {
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
        
        String reply;
        String servername;
        String prefix;
        String ircCommand;
        String response;

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

