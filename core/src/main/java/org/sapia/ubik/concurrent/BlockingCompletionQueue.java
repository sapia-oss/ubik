package org.sapia.ubik.concurrent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.sapia.ubik.util.Assertions;

/**
 * An instance of this class is used to synchronize threads: a consumer thread
 * blocks until all expected items have been added to this queue by one or more
 * producer threads.
 * 
 * @author yduchesne
 * 
 */
public class BlockingCompletionQueue<T> {

  private CountDownLatch countdown;
  private List<T> items;
  private volatile boolean completed;
  private int expectedCount;

  public BlockingCompletionQueue(int expectedCount) {
    this.countdown = new CountDownLatch(expectedCount);
    this.items = Collections.synchronizedList(new ArrayList<T>(expectedCount));
    this.expectedCount = expectedCount;
  }

  /**
   * @param item
   *          the item to add to this queue.
   */
  public void add(T item) {
    checkCompleted();
    Assertions.illegalState(countdown.getCount() == 0, "All expected items have been added");
    items.add(item);
    countdown.countDown();
  }

  /**
   * Waits until all expected items have been added to this queue.
   * 
   * @return the {@link List} of items that this queue holds.
   * @throws InterruptedException
   *           if the calling thread is interrupted while waiting.
   */
  public List<T> await() throws InterruptedException {
    checkCompleted();
    countdown.await();
    List<T> toReturn;
    synchronized (items) {
      toReturn = new ArrayList<T>(items);
      items.clear();
      completed = true;
    }
    return toReturn;
  }

  /**
   * Waits until all expected items have been added to this queue, or until the
   * given timeout is reached.
   * 
   * @param timeout
   *          the maximum amount of time that this instance should wait until
   *          this instance's items are returned.
   * @return this instance's items.
   * @throws InterruptedException
   *           if the calling thread is interrupted while waiting.
   */
  public List<T> await(long timeout) throws InterruptedException {
    checkCompleted();
    countdown.await(timeout, TimeUnit.MILLISECONDS);
    List<T> toReturn;
    synchronized (items) {
      toReturn = new ArrayList<T>(items);
      items.clear();
      completed = true;
    }
    return toReturn;
  }

  /**
   * @return the number of expected items.
   */
  public int getExpectedCount() {
    return expectedCount;
  }

  private void checkCompleted() {
    if (completed) {
      throw new IllegalStateException("This queue's items have been removed (warning: this instance should not be reused)");
    }
  }

}
