package simpleircserver.processor;
/*
 * 
 * IrcTalkerProcessor 
 * is part of Simple Irc Server
 *
 *
 * Copyright (С) 2012, 2015, Nikolay Kirdin
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

import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import simpleircserver.base.Globals;
import simpleircserver.connection.Connection;
import simpleircserver.connection.ConnectionState;
import simpleircserver.parser.commands.QuitIrcCommand;
import simpleircserver.talker.IrcTalker;
import simpleircserver.talker.IrcTalkerState;
import simpleircserver.talker.server.IrcServer;
import simpleircserver.talker.service.Service;
import simpleircserver.talker.user.User;

/**
 * Программный процессор, который обследует объекты, хранящие информацию о
 * клиентах сервера. Он проверяет их состояние и состояние соответствующего
 * соединения. В случае обнаружения ошибочного состояния или этапа завершения
 * жизненного цикла этого объекта, будут выполнены завершающие действия для
 * этого объекта.
 *
 * @version 0.5 2012-02-12
 * @version 0.5.3 2015-11-05 Program units were moved from default package into
 *          packages with names. Unit tests were added.
 * @author Nikolay Kirdin
 */
public class IrcTalkerProcessor extends AbstractIrcServerProcessor {

    /** Конструктор. */
    public IrcTalkerProcessor() {
    }

    /**
     * Метод, который обследует объекты классов User, Service и IrcServer.
     * 
     * <P>
     * В основном цикле этого метода проверяется состояние объекта и
     * соответствующего соединения. В том случае, если объект должен завершить
     * свой жизненый цикл или соединение завершает или завершило свой жизненый
     * цикл, будут проведены завершающие действия для обследуемого объекта.
     * Состояние объекта определяется полем {@link IrcTalker#state}. Состояние
     * соединения определяется полем {@link Connection#connectionState}.
     *
     * <P>
     * В том случае, если значение поля {@link IrcTalker#state} обследуемого
     * объекта будет {@link IrcTalkerState#BROKEN}, то для этого объекта будет
     * выполнена команда IRC QUIT с параметром
     * <code>"Broken state: " + (new Date()).toString()</code>.
     *
     * <P>
     * В том случае, если значение поля {@link IrcTalker#state} обследуемого
     * объекта будет {@link IrcTalkerState#CLOSE}, то этот объект будет удален
     * из репозитария и его состояние будет изменено на состояние
     * {@link IrcTalkerState#CLOSED}.
     *
     * <P>
     * Если будет обнаружено, что у обследуемого объекта нет соединения или
     * соответвующее ему соединение находится в одном из следующих состояний:
     * {@link ConnectionState#CLOSED} или {@link ConnectionState#BROKEN}, то
     * состояние этого объекта будет изменено на состояние
     * {@link IrcTalkerState#BROKEN}.
     * 
     */
    public void performProcessorOperation() {

        Iterator<User> ircTalkerSetIterator = Globals.db.get().getUserSetIterator();

        while (ircTalkerSetIterator.hasNext()) {
            IrcTalker ircTalker = ircTalkerSetIterator.next();

            Connection connection = ircTalker.getConnection();

            if (connection == null || connection.getConnectionState() == ConnectionState.CLOSED
                    || connection.getConnectionState() == ConnectionState.BROKEN) {
                ircTalker.setBroken();
            }

            IrcTalkerState ircTalkerState = ircTalker.getState();

            switch (ircTalkerState) {
            case NEW:
                break;
            case REGISTERING:
            case REGISTERED:
                break;
            case BROKEN:
                QuitIrcCommand.create(Globals.db.get(), ircTalker, "Broken state: " + (new Date()).toString()).run();
            case CLOSE:
            case CLOSING:
                ircTalker.setState(IrcTalkerState.CLOSED);
                ircTalker.stateTime.set(System.currentTimeMillis());
                if (ircTalker instanceof User) {
                    Globals.db.get().unRegister((User) ircTalker);
                } else if (ircTalker instanceof Service) {
                    Globals.db.get().unRegister((Service) ircTalker);
                } else if (ircTalker instanceof IrcServer) {
                    Globals.db.get().unRegister((IrcServer) ircTalker);
                } else {
                    logger.log(Level.SEVERE, "IrcTalkerProcessor. Internal error." + " Unknown IrcTalker:" + ircTalker);
                    System.err.println("IrcTalkerProcessor. " + "Internal error." + "Unknown IrcTalker:" + ircTalker);
                    /** Останов сервера */
                    down.set(true);
                    Globals.serverDown.set(true);
                }
                logger.log(Level.FINEST, "ircTalker:" + ircTalker + " deleted and unregistered.");
                logger.log(Level.FINEST,
                        "ircTalker:" + ircTalker + ircTalker.getConnection() + " ircTalker set CLOSED");

                break;
            case OPERATIONAL:
                break;
            case CLOSED:
                break;
            default:
                logger.log(Level.SEVERE, "IrcTalkerProcessor. Internal error." + " Unknown IrcTalker state:" + ircTalker
                        + " " + ircTalker.getState());
                System.err.println("IrcTalkerProcessor. Internal " + "error." + "Unknown IrcTalker state:" + ircTalker
                        + " " + ircTalker.getState());
                /** Останов сервера */
                down.set(true);
                Globals.serverDown.set(true);
            }
        }

    }

    /**
     * Выполнение завершающих действий при останове сервера. Для всех объектов
     * классов порожденных от {@link IrcTalker} будет выполнена команда IRC QUIT
     * с параметром <code>"Termination."</code>.
     */
    public void termination() {

        Logger logger = Globals.logger.get();

        logger.log(Level.FINEST, "Started");

        Iterator<User> ircTalkerSetIterator = null;
        ircTalkerSetIterator = Globals.db.get().getUserSetIterator();

        while (ircTalkerSetIterator.hasNext()) {
            IrcTalker ircTalker = ircTalkerSetIterator.next();
            logger.log(Level.FINER, "ircTalker:" + ircTalker + ircTalker.getConnection() + " Termination.");

            QuitIrcCommand.create(Globals.db.get(), ircTalker, "Termination.").run();
        }

        LinkedHashSet<IrcTalker> processingIrcTalkerSet = new LinkedHashSet<IrcTalker>();

        LinkedHashSet<Service> serviceSet = Globals.db.get().getServiceSet();
        processingIrcTalkerSet.addAll(serviceSet);

        LinkedHashSet<IrcServer> ircServerSet = Globals.db.get().getIrcServerSet();
        processingIrcTalkerSet.addAll(ircServerSet);

        processingIrcTalkerSet.remove(Globals.thisIrcServer.get());

        for (IrcTalker ircTalker : processingIrcTalkerSet) {
            logger.log(Level.FINER, "ircTalker:" + ircTalker + ircTalker.getConnection() + " Termination.");

            QuitIrcCommand.create(Globals.db.get(), ircTalker, "Termination.").run();
        }

        logger.log(Level.FINEST, "Ended");
    }

    /**
     * Завершение основного цикла.
     * 
     * @return true - действия успешно выполнены.
     */
    @Override
    public boolean processorStop() {
        boolean result = super.processorStop();
        termination();
        return result;
    }

    /** Сброс индикации высокой загруженности. */
    @Override
    public void removeProcessorFromHighLoadSet() {
        for (IrcServerProcessor isp : Globals.ircServerProcessorSet.get()) {
            if (isp instanceof IrcTalkerProcessor) {
                Globals.ircServerProcessorSet.get().remove(isp);
                break;
            }
        }
    }

}
