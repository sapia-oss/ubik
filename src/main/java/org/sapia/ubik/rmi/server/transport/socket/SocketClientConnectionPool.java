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
 * Implements a pool of client-side {@link SocketConnection} instances.
 * It multiplexes the pooled connections among multiple callers - thereby
 * sparing the creation of multiple connections, and releiving the server
 * of intensive threading - typically, one connection corresponds to one server
 * thread.
 *
 * @author Yanick Duchesne
 */
public class SocketClientConnectionPool implements Connections {
  
  ConnectionPool pool;

  /**
   * @param host the host of the server to connect to.
   * @param port the port of the server.
   */
  public SocketClientConnectionPool(
      String host, 
      int port,
      ConnectionFactory connectionFactory) {
    pool = new ConnectionPool.Builder()
             .host(host)
             .port(port)
             .connectionFactory(connectionFactory)
             .build();
  }

  /**
   * @see org.sapia.ubik.rmi.server.transport.Connections#acquire()
   */
  public RmiConnection acquire() throws RemoteException, ThreadInterruptedException {
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

  /**
   * @see org.sapia.ubik.rmi.server.transport.Connections#release(RmiConnection)
   */
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
    return SocketTransportProvider.TRANSPORT_TYPE;
  }

  ConnectionPool internalPool() {
    return pool;
  }
}
