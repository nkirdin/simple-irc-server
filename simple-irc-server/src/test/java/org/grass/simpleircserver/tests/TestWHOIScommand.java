package org.grass.simpleircserver.tests;

import org.junit.*;
import static org.junit.Assert.*;
import java.io.*;
import java.util.*;
import java.util.logging.*;
import java.net.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import java.text.*;

import org.grass.simpleircserver.*;
import org.grass.simpleircserver.base.*;
import org.grass.simpleircserver.channel.*;
import org.grass.simpleircserver.config.*;
import org.grass.simpleircserver.connection.*;
import org.grass.simpleircserver.parser.*;
import org.grass.simpleircserver.parser.commands.*;
import org.grass.simpleircserver.processor.*;
import org.grass.simpleircserver.talker.*;
import org.grass.simpleircserver.talker.server.*;
import org.grass.simpleircserver.talker.service.*;
import org.grass.simpleircserver.talker.user.*;
import org.grass.simpleircserver.tools.*;

public class TestWHOIScommand extends TestIrcCommand {
    public void run() {
        System.out.println("--WHOIS------------------------------------------");
        IrcCommandParser icp = new IrcCommandParser();
        
        dropAll();
        serverInit();
        userInit(); operatorInit();
        serviceInit();
        
        String reply;
        String servername;
        String prefix;
        String ircCommand;
        String response;
        String userPassword;
        String responseCode;
        String responseMsg;
        String content;
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
        icp.setParsingString(ircCommand + " " + "user" + " " + "test");
        icp.ircParse();
        icp.getRequestor().getOutputQueue().clear();

        
        mask = requestor[0].getNickname() + "," + requestor[1].getNickname();
        ircCommand = "WHOIS" + " " + mask;
        icp.setParsingString(ircCommand);
        icp.ircParse();
        
        for (i = 0; i < 2 ; i++) {
            servername = ((User) icp.getRequestor()).getIrcServer().getHostname();
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
        servername = ((User) icp.getRequestor()).getIrcServer().getHostname();
        //402    ERR_NOSUCHSERVER "<server name> :No such server"

        response = "402" + " " + requestor[0].getNickname() + " " + target + " " +":" + "No such server";
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertTrue("ERR_NOSUCHSERVER", reply.equals(":" + prefix + " " + response));
        
        System.out.println("**WHOIS**************************************OK**");
    }   
}    

