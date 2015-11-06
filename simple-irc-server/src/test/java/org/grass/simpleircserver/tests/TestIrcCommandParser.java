/*
 * 
 * TestIrcCommandParser
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
 * TestIrcCommandParser
 * @version 0.5.3.1 2015-11-06 
 * @author  Nikolay Kirdin
 */
public class TestIrcCommandParser implements IrcParamRegex{
    @Test
    public void testRegex() { 
        new TestRegex().run();
    }
    
    @Test
    public void testPrefixUserConnection() { 
        new TestPrefixUserConnection().run();
    } 
    
    @Test
    public void testPrefixIrcServerConnection() { 
        new TestPrefixIrcServerConnection().run();
    } 

    @Test
    public void testPrefixServiceConnection() { 
        new TestPrefixServiceConnection().run();
    } 
    
    @Test
    public void testNICKcommand() { 
        new TestNICKcommand().run();
    }
    
    @Test
    public void testUSERcommand() { 
        new TestUSERcommand().run();
    }
    
    @Test
    public void testOPERcommand() { 
        new TestOPERcommand().run();
    }
    
    @Test
    public void testMODEcommand() { 
        new TestMODEcommand().run();
    }
    
    @Test
    public void testSERVICEcommand() { 
        new TestSERVICEcommand().run();
    }
    
    @Test
    public void testQUITcommand() { 
        new TestQUITcommand().run();
    }
    
    @Test
    public void testSQUITcommand() { 
        new TestSQUITcommand().run();
    }
    
    @Test
    public void testJOINcommand() { 
        new TestJOINcommand().run();
    }
    
    @Test
    public void testTOPICcommand() { 
        new TestTOPICcommand().run();
    }
    
    @Test
    public void testNAMEScommand() { 
        new TestNAMEScommand().run();
    }
    
    @Test
    public void testLISTcommand() { 
        new TestLISTcommand().run();
    }
    
//    @Test
//    public void testINVITEcommand() { 
//        new InviteCommandTest().run();
//    }
    
    @Test
    public void testKICKcommand() { 
        new TestKICKcommand().run();
    }
    
    @Test
    public void testPRIVMSGcommand() { 
        new TestPRIVMSGcommand().run();
    }
    
    @Test
    public void testNOTICEcommand() { 
        new TestNOTICEcommand().run();
    }
    
    @Test
    public void testMOTDcommand() { 
        new TestMOTDcommand().run();
    }
    
    @Test
    public void testLUSERScommand() { 
        new TestLUSERScommand().run();
    }
    
    @Test
    public void testVERSIONcommand() { 
        new TestVERSIONcommand().run();
    }
    
    @Test
    public void testSTATScommand() { 
        new TestSTATScommand().run();
    }
    
    @Test
    public void testLINKScommand() { 
        new TestLINKScommand().run();
    }
    
    @Test
    public void testTIMEcommand() { 
        new TestTIMEcommand().run();
    }
    
//    @Test
//    public void testCONNECTcommand() { 
//        new ConnectCommandTest().run();
//    }
    
    @Test
    public void testTRACEcommand() { 
        new TestTRACEcommand().run();
    }
    
//    @Test
//    public void testADMINcommand() { 
//        new AdminCommandTest().run();
//    }
    
//    @Test
//    public void testINFOcommand() { 
//        new InfoCommandTest().run();
//    }
    
    @Test
    public void testSERVLISTcommand() { 
        new TestSERVLISTcommand().run();
    }
    
    @Test
    public void testSQUERYcommand() { 
        new TestSQUERYcommand().run();
    }
    
    @Test
    public void testWHOcommand() { 
        new TestWHOcommand().run();
    }
    
    @Test
    public void testWHOIScommand() { 
        new TestWHOIScommand().run();
    }
    
    @Test
    public void testWHOWAScommand() { 
        new TestWHOWAScommand().run();
    }
    
    @Test
    public void testKILLcommand() { 
        new TestKILLcommand().run();
    }
    
    @Test
    public void testPINGcommand() { 
        new TestPINGcommand().run();
    }
    
    @Test
    public void testPONGcommand() { 
        new TestPONGcommand().run();
    }
    
//    @Test
//    public void testERRORcommand() { 
//        new ErrorCommandTest().run();
//    }
    
//    @Test
//    public void testAWAYcommand() { 
//        new AwayCommandTest().run();
//    }
    
    @Test
    public void testREHASHcommand() { 
        new TestREHASHcommand().run();
    }
    
//    @Test
//    public void testDIEcommand() { 
//        new DieCommandTest().run();
//    }
    
    @Test
    public void testRESTARTcommand() { 
        new TestRESTARTcommand().run();
    }
    
    @Test
    public void testSUMMONcommand() { 
        new TestSUMMONcommand().run();
    }
    
    @Test
    public void testUSERScommand() { 
        new TestUSERScommand().run();
    }
    
    @Test
    public void testWALLOPScommand() { 
        new TestWALLOPScommand().run();
    }
    
    @Test
    public void testUSERHOSTcommand() { 
        new TestUSERHOSTcommand().run();
    }
    
    @Test
    public void testISONcommand() { 
        new TestISONcommand().run();
    }    
}



