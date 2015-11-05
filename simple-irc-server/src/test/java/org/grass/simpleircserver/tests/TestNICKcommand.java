package org.grass.simpleircserver.tests;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import java.util.Locale;

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

public class TestNICKcommand extends TestIrcCommand {
    public void run() {
        System.out.println("--NICK-------------------------------------------");

        dropAll();
        serverInit();        
        
        String reply;
        String servername;
        String prefix;
        String ircCommand;
        String response;
        String userPassword;
        String responseCode;
        String responseMsg;
        String content;
        String mask;

        int i;
        
        IrcCommandParser icp = new IrcCommandParser();
        
        icp.setRequestor(User.create());
        ((User) icp.getRequestor()).setIrcServer(Globals.thisIrcServer.get());
        icp.getRequestor().setConnection(Connection.create());
        
        // 431    ERR_NONICKNAMEGIVEN ":No nickname given"

        ircCommand = "NICK";
        prefix = Globals.thisIrcServer.get().getHostname();
        icp.setParsingString(ircCommand);
        responseCode = "431";
        responseMsg = "No nickname given";
        response = responseCode + " " + "" + " " + ":" + responseMsg;
        icp.ircParse();
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertTrue("ERR_NONICKNAMEGIVEN", reply.equals(":" + prefix + " " + response));
                
        String nickname = ":";
        prefix = Globals.thisIrcServer.get().getHostname();
/*        responseCode = "431";
        responseMsg = "No nickname given";
*/
        responseCode = "432";
        responseMsg = "Erroneous nickname";
        response = responseCode + " " + "" + " " + "" + " " + ":" + responseMsg;
        icp.setParsingString(ircCommand + " " + nickname);
        icp.ircParse();
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertTrue("ERR_NONICKNAMEGIVEN Colon.", reply.equals(":" + prefix + " " + response));
        
        // Проверка регистрации ника.
        nickname = "A";
        prefix = nickname;
        icp.setParsingString(ircCommand + " " + nickname);
        response = ircCommand + " " + nickname;
        assertFalse("Nickname is not in DB", db.getUserNicknameSet().contains(nickname));
        icp.ircParse();
        assertTrue("Nothing", icp.getRequestor().getOutputQueue().isEmpty());
        assertTrue("Nickname is placed in class instance", nickname.equals(db.getUser(nickname).getNickname()));
        assertTrue("Nickname is placed in DB", db.getUserNicknameSet().contains(nickname));
        
        icp.setParsingString("QUIT");
        icp.ircParse();
        icp.getRequestor().getOutputQueue().clear();

        icp.setRequestor(User.create());
        ((User) icp.getRequestor()).setIrcServer(Globals.thisIrcServer.get());
        icp.getRequestor().setConnection(Connection.create());
/*
        nickname = "A";
        prefix = nickname;
        icp.setParsingString(ircCommand + " " + ":" + nickname);
        response = ircCommand + " " + nickname;
        assertFalse("Nickname is not in DB", db.getUserNicknameSet().contains(nickname));
        icp.ircParse();
        System.out.println(icp.getRequestor().getOutputQueue());
        assertTrue("Nothing", icp.getRequestor().getOutputQueue().isEmpty());
        assertTrue("Nickname is placed in class instance", nickname.equals(db.getUser(nickname).getNickname()));
        assertTrue("Nickname is placed in DB", db.getUserNicknameSet().contains(nickname));
*/        

        
        icp.setRequestor(User.create());
        ((User) icp.getRequestor()).setIrcServer(Globals.thisIrcServer.get());
        icp.getRequestor().setConnection(Connection.create());
        
        //432    ERR_ERRONEUSNICKNAME "<nick> :Erroneous nickname"
        nickname = "abcdefghijkjgjhgjgjgjgjg";
        icp.setParsingString(ircCommand + " " + nickname);
        responseCode = "432";
        responseMsg = "Erroneous nickname";
        prefix = Globals.thisIrcServer.get().getHostname();
        response = responseCode + " " + "" + " " + nickname + " " + ":" + responseMsg;
        assertFalse("Nickname is not in DB", db.getUserNicknameSet().contains(nickname));
        icp.ircParse();
        assertFalse("Nickname is not placed in class instance", nickname.equals(icp.getRequestor().getNickname()));
        assertFalse("Nickname is not placed in DB", db.getUserNicknameSet().contains(nickname));
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertTrue("ERR_ERRONEUSNICKNAME", reply.equals(":" + prefix + " " + response));

        icp.setParsingString("QUIT");
        icp.ircParse();        
        
        dropUser(); 
        
        icp.setRequestor(User.create());
        ((User) icp.getRequestor()).setIrcServer(Globals.thisIrcServer.get());
        icp.getRequestor().setConnection(Connection.create());
        
        ircCommand = "NICK";
        prefix = userNickname[0];
        response = ircCommand + " " + userNickname[0];
        icp.setParsingString(ircCommand + " " + userNickname[0]);
        icp.ircParse();
        ircCommand = "USER";
        prefix = Globals.thisIrcServer.get().getHostname();
        icp.setParsingString(ircCommand + " " + userUsername[0] + " " + userMode[0] + " " + "*" + " " + ":" + userRealname[0]);
        icp.ircParse();
        icp.getRequestor().getOutputQueue().clear();
        
        ircCommand = "NICK";
        icp.setParsingString(ircCommand + " " + userNickname[0].toUpperCase(Locale.ENGLISH));
        icp.ircParse();
        response = "433" + " " + userNickname[0] + " " + userNickname[0].toUpperCase(Locale.ENGLISH) + " " + ":" + "Nickname is already in use";
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertTrue("ERR_NICKNAMEINUSE for the same nick in upper case", reply.equals(":" + prefix + " " + response));
        
        ircCommand = "NICK";
        prefix = userNickname[0];
        response = ircCommand + " " + userNickname[1];
        icp.setParsingString(ircCommand + " " + userNickname[1]);
        icp.ircParse();
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertTrue("Change nickname", reply.equals(":" + prefix + " " + response));
        
        
        icp.setRequestor(User.create());
        ((User) icp.getRequestor()).setIrcServer(Globals.thisIrcServer.get());
        icp.getRequestor().setConnection(Connection.create());
        
        // 433    ERR_NICKNAMEINUSE "<nick> :Nickname is already in use"
        ircCommand = "NICK";
        prefix = Globals.thisIrcServer.get().getHostname();
        icp.setParsingString(ircCommand + " " + userNickname[1]);
        icp.ircParse();
        response = "433" + " " + "" + " " + userNickname[1] + " " + ":" + "Nickname is already in use";
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertTrue("ERR_NICKNAMEINUSE for new client", reply.equals(":" + prefix + " " + response));
        
        
        icp.setParsingString("QUIT");
        icp.ircParse();
        
        icp.setRequestor(User.create());
        ((User) icp.getRequestor()).setIrcServer(Globals.thisIrcServer.get());
        icp.getRequestor().setConnection(Connection.create());
        
        ircCommand = "NICK";
        prefix = userNickname[2];
        icp.setParsingString(ircCommand + " " + userNickname[2]);
        icp.ircParse();
        assertTrue("OK new user", db.getUser(userNickname[2]).getOutputQueue().isEmpty());
                
        // 433    ERR_NICKNAMEINUSE "<nick> :Nickname is already in use"
        ircCommand = "NICK";
        prefix = Globals.thisIrcServer.get().getHostname();
        icp.setParsingString(ircCommand + " " + userNickname[1]);
        icp.ircParse();
        response = "433" + " " + userNickname[2] + " " + userNickname[1] + " " + ":" + "Nickname is already in use";
        reply = db.getUser(userNickname[2]).getOutputQueue().poll().getReport();
        assertTrue("ERR_NICKNAMEINUSE for existing client", reply.equals(":" + prefix + " " + response));
        
        dropUser();
        
        icp.setRequestor(User.create());
        ((User) icp.getRequestor()).setIrcServer(Globals.thisIrcServer.get());
        icp.getRequestor().setConnection(Connection.create());
        
        ircCommand = "NICK";
        prefix = userNickname[0];
        response = ircCommand + " " + userNickname[0];
        icp.setParsingString(ircCommand + " " + userNickname[0]);
        icp.ircParse();
        
        ircCommand = "USER";
        prefix = Globals.thisIrcServer.get().getHostname();
        icp.setParsingString(ircCommand + " " + userUsername[0] + " " + userMode[0] + " " + "*" + " " + ":" + userRealname[0]);
        icp.ircParse();
        db.getUser(userNickname[0]).getOutputQueue().clear();
        
        ircCommand = "MODE";
        prefix = userNickname[0];
        response = ircCommand + " " + userNickname[0];
        icp.setParsingString(ircCommand + " " + userNickname[0] + " " + "+r");
        icp.ircParse();
        db.getUser(userNickname[0]).getOutputQueue().clear();
        
        ircCommand = "NICK";
        prefix = Globals.thisIrcServer.get().getHostname();
        icp.setParsingString(ircCommand + " " + userNickname[1]);
        //484    ERR_RESTRICTED ":Your connection is restricted!"
        response = "484" + " " + userNickname[0] +" " + ":" + "Your connection is restricted!";
        icp.ircParse();
        reply = db.getUser(userNickname[0]).getOutputQueue().poll().getReport();
        assertTrue("ERR_RESTRICTED", reply.equals(":" + prefix + " " + response));
        
        System.out.println("**NICK***************************************OK**");
    }
}    

