package simpleircserver.tools;
/*
 *
 * IrcValueMonitor
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
import java.util.concurrent.atomic.*;

/**
 * Класс, который служит для вычисления среднего значения конечной
 * последовательности целых чисел.
 *
 * <P>Для вычисления среднего значения, необходимо создать объект этого
 * класса с параметром {@link #size} (этот параметр определяет
 * максимальную длину выборки),  затем, с помощью, либо
 * {@link #setValue}, либо {@link #intervalStart} и {@link #intervalEnd}
 * задать элементы последовательности.
 * <P>С помощью метода {@link #getAvgValue} вычисляется среднее
 * значение последовательности.
 *
 * <P>С помощью метода {@link #getAvgInterval} вычисляется среднее
 * значение разностей между парами элементов выборки.
 *
 * <P>С помощью методов {@link #reset} инициализируются объекты класса.
 *
 * <P>Выборка хранится в кольцевом буфере. Среднее значение  вычисляется
 * путем суммирования всех элементов выборки, с последущим делением
 * полученной суммы на количество этих величин. Необходимо учтывать, что
 * контроль переполнения суммы не производится, и при необходимости
 * нужно масштабировать добавляемые величины.
 *
 * <P> Метод {@link #setValue} задает один элемент последовательности.
 * Методы {@link #intervalStart} и {@link #intervalEnd} используются для
 * вычисления элемента последовательности как интервального значения.
 * Т.о. метод  {@link #setValue} эквивалентен последовательности вызовов
 * {@link #intervalStart} и {@link #intervalEnd}. Для каждого
 * {@link #intervalStart} должен быть вызван соответствующий
 * {@link #intervalEnd}. Например:
 * <PRE>
 * IrcAvgMeter avgMeter = new IrcAvgMeter(1000);
 * for(int i ...) {
 *      avgMeter.intervalStart(System.currentTimeMillis());
 *      ...
 *      вычисления
 *      ...
 *      avgMeter.intervalEnd(System.currentTimeMillis());
 * }
 * avgCycleDuration = avgMeter.getAvgValue();
 * </PRE>
 * или
 * <PRE>
 * IrcAvgMeter avgMeter = new IrcAvgMeter(1000);
 * for(int i ...) {
 *      ...
 *      try {
 *          avgMeter.intervalStart(System.currentTimeMillis());
 *          действия
 *          avgMeter.intervalEnd(System.currentTimeMillis());
 *      } catch (some exception) {
 *          действия
 *          avgMeter.intervalEnd(System.currentTimeMillis());
 *      }
 * }
 * avgCycleDuration = avgMeter.getAvgValue();
 * </PRE>
 *
 * @version 0.5 2012-02-19
 * @version 0.5.3 2015-11-05 Program units were moved from default package into packages with names. Unit tests were added.
 * @author  Nikolay Kirdin
 */
public class IrcAvgMeter {
    
    /**
     * Минимально необходимое количество измерений для вычисления
     * среднего значения.
     */
    public static final int MIN_SIZE = 5;
     
     /** Длина кольцевого буфера. */
     private final int size;
     
     /**
      * Индекс элемента кольцевого буфера, в который будет помещено
      * значение.
      */
     private int index;
     
     /** Массив, используемый в качестве кольцевого буфера. */
     private long[] lapValue;
     
     /**
      * Признак превышения количества введенных значений над длиной
      * буфера {@link #size}. false - количество введенных значений
      * меньше длины буфера, true - больше.
      */
     private boolean complete;
      
     /**
      * Общее количество измерений (интервал {@link #intervalStart} -
      * {@link #intervalEnd} считается как одно измерение).
      */
     public AtomicLong counter = new AtomicLong();
      
     /**
      * Конструктор. Параметр конструктора  {@link #size} должен быть
      * находится в диапазоне  {@link #MIN_SIZE} - 
      * {@link Integer#MAX_VALUE}. В том случае, если параметр
      * {@link #size} не будет удовлетворять этим условиям, то будет
      * сгенерировано исключение {@link IllegalArgumentException}.
      * @param size максимальная длина выборки.
      * @throws IllegalArgumentException  в том случае, если size
      * меньше {@link #MIN_SIZE}.
      */
    public IrcAvgMeter(int size) throws IllegalArgumentException {
          if (size < MIN_SIZE) {
               throw new IllegalArgumentException(
                       "IrcAvgMeter. size out of range: " + size);
           }
           this.size = size;
           lapValue = new long[size];
       }
    /**
     * Инициализация объекта.
     * Обнуляется кольцевой буфер, указатель буфера устанавливается в
     * начало буфера, признаку {@link #complete} присваивается
     * значение false.
     */
    public void reset() {
         index = 0;
       complete = false;
       Arrays.fill(lapValue, 0L);
    }
    /**
     * Задание одного значения последовательности.
     * @param value - значение.
     */
    public void setValue(long value) {
        if (!complete && index == lapValue.length - 1) {
            complete = true;
         }
         lapValue[index] = value;
         index = (index + 1) % lapValue.length;
         counter.getAndIncrement();
     }
     /**
      * Задание начала интервала.
      * @param value начальное значение интервала.
      */
     public void intervalStart(long value) {
         lapValue[index] = value;
     }
     
