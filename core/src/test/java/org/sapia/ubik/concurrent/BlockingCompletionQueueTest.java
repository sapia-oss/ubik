package org.sapia.ubik.concurrent;

import static org.junit.Assert.*;

import java.util.List;
import java.util.Random;
import java.util.concurrent.Executors;

import java.util.concurrent.ExecutorService;

import org.junit.Before;
import org.junit.Test;
import org.sapia.ubik.util.Chrono;
import org.sapia.ubik.util.Delay;
import org.sapia.ubik.util.Clock.MutableClock;

public class BlockingCompletionQueueTest {

  private BlockingCompletionQueue<Integer> queue;
  private ExecutorService executor;

  @Before
  public void setUp() throws Exception {
    queue = new BlockingCompletionQueue<Integer>(5);
    executor = Executors.newFixedThreadPool(queue.getExpectedCount());
  }

  @Test
  public void testAwait() throws Exception {
    for (int i = 0; i < queue.getExpectedCount(); i++) {
      executor.execute(new Runnable() {
        @Override
        public void run() {
          queue.add(new Random().nextInt(10));
        }
      });
    }

    List<Integer> items = queue.await();
    assertEquals(queue.getExpectedCount(), items.size());
  }

  @Test
  public void testAddWithCompleted() throws Exception {
    for (int i = 0; i < queue.getExpectedCount(); i++) {
      executor.execute(new Runnable() {
        @Override
        public void run() {
          queue.add(new Random().nextInt(10));
        }
      });
    }
    queue.await();
    queue.add(new Random().nextInt(10));
    assertTrue(queue.await().isEmpty());
  }

  @Test
  public void testAwaitWithTimeout() throws Exception {
    for (int i = 0; i < queue.getExpectedCount(); i++) {
      executor.execute(new Runnable() {
        @Override
        public void run() {
          queue.add(new Random().nextInt(10));
        }
      });
    }

    List<Integer> items = queue.await(3000);
    assertEquals(queue.getExpectedCount(), items.size());
  }

  @Test
  public void testAwaitWithTimeoutEmptyItems() throws Exception {
    MutableClock clock = new MutableClock();
    Delay delay = new Delay(clock, 2000);
    Chrono chrono = new Chrono(clock);
    List<Integer> items = queue.await(delay.remaining());
    clock.increaseCurrentTimeMillis(2000);
    assertTrue(chrono.getElapsed() >= delay.getDuration());
    assertEquals(0, delay.remaining());
    assertEquals(0, items.size());
  }

}
