package org.sapia.ubik.rmi.server.transport.http;

import java.net.HttpURLConnection;
import java.net.URL;
import java.rmi.RemoteException;

import org.sapia.ubik.net.Uri;
import org.sapia.ubik.rmi.server.transport.Connections;
import org.sapia.ubik.rmi.server.transport.RmiConnection;
import org.sapia.ubik.util.pool.Pool;


/**
 * This class implements the <code>Connections</code> interface over the JDK's
 * HTTP support classes ({@link URL}, {@link HttpURLConnection}). It is
 * a sub-optimal implementation used only if the Jakarta HTTP client classes are not
 * in the classpath.
 *
 * @author Yanick Duchesne
 */
public class JdkClientConnectionPool implements Connections {
  private HttpAddress  address;
  private InternalPool pool = new InternalPool();

  /**
   * @param address the address of the target server.
   */
  public JdkClientConnectionPool(HttpAddress address) {
    this.address = address;
  }

  /**
   * @param transportType the "transport type" identifier.
   * @param serverUri the address of the target server.
   */
  public JdkClientConnectionPool(String transportType, Uri serverUri) {
    this(new HttpAddress(serverUri));
  }

  /**
   * @see org.sapia.ubik.rmi.server.transport.Connections#acquire()
   */
  public RmiConnection acquire() throws RemoteException {
    try {
      return pool.acquire().setUp(address);
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
    return address.getTransportType();
  }
  
  @Override
  public void release(RmiConnection conn) {
    pool.release((JdkRmiClientConnection)conn);
  }

  ///// INNER CLASS /////////////////////////////////////////////////////////////
  
  static class InternalPool extends Pool<JdkRmiClientConnection> {
    /**
     * @see org.sapia.ubik.util.pool.Pool#doNewObject()
     */
    protected JdkRmiClientConnection doNewObject() throws Exception {
      return new JdkRmiClientConnection();
    }
  }
}
