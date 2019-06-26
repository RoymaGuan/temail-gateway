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

import java.util.Random;
import java.util.function.Consumer;
import lombok.Data;

/**
 * 1、test connectoin recovery
 * 2、generate trash data to clean
 */
@Data
public class GrpcConcurrentTaskOffLine implements Runnable {

  private static final Random RANDOM = new Random(2);

  private final int cyclicTimes = 5;

  private final GrpcConcurrentData grpcConcrData;

  private final Consumer<Object> consumer;

  public GrpcConcurrentTaskOffLine(GrpcConcurrentData GrpcConcurrentData, Consumer<Object> consumer) {
    this.grpcConcrData = GrpcConcurrentData;
    this.consumer = consumer;
  }

  @Override
  public void run() {
    //start registry and heartBeat
    grpcConcrData.init4Test();
    grpcConcrData.grpcClientWrapper.initClient();
    //begin the test
    for (int i = 0; i < grpcConcrData.temailAccoutLocations.size(); i++) {
      grpcConcrData.grpcClientWrapper.syncChannelLocations(grpcConcrData.temailAccoutLocations.get(i));
    }
    if (consumer != null) {
      this.consumer.accept("finsished");
    }
  }
}
