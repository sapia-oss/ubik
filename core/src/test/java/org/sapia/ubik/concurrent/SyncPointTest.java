package org.sapia.ubik.concurrent;

import static org.junit.Assert.assertTrue;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.Before;
import org.junit.Test;

public class SyncPointTest {

  private ExecutorService executor;
  private SyncPoint sync;

  @Before
  public void setUp() {
    executor = Executors.newCachedThreadPool();
    sync = new SyncPoint();
  }

  @Test
  public void testAwait() throws InterruptedException {
    executor.execute(new Runnable() {
      @Override
      public void run() {
        try {
          Thread.sleep(500);
          sync.notifyCompletion();
        } catch (InterruptedException e) {
        }
      }
    });
    sync.await();
  }

  @Test
  public void testAwaitWithTimeout() throws InterruptedException {
    executor.execute(new Runnable() {
      @Override
      public void run() {
        try {
          Thread.sleep(500);
          sync.notifyCompletion();
        } catch (InterruptedException e) {
        }
      }
    });
    assertTrue("Notification not done within delay", sync.await(3000));
  }

  @Test
  public void testAwaitMultipleTimes() throws InterruptedException {

    for (int i = 0; i < 3; i++) {
      executor.execute(new Runnable() {
        @Override
        public void run() {
          try {
            Thread.sleep(500);
            sync.notifyCompletion();
          } catch (InterruptedException e) {
          }
        }
      });
      assertTrue("Notification not done within delay", sync.await(3000));
    }
  }

}
