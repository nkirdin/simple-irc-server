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

public class TestJOINcommand extends TestIrcCommand {
    public void run() {
        System.out.println("--JOIN/PART--------------------------------------");
        String reply;
        String prefix;
        String ircCommand;
        String [] errouneous;
        String [] correctous;
        String response;
        String content = "";
        int i;
        int channelCount = 0;

        long maxMemory = Runtime.getRuntime().maxMemory();
        IrcCommandParser icp = new IrcCommandParser();

        dropAll();
        serverInit();
        userInit(); 
        operatorInit();
        serviceInit();

        icp.setRequestor(User.create());
        ((User) icp.getRequestor()).setIrcServer(Globals.thisIrcServer.get());
        icp.getRequestor().setConnection(Connection.create());
        
        // Запрет доступа незарегистрированному клиенту. 
        ircCommand = "JOIN";
        icp.setParsingString(ircCommand + " " + channelName[0]);
        prefix = Globals.thisIrcServer.get().getHostname();
        response = "451" + " " + icp.getRequestor().getNickname() + " " + ":" + "You have not registered";
        icp.ircParse();
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertTrue(reply.equals(":" + prefix + " " + response));
        
        icp.setRequestor(db.getUser(userNickname[0]));
        
        //Нет имени канала.
        ircCommand = "JOIN";
        icp.setParsingString(ircCommand);
        prefix = Globals.thisIrcServer.get().getHostname();
        response = "461" + " " + userNickname[0] + " " + ircCommand + " " + ":" + "Not enough parameters";
        icp.ircParse();
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertTrue(reply.equals(":" + prefix + " " + response));
        icp.getRequestor().getOutputQueue().clear(); 
        
        //Одно имя канала.        
        ircCommand = "JOIN";
        icp.setParsingString(ircCommand + " " + channelName[0]);
        icp.ircParse();
        prefix = userNickname[0];
        response = ircCommand + " " + channelName[0];
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertTrue(reply.equals(":" + prefix + " " + response));
        
        response = "331" + " " + userNickname[0] + " " + channelName[0] + " " + ":" + "No topic is set";
        prefix = Globals.thisIrcServer.get().getHostname();
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertTrue(reply.equals(":" + prefix + " " + response));
        
        ircCommand = "NAMES";
        prefix = Globals.thisIrcServer.get().getHostname();
        String responseCode = "353";
        response = responseCode + " " + userNickname[0] + " " + "=" + channelName[0] + " " + ":" + "@" + userNickname[0];
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertTrue("One channel check", reply.equals(":" + prefix + " " + response));
        responseCode = "366";
        String responseMsg = "End of NAMES list";
        response = responseCode + " " + userNickname[0] + " " + channelName[0] + " " + ":" + responseMsg;
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertTrue("One channel check. End of NAMES", reply.equals(":" + prefix + " " + response));
        
        ircCommand = "JOIN";
        icp.setParsingString(ircCommand + " " + channelName[0].toUpperCase(Locale.ENGLISH));
        icp.ircParse();
        assertTrue("Attemp to join with the same channelname in upper case. No reply."
            , icp.getRequestor().getOutputQueue() == null || icp.getRequestor().getOutputQueue().isEmpty());
        
        
        //Список имен каналов.
        ircCommand = "JOIN";
        
        content = ircCommand + " ";
        for (int j = 1; j < channelName.length; j++) content = content + channelName[j] + ",";
        if (content.endsWith(",")) content = content.substring(0, content.length() - 1);
        icp.setParsingString(content);
        icp.ircParse();
        ////System.out.println(icp.getRequestor().getOutputQueue());
        prefix = userNickname[0];
        assertTrue(icp.getRequestor().getOutputQueue().size() == (channelName.length - 1) * 4);
        i = 1;
        while (!icp.getRequestor().getOutputQueue().isEmpty()) {
            response = ircCommand + " " + channelName[i++];
            reply = icp.getRequestor().getOutputQueue().poll().getReport();
            assertTrue(reply.equals(":" + prefix + " " + response));
            reply = icp.getRequestor().getOutputQueue().poll().getReport();
            reply = icp.getRequestor().getOutputQueue().poll().getReport();
            reply = icp.getRequestor().getOutputQueue().poll().getReport();
        }
        
        //Проверка доступа к запаролированным каналам.
        ircCommand = "MODE";
        for (int j = 0; j < channelName.length; j++) {
            icp.setParsingString(ircCommand + " " + channelName[j] + " " + "+k" + " " + channelKey[j]);
            icp.ircParse();
            ////System.out.println(icp.getRequestor().getOutputQueue());
        }
        icp.getRequestor().getOutputQueue().clear();
        
        icp.setRequestor(db.getUser(userNickname[1]));

        //Отказ в доступе, если пароль не указан.
        ircCommand = "JOIN";
        content = ircCommand + " ";
        for (int j = 0; j < channelName.length; j++) content = content + channelName[j] + ",";
        if (content.endsWith(",")) content = content.substring(0, content.length() - 1);
        icp.setParsingString(content);
        icp.ircParse();
        //System.out.println(icp.getRequestor().getOutputQueue());
        //475    ERR_BADCHANNELKEY "<channel> :Cannot join channel (+k)"
        prefix = Globals.thisIrcServer.get().getHostname();
        assertTrue(icp.getRequestor().getOutputQueue().size() == channelName.length);
        i = 0;
        while (!icp.getRequestor().getOutputQueue().isEmpty()) {
            response = "475" + " " + userNickname[1] + " " + channelName[i++] + " " + ":Cannot join channel (+k)";
            reply = icp.getRequestor().getOutputQueue().poll().getReport();
            assertTrue(reply.equals(":" + prefix + " " + response));
        }
        
        //Предоставление доступа при указании пароля.
        icp.setRequestor(db.getUser(userNickname[1]));
        ircCommand = "JOIN";
        content = ircCommand + " ";
        for (int j = 0; j < channelName.length; j++) content = content + channelName[j] + ",";
        if (content.endsWith(",")) content = content.substring(0, content.length() - 1);
        content = content + " ";
        for (int j = 0; j < channelName.length; j++) content = content + channelKey[j] + ",";
        if (content.endsWith(",")) content = content.substring(0, content.length() - 1);
        icp.setParsingString(content);
        icp.ircParse();
        prefix = userNickname[1];
         
        icp.setRequestor(db.getUser(userNickname[0]));
        assertTrue(icp.getRequestor().getOutputQueue().size() == channelName.length);
        i = 0;
        while (!icp.getRequestor().getOutputQueue().isEmpty()) {
            response = ircCommand + " " + channelName[i++];
            reply = icp.getRequestor().getOutputQueue().poll().getReport();
            assertTrue(reply.equals(":" + prefix + " " + response));
        }
       
        icp.setRequestor(db.getUser(userNickname[1]));
        assertTrue(icp.getRequestor().getOutputQueue().size() == channelName.length * 5);
        i = 0;
        while (!icp.getRequestor().getOutputQueue().isEmpty()) {
            response = ircCommand + " " + channelName[i++];
            reply = icp.getRequestor().getOutputQueue().poll().getReport();
            assertTrue(reply.equals(":" + prefix + " " + response));
            reply = icp.getRequestor().getOutputQueue().poll().getReport();
            reply = icp.getRequestor().getOutputQueue().poll().getReport();
            reply = icp.getRequestor().getOutputQueue().poll().getReport();
            reply = icp.getRequestor().getOutputQueue().poll().getReport();

        }
        //Проверка JOIN 0
        ircCommand = "JOIN";
        Set<IrcChannel> channelSet = ((User) icp.getRequestor()).getChannelSet();
        icp.setParsingString(ircCommand + " " + "0");
        icp.ircParse();
        //System.out.println(icp.getRequestor().getOutputQueue());
        prefix = userNickname[1];
        i = 0;
        assertTrue(icp.getRequestor().getOutputQueue().size() == channelName.length);
        
        for (IrcChannel ch : channelSet) {
            response = "PART" + " " + ch.getNickname();
            //System.out.println(":" + prefix + " " + response);
            reply = icp.getRequestor().getOutputQueue().poll().getReport();
            assertTrue(reply.equals(":" + prefix + " " + response));
        }
        
        icp.setRequestor(db.getUser(userNickname[0]));
        i = 0;
        assertTrue(icp.getRequestor().getOutputQueue().size() == channelName.length);
        for (IrcChannel ch : channelSet) {
            response = "PART" + " " + ch.getNickname();
            //System.out.println(":" + prefix + " " + response);
            reply = icp.getRequestor().getOutputQueue().poll().getReport();
            assertTrue(reply.equals(":" + prefix + " " + response));
        }
        
        icp.setRequestor(db.getUser(userNickname[0]));
        ircCommand = "JOIN";
        icp.setParsingString(ircCommand + " " + "0");
        icp.ircParse();
        //System.out.println(icp.getRequestor().getOutputQueue());
        icp.getRequestor().getOutputQueue().clear();
        
        icp.setRequestor(db.getUser(userNickname[0]));
        ircCommand = "JOIN";
        icp.setParsingString(ircCommand + " " + channelName[0]);
        icp.ircParse();
        //System.out.println(icp.getRequestor().getOutputQueue());
        icp.getRequestor().getOutputQueue().clear();
        
        //Проверка запрета доступа забаненому пользователю.
        // 474    ERR_BANNEDFROMCHAN "<channel> :Cannot join channel (+b)"
        icp.setRequestor(db.getUser(userNickname[0]));
        ircCommand = "MODE";
        icp.setParsingString(ircCommand + " " + channelName[0] + " " + "+b" + " " + "*!*@*");
        icp.ircParse();
        //System.out.println(icp.getRequestor().getOutputQueue());
        icp.getRequestor().getOutputQueue().clear();
        
        icp.setRequestor(db.getUser(userNickname[1]));
        icp.getRequestor().getOutputQueue().clear();
        ircCommand = "JOIN";
        icp.setParsingString(ircCommand + " " + channelName[0]);
        icp.ircParse();
        //System.out.println(icp.getRequestor().getOutputQueue());
        prefix = Globals.thisIrcServer.get().getHostname();
        response = "474" + " " + userNickname[1] + " " + channelName[0] + " " + ":Cannot join channel (+b)";
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertTrue(reply.equals(":" + prefix + " " + response));
        
        //Проверка исключения для доступа забаненому домену.
        icp.setRequestor(db.getUser(userNickname[0]));
        icp.getRequestor().getOutputQueue().clear();
        ircCommand = "MODE";
        icp.setParsingString(ircCommand + " " + channelName[0] + " " + "+e" + " " + userNickname[1] + "!*@*");
        icp.ircParse();
        //System.out.println(icp.getRequestor().getOutputQueue());
        icp.setRequestor(db.getUser(userNickname[1]));
        icp.getRequestor().getOutputQueue().clear();
        ircCommand = "JOIN";
        icp.setParsingString(ircCommand + " " + channelName[0]);
        icp.ircParse();
        //System.out.println(icp.getRequestor().getOutputQueue());
        prefix = userNickname[1];
        response = ircCommand + " " + channelName[0];
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertTrue(reply.equals(":" + prefix + " " + response));

        ircCommand = "PART";
        icp.setParsingString(ircCommand + " " + channelName[0]);
        icp.ircParse();
        //System.out.println(icp.getRequestor().getOutputQueue());
        //Отмена исключения для доступа забаненому домену.
        icp.setRequestor(db.getUser(userNickname[0]));
        icp.getRequestor().getOutputQueue().clear();
        ircCommand = "MODE";
        icp.setParsingString(ircCommand + " " + channelName[0] + " " + "-e" + " " + userNickname[1] + "!*@*");
        icp.ircParse();
        //System.out.println(icp.getRequestor().getOutputQueue());
        //Бан для домена снят. Проверка
        icp.setRequestor(db.getUser(userNickname[0]));
        icp.getRequestor().getOutputQueue().clear();
        ircCommand = "MODE";
        prefix = Globals.thisIrcServer.get().getHostname();
        icp.setParsingString(ircCommand + " " + channelName[0] + " " + "-b" + " " + "*!*@*");
        icp.ircParse();
        //System.out.println(icp.getRequestor().getOutputQueue());
        icp.setRequestor(db.getUser(userNickname[1]));
        icp.getRequestor().getOutputQueue().clear();
        ircCommand = "JOIN";
        icp.setParsingString(ircCommand + " " + channelName[0]);
        prefix = userNickname[1];
        icp.ircParse();
        //System.out.println(icp.getRequestor().getOutputQueue());
        response = ircCommand + " " + channelName[0];
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertTrue(reply.equals(":" + prefix + " " + response));
        
        ircCommand = "PART";
        icp.setParsingString(ircCommand + " " + channelName[0]);
        icp.ircParse();
        //System.out.println(icp.getRequestor().getOutputQueue());
        // Проверка запрета доступа к каналу inviteonly.
        // 473    ERR_INVITEONLYCHAN "<channel> :Cannot join channel (+i)"
        icp.setRequestor(db.getUser(userNickname[0]));
        icp.getRequestor().getOutputQueue().clear();
        ircCommand = "MODE";
        icp.setParsingString(ircCommand + " " + channelName[0] + " " + "+i");
        icp.ircParse();
        //System.out.println(icp.getRequestor().getOutputQueue());
        icp.setRequestor(db.getUser(userNickname[1]));
        icp.getRequestor().getOutputQueue().clear();
        ircCommand = "JOIN";
        icp.setParsingString(ircCommand + " " + channelName[0]);
        icp.ircParse();
        //System.out.println(icp.getRequestor().getOutputQueue());
        prefix = Globals.thisIrcServer.get().getHostname();
        response = "473" + " " + userNickname[1] + " " + channelName[0] + " " + ":Cannot join channel (+i)";
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertTrue(reply.equals(":" + prefix + " " + response));
        
        //Запрет снят. Проверка
        icp.setRequestor(db.getUser(userNickname[0]));
        icp.getRequestor().getOutputQueue().clear();
        ircCommand = "MODE";
        prefix = Globals.thisIrcServer.get().getHostname();
        icp.setParsingString(ircCommand + " " + channelName[0] + " " + "-i");
        icp.ircParse();
        //System.out.println(icp.getRequestor().getOutputQueue());
        icp.setRequestor(db.getUser(userNickname[1]));
        icp.getRequestor().getOutputQueue().clear();
        ircCommand = "JOIN";
        icp.setParsingString(ircCommand + " " + channelName[0]);
        prefix = userNickname[1];
        icp.ircParse();
        //System.out.println(icp.getRequestor().getOutputQueue());
        response = ircCommand + " " + channelName[0];
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertTrue(reply.equals(":" + prefix + " " + response));
        
        ircCommand = "PART";
        icp.setParsingString(ircCommand + " " + channelName[0]);
        icp.ircParse();
        //System.out.println(icp.getRequestor().getOutputQueue());
        // Проверка запрета доступа к каналу c ограниченным количеством пользователей.
        icp.setRequestor(db.getUser(userNickname[0]));
        icp.getRequestor().getOutputQueue().clear();
        ircCommand = "MODE";
        icp.setParsingString(ircCommand + " " + channelName[0] + " " + "+l" + " " + "20");
        icp.ircParse();
        //System.out.println(icp.getRequestor().getOutputQueue());
        String temporaryNick = "tmp";
        
        for (i = 2; i < 21; i++ ) {
            icp.setRequestor(User.create());
        ((User) icp.getRequestor()).setIrcServer(Globals.thisIrcServer.get());

            //Регистрация
            ircCommand = "NICK";
            icp.setParsingString(ircCommand + " " + temporaryNick + i);
            icp.ircParse();
        
            ircCommand = "USER";
            icp.setParsingString(ircCommand + " " + userUsername[0] + " " + userMode[0] + " " + "*" + " " + ":" + userRealname[0]);
            icp.ircParse();
        
            ircCommand = "JOIN";
            icp.setParsingString(ircCommand + " " + channelName[0]);
            icp.ircParse();
        }   
        
        // 471    ERR_CHANNELISFULL "<channel> :Cannot join channel (+l)"
        icp.setRequestor(db.getUser(userNickname[1]));
        icp.getRequestor().getOutputQueue().clear();
        ircCommand = "JOIN";
        icp.setParsingString(ircCommand + " " + channelName[0]);
        icp.ircParse();
        //System.out.println(icp.getRequestor().getOutputQueue());
        prefix = Globals.thisIrcServer.get().getHostname();
        response = "471" + " " + userNickname[1] + " " + channelName[0] + " " + ":Cannot join channel (+l)";
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertTrue(reply.equals(":" + prefix + " " + response));
        
        //Запрет снят. Проверка
        icp.setRequestor(db.getUser(userNickname[0]));
        icp.getRequestor().getOutputQueue().clear();
        ircCommand = "MODE";
        prefix = Globals.thisIrcServer.get().getHostname();
        icp.setParsingString(ircCommand + " " + channelName[0] + " " + "+l" + " " + "100");
        icp.ircParse();
        //System.out.println(icp.getRequestor().getOutputQueue());
        icp.setRequestor(db.getUser(userNickname[1]));
        icp.getRequestor().getOutputQueue().clear();
        ircCommand = "JOIN";
        icp.setParsingString(ircCommand + " " + channelName[0]);
        prefix = userNickname[1];
        icp.ircParse();
        //System.out.println(icp.getRequestor().getOutputQueue());
        response = ircCommand + " " + channelName[0];
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertTrue(reply.equals(":" + prefix + " " + response));
        
        ircCommand = "JOIN 0";
        icp.setParsingString(ircCommand);
        icp.ircParse();  
        //System.out.println(icp.getRequestor().getOutputQueue());
        for (i = 2; i < 21; i++ ) {
            ircCommand = "QUIT";
            icp.setRequestor(db.getUser(temporaryNick + i));
            icp.setParsingString(ircCommand);
            icp.ircParse();
            //System.out.println(icp.getRequestor().getOutputQueue());
        }   

        // Проверка на ограничение подключаемых каналов.
        // 405    ERR_TOOMANYCHANNELS "<channel name> :You have joined too many channels"

        icp.setRequestor(db.getUser(userNickname[0]));
        icp.getRequestor().getOutputQueue().clear();
        int maxNumber = 3;
        ((User) icp.getRequestor()).setMaximumChannelNumber(maxNumber);
        i = 0;
        do {
            ircCommand = "JOIN";
            icp.setParsingString(ircCommand + " " + "#" + temporaryNick + i);
            response = "405" + " " + userNickname[0] + " " + "#" + temporaryNick + i + " " + ":You have joined too many channels";
            prefix = Globals.thisIrcServer.get().getHostname();
            icp.ircParse();
            //System.out.println(icp.getRequestor().getOutputQueue());
            i++;
            reply = icp.getRequestor().getOutputQueue().poll().getReport();
            icp.getRequestor().getOutputQueue().clear();
            
        } while (!reply.equals(":" + prefix + " " + response));
        assertTrue(i == maxNumber);
        
        ircCommand = "QUIT";
        icp.setParsingString(ircCommand);
        icp.ircParse();

        dropAll();
        serverInit();
        userInit(); operatorInit();
        serviceInit();
        
        icp.setRequestor(db.getUser(userNickname[0]));
        ircCommand = "PART";
        icp.setParsingString(ircCommand);
        prefix = Globals.thisIrcServer.get().getHostname();
        response = "461" + " " + userNickname[0] + " " + ircCommand + " " + ":" + "Not enough parameters";
        icp.ircParse();
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertTrue("ERR_NEEDMOREPARAMS" ,reply.equals(":" + prefix + " " + response));
        
        ircCommand = "PART";
        String chName = "channelName";
        icp.setParsingString(ircCommand + " " + chName);
        prefix = Globals.thisIrcServer.get().getHostname();
        response = "421" + " " + userNickname[0] + " " + ircCommand + " " + ":Unknown command";
        icp.ircParse();
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertTrue(reply.equals(":" + prefix + " " + response));

        ircCommand = "PART";
        chName = "#channelName";
        icp.setParsingString(ircCommand + " " + chName);
        prefix = Globals.thisIrcServer.get().getHostname();
        // 442    ERR_NOTONCHANNEL "<channel> :You're not on that channel"
        response = "442" + " " + userNickname[0] + " " + chName + " " + ":You're not on that channel";
        icp.ircParse();
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertTrue("ERR_NOTONCHANNEL", reply.equals(":" + prefix + " " + response));
        
        ircCommand = "JOIN";
        chName = "#channelName";
        icp.setParsingString(ircCommand + " " + chName);
        prefix = userNickname[0];
        icp.ircParse();
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        icp.getRequestor().getOutputQueue().clear(); 
        
        ircCommand = "PART";
        chName = "#channelName";
        icp.setParsingString(ircCommand + " " + chName);
        prefix = userNickname[0];
        response = ircCommand + " " + chName;
        icp.ircParse();
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertTrue(reply.equals(":" + prefix + " " + response));
        
        ircCommand = "JOIN 0";
        icp.setParsingString(ircCommand);
        icp.ircParse();
        icp.getRequestor().getOutputQueue().clear();
        
        ircCommand = "JOIN";
        content = ircCommand + " ";
        for (int j = 0; j < channelName.length; j++) content = content + channelName[j] + ",";
        if (content.endsWith(",")) content = content.substring(0, content.length() - 1);
        icp.setParsingString(content);
        icp.ircParse();
        icp.getRequestor().getOutputQueue().clear();
       
        //Проверка обработки списка имен каналов.
        ircCommand = "PART";
        String info = "Good bye!";
        content = ircCommand + " ";
        for (int j = 0; j < channelName.length; j++) content = content + channelName[j] + ",";
        if (content.endsWith(",")) content = content.substring(0, content.length() - 1);
        content = content + " " + ":" + info;
        icp.setParsingString(content);
        icp.ircParse();
        prefix = userNickname[0];
        assertTrue(icp.getRequestor().getOutputQueue().size() == channelName.length);
        i = 0;
        while (!icp.getRequestor().getOutputQueue().isEmpty()) {
            response = "PART" + " " + channelName[i++] + " " + ":" + info;
            reply = icp.getRequestor().getOutputQueue().poll().getReport();
            assertTrue(reply.equals(":" + prefix + " " + response));
        }

        icp.setParsingString("QUIT");
        icp.ircParse();
        
        icp.setRequestor(service[0]);
        
        ircCommand = "JOIN";
        icp.setParsingString(ircCommand + " " + channelName[0]);
        prefix = Globals.thisIrcServer.get().getHostname();
        response = "421" + " " + service[0].getNickname() + " " + ircCommand + " " + ":Unknown command";
        icp.ircParse();
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertTrue(reply.equals(":" + prefix + " " + response));
        
        ircCommand = "PART";
        icp.setParsingString(ircCommand + " " + channelName[0]);
        prefix = Globals.thisIrcServer.get().getHostname();
        response = "421" + " " + service[0].getNickname() + " " + ircCommand + " " + ":Unknown command";
        icp.ircParse();
        reply = icp.getRequestor().getOutputQueue().poll().getReport();
        assertTrue(reply.equals(":" + prefix + " " + response));

        System.out.println("**JOIN/PART**********************************OK**");
    }   
}    

