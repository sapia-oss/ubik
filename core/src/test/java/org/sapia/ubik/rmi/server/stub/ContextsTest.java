package org.sapia.ubik.rmi.server.stub;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.sapia.ubik.rmi.server.oid.DefaultOID;
import org.sapia.ubik.rmi.server.transport.memory.InMemoryAddress;

public class ContextsTest {

  List<RemoteRefContext> contextList;
  Contexts contexts;

  @Before
  public void setUp() throws Exception {
    contextList = new ArrayList<RemoteRefContext>();
    contexts = new Contexts();
    for (int i = 0; i < 5; i++) {
      RemoteRefContext ctx = new RemoteRefContext(new DefaultOID(i), new InMemoryAddress("test"));
      contextList.add(ctx);
    }
    contexts.addAll(contextList);
  }

  @Test
  public void testUpdateListener() {

    Contexts.UpdateListener listener = mock(Contexts.UpdateListener.class);
    contexts.addUpdateListener(listener);

    List<RemoteRefContext> toAdd = new ArrayList<RemoteRefContext>();
    for (int i = 5; i < 10; i++) {
      RemoteRefContext ctx = new RemoteRefContext(new DefaultOID(i), new InMemoryAddress("test"));
      toAdd.add(ctx);
    }

    contexts.addAll(toAdd);
    verify(listener, times(1)).onUpdate(any(Collection.class));
  }

  @Test
  public void testGetContexts() {
    Collection<RemoteRefContext> contextCollection = contexts.getContexts();
    assertEquals("Content not equal", contexts.count(), contextCollection.size());
  }

  @Test
  public void testRemove() {
    int originalCount = contexts.count();
    Collection<RemoteRefContext> contextCollection = contexts.getContexts();
    contexts.remove(contextCollection.iterator().next());
    assertEquals("Item was not removed", originalCount - 1, contexts.count());
  }

}
