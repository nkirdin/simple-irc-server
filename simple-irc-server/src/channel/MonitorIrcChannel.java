/*
 * 
 * MonitorIrcChannel 
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

/**
 * Класс, с помощью которого создаются локальные служебные каналы. 
 * Основное отличие от стандартного локального канала заключается в том, 
 * что в канале, созданным с помощью MonitorIrcChannel всегда будет, 
 * как минимум один неудаляемый пользователь (псевдопользователь 
 * anonymous). Канал создается со следующими режимами:
 * <UL>
 *      <LI> {@link ChannelMode#t};</LI>
 *      <LI> {@link ChannelMode#q};</LI>
 * </UL>
 * 
 * Псевдопользователь anonymous является оператором канала 
 * ({@link ChannelMode#o}).
 *
 * @version 0.5.1 2012-03-27
 * @author  Nikolay Kirdin 
 */
public class MonitorIrcChannel extends IrcChannel {
    
    /**
     * Конструктор.
     * @param nickname  имя канала.
     * @param topic топик канала.
     */
    public MonitorIrcChannel(String nickname, String topic) {
        super(nickname, topic);
        modeSet = EnumSet.of(ChannelMode.t, ChannelMode.q);
        if (Globals.anonymousUser.get() != null) {
            memberMap.put(Globals.anonymousUser.get(),
                    EnumSet.of(ChannelMode.o));
        }
    }
    
    /** Защита от удаления канала.  */
    public void delete() {}
}
