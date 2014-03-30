package org.sapia.ubik.rmi.naming.remote;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.lang.reflect.Method;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Properties;

import javax.naming.NamingException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.sapia.ubik.net.ServerAddress;
import org.sapia.ubik.rmi.naming.remote.discovery.ServiceDiscoveryEvent;
import org.sapia.ubik.rmi.server.oid.DefaultOID;
import org.sapia.ubik.rmi.server.stub.RemoteRefContext;
import org.sapia.ubik.rmi.server.stub.StubContainer;
import org.sapia.ubik.rmi.server.stub.StubInvocationHandler;
import org.sapia.ubik.util.Func;

@RunWith(MockitoJUnitRunner.class)
public class LazyStubInvocationHandlerTest {

  @Mock
  private StubInvocationHandler     delegate;
  @Mock
  private RemoteContext             context;
  private LazyStubInvocationHandler handler;
  @Mock
  private Func<Void, LazyStubInvocationHandler> function;
  
  @Before
  public void setUp() throws Exception {
    handler = new LazyStubInvocationHandler("test", context, function);
    
    Collection<RemoteRefContext> refContexts = new ArrayList<>();
    refContexts.add(new RemoteRefContext(new DefaultOID(1), Mockito.mock(ServerAddress.class)));

    when(delegate.getContexts()).thenReturn(refContexts);
    when(delegate.isValid()).thenReturn(true);
  }
  
  @Test
  public void testToStubContainer() {
  }

  @Test
  public void testGetContexts_ServiceDiscovered() {
    handler.setDelegate(delegate);
    assertEquals(1, handler.getContexts().size());
  }
  
  @Test
  public void testGetContexts_ServiceNotDiscovered() {
    assertEquals(0, handler.getContexts().size());
  }

  public void testInvoke() throws Throwable {
    handler.setDelegate(delegate);
    handler.invoke(null, null, null);
    verify(delegate).invoke(any(), any(Method.class), any(Object[].class));
  }
  
  @Test(expected = NamingException.class)
  public void testInvoke_ServiceNotDiscovered() throws Throwable {
    when(context.lookup(anyString())).thenThrow(new NamingException());
    handler.invoke(null, null, null);
  }

  @Test
  public void testIsValid() throws RemoteException {
    handler.setDelegate(delegate);
    assertTrue(handler.isValid());
  }
  
  @Test
  public void testIsValid_DelegateNotSet() throws Exception {
    when(context.lookup("anyString")).thenThrow(new NamingException(""));
    
    assertFalse(handler.isValid());
  }  

  @Test
  public void testIsValid_RemoteException() throws RemoteException {
    when(delegate.isValid()).thenThrow(new RemoteException("test"));
    assertFalse(handler.isValid());
  }  
  
  @Test
  public void testOnServiceDiscovered() throws RemoteException {
    StubContainer container = Mockito.mock(StubContainer.class);
    when(container.toStub(any(ClassLoader.class))).thenReturn(container);
    when(container.getStubInvocationHandler()).thenReturn(delegate);
    ServiceDiscoveryEvent evt = new ServiceDiscoveryEvent(new Properties(), "/test", container);
    handler.onServiceDiscovered(evt);
    assertNotNull(handler.getDelegate());
  }

}
