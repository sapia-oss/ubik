package org.sapia.ubik.util;

import static org.junit.Assert.*;

import org.junit.Test;

public class PropertiesUtilTest {

  @Test
  public void testIsSystemPropertyTrue() {
    System.setProperty("ubik.test", "true");
    assertTrue("Expected system property value to be true", PropertiesUtil.isSystemPropertyTrue("ubik.test"));
  }

  @Test
  public void testIsSystemPropertyFalse() {
    System.setProperty("ubik.test", "false");
    assertTrue("Expected system property value to be false", PropertiesUtil.isSystemPropertyFalse("ubik.test"));
  }

  @Test
  public void testClearUbikSystemProperties() {
    System.setProperty("ubik.test", "test");
    PropertiesUtil.clearUbikSystemProperties();
    boolean deleted = System.getProperty("ubik.test") == null;
    assertTrue("Ubik system property not deleted", deleted);
  }

}
