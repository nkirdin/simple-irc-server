/*
 * 
 * KickCommandTest
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

import org.junit.Test;

import simpleircserver.base.Globals;
import simpleircserver.connection.Connection;
import simpleircserver.parser.IrcCommandParser;
import simpleircserver.talker.IrcTalker;
import simpleircserver.talker.user.User;
import simpleircserver.tests.IrcCommandTest;

/**
 * KickCommandTest
 * @version 0.5.3.1 2015-11-06 
 * @author  Nikolay Kirdin
 */
public class KickCommandTest extends IrcCommandTest {
	
    @Test
    public void kickCommandTest() {
        System.out.println("--KICK-------------------------------------------");
        String reply;
        String prefix = Globals.thisIrcServer.get().getHostname();
        String ircCommand;
        String response;
        String responseCode;

        int i;
        
        dropAll();
        serverInit();
        serviceInit();
        userInit(); 
        operatorInit();
        
        IrcCommandParser icp = new IrcCommandParser();


        icp.setRequestor(User.create());
        ((User) icp.getRequestor()).setIrcServer(Globals.thisIrcServer.get());
        icp.getRequestor().setConnection(Connection.create());

        // 451    ERR_NOTREGISTERED ":You have not registered"

        ircCommand = "KICK";
        icp.setParsingString(ircCommand + " " + channelName[0] + " " + userNickname[0]);
        response = "451" + " " + "" + " " + ":" + "You have not registered";
        icp.ircParse();
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertEquals("Not registered", ":" + prefix + " " + response, reply);
        
        icp.setRequestor(db.getUser(userNickname[0]));        
        
        ircCommand = "JOIN";
        icp.setParsingString(ircCommand + " " + channelName[0]);
        icp.ircParse();
        icp.getRequestor().getOutputQueue().clear();
         
        for (IrcTalker ircTalker : requestor) {
            if (ircTalker.getOutputQueue() != null) ircTalker.getOutputQueue().clear();
        }
                
        ircCommand = "OPER";
        icp.setParsingString(ircCommand + " " + "operatorname1" + " " + "operatorpassword1");
        icp.ircParse();
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        
        // 461    ERR_NEEDMOREPARAMS "<command> :Not enough parameters"
        ircCommand = "KICK";
        icp.setParsingString(ircCommand);
        icp.ircParse();
        responseCode = "461";
        response = responseCode + " " + userNickname[0] + " " + ircCommand + " " + ":" + "Not enough parameters";
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertEquals("ERR_NEEDMOREPARAMS", ":" + prefix + " " + response, reply);
        
        // 461    ERR_NEEDMOREPARAMS "<command> :Not enough parameters"
        ircCommand = "KICK";
        icp.setParsingString(ircCommand + " " + channelName[0]);
        icp.ircParse();
        responseCode = "461";
        response = responseCode + " " + userNickname[0] + " " + ircCommand + " " + ":" + "Not enough parameters";
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertEquals("ERR_NEEDMOREPARAMS", ":" + prefix + " " + response, reply);
        
        ircCommand = "KICK";
        icp.setParsingString(ircCommand + " " + channelName[1] + " " + userNickname[1] );
        icp.ircParse();
        //403    ERR_NOSUCHCHANNEL "<channel name> :No such channel"
        responseCode = "403";
        response = responseCode + " " + userNickname[0] + " " + channelName[1] + " " + ":" + "No such channel";
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertEquals("check ERR_NOSUCHCHANNEL", ":" + prefix + " " + response, reply);
        
        ircCommand = "KICK";
        String name = "nOsUch";
        icp.setParsingString(ircCommand + " " + channelName[0] + " " + name);
        icp.ircParse();
        //401    ERR_NOSUCHNICK "<nick> :No such nick/channel"
        responseCode = "401";
        response = responseCode + " " + userNickname[0] + " " + name + " " + ":" + "No such nick/channel";
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertEquals("check ERR_NOSUCHNICK", ":" + prefix + " " + response, reply);
        
        ircCommand = "MODE";
        icp.setParsingString(ircCommand + " " + userNickname[0] + " " + "-o");
        icp.ircParse();
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        
        ircCommand = "KICK";
        icp.setParsingString(ircCommand + " " + channelName[0] + " " + userNickname[1]);
        icp.ircParse();
        //441    ERR_USERNOTINCHANNEL "<nick> <channel> :They aren't on that channel"
        responseCode = "441";
        response = responseCode + " " + userNickname[0] + " " + userNickname[1] + " " + channelName[0] + " " + ":" + "They aren't on that channel";
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertEquals("check ERR_USERNOTINCHANNEL", ":" + prefix + " " + response, reply);
 
        for (i = 1; i < requestor.length; i++ ) {
            icp.setRequestor(requestor[i]);
            ircCommand = "JOIN";
            icp.setParsingString(ircCommand + " " + channelName[0]);
            icp.ircParse();
        }
        
        icp.setRequestor(db.getUser(userNickname[1]));
        ircCommand = "JOIN";
        icp.setParsingString(ircCommand + " " + channelName[1]);
        icp.ircParse();

        for (IrcTalker ircTalker : requestor) {
            if (ircTalker.getOutputQueue() != null) ircTalker.getOutputQueue().clear();
        }
        
        icp.setRequestor(db.getUser(userNickname[0]));
        // 442    ERR_NOTONCHANNEL "<channel> :You're not on that channel"
        ircCommand = "KICK";
        icp.setParsingString(ircCommand + " " + channelName[1] + " " + userNickname[1]);
        icp.ircParse();
        responseCode = "442";
        response = responseCode + " " + userNickname[0] + " " + channelName[1] + " " + ":" + "You're not on that channel";
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertEquals("ERR_NOTONCHANNEL", ":" + prefix + " " + response, reply);
        
        icp.setRequestor(db.getUser(userNickname[1]));
        ircCommand = "PART";
        icp.setParsingString(ircCommand + " " + channelName[1]);
        icp.ircParse();
        
        for (IrcTalker ircTalker : requestor) {
            if (ircTalker.getOutputQueue() != null) ircTalker.getOutputQueue().clear();
        }
        
        ircCommand = "KICK";
        icp.setParsingString(ircCommand + " " + channelName[0] + " " + userNickname[1]);
        icp.ircParse();
        //482    ERR_CHANOPRIVSNEEDED "<channel> :You're not channel operator"
        responseCode = "482";
        response = responseCode + " " + userNickname[1] + " " + channelName[0] + " " + ":" + "You're not channel operator";
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertEquals("check ERR_CHANOPRIVSNEEDED for selfkicking", ":" + prefix + " " + response, reply);
        
        ircCommand = "KICK";
        icp.setParsingString(ircCommand + " " + channelName[0] + " " + userNickname[2]);
        icp.ircParse();
        //482    ERR_CHANOPRIVSNEEDED "<channel> :You're not channel operator"
        responseCode = "482";
        response = responseCode + " " + userNickname[1] + " " + channelName[0] + " " + ":" + "You're not channel operator";
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertEquals("check ERR_CHANOPRIVSNEEDED", ":" + prefix + " " + response, reply);
        
        icp.setRequestor(db.getUser(userNickname[0]));
        ircCommand = "KICK";
        icp.setParsingString(ircCommand + " " + channelName[0] + " " + userNickname[1] + "," + userNickname[2]);
        icp.ircParse();
        response = ircCommand + " " + channelName[0] + " " + userNickname[1];
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertEquals("Reply to first user (about first kicked).", ":" + userNickname[0] + " " + response, reply);
        
        icp.setRequestor(db.getUser(userNickname[1]));
        response = ircCommand + " " + channelName[0] + " " + userNickname[1];
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertEquals("Reply to second user (about first kicked).", ":" + userNickname[0] + " " + response, reply);
        
        icp.setRequestor(db.getUser(userNickname[2]));
        response = ircCommand + " " + channelName[0] + " " + userNickname[1];
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertEquals("Reply to third user (about first kicked).", ":" + userNickname[0] + " " + response, reply);
        
        icp.setRequestor(db.getUser(userNickname[0]));
        response = ircCommand + " " + channelName[0] + " " + userNickname[2];
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertEquals("Reply to first user (about second kicked).", ":" + userNickname[0] + " " + response, reply);
        
        icp.setRequestor(db.getUser(userNickname[2]));
        response = ircCommand + " " + channelName[0] + " " + userNickname[2];
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertEquals("Reply to second user (about second kicked).", ":" + userNickname[0] + " " + response, reply);

        icp.setRequestor(db.getUser(userNickname[0]));
        ircCommand = "JOIN";
        icp.setParsingString(ircCommand + " " + channelName[1]);
        icp.ircParse();
        
        icp.setRequestor(db.getUser(userNickname[1]));
        ircCommand = "JOIN";
        icp.setParsingString(ircCommand + " " + channelName[0]);
        icp.ircParse();
        
        icp.setRequestor(db.getUser(userNickname[2]));
        ircCommand = "JOIN";
        icp.setParsingString(ircCommand + " " + channelName[1]);
        icp.ircParse();
        
        for (IrcTalker ircTalker : requestor) {
            if (ircTalker.getOutputQueue() != null) ircTalker.getOutputQueue().clear();
        }
        
        icp.setRequestor(db.getUser(userNickname[0]));
        
        ircCommand = "KICK";
        icp.setParsingString(ircCommand + " " + channelName[0] + "," + channelName[1] + " " + userNickname[1] + "," + userNickname[2]);
        icp.ircParse();
        response = ircCommand + " " + channelName[0] + " " + userNickname[1];
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertEquals("Reply to first user (about first kicked from first channel).", ":" + userNickname[0] + " " + response, reply);
        
        icp.setRequestor(db.getUser(userNickname[1]));
        response = ircCommand + " " + channelName[0] + " " + userNickname[1];
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertEquals("Reply to second user (about first kicked from first channel).", ":" + userNickname[0] + " " + response, reply);

        icp.setRequestor(db.getUser(userNickname[0]));
        response = ircCommand + " " + channelName[1] + " " + userNickname[2];
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertEquals("Reply to first user (about second kicked from second channel).", ":" + userNickname[0] + " " + response, reply);
        
        icp.setRequestor(db.getUser(userNickname[2]));
        response = ircCommand + " " + channelName[1] + " " + userNickname[2];
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertEquals("Reply to second user (about second kicked from second channel).", ":" + userNickname[0] + " " + response, reply);
        
        icp.setRequestor(db.getUser(userNickname[0]));
        ircCommand = "KICK";
        icp.setParsingString(ircCommand + " " + channelName[0] + " " + userNickname[0]);
        icp.ircParse();
        response = ircCommand + " " + channelName[0] + " " + userNickname[0];
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertEquals("for chopers selfkicking is allowed", ":" + userNickname[0] + " " + response, reply);
        
        icp.setRequestor(db.getService(serviceNickname[0]));                
        ircCommand = "KICK";
        icp.setParsingString(ircCommand + " " + channelName[0] + " " + userNickname[0]);
        response = "421" + " " + service[0].getNickname() + " " + ircCommand + " " + ":Unknown command";
        icp.ircParse();
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertEquals("Unknown command", ":" + prefix + " " + response, reply);
                
        System.out.println("**KICK***************************************OK**");
    }   
}    

