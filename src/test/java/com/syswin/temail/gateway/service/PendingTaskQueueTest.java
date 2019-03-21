package com.syswin.temail.gateway.service;

import static com.seanyinx.github.unit.scaffolding.Randomness.uniquify;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.waitAtMost;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import org.junit.Test;

public class PendingTaskQueueTest {

  private final List<String> receivedMessages = new ArrayList<>();
  private final String message = uniquify("hello");
  private PendingTaskQueue<String> queue = new PendingTaskQueue<>(100, receivedMessages::add);

  @Test
  public void shouldRunAddedTask() {
    queue.run();
    queue.addTask(message);
    waitAtMost(600, MILLISECONDS).until(() -> receivedMessages.contains(message));
  }

  @Test
  public void shouldRetryTask() {
    AtomicInteger atomicInteger = new AtomicInteger(2);
    Consumer<String> consumer = msg -> {
      if (atomicInteger.getAndDecrement() > 0) {
        throw new RuntimeException("oops");
      }
      receivedMessages.add(msg);
    };
    queue = new PendingTaskQueue<>(100, consumer);
    queue.run();
    queue.addTask(message);
    waitAtMost(600, MILLISECONDS).until(() -> receivedMessages.contains(message));
  }

  @Test
  public void shouldInterruptTaskRunner() throws InterruptedException {
    ExecutorService scheduler = Executors.newSingleThreadExecutor();
    queue = new PendingTaskQueue<>(100, receivedMessages::add, scheduler);
    queue.run();
    scheduler.shutdownNow();
    queue.addTask(message);
    Thread.sleep(300);
    assertThat(receivedMessages).isEmpty();
  }
}
