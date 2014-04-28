package org.sapia.ubik.util;

import static org.junit.Assert.assertEquals;

import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;

public class TimeTest {

  private Time t;

  @Before
  public void setUp() throws Exception {
    t = new Time(30, TimeUnit.SECONDS);
  }

  @Test
  public void testEquals() {
    Time other = new Time(30000, TimeUnit.MILLISECONDS);
    assertEquals(t, other);
  }

  @Test
  public void testGetValueInMillis() {
    assertEquals(30000, t.getValueInMillis());
  }

  @Test
  public void testGetValueInSeconds() {
    t = new Time(30000, TimeUnit.MILLISECONDS);
    assertEquals(30, t.getValueInSeconds());
  }

  @Test
  public void testCreateMillis() {
    t = Time.createMillis(30);
    assertEquals(30, t.getValueInMillis());
  }

  @Test
  public void testCreateSeconds() {
    t = Time.createSeconds(30);
    assertEquals(30, t.getValueInSeconds());
  }

  @Test
  public void testValueOf_Hour() {
    t = Time.valueOf("1h");
    assertEquals(1000 * 60 * 60, t.getValueInMillis());
  }

  @Test
  public void testValueOf_Minute() {
    t = Time.valueOf("1min");
    assertEquals(1000 * 60, t.getValueInMillis());
  }

  @Test
  public void testValueOf_Second() {
    t = Time.valueOf("1s");
    assertEquals(1000, t.getValueInMillis());
  }

  @Test
  public void testValueOf_Millis() {
    t = Time.valueOf("100ms");
    assertEquals(100, t.getValueInMillis());
  }
}
