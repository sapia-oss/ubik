package org.sapia.ubik.net;

import java.io.*;

import java.rmi.server.RMIClientSocketFactory;

import java.util.*;


/**
 * Implements a pool of <code>Connection</code> instances.
 *
 * @author Yanick Duchesne
 * 2002-08-09
 */
public class ConnectionPool {
  public static final long         DEFAULT_ACQUIRE_TIME_OUT = 2000;
  private List<Connection>         _conns         = new Vector<Connection>(50);
  protected ConnectionFactory      _fac;
  protected String                 _host;
  protected int                    _port;
  protected int                    _maxSize;
  protected int                    _currentCount;
  protected long                   _lastUsageTime = System.currentTimeMillis();
  protected RMIClientSocketFactory _socketFactory;

  public ConnectionPool(String host, int port, ConnectionFactory fac,
    RMIClientSocketFactory socketFactory) {
    _fac             = fac;
    _host            = host;
    _port            = port;
    _socketFactory   = socketFactory;
  }

  public ConnectionPool(String host, int port, ConnectionFactory fac,
    RMIClientSocketFactory socketFactory, int maxSize) {
    this(host, port, fac, socketFactory);
    _maxSize = maxSize;
  }

  public ConnectionPool(String host, int port,
    RMIClientSocketFactory socketFactory) {
    this(host, port, new SocketConnectionFactory(), socketFactory);
  }

  public ConnectionPool(String host, int port, int maxSize,
    RMIClientSocketFactory socketFactory) {
    this(host, port, new SocketConnectionFactory(), socketFactory, maxSize);
  }

  public synchronized Connection acquire() throws IOException {
    return acquire(DEFAULT_ACQUIRE_TIME_OUT);
  }

  public synchronized Connection acquire(long timeout)
    throws IOException {
    Connection conn;

    if (_conns.size() == 0) {
      if (_maxSize <= 0) {
        conn = newConnection();
      } else {
        if (_conns.size() == 0) {
          try {
            if (timeout > 0) {
              wait(timeout);
            }
          } catch (InterruptedException e) {
            throw new IOException("thread interrupted");
          }

          if (_currentCount >= _maxSize) {
            throw new IOException("maximum connection pool size reached");
          } else {
            conn = newConnection();
          }
        } else {
          conn = (Connection) _conns.remove(0);
        }
      }
    } else {
      conn = (Connection) _conns.remove(0);
    }

    return conn;
  }

  public synchronized void release(Connection conn) {
    _lastUsageTime = System.currentTimeMillis();
    _conns.add(conn);
    notify();
  }

  public synchronized int getCount() {
    return _currentCount;
  }

  /**
   * Returns the time an object was last acquired from this
   * pool.
   */
  public long getLastUsageTime() {
    return _lastUsageTime;
  }

  /**
   * Shrinks the pool to the specified size, or until the pool is
   * empty.
   *
   * @param size the size to which to shrink the pool.
   */
  public synchronized void shrinkTo(int size) {
    while ((_conns.size() > size) && (_conns.size() > 0)) {
      _currentCount--;

      if (_currentCount < 0) {
        _currentCount = 0;
      }

      Connection conn = (Connection) _conns.remove(0);
      conn.close();
    }
  }

  protected Connection newConnection() throws IOException {
    _currentCount++;

    return _fac.newConnection(_host, _port);
  }
}
