package org.sapia.ubik.rmi.server.stub;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;

import org.junit.Test;
import org.sapia.ubik.rmi.server.oid.DefaultOID;
import org.sapia.ubik.rmi.server.transport.memory.InMemoryAddress;

public class RemoteRefContextTest {

  @Test
  public void testEquals() {
    RemoteRefContext context1 = new RemoteRefContext(new DefaultOID(1), new InMemoryAddress("test1"));
    RemoteRefContext context2 = new RemoteRefContext(new DefaultOID(1), new InMemoryAddress("test2"));
    assertEquals("Contexts should be considered equal", context1, context2);
  }

  @Test
  public void testNotEquals() {
    RemoteRefContext context1 = new RemoteRefContext(new DefaultOID(1), new InMemoryAddress("test1"));
    RemoteRefContext context2 = new RemoteRefContext(new DefaultOID(2), new InMemoryAddress("test2"));
    assertNotSame("Contexts should be considered equal", context1, context2);
  }

}
