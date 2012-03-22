import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/*
 * 
 * IrcTranscriptConfig 
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

/**
 * Класс, хранящий конфигурируемые параметры для протоколирования 
 * сообщений клиентов. 
 *
 * @version 0.5 2012-03-10
 * @author  Nikolay Kirdin
 */
public class IrcTranscriptConfig {
    
    /** Время последней записи протокола на внешний носитель. */
    private long writingTime; 
    
    /** Путь к файлу-протоколу клиентских сообщений. */
    private String transcript;

    /** Количество ротаций файла-протокола клиентских сообщений. */
    private int rotate;
    
    /** Максимальная длина файла-протокола клиентских сообщений. */
    private long length;
        
    /** Очередь сообщений клиентов. */
    private BlockingQueue<String> transcriptQueue = 
            new ArrayBlockingQueue<String>(
                    Globals.maxTranscriptQueueSize);

    /**
     * Конструктор.
     * @param transcript путь к файлу-протоколу клиентских сообщений.
     * @param rotate количество ротаций.
     * @param length длина файла-протокола клиентских сообщений..
     */
    public IrcTranscriptConfig(String transcript, int rotate, 
            int length) {
        this.transcript = transcript;
        this.setRotate(rotate);
        this.setLength(length);
    }

    /**
     * Получение пути к файлу-протоколу клиентских сообщений.
     * @return the transcript
     */
    public String getTranscript() {
        return transcript;
    }

    /**
     * Задание пути к файлу-протоколу клиентских сообщений.
     * @param transcript
     */
    public void setTranscript(String transcript) {
        this.transcript = transcript;
    }

    /**
     * Получение максимального количества ротаций файла-протокола.
     * @return максимальное количество ротаций файла-протокола.
     */
    public int getRotate() {
        return rotate;
    }

    /**
     * Задание максимального количества ротаций файла-протокола.
     * @param rotate максимальное количество ротаций файла-протокола.
     */
    public void setRotate(int rotate) {
        this.rotate = rotate;
    }

    /**
     * Получение максимальной длины файла-протокола.
     * @return максимальная длина файла-протокола.
     */
    public long getLength() {
        return length;
    }

    /**
     * Задание максимальной длины файла-протокола.
     * @param length максимальная длина файла-протокола.
     */
    public void setLength(long length) {
        this.length = length;
    }

    /**
     * Получение времени последней операции записи в файл-протокол.
     * @return время последней операции записи в файл-протокол.
     */
    public long getWritingTime() {
        return writingTime;
    }

    /**
     * Задание времени последней операции записи в файл-протокол.
     * @param writingTime время последней операции записи в файл-протокол.
     */
    public void setWritingTime(long writingTime) {
        this.writingTime = writingTime;
    }

    /** 
     * Получение очереди сообщений.
     * @return очередь сообщений.
     */
    public BlockingQueue<String> getTranscriptQueue() {
        return transcriptQueue;
    }
    
    /**
     * Запись сообщения в очередь.
     * @param record сообщение.
     */
    public boolean offerToQueue(String record) {
        return transcriptQueue.offer(record);
        
    }
    
    /** 
     * Получение сообщения из очереди.
     * @return сообщение.
     */
    public String pollFromQueue() {
        return transcriptQueue.poll();        
    }

    /** Получение длины очереди.
     * @return длина очереди.
     */
    public int getQueueSize() {
        return transcriptQueue.size();
    }
    
}
