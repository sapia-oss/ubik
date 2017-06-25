package org.sapia.ubik.rmi.server.transport.http;

import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import org.sapia.ubik.net.ServerAddress;
import org.sapia.ubik.net.Uri;
import org.sapia.ubik.net.UriSyntaxException;
import org.sapia.ubik.rmi.server.Server;
import org.sapia.ubik.rmi.server.transport.Connections;
import org.sapia.ubik.rmi.server.transport.TransportProvider;
import org.sapia.ubik.rmi.threads.Threads;
import org.sapia.ubik.util.Conf;
import org.sapia.ubik.util.Localhost;

/**
 * An instance of this class creates {@link HttpRmiServer} instances, as well as
 * client-side connections (using Jakartas HTTP client). It is the entry-point
 * into Ubik's HTTP tranport layer.
 * <p>
 * For the properties that an instance of this class takes (and their default
 * values), see the {@link HttpConsts} interface.
 * 
 * @see org.sapia.ubik.rmi.server.transport.http.HttpConsts
 * @see org.sapia.ubik.rmi.server.transport.http.HttpRmiServer
 * 
 * @author Yanick Duchesne
 */
public class HttpTransportProvider implements TransportProvider, HttpConsts {
  private static boolean usesJakarta;

  static {
    try {
      Class.forName("org.apache.http.client.HttpClient");
      usesJakarta = true;
    } catch (Exception e) {
    }
    
    if (System.getProperty(HttpConsts.HTTP_CLIENT_JDK) != null) {
      usesJakarta = !System.getProperty(HttpConsts.HTTP_CLIENT_JDK).equalsIgnoreCase("true");
    }
  } 

  private String transportType;
  private Router handlers = new Router();
  private Map<ServerAddress, Connections> pools = new ConcurrentHashMap<ServerAddress, Connections>();

  /**
   * Creates an instance of this class using the default HTTP transport type
   * identifier.
   * 
   * @see HttpConsts#TRANSPORT_TYPE
   */
  public HttpTransportProvider() {
    this(HttpConsts.TRANSPORT_TYPE);
  }

  /**
   * @param transportType
   *          a "transport type" identifier.
   */
  public HttpTransportProvider(String transportType) {
    this.transportType = transportType;
  }

  /**
   * @return the {@link Router} that holds the {@link Handler}s that are
   *         associated to predefined request paths.
   */
  public Router getRouter() {
    return handlers;
  }

  /**
   * @see org.sapia.ubik.rmi.server.transport.TransportProvider#getPoolFor(org.sapia.ubik.net.ServerAddress)
   */
  public synchronized Connections getPoolFor(ServerAddress address) throws RemoteException {
    Connections conns;

    if ((conns = pools.get(address)) == null) {
      try {
        if (usesJakarta) {
          int maxConnections = Conf.getSystemProperties().getIntProperty(HTTP_CLIENT_MAX_CONNECTIONS_KEY, DEFAULT_MAX_CLIENT_CONNECTIONS);
          conns = new HttpClientConnectionPool((HttpAddress) address, maxConnections);
        } else {
          conns = new JdkClientConnectionPool((HttpAddress) address);
        }

        pools.put(address, conns);
      } catch (UriSyntaxException e) {
        throw new RemoteException("Could not process given address", e);
      }
    }

    return conns;
  }

  /**
   * @see org.sapia.ubik.rmi.server.transport.TransportProvider#getTransportType()
   */
  public String getTransportType() {
    return transportType;
  }

  /**
   * @see org.sapia.ubik.rmi.server.transport.TransportProvider#newDefaultServer()
   */
  public Server newDefaultServer() throws RemoteException {
    throw new UnsupportedOperationException("Transport provider does not support anonymous servers/dynamic ports");
  }

  /**
   * @see org.sapia.ubik.rmi.server.transport.TransportProvider#newServer(java.util.Properties)
   */
  public Server newServer(Properties props) throws RemoteException {
    Conf configProps = new Conf().addProperties(props).addSystemProperties();
    Uri serverUrl;
    int port = configProps.getIntProperty(HTTP_PORT_KEY, DEFAULT_HTTP_PORT);

    try {
      String bindAddress = configProps.getProperty(HTTP_BIND_ADDRESS_KEY, Localhost.getPreferredLocalAddress().getHostAddress());
      serverUrl = Uri.parse("http://" + bindAddress + ":" + port + CONTEXT_PATH);
    } catch (UriSyntaxException e) {
      throw new RemoteException("Could not parse server URL", e);
    } catch (UnknownHostException e) {
      throw new RemoteException("Could not acquire local address", e);
    }

    UbikHttpHandler handler = new UbikHttpHandler(serverUrl, Threads.createWorkerPool());
    handlers.addHandler(CONTEXT_PATH, handler);
    HttpRmiServer svr = new HttpRmiServer(handlers, serverUrl, port);
    return svr;
  }

  /**
   * @see org.sapia.ubik.rmi.server.transport.TransportProvider#shutdown()
   */
  public void shutdown() {
  }
}
