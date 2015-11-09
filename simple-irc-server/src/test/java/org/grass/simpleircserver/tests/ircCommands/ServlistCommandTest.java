/*
 * 
 * ServlistCommandTest
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
import org.grass.simpleircserver.talker.service.Service;
import org.grass.simpleircserver.talker.user.User;
import org.grass.simpleircserver.tests.IrcCommandTest;
import org.junit.Test;

/**
 * ServlistCommandTest
 * @version 0.5.3.1 2015-11-06 
 * @author  Nikolay Kirdin
 */
public class ServlistCommandTest extends IrcCommandTest {
	
	@Test
    public void servlistCommandTest() {
        System.out.println("--SERVLIST---------------------------------------");
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
        
        ircCommand = "SERVLIST";
        icp.setParsingString(ircCommand);
        prefix = Globals.thisIrcServer.get().getHostname();
        response = "451" + " " + "" + " " + ":" + "You have not registered";
        icp.ircParse();
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertTrue("Not registered reply", reply.equals(":" + prefix + " " + response));
        
        icp.setRequestor(requestor[0]);

//      234    RPL_SERVLIST "<name> <server> <mask> <type> <hopcount> <info>"
//      235    RPL_SERVLISTEND "<mask> <type> :End of service listing"
        
        ircCommand = "SERVLIST";
        String serviceMask = "";
        String serviceType = "";
        icp.setParsingString(ircCommand);
        prefix = Globals.thisIrcServer.get().getHostname();
        icp.ircParse();
        LinkedHashSet<Service> serviceSet = db.getServiceSet();
        for (Service ircService : serviceSet) {
            reply = icp.getRequestor().getOutputQueue().poll().getReport();
            response = "234" + " " + icp.getRequestor().getNickname()
                    + " " + ircService.getNickname()
                    + " " + ircService.getIrcServer().getHostname()
                    + " " + serviceMask 
                    + " " + serviceType
                    + " " + ircService.getIrcServer().getHopcount()
                    + " " + ircService.getInfo();
            assertTrue("RPL_SERVLIST", reply.equals(":" + prefix + " " + response));
        }
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        response = "235" + " " + requestor[0].getNickname() + " " + serviceMask + " " + serviceType  + " " + ":" + "End of service listing";
        assertTrue("RPL_SERVLISTEND", reply.equals(":" + prefix + " " + response));
        
        serviceMask = "Nothing";
        serviceType = "*";

        icp.setParsingString(ircCommand + " " + serviceMask + " " + serviceType);
        prefix = Globals.thisIrcServer.get().getHostname();
        icp.ircParse();
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        response = "235" + " " + requestor[0].getNickname() + " " + serviceMask + " " + serviceType  + " " + ":" + "End of service listing";
        assertTrue("Nothing. RPL_SERVLISTEND", reply.equals(":" + prefix + " " + response));
        
        System.out.println("**SERVLIST***********************************OK**");
    }   
}    

