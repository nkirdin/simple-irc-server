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

public class TestSERVLISTcommand extends TestIrcCommand {
    public void run() {
        System.out.println("--SERVLIST---------------------------------------");
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

        int i;
        
        icp.setRequestor(User.create());
        ((User) icp.getRequestor()).setIrcServer(Globals.thisIrcServer.get());
        icp.getRequestor().setConnection(Connection.create());
        
        ircCommand = "SERVLIST";
        icp.setParsingString(ircCommand);
        prefix = Globals.thisIrcServer.get().getHostname();
        response = "451" + " " + "" + " " + ":" + "You have not registered";
        icp.ircParse();
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertTrue("Not registered reply", reply.equals(":" + prefix + " " + response));
        
        icp.setRequestor(requestor[0]);

//      234    RPL_SERVLIST "<name> <server> <mask> <type> <hopcount> <info>"
//      235    RPL_SERVLISTEND "<mask> <type> :End of service listing"
        
        ircCommand = "SERVLIST";
        String serviceMask = "";
        String serviceType = "";
        icp.setParsingString(ircCommand);
        prefix = Globals.thisIrcServer.get().getHostname();
        servername = Globals.thisIrcServer.get().getHostname();
        icp.ircParse();
        LinkedHashSet<Service> serviceSet = db.getServiceSet();
        for (Service ircService : serviceSet) {
            reply = icp.getRequestor().getOutputQueue().poll().getReport();
            response = "234" + " " + icp.getRequestor().getNickname()
                    + " " + ircService.getNickname()
                    + " " + ircService.getIrcServer().getHostname()
                    + " " + serviceMask 
                    + " " + serviceType
                    + " " + ircService.getIrcServer().getHopcount()
                    + " " + ircService.getInfo();
            assertTrue("RPL_SERVLIST", reply.equals(":" + prefix + " " + response));
        }
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        response = "235" + " " + requestor[0].getNickname() + " " + serviceMask + " " + serviceType  + " " + ":" + "End of service listing";
        assertTrue("RPL_SERVLISTEND", reply.equals(":" + prefix + " " + response));
        
        serviceMask = "Nothing";
        serviceType = "*";

        icp.setParsingString(ircCommand + " " + serviceMask + " " + serviceType);
        prefix = Globals.thisIrcServer.get().getHostname();
        icp.ircParse();
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        response = "235" + " " + requestor[0].getNickname() + " " + serviceMask + " " + serviceType  + " " + ":" + "End of service listing";
        assertTrue("Nothing. RPL_SERVLISTEND", reply.equals(":" + prefix + " " + response));
        
        System.out.println("**SERVLIST***********************************OK**");
    }   
}    

