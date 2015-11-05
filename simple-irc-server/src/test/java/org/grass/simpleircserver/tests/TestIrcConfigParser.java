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
import java.nio.charset.*;

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

public class TestIrcConfigParser {
    public void run() {
        System.out.println("--usingIrcConfigFile--Use-Config-File------------");
        String testConfig = "testConfig.xml";
        String adminName = "IrcAdmin";
        String adminLocation = "IrcServerLocation";
        String adminLocation2 = "IrcServerLocation2";
        String adminEmail = "ircAdmin@irc.localhost";
        String adminInfo = "Nothing";
        String tzString = "GMT+0900";
        TimeZone timeZone = TimeZone.getTimeZone(tzString);
        Level debugLevel = Level.FINEST;
        String motdFilename = "IrcServerMotd.txt";
        String chString = "ISO-8859-1";
        Charset charset = Charset.forName(chString);
        int serverPort = 2345;
        String transcript = "clientsTranscript.txt";
        int rotate = 5;
        String length = "5M";
        String ipAddr = "192.168.10.1";
        String operatorName = "username";
        String operatorPass = "test";
        String [] xmlText = {"<?xml version=\"1.0\" encoding=\"UTF-8\"?>",
                            "<!-- File Name: " + testConfig + "-->",
                            "<CONFIG>",
                            "<ADMIN name=\"" + adminName 
                                + "\" location=\"" + adminLocation 
                                + "\" location2=\"" + adminLocation2 
                                + "\" email=\"" + adminEmail 
                                + "\" info=\"" 
                                + adminInfo + "\">",
                            "</ADMIN>",
                            "<SERVER debuglevel=\"" + debugLevel 
                                + "\" timezone=\"" + tzString 
                                + "\" motdfile=\"" + motdFilename +
                            "\">",
                            "</SERVER>",
                            "<INTERFACE" + " iface=\"" + ipAddr + "\"" + 
                            " port=\"" + serverPort + "\" charset=\"" + 
                            chString + "\">",
                            "</INTERFACE>",
                            "<TRANSCRIPT transcript=\"" + transcript 
                            + "\" rotate=\"" + rotate +
                            "\" length=\"" + length +
                            "\">",
                            "</TRANSCRIPT>",
                            "<OPERATOR username=\"" + operatorName 
                            + "\" password=\"" + operatorPass + "\">",
                            "</OPERATOR>",
                            "</CONFIG>"};
       
        // Check correctouos reading.

        
        ParameterInitialization.loggerSetup();
        
        ParameterInitialization.configSetup();

        DB db = Globals.db.get();   
        
        Globals.ircTranscriptConfig.set(new IrcTranscriptConfig(
        		Globals.transcriptFileName.get(),
        		Globals.transcriptRotate.get(),
        		Globals.transcriptLength.get()));        

        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(testConfig));
            for (String s : xmlText) {
                bw.write(s);
                bw.newLine();
            }
            bw.flush();
            bw.close();
        } catch (IOException e) {
            throw new Error("Error:" + e);
        }

        IrcConfigParser ircConfigParser = new IrcConfigParser(testConfig, 
                Globals.db.get(), Globals.logger.get());
        
        

        boolean result = ircConfigParser.useIrcConfigFile();

        assertFalse("Successfull config reading", result);
        
        assertTrue("IrcAdminConfig defined", db.getIrcAdminConfig() != null);
        assertTrue("AdminName", db.getIrcAdminConfig().getName().equals(adminName));
        assertTrue("AdminLocation", db.getIrcAdminConfig().getLocation().equals(adminLocation));
        assertTrue("AdminEmail", db.getIrcAdminConfig().getEmail().equals(adminEmail));
        assertTrue("AdminInfo", db.getIrcAdminConfig().getInfo().equals(adminInfo));
        
        assertTrue("IrcServerConfig defined", db.getIrcServerConfig() != null);
        assertTrue("DebugLevel", db.getIrcServerConfig().getDebugLevel() == debugLevel);     
        assertTrue("timezone", db.getIrcServerConfig().getTimeZone().equals(timeZone));
        assertTrue("motdFilename", db.getIrcServerConfig().getMotdFilename().equals(motdFilename));
        
        assertTrue("IrcInterfaceConfig defined", db.getIrcInterfaceConfig() != null);
        assertTrue("InetAddress", db.getIrcInterfaceConfig().getInetAddress().getHostAddress().equals(ipAddr)); 
        assertTrue("port", db.getIrcInterfaceConfig().getPort() == serverPort);        
        assertTrue("charset", db.getIrcInterfaceConfig().getCharset().equals(charset));
        
        assertTrue("IrcTranscriptConfig defined", Globals.ircTranscriptConfig.get() != null);
        assertTrue("transcript", Globals.ircTranscriptConfig.get().getTranscript().equals(transcript));
        assertTrue("Rotate", Globals.ircTranscriptConfig.get().getRotate() == rotate);     
        assertTrue("Length", Globals.ircTranscriptConfig.get().getLength() == 5 * 1048576);
        
        assertTrue("IrcOperatorConfigMap defined", db.getIrcOperatorConfigMap() != null);
        assertTrue("OperatorName", db.getIrcOperatorConfigMap().containsKey(operatorName));
        assertTrue("OperatorPass", db.getIrcOperatorConfigMap().get(operatorName).getPassword().equals(operatorPass));
        
        assertTrue("Check password", db.checkOperator(operatorName, operatorPass));

        System.out.println("**usingIrcConfigFile**Use*Config*File********OK**");  
        
        System.out.println("--usingIrcConfigFile--No-config-file-------------");
        testConfig = "testConfig2.xml";
        
        Globals.db.set(new DB());
        db = Globals.db.get();

        ircConfigParser = new IrcConfigParser(testConfig, 
                Globals.db.get(), Globals.logger.get());
        
        result = ircConfigParser.useIrcConfigFile();
        
        assertTrue("Config file not found.", result);
        
        System.out.println("**usingIrcConfigFile**No*config*file*********OK**");
        
    }
}    

