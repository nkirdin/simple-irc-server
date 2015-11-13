package simpleircserver;
/*
 * 
 * Server 
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

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import simpleircserver.base.Constants;
import simpleircserver.base.Globals;
import simpleircserver.config.IrcConfigParser;
import simpleircserver.config.ParameterInitialization;
import simpleircserver.processor.AbstractIrcServerProcessor;
import simpleircserver.processor.IncomingConnectionListener;
import simpleircserver.processor.InputQueueProcessor;
import simpleircserver.processor.InputStreamProcessor;
import simpleircserver.processor.IrcTalkerProcessor;
import simpleircserver.processor.NetworkConnectionProcessor;
import simpleircserver.processor.OutputQueueProcessor;
import simpleircserver.processor.TranscriptFileProcessor;
/**
 * Server - класс, который служит для управления запуском, остановом, и 
 * перезапуском основных компонентов сервера IRC. 
 *
 * @version 0.5.1 2012-03-27
 * @version 0.5.3 2015-11-05 Program units were moved from default package into packages with names. Unit tests were added.
 * @version 0.5.3.1	2015-11-11 Corrected conditions of changing of server start time.	
 * @version 0.5.4 2015-11-13 Moved some staff to processors. Begin to use streams and lambdas. 	
 * @author  Nikolay Kirdin
 */

public class Server implements Runnable {
    
    /** Код завершения в случае успешного завершения. */
    public static final int  SERV_OK = 0;
    
    /** Код завершения для ситуации "ошибка в командной строке". */
    public static final int SERV_WRONG_KEY = 1;
    
    /** 
     * Код завершения для ситуации "во время запуска компонентов 
     * сервера произошла ошибка, или ошибка в файле конфигурации". 
     */
    public static final int SERV_START_ERR = 2;    
    
    /** Объект, реализующий общую инициализацию. */
    private ParameterInitialization parameterInitialization;
    
    /** 
     * Объект, реализующий процесс обслуживания вывода в файл-протокол 
     * сообщений клиентов. 
     */
    private TranscriptFileProcessor transcriptFileProcessor;
    
    /** 
     * Объект, реализующий процесс проверки состояния соединений 
     * клиентов сервера. 
     */
    private NetworkConnectionProcessor networkConnectionProcessor;
    
    /** 
     * Объект, реализующий процесс вывода сообщений клиентам сервера. 
     */
    private OutputQueueProcessor outputQueueProcessor;
    
    /** 
     * Объект, реализующий процесс обработки сообщений клиентов 
     * сервера. 
     */
    private InputQueueProcessor inputQueueProcessor;
    
    /** 
     * Объект, реализующий процесс чтения поступающих сообщений клиентов 
     * сервера. 
     */
    private InputStreamProcessor inputStreamProcessor;
    
    /** 
     * Объект, реализующий процесс обработки входящих сетевых 
     * соединений.
     */
    private IncomingConnectionListener incomingConnectionListener;
    
    /** 
     * Объект, реализующий процесс проверки состояния клиентов сервера. 
     */
    private IrcTalkerProcessor ircTalkerProcessor;
    
    /** Признак ошибки. */
    private boolean error = false;
    
    /** Код завершения. */
    private int exitStatus = 0;
    
    /** Конструктор. */
    public Server() {}
        
    /**
     * Метод используется для запуска сервера из командной строки. 
     * С помощью ключей командной строки можно указать пути к файлу 
     * конфигурации и файлу журналирования. 
     *
     * <P> Параметры командной строки могут принимать следующие значения:
     * <UL>
     * <LI> {@code "-c <config file>"} - указание файла конфигурации;</LI>
     * <LI> {@code "-h"} - вывод краткой справки;</LI>
     * <LI> {@code "-l <logging file>"} - указание журнального файла;</LI>
     * <LI> {@code "-V} - вывод версии программы.</LI>
     * </UL>
     * <P>
     * Используется переменная {@link Globals#configFilename}, хранящая 
     * путь к конфигурационному файлу.
     * <P>
     * Используется переменная {@link Globals#logFileHandlerFileName}, 
     * хранящая путь к файлу для журналирования.
     * <P>
     * В переменной  {@link #exitStatus} хранится код завершения. При 
     * обнаружении ошибки в командной строке переменной {@link #exitStatus} 
     * присваивается значение {@link #SERV_WRONG_KEY}, при обнаружении 
     * ошибки в конфигурационном файле или возникновении ошибки при 
     * запуске компонентов сервера {@link #exitStatus} присваивается 
     * значение {@link #SERV_START_ERR}, при успешном запуске и 
     * завершении работы этой переменной присваивается значение 
     * {@link #SERV_OK}. 
     * <P>
     * Выполнение метода завершается с помощью {@link System#exit}, 
     * который передает операционной системе код завершения 
     * {@link #exitStatus}.
     * 
     */
    
