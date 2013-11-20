package org.sapia.ubik.rmi.server.transport.memory;

import java.rmi.RemoteException;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import org.sapia.ubik.net.ServerAddress;
import org.sapia.ubik.rmi.server.Hub;
import org.sapia.ubik.rmi.server.Server;
import org.sapia.ubik.rmi.server.transport.Connections;
import org.sapia.ubik.rmi.server.transport.TransportProvider;
import org.sapia.ubik.util.Props;

/**
 * This {@link TransportProvider} creates {@link InMemoryServer} instances, and
 * manages connections to such instances.
 * 
 * @author yduchesne
 * 
 */
public class InMemoryTransportProvider implements TransportProvider {

  /**
   * This constant corresponds to the
   * <code>ubik.rmi.transport.memory.server.name</code> system property.
   */
  public static final String SERVER_NAME = "ubik.rmi.transport.memory.server.name";

  /**
   * This constant corresponds to the
   * <code>ubik.rmi.transport.memory.marshalling</code> system property.
   */
  public static final String MARSHALLING = "ubik.rmi.transport.memory.marshalling";

  public static final String DEFAULT_SERVER_NAME = "default";

  private Map<String, InMemoryServer> servers = new ConcurrentHashMap<String, InMemoryServer>();

  private boolean useMarshalling = Props.getSystemProperties().getBooleanProperty(MARSHALLING, false);

  public Connections getPoolFor(ServerAddress address) throws RemoteException {
    if (!(address instanceof ServerAddress)) {
      throw new IllegalArgumentException(String.format("Wrong address type %s. Expected %s", address.getClass(), InMemoryAddress.class));
    }
    return new InMemoryConnections(useMarshalling, (InMemoryAddress) address, this);
  }

  @Override
  public String getTransportType() {
    return InMemoryAddress.TRANSPORT_TYPE;
  }

  @Override
  public synchronized Server newDefaultServer() throws RemoteException {
    InMemoryServer server = servers.get(DEFAULT_SERVER_NAME);
    if (server == null) {
      server = new InMemoryServer(Hub.getModules().getServerRuntime().getDispatcher(), new InMemoryAddress(DEFAULT_SERVER_NAME));
      servers.put(DEFAULT_SERVER_NAME, server);
    }
    return server;
  }

  public synchronized Server newServer(Properties props) throws RemoteException {
    Props wrapper = new Props().addProperties(props);
    String name = wrapper.getProperty(SERVER_NAME, DEFAULT_SERVER_NAME);
    InMemoryServer server = servers.get(name);
    if (server == null) {
      server = new InMemoryServer(Hub.getModules().getServerRuntime().getDispatcher(), new InMemoryAddress(name));
      servers.put(name, server);
    }
    return server;
  }

  public Server getServerFor(InMemoryAddress address) throws RemoteException {
    InMemoryServer server = servers.get(address.getName());
    if (server == null) {
      throw new RemoteException("No such server: " + address.getName());
    }

    return server;
  }

  @Override
  public void shutdown() {
    servers.clear();
  }

}
