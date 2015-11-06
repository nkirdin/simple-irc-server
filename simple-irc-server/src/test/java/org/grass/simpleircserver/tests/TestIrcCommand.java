/*
 * 
 * TestIrcCommand
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
 * TestIrcCommand
 * @version 0.5.3.1 2015-11-06 
 * @author  Nikolay Kirdin
 */
public class TestIrcCommand implements IrcParamRegex {
    static DB db;
    static User[] requestor = new User[4];
    static String[] userNickname = {"nick0","nick1", "nick2", "nick3"};
    static String[] userUsername = {"user0", "user1", "user2", "user3"};
    static String[] userRealname = {"User0 Userson0", "User1 Userson1", "User2 Userson2", "User3 Userson3"};
    static String[] userHost = {"host0.test.local", "host1.test.local", "host2.test.local", "host3.test.local"};
    static String[] userMode = {"0", "0", "0", "0", "0", "0"};
    static String[] channelName = {"#channel", "#channel2", "#channel3", "#channel4"};
    static String[] channelKey = {"key", "key2", "key3", "key4"};
    static String[] channelMode = {"", "+n", "+m", "+b nick2", "+be nick? nick3"};
    static IrcServer[] ircServer = new IrcServer[3];
    static String[] ircServerHostname = {"irc1.test.local", "irc2.test.local", "irc3.test.local"};
    static String[] ircServerInfo = {"info1", "info2", "info3"};

    static Service[] service = new Service[3];
    static String[] serviceNickname = {"service0", "service1", "service2"}; 
    static String[] distribution = {"*.test.local", "*.*.local", "distribution22.test.local"}; 
    static String[] ircServiceInfo = {"info1", "info2", "info3"};
    
    
    static InetAddress thisInetAddress;
    static InetAddress localhostInetAddress;
    static ServerSocket thisIrcServerSocket;
    static int serverSocket = 1234;
    static String thisServerFqdn;
    static String info;
    static User anonympusUser;
    
    static LinkedHashMap<String, IrcOperatorConfig> ircOperatorConfigMap = new LinkedHashMap<String, IrcOperatorConfig>();


    
    static {
        db = new DB();
        Globals.db.set(db);
        
        Globals.logger.set(Logger.getLogger("Server"));
        
        try {
            Globals.logFileHandler.set(new FileHandler(Globals.logFileHandlerFileName.get()));
        } catch (IOException e) {
            System.err.println("Server.init. Internal error: Cannot obtain logger filehandler:" + e);
            throw new Error("Server.init. Internal error: Cannot obtain logger filehandler:" + e); 
        }
        
		Globals.logger.get().addHandler(Globals.logFileHandler.get());
		Globals.logFileHandler.get().setLevel(Globals.fileLogLevel.get());
		Globals.logger.get().setLevel(Globals.fileLogLevel.get());
        
        
        try {
            localhostInetAddress = InetAddress.getByAddress("irc0.test.local", new byte[] {127, 1, 0, 10});
        } catch (UnknownHostException e) {
            throw new Error("Server.init. Internal error: Cannot obtain localhost InetAddress.");   
        }
        try {
            thisInetAddress = InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            thisInetAddress = localhostInetAddress;    
        }
        try {
            thisIrcServerSocket = new ServerSocket(serverSocket);
        } catch (IOException e) {
            throw new Error("Server.init. Internal error: Cannot obtain server soket." + serverSocket);      
        }
        thisServerFqdn = thisInetAddress.getHostName();
        info = "It is a simple IRC server.";
        
        Globals.thisIrcServer.set(IrcServer.create(thisInetAddress, thisServerFqdn, info));
        Globals.anonymousIrcServer.set(IrcServer.create(localhostInetAddress, "anonymous.anonymous", "Irc server for anonymous."));
        anonympusUser = User.create();
        anonympusUser.setIrcServer(Globals.anonymousIrcServer.get());
        anonympusUser.setNickname("anonymous");
        anonympusUser.setUsername("anonymous");
        anonympusUser.setRealname("anonymous");
        anonympusUser.setHostname(Globals.anonymousIrcServer.get().getHostname());
        Globals.anonymousUser.set(anonympusUser);
                
        db.register(Globals.thisIrcServer.get());
        db.register(Globals.anonymousIrcServer.get());
        db.register(Globals.anonymousUser.get());

        db.setIrcServerConfig(new IrcServerConfig(
                Globals.timeZone.get(),
                Globals.fileLogLevel.get(),
                Globals.motdFilename.get()));  
        
        db.setIrcInterfaceConfig(new IrcInterfaceConfig(
                Globals.serverInetAddress.get(),
                Globals.serverPortNumber.get(),
                Globals.listenerCharset.get()));        

        Globals.ircTranscriptConfig.set(null);        
        
    }
    
    
    public static void serverInit() {
        
        for (int i = 0; i < ircServer.length; i++) {
            ircServer[i] = IrcServer.create();
            ircServer[i].setHostname(ircServerHostname[i]);
            try {
//                ircServer[i].setNetworkId(InetAddress.getByName(ircServerHostname[i]));
            	ircServer[i].setNetworkId(InetAddress.getByAddress(ircServerHostname[i], new byte[] {127, 1, 0, (byte)(20 + i)}));
            } catch (UnknownHostException e) {
            	System.out.println("ircServer: " + ircServer[i] + " ircServerHostname: " + ircServerHostname[i] );
                throw new Error("UnknownHostException: " + " " + e);
            }
            ircServer[i].setInfo(ircServerInfo[i]);
            db.register(ircServer[i]);
            Connection conn = Connection.create();
            
            conn.ircTalker.set(ircServer[i]);
            ircServer[i].setConnection(conn);            
            db.register(conn);
            ircServer[i].setRegistered(true);
        }
    }
    
    public static void userInit() {    
        for (int i = 0; i < requestor.length; i++) {
            requestor[i] = User.create();
            requestor[i].setNickname(userNickname[i]);
            requestor[i].setUsername(userUsername[i]);
            requestor[i].setRealname(userRealname[i]);
            requestor[i].setHostname(userHost[i]);
            try {
//                requestor[i].setNetworkId(InetAddress.getByName(userHost[i]));
            	requestor[i].setNetworkId(InetAddress.getByAddress(userHost[i], new byte[] {127, 1, 0, (byte)(30 + i)}));
            } catch (UnknownHostException e) {
                throw new Error("UnknownHostException" + e);
            }
            requestor[i].setIrcServer(Globals.thisIrcServer.get());
            db.register(requestor[i]);
            Connection conn = Connection.create();
            
            conn.ircTalker.set(requestor[i]);
            requestor[i].setConnection(conn);
            db.register(conn);
            requestor[i].setRegistered(true);
        }
    }
    
    public static void serviceInit() {    
        for (int i = 0; i < service.length; i++) {
            service[i] = Service.create();
            service[i].setNickname(serviceNickname[i]);
            service[i].setIrcServer(Globals.thisIrcServer.get());
            service[i].setInfo(ircServiceInfo[i]);
            service[i].setDistribution(distribution[i]);
            db.register(service[i]);
            Connection conn = Connection.create();
            
            conn.ircTalker.set(service[i]);
            service[i].setConnection(conn);
            db.register(conn);
            service[i].setRegistered(true);
        }
    }
    
    
    public static void operatorInit() {
        IrcOperatorConfig ircOperatorConfig = new IrcOperatorConfig("user", "test");
        ircOperatorConfigMap.put(ircOperatorConfig.getName(), ircOperatorConfig);
        db.setIrcOperatorConfigMap(ircOperatorConfigMap);
    }
    
    
    public static void dropUser() {
        for (Iterator<User> userSetIterator = db.getUserSetIterator(); 
                userSetIterator.hasNext();) {
            User rqstr = userSetIterator.next();
            if (rqstr == Globals.anonymousUser.get()) {
                continue;
            }
            QuitIrcCommand ircCmd = QuitIrcCommand.create(db, rqstr, "Good bye!");
            ircCmd.run();
            rqstr.disconnect();
            rqstr.close();
            db.unRegister((User) rqstr);            
        }
    }
    public static void dropServer() {
        LinkedHashSet<IrcServer> serverSet = db.getIrcServerSet();
        serverSet.remove(Globals.thisIrcServer.get());
        serverSet.remove(Globals.anonymousIrcServer.get());
        for (IrcTalker rqstr: serverSet) {
            QuitIrcCommand ircCmd = QuitIrcCommand.create(db, rqstr, "Good bye!");
            ircCmd.run();
            rqstr.disconnect();
            rqstr.close();
            db.unRegister((IrcServer) rqstr);            
        }
    } 
    public static void dropService() {
        LinkedHashSet<Service> serviceSet = db.getServiceSet();
        for (IrcTalker rqstr: serviceSet) {
            QuitIrcCommand ircCmd = QuitIrcCommand.create(db, rqstr, "Good bye!");
            ircCmd.run();
            rqstr.disconnect();
            rqstr.close();
            db.unRegister((Service) rqstr);            
        }
    } 
    
    public static void dropConnection() {
        List<Connection> connectionList = db.getConnectionList();
        for (Connection conn: connectionList) {
            conn.running.set(false);
        }
        try {Thread.sleep(5);} catch (InterruptedException e) {}
        for (Connection conn: connectionList) {
            db.unRegister(conn);
        }
        
    } 
    
    public static void dropChannel() {
        for (IrcChannel ch : db.getChannelSet()) {
            System.out.println(db.unRegister(ch));    
        }
    } 
    
    public static void dropHistory() {
        db.dropHistory();
    } 
    
    public static void dropAll() {
        
        dropUser();
        dropService();
        dropServer();
        dropChannel();
        dropHistory();
        dropConnection();
    }
}    

