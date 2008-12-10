package org.sapia.ubik.net;

import java.io.IOException;

import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import org.sapia.ubik.rmi.server.perf.Statistic;


/**
 * Implements a socket server.
 *
 * @author Yanick Duchesne
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public abstract class SocketServer implements Runnable {
  private ServerSocket              _server;
  protected SocketConnectionFactory _fac;
  private String                    _address;
  private ThreadPool                _tp;
  private boolean                   _started;
  private SocketException           _startExc;

  protected SocketServer(int port, ThreadPool tp,
    UbikServerSocketFactory socketFactory) throws java.io.IOException {
    this(port, new SocketConnectionFactory(), tp, socketFactory);
  }

  protected SocketServer(String bindAddr, int port, ThreadPool tp,
    UbikServerSocketFactory socketFactory) throws java.io.IOException {
    this(bindAddr, port, new SocketConnectionFactory(), tp, socketFactory);
  }

  protected SocketServer(int port, SocketConnectionFactory fac, ThreadPool tp,
    UbikServerSocketFactory socketFactory) throws java.io.IOException {
    _server   = socketFactory.createServerSocket(port);
    _fac      = fac;
    _tp       = tp;
  }

  protected SocketServer(String bindAddr, int port,
    SocketConnectionFactory fac, ThreadPool tp,
    UbikServerSocketFactory socketFactory) throws java.io.IOException {
    _server   = socketFactory.createServerSocket(port, bindAddr);
    _fac      = fac;
    _tp       = tp;
  }

  /**
   * Creates a new SocketServer instance
   *
   * @param tp
   * @param server
   * @throws IOException
   */
  protected SocketServer(ThreadPool tp, ServerSocket server)
    throws IOException {
    this(new SocketConnectionFactory(), tp, server);
  }

  /**
   * Creates a new SocketServer instance.
   *
   * @param fac
   * @param tp
   * @param server
   * @throws IOException
   */
  protected SocketServer(SocketConnectionFactory fac, ThreadPool tp,
    ServerSocket server) throws IOException {
    _server   = server;
    _fac      = fac;
    _tp       = tp;
  }

  public String getAddress() {
    if (_address == null) {
      _address = _server.getInetAddress().getHostAddress();

      if (_address.equals("0.0.0.0")) {
        try {
          _address = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
          throw new IllegalStateException(e.getClass().getName() +
            " caught - msg " + e.getMessage());
        }
      }
    }

    return _address;
  }

  public void enableStats(){
    _tp.enabledStats();
  }  
  
  public void disableStats(){
    _tp.disableStats();
  }

  public int getPort() {
    return _server.getLocalPort();
  }
  
  public Statistic getRequestsPerSecondStat(){
    return _tp.getRpsStat();
  }
  
  public Statistic getRequestDurationStat(){
    return _tp.getDurationStat();
  }
  
  public int getThreadCount(){
    return _tp.getThreadCount();
  }

  public void close() {
    try {
      _tp.shutdown();
      _server.close();
    } catch (Throwable t) {
      t.printStackTrace();
    }
  }

  public void run() {
    try {
      _tp.fill(1);
    } catch (Throwable t) {
      t.printStackTrace();

      return;
    }

    while (true) {
      try {
        Socket client = null;

        try {
          if (!_started) {
            try {
              _server.setSoTimeout(1);
              client = _server.accept();
            } catch (SocketTimeoutException e) {
              //noop;
            }

            _started = true;
            _server.setSoTimeout(0);
            notifyStarted();

            if (client == null) {
              continue;
            }
          } else {
            client = _server.accept();
          }
        } catch (SocketTimeoutException ste) {
          //noop;
          continue;
        } catch (SocketException e) {
          if (!_started) {
            _startExc   = e;
            _started    = true;
            notifyStarted();
          }

          break;
        }

        final Connection conn = _fac.newConnection(client);

        Request          req = new Request(conn,
            new TCPAddress(getAddress(), getPort()));
        ((PooledThread) _tp.acquire()).exec(req);
      } catch (Throwable t) {
        if (handleError(t)) {
          close();

          break;
        }
      }
    }
  }

  /**
   * Call to block on this instance until its internal socket server
   * has been started.
   *
   * @throws InterruptedException if the calling thread is interrupted while waiting.
   * @throws SocketException if an exception occurs while internally performing startup.
   */
  public synchronized void waitStarted()
    throws InterruptedException, SocketException {
    while (!_started) {
      wait();
    }

    if (_startExc != null) {
      throw _startExc;
    }
  }

  protected synchronized void notifyStarted() {
    notifyAll();
  }

  protected boolean handleError(Throwable t) {
    t.printStackTrace();

    return true;
  }
}
