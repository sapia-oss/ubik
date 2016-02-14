package org.sapia.ubik.util;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class PropUtilTest {

  @Test
  public void testIsSystemPropertyTrue() {
    System.setProperty("ubik.test", "true");
    assertTrue("Expected system property value to be true", PropUtil.isSystemPropertyTrue("ubik.test"));
  }

  @Test
  public void testIsSystemPropertyFalse() {
    System.setProperty("ubik.test", "false");
    assertTrue("Expected system property value to be false", PropUtil.isSystemPropertyFalse("ubik.test"));
  }

  @Test
  public void testClearUbikSystemProperties() {
    System.setProperty("ubik.test", "test");
    PropUtil.clearUbikSystemProperties();
    boolean deleted = System.getProperty("ubik.test") == null;
    assertTrue("Ubik system property not deleted", deleted);
  }

}
