package org.sapia.ubik.util;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.sapia.ubik.util.Clock.MutableClock;

public class DelayTest {

  private MutableClock clock;
  private Delay delay;

  @Before
  public void setUp() {
    clock = new MutableClock();
    delay = new Delay(clock, 1000);
  }

  @Test
  public void testIsOver() throws Exception {
    assertFalse(delay.isOver());
    clock.increaseCurrentTimeMillis(1000);
    assertTrue(delay.isOver());
  }

  @Test
  public void testRemaining() {
    assertEquals(1000, delay.remaining());
    clock.increaseCurrentTimeMillis(1000);
    assertEquals(0, delay.remaining());
  }

  @Test
  public void testReset() {
    clock.increaseCurrentTimeMillis(1000);
    assertEquals(0, delay.remaining());
    delay.reset();
    assertEquals(1000, delay.remaining());
  }

  @Test
  public void testRemainingNotZero() {
    assertEquals(1000, delay.remaining());
    clock.increaseCurrentTimeMillis(1000);
    assertEquals(1, delay.remainingNotZero());
  }

  @Test
  public void testGetDuration() {
    assertEquals(1000, delay.getDuration());
  }

  @Test
  public void testGetStart() {
    assertEquals(0, delay.getStart());
  }

}
