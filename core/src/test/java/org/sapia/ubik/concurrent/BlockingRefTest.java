package org.sapia.ubik.concurrent;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.sapia.ubik.util.Pause;

public class BlockingRefTest {

  private BlockingRef<Integer> ref;

  @Before
  public void setUp() throws Exception {
    ref = new BlockingRef<Integer>();
  }

  @Test
  public void testAwait() throws Exception {
    Thread t = new Thread() {

      @Override
      public void run() {
        ref.set(1);
      }

    };
    t.start();

    int value = ref.await();
    assertEquals(1, value);
  }

  @Test
  public void testAwaitWithTimeout() throws Exception {
    Thread t = new Thread() {

      @Override
      public void run() {
        ref.set(1);
      }

    };
    t.start();

    int value = ref.await(1000);
    assertEquals(1, value);
  }

  @Test
  public void testAwaitNull() throws Exception {
    Thread t = new Thread() {

      @Override
      public void run() {
        ref.set(null);
      }

    };
    t.start();

    Integer value = ref.await();
    assertTrue(value == null);
  }

  @Test
  public void testAwaitWithTimeoutReached() throws Exception {
    Pause delay = new Pause(2000);
    ref.await(delay.remaining());
    assertEquals(0, delay.remaining());
  }
}
