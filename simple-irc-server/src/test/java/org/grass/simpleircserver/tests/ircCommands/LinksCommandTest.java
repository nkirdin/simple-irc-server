/*
 * 
 * LinksCommandTest
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

import java.util.LinkedHashSet;

import org.grass.simpleircserver.base.Globals;
import org.grass.simpleircserver.connection.Connection;
import org.grass.simpleircserver.parser.IrcCommandParser;
import org.grass.simpleircserver.talker.server.IrcServer;
import org.grass.simpleircserver.talker.user.User;
import org.grass.simpleircserver.tests.IrcCommandTest;
import org.junit.Test;

/**
 * LinksCommandTest
 * @version 0.5.3.1 2015-11-06 
 * @author  Nikolay Kirdin
 */
public class LinksCommandTest extends IrcCommandTest {
	
	@Test
    public void linksCommandTest() {
        System.out.println("--LINKS------------------------------------------");
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

        ircCommand = "LINKS";
        icp.setParsingString(ircCommand);
        prefix = Globals.thisIrcServer.get().getHostname();
        response = "451" + " " + "" + " " + ":" + "You have not registered";
        icp.ircParse();
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertTrue("Not registered reply", reply.equals(":" + prefix + " " + response));
                
        //364    RPL_LINKS "<mask> <server> :<hopcount> <server info>"
        //365    RPL_ENDOFLINKS "<mask> :End of LINKS list"
        
        icp.setRequestor(requestor[0]);
        
        ircCommand = "LINKS";
        String tgt = "no.such.server";
        String serverMask = "*.irc.example.com";
        icp.setParsingString(ircCommand + " " + tgt + " " + serverMask);
        prefix = Globals.thisIrcServer.get().getHostname();
        //402    ERR_NOSUCHSERVER "<server name> :No such server"
        icp.ircParse();
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        response = "402" + " " + requestor[0].getNickname() + " " + tgt + " " + ":" + "No such server";
        assertTrue("ERR_NOSUCHSERVER", reply.equals(":" + prefix + " " + response));
        
        db.register(Globals.thisIrcServer.get());
        ircCommand = "LINKS";
        serverMask ="";
        icp.setParsingString(ircCommand + " " + serverMask);
        prefix = Globals.thisIrcServer.get().getHostname();
        icp.ircParse();
        LinkedHashSet<IrcServer> serverSet = db.getIrcServerSet();
        for (IrcServer server : serverSet) {
            response = "364" + " " + requestor[0].getNickname() + " " + serverMask + " " + server.getHostname() + " " + ":" + server.getHopcount() + " " + server.getInfo();
            reply = icp.getRequestor().getOutputQueue().poll().getReport();
            assertTrue("RPL_LINKS", reply.equals(":" + prefix + " " + response));
        }
        response = "365" + " " + requestor[0].getNickname() + " " + serverMask + " " + ":" + "End of LINKS list";
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertTrue("RPL_ENDOFLINKS", reply.equals(":" + prefix + " " + response));
        
        ircCommand = "QUIT";
        icp.setParsingString(ircCommand);
        icp.ircParse();
        
        System.out.println("**LINKS**************************************OK**");
    }   
}    

