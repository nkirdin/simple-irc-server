package simpleircserver.processor;
/*
 * 
 * IncomingConnectionListener 
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

import java.util.logging.*;

import simpleircserver.base.Globals;
import simpleircserver.connection.ConnectionState;
import simpleircserver.connection.NetworkConnection;
import simpleircserver.parser.Reply;
import simpleircserver.talker.IrcTalkerState;
import simpleircserver.talker.user.User;

import java.util.concurrent.atomic.*;
import java.io.*;
import java.net.*;
import java.nio.charset.Charset;


 /**
 * Программный процессор, который обслуживает обращения к серверному 
 * порту. Для новых сетевых соединений он создает объекты классов 
 * {@link NetworkConnection} и {@link User} затем помещает их в 
 * репозиторий, после этого запускается процессы определения доменного 
 * имени клиента и инициализации потоков ввода/вывода. 
 *
 * @version 0.5 2012-02-13
 * @version 0.5.3 2015-11-05 Program units were moved from default package into packages with names. Unit tests were added.
 * @author  Nikolay Kirdin
 */
public class IncomingConnectionListener implements Runnable {
    
    /** 
     * Управление выполнением/остановом основного цикла.
     * true - цикл выполняется, false - цикл приостановлен. 
     */ 
    public AtomicBoolean running = new AtomicBoolean();

    /** 
     * Управление выполнением/завершением основного цикла.
     * true - цикл завершается, false - цикл может выполнятся.
     */ 
    public AtomicBoolean down = new AtomicBoolean();
    
    /** Поток метода run этого объекта. */ 
    public AtomicReference<Thread> thread = new AtomicReference<Thread>();
    
    /** Минимальная длительность таймаутов. */
    public AtomicLong limitingTO = new AtomicLong(1);
    
    /** Стандартная длительность таймаутов. */
    public AtomicLong sleepTO = new AtomicLong(100);
    
    /** Таймаут сокета. */
    public AtomicInteger soTimeout = new AtomicInteger(100);
    
    /** Кодировка сообщений. */
    public AtomicReference<Charset> listenerCharset = 
            new AtomicReference<Charset>(Globals.listenerCharset.get());
        
    /** Признак ошибки при выполнении метода. */        
    public AtomicBoolean error = new AtomicBoolean(false);
    
    /** IP-адрес интерфейса. */
    private InetAddress inetAddress;
    
    /** Серверный сокет. */
    private ServerSocket serverSocket;
    
    /** Номер порта для запросов на сетевое соединение. */
    private int serverPortNumber = Globals.serverPortNumber.get();
    
    /** Конструктор по умолчанию. */
    public IncomingConnectionListener() {}

    /**
     * Получение IP-адреса интерфейса. 
     * @return IP-адреса интерфейса. 
     */
    public InetAddress getInetAddress() {
        return inetAddress;
    }

    /**
     * Задание IP-адреса интерфейса. 
     * @param inetAddress номер сетевого порта.
     */
    public void setInetAddress(InetAddress inetAddress) {
        this.inetAddress = inetAddress;
    }
    
    /**
     * Получение номера сетевого порта. 
     * @return номер сетевого порта для запросов на сетевое соединение. 
     */
    public int getServerPortNumber() {
        return serverPortNumber;
    }

    /**
     * Задание номера сетевого порта для запросов на сетевое соединение.
     * @param serverPortNumber номер сетевого порта.
     */
    public void setServerPortNumber(int serverPortNumber) {
        this.serverPortNumber = serverPortNumber;
    }

    /** 
     * Получение объекта класса {@link ServerSocket} сервера.
     * @return серверный сокет.
     */
    public ServerSocket getServerSocket() {
        return serverSocket;
    }

    /**
     * Задание объекта класса {@link ServerSocket} сервера.
     * @param serverSocket объект класса {@link ServerSocket} сервера.
     */
    public void setServerSocket(ServerSocket serverSocket) {
        this.serverSocket = serverSocket;
    }

