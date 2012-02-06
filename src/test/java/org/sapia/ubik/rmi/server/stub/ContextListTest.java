package org.sapia.ubik.rmi.server.stub;

import static org.junit.Assert.assertEquals;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

import java.util.Collection;

import org.junit.Before;
import org.junit.Test;
import org.sapia.ubik.rmi.server.OID;
import org.sapia.ubik.rmi.server.transport.memory.InMemoryAddress;

public class ContextListTest {
  
  private ContextList contexts;

  @Before
  public void setUp() throws Exception {
    contexts = new ContextList();
    for(int i = 0; i < 5; i++) {
      RemoteRefContext ctx = new RemoteRefContext(new OID(i), new InMemoryAddress("test"));
      contexts.add(ctx);
    }
  }

  @Test
  public void testRemove() {
    int originalSize = contexts.count();
    RemoteRefContext toRemove = contexts.getAll().get(0); 
    contexts.remove(toRemove);
    assertEquals("Removal did not occur", originalSize - 1, contexts.count());
  }

  @Test
  public void testGetAll() {
    assertEquals("Expected same count", contexts.count(), contexts.getAll().size());
  }

  @Test
  public void testRotate() throws Exception {
    RemoteRefContext expectedNext  = contexts.getAll().get(1);
    contexts.rotate();
    assertEquals("Rotation did not occur; last should become next", expectedNext, contexts.getAll().get(0));
  }

  @Test
  public void testOnUpdate() {
    Collection<RemoteRefContext> allContexts = contexts.getAll();
    allContexts.add(new RemoteRefContext(new OID(contexts.count()), new InMemoryAddress("test")));
    contexts.onUpdate(allContexts);
    
    assertTrue(
        "Update did not occur, original context list should have been updated", 
        contexts.getAll().containsAll(allContexts));
  }

  @Test
  public void testRemovalListener() {
    ContextList.RemovalListener listener = mock(ContextList.RemovalListener.class);
    contexts.addRemovalListener(listener);
    contexts.remove(contexts.getAll().get(0));
    verify(listener, times(1)).onRemoval(any(RemoteRefContext.class));
  }

}
