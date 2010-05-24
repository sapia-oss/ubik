package org.sapia.ubik.rmi.examples.time;

import junit.framework.TestCase;


/**
 * @
 */
public class TimeServiceImplTest extends TestCase {
  public TimeServiceImplTest(String aName) {
    super(aName);
  }

  public void testService() throws Exception {
    System.out.println(new TimeServiceImpl().getTime());
  }
}
