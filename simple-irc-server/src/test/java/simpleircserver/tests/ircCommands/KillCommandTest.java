/*
 * 
 * KillCommandTest
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import simpleircserver.base.Globals;
import simpleircserver.connection.Connection;
import simpleircserver.parser.IrcCommandParser;
import simpleircserver.talker.IrcTalker;
import simpleircserver.talker.user.User;
import simpleircserver.tests.IrcCommandTest;

/**
 * KillCommandTest
 * @version 0.5.3.1 2015-11-06 
 * @author  Nikolay Kirdin
 */
public class KillCommandTest extends IrcCommandTest {
	
    @Test
    public void killCommandTest() {
        System.out.println("--KILL-------------------------------------------");
        IrcCommandParser icp = new IrcCommandParser();
        
        dropAll();
        serverInit();
        serviceInit();
        operatorInit();

        String[] userNickname = {"neeck1", "neeck2", "nick3"};
        String[] userUsername = {"user1", "user2", "user3"};
        String[] userRealname = {"Real Name 1", "Real Name 2A", "Real Name 3A"};
        String[] userHost = {"HostName1", "HostName2", "HostName2"};
        String[] userMode = {"0", "0", "0"};
        
        String reply;
        String prefix = Globals.thisIrcServer.get().getHostname();;
        String ircCommand;
        String response;
        String info;

        
        icp.setRequestor(User.create());
        ((User) icp.getRequestor()).setIrcServer(Globals.thisIrcServer.get());
        Connection conn = Connection.create();
        
        conn.ircTalker.set(icp.getRequestor());
        icp.getRequestor().setConnection(conn);
        db.register(conn);

        
        ircCommand = "KILL";
        String comment = "You're too stupid."; 
        icp.setParsingString(ircCommand);
        icp.ircParse();
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        
        response = "451" + " " + "" + " " + ":" + "You have not registered";
        assertEquals("Not registered", ":" + prefix + " " + response, reply);
        
        for (int i = 0; i < userNickname.length - 1 ; i++) {
            icp.setRequestor(User.create());
            ((User) icp.getRequestor()).setIrcServer(Globals.thisIrcServer.get());
            conn = Connection.create();
            
            conn.ircTalker.set(icp.getRequestor());
            icp.getRequestor().setConnection(conn);
            db.register(conn);

            ((User) icp.getRequestor()).setIrcServer(ircServer[i]);
            
            ircCommand = "NICK";
            icp.setParsingString(ircCommand + " " + userNickname[i]);
            icp.ircParse();
            ((User) icp.getRequestor()).setHostname(userHost[i]);
        
            ircCommand = "USER";
            icp.setParsingString(ircCommand + " " + userUsername[i] + " " + userMode[i] + " " + "*" + " " + ":" + userRealname[i]);
            icp.ircParse();
            icp.getRequestor().getOutputQueue().clear();
        }
        
        icp.setRequestor(db.getUser(userNickname[0]));
        
        //481    ERR_NOPRIVILEGES ":Permission Denied- You're not an IRC operator"

        ircCommand = "KILL";
        icp.setParsingString(ircCommand + " " + userNickname[1] + " " + ":" + comment);
        icp.ircParse();
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        
        response = "481" + " " + userNickname[0] + " " + ":" + "Permission Denied- You're not an IRC operator";
        assertEquals("ERR_NOPRIVILEGES", ":" + prefix + " " + response, reply);
        
        ircCommand = "OPER";
        icp.setParsingString(ircCommand + " " + "operatorname1" + " " + "operatorpassword1");
        icp.ircParse();
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        
        // 461    ERR_NEEDMOREPARAMS "<command> :Not enough parameters"
        
        ircCommand = "KILL";
        icp.setParsingString(ircCommand);
        icp.ircParse();
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        
        response = "461" + " " + userNickname[0] + " " + ircCommand  + " " +":" + "Not enough parameters";
        assertEquals("ERR_NEEDMOREPARAMS", ":" + prefix + " " + response, reply);
        
        ircCommand = "KILL";
        icp.setParsingString(ircCommand + " " + userNickname[1] + " " + ":");
        icp.ircParse();
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        response = "461" + " " + userNickname[0] + " " + ircCommand  + " " +":" + "Not enough parameters";
        assertTrue("ERR_NEEDMOREPARAMS 2", reply.equals(":" + prefix + " " + response));
        
        ircCommand = "KILL";
        icp.setParsingString(ircCommand + " " + userNickname[2] + " " + ":" + comment);
        icp.ircParse();
        reply = icp.getRequestor().getOutputQueue().poll().getReport();

        response = "401" + " " + userNickname[0] + " " + userNickname[2] + " " +":" + "No such nick/channel";
        assertEquals("ERR_NOSUCHNICK", ":" + prefix + " " + response, reply);
        
        int i = userNickname.length - 1;
        icp.setRequestor(User.create());
        ((User) icp.getRequestor()).setIrcServer(Globals.thisIrcServer.get());
        icp.getRequestor().setConnection(Connection.create());       
        ((User) icp.getRequestor()).setIrcServer(ircServer[i]);
        ircCommand = "NICK";
        icp.setParsingString(ircCommand + " " + userNickname[i]);
        icp.ircParse();    

        ((User) icp.getRequestor()).setHostname(userHost[i]);
        ircCommand = "USER";
        icp.setParsingString(ircCommand + " " + userUsername[i] + " " + userMode[i] + " " + "*" + " " + ":" + userRealname[i]);
        icp.ircParse();               
        icp.getRequestor().getOutputQueue().clear(); 
        IrcTalker requestor = icp.getRequestor();
        
        icp.setRequestor(db.getUser(userNickname[0]));
        icp.getRequestor().getOutputQueue().clear();
        
        ircCommand = "KILL";
        icp.setParsingString(ircCommand + " " + userNickname[i] + " " + ":" + comment);
        icp.ircParse();
        reply = requestor.getOutputQueue().poll().getReport();
        
        info = comment + " " + icp.getRequestor().getNickname() + "@" + ((User)icp.getRequestor()).getIrcServer().getHostname();
        response = ircCommand + " " + userNickname[i] + " " +":" + info;        
        assertEquals("Kill message to killing user", ":" + userNickname[0] + " " + response, reply);
        
        System.out.println("**KILL***************************************OK**");
    }   
}    

