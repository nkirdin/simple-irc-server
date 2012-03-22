/*
 * 
 * IrcParamRegex 
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

import java.util.regex.*;

/**
 * IrcParamRegex - интерфейс, в котором хранятся строки с регулярными 
 * выражениями, которые используются при разборе сообщений. 
 *
 * @version 0.5 2012-02-07
 * @author  Nikolay Kirdin
 */
interface IrcParamRegex {
    
    //String stringWithNulCrLf = "[^\\00\\r\\n]*[\\00\\r\\n]+.*";
    /** Восьмибитные строки без симоволов NUL CR LF. */
    String noNulCrLf = "[\\x01-\\x09]|[\\x0B-\\x0C]|[\\x0E-\\xFF]";
    
    /** Восьмибитные строки без симоволов NUL CR LF и SP. */
    String nospcrlfclRegex = "[\\x01-\\x09]|[\\x0B-\\x0C]|[\\x0E-\\x1F]"
            + "|[\\x21-\\x39]|[\\x3B-\\xFF]";
            
    /** Латинские буквы. */            
    String letterRegex = "[A-Z]|[a-z]";
    
    /** Десятичные цифры. */
    String digitRegex = "\\d";
    
    /** Шестнадцетиричные цифры. */
    String hexDigitRegex = "(" + digitRegex + "|" + "[A-F]" + "){1,4}";
    
    /** Специальные символы. */
    String specialRegex = "[\\x5B-\\x60]|[\\x7B-\\x7D]";
    
    /** Метасимвол "звездочка". */
    String wildManyRegex = "\\*";
    
    /** Метасимвол "знак вопроса". */
    String wildOneRegex = "\\?"; 
    
    /** Пятизначные десятичные числа без знака. */
    String numberRegex = "\\d{1,5}";
    
    /** IP-адрес версии 4 (упрощенный вариант). */
    String ip4AddrRegex = digitRegex + "{1,3}\\." + digitRegex
            + "{1,3}\\." + digitRegex + "{1,3}\\." + digitRegex + "{1,3}";
    
    /** IP-адрес версии 6 (упрощенный вариант). */
    String ip6AddrRegex = "(" + hexDigitRegex
            + "(:" + hexDigitRegex + "){7}" + ")|(" + "0:0:0:0:0:"
            + "(0|(FFFF)):" + "(" + ip4AddrRegex + ")" + ")";

    /** IP-адрес хоста. */
    String hostAddrRegex = "(" + ip4AddrRegex + ")|(" + ip6AddrRegex + 
            ")";
    
    /** Компонент доменного имени. */
    String shortNameRegex = "([A-Za-z][A-Za-z0-9\\-]*[A-Za-z0-9]*)";
    
    /** Полное доменное имя. */
    String hostNameRegex = shortNameRegex + "(\\." + shortNameRegex + 
            ")*";

    /** Пятисимвольный идентификатор для "safe" канала. */
    String channelIdRegex = "([A-Z]|[0-9]){5}";
    
    /** Имя канала без префикса. */
    String chanStringRegex = "(" + "[\\x01-\\x06]|[\\x08-\\x09]|" +
            "[\\x0B-\\x0C]|[\\x0E-\\x1F]|[\\x21-\\x2B]|[\\x2D-\\x39]|" +
            "[\\x3B-\\xFF]" + ")" + "{1," + (Constants.CHANNELLEN - 1) 
            + "}";
    /*String channelRegex = "(" + "#" + "|" + "\\+" + "|"
            + "(!" + channelIdRegex + ")" + "|" + "&" + ")"
            + chanStringRegex + "(:" + chanStringRegex + ")*";
    */
    
    /** Имя канала с префиксом. */
    String channelRegex = "(" + "#" + "|" + "&" + ")" 
    +   chanStringRegex  + "(:" + chanStringRegex + ")*";

    /** Ключ (пароль) канала. */
    String keyRegex = "(" + "[\\x01-\\x06]|\\x08|[\\x0E-\\x1F]|" +
            "[\\x21-\\x7F]" + "){1,23}";

    /** Никнэйм пользователя. */        
    String nickNameRegex = "(" + letterRegex + "|" + specialRegex + ")"
            + "(" + letterRegex + "|" + digitRegex + "|" + specialRegex
            + "|" + "-" + "){0," + Constants.NICKLEN + "}";

    /** Login name пользователя  для его хоста. */
    String userRegex = "(" + "[\\x01-\\x09]|[\\x0B-\\x0C]|[\\x0E-\\x1F]"
            + "|[\\x22-\\x24]|[\\x26-\\x3F]|[\\x41-\\xFF]" + "){1,100}";

    /** Сетевой идентификатор хоста. */        
    String hostRegex = "(" + hostNameRegex + ")|(" + hostAddrRegex + ")";
    
    /** Сетевой идентификатор сервера. */
    String serverNameRegex = hostRegex;
    
    /** Маска доменного имени хоста. */
    String servernameMaskRegex = "((" + shortNameRegex
            + "|\\*|\\?)+\\.)*" + shortNameRegex;

    /** Регулярное выражение для адресации сообщений хостам и серверам.*/
    String targetMaskRegex = "(\\$|#)" + servernameMaskRegex;
    
    /** Получатель сообщения. */
    String msgToRegex = channelRegex + "|" + "(" + userRegex
            + "(%" + hostRegex + ")?" + "@" + serverNameRegex + ")"
            + "|" + "(" + userRegex + "%" + hostRegex + ")"
            + "|" + nickNameRegex + "|" + "(" + nickNameRegex + "!" +
            userRegex + "@" + hostRegex + ")|(" + targetMaskRegex + ")";
            
    
    //String msgTargetRegex = msgToRegex + "(" + "," + msgToRegex + ")";
    
