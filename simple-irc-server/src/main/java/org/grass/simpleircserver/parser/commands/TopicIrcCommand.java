package org.grass.simpleircserver.parser.commands;
/*
 * 
 * TopicIrcCommand 
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

import org.grass.simpleircserver.base.Constants;
import org.grass.simpleircserver.base.DB;
import org.grass.simpleircserver.channel.IrcChannel;
import org.grass.simpleircserver.parser.IrcCommandReport;
import org.grass.simpleircserver.parser.IrcSyntaxException;
import org.grass.simpleircserver.talker.IrcTalker;
import org.grass.simpleircserver.talker.user.User;

/**
 * TopicIrcCommand - класс, который проверяет параметры команды IRC 
 * TOPIC и исполняет ее. 
 *
 *    <P>Command: TOPIC
 * <P>Parameters: &lt;channel&gt; [&lt;topic&gt;]
 *
 * @version 0.5 2012-02-20
 * @version 0.5.3 2015-11-05 Program units were moved from default package into packages with names. Unit tests were added.
 * @author  Nikolay Kirdin
 */
public class TopicIrcCommand extends IrcCommandBase {

    /** Название команды. */    
    public static final String commandName = "TOPIC";

    /** Параметр: &lt;channel&gt;. */
    private String channelName = null;
    
    /** Параметр: &lt;topic&gt;. */
    private String topic = null;

    public TopicIrcCommand() {}

    /** 
     * Создатель объекта команды без проверки параметров.
     * @param db репозитарий.
     * @param client источник команды.
     * @param channelName параметр &lt;channel&gt;.
     * @param topic - параметр &lt;topic&gt;.
     * @return объект команды.
     */
    public static TopicIrcCommand create(DB db,
            User client,
            String channelName,
            String topic) {
        TopicIrcCommand topicIrcCommand = new TopicIrcCommand();
        topicIrcCommand.db = db;
        topicIrcCommand.client = client;
        topicIrcCommand.channelName = channelName;
        topicIrcCommand.topic = topic;
        topicIrcCommand.setExecutable(true);
        return topicIrcCommand;
    }
    
    /** 
     * Создатель объекта команды с проверкой параметров.
     * @param pList список параметров команды.
     * @param trailing true, если в последним в списке параметров
     * находится секция "trailing".
     * @param requestor источник команды.
     * @param db репозитарий.
     * @throws IrcSyntaxException если будет обнаружена синтаксическая 
     * ошибка.
     */
    public void checking(LinkedList<String> pList,
            boolean trailing,
            IrcTalker requestor,
            DB db) {

        int index = 0;
        this.db = db;
        this.pList = pList;
        this.trailing = trailing;

        if (!(requestor instanceof User)) {
            requestor.send(errUnknownCommand(requestor, commandName));
            return;
        } 
        if (!requestor.isRegistered()) {
            requestor.send(errNotRegistered(requestor));
            return;
        } 
        
        client = (User) requestor;
        try {
            channelName = check(pList.get(index++), channelPattern);
            if (index != pList.size()) {
                if (!pList.get(index).isEmpty()) {
                    topic = check(pList.get(index++), stringPattern);
                } else {
                    topic = "";
                }
            } else {
                topic = null;
            }
            if (topic != null && topic.length() > Constants.TOPIC_LEN) {
                throw new IrcSyntaxException("Wrong topic.");
            }
            setExecutable(true);
        } catch (IrcSyntaxException e) {
            client.send(errUnknownCommand(requestor, commandName));
        } catch (IndexOutOfBoundsException e) {
            client.send(errNeedMoreParams(requestor, commandName));
        }
    }

    /** Исполнитель команды. */
    public void run() {

        if (!isExecutable()) {
            return;
        }
         
        IrcChannel ch = db.getChannel(channelName);

        if (ch == null) {
            reportList.offer(errNoSuchChannel(client, channelName));
            return;
        } 
        if (topic == null) {
            topic = ch.getTopic();
            if (topic == null || topic.isEmpty()) {
                client.send(rplNoTopic(client, ch));
            } else {
                client.send(rplTopic(client, ch, topic));
            }
            return;
        } 
        if (!client.isOperator() && !client.isMember(ch)) {
            client.send(errNotOnChannel(client, channelName));
            return;
        } 
        if (!client.isOperator() && 
            !ch.checkChannelOperator(client) && 
            !ch.isTopicable()) {
            client.send(errNotChannelOp(client, channelName));
            return;
        } 
          
        ch.setTopic(topic);
        client.send(new IrcCommandReport (
            commandName + " " + 
            makeParameterString(pList, trailing), client, client));
        ch.send(new IrcCommandReport (
            commandName + " " + 
            makeParameterString(pList, trailing), ch, client));
    }
    
}
