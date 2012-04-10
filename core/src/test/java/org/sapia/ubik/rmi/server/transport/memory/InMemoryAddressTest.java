package org.sapia.ubik.rmi.server.transport.memory;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class InMemoryAddressTest {

  @Before
  public void setUp() throws Exception {
  }

  @Test
  public void testGetName() {
    InMemoryAddress address = new InMemoryAddress("test");
    assertEquals("test", address.getName());
  }

  @Test
  public void testEquals() {
    InMemoryAddress address1 = new InMemoryAddress("test");
    InMemoryAddress address2 = new InMemoryAddress("test");
    assertEquals(address1, address2);
  }
  
  @Test
  public void testNotEquals() {
    InMemoryAddress address1 = new InMemoryAddress("test1");
    InMemoryAddress address2 = new InMemoryAddress("test2");
    assertNotSame(address1, address2);
  }  

}
