package org.sapia.ubik.util.throttle;

import java.util.concurrent.TimeUnit;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.sapia.ubik.util.SysClock.MutableClock;

public class RateThrottleTest {
  
  private MutableClock clock;
  private RateThrottle throttle;
  
  @Before
  public void setUp() throws Exception {
    clock = new MutableClock();
    clock.increaseCurrentTimeMillis(1000);
    throttle = new RateThrottle(clock, 5, 1, TimeUnit.SECONDS);
  }

  @Test
  public void testTryAcquire_with_first_attempt() {
    assertThat(throttle.tryAcquire()).isTrue();
  }

  @Test
  public void testTryAcquire_with_threshold_reached() {
    doTryAcquire(5);
    assertThat(throttle.tryAcquire()).isFalse();
  }
  
  @Test
  public void testTryAcquire_with_after_period_reset_and_threshold_reached() {
    doTryAcquire(5);
    clock.increaseCurrentTimeMillis(1000);
    assertThat(throttle.tryAcquire()).isTrue();
  }
  
  @Test
  public void testTryAcquire_with_multiple_period_resets() {
    doTryAcquire(5);
    clock.increaseCurrentTimeMillis(1000);
    assertThat(throttle.tryAcquire()).isTrue();
    doTryAcquire(5);
    assertThat(throttle.tryAcquire()).isFalse();
    clock.increaseCurrentTimeMillis(1000);
    assertThat(throttle.tryAcquire()).isTrue();
  }
  
  private void doTryAcquire(int count) {
    for (int i = 0; i < count; i++) {
      throttle.tryAcquire();
    }
  }
}
