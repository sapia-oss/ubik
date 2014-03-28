package org.sapia.ubik.concurrent;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.sapia.ubik.util.SysClock;
import org.sapia.ubik.util.SysClock.MutableClock;

public class TimeIntervalBarrierTest {

  private MutableClock        clock;
  private TimeIntervalBarrier barrier;

  @Before
  public void setUp() throws Exception {
    clock = SysClock.MutableClock.getInstance();
    barrier = new TimeIntervalBarrier(clock, 30);
  }

  @Test
  public void testTryAcquire_False() {
    assertFalse(barrier.tryAcquire());
    clock.increaseCurrentTimeMillis(29);
    assertFalse(barrier.tryAcquire());
  }

  @Test
  public void testTryAcquire_True() {
    clock.increaseCurrentTimeMillis(30);
    assertTrue(barrier.tryAcquire());
    clock.increaseCurrentTimeMillis(30);
    assertTrue(barrier.tryAcquire());
  }
}