    /** Маска для поиска клиента. */
    String userMaskRegex = "(" + nickNameRegex + "|" + wildManyRegex
            + "|" + wildOneRegex + ")+"
            + "(!" + "(" + "(" + shortNameRegex + "|" + wildManyRegex +
            "|" + wildOneRegex + ")+" + "(\\." + shortNameRegex + ")*)" 
            + "|" + hostAddrRegex + ")?" + "(%" + "(" + "(" + 
            shortNameRegex + "|" + wildManyRegex + "|" + wildOneRegex + 
            ")+" + "(\\." + shortNameRegex + ")*)" + "|"
            + hostAddrRegex + ")?" + "(@" + "(" + "(" + shortNameRegex +
            "|" + wildManyRegex + "|" + wildOneRegex + ")+" + "(\\." + 
            shortNameRegex + ")*)" + "|" + hostAddrRegex + ")?";

    /** Аргументы для команды MODE при управлении каналами. */        
    String channelModeParamsRegex = "((" + digitRegex + "{1,5})|("
            + userMaskRegex + ")|(" + keyRegex + ")|(" + nickNameRegex +
            "))+";

    /** Аргументы для команды STATS. */
    String queryRegex = "[lmou]";
    
    /** Список аргументов команды stats. */
    String queryListRegex = queryRegex + "+";

    /** Параметр команды. */
    String wordRegex = "(" + nospcrlfclRegex + "){1,50}";
    
    /** "trailing" */
    String stringRegex = "(" + noNulCrLf + "){1,510}";
    
    /** Строка состоящая только из символов ASCII. */
    String stringUsAsciiRegex = 
            "([\\x01-\\x09]|[\\x0B-\\x0C]|[\\x0E-\\x7F]){1,510}";
    
    /** Аргумент команды PASS. */
    String userPassword = "(" + nospcrlfclRegex + "){1,16}";
    
    /**  Набор символов для маски userUsernameMask. */
    String userRegexForMask = "(" + "[\\x01-\\x09]|[\\x0B-\\x0C]"
            + "|[\\x0E-\\x1F]|[\\x22-\\x24]|[\\x26-\\x29]|[\\x2B-\\x3E]"
            + "|[\\x41-\\xFF]|(\\x5C\\x2A)|(\\x5C\\x3F)" + "){1,100}";

    /** Маска login name пользователя  для его хоста. */        
    String userUsernameMask = "(" + wildManyRegex + "|"
            + wildOneRegex + ")*" + userRegexForMask + "(" + 
            wildManyRegex + "|" + wildOneRegex + "|" + nickNameRegex + 
            ")*";

    /** Маска никнэйма. */
    String userNicknameMask = "(" + wildManyRegex + "|" + wildOneRegex +
            ")*" + nickNameRegex + "(" + wildManyRegex + "|" + 
            wildOneRegex + "|" + nickNameRegex + ")*";
    
    /** Строка для формирования маски имени канала. */        
    String chanStringForMaskRegex = "("
            + "[^\\x07\\x10\\u000D\\u000A\\x20\\*\\x2C\\x3A\\x3F]|" +
            "(\\x5C\\x2A)" + "|(\\x5C\\x3F)" + "){1,49}";

/*
    String channelNicknameMaskRegex = "(" + "#" + "|" + "\\+"
            + "|" + "(!" + channelIdRegex + ")" + "|" + "&" + ")"
            + "(" + wildManyRegex + "|" + wildOneRegex + ")*"
            + chanStringForMaskRegex + "(" + chanStringForMaskRegex + "|"
            + wildManyRegex + "|" + wildOneRegex + ")*";
*/
    /** Маска имени канала. */
    String channelNicknameMaskRegex = "(" + "#" + "|" + "&" + ")"
            + "(" + wildManyRegex + "|" + wildOneRegex + ")*"
            + chanStringForMaskRegex + "(" + chanStringForMaskRegex + "|"
            + wildManyRegex + "|" + wildOneRegex + ")*";

    /** Имя сервиса. */        
    String serviceNameRegex = nickNameRegex + "(" + "@" + hostNameRegex 
            + ")?";

    /** Скомпилированные варианты регулярных выражений. */
    /** Маска доменного имени хоста. */
    Pattern servernameMaskPattern = Pattern.compile(servernameMaskRegex);
    
    /** Параметр команды. */
    Pattern wordPattern = Pattern.compile(wordRegex);
    
    /** Строка. */
    Pattern stringPattern = Pattern.compile(stringRegex);
    
    /** Сервис. */
    Pattern serviceNamePattern = Pattern.compile(serviceNameRegex);
    
    /** Элемент доменного имени. */
    Pattern shortNamePattern = Pattern.compile(shortNameRegex);
    
    /** Никнэйм. */
    Pattern nickNamePattern = Pattern.compile(nickNameRegex);
    
    /** Канал. */
    Pattern channelPattern = Pattern.compile(channelRegex);
    
    /** Адресат. */
    Pattern msgToPattern = Pattern.compile(msgToRegex);
    
    /** Маска имени канала. */
    Pattern channelNicknameMaskPattern = 
            Pattern.compile(channelNicknameMaskRegex);
    
    /** Пароль канала. */
    Pattern keyPattern = Pattern.compile(keyRegex);
}
