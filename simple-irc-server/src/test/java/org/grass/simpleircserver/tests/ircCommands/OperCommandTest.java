/*
 * 
 * OperCommandTest
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
import org.grass.simpleircserver.talker.user.User;
import org.grass.simpleircserver.tests.IrcCommandTest;
import org.junit.Test;

/**
 * OperCommandTest
 * @version 0.5.3.1 2015-11-06 
 * @author  Nikolay Kirdin
 */
public class OperCommandTest extends IrcCommandTest {
	
	@Test
    public void operCommandTest() {
        System.out.println("--OPER-------------------------------------------");
                
        String reply;
        String prefix = Globals.thisIrcServer.get().getHostname();
        String ircCommand;
        String response;

        dropUser();
        userInit(); 
        operatorInit();

        IrcCommandParser icp = new IrcCommandParser();
        
        icp.setRequestor(User.create());
        ((User) icp.getRequestor()).setIrcServer(Globals.thisIrcServer.get());
        icp.getRequestor().setConnection(Connection.create());
        
        String unregisteredNick = "nOSuch";
        
        ircCommand = "OPER";
        icp.setParsingString(ircCommand + " " + unregisteredNick + " " + "userPassword");
        icp.ircParse();
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        
        response = "451" + " " + "" + " " + ":" + "You have not registered";
        assertEquals("Not registered: ", ":" + prefix + " " + response, reply);
        
        icp.setRequestor(db.getUser(userNickname[0]));  
        ircCommand = "OPER";
        icp.setParsingString(ircCommand);
        //461    ERR_NEEDMOREPARAMS  "<command> :Not enough parameters"
        icp.ircParse();
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        
        response = "461" + " " + userNickname[0] + " " + ircCommand + " " + ":" + "Not enough parameters";        
        assertEquals("ERR_NEEDMOREPARAMS", ":" + prefix + " " + response, reply);
        
        ircCommand = "OPER";
        icp.setParsingString(ircCommand + " " + userNickname[0]);
        //461    ERR_NEEDMOREPARAMS  "<command> :Not enough parameters"
        icp.ircParse();
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        
        response = "461" + " " + userNickname[0] + " " + ircCommand + " " + ":" + "Not enough parameters";
        assertEquals("ERR_NEEDMOREPARAMS", ":" + prefix + " " + response, reply);
        
        String errouneous = "A2345678901234567890123456789";
        
        ircCommand = "OPER";
        icp.setParsingString(ircCommand + " " + userUsername[0] + " " + errouneous);
        icp.ircParse();
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        
        response = "421" + " " + userNickname[0] + " " + ircCommand + " " + ":Unknown command";        
        assertEquals("Unknown command: ", ":" + prefix + " " + response, reply);

        String correctous = "0";
        
        ircCommand = "OPER";
        icp.setParsingString(ircCommand + " " + userUsername[0] + " " + correctous);
        //464    ERR_PASSWDMISMATCH     ":Password incorrect"
        icp.ircParse();
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        
        response = "464" + " " + userNickname[0] + " " + ":Password incorrect";        
        assertEquals("Password incorrect: ", ":" + prefix + " " + response, reply);
            
        ircCommand = "OPER";
        icp.setParsingString(ircCommand + " " + "operatorname1" + " " + "operatorpassword1");
        //381    RPL_YOUREOPER   ":You are now an IRC operator"
        icp.ircParse();
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        
        response = "381" + " " + userNickname[0] + " " + ":You are now an IRC operator";        
        assertEquals("Operator:", ":" + prefix + " " + response, reply);
        assertTrue("Oper mode is set", ((User) icp.getRequestor()).isOperator());

        System.out.println("**OPER***************************************OK**");        
        
    }
}    

