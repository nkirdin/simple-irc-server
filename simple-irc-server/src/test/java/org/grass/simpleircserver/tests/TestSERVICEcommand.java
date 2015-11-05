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

public class TestSERVICEcommand extends TestIrcCommand {
    public void run() {
        System.out.println("--SERVICE----------------------------------------");
        String reply;
        String servername;
        String prefix;
        String ircCommand;
        String [] errouneous;
        String [] correctous;
        String userNickname;
        String userUsername;
        String userRealname;
        String response;
        String userMode;
        String userPassword;
        
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
        servername = Globals.thisIrcServer.get().getHostname();
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

