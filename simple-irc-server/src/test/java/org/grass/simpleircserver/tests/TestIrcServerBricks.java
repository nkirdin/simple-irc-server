package org.grass.simpleircserver.tests;

import org.junit.*;
import static org.junit.Assert.*;
import java.io.*;
import java.util.*;
import java.util.logging.*;
import java.net.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

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

public class TestIrcServerBricks {
    
    @Test
    public void testIrcAvgMeter() {
        TestIrcAvgMeter testIrcServerBricks = new TestIrcAvgMeter();
        testIrcServerBricks.run();
    }
   
    @Test
    public void testIrcConfigParser() {
        TestIrcConfigParser testIrcServerBricks = new TestIrcConfigParser();
        testIrcServerBricks.run();
    }
}



