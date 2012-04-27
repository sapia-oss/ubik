package org.sapia.ubik.rmi.server;

import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class UIDGeneratorTest {
  
  @Before
  public void setUp() {
    UIDGenerator.reset();
  }
  
  @After
  public void tearDown() {
    UIDGenerator.reset();
  }
  
  @Test
  public void testCreateUID() {
    assertEquals(1, UIDGenerator.createUID());
    assertEquals(2, UIDGenerator.createUID());
    
  }

}
