/*
 * 
 * TranscriptFileProcessor 
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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;

/**
 * Класс, который обслуживает файл-протокол сообщений 
 * клиентов {@link IrcTranscriptConfig#transcript}. 
 * 
 * @version 0.5.1 2012-03-27
 * @author  Nikolay Kirdin
 */
public class TranscriptFileProcessor implements Runnable,
        IrcServerProcessor {
    /** 
     * Управление выполнением/остановом основного цикла.
     * true - цикл выполняется, false - цикл приостановлен. 
     */ 
    public AtomicBoolean running = new AtomicBoolean(true);
    
    /** 
     * Управление выполнением/завершением основного цикла.
     * true - цикл завершается, false - цикл может выполнятся.
     */ 
    public AtomicBoolean down = new AtomicBoolean(false);
    
    /** Поток метода run этого объекта. */ 
    public AtomicReference<Thread> thread = 
            new AtomicReference<Thread>();
    
    /** Стандартная длительность таймаутов. */
    public AtomicLong sleepTO = new AtomicLong(100);
    
    /** Минимальная длительность таймаута основного цикла. */
    public AtomicLong limitingTO = new AtomicLong(100);
    
    /** 
     * Планируемая длительность одного цикла. Определяет нижнюю границу
     * максимальной длительности выполнения цикла, она включает в себя 
     * таймаут цикла.
     */
    public AtomicLong plannedDuration = new AtomicLong(400);
    
    
    /** Хранилище параметров вывода файла протокола. */
    private IrcTranscriptConfig ircTranscriptConfig;
    
    /** Признак высокой загруженности процессора. */
    private boolean highLoad;    

    /** Основной конструктор. */
    public TranscriptFileProcessor() {}

    /**
     * Метод удаляет ротированные файлы начиная с файла с суффиксом 
     * {@link IrcTranscriptConfig#rotate}, до тех пор пока суммарный 
     * объем занимаемый удаляемыми файлами остается меньше  
     * neededSpace, файл без суффикса не удаляется. Файлы должны быть 
     * закрыты, и должно быть разрешено их удаление. Если файл удалить
     * не удается, то этот факт заносится в журнал.
     * @param neededSpace объем который нужно освободить.
     * @return  суммарный объем удаленных файлов.  
     */
    public long reduceRotatedFiles(long neededSpace) {
        long usedSpace = 0;
        String filename = ircTranscriptConfig.getTranscript();
        int rotate = ircTranscriptConfig.getRotate();
        
        for (int i = rotate; i > 0 && usedSpace < neededSpace; i--) {
            File transcriptFile = new File(filename + "." + i);
            if (!transcriptFile.exists()) {
                continue;
            }
            
            boolean isDeleted = transcriptFile.delete();
            if (isDeleted) {
                usedSpace += transcriptFile.length();
            } else {
                Globals.logger.get().log(Level.WARNING, "File: " + 
                        transcriptFile + " was not deleted." + 
                        " Check permissions.");
            }
        }
        return usedSpace;
    }

    /** 
     * Вычисляется фактический ограничитель длины (в байтах) 
     * файла-протокола. Проверяется доступная дисковая емкость, если 
     * доступная емкость больше чем 
     * 3 * {@link IrcTranscriptConfig#length}, то в качестве граничного 
     * значения используется величина, переданная в качестве параметра,
     * в противном случае в качестве граничного значения используется
     * {@link IrcTranscriptConfig#length} / 4.
     * @param length предполагаемая длина файла-протокола.
     * @return актуальная длина, которую необходимо использовать.
     */
    public long makeUsingLength(long length) {
        long usingLength;
        String filename = ircTranscriptConfig.getTranscript();
        File transcriptFile = new File(filename);
        long usableSpace = transcriptFile.getUsableSpace();
        if (length * 3 >= usableSpace) {
            usingLength = usableSpace / 4;
        } else {
            usingLength = length;
        }
        return usingLength;
    }
    
    /** 
     * Выполнение ротации файлов. Начиная с файла с суффиксом 
     * {@link IrcTranscriptConfig#rotate} - 1, файлы переименовываюся 
     * путем увеличения на единицу значения  суффикса. Файл без суффикса
     * переименуется в вариант с суффиксом равным 1. Создается 
     * новый файл с именем     {@link IrcTranscriptConfig#transcript}, он 
     * начинает использоваться в качестве файла-протокола.
     * @return true ротация всех файлов проведена успешно, false -
     * некоторые файлы переименовать не удалось или не удалось создать 
     * новый файл протокол.
     */
    public boolean rotateFile() {
        boolean result = true;
        boolean isRenamed = false;
        boolean isCreated = false;
        File transcriptFile = null;
        File newTranscriptFile = null;
        String filename = ircTranscriptConfig.getTranscript();
        int rotate = ircTranscriptConfig.getRotate();
        
        for (int i = rotate - 1; i >= 0; i--) {
            if (i > 0) {
                transcriptFile = new File(filename + "." + i);
            } else {
                transcriptFile = new File(filename);
            }
            newTranscriptFile = new File(filename + "." + (i + 1));
            if (!transcriptFile.exists()) {
                continue;
            }
            if (newTranscriptFile.exists()) {
                newTranscriptFile.delete();
            }
            isRenamed = transcriptFile.renameTo(newTranscriptFile);            
            if (!isRenamed) {
                Globals.logger.get().log(Level.WARNING, "File: " + 
                        transcriptFile + " was not renamed." + 
                        " Check permissions.");
                result = false;
            }
        }
        try {
            isCreated = transcriptFile.createNewFile();
        } catch (IOException e) {
            Globals.logger.get().log(Level.WARNING, "File: " + 
                    transcriptFile + " was not created. " + e);
            result = false;            
        }
        return result && isCreated;
    }
    
    
    /**
     * Получение объекта-хранилища параметров файла-протокола.
     * @return объект-хранилище параметров файла-протокола.
     */
    public IrcTranscriptConfig getIrcTranscriptConfig() {
        return ircTranscriptConfig;
    }

    /**
     * Задание объекта-хранилища параметров файла-протокола.
     * @param ircTranscriptConfig объект-хранилище параметров 
     * файла-протокола.
     */
    public void setIrcTranscriptConfig(
            IrcTranscriptConfig ircTranscriptConfig) {
        
        IrcTranscriptConfig previousIrcTranscriptConfig = null;
        
        if (this.ircTranscriptConfig != null) {
            previousIrcTranscriptConfig = this.ircTranscriptConfig;
            flushQueue();
        }
        
        this.ircTranscriptConfig = ircTranscriptConfig;
        
        if (previousIrcTranscriptConfig != null) {
            String message = null;
            while ((message = previousIrcTranscriptConfig.pollFromQueue())
                    != null) {
                this.ircTranscriptConfig.offerToQueue(message);
            }
        }
    }

    /** Запись содержимого очереди сообщений в файл-протокол. */
    public void flushQueue() {
        BufferedWriter bw = null;
        String transcript = ircTranscriptConfig.getTranscript();
        String outputString = "";
        try {
            bw = new BufferedWriter(new FileWriter(transcript, true));
            if (ircTranscriptConfig.getQueueSize() == 
                    Globals.maxTranscriptQueueSize) {
                outputString = System.currentTimeMillis() + " " + "-1 " +
                    "Some messages may be lost.";
                    Globals.logger.get().log(Level.WARNING, outputString);
                bw.write(outputString);
            }
            while ((outputString = ircTranscriptConfig.pollFromQueue())
                    != null) {
                bw.write(outputString);
                bw.newLine();
            }
            
        } catch (IOException e) {
            Globals.logger.get().log(Level.WARNING, "File: " + 
                    transcript + " was not opened for write." + 
                    e);
        } finally {
            try {
                if (bw != null) {
                    bw.close();
                }
            } catch (IOException e) {}
        }
    }

    /**
     * Метод, который обслуживает вывод в файл-протокол сообщений 
     * клиентов.     
     * 
     * <P>В основном цикле этого метода проверяется состояние 
     * файла-протокола и списка-протокола с сообщениями клиентов.  
     *
     * После проверки файлов будет выполенена запись команд клиентов, 
     * которые хранятся в очереди 
     * {@link IrcTranscriptConfig#transcriptQueue}.
     * Запись будет выполнятся в при выполнении одного из следующих 
     * условий:
     * <UL>
     *         <LI> количество элементов очереди более чем 
     *         {@link Globals#maxTranscriptQueueSize} / 2;</LI>
     *         <LI> с момента последней записи прошло более 
     *         {@link Globals#transcriptWritePeriod} ms.</LI>
     * </UL> 
     * Факты возникновения исключений фиксируются в журнальном файле, 
     * выполнение метода продолжается. 
     *
     * <P> С помощью средней длительности основного цикла, 
     * запланированной длительности основного цикла, минимальной 
     * продолжительности таймаута и средней продолжительности 
     * таймаута определяется степень нагруженности этого программного 
     * процессора. При выполнении любого из следующих условий этот 
     * программный процессор признается сильно нагруженным:
     * <OL>
     *     <LI>Средняя длительность основного цикла составляе более 70% от
     *  запланированной длительности основного цикла.</LI>
     *  <LI>Средняя продолжительность таймаута на 10% больше 
     *  вычисленной продолжительности таймаута.</LI> 
     * </OL>
     * Режим высокой нагруженности сбрасывается при выполнении всех 
     * следующих условий:
     * <OL>
     *     <LI>Средняя длительность основного цикла на 50%  меньше
     *  запланированной длительности основного цикла более.</LI>
     *  <LI>Средняя продолжительность таймаута меньше или равна 
     *  вычисленной продолжительности таймаута.</LI> 
     * </OL> 
     * <P> Каждые {@link Globals#monitoringPeriod} (ms) в канал 
     * {@link Globals#monitorIrcChannel} выводятся диагностические 
     * сообщения содержащие следующую информацию:
     * <UL>
     *         <LI>среднее время (ms) выполнения основного цикла 
     *         (без таймаута);</LI>
     *         <LI>средняя длительность (ms) таймаута;</LI>
     *         <LI>средняя планируемая длительность (ms) таймаутов.</LI>
     * </UL>
     */
    public void run() {
        
        int procTimeLength = 100;
        long avgWorkingTime = 0;
        long waitingTO = limitingTO.get();
        long startMonitorTime = System.currentTimeMillis();
        IrcAvgMeter avgWork = new IrcAvgMeter(procTimeLength);

        IrcAvgMeter avgWaitingTO = new IrcAvgMeter(procTimeLength);
        avgWaitingTO.setValue(waitingTO);
        
        long avgActualTO = 0;
        IrcAvgMeter avgTO = new IrcAvgMeter(procTimeLength);
        avgTO.setValue(limitingTO.get());
                
        Globals.logger.get().log(Level.FINEST, "Running");

        while (!down.get()) {
            
            avgWork.intervalStart(System.currentTimeMillis());
            while (!running.get() && !down.get()) {
                try {
                    Thread.sleep(sleepTO.get());
                } catch (InterruptedException e) {}
                avgWork.intervalStart(System.currentTimeMillis());
            }

            if (down.get()) {
                break;
            }
            String filename = ircTranscriptConfig.getTranscript();
            long length = ircTranscriptConfig.getLength();
            File transcriptFile = new File(filename);
            long usingLength = makeUsingLength(length);
            
            if (transcriptFile.getUsableSpace() < 2 * usingLength) { 
                reduceRotatedFiles(2 * usingLength);
            }

            if (transcriptFile.length() >= makeUsingLength(length)) {
                rotateFile();
            }
            
            long lastWritingTime = ircTranscriptConfig.getWritingTime();
            long currentTime = System.currentTimeMillis();
            int queueSize = ircTranscriptConfig.getQueueSize();
            
            if ((currentTime - lastWritingTime >= 
                    Globals.transcriptWritePeriod.get()) || 
                    (queueSize >= Globals.maxTranscriptQueueSize / 2)) { 
                ircTranscriptConfig.setWritingTime(currentTime);
                flushQueue();
            }
            
            avgWork.intervalEnd(System.currentTimeMillis());
            
            avgActualTO = avgTO.getAvgValue();
            avgTO.intervalStart(System.currentTimeMillis());
            avgWorkingTime = avgWork.getAvgValue();
            
            waitingTO = Math.max(limitingTO.get(), Math.min(
                plannedDuration.get(),
                plannedDuration.get() - avgWorkingTime));
                
            avgWaitingTO.setValue(waitingTO);
            
            long awto = avgWaitingTO.getAvgValue();
            
            if (!highLoad && (
                    (avgWorkingTime * 7 >= plannedDuration.get() * 10) ||
                    (avgActualTO * 10 > waitingTO * 11))) {
                highLoad = true;
                Globals.logger.get().log(Level.INFO, "Set highLoad." + 
                " avgWorkingTime (ms):" + avgWorkingTime +
                " plannedDuration (ms):" + plannedDuration.get() +
                " avgActualTO (ms):" + avgActualTO +
                " avgWaitingTO (ms):" + awto);
                
                /** Индикация высокой загруженности. */
                Globals.ircServerProcessorSet.get().add(this);
                
            } else if (highLoad && 
                    (avgWorkingTime * 2 <= plannedDuration.get()) &&
                    (avgActualTO <= waitingTO)) {
                
                highLoad = false;
                
                Globals.logger.get().log(Level.INFO, "UnSet highLoad." + 
                " avgWorkingTime (ms):" + avgWorkingTime +
                " plannedDuration (ms):" + plannedDuration.get() +
                " avgActualTO (ms):" + avgActualTO +
                " avgWaitingTO (ms):" + awto);
                
                /** Сброс индикации высокой загруженности. */
                for (IrcServerProcessor isp: 
                    Globals.ircServerProcessorSet.get()) {
                    if (isp instanceof TranscriptFileProcessor) {
                        Globals.ircServerProcessorSet.get().remove(isp);
                        break;
                    }
                }
            }

            if ((System.currentTimeMillis() - startMonitorTime) >=
                    Globals.monitoringPeriod.get()) {
                String monitoringString = "TranscriptFileProcessor:" +  
                        " avgWorkingTime (ms):" + avgWorkingTime +
                        " avgActualTO (ms):" + avgActualTO +
                        " avgWaitingTO (ms):" + awto;
                
                Globals.logger.get().log(Level.FINEST, monitoringString);  
                
                if (Globals.monitorIrcChannel.get() != null ) {
                    List<String> targetList = new ArrayList<String>();
                    String channelname = 
                            Globals.monitorIrcChannel.get().getNickname();
                    targetList.add(channelname);
                    NoticeIrcCommand.create(Globals.db.get(), 
                            Globals.anonymousUser.get(), 
                            targetList,
                            monitoringString).run();
                }
                startMonitorTime = System.currentTimeMillis();
            }

            try {
                Thread.sleep(waitingTO);
            } catch (InterruptedException e) {}
            avgTO.intervalEnd(System.currentTimeMillis());
        }
        
        Globals.logger.get().log(Level.FINEST, "Ended");
    }
}
