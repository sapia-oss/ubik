package org.sapia.ubik.rmi.server.transport;

import java.rmi.RemoteException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.sapia.ubik.log.Category;
import org.sapia.ubik.log.Log;
import org.sapia.ubik.module.Module;
import org.sapia.ubik.module.ModuleContext;
import org.sapia.ubik.net.ServerAddress;
import org.sapia.ubik.provider.Providers;
import org.sapia.ubik.rmi.server.transport.memory.InMemoryTransportProvider;
import org.sapia.ubik.rmi.server.transport.mina.MinaTransportProvider;
import org.sapia.ubik.rmi.server.transport.socket.SocketTransportProvider;
import org.sapia.ubik.util.Assertions;

/**
 * The transport manager is the single-entry point into Ubik RMI's transport
 * layer. It allows registering {@link TransportProvider} instances, which
 * provide transport implementations on top of different network protocols.
 * <p>
 * This class registers the following transport providers automatically:
 *
 * <ul>
 *   <li> {@link SocketTransportProvider}.
 *   <li> {@link MinaTransportProvider}.
 *   <li> {@link InMemoryTransportProvider}.
 * </ul>
 *
 * @author Yanick Duchesne
 */
public class TransportManager implements Module {

  private Category log = Log.createCategory(getClass());
  
  private Map<String, TransportProvider> providers = new ConcurrentHashMap<String, TransportProvider>();

  @Override
  public void init(ModuleContext context) {
    registerProvider(new SocketTransportProvider());
    registerProvider(new MinaTransportProvider());
    registerProvider(new InMemoryTransportProvider());
  }

  @Override
  public void start(ModuleContext context) {
  }

  /**
   * Internally shuts down all transport providers.
   * 
   * @see TransportProvider#shutdown()
   */
  @Override
  public void stop() {
    synchronized (providers) {
      for (Map.Entry<String, TransportProvider> p : providers.entrySet()) {
        log.info("Shutting down transport provider: %s", p.getKey());
        p.getValue().shutdown();
      }
      providers.clear();
    }   
  }

  /**
   * Registers the transport provider of the given type with the transport
   * manager. The provider is internally mapped to its "transport type".
   *
   * @see TransportProvider#getTransportType()
   * @param provider
   *          a {@link TransportProvider} instance.
   * @throws IllegalArgumentException
   *           if a provider is already registered for the given type.
   */
  public void registerProvider(TransportProvider provider) {
    if (providers.containsKey(provider.getTransportType())) {
      throw new IllegalArgumentException("Transport provider already registered for: " + provider.getTransportType());
    }

    providers.put(provider.getTransportType(), provider);
  }

  /**
   * Returns the transport provider corresponding to the given type.
   *
   * @param type
   *          the logical type of the desired transport provider.
   * @return a {@link TransportProvider}.
   * @throws IllegalArgumentException
   *           if no provider is registered for the passed in type.
   */
  public TransportProvider getProviderFor(String type) {
    TransportProvider provider = providers.get(type);
    
    if (provider == null) {
      synchronized (providers) {
        provider = providers.get(type);
        if (provider == null) {
          provider = Providers.get().load(TransportProvider.class, type);
          providers.put(type, provider);
        }
      }
    }
    
    Assertions.isFalse(provider == null, "No transport provider for: " + type);
   
    return provider;
  }

  /***
   * Gets a connection pool that holds connections to a server, given the
   * server's address.
   *
   * @return a {@link Connections} instance.
   * @param address
   *          a {@link ServerAddress}.
   * @throws RemoteException
   *           if an problem occurs acquiring the connection.
   */
  public Connections getConnectionsFor(ServerAddress address) throws RemoteException {
    return getProviderFor(address.getTransportType()).getPoolFor(address);
  }

  /**
   * Returns the default transport provider.
   *
   * @return the {@link SocketTransportProvider}.
   */
  public MinaTransportProvider getDefaultProvider() {
    return (MinaTransportProvider) getProviderFor(MinaTransportProvider.TRANSPORT_TYPE);
  }

}
