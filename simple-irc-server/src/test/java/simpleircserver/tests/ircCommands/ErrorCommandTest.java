/*
 * 
 * ErrorCommandTest
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

import org.junit.Test;

import simpleircserver.ParameterInitialization;
import simpleircserver.base.Constants;
import simpleircserver.base.Globals;
import simpleircserver.parser.IrcCommandParser;
import simpleircserver.tests.IrcCommandTest;
import simpleircserver.tests.ServerTestUtils;

/**
 * ErrorCommandTest
 * @version 0.5.3.1 2015-11-06 
 * @author  Nikolay Kirdin
 */
public class ErrorCommandTest extends IrcCommandTest {
	
    @Test
    public void errorCommandTest() {
        System.out.println("--ERROR------------------------------------------");
        
        
        dropAll();
        serverInit();
        serviceInit();

        String logFilePath = ServerTestUtils.buildResourceFilePath(Constants.LOG_FILE_PATH);
        Globals.logFileHandlerFileName.set(logFilePath);
        ParameterInitialization.loggerSetup();
        
        IrcCommandParser icp = new IrcCommandParser();

        String ircCommand;
        String content;
                
        icp.setRequestor(ircServer[0]);
        
        ircCommand = "ERROR";
        content = "Error message."; 
        icp.setParsingString(ircCommand + " " + ":" + content);
        icp.ircParse();
                   
        System.out.println("**ERROR**************************************OK**");
    }   
}    

