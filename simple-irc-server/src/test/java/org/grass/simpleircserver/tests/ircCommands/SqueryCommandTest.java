/*
 * 
 * SqueryCommandTest
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
 * SqueryCommandTest
 * @version 0.5.3.1 2015-11-06 
 * @author  Nikolay Kirdin
 */
public class SqueryCommandTest extends IrcCommandTest {
	
	@Test
    public void squeryCommandTest() {
        System.out.println("--SQUERY-----------------------------------------");
        IrcCommandParser icp = new IrcCommandParser();
        
        dropAll();
        serverInit();
        userInit(); operatorInit();
        serviceInit();

        String reply;
        String prefix;
        String ircCommand;
        String response;
        String content;
        Connection conn;
                        
        icp.setRequestor(User.create());
        ((User) icp.getRequestor()).setIrcServer(Globals.thisIrcServer.get());
        conn = Connection.create();
        
        conn.ircTalker.set(icp.getRequestor());
        icp.getRequestor().setConnection(conn);
        db.register(conn);
        

        ircCommand = "SQUERY";
        content = "Hello world!";
        icp.setParsingString(ircCommand + " " + channelName[0] + " " + content);
        prefix = Globals.thisIrcServer.get().getHostname();
        response = "451" + " " + "" + " " + ":" + "You have not registered";
        icp.ircParse();
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertTrue("Not registered", reply.equals(":" + prefix + " " + response));
                
        icp.setRequestor(requestor[0]);
        
        ircCommand = "SQUERY";
        icp.setParsingString(ircCommand);
        prefix = Globals.thisIrcServer.get().getHostname();
        //411    ERR_NORECIPIENT ":No recipient given (<command>)"
        response = "411" + " " + requestor[0].getNickname() + " " + ":" + "No recipient given (" + ircCommand + ")";
        icp.ircParse();
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertTrue("ERR_NORECIPIENT", reply.equals(":" + prefix + " " + response));
        
        ircCommand = "SQUERY";
        icp.setParsingString(ircCommand + " " + service[0].getNickname());
        prefix = Globals.thisIrcServer.get().getHostname();
        //412    ERR_NOTEXTTOSEND ":No text to send"
        response = "412" + " " + requestor[0].getNickname() + " " + ":" + "No text to send";
        icp.ircParse();
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertTrue("ERR_NOTEXTTOSEND userNickname[0]", reply.equals(":" + prefix + " " + response));
        
        ircCommand = "SQUERY";
        content = "Hello world!";
        icp.setParsingString(ircCommand + " " + requestor[1].getNickname() + " " + ":" + content);
        prefix = Globals.thisIrcServer.get().getHostname();
        //401    ERR_NOSUCHNICK "<nickname> :No such nick/channel"
        response = "401" + " " + requestor[0].getNickname() + " " + requestor[1].getNickname() + " " + ":" + "No such nick/channel";
        icp.ircParse();
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertTrue("ERR_NOSUCHNICK userNickname[1]", reply.equals(":" + prefix + " " + response));
                
        // Not a Service
        ircCommand = "SQUERY";
        content = "Hello world!";
        icp.setParsingString(ircCommand + " " + channelName[1] + " " + ":" + content);
        prefix = Globals.thisIrcServer.get().getHostname();
        //421    ERR_UNKNOWNCOMMAND "<command> :Unknown command"
        response = "421" + " " + requestor[0].getNickname() + " " + ircCommand + " " + ":" + "Unknown command";
        icp.ircParse();
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertTrue("ERR_UNKNOWNCOMMAND. Not a Service", reply.equals(":" + prefix + " " + response));
        
        // Service. OK
        ircCommand = "SQUERY";
        content = "Hello world!";
        icp.setParsingString(ircCommand + " " + service[0].getNickname() + " " + ":" + content);
        icp.ircParse();
        assertTrue("Only servicename, processing, nothing", icp.getRequestor().getOutputQueue().isEmpty());
        
        ircCommand = "SQUERY";
        content = "Hello world!";
        icp.setParsingString(ircCommand + " " + service[2].getNickname() + " " + ":" + content);
        icp.ircParse();
        assertTrue("servicename@servername, processing, nothing", icp.getRequestor().getOutputQueue().isEmpty());
        
        ircCommand = "SQUERY";
        content = "Hello world!";
        icp.setParsingString(ircCommand + " " + service[1].getNickname() + " " + ":" + content);
        icp.ircParse();
        assertTrue("servicename@servername, forwarding, nothing", icp.getRequestor().getOutputQueue().isEmpty());
        
        // Service list. OK
        
        ircCommand = "SQUERY";
        content = "Hello world!";
        icp.setParsingString(ircCommand + " " + service[0].getNickname() + " " + ":" + content);
        icp.ircParse();
        assertTrue("Only servicename, OK nothing", icp.getRequestor().getOutputQueue().isEmpty());
        assertTrue("servicename@servername, forwarding, nothing", icp.getRequestor().getOutputQueue().isEmpty());        
        
        System.out.println("**SQUERY*************************************OK**");
    }   
}    

