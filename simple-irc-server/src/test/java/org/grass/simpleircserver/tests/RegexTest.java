/*
 * 
 * RegexTest
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

package org.grass.simpleircserver.tests;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.grass.simpleircserver.parser.commands.IrcParamRegex;
import org.junit.Test;

/**
 * RegexTest
 * @version 0.5.3.1 2015-11-06 
 * @author  Nikolay Kirdin
 */
public class RegexTest implements IrcParamRegex {
	
	@Test
    public void regexTest() {
        /*Проверка правильности составления регулярных выражений*/
        System.out.println("--Regex------------------------------------------");
        String[] errorneous, correctous;
        //System.out.println("--Test ip4AddrRegex------------------------------");
        correctous = new String[] {"1.1.1.1", "0.0.0.0", "1.0.0.0"
                , "255.255.255.255", "2.3.4.5", "6.7.8.9", "100.255.135.9"};
        
        for (String s : correctous) {
            assertTrue(s.matches(ip4AddrRegex));
        }
        errorneous = new String[] {"-1.1.1.1", "0.-0.0.0", "1.0.-0.0"
                , "255.255.255.-255", "A.3.4.5", "a.7.8.9", "100.ff.135.9"
                , "-1.1:1.1", "0", "1.0.", "1.0", "2#.255.255.255", "%.3.4.5"
                , "22.$.8.9", "100.ff.*.9"};
        for (String s : errorneous) {
            assertFalse(s.matches(ip4AddrRegex));
        }
        //System.out.println("--Test ip4AddrRegex---------OK-------------------");
        
        //System.out.println("--Test ip6AddrRegex------------------------------");
        correctous = new String[] {"1:2:3:4:5:6:7:8", "0:0:0:0:0:0:0:0", "A:B:C:D:E:F:9:5"
            , "FFFF:FFFF:0:1:CCCC:B:DDDD:7"
                , "0:0:0:0:0:0:1.1.1.1", "0:0:0:0:0:FFFF:1.1.1.1"
                };
        for (String s : correctous) {
            assertTrue(s.matches(ip6AddrRegex));
        }
        errorneous = new String[] {"-A:B:C:D:F:1:9", "0:-0:0:0:0:O:1.1.1.1"
                , "0:0:0:0:G:FFFF:1.1.1.1", "aFFF:FFFF:0:1:CCCC:B:DDDD" 
                , "255.255.255.255", "@:#:$:%:^:*:(:)"};
        for (String s : errorneous) {
            assertFalse(s.matches(ip6AddrRegex));
        }
        //System.out.println("--Test ip6AddrRegex---------OK-------------------");
        
        //System.out.println("--Test shortNameRegex----------------------------");
        correctous = new String[] {"a", "b", "ab", "a-", "a-b", "a1"
                , "ab1", "ab-1", "ab1-", "wewr"};
        for (String s : correctous) {
            assertTrue(s.matches(shortNameRegex));
        }
        errorneous = new String[] {"-a", "1b", "_ab", "a_", "a_b", "a1?"
                , "ab\\*1", "ab-1%", "#ab1-", "@wewr"
                , "nick!user%host.name@example.com", "a#", "a~", "a$", "a%", "a^"
                , "a&", "a(", "a)", "a+", "+a", "|a", "a|", ";a", "a;", ":a", "a:"
                , "a<", "<a", "a>", ">a"};
        for (String s : errorneous) {
            assertFalse(s.matches(shortNameRegex));
        }
        //System.out.println("--Test shortNameRegex-------OK-------------------");
        
        //System.out.println("--Test hostNameRegex-----------------------------");
        correctous = new String[] {"a.a", "a", "irc", "b.a1", "ab.b.a1", "a-.b-.aaa.uui"
                , "a-b.k.l.i", "a1.a2.a3", "z1-0.m-2-3-4-5.yy"
                , "irc.example.com" };
        for (String s : correctous) {
            assertTrue(s.matches(hostNameRegex));
        }
        errorneous = new String[] {"a.a.", "b.a1!", "ab.b.a1@", "@a-.b-.aaa.uui"
                , "a-b.!.l.i", "a@1.a2.a3", "z1-0.@.m-2-3-4-5.yy"
                , "%irc.example.com", "@wewr%"
                , "nick!user%host.name@example.com"};
        for (String s : errorneous) {
            assertFalse(s.matches(hostNameRegex));
        }
        //System.out.println("--Test hostNameRegex--------OK-------------------");
        
        //System.out.println("--Test chanStringRegex---------------------------");
        correctous = new String[] {"a.a", "b.a1", "ab.b.a1", "a-.b-.aaa.uui"
                , "a-b.k.l.i", "a1.a2.a3", "z1-0.m-2-3-4-5.yy"
        , "irc.example.com", "~@$%()_{}[]|\\+\\*<>\\?", "A234567890123456789012345678901234567890123456789"};
        for (String s : correctous) {
            assertTrue(s.matches(chanStringRegex));
        }
        errorneous = new String[] {"a:a.", "b.a1!,", "a,b.b.a1@", "@a-.b-.aaa:.uui"
                , "A2345678901234567890123456789012345678901234567890"
                , "A2345678901234 567890123456789 012345678901 23456789"
                };
        for (String s : errorneous) {
            assertFalse(s.matches(chanStringRegex));
        }
        //System.out.println("--Test chanStringRegex------OK-------------------");
        
        //System.out.println("--Test channelRegex------------------------------");
        correctous = new String[] {"#1", "&{|}", "#a:$$$$$", "&a1.a2:.a3",
            /*"+@", "!ABC12a", "!12345ad", "+z1-0.m-2-3-4-5.yy@@%%^^", 
            "!A34567890123456789012345678901234567890123456789" */ 
         "#irc.example.com", "&~@&&&$%()_{}[]|\\+\\*<>\\?"};
        for (String s : correctous) {
            assertTrue(s.matches(channelRegex));
        }
        errorneous = new String[] {"a:a.", "\\*#b.a1!,", "a,b.b.a1@", "@a-.b-.aaa:.uui"
                , "&A2345678901234567890123456789012345678901234567890"
                , "#A2345678901234 567890123456789 012345678901 23456789"
                , "+@", "!ABC12a", "!12345ad", "+z1-0.m-2-3-4-5.yy@@%%^^", 
            "!A34567890123456789012345678901234567890123456789"
                };
        for (String s : errorneous) {
            assertFalse(s.matches(channelRegex));
        }
        //System.out.println("--Test channelRegex---------OK-------------------");
        
        //System.out.println("--Test keyRegex----------------------------------");
        correctous = new String[] {"#1", "+@", "!ABC12a", "&{|}", "!12345ad"
            , "#a:$$$$$", "&a1.a2:.a3", "+z1-0.m-2-3-4-5.yy@@"
        , "#irc.example.com", "&~|\\+\\*<>\\?", "!A345678901234567890123"};
        for (String s : correctous) {
            assertTrue(s.matches(keyRegex));
        }
        errorneous = new String[] {"a :a.", "\\*# b.a1!"};
        for (String s : errorneous) {
            assertFalse(s.matches(keyRegex));
        }
        //System.out.println("--Test keyRegex-------------OK-------------------");
        
        //System.out.println("--Test nickNameRegex-----------------------------");
        correctous = new String[] {"a[]`_^", "ABC12a", "z{|}", "t123{|}-", "A"
            , "a----", "A[]`_^{|}", "irc-exa", "A345678"};
        for (String s : correctous) {
            assertTrue(s.matches(nickNameRegex));
        }
        errorneous = new String[] {"a1.a2:.a3", "+z1-0.m-2-3-4-5.yy@@"
                , "#e", "e#", "&f", "j&", "+o", "o+", "1", "3e", "-pp"
                ,  "irc.example.com", "&~|\\+\\*<>\\?", "!A345678901234567890123"
        };
        for (String s : errorneous) {
            assertFalse(s.matches(nickNameRegex));
        }
        //System.out.println("--Test nickNameRegex--------OK-------------------");
        
        //System.out.println("--Test userRegex---------------------------------");
        correctous = new String[] {"a[]`_^", "ABC12a", "z{|}", "t123{|}-"
            , "a----", "irc-exa", "A345678"};
        for (String s : correctous) {
            assertTrue(s.matches(userRegex));
        }
        errorneous = new String[] {"a1@a2:.a3", "a%", "a!"
        , "A2345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901"    
        };
        for (String s : errorneous) {
            assertFalse(s.matches(userRegex));
        }
        //System.out.println("--Test userRegex------------OK-------------------");

        //System.out.println("--Test servernameMaskRegex-----------------------");
        correctous = new String[] {"a.a", "a.a.a", "a.*.a", "a.*.*.a", "a.?.a"
            , "a.?.?.a", "a.*.?.a", "*.a", "*.*.a", "?.a", "?.?.a"
            , "a.?.?.a", "a.*.?.a", "*.a", "?.*.*.a", "?.a", "?.?.a"
            , "*.*.com", "example.com", "irc.example.com"
        };
        for (String s : correctous) {
            assertTrue(s.matches(servernameMaskRegex));
        }
        errorneous = new String[] { "a.", ".a", "a.*", "a.*.*"
            , "a.*.", "a.?", "a.?.", "a.?.?", "a.?.?.", };
        for (String s : errorneous) {
            assertFalse(s.matches(servernameMaskRegex));
        }
        //System.out.println("--Test servernameMaskRegex--OK-------------------");
        
        //System.out.println("--Test targetMaskRegex---------------------------");
        correctous = new String[] {"$*.irc.com", "#irc.*.local"
        };
        for (String s : correctous) {
            assertTrue(s.matches(targetMaskRegex));
        }
        errorneous = new String[] {"*.irc.com", "irc.*.local"};
        for (String s : errorneous) {
            assertFalse(s.matches(targetMaskRegex));
        }
        //System.out.println("--Test targetMaskRegex------OK-------------------");
        
        
        //System.out.println("--Test msgToRegex--------------------------------");
        correctous = new String[] { "#2", "&3"
            , "a-%host.dom.ain@example.com", "a-@example.com", "a-%host.dom.ain"
            , "nickname", "nickname!a-@host.dom.ain", "$*.irc.com", "#irc.*.local"};
        for (String s : correctous) {
            assertTrue(s.matches(msgToRegex));
        }
        errorneous = new String[] {"a-%host.dom.ain%example.com", "!A2345channel"
            , "a-!host.dom.ain!example.com", "a-@host.dom.ain@example.com", "+4"
            , "a-@host.dom.ain%example.com", "a-@host.dom.ain%example.com!error"
            , "a-@example.!com", "a-%host.dom.@.ain"
            , "*nickname", "nickname@a-!host.dom.ain", "$*.irc.*", "$irc.*.*"
        };
        for (String s : errorneous) {
            assertFalse(s.matches(msgToRegex));
        }
        //System.out.println("--Test msgToRegex-----------OK-------------------");
        
        //System.out.println("--Test userMaskRegex-----------------------------");
        correctous = new String[] {"A23456789", "*", "A234567*","?", "A234567??"
            , "A23456789!example"
            , "A23456789!example.com", "A23456789!*", "A23456789!?"
            , "A23456789!*.com", "A23456789!?.com", "A23456789!?"
            , "A23456789%example.com", "A23456789%*", "A23456789%?"
            , "A23456789%*.com", "A23456789%?.com", "A23456789%?"
            , "A23456789@example.com", "A23456789@*", "A23456789@?"
            , "A23456789@*.com", "A23456789@?.com", "A23456789@?"
            , "A23456789!gross.local%example.com", "A23456789!*%example.com", "A23456789!?%example.com"
            , "A23456789!*.com%example.com", "A23456789!?.com%example.com", "A23456789!?%example.com"
            , "A23456789!gross.local@example.com", "A23456789!*@example.com", "A23456789!?@example.com"
            , "A23456789!*.com@example.com", "A23456789!?.com@example.com", "A23456789!?@example.com"
            , "A23456789!gross.local%example.com@acme.org", "A23456789!*%example.com@acme.org", "A23456789!?%example.com@acme.org"
            , "A23456789!*.com%example.com@acme.org", "A23456789!?.com%example.com@acme.org", "A23456789!?%example.com@acme.org"
            , "A23456789!gross.local%example.com@*.org", "A23456789!*%example.com@?.org", "A23456789!?%example.com@*.org"
            , "A23456789!*.com%example.com@*.org", "A23456789!?.com%example.com@?.org", "A23456789!?%example.com@acme.om"
            };
        for (String s : correctous) {
            assertTrue(s.matches(userMaskRegex));
        }
        errorneous = new String[] {
             "A23456789!example!"
            , "A23456789!example.com*", "A23456789!*?@", "A23456789!?%"
            , "A23456789!*.com%", "A23456789!?.com@", "A23456789!?!"
            , "A23456789%gross.local!example.com", "A23456789%*!example.com", "A23456789@?!example.com"
            , "A23456789@gross.local%example.com!*.org", "A23456789@*!example.com@?.org", "A23456789@?!example.com@!*.org"
            , "A23456789!@%!*.com%example.com@*.org", "A23456789!?.com%@@example.com@?.org", "A23456789!?%!!!example.com@acme.?"
        };
        for (String s : errorneous) {
            assertFalse(s.matches(userMaskRegex));
        }
        //System.out.println("--Test userMaskRegex--------OK-------------------");
        
        //System.out.println("--Test userUsernameMask--------------------------");
        correctous = new String[] {"a", "a*", "a?", "*a", "?a", "a*a", "a?a"
                , "a*?", "a?*", "*?a", "?*a", "a*a*", "a?a?", "*a*", "?a?"
                , "a*\\", "a\\*", "a\\?", "\\*", "\\?a", "a\\*a", "a\\?a"
                , "\\*?", "\\?*", "\\?"};
        for (String s : correctous) {
            assertTrue(s.matches(userUsernameMask));
        }
        errorneous = new String[] {"*", "?", "*?", "?*"};
        for (String s : errorneous) {
            assertFalse(s.matches(userUsernameMask));
        }
        //System.out.println("--Test userUsernameMask-----OK-------------------");
        
        //System.out.println("--Test channelNicknameMaskRegex------------------");
        correctous = new String[] {"#a*","#a\\*", "&a?", "&a\\?", "#a*a", "&a?a"
                ,  "#a*a*", "&a?a?", "&*a*", "#?a?"};
        for (String s : correctous) {
            assertTrue(s.matches(channelNicknameMaskRegex));
        }
        errorneous = new String[] {"*#a", "?+a", "*?&a", "?*!A2345", "+*a", "!A2345?a", "!A2345a?*", "!A2345*?a", "+?*a", "+a*?"};
        for (String s : errorneous) {
            assertFalse(s.matches(channelNicknameMaskRegex));
        }

        System.out.println("**Regex**************************************OK**");
        
    }
}    

