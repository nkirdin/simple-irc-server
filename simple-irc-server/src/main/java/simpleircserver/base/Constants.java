package simpleircserver.base;
import simpleircserver.channel.ChannelMode;
import simpleircserver.connection.Connection;
import simpleircserver.talker.user.UserMode;

/*
 * 
 * Constants 
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

/**
 * Интерфейс, который определяет константы и задает их значения. 
 *
 * @version 0.5.2 2012-03-29
 * @version 0.5.3 2015-11-05 Program units were moved from default package into packages with names. Unit tests were added. Changed version and date of the creation of the server.
 * @author  Nikolay Kirdin
 */
public interface Constants {
    
    /** Максимальное количество одновременно открытых сокетов */
    int HARD_LIMIT = 4000;
    
    /** Минимальная длина для некоторых очередей и списков*/
    int MIN_LIMIT = 10;
    
    /** 
     * Минимальный объем свободной памяти, необходимый для разрешения
     * создания объектов классов (и порождаемых от них) IrcTalker, 
     * IrcChannel и Connection в байтах.  
     */
    long MIN_FREE_MEMORY = 10 * 1024 * 1024;
    
    /** 
     * Максимальное количество никнэймов в параметре команды IRC 
     * USERHOST 
     */
    int MAX_USERHOST_LIST_SIZE = 5;
    
    /** Максимальное количество никнэймов в параметре команды IRC ISON */
    int MAX_ISON_LIST_SIZE = 10; // old value: 100
    
    /** Максимально допустимая длина сообщения IRC. */
    int MAX_PARSING_STRING_LENGTH = 512;
    
    /** 
     * Максимальная длина строки текста для вывода в командах IRC INFO 
     * и MOTD. 
     */
    int MAX_OUTPUT_LINE_LENGTH = 80;
    
    /** 
     * Максимальная количество строк текста  в ответах на запросы и в 
     * командах IRC INFO и MOTD. 
     */
    int MAX_OUTPUT_LINE_NUMBER = 5000;
    
    /** 
     * Максимальная количество символов текста для вывода в командах IRC 
     * INFO и MOTD. 
     */
    int MAX_OUTPUT_LINE_CHARS = 8000;
    
    /** 
     * Максимально допустимая длина FQDN хоста. При превышении этой 
     * величины, в качестве идентификатора хоста используется его 
     * IP-адрес
     */
    int MAX_HOSTNAME_LENGTH = 63;
        
    /** Наименьший допустимый номер порта. */
    int MIN_PORT_NUMBER = 0;
    
    /** Наибольший допустимый номер порта. */
    int MAX_PORT_NUMBER = 65535;
    
    /** Период (ms) вывода диагностических сообщений. */
    long MONITORING_PERIOD = 20000;
    
    /*
     BEGIN IRC RPL_ISUPPORT 
     Numeric Definition draft-brocklesby-irc-isupport-03
     Edward Brocklesby 57 Williamson Way Oxford, Oxon  OX4 4TU GB 
     EMail: ejb@goth.net
    */
    /** 
     * Константа определяющая метод сравнения символов независимо от 
     * регистра. Возможные значения: "ascii"; "rfc1459"; 
     * "strict-rfc1459". Реализован только метод "ascii". При сравнении 
     * символов используется локаль Locale.ENGLISH.
     */
    String CASEMAPPING = "ascii";

    /** 
     * Максимально допустимое количество каналов к которым может 
     * подключиться пользователь 
     */
     int CHANLIMIT = 5; // previous value: 21
    
    /** 
     * Допустимые для использования в команде IRC MODE режимы каналов
     * ({@link ChannelMode}).
     */
    String CHANMODES = "beI,,kl,imnpstaqr";
    
    /** Максимально допустимая длина имени канала. */
    int CHANNELLEN = 50;
    
    /** Допустимые префиксы для имен каналов. */
    String CHANTYPES = "#&";
    
    /** 
     * Ключ команды MODE, который используется для управления списком
     * исключений из списка запрета на подключение для канала с режимом 
     * {@link ChannelMode#b}.
     */
    String EXCEPTS = "e";
    
    /** 
     * Ключ команды IRC MODE, который используется для управления списком 
     * учетных записей клиентов, которым разрешено автоматическое 
     * подключение к каналу с режимом "invite only" 
     * ({@link ChannelMode#i}).
     */
    String INVEX = "I";
    
    /**  
    * Максимально допустимая длина параметра {@code <comment>} для 
     * команды IRC KICK. 
     */
    int KICKLEN = 160;
    
    /** 
     * Максимально допустимое количество элементов списка в параметрах 
     * команд IRC.
     */
     int MAXLIST = 5; // previous value: 42
    
    /** 
     * Максимально допустимое количество ключей с параметрами в команде 
     * IRC MODE.
     */
    int MODES = 3;
    
    /** Максимально допустимая длина никнэйма. */
    int NICKLEN = 15;
    
    /** 
     * Текстовое представление статуса для члена канала.
     * Оператор канала (режим {@link ChannelMode#o}) индицируется 
     * символом "@". Член модерируемого канала обладающий "правом 
     * голоса" ({@link ChannelMode#v}) индицируется символом "+".
     */
    String PREFIX = "(ov)@+";
    //  String SAFELIST = "";
    //  String STATUSMSG = "@+";
    //  String STD = "RFC2812";
    //  String TARGMAX = "";
    
