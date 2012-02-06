package org.sapia.ubik.rmi.server.stats;

import org.sapia.ubik.rmi.server.stats.StatValue;

import junit.framework.TestCase;

public class StatValueTest extends TestCase {

  public StatValueTest(String arg0) {
    super(arg0);
  }

  protected void setUp() throws Exception {
    super.setUp();
  }

  public void testAvg() {
    StatValue val = new StatValue();
    val.incrementInt(4);
    val.incrementLong(3);
    Double d = new Double(val.avg(2));
    assertEquals(new Double(3.5), d);
    val.incrementDouble(8);
    d = new Double(val.avg(3));
    assertEquals(new Double(5), d);    
  }

  public void testSetGet() {
    StatValue val = new StatValue();
    val.set(10);
    assertEquals(new Double(10), new Double(val.get()));
  }

  public void testIncrementLong() {
    StatValue val = new StatValue();
    val.set(10);
    val.incrementLong(10);
    
    assertEquals(new Double(20), new Double(val.get()));    
  }

  public void testIncrementInt() {
    StatValue val = new StatValue();
    val.set(10);
    val.incrementInt(10);
    assertEquals(new Double(20), new Double(val.get()));
  }  
}
