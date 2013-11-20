package org.sapia.ubik.rmi.server.transport.http;

import java.rmi.RemoteException;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.sapia.ubik.net.Uri;
import org.sapia.ubik.net.UriSyntaxException;
import org.sapia.ubik.rmi.server.transport.Connections;
import org.sapia.ubik.rmi.server.transport.RmiConnection;

/**
 * This class implements the <code>Connections</code> interface over Apache's
 * {@link HttpClient}. It does not do pooling, and leaves connection management
 * to the client.
 * 
 * @author Yanick Duchesne
 */
public class HttpClientConnectionPool implements Connections {

  private HttpAddress address;
  private PoolingClientConnectionManager connectionPool;
  private HttpClient client;

  /**
   * @param address
   *          the address of the target server.
   * @param maxConnections
   *          the maximum number of connections that the {@link HttpClient}
   *          should pool at once.
   */
  public HttpClientConnectionPool(HttpAddress address, int maxConnections) throws UriSyntaxException {
    this.address = address;
    connectionPool = new PoolingClientConnectionManager();
    connectionPool.setDefaultMaxPerRoute(maxConnections);
    client = new DefaultHttpClient(connectionPool);
  }

  /**
   * @param serverUri
   *          the address of the target server.
   */
  public HttpClientConnectionPool(Uri serverUri) {
    this.address = new HttpAddress(serverUri);
  }

  @Override
  public synchronized HttpRmiClientConnection acquire() throws RemoteException {
    return new HttpRmiClientConnection(client, address);
  }

  @Override
  public synchronized void clear() {
    // no other choice but to recreate a new HttpClient.
    // (HttpClient API does not allow clearing pool).
    connectionPool.shutdown();
    connectionPool = new PoolingClientConnectionManager();
    connectionPool.setDefaultMaxPerRoute(connectionPool.getDefaultMaxPerRoute());
    client = new DefaultHttpClient(connectionPool);
  }

  @Override
  public String getTransportType() {
    return address.getTransportType();
  }

  @Override
  public void release(RmiConnection conn) {
  }

  @Override
  public void invalidate(RmiConnection conn) {
  }

}
