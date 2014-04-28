package org.sapia.ubik.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

public class TimeUtilTest {


  @Before
  public void setUp() throws Exception {
  }

  @Test
  public void testCreateRandomTime() {
    Time min = Time.valueOf("10s");
    Time max = Time.valueOf("15s");
    Time rand = TimeUtil.createRandomTime(min, max);
    assertTrue("Random time expected to be greater than or equal to min time. Got: " + rand.getValue(), rand.getValueInMillis() >= min.getValueInMillis());
    assertTrue("Random time expected to be lower than or equal to min time. Got: " + rand.getValue(), rand.getValueInMillis() <= max.getValueInMillis());
  }

  @Test
  public void testParseRandomTime_Dash() {
    Time rand = TimeUtil.parseRandomTime("10s-15s");
    assertTrue("Random time expected to be greater than or equal to min time. Got: " + rand.getValue(), rand.getValueInMillis() >= 10000);
    assertTrue("Random time expected to be lower than or equal to min time. Got: " + rand.getValue(), rand.getValueInMillis() <= 15000);
  }

  @Test
  public void testParseRandomTime_Pipe() {
    Time rand = TimeUtil.parseRandomTime("10s|15s");
    assertTrue("Random time expected to be greater than or equal to min time. Got: " + rand.getValue(), rand.getValueInMillis() >= 10000);
    assertTrue("Random time expected to be lower than or equal to min time. Got: " + rand.getValue(), rand.getValueInMillis() <= 15000);
  }

  @Test
  public void testParseRandomTime_Colon() {
    Time rand = TimeUtil.parseRandomTime("10s:15s");
    assertTrue("Random time expected to be greater than or equal to min time. Got: " + rand.getValue(), rand.getValueInMillis() >= 10000);
    assertTrue("Random time expected to be lower than or equal to min time. Got: " + rand.getValue(), rand.getValueInMillis() <= 15000);
  }

  @Test
  public void testParseRandomTime_Comma() {
    Time rand = TimeUtil.parseRandomTime("10s,15s");
    assertTrue("Random time expected to be greater than or equal to min time. Got: " + rand.getValue(), rand.getValueInMillis() >= 10000);
    assertTrue("Random time expected to be lower than or equal to min time. Got: " + rand.getValue(), rand.getValueInMillis() <= 15000);
  }


  @Test
  public void testCreateRandomTime_NoMax() {
    Time rand = TimeUtil.parseRandomTime("10s");
    assertEquals(10, rand.getValueInSeconds());

  }
}
