package org.grass.simpleircserver.parser;

/*
 * 
 * Reply 
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
 * Enum, хранящий шаблоны формализованных сообщений, которые 
 * возвращаются клиенту в качестве результата исполнения команды.
 *
 * @version 0.5.2 2012-03-29
 * @version 0.5.3 2015-11-05 Program units were moved from default package into packages with names. Unit tests were added.
 * @author  Nikolay Kirdin
 * 
 */
public enum Reply {
    RPL_OK ( 0, "RPL_OK", "%s", "<string>"),
    RPL_WELCOME ( 1, "RPL_WELCOME", "001 %s :Welcome to the Internet Relay Network %s!%s@%s", "<nick>!<user>@<host>"),
    RPL_YOURHOST ( 2, "RPL_YOURHOST", "002 %s :Your host is %s, running version %s", "<servername> <ver>"),
    RPL_CREATED ( 3, "RPL_CREATED", "003 %s :This server was created %s", "<date>"),
    RPL_MYINFO ( 4, "RPL_MYINFO", "004 %s %s %s %s %s", "<servername> <version> <available user modes><available channel modes>"),
    //        RPL_BOUNCE          ( 005, "RPL_BOUNCE", "5 %s :Try server <server name>, port <port number>", "<server name> <port number>"),
    RPL_ISUPPORT ( 5, "RPL_ISUPPORT", "005 %s %s :are supported by this server", "<parms>"),
    RPL_TRACELINK ( 200, "RPL_TRACELINK", "200 %s Link <version & debug level> <destination> <next server> V<protocol version> <link uptime in seconds> <backstream sendq> <upstream sendq>"),
    RPL_TRACECONNECTING ( 201, "RPL_TRACECONNECTING", "201 %s Try. <class> <server>"),
    RPL_TRACEHANDSHAKE ( 202, "RPL_TRACECONNECTING", "202 %s H.S. <class> <server>"),
    RPL_TRACEUNKNOWN ( 203, "RPL_TRACEUNKNOWN", "203 %s ???? <class> [<client IP address in dot form>]"),
    RPL_TRACEOPERATOR ( 204, "RPL_TRACEOPERATOR", "204 %s Oper <class> <nick>"),
    RPL_TRACEUSER ( 205, "RPL_TRACEUSER", "205 %s User <class> <nick>"),
    RPL_TRACESERVER ( 206, "RPL_TRACESERVER", "206 %s Serv <class> <int>S <int>C <server> <nick!user|*!*>@<host|server> V<protocol version>"),
    RPL_TRACESERVICE ( 207, "RPL_TRACESERVICE", "207 %s Service <class> <name> <type> <active type>"),
    RPL_TRACENEWTYPE ( 208, "RPL_TRACENEWTYPE", "208 %s <newtype> 0 <client name>"),
    RPL_TRACECLASS ( 209, "RPL_TRACECLASS", "209 %s Class <class> <count>"),
    RPL_TRACERECONNECT ( 210, "RPL_TRACERECONNECT", "210 %s Unused."),
    RPL_STATSLINKINFO ( 211, "RPL_STATSLINKINFO", "211 %s %s %s %s %s %s %s %s", "<linkname> <sendq> <sent messages> <sent Kbytes> <received messages> <received Kbytes> <time open>"),
    RPL_STATSCOMMANDS ( 212, "RPL_STATSCOMMANDS", "212 %s %s %s %s %s %s", "<command> <count> <byte count> <remote count> <avg duration(ms)>"),
    RPL_ENDOFSTATS ( 219, "RPL_ENDOFSTATS", "219 %s %s :End of STATS report", "<stats letter>"),
    RPL_UMODEIS ( 221, "RPL_UMODEIS", "221 %s %s", "<user mode string> "),
    RPL_SERVLIST ( 234, "RPL_SERVLIST", "234 %s %s %s %s %s %s %s", "<name> <server> <mask> <type> <hopcount> <info>"),
    RPL_SERVLISTEND ( 235, "RPL_SERVLISTEND", "235 %s %s %s :End of service listing", "<mask> <type>"),
    RPL_STATSUPTIME ( 242, "RPL_STATSUPTIME", "242 %s :Server Up %s days %s:%2s:%2s", "<days><hours><minutes><secondes>"),
    RPL_STATSOLINE ( 243, "RPL_STATSOLINE", "243 %s O %s * %s", "<hostmask><name>"),
    RPL_LUSERCLIENT ( 251, "RPL_LUSERCLIENT", "251 %s :There are %s users and %s services on %s servers", "<integer><integer><integer>"),
    RPL_LUSEROP ( 252, "RPL_LUSEROP", "252 %s %s :operator(s) online", "<integer>"),
    RPL_LUSERUNKNOWN ( 253, "RPL_LUSERUNKNOWN", "253 %s %s :unknown connection(s)", "<integer>"),
    RPL_LUSERCHANNELS ( 254, "RPL_LUSERCHANNELS", "254 %s %s :channels formed", "<integer>"),
    RPL_LUSERME ( 255, "RPL_LUSERME", "255 %s :I have %s clients and %s servers", "<integer><integer>"),
    RPL_ADMINME ( 256, "RPL_ADMINME", "256 %s %s :Administrative info", "<server>"),
    RPL_ADMINLOC1 ( 257, "RPL_ADMINLOC1", "257 %s :%s", "<admin info>"),
    RPL_ADMINLOC2 ( 258, "RPL_ADMINLOC2", "258 %s :%s", "<admin info>"),
    RPL_ADMINEMAIL ( 259, "RPL_ADMINEMAIL", "259 %s :%s", "<admin info>"),
    RPL_TRACELOG ( 261, "RPL_TRACELOG", "261 %s File <logfile> <debug level>"),
    RPL_TRACEEND ( 262, "RPL_TRACEEND", "262 %s <server name> <version & debug level> :End of TRACE"),
    RPL_TRYAGAIN ( 263, "RPL_TRYAGAIN", "263 %s %s :Please wait a while and try again.", "<command>"),
    RPL_AWAY ( 301, "RPL_AWAY", "301 %s %s :%s", "<nick> :<away message>"),
    RPL_USERHOST ( 302, "RPL_USERHOST", "302 %s :%s", "*1<reply> *( \" \" <reply> )"),
    RPL_ISON ( 303, "RPL_ISON", "303 %s :%s", "*1<nick> *( \" \" <nick> )"),
    RPL_UNAWAY ( 305, "RPL_UNAWAY", "305 %s :You are no longer marked as being away"),
    RPL_NOWAWAY ( 306, "RPL_NOWAWAY", "306 %s :You have been marked as being away"),
    RPL_WHOISUSER ( 311, "RPL_WHOISUSER", "311 %s %s %s %s * :%s", "<nick> <user> <host> * :<real name>"),
    RPL_WHOISSERVER ( 312, "RPL_WHOISSERVER", "312 %s %s %s :%s", "<nick> <server> :<server info>"),
    RPL_WHOISOPERATOR ( 313, "RPL_WHOISOPERATOR", "313 %s %s :is an IRC operator", "<nick>"),
    RPL_WHOWASUSER ( 314, "RPL_WHOWASUSER", "314 %s %s %s %s * :%s", "<nick> <user> <host> <real name>"),
    RPL_ENDOFWHO ( 315, "RPL_ENDOFWHO", "315 %s %s :End of WHO list", "<name>"),
    RPL_WHOISIDLE ( 317, "RPL_WHOISIDLE", "317 %s %s %s :seconds idle", "<nick> <integer>"),
    RPL_ENDOFWHOIS ( 318, "RPL_ENDOFWHOIS", "318 %s %s :End of WHOIS list", "<nick>"),
    RPL_WHOISCHANNELS ( 319, "RPL_WHOISCHANNELS", "319 %s %s :%s", "<nick> *( ( \"@\" / \"+\" ) <channel> \" \" )"),
    RPL_LISTSTART ( 321, "RPL_LISTSTART", "321 %s Obsolete. Not used."),
    RPL_LIST ( 322, "RPL_LIST", "322 %s %s", "<channel> <# visible> <topic>"),
    RPL_LISTEND ( 323, "RPL_LISTEND", "323 %s :End of LIST"),
    RPL_CHANNELMODEIS ( 324, "RPL_CHANNELMODEIS", "324 %s %s %s", "<channel> <mode params>"),
    RPL_UNIQOPIS ( 325, "RPL_UNIQOPIS", "325 %s %s %s", "<channel> <nickname>"),
    RPL_NOTOPIC ( 331, "RPL_NOTOPIC", "331 %s %s :No topic is set", "<channel>"),
    RPL_TOPIC ( 332, "RPL_TOPIC", "332 %s %s :%s", "<channel> <topic>"),
    RPL_INVITING ( 341, "RPL_INVITING", "341 %s %s %s", " <channel> <nick>"),
    RPL_SUMMONING ( 342, "RPL_SUMMONING", "342 %s %s :Summoning user to IRC", "<user>"),
    RPL_INVITELIST ( 346, "RPL_INVITELIST", "346 %s %s %s", "<channel> <invitemask>"),
    RPL_ENDOFINVITELIST ( 347, "RPL_ENDOFINVITELIST", "347 %s %s :End of channel invite list", "<channel>"),
    RPL_EXCEPTLIST ( 348, "RPL_EXCEPTLIST", "348 %s %s %s", "<channel> <exceptionmask>"),
    RPL_ENDOFEXCEPTLIST ( 349, "RPL_ENDOFEXCEPTLIST", "349 %s %s :End of channel exception list", "<channel>"),
    RPL_VERSION ( 351, "RPL_VERSION", "351 %s %s.%s %s :%s", "<version>.<debuglevel> <server> :<comments>"),
    RPL_WHOREPLY ( 352, "RPL_WHOREPLY", "352 %s %s %s %s %s %s %s :%s %s", "<channel> <user> <host> <server> <nick> ( \"H\" / \"G\" > [\"*\"] [ ( \"@\" / \"+\" ) ] :<hopcount> <real name>"),
    RPL_NAMREPLY ( 353, "RPL_NAMREPLY", "353 %s %s%s :%s%s", "<channel status> <channel> <user status> <nick> "),
    RPL_KILLDONE ( 361, "RPL_KILLDONE", "361 %s %s : Killed by %s.", "<nickname> <user>"),
    RPL_LINKS ( 364, "RPL_LINKS", "364 %s %s %s :%s %s", "<mask> <server> <hopcount> <server info>"),
    RPL_ENDOFLINKS ( 365, "RPL_ENDOFLINKS", "365 %s %s :End of LINKS list", "<mask>"),
    RPL_ENDOFNAMES ( 366, "RPL_ENDOFNAMES", "366 %s %s :End of NAMES list", "<channel>"),
    RPL_BANLIST ( 367, "RPL_BANLIST", "367 %s %s %s", "<channel> <banmask>"),
    RPL_ENDOFBANLIST ( 368, "RPL_BANLIST", "368 %s %s :End of channel ban list", "<channel>"),
    RPL_ENDOFWHOWAS ( 369, "RPL_ENDOFWHOWAS", "369 %s %s :End of WHOWAS", "<nick>"),
    RPL_INFO ( 371, "RPL_INFO", "371 %s :", "<string>"),
    RPL_MOTD ( 372, "RPL_MOTD", "372 %s :- ", "<text>"),
    RPL_ENDOFINFO ( 374, "RPL_ENDOFINFO", "374 %s :End of INFO list"),
    RPL_MOTDSTART ( 375, "RPL_MOTDSTART", "375 %s :- %s Message of the day - ", "<server>"),
    RPL_ENDOFMOTD ( 376, "RPL_ENDOFMOTD", "376 %s :End of MOTD command"),
    RPL_YOUREOPER ( 381, "RPL_YOUREOPER", "381 %s :You are now an IRC operator"),
    RPL_REHASHING ( 382, "RPL_REHASHING", "382 %s %s :Rehashing", "<config file>"),
    RPL_YOURESERVICE ( 383, "RPL_YOURESERVICE", "383 %s :You are service %s", "<servicename>"),
    RPL_TIME ( 391, "RPL_TIME", "391 %s %s :%s", "<server> <string showing server's local time>"),
    RPL_USERSSTART ( 392, "RPL_USERSSTART", "392 %s :UserID   Terminal  Host"),
    RPL_USERS ( 393, "RPL_USERS", "393 %s : %s %s %s", "<username> <ttyline> <hostname>"),
    RPL_ENDOFUSERS ( 394, "RPL_ENDOFUSERS", "394 %s :End of users"),
    RPL_NOUSERS ( 395, "RPL_NOUSERS", "395 %s :Nobody logged in"),

