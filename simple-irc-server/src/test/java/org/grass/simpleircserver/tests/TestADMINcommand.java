package org.grass.simpleircserver.tests;

import static org.junit.Assert.assertTrue;

import org.grass.simpleircserver.base.DB;
import org.grass.simpleircserver.base.Globals;
import org.grass.simpleircserver.config.IrcAdminConfig;
import org.grass.simpleircserver.connection.Connection;
import org.grass.simpleircserver.parser.IrcCommandParser;
import org.grass.simpleircserver.talker.user.User;

public class TestADMINcommand extends TestIrcCommand {
    public void run() {
        System.out.println("--ADMIN------------------------------------------");
        IrcCommandParser icp = new IrcCommandParser();
        
        dropAll();
        serverInit();
        userInit(); 
        operatorInit();
        serviceInit();

        DB db = Globals.db.get(); 
        String reply;
        String servername;
        String prefix;
        String ircCommand;
        String response;
        String userPassword;
        String responseCode;
        String responseMsg;
        String content;

        int i;
        
        icp.setRequestor(User.create());
        ((User) icp.getRequestor()).setIrcServer(Globals.thisIrcServer.get());
        icp.getRequestor().setConnection(Connection.create());

        ircCommand = "ADMIN";
        icp.setParsingString(ircCommand);
        prefix = Globals.thisIrcServer.get().getHostname();
        response = "451" + " " + "" + " " + ":" + "You have not registered";
        icp.ircParse();
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertTrue("Not registered reply", reply.equals(":" + prefix + " " + response));
        
        icp.setRequestor(requestor[0]);

//       256    RPL_ADMINME "<server> :Administrative info"
//       257    RPL_ADMINLOC1 ":<admin info>"
//       258    RPL_ADMINLOC2 ":<admin info>"
//       259    RPL_ADMINEMAIL ":<admin info>"

        db.setIrcAdminConfig(new IrcAdminConfig("adminName"
                            , "adminLocation"
                            , "adminLocation2"
                            , "adminEmail"
                            , "adminInfo"));
        
        String adminName = db.getIrcAdminConfig().getName();
        String adminLocation = db.getIrcAdminConfig().getLocation();
        String adminLocation2 = db.getIrcAdminConfig().getLocation2();
        String adminEmail = db.getIrcAdminConfig().getEmail();
        String adminInfo = db.getIrcAdminConfig().getInfo();


        ircCommand = "ADMIN";
        String serverMask ="";
        icp.setParsingString(ircCommand + " " + serverMask);
        prefix = Globals.thisIrcServer.get().getHostname();
        servername = Globals.thisIrcServer.get().getHostname();
        icp.ircParse();
        response = "256" + " " + requestor[0].getNickname() + " " + servername + " " + ":" + "Administrative info";
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertTrue("RPL_ADMINME", reply.equals(":" + prefix + " " + response));

        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        response = "257" + " " + requestor[0].getNickname() + " "+ ":" + adminLocation;
        assertTrue("RPL_ADMINLOC1", reply.equals(":" + prefix + " " + response));
        
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        response = "258" + " " + requestor[0].getNickname() + " " + ":" + adminLocation2;
        assertTrue("RPL_ADMINLOC2", reply.equals(":" + prefix + " " + response));
        
        
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        response = "259" + " " + requestor[0].getNickname() + " " + ":" + adminEmail + ", " + adminName + ", " + adminInfo;
        assertTrue("RPL_ADMINEMAIL", reply.equals(":" + prefix + " " + response));
        
        ircCommand = "ADMIN";
        serverMask = "*.irc.example.com";
        icp.setParsingString(ircCommand + " " + serverMask);
        prefix = Globals.thisIrcServer.get().getHostname();
        //402    ERR_NOSUCHSERVER "<server name> :No such server"
        icp.ircParse();
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        response = "402" + " " + requestor[0].getNickname() + " " + serverMask + " " + ":" + "No such server";
        assertTrue("ERR_NOSUCHSERVER", reply.equals(":" + prefix + " " + response));
        
        System.out.println("**ADMIN**************************************OK**");
    }   
}    