    /**
     * Метод run() - это метод, который обслуживает порт IRC сервера.
     *
     *<P> В основном цикле этого метода производится прослушивание 
     * серверного сокета, время блокирования этого сокета ограничено 
     * величиной {@link #soTimeout}.
     * 
     * <P>В начале выполнения метод инициализирует объект класса 
     * {@link ServerSocket} и изменяет значения для следующих параметров 
     * порта: 
     * <UL>
     *      <LI> SO_RCVBUF устанавливается равным 
     *      {@link Globals#receiveBufferSize};</LI>
     *      <LI> SO_TIMEOUT устанавливается равным 
     *      {@link #soTimeout};</LI>
     *      <LI> IP-адрес интерфейса устанавливается тем-же, что у 
     *      локального интерфейса.</LI>
     * </UL>
     *
     * <P>Если во время инициализации порта или во время выполнения 
     * основного цикла, будут обнаружены какие-либо ошибки, связанные с 
     * этим портом, то этот факт будет занесен в журнал, выполнение 
     * метода будет продолжено. 
     * 
     * <P>После получения локального сокета нового соединения, проверяется 
     * степень нагруженности программы, путем проверки множества 
     * {@link Globals#ircServerProcessorSet}, если это множество не пусто,
     * то программа находится в высоконагруженном сосотоянии. В этом 
     * случае сокет закрывается и после таймаута происходит переход к 
     * началу основного цикла. 
     * 
     * <P>После того как удаленный клиент установит связь с сервером, 
     * будут созданы объекты классов {@link NetworkConnection} и 
     * {@link User}, в которых будет хранится информация, необходимая 
     * для осуществления обмена данными с этим клиентом. Затем, будет 
     * произведена  попытка помещения этих объектов в репозитарий. При 
     * успешном помещении будет произведен запуск процессов определения 
     * доменного имени клиента и инициализации потоков ввода/вывода. В 
     * том случае, если попытка окончилась неудачей, созданные объекты 
     * будут удалены и будет произведено увеличение таймаута основного 
     * цикла до величины {@link #sleepTO}.
     *
     * <P>При создании объекта класса {@link NetworkConnection} он будет 
     * находится в состоянии {@link ConnectionState#NEW}, а объект 
     * класса {@link User} будет находится в состоянии 
     * {@link IrcTalkerState#NEW}. После успешного помещения этих 
     * объектов в репозитарий объект класса {@link User} будет переведен
     * в состояние {@link IrcTalkerState#REGISTERING}. В том случае, 
     * если попытка помещения в репозитарий закончится неудачно эти 
     * объекты будут переведены в следующие состояния: для  объекта 
     * класса {@link NetworkConnection} - {@link ConnectionState#BROKEN}, 
     * для объекта класса {@link User} - {@link IrcTalkerState#BROKEN}.
     *  
     * <P>В переменной {@link #error} хранится признак ошибки, ее значение 
     * будет сохраняться равным false, до тех пор пока не будет 
     * обнаружена ошибка, после этого ее значение будет изменено на 
     * true.
     */
    public void run() {
        
        Logger logger = Globals.logger.get();
        
        long waitingTO = limitingTO.get();
        boolean isConnectionAllowed = false;
        boolean isUserAllowed = false;
        Socket socket = null;
        NetworkConnection connection = null;
        User user = null;

        logger.log(Level.FINEST, "Running");

        try {
            serverSocket = new ServerSocket();
            serverSocket.setReceiveBufferSize(
                    Globals.receiveBufferSize.get());
            serverSocket.setSoTimeout(soTimeout.get());
            SocketAddress serverSocketAddress = 
                    new InetSocketAddress(
                    inetAddress, 
                    serverPortNumber);
            serverSocket.bind(serverSocketAddress);   
            
            Globals.serverSocket.set(serverSocket);
            logger.log(Level.INFO, "ServerSocked: " + serverSocket + 
                " is opened." + " ReceiveBufferSize:" + 
                serverSocket.getReceiveBufferSize() +
                "," + " SO Timeout (ms):" + serverSocket.getSoTimeout() +
                "," + " Charset:" + listenerCharset.get());

        } catch (IOException e) {
            logger.log(Level.SEVERE, "ServerSocket: " +
                    serverPortNumber + ". Opening error." + " " + e);
            error.set(true);
        }

        while (!down.get() && !error.get()) {

            if (!running.get() && !down.get()) {
                try {
                    Thread.sleep(sleepTO.get());
                } catch (InterruptedException e) {}
                continue;
            }

            if (down.get()) {
                break;
            }

            waitingTO = limitingTO.get();

            try {
                socket = serverSocket.accept();
                if (!Globals.ircServerProcessorSet.get().isEmpty()) {
                    if (socket != null) {
                        socket.close();
                    }
                    try {
                        Thread.sleep(sleepTO.get());
                    } catch (InterruptedException e) {}
                    continue;
                }                
                connection = NetworkConnection.create(socket);
                if (connection != null) {
                    connection.getSocket().setSoTimeout(
                            connection.soTimeout.get());
                    connection.getSocket().setReceiveBufferSize(
                            Globals.receiveBufferSize.get());
                    connection.charset.set(listenerCharset.get());

                    isConnectionAllowed = 
                            Globals.db.get().register(connection) ==
                            Reply.RPL_OK;
                } else {
                    isConnectionAllowed = false;
                }

                if (isConnectionAllowed) {
                    user = User.create();
                    if (user != null) {
                        user.setConnection(connection);
                        user.setNickname(user.getIdString());
                        user.setIrcServer(Globals.thisIrcServer.get());
                        connection.ircTalker.set(user);

                        isUserAllowed = Globals.db.get().register(user) ==
                                Reply.RPL_OK;
                        if (!isUserAllowed) {
                            user.setConnection(null);
                            connection.ircTalker.set(null);
                        }
                    } else {
                        isUserAllowed = false;
                    }
                }

                if (isConnectionAllowed && isUserAllowed) {
                    
                    user.setState(IrcTalkerState.REGISTERING);
                    connection.run();
                    /*
                    logger.log(Level.FINEST, "Connection for " +
                            socket + " accepted." + " ReceiveBufferSize:" 
                            + socket.getReceiveBufferSize() +
                            "," + " SO Timeout (ms):" + socket.getSoTimeout() +
                            "," + " Charset:" + connection.charset.get());
                    */
                    
                } else {
                    logger.log(Level.INFO, "Connection for " + socket + 
                            " rejected.");
                    if (connection != null) {
                        connection.delete();
                    }
                    waitingTO = sleepTO.get();
                }
            } catch (SocketTimeoutException e) {
                
            } catch (IOException e) {
                //error.set(true);
                waitingTO = sleepTO.get();
                logger.log(Level.WARNING, "ServerSocked: " +
                        serverSocket + ". Accepting error." + " " + e);
            }
            try {
                Thread.sleep(waitingTO);
            } catch (InterruptedException e) {}
        }
        
        try {
            if (serverSocket != null) {
                serverSocket.close();
            }
            logger.log(Level.FINEST, "ServerSocket:" +
                    serverSocket + " is closed.");
        } catch (IOException e) {
            logger.log(Level.WARNING, "ServerSocket:" +
                    serverPortNumber + ". Closing error. " + e);
        }
        logger.log(Level.FINEST, "Ended");
    }
}
