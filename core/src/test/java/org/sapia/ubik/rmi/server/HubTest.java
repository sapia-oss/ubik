package org.sapia.ubik.rmi.server;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

import java.util.Properties;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.sapia.ubik.concurrent.BlockingRef;
import org.sapia.ubik.ioc.BeanLookup;
import org.sapia.ubik.rmi.Consts;
import org.sapia.ubik.rmi.server.transport.memory.InMemoryAddress;
import org.sapia.ubik.rmi.server.transport.memory.InMemoryTransportProvider;

public class HubTest {
  
  private BeanLookup testLookup;
  
  @Before
  public void setUp() {
    testLookup = new TestBeanLookup();
  }

  @After
  public void tearDown() {
    Hub.removeBeanLookup(testLookup);
    Hub.shutdown();
  }

  @Test
  public void testLifeCycle() {
    Hub.getModules();
    assertFalse("Hub should not be shut down", Hub.isShutdown());
    Hub.shutdown();
    assertTrue("Hub should be shut down", Hub.isShutdown());
  }

  @Test
  public void testGetBean() {
    Hub.addBeanLookup(testLookup);
    assertNotNull(Hub.getBean(Bean.class));
  }

  @Test
  public void testRemoveBeanLookup() {
    Hub.addBeanLookup(testLookup);
    Hub.removeBeanLookup(testLookup);
    assertNull(Hub.getBean(Bean.class));
  }

  @Test
  public void testExportAndConnect() throws Exception {
    Properties props = new Properties();
    props.setProperty(Consts.TRANSPORT_TYPE, InMemoryAddress.TRANSPORT_TYPE);

    TestRemoteInterface obj = mock(TestRemoteInterface.class);

    final BlockingRef<Boolean> called = new BlockingRef<Boolean>();

    doAnswer(new Answer<Void>() {

      @Override
      public Void answer(InvocationOnMock invocation) throws Throwable {
        called.set(true);
        return null;
      }
    }).when(obj).perform();

    Hub.exportObject(obj, props);
    TestRemoteInterface remoteRef = (TestRemoteInterface) Hub.connect(new InMemoryAddress(InMemoryTransportProvider.DEFAULT_SERVER_NAME));
    remoteRef.perform();
    assertTrue(called.await(3000));

  }

  public static class Bean {

  }
  
  public static class TestBeanLookup implements BeanLookup {
    @Override
    public <T> T getBean(Class<T> typeOf) {
      return typeOf.cast(new Bean());
    }
    
    @Override
    public boolean equals(Object obj) {
      return obj == this;
    }
   
  }

}
