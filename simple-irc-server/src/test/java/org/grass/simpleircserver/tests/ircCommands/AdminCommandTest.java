/*
 * 
 * AdminCommandTest
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

import static org.junit.Assert.assertEquals;

import org.grass.simpleircserver.base.DB;
import org.grass.simpleircserver.base.Globals;
import org.grass.simpleircserver.config.IrcAdminConfig;
import org.grass.simpleircserver.connection.Connection;
import org.grass.simpleircserver.parser.IrcCommandParser;
import org.grass.simpleircserver.talker.user.User;
import org.grass.simpleircserver.tests.IrcCommandTest;
import org.junit.Test;

/**
 * AdminCommandTest
 * @version 0.5.3.1 2015-11-06 
 * @author  Nikolay Kirdin
 */
public class AdminCommandTest extends IrcCommandTest {
	
	@Test
	public void adminCommandTest() {
        System.out.println("--ADMIN-------------------------------------------");

        IrcCommandParser icp = new IrcCommandParser();
        
        dropAll();
        serverInit();
        userInit(); 
        operatorInit();
        serviceInit();

        DB db = Globals.db.get(); 
        String reply;
        String servername;
        String prefix;
        String ircCommand;
        String response;
        
        icp.setRequestor(User.create());
        ((User) icp.getRequestor()).setIrcServer(Globals.thisIrcServer.get());
        icp.getRequestor().setConnection(Connection.create());

        ircCommand = "ADMIN";
        icp.setParsingString(ircCommand);
        prefix = Globals.thisIrcServer.get().getHostname();
        response = "451" + " " + "" + " " + ":" + "You have not registered";
        icp.ircParse();
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertEquals("Not registered reply", reply, ":" + prefix + " " + response);
        
        icp.setRequestor(requestor[0]);

//       256    RPL_ADMINME "<server> :Administrative info"
//       257    RPL_ADMINLOC1 ":<admin info>"
//       258    RPL_ADMINLOC2 ":<admin info>"
//       259    RPL_ADMINEMAIL ":<admin info>"

        db.setIrcAdminConfig(new IrcAdminConfig("adminName"
                            , "adminLocation"
                            , "adminLocation2"
                            , "adminEmail"
                            , "adminInfo"));
        
        String adminName = db.getIrcAdminConfig().getName();
        String adminLocation = db.getIrcAdminConfig().getLocation();
        String adminLocation2 = db.getIrcAdminConfig().getLocation2();
        String adminEmail = db.getIrcAdminConfig().getEmail();
        String adminInfo = db.getIrcAdminConfig().getInfo();


        ircCommand = "ADMIN";
        String serverMask ="";
        icp.setParsingString(ircCommand + " " + serverMask);
        prefix = Globals.thisIrcServer.get().getHostname();
        servername = Globals.thisIrcServer.get().getHostname();
        icp.ircParse();
        response = "256" + " " + requestor[0].getNickname() + " " + servername + " " + ":" + "Administrative info";
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertEquals("RPL_ADMINME", reply, ":" + prefix + " " + response);

        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        response = "257" + " " + requestor[0].getNickname() + " "+ ":" + adminLocation;
        assertEquals("RPL_ADMINLOC1", reply, ":" + prefix + " " + response);
        
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        response = "258" + " " + requestor[0].getNickname() + " " + ":" + adminLocation2;
        assertEquals("RPL_ADMINLOC2", reply, ":" + prefix + " " + response);
        
        
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        response = "259" + " " + requestor[0].getNickname() + " " + ":" + adminEmail + ", " + adminName + ", " + adminInfo;
        assertEquals("RPL_ADMINEMAIL", reply, ":" + prefix + " " + response);
        
        ircCommand = "ADMIN";
        serverMask = "*.irc.example.com";
        icp.setParsingString(ircCommand + " " + serverMask);
        prefix = Globals.thisIrcServer.get().getHostname();
        //402    ERR_NOSUCHSERVER "<server name> :No such server"
        icp.ircParse();
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        response = "402" + " " + requestor[0].getNickname() + " " + serverMask + " " + ":" + "No such server";
        assertEquals("ERR_NOSUCHSERVER", reply, ":" + prefix + " " + response);
        System.out.println("**ADMIN**************************************OK**");

    }   
}    

