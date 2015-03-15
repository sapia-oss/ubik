package org.sapia.ubik.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

public class TimeRangeTest {


  @Before
  public void setUp() throws Exception {
  }

  @Test
  public void testNewTimeRange() {
    TimeValue min = TimeValue.valueOf("10s");
    TimeValue max = TimeValue.valueOf("15s");
    TimeRange range = new TimeRange(min, max);
    assertEquals(10000, range.getMin().getValueInMillis());
    assertEquals(15000, range.getMax().getValueInMillis());
  }

  @Test
  public void testCreateRandomTime() {
    TimeValue min = TimeValue.valueOf("10s");
    TimeValue max = TimeValue.valueOf("15s");
    TimeValue rand = new TimeRange(min, max).getRandomTime();
    assertTrue("Random time expected to be greater than or equal to min time. Got: " + rand.getValue(), rand.getValueInMillis() >= min.getValueInMillis());
    assertTrue("Random time expected to be lower than or equal to min time. Got: " + rand.getValue(), rand.getValueInMillis() <= max.getValueInMillis());
  }

  @Test
  public void testParseRandomTime_Dash() {
    TimeValue rand = TimeRange.valueOf("10s-15s").getRandomTime();
    assertTrue("Random time expected to be greater than or equal to min time. Got: " + rand.getValue(), rand.getValueInMillis() >= 10000);
    assertTrue("Random time expected to be lower than or equal to min time. Got: " + rand.getValue(), rand.getValueInMillis() <= 15000);
  }

  @Test
  public void testParseRandomTime_Pipe() {
    TimeValue rand = TimeRange.valueOf("10s|15s").getRandomTime();
    assertTrue("Random time expected to be greater than or equal to min time. Got: " + rand.getValue(), rand.getValueInMillis() >= 10000);
    assertTrue("Random time expected to be lower than or equal to min time. Got: " + rand.getValue(), rand.getValueInMillis() <= 15000);
  }

  @Test
  public void testParseRandomTime_Colon() {
    TimeValue rand = TimeRange.valueOf("10s:15s").getRandomTime();
    assertTrue("Random time expected to be greater than or equal to min time. Got: " + rand.getValue(), rand.getValueInMillis() >= 10000);
    assertTrue("Random time expected to be lower than or equal to min time. Got: " + rand.getValue(), rand.getValueInMillis() <= 15000);
  }

  @Test
  public void testParseRandomTime_Comma() {
    TimeValue rand = TimeRange.valueOf("10s,15s").getRandomTime();
    assertTrue("Random time expected to be greater than or equal to min time. Got: " + rand.getValue(), rand.getValueInMillis() >= 10000);
    assertTrue("Random time expected to be lower than or equal to min time. Got: " + rand.getValue(), rand.getValueInMillis() <= 15000);
  }


  @Test
  public void testCreateRandomTime_NoMax() {
    TimeValue rand = TimeRange.valueOf("10s").getRandomTime();
    assertEquals(10, rand.getValueInSeconds());

  }
}
