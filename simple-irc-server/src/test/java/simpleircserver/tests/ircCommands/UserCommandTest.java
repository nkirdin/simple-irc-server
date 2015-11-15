/*
 * 
 * UserCommandTest
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

import simpleircserver.base.Constants;
import simpleircserver.base.Globals;
import simpleircserver.connection.Connection;
import simpleircserver.parser.IrcCommandParser;
import simpleircserver.talker.user.User;
import simpleircserver.tests.IrcCommandTest;
import simpleircserver.tests.ServerTestUtils;

/**
 * UserCommandTest
 * @version 0.5.3.1 2015-11-06 
 * @author  Nikolay Kirdin
 */
public class UserCommandTest extends IrcCommandTest {
	
    @Test
    public void userCommandTest() {
        System.out.println("--USER-------------------------------------------");
        
        String reply;
        String prefix;
        String ircCommand;
        String response;
       
        dropAll();
        serverInit();
        // It needs to correct path to motd because there are strange errors when testing with surefire.
        String motdFilePath = ServerTestUtils.buildResourceFilePath(Constants.MOTD_FILE_PATH);
        Globals.motdFilename.set(motdFilePath);
        
        IrcCommandParser icp = new IrcCommandParser();
        
        icp.setRequestor(User.create());
        ((User) icp.getRequestor()).setIrcServer(Globals.thisIrcServer.get());
        icp.getRequestor().setConnection(Connection.create());
        
        ircCommand = "NICK";
        icp.setParsingString(ircCommand + " " + userNickname[0]);
        icp.ircParse();
        
        ircCommand = "USER";
        
        // 421    ERR_UNKNOWNCOMMAND "<command> :Unknown command"
        String errouneous = "A1234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789";
        
        prefix = Globals.thisIrcServer.get().getHostname();
        icp.setParsingString(ircCommand + " " + errouneous + " " + "0" + " " + "*" + " " + ":" + userRealname[0]);
        response = "421" + " " + userNickname[0] + " " + ircCommand + " " + ":Unknown command";
        icp.ircParse();
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertTrue("ERR_UNKNOWNCOMMAND", reply.equals(":" + prefix + " " + response));

/*        
        icp.setParsingString(ircCommand + " " +  "*" + " " + "*" + " " + ":" + userRealname[0]);
        response = "421" + " " + userNickname[0] + " " + ircCommand + " " + ":Unknown command";
        icp.ircParse();
        System.out.println(icp.getRequestor().getOutputQueue());
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertTrue("ERR_UNKNOWNCOMMAND", reply.equals(":" + prefix + " " + response));
*/

        //461    ERR_NEEDMOREPARAMS "<command> :Not enough parameters"
        icp.setParsingString(ircCommand + " " + userUsername[0] + " " + "0" + " " + "*");// + " " + ":";// + userRealname;
        response = "461" + " " + userNickname[0] + " " + ircCommand + " " + ":Not enough parameters";
        icp.ircParse();
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertTrue("ERR_NEEDMOREPARAMS", reply.equals(":" + prefix + " " + response));
/*        
        icp.setParsingString(ircCommand + " " + userUsername[0] + " " + "0" +  ":");// + userRealname;
        response = "421" + " " + userNickname[0] + " " + ircCommand + " " + ":Unknown command";
        icp.ircParse();
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertTrue("ERR_UNKNOWNCOMMAND", reply.equals(":" + prefix + " " + response));
        
        icp.setParsingString(ircCommand + " " + userUsername[0] + " " + "0" +  ":");// + userRealname;
        response = "421" + " " + userNickname[0] + " " + ircCommand + " " + ":Unknown command";
        icp.ircParse();
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertTrue("ERR_UNKNOWNCOMMAND", reply.equals(":" + prefix + " " + response));
*/

        ircCommand = "QUIT";
        icp.setParsingString(ircCommand);
        icp.ircParse();
        icp.getRequestor().getOutputQueue().clear();
       
        dropUser();
        
        icp.setRequestor(User.create());
        ((User) icp.getRequestor()).setIrcServer(Globals.thisIrcServer.get());
        icp.getRequestor().setConnection(Connection.create());

        ircCommand = "NICK";
        icp.setParsingString(ircCommand + " " + userNickname[0]);
        icp.ircParse();
           
        ircCommand = "USER";
        prefix = Globals.thisIrcServer.get().getHostname();
        icp.setParsingString(ircCommand + " " + userUsername[0] + " " + userMode[0] + " " + "*" + " " + ":" + userRealname[0]);
        icp.ircParse();
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        //001    RPL_WELCOME "Welcome to the Internet Relay Network <nick>!<user>@<host>" 
        response = "001" + " " + userNickname[0] + " " + ":" + "Welcome to the Internet Relay Network " + ".*";
        assertTrue("RPL_WELCOME", reply.matches(":" + prefix + " " + response));
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        //002    RPL_YOURHOST "Your host is <servername>, running version <ver>" 
        response = "002" + " " + userNickname[0] + " " + ":" + "Your host is " + ".*";
        assertTrue("RPL_YOURHOST", reply.matches(":" + prefix + " " + response));
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        //003    RPL_CREATED "This server was created <date>" 
        response = "003" + " " + userNickname[0] + " " + ":" + "This server was created " + ".*";
        assertTrue("RPL_CREATED", reply.matches(":" + prefix + " " + response));
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        //004    RPL_MYINFO "<servername> <version> <available user modes> <available channel modes>" 
        response = "004" + " " + userNickname[0] + " " + Globals.thisIrcServer.get().getHostname()
                         + " " + Constants.SERVER_VERSION 
                         + " " + Constants.USER_MODES
                         + " " + Constants.CHANNEL_MODES;
        assertTrue("RPL_MYINFO", reply.matches(":" + prefix + " " + response));
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        //005   RPL_ISUPPORT
        response = "005" + " " + userNickname[0] 
                         + " " + "PREFIX=" + Constants.PREFIX
                         + " " + "CHANTYPES=" + Constants.CHANTYPES
                         + " " + "MODES=" + Constants.MODES
                         + " " + "CHANLIMIT=" + Constants.CHANTYPES + ":" + Constants.CHANLIMIT
                         + " " + "NICKLEN=" + Constants.NICKLEN
                         + " " + "TOPIC_LEN=" + Constants.TOPIC_LEN
                         + " " + "KICKLEN=" + Constants.KICKLEN
                         + " " + "MAXLIST=" + "beI" + ":" + Constants.MAXLIST
                         + " " + "CHANNELLEN=" + Constants.CHANNELLEN
                         + " " + "CHANMODES=" + Constants.CHANMODES
                         + " " + "EXCEPTS=" + Constants.EXCEPTS
                         + " " + "INVEX=" + Constants.INVEX
                         + " " + "CASEMAPPING=" + Constants.CASEMAPPING
                         + " " + ":" + "are supported by this server";
        assertTrue("RPL_ISUPPORT", reply.equals(":" + prefix + " " + response));

        response = "375" + " " + icp.getRequestor().getNickname() + " " + ":-" + " " + Globals.thisIrcServer.get().getHostname() + " " +"Message of the day - ";
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertTrue("RPL_MOTDSTART prefix: " + prefix + " response: " + response + "reply: " + reply , reply.equals(":" + prefix + " " + response));
        //372    RPL_MOTD ":- <text>"
        response = "372" + " " + icp.getRequestor().getNickname() + " " + ":" + "-" +".*";
        
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        while (reply.matches(":" + prefix + " " + response)) {
            assertTrue("RPL_MOTD", reply.matches(":" + prefix + " " + response));
            reply = icp.getRequestor().getOutputQueue().poll().getReport();
        }  
        //376    RPL_ENDOFMOTD ":End of MOTD command"
        response = "376" + " " + icp.getRequestor().getNickname() + " " + ":" + "End of MOTD command";
        assertTrue("RPL_MOTD", reply.equals(":" + prefix + " " + response));


        
        // 462    ERR_ALREADYREGISTRED ":Unauthorized command (already registered)"
        ircCommand = "USER";
        prefix = Globals.thisIrcServer.get().getHostname();
        icp.setParsingString(ircCommand + " " + userUsername[0] + " " + userMode[0] + " " + "*" + " " + ":" + userRealname[0]);
        icp.ircParse();
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        response = "462" + " " + userNickname[0] + " " + ":Unauthorized command (already registered)";
        assertTrue("ERR_ALREADYREGISTRED", reply.equals(":" + prefix + " " + response));
        
        dropUser();        
        icp.setRequestor(User.create());
        ((User) icp.getRequestor()).setIrcServer(Globals.thisIrcServer.get());
        icp.getRequestor().setConnection(Connection.create());
        icp.getRequestor().setHostname(Globals.thisIrcServer.get().getHostname());

        ircCommand = "NICK";
        icp.setParsingString(ircCommand + " " + userNickname[0]);
        icp.ircParse();
           
        ircCommand = "USER";
        prefix = Globals.thisIrcServer.get().getHostname();
        
        icp.setParsingString(ircCommand + " " + userUsername[0] + " " + icp.getRequestor().getHostname() + " " + "127.0.0.1" + " " + ":" + userRealname[0]);
        icp.ircParse();
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        //001    RPL_WELCOME "Welcome to the Internet Relay Network <nick>!<user>@<host>" 
        response = "001" + " " + userNickname[0] + " " + ":" + "Welcome to the Internet Relay Network " + ".*";
        assertTrue("RPL_WELCOME", reply.matches(":" + prefix + " " + response));
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        //002    RPL_YOURHOST "Your host is <servername>, running version <ver>" 
        response = "002" + " " + userNickname[0] + " " + ":" + "Your host is " + ".*";
        assertTrue("RPL_YOURHOST", reply.matches(":" + prefix + " " + response));
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        //003    RPL_CREATED "This server was created <date>" 
        response = "003" + " " + userNickname[0] + " " + ":" + "This server was created " + ".*";
        assertTrue("RPL_CREATED", reply.matches(":" + prefix + " " + response));
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        //004    RPL_MYINFO "<servername> <version> <available user modes> <available channel modes>" 
        response = "004" + " " + userNickname[0] + " " + Globals.thisIrcServer.get().getHostname()
                         + " " + Constants.SERVER_VERSION 
                         + " " + Constants.USER_MODES
                         + " " + Constants.CHANNEL_MODES;
        assertTrue("RPL_MYINFO", reply.matches(":" + prefix + " " + response));
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        //005   RPL_ISUPPORT
        response = "005" + " " + userNickname[0] 
                         + " " + "PREFIX=" + Constants.PREFIX
                         + " " + "CHANTYPES=" + Constants.CHANTYPES
                         + " " + "MODES=" + Constants.MODES
                         + " " + "CHANLIMIT=" + Constants.CHANTYPES + ":" + Constants.CHANLIMIT
                         + " " + "NICKLEN=" + Constants.NICKLEN
                         + " " + "TOPIC_LEN=" + Constants.TOPIC_LEN
                         + " " + "KICKLEN=" + Constants.KICKLEN
                         + " " + "MAXLIST=" + "beI" + ":" + Constants.MAXLIST
                         + " " + "CHANNELLEN=" + Constants.CHANNELLEN
                         + " " + "CHANMODES=" + Constants.CHANMODES
                         + " " + "EXCEPTS=" + Constants.EXCEPTS
                         + " " + "INVEX=" + Constants.INVEX
                         + " " + "CASEMAPPING=" + Constants.CASEMAPPING
                         + " " + ":" + "are supported by this server";
        assertTrue("RPL_ISUPPORT", reply.equals(":" + prefix + " " + response));

        response = "375" + " " + icp.getRequestor().getNickname() + " " + ":-" + " " + Globals.thisIrcServer.get().getHostname() + " " +"Message of the day - ";
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertTrue("RPL_MOTDSTART", reply.equals(":" + prefix + " " + response));
        //372    RPL_MOTD ":- <text>"
        response = "372" + " " + icp.getRequestor().getNickname() + " " + ":" + "-" +".*";
        
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        while (reply.matches(":" + prefix + " " + response)) {
            assertTrue("RPL_MOTD", reply.matches(":" + prefix + " " + response));
            reply = icp.getRequestor().getOutputQueue().poll().getReport();
        }  
        //376    RPL_ENDOFMOTD ":End of MOTD command"
        response = "376" + " " + icp.getRequestor().getNickname() + " " + ":" + "End of MOTD command";
        assertTrue("RPL_MOTD", reply.equals(":" + prefix + " " + response));

        System.out.println("**USER***************************************OK**");
    }
}    

