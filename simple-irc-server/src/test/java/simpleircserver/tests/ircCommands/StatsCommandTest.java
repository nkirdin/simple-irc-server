/*
 * 
 * StatsCommandTest
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

import java.util.LinkedHashMap;

import org.junit.Test;

import simpleircserver.base.Globals;
import simpleircserver.connection.Connection;
import simpleircserver.parser.IrcCommandParser;
import simpleircserver.talker.IrcTalker;
import simpleircserver.talker.user.User;
import simpleircserver.tests.IrcCommandTest;
import simpleircserver.tools.IrcAvgMeter;

/**
 * StatsCommandTest
 * @version 0.5.3.1 2015-11-06 
 * @author  Nikolay Kirdin
 */
public class StatsCommandTest extends IrcCommandTest {
	
    @Test
    public void statsCommandTest() {
        System.out.println("--STATS------------------------------------------");
        IrcCommandParser icp = new IrcCommandParser();
        
        dropAll();
        serverInit();
        serviceInit();

        IrcTalker[] requestor = new IrcTalker[3];
        String[] userNickname = {"nick1", "nick2", "nick3"};
        String[] userUsername = {"user1", "user2", "user3"};
        String[] userRealname = {"Real Name 1", "Real Name 2", "Real Name 3"};
        String[] userMode = {"0", "0", "0"};

        String reply;
        String prefix;
        String ircCommand;
        String response;
        
        icp.setRequestor(User.create());
        ((User) icp.getRequestor()).setIrcServer(Globals.thisIrcServer.get());
        icp.getRequestor().setConnection(Connection.create());

        ircCommand = "STATS";
        icp.setParsingString(ircCommand);
        prefix = Globals.thisIrcServer.get().getHostname();
        response = "451" + " " + "" + " " + ":" + "You have not registered";
        icp.ircParse();
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertTrue("Not registered reply", reply.equals(":" + prefix + " " + response));
        
        ircCommand = "NICK";
        icp.setParsingString(ircCommand + " " + userNickname[0]);
        icp.ircParse();
        requestor[0] = icp.getRequestor();
        
        ircCommand = "USER";
        icp.setParsingString(ircCommand + " " + userUsername[0] + " " + userMode[0] + " " + "*" + " " + ":" + userRealname[0]);
        icp.ircParse();
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        icp.getRequestor().getOutputQueue().clear();
        
        ircCommand = "STATS";
        String queryString = "l";
        icp.setParsingString(ircCommand + " " + queryString);
        prefix = Globals.thisIrcServer.get().getHostname();
        icp.ircParse();
        //211    RPL_STATSLINKINFO "<linkname> <sendq> <sent messages> <sent Kbytes> <received messages> <received Kbytes> <time open>
        //219    RPL_ENDOFSTATS "<stats letter> :End of STATS report"
        response = "211" + " " + userNickname[0] + " " + String.format("%d", 0)
                + " " + String.format("%d", 0)
                + " " + String.format("%d", 0)
                + " " + String.format("%d", 0)
                + " " + String.format("%d", 0)
                + " " + String.format("%d", 0)
                + " " + String.format("%d", 0);
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        
        assertTrue("RPL_STATSLINKINFO", reply.equals(":" + prefix + " " + response));
        response = "219" + " " + userNickname[0] + " " + queryString + " " + ":" + "End of STATS report";
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        
        assertTrue("RPL_ENDOFSTATS", reply.equals(":" + prefix + " " + response));
        
        ircCommand = "STATS";
        queryString = "m";
        icp.setParsingString(ircCommand + " " + queryString);
        prefix = Globals.thisIrcServer.get().getHostname();
        icp.ircParse();
        //212    RPL_STATSCOMMANDS "<command> <count> <byte count> <remote count>"
        //219    RPL_ENDOFSTATS "<stats letter> :End of STATS report"
        LinkedHashMap<String, IrcAvgMeter> commandStats = IrcCommandParser.getCommandStats();
        for (String command : commandStats.keySet()) {
            long counter = commandStats.get(command).counter.get();
            if (command.equals("stats")) {
                counter--;
            }
            reply = icp.getRequestor().getOutputQueue().poll().getReport();
            response = "212" + " " + userNickname[0] + " " + command + " " + counter;
            assertTrue("RPL_STATSCOMMANDS", reply.matches(":" + prefix + " " + response + ".*"));
        }
        response = "219" + " " + userNickname[0] + " " + queryString + " " + ":" + "End of STATS report";
        
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        
        assertTrue("RPL_ENDOFSTATS", reply.equals(":" + prefix + " " + response));
        
        ircCommand = "STATS";
        queryString = "u";
        icp.setParsingString(ircCommand + " " + queryString);
        prefix = Globals.thisIrcServer.get().getHostname();
        icp.ircParse();
        //242    RPL_STATSUPTIME  ":Server Up %d days %d:%02d:%02d"
        //219    RPL_ENDOFSTATS "<stats letter> :End of STATS report"
        
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
            long upTime = System.currentTimeMillis() - Globals.serverStartTime.get();
            int days = (int) (upTime / (24 * 60 * 60 * 1000));
            int hours = (int) ((upTime % (24 * 60 * 60 * 1000)) / (60 * 60 * 1000));
            int minutes = (int) ((upTime % (24 * 60 * 60 * 1000) % (60 * 60 * 1000))/ (60 * 1000));
            int seconds = (int) ((upTime % (24 * 60 * 60 * 1000) % (60 * 60 * 1000) % (60 * 1000))/ 1000);
        
        response = "242" + " " + userNickname[0] + " " + ":" + 
                "Server Up" + " " + String.format("%s", String.valueOf(days)) + " " + "days"
                + " " + String.format("%s", String.valueOf(hours))
                + ":" + String.format("%2s", String.valueOf(minutes))
                + ":" + String.format("%2s", String.valueOf(seconds));
        assertTrue("RPL_STATSUPTIME", reply.matches(":" + prefix + " " + response));
        response = "219" + " " + userNickname[0] + " " + queryString + " " + ":" + "End of STATS report";
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        
        assertTrue("RPL_ENDOFSTATS", reply.equals(":" + prefix + " " + response));
        
        ircCommand = "STATS";
        queryString = "o";
        icp.setParsingString(ircCommand + " " + queryString);
        prefix = Globals.thisIrcServer.get().getHostname();
        icp.ircParse();
        //243    RPL_STATSOLINE "O <hostmask> * <name>"
        //219    RPL_ENDOFSTATS "<stats letter> :End of STATS report"
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        response = "243" + " " + userNickname[0] + " " + "O" + " " + "" + " " + "*" + " " + "";
        
        assertTrue("RPL_STATSOLINE", reply.equals(":" + prefix + " " + response));
        response = "219" + " " + userNickname[0] + " " + queryString + " " + ":" + "End of STATS report";
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        
        assertTrue("RPL_ENDOFSTATS", reply.equals(":" + prefix + " " + response));
        
        ircCommand = "STATS";
        queryString = "";
        icp.setParsingString(ircCommand + " " + queryString);
        prefix = Globals.thisIrcServer.get().getHostname();
        icp.ircParse();
        //219    RPL_ENDOFSTATS "<stats letter> :End of STATS report"
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        response = "219" + " " + userNickname[0] + " " + queryString + " " + ":" + "End of STATS report";
        assertTrue("No query only RPL_ENDOFSTATS", reply.equals(":" + prefix + " " + response));
        
        ircCommand = "STATS";
        queryString = "o";
        String serverMask = "irc.example.com";
        icp.setParsingString(ircCommand + " " + queryString + " " + serverMask);
        prefix = Globals.thisIrcServer.get().getHostname();
        //402    ERR_NOSUCHSERVER "<server name> :No such server"
        icp.ircParse();
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        response = "402" + " " + userNickname[0] + " " + serverMask + " " + ":" + "No such server";
        
        assertTrue("ERR_NOSUCHSERVER", reply.equals(":" + prefix + " " + response));
        
        ircCommand = "QUIT";
        icp.setParsingString(ircCommand);
        icp.ircParse();
        
        System.out.println("**STATS**************************************OK**");
    }   
}    

