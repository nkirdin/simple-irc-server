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

public class TestServerBase {
	public AtomicBoolean running = new AtomicBoolean(true);
	public AtomicBoolean down = new AtomicBoolean(false);
    public AtomicBoolean closeAll = new AtomicBoolean(false);
    
    public AtomicBoolean restart = new AtomicBoolean(false);
    public AtomicLong sleepTO = new AtomicLong(100);
    

    public void setRunning(boolean running){
        this.running.set(running);
    }
    public void setDown(boolean down){
        this.down.set(down);
    }
    public void setCloseAll(boolean closeAll){
        this.closeAll.set(closeAll);
    }
    
    Socket socket;

    public class Client {
        public Socket s;
        public Connection c;
        public BufferedReader br;
        public BufferedWriter bw;
        public String nickname;
    }
    
    Client[] client = new Client[4];
    public void run() throws Exception {}
}
    

