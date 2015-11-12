/*
 * 
 * LusersCommandTest
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

import java.util.Iterator;

import org.junit.Test;

import simpleircserver.base.Globals;
import simpleircserver.connection.Connection;
import simpleircserver.parser.IrcCommandParser;
import simpleircserver.talker.IrcTalker;
import simpleircserver.talker.user.User;
import simpleircserver.tests.IrcCommandTest;

/**
 * LusersCommandTest
 * @version 0.5.3.1 2015-11-06 
 * @author  Nikolay Kirdin
 */
public class LusersCommandTest extends IrcCommandTest {
	
    @Test
    public void lusersCommandTest() {
        System.out.println("--LUSERS-----------------------------------------");
        IrcCommandParser icp = new IrcCommandParser();
        
        dropAll();
        serverInit();
        serviceInit();
        operatorInit();

        IrcTalker[] requestor = new IrcTalker[3];
        String[] userNickname = {"nick1", "nick2", "nick3"};
        String[] userUsername = {"user1", "user2", "user3"};
        String[] userRealname = {"Real Name 1", "Real Name 2", "Real Name 3"};
        String[] userMode = {"0", "0", "0"};
        String[] channelName = {"#channel1", "#channel2", "#channel3", "#channel4", "#channel5"};

        String reply;
        String prefix = Globals.thisIrcServer.get().getHostname();
        String ircCommand;
        String response;
        
        icp.setRequestor(User.create());
        ((User) icp.getRequestor()).setIrcServer(Globals.thisIrcServer.get());
        icp.getRequestor().setConnection(Connection.create());
        
        ircCommand = "LUSERS";
        icp.setParsingString(ircCommand);        
        icp.ircParse();
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        
        response = "451" + " " + "" + " " + ":" + "You have not registered";        
        assertEquals("Not registered reply", ":" + prefix + " " + response, reply);
        
        ircCommand = "NICK";
        icp.setParsingString(ircCommand + " " + userNickname[0]);
        icp.ircParse();
        requestor[0] = icp.getRequestor();
        
        ircCommand = "USER";
        icp.setParsingString(ircCommand + " " + userUsername[0] + " " + userMode[0] + " " + "*" + " " + ":" + userRealname[0]);
        icp.ircParse();
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        icp.getRequestor().getOutputQueue().clear();
        
        ircCommand = "JOIN";
        icp.setParsingString(ircCommand + " " + channelName[0]);
        icp.ircParse();
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        icp.getRequestor().getOutputQueue().clear();
        
        ircCommand = "OPER";
        icp.setParsingString(ircCommand + " " + "operatorname1" + " " + "operatorpassword1");
        //381    RPL_YOUREOPER   ":You are now an IRC operator"
        icp.ircParse();
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        
        response = "381" + " " + userNickname[0] + " " + ":You are now an IRC operator";        
        assertEquals("RPL_YOUREOPER", ":" + prefix + " " + response, reply);
        
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {}
        
        int numOfUsers = 0, numOfServers = 0, numOfOperators = 0, numOfServices = 0, numOfChannels = 0;
                            
        numOfServers = db.getIrcServerSet().size();    
        
        numOfServices = db.getServiceSet().size();    
        
        numOfChannels = db.getChannelSet().size();    
                    
        for (Iterator<User> userSetIterator = db.getUserSetIterator(); userSetIterator.hasNext();) {
            User user = userSetIterator.next();
            numOfUsers++;
            if (user.isOperator()) {
                numOfOperators++;
            }
        }
        
        ircCommand = "LUSERS";
        icp.setParsingString(ircCommand);
        //251    RPL_LUSERCLIENT ":There are <integer> users and <integer> services on <integer> servers"
        icp.ircParse();
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        
        response = "251" + " " + userNickname[0] + " " + ":There are " + String.format("%d", numOfUsers) 
                + " users and " + String.format("%d", numOfServices) + " services on " 
                + String.format("%d", numOfServers) + " servers";
        assertEquals("RPL_LUSERCLIENT", ":" + prefix + " " + response, reply);
        
        //252    RPL_LUSEROP "<integer> :operator(s) online"
        reply = icp.getRequestor().getOutputQueue().poll().getReport();

        response = "252" + " " + userNickname[0] + " " + String.format("%d", numOfOperators) + " " + ":" + "operator(s) online";
        assertEquals("RPL_LUSEROP", ":" + prefix + " " + response, reply);
        
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        //254    RPL_LUSERCHANNELS "<integer> :channels formed"
        response = "254" + " " + userNickname[0] + " " + String.format("%d", numOfChannels) + " " + ":" + "channels formed";
        assertEquals("RPL_LUSERCHANNELS", ":" + prefix + " " + response, reply);
        
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        //255    RPL_LUSERME ":I have <integer> clients and <integer> servers"
        response = "255" + " " + userNickname[0] + " " + ":" + "I have " + String.format("%d", numOfUsers) + " clients and " + String.format("%d", numOfServers) + " servers";
        assertEquals("RPL_LUSERME", ":" + prefix + " " + response, reply);
        
        //253    RPL_LUSERUNKNOWN "<integer> :unknown connection(s)"

        ircCommand = "LUSERS";
        String mask = "*.irc.example.com";
        String serverMask = "irc.example.com";
        icp.setParsingString(ircCommand + " " + mask + " " + serverMask);
        //402    ERR_NOSUCHSERVER "<server name> :No such server"
        icp.ircParse();
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        response = "402" + " " + userNickname[0] + " " + serverMask + " " + ":" + "No such server";
        assertEquals("ERR_NOSUCHSERVER", ":" + prefix + " " + response, reply);
        
        ircCommand = "QUIT";
        icp.setParsingString(ircCommand);
        icp.ircParse();
        
        System.out.println("**LUSERS*************************************OK**");
    }   
}    

