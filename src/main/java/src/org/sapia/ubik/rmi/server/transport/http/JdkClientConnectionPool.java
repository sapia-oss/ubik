package org.sapia.ubik.rmi.server.transport.http;

import java.rmi.RemoteException;

import org.sapia.ubik.net.Connection;
import org.sapia.ubik.net.Pool;
import org.sapia.ubik.net.Uri;
import org.sapia.ubik.net.UriSyntaxException;
import org.sapia.ubik.rmi.server.transport.Connections;
import org.sapia.ubik.rmi.server.transport.RmiConnection;


/**
 * This class implements the <code>Connections</code> interface over the JDK's
 * HTTP support classes (<code>URL</code>, <code>HttpURLConnection</code>). It is
 * a sub-optimal implementation used only if the Jakarta HTTP client classes are not
 * in the classpath.
 *
 * @author Yanick Duchesne
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2004 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class JdkClientConnectionPool implements Connections {
  private HttpAddress  _address;
  private InternalPool _pool = new InternalPool();

  /**
   * @param transportType the "transport type" identifier.
   * @param address the address of the target server.
   */
  public JdkClientConnectionPool(HttpAddress address) throws UriSyntaxException {
    _address = address;
  }

  /**
   * @param transportType the "transport type" identifier.
   * @param serverUri the address of the target server.
   */
  public JdkClientConnectionPool(String transportType, Uri serverUri) {
    _address = new HttpAddress(serverUri);
  }

  /**
   * @see org.sapia.ubik.rmi.server.transport.Connections#acquire()
   */
  public RmiConnection acquire() throws RemoteException {
    try {
      return ((JdkRmiClientConnection) _pool.acquire()).setUp(_address);
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
   * @see org.sapia.ubik.rmi.server.transport.Connections#release(org.sapia.ubik.net.Connection)
   */
  public void release(Connection conn) {
    conn.close();
    _pool.release(conn);
  }

  ///// INNER CLASS /////////////////////////////////////////////////////////////
  static class InternalPool extends Pool {
    /**
     * @see org.sapia.ubik.net.Pool#doNewObject()
     */
    protected Object doNewObject() throws Exception {
      return new JdkRmiClientConnection();
    }
  }
}
