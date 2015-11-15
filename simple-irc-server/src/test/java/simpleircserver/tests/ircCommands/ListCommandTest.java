/*
 * 
 * ListCommandTest
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
import simpleircserver.talker.user.User;
import simpleircserver.tests.IrcCommandTest;

/**
 * ListCommandTest
 * @version 0.5.3.1 2015-11-06 
 * @author  Nikolay Kirdin
 */
public class ListCommandTest extends IrcCommandTest {
	
    @Test
    public void listCommandTest() {
        System.out.println("--LIST-------------------------------------------");
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

        String[] channelName = {"#channel", "#channel2", "#channel3", "#channel4"};

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

        ircCommand = "LIST";
        icp.setParsingString(ircCommand + " " + channelName[0]);
        prefix = Globals.thisIrcServer.get().getHostname();
        response = "451" + " " + "" + " " + ":" + "You have not registered";
        icp.ircParse();
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertTrue(reply.equals(":" + prefix + " " + response));
        
        ircCommand = "NICK";
        icp.setParsingString(ircCommand + " " + userNickname);
        icp.ircParse();
        
        ircCommand = "USER";
        icp.setParsingString(ircCommand + " " + userUsername + " " + userMode + " " + "*" + " " + ":" + userRealname);
        icp.ircParse();
        icp.getRequestor().getOutputQueue().clear();

        ircCommand = "LIST";
        icp.setParsingString(ircCommand);
        prefix = Globals.thisIrcServer.get().getHostname();
        responseCode = "323";
        responseMsg = "End of LIST";
        response = responseCode + " " + userNickname + " " + ":" + responseMsg;
        icp.ircParse();
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertTrue("RPL_LISTEND", reply.equals(":" + prefix + " " + response));
        
        ircCommand = "JOIN";
        icp.setParsingString(ircCommand + " " + channelName[0]);
        prefix = userNickname;
        response = ircCommand + " " + channelName[0];
        icp.ircParse();
        icp.getRequestor().getOutputQueue().clear();
        
        ircCommand = "LIST";
        icp.setParsingString(ircCommand);
        prefix = Globals.thisIrcServer.get().getHostname();
        responseCode = "322";
        response = responseCode + " " + userNickname + " " + channelName[0] + " " + "1" + " " + ":";
        icp.ircParse();
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertTrue("RPL_LIST", reply.equals(":" + prefix + " " + response));
        
        responseCode = "323";
        responseMsg = "End of LIST";
        response = responseCode + " " + userNickname + " " + ":" + responseMsg;
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertTrue("RPL_LISTEND", reply.equals(":" + prefix + " " + response));

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
        icp.setParsingString(ircCommand + " " + channelName[1]);
        icp.ircParse();
        icp.getRequestor().getOutputQueue().clear();

        ircCommand = "LIST";
        icp.setParsingString(ircCommand);
        icp.ircParse();
        prefix = Globals.thisIrcServer.get().getHostname();
        responseCode = "322";
        response = responseCode + " " + userNickname2 + " " + channelName[0] + " " + "1" + " " + ":";
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertTrue("RPL_LIST", reply.equals(":" + prefix + " " + response));
        
        prefix = Globals.thisIrcServer.get().getHostname();
        responseCode = "322";
        response = responseCode + " " + userNickname2 + " " + channelName[1] + " " + "1" + " " + ":";
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertTrue("RPL_LIST", reply.equals(":" + prefix + " " + response));
        
        responseCode = "323";
        responseMsg = "End of LIST";
        response = responseCode + " " + userNickname2 + " " + ":" + responseMsg;
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertTrue("RPL_LISTEND", reply.equals(":" + prefix + " " + response));

        icp.setRequestor(db.getUser(userNickname));
        
        ircCommand = "MODE";
        String channelMode = "+p";
        icp.setParsingString(ircCommand + " " + channelName[0] + " " + channelMode);
        icp.ircParse();
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        
        icp.setRequestor(db.getUser(userNickname2));
        
        ircCommand = "LIST";
        icp.setParsingString(ircCommand);
        icp.ircParse();
        prefix = Globals.thisIrcServer.get().getHostname();
        responseCode = "322";
        response = responseCode + " " + userNickname2 + " " + channelName[1] + " " + "1" + " " + ":";
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertTrue("RPL_LIST", reply.equals(":" + prefix + " " + response));

        responseCode = "323";
        responseMsg = "End of LIST";
        response = responseCode + " " + userNickname2 + " " + ":" + responseMsg;
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertTrue("RPL_LISTEND", reply.equals(":" + prefix + " " + response));
        
        icp.setRequestor(db.getUser(userNickname));

        ircCommand = "MODE";
        userMode = "-p";
        icp.setParsingString(ircCommand + " " + channelName[0] + " " + userMode);
        icp.ircParse();
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        
        icp.setRequestor(db.getUser(userNickname2));
        
        ircCommand = "LIST";
        icp.setParsingString(ircCommand);
        icp.ircParse();
        prefix = Globals.thisIrcServer.get().getHostname();
        responseCode = "322";
        response = responseCode + " " + userNickname2 + " " + channelName[0] + " " + "1" + " " + ":";
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertTrue("RPL_LIST", reply.equals(":" + prefix + " " + response));

        responseCode = "322";
        response = responseCode + " " + userNickname2 + " " + channelName[1] + " " + "1" + " " + ":";
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertTrue("RPL_LIST", reply.equals(":" + prefix + " " + response));

        responseCode = "323";
        responseMsg = "End of LIST";
        response = responseCode + " " + userNickname2 + " " + ":" + responseMsg;
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertTrue("RPL_LISTEND", reply.equals(":" + prefix + " " + response));
        
        icp.setRequestor(db.getUser(userNickname));

        ircCommand = "MODE";
        userMode = "+s";
        icp.setParsingString(ircCommand + " " + channelName[0] + " " + userMode);
        icp.ircParse();
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        
        icp.setRequestor(db.getUser(userNickname2));
        
        ircCommand = "LIST";
        icp.setParsingString(ircCommand);
        icp.ircParse();
        prefix = Globals.thisIrcServer.get().getHostname();
        responseCode = "322";
        response = responseCode + " " + userNickname2 + " " + channelName[1] + " " + "1" + " " + ":";
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertTrue("RPL_LIST", reply.equals(":" + prefix + " " + response));
        
        responseCode = "323";
        responseMsg = "End of LIST";
        response = responseCode + " " + userNickname2 + " " + ":" + responseMsg;
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertTrue("RPL_LISTEND", reply.equals(":" + prefix + " " + response));
        
        icp.setRequestor(db.getUser(userNickname));
        
        ircCommand = "MODE";
        channelMode = "-s";
        icp.setParsingString(ircCommand + " " + channelName[0] + " " + channelMode);
        icp.ircParse();
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        
        icp.setRequestor(db.getUser(userNickname2));
        
        ircCommand = "LIST";
        icp.setParsingString(ircCommand);
        icp.ircParse();
        prefix = Globals.thisIrcServer.get().getHostname();
        responseCode = "322";
        response = responseCode + " " + userNickname2 + " " + channelName[0] + " " + "1" + " " + ":";
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertTrue("RPL_LIST", reply.equals(":" + prefix + " " + response));

        responseCode = "322";
        response = responseCode + " " + userNickname2 + " " + channelName[1] + " " + "1" + " " + ":";
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertTrue("RPL_LIST", reply.equals(":" + prefix + " " + response));

        responseCode = "323";
        responseMsg = "End of LIST";
        response = responseCode + " " + userNickname2 + " " + ":" + responseMsg;
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertTrue("RPL_LISTEND", reply.equals(":" + prefix + " " + response));

        icp.setRequestor(db.getUser(userNickname));
        
        ircCommand = "QUIT";
        icp.setParsingString(ircCommand);
        icp.ircParse();
        
        icp.setRequestor(db.getUser(userNickname2));
        
        ircCommand = "LIST";
        icp.setParsingString(ircCommand);
        icp.ircParse();
        prefix = Globals.thisIrcServer.get().getHostname();
        responseCode = "322";
        response = responseCode + " " + userNickname2 + " " + channelName[1] + " " + "1" + " " + ":";
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertTrue("RPL_LIST", reply.equals(":" + prefix + " " + response));

        responseCode = "323";
        responseMsg = "End of LIST";
        response = responseCode + " " + userNickname2 + " " + ":" + responseMsg;
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertTrue("RPL_LISTEND", reply.equals(":" + prefix + " " + response));
        
        ircCommand = "LIST";
        String serverMask = "example.com";
        icp.setParsingString(ircCommand + " " + channelName[1] + " " + serverMask);
        icp.ircParse();
        prefix = Globals.thisIrcServer.get().getHostname();
        //402    ERR_NOSUCHSERVER "<server name> :No such server"
        responseCode = "402";
        response = responseCode + " " + userNickname2 + " " + serverMask + " " + ":" + "No such server";
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertTrue("ERR_NOSUCHSERVER", reply.equals(":" + prefix + " " + response));
        
        ircCommand = "QUIT";
        icp.setParsingString(ircCommand);
        icp.ircParse();
        
        icp.setRequestor(service[0]);
        ircCommand = "LIST";
        icp.setParsingString(ircCommand + " " + channelName[0]);
        prefix = Globals.thisIrcServer.get().getHostname();
        response = "421" + " " + service[0].getNickname() + " " + ircCommand + " " + ":Unknown command";
        icp.ircParse();
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertTrue(reply.equals(":" + prefix + " " + response));
                
        System.out.println("**LIST***************************************OK**");
    }   
}    

