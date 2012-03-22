/*
 * 
 * IrcConfigParser 
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

import java.util.*;
import java.util.logging.*;
import java.io.*;
import java.net.*;
import java.nio.charset.*;
import javax.xml.stream.*;
 
/**
 * Класс, который служит для чтения и извлечения параметров конфигурации
 * из конфигурационного файла.
 *
 * <p>Для файла конфигурации используется следующая DTD.						
 * <pre>
 * 
 *   &lt;?xml version="1.0" encoding="UTF-8"?&gt;
 *   &lt;!-- DTD --&gt;
 *   &lt;!DOCTYPE CONFIG
 *       [
 *           &lt;!ELEMENT CONFIG (ADMIN, SERVER?, INTERFACE?, OPERATOR?,
 *            TRANSCRIPT?)&gt;
 *               &lt;!ELEMENT ADMIN (#PCDATA)&gt;
 *               &lt;!ATTLIST ADMIN 
 *                   name CDATA #REQUIRED 
 *                   location CDATA #REQUIRED
 *                   location2 CDATA #REQUIRED
 *                   email CDATA #REQURED
 *                   info CDATA #REQURED
 *               &gt;
 *               &lt;!ELEMENT SERVER (#PCDATA)&gt;
 *               &lt;!ATTLIST SERVER
 *                   debuglevel CDATA 
 *                   timezone CDATA
 *               &gt;
 *               &lt;!ELEMENT INTERFACE (#PCDATA)&gt;
 *               &lt;!ATTLIST INTERFACE
 *                  iface CDATA 
 *                  port CDATA
 *                  charset CDATA
 *               &gt;
 *               &lt;!ELEMENT TRANSCRIP (#PCDATA)&gt;
 *               &lt;!ATTLIST TRANSCRIP 
 *                   transcript CDATA
 *                   rotate CDATA
 *                   length CDATA
 *              &gt;
 *               &lt;!ELEMENT OPERATOR (#PCDATA)&gt;
 *               &lt;!ATTLIST OPERATOR 
 *                  username CDATA #REQUIRED 
 *                  password CDATA #REQUIRED
 *              &gt;
 *       ]
 *   &gt;
 * </pre>
 * <p>Сохранение параметров будет произведено только при успешном 
 * синтаксическом разборе всего файла. Для этого необходимо, 
 * чтобы в файле конфигурации был полностью определен элемент "ADMIN".
 * <p> Ниже приведены описания элементов и атрибутов: 
 * <ol>
 * 		<li> Все атрибуты должны быть строками в кодировке US-ASCII.</li>
 *    	<li> Атрибуты элемента "ADMIN":
 *		<ul>
 *          <li>"name", с помощью этого атрибута указываются имя и 
 *          фамилия администратора сервера;</li>
 *          <li>"location", с помощью этого атрибута указывается адрес 
 *          расположения сервера IRC;</li>
 *          <li>"location2", с помощью этого атрибута указывается
 *          наименование и адрес организации, которой 
 *          принадлежит сервер IRC;</li>
 *          <li>"email", с помощью этого атрибута указывается адрес 
 *          электронной почты администратора сервера;</li>   
 *          <li>"info", с помощью этого атрибута кратко характризуется 
 *          назначение сервера IRC.</li>   
 *      </ul>
 *      Значения атрибутов этого элемента можно получить выполнив 
 *      команду IRC ADMIN. </li>
 *      <li> Атрибуты элемента "SERVER":
 *      	<ul>
 *          	<li>"debuglevel", с помощью этого атрибута  
 *          	указывается уровень журналируемых сообщений. Эти уровни 
 *          	определены в классе {@link java.util.logging.Level}.
 *          	Допустимо использовать следующие уровни:
 *          	<ul>
 *              	<li> SEVERE (высший уровень);</li>
 *              	<li> WARNING;</li>
 *              	<li> INFO;</li>
 *              	<li> CONFIG;</li>
 *              	<li> FINE;</li>
 *              	<li> FINER;</li>
 *              	<li> FINEST (низший уровень);</li>
 *              	<li> OFF - отключение журналирования;</li>
 *              	<li> ALL - регистрация всех сообщений;</li>
 *          	</ul>
 *          	с уменьшением уровня увеличивается количество и 
 *          	разнообразие журналируемых сообщений. По умолчанию, 
 *          	используется значение,хранящееся в переменной 
 *          	{@link Globals#fileLogLevel};</li>
 *          	<li>"timezone", с помощью этого атрибута задается 
 *          	часовой пояс сервера. Допустимые значения определены в 
 *          	классе {@link java.util.TimeZone}. По умолчанию 
 *          	используется значение "GMT".</li> 
 *          </ul>
 *      <li> Атрибуты элемента "INTERFACE":
 *      	<ul> 
 *          	<li>"iface", с помощью этого атрибута, в нотации IPv4, 
 *          	задается IP-адрес интерфейса, который будет использован 
 *          	для входящих сетевых подключения. В случае указания 
 *          	пустой строки, сервер будет использовать все доступные 
 *          	сетевые интерфейсы. По умолчанию значение атрибута равно 
 *          	пустой строке; </li>
 *         	<li>"port", с помощью этого атрибута задается номер порта 
 *         	интерфейса, который будет использован для входящих сетевых 
 *         	подключений. Атрибут должен быть целым десятичным числом 
 *          	в диапазоне {@link Constants#MIN_PORT_NUMBER} - 
 *          	{@link Constants#MAX_PORT_NUMBER}. По умолчанию в 
 *          	качестве номера порта используется величина, хранящаяся 
 *          	в {@link Globals#serverPortNumber};</li> 
 *          	<li> "charset", с помощью этого атрибута задается 
 *          	кодировка входящих сообщений. Значения этого атрибута 
 *          	определяется в классе {@link java.nio.charset.Charset}. 
 *          	Допустимо использовать следующие кодировки:
 *          	<ul>
 *              	<li> US-ASCII;</li>
 *              	<li> ISO-8859-1;</li>
 *              	<li> UTF-8;</li>
 *              	<li> UTF-16BE;</li>
 *              	<li> UTF-16LE;</li>
 *              	<li> UTF-16.</li>
 *          	</ul> 
 *          	по умолчанию в качестве кодировки интерфейса 
 *          	используется кодировка {@link Globals#listenerCharset}.
 *          	</li>
 *          </ul>
 *      <li>Атрибуты элемента "TRANSCRIPT":
 *      	<ul>
 *          	<li>"transcript", с помощью этого атрибута указывается 
 *          	путь к файлу с протоколом клиентских сообщений. По 
 *          	умолчанию будет использован файл "IrcTranscript.txt" в 
 *          	текущем каталоге;</li>
 *          	<li>"rotate", с помощью этого атрибута указывается 
 *          	количество сохраняемых экземпляров файлов-протоколов 
 *          	клиентских сообщений. По умолчанию это количество равно 
 *          	5;</li> 
 *          	<li>"length", с помощью этого атрибута указывается 
 *          	максимальная длина в байтах файлов-протоколов клиентских 
 *          	сообщений. К числу может быть добавлен суффикс - 
 *          	одна из латинских букв "K" или "M", для задания 
 *          	множителей 1024 и 1048576 соответственно. По умолчанию
 *          	эта	длина ограничена 100K байт.</li>
 *          </ul>
 *         </li>
 * 		<li>Атрибуты элемента "OPERATOR": "username" и "password". 
 *      Значениеми этих атрибутов должны быть значения, которые будут 
 *      использоваться как параметры &lt;username&gt; и &lt;password&gt; 
 *      команды IRC "OPER". Максимальное количество этих элементов 
 *      ограничено Количество операторов ограничено полем 
 *      {@link DB#maxIrcOperatorConfigMapSize}.</li>
 * </ol>
 * <p> Ниже приведено примерное содержание файла конфигурации.
 * <p>При успешном завершении разбора файла, могут создаваться 
 * объекты следующих классов:
 * <ul>
 *      <li>{@link DB#ircAdminConfig}. В этом объекте будет находиться 
 *      административная информация об администраторе и сервере 
 *      (заполняется данными из элемента "ADMIN");</li>
 *      <li>{@link DB#ircServerConfig}. В этом объекте будет находиться 
 *      информация с параметрами сервера (заполняется данными из 
 *      элемента "SERVER");</li>
 *      <li>{@link DB#ircInterfaceConfig}. В этом объекте будет 
 *      находиться информация с сетевыми параметрами (заполняется 
 *      данными из элемента "INTERFACE");</li>
 *      <li>{@link Globals#ircTranscriptConfig}. В этом объекте будет 
 *      находиться информация с параметрами файла-протокола клиентских 
 *      сообщений (заполняется данными из элемента "TRANSCRIPT");</li>
 *      <li>{@link DB#ircOperatorConfigMap}. В этом ассоциативном 
 *      массиве хранятся учетные записи операторов (заполняется данными 
 *      из элемента "OPERATOR").</li>
 * </ul>
 * <pre>
 * <p> Ниже приведен пример файла конфигурации.
 * 
 * &lt;?xml version="1.0" encoding="UTF-8"?&gt;
 * &lt;!-- File Name: IrcServerConfig.xml --&gt;
 * &lt;CONFIG&gt;
 * 		&lt;ADMIN name="Adminname Adminsurename" 
 * 			location="25, Serverstreet, Servercity, Servercountry" 
 * 			location2="Organizationname, 25, Organizationstreet, Organizationcity, Organizationcountry"  
 * 			email="ircAdmin@dom.ain" 
 * 			info="Experimental IRC server."&gt;
 * 		&lt;/ADMIN&gt;
 * 		&lt;SERVER 
 * 			timezone="GMT+0400" 
 * 			debuglevel="WARNING"&gt;
 * 		&lt;/SERVER&gt;
 * 		&lt;INTERFACE  
 * 			port="6667" 
 * 			charset="UTF-8"&gt;
 * 		&lt;/INTERFACE&gt;
 * 		&lt;TRANSCRIPT 
 * 			transcript="IrcServerTranscript.txt" 
 * 			length="1M" 
 * 			rotate="10"&gt;
 * 			&lt;/TRANSCRIPT&gt;
 * 		&lt;OPERATOR 
 * 			username="operatorname1" password="operatorpassword1"&gt;
 * 		&lt;/OPERATOR&gt;
 * 		&lt;OPERATOR 
 * 			username="operatorname2" password="operatorpassword2"&gt;
 * 		&lt;/OPERATOR&gt;
 * &lt;/CONFIG&gt;
 * </pre>
 *
 * 
 * @version 0.5 2012-02-11
 * @author  Nikolay Kirdin
 *  
 */
