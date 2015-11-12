/*
 * 
 * SummonCommandTest
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
import org.grass.simpleircserver.parser.IrcCommandParser;
import org.grass.simpleircserver.tests.IrcCommandTest;
import org.junit.Test;

/**
 * SummonCommandTest
 * @version 0.5.3.1 2015-11-06 
 * @author  Nikolay Kirdin
 */
public class SummonCommandTest extends IrcCommandTest {
	
	@Test
    public void summonCommandTest() {
        System.out.println("--SUMMON-----------------------------------------");
        
        IrcCommandParser icp = new IrcCommandParser();
        
        dropAll();
        serverInit();
        serviceInit();
        userInit();

        
        String reply;
        String prefix;
        String ircCommand;
        String response;
        
        icp.setRequestor(requestor[0]);

        //445    ERR_SUMMONDISABLED ":SUMMON has been disabled"
        
        ircCommand = "SUMMON";
        icp.setParsingString(ircCommand);
        prefix = Globals.thisIrcServer.get().getHostname();
        response = "445" + " " + requestor[0].getNickname() + " " + ":" + "SUMMON has been disabled";
        icp.ircParse();
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertTrue("ERR_SUMMONDISABLED", reply.equals(":" + prefix + " " + response));
        System.out.println("**SUMMON*************************************OK**");
    }   
}    

