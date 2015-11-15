/*
 * 
 * NamesCommandTest
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

import java.util.Iterator;

import org.junit.Test;

import simpleircserver.base.Globals;
import simpleircserver.connection.Connection;
import simpleircserver.parser.IrcCommandParser;
import simpleircserver.talker.user.User;
import simpleircserver.tests.IrcCommandTest;

/**
 * NamesCommandTest
 * @version 0.5.3.1 2015-11-06 
 * @author  Nikolay Kirdin
 */
public class NamesCommandTest extends IrcCommandTest {
	
    @Test
    public void namesCommandTest() {
        System.out.println("--NAMES------------------------------------------");
        String reply;
        String prefix;
        String ircCommand;
        String userNickname;
        String userUsername;
        String userRealname;
        String response;
        String userMode;
        String responseCode;
        String responseMsg;

        String[] channelName2 = {"#channel", "#channel2", "#channel3", "#channel4"};

        IrcCommandParser icp = new IrcCommandParser();
        
        dropAll();
        serverInit();
//        userInit(); operatorInit();
        serviceInit();

        icp.setRequestor(User.create());
        ((User) icp.getRequestor()).setIrcServer(Globals.thisIrcServer.get());
        icp.getRequestor().setConnection(Connection.create());
        
        userNickname = "nick";
        ircCommand = "USER";
        userUsername = "user";
        userRealname = "User Userson";
        userMode = "0";
        // 451    ERR_NOTREGISTERED ":You have not registered"

        ircCommand = "NAMES";
        icp.setParsingString(ircCommand + " " + channelName2[0]);
        prefix = Globals.thisIrcServer.get().getHostname();
        response = "451" + " " + "" + " " + ":" + "You have not registered";
        icp.ircParse();
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertTrue("check ERR_NOTREGISTERED", reply.equals(":" + prefix + " " + response));
        
        ircCommand = "NICK";
        icp.setParsingString(ircCommand + " " + userNickname);
        icp.ircParse();
        
        ircCommand = "USER";
        icp.setParsingString(ircCommand + " " + userUsername + " " + userMode + " " + "*" + " " + ":" + userRealname);
        icp.ircParse();
        icp.getRequestor().getOutputQueue().clear();

        ircCommand = "NAMES";
        icp.setParsingString(ircCommand);
        prefix = Globals.thisIrcServer.get().getHostname();
        responseCode = "353";
        
        icp.ircParse();
        for (User user : db.getUserSet()) {
            reply = icp.getRequestor().getOutputQueue().poll().getReport();
            response = responseCode + " " + userNickname + " " + "*" + " " + ":" + user.getNickname();
            assertTrue("RPL_NAMREPLY No channel check", reply.equals(":" + prefix + " " + response));
        }
        responseCode = "366";
        responseMsg = "End of NAMES list";
        response = responseCode + " " + userNickname + " " + "*" + " " + ":" + responseMsg;
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertTrue("RPL_ENDOFNAMES", reply.equals(":" + prefix + " " + response));
        
        ircCommand = "JOIN";
        icp.setParsingString(ircCommand + " " + channelName2[0]);
        icp.ircParse();
        icp.getRequestor().getOutputQueue().clear();
        
        ircCommand = "NAMES";
        icp.setParsingString(ircCommand);
        prefix = Globals.thisIrcServer.get().getHostname();
        responseCode = "353";
        response = responseCode + " " + userNickname + " " + "=" + channelName2[0] + " " + ":" + "@" + userNickname;
        icp.ircParse();
        //System.out.println(icp.getRequestor().getOutputQueue());
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertTrue("RPL_NAMREPLY One channel check", reply.equals(":" + prefix + " " + response));
        responseCode = "366";
        responseMsg = "End of NAMES list";
        response = responseCode + " " + userNickname + " " + channelName2[0] + " " + ":" + responseMsg;
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertTrue("One channel check. End of NAMES", reply.equals(":" + prefix + " " + response));
        responseCode = "353";
        
        for (User user : db.getUserSet()) {
            if (user == db.getUser(userNickname)) {
                continue;
            }
            reply = icp.getRequestor().getOutputQueue().poll().getReport();
            response = responseCode + " " + userNickname + " " + "*" + " " + ":" + user.getNickname();
            assertTrue("RPL_NAMREPLY One channel check", reply.equals(":" + prefix + " " + response));
        }
        responseCode = "366";
        responseMsg = "End of NAMES list";
        response = responseCode + " " + userNickname + " " + "*" + " " + ":" + responseMsg;
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertTrue("RPL_ENDOFNAMES", reply.equals(":" + prefix + " " + response));
        
        icp.setRequestor(User.create());
        ((User) icp.getRequestor()).setIrcServer(Globals.thisIrcServer.get());
        icp.getRequestor().setConnection(Connection.create());

        String userNickname2 = "john"; 
        ircCommand = "NICK";
        icp.setParsingString(ircCommand + " " + userNickname2);
        icp.ircParse();
        
        ircCommand = "USER";
        icp.setParsingString(ircCommand + " " + userUsername + " " + userMode + " " + "*" + " " + ":" + userRealname);
        icp.ircParse();
        icp.getRequestor().getOutputQueue().clear();
        
        ircCommand = "JOIN";
        icp.setParsingString(ircCommand + " " + channelName2[1]);
        icp.ircParse();
        icp.getRequestor().getOutputQueue().clear();

        ircCommand = "NAMES";
        icp.setParsingString(ircCommand);
        icp.ircParse();
        //System.out.println(icp.getRequestor().getOutputQueue());
        prefix = Globals.thisIrcServer.get().getHostname();
        responseCode = "353";
        response = responseCode + " " + userNickname2 + " " + "=" + channelName2[0] + " " + ":" + "@" + userNickname;
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertTrue("RPL_NAMREPLY Two channels, two visible users", reply.equals(":" + prefix + " " + response));
        responseCode = "366";
        responseMsg = "End of NAMES list";
        response = responseCode + " " + userNickname2 + " " + channelName2[0] + " " + ":" + responseMsg;
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertTrue("RPL_ENDOFNAMES Two channels, two visible users. First End of NAMES", reply.equals(":" + prefix + " " + response));
        prefix = Globals.thisIrcServer.get().getHostname();
        responseCode = "353";
        response = responseCode + " " + userNickname2 + " " + "=" + channelName2[1] + " " + ":" + "@" + userNickname2;
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertTrue("RPL_NAMREPLY Two channels, two visible users. Second channel reply.", reply.equals(":" + prefix + " " + response));
        responseCode = "366";
        responseMsg = "End of NAMES list";
        response = responseCode + " " + userNickname2 + " " + channelName2[1] + " " + ":" + responseMsg;
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertTrue("Two channels, two visible users. First End of NAMES", reply.equals(":" + prefix + " " + response));
        
        responseCode = "353";
        
        for (Iterator<User> userSetIterator = db.getUserSetIterator(); 
                userSetIterator.hasNext();) {
            User user = userSetIterator.next();
            if (user == db.getUser(userNickname) || user == db.getUser(userNickname2)) {
                continue;
            }
            reply = icp.getRequestor().getOutputQueue().poll().getReport();
            response = responseCode + " " + userNickname2 + " " + "*" + " " + ":" + user.getNickname();
            assertTrue("RPL_NAMREPLY Two channels, two visible users", reply.equals(":" + prefix + " " + response));
        }
        
        responseCode = "366";
        responseMsg = "End of NAMES list";
        response = responseCode + " " + userNickname2 + " " + "*" + " " + ":" + responseMsg;
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertTrue("RPL_ENDOFNAMES Two channels, two visible users. Last End of NAMES", reply.equals(":" + prefix + " " + response));
        
        icp.setRequestor(db.getUser(userNickname));
        icp.getRequestor().getOutputQueue().clear();
        
        ircCommand = "MODE";
        userMode = "+i";
        icp.setParsingString(ircCommand + " " + userNickname + " " + userMode);
        icp.ircParse();
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        
        icp.setRequestor(db.getUser(userNickname2));
        
        ircCommand = "NAMES";
        icp.setParsingString(ircCommand);
        icp.ircParse();
        //System.out.println(icp.getRequestor().getOutputQueue());
        prefix = Globals.thisIrcServer.get().getHostname();
        responseCode = "366";
        responseMsg = "End of NAMES list";
        response = responseCode + " " + userNickname2 + " " + channelName2[0] + " " + ":" + responseMsg;
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertTrue("RPL_ENDOFNAMES One invisible user. End of NAMES", reply.equals(":" + prefix + " " + response));
        prefix = Globals.thisIrcServer.get().getHostname();
        responseCode = "353";
        response = responseCode + " " + userNickname2 + " " + "=" + channelName2[1] + " " + ":" + "@" + userNickname2;
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertTrue("RPL_NAMREPLY One visible user.", reply.equals(":" + prefix + " " + response));
        responseCode = "366";
        responseMsg = "End of NAMES list";
        response = responseCode + " " + userNickname2 + " " + channelName2[1] + " " + ":" + responseMsg;
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertTrue("RPL_ENDOFNAMES One visible user. End of NAMES", reply.equals(":" + prefix + " " + response));
        
        responseCode = "353";
        for (Iterator<User> userSetIterator = db.getUserSetIterator(); 
                userSetIterator.hasNext();) {
            User user = userSetIterator.next();
            //System.out.println(user);
            if (user == db.getUser(userNickname2)) {
                continue;
            }
            if (user == db.getUser(userNickname)) {
                continue;
            }
            reply = icp.getRequestor().getOutputQueue().poll().getReport();
            response = responseCode + " " + userNickname2 + " " + "*" + " " + ":" + user.getNickname();
            //System.out.println(":" + prefix + " " + response);
            assertTrue("RPL_NAMREPLY Two channels, two visible users", reply.equals(":" + prefix + " " + response));
        }        
        
        responseCode = "366";
        responseMsg = "End of NAMES list";
        response = responseCode + " " + userNickname2 + " " + "*" + " " + ":" + responseMsg;
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        //System.out.println(":" + prefix + " " + response);
        assertTrue("RPL_ENDOFNAMES Visible and invisible users. Last End of NAMES", reply.equals(":" + prefix + " " + response));
        
        icp.setRequestor(db.getUser(userNickname));

        ircCommand = "MODE";
        userMode = "-i";
        icp.setParsingString(ircCommand + " " + userNickname + " " + userMode);
        icp.ircParse();
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        
        ircCommand = "JOIN";
        icp.setParsingString(ircCommand + " " + "0");
        icp.ircParse();
        icp.getRequestor().getOutputQueue().clear();
        
        icp.setRequestor(db.getUser(userNickname2));
        icp.getRequestor().getOutputQueue().clear();
        
        ircCommand = "NAMES";
        icp.setParsingString(ircCommand);
        icp.ircParse();
        prefix = Globals.thisIrcServer.get().getHostname();
        responseCode = "353";
        response = responseCode + " " + userNickname2 + " " + "=" + channelName2[1] + " " + ":" + "@" + userNickname2;
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertTrue("RPL_NAMREPLY Control check one channel.", reply.equals(":" + prefix + " " + response));
        responseCode = "366";
        responseMsg = "End of NAMES list";
        response = responseCode + " " + userNickname2 + " " + channelName2[1] + " " + ":" + responseMsg;
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertTrue("RPL_ENDOFNAMES Control check one channel. End of NAMES", reply.equals(":" + prefix + " " + response));
        responseCode = "353";
        
        for (Iterator<User> userSetIterator = db.getUserSetIterator(); 
                userSetIterator.hasNext();) {
            User user = userSetIterator.next();
            if (user == db.getUser(userNickname2)) {
                continue;
            }
            reply = icp.getRequestor().getOutputQueue().poll().getReport();
            response = responseCode + " " + userNickname2 + " " + "*" + " " + ":" + user.getNickname();
            assertTrue("RPL_NAMREPLY Two channels, two visible users", reply.equals(":" + prefix + " " + response));
        }        
        
        responseCode = "366";
        responseMsg = "End of NAMES list";
        response = responseCode + " " + userNickname2 + " " + "*" + " " + ":" + responseMsg;
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertTrue("RPL_ENDOFNAMES Control check one visible user. End of NAMES", reply.equals(":" + prefix + " " + response));
        
        ircCommand = "NAMES";
        String serverMask = "example.com";
        icp.setParsingString(ircCommand + " " + channelName2[1] + " " + serverMask);
        icp.ircParse();
        prefix = Globals.thisIrcServer.get().getHostname();
        //402    ERR_NOSUCHSERVER "<server name> :No such server"
        responseCode = "402";
        response = responseCode + " " + userNickname2 + " " + serverMask + " " + ":" + "No such server";
        //System.out.println(icp.getRequestor().getOutputQueue());
        //System.out.println(":" + prefix + " " + response);
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        
        assertTrue("Check ERR_NOSUCHSERVER", reply.equals(":" + prefix + " " + response));
        
        ircCommand = "QUIT";
        icp.setParsingString(ircCommand);
        icp.ircParse();
        
        icp.setRequestor(db.getUser(userNickname));

        ircCommand = "QUIT";
        icp.setParsingString(ircCommand);
        icp.ircParse();
                
        icp.setRequestor(service[0]);
        ircCommand = "NAMES";
        icp.setParsingString(ircCommand + " " + channelName2[0]);
        prefix = Globals.thisIrcServer.get().getHostname();
        response = "421" + " " + service[0].getNickname() + " " + ircCommand + " " + ":Unknown command";
        icp.ircParse();
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertTrue(reply.equals(":" + prefix + " " + response));
                
        System.out.println("**NAMES**************************************OK**");
    }   
}    

