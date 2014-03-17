package org.sapia.ubik.concurrent;

import java.util.concurrent.atomic.AtomicInteger;

import org.sapia.ubik.util.Pause;

public class Counter {

  private AtomicInteger count = new AtomicInteger();
  private int limit;

  public Counter(int limit) {
    this.limit = limit;
  }

  public void increment() {
    count.incrementAndGet();
  }

  public void add(int toAdd) {
    count.addAndGet(toAdd);
  }

  public int getCount() {
    return count.get();
  }

  public void await(long timeout) throws InterruptedException {
    Pause delay = new Pause(timeout);
    while (count.get() < limit && !delay.isOver()) {
      wait(delay.remainingNotZero());
    }
  }

}
