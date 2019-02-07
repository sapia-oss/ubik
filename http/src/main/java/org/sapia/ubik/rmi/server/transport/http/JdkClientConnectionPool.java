package org.sapia.ubik.rmi.server.transport.http;

import java.net.HttpURLConnection;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.sapia.ubik.log.Category;
import org.sapia.ubik.log.Log;
import org.sapia.ubik.net.Uri;
import org.sapia.ubik.rmi.server.transport.Connections;
import org.sapia.ubik.rmi.server.transport.RmiConnection;
import org.sapia.ubik.util.SysClock;
import org.sapia.ubik.util.SysClock.RealtimeClock;
import org.sapia.ubik.util.pool.Pool;

/**
 * This class implements the <code>Connections</code> interface over the JDK's
 * HTTP support classes ({@link URL}, {@link HttpURLConnection}). It is a
 * sub-optimal implementation used only if the Jakarta HTTP client classes are
 * not in the classpath.
 * 
 * @author Yanick Duchesne
 */
public class JdkClientConnectionPool implements Connections {
  
  private Category                    log    = Log.createCategory(getClass());
  private HttpAddress                 address;
  private Set<JdkRmiClientConnection> active = Collections.newSetFromMap(new ConcurrentHashMap<>());
  private InternalPool                pool   = new InternalPool();
  private SysClock                    clock  = RealtimeClock.getInstance();
  
  /**
   * @param address
   *          the address of the target server.
   */
  public JdkClientConnectionPool(HttpAddress address) {
    this.address = address;
  }
 
  /**
   * @param transportType
   *          the "transport type" identifier.
   * @param serverUri
   *          the address of the target server.
   */
  public JdkClientConnectionPool(String transportType, Uri serverUri) {
    this(new HttpAddress(serverUri));
  }
  
  // Visible for testing
  void setClock(SysClock clock) {
    this.clock = clock;
  }

  @Override
  public RmiConnection acquire() throws RemoteException {
    synchronized (pool) {
      try {
        RmiConnection connection =  pool.acquire().setUp(address);
        active.add((JdkRmiClientConnection) connection);
        return connection;
      } catch (Exception e) {
        if (e instanceof RemoteException) {
          throw (RemoteException) e;
        }

        throw new RemoteException("Could acquire connection", e);
      }        
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
    synchronized (pool) {
      pool.release((JdkRmiClientConnection) conn);
      active.remove(conn);
    }
  }

  @Override
  public void invalidate(RmiConnection conn) {
    synchronized (pool) {
      doInvalidate(conn);
    }
  }
  
  void terminateTimedOutConnections() {
    synchronized (pool) {
      Set<JdkRmiClientConnection> toCheck = new HashSet<>(active);
      toCheck.forEach(c -> {
        if (c.isInReadTimeout()) {
          log.warning("Invalidating connection to %s since it is deemed in a read timeout situation", c.getServerAddress());
          doInvalidate(c);
        }
      });
    }
  }
  
  private void doInvalidate(RmiConnection conn) {
    pool.invalidate((JdkRmiClientConnection) conn);
    active.remove(conn);
  }

  // /// INNER CLASS
  // /////////////////////////////////////////////////////////////

  class InternalPool extends Pool<JdkRmiClientConnection> {
    /**
     * @see org.sapia.ubik.util.pool.Pool#doNewObject()
     */
    protected JdkRmiClientConnection doNewObject() throws Exception {
      JdkRmiClientConnection conn = new JdkRmiClientConnection();
      conn.setClock(clock);
      return conn;
    }
  }
}