public class IrcConfigParser {
    
    /** Репозитарий. */
    private DB db;
    
    /** Журналирующая подсистема. */
    private Logger logger;
    
    /** Признак ошибки. */
    private boolean error = false;
    
    /** Текстовое описание ошибки. */
    private String errorDescription = "";

    /** Информация о сервере и его администраторе. */
    private IrcAdminConfig ircAdminConfig = null;
    
    /** Параметры сервера. */
    private IrcServerConfig ircServerConfig = null;
    
    /** Параметры операторов. */
    private IrcOperatorConfig ircOperatorConfig = null;
    
    /** Параметры интерфейса. */
    private IrcInterfaceConfig ircInterfaceConfig = null;
    
    /** Параметры файла-протокола клиентских сообщений. */
    private IrcTranscriptConfig ircTranscriptConfig = null;
    
    /** Ассоциативный массив с параметрами операторов. */
    private LinkedHashMap<String, IrcOperatorConfig>
            ircOperatorConfigMap = null;

    /** Путь к файлу конфигурации. */
    private String configFilename;
    
    /** Буферированный поток ввода для файла конфигурации. */
    private BufferedReader br = null;
    
    /** XML-поток. */
    private XMLStreamReader xsr = null;

    /** XML-событие. */
    private int event = XMLStreamConstants.START_DOCUMENT;

