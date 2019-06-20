package com.syswin.temail.gateway.service;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import lombok.extern.slf4j.Slf4j;

//now we use a async queue to retry the failed request
@Slf4j
class PendingTaskQueue<T> implements Runnable {

  private final BlockingQueue<T> pendingTaskQueue;
  private final Function<T, Boolean> taskConsumer;
  private final ExecutorService scheduler;
  private final int delayInMillis;

  PendingTaskQueue(int delayInMillis, Function<T, Boolean> taskConsumer) {
    this(delayInMillis, taskConsumer, Executors.newSingleThreadScheduledExecutor());
  }

  PendingTaskQueue(int delayInMillis, Function<T, Boolean> taskConsumer, ExecutorService scheduler) {
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
      T pair = null;
      boolean needSleep = false;
      while (!Thread.currentThread().isInterrupted()) {
        //fetch a task
        pair = getT();
        if (pair == null) {
          continue;
        }

        //if need wait
        if (needSleep && !tryWait()) {
          pendingTaskQueue.offer(pair);
          continue;
        }

        try {
          log.debug("Fetch a retry task: {}", pair);
          needSleep = !taskConsumer.apply(pair);
        } catch (Exception e) {
          log.error("failed to add retry task: ", e);
          pendingTaskQueue.offer(pair);
          needSleep = true;
        }
      }
    });
  }

  private T getT() {
    T pair;
    try {
      pair = pendingTaskQueue.take();
    } catch (InterruptedException e) {
      log.error("Thread interrupted while trying to fetch a task!");
      Thread.currentThread().interrupt();
      return null;
    } catch (Exception e) {
      log.warn("Exception happened while trying to fetch a task.");
      return null;
    }
    return pair;
  }


  private boolean tryWait() {
    try {
      TimeUnit.MILLISECONDS.sleep(delayInMillis);
    } catch (InterruptedException e) {
      log.error("Thread interrupted while trying to sleep for: "
          + "{} milliseconds.", delayInMillis);
      Thread.currentThread().interrupt();
      return false;
    } catch (Exception e) {
      log.warn("Exception happened while trying to fetch a task.");
      return false;
    }
    return true;
  }

  public void shutDown(){
    this.scheduler.shutdown();
  }

}
