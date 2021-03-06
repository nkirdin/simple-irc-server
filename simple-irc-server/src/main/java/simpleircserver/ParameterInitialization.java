package simpleircserver;
/*
 * 
 * ParameterInitialization 
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

import java.util.logging.*;

import simpleircserver.base.DB;
import simpleircserver.base.Globals;
import simpleircserver.channel.MonitorIrcChannel;
import simpleircserver.config.IrcInterfaceConfig;
import simpleircserver.config.IrcServerConfig;
import simpleircserver.config.IrcTranscriptConfig;
import simpleircserver.talker.server.IrcServer;
import simpleircserver.talker.user.User;

import java.util.concurrent.atomic.*;
import java.io.*;
import java.net.*;

/**
 * Класс, который служит для задания начальных значений полям и 
 * переменным. 
 *
 * @version 0.5 2012-02-06
 * @version 0.5.3 2015-11-05 Program units were moved from default package into packages with names. Unit tests were added.
 * @author  Nikolay Kirdin
 *
 */
public class ParameterInitialization {    
    
    /** Конструктор по умолчанию. */
    public ParameterInitialization() {}
    
    /**
     * Метод networkComponentsSetup() - это метод, в который производится инициализация 
     * переменных.
     * 
     * <P> Инициализируются следующие переменные:
     * <UL>
     *      <LI> {@link Globals#thisIrcServer} описатель данного 
     *      сервера;</LI>
     *      <LI> {@link Globals#anonymousIrcServer} описатель 
     *      сервера для anonymous;</LI>
     *      <LI> {@link Globals#monitorIrcChannel} описатель 
     *      служебного диагностического канала;</LI>
     *      <LI> {@link Globals#ircTranscriptConfig} описатель 
     *      файла-протокола клиентских сообщений.</LI>
     *  </UL>
     *  @return true - инициализация выполнена успешно, false - инициализация не выполнена.
     */
    public static boolean networkComponentsSetup() {
        InetAddress thisInetAddress = null;
        InetAddress localhostInetAddress = null;
        
        try {
            localhostInetAddress = InetAddress.getByAddress("localhost", new byte[] {127, 0, 0, 1});
        } catch (UnknownHostException e) {
            Globals.logger.get().log(Level.SEVERE, "Cannot obtain localhost InetAddress: " + e);
            return false;
        }
        
        try {
            thisInetAddress = InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            Globals.logger.get().log(Level.INFO, "Cannot obtain localhost InetAddress: " + e);
            thisInetAddress = localhostInetAddress;    
        }
                
        
        Globals.thisIrcServer.set(IrcServer.create(thisInetAddress, 
                thisInetAddress.getHostName(), 
                Globals.thisIrcServerInfo.get()));
        
        Globals.anonymousIrcServer.set(IrcServer.create(
                localhostInetAddress, 
                "anonymous.anonymous",
                Globals.anonymousIrcServerInfo.get()));
        
        Globals.anonymousUser.set(User.create());
        Globals.anonymousUser.get().setIrcServer(Globals.anonymousIrcServer.get());
        Globals.anonymousUser.get().setNickname("anonymous");
        Globals.anonymousUser.get().setUsername("anonymous");
        Globals.anonymousUser.get().setRealname("anonymous");
        Globals.anonymousUser.get().setHostname("localhost");
        
        Globals.monitorIrcChannel.set(new MonitorIrcChannel("&MonitorIrcChannel","Connections monitor"));
        
        
        Globals.db.get().register(Globals.thisIrcServer.get());
        Globals.db.get().register(Globals.anonymousIrcServer.get());
        Globals.db.get().register(Globals.anonymousUser.get());
        Globals.db.get().register(Globals.monitorIrcChannel.get());
         
        return true;
    }
    
    /**
     * Инициализация журналирующей подсистемы. Инициализируются 
     * следующие переменные:
     * <UL>
     *      <LI> {@link Globals#logger};</LI>
     *      <LI> {@link Globals#logFileHandler};</LI>
     *  </UL> 
     * @return true - действия выполнены успешно, false - действия не выполнены. 
     */
    public static boolean loggerSetup() {

        Globals.logger.set(Logger.getLogger("IrcServer"));
        
        try {
            Globals.logFileHandler.set(new FileHandler(Globals.logFileHandlerFileName.get()));
        } catch (IOException e) {
            System.err.println("Globals.loggerSetup. Internal error: Cannot obtain Globals.logger filehandler: " + e);
            return false;
        }
        
        Globals.logger.get().addHandler(Globals.logFileHandler.get());
        Globals.logFileHandler.get().setLevel(Globals.fileLogLevel.get());
        Globals.logger.get().setLevel(Globals.fileLogLevel.get());
        Globals.logger.get().setUseParentHandlers(false);
        Globals.logger.get().log(Level.INFO, "Current logLevel:" + Globals.logger.get().getLevel());
        return true;
    }
    
    /** Установка уровня журналирования. */
    public static void loggerLevelSetup() {
        Level level = Globals.db.get().getIrcServerConfig().getDebugLevel();
        Globals.logFileHandler.get().setLevel(level);
        Globals.logger.get().setLevel(level);
        Globals.logger.get().log(Level.INFO, "Current logLevel:" + Globals.logger.get().getLevel());
    }
    
    /** Закрытие журналирующей подсистемы. */
    public static void loggerDown() {
        Globals.logger.get().log(Level.WARNING, "Ended");
        Globals.logFileHandler.get().close();
    }
    
    /** 
     * Инициализация репозитария. Выполняются следующие операции:
     * <OL>
     *         <LI> Создается новый репозитарий;</LI>
     *         <LI> Создается новый объект класса {@link IrcServerConfig} 
     *         и сохраняется в репозитарии;</LI>
     *         <LI>  Создается новый объект класса {@link IrcInterfaceConfig} 
     *         и сохраняется в репозитарии.</LI>
     * </OL>
     */
    public static void configSetup() {
        Globals.db.set(new DB());
        
        Globals.db.get().setIrcServerConfig(new IrcServerConfig(
                Globals.timeZone.get(),
                Globals.fileLogLevel.get(),
                Globals.motdFilename.get()));      
        
        Globals.db.get().setIrcInterfaceConfig(new IrcInterfaceConfig(
                Globals.serverInetAddress.get(),
                Globals.serverPortNumber.get(),
                Globals.listenerCharset.get()));        

        Globals.ircTranscriptConfig.set(new IrcTranscriptConfig(
                Globals.transcriptFileName.get(),
                Globals.transcriptRotate.get(),
                Globals.transcriptLength.get()));        
        
    }
}
