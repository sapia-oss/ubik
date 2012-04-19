package org.sapia.ubik.rmi.server.stub;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;

import java.lang.reflect.Proxy;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.sapia.ubik.log.Log;
import org.sapia.ubik.rmi.server.TestSocketServerTransportSetup;
import org.sapia.ubik.rmi.server.TestStatelessRemoteInterface;


public class RemoteRefStatelessTest {
  
  private TestSocketServerTransportSetup transport;
  
  @Before
  public void setUp() throws Exception {
  	Log.setDebug();
  	
    transport = new TestSocketServerTransportSetup();
    transport.setUp();
  }  
  
  @After
  public void tearDown() {
    transport.tearDown();
  }
  
  @Test
  public void testIsStatelessStub() throws Exception {    
    TestStatelessRemoteInterface remoteObject = mock(TestStatelessRemoteInterface.class);
    transport.bind("test", remoteObject);
    remoteObject = (TestStatelessRemoteInterface) transport.lookup("test");
    assertTrue(
        "Stub was not enriched for reliability", 
        Proxy.getInvocationHandler(remoteObject) instanceof RemoteRefStateless);
  }
  
  @Test
  public void testRoundRobin() throws Exception {    
  
    List<TestStatelessCounter> counters = new ArrayList<TestStatelessCounter>(3);
    for(int i = 0; i < 3; i++) {
      TestStatelessCounter counter = new TestStatelessCounter();
      counters.add(counter);
      transport.bind("test", counter);
      Thread.sleep(1000);
    }
    
    TestStatelessRemoteInterface remoteProxy = (TestStatelessRemoteInterface) transport.lookup("test");
    
    for(int i = 0; i < counters.size(); i++) {
      remoteProxy.perform();
    }

    for(TestStatelessCounter counter: counters) {
      assertEquals("Expected each remote counter to have been invoked once", 1, counter.count);
    }
  }
  
  @Test
  public void testNoFailOver() throws Exception {    
    
    List<TestFailingStatelessCounter> counters = new ArrayList<TestFailingStatelessCounter>(3);
    for(int i = 0; i < 3; i++) {
      TestFailingStatelessCounter counter = new TestFailingStatelessCounter();
      counters.add(counter);
      transport.bind("test", counter);
      Thread.sleep(1000);
    }
    
    TestStatelessRemoteInterface remoteProxy = (TestStatelessRemoteInterface) transport.lookup("test");
    
    for(int i = 0; i < counters.size(); i++) {
      remoteProxy.perform();
    }
    
    try {
      remoteProxy.perform();
      fail("Failover should not have occurred");
    } catch (RemoteException e) {
      //noop
    }
  }
  
  public static class TestStatelessCounter implements TestStatelessRemoteInterface{
    int count;
    @Override
    public void perform() throws RemoteException {
      count++;
    }
  }
  
  public static class TestFailingStatelessCounter implements TestStatelessRemoteInterface{
    int count;
    @Override
    public void perform() throws RemoteException {
      if(count == 1) {
        throw new RemoteException("Failure");
      }
      count++;
    }
  }
}
