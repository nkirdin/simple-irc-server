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
    
    @Test
    public void testINVITEcommand() { 
        new TestINVITEcommand().run();
    }
    
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
    
    @Test
    public void testCONNECTcommand() { 
        new TestCONNECTcommand().run();
    }
    
    @Test
    public void testTRACEcommand() { 
        new TestTRACEcommand().run();
    }
    
    @Test
    public void testADMINcommand() { 
        new TestADMINcommand().run();
    }
    
    @Test
    public void testINFOcommand() { 
        new TestINFOcommand().run();
    }
    
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
    
    @Test
    public void testERRORcommand() { 
        new TestERRORcommand().run();
    }
    
    @Test
    public void testAWAYcommand() { 
        new TestAWAYcommand().run();
    }
    
    @Test
    public void testREHASHcommand() { 
        new TestREHASHcommand().run();
    }
    
    @Test
    public void testDIEcommand() { 
        new TestDIEcommand().run();
    }
    
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



