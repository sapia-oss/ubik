package org.sapia.ubik.util;

import static org.junit.Assert.*;

import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;
import org.sapia.ubik.util.SysClock.MutableClock;

public class ChronoTest {

  private MutableClock clock;
  private Chrono chrono;

  @Before
  public void setUp() {
    clock = new MutableClock();
    chrono = new Chrono(clock);
  }

  @Test
  public void testGetElapsed() {
    assertEquals(0, chrono.getElapsed());
    clock.increaseCurrentTimeMillis(1000);
    assertEquals(1000, chrono.getElapsed());
  }

  @Test
  public void testGetElapsedTimeUnit() {
    assertEquals(0, chrono.getElapsed());
    clock.increaseCurrentTimeMillis(1000);
    assertEquals(1, chrono.getElapsed(TimeUnit.SECONDS));
  }

}
