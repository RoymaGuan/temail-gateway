/*
 * MIT License
 *
 * Copyright (c) 2019 Syswin
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.syswin.temail.gateway.channels.clients.grpc;

import java.util.Arrays;
import java.util.Random;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CommonDataGeneUtil {

  private static final Logger LOGGER = LoggerFactory.getLogger(CommonDataGeneUtil.class);

  private static final Random RANDOM = new Random(1);

  private static final char[] UPPER_CASE = new char[26];

  private static final char[] LOWER_CASE = new char[26];

  private static final char[] NUMBER = new char[10];

  static {
    for (int i = 0; i < 26; i++) {
      UPPER_CASE[i] = (char) ((int) 'A' + i);
      LOWER_CASE[i] = (char) ((int) 'a' + i);
    }
    for (int i = 0; i < 10; i++) {
      NUMBER[i] = (char) ((int) '0' + i);
    }
    LOGGER.info("init: ");
    LOGGER.info("UPPER_CASE: " + Arrays.toString(UPPER_CASE));
    LOGGER.info("LOWER_CASE: " + Arrays.toString(LOWER_CASE));
    LOGGER.info("NUMBER" + Arrays.toString(NUMBER));
  }

  ;

  public static String extractChar(ExtractType type, int size) {
    StringBuilder sbd = new StringBuilder("");
    switch (type) {
      case UPPER: {
        for (int i = 0; i < size; i++) {
          sbd.append(UPPER_CASE[RANDOM.nextInt(UPPER_CASE.length)]);
        }
        break;
      }
      case LOWER: {
        for (int i = 0; i < size; i++) {
          sbd.append(LOWER_CASE[RANDOM.nextInt(LOWER_CASE.length)]);
        }
        break;
      }
      case NUM: {
        for (int i = 0; i < size; i++) {
          sbd.append(NUMBER[RANDOM.nextInt(NUMBER.length)]);
        }
        break;
      }
    }
    return sbd.toString();
  }

  public static String extractIp() {
    return new StringBuilder(extractChar(ExtractType.NUM, 2)).append(".")
        .append(extractChar(ExtractType.NUM, 2)).append(".")
        .append(extractChar(ExtractType.NUM, 2)).append(".")
        .append(extractChar(ExtractType.NUM, 2)).toString();
  }

  public static String extractUUID() {
    return UUID.randomUUID().toString().replace("-", "");
  }

  public enum ExtractType {UPPER, LOWER, NUM}

}
