package org.sapia.ubik.rmi.server;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.rmi.RemoteException;
import java.util.Collection;
import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.sapia.archie.impl.DefaultNameParser;
import org.sapia.archie.jndi.JndiNameParser;
import org.sapia.ubik.mcast.DomainName;
import org.sapia.ubik.mcast.memory.InMemoryBroadcastDispatcher;
import org.sapia.ubik.module.ModuleContext;
import org.sapia.ubik.module.TestModuleContext;
import org.sapia.ubik.net.TCPAddress;
import org.sapia.ubik.rmi.server.oid.DefaultOID;
import org.sapia.ubik.rmi.server.oid.OIDCreationStrategy;
import org.sapia.ubik.rmi.server.stub.RemoteRefContext;
import org.sapia.ubik.rmi.server.stub.RemoteRefReliable;
import org.sapia.ubik.rmi.server.stub.RemoteRefStateless;
import org.sapia.ubik.rmi.server.stub.Stub;
import org.sapia.ubik.rmi.server.stub.StubContainer;
import org.sapia.ubik.rmi.server.stub.StubInvocationHandler;
import org.sapia.ubik.rmi.server.stub.creation.StubCreationStrategy;
import org.sapia.ubik.rmi.server.stub.enrichment.StubEnrichmentStrategy;
import org.sapia.ubik.rmi.server.stub.enrichment.StubEnrichmentStrategy.JndiBindingInfo;
import org.sapia.ubik.rmi.server.stub.handler.StubInvocationHandlerCreationStrategy;

public class StubProcessorTest {

  private TestModuleContext moduleContext;
  private StubProcessor processor;

  @Before
  public void setUp() {
    processor = new StubProcessor();
    moduleContext = new TestModuleContext();
    moduleContext.getContainer().bind(new ObjectTable());
    moduleContext.getContainer().bind(new ServerTable());
    moduleContext.getContainer().init();
    processor.init(moduleContext);
  }

  @Test
  public void testInsertOIDCreationStrategy() {
    OIDCreationStrategy stra = mock(OIDCreationStrategy.class);
    processor.insertOIDCreationStrategy(stra);
    assertEquals("Expected mock OIDCreationStrategy", stra, processor.getOIDCreationStrategies().get(0));
  }

  @Test
  public void testAppendOIDCreationStrategy() {
    OIDCreationStrategy stra = mock(OIDCreationStrategy.class);
    processor.appendOIDCreationStrategy(stra);
    assertNotSame("Did not expect mock OIDCreationStrategy", stra, processor.getOIDCreationStrategies().get(0));
  }

  @Test
  public void testInsertHandlerCreationStrategy() {
    StubInvocationHandlerCreationStrategy stra = mock(StubInvocationHandlerCreationStrategy.class);
    processor.insertHandlerCreationStrategy(stra);
    assertEquals("Expected mock StubInvocationHandlerCreationStrategy", stra, processor.getHandlerStrategies().get(0));
  }

  @Test
  public void testAppendHandlerCreationStrategy() {
    StubInvocationHandlerCreationStrategy stra = mock(StubInvocationHandlerCreationStrategy.class);
    processor.appendHandlerCreationStrategy(stra);
    assertNotSame("Did not expect mock StubInvocationHandlerCreationStrategy", stra, processor.getHandlerStrategies().get(0));
  }

  @Test
  public void testInsertStubCreationStrategy() {
    StubCreationStrategy stra = mock(StubCreationStrategy.class);
    processor.insertStubCreationStrategy(stra);
    assertEquals("Expected mock StubCreationStrategy", stra, processor.getStubStrategies().get(0));
  }

  @Test
  public void testAppendStubCreationStrategy() {
    StubCreationStrategy stra = mock(StubCreationStrategy.class);
    processor.appendStubCreationStrategy(stra);
    assertNotSame("Did not expect mock StubCreationStrategy", stra, processor.getStubStrategies().get(0));
  }

  @Test
  public void testInsertEnrichmentStrategy() {
    StubEnrichmentStrategy stra = mock(StubEnrichmentStrategy.class);
    processor.insertStubEnrichmentStrategy(stra);
    assertEquals("Expected mock StubEnrichmentStrategy", stra, processor.getEnrichmentStrategies().get(0));
  }

  @Test
  public void testAppendEnrichmentStrategy() {
    StubEnrichmentStrategy stra = mock(StubEnrichmentStrategy.class);
    processor.appendStubEnrichmentStrategy(stra);
    assertNotSame("Did not expect mock StubEnrichmentStrategy", stra, processor.getEnrichmentStrategies().get(0));
  }