    ERR_NOTOK (400, "ERR_NOTOK", "%s", "<prefix> <string>"),
    ERR_NOSUCHNICK ( 401, "ERR_NOSUCHNICK", "401 %s %s :No such nick/channel", "<nickname>"),
    ERR_NOSUCHSERVER ( 402, "ERR_NOSUCHSERVER", "402 %s %s :No such server", "<server name>"),
    ERR_NOSUCHCHANNEL ( 403, "ERR_NOSUCHCHANNEL", "403 %s %s :No such channel", "<channel name>"),
    ERR_CANNOTSENDTOCHAN ( 404, "ERR_CANNOTSENDTOCHAN", "404 %s %s :Cannot send to channel", "<channel name>"),
    ERR_TOOMANYCHANNELS ( 405, "ERR_TOOMANYCHANNELS", "405 %s %s :You have joined too many channels", "<channel name>"),
    ERR_WASNOSUCHNICK ( 406, "ERR_WASNOSUCHNICK", "406 %s %s :There was no such nickname", "<nickname>"),
    ERR_TOOMANYTARGETS ( 407, "ERR_TOOMANYTARGETS", "407 %s %s <target> :<error code> recipients. <abort message>"),
    ERR_NOSUCHSERVICE ( 408, "ERR_NOSUCHSERVICE", "408 %s %s :No such service", "<service name>"),
    ERR_NOORIGIN ( 409, "ERR_NOORIGIN", "409 %s :No origin specified"),
    ERR_NORECIPIENT ( 411, "ERR_NORECIPIENT", "411 %s :No recipient given (%s)", "<command>"),
    ERR_NOTEXTTOSEND ( 412, "ERR_NOTEXTTOSEND", "412 %s :No text to send"),
    ERR_NOTOPLEVEL ( 413, "ERR_NOTOPLEVEL", "413 %s %s :No toplevel domain specified", "<mask>"),
    ERR_WILDTOPLEVEL ( 414, "ERR_WILDTOPLEVEL", "414 %s %s :Wildcard in toplevel domain", "<mask>"),
    ERR_BADMASK ( 415, "ERR_BADMASK", "415 %s %s :Bad Server/host mask", "<mask>"),
    ERR_UNKNOWNCOMMAND ( 421, "ERR_UNKNOWNCOMMAND", "421 %s %s :Unknown command", "<command>"),
    ERR_NOMOTD ( 422, "ERR_NOMOTD", "422 %s :MOTD File is missing"),
    ERR_NOADMININFO ( 423, "ERR_NOADMININFO", "423 %s %s :No administrative info available", "<server>"),
    ERR_FILEERROR ( 424, "ERR_FILEERROR", "424 %s :File error doing %s on %s", "<file op>  <file>"),
    ERR_NONICKNAMEGIVEN ( 431, "ERR_NONICKNAMEGIVEN", "431 %s :No nickname given"),
    ERR_ERRONEUSNICKNAME ( 432, "ERR_ERRONEUSNICKNAME", "432 %s %s :Erroneous nickname", "<nick>"),
    ERR_NICKNAMEINUSE ( 433, "ERR_NICKNAMEINUSE", "433 %s %s :Nickname is already in use", "<nick>"),
    ERR_NICKCOLLISION ( 436, "ERR_NICKCOLLISION", "436 %s <nick> :Nickname collision KILL from <user>@<host>"),
    ERR_UNAVAILRESOURCE ( 437, "ERR_UNAVAILRESOURCE", "437 %s %s/%s :Nick/channel is temporarily unavailable", "<nick/channel>"),
    ERR_USERNOTINCHANNEL ( 441, "ERR_USERNOTINCHANNEL", "441 %s %s %s :They aren't on that channel", "<nick> <channel>"),
    ERR_NOTONCHANNEL ( 442, "ERR_NOTONCHANNEL", "442 %s %s :You're not on that channel", "<channel>"),
    ERR_USERONCHANNEL ( 443, "ERR_USERONCHANNEL", "443 %s %s %s :is already on channel", "<user> <channel>"),
    ERR_NOLOGIN ( 444, "ERR_NOLOGIN", "444 %s %s :User not logged in", "<user>"),
    ERR_SUMMONDISABLED ( 445, "ERR_SUMMONDISABLED", "445 %s :SUMMON has been disabled"),
    ERR_USERSDISABLED ( 446, "ERR_USERSDISABLED", "446 %s :USERS has been disabled"),
    ERR_NOTREGISTERED ( 451, "ERR_NOTREGISTERED", "451 %s :You have not registered"),
    ERR_NEEDMOREPARAMS ( 461, "ERR_NEEDMOREPARAMS", "461 %s %s :Not enough parameters", "<command>"),
    ERR_ALREADYREGISTRED ( 462, "ERR_ALREADYREGISTRED", "462 %s :Unauthorized command (already registered)"),
    ERR_NOPERMFORHOST ( 463, "ERR_NOPERMFORHOST", "463 %s :Your host isn't among the privileged"),
    ERR_PASSWDMISMATCH ( 464, "ERR_PASSWDMISMATCH", "464 %s :Password incorrect"),
    ERR_YOUREBANNEDCREEP ( 465, "ERR_YOUREBANNEDCREEP", "465 %s :You are banned from this server"),
    ERR_YOUWILLBEBANNED ( 466, "ERR_YOUWILLBEBANNED", "466 %s Sent by a server to a user to inform that access to the server will soon be denied."),
    ERR_KEYSET ( 467, "ERR_KEYSET", "467 %s %s :Channel key already set", "<channel>"),
    ERR_CHANNELISFULL ( 471, "ERR_CHANNELISFULL", "471 %s %s :Cannot join channel (+l)", "<channel>"),
    ERR_UNKNOWNMODE ( 472, "ERR_UNKNOWNMODE", "472 %s %s :is unknown mode char to me for %s", "<char><channel>"),
    ERR_INVITEONLYCHAN ( 473, "ERR_INVITEONLYCHAN", "473 %s %s :Cannot join channel (+i)", "<channel>"),
    ERR_BANNEDFROMCHAN ( 474, "ERR_BANNEDFROMCHAN", "474 %s %s :Cannot join channel (+b)", "<channel>"),
    ERR_BADCHANNELKEY ( 475, "ERR_BADCHANNELKEY", "475 %s %s :Cannot join channel (+k)", "<channel>"),
    ERR_BADCHANMASK ( 476, "ERR_BADCHANMASK", "476 %s %s :Bad Channel Mask", "<channel>"),
    ERR_NOCHANMODES ( 477, "ERR_NOCHANMODES", "477 %s %s :Channel doesn't support modes", "<channel>"),
    ERR_BANLISTFULL ( 478, "ERR_BANLISTFULL", "478 %s %s %s :Channel list is full", "<channel> <char>"),
    ERR_NOPRIVILEGES ( 481, "ERR_NOPRIVILEGES", "481 %s :Permission Denied- You're not an IRC operator"),
    ERR_CHANOPRIVSNEEDED ( 482, "ERR_CHANOPRIVSNEEDED", "482 %s %s :You're not channel operator", "<channel>"),
    ERR_CANTKILLSERVER ( 483, "ERR_CANTKILLSERVER", "483 %s :You can't kill a server!"),
    ERR_RESTRICTED ( 484, "ERR_RESTRICTED", "484 %s :Your connection is restricted!"),
    ERR_UNIQOPPRIVSNEEDED ( 485, "ERR_UNIQOPPRIVSNEEDED", "485 %s :You're not the original channel operator"),
    ERR_NOOPERHOST ( 491, "ERR_NOOPERHOST", "491 %s :No O-lines for your host"),
    ERR_UMODEUNKNOWNFLAG ( 501, "ERR_UMODEUNKNOWNFLAG", "501 %s :Unknown MODE flag"),
    ERR_USERSDONTMATCH ( 502, "ERR_USERSDONTMATCH", "502 %s :Cannot change mode for other users");

    /** Цифровой код формализованного сообщения. */
    public final int code;

    /** Текстовое название формализованного сообщения. */
    public final String string;

    /** Строка формата для создания текстового сообщения. */
    public final String format;

    private Reply(int code, String string, String format) {
        this.code = code;
        this.string = string;
        this.format = format;
    }

    private Reply(int code, String string, String format, 
            String formatArgs) {
        this.code = code;
        this.string = string;
        this.format = format;
    }

    /**
     * Создание формализованного сообщения.
     * @param reply тип формализованного сообщения.
     * @param args параметры сообщения.
     * @return строка с формализованным сообщением. 
     */
    public static String makeText(Reply reply, String... args) {
        return String.format(reply.format, (Object[]) args);
    }

}