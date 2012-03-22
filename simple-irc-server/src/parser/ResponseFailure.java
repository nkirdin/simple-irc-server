/*
 * 
 * ResponseFailure 
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
 * Класс, который хранит формализованые сообщения, которые возвращаются 
 * клиенту в качестве неудачного результата исполнения команды.
 *
 * @version 0.5 2012-02-14
 * @author  Nikolay Kirdin
 * 
 */
public class ResponseFailure extends Response {
    
    /** Конструтктор по умолчанию. */
    public ResponseFailure (){}
    
    /**
     * Конструктор.
     * @param reply формализованный результат исполнения команды IRC.
     */
    public ResponseFailure (Reply reply){
        super(reply);
    }
    
    /**
     * Создатель формализованного ответа.
     * @param reply тип формализованного ответа.
     * @param sender отправитель.
     * @param args параметры формализованного ответа.
     * @return формализованный ответ с параметрами.
     */
    public static ResponseFailure create(Response.Reply reply, 
            IrcTalker sender, String... args) {
        ResponseFailure response = new ResponseFailure(reply);
        response.setText(":" + sender.getNickname() + " " + 
                makeText(reply, args));
        return response;  
    }

    /**
     * Создатель формализованного ответа.
     * @param reply тип формализованного ответа.
     * @param args параметры формализованного ответа.
     * @return формализованный ответ с параметрами.
     */
    public static ResponseFailure create(Response.Reply reply, 
            String... args) {
        ResponseFailure response = new ResponseFailure(reply);
        response.setText(makeText(reply, args));
        return response;  
    }

}
