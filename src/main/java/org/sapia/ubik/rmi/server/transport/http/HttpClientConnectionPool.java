package org.sapia.ubik.rmi.server.transport.http;

import java.rmi.RemoteException;

import org.apache.commons.httpclient.HttpClient;
import org.sapia.ubik.net.Pool;
import org.sapia.ubik.net.Uri;
import org.sapia.ubik.net.UriSyntaxException;
import org.sapia.ubik.rmi.server.transport.Connections;
import org.sapia.ubik.rmi.server.transport.RmiConnection;


/**
 * This class implements the <code>Connections</code> interface over Jakarta's
 * HTTP client. It does not do pooling, and leaves connection management to the
 * HTTP client.
 *
 * @author Yanick Duchesne
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2004 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class HttpClientConnectionPool implements Connections {
  private HttpAddress  _address;
  private HttpClient   _client = new HttpClient();
  private InternalPool _pool   = new InternalPool();

  /**
   * @param address the address of the target server.
   */
  public HttpClientConnectionPool(HttpAddress address)
    throws UriSyntaxException {
    _address = address;
  }

  /**
   * @param transportType the "transport type" identifier.
   * @param serverUri the address of the target server.
   */
  public HttpClientConnectionPool(Uri serverUri) {
    _address = new HttpAddress(serverUri);
  }

  /**
   * @see org.sapia.ubik.rmi.server.transport.Connections#acquire()
   */
  public HttpRmiClientConnection acquire() throws RemoteException {
    try {
      return ((HttpRmiClientConnection) _pool.acquire()).setUp(_client, _address);
    } catch (Exception e) {
      if (e instanceof RemoteException) {
        throw (RemoteException) e;
      }

      throw new RemoteException("Could acquire connection", e);
    }
  }

  /**
   * @see org.sapia.ubik.rmi.server.transport.Connections#clear()
   */
  public void clear() {
  }

  /**
   * @see org.sapia.ubik.rmi.server.transport.Connections#getTransportType()
   */
  public String getTransportType() {
    return _address.getTransportType();
  }

  /**
   * @see org.sapia.ubik.rmi.server.transport.Connections#release(RmiConnection)
   */
  public void release(RmiConnection conn) {
    conn.close();
    _pool.release((HttpRmiClientConnection)conn);
  }

  ///// INNER CLASS /////////////////////////////////////////////////////////////
  static class InternalPool extends Pool<HttpRmiClientConnection> {
    /**
     * @see org.sapia.ubik.net.Pool#doNewObject()
     */
    protected HttpRmiClientConnection doNewObject() throws Exception {
      return new HttpRmiClientConnection();
    }
  }
}
