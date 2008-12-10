package org.sapia.ubik.rmi.server.transport;

import java.rmi.RemoteException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.sapia.ubik.net.ServerAddress;
import org.sapia.ubik.rmi.Consts;
import org.sapia.ubik.rmi.server.transport.nio.tcp.NioTcpTransportProvider;
import org.sapia.ubik.rmi.server.transport.socket.MultiplexSocketTransportProvider;
import org.sapia.ubik.rmi.server.transport.socket.SocketTransportProvider;


/**
 * The transport manager is the single-entry point into Ubik RMI's transport
 * layer. It allows to register <code>TransportProvider</code> instances,
 * which provide transport implementations on top of different network
 * protocols.
 *
 * @author Yanick Duchesne
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class TransportManager {
  private static Map _providers = Collections.synchronizedMap(new HashMap());

  static {
    TransportProvider provider = new MultiplexSocketTransportProvider();
    registerProvider(provider);
    provider = new NioTcpTransportProvider();
    registerProvider(provider);
    

    String[] propNames = (String[]) System.getProperties().keySet().toArray(new String[System.getProperties()
                                                                                             .size()]);
    String   propName;
    String   className;

    for (int i = 0; i < propNames.length; i++) {
      propName = (String) propNames[i];

      if (propName.startsWith(Consts.TRANSPORT_PROVIDER)) {
        className = System.getProperty(propName);

        if (className == null) {
          throw new IllegalStateException(
            "No class name defined for transport provider: " + propName);
        }

        try {
          provider = (TransportProvider) Class.forName(className).newInstance();
          registerProvider(provider);
        } catch (Throwable e) {
          e.printStackTrace();
          throw new IllegalStateException(
            "Could not instantiate transport provider: " + className);
        }
      }
    }
  }

  /**
   * Registers the transport provider of the given type with the transport manager. The
   * provider is internally mapped to its "transport type".
   *
   * @see TransportProvider#getTransportType()
   * @param provider a <code>TransportProvider</code> instance.
   * @throws IllegalArgumentException if a provider is already registered for the
   * given type.
   */
  public static void registerProvider(TransportProvider provider) {
    if (_providers.containsKey(provider.getTransportType())) {
      throw new IllegalArgumentException(
        "Transport provider already registered for: " +
        provider.getTransportType());
    }

    _providers.put(provider.getTransportType(), provider);
  }

  /**
   * Returns the transport provider corresponding to the given type.
   *
   * @param type the logical type of the desired transport provider.
   * @return a <code>TransportProvider</code>.
   * @throws IllegalArgumentException if no provider is registered for
   * the passed in type.
   */
  public static TransportProvider getProviderFor(String type) {
    TransportProvider provider = (TransportProvider) _providers.get(type);

    if (provider == null) {
      throw new IllegalArgumentException("No transport provider for: " + type);
    }

    return provider;
  }

  /***
   * Gets a connection pool that holds connections to a server,
   * given the server's address.
   *
   * @return a <code>ConnectionPool</code>.
   * @param address a <code>ServerAddress</code>.
   * @throws RemoteException if an problem occurs acquiring the connection.
   */
  public static Connections getConnectionsFor(ServerAddress address)
    throws RemoteException {
    return getProviderFor(address.getTransportType()).getPoolFor(address);
  }

  /**
   * Returns the default transport provider.
   *
   * @return the <code>SocketTransportProvider</code>.
   */
  public static SocketTransportProvider getDefaultProvider() {
    return (SocketTransportProvider) _providers.get(SocketTransportProvider.TRANSPORT_TYPE);
  }

  public static void shutdown() {
    TransportProvider provider;
    Iterator          providers;

    synchronized (_providers) {
      providers = _providers.values().iterator();

      while (providers.hasNext()) {
        provider = (TransportProvider) providers.next();
        provider.shutdown();
      }
    }
  }
}
