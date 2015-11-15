/*
 * 
 * PrivmsgCommandTest
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
 * PrivmsgCommandTest
 * @version 0.5.3.1 2015-11-06 
 * @author  Nikolay Kirdin
 */
public class PrivmsgCommandTest extends IrcCommandTest {
	
    @Test
    public void privmsgCommandTest() {
        System.out.println("--PRIVMSG----------------------------------------");
        IrcCommandParser icp = new IrcCommandParser();
        
        dropAll();
        serverInit();
        serviceInit();

        IrcTalker[] requestor = new User[3];
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
        
        ircCommand = "PRIVMSG";
        content = "Hello world!";
        icp.setParsingString(ircCommand + " " + channelName[0] + " " + content);
        prefix = Globals.thisIrcServer.get().getHostname();
        response = "451" + " " + "" + " " + ":" + "You have not registered";
        icp.ircParse();
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertTrue("Not registered", reply.equals(":" + prefix + " " + response));
        
        ircCommand = "NICK";
        icp.setParsingString(ircCommand + " " + userNickname[0]);
        icp.ircParse();
        requestor[0] = icp.getRequestor();
        
        ircCommand = "USER";
        icp.setParsingString(ircCommand + " " + userUsername[0] + " " + userMode[0] + " " + "*" + " " + ":" + userRealname[0]);
        icp.ircParse();
        icp.getRequestor().getOutputQueue().clear();
        
        ircCommand = "PRIVMSG";
        icp.setParsingString(ircCommand);
        prefix = Globals.thisIrcServer.get().getHostname();
        //411    ERR_NORECIPIENT ":No recipient given (<command>)"
        response = "411" + " " + userNickname[0] + " " + ":" + "No recipient given (" + ircCommand + ")";
        icp.ircParse();
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertTrue("ERR_NORECIPIENT", reply.equals(":" + prefix + " " + response));
        
        ircCommand = "PRIVMSG";
        icp.setParsingString(ircCommand + " " + userNickname[0]);
        prefix = Globals.thisIrcServer.get().getHostname();
        //412    ERR_NOTEXTTOSEND ":No text to send"
        response = "412" + " " + userNickname[0] + " " + ":" + "No text to send";
        icp.ircParse();
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertTrue("ERR_NOTEXTTOSEND userNickname[0]", reply.equals(":" + prefix + " " + response));
        
        ircCommand = "PRIVMSG";
        content = "Hello world!";
        icp.setParsingString(ircCommand + " " + userNickname[1] + " " + ":" + content);
        prefix = Globals.thisIrcServer.get().getHostname();
        //401    ERR_NOSUCHNICK "<nickname> :No such nick/channel"
        response = "401" + " " + userNickname[0] + " " + userNickname[1] + " " + ":" + "No such nick/channel";
        icp.ircParse();
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertTrue("ERR_NOSUCHNICK userNickname[1]", reply.equals(":" + prefix + " " + response));
        
        ircCommand = "PRIVMSG";
        content = "Hello world!";
        icp.setParsingString(ircCommand + " " + channelName[0] + " " + ":" + content);
        prefix = Globals.thisIrcServer.get().getHostname();
        //403    ERR_NOSUCHCHANNEL "<channel name> :No such channel"
        response = "403" + " " + userNickname[0] + " " + channelName[0] + " " + ":" + "No such channel";
        icp.ircParse();
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertTrue("ERR_NOSUCHCHANNEL channelName[0]", reply.equals(":" + prefix + " " + response));
        
        //404    ERR_CANNOTSENDTOCHAN "<channel name> :Cannot send to channel"
        i = 0;
        for (String name : channelName) {
            ircCommand = "JOIN";
            icp.setParsingString(ircCommand + " " + name);
            icp.ircParse();
            
            icp.getRequestor().getOutputQueue().clear();
            ircCommand = "MODE";
            icp.setParsingString(ircCommand + " " + name + " " + channelMode[i++]);
            icp.ircParse();
            db.getChannel(name).maxChannelRate.set(1000000);
        }
        icp.getRequestor().getOutputQueue().clear();
        
        icp.setRequestor(User.create());
        ((User) icp.getRequestor()).setIrcServer(Globals.thisIrcServer.get());
        icp.getRequestor().setConnection(Connection.create());

        ircCommand = "NICK";
        icp.setParsingString(ircCommand + " " + userNickname[2]);
        icp.ircParse();
        requestor[2] = icp.getRequestor();
        
        ircCommand = "USER";
        icp.setParsingString(ircCommand + " " + userUsername[2] + " " + userMode[2] + " " + "*" + " " + ":" + userRealname[2]);
        icp.ircParse();
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        icp.getRequestor().getOutputQueue().clear();

        ircCommand = "JOIN";
        icp.setParsingString(ircCommand + " " + channelName[1]);
        icp.ircParse();
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        icp.getRequestor().getOutputQueue().clear();
        
        ircCommand = "JOIN";
        icp.setParsingString(ircCommand + " " + channelName[2]);
        icp.ircParse();
        icp.getRequestor().getOutputQueue().clear();

        icp.setRequestor(User.create());
        ((User) icp.getRequestor()).setIrcServer(Globals.thisIrcServer.get());
        icp.getRequestor().setConnection(Connection.create());

        ircCommand = "NICK";
        icp.setParsingString(ircCommand + " " + userNickname[1]);
        icp.ircParse();
        requestor[1] = icp.getRequestor();
        
        ircCommand = "USER";
        icp.setParsingString(ircCommand + " " + userUsername[1] + " " + userMode[1] + " " + "*" + " " + ":" + userRealname[1]);
        icp.ircParse();
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        
        requestor[0].getOutputQueue().clear();
        requestor[1].getOutputQueue().clear();
        requestor[2].getOutputQueue().clear();

        ircCommand = "PRIVMSG";
        content = "Hello world!";
        icp.setParsingString(ircCommand + " " + channelName[1] + " " + ":" + content);
        prefix = Globals.thisIrcServer.get().getHostname();
        response = "404" + " " + userNickname[1] + " " + channelName[1] + " " + ":" + "Cannot send to channel";
        icp.ircParse();
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertTrue("ERR_CANNOTSENDTOCHAN (+n)", reply.equals(":" + prefix + " " + response));
        
        ircCommand = "PRIVMSG";
        content = "Hello world!";
        icp.setParsingString(ircCommand + " " + channelName[2] + " " + ":" + content);
        prefix = Globals.thisIrcServer.get().getHostname();
        response = "404" + " " + userNickname[1] + " " + channelName[2] + " " + ":" + "Cannot send to channel";
        icp.ircParse();
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertTrue("ERR_CANNOTSENDTOCHAN (+m)", reply.equals(":" + prefix + " " + response));
        
        ircCommand = "JOIN";
        icp.setParsingString(ircCommand + " " + channelName[1]);
        icp.ircParse();
        requestor[0].getOutputQueue().clear();
        requestor[1].getOutputQueue().clear();
        requestor[2].getOutputQueue().clear();

        
        ircCommand = "PRIVMSG";
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
        requestor[0].getOutputQueue().clear();
        requestor[1].getOutputQueue().clear();
        requestor[2].getOutputQueue().clear();
        
        ircCommand = "PRIVMSG";
        content = "Hello world!";
        icp.setParsingString(ircCommand + " " + channelName[2] + " " + ":" + content);
        prefix = Globals.thisIrcServer.get().getHostname();
        response = "404" + " " + userNickname[1] + " " + channelName[2] + " " + ":" + "Cannot send to channel";
        icp.ircParse();
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertTrue("member of the channel? but cannot to send. ERR_CANNOTSENDTOCHAN (+m)", reply.equals(":" + prefix + " " + response));
        
        icp.setRequestor(requestor[0]);
      
        ircCommand = "MODE";
        userMode[1] = "+v";
        icp.setParsingString(ircCommand + " " + channelName[2] + " " + userMode[1] + " " + userNickname[1]);
        icp.ircParse();
        requestor[0].getOutputQueue().clear();
        requestor[1].getOutputQueue().clear();
        requestor[2].getOutputQueue().clear();
        
        icp.setRequestor(requestor[1]);
        ircCommand = "PRIVMSG";
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
        requestor[0].getOutputQueue().clear();
        requestor[1].getOutputQueue().clear();
        requestor[2].getOutputQueue().clear();
        
        icp.setRequestor(requestor[2]);
        ircCommand = "JOIN";
        icp.setParsingString(ircCommand + " " + "0");
        icp.ircParse();
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        requestor[0].getOutputQueue().clear();
        requestor[1].getOutputQueue().clear();
        requestor[2].getOutputQueue().clear();
        
        icp.setRequestor(requestor[1]);
        
        ircCommand = "PRIVMSG";
        content = "Hello world!";
        icp.setParsingString(ircCommand + " " + channelName[2] + " " + ":" + content);
        prefix = Globals.anonymousUser.get().getNickname();
        response = ircCommand + " " + channelName[2] + " " + ":" + content;
        icp.ircParse();
        icp.setRequestor(requestor[0]);
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertTrue("Successfull send to +a channel(first user)", reply.equals(":" + prefix + " " + response));
        
        icp.setRequestor(requestor[2]);
        
        ircCommand = "PRIVMSG";
        content = "Hello world!";
        icp.setParsingString(ircCommand + " " + channelName[2] + " " + ":" + content);
        prefix = Globals.anonymousUser.get().getNickname();
        response = ircCommand + " " + channelName[2] + " " + ":" + content;
        icp.ircParse();
        //System.out.println(icp.getRequestor().getOutputQueue());
        icp.setRequestor(requestor[0]);
        //System.out.println(icp.getRequestor().getOutputQueue());
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertTrue("Successfull send to +a channel(first user)", reply.equals(":" + prefix + " " + response));
        icp.setRequestor(requestor[1]);
        //System.out.println(icp.getRequestor().getOutputQueue());
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertTrue("Successfull send to +a channel(second user)", reply.equals(":" + prefix + " " + response));
        
        icp.setRequestor(requestor[2]);
        
        ircCommand = "PRIVMSG";
        content = "Hey folks!";
        icp.setParsingString(ircCommand + " " + userNickname[0] + "," + userNickname[1] + " " + ":" + content);
        icp.ircParse();
        //System.out.println(icp.getRequestor().getOutputQueue());
        prefix = userNickname[2];
        response = ircCommand + " " + userNickname[0] + " " + ":" + content;
        icp.setRequestor(requestor[0]);
        //System.out.println(icp.getRequestor().getOutputQueue());
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertTrue("Successfull send to users list (first user)", reply.equals(":" + prefix + " " + response));
        icp.setRequestor(requestor[1]);
        //System.out.println(icp.getRequestor().getOutputQueue());
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
        
        ircCommand = "PRIVMSG";
        content = "Do you want same cap of tea?";
        icp.setParsingString(ircCommand + " " + userNickname[2] + " " + ":" + content);
        prefix = Globals.thisIrcServer.get().getHostname();
        //301    RPL_AWAY "<nick> :<away message>"
        response = "301" + " " + userNickname[1] + " " + userNickname[2] + " " + ":" + awayText;
        icp.ircParse();
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertTrue("Successfull away reply.", reply.equals(":" + prefix + " " + response));
        
        System.out.println("**PRIVMSG************************************OK**");
    }   
}    

