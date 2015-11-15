/*
 * 
 * PongCommandTest
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

import org.junit.Test;

import simpleircserver.base.Globals;
import simpleircserver.connection.Connection;
import simpleircserver.parser.IrcCommandParser;
import simpleircserver.talker.IrcTalker;
import simpleircserver.talker.user.User;
import simpleircserver.tests.IrcCommandTest;

/**
 * PongCommandTest
 * @version 0.5.3.1 2015-11-06 
 * @author  Nikolay Kirdin
 */
public class PongCommandTest extends IrcCommandTest {
	
    @Test
    public void pongCommandTest() {
        System.out.println("--PONG-------------------------------------------");
        
        IrcCommandParser icp = new IrcCommandParser();
        
        dropAll();
        serverInit();
        serviceInit();

        IrcTalker[] requestor = new IrcTalker[3];
        String[] userNickname = {"neeck1", "neeck2", "nick3"};
        String[] userUsername = {"user1", "user2he", "user3he"};
        String[] userRealname = {"Real Name 1", "Real Name 2A", "Real Name 3A"};
        String[] userHost = {"HostName1", "HostName2", "HostName2"};
        String[] userMode = {"0", "0", "0"};
        
        String reply;
        String prefix;
        String ircCommand;
        String response;

        int i;
        
        icp.setRequestor(User.create());
        ((User) icp.getRequestor()).setIrcServer(Globals.thisIrcServer.get());
        Connection conn = Connection.create();
        
        conn.ircTalker.set(icp.getRequestor());
        icp.getRequestor().setConnection(conn);
        db.register(conn);


        ircCommand = "PONG";
        icp.setParsingString(ircCommand + " " + ircServer[0].getHostname());
        icp.ircParse();
        assertTrue("If sender not registered, then no reply", icp.getRequestor().getOutputQueue().isEmpty());
        
        for (i = 0; i < requestor.length; i++) {
            icp.setRequestor(User.create());
            ((User) icp.getRequestor()).setIrcServer(Globals.thisIrcServer.get());
            conn = Connection.create();
            
            conn.ircTalker.set(icp.getRequestor());
            icp.getRequestor().setConnection(conn);
            db.register(conn);


            ((User) icp.getRequestor()).setIrcServer(ircServer[i]);
            
            ircCommand = "NICK";
            icp.setParsingString(ircCommand + " " + userNickname[i]);
            icp.ircParse();
            requestor[i] = icp.getRequestor();
            ((User) icp.getRequestor()).setHostname(userHost[i]);
        
            ircCommand = "USER";
            icp.setParsingString(ircCommand + " " + userUsername[i] + " " + userMode[i] + " " + "*" + " " + ":" + userRealname[i]);
            icp.ircParse();
            icp.getRequestor().getOutputQueue().clear();
        }
        
        icp.setRequestor(requestor[0]);
        prefix = Globals.thisIrcServer.get().getHostname();
/*        
        ircCommand = "PONG";
        icp.setParsingString(ircCommand + " " + channelName[0]);
        prefix = Globals.thisIrcServer.get().getHostname();
        //421    ERR_UNKNOWNCOMMAND  "<command> :Unknown command"
        response = "421" + " " + userNickname[0] + " " + ircCommand + " " + ":" + "Unknown command";
        icp.ircParse();
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertTrue("ERR_UNKNOWNCOMMAND", reply.equals(":" + prefix + " " + response));
*/
//        409    ERR_NOORIGIN ":No origin specified"
//        402    ERR_NOSUCHSERVER "<server name> :No such server"
/*
        ircCommand = "PONG";
        icp.setParsingString(ircCommand);
        prefix = Globals.thisIrcServer.get().getHostname();
        response = "409" + " " + userNickname[0] + " " + ":" + "No origin specified";
        icp.ircParse();
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertTrue("ERR_NOORIGIN", reply.equals(":" + prefix + " " + response));
*/

        ircCommand = "PONG";
        icp.setParsingString(ircCommand);
        prefix = Globals.thisIrcServer.get().getHostname();
//        response = "409" + " " + userNickname[0] + " " + ":" + "No origin specified";
        icp.ircParse();
//        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertTrue("Nothing. PONG accepted. Assume that origin is a client.", icp.getRequestor().getOutputQueue().isEmpty());

/*
        ircCommand = "PONG";
        String tgt = "no.such.server";
        icp.setParsingString(ircCommand  + " " + tgt);
        prefix = Globals.thisIrcServer.get().getHostname();
        response = "402" + " " + userNickname[0] + " " + tgt  + " " + ":" + "No such server";
        icp.ircParse();
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertTrue("ERR_NOSUCHSERVER", reply.equals(":" + prefix + " " + response));
         
        ircCommand = "PONG";
        icp.setParsingString(ircCommand  + " " + ircServer[1].getHostname());
        prefix = Globals.thisIrcServer.get().getHostname();        
        response = ircCommand  + " " + ircServer[1].getHostname();
        icp.ircParse();
        assertTrue("PONG reply to me. Nothing", icp.getRequestor().getOutputQueue().isEmpty());
*/                        
        icp.setRequestor(ircServer[1]);
        
        ircCommand = "PONG";
        icp.setParsingString(ircCommand + " " + ircServer[1].getHostname() + " " + ircServer[2].getHostname());
        prefix = ircServer[1].getHostname();        
        response = ircCommand + " " + ircServer[1].getHostname() + " " + ircServer[2].getHostname() + " " + ":" + Globals.thisIrcServer.get().getHostname();
        icp.ircParse();
        reply = ircServer[2].getOutputQueue().poll().getReport();
        assertTrue("Forward PONG to destination", reply.equals(":" + prefix + " " + response));
          
        System.out.println("**PONG***************************************OK**");
    }   
}    

