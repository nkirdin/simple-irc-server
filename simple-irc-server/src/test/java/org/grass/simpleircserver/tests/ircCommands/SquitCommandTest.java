/*
 * 
 * SquitCommandTest
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

import static org.junit.Assert.assertTrue;

import org.grass.simpleircserver.base.Globals;
import org.grass.simpleircserver.connection.Connection;
import org.grass.simpleircserver.parser.IrcCommandParser;
import org.grass.simpleircserver.talker.user.User;
import org.grass.simpleircserver.tests.IrcCommandTest;
import org.junit.Test;

/**
 * SquitCommandTest
 * @version 0.5.3.1 2015-11-06 
 * @author  Nikolay Kirdin
 */
public class SquitCommandTest extends IrcCommandTest {
	
	@Test
    public void squitCommandTest() {
        System.out.println("--SQUIT------------------------------------------");
        String reply;
        String prefix;
        String ircCommand;
        String response;
        String comment = "Regular shutdown.";

        IrcCommandParser icp = new IrcCommandParser();
        
        dropAll();
        serverInit();
        userInit(); 
        operatorInit();

        String target = ircServer[1].getHostname();
        
        icp.setRequestor(User.create());
        ((User) icp.getRequestor()).setIrcServer(Globals.thisIrcServer.get());
        icp.getRequestor().setConnection(Connection.create());
        
        ircCommand = "SQUIT";
        icp.setParsingString(ircCommand + " " + target + " " + ":" + comment);
        prefix = Globals.thisIrcServer.get().getHostname();
        response = "451" + " " + "" + " " + ":" + "You have not registered";
        icp.ircParse();
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertTrue("ERR_NOTREGISTERED",reply.equals(":" + prefix + " " + response));

        icp.setRequestor(db.getUser(userNickname[0]));        
        ircCommand = "SQUIT";
        icp.setParsingString(ircCommand);
        prefix = Globals.thisIrcServer.get().getHostname();
        response = "461" + " " + userNickname[0] + " " + ircCommand + " " + ":" + "Not enough parameters";
        icp.ircParse();
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertTrue("ERR_NEEDMOREPARAMS, no target, no comment", reply.equals(":" + prefix + " " + response));
                
        ircCommand = "SQUIT";
        icp.setParsingString(ircCommand + " " + target);
        prefix = Globals.thisIrcServer.get().getHostname();
        response = "461" + " " + userNickname[0] + " " + ircCommand + " " + ":" + "Not enough parameters";
        icp.ircParse();
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertTrue("ERR_NEEDMOREPARAMS, no comment", reply.equals(":" + prefix + " " + response));
        
        ircCommand = "SQUIT";
        icp.setParsingString(ircCommand + " " + target + " " + ":" + comment);
        prefix = Globals.thisIrcServer.get().getHostname();
        //481    ERR_NOPRIVILEGES  ":Permission Denied- You're not an IRC operator"
        response = "481" + " " + userNickname[0] + " " + ":Permission Denied- You're not an IRC operator";
        icp.ircParse();
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertTrue("ERR_NOPRIVILEGES", reply.equals(":" + prefix + " " + response));
        
        ircCommand = "OPER";
        icp.setParsingString(ircCommand + " " + "operatorname1" + " " + "operatorpassword1");
        icp.ircParse();
        reply = icp.getRequestor().getOutputQueue().poll().getReport();

        String badTarget = "no.such.server";
        ircCommand = "SQUIT";
        icp.setParsingString(ircCommand + " " + badTarget + " " + ":" + comment);
        prefix = Globals.thisIrcServer.get().getHostname();
        //402    ERR_NOSUCHSERVER "<server name> :No such server"
        response = "402" + " " + userNickname[0] + " " + badTarget + " " + ":No such server";
        icp.ircParse();
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertTrue("ERR_NOSUCHSERVER", reply.equals(":" + prefix + " " + response));
        
        ircCommand = "SQUIT";
        icp.setParsingString(ircCommand + " " + target + " " + ":" + comment);
        prefix = Globals.thisIrcServer.get().getHostname();;
        icp.ircParse();
        // Check WALLOPS
        prefix = Globals.thisIrcServer.get().getHostname();
        response = "WALLOPS" + " " + ":" + userNickname[0] + " " + ircCommand + " " + target + " " + ":" + comment;
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertTrue("Receiving WALLOPS", reply.equals(":" + prefix + " " + response));

        System.out.println("**SQUIT**************************************OK**");
    }   
}    

