package org.sapia.ubik.util;

import junit.framework.TestCase;

public class ExtendedPropertiesTest extends TestCase {


  public void testSetString() {
    assertEquals(new ExtendedProperties().setString("p", "v").toConf().getProperty("p"), "v");
  }

  public void testSetInt() {
    assertEquals(new ExtendedProperties().setInt("p", 1).toConf().getIntProperty("p"), 1);
  }

  public void testSetLong() {
    assertEquals(new ExtendedProperties().setLong("p", 1).toConf().getLongProperty("p"), 1);
  }

  public void testSetBoolean() {
    assertEquals(new ExtendedProperties().setBoolean("p", true).toConf().getBooleanProperty("p"), true);
  }

}
