package org.sapia.ubik.rmi.server.stub;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.sapia.ubik.net.TCPAddress;
import org.sapia.ubik.rmi.server.oid.DefaultOID;


@RunWith(MockitoJUnitRunner.class)
public class ThreadSpecificContextListTest {
  
  @Mock
  private ContextList.Callback      callback;
  
  private ThreadSpecificContextList contexts;
  
  @Before
  public void setUp() {
    contexts = new ThreadSpecificContextList(callback);
  }
  
  @Test
  public void testRoundrobin() throws Exception {
    setUpCallback(3, 0, 1);
    
    RemoteRefContext c1 = contexts.roundrobin();
    assertEquals(0, ((TCPAddress) c1.getAddress()).getPort());
    
    RemoteRefContext c2 = contexts.roundrobin();
    assertEquals(1, ((TCPAddress) c2.getAddress()).getPort());
  }
  
  @Test
  public void testFullRoundrobin() throws Exception {
    setUpCallback(3, 0, 1);
    
    for (int i = 0; i < 3; i++) {
      contexts.roundrobin();
    }
    
    RemoteRefContext c1 = contexts.roundrobin();
    assertEquals(0, ((TCPAddress) c1.getAddress()).getPort());
  }
  
  @Test
  public void testRefresh() throws Exception {
    setUpCallback(3, 0, 1);
    
    contexts.roundrobin();

    setUpCallback(3, 3, 2);
    RemoteRefContext c1 = contexts.roundrobin();

    assertEquals(3, ((TCPAddress) c1.getAddress()).getPort());
  }
  
  @Test(expected = RemoteException.class)
  public void testNoRemoteRefContext() throws RemoteException {
    setUpCallback(0, 0, 1);
    
    contexts.roundrobin();
  }
  
  private void setUpCallback(int numContexts, int startAt, long timestamp) {
    when(callback.getContexts()).thenReturn(contexts(numContexts, startAt));
    when(callback.getTimestamp()).thenReturn(timestamp);
  }
  
  private List<RemoteRefContext> contexts(int numContexts, int startAt) {
    List<RemoteRefContext> lst = new ArrayList<>();
    for (int i = startAt; i < startAt + numContexts; i++) {
      RemoteRefContext ctx = new RemoteRefContext(
          new DefaultOID(i), 
          new TCPAddress("test", "host", i)
      );
      lst.add(ctx);
    }
    return lst;
  }

}
