/*
 * 
 * MotdCommandTest
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

import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Test;

import simpleircserver.base.Constants;
import simpleircserver.base.Globals;
import simpleircserver.connection.Connection;
import simpleircserver.parser.IrcCommandParser;
import simpleircserver.talker.user.User;
import simpleircserver.tests.IrcCommandTest;
import simpleircserver.tests.ServerTestUtils;

/**
 * MotdCommandTest
 * @version 0.5.3.1 2015-11-06 
 * @author  Nikolay Kirdin
 */
public class MotdCommandTest extends IrcCommandTest {
	
    @Test
    public void motdCommandTest() {
        System.out.println("--MOTD-------------------------------------------");
        IrcCommandParser icp = new IrcCommandParser();
        
        dropAll();
        serverInit();
        userInit(); 
        operatorInit();
        serviceInit();

        String reply;
        String servername;
        String prefix;
        String ircCommand;
        String response;
        
        icp.setRequestor(User.create());
        ((User) icp.getRequestor()).setIrcServer(Globals.thisIrcServer.get());
        icp.getRequestor().setConnection(Connection.create());
        
        ircCommand = "MOTD";
        icp.setParsingString(ircCommand);
        prefix = Globals.thisIrcServer.get().getHostname();
        response = "451" + " " + "" + " " + ":" + "You have not registered";
        icp.ircParse();
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertTrue("Not registered reply", reply.equals(":" + prefix + " " + response));
        
        icp.setRequestor(requestor[0]);
        Globals.motdFilename.set("");
        
        // 422    ERR_NOMOTD ":MOTD File is missing"
        ircCommand = "MOTD";
        icp.setParsingString(ircCommand);
        prefix = Globals.thisIrcServer.get().getHostname();
        response = "422" + " " + requestor[0].getNickname() + " " + ":" + "MOTD File is missing";
        icp.ircParse();
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertTrue("ERR_NOMOTD", icp.getRequestor().getOutputQueue().isEmpty());

        String motdFilePath = ServerTestUtils.buildResourceFilePath(Constants.MOTD_FILE_PATH);
        Globals.motdFilename.set(motdFilePath);
        
        ircCommand = "MOTD";
        icp.setParsingString(ircCommand);
        prefix = Globals.thisIrcServer.get().getHostname();
        servername = Globals.thisIrcServer.get().getHostname();
        //375    RPL_MOTDSTART ":- <server> Message of the day - "
        icp.ircParse();
        response = "375" + " " + requestor[0].getNickname() + " " + ":-" + " " + servername + " " +"Message of the day - ";
        reply = icp.getRequestor().getOutputQueue().poll().getReport();        

        assertTrue("RPL_MOTDSTART", reply.equals(":" + prefix + " " + response));
        //372    RPL_MOTD ":- <text>"
        response = "372" + " " + requestor[0].getNickname() + " " + ":" + "-" +".*";
        
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        while (reply.matches(":" + prefix + " " + response)) {
            assertTrue("RPL_MOTD", reply.matches(":" + prefix + " " + response));
            reply = icp.getRequestor().getOutputQueue().poll().getReport();
        }  
        //376    RPL_ENDOFMOTD ":End of MOTD command"
        response = "376" + " " + requestor[0].getNickname() + " " + ":" + "End of MOTD command";
        assertTrue("RPL_MOTD", reply.equals(":" + prefix + " " + response));
                
        System.out.println("**MOTD***************************************OK**");
    }

}    