    /** Индикатор элемента "CONFIG". */
    private String configElement = "CONFIG";
    
    /** Индикатор элемента "ADMIN". */
    private String adminElement = "ADMIN";
    
    /** Индикатор элемента "SERVER". */
    private String serverElement = "SERVER";
    
    /** Индикатор элемента "INTERFACE". */
    private String interfaceElement = "INTERFACE";
    
    /** Индикатор элемента "OPERATOR". */
    private String operatorElement = "OPERATOR";
    
    /** Индикатор элемента "TRANSCRIPT". */
    private String transcriptElement = "TRANSCRIPT";

    /**
     * Конструктор.
     * @param configFilename путь к файлу конфигурации.
     * @param db репозитарий.
     * @param logger объект для обеспечения журналирования.
     */
    public IrcConfigParser(String configFilename, DB db, Logger logger) {
        this.logger = logger;
        this.configFilename = configFilename;
        this.db = db;
    }

    /**
     * Метод используется для получения параметров сервера из файла 
     * конфигурации. Этот метод проверяет существование файла 
     * конфигурации, вызывает методы для его разбора, извлекает 
     * конфигурационные параметры, и сохраняет их в репозитарии. 
     * @return false если разбор файла и создание объектов завершились 
     * успешно, true если на каком-либо этапе была обнаружена ошибка.
     */
    public boolean useIrcConfigFile() {

        logger.log(Level.WARNING, "Reading config File:" + 
        		configFilename);

        try {

            br = new BufferedReader(new FileReader(configFilename));
            XMLInputFactory xif = XMLInputFactory.newInstance();
            xsr = xif.createXMLStreamReader(br);

            startIrcConfigFile();

            startConfigElement();

            readAdminElement();

            if (isStartElement(serverElement)) {
                readServerElement();
            }
            
            if (isStartElement(interfaceElement)) {
                readInterfaceElement();
            }

            if (isStartElement(transcriptElement)) {
                readTranscriptElement();
            }

            while (isStartElement(operatorElement)) {
                readOperatorElement();
            }

            //endConfigElement();

            endIrcConfigFile();

            if (!error) {

                if (ircAdminConfig != null) {
                    db.setIrcAdminConfig(ircAdminConfig);
                } else {
                    error = true;
                    errorDescription = "Admin\'s info not defined.";
                    logger.log(Level.WARNING, errorDescription);
                }

                if (ircServerConfig != null) {
                    db.setIrcServerConfig(ircServerConfig);
                }

                if (ircInterfaceConfig != null) {
                    db.setIrcInterfaceConfig(ircInterfaceConfig);
                }

                if (ircTranscriptConfig != null) {
                    Globals.ircTranscriptConfig.set(ircTranscriptConfig);
                }

                if (ircOperatorConfigMap != null) {
                    db.setIrcOperatorConfigMap(ircOperatorConfigMap);
                    logger.log(Level.WARNING, "Operator(s) configured."
                            + " Count: " + ircOperatorConfigMap.size());
                }
            }
            if (!error) {
            	logger.log(Level.WARNING, "Configuration parameters " +
            			"for admin. Name: " + ircAdminConfig.getName() 
            			+","
            			+ " Location: " + ircAdminConfig.getLocation()
            			+","
            			+ " Location2: " + ircAdminConfig.getLocation2()
            			+","
            			+ " Email: " + ircAdminConfig.getEmail() + ","
            			+ " Info: " + ircAdminConfig.getInfo());

            	logger.log(Level.WARNING, "Configuration parameters " +
            			"for server." + " TimeZone: " + 
            			ircServerConfig.getTimeZone().getID() +","
            			+ " DebugLevel: " + 
            			ircServerConfig.getDebugLevel());

            	logger.log(Level.WARNING, "Configuration parameters " +
            			"for interface."
            			+ " IP: " + ircInterfaceConfig.getInetAddress()
            			+ ","
            			+ " Port: " + ircInterfaceConfig.getPort() + ","
            			+ " Charset: " + ircInterfaceConfig.getCharset());

            	logger.log(Level.WARNING, "Configuration parameters " +
            			"for users transcript. Filename: " + 
            			ircTranscriptConfig.getTranscript() +","
            			+ " Rotate: " + 
            			ircTranscriptConfig.getRotate()
            			+ " Length: " + 
            			ircTranscriptConfig.getLength());
            } else {
            	logger.log(Level.SEVERE, "Error in config File:" + 
            			configFilename);
            }

        } catch (IOException e) {
            error = true;
            errorDescription = "File error: " + configFilename + " " + e;
            logger.log(Level.WARNING, errorDescription);
        }
        catch (XMLStreamException e) {
            error = true;
            errorDescription = "XML stream processing error: " +
                    configFilename + " " + e;
            logger.log(Level.WARNING, errorDescription);
            
        } finally {
            try {
                if (xsr != null) {
                    xsr.close();
                }
                if (br != null) {
                    br.close();
                }
            } catch (XMLStreamException e) {
                error = true;
                errorDescription = "XML stream closing error: " +
                        configFilename + " " + e;
                logger.log(Level.WARNING, errorDescription);
            } catch (IOException e) {
                error = true;
                errorDescription = "File closing error: " +
                        configFilename + " " + e;
                logger.log(Level.WARNING, errorDescription);
            }
        }
        return error;
    }

