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

package com.syswin.temail.gateway.service;

import static com.seanyinx.github.unit.scaffolding.Randomness.uniquify;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.waitAtMost;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import org.assertj.core.api.Assertions;
import org.awaitility.Awaitility;
import org.junit.Test;

public class PendingTaskQueueTest {

  private final List<String> receivedMessages = new ArrayList<>();
  private final String message = uniquify("hello");
  private PendingTaskQueue<String> queue =
      new PendingTaskQueue<>(100, receivedMessages::add);


  private AtomicInteger atomicInteger = new AtomicInteger(0);
  private boolean bypass = false;


  @Test
  public void shouldRunAddedTask() {
    queue.run();
    queue.addTask(message);
    waitAtMost(600, MILLISECONDS).until(() -> receivedMessages.contains(message));
  }

  @Test
  public void shouldRetryTask() {
    AtomicInteger atomicInteger = new AtomicInteger(2);
    Function<String, Boolean> consumer = msg -> {
      if (atomicInteger.getAndDecrement() > 0) {
        throw new RuntimeException("oops");
      }
      receivedMessages.add(msg);
      return true;
    };
    queue = new PendingTaskQueue<String>(100, consumer);
    queue.run();
    queue.addTask(message);
    waitAtMost(600, MILLISECONDS).until(() -> receivedMessages.contains(message));
  }

  @Test
  public void shouldInterruptTaskRunner() throws InterruptedException {
    ExecutorService scheduler = Executors.newSingleThreadExecutor();
    queue = new PendingTaskQueue<String>(100, receivedMessages::add, scheduler);
    queue.run();
    scheduler.shutdownNow();
    queue.addTask(message);
    Thread.sleep(300);
    assertThat(receivedMessages).isEmpty();
  }

  @Test
  public void testConditionDetector() throws InterruptedException {
    queue = new PendingTaskQueue<String>(2000, this::logTimeInterval);
    queue.addTask("true");
    queue.addTask("true");
    queue.addTask("true");
    queue.run();
    Awaitility.waitAtMost(200, TimeUnit.MILLISECONDS)
        .until(()->{return (receivedMessages.size() == 3
            && atomicInteger.get() == 3);});

    queue.addTask("false");
    queue.addTask("false");
    queue.addTask("false");
    queue.addTask("false");
    queue.addTask("false");
    TimeUnit.SECONDS.sleep(5);
    Assertions.assertThat((receivedMessages.size() == 3
        && atomicInteger.get() < 6));

    bypass = true;
    Awaitility.waitAtMost(3400, TimeUnit.MILLISECONDS)
        .until(()->{return (receivedMessages.size() == 8);});

  }

  private Boolean logTimeInterval(String s) {
    Boolean aBoolean = Boolean.valueOf(s);
    atomicInteger.incrementAndGet();
    boolean handleSuccess = aBoolean || bypass;
    if (handleSuccess) {
      receivedMessages.add(s);
    }else {
      queue.addTask(s);
    }
    return handleSuccess;
  }

}
