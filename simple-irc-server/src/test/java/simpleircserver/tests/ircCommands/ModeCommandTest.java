/*
 * 
 * ModeCommandTest
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
 * ModeCommandTest
 * @version 0.5.3.1 2015-11-06 
 * @author  Nikolay Kirdin
 */
public class ModeCommandTest extends IrcCommandTest {
	
    @Test
    public void modeCommandTest() {
        System.out.println("--MODE-------------------------------------------");
        
        String reply;
        String prefix;
        String ircCommand;
        String response;
        String [] errouneous;
        String [] correctous;
        
        int i;

        IrcCommandParser icp = new IrcCommandParser();

        dropAll();
        serverInit();
        userInit(); operatorInit();
        serviceInit();

        icp.setRequestor(User.create());
        ((User) icp.getRequestor()).setIrcServer(Globals.thisIrcServer.get());
        icp.getRequestor().setConnection(Connection.create());
        
        ircCommand = "MODE";
        icp.setParsingString(ircCommand + " " + userUsername[0]);
        prefix = Globals.thisIrcServer.get().getHostname();
        response = "451" + " " + "" + " " + ":" + "You have not registered";
        icp.ircParse();
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertTrue(reply.equals(":" + prefix + " " + response));

        icp.setRequestor(db.getUser(userNickname[0]));
        
        ircCommand = "MODE";
        prefix = Globals.thisIrcServer.get().getHostname();
        icp.setParsingString(ircCommand);
        //461    ERR_NEEDMOREPARAMS  "<command> :Not enough parameters"
        response = "461" + " " + userNickname[0] + " " + ircCommand + " " + ":" + "Not enough parameters";
        icp.ircParse();
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertTrue("ERR_NEEDMOREPARAMS", reply.equals(":" + prefix + " " + response));
        
        ircCommand = "MODE";
        prefix = Globals.thisIrcServer.get().getHostname();
        icp.setParsingString(ircCommand + " " + userNickname[0] + " " + "+");
        //461    ERR_NEEDMOREPARAMS  "<command> :Not enough parameters"
        response = "461" + " " + userNickname[0] + " " + ircCommand + " " + ":" + "Not enough parameters";
        icp.ircParse();
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertTrue("ERR_NEEDMOREPARAMS", reply.equals(":" + prefix + " " + response));
        
        ircCommand = "MODE";
        prefix = Globals.thisIrcServer.get().getHostname();
        icp.setParsingString(ircCommand + " " + userNickname[0] + " " + "-");
        //461    ERR_NEEDMOREPARAMS  "<command> :Not enough parameters"
        response = "461" + " " + userNickname[0] + " " + ircCommand + " " + ":" + "Not enough parameters";
        icp.ircParse();
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertTrue("ERR_NEEDMOREPARAMS", reply.equals(":" + prefix + " " + response));

        ircCommand = "MODE";
        prefix = Globals.thisIrcServer.get().getHostname();
        icp.setParsingString(ircCommand + " " + userNickname[1]);
        // 502    ERR_USERSDONTMATCH ":Cannot change mode for other users"
        response = "502" + " " + userNickname[0] + " " + ":Cannot change mode for other users";
        icp.ircParse();
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertTrue("ERR_USERSDONTMATCH", reply.equals(":" + prefix + " " + response));
       
        errouneous= new String[] {"+o", "+O", "+a", "+oOa", "o", "O", "i", "w"
        , "r", "a", "s", "-a", "-r", "-ar", "+g", "-fm"};
        
        for (String userModeString : errouneous) {
            ircCommand = "MODE";
            prefix = Globals.thisIrcServer.get().getHostname();
            icp.setParsingString(ircCommand + " " + userNickname[0] + " " + userModeString);
            // 501    ERR_UMODEUNKNOWNFLAG ":Unknown MODE flag"
            response = "501" + " " + userNickname[0] + " " + ":Unknown MODE flag";
            icp.ircParse();
            reply = icp.getRequestor().getOutputQueue().poll().getReport();
            assertTrue("ERR_UMODEUNKNOWNFLAG", reply.equals(":" + prefix + " " + response));
        }
        
        correctous= new String[] {"+i", "-i", "+w", "-w", "+r", "+iwr", "-o", "-O", "-oO", "-i", "-w", "-iwoO"};
        
        for (String userModeString : correctous) {
            ircCommand = "MODE";
            prefix = userNickname[0];
            icp.setParsingString(ircCommand + " " + userNickname[0] + " " + userModeString);
            icp.ircParse();
            assertTrue((userModeString.length() - 1) == icp.getRequestor().getOutputQueue().size());
            for (i = 1; i < userModeString.length(); i++) {
                reply = icp.getRequestor().getOutputQueue().poll().getReport();
                response = ircCommand + " " + userModeString.substring(0,1) + userModeString.substring(i, i + 1);
                assertTrue(reply.equals(":" + prefix + " " + response));
            }
            //221    RPL_UMODEIS "<user mode string>"

            ircCommand = "MODE";
            prefix = Globals.thisIrcServer.get().getHostname();
            icp.setParsingString(ircCommand + " " + userNickname[0]);
            icp.ircParse();
            reply = icp.getRequestor().getOutputQueue().poll().getReport();
            switch(userModeString.charAt(0)) {
            case '-':
                response = "221" + " " + userNickname[0] + " " + "\\+" + "[^" + userModeString.substring(1, userModeString.length()) + "]*";
                assertTrue(reply.matches(":" + prefix + " " + response));
                break;
            case '+':
                response = "221" + " " + userNickname[0] + " " + "\\+" + "[" + userModeString.substring(1, userModeString.length()) + "]+";
                assertTrue(reply.matches(":" + prefix + " " + response));
                break;
            default:
                assertTrue(false);
                break;
            } 
            
        }
        System.out.println("**MODE***************************************OK**");
    }
}    

