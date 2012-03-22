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

import java.util.*;
import java.util.logging.*;
import java.io.*;
import java.net.*;
import java.nio.charset.*;
/**
 * Server - класс, который служит для управления запуском, остановом, и 
 * перезапуском основных компонентов сервера IRC. 
 *
 * @version 0.5.1 2012-02-20
 * @author  Nikolay Kirdin
 */

public class Server implements Runnable {
	
	/** Код завершения в случае успешного завершения. */
	public static final int  SERV_OK = 0;
	
    /** Код завершения для ситуации "неизвестный ключ командной строки". */
    public static final int SERV_WRONG_KEY = 1;
    
    /** 
     * Код завершения для ситуации "во время запуска компонентов 
     * сервера произошла ошибка". 
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
     * завершении работы этой переменной присваивается значение 0. 
     * <P>
     * Выполнение метода завершается с помощью {@link System#exit}, 
     * который передает операционной системе код завершения 
     * {@link #exitStatus}.
     * 
     */
    
    public static void main(String[] args) {
        Server server = new Server();
        String key = null;
        boolean done = false;
        String helpText = 
                "Simple Irc Server v." + Constants.SERVER_VERSION + " " +
                "Copyright (C) 2012  Nikolay Kirdin\n" +
                "This program comes with ABSOLUTELY NO WARRANTY. \n" +
                "This program is free software: you can redistribute it " +
                "and/or modify it under the terms of the GNU Lesser " +
                "General Public License Version 3. " +
                "http://www.gnu.org/licenses/.\n\n" + 
                "Usage: Server.server [-c <config file>] [-h]" +
                " [-l <logging file>] [-v] \n" +
                " By default: configuration file " +
                "is \"IrcServerConfig.xml\",\n" +
                " logging file is \"IrcServerLog.xml\" in a current " +
                "directory.\n" + "-v print version of program/n";

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
                    server.error = true;
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
                    server.error = true;
                    server.exitStatus = SERV_WRONG_KEY;
                }
            } else if (key.equals("-V")) {
                System.out.println(Constants.SERVER_VERSION);
                done = true;
                server.exitStatus = SERV_OK;
            } else {
                System.err.println("Error. Unknown key:" + key);
                server.error = true;
                server.exitStatus = SERV_WRONG_KEY;
            }
        }
        
        if (!server.error && !done) { 
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
     * <LI> {@link #SERV_OK} - метод успешно завершен; 
     * <LI> {@link #SERV_START_ERR} - во время выполнения метода были 
     * обнаружены ошибки. 
     * </UL>
     *
     * <P> Поведение метода (старт, останов, перезапуск, повторное 
     * чтение файла конфигурации) определяется следующими переменными: 
     * <UL>
     * <LI> {@link #error}; 
     * <LI> {@link Globals#serverDown}; 
     * <LI> {@link Globals#serverRestart};
     * <LI> {@link Globals#serverReconfigure}. 
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
        
        IrcConfigParser ircConfigParser = null;
        
        Globals.serverStartTime.set(System.currentTimeMillis());

        Locale.setDefault(Locale.ENGLISH);

        TimeZone.setDefault(Globals.timeZone.get());
        
        ParameterInitialization.loggerSetup();

        while (!Globals.serverDown.get() && !error) {
            
        	Globals.serverRestart.set(false);
            
            ParameterInitialization.configSetup();

            ircConfigParser = new IrcConfigParser(
            		Globals.configFilename.get(),
            		Globals.db.get(), 
            		Globals.logger.get());
            error = ircConfigParser.useIrcConfigFile();

            TimeZone.setDefault(
            		Globals.db.get().getIrcServerConfig().getTimeZone());
            
            if (!error) {
                ParameterInitialization.loggerLevelSetup();
                error = parameterInitializationStart();
            }
            if (!error) {
                error = transcriptFileProcessorStart();
            }            
            if (!error) {
                error = ircTalkerProcessorStart();
            }
            if (!error) {
                error = networkConnectionProcessorStart();
            }
            if (!error) {
                error = outputQueueProcessorStart();
            }
            if (!error) {
                error = inputQueueProcessorStart();
            }
            if (!error) {
                error = inputStreamProcessorStart();
            }
            if (!error) {
                error = incomingConnectionListenerStart();
            }
            
            while (!Globals.serverDown.get() && 
            		!Globals.serverRestart.get() && !error) {
                boolean confError = false;
                if (Globals.serverReconfigure.get()) {
                	Globals.serverReconfigure.set(false);
                    ircConfigParser = new IrcConfigParser(
                    		Globals.configFilename.get(), 
                    		Globals.db.get(), 
                    		Globals.logger.get());
                    confError = ircConfigParser.useIrcConfigFile();
                    if (!confError) {
                        TimeZone.setDefault(
                        		Globals.db.get().getIrcServerConfig(
                                ).getTimeZone());
                        transcriptFileProcessor.setIrcTranscriptConfig(
                        		Globals.ircTranscriptConfig.get());
                        ParameterInitialization.loggerLevelSetup();
                        incomingConnectionListenerStop();
                        error = incomingConnectionListenerStart();
                    }
                }
                try {
                    Thread.sleep(Globals.sleepTO.get() * 2);
                } catch (InterruptedException e) {}
            }
            
            /* Уменьшие периода опроса выходных очередей */
            if (outputQueueProcessor != null) {
                outputQueueProcessor.plannedDuration.set(1);
                outputQueueProcessor.sleepTO.set(1);
                outputQueueProcessor.limitingTO.set(1);
            }            
            
            incomingConnectionListenerStop();
            
            inputStreamProcessorStop();

            inputQueueProcessorStop();

            ircTalkerProcessorStop();

            outputQueueProcessorStop();

            networkConnectionProcessorStop();
            
            transcriptFileProcessorStop();
            
        }
        
        ParameterInitialization.loggerDown();
        
        if (error) {
            exitStatus = SERV_START_ERR;
        }
    }

    /** 
     * Общая инициализация. 
     * @return true инициализация успешно завершена, 
     * false инициализация завершена с ошибками. 
     */
    private boolean parameterInitializationStart() {
        parameterInitialization = new ParameterInitialization();
        parameterInitialization.run();
        return parameterInitialization.error.get();
    }

    /** 
     * Инициализация процесса вывода сообщений в файл-протокол.
     * @return true инициализация успешно завершена, 
     * false инициализация завершена с ошибками. 
     */
    private boolean transcriptFileProcessorStart() {
        boolean error = false;
        transcriptFileProcessor = new TranscriptFileProcessor();
        transcriptFileProcessor.setIrcTranscriptConfig(
        		Globals.ircTranscriptConfig.get());
        transcriptFileProcessor.thread.set(new 
        		Thread(transcriptFileProcessor));        
        transcriptFileProcessor.running.set(true);
        transcriptFileProcessor.thread.get().start();
        Globals.logger.get().log(Level.INFO, "TranscriptFileProcessor:" + 
        		transcriptFileProcessor.thread.get());
        
        try {
            Thread.sleep(Globals.sleepTO.get());
        } catch (InterruptedException e) {}
        
        error = transcriptFileProcessor.thread.get().getState() == 
        		Thread.State.NEW
                || transcriptFileProcessor.thread.get().getState() ==
                Thread.State.TERMINATED;
                
        return error;
    }

    /** Завершение процесса вывода сообщений в файл-протокол.*/
    private void transcriptFileProcessorStop() {
        if (transcriptFileProcessor != null
            && transcriptFileProcessor.thread.get() != null) {
        	transcriptFileProcessor.down.set(true);
            stopProcess(transcriptFileProcessor.thread.get());
        }
    }
    
    /** 
     * Инициализация процесса проверки состояния клиентов сервера. 
     * @return true инициализация успешно завершена, 
     * false инициализация завершена с ошибками. 
     */
    private boolean ircTalkerProcessorStart() {
        boolean error = false;
        ircTalkerProcessor = new IrcTalkerProcessor();
        ircTalkerProcessor.thread.set(new Thread(ircTalkerProcessor));        
        ircTalkerProcessor.running.set(true);
        ircTalkerProcessor.thread.get().start();
        Globals.logger.get().log(Level.INFO, "IrcTalkerProcessor:" + 
                ircTalkerProcessor.thread.get());
        
        try {
            Thread.sleep(Globals.sleepTO.get());
        } catch (InterruptedException e) {}
        
        error = ircTalkerProcessor.thread.get().getState() == 
        		Thread.State.NEW
                || ircTalkerProcessor.thread.get().getState() ==
                Thread.State.TERMINATED;
                
        return error;
    }

    /** Завершение проверки состояния клиентов сервера.*/
    private void ircTalkerProcessorStop() {
        if (ircTalkerProcessor != null
            && ircTalkerProcessor.thread.get() != null) {
            ircTalkerProcessor.down.set(true);
            stopProcess(ircTalkerProcessor.thread.get());
            ircTalkerProcessor.termination();
        }
        
    }

    /**
     * Инициализация проверки состояния соединений клиентов сервера.
     * @return true инициализация успешно завершена, 
     * false инициализация завершена с ошибками. 
     */
    private boolean networkConnectionProcessorStart() {
        boolean error = false;
        networkConnectionProcessor = new NetworkConnectionProcessor();
        networkConnectionProcessor.thread.set(new 
        		Thread(networkConnectionProcessor));        
        networkConnectionProcessor.running.set(true);
        networkConnectionProcessor.thread.get().start();
        Globals.logger.get().log(Level.INFO, "NetworkConnectionProcessor:" + 
                networkConnectionProcessor.thread.get());
        
        try {
            Thread.sleep(Globals.sleepTO.get());
        } catch (InterruptedException e) {}
        
        error = networkConnectionProcessor.thread.get().getState() ==
                Thread.State.NEW
                || networkConnectionProcessor.thread.get().getState() ==
                Thread.State.TERMINATED;
                
        return error;
    }

    /** 
     * Завершение процесса проверки состояния соединений клиентов 
     * сервера. 
     */
    private void networkConnectionProcessorStop() {
        if (networkConnectionProcessor != null
            && networkConnectionProcessor.thread.get() != null) {
            networkConnectionProcessor.down.set(true);
            stopProcess(networkConnectionProcessor.thread.get());
            networkConnectionProcessor.termination();
        }
        
    }

    /** 
     * Инициализация процесса вывода сообщений клиентам. 
     * @return true инициализация успешно завершена, 
     * false инициализация завершена с ошибками. 
     */
    private boolean outputQueueProcessorStart() {
        boolean error = false;
        outputQueueProcessor = new OutputQueueProcessor();
        outputQueueProcessor.thread.set(new Thread(outputQueueProcessor));        
        outputQueueProcessor.running.set(true);
        outputQueueProcessor.thread.get().start();
        Globals.logger.get().log(Level.INFO, "OutputQueueProcessor:" + 
                outputQueueProcessor.thread.get());
        
        try {
            Thread.sleep(Globals.sleepTO.get());
        } catch (InterruptedException e) {}
        
        error = outputQueueProcessor.thread.get().getState() ==
                Thread.State.NEW
                || outputQueueProcessor.thread.get().getState() ==
                Thread.State.TERMINATED;
                
        return error;
    }
    
    /** Завершение процесса вывода сообщений клиентам. */
    private void outputQueueProcessorStop() {
        if (outputQueueProcessor != null
            && outputQueueProcessor.thread.get() != null) {
            outputQueueProcessor.down.set(true);
            stopProcess(outputQueueProcessor.thread.get());
        }
    }

    /** 
     * Инициализация процесса обработки ссобщений клиентов. 
     * @return true инициализация успешно завершена, 
     * false инициализация завершена с ошибками. 
     */
    private boolean inputQueueProcessorStart() {
        boolean error = false;
        inputQueueProcessor = new InputQueueProcessor();
        inputQueueProcessor.thread.set(new Thread(inputQueueProcessor));        
        inputQueueProcessor.running.set(true);
        inputQueueProcessor.thread.get().start();
        Globals.logger.get().log(Level.INFO, "InputQueueProcessor:" + 
                inputQueueProcessor.thread.get());
        
        try {
            Thread.sleep(Globals.sleepTO.get());
        } catch (InterruptedException e) {}
        
        error = inputQueueProcessor.thread.get().getState() == 
        		Thread.State.NEW
                || inputQueueProcessor.thread.get().getState() ==
                Thread.State.TERMINATED;
                
        return error;
    }
    
    /** Завершение процесса обработки сообщений клиентов. */
    private void inputQueueProcessorStop() {
        if (inputQueueProcessor != null
            && inputQueueProcessor.thread.get() != null) {
            inputQueueProcessor.down.set(true);
            stopProcess(inputQueueProcessor.thread.get());
        }
    }

    /** 
     * Инициализация процесса чтения поступающих сообщений клиентов 
     * сервера. 
     * @return true инициализация успешно завершена, 
     * false инициализация завершена с ошибками. 
     */
    private boolean inputStreamProcessorStart() {
        boolean error = false;
        inputStreamProcessor = new InputStreamProcessor();
        inputStreamProcessor.running.set(true);
        inputStreamProcessor.thread.set(new Thread(inputStreamProcessor));        
        inputStreamProcessor.thread.get().start();
        Globals.logger.get().log(Level.INFO, "InputStreamProcessor:" + 
                inputStreamProcessor.thread.get());
        
        try {
            Thread.sleep(Globals.sleepTO.get());
        } catch (InterruptedException e) {}
        
        error = inputStreamProcessor.thread.get().getState() == 
        		Thread.State.NEW
                || inputStreamProcessor.thread.get().getState() ==
                Thread.State.TERMINATED;
                
        return error;
    }
    
    /** 
     * Завершение процесса чтения поступающих сообщений клиентов 
     * сервера. 
     */
    private void inputStreamProcessorStop() {
        if (inputStreamProcessor != null
            && inputStreamProcessor.thread.get() != null) {
            inputStreamProcessor.down.set(true);
            stopProcess(inputStreamProcessor.thread.get());
        }
    }

    /** 
     * Инициализация процесса обработки входящих сетевых соединений. 
     * @return true инициализация успешно завершена, 
     * false инициализация завершена с ошибками. 
     */
    private boolean incomingConnectionListenerStart() {
        boolean error = false;
        int serverPortNumber = 
        		Globals.db.get().getIrcInterfaceConfig().getPort();
        Charset listenerCharset = 
        		Globals.db.get().getIrcInterfaceConfig().getCharset();
        InetAddress inetAddress = 
        		Globals.db.get().getIrcInterfaceConfig().getInetAddress(); 
        incomingConnectionListener = new IncomingConnectionListener();
        incomingConnectionListener.setInetAddress(inetAddress);
        incomingConnectionListener.setServerPortNumber(serverPortNumber);
        incomingConnectionListener.listenerCharset.set(listenerCharset);
        incomingConnectionListener.thread.set(new 
        		Thread(incomingConnectionListener));        
        incomingConnectionListener.running.set(true);
        incomingConnectionListener.thread.get().start();
        Globals.logger.get().log(Level.INFO, "IncomingConnectionListener:" + 
                incomingConnectionListener.thread.get());
        
        try {
            Thread.sleep(Globals.sleepTO.get());
        } catch (InterruptedException e) {}
        
        error = incomingConnectionListener.thread.get().getState() ==
                Thread.State.NEW
                || incomingConnectionListener.thread.get().getState() ==
                Thread.State.TERMINATED
                || incomingConnectionListener.error.get();
                
        return error;
    }
    
    /** Завершение процесса обработки входящих сетевых соединений. */
    private void incomingConnectionListenerStop() {
        if (incomingConnectionListener != null
            && incomingConnectionListener.thread.get() != null) {
            incomingConnectionListener.down.set(true);
            stopProcess(incomingConnectionListener.thread.get());
        }
    }

    /** 
     * Останов потока. 
     * @param thread поток, который необходимо остановить.
     */
    private void stopProcess(Thread thread) {
        
        try {
            Thread.sleep(Globals.sleepTO.get() * 2);
        } catch (InterruptedException e) {}
        
        if (thread.getState() != Thread.State.NEW
            && thread.getState() != Thread.State.RUNNABLE
            && thread.getState() != Thread.State.TERMINATED) {
            thread.interrupt();
        }
        
        try {
            Thread.sleep(Globals.sleepTO.get() * 2);
        } catch (InterruptedException e) {}
    }

}
