package simpleircserver.tests;

import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.TimeZone;
import java.util.logging.Level;

import simpleircserver.base.Constants;
import simpleircserver.base.Globals;
import simpleircserver.connection.NullConnection;
import simpleircserver.processor.IrcServerProcessor;

public class ServerTestUtils {
    public static void restoreGlobals() {
        /** Объект класса {@link IrcServer}, описывающий данный сервер. */
        Globals.thisIrcServer.set(null);

        /** Краткая информация о данном сервере. */
        Globals.thisIrcServerInfo.set(Constants.SERVER_INFO);

        /**
         * Объект класса {@link IrcServer}, описывающий сервер для
         * псевдопользователя anonymous.
         */
        Globals.anonymousIrcServer.set(null);

        /** Краткая информация о сервере для псевдопользователя anonymous. */
        Globals.anonymousIrcServerInfo.set(Constants.ANONYMOUS_SERVER_INFO);

        /**
         * Объект класса {@link MonitorIrcChannel}, описывающий канал для
         * диагностических сообщений сервера.
         */
        Globals.monitorIrcChannel.set(null);

        /** Объект журналирующей подсистемы. */
        Globals.logger.set(null);

        /** Имя файла для журналирующей подсистемы по умолчанию. */
        Globals.logFileHandlerFileName.set(Constants.LOG_FILE_PATH);

        /** Уровень сообщений журналирующей подсистемы. */
        Globals.fileLogLevel.set(Level.parse(Constants.LOG_LEVEL));

        /** Объект журналирующей системы, управляющий выводом в файл. */
        Globals.logFileHandler.set(null);

        /** TimeZone для сервера по умолчанию. */
        Globals.timeZone.set(TimeZone.getTimeZone(Constants.TIME_ZONE));

        /** Путь к файлу-протоколу сообщений пользователей. */
        Globals.transcriptFileName.set(Constants.TRANSCRIPT_FILE_PATH);

        /**
         * Количество экземпляров файлов-протоколов сообщений пользователей.
         */
        Globals.transcriptRotate.set(Constants.TRANSCRIPT_ROTATE);

        /** Путь к файлу конфигурации по умолчанию. */
        Globals.configFilename.set(Constants.CONFIG_FILE_PATH);

        /**
         * Период вывода на внешний носитель элементов очереди протокола
         * сообщений (ms).
         */
        Globals.transcriptWritePeriod.set(Constants.TRANSCRIPT_WRITE_PERIOD);

        /** Максимальная длина очереди протокола сообщений. 
        Globals.maxTranscriptQueueSize = Math.max(Constants.MAX_SERVER_CLIENTS,
                Constants.MAX_SERVER_CLIENTS / Constants.MIN_AVG_READ_PERIOD * Constants.TRANSCRIPT_WRITE_PERIOD);
          */

        /** Длина файла-протокола сообщений пользователей (байт). */
        Globals.transcriptLength.set(Constants.TRANSCRIPT_FILE_LENGTH);

        /** Путь к файлу motd по умолчанию. */
        Globals.motdFilename.set(Constants.MOTD_FILE_PATH);

        /** IP-адрес интерфеса по умолчанию. */
        Globals.serverInetAddress.set(null);

        /** Номер порта по умолчанию. */
        Globals.serverPortNumber.set(Constants.SERVER_PORT_NUMBER);

        /** Объект класса {@link ServerSocket} для данного сервера. */
        Globals.serverSocket.set(null);

        /** Размер буфера приема порта. */
        Globals.receiveBufferSize.set(Constants.RECEIVE_BUFFER_SIZE);

        /**
         * Кодировка сообщений для соединения ({@link Connection}) по умолчанию.
         */
        Globals.listenerCharset.set(
                Charset.forName(Constants.LISTENER_CHARSET));

        /**
         * Минимальный средней период (ms) поступления входящих сообщений для
         * соединения ({@link Connection}). По умолчанию равен
         * {@link Constants#MIN_AVG_READ_PERIOD}
         */
        Globals.minAvgReadPeriod.set(Constants.MIN_AVG_READ_PERIOD);

        /** Минимальный период передачи сообщения IRC PING (ms). */
        Globals.pingSendingPeriod.set(Constants.PING_SENDING_PERIOD);

        /** Время по умолчанию для таймаутов (ms). */
        Globals.sleepTO.set(Constants.SLEEP_TO);

        /**
         * Служебное псевдосоединение {@link NullConnection}.
         */
        Globals.nullConnection.set(new NullConnection());

        /** Служебный псевдопользователь с никнэймом anonymous. */
        Globals.anonymousUser.set(null);

        /** Репозитарий ({@link DB}). */
        Globals.db.set(null);

        /** Время старта процесса {@link Server#run}. */
        Globals.serverStartTime.set(0);

        /**
         * Переменная, управляющая остановом сервера. Останов происходит после
         * присваивания этой переменной значения true.
         */
        Globals.serverDown.set(false);

        /**
         * Переменная, управляющая перезапуском сервера. Перезапуск происходит
         * после присваивания этой переменной значения true.
         */
        Globals.serverRestart.set(false);

        /**
         * Переменная, управляющая повторным чтением файла конфигурации сервера.
         * Повторное чтение происходит после присваивания этой переменной
         * значения true.
         */
        Globals.serverReconfigure.set(false);

        /**
         * Переменная, хранящая множество, с помощью которого индицируется
         * состояние перегруженности одного из программных процессоров.
         */
        Globals.ircServerProcessorSet.set(
                new HashSet<IrcServerProcessor>());
        /**
         * Объект-хранилище параметров файла-протокола клиентских сообщений.
         */
        Globals.ircTranscriptConfig.set(null);

        /** Период (ms) вывода диагностических сообщений. */
        Globals.monitoringPeriod.set(Constants.MONITORING_PERIOD);

    }
    
    public static String buildResourceFilePath(String resourceFilename) {
        // It needs to correctly point location of motdFile. Because there are strange errors when testing it with surefire. 2015-11-10 NK
        Path currentPath = Paths.get("").toAbsolutePath(); 
        String motdFilePath = currentPath.resolve(Paths.get(resourceFilename)).toString();
        return motdFilePath;
    }   

}
