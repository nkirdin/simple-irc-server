/*
 * 
 * IrcConfigParserTest
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

/**
 * IrcConfigParserTest
 * @version 0.5.3.1 2015-11-06 
 * @author  Nikolay Kirdin
 */
public class IrcConfigParserTest {
	/*
	@Test
    public void ircConfigParserTest() {
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
        
        assertNotNull("IrcAdminConfig defined", db.getIrcAdminConfig());
        assertEquals("AdminName", adminName, db.getIrcAdminConfig().getName());
        assertEquals("AdminLocation", adminLocation, db.getIrcAdminConfig().getLocation());
        assertEquals("AdminEmail", adminEmail, db.getIrcAdminConfig().getEmail());
        assertEquals("AdminInfo", adminInfo, db.getIrcAdminConfig().getInfo());
        
        assertNotNull("IrcServerConfig defined", db.getIrcServerConfig());
        assertEquals("DebugLevel", debugLevel, db.getIrcServerConfig().getDebugLevel());     
        assertEquals("timezone", timeZone, db.getIrcServerConfig().getTimeZone());
        assertEquals("motdFilename", motdFilename, db.getIrcServerConfig().getMotdFilename());
        
        assertNotNull("IrcInterfaceConfig defined", db.getIrcInterfaceConfig());
        assertEquals("InetAddress", ipAddr, db.getIrcInterfaceConfig().getInetAddress().getHostAddress()); 
        assertEquals("port", serverPort, db.getIrcInterfaceConfig().getPort());        
        assertEquals("charset", charset, db.getIrcInterfaceConfig().getCharset());
        
        assertNotNull("IrcTranscriptConfig defined", Globals.ircTranscriptConfig.get());
        assertEquals("transcript", transcript, Globals.ircTranscriptConfig.get().getTranscript());
        assertEquals("Rotate", rotate, Globals.ircTranscriptConfig.get().getRotate());     
        assertEquals("Length", 5 * 1048576, Globals.ircTranscriptConfig.get().getLength());
        
        assertNotNull("IrcOperatorConfigMap defined", db.getIrcOperatorConfigMap());
        assertTrue("OperatorName", db.getIrcOperatorConfigMap().containsKey(operatorName));
        assertEquals("OperatorPass", operatorPass, db.getIrcOperatorConfigMap().get(operatorName).getPassword());
        
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
    */
}    

