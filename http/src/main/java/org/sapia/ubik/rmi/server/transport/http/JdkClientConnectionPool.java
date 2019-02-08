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
import org.sapia.ubik.rmi.server.transport.http.JdkRmiClientConnection.JdkRmiClientConnectionFactory;
import org.sapia.ubik.util.SysClock.RealtimeClock;
import org.sapia.ubik.util.pool.Pool;

/**
 * This class implements the <code>Connections</code> interface over the JDK's
 * HTTP support classes ({@link URL}, {@link HttpURLConnection}). It is a
 * sub-optimal implementation used only if the Jakarta HTTP client classes are
 * not in the classpath.
 * 
 * @author yduchesne
 */
public class JdkClientConnectionPool implements Connections {
  
  private Category                      log    = Log.createCategory(getClass());
  private HttpAddress                   address;
  private Set<JdkRmiClientConnection>   active = Collections.newSetFromMap(new ConcurrentHashMap<>());
  private InternalPool                  pool   = new InternalPool();
  private JdkRmiClientConnectionFactory factory;
  
  /**
   * @param address the address of the target server.
   * @param factory the {@link JdkRmiClientConnectionFactory} to use.
   */
  public JdkClientConnectionPool(HttpAddress address, JdkRmiClientConnectionFactory factory) {
    this.address = address;
    this.factory = factory;
  }
  
  /**
   * @param address
   *          the address of the target server.
   */
  public JdkClientConnectionPool(HttpAddress address) {
    this(address, () -> { 
      JdkRmiClientConnection conn =  new JdkRmiClientConnection();
      conn.setClock(RealtimeClock.getInstance());
      return conn;
    });
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
    pool.shrinkTo(0);
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
  
  // --------------------------------------------------------------------------
  // Visible for testing
  
  boolean isActive(RmiConnection conn) {
    return active.contains(conn);
  }
  
  Pool<JdkRmiClientConnection> getInternalPool() {
    return pool;
  }
  
  // --------------------------------------------------------------------------
  // Restricted
  
  void terminateTimedOutConnections() {
    Set<JdkRmiClientConnection> toCheck = new HashSet<>(active.size() + 1);
    synchronized (pool) {
      toCheck.addAll(active);
    }
    toCheck.forEach(c -> {
      if (c.isInReadTimeout()) {
        log.warning("Invalidating connection to %s since it is deemed in a read timeout situation", c.getServerAddress());
        doInvalidate(c);
      }
    });
  }
  
  private void doInvalidate(RmiConnection conn) {
    if (active.contains(conn)) {
      pool.invalidate((JdkRmiClientConnection) conn);
      active.remove(conn);
    }
  }

  // ==========================================================================
  // Inner class

  class InternalPool extends Pool<JdkRmiClientConnection> {
    @Override
    protected JdkRmiClientConnection doNewObject() throws Exception {
      JdkRmiClientConnection conn = factory.newConnection();
      return conn;
    }
    
    @Override
    protected void cleanup(JdkRmiClientConnection pooled) {
      pooled.close();
    }
  }
}
