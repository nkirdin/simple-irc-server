/*
 *
 * IrcMatcher
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
 * Класс, проверяющий соответствие текстовой строки простейшему
 * регулярному выражению.
 *
 * @version 0.5 2012-02-14
 * @author  Nikolay Kirdin
 */
public final class IrcMatcher {
    
    /** Конструктор по умолчанию. */
    private IrcMatcher() {}
    
    /**
     * Метод, проверяющий соответствие текстовой строки простейшему
     * регулярному выражению. Используются следующие правила:
     * <UL>
     *      <LI>символ "звездочка" '*' - является метасимволом, который
     * разрешает появляться любому символу в проверяемой строке,
     * неограниченное число раз или не появляться вовсе;</LI>
     *      <LI>символ "вопросительный знак" '?' - является метасимволом,
     * который разрешает любому символу появляться в проверяемой
     * строке один раз или не появляться вовсе;</LI>
     *      <LI>эти метасимволы могут экранироваться символом обратной
     * наклонной черты '\', при экранировании символы '*' и '?'
     * обозначают самих себя;</LI>
     *      <LI>все остальные символы обозначают самих себя.</LI>
     * </UL>
     * @param pattern регулярное выражение.
     * @param string проверяемая строка.
     * @return true  если проверяемая строка соответствует регулярному
     * выражению.
     */
    public static boolean match(final String pattern, final String string) {
        int j = 0;
        int i = 0;
        boolean done = false;
        boolean match = true;
        boolean escape = false;
        loop:
            while (!done) {
                if (i == string.length()) {
                    if (!escape && (j == pattern.length() - 1) &&
                        (pattern.charAt(j) == '*')) {
                        break loop;
                    }
                    if (j != pattern.length()) {
                        match = false;
                    }
                    break loop;
                }
                if (j == pattern.length()) {
                    match = false;
                    break loop;
                }
                if (!escape) {
                    switch(pattern.charAt(j)) {
                    case (char) 0X5C:
                        escape = true;
                        j++;
                        break;
                    case '*':
                        if (j == pattern.length() - 1) {
                            break loop;
                        }
                        if ((j < pattern.length() - 1) &&
                            (i < string.length() - 1) &&
                        pattern.charAt(j + 1) == string.charAt(i + 1)) {
                            j++;
                        }
                        i++;
                        break;
                    case '?':
                        j++;
                        i++;
                        break;
                    default:
                        if (pattern.charAt(j) == string.charAt(i)) {
                            j++;
                            i++;
                        } else {
                            match = false;
                            done = true;
                        }
                        break;
                    }
                } else {
                    escape = false;
                    if (pattern.charAt(j) == string.charAt(i)) {
                        j++;
                        i++;
                    } else {
                        match = false;
                        done = true;
                    }
                }
            }
        return match;
    }
}