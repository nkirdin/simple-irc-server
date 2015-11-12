/*
 * 
 * QuitCommandTest
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

package simpleircserver.tests.ircCommands;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import simpleircserver.parser.IrcCommandParser;
import simpleircserver.talker.IrcTalker;
import simpleircserver.talker.IrcTalkerState;
import simpleircserver.tests.IrcCommandTest;

/**
 * QuitCommandTest
 * @version 0.5.3.1 2015-11-06 
 * @author  Nikolay Kirdin
 */
public class QuitCommandTest extends IrcCommandTest {
	
    @Test
    public void quitCommandTest() {
        System.out.println("--QUIT-------------------------------------------");
        
        String reply;
        String prefix;
        String ircCommand;
        String response;

        int i;

        IrcCommandParser icp = new IrcCommandParser();

        dropAll();
        serverInit();
        userInit(); operatorInit();
        serviceInit();

        icp.setRequestor(db.getUser(userNickname[0]));
        
        ircCommand = "QUIT";
        icp.setParsingString(ircCommand);
        prefix = userNickname[0];
        response = "ERROR" + " " + ":Closing Link:" + " .*";
        assertTrue("IrcTalker registered", db.getUser(userNickname[0]) != null);
        assertTrue("IrcTalker operational", icp.getRequestor().getState() == IrcTalkerState.OPERATIONAL);
        icp.ircParse();
        assertTrue("IrcTalker close", icp.getRequestor().getState() == IrcTalkerState.CLOSE);
        //assertTrue("IrcTalker unregistered", db.getUser(userNickname[0]) == null);

        dropUser();
        userInit(); operatorInit();
        icp.setRequestor(db.getUser(userNickname[0]));
        
        ircCommand = "JOIN";
        icp.setParsingString(ircCommand + " " + channelName[0]);
        icp.ircParse();
        
        for (i = 1; i < requestor.length; i++) {
            icp.setRequestor(db.getUser(userNickname[i]));
        
            ircCommand = "JOIN";
            icp.setParsingString(ircCommand + " " + channelName[0]);
            icp.ircParse();
            
        } 
        
        for (IrcTalker ircTalker : requestor) {
            if (ircTalker.getOutputQueue() != null) ircTalker.getOutputQueue().clear();
        }
        
        icp.setRequestor(db.getUser(userNickname[0]));
        
        ircCommand = "QUIT";
        String quitInfo = "I'll be back.";
        icp.setParsingString(ircCommand + " " + ":" + quitInfo);
        icp.ircParse();
        
        prefix = userNickname[0];
        response = ircCommand + " " + ":" + quitInfo;
        
        for (i = 1; i < requestor.length; i++) {
            reply = requestor[i].getOutputQueue().poll().getReport();
            assertTrue(reply.equals(":" + prefix + " " + response));
        }
        
        icp.setRequestor(service[0]);

        ircCommand = "QUIT";
        quitInfo = "I'll serve you later.";
        icp.setParsingString(ircCommand + " " + ":" + quitInfo);
        
        assertTrue("IrcTalker registered", db.getService(icp.getRequestor().getNickname()) != null);
        assertTrue("IrcTalker operational", icp.getRequestor().getState() == IrcTalkerState.OPERATIONAL);
        icp.ircParse();
        assertTrue("IrcTalker close", icp.getRequestor().getState() == IrcTalkerState.CLOSE);
        //assertTrue("IrcTalker unregistered", db.getUser(userNickname[0]) == null);
        
        System.out.println("**QUIT***************************************OK**");

    }   
}    