    public static void main(String[] args) {
        String helpText = 
        		"Simple Irc Server v." + Constants.SERVER_VERSION + " " +
                "Copyright (C) 2012  Nikolay Kirdin\n" +
                "This program comes with ABSOLUTELY NO WARRANTY. \n" +
                "This program is free software: you can redistribute it " +
                "and/or modify it under the terms of the GNU Lesser " +
                "General Public License Version 3. " +
                "http://www.gnu.org/licenses/.\n\n" + 
                "Usage: Server [-c <config file>] [-h] [-l <logging file>] [-V]\n" +
                "By default: configuration file is \"IrcServerConfig.xml\",\n" +
                " logging file is \"IrcServerLog.xml\" in a current directory.\n"
                + "-V print version of program./n";
            
        Server server = new Server();
        String key = null;
        boolean done = false;

        int index = 0;
        
        while (index < args.length) {
            
            key = args[index++];
            
            if (key.equals("-c")) {
                try {
                    String configFilepath =
                        new File(args[index++]).getCanonicalPath();
                    Globals.configFilename.set(configFilepath);
                } catch (Exception e) {
                    System.err.println("Error in configuration file path:"
                            + e);
                    server.setError(true);
                    server.exitStatus = SERV_WRONG_KEY;
                }
            } else if (key.equals("-h")) {
                System.out.println(helpText);
                done = true;
                server.exitStatus = SERV_OK;
            } else if (key.equals("-l")) {
                try {
                    String loggingFilepath = 
                            new File(args[index++]).getCanonicalPath();
                    Globals.logFileHandlerFileName.set(loggingFilepath);
                } catch (Exception e) {
                    System.err.println("Error in logging file path:" + e);
                    server.setError(true);
                    server.exitStatus = SERV_WRONG_KEY;
                }
            } else if (key.equals("-V")) {
                System.out.println(Constants.SERVER_VERSION);
                done = true;
                server.exitStatus = SERV_OK;
            } else {
                System.err.println("Error. Unknown key:" + key);
                server.setError(true);
                server.exitStatus = SERV_WRONG_KEY;
            }
        }
        
        if (!server.isError() && !done) { 
            server.run();
        }
        
        System.exit(server.exitStatus);
    }    
    
    /**
     * Этот метод управляет запуском, перезапуском, остановом 
     * компонентов сервера и повторным чтением файла конфигурации. Для 
     * успешного запуска сервера, необходимо, чтобы процессу был 
     * доступен файл конфигурации и файл журналирования.  
     *
     * <P>Переменной {@link #exitStatus} могут присваиваться следующие
     * коды завершения:
     * <UL>
     * <LI> {@link #SERV_OK} - метод успешно завершен;</LI>
     * <LI> {@link #SERV_START_ERR} - во время выполнения метода были 
     * обнаружены ошибки.</LI>
     * </UL>
     *
     * <P> Поведение метода (старт, останов, перезапуск, повторное 
     * чтение файла конфигурации) определяется следующими переменными: 
     * <UL>
     * <LI> {@link #error}; 
     * <LI> {@link Globals#serverDown};</LI>
     * <LI> {@link Globals#serverRestart};</LI>
     * <LI> {@link Globals#serverReconfigure}.</LI>
     * </UL>
     * <P> Нормальным значением для этих переменных является значение 
     * false. В том случае, если им будет присвоено значение true, то 
     * метод выполнит следующие действия по изменению состояния 
     * компонентов сервера.
     * 
     * <P> После присвоения {@link #error} значения true запущенные 
     * компоненты будут остановлены и метод прекратит работу, переменной 
     * {@link #exitStatus} будет присвоено значение
     * {@link #SERV_START_ERR}. Значение переменной {@link #error}
     * определяется успехом или неуспехом запуска компонентов, при 
     * неуспешном запуске ей присваивается значение true.
     *
     * <P> Переменная {@link Globals#serverDown} управляет нормальным 
     * остановом сервера. После присвоения значения true будут выполнены
     * процедуры остановки запущенных компонентов и метод завершится.  
     * 
     * <P> Переменная {@link Globals#serverRestart} управляет 
     * процедурой перезапуска сервера. После присвоения значения true 
     * будут выполнены процедуры останова компонентов сервера, затем 
     * будут выполнены процедуры запуска компонентов. 
     *
     * <P> Переменная {@link Globals#serverReconfigure} управляет 
     * процедурой повторного чтения файла конфигурации. После присвоения 
     * значения true будут выполнены процедуры чтения файла конфигурации.
     */
     