  @Test
  public void testCreateInvocationHandlerFor() throws Exception {

    StubInvocationHandlerCreationStrategy stra = new StubInvocationHandlerCreationStrategy() {
      @Override
      public void init(ModuleContext context) {
      }

      @Override
      public StubInvocationHandler createInvocationHandlerFor(Object toExport, RemoteRefContext context) throws RemoteException {
        return new TestStubInvocationHandler(context);
      }

      @Override
      public boolean apply(Object toExport) {
        return true;
      }
    };

    processor.insertHandlerCreationStrategy(stra);
    RemoteRefContext ctx = new RemoteRefContext(new DefaultOID(), new TCPAddress("test", "test", 0));
    StubInvocationHandler handler = processor.createInvocationHandlerFor(new Object(), ctx);

    assertTrue("Expected TestStubInvocationHandler", handler instanceof TestStubInvocationHandler);
    assertTrue("Collection should container " + ctx, handler.getContexts().contains(ctx));
  }

  @Test
  public void testCreateStubFor() {
    StubCreationStrategy stra = mock(StubCreationStrategy.class);
    when(stra.createStubFor(any(Object.class), any(StubInvocationHandler.class), any(Class[].class))).thenReturn(new TestRemoteIntf() {
    });
    when(stra.apply(any(Object.class), any(StubInvocationHandler.class))).thenReturn(true);

    processor.insertStubCreationStrategy(stra);
    Object stub = processor.createStubFor(new Object(), mock(StubInvocationHandler.class));

    assertTrue("Expected TestRemoteIntf instance. Got: " + stub, stub instanceof TestRemoteIntf);
  }

  @Test
  public void testReliableStubEnrichment() throws Exception {

    StubCreationStrategy stra = new StubCreationStrategy() {

      @Override
      public void init(ModuleContext context) {
      }

      @Override
      public Object createStubFor(Object exported, StubInvocationHandler handler, Class<?>[] stubInterfaces) {
        return Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), new Class[] { Stub.class, TestRemoteIntf.class }, handler);
      }

      @Override
      public boolean apply(Object exported, StubInvocationHandler handler) {
        return true;
      }
    };

    processor.insertStubCreationStrategy(stra);

    Object exported = new Object();
    RemoteRefContext ctx = new RemoteRefContext(new DefaultOID(), new TCPAddress("test", "test", 0));
    moduleContext.lookup(ObjectTable.class).register(ctx.getOid(), exported);
    Object stub = processor.createStubFor(exported, processor.createInvocationHandlerFor(exported, ctx));
    JndiBindingInfo bindingInfo = new JndiBindingInfo("ubik://localhost:1099/", new JndiNameParser(new DefaultNameParser()).parse("test/service"),
        DomainName.parse("localdomain"), new InMemoryBroadcastDispatcher.InMemoryMulticastAddress("test"));
    Object enriched = processor.enrichForJndiBinding(stub, bindingInfo);
    Object handler = Proxy.getInvocationHandler(enriched);
    assertTrue("Expected instance of RemoteRefReliable, got: " + handler, handler instanceof RemoteRefReliable);
  }

  @Test
  public void testStatelessStubEnrichment() throws Exception {

    StubCreationStrategy stra = new StubCreationStrategy() {

      @Override
      public void init(ModuleContext context) {
      }

      @Override
      public Object createStubFor(Object exported, StubInvocationHandler handler, Class<?>[] stubInterfaces) {
        return Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(),
            new Class[] { Stub.class, TestRemoteIntf.class, Stateless.class }, handler);
      }

      @Override
      public boolean apply(Object exported, StubInvocationHandler handler) {
        return true;
      }
    };

    processor.insertStubCreationStrategy(stra);

    Object exported = new Object();
    RemoteRefContext ctx = new RemoteRefContext(new DefaultOID(), new TCPAddress("test", "test", 0));
    moduleContext.lookup(ObjectTable.class).register(ctx.getOid(), exported);
    Object stub = processor.createStubFor(exported, processor.createInvocationHandlerFor(exported, ctx));
    JndiBindingInfo bindingInfo = new JndiBindingInfo("ubik://localhost:1099/", new JndiNameParser(new DefaultNameParser()).parse("test/service"),
        DomainName.parse("localdomain"), new InMemoryBroadcastDispatcher.InMemoryMulticastAddress("test"));
    Object enriched = processor.enrichForJndiBinding(stub, bindingInfo);
    Object handler = Proxy.getInvocationHandler(enriched);
    assertTrue("Expected instance of RemoteRefStateless, got: " + handler, handler instanceof RemoteRefStateless);
  }

  // ///////////////////////////////////////////////////////////////////////////

  static interface TestRemoteIntf {
  }

  // ///////////////////////////////////////////////////////////////////////////

  static class TestStubInvocationHandler implements StubInvocationHandler {

    private RemoteRefContext context;

    TestStubInvocationHandler(RemoteRefContext context) {
      this.context = context;
    }

    @Override
    public Collection<RemoteRefContext> getContexts() {
      return Collections.singleton(context);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
      return null;
    }

    @Override
    public StubContainer toStubContainer(Object proxy) {
      return new StubContainer() {
        @Override
        public Object toStub(ClassLoader loader) throws RemoteException {
          return new Object();
        }

        @Override
        public StubInvocationHandler getStubInvocationHandler() {
          return TestStubInvocationHandler.this;
        }
      };
    }

    @Override
    public boolean isValid() throws RemoteException {
      return true;
    }

  }
}
