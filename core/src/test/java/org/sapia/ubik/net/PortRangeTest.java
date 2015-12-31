package org.sapia.ubik.net;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class PortRangeTest {
  
  private PortRange range;

  @Before
  public void setUp() throws Exception {
    range = new PortRange(10, 20);
  }
  
  @Test(expected = IllegalArgumentException.class)
  public void testInvalidRange() {
    new PortRange(20, 10);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testInvalidMinPort() {
    new PortRange(-1, 20);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testInvalidMaxPort() {
    new PortRange(10, -1);
  }
  
  @Test
  public void testGetMaxPort() {
    assertEquals(20, range.getMaxPort());
  }

  @Test
  public void testGetMinPort() {
    assertEquals(10, range.getMinPort());
  }
  
  @Test
  public void testValueOf() {
    PortRange pr = PortRange.valueOf("[10 - 20]");
    assertEquals(10, pr.getMinPort());
    assertEquals(20, pr.getMaxPort());
  }

  @Test
  public void testEquals() {
    assertEquals(range, new PortRange(10, 20));
  }

  @Test
  public void testEquals_false() {
    assertNotEquals(range, new PortRange(20, 30));
  }
  
  @Test
  public void testToString() {
    assertEquals("[10 - 20]", range.toString());
  }

}
