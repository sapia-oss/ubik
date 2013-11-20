package org.sapia.ubik.rmi.server.transport.socket;

import java.rmi.RemoteException;

import org.sapia.ubik.net.ConnectionFactory;
import org.sapia.ubik.net.ConnectionPool;
import org.sapia.ubik.net.SocketConnection;
import org.sapia.ubik.net.ThreadInterruptedException;
import org.sapia.ubik.rmi.server.transport.Connections;
import org.sapia.ubik.rmi.server.transport.RmiConnection;
import org.sapia.ubik.util.pool.NoObjectAvailableException;
import org.sapia.ubik.util.pool.PooledObjectCreationException;

/**
 * Implements a pool of client-side {@link SocketConnection} instances. It
 * multiplexes the pooled connections among multiple callers - thereby sparing
 * the creation of multiple connections, and releiving the server of intensive
 * threading - typically, one connection corresponds to one server thread.
 * 
 * @author Yanick Duchesne
 */
public class SocketClientConnectionPool implements Connections {

  private String transportType;
  private ConnectionPool pool;

  /**
   * @param host
   *          the host of the server to connect to.
   * @param port
   *          the port of the server.
   */
  public SocketClientConnectionPool(String transportType, String host, int port, ConnectionFactory connectionFactory) {
    this.transportType = transportType;
    pool = new ConnectionPool.Builder().host(host).port(port).connectionFactory(connectionFactory).build();
  }

  @Override
  public RmiConnection acquire() throws RemoteException, ThreadInterruptedException {
    try {
      return (RmiConnection) pool.acquire();
    } catch (PooledObjectCreationException e) {
      if (e.getCause() instanceof RemoteException) {
        throw (RemoteException) e.getCause();
      }
      throw new IllegalStateException("Could get connection from pool", e);
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

  @Override
  public void clear() {
    pool.shrinkTo(0);
  }

  @Override
  public void invalidate(RmiConnection conn) {
    pool.invalidate(conn);
  }

  @Override
  public String getTransportType() {
    return transportType;
  }

  ConnectionPool internalPool() {
    return pool;
  }
}