    public void run() {
        List<AbstractIrcServerProcessor> serverProcessorList = new ArrayList<>();
        
        IrcConfigParser ircConfigParser = null;
        
        Locale.setDefault(Locale.ENGLISH);

        TimeZone.setDefault(Globals.timeZone.get());
        
        ParameterInitialization.loggerSetup();

        while (!Globals.serverDown.get() && !error) {
            
        	if (Globals.serverRestart.get()) {
        		Globals.serverStartTime.set(System.currentTimeMillis());
                Globals.serverRestart.set(false);
        	}
            
            ParameterInitialization.configSetup();

            ircConfigParser = new IrcConfigParser(
                    Globals.configFilename.get(),
                    Globals.db.get(), 
                    Globals.logger.get());
            error = ircConfigParser.useIrcConfigFile();

            TimeZone.setDefault(Globals.db.get().getIrcServerConfig().getTimeZone());
            
            parameterInitialization = new ParameterInitialization();

            if (!error) {
                ParameterInitialization.loggerLevelSetup();
                parameterInitialization.run();
            }
            
            transcriptFileProcessor = new TranscriptFileProcessor();
            outputQueueProcessor = new OutputQueueProcessor();
            incomingConnectionListener = new IncomingConnectionListener();

            /** Attention
             * Order is matter!!!
             */
            serverProcessorList.addAll(Arrays.<AbstractIrcServerProcessor>asList(
                    transcriptFileProcessor,
                    new NetworkConnectionProcessor(),
                    outputQueueProcessor,
                    new IrcTalkerProcessor(), 
                    new InputQueueProcessor(),
                    new InputStreamProcessor(),
                    incomingConnectionListener
                    ));
                       
            serverProcessorList.stream().forEach(p -> {if (!isError()) p.processorStart();});                       
            
            while (!Globals.serverDown.get() && !Globals.serverRestart.get() && !error) {
                boolean confError = false;
                if (Globals.serverReconfigure.get()) {
                    Globals.serverReconfigure.set(false);
                    ircConfigParser = new IrcConfigParser(
                            Globals.configFilename.get(), 
                            Globals.db.get(), 
                            Globals.logger.get());
                    confError = ircConfigParser.useIrcConfigFile();
                    if (!confError) {
                        TimeZone.setDefault(Globals.db.get().getIrcServerConfig().getTimeZone());
                        transcriptFileProcessor.setIrcTranscriptConfig(Globals.ircTranscriptConfig.get());
                        ParameterInitialization.loggerLevelSetup();
                        incomingConnectionListener.processorStop();
                        error = incomingConnectionListener.processorStart();
                    }
                }
                try {
                    Thread.sleep(Globals.sleepTO.get() * 2);
                } catch (InterruptedException e) {}
            }
            
            outputQueueProcessor.shortenTimeouts();        
            
            Collections.reverse(serverProcessorList);
            serverProcessorList.stream().forEach(p -> p.processorStop());
            serverProcessorList.clear();
        }
        
        ParameterInitialization.loggerDown();
        
        if (isError()) {
            exitStatus = SERV_START_ERR;
        }
    }

    public boolean isError() {
        return error;
    }

    public void setError(boolean error) {
        this.error = error;
    }

}
