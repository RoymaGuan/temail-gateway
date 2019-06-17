package com.syswin.temail.gateway.service;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import lombok.extern.slf4j.Slf4j;

//now we use a async queue to retry the failed request
@Slf4j
class PendingTaskQueue<T> implements Runnable {

  private final BlockingQueue<T> pendingTaskQueue;
  private final Consumer<T> taskConsumer;
  private final Executor scheduler;
  private final int delayInMillis;

  PendingTaskQueue(int delayInMillis, Consumer<T> taskConsumer) {
    this(delayInMillis, taskConsumer, Executors.newSingleThreadScheduledExecutor());
  }

  PendingTaskQueue(int delayInMillis, Consumer<T> taskConsumer, Executor scheduler) {
    this.pendingTaskQueue = new ArrayBlockingQueue<>(512 * 4);
    this.taskConsumer = taskConsumer;
    this.scheduler = scheduler;
    this.delayInMillis = delayInMillis;
  }

  boolean addTask(T pair) {
    log.debug("add 1 task, now there is {} tasks wait to be retry!", pendingTaskQueue.size());
    return pendingTaskQueue.offer(pair);
  }

  public void run() {
    scheduler.execute(() -> {
      try {
        while (!Thread.currentThread().isInterrupted()) {
          T pair = obtainTask();
          try {
            log.debug("obtain one retry task: {}", pair);
            taskConsumer.accept(pair);
          } catch (Exception e) {
            log.error("failed to add retry task: ", e);
            pendingTaskQueue.offer(pair);
            Thread.sleep(delayInMillis);
          }
        }
      } catch (InterruptedException e) {
        log.warn("Pending task scheduler is interrupted", e);
      }
    });
  }

  private T obtainTask() throws InterruptedException {
    log.debug("remove 1 task, now there is {} tasks wait to be retry!", pendingTaskQueue.size());
    return pendingTaskQueue.take();
  }
}
