package org.sapia.ubik.rmi.server.stub;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;
import java.lang.reflect.Proxy;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.sapia.ubik.net.TCPAddress;
import org.sapia.ubik.rmi.server.Hub;
import org.sapia.ubik.rmi.server.TestInMemoryTransportSetup;
import org.sapia.ubik.rmi.server.TestRemoteInterface;
import org.sapia.ubik.rmi.server.oid.DefaultOID;
import org.sapia.ubik.rmi.server.stub.RemoteRefContext;
import org.sapia.ubik.rmi.server.stub.RemoteRefEx;
import org.sapia.ubik.test.TestUtils;

public class RemoteRefExTest {

  private TestInMemoryTransportSetup transport;
  
  @Before
  public void setUp() {
    transport = new TestInMemoryTransportSetup();
    transport.setUp();
  }  
  
  @After
  public void tearDown() {
    transport.tearDown();
  }
  
  @Test
  public void testSerializeDeserialize() throws Exception {
    RemoteRefContext context = new RemoteRefContext(new DefaultOID(0), new TCPAddress("localhost", 0));
    RemoteRefEx ref = new RemoteRefEx(context);
    Object proxy;

    proxy = Proxy.newProxyInstance(
        Thread.currentThread().getContextClassLoader(),
        TestUtils.getInterfacesFor(java.rmi.Remote.class), 
        ref
    );

    byte[] bytes = TestUtils.serialize(proxy);
    proxy = TestUtils.deserialize(bytes);
  }
  
  @Test
  public void testExportConnect() throws Exception {
    TestRemoteInterface remoteObject = mock(TestRemoteInterface.class);
    
    Object remoteProxy = transport.exportObject(remoteObject);
    RemoteRefEx ref  = (RemoteRefEx)Proxy.getInvocationHandler(remoteProxy);
    assertEquals(1, Hub.getModules().getObjectTable().getRefCount(ref.getContexts().iterator().next().getOid()));
    
    remoteProxy = transport.connect();
    assertEquals(1, Hub.getModules().getObjectTable().getRefCount(ref.getContexts().iterator().next().getOid()));
  }

}
