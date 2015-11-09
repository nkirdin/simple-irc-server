/*
 * 
 * WhoCommandTest
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

import java.util.EnumSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.grass.simpleircserver.base.Globals;
import org.grass.simpleircserver.channel.ChannelMode;
import org.grass.simpleircserver.channel.IrcChannel;
import org.grass.simpleircserver.connection.Connection;
import org.grass.simpleircserver.parser.IrcCommandParser;
import org.grass.simpleircserver.talker.user.User;
import org.grass.simpleircserver.tests.IrcCommandTest;
import org.grass.simpleircserver.tools.IrcMatcher;
import org.junit.Test;

/**
 * WhoCommandTest
 * @version 0.5.3.1 2015-11-06 
 * @author  Nikolay Kirdin
 */
public class WhoCommandTest extends IrcCommandTest {
	
	@Test
    public void whoCommandTest() {
        //System.out.println("--WHO--------------------------------------------");
        IrcCommandParser icp = new IrcCommandParser();

        dropAll();
        serverInit();
        userInit(); operatorInit();
        serviceInit();
        
        String reply;
        String prefix;
        String ircCommand;
        String response;
        String status;
        String channelNickname;
        Map<String, LinkedHashSet<User>> outputUserMap = new LinkedHashMap<String, LinkedHashSet<User>>();
        
        boolean operMode = false;
        boolean channelSearch = false;

        int i;
        
        icp.setRequestor(User.create());
        ((User) icp.getRequestor()).setIrcServer(Globals.thisIrcServer.get());
        Connection conn = Connection.create();
        
        conn.ircTalker.set(icp.getRequestor());
        icp.getRequestor().setConnection(conn);
        db.register(conn);


        ircCommand = "WHO";
        String mask = "0";
        icp.setParsingString(ircCommand + " " + mask);
        prefix = Globals.thisIrcServer.get().getHostname();
        response = "451" + " " + "" + " " + ":" + "You have not registered";
        icp.ircParse();
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertTrue("Not registered", reply.equals(":" + prefix + " " + response));
                 
        icp.setRequestor(requestor[0]);
        
        //402    ERR_NOSUCHSERVER "<server name> :No such server" 
        //ircCommand = "WHO";
        //mask = "no.such.server";
        //icp.setParsingString(ircCommand + " " + mask);
        //prefix = Globals.thisIrcServer.get().getHostname();
        //response = "402" + " " + userNickname[0] + " " + mask + " " + ":" + "You have not registered";
        //icp.ircParse();
        //reply = icp.getRequestor().getOutputQueue().poll().getReport();
        //assertTrue("ERR_NOSUCHSERVER", reply.equals(":" + prefix + " " + response));

        //352    RPL_WHOREPLY "<channel> <user> <host> <server> <nick> ( "H" / "G" > ["*"] [ ( "@" / "+" ) ]  :<hopcount> <real name>"
        //315    RPL_ENDOFWHO "<name> :End of WHO list"
        
        
        ircCommand = "WHO" + " " + mask;
        icp.setParsingString(ircCommand);
        icp.ircParse();
        //System.out.println(icp.getRequestor().getOutputQueue());
        operMode = false;
        String usedMask = "*";
        outputUserMap = new LinkedHashMap<String, LinkedHashSet<User>>();   
        for (Iterator<User> userSetIterator = db.getUserSetIterator(); userSetIterator.hasNext();) {
            User user = userSetIterator.next();
            String token = null;   
            if (!usedMask.equals("*")) {
                if (IrcMatcher.match(usedMask, user.getHostname())) {
                    token = user.getHostname();       
                } else if (IrcMatcher.match(usedMask, user.getIrcServer().getHostname())) {
                    token = user.getIrcServer().getHostname();
                } else if (IrcMatcher.match(usedMask, user.getRealname())) {
                    token = user.getRealname();
                } else if (IrcMatcher.match(usedMask, user.getNickname())) {
                    token = user.getNickname();      
                } else {
                    continue;
                }
            } else {
               token = usedMask; 
            }

            if (!user.isVisible(icp.getRequestor()) || (operMode && !user.isOperator())) continue;
                
            if (outputUserMap.get(token) == null) {
                outputUserMap.put(token, new LinkedHashSet<User>());
            }
            //System.out.println(":" + token + " " + user);
            outputUserMap.get(token).add(user);
        }  
        
        channelNickname = "*";
        for (String token : outputUserMap.keySet()) {
            IrcChannel ch = null;
        //352    RPL_WHOREPLY "<channel> <user> <host> <server> <nick> ("H"|"G")["*"][("@"|"+")]  :<hopcount> <real name>" 
            if (channelSearch) { 
                ch = db.getChannel(token);
                if (ch == null) continue;
                channelNickname = token;
            }
            for (User user : outputUserMap.get(token)) {
                status = (user.hasAwayText() ? "G" : "H") 
                    + (user.isOperator() ? "*" : "");
                if (channelSearch) {
                    status = status + ch.getStatus(user);
                }                            
            
                prefix = Globals.thisIrcServer.get().getHostname();
                response = "352" + " " + requestor[0].getNickname()
                        + " " + channelNickname
                        + " " + user.getUsername()
                        + " " + user.getHostname()
                        + " " + user.getIrcServer().getHostname()
                        + " " + user.getNickname()
                        + " " + status
                        + " " + ":"
                        + ((User) user).getIrcServer().getHopcount()
                        + " " + user.getRealname();
                reply = icp.getRequestor().getOutputQueue().poll().getReport();
                //System.out.println(":" + prefix + " " + response);
                assertTrue("RPL_WHOREPLY no channels", reply.equals(":" + prefix + " " + response));
            }
            response = "315" + " " + requestor[0].getNickname() + " " + token + " " +":" + "End of WHO list";
            reply = icp.getRequestor().getOutputQueue().poll().getReport();
            //System.out.println(":" + prefix + " " + response);
            assertTrue("RPL_ENDOFWHO no channels", reply.equals(":" + prefix + " " + response));
        }
        
        for (i = 0; i < requestor.length ; i++) {
            icp.setRequestor(requestor[i]);
            
            ircCommand = "JOIN";
            icp.setParsingString(ircCommand + " " + channelName[0]);
            icp.ircParse();
        }
        
        for (Iterator<User> userSetIterator = db.getUserSetIterator(); userSetIterator.hasNext();) {
            User user = userSetIterator.next();
            if (user.getOutputQueue() != null) user.getOutputQueue().clear();
        }
        
        icp.setRequestor(requestor[0]);
        mask = channelName[0];
        ircCommand = "WHO" + " " + mask;
        icp.setParsingString(ircCommand);
        icp.ircParse();

        IrcChannel channel = db.getChannel(channelName[0]);
        for (Iterator<Map.Entry<User, EnumSet <ChannelMode>>> 
            userSetIterator = channel.getUserEntrySetIterator(); 
            userSetIterator.hasNext();) {
            Map.Entry<User, EnumSet <ChannelMode>> userEntry = userSetIterator.next();
            User user = userEntry.getKey();

            prefix = Globals.thisIrcServer.get().getHostname();
            channelNickname = channelName[0];
            status = (user.hasAwayText() ? "G" : "H") 
                    + (user.isOperator() ? "*" : "");
                if (!channelNickname.equals("*")) {
                    IrcChannel ch = db.getChannel(channelNickname);
                    if (ch != null) status = status + ch.getStatus(user);
                }                            
            
            response = "352" + " " + requestor[0].getNickname()
                    + " " + channelNickname
                    + " " + user.getUsername()
                    + " " + user.getHostname()
                    + " " + user.getIrcServer().getHostname()
                    + " " + user.getNickname()
                    + " " + status
                    + " " + ":"
                    + user.getIrcServer().getHopcount()
                    + " " + user.getRealname();

            reply = icp.getRequestor().getOutputQueue().poll().getReport();
            assertTrue("RPL_WHOREPLY with different channels", reply.equals(":" + prefix + " " + response));
        }
        response = "315" + " " + requestor[0].getNickname() + " " + channelName[0] + " " +":" + "End of WHO list";
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertTrue("RPL_ENDOFWHO with different channels", reply.equals(":" + prefix + " " + response));
                
        mask = requestor[1].getHostname();
        ircCommand = "WHO" + " " + mask;
        icp.setParsingString(ircCommand);
        icp.ircParse();
        for (Iterator<User> userSetIterator = db.getUserSetIterator(); userSetIterator.hasNext();) {
            User user = userSetIterator.next();
            prefix = Globals.thisIrcServer.get().getHostname();
            channelNickname = "*";
            status = (user.hasAwayText() ? "G" : "H") 
                    + (user.isOperator() ? "*" : "");
                if (!channelNickname.equals("*")) {
                    IrcChannel ch = db.getChannel(channelNickname);
                    if (ch != null) status = status + ch.getStatus(user);
                }                            
            
            if (user.getHostname().equals(requestor[1].getHostname())) {
                response = "352" + " " + requestor[0].getNickname()
                        + " " + channelNickname
                        + " " + user.getUsername()
                        + " " + user.getHostname()
                        + " " + user.getIrcServer().getHostname()
                        + " " + user.getNickname()
                        + " " + status
                        + " " + ":"
                        + ((User) user).getIrcServer().getHopcount()
                        + " " + user.getRealname();
            
                 reply = icp.getRequestor().getOutputQueue().poll().getReport();
                 assertTrue("RPL_WHOREPLY with mask for hostname", reply.equals(":" + prefix + " " + response));
            }
        }
        response = "315" + " " + requestor[0].getNickname() + " " + requestor[1].getHostname() + " " +":" + "End of WHO list";
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertTrue("RPL_ENDOFWHO with mask for hostname", reply.equals(":" + prefix + " " + response));
        //System.out.println("**WHO****************************************OK**");
    }   
}    

