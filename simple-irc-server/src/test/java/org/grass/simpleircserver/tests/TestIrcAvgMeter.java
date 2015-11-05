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

public class TestIrcAvgMeter {
    public void run() {
        int count = 10;
        System.out.println("--IrcAvgMeter------------------------------------");
        IrcAvgMeter ircAvgMeter = new IrcAvgMeter(count);
        
        for (int i = 1 ; i <= count; i++) {
            ircAvgMeter.setValue(i);
        }
        assertTrue("Average value for first " + count + " digits",
                (count + 1) / 2  == ircAvgMeter.getAvgValue());
        
        ircAvgMeter = new IrcAvgMeter(2 * count + 1);
        for (int i = -count ; i <= count; i++) {
            ircAvgMeter.setValue(i);
        }

        assertTrue("Average value for simmetric zero " + 2 * count + 1 +
                " digits", 0  == ircAvgMeter.getAvgValue());
        
        ircAvgMeter = new IrcAvgMeter(2 * count);
        for (int i = 1 ; i <= count; i++) {
            ircAvgMeter.setValue(i);
        }

        assertTrue("Average value for first " + count + " digits, check" + 
                " complete ", (count  + 1) / 2  == 
                ircAvgMeter.getAvgValue());
        
        ircAvgMeter.reset();
        for (int i = 1 ; i <= count; i++) {
            ircAvgMeter.setValue(i);
        }
        assertTrue("Average value for first " + count + " digits, check" + 
                " reset ", (count + 1) / 2  == 
                ircAvgMeter.getAvgValue());
        
        ircAvgMeter = new IrcAvgMeter(count);
        for (int i = 1; i <= count; i++) {
            ircAvgMeter.intervalStart(i);
            ircAvgMeter.intervalEnd(i + 1);
        }

        assertTrue("Average value for interval 1" +
                " Check intervalStart and intervalEnd.", 
                1 == ircAvgMeter.getAvgValue());
        
        count = 1000;
        ircAvgMeter = new IrcAvgMeter(count);
        for (int i = 1; i <= (25 * count) / 2 + 53; i++) {
            ircAvgMeter.intervalStart(i);
            ircAvgMeter.intervalEnd(i + 1);
        }

        assertTrue("Average value for interval 1" +
                " Check intervalStart and intervalEnd.", 
                1 == ircAvgMeter.getAvgValue());
        
        count = 10;
        ircAvgMeter = new IrcAvgMeter(count);
        for (int i = 0; i < count / 2; i++) {
            ircAvgMeter.setValue(i);
        }

        assertTrue("Average value for intervals" +
                " Number of values less than count.", 
                1 == ircAvgMeter.getAvgInterval());
        
        count = 10;
        ircAvgMeter = new IrcAvgMeter(count);
        for (int i = 0; i < count; i++) {
            ircAvgMeter.setValue(i);
        }

        assertTrue("Average value for intervals" +
                " Number of values equals count.", 
                1 == ircAvgMeter.getAvgInterval());
        
        count = 10;
        ircAvgMeter = new IrcAvgMeter(count);
        for (int i = 0; i < count + 3; i++) {
            ircAvgMeter.setValue(i);
        }

        assertTrue("Average value for intervals" +
                " Number of values greater than count.", 
                1 == ircAvgMeter.getAvgInterval());
        
        System.out.println("**IrcAvgMeter********************************OK**");          
    }
}    

