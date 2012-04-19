package org.sapia.ubik.rmi.server.transport.http;

import java.rmi.RemoteException;

import org.apache.commons.httpclient.HttpClient;
import org.sapia.ubik.net.Uri;
import org.sapia.ubik.net.UriSyntaxException;
import org.sapia.ubik.rmi.server.transport.Connections;
import org.sapia.ubik.rmi.server.transport.RmiConnection;
import org.sapia.ubik.util.pool.Pool;


/**
 * This class implements the <code>Connections</code> interface over Jakarta's
 * HTTP client. It does not do pooling, and leaves connection management to the
 * HTTP client.
 *
 * @author Yanick Duchesne
 */
public class HttpClientConnectionPool implements Connections {
  private HttpAddress  address;
  private HttpClient   client = new HttpClient();
  private InternalPool pool   = new InternalPool();

  /**
   * @param address the address of the target server.
   */
  public HttpClientConnectionPool(HttpAddress address)
    throws UriSyntaxException {
    this.address = address;
  }

  /**
   * @param serverUri the address of the target server.
   */
  public HttpClientConnectionPool(Uri serverUri) {
    this.address = new HttpAddress(serverUri);
  }

  @Override
  public HttpRmiClientConnection acquire() throws RemoteException {
    try {
      return ((HttpRmiClientConnection) this.pool.acquire()).setUp(client, address);
    } catch (Exception e) {
      if (e instanceof RemoteException) {
        throw (RemoteException) e;
      }

      throw new RemoteException("Could acquire connection", e);
    }
  }

  @Override
  public void clear() {
  }

  @Override
  public String getTransportType() {
    return address.getTransportType();
  }
  
  @Override
  public void release(RmiConnection conn) {
    conn.close();
    pool.release((HttpRmiClientConnection)conn);
  }
  
  @Override
  public void invalidate(RmiConnection conn) {
  	pool.invalidate((HttpRmiClientConnection) conn);
  }

  ///// INNER CLASS /////////////////////////////////////////////////////////////
  static class InternalPool extends Pool<HttpRmiClientConnection> {
    /**
     * @see org.sapia.ubik.util.pool.Pool#doNewObject()
     */
    protected HttpRmiClientConnection doNewObject() throws Exception {
      return new HttpRmiClientConnection();
    }
  }
}
