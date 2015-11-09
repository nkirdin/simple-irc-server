/*
 * 
 * ServiceCommandTest
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
import org.grass.simpleircserver.talker.user.User;
import org.grass.simpleircserver.tests.IrcCommandTest;
import org.junit.Test;

/**
 * ServiceCommandTest
 * @version 0.5.3.1 2015-11-06 
 * @author  Nikolay Kirdin
 */
public class ServiceCommandTest extends IrcCommandTest {
	
	@Test
    public void serviceCommandTest() {
        System.out.println("--SERVICE----------------------------------------");
        String reply;
        String prefix;
        String ircCommand;
        String response;
        
        String serviceNickname = "service1";
        String serviceDistribution = "*.ru";
        String serviceInfo = "A short information about this service.";
        
        IrcCommandParser icp = new IrcCommandParser();

        dropAll();
        serverInit();
        userInit(); operatorInit();

        icp.setRequestor(User.create());
        ((User) icp.getRequestor()).setIrcServer(Globals.thisIrcServer.get());
        icp.getRequestor().setConnection(Connection.create());
        
        ircCommand = "SERVICE";
//        421    ERR_UNKNOWNCOMMAND "<command> :Unknown command"
        prefix = Globals.thisIrcServer.get().getHostname();
        icp.setParsingString(ircCommand + " " + serviceNickname + " * "
            + "###########" + " * " + "*" + " " + ":" + serviceInfo);
        response = "421" + " " + "" + " " + ircCommand + " " + ":" + "Unknown command";
        icp.ircParse();
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertTrue("ERR_UNKNOWNCOMMAND", reply.equals(":" + prefix + " " + response));
        
        //icp.setRequestor(User.create());
        //461    ERR_NEEDMOREPARAMS "<command> :Not enough parameters"

        ircCommand = "SERVICE";
        icp.setParsingString(ircCommand + " " + serviceNickname + " * "
            + serviceDistribution + " * " + "*" + " ");
        response = "461" + " " + "" + " " + ircCommand + " " + ":" + "Not enough parameters";
        icp.ircParse();
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertTrue("ERR_NEEDMOREPARAMS", reply.equals(":" + prefix + " " + response));

        icp.setParsingString(ircCommand + " " + serviceNickname + " * ");
        response = "461" + " " + "" + " " + ircCommand + " " + ":" + "Not enough parameters";
        icp.ircParse();
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertTrue("ERR_NEEDMOREPARAMS", reply.equals(":" + prefix + " " + response));

        icp.setParsingString(ircCommand);
        response = "461" + " " + "" + " " + ircCommand + " " + ":" + "Not enough parameters";
        icp.ircParse();
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertTrue("ERR_NEEDMOREPARAMS", reply.equals(":" + prefix + " " + response));        
        
        ircCommand = "SERVICE";
        serviceNickname = "#@*&WrongServiceName!@#$!@%$";
//         432    ERR_ERRONEUSNICKNAME "<nick> :Erroneous nickname"
        icp.setParsingString(ircCommand + " " + serviceNickname + " * "
            + serviceDistribution + " * " + "*" + " " + ":" + serviceInfo);
        response = "432" + " " + "" + " " + serviceNickname + " " + ":" + "Erroneous nickname";
        icp.ircParse();
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertTrue("ERR_ERRONEUSNICKNAME", reply.equals(":" + prefix + " " + response));
        
        ircCommand = "SERVICE";
        serviceNickname = "service";
        prefix = Globals.thisIrcServer.get().getHostname();
//         383    RPL_YOURESERVICE "You are service <servicename>"        
        icp.setParsingString(ircCommand + " " + serviceNickname + " * "
            + serviceDistribution + " * " + "*" + " " + ":" + serviceInfo);
        response = "383" + " " + serviceNickname + " " + ":" + "You are service" + " " + serviceNickname;        
        icp.ircParse();
        icp.setRequestor(db.getService(serviceNickname));
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertTrue("RPL_YOURESERVICE", reply.equals(":" + prefix + " " + response));
        
        ircCommand = "SERVICE";
//         462    ERR_ALREADYREGISTRED ":Unauthorized command (already registered)"
        icp.setParsingString(ircCommand + " " + serviceNickname + " * "
            + serviceDistribution + " * " + "*" + " " + ":" + serviceInfo);
        response = "462" + " " + serviceNickname + " " + ":" + "Unauthorized command (already registered)";
        icp.ircParse();
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertTrue("ERR_ALREADYREGISTRED", reply.equals(":" + prefix + " " + response));
        
        ircServer[0].setRegistered(false);
        icp.setRequestor(ircServer[0]);
              
        serviceNickname = "abcde";
//         383    RPL_YOURESERVICE "You are service <servicename>"        
        icp.setParsingString(ircCommand + " " + serviceNickname + " * "
            + serviceDistribution + " * " + "*" + " " + ":" + serviceInfo);
        response = "383" + " " + serviceNickname + " " + ":" + "You are service" + " " + serviceNickname;
        icp.ircParse();
        icp.setRequestor(db.getService(serviceNickname));
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertTrue("RPL_YOURESERVICE (from server)", reply.equals(":" + prefix + " " + response));
        
        System.out.println("**SERVICE************************************OK**");
    }   
}    

