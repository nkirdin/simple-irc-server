/*
 * 
 * InfoCommandTest
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import simpleircserver.base.Globals;
import simpleircserver.connection.Connection;
import simpleircserver.parser.IrcCommandParser;
import simpleircserver.talker.user.User;
import simpleircserver.tests.IrcCommandTest;

/**
 * InfoCommandTest
 * @version 0.5.3.1 2015-11-06 
 * @author  Nikolay Kirdin
 */
public class InfoCommandTest extends IrcCommandTest {
	
    @Test
    public void infoCommandTest() {
        System.out.println("--INFO-------------------------------------------");
        
        dropAll();
        serverInit();
        userInit(); 
        operatorInit();
        serviceInit();
        
        IrcCommandParser icp = new IrcCommandParser();


        String reply;
        String prefix;
        String ircCommand;
        String response;
        
        icp.setRequestor(User.create());
        ((User) icp.getRequestor()).setIrcServer(Globals.thisIrcServer.get());
        icp.getRequestor().setConnection(Connection.create());

        ircCommand = "INFO";
        icp.setParsingString(ircCommand);
        prefix = Globals.thisIrcServer.get().getHostname();
        response = "451" + " " + "" + " " + ":" + "You have not registered";
        icp.ircParse();
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertEquals("Not registered reply", reply,":" + prefix + " " + response);
        //String infoFilename = Globals.infoFilename.get();
        
        icp.setRequestor(requestor[0]);

//           371    RPL_INFO ":<string>"
//           374    RPL_ENDOFINFO ":End of INFO list"

        ircCommand = "INFO";
        String serverMask ="";
        icp.setParsingString(ircCommand + " " + serverMask);
        prefix = Globals.thisIrcServer.get().getHostname();
        icp.ircParse();
        
        prefix = Globals.thisIrcServer.get().getHostname();
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        response = "371" + " " + requestor[0].getNickname() + " " + ":" +".*";
        while (reply.matches(":" + prefix + " " + response)) {
            assertTrue("RPL_INFO", reply.matches(":" + prefix + " " + response));
            reply = icp.getRequestor().getOutputQueue().poll().getReport();
        }  
        response = "374" + " " + requestor[0].getNickname() + " " + ":" + "End of INFO list";
        assertEquals("RPL_ENDOFINFO", reply,":" + prefix + " " + response);
        
        ircCommand = "INFO";
        serverMask = "*.irc.example.com";
        icp.setParsingString(ircCommand + " " + serverMask);
        prefix = Globals.thisIrcServer.get().getHostname();
        //402    ERR_NOSUCHSERVER "<server name> :No such server"
        icp.ircParse();
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        response = "402" + " " + requestor[0].getNickname() + " " + serverMask + " " + ":" + "No such server";
        assertEquals("ERR_NOSUCHSERVER", reply, ":" + prefix + " " + response);
        
        System.out.println("**INFO***************************************OK**");
    }   
}    

