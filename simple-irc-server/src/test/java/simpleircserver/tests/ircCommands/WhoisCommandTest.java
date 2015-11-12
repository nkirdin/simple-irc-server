/*
 * 
 * WhoisCommandTest
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

import simpleircserver.base.Globals;
import simpleircserver.connection.Connection;
import simpleircserver.parser.IrcCommandParser;
import simpleircserver.talker.IrcTalker;
import simpleircserver.talker.user.User;
import simpleircserver.tests.IrcCommandTest;

/**
 * WhoisCommandTest
 * @version 0.5.3.1 2015-11-06 
 * @author  Nikolay Kirdin
 */
public class WhoisCommandTest extends IrcCommandTest {
	
    @Test
    public void whoisCommandTest() {
        System.out.println("--WHOIS------------------------------------------");
        IrcCommandParser icp = new IrcCommandParser();
        
        dropAll();
        serverInit();
        userInit(); operatorInit();
        serviceInit();
        
        String reply;
        String prefix;
        String ircCommand;
        String response;
        String mask;

        int i;
        
        icp.setRequestor(User.create());
        ((User) icp.getRequestor()).setIrcServer(Globals.thisIrcServer.get());
        Connection conn = Connection.create();
        
        conn.ircTalker.set(icp.getRequestor());
        icp.getRequestor().setConnection(conn);
        db.register(conn);


        ircCommand = "WHOIS";
        mask = "0";
        icp.setParsingString(ircCommand + " " + mask);
        prefix = Globals.thisIrcServer.get().getHostname();
        response = "451" + " " + "" + " " + ":" + "You have not registered";
        icp.ircParse();
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertTrue("Not registered", reply.equals(":" + prefix + " " + response));
                
        for (i = 0; i < requestor.length ; i++) {
            icp.setRequestor(requestor[i]);
            
            ircCommand = "JOIN";
            icp.setParsingString(ircCommand + " " + channelName[i]);
            icp.ircParse();
        }
        
        for (IrcTalker rqstr : requestor) rqstr.getOutputQueue().clear();

//           311    RPL_WHOISUSER "<nick> <user> <host> * :<real name>"
//           312    RPL_WHOISSERVER "<nick> <server> :<server info>"
//           313    RPL_WHOISOPERATOR "<nick> :is an IRC operator"
//           317    RPL_WHOISIDLE "<nick> <integer> :seconds idle"
//           318    RPL_ENDOFWHOIS "<nick> :End of WHOIS list"
//           319    RPL_WHOISCHANNELS "<nick> :*( ( "@" / "+" ) <channel> " " )"

        icp.setRequestor(requestor[0]);
        
        prefix = Globals.thisIrcServer.get().getHostname();
        mask = "no*su?,ch?Nick,NoSu*ch?";
        ircCommand = "WHOIS" + " " + mask;
        icp.setParsingString(ircCommand);
        icp.ircParse();
        //401    ERR_NOSUCHNICK "<nickname> :No such nick/channel"
        response = "401" + " " + requestor[0].getNickname() + " " + mask + " " +":" + "No such nick/channel";
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertTrue("ERR_NOSUCHNICK", reply.equals(":" + prefix + " " + response));

        ircCommand = "OPER";
        icp.setParsingString(ircCommand + " " + "operatorname1" + " " + "operatorpassword1");
        icp.ircParse();
        icp.getRequestor().getOutputQueue().clear();

        
        mask = requestor[0].getNickname() + "," + requestor[1].getNickname();
        ircCommand = "WHOIS" + " " + mask;
        icp.setParsingString(ircCommand);
        icp.ircParse();
        
        for (i = 0; i < 2 ; i++) {
            prefix = Globals.thisIrcServer.get().getHostname();
            response = "311" + " " + requestor[0].getNickname() + " " + requestor[i].getNickname()
                    + " " + requestor[i].getUsername()
                    + " " + requestor[i].getHostname()
                    + " " + "*" + " " + ":" + requestor[i].getRealname();
            
            reply = icp.getRequestor().getOutputQueue().poll().getReport();
            assertTrue("RPL_WHOISUSER for userlist", reply.equals(":" + prefix + " " + response));
            
            response = "312" + " " + requestor[0].getNickname()
                    + " " + requestor[i].getNickname()
                    + " " + requestor[i].getIrcServer().getHostname()
                    + " " + ":" + requestor[i].getIrcServer().getInfo();
            reply = icp.getRequestor().getOutputQueue().poll().getReport();
            assertTrue("RPL_WHOISSERVER for userlist", reply.equals(":" + prefix + " " + response));
            
            if (i == 0) {
                response = "313" + " " + requestor[0].getNickname() + " " + requestor[i].getNickname() + " " + ":" + "is an IRC operator";
                reply = icp.getRequestor().getOutputQueue().poll().getReport();
                assertTrue("RPL_WHOISOPERATOR for userlist", reply.equals(":" + prefix + " " + response));
            }
            
            response = "317" + " " + requestor[0].getNickname() + " " + requestor[i].getNickname() + " " +  ((User) requestor[i]).getIdle() + " " + ":" + "seconds idle";
            reply = icp.getRequestor().getOutputQueue().poll().getReport();
            assertTrue("RPL_WHOISIDLE for userlist", reply.equals(":" + prefix + " " + response));
            
            response = "319" + " " + requestor[0].getNickname() + " " + requestor[i].getNickname() + " " +  ":" + "@" + channelName[i];
            reply = icp.getRequestor().getOutputQueue().poll().getReport();
            assertTrue("RPL_WHOISCHANNELS for userlist", reply.equals(":" + prefix + " " + response));

            response = "318" + " " + requestor[0].getNickname() + " " + requestor[i].getNickname() + " " +":" + "End of WHOIS list";
            reply = icp.getRequestor().getOutputQueue().poll().getReport();
            assertTrue("RPL_ENDOFWHOIS for userlist", reply.equals(":" + prefix + " " + response));
        }

        icp.setRequestor(requestor[0]);
        mask = "no*su?,ch?Nick,NoSu*ch?";
        String target = "irc.example.com";
        ircCommand = "WHOIS" + " " + target +" " + mask;
        icp.setParsingString(ircCommand);
        icp.ircParse();
        //402    ERR_NOSUCHSERVER "<server name> :No such server"

        response = "402" + " " + requestor[0].getNickname() + " " + target + " " +":" + "No such server";
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertTrue("ERR_NOSUCHSERVER", reply.equals(":" + prefix + " " + response));
        
        System.out.println("**WHOIS**************************************OK**");
    }   
}    

