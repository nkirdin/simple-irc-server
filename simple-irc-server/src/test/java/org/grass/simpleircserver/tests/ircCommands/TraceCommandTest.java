/*
 * 
 * TraceCommandTest
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
 * TraceCommandTest
 * @version 0.5.3.1 2015-11-06 
 * @author  Nikolay Kirdin
 */
public class TraceCommandTest extends IrcCommandTest {
	
	@Test
    public void traceCommandTest() {
        System.out.println("--TRACE------------------------------------------");
        IrcCommandParser icp = new IrcCommandParser();
        
        dropAll();
        serverInit();
        userInit(); operatorInit();
        serviceInit();

        String reply;
        String prefix;
        String ircCommand;
        String response;
        
        icp.setRequestor(User.create());
        ((User) icp.getRequestor()).setIrcServer(Globals.thisIrcServer.get());
        icp.getRequestor().setConnection(Connection.create());

        ircCommand = "TRACE";
        icp.setParsingString(ircCommand);
        prefix = Globals.thisIrcServer.get().getHostname();
        response = "451" + " " + "" + " " + ":" + "You have not registered";
        icp.ircParse();
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertTrue("Not registered reply", reply.equals(":" + prefix + " " + response));
        
        icp.setRequestor(requestor[0]);
        
        ircCommand = "OPER";
        icp.setParsingString(ircCommand + " " + "operatorname1" + " " + "operatorpassword1");
        icp.ircParse();
        icp.getRequestor().getOutputQueue().clear();
        
        // 206    RPL_TRACESERVER "Serv <class> <int>S <int>C <server>  <nick!user|*!*>@<host|server> V<protocol version>"
        // 207    RPL_TRACESERVICE "Service <class> <name> <type> <active type>"
        // 204    RPL_TRACEOPERATOR "Oper <class> <nick>"
        
        ircCommand = "TRACE";
        icp.setParsingString(ircCommand);
        prefix = Globals.thisIrcServer.get().getHostname();
        icp.ircParse();
        response = "481" + " " + requestor[0].getNickname() + " " + ":" + "Permission Denied- You're not an IRC operator";
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertTrue("ERR_NOPRIVILEGES", reply.equals(":" + prefix + " " + response));

        System.out.println("**TRACE**************************************OK**");
    }   
}    

