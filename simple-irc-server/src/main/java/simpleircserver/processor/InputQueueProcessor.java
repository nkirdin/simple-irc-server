package simpleircserver.processor;
/*
 * 
 * InputQueueProcessor 
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

import java.util.Arrays;
import java.util.Iterator;
import java.util.logging.Level;

import simpleircserver.base.Globals;
import simpleircserver.connection.Connection;
import simpleircserver.connection.ConnectionState;
import simpleircserver.parser.IrcCommandParser;
import simpleircserver.parser.IrcIncomingMessage;
import simpleircserver.parser.commands.IrcCommandBase;
import simpleircserver.talker.IrcTalker;
import simpleircserver.talker.server.IrcServer;
import simpleircserver.talker.service.Service;
import simpleircserver.talker.user.User;
import simpleircserver.tools.IrcAvgMeter;

/**
 * InputQueueProcessor - программный процессор, который просматривает входные
 * очереди сетевого соединения. В основном цикле метода {@link #run}
 * просматриваются входные очереди и из них извлекаются сообщения клиентов.
 * Затем эти сообщения передаются на исполнение интерпретатору команд IRC.
 *
 * @version 0.5 2012-02-13
 * @version 0.5.3 2015-11-05 Program units were moved from default package into
 *          packages with names. Unit tests were added.
 * @author Nikolay Kirdin
 */
public class InputQueueProcessor extends AbstractIrcServerProcessor {

    private IrcAvgMeter avgMessageExecutionDuration = new IrcAvgMeter(STATISTIC_DATA_LENGTH_DEFAULT);

    private IrcCommandParser icp = new IrcCommandParser();

    {
        minimalDurationOfTimeout.set(20);

        plannedDurationOfCycle.set(80);
    }

    /** Конструктор по умолчанию. */
    public InputQueueProcessor() {
    }

    /**
     * Метод, просматривающий входные очереди сетевых соединений.
     *
     * <P>
     * В основном цикле этого метода производится просмотр входных очередей
     * сетевых соединений. Если во входной очереди будет находится сообщение
     * клиента, то это сообщение будет извлечено из очереди и передано
     * интерпретатору команд IRC.
     *
     * <P>
     * Интерпретация команд производится с помощью метода
     * {@link IrcCommandParser#ircParse}.
     *
     * <P>
     * Если во время выполнения команды будет вызвано исключение, то этот факт
     * будет занесет в журнал, выполнение будет остановлено.
     * 
     */
    @Override
    public void performProcessorOperation() {

        Iterator<Connection> connectionListIterator = Globals.db.get().getConnectionListIterator();

        while (connectionListIterator.hasNext()) {

            Connection connection = connectionListIterator.next();
            if (connection.getConnectionState() != ConnectionState.OPERATIONAL) {
                continue;
            }

            IrcIncomingMessage ircIncomingMessage = connection.inputQueue.getAndSet(null);

            if (ircIncomingMessage == null) {
                continue;
            }

            avgMessageExecutionDuration.intervalStart(System.nanoTime());

            try {
                icp.setIncomingMessage(ircIncomingMessage);
                icp.ircParse();
            } catch (Throwable e) {

                IrcServer ircServer = null;
                IrcTalker ircTalker = connection.ircTalker.get();
                long id = ircIncomingMessage.id;
                String message = ircIncomingMessage.message;

                if (ircTalker instanceof User) {
                    ircServer = ((User) ircTalker).getIrcServer();
                } else if (ircTalker instanceof Service) {
                    ircServer = ((Service) ircTalker).getIrcServer();
                }

                logger.log(Level.SEVERE,
                        "Exception. ircCommandReport id: " + id + " Connection: " + connection + " Requestor: "
                                + ircTalker + " " + ircTalker.getHostname() + " " + ircTalker.getNetworkId() + " "
                                + ircServer);
                logger.log(Level.SEVERE,
                        "Exception. ircCommandReport id: " + id + " " + e + " Message: " + e.getMessage());
                logger.log(Level.SEVERE,
                        "Exception. ircCommandReport id: " + id + " StackTrace: " + Arrays.toString(e.getStackTrace()));
                logger.log(Level.SEVERE, "Exception. ircCommandReport id: " + id + " Parsing String: " + message);
                ircTalker.send(IrcCommandBase.errFileError(ircTalker, String.valueOf(id), message));

                /** Останов сервера */
                System.err.println("Server stopped: Internal error: " + e);
                down.set(true);
                Globals.serverDown.set(true);
            }
            avgMessageExecutionDuration.intervalEnd(System.nanoTime());
        }

    }

    /** Сброс индикации высокой загруженности. */
    @Override
    public void removeProcessorFromHighLoadSet() {
        for (IrcServerProcessor isp : Globals.ircServerProcessorSet.get()) {
            if (isp instanceof InputQueueProcessor) {
                Globals.ircServerProcessorSet.get().remove(isp);
                break;
            }
        }
    }

    @Override
    public String getMonitoringstring() {
        String result = " avgMessageExecutionDuration (ns): " + avgMessageExecutionDuration.getAvgValue();
        return result;
    }

}