    /** 
     * Проверка начала элемента.
     * @param elementName название элемента.
     * @return true, если это начало элемента, имя которого указано в 
     * качестве аргумента.  
     */
    private boolean isStartElement(String elementName) {
    	boolean result = (event == XMLStreamConstants.START_ELEMENT)
                && (xsr.getLocalName().equals(elementName));
        return result;
    }

    /** Определение начала XML-документа. */
    private void startIrcConfigFile() throws XMLStreamException, 
    		IOException {

        boolean locError = false;
        boolean done = false;
        int line = 0;
        int column = 0;

        event = xsr.getEventType();

        if (event != XMLStreamConstants.START_DOCUMENT) {
            locError = true;
            done = true;
            line = xsr.getLocation().getLineNumber();
            column = xsr.getLocation().getColumnNumber();
            errorDescription = " Line: " + line + " Column: " + column +
                    " Unexpected XML event: " + event;
        } else {
            event = xsr.next();
        }

        while (!done) {
            line = xsr.getLocation().getLineNumber();
            column = xsr.getLocation().getColumnNumber();

            switch (event) {

            case XMLStreamConstants.COMMENT:
                event = xsr.next();
                break;

            case XMLStreamConstants.DTD:
                event = xsr.nextTag();
                done = true;
                break;

            case XMLStreamConstants.START_ELEMENT:
                done = true;
                break;

            default:
                locError = true;
                done = true;
                errorDescription = " Line: " + line + " Column: " + 
                		column + " Unexpected XML event: " + event;
                break;

            }
        }

        if (locError) {
            logger.log(Level.WARNING,
                    "Error in " + "START" + " element." + 
                    errorDescription);
        }

        error = error || locError;
    }

    /** Определение начала элемента "CONFIG".*/
    private void startConfigElement() throws XMLStreamException, 
    		IOException {

        boolean locError = false;
        boolean done = false;
        int line = 0;
        int column = 0;

        if (event != XMLStreamConstants.START_ELEMENT ||
            !xsr.getLocalName().equals(configElement)) {
            locError = true;
            done = true;
            line = xsr.getLocation().getLineNumber();
            column = xsr.getLocation().getColumnNumber();
            errorDescription = " Line: " + line + " Column: " + column +
                    " Unexpected XML event: " + event;
        } else {
            event = xsr.nextTag();
        }

        while (!done) {
            line = xsr.getLocation().getLineNumber();
            column = xsr.getLocation().getColumnNumber();

            switch (event) {

            case XMLStreamConstants.COMMENT:
                event = xsr.nextTag();
                break;

            case XMLStreamConstants.START_ELEMENT:
                done = true;
                break;

            default:
                locError = true;
                done = true;
                errorDescription = " Line: " + line + " Column: " + 
                		column + " Unexpected XML event: " + event;
                break;
            }
        }

        if (locError) {
            logger.log(Level.WARNING,
                    "Error in " + "START" + " element." + 
                    errorDescription);
        }

        error = error || locError;
    }

    /** Разбор и получение данных из элемента "ADMIN". */
    private void readAdminElement() throws XMLStreamException, 
    		IOException {

        boolean locError = false;
        boolean done = false;
        int line = 0;
        int column = 0;

        String nameAttribute = "name";
        String locationAttribute = "location";
        String location2Attribute = "location2";
        String emailAttribute = "email";
        String infoAttribute = "info";

        String nameAtt = null;
        String locationAtt = null;
        String location2Att = null;
        String emailAtt = null;
        String infoAtt = null;

        event = xsr.getEventType();

        while (!done) {
            line = xsr.getLocation().getLineNumber();
            column = xsr.getLocation().getColumnNumber();

            switch (event) {

            case XMLStreamConstants.COMMENT:
                event = xsr.nextTag();
                break;

            case XMLStreamConstants.CHARACTERS:
                event = xsr.nextTag();
                break;

            case XMLStreamConstants.START_ELEMENT:

                if (!xsr.getLocalName().equals(adminElement)) {
                    locError = true;
                    done = true;
                    errorDescription = " Line: " + line +
                            " Column: " + column +
                            " Not an " + adminElement +
                            " element: " + xsr.getLocalName();
                    break;
                }

                try {

                    nameAtt = IrcCommandBase.check(
                            xsr.getAttributeValue(null, nameAttribute)
                            , IrcParamRegex.stringUsAsciiRegex);

                    locationAtt = IrcCommandBase.check(
                            xsr.getAttributeValue(null, 
                            locationAttribute)
                            , IrcParamRegex.stringUsAsciiRegex);

                    location2Att = IrcCommandBase.check(
                            xsr.getAttributeValue(null, 
                            location2Attribute)
                            , IrcParamRegex.stringUsAsciiRegex);

                    emailAtt = IrcCommandBase.check(
                            xsr.getAttributeValue(null, emailAttribute)
                            , IrcParamRegex.stringUsAsciiRegex);

                    infoAtt = IrcCommandBase.check(
                            xsr.getAttributeValue(null, infoAttribute)
                            , IrcParamRegex.stringUsAsciiRegex);
                    
                    ircAdminConfig = new IrcAdminConfig(nameAtt
                            , locationAtt
                            , location2Att
                            , emailAtt
                            , infoAtt);
                    
                    Globals.thisIrcServerInfo.set(infoAtt);
                    

                } catch (IrcSyntaxException e) {
                    locError = true;
                    done = true;
                    errorDescription = " Line: " + line + " Column: " + 
                            column + " Syntax error in attribute(s).";
                } catch (IndexOutOfBoundsException e) {
                    locError = true;
                    done = true;
                    errorDescription = " Line: " + line + " Column: " + 
                            column + " Need more attribute(s).";
                }

                event = xsr.next();
                break;

            case XMLStreamConstants.END_ELEMENT:
                event = xsr.nextTag();
                done = true;
                break;

            default:
                locError = true;
                done = true;
                errorDescription = " Line: " + line + " Column: " + 
                        column + " Unexpected XML event: " + event;
                break;
            }
        }

        if (locError) {
            logger.log(Level.WARNING,
                    "Error in " + adminElement + " element." + 
                    errorDescription);
        }

        error = error || locError;
    }

