package org.sapia.ubik.rmi.server.transport.nio.tcp;

import java.rmi.RemoteException;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

import org.sapia.ubik.net.Connection;
import org.sapia.ubik.net.ConnectionPool;
import org.sapia.ubik.net.DefaultClientSocketFactory;
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
 * It multiplexes the pooled connections among multiple callers.
 * 
 * @author Yanick Duchesne
 *         <dl>
 *         <dt><b>Copyright: </b>
 *         <dd>Copyright &#169; 2002-2005 <a
 *         href="http://www.sapia-oss.org">Sapia Open Source Software </a>. All
 *         Rights Reserved.</dd>
 *         </dt>
 *         <dt><b>License: </b>
 *         <dd>Read the license.txt file of the jar or visit the <a
 *         href="http://www.sapia-oss.org/license.html">license page </a> at the
 *         Sapia OSS web site</dd>
 *         </dt>
 *         </dl>
 */
public class NioTcpClientConnectionPool implements Connections {
  private static Map     _pools = new Hashtable();
  private static boolean _started;
  ConnectionPool         _pool;

  /**
   * Constructor for RMIClientConnectionPool.
   * 
   * @param host
   *          the host of the server to connect to.
   * @param port
   *          the port of the server.
   */
  NioTcpClientConnectionPool(String host, int port, int bufsize) {
    _pool = new ConnectionPool(host, port, new NioRmiConnectionFactory(bufsize),
        new DefaultClientSocketFactory());
  }

  /**
   * @see org.sapia.ubik.rmi.server.transport.Connections#acquire()
   */
  public RmiConnection acquire() throws RemoteException {
    try {
      return (RmiConnection) _pool.acquire();
    } catch(java.io.IOException e) {
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
    return NioTcpTransportProvider.TRANSPORT_TYPE;
  }

  ConnectionPool internalPool() {
    return _pool;
  }

  static synchronized void shutdown() {
    NioTcpClientConnectionPool pool;
    Iterator pools;

    if(_started) {
      synchronized(_pools) {
        pools = _pools.values().iterator();

        while(pools.hasNext()) {
          pool = (NioTcpClientConnectionPool) pools.next();
          pool.internalPool().shrinkTo(0);
        }
      }
    }
  }

  static synchronized NioTcpClientConnectionPool getInstance(
      ServerAddress address, int bufsize) {
    if(!_started) {
      _started = true;

      Hub.taskMan.addTask(
          new TaskContext(NioTcpClientConnectionPool.class.getName(), PoolCleaner.INTERVAL),
          new PoolCleaner(_pools)
      );
    }

    NioTcpClientConnectionPool pool = (NioTcpClientConnectionPool) _pools
        .get(address);

    if(pool == null) {
      pool = new NioTcpClientConnectionPool(((NioAddress) address).getHost(),
          ((NioAddress) address).getPort(), bufsize);
      _pools.put(address, pool);
    }

    return pool;
  }

  static synchronized void invalidate(TCPAddress id) {
    _pools.remove(id);
  }

  /*
   * ////////////////////////////////////////////////// INNER CLASSES
   * //////////////////////////////////////////////////
   */
  static final class PoolCleaner implements Task {
    static final long INTERVAL = 5000;
    Map               _pools;

    PoolCleaner(Map pools) {
      _pools = pools;
    }

    public void exec(TaskContext context) {
      NioTcpClientConnectionPool[] pools;

      pools = (NioTcpClientConnectionPool[]) _pools.values().toArray(
          new NioTcpClientConnectionPool[_pools.size()]);

      for(int i = 0; i < pools.length; i++) {
        if((System.currentTimeMillis() - pools[i].internalPool()
            .getLastUsageTime()) > INTERVAL) {
          Log.debug(getClass(),
              "Shrinking nio socket client connection pool...");
          pools[i].internalPool().shrinkTo(0);
        }
      }
    }
  }
}
