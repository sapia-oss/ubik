package org.sapia.ubik.rmi.server.transport.nio.tcp;

import java.rmi.RemoteException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.sapia.ubik.log.Log;
import org.sapia.ubik.net.ConnectionPool;
import org.sapia.ubik.net.ServerAddress;
import org.sapia.ubik.net.TCPAddress;
import org.sapia.ubik.net.ThreadInterruptedException;
import org.sapia.ubik.rmi.server.Hub;
import org.sapia.ubik.rmi.server.transport.Connections;
import org.sapia.ubik.rmi.server.transport.RmiConnection;
import org.sapia.ubik.taskman.Task;
import org.sapia.ubik.taskman.TaskContext;
import org.sapia.ubik.util.pool.NoObjectAvailableException;
import org.sapia.ubik.util.pool.PooledObjectCreationException;

/**
 * Implements a pool of client-side {@link NioTcpRmiClientConnection} instances.
 * It multiplexes the pooled connections among multiple callers.
 * 
 * @author Yanick Duchesne
 */
public class NioTcpClientConnectionPool implements Connections {
  
  private static Map<ServerAddress, NioTcpClientConnectionPool> pools = new ConcurrentHashMap<ServerAddress, NioTcpClientConnectionPool>();
  private static volatile boolean started;
  ConnectionPool                  pool;

  /**
   * @param host
   *          the host of the server to connect to.
   * @param port
   *          the port of the server.
   */
  NioTcpClientConnectionPool(String host, int port, int bufsize) {
    pool = new ConnectionPool.Builder()
      .host(host)
      .port(port)
      .connectionFactory(new NioRmiConnectionFactory(bufsize))
      .build();
  }

  /**
   * @see org.sapia.ubik.rmi.server.transport.Connections#acquire()
   */
  public RmiConnection acquire() throws RemoteException {
    try {
      return (RmiConnection) pool.acquire();
    } catch (PooledObjectCreationException e) {
      throw new RemoteException("Could not create connection", e);
    } catch (NoObjectAvailableException e) {
      throw new RemoteException("No connection available", e);
    } catch (InterruptedException e) {
      throw new ThreadInterruptedException();
    }
  }

  @Override
  public void release(RmiConnection conn) {
    pool.release(conn);
  }

  /**
   * @see org.sapia.ubik.rmi.server.transport.Connections#clear()
   */
  public void clear() {
    pool.shrinkTo(0);
  }

  /**
   * @see org.sapia.ubik.rmi.server.transport.Connections#getTransportType()
   */
  public String getTransportType() {
    return NioTcpTransportProvider.TRANSPORT_TYPE;
  }

  ConnectionPool internalPool() {
    return pool;
  }

  static synchronized void shutdown() {

    if(started) {
      for(NioTcpClientConnectionPool pool : pools.values()) {
        pool.internalPool().shrinkTo(0);
      }
    }
  }

  static synchronized NioTcpClientConnectionPool getInstance(
      ServerAddress address, int bufsize) {
    if(!started) {
      started = true;

      Hub.getModules().getTaskManager().addTask(
          new TaskContext(NioTcpClientConnectionPool.class.getName(), PoolCleaner.INTERVAL),
          new PoolCleaner(pools)
      );
    }

    NioTcpClientConnectionPool pool = (NioTcpClientConnectionPool) pools.get(address);

    if(pool == null) {
      pool = new NioTcpClientConnectionPool(((NioAddress) address).getHost(),
          ((NioAddress) address).getPort(), bufsize);
      pools.put(address, pool);
    }

    return pool;
  }

  static synchronized void invalidate(TCPAddress id) {
    pools.remove(id);
  }
  
  // --------------------------------------------------------------------------

  static final class PoolCleaner implements Task {
    
    static final long INTERVAL = 5000;
    
    Map<ServerAddress, NioTcpClientConnectionPool> pools;

    PoolCleaner(Map<ServerAddress, NioTcpClientConnectionPool> pools) {
      this.pools = pools;
    }

    public void exec(TaskContext context) {
      NioTcpClientConnectionPool[] toClean;

      toClean = (NioTcpClientConnectionPool[]) pools.values().toArray(new NioTcpClientConnectionPool[pools.size()]);

      for(int i = 0; i < toClean.length; i++) {
        if((System.currentTimeMillis() - toClean[i].internalPool()
            .getLastUsageTime()) > INTERVAL) {
          Log.debug(getClass(), "Shrinking nio socket client connection pool...");
          toClean[i].internalPool().shrinkTo(0);
        }
      }
    }
  }
}
