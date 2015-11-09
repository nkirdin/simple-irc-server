/*
 * 
 * InviteCommandTest
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

import org.grass.simpleircserver.base.Globals;
import org.grass.simpleircserver.connection.Connection;
import org.grass.simpleircserver.parser.IrcCommandParser;
import org.grass.simpleircserver.talker.user.User;
import org.grass.simpleircserver.tests.IrcCommandTest;
import org.junit.Test;

/**
 * InviteCommandTest
 * @version 0.5.3.1 2015-11-06 
 * @author  Nikolay Kirdin
 */
public class InviteCommandTest extends IrcCommandTest {
	
	@Test
    public void inviteCommandTest() {
        System.out.println("--INVITE-----------------------------------------");
        String reply;
        String prefix;
        String ircCommand;
        String userNickname;
        String userUsername;
        String userRealname;
        String response;
        String userMode;
        String channelMode;
        String responseCode;

        String[] channelName2 = {"#channel", "#channel2", "#channel3", "#channel4"};

        
        dropAll();
        serviceInit();
        
        IrcCommandParser icp = new IrcCommandParser();

        icp.setRequestor(User.create());
        ((User) icp.getRequestor()).setIrcServer(Globals.thisIrcServer.get());
        icp.getRequestor().setConnection(Connection.create());

        userNickname = "nick";
        ircCommand = "USER";
        userUsername = "user";
        userRealname = "User Userson";
        userMode = "0";
        // 451    ERR_NOTREGISTERED ":You have not registered"

        ircCommand = "INVITE";
        String userNickname2 = "john";
        icp.setParsingString(ircCommand + " " + userNickname2 + " " + channelName2[0]);
        prefix = Globals.thisIrcServer.get().getHostname();
        response = "451" + " " + "" + " " + ":" + "You have not registered";
        icp.ircParse();
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertEquals("Not registered", reply, ":" + prefix + " " + response);
        
        ircCommand = "NICK";
        icp.setParsingString(ircCommand + " " + userNickname);
        icp.ircParse();
        
        ircCommand = "USER";
        icp.setParsingString(ircCommand + " " + userUsername + " " + userMode + " " + "*" + " " + ":" + userRealname);
        icp.ircParse();
        icp.getRequestor().getOutputQueue().clear();
        
        ircCommand = "JOIN";
        icp.setParsingString(ircCommand + " " + channelName2[0]);
        icp.ircParse();
        icp.getRequestor().getOutputQueue().clear();
        
        ircCommand = "MODE";
        channelMode = "+i";
        icp.setParsingString(ircCommand + " " + channelName2[0] + " " + channelMode);
        icp.ircParse();
        reply = icp.getRequestor().getOutputQueue().poll().getReport();

        icp.setRequestor(User.create());
        ((User) icp.getRequestor()).setIrcServer(Globals.thisIrcServer.get());
        icp.getRequestor().setConnection(Connection.create());

        ircCommand = "NICK";
        icp.setParsingString(ircCommand + " " + userNickname2);
        icp.ircParse();
        
        ircCommand = "USER";
        icp.setParsingString(ircCommand + " " + userUsername + " " + userMode + " " + "*" + " " + ":" + userRealname);
        icp.ircParse();
        icp.getRequestor().getOutputQueue().clear();
        
        ircCommand = "JOIN";
        icp.setParsingString(ircCommand + " " + channelName2[0]);
        icp.ircParse();
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        prefix = Globals.thisIrcServer.get().getHostname();
        //473    ERR_INVITEONLYCHAN "<channel> :Cannot join channel (+i)"
        responseCode = "473";
        response = responseCode + " " + userNickname2 + " " + channelName2[0] + " " + ":" + "Cannot join channel (+i)";
        assertEquals("Check ERR_INVITEONLYCHAN", reply, ":" + prefix + " " + response);

        icp.setRequestor(db.getUser(userNickname));

        // 461    ERR_NEEDMOREPARAMS "<command> :Not enough parameters"
        ircCommand = "INVITE";
        icp.setParsingString(ircCommand + " " + userNickname2);
        icp.ircParse();
        prefix = Globals.thisIrcServer.get().getHostname();
        responseCode = "461";
        response = responseCode + " " + userNickname + " " + ircCommand + " " + ":" + "Not enough parameters";
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertEquals("ERR_NEEDMOREPARAMS", reply, ":" + prefix + " " + response);
        
        ircCommand = "INVITE";
        icp.setParsingString(ircCommand + " " + userNickname2 + " " + channelName2[0]);
        icp.ircParse();
        prefix = Globals.thisIrcServer.get().getHostname();
        //341    RPL_INVITING "<channel> <nick>"
        responseCode = "341";
        response = responseCode + " " + userNickname + " " + channelName2[0] + " " + userNickname2;
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertEquals("check RPL_INVITING for first user", reply, ":" + prefix + " " + response);
        
        icp.setRequestor(db.getUser(userNickname2));
        responseCode = "341";
        response = responseCode + " " + userNickname + " " + channelName2[0] + " " + userNickname2;
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertEquals("check RPL_INVITING for second user", reply, ":" + prefix + " " + response);
        
        icp.setRequestor(db.getUser(userNickname));
        ircCommand = "INVITE";
        icp.setParsingString(ircCommand + " " + userNickname2 + " " + channelName2[0]);
        icp.ircParse();
        prefix = Globals.thisIrcServer.get().getHostname();
        //443    ERR_USERONCHANNEL "<user> <channel> :is already on channel"
        responseCode = "443";
        response = responseCode + " " + userNickname + " " + userNickname2 + " " + channelName2[0] + " " + ":" + "is already on channel";
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertEquals("check ERR_USERONCHANNEL", reply, ":" + prefix + " " + response);
       
        ircCommand = "INVITE";
        String userNickname3 = "paul";
        icp.setParsingString(ircCommand + " " + userNickname3 + " " + channelName2[0]);
        icp.ircParse();
        prefix = Globals.thisIrcServer.get().getHostname();
        //401    ERR_NOSUCHNICK "<nickname> :No such nick/channel"
        responseCode = "401";
        response = responseCode + " " + userNickname + " " + userNickname3 + " " + ":" + "No such nick/channel";
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertEquals("check ERR_NOSUCHNICK", reply, ":" + prefix + " " + response);

        icp.setRequestor(User.create());
        ((User) icp.getRequestor()).setIrcServer(Globals.thisIrcServer.get());
        icp.getRequestor().setConnection(Connection.create());

        userNickname3 = "paul"; 
        ircCommand = "NICK";
        icp.setParsingString(ircCommand + " " + userNickname3);
        icp.ircParse();
        
        ircCommand = "USER";
        icp.setParsingString(ircCommand + " " + userUsername + " " + userMode + " " + "*" + " " + ":" + userRealname);
        icp.ircParse();
        icp.getRequestor().getOutputQueue().clear();
        
        icp.setRequestor(db.getUser(userNickname2));
                
        ircCommand = "INVITE";
        icp.setParsingString(ircCommand + " " + userNickname3 + " " + channelName2[0]);
        icp.ircParse();
        prefix = Globals.thisIrcServer.get().getHostname();
        //482    ERR_CHANOPRIVSNEEDED "<channel> :You're not channel operator"
        responseCode = "482";
        response = responseCode + " " + userNickname2 + " " + channelName2[0] + " " + ":" + "You're not channel operator";
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertEquals("check ERR_CHANOPRIVSNEEDED", reply, ":" + prefix + " " + response);

        ircCommand = "JOIN";
        icp.setParsingString(ircCommand + " " + "0");
        icp.ircParse();
        icp.getRequestor().getOutputQueue().clear();
        
        icp.setRequestor(db.getUser(userNickname));
        icp.getRequestor().getOutputQueue().clear();

        ircCommand = "MODE";
        channelMode = "-i";
        icp.setParsingString(ircCommand + " " + channelName2[0] + " " + channelMode);
        icp.ircParse();
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        
        icp.setRequestor(db.getUser(userNickname2));
        icp.getRequestor().getOutputQueue().clear();

        ircCommand = "INVITE";
        icp.setParsingString(ircCommand + " " + userNickname3 + " " + channelName2[0]);
        icp.ircParse();
        prefix = Globals.thisIrcServer.get().getHostname();
        //442    ERR_NOTONCHANNEL "<channel> :You're not on that channel"
        responseCode = "442";
        response = responseCode + " " + userNickname2 + " " + channelName2[0] + " " + ":" + "You're not on that channel";
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertEquals("check ERR_NOTONCHANNEL", reply, ":" + prefix + " " + response);

        icp.setRequestor(db.getUser(userNickname));
        
        ircCommand = "QUIT";
        icp.setParsingString(ircCommand);
        icp.ircParse();
        
        icp.setRequestor(db.getUser(userNickname2));
        
        ircCommand = "QUIT";
        icp.setParsingString(ircCommand);
        icp.ircParse();
        
        icp.setRequestor(db.getUser(userNickname3));
        
        ircCommand = "QUIT";
        icp.setParsingString(ircCommand);
        icp.ircParse();
        
        icp.setRequestor(service[0]);
        ircCommand = "INVITE";
        icp.setParsingString(ircCommand + " " + userNickname + " " + channelName2[0]);
        prefix = Globals.thisIrcServer.get().getHostname();
        response = "421" + " " + service[0].getNickname() + " " + ircCommand + " " + ":Unknown command";
        icp.ircParse();
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertEquals("INVITE: ", reply, ":" + prefix + " " + response);
                
        System.out.println("**INVITE*************************************OK**");
    }   
}    

