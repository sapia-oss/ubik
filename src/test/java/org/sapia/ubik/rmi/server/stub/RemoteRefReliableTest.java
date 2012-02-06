package org.sapia.ubik.rmi.server.stub;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

import java.lang.reflect.Proxy;
import java.rmi.RemoteException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.sapia.ubik.net.TCPAddress;
import org.sapia.ubik.rmi.server.OID;
import org.sapia.ubik.rmi.server.TestRemoteInterface;
import org.sapia.ubik.rmi.server.TestSocketServerTransportSetup;
import org.sapia.ubik.rmi.server.stub.RemoteRefContext;
import org.sapia.ubik.rmi.server.stub.RemoteRefReliable;
import org.sapia.ubik.test.TestUtils;


public class RemoteRefReliableTest {

  private TestSocketServerTransportSetup transport;
  
  @Before
  public void setUp() throws Exception {
    transport = new TestSocketServerTransportSetup();
    transport.setUp();
  }  
  
  @After
  public void tearDown() {
    transport.tearDown();
  }
  
  @Test
  public void testSerializeDeserialize() throws Exception {
    RemoteRefContext context = new RemoteRefContext(new OID(0), new TCPAddress("localhost", 0));
    RemoteRefReliable ref = new RemoteRefReliable(context, "ubik://localhost/test");

    Object proxy = Proxy.newProxyInstance(
        Thread.currentThread().getContextClassLoader(),
        TestUtils.getInterfacesFor(java.rmi.Remote.class), 
        ref
    );
    TestUtils.serialize(proxy);
    byte[] b = TestUtils.serialize(proxy);
    proxy = TestUtils.deserialize(b);
  }
 
  @Test
  public void testIsReliableStub() throws Exception {    
  
    TestRemoteInterface remoteObject = mock(TestRemoteInterface.class);
    
    transport.bind("test", remoteObject);
    
    remoteObject = (TestRemoteInterface) transport.lookup("test");
    
    assertTrue(
        "Stub was not enriched for reliability", 
        Proxy.getInvocationHandler(remoteObject) instanceof RemoteRefReliable);
  }
  
  @Test
  public void testFailOver() throws Exception {    
    
    TestRemoteInterface faultedRemoteObject = mock(TestRemoteInterface.class);
    doAnswer(new Answer<Void>() {
      @Override
      public Void answer(InvocationOnMock invocation) throws Throwable {
        throw new RemoteException("Unavailable");
      }
    }).when(faultedRemoteObject).perform();
    
    TestRemoteInterface remoteObject        = mock(TestRemoteInterface.class);
    
    transport.bind("test", faultedRemoteObject);
    transport.bind("test", remoteObject);
    
    TestRemoteInterface remoteProxy = (TestRemoteInterface) transport.lookup("test");
    remoteProxy.perform();
  }
  
  @Test
  public void testNoFailOver() throws Exception {    
    
    TestRemoteInterface faultedRemoteObject = mock(TestRemoteInterface.class);
    doAnswer(new Answer<Void>() {
      @Override
      public Void answer(InvocationOnMock invocation) throws Throwable {
        throw new RemoteException("Unavailable");
      }
    }).when(faultedRemoteObject).perform();
    
    transport.bind("test", faultedRemoteObject);
    
    TestRemoteInterface remoteProxy = (TestRemoteInterface) transport.lookup("test");
    try {
      remoteProxy.perform();
      fail("Failover should not have occurred");
    } catch (RemoteException e) {
      //noop
    }
  }

}
