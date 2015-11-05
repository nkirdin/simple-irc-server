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

public class TestServerStartRestartStop extends TestServerBase { 
    public void run() {
        System.out.println("--Server--Start/Restart/Stop---------------------");
        //Server start/restart/stop
        Server server = new Server();
        Thread serverThread = new Thread(server);
        serverThread.start();
        
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {}
        Globals.logger.get().log(Level.INFO, "Start");
        Globals.logger.get().log(Level.FINEST, "--Server--Start/Restart/Stop---------------------");
        assertTrue("Server started",serverThread.getState() != Thread.State.NEW && serverThread.getState() != Thread.State.TERMINATED);
        //Restart server
        long serverStartTime1 = Globals.serverStartTime.get();
        Globals.serverRestart.set(true);
        long restartTO = 5000;
        try {
            Thread.sleep(restartTO);
        } catch (InterruptedException e) {}
        assertTrue("Server running",serverThread.getState() != Thread.State.NEW && serverThread.getState() != Thread.State.TERMINATED);
        assertTrue("Server restarted", Globals.serverStartTime.get() > serverStartTime1);
        Globals.serverDown.set(true);
        Globals.logger.get().log(Level.INFO, "Down");
        Globals.logger.get().log(Level.FINEST, "**Server**Start/Restart/Stop*****************OK**");
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {}
        assertTrue("Server terminated", serverThread.getState() == Thread.State.TERMINATED);
        System.out.println("**Server**Start/Restart/Stop*****************OK**");
        
    }
}

