package org.sapia.ubik.rmi.server.transport.socket;

import java.rmi.RemoteException;
import java.rmi.server.RMIClientSocketFactory;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

import org.sapia.ubik.net.Connection;
import org.sapia.ubik.net.ConnectionPool;
import org.sapia.ubik.net.ServerAddress;
import org.sapia.ubik.net.TCPAddress;
import org.sapia.ubik.rmi.server.Hub;
import org.sapia.ubik.rmi.server.Log;
import org.sapia.ubik.rmi.server.transport.Connections;
import org.sapia.ubik.rmi.server.transport.RmiConnection;
import org.sapia.ubik.taskman.Task;
import org.sapia.ubik.taskman.TaskContext;


/**
 * Implements a pool of client-side <code>SocketConnection</code> instances.
 * It multiplexes the pooled connections among multiple callers - thereby
 * sparing the creation of multiple connections, and releiving the server
 * of intensive threading - typically, one connection corresponds to one server
 * thread.
 *
 * @author Yanick Duchesne
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class SocketClientConnectionPool implements Connections {
  private static Map     _pools   = new Hashtable();
  private static boolean _started;
  ConnectionPool         _pool;

  /**
   * Constructor for RMIClientConnectionPool.
   * @param host the host of the server to connect to.
   * @param port the port of the server.
   */
  public SocketClientConnectionPool(String host, int port, long resetInterval,
    RMIClientSocketFactory socketFactory) {
    _pool = new ConnectionPool(host, port, new SocketRmiConnectionFactory().setResetInterval(resetInterval),
        socketFactory);
  }

  /**
   * @see org.sapia.ubik.rmi.server.transport.Connections#acquire()
   */
  public RmiConnection acquire() throws RemoteException {
    try {
      return (RmiConnection) _pool.acquire();
    } catch (java.io.IOException e) {
      throw new RemoteException("Could not acquire a connection", e);
    }
  }

  /**
   * @see org.sapia.ubik.rmi.server.transport.Connections#release(Connection)
   */
  public void release(Connection conn) {
    _pool.release(conn);
  }

  /**
   * @see org.sapia.ubik.rmi.server.transport.Connections#clear()
   */
  public void clear() {
    _pool.shrinkTo(0);
  }

  /**
   * @see org.sapia.ubik.rmi.server.transport.Connections#getTransportType()
   */
  public String getTransportType() {
    return SocketTransportProvider.TRANSPORT_TYPE;
  }

  ConnectionPool internalPool() {
    return _pool;
  }

  static synchronized void shutdown() {
    SocketClientConnectionPool pool;
    Iterator                   pools;

    if (_started) {
      synchronized (_pools) {
        pools = _pools.values().iterator();

        while (pools.hasNext()) {
          pool = (SocketClientConnectionPool) pools.next();
          pool.internalPool().shrinkTo(0);
        }
      }
    }
  }

  static synchronized SocketClientConnectionPool getInstance(
    ServerAddress address, RMIClientSocketFactory factory, long resetInterval) {
    if (!_started) {
      _started = true;

      Hub.taskMan.addTask(
          new TaskContext(SocketClientConnectionPool.class.getName(), PoolCleaner.INTERVAL),
          new PoolCleaner(_pools)
          
      );
    }

    SocketClientConnectionPool pool = (SocketClientConnectionPool) _pools.get(address);

    if (pool == null) {
      pool = new SocketClientConnectionPool(((TCPAddress) address).getHost(),
          ((TCPAddress) address).getPort(), resetInterval, factory);
      _pools.put(address, pool);
    }

    return pool;
  }

  static synchronized void invalidate(TCPAddress id) {
    _pools.remove(id);
  }

  /*//////////////////////////////////////////////////
                    INNER CLASSES
  //////////////////////////////////////////////////*/
  static final class PoolCleaner implements Task {
    static final long INTERVAL = 30000;
    Map               _pools;

    PoolCleaner(Map pools) {
      _pools = pools;
    }

    public void exec(TaskContext context) {
      SocketClientConnectionPool[] pools;

      pools = (SocketClientConnectionPool[]) _pools.values().toArray(new SocketClientConnectionPool[_pools.size()]);

      for (int i = 0; i < pools.length; i++) {
        if ((System.currentTimeMillis() -
              pools[i].internalPool().getLastUsageTime()) > INTERVAL) {
          Log.debug(getClass(), "Shrinking socket client connection pool...");
          pools[i].internalPool().shrinkTo(0);
        }
      }
    }
  }
}
