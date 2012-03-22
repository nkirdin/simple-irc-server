/*
 * 
 * Constants 
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

import java.util.*; 
import java.util.logging.*; 
import java.util.concurrent.atomic.*;
import java.net.*;
import java.nio.charset.*;
 
/**
 * Интерфейс, который определяет глобальные переменные и задает их 
 * значения по умолчанию. 
 *
 * @version 0.5 2012-02-10
 * @author  Nikolay Kirdin
 */
interface Globals {
    
    /** Объект класса {@link IrcServer}, описывающий данный сервер. */
    AtomicReference<IrcServer> thisIrcServer = 
            new AtomicReference<IrcServer>();
            
    /** Краткая информация о данном сервере. */        
    AtomicReference<String> thisIrcServerInfo = 
            new AtomicReference<String>("It is a simple IRC server.");
    
    /** 
     * Объект класса {@link IrcServer}, описывающий сервер для 
     * псевдопользователя anonymous.
     */
    AtomicReference<IrcServer> anonymousIrcServer = 
            new AtomicReference<IrcServer>();
            
    /** Краткая информация о сервере для псевдопользователя anonymous. */        
    AtomicReference<String> anonymousIrcServerInfo = 
            new AtomicReference<String>("Irc server for anonymous.");
    
    /**
     * Объект класса {@link MonitorIrcChannel}, описывающий канал для 
     * диагностических сообщений сервера.
     */
    AtomicReference<MonitorIrcChannel> monitorIrcChannel = 
            new AtomicReference<MonitorIrcChannel>();
    
    /** Объект журналирующей подсистемы. */        
    AtomicReference<Logger> logger = new AtomicReference<Logger>();
    
    /** Имя файла для журналирующей подсистемы по умолчанию. */
    AtomicReference<String> logFileHandlerFileName
            = new AtomicReference<String>("IrcServerLog.xml");
    
    /** Уровень сообщений журналирующей подсистемы. */
    AtomicReference<Level> fileLogLevel = 
            new AtomicReference<Level>(Level.WARNING);
    
    /** Объект журналирующей системы, управляющий выводом в файл. */        
    AtomicReference<Handler> logFileHandler = 
            new AtomicReference<Handler>();
            
    /** TimeZone для сервера по умолчанию. */
    AtomicReference<TimeZone> timeZone = new AtomicReference<TimeZone>(
            TimeZone.getTimeZone("GMT"));
    
    /** Путь к файлу-протоколу сообщений пользователей. */
    AtomicReference<String> transcriptFileName
            = new AtomicReference<String>("IrcServerTranscript.txt");
    
    /** 
     * Количество экземпляров файлов-протоколов сообщений пользователей. 
     */
    AtomicInteger transcriptRotate = new AtomicInteger(5);
    
    /** Длина файла-протокола сообщений пользователей (байт). */
    AtomicInteger transcriptLength = new AtomicInteger(102400);
    
    /** Путь к файлу конфигурации по умолчанию. */
    AtomicReference<String> configFilename = 
            new AtomicReference<String>("IrcServerConfig.xml");
    
    /** Максимальная длина очереди протокола сообщений. */
    int maxTranscriptQueueSize = 4096;
    
    /** 
     * Период вывода на внешний носитель элементов очереди протокола 
     * сообщений (ms).
     */
    int transcriptWritePeriod = 5000;
            
    /** Путь к файлу info по умолчанию. */
    AtomicReference<String> infoFilename  = 
            new AtomicReference<String>("IrcServerInfo.txt");
            
    /** Путь к файлу motd по умолчанию. */
    AtomicReference<String> motdFilename  = 
            new AtomicReference<String>("IrcServerMotd.txt");

    /** IP-адрес интерфеса по умолчанию. */
    AtomicReference<InetAddress> serverInetAddress = 
            new AtomicReference<InetAddress>();
    
    /** Номер порта по умолчанию. */
    AtomicInteger serverPortNumber = new AtomicInteger(6667);
    
    /** Объект класса {@link ServerSocket} для данного сервера. */
    AtomicReference<ServerSocket> serverSocket = 
            new AtomicReference<ServerSocket>();
            
    /** Размер буфера приема порта. */
    AtomicInteger receiveBufferSize = new AtomicInteger(1024);
    
    /** 
     * Кодировка сообщений для соединения ({@link Connection}) по 
     * умолчанию. 
     */
    AtomicReference<Charset> listenerCharset = 
            new AtomicReference<Charset>(Charset.forName("UTF-8"));
            
    /** Минимальный средней период (ms) поступления  входящих сообщений
     * для соединения  ({@link Connection}).
     */       
    AtomicLong minAvgReadPeriod = new AtomicLong(2000);
    //AtomicInteger maxAvgReadRate = new AtomicInteger(10000);
    
    /** Минимальный период передачи сообщения IRC PING (ms). */
    AtomicLong pingSendingPeriod = new AtomicLong(300000);
    
    /** Время по умолчанию для таймаутов (ms). */
    AtomicLong sleepTO = new AtomicLong(100);
        
    /** 
     * Служебное псевдосоединение {@link NullConnection}. 
     */
    AtomicReference<Connection> nullConnection = 
            new AtomicReference<Connection>(new NullConnection());
            
    /** Служебный псевдопользователь с никнэймом anonymous.  */        
    AtomicReference<User> anonymousUser = new AtomicReference<User>();
    
    /**Репозитарий ({@link DB}).*/
    AtomicReference<DB> db = new AtomicReference<DB>();
    
    /** Время старта процесса {@link Server#run}. */
    AtomicLong serverStartTime = new AtomicLong();

    /** 
     * Переменная, управляющая остановом сервера. Останов происходит 
     * после присваивания этой переменной значения true. 
     */
    AtomicBoolean serverDown = new AtomicBoolean(false);
    
    /** 
     * Переменная, управляющая перезапуском сервера. Перезапуск 
     * происходит после присваивания этой переменной значения true. 
     */
    AtomicBoolean serverRestart = new AtomicBoolean(false);
    
    /** 
     * Переменная, управляющая повторным чтением файла конфигурации 
     * сервера. Повторное чтение происходит после присваивания этой 
     * переменной значения true. 
     */
    AtomicBoolean serverReconfigure = new AtomicBoolean(false);
    
    /** 
     * Переменная, хранящая множество, с помощью которого индицируется 
     * состояние перегруженности одного из программных процессоров. 
     */
    AtomicReference<Set<IrcServerProcessor>> ircServerProcessorSet = 
    		new AtomicReference<Set<IrcServerProcessor>>(new 
    				HashSet<IrcServerProcessor>());
    /** 
     * Объект-хранилище параметров файла-протокола клиентских сообщений. 
     */
    AtomicReference<IrcTranscriptConfig> ircTranscriptConfig = 
    		new AtomicReference<IrcTranscriptConfig>();
    
    /** Период (ms) вывода диагностических сообщений. */
    AtomicLong monitoringPeriod = 
    		new AtomicLong(Constants.MONITORING_PERIOD);
}
