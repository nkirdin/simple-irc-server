/*
 * 
 * NoticeCommandTest
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
import org.grass.simpleircserver.connection.Connection;
import org.grass.simpleircserver.parser.IrcCommandParser;
import org.grass.simpleircserver.talker.IrcTalker;
import org.grass.simpleircserver.talker.service.Service;
import org.grass.simpleircserver.talker.user.User;
import org.grass.simpleircserver.tests.IrcCommandTest;
import org.junit.Test;

/**
 * NoticeCommandTest
 * @version 0.5.3.1 2015-11-06 
 * @author  Nikolay Kirdin
 */
public class NoticeCommandTest extends IrcCommandTest {
	
	@Test
    public void noticeCommandTest() {
        System.out.println("--NOTICE-----------------------------------------");
        IrcCommandParser icp = new IrcCommandParser();
        
        dropAll();
        serverInit();
        serviceInit();

        IrcTalker[] requestor = new IrcTalker[6];
        String[] userNickname = {"nick1", "nick2", "nick3"};
        String[] userUsername = {"user1", "user2", "user3"};
        String[] userRealname = {"Real Name 1", "Real Name 2", "Real Name 3"};
        String[] userMode = {"0", "0", "0"};
        String[] channelName = {"#channel1", "#channel2", "#channel3", "#channel4", "#channel5"};
        String[] channelMode = {"", "+n", "+m", "+b nick2", "+be nick? nick3"};

        String reply;
        String prefix;
        String ircCommand;
        String response;
        String content;

        int i;
        icp.setRequestor(User.create());
        ((User) icp.getRequestor()).setIrcServer(Globals.thisIrcServer.get());
        icp.getRequestor().setConnection(Connection.create());

        ircCommand = "NOTICE";
        content = "Hello world!";
        icp.setParsingString(ircCommand + " " + channelName[0] + " " + content);
        prefix = Globals.thisIrcServer.get().getHostname();
        response = "451" + " " + "" + " " + ":" + "You have not registered";
        icp.ircParse();
        assertTrue("No 'Not registered' reply", icp.getRequestor().getOutputQueue().isEmpty());
        
        ircCommand = "NICK";
        icp.setParsingString(ircCommand + " " + userNickname[0]);
        icp.ircParse();
        requestor[0] = db.getUser(userNickname[0]);
        icp.setRequestor(requestor[0]); 
        ircCommand = "USER";
        icp.setParsingString(ircCommand + " " + userUsername[0] + " " + userMode[0] + " " + "*" + " " + ":" + userRealname[0]);
        icp.ircParse();
        icp.getRequestor().dropOutputQueue();

        ircCommand = "NOTICE";
        content = "Hello world!";
        icp.setParsingString(ircCommand + " "+ ":" + content);
        prefix = Globals.thisIrcServer.get().getHostname();
        //411    ERR_NORECIPIENT ":No recipient given (<command>)"
        response = "411" + " " + userNickname[0] + " " + ":" + "No recipient given (" + ircCommand + ")";
        icp.ircParse();
        assertTrue(" No ERR_NORECIPIENT reply", icp.getRequestor().getOutputQueue().isEmpty());
        
        ircCommand = "NOTICE";
        icp.setParsingString(ircCommand + " " + userNickname[0]);
        prefix = Globals.thisIrcServer.get().getHostname();
        //412    ERR_NOTEXTTOSEND ":No text to send"
        response = "412" + " " + userNickname[0] + " " + ":" + "No text to send";
        icp.ircParse();
        assertTrue(" No ERR_NOTEXTTOSEND reply", icp.getRequestor().getOutputQueue().isEmpty());
        
        ircCommand = "NOTICE";
        content = "Hello world!";
        icp.setParsingString(ircCommand + " " + userNickname[1] + " " + ":" + content);
        prefix = Globals.thisIrcServer.get().getHostname();
        //401    ERR_NOSUCHNICK "<nickname> :No such nick/channel"
        response = "401" + " " + userNickname[0] + " " + userNickname[1] + " " + ":" + "No such nick/channel";
        icp.ircParse();
        assertTrue(" No ERR_NOSUCHNICK reply", icp.getRequestor().getOutputQueue().isEmpty());
        
        ircCommand = "NOTICE";
        content = "Hello world!";
        icp.setParsingString(ircCommand + " " + channelName[0] + " " + ":" + content);
        prefix = Globals.thisIrcServer.get().getHostname();
        //403    ERR_NOSUCHCHANNEL "<channel name> :No such channel"
        response = "403" + " " + userNickname[0] + " " + channelName[0] + " " + ":" + "No such channel";
        icp.ircParse();
        assertTrue(" No ERR_NOSUCHCHANNEL reply", icp.getRequestor().getOutputQueue().isEmpty());
        
        
        //404    ERR_CANNOTSENDTOCHAN "<channel name> :Cannot send to channel"
        i = 0;
        for (String name : channelName) {
            ircCommand = "JOIN";
            icp.setParsingString(ircCommand + " " + name);
            icp.ircParse();
            
            icp.getRequestor().dropOutputQueue();
            ircCommand = "MODE";
            icp.setParsingString(ircCommand + " " + name + " " + channelMode[i++]);
            icp.ircParse();
            db.getChannel(name).maxChannelRate.set(1000000);
        }
        icp.getRequestor().dropOutputQueue();
        
        icp.setRequestor(User.create());
        ((User) icp.getRequestor()).setIrcServer(Globals.thisIrcServer.get());
        icp.getRequestor().setConnection(Connection.create());

        
        ircCommand = "NICK";
        icp.setParsingString(ircCommand + " " + userNickname[2]);
        icp.ircParse();
        requestor[2] = db.getUser(userNickname[2]);
        
        ircCommand = "USER";
        icp.setParsingString(ircCommand + " " + userUsername[2] + " " + userMode[2] + " " + "*" + " " + ":" + userRealname[2]);
        icp.ircParse();
        icp.getRequestor().dropOutputQueue();

        ircCommand = "JOIN";
        icp.setParsingString(ircCommand + " " + channelName[1]);
        icp.ircParse();
        
        ircCommand = "JOIN";
        icp.setParsingString(ircCommand + " " + channelName[2]);
        icp.ircParse();
        
        
        icp.setRequestor(User.create());
        ((User) icp.getRequestor()).setIrcServer(Globals.thisIrcServer.get());
        icp.getRequestor().setConnection(Connection.create());

        
        ircCommand = "NICK";
        icp.setParsingString(ircCommand + " " + userNickname[1]);
        icp.ircParse();
        requestor[1] = db.getUser(userNickname[1]);
        
        ircCommand = "USER";
        icp.setParsingString(ircCommand + " " + userUsername[1] + " " + userMode[1] + " " + "*" + " " + ":" + userRealname[1]);
        icp.ircParse();
        
        
        requestor[0].dropOutputQueue();
        requestor[1].dropOutputQueue();
        requestor[2].dropOutputQueue();
        
        icp.setRequestor(icp.getRequestor());
        
        ircCommand = "NOTICE";
        content = "Hello world!";
        icp.setParsingString(ircCommand + " " + channelName[1] + " " + ":" + content);
        prefix = Globals.thisIrcServer.get().getHostname();
        response = "404" + " " + userNickname[1] + " " + channelName[1] + " " + ":" + "Cannot send to channel";
        icp.ircParse();
        assertTrue("No ERR_CANNOTSENDTOCHAN reply", icp.getRequestor().getOutputQueue().isEmpty());
                
        ircCommand = "JOIN";
        icp.setParsingString(ircCommand + " " + channelName[1]);
        icp.ircParse();
        
        requestor[0].dropOutputQueue();
        requestor[1].dropOutputQueue();
        requestor[2].dropOutputQueue();
        
        
        ircCommand = "NOTICE";
        content = "Hello world!";
        icp.setParsingString(ircCommand + " " + channelName[1] + " " + ":" + content);
        prefix = userNickname[1];
        response = ircCommand + " " + channelName[1] + " " + ":" + content;
        icp.ircParse();
        icp.setRequestor(requestor[0]);
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertTrue("Successfull send to +n channel(first user)", reply.equals(":" + prefix + " " + response)); 
        icp.setRequestor(requestor[2]);
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertTrue("Successfull send to +n channel(second user)", reply.equals(":" + prefix + " " + response));        
        
        icp.setRequestor(requestor[1]);
        ircCommand = "JOIN";
        icp.setParsingString(ircCommand + " " + channelName[2]);
        icp.ircParse();
        
        requestor[0].dropOutputQueue();
        requestor[1].dropOutputQueue();
        requestor[2].dropOutputQueue();
        
        ircCommand = "NOTICE";
        content = "Hello world!";
        icp.setParsingString(ircCommand + " " + channelName[2] + " " + ":" + content);
        prefix = Globals.thisIrcServer.get().getHostname();
        response = "404" + " " + userNickname[1] + " " + channelName[2] + " " + ":" + "Cannot send to channel";
        icp.ircParse();
        assertTrue("member of the channel? but cannot to send. No ERR_CANNOTSENDTOCHAN (+m)", icp.getRequestor().getOutputQueue().isEmpty());
        
        icp.setRequestor(requestor[0]);
      
        ircCommand = "MODE";
        userMode[1] = "+v";
        icp.setParsingString(ircCommand + " " + channelName[2] + " " + userMode[1] + " " + userNickname[1]);
        icp.ircParse();
        requestor[0].dropOutputQueue();
        requestor[1].dropOutputQueue();
        requestor[2].dropOutputQueue();
        
        icp.setRequestor(requestor[1]);
        
        ircCommand = "NOTICE";
        content = "Hello world!";
        icp.setParsingString(ircCommand + " " + channelName[2] + " " + ":" + content);
        prefix = userNickname[1];
        response = ircCommand + " " + channelName[2] + " " + ":" + content;
        icp.ircParse();
        icp.setRequestor(requestor[0]);
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertTrue("Successfull send to +m channel(first user)", reply.equals(":" + prefix + " " + response));
        icp.setRequestor(requestor[2]);
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertTrue("Successfull send to +m channel(second user)", reply.equals(":" + prefix + " " + response));
        
        icp.setRequestor(requestor[0]);
      
        ircCommand = "MODE";
        channelMode[2] = "-m+a";
        icp.setParsingString(ircCommand + " " + channelName[2] + " " + channelMode[2]);
        icp.ircParse();
        requestor[0].dropOutputQueue();
        requestor[1].dropOutputQueue();
        requestor[2].dropOutputQueue();
        
        icp.setRequestor(requestor[2]);
        ircCommand = "JOIN";
        icp.setParsingString(ircCommand + " " + "0");
        icp.ircParse();
        requestor[0].dropOutputQueue();
        requestor[1].dropOutputQueue();
        requestor[2].dropOutputQueue();
        
        icp.setRequestor(requestor[1]);
        
        ircCommand = "NOTICE";
        content = "Hello world!";
        icp.setParsingString(ircCommand + " " + channelName[2] + " " + ":" + content);
        prefix = Globals.anonymousUser.get().getNickname();
        response = ircCommand + " " + channelName[2] + " " + ":" + content;
        icp.ircParse();
        icp.setRequestor(requestor[0]);
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertTrue("Successfull send to +a channel(first user)", reply.equals(":" + prefix + " " + response));
        
        icp.setRequestor(requestor[2]);
        
        ircCommand = "NOTICE";
        content = "Hello world!";
        icp.setParsingString(ircCommand + " " + channelName[2] + " " + ":" + content);
        prefix = Globals.anonymousUser.get().getNickname();
        response = ircCommand + " " + channelName[2] + " " + ":" + content;
        icp.ircParse();
        icp.setRequestor(requestor[0]);
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertTrue("Successfull send to +a channel(first user)", reply.equals(":" + prefix + " " + response));
        icp.setRequestor(requestor[1]);
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertTrue("Successfull send to +a channel(second user)", reply.equals(":" + prefix + " " + response));
        
        icp.setRequestor(requestor[2]);
        
        ircCommand = "NOTICE";
        content = "Hey folks!";
        icp.setParsingString(ircCommand + " " + userNickname[0] + "," + userNickname[1] + " " + ":" + content);
        icp.ircParse();
        prefix = userNickname[2];
        response = ircCommand + " " + userNickname[0] + " " + ":" + content;
        icp.setRequestor(requestor[0]);
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertTrue("Successfull send to users list (first user)", reply.equals(":" + prefix + " " + response));
        icp.setRequestor(requestor[1]);
        response = ircCommand + " " + userNickname[1] + " " + ":" + content;
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertTrue("Successfull send to users list (second user)", reply.equals(":" + prefix + " " + response));
        
        icp.setRequestor(requestor[2]);
        
        ircCommand = "AWAY";
        String awayText = "Gone to supper.";
        icp.setParsingString(ircCommand + " " + ":" + awayText);
        icp.ircParse();
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        
        icp.setRequestor(requestor[1]);
        
        ircCommand = "NOTICE";
        content = "Do you want same cap of tea?";
        icp.setParsingString(ircCommand + " " + userNickname[2] + " " + ":" + content);
        prefix = Globals.thisIrcServer.get().getHostname();
        //301    RPL_AWAY "<nick> :<away message>"
        response = "301" + " " + userNickname[1] + " " + userNickname[2] + " " + ":" + awayText;
        icp.ircParse();
        assertTrue("No away reply.", icp.getRequestor().getOutputQueue().isEmpty());

        for (IrcTalker rqstr : requestor) {
            if (rqstr == null) continue;
            
            icp.setRequestor(rqstr);
            
            ircCommand = "QUIT";
            icp.setParsingString(ircCommand);
            icp.ircParse();
            icp.getRequestor().dropOutputQueue();
        }
        
        icp = new IrcCommandParser();
        
        dropAll();
        serverInit();
        serviceInit();

        String[] serviceNickname = {"service1", "service2", "service3"};
        String[] serviceDistribution = {"*.com", "*.net", "*.local"};
        String[] serviceInfo = {"Service Info 1", "Service Info 2", "Service Info 3"};

        icp.setRequestor(Service.create());
        
        icp.getRequestor().setNickname("SimpleService");
        ((Service) icp.getRequestor()).setIrcServer(Globals.thisIrcServer.get());
        icp.getRequestor().setConnection(Connection.create());
        
        ircCommand = "NOTICE";
        content = "Hello world!";
        icp.setParsingString(ircCommand + " " + channelName[0] + " " + content);
        prefix = Globals.thisIrcServer.get().getHostname();
        response = "451" + " " + "" + " " + ":" + "You have not registered";
        icp.ircParse();
        assertTrue("No 'Not registered' reply", icp.getRequestor().getOutputQueue().isEmpty());
        
        icp.setRequestor(User.create());
        ((User) icp.getRequestor()).setIrcServer(Globals.thisIrcServer.get());
        icp.getRequestor().setConnection(Connection.create());

        
        ircCommand = "SERVICE";
        icp.setParsingString(ircCommand + " " + serviceNickname[0] + " * "
            + serviceDistribution[0] + " * " + "*" + " " + ":" + serviceInfo[0]);
        icp.ircParse();
        requestor[3] = db.getService(serviceNickname[0]);
        icp.getRequestor().dropOutputQueue();
        
        ircCommand = "NOTICE";
        content = "Hello world!";
        icp.setParsingString(ircCommand + " "+ ":" + content);
        prefix = Globals.thisIrcServer.get().getHostname();
        //411    ERR_NORECIPIENT ":No recipient given (<command>)"
        response = "411" + " " + userNickname[0] + " " + ":" + "No recipient given (" + ircCommand + ")";
        icp.ircParse();
        assertTrue(" No ERR_NORECIPIENT reply", icp.getRequestor().getOutputQueue().isEmpty());
        
        ircCommand = "NOTICE";
        icp.setParsingString(ircCommand + " " + userNickname[0]);
        prefix = Globals.thisIrcServer.get().getHostname();
        //412    ERR_NOTEXTTOSEND ":No text to send"
        response = "412" + " " + userNickname[0] + " " + ":" + "No text to send";
        icp.ircParse();
        assertTrue(" No ERR_NOTEXTTOSEND reply", icp.getRequestor().getOutputQueue().isEmpty());
        
        ircCommand = "NOTICE";
        content = "Hello world!";
        icp.setParsingString(ircCommand + " " + userNickname[0] + " " + ":" + content);
        prefix = Globals.thisIrcServer.get().getHostname();
        //401    ERR_NOSUCHNICK "<nickname> :No such nick/channel"
        response = "401" + " " + userNickname[0] + " " + userNickname[1] + " " + ":" + "No such nick/channel";
        icp.ircParse();
        assertTrue(" No ERR_NOSUCHNICK reply", icp.getRequestor().getOutputQueue().isEmpty());
        
        ircCommand = "NOTICE";
        content = "Hello world!";
        icp.setParsingString(ircCommand + " " + channelName[0] + " " + ":" + content);
        prefix = Globals.thisIrcServer.get().getHostname();
        //403    ERR_NOSUCHCHANNEL "<channel name> :No such channel"
        response = "403" + " " + userNickname[0] + " " + channelName[0] + " " + ":" + "No such channel";
        icp.ircParse();
        assertTrue(" No ERR_NOSUCHCHANNEL reply", icp.getRequestor().getOutputQueue().isEmpty());
        
        for(i = 0; i < 3; i++){
            icp.setRequestor(User.create());
        ((User) icp.getRequestor()).setIrcServer(Globals.thisIrcServer.get());
        icp.getRequestor().setConnection(Connection.create());

            ircCommand = "NICK";
            icp.setParsingString(ircCommand + " " + userNickname[i]);
            icp.ircParse();
            requestor[i] = icp.getRequestor();
         
            ircCommand = "USER";
            icp.setParsingString(ircCommand + " " + userUsername[i] + " " + "0" + " " + "*" + " " + ":" + userRealname[i]);
            icp.ircParse();
            
            icp.getRequestor().dropOutputQueue();
        }
        
        i = 0;
        icp.setRequestor(requestor[0]);
        for (String name : channelName) {
            ircCommand = "JOIN";
            icp.setParsingString(ircCommand + " " + name);
            icp.ircParse();
            
            icp.getRequestor().dropOutputQueue();
            ircCommand = "MODE";
            icp.setParsingString(ircCommand + " " + name + " " + channelMode[i++]);
            icp.ircParse();
        }
        icp.getRequestor().dropOutputQueue();
        
        icp.setRequestor(requestor[3]);
        
        ircCommand = "NOTICE";
        content = "Hello world!";
        icp.setParsingString(ircCommand + " " + channelName[1] + " " + ":" + content);
        prefix = Globals.thisIrcServer.get().getHostname();
        response = "404" + " " + serviceNickname[0] + " " + channelName[1] + " " + ":" + "Cannot send to channel";
        icp.ircParse();
        assertTrue("No ERR_CANNOTSENDTOCHAN reply", icp.getRequestor().getOutputQueue().isEmpty());
        
        icp.setRequestor(requestor[3]);
        ircCommand = "NOTICE";
        content = "Hey folks!";
        icp.setParsingString(ircCommand + " " + userNickname[0] + "," + userNickname[1] + " " + ":" + content);
        icp.ircParse();
        prefix = serviceNickname[0];
        response = ircCommand + " " + userNickname[0] + " " + ":" + content;
        
        icp.setRequestor(db.getUser(userNickname[0]));
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertTrue("Successfull send to users list (first user)", reply.equals(":" + prefix + " " + response));
        icp.setRequestor(requestor[1]);
        response = ircCommand + " " + userNickname[1] + " " + ":" + content;
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertTrue("Successfull send to users list (second user)", reply.equals(":" + prefix + " " + response));
        
        icp.setRequestor(requestor[2]);
        
        ircCommand = "AWAY";
        awayText = "Gone to supper.";
        icp.setParsingString(ircCommand + " " + ":" + awayText);
        icp.ircParse();
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        
        icp.setRequestor(requestor[3]);
        
        ircCommand = "NOTICE";
        content = "Do you want same cap of tea?";
        icp.setParsingString(ircCommand + " " + userNickname[2] + " " + ":" + content);
        prefix = Globals.thisIrcServer.get().getHostname();
        //301    RPL_AWAY "<nick> :<away message>"
        response = "301" + " " + serviceNickname[0] + " " + userNickname[2] + " " + ":" + awayText;
        icp.ircParse();
        assertTrue("No away reply.", icp.getRequestor().getOutputQueue().isEmpty());

        icp.setRequestor(Service.create());
        
        ((Service) icp.getRequestor()).setIrcServer(Globals.thisIrcServer.get());
                
        icp.setParsingString(ircCommand + " " + serviceNickname[1] + " * "
            + serviceDistribution[1] + " * " + "*" + " " + ":" + serviceInfo[1]);
        icp.ircParse();
        icp.getRequestor().dropOutputQueue();
        requestor[4] = db.getService(serviceNickname[1]);
        
        icp.setRequestor(requestor[3]);
        
        ircCommand = "NOTICE";
        content = "Hey folks!";
        icp.setParsingString(ircCommand + " " + serviceNickname[1] + " " + ":" + content);
        icp.ircParse();

        assertTrue("No reply for notice.", icp.getRequestor().getOutputQueue().isEmpty());
        
        icp.setRequestor(requestor[4]);
        assertTrue("No delivering to service.", icp.getRequestor().getOutputQueue().isEmpty());
        
        System.out.println("**NOTICE*************************************OK**");
    }   
}    