    /**  
     * Максимально допустимая длина параметра {@code <topic>} для 
     * команды IRC TOPIC. 
     */
    int TOPIC_LEN = 160;
    
    /** 
     * Допустимые ключи для управления режимом клиента ({@link UserMode}) 
     * в команде IRC MODE. 
     */
    String USER_MODES = "aoirw";
    
    /** 
     * Допустимые ключи для управления режимом канала 
     * ({@link ChannelMode}) в команде IRC MODE. 
     */
    String CHANNEL_MODES = "abeiIklmnoOpqrstv";
    
    /*
      END IRC RPL_ISUPPORT 
    */

    /** Максимальное количество клиентов сервера. */
    int MAX_SERVER_CLIENTS = HARD_LIMIT;
    
    /** Максимальное количество каналов. */
    int MAX_CHANNEL_NUMBER = HARD_LIMIT / 2;
   
    /** Максимальное количество членов канала. */     
    int MAX_CHANNEL_MEMBER = 1000;
    
    /** Максимальное количество операторов. */     
    int MAX_OPERATOR_NUMBER = 100;
    
    /** 
     * Максимальное среднее количество передач сообщений канала за 
     * 10 секунд. 
     */
    int MAX_CHANNEL_RATE_10 = 10;
    
    /**
     * Минимальный средний период (ms) ввода сообщений клиентом.
     */
    int MIN_AVG_READ_PERIOD = 2000;
    
    /** 
     * Период вывода на внешний носитель элементов очереди протокола 
     * сообщений (ms).
     */
    int TRANSCRIPT_WRITE_PERIOD = 5000;
    
    /** 
     * Максимальная длина файла-протокола сообщений (байт).
     */
    int TRANSCRIPT_FILE_LENGTH = 102400;
    
    /**
     * Номер порта сервера по умолчанию.
     */
    int SERVER_PORT_NUMBER = 6667;

    /** Краткое описание сервера. */
    String SERVER_INFO  = "It is a simple IRC server.";
    
    /** Краткое описание сервера для anonymous. */
    String ANONYMOUS_SERVER_INFO = "Irc server for anonymous.";
    
    /** Путь к журнальному файлу. */
    String LOG_FILE_PATH = "IrcServerLog.xml";
    
    /** Уровень сообщений журналирующей подсистемы. */
    String LOG_LEVEL = "WARNING";
    
    /** TimeZone для сервера по умолчанию. */
    String TIME_ZONE = "GMT";
    
    /** Путь к файлу-протоколу сообщений пользователей. */
    String TRANSCRIPT_FILE_PATH = "IrcServerTranscript.txt";
    
    /** 
     * Количество экземпляров файлов-протоколов сообщений пользователей. 
     */
    int TRANSCRIPT_ROTATE = 5;
    
    /** Путь к файлу конфигурации по умолчанию. */
    String CONFIG_FILE_PATH = "IrcServerConfig.xml";
    
    /** Путь к файлу motd по умолчанию. */
    String MOTD_FILE_PATH = "IrcServerMotd.txt";
    
    /** Размер буфера приема порта. */
    int RECEIVE_BUFFER_SIZE = 1024;
    
    /** 
     * Кодировка сообщений для соединения ({@link Connection}) по 
     * умолчанию. 
     */
    String LISTENER_CHARSET = "UTF-8";
    
    /** Минимальный период передачи сообщения IRC PING (ms). */
    long PING_SENDING_PERIOD = 300000;
    
    /** Время по умолчанию для таймаутов (ms). */
    long SLEEP_TO = 100;
    
    /** 
     * Максимальное количество отсчетов для вычисления средней скорости 
     * поступления сообщений. 
     */
    int NUMBER_RATE_POINTS = 300;
    
    
    /** Версия сервера. */
    String SERVER_VERSION = "0.5.3";
    
    /** Коментарий для данной версии сервера. */
    String VERSION_COMMENT = "Blocking IO";
        
    /** Дата компиляции сервера. */    
    String DATE_CREATED = "2015-11-12";

    /** Текст для команды инфо.*/
    String [] INFO_TEXT = new String [] { 
		"       Simple Irc Server",
		"            v." + Constants.SERVER_VERSION,
		"          " + Constants.DATE_CREATED,
		"  Copyright (c) 2012, 2015 Nikolay Kirdin",
		"",
		"  This program is free software: you can",
		"  redistribute it and/or modify it under",
		"  the terms of the GNU Lesser General",
		"  Public License as published by the",
		"  Free Software Foundation, either",
		"  version 3 of the License, or any",
		"  later version.",
		"",
		"  This program is distributed in the hope",
		"  that it will be useful, but WITHOUT ANY",
		"  WARRANTY; without even the implied",
		"  warranty of MERCHANTABILITY or FITNESS",
		"  FOR A PARTICULAR PURPOSE.  See the",
		"  GNU Lesser General Public License",
		"  version 3 for more details.",
		"",
		"  You should have received a copy of the",
		"  GNU Lesser General Public License",
		"  along with this program.  If not, see",
		"  <http://www.gnu.org/licenses/>."};

    
}
