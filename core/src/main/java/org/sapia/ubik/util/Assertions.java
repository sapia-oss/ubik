package org.sapia.ubik.util;

import java.util.Collection;

/**
 * Provides assertion methods.
 * 
 * @author yduchesne
 *
 */
public final class Assertions {
  
  private static final Object[] EMPTY_ARRAY = new Object[]{};
  
  private Assertions() {
  }
  
  public static void isTrue(boolean condition, String msg, Object...args) {
    if(!condition) {
      throw new IllegalArgumentException(String.format(msg, args));
    }
  }
  
  public static void isTrue(boolean condition, String msg) {
    isTrue(condition, msg, EMPTY_ARRAY);
  }
  
  public static void isFalse(boolean condition, String msg, Object...args) {
    if(condition) {
      throw new IllegalArgumentException(String.format(msg, args));
    }
  }
  
  public static void isFalse(boolean condition, String msg) {
    isFalse(condition, msg, EMPTY_ARRAY);
  }
  
  public static void equals(Object expected, Object actual, String msg, Object...args) {
    if(expected == null && actual == null) {
      return;
    }
    
    if(expected == null || actual == null) {
      throw new IllegalArgumentException(String.format(msg, args));
    }
    
    if(!expected.equals(actual)) {
      throw new IllegalArgumentException(String.format(msg, args));
    }
  }
  
  public static void equals(Object expected, Object actual) {
    if(expected == null && actual == null) {
      return;
    }
    if(expected == null || actual == null) {
      throw new IllegalArgumentException(String.format("Expected %s, got %s", 
          expected == null ? "null" : expected, 
          actual == null ? "null" : actual));
    }
    if(!expected.equals(actual)) {
      throw new IllegalArgumentException(String.format("Expected %s, got %s", expected, actual));
    }
  }
  
  public static void notEmpty(Collection<?> toCheck, String msg, Object...args) {
    if(toCheck.isEmpty()) {
      throw new IllegalArgumentException(String.format(msg, args));
    }
  }

  public static void notNull(Object toCheck, String msg, Object...args) {
    if(toCheck == null) {
      throw new IllegalArgumentException(String.format(msg, args));
    }
  }
  
  public static void notNull(Object toCheck, String msg) {
    notNull(toCheck, msg, EMPTY_ARRAY);
  }
  
  public static void greater(int actualValue, int expectedValue, String msg, Object...args) {
    if(actualValue <= expectedValue) {
        throw new IllegalArgumentException(String.format(msg, args));
    }
  }
  
  public static void greaterOrEqual(int actualValue, int expectedValue, String msg, Object...args) {
    if(actualValue < expectedValue) {
        throw new IllegalArgumentException(String.format(msg, args));
    }
  }
  
  public static void lower(int actualValue, int expectedValue, String msg, Object...args) {
    if(actualValue > expectedValue) {
        throw new IllegalArgumentException(String.format(msg, args));
    }
  }
  
  public static void lowerOrEqual(int actualValue, int expectedValue, String msg, Object...args) {
    if(actualValue > expectedValue) {
        throw new IllegalArgumentException(String.format(msg, args));
    }
  }  
  
  public static void illegalState(boolean condition, String msg, Object...args) {
    if (condition) {
      throw new IllegalStateException(String.format(msg, args));
    }
  }
  
  public static void illegalState(boolean condition, String msg) {
    if (condition) {
      throw new IllegalStateException(msg);
    }
  }  
}
