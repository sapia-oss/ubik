package org.sapia.ubik.rmi.server.stub;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

import java.util.Collection;

import org.junit.Before;
import org.junit.Test;
import org.sapia.ubik.net.ServerAddress;
import org.sapia.ubik.rmi.server.oid.DefaultOID;
import org.sapia.ubik.rmi.server.oid.OID;
import org.sapia.ubik.rmi.server.transport.memory.InMemoryAddress;

public class ContextListTest {

  private ContextList contexts;

  @Before
  public void setUp() throws Exception {
    contexts = new ContextList();
    for (int i = 0; i < 5; i++) {
      RemoteRefContext ctx = new RemoteRefContext(new DefaultOID(i), new InMemoryAddress("test"));
      contexts.add(ctx);
    }
  }

  @Test
  public void testRemove() {
    int originalSize = contexts.count();
    long ts = contexts.getTimestamp();
    RemoteRefContext toRemove = contexts.getAll().get(0);
    contexts.remove(toRemove);
    
    assertEquals("Removal did not occur", originalSize - 1, contexts.count());
    assertNotSame("Timestamp not updated", ts, contexts.getTimestamp());
  }
  
  @Test
  public void testAdd() {
    int originalSize = contexts.count();
    long ts = contexts.getTimestamp();
    RemoteRefContext toAdd = new RemoteRefContext(mock(OID.class), mock(ServerAddress.class));
    contexts.add(toAdd);
    
    assertEquals("Updated did not occur", originalSize + 1, contexts.count());
    assertNotSame("Timestamp not updated", ts, contexts.getTimestamp());
  }

  @Test
  public void testGetAll() {
    assertEquals("Expected same count", contexts.count(), contexts.getAll().size());
  }

  @Test
  public void testOnUpdate() {
    Collection<RemoteRefContext> allContexts = contexts.getAll();
    allContexts.add(new RemoteRefContext(new DefaultOID(contexts.count()), new InMemoryAddress("test")));
    long ts = contexts.getTimestamp();
    contexts.onUpdate(allContexts);
    
    assertTrue("Update did not occur, original context list should have been updated", contexts.getAll().containsAll(allContexts));
    assertNotSame("Timestamp not updated", ts, contexts.getTimestamp());
  }

  @Test
  public void testRemovalListener() throws InterruptedException {
    ContextList.RemovalListener listener = mock(ContextList.RemovalListener.class);
    contexts.addRemovalListener(listener);
    contexts.remove(contexts.getAll().get(0));
    Thread.sleep(500);
    verify(listener, times(1)).onRemoval(any(RemoteRefContext.class));
  }

}
