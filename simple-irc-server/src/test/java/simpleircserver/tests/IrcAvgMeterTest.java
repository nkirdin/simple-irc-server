/*
 * 
 * IrcAvgMeterTest
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

package simpleircserver.tests;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import simpleircserver.tools.IrcAvgMeter;

/**
 * IrcAvgMeterTest
 * @version 0.5.3.1 2015-11-06 
 * @author  Nikolay Kirdin
 */
public class IrcAvgMeterTest {
	
	@Test
    public void ircAvgMeterTest() {
        int count = 10;
        System.out.println("--IrcAvgMeter------------------------------------");
        IrcAvgMeter ircAvgMeter = new IrcAvgMeter(count);
        
        for (int i = 1 ; i <= count; i++) {
            ircAvgMeter.setValue(i);
        }
        assertEquals("Average value for first " + count + " digits",
                (count + 1) / 2, ircAvgMeter.getAvgValue());
        
        ircAvgMeter = new IrcAvgMeter(2 * count + 1);
        for (int i = -count ; i <= count; i++) {
            ircAvgMeter.setValue(i);
        }

        assertEquals("Average value for simmetric zero " + 2 * count + 1 +
                " digits", 0, ircAvgMeter.getAvgValue());
        
        ircAvgMeter = new IrcAvgMeter(2 * count);
        for (int i = 1 ; i <= count; i++) {
            ircAvgMeter.setValue(i);
        }

        assertEquals("Average value for first " + count + " digits, check" + " complete ", 
        		(count  + 1) / 2, 
                ircAvgMeter.getAvgValue());
        
        ircAvgMeter.reset();
        for (int i = 1 ; i <= count; i++) {
            ircAvgMeter.setValue(i);
        }
        assertEquals("Average value for first " + count + " digits, check" + " reset ", 
        		(count + 1) / 2, 
                ircAvgMeter.getAvgValue());
        
        ircAvgMeter = new IrcAvgMeter(count);
        for (int i = 1; i <= count; i++) {
            ircAvgMeter.intervalStart(i);
            ircAvgMeter.intervalEnd(i + 1);
        }

        assertEquals("Average value for interval 1" +
                " Check intervalStart and intervalEnd.", 
                1, ircAvgMeter.getAvgValue());
        
        count = 1000;
        ircAvgMeter = new IrcAvgMeter(count);
        for (int i = 1; i <= (25 * count) / 2 + 53; i++) {
            ircAvgMeter.intervalStart(i);
            ircAvgMeter.intervalEnd(i + 1);
        }

        assertEquals("Average value for interval 1" +
                " Check intervalStart and intervalEnd.", 
                1, ircAvgMeter.getAvgValue());
        
        count = 10;
        ircAvgMeter = new IrcAvgMeter(count);
        for (int i = 0; i < count / 2; i++) {
            ircAvgMeter.setValue(i);
        }

        assertEquals("Average value for intervals" +
                " Number of values less than count.", 
                1, ircAvgMeter.getAvgInterval());
        
        count = 10;
        ircAvgMeter = new IrcAvgMeter(count);
        for (int i = 0; i < count; i++) {
            ircAvgMeter.setValue(i);
        }

        assertEquals("Average value for intervals" +
                " Number of values equals count.", 
                1, ircAvgMeter.getAvgInterval());
        
        count = 10;
        ircAvgMeter = new IrcAvgMeter(count);
        for (int i = 0; i < count + 3; i++) {
            ircAvgMeter.setValue(i);
        }

        assertEquals("Average value for intervals" +
                " Number of values greater than count.", 
                1, ircAvgMeter.getAvgInterval());
        
        System.out.println("**IrcAvgMeter********************************OK**");          
    }
}    

