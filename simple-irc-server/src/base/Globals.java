/*
 * 
 * Globals 
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
 * @version 0.5.2 2012-03-29
 * @author  Nikolay Kirdin
 */
interface Globals {
    
    /** Объект класса {@link IrcServer}, описывающий данный сервер. */
    AtomicReference<IrcServer> thisIrcServer = 
            new AtomicReference<IrcServer>();
            
    /** Краткая информация о данном сервере. */        
    AtomicReference<String> thisIrcServerInfo = 
            new AtomicReference<String>(Constants.SERVER_INFO);
    
    /** 
     * Объект класса {@link IrcServer}, описывающий сервер для 
     * псевдопользователя anonymous.
     */
    AtomicReference<IrcServer> anonymousIrcServer = 
            new AtomicReference<IrcServer>();
            
    /** Краткая информация о сервере для псевдопользователя anonymous. */        
    AtomicReference<String> anonymousIrcServerInfo = 
            new AtomicReference<String>(Constants.ANONYMOUS_SERVER_INFO);
    
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
            = new AtomicReference<String>(Constants.LOG_FILE_PATH);
    
    /** Уровень сообщений журналирующей подсистемы. */
    AtomicReference<Level> fileLogLevel = 
            new AtomicReference<Level>(Level.parse(Constants.LOG_LEVEL));
    
    /** Объект журналирующей системы, управляющий выводом в файл. */        
    AtomicReference<Handler> logFileHandler = 
            new AtomicReference<Handler>();
            
    /** TimeZone для сервера по умолчанию. */
    AtomicReference<TimeZone> timeZone = new AtomicReference<TimeZone>(
            TimeZone.getTimeZone(Constants.TIME_ZONE));
    
    /** Путь к файлу-протоколу сообщений пользователей. */
    AtomicReference<String> transcriptFileName
            = new AtomicReference<String>(Constants.TRANSCRIPT_FILE_PATH);
    
    /** 
     * Количество экземпляров файлов-протоколов сообщений пользователей. 
     */
    AtomicInteger transcriptRotate = 
            new AtomicInteger(Constants.TRANSCRIPT_ROTATE);
    
    /** Путь к файлу конфигурации по умолчанию. */
    AtomicReference<String> configFilename = 
            new AtomicReference<String>(Constants.CONFIG_FILE_PATH);
    
    /** 
     * Период вывода на внешний носитель элементов очереди протокола 
     * сообщений (ms).
     */
    AtomicInteger transcriptWritePeriod = 
            new AtomicInteger(Constants.TRANSCRIPT_WRITE_PERIOD);
    
    
    /** Максимальная длина очереди протокола сообщений. */
    int maxTranscriptQueueSize = Math.max(Constants.MAX_SERVER_CLIENTS,
            Constants.MAX_SERVER_CLIENTS / Constants.MIN_AVG_READ_PERIOD 
            * Constants.TRANSCRIPT_WRITE_PERIOD) ;    

    /** Длина файла-протокола сообщений пользователей (байт). */
    AtomicInteger transcriptLength = 
            new AtomicInteger(Constants.TRANSCRIPT_FILE_LENGTH);
                        
    /** Путь к файлу motd по умолчанию. */
    AtomicReference<String> motdFilename  = 
            new AtomicReference<String>(Constants.MOTD_FILE_PATH);

    /** IP-адрес интерфеса по умолчанию. */
    AtomicReference<InetAddress> serverInetAddress = 
            new AtomicReference<InetAddress>();
    
    /** Номер порта по умолчанию. */
    AtomicInteger serverPortNumber = 
            new AtomicInteger(Constants.SERVER_PORT_NUMBER);
    
    /** Объект класса {@link ServerSocket} для данного сервера. */
    AtomicReference<ServerSocket> serverSocket = 
            new AtomicReference<ServerSocket>();
            
    /** Размер буфера приема порта. */
    AtomicInteger receiveBufferSize = 
            new AtomicInteger(Constants.RECEIVE_BUFFER_SIZE);
    
    /** 
     * Кодировка сообщений для соединения ({@link Connection}) по 
     * умолчанию. 
     */
    AtomicReference<Charset> listenerCharset = 
            new AtomicReference<Charset>(
                    Charset.forName(Constants.LISTENER_CHARSET));
            
    /** Минимальный средней период (ms) поступления  входящих сообщений
     * для соединения  ({@link Connection}). По умолчанию равен 
     * {@link Constants#MIN_AVG_READ_PERIOD}
     */       
    AtomicLong minAvgReadPeriod = new AtomicLong(Constants.MIN_AVG_READ_PERIOD);
    
    /** Минимальный период передачи сообщения IRC PING (ms). */
    AtomicLong pingSendingPeriod = 
            new AtomicLong(Constants.PING_SENDING_PERIOD);
    
    /** Время по умолчанию для таймаутов (ms). */
    AtomicLong sleepTO = new AtomicLong(Constants.SLEEP_TO);
        
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
