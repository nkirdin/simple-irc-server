package simpleircserver.tools;
/*
 * 
 * IrcFileFormat
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

import java.util.*;
import java.io.*;

/**
 * Класс, который используется для форматирования строк текста.
 *
 * @version 0.5 2012-03-17
 * @version 0.5.3 2015-11-05 Program units were moved from default package into packages with names. Unit tests were added.
 * @author  Nikolay Kirdin
 */
public final class IrcFileFormat {
    
    /** Конструктор. */
    private IrcFileFormat() {}
    
    /**
     * Форматирование строк текста из файла.
     * В процессе форматирования строки текста ограничиваются по длине.
     * Часть строки, выходящая за границу, переносится на следующую строку.
     * Объем выводимого текста ограничивается, либо количеством строк, 
     * либо количеством символов, ограничение производится по тому 
     * ограничителю, который будет достигнут первым.
     * @param truncLen количество символов, на которое небходимо 
     * укоротить строку.
     * @param maxLines ограничитель количества строк в выводимом тексте.
     * @param maxChars ограничитель количества символов в выводимом 
     * тексте.
     * @return отформатированныи текст в виде списка строк или null, 
     * если при обращении к файлу было сгенерировано исключение.
     */
    public static List<String> getFormattedText(String inputFilename, 
            int truncLen, int maxLines, int maxChars) {
        int lineIndex = 0;
        int lineCount = 0;
        int charCount = 0;
        int outputLength = 0;
        String inputLine = null;
        String outputString = null;
        List<String> result = new ArrayList<String>();
        File inputFile = new File(inputFilename);
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(inputFile));
            while ((inputLine = br.readLine()) != null &&
                    lineCount < maxLines && 
                    charCount < maxChars) {
                lineIndex = 0;
                do {
                    outputLength = Math.min(inputLine.length() - lineIndex,
                            maxLines - truncLen);
                    outputString = inputLine.substring(lineIndex,
                            lineIndex + outputLength);
                    result.add(outputString);
                    lineCount++;
                    lineIndex = lineIndex + outputLength;
                } while (lineIndex < inputLine.length());
                charCount += inputLine.length();
            }
        } catch (IOException e) {
            result = null;
        } finally {
            try {
                if (br != null) {
                    br.close();
                }
            } catch (IOException e) {}
        }
        return result;
    }
}