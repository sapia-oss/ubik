package org.sapia.ubik.rmi.server.transport.memory;

import static org.junit.Assert.*;

import java.util.Properties;

import org.junit.Before;
import org.junit.Test;
import org.sapia.ubik.net.ServerAddress;
import org.sapia.ubik.rmi.server.Server;
import org.sapia.ubik.rmi.server.command.RMICommand;
import org.sapia.ubik.rmi.server.transport.Connections;
import org.sapia.ubik.rmi.server.transport.RmiConnection;

public class InMemoryTransportProviderTest {

  private InMemoryTransportProvider provider;

  @Before
  public void setUp() throws Exception {
    provider = new InMemoryTransportProvider();
    provider.newDefaultServer();
  }

  @Test
  public void testGetPoolFor() throws Exception {
    Connections pool = provider.getPoolFor(new InMemoryAddress(InMemoryTransportProvider.DEFAULT_SERVER_NAME));
    RmiConnection connection = pool.acquire();
    connection.send(new TestRMICommand());
    assertEquals("TEST-RESPONSE", connection.receive());
    pool.release(connection);
  }

  @Test
  public void testNewDefaultServer() throws Exception {
    ServerAddress address = new InMemoryAddress(InMemoryTransportProvider.DEFAULT_SERVER_NAME);
    Server server = provider.newDefaultServer();
    assertEquals(address, server.getServerAddress());
  }

  @Test
  public void testNewServer() throws Exception {
    Properties props = new Properties();
    props.setProperty(InMemoryTransportProvider.SERVER_NAME, "test");
    Server server = provider.newServer(props);
    ServerAddress address = new InMemoryAddress("test");
    assertEquals(address, server.getServerAddress());
  }

  // --------------------------------------------------------------------------

  static class TestRMICommand extends RMICommand {

    @Override
    public Object execute() throws Throwable {
      return "TEST-RESPONSE";
    }
  }

}