    /** Разбор и получение данных из элемента "SERVER". */
    private void readServerElement() throws XMLStreamException, 
    		IOException {

        boolean locError = false;
        boolean done = false;
        int line = 0;
        int column = 0;

        String timezoneAttribute = "timezone";
        String timezoneAttString = null;
        String timezoneAtt = null;
        TimeZone timeZone = null;
        
        String debugAttribute = "debuglevel";
        String debugAtt = null;
        Level debugLevel = null;
                
        while (!done) {
            line = xsr.getLocation().getLineNumber();
            column = xsr.getLocation().getColumnNumber();

            switch (event) {

            case XMLStreamConstants.COMMENT:
                event = xsr.nextTag();
                break;

            case XMLStreamConstants.CHARACTERS:
                event = xsr.nextTag();
                break;

            case XMLStreamConstants.START_ELEMENT:

                if (!xsr.getLocalName().equals(serverElement)) {
                    done = true;
                    break;
                }

                timezoneAttString = xsr.getAttributeValue(null, 
                        timezoneAttribute);
                debugAtt = xsr.getAttributeValue(null, debugAttribute);
                
                if (timezoneAttString != null) {
                    try {
                        timezoneAtt = IrcCommandBase.check(
                        		timezoneAttString, 
                        		IrcParamRegex.wordRegex);
                        timeZone = TimeZone.getTimeZone(timezoneAtt);
                        
                    } catch (IrcSyntaxException e) {
                        locError = true;
                        done = true;
                        errorDescription = " Line: " + line + 
                        		" Column: " + column + 
                        		" Syntax error near attribute(s). " + e;
                    }
                    catch (IndexOutOfBoundsException e) {
                        locError = true;
                        done = true;
                        errorDescription = " Line: " + line + 
                        		" Column: " + column + 
                        		" Need more attribute(s).";
                    }
                } 

                if (debugAtt != null) {
                    try {
                        
                        debugLevel = Level.parse(IrcCommandBase.check(
                                debugAtt, 
                                IrcParamRegex.stringUsAsciiRegex));
                         
                    } catch (IrcSyntaxException e) {
                        locError = true;
                        done = true;
                        errorDescription = " Line: " + line + 
                        		" Column: " + column + 
                        		" Syntax error near attribute(s). " 
                                + e;
                    }
                    catch (IndexOutOfBoundsException e) {
                        locError = true;
                        done = true;
                        errorDescription = " Line: " + line + 
                        		" Column: " + column + 
                        		" Need more attribute(s).";
                    }
                    catch (IllegalArgumentException e) {
                        locError = true;
                        done = true;
                        errorDescription = " Line: " + line + 
                        		" Column: " + column + 
                        		" Syntax error near attribute(s).";
                    }                    
                } 
                
                if (!locError && ! error) {
                    if (timeZone == null) {
                        timeZone = TimeZone.getDefault();
                    }
                    if (debugLevel == null) {
                        debugLevel = Globals.fileLogLevel.get();
                    }
                    
                    ircServerConfig = db.getIrcServerConfig();
                    synchronized (ircServerConfig) {
                        ircServerConfig.setTimeZone(timeZone);
                        ircServerConfig.setDebugLevel(debugLevel);
                    }
                }

                event = xsr.next();
                break;

            case XMLStreamConstants.END_ELEMENT:
                event = xsr.nextTag();
                done = true;
                break;

            default:
                locError = true;
                done = true;
                errorDescription = " Line: " + line + " Column: " + 
                		column + " Unexpected XML event: " + event;
                break;
            }
        }

        if (locError) {
            logger.log(Level.WARNING,
                    "Error in " + serverElement + " element." + 
                    errorDescription);
        }

        error = error || locError;
    }
    
