/*
 * 
 * NetworkConnection 
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
import java.util.concurrent.atomic.*;
import java.net.*;
import java.io.*;

/**
 * Класс, хранящий информацию о сетевом соединении.
 *
 * @version 0.5 2012-02-13
 * @author  Nikolay Kirdin
 */
public class NetworkConnection extends Connection implements Runnable {

    /** Сокет. */
    public final Socket socket;

    /** Таймаут сокета. */
    public AtomicInteger soTimeout = new AtomicInteger(1);
    
    /** true - признак успешной инициализации BufferedReader.*/
    private AtomicBoolean isBufferedReaderOK = new AtomicBoolean();

    /** true - признак успешной инициализации BufferedWriter.*/
    private AtomicBoolean isBufferedWriterOK = new AtomicBoolean();
    
    /** 
     * Конструктор.
     * @param socket сокет.
     */
    private NetworkConnection(Socket socket) {
        this.socket = socket;
    }

    /**
     * Создатель объекта без параметров. Проверяется объем свободной 
     * памяти, если этот объем меньше {@link Constants#MIN_FREE_MEMORY}, 
     * то будет вызван сборщик мусора. Если после прцедуры сборки мусора, 
     * памяти будет недостаточно, то объект создаваться не будет.
     * @return новый объект класса IrcServer.
     */
    public static NetworkConnection create(Socket socket) {
        NetworkConnection result = null;
        long freeMemory = Runtime.getRuntime().freeMemory();
        if (freeMemory < Constants.MIN_FREE_MEMORY) {
            System.gc();
        }
        freeMemory = Runtime.getRuntime().freeMemory();
        if (freeMemory >= Constants.MIN_FREE_MEMORY) {
            result = new NetworkConnection(socket);
        } else {
            Globals.logger.get().log(Level.SEVERE, 
                    "Insufficient free memory (b): " + freeMemory);
        }
        return result;
    }
    
    /**
     * Получение сокета.
     * @return сокет.
     */
    public Socket getSocket() {
        return socket;
    }

    /**
     * Текстовое представление объекта. Объект представляется в следующем
     * виде:
     * <P> <code>"<"Connection-"><уникальный идентификатор><" :"> 
     * <информация о сокете>"</code>
     */
    public String toString() {
        return String.format("Connection-%09d: %s", getId(), 
                socket.toString());
    }

    /** Завершающие действия при разрыве связи. Закрытие сокета. */
    public void delete() {
        try {
            if (socket != null) {
                synchronized (socket) {
                    socket.close();
                }
            }
            Globals.logger.get().log(Level.FINEST, "NetworkConnection:" +
                    NetworkConnection.this + " socket closed");
        } catch (IOException e) {
            Globals.logger.get().log(Level.INFO, "NetworkConnection:" +
                    NetworkConnection.this + " socket closing error:" + 
                    " " + e);
        }
    }

    /**
     * Действия по инциализации сетевого соединения. Открытие 
     * буферированных потоков ввода/вывода. Получение доменного имени 
     * клиента.
     */ 
    public void run() {
        
        ircTalker.get().setHostname(
                socket.getInetAddress().getHostAddress());

        Thread bufferedReaderThread = new Thread(new Runnable() {
                    public void run() {
                        if (getConnectionState() == ConnectionState.NEW) {
                            try {
                                InputStream is = 
                                        getSocket().getInputStream();
                                br.set(new BufferedReader(
                                        new InputStreamReader(is, 
                                        charset.get())));
                                isBufferedReaderOK.set(true);
                            } catch (IOException e) {}
                        }
                    }
                }
                                                );

        Thread bufferedWriterThread = new Thread(new Runnable() {
                    public void run() {
                        if (getConnectionState() == ConnectionState.NEW) {
                            try {
                                OutputStream os = 
                                        getSocket().getOutputStream();
                                bw.set(new BufferedWriter(
                                        new OutputStreamWriter(os, 
                                        charset.get())));
                                synchronized (bw.get()) {
                                    bw.get().write(":" +
                                            Globals.thisIrcServer.get(
                                                    ).getHostname() +
                                            " " + "020" + " " + "*" + 
                                            " " + ":Please wait while " +
                                            "we process your connection.");
                                    bw.get().newLine();
                                    bw.get().flush();
                                }
                                isBufferedWriterOK.set(true);
                                writeCountDelta.getAndIncrement();
                            } catch (IOException e) {}
                        }
                    }
                }
                                                );

        Thread nameResolvingThread = new Thread(new Runnable() {
                    public void run() {
                        
                        String hostname = socket.getInetAddress(
                                ).getCanonicalHostName();
                        if (hostname != null && hostname.length() <= 
                                Constants.MAX_HOSTNAME_LENGTH) {
                             ircTalker.get().setHostname(hostname);
                        }
                        Globals.logger.get().log(Level.FINEST,
                                "NetworkConnection:" +
                                NetworkConnection.this +
                                " Host:" + ircTalker.get().getHostname());

                    }
                });

        try {

            bufferedReaderThread.start();
            bufferedWriterThread.start();
            nameResolvingThread.start();

            try {
                bufferedReaderThread.join();
                bufferedWriterThread.join();
            } catch (InterruptedException e) {
                throw new IOException(e);
            }

            if (isBufferedReaderOK.get() && isBufferedWriterOK.get() &&
                getConnectionState() == ConnectionState.NEW) {
                setConnectionState(ConnectionState.OPERATIONAL);
                Globals.logger.get().log(Level.FINEST, 
                        "NetworkConnection:" +
                        NetworkConnection.this + " " + 
                        getConnectionState() 
                        + " Socket streams opened");
            } else {
                nameResolvingThread.interrupt();
                throw new IOException("NetworkConnection:" +
                        NetworkConnection.this + " init error");
            }

        } catch (IOException e) {
            setConnectionState(ConnectionState.BROKEN);
            Globals.logger.get().log(Level.INFO, "NetworkConnection:" +
                    NetworkConnection.this + " Socket streams opening" + 
                    " " + e);
        }
    }
}
