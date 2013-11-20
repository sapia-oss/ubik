package org.sapia.ubik.util;

import java.util.Collections;

import org.junit.Test;

public class AssertionsTest {

  @Test
  public void testIsTrueWithArgs() {
    Assertions.isTrue(true, "", new Object[] {});
  }

  @Test
  public void testIsTrue() {
    Assertions.isTrue(true, "");
  }

  @Test
  public void testIsFalseWithArgs() {
    Assertions.isFalse(false, "", new Object[] {});
  }

  @Test
  public void testIsFalse() {
    Assertions.isFalse(false, "");
  }

  @Test
  public void testEquals() {
    Assertions.equals("test", "test");
  }

  @Test
  public void testEqualsWithArgs() {
    Assertions.equals("test", "test", "", new Object[] {});
  }

  @Test
  public void testNotEmpty() {
    Assertions.notEmpty(Collections.singleton("test"), "", new Object[] {});
  }

  @Test
  public void testNotNullWithArgs() {
    Assertions.notNull("test", "", new Object[] {});
  }

  @Test
  public void testNotNull() {
    Assertions.notNull("test", "");
  }

  @Test
  public void testGreater() {
    Assertions.greater(2, 1, "", new Object[] {});
  }

  @Test
  public void testGreaterOrEqual() {
    Assertions.greaterOrEqual(2, 1, "", new Object[] {});
    Assertions.greaterOrEqual(1, 1, "", new Object[] {});
  }

  @Test
  public void testLower() {
    Assertions.lower(1, 2, "", new Object[] {});
  }

  @Test
  public void testLowerOrEqual() {
    Assertions.lowerOrEqual(1, 2, "", new Object[] {});
    Assertions.lowerOrEqual(1, 1, "", new Object[] {});
  }

}