    /** Разбор и получение данных из элемента "INTERFACE". */
    private void readInterfaceElement() throws XMLStreamException, 
    		IOException {

        boolean locError = false;
        boolean done = false;
        int line = 0;
        int column = 0;

        String ifaceAttribute = "iface";
        String ifaceAttString = null;
        String ifaceAtt = null;
        InetAddress ifaceAddress = null;

        String portAttribute = "port";
        String portAttString = null;
        String portAtt = null;
        int portNumber = -1;
        
        String charsetAttribute = "charset";
        String charsetAttString = null;
        String charsetAtt = null;
        Charset charset = null;
        
        event = xsr.getEventType();

        while (!done) {
            line = xsr.getLocation().getLineNumber();
            column = xsr.getLocation().getColumnNumber();

            switch (event) {

            case XMLStreamConstants.COMMENT:
                event = xsr.nextTag();
                break;

            case XMLStreamConstants.CHARACTERS:
                event = xsr.nextTag();
                break;

            case XMLStreamConstants.START_ELEMENT:

                if (!xsr.getLocalName().equals(interfaceElement)) {
                    done = true;
                    break;
                }
                
                ifaceAttString = xsr.getAttributeValue(null, 
                        ifaceAttribute); 
                portAttString = xsr.getAttributeValue(null, 
                        portAttribute);
                charsetAttString = xsr.getAttributeValue(null, 
                        charsetAttribute);
                
                if (portAttString != null) {
                    try {
                        portAtt = IrcCommandBase.check(portAttString
                                , IrcParamRegex.numberRegex);
                        portNumber = Integer.parseInt(portAtt);

                        if (portNumber < Constants.MIN_PORT_NUMBER || 
                            portNumber > Constants.MAX_PORT_NUMBER) {
                            throw new IrcSyntaxException(
                                "Port number out of bounds:" + 
                                portNumber);
                        }
                        
                    } catch (IrcSyntaxException e) {
                        locError = true;
                        done = true;
                        errorDescription = " Line: " + line + 
                        		" Column: " + column + 
                        		" Syntax error near attribute(s). " 
                                + e;
                    }
                    catch (IndexOutOfBoundsException e) {
                        locError = true;
                        done = true;
                        errorDescription = " Line: " + line + 
                        		" Column: " + column + 
                        		" Need more attribute(s).";
                    }
                    catch (NumberFormatException e) {
                        locError = true;
                        done = true;
                        errorDescription = " Line: " + line + " Column: " + 
                                column + " Syntax error near attribute(s).";
                    }
                } 

                if (charsetAttString != null) {
                    try {
                        charsetAtt = IrcCommandBase.check(
                        		charsetAttString, 
                        		IrcParamRegex.wordRegex);
                        charset = Charset.forName(charsetAtt);
                    } catch (IrcSyntaxException e) {
                        locError = true;
                        done = true;
                        errorDescription = " Line: " + line + 
                        		" Column: " + column + 
                        		" Syntax error near attribute(s).";
                        break;
                    }
                    catch (IndexOutOfBoundsException e) {
                        locError = true;
                        done = true;
                        errorDescription = " Line: " + line + 
                        		" Column: " + column + 
                        		" Need more attribute(s).";
                        break;
                    }
                    catch (IllegalCharsetNameException e) {
                        locError = true;
                        done = true;
                        errorDescription = " Line: " + line + 
                        		" Column: " + column + 
                        		" Syntax error near attribute(s). " 
                                + e;
                        break;
                    }
                    catch (UnsupportedCharsetException e) {
                        locError = true;
                        done = true;
                        errorDescription = " Line: " + line + 
                        		" Column: " + column + 
                        		" Syntax error near attribute(s). " 
                                + e;
                        
                    }
                    catch (IllegalArgumentException e) {
                        locError = true;
                        done = true;
                        errorDescription = " Line: " + line + 
                        		" Column: " + column + 
                        		" Syntax error near attribute(s). " 
                                + e;
                        
                    }
                }
                
                if (ifaceAttString != null) {
                    try {
                        if (ifaceAttString.isEmpty()) {
                            ifaceAddress = null;
                        } else if (IrcCommandBase.isIt(ifaceAttString, 
                                IrcParamRegex.ip4AddrRegex)) {
                            String [] Ip4String = 
                                    ifaceAttString.split("\\.");
                            byte [] Ip4Byte = new byte[4];
                            int i = 0;
                            for (String s : Ip4String) {
                                Ip4Byte[i++] = (byte) Integer.parseInt(s);
                            }
                           ifaceAddress = InetAddress.getByAddress(
                                Ip4Byte);
                        } else {
                        	locError = true;
                            done = true;
                            errorDescription = " Line: " + line + 
                            		" Column: " + column + 
                            		" Syntax error near attribute(s): "
                            		+ ifaceAttString;                            
                        } 

                    /*    
                    } catch (IrcSyntaxException e) {
                        locError = true;
                        done = true;
                        errorDescription = " Line: " + line + " Column: " + 
                                column + " Syntax error near attribute(s): " + 
                                ifaceAttString + " " + e;
                    }*/
                    } catch (IndexOutOfBoundsException e) {
                        locError = true;
                        done = true;
                        errorDescription = " Line: " + line + 
                        		" Column: " + column + 
                        		" Syntax error near attribute(s): " + 
                                ifaceAttString + " " + e;
                    } catch (NumberFormatException e) {
                        locError = true;
                        done = true;
                        errorDescription = " Line: " + line + 
                        		" Column: " + column + 
                        		" Syntax error near attribute(s): " + 
                                ifaceAttString + " " + e;
                    } catch (UnknownHostException e) {
                        locError = true;
                        done = true;
                        errorDescription = " Line: " + line + 
                        		" Column: " + column + 
                        		" Syntax error near attribute(s): " + 
                                ifaceAttString + " " + e;
                    }
                } 


                
                if (!locError && !error) {
                    if (portNumber < 0) {
                        portNumber = Globals.serverPortNumber.get();
                    }
                    if (charset == null) {
                        charset = Globals.listenerCharset.get();
                    }
                    
                    ircInterfaceConfig = db.getIrcInterfaceConfig();
                    synchronized (ircInterfaceConfig) {
                        ircInterfaceConfig.setInetAddress(ifaceAddress);
                        ircInterfaceConfig.setPort(portNumber);
                        ircInterfaceConfig.setCharset(charset);
                    }
                }

                event = xsr.next();
                break;

            case XMLStreamConstants.END_ELEMENT:
                event = xsr.nextTag();
                done = true;
                break;

            default:
                locError = true;
                done = true;
                errorDescription = " Line: " + line + " Column: " + 
                		column + " Unexpected XML event: " + event;
                break;
            }
        }

        if (locError) {
            logger.log(Level.WARNING,
                    "Error in " + interfaceElement + " element." + 
                    errorDescription);
        }

        error = error || locError;
    }
    
