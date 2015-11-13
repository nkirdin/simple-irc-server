/*
 * 
 * AbstractIrcServerProcessor 
 * is part of Simple Irc Server
 *
 *
 * Copyright (С) 2015, Nikolay Kirdin
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

package simpleircserver.processor;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;

import simpleircserver.base.Globals;

/**
 * Server - класс, который служит для управления запуском, остановом, и 
 * перезапуском основных компонентов сервера IRC. 
 *
 * @version 0.5.4 2015-11-13
 * @author  Nikolay Kirdin
 */
public abstract class AbstractIrcServerProcessor implements IrcServerProcessor, Runnable {
    
    /** 
     * Управление выполнением/остановом основного цикла.
     * true - цикл выполняется, false - цикл приостановлен. 
     */ 
    public AtomicBoolean running = new AtomicBoolean(true);

    /** 
     * Управление выполнением/завершением основного цикла.
     * true - цикл завершается, false - цикл может выполнятся.
     */ 
    public AtomicBoolean down = new AtomicBoolean();
        
    /** Поток метода run этого объекта. */ 
    public AtomicReference<Thread> thread = new AtomicReference<Thread>();

    /** 
     * Инициализация процесса обработки ссобщений клиентов. 
     * @return true инициализация успешно завершена, 
     * false инициализация завершена с ошибками. 
     */
    public boolean processorStart() {
        boolean error = false;
        thread.set(new Thread(this));        
        running.set(true);
        thread.get().start();
        Globals.logger.get().log(Level.INFO, getClass().getName()+ ": " + thread.get());
        
        try {
            Thread.sleep(Globals.sleepTO.get());
        } catch (InterruptedException e) {}
        
        error = thread.get().getState() == Thread.State.NEW
                || thread.get().getState() == Thread.State.TERMINATED;
                
        return error;
    }

    
    /** 
     * Останов потока. 
     * @param thread поток, который необходимо остановить.
     */
    public void stopProcess(Thread thread) {
        
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
    
    /** Завершение процесса вывода сообщений в файл-протокол.*/
    public void processorStop() {
        if (thread.get() != null) {
            down.set(true);
            stopProcess(thread.get());
        }
    }

}
