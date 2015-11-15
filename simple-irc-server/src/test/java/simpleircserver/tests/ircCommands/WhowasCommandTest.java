/*
 * 
 * WhowasCommandTest
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
import simpleircserver.talker.user.User;
import simpleircserver.tests.IrcCommandTest;

/**
 * WhowasCommandTest
 * @version 0.5.3.1 2015-11-06 
 * @author  Nikolay Kirdin
 */
public class WhowasCommandTest extends IrcCommandTest {
	
    @Test
    public void whowasCommandTest() {
        System.out.println("--WHOWAS-----------------------------------------");
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

        ircCommand = "WHOWAS";
        mask = "0";
        icp.setParsingString(ircCommand + " " + mask);
        prefix = Globals.thisIrcServer.get().getHostname();
        response = "451" + " " + "" + " " + ":" + "You have not registered";
        icp.ircParse();
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertTrue("Not registered", reply.equals(":" + prefix + " " + response));
        
//           406    ERR_WASNOSUCHNICK "<nickname> :There was no such nickname"

//         - Returned by WHOWAS to indicate there is no history
//           information for that nickname.
//           314    RPL_WHOWASUSER "<nick> <user> <host> * :<real name>"
//           312    RPL_WHOISSERVER "<nick> <server> :<server info>"
//           369    RPL_ENDOFWHOWAS "<nick> :End of WHOWAS"

//           - When replying to a WHOWAS message, a server MUST use
//           the replies RPL_WHOWASUSER, RPL_WHOISSERVER or
//           ERR_WASNOSUCHNICK for each nickname in the presented
//           list.  At the end of all reply batches, there MUST
//           be RPL_ENDOFWHOWAS (even if there was only one reply
//           and it was an error).
//           WHOWAS <nickname> *( "," <nickname> ) [ <count> [ <target> ] ]

        icp.setRequestor(requestor[0]);
        prefix = Globals.thisIrcServer.get().getHostname();
        String noSuchNick = "nOsUchni";
        ircCommand = "WHOWAS";
        icp.setParsingString(ircCommand  + " " + noSuchNick);
        icp.ircParse();
        //406    ERR_WASNOSUCHNICK "<nickname> :There was no such nickname"
        response = "406" + " " + requestor[0].getNickname() + " " + noSuchNick + " " +":" + "There was no such nickname";
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertTrue("ERR_WASNOSUCHNICK", reply.equals(":" + prefix + " " + response));

        dropUser();
        dropHistory();
                
        for (int j = 0 ; j < 10; j++) { 
            for (i = 0; i < requestor.length; i++) {
                icp.setRequestor(User.create());
                ((User) icp.getRequestor()).setIrcServer(Globals.thisIrcServer.get());

                ircCommand = "NICK";
                icp.setParsingString(ircCommand + " " + userNickname[i]);
                icp.ircParse();
                ((User) icp.getRequestor()).setHostname(userHost[i]);
        
                ircCommand = "USER";
                icp.setParsingString(ircCommand + " " + userUsername[i] + " " + userMode[i] + " " + "*" + " " + ":" + userRealname[i] + j);
                icp.ircParse();
            
                ircCommand = "QUIT";
                icp.setParsingString(ircCommand);
                icp.ircParse();
                icp.getRequestor().disconnect();
                icp.getRequestor().close();
                db.unRegister((User) icp.getRequestor());
                
            }
        }

//           314    RPL_WHOWASUSER "<nick> <user> <host> * :<real name>"
//           312    RPL_WHOISSERVER "<nick> <server> :<server info>"
//           369    RPL_ENDOFWHOWAS "<nick> :End of WHOWAS"

        for (i = 0; i < requestor.length; i++) {
            icp.setRequestor(User.create());
            ((User) icp.getRequestor()).setIrcServer(Globals.thisIrcServer.get());
            conn = Connection.create();
            
            conn.ircTalker.set(icp.getRequestor());
            icp.getRequestor().setConnection(conn);
            db.register(conn);


            ircCommand = "NICK";
            icp.setParsingString(ircCommand + " " + userNickname[i]);
            icp.ircParse();
            ((User) icp.getRequestor()).setHostname(userHost[i]);
        
            ircCommand = "USER";
            icp.setParsingString(ircCommand + " " + userUsername[i] + " " + userMode[i] + " " + "*" + " " + ":" + userRealname[i]);
            icp.ircParse();
        }
        
        for (User user : db.getUserSet()) {
            if (user.getOutputQueue() != null) user.getOutputQueue().clear();
        }
        
        icp.setRequestor(db.getUser(userNickname[0]));
        mask = userNickname[0] + "," + userNickname[1];
        ircCommand = "WHOWAS";
        icp.setParsingString(ircCommand + " " + mask);
        icp.ircParse();
        for (i = 0; i < 2 ; i++) {
            
            prefix = Globals.thisIrcServer.get().getHostname();
            for (int j = 9; j >= 0; j--) {
                response = "314" + " " + userNickname[0]
                        + " " + userNickname[i]
                        + " " + userUsername[i]
                        + " " + userHost[i]
                        + " " + "*"
                        + " " + ":" + userRealname[i] + j;
                reply = icp.getRequestor().getOutputQueue().poll().getReport();
                assertTrue("RPL_WHOWASUSER for userlist", reply.equals(":" + prefix + " " + response));
            
                response = "312" + " " + userNickname[0]
                        + " " + userNickname[i]
                        + " " + db.getUser(userNickname[i]).getIrcServer().getHostname()
                        + " " + ":" + "";//db.getUser(userNickname[i]).getIrcServer().getInfo();
                reply = icp.getRequestor().getOutputQueue().poll().getReport();
                assertTrue("RPL_WHOISSERVER for userlist", reply.equals(":" + prefix + " " + response));
            }
            response = "369" + " " + userNickname[0] + " " + userNickname[i] + " " +":" + "End of WHOWAS";
            reply = icp.getRequestor().getOutputQueue().poll().getReport();
            assertTrue("RPL_ENDOFWHOWAS for userlist", reply.equals(":" + prefix + " " + response));
        }
        
        mask = userNickname[0];
        int count = 5;
        ircCommand = "WHOWAS" + " " + mask + " " + count;
        
        icp.setParsingString(ircCommand);
        icp.ircParse();
        for (i = 0; i < 1 ; i++) {
            prefix = Globals.thisIrcServer.get().getHostname();
            for (int j = 9; j > 9 - count; j--) {
                response = "314" + " " + userNickname[0] + " " + userNickname[i] + " " + userUsername[i] + " " + userHost[i]
                        + " " + "*" + " " + ":" + userRealname[i] + j;
                reply = icp.getRequestor().getOutputQueue().poll().getReport();

                assertTrue("RPL_WHOWASUSER for userlist", reply.equals(":" + prefix + " " + response));
            
                response = "312" + " " + userNickname[0]
                        + " " + userNickname[i]
                        + " " + db.getUser(userNickname[i]).getIrcServer().getHostname()
                        + " " + ":" + "";//db.getUser(userNickname[i]).getIrcServer().getInfo();
                reply = icp.getRequestor().getOutputQueue().poll().getReport();
                assertTrue("RPL_WHOISSERVER for userlist", reply.equals(":" + prefix + " " + response));
            }
            response = "369" + " " + userNickname[0] + " " + userNickname[i] + " " +":" + "End of WHOWAS";
            reply = icp.getRequestor().getOutputQueue().poll().getReport();
            assertTrue("RPL_ENDOFWHOWAS for userlist", reply.equals(":" + prefix + " " + response));
        }

        icp.setRequestor(db.getUser(userNickname[0]));
        mask = userNickname[0];
        String target = "irc.example.com";
        ircCommand = "WHOWAS" +" " + mask + " " + count + " " + target;
        icp.setParsingString(ircCommand);
        icp.ircParse();
        //402    ERR_NOSUCHSERVER "<server name> :No such server"

        response = "402" + " " + userNickname[0] + " " + target + " " +":" + "No such server";
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertTrue("ERR_NOSUCHSERVER", reply.equals(":" + prefix + " " + response));
        
        System.out.println("**WHOWAS*************************************OK**");
    }   
}    