    /** Разбор и получение данных из элемента "TRANSCRIPT". */
    private void readTranscriptElement() throws XMLStreamException, 
    		IOException {

        boolean locError = false;
        boolean done = false;
        int line = 0;
        int column = 0;

        String transcriptAttribute = "transcript";
        String transcriptAttString = null;
        String transcriptAtt = null;

        String rotateAttribute = "rotate";
        String rotateAttString = null;
        int rotateAtt = -1;

        String lengthAttribute = "length";
        String lengthAttString = null;
        int lengthAtt = -1;

        
        
        event = xsr.getEventType();

        while (!done) {
            line = xsr.getLocation().getLineNumber();
            column = xsr.getLocation().getColumnNumber();

            switch (event) {

            case XMLStreamConstants.COMMENT:
                event = xsr.nextTag();
                break;

            case XMLStreamConstants.CHARACTERS:
                event = xsr.nextTag();
                break;

            case XMLStreamConstants.START_ELEMENT:

                if (!xsr.getLocalName().equals(transcriptElement)) {
                    done = true;
                    break;
                }

                transcriptAttString = xsr.getAttributeValue(null, 
                		transcriptAttribute);
                
                rotateAttString = xsr.getAttributeValue(null, 
                		rotateAttribute);
                
                lengthAttString = xsr.getAttributeValue(null, 
                		lengthAttribute);

                if (transcriptAttString != null) {
                    try {
                    	transcriptAtt = IrcCommandBase.check(
                    			transcriptAttString, 
                    			"(" + IrcParamRegex.nospcrlfclRegex + 
                    			"){1,255}");
                        
                    } catch (IrcSyntaxException e) {
                        locError = true;
                        done = true;
                        errorDescription = " Line: " + line +
                        		" Column: " + column + 
                        		" Syntax error near attribute(s) " 
                                + e;
                    }
                    catch (IndexOutOfBoundsException e) {
                        locError = true;
                        done = true;
                        errorDescription = " Line: " + line + 
                        		" Column: " + column + 
                        		" Need more attribute(s) " + e;
                    }
                } 

                if (rotateAttString != null) {
                    try {
                        IrcCommandBase.check(rotateAttString, "\\d+");
                        rotateAtt = Integer.parseInt(rotateAttString);

                        if (rotateAtt < 1) {
                            throw new IrcSyntaxException(
                                "transcript transcript out of bounds:" + 
                                rotateAtt);
                        }
                        
                    } catch (IrcSyntaxException e) {
                        locError = true;
                        done = true;
                        errorDescription = " Line: " + line + 
                        		" Column: " + column + 
                        		" Syntax error near attribute(s) " 
                                + e;
                    }
                    catch (IndexOutOfBoundsException e) {
                        locError = true;
                        done = true;
                        errorDescription = " Line: " + line + 
                        		" Column: " + column + 
                        		" Need more attribute(s)" + e;
                    }
                    catch (NumberFormatException e) {
                        locError = true;
                        done = true;
                        errorDescription = " Line: " + line + 
                        		" Column: " + column + 
                        		" Syntax error near attribute(s)" 
                        		+ e;
                    }
                } 

                if (lengthAttString != null) {
                    try {
                    	IrcCommandBase.check(lengthAttString,
                    			"\\d+(K|M)?");
                    	int multiplier = 1;
                    	if (lengthAttString.endsWith("K")) {
                    		multiplier = 1024;
                    		lengthAttString = lengthAttString.substring(
                    				0, lengthAttString.length() - 1);
                    	} else if (lengthAttString.endsWith("M")) {
                    		multiplier = 1048576;
                    		lengthAttString = lengthAttString.substring(
                    				0, lengthAttString.length() - 1);
                    	}
                    	lengthAtt = Integer.parseInt(lengthAttString)
                    			* multiplier;
                    	if (lengthAtt < 1) {
                            throw new IrcSyntaxException(
                                    "transcript length out of bounds:" + 
                                    lengthAtt);
                    		
                    	}
                    	
                        
                    } catch (IrcSyntaxException e) {
                        locError = true;
                        done = true;
                        errorDescription = " Line: " + line + " Column: " + 
                                column + " Syntax error near attribute(s) " 
                                + e;
                    }
                    catch (IndexOutOfBoundsException e) {
                        locError = true;
                        done = true;
                        errorDescription = " Line: " + line + 
                        		" Column: " + column + 
                        		" Need more attribute(s) " + e;
                    }
                    catch (NumberFormatException e) {
                        locError = true;
                        done = true;
                        errorDescription = " Line: " + line + 
                        		" Column: " + column + 
                        		" Syntax error near attribute(s) "
                        		+ e;
                    }
                    catch (ArithmeticException e) {
                        locError = true;
                        done = true;
                        errorDescription = " Line: " + line + 
                        		" Column: " + column + 
                        		" Syntax error near attribute(s)"
                        		+ e;
                    }
                } 
                
                if (!locError && ! error) {
                    if (transcriptAtt == null) {
                    	transcriptAtt = Globals.transcriptFileName.get();
                    }
                    if (rotateAtt < 1) {
                    	rotateAtt = Globals.transcriptRotate.get();
                    }
                    if (lengthAtt < 1024) {
                    	lengthAtt = Globals.transcriptLength.get();
                    }
                	
                	ircTranscriptConfig = 
                			Globals.ircTranscriptConfig.get();
                    synchronized (ircTranscriptConfig) {
                        ircTranscriptConfig.setTranscript(transcriptAtt);
                        ircTranscriptConfig.setRotate(rotateAtt);
                        ircTranscriptConfig.setLength(lengthAtt);
                    }
                }

                event = xsr.next();
                break;

            case XMLStreamConstants.END_ELEMENT:
                event = xsr.nextTag();
                done = true;
                break;

            default:
                locError = true;
                done = true;
                errorDescription = " Line: " + line + " Column: " + 
                		column + " Unexpected XML event: " + event;
                break;
            }
        }

        if (locError) {
            logger.log(Level.WARNING,
                    "Error in " + transcriptElement + " element." + 
                    errorDescription);
        }

        error = error || locError;
    }

