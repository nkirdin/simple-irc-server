package simpleircserver;
/*
 * 
 * Server 
 * is part of Simple Irc Server
 *
 *
 * Copyright (С) 2012, 2015 Nikolay Kirdin
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
     *  Код завершения для ситуации "во время запуска или рестарта компонентов сервера произошла ошибка". 
     */
    public static final int SERV_START_ERR = 2;    

    /** 
     * Код завершения для ситуации "сервер не был запущен по причинам не связанными с ошибками". 
     */
    public static final int SERV_NOT_START = 4;    
    
    /** 
     * Код завершения для ситуации "сервер не был запущен по причинам связанными с инициализацией логгера". 
     */
    public static final int SERV_WRONG_LOG = 8;    
    
    /**
     * 
     * Код завершения для ситуации "найдена ошибка в файле конфигурации".
     */
    public static final int SERV_CONF_ERR = 16;

    /** Признак ошибки. */
    private boolean error = false;
    
    /** Состояние сервера. Комбинация из значений: 
     * SERV_OK; SERV_CONF_ERR, SERV_WRONG_KEY; SERV_START_ERR; SERV_NOT_START, SERV_WRONG_LOG. 
     */    
    private int state = SERV_OK;
            
    /** Конструктор. */
    public Server() {
        Locale.setDefault(Locale.ENGLISH);
        TimeZone.setDefault(Globals.timeZone.get());
        if (!ParameterInitialization.loggerSetup()) {            
            state = SERV_WRONG_LOG;
            error = true;
        }
    }
        
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
     * В переменной  {@link #state} хранится код состояния сервера, 
     * который представляет собой побитовою комбинацию значений SERV_OK; 
     * SERV_WRONG_KEY; SERV_START_ERR; SERV_NOT_START. При 
     * обнаружении ошибки в командной строке в переменную {@link #state} 
     * добавляется значение {@link #SERV_WRONG_KEY}. При обнаружении 
     * ошибки в конфигурационном файле или возникновении ошибки при 
     * запуске компонентов сервера в переменную {@link #state} добавляется 
     * значение {@link #SERV_START_ERR}. Если сервер не должен быть 
     * запущен по причинам, не связанными с ошибками, то в эту переменную 
     * добавляется значение {@link #SERV_NOT_START}. При успешном запуске и 
     * успешном завершении работы эта переменная должна содержать значение 
     * {@link #SERV_OK}. 
     * <P>
     * Выполнение метода завершается с помощью {@link System#exit}, 
     * который передает операционной системе код завершения 
     * {@link #state}.
     * 
     */   
    public static void main(String[] args) {
        
        int state = parseCommandLine(args);
        
        if (state == SERV_OK) { 
            
            Server server = new Server();
            
            server.run();

            state = server.getState();
        }
        
        System.exit(state);
    }

    
    /**
     * Этот метод управляет запуском, перезапуском, остановом 
     * компонентов сервера и повторным чтением файла конфигурации. Для 
     * успешного запуска сервера, необходимо, чтобы процессу был 
     * доступен файл конфигурации и файл журналирования.  
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
     * {@link #state} будет присвоено значение
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
     
    /* Список процессоров для запуска. В порядке очередности. */ 
    List<AbstractIrcServerProcessor> startList = new ArrayList<>();
    
    /* Список процессоров для реконфигурирования. В порядке очередности. */ 
    List<AbstractIrcServerProcessor> reconfigureList = new ArrayList<>();
    
    /* Список процессоров для выполнения действий перед остановом. В порядке очередности. */ 
    List<AbstractIrcServerProcessor> predstopList = new ArrayList<>();
    
    /* Список процессоров для останова. В порядке очередности. */ 
    List<AbstractIrcServerProcessor> stopList = new ArrayList<>();
    
    public void run() {

        Globals.serverStartTime.set(System.currentTimeMillis());

        initProcessors();

        while (!Globals.serverDown.get() && !isError()) {
            
        	if (Globals.serverRestart.get()) {
        		Globals.serverStartTime.set(System.currentTimeMillis());
                Globals.serverRestart.set(false);
        	}
            
            ParameterInitialization.configSetup();

            if (parseConfigFile()) {
                setError(true);
                setState(getState() | SERV_CONF_ERR);
            }
          
            setServerTimezone();
            
            if (!isError()) {
                ParameterInitialization.loggerLevelSetup();
                setError(!ParameterInitialization.networkComponentsSetup());
            }
            
            startProcessors();                       
            
            while (!Globals.serverDown.get() && !Globals.serverRestart.get() && !error) {
                if (Globals.serverReconfigure.get()) {
                    Globals.serverReconfigure.set(false);
                    reconfigureProcessors();
                }
                try {
                    Thread.sleep(Globals.sleepTO.get() * 2);
                } catch (InterruptedException e) {}
            }
            
            predstopProcessors();        
            
            stopProcessors();
        }
        
        ParameterInitialization.loggerDown();
        
        if (isError()) {
            setState(getState() | SERV_START_ERR);
        }
    }

    private boolean parseConfigFile() {
        IrcConfigParser ircConfigParser = 
                new IrcConfigParser(Globals.configFilename.get(), Globals.db.get(), Globals.logger.get());
        return ircConfigParser.useIrcConfigFile();        
    }

    private void initProcessors() {

        TranscriptFileProcessor transcriptFileProcessor = new TranscriptFileProcessor();
        OutputQueueProcessor outputQueueProcessor = new OutputQueueProcessor();
        IncomingConnectionListener incomingConnectionListener = new IncomingConnectionListener();

        /** Attention
         * Order is matter!!!
         */
        startList.addAll(Arrays.<AbstractIrcServerProcessor>asList(
                transcriptFileProcessor,
                new NetworkConnectionProcessor(),
                outputQueueProcessor,
                new IrcTalkerProcessor(), 
                new InputQueueProcessor(),
                new InputStreamProcessor(),
                incomingConnectionListener
                ));

        reconfigureList.add(transcriptFileProcessor);
        reconfigureList.add(incomingConnectionListener);
        
        predstopList.add(outputQueueProcessor);
        
        stopList.addAll(startList);        
        Collections.reverse(stopList);
    }

    private void startProcessors() {               
        startList.forEach(p -> {if (!this.isError()) this.setError(!p.processorStart());});
    }

    private void reconfigureProcessors() {
        if (!parseConfigFile()) {
            setServerTimezone();
            ParameterInitialization.loggerLevelSetup();
            reconfigureList.forEach(p -> this.setError(!p.processorReconfigure()));
        } else {
            setError(true);
            setState(getState() | SERV_CONF_ERR);
        }
    }

    private void predstopProcessors() {
        predstopList.forEach(p -> p.processorPredstop());
        try {
            Thread.sleep(Globals.sleepTO.get() * 2);
        } catch (InterruptedException e) {}
    }    

    private void stopProcessors() {
        stopList.forEach(p -> p.processorStop());
    }

    private static int parseCommandLine(String[] args) {
        String helpText = "Simple Irc Server v." + Constants.SERVER_VERSION + " "
                + "Copyright (C) 2012, 2015  Nikolay Kirdin\n" + "This program comes with ABSOLUTELY NO WARRANTY. \n"
                + "This program is free software: you can redistribute it "
                + "and/or modify it under the terms of the GNU Lesser " + "General Public License Version 3. "
                + "http://www.gnu.org/licenses/.\n\n"
                + "Usage: Server [-c <config file>] [-h] [-l <logging file>] [-V]\n"
                + "By default: configuration file is \"IrcServerConfig.xml\",\n"
                + " logging file is \"IrcServerLog.xml\" in a current directory.\n" + "-V print version of program./n";

        
        int state = SERV_OK;
        
        String outputMessage = "";

        int index = 0;

        while (index < args.length) {

            switch(args[index++]) {
            
            case "-c":
                try {
                    String configFilepath = new File(args[index++]).getCanonicalPath();
                    Globals.configFilename.set(configFilepath);
                } catch (Exception e) {
                    outputMessage = outputMessage + "\n\r" + "Error in configuration file path: " + e;
                    state |= SERV_WRONG_KEY;
                }
                break;
            case "-h":
                outputMessage = outputMessage + "\n\r" + helpText;
                state |= SERV_NOT_START;
                break;
            case "-l":
                try {
                    String loggingFilepath = new File(args[index++]).getCanonicalPath();
                    Globals.logFileHandlerFileName.set(loggingFilepath);
                } catch (Exception e) {
                    outputMessage = outputMessage + "\n\r" + "Error in logging file path:" + e;
                    state |= SERV_WRONG_KEY;
                }
                break;
            case "-V":
                outputMessage = outputMessage + "\n\r Version: " + Constants.SERVER_VERSION;
                state |= SERV_NOT_START;
            default:
                outputMessage = outputMessage + "\n\r" + "Error. Unknown key:" + args[index - 1];
                state |= SERV_WRONG_KEY;
            }                    
                
        }

        if (!outputMessage.isEmpty()) {
            System.err.println(outputMessage);
        }
        
        return state;

    }
    
    private void setServerTimezone() {
        TimeZone.setDefault(Globals.db.get().getIrcServerConfig().getTimeZone());
    }

    public boolean isError() {
        return error;
    }

    public void setError(boolean error) {
        this.error = error;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

}
