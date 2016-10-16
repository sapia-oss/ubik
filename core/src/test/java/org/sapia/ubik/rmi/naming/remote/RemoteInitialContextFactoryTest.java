package org.sapia.ubik.rmi.naming.remote;

import static org.junit.Assert.fail;

import java.util.Hashtable;

import javax.naming.InitialContext;
import javax.naming.NameNotFoundException;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.sapia.ubik.mcast.EventChannel;
import org.sapia.ubik.rmi.Consts;
import org.sapia.ubik.util.Conf;
import org.sapia.ubik.util.PropUtil;

public class RemoteInitialContextFactoryTest {

  private EmbeddableJNDIServer server;

  @Before
  public void setUp() throws Exception {
    EventChannel.disableReuse();
    PropUtil.clearUbikSystemProperties();
    System.setProperty(Consts.UBIK_DOMAIN_NAME, "test");
    System.setProperty(Consts.BROADCAST_PROVIDER, Consts.BROADCAST_PROVIDER_MEMORY);
    System.setProperty(Consts.BROADCAST_MEMORY_NODE, "test");
    EventChannel ec = new EventChannel("test", Conf.getSystemProperties());
    server = new EmbeddableJNDIServer(ec, 1099);
    server.start(false);
  }

  @After
  public void tearDown() {
    EventChannel.enableReuse();
    PropUtil.clearUbikSystemProperties();
    server.stop();
  }

  @Test
  @Ignore
  public void testJndiDiscovery() throws Exception {
    Hashtable props = new Hashtable();
    props.put(InitialContext.PROVIDER_URL, "ubik://localhost:9999/");
    props.put(InitialContext.INITIAL_CONTEXT_FACTORY, RemoteInitialContextFactory.class.getName());
    InitialContext context = new InitialContext(props);
    try {
      context.lookup("some/service");
      fail("NameNotFoundException should have been thrown");
    } catch (NameNotFoundException e) {
      // ok
    }
    context.close();
  }

}