    /** Разбор и получение данных из элемента "OPERATOR". */
    private void readOperatorElement() throws XMLStreamException, 
    		IOException {

        boolean locError = false;
        boolean done = false;
        int line = 0;
        int column = 0;

        String nameAttribute = "username";
        String passwordAttribute = "password";

        String nameAtt = null;
        String passwordAtt = null;

        event = xsr.getEventType();

        while (!done) {
            line = xsr.getLocation().getLineNumber();
            column = xsr.getLocation().getColumnNumber();

            switch (event) {

            case XMLStreamConstants.COMMENT:
                event = xsr.nextTag();
                break;

            case XMLStreamConstants.CHARACTERS:
                event = xsr.nextTag();
                break;

            case XMLStreamConstants.START_ELEMENT:

                if (!xsr.getLocalName().equals(operatorElement)) {
                    locError = true;
                    done = true;
                    errorDescription = " Line: " + line + " Column: " + 
                            column + " Not an " + operatorElement +
                            " element: " + xsr.getLocalName();
                    break;
                }

                try {

                    nameAtt = IrcCommandBase.check(
                            xsr.getAttributeValue(null, nameAttribute)
                            , IrcParamRegex.stringUsAsciiRegex);

                    passwordAtt = IrcCommandBase.check(
                            xsr.getAttributeValue(null, 
                            passwordAttribute)
                            , IrcParamRegex.stringUsAsciiRegex);

                    ircOperatorConfig = new IrcOperatorConfig(nameAtt,
                            passwordAtt);

                    if (ircOperatorConfigMap == null) {

                        ircOperatorConfigMap =
                            new LinkedHashMap<String, IrcOperatorConfig>();
                    }

                    if (ircOperatorConfigMap.size() > 
                        db.maxIrcOperatorConfigMapSize.get()) {
                            locError = true;
                            done = true;
                            errorDescription = " Line: " + line + 
                                    " Column: " + column + 
                                    " Maximum number of operators exceed.";
                        }
                    ircOperatorConfigMap.put(nameAtt, ircOperatorConfig);

                } catch (IrcSyntaxException e) {
                    locError = true;
                    done = true;
                    errorDescription = " Line: " + line + " Column: " + 
                             column + " Syntax error near attribute(s).";
                    
                }
                catch (IndexOutOfBoundsException e) {
                    locError = true;
                    done = true;
                    errorDescription = " Line: " + line + " Column: " + 
                            column + " Need more attribute(s).";
                    
                }

                event = xsr.next();
                break;

            case XMLStreamConstants.END_ELEMENT:
                event = xsr.nextTag();
                done = true;
                break;

            default:
                locError = true;
                done = true;
                errorDescription = " Line: " + line + " Column: " + 
                		column + " Unexpected XML event: " + event;
                break;
            }
        }

        if (locError) {
            logger.log(Level.WARNING,
                    "Error in " + operatorElement + " element." + 
                    errorDescription);
        }

        error = error || locError;
    }

    /** Определение завершения элемента. */
    private void endElement() throws XMLStreamException, IOException {

        boolean locError = false;
        boolean done = false;
        int line = 0;
        int column = 0;

        event = xsr.getEventType();

        while (!done) {
            line = xsr.getLocation().getLineNumber();
            column = xsr.getLocation().getColumnNumber();

            switch (event) {

            case XMLStreamConstants.COMMENT:
                event = xsr.nextTag();
                break;

            case XMLStreamConstants.END_ELEMENT:
                event = xsr.nextTag();
                done = true;
                break;

            default:
                locError = true;
                done = true;
                errorDescription = " Line: " + line + " Column: " + 
                		column + " Unexpected XML event: " + event;
                break;
            }
        }

        if (locError) {
            logger.log(Level.WARNING,
                    "Error closing " + configElement + " element." + 
                    errorDescription);
        }

        error = error || locError;
    }

    /** Определение завершения XML-документа. */
    private void endIrcConfigFile() throws XMLStreamException, 
    		IOException {

        boolean locError = false;
        boolean done = false;
        int line = 0;
        int column = 0;

        event = xsr.getEventType();

        while (!done) {
            line = xsr.getLocation().getLineNumber();
            column = xsr.getLocation().getColumnNumber();

            switch (event) {

            case XMLStreamConstants.COMMENT:
                event = xsr.nextTag();
                break;

            case XMLStreamConstants.END_ELEMENT:
                event = xsr.next();
                break;

            case XMLStreamConstants.END_DOCUMENT:
                done = true;
                break;

            default:
                locError = true;
                done = true;
                errorDescription = " Line: " + line + " Column: " + 
                		column + " Unexpected XML event: " + event;
                break;
            }
        }

        if (event != XMLStreamConstants.END_DOCUMENT) {
            locError = true;
            done = true;
            line = xsr.getLocation().getLineNumber();
            column = xsr.getLocation().getColumnNumber();
            errorDescription = " Line: " + line + " Column: " + column +
                    " Unexpected XML event: " + event;
        }

        if (locError) {
            logger.log(Level.WARNING,
                    "Ending error: " + errorDescription);
        }
        error = error || locError;
    }
}
