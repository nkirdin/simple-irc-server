/*
 * 
 * TranscripFileProcessorTest
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

package simpleircserver.tests;

import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;

import org.junit.Test;

import simpleircserver.base.Constants;
import simpleircserver.base.Globals;
import simpleircserver.config.IrcTranscriptConfig;
import simpleircserver.config.ParameterInitialization;
import simpleircserver.processor.TranscriptFileProcessor;

/**
 * TranscripFileProcessorTest
 * @version 0.5.3.1 2015-11-06 
 * @author  Nikolay Kirdin
 */
public class TranscripFileProcessorTest {
	IrcTranscriptConfig ircTranscriptConfig = null;
	TranscriptFileProcessor transcriptFileProcessor = null;
	
    int rotate = 10;
    int length = 8096;

    @Test
	public void transcripFileProcessorTest() {
		System.out.println("--TranscripFileProcessor-------------------------");		

        int rotate = 3;
        int length = 1024;
        
        String logFilePath = IrcCommandTest.buildResourceFilePath(Constants.LOG_FILE_PATH);
        Globals.logFileHandlerFileName.set(logFilePath);
        ParameterInitialization.configSetup();

        ParameterInitialization.loggerSetup();
        
        
        String transcriptFilePath = IrcCommandTest.buildResourceFilePath(Constants.TRANSCRIPT_FILE_PATH);
        ircTranscriptConfig = new IrcTranscriptConfig(transcriptFilePath, 
        		rotate, length);
        		
        transcriptFileProcessor = new TranscriptFileProcessor();
        
        testTranscriptFileReduce(transcriptFilePath);
        
        testTranscriptLengthReduce(transcriptFilePath);
        
        testTranscriptLengthRestore(transcriptFilePath);
        
        testTranscriptFileRotate(transcriptFilePath);
        
        testWriteQueueElems(transcriptFilePath);
		
		System.out.println("**TranscripFileProcessor*********************OK**");
	}


    /** 
     * Проверка очистки. Количество ротированных файлов равно 
     * {@link IrcTranscriptConfig#rotate}. Должен быть удален один 
     * или несколько файлов, так, что бы их суммарный объем был 
     * больше или равен 2 * {@link IrcTranscriptConfig#length}.    
     */
	private void testTranscriptFileReduce(String transcript) {        
		int usedSpaceBeforeDeletion = 0;
        try {
        	String filename = transcript;
            for (int i = 0; i <= rotate; i++) {
            	if (i > 0 ) {
            		filename = transcript + "." + i;
            	}
            	File transcriptFile = new File(filename);
            	BufferedWriter bw = new BufferedWriter(new FileWriter(transcriptFile));
            	bw.write(filename);
            	bw.newLine();
            	for (int j = filename.length() + 1; j < length; j++) {
                    bw.write(String.valueOf(i));
            	}
                bw.close();
                usedSpaceBeforeDeletion += transcriptFile.length();
            }
        } catch (IOException e) {
            throw new Error("Error:" + e);
        }
        ircTranscriptConfig.setTranscript(transcript);
        ircTranscriptConfig.setRotate(rotate);
        ircTranscriptConfig.setLength(length);
        
        transcriptFileProcessor.setIrcTranscriptConfig(ircTranscriptConfig);
        transcriptFileProcessor.reduceRotatedFiles((long)length * 2);
        
        int usedSpaceAfterDeletion = 0;
        for ( int i = 0 ; i <= rotate; i++) {
        	String filename = transcript;
        	if (i > 0 ) {
        		filename = transcript + "." + i;
        	}
        	File transcriptFile = new File(filename);
        	if (!transcriptFile.exists()) {
        		continue;
        	}
        	usedSpaceAfterDeletion += transcriptFile.length();
        }
        assertTrue("Check old files deletion", usedSpaceBeforeDeletion - 
        		usedSpaceAfterDeletion >= length * 2);
	}
	
    /** 
     * Проверка изменения величины ограничителя длины файла. 
     * Cвободного места меньше, чем 
     * 3 * {@link IrcTranscriptConfig#length}. Должено быть 
     * вычислено граничное значение для размера файла равное одной 
     * четвертой от доступного пространства.     
     */
	private void testTranscriptLengthReduce(String transcript) {
        
        ircTranscriptConfig.setTranscript(transcript);
        ircTranscriptConfig.setRotate(rotate);
        File testingFile = new File(transcript);
        long allowableSpace = testingFile.getUsableSpace();
        ircTranscriptConfig.setLength(allowableSpace);
        
        transcriptFileProcessor.setIrcTranscriptConfig(ircTranscriptConfig);
        long usingLength = 
        		transcriptFileProcessor.makeUsingLength(allowableSpace);
        assertTrue("Change usable length", 
        		usingLength  <= allowableSpace / 4);
	}
    
	/** 
     * Проверка изменения величины ограничителя длины файла. 
     * Cвободного места больше, чем 
     * 2 * {@link IrcTranscriptConfig#length}. Должено быть 
     * восстановлено граничное значение для размера файла равное 
     * {@link IrcTranscriptConfig#length}.     
     */
	private void testTranscriptLengthRestore(String transcript) {
        ircTranscriptConfig.setTranscript(transcript);
        ircTranscriptConfig.setRotate(rotate);
        ircTranscriptConfig.setLength(length);

        transcriptFileProcessor.setIrcTranscriptConfig(ircTranscriptConfig);        
        long usingLength = 
        		transcriptFileProcessor.makeUsingLength(length);
        assertTrue("Restore usable length", usingLength == length);
	}

    /** 
     * Проверка переименования. Количество файлов равно 
     * {@link IrcTranscriptConfig#rotate}, свободное место 
     * неограничено, длина текущего файла равно или больше чем, 
     * {@link IrcTranscriptConfig#length}. Должена быть проведена 
     * ротация файлов и создан новый файл, должны быть изменены 
     * дескрипторы файлов для операций протоколирования. 
     */
	private void testTranscriptFileRotate(String transcript) {
        try {
        	String filename = transcript;
        	String firstLine = "";
            for (int i = 0; i <= rotate; i++) {
            	if (i == 0 ) {
            		firstLine = filename;
            	} else {
            		filename = transcript + "." + i;
            		firstLine = filename;
            	}
            	File transcriptFile = new File(filename);
            	BufferedWriter bw = new BufferedWriter(new FileWriter(transcriptFile));
            	bw.write(firstLine);
            	bw.newLine();
            	while (transcriptFile.length() <= length) {
                    bw.write(String.valueOf(i));
                    bw.flush();
            	}
                bw.close();
            }
        } catch (IOException e) {
            throw new Error("Error:" + e);
        }
        
        ircTranscriptConfig.setTranscript(transcript);
        ircTranscriptConfig.setRotate(rotate);        
        ircTranscriptConfig.setLength(length);
        
        transcriptFileProcessor.setIrcTranscriptConfig(ircTranscriptConfig);        
        transcriptFileProcessor.rotateFile();
        
        try {
        	String filename = transcript;
            for (int i = rotate; i >= 0; i--) {
            	if (i > 0 ) {
            		filename = transcript + "." + i;
            	} else {
            		filename = transcript;
            	}
            	File transcriptFile = new File(filename);
            	BufferedReader br = new BufferedReader(new
            			FileReader(transcriptFile));
            	String firstLine = br.readLine();
                br.close();
                if (i == 0) {
                	assertTrue("Rotation of zero file was executed.", 	
                			firstLine == null);
                	continue;
                }
                if (i == 1) {
                	assertTrue("Rotation of first file was executed.", 	
                			firstLine.equals(transcript));
                	continue;
                }
                int oLen = firstLine.length();
                int suffix = Integer.parseInt(firstLine.substring(
                		oLen - 1, oLen));
                assertTrue("Rotation was executed.", suffix == i - 1);
            }
        } catch (IOException e) {
            throw new Error("Error:" + e);
        } catch (NumberFormatException e) {
        	throw new Error("Error:" + e);
        }
	}
	
    /** 
     * Проверка записи содержимого очереди протокола на внешний носитель. 
     */
    public void testWriteQueueElems(String transcript) {   
    	BufferedReader br = null;
    	LinkedList<String> stringList = new LinkedList<String>();
        ircTranscriptConfig.setTranscript(transcript);
        ircTranscriptConfig.setRotate(rotate);        
        ircTranscriptConfig.setLength(length);
        
        
    	for (int i = 0; i < 10; i++) {
    		String outputString = System.currentTimeMillis() + " " +
    				i + "  " + ":" + 
    				"nickname!user%host.doma.in@server.doma.in" + " " +
    				"COMMAND" + " " + "some,parameters*" + " " + ":" + 
    				"And very,very,very, and very long trailing.";
    		try {
    			Thread.sleep(1);
    		} catch (InterruptedException e) {}
    		stringList.offer(outputString);
    		ircTranscriptConfig.offerToQueue(outputString);
    	}
    	
        transcriptFileProcessor.setIrcTranscriptConfig(ircTranscriptConfig);        
        transcriptFileProcessor.flushQueue();
    	
        try {
        	String filename = transcript;
        	String inLine = null;
          	File transcriptFile = new File(filename);
            br = new BufferedReader(new FileReader(transcriptFile));
            
            while ((inLine = br.readLine()) != null) {
            	assertTrue("Correct transcrip reading.", 
            			stringList.poll().equals(inLine));
            }
            assertTrue("All records readed.", stringList.isEmpty());
            
        } catch (IOException e) {
            throw new Error("Error:" + e);
        } finally {
        	try {
        		if (br != null) {
        			br.close();
        		} 
        	} catch (IOException e) {}
        }
	}

}