     /**
      * Задание конца интервала. Вычисление элемента выборки как
      * разности между начальным значением интервала, заданным в методе
      * {@link #intervalStart} и параметром value данного метода.
      * @param value конечное значение интервала.
      */
     public void intervalEnd(long value) {
         if (!complete && index == lapValue.length - 1) {
             complete = true;
         }
         lapValue[index] = value - lapValue[index];
         index = (index + 1) % lapValue.length;
         counter.getAndIncrement();
     }
     
     /**
      * Вычисление среднего значения последовательности.
      * @return среднее значение последовательности.
      */
     public long getAvgValue() {
         long result = 0;
         int number = 0;
         if (complete) {
             number = size;
         } else {
             number = index;
         }
         for (int i = 0; i < number; i++) {
             result += lapValue[i];
         }
         if (number != 0) {
             result = result / number;
         } else {
             result = 0;
         }
         return  result;
     }
     
     /**
      * Вычисление среднего значения интервалов.
      * В данном методе интервал определяется как разность между парой
      * значений. Среднеее значение интервалов вычисляется как отношение
      * суммы интервалов к их количеству.
      * @return среднее значение интервалов.
      */
     public long getAvgInterval() {
         long result = 0;
         int number = 0;
         int base = 0;
         int ptrLap = 0;
         int ptrLapPrev = 0;
         if (complete) {
             number = size;
             base = index % lapValue.length;
         } else {
             number = index;
         }
         for (int i = number - 1; i > 0; i--) {
             ptrLap = (base + i) % lapValue.length;
             ptrLapPrev = (base + i - 1) % lapValue.length;
             result += (lapValue[ptrLap] - lapValue[ptrLapPrev]);
         }
         if (number == 0) {
             result = 0;
         } else {
             result = result / (number - 1);
         }
         return  result;
     }

     /**
      * Вычисление предполагаемого среднего значения интервалов, при 
      * условии, что последнее значение равно value. value не 
      * сохраняется в массиве и не влияет на последующие вычисления. 
      * В данном методе интервал определяется как разность между парой
      * значений. Среднеее значение интервалов вычисляется как отношение
      * суммы интервалов к их количеству.
      * @param value предполагаемое последнее значение 
      * последовательности.
      * @return среднее значение интервалов.
      */
     public long getAvgInterval(long value) {
         long result = 0;
         int number = 0;
         int base = 0;
         int ptrLap = 0;
         int ptrLapPrev = 0;
         if (complete) {
             number = size;
             base = index % lapValue.length;
         } else {
             number = index;
         }
         for (int i = number - 1; i > 0; i--) {
             ptrLap = (base + i) % lapValue.length;
             ptrLapPrev = (base + i - 1) % lapValue.length;
             result += (lapValue[ptrLap] - lapValue[ptrLapPrev]);
         }
         if (number == 0) {
             result = 0;
         } else {
             result = (result + (value - 
                     lapValue[(base + number -1) % 
                     lapValue.length] )) / number;
         }
         return  result;
     }

     /**
      * Получение первого значения.
      * @return первое значение.
      */
     public long getFirstValue() {
         long result = 0;
         int ptr = 0;
         if (complete) {
             ptr = (index + 1) % lapValue.length ;
         }
         result = lapValue[ptr];
         return  result;
     }
     
     /**
      * Получение последнего значения.
      * @return последнее значение.
      */
     public long getLastValue() {
         long result = 0;
         int ptr = 0;
         if (complete) {
             ptr = (index + lapValue.length - 1) % lapValue.length ;
         }
         result = lapValue[ptr];
         return  result;
     }
     
     /**
      * Получение количества значений.
      * @return количество значений.
      */
     public long getCounter() {
         long result = 0;
         if (complete) {
             result = size;
         } else {
             result = (index < 1) ? 0 : index - 1;
         }
         return  result;
     }
     
}