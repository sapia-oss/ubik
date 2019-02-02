package org.sapia.ubik.util.throttle;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Before;
import org.junit.Test;

public class NullThrottleTest {

  private NullThrottle throttle;
  
  @Before
  public void setUp() throws Exception {
    throttle = new NullThrottle();
  }

  @Test
  public void testTryAcquire() {
    assertThat(throttle.tryAcquire()).isTrue();
  }

}
