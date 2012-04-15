package org.sapia.ubik.net.mplex;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;


/**
 * This utility class adapts a {@link MultiplexSocketConnector} to a
 * {@link java.net.ServerSocket}. That means that a socket connector, that
 * was previously created with a multiplex server socket, can be used as
 * a traditionnal server socket. This adapter can be useful when integrating
 * the multiplex module into existing code that rely on the {@link java.net.ServerSocket}
 * object.
 * <p>
 *
 * Note that the following methods are not supported:
 * <ul>
 *   <li>bind(SocketAddress endpoint)</li>
 *   <li>bind(SocketAddress endpoint, int backlog)</li>
 *   <li>setSoTimeout(int timeout)</li>
 *   <li>setReuseAddress(boolean on)</li>
 *   <li>setReceiveBufferSize(int size)</li>
 * </ul><p>
 *
 * Calling any of these methods will result in an {@link UnsupportedOperationException}
 * beign thrown.
 *
 *
 * @author <a href="mailto:jc@sapia-oss.org">Jean-Cedric Desrochers</a>
 */
public class ServerSocketAdapter extends ServerSocket {
  /** The socket connector wrapped by this adapter. */
  private MultiplexSocketConnector theDelegate;

  /**
   * Creates a new ServerSocketAdapter instance.
   */
  public ServerSocketAdapter(MultiplexSocketConnector anInterceptor)
    throws IOException {
    super();
    theDelegate = anInterceptor;
  }

  /**
   * @see java.net.ServerSocket#bind(java.net.SocketAddress)
   */
  public void bind(SocketAddress endpoint) throws IOException {
    throw new UnsupportedOperationException();
  }

  /**
   * @see java.net.ServerSocket#bind(java.net.SocketAddress, int)
   */
  public void bind(SocketAddress endpoint, int backlog)
    throws IOException {
    throw new UnsupportedOperationException();
  }

  /**
   * @see java.net.ServerSocket#getInetAddress()
   */
  public InetAddress getInetAddress() {
    return theDelegate.getInetAddress();
  }

  /**
   * @see java.net.ServerSocket#getLocalPort()
   */
  public int getLocalPort() {
    return theDelegate.getLocalPort();
  }

  /**
   * @see java.net.ServerSocket#getLocalSocketAddress()
   */
  public SocketAddress getLocalSocketAddress() {
    return theDelegate.getLocalSocketAddress();
  }

  /**
   * @see java.net.ServerSocket#accept()
   */
  public Socket accept() throws IOException {
    return theDelegate.accept();
  }

  /**
   * @see java.net.ServerSocket#close()
   */
  public void close() throws IOException {
    theDelegate.close();
  }

  /**
   * @see java.net.ServerSocket#isBound()
   */
  public boolean isBound() {
    return theDelegate.isBound();
  }

  /**
   * @see java.net.ServerSocket#isClosed()
   */
  public boolean isClosed() {
    return theDelegate.isClosed();
  }

  /**
   * @see java.net.ServerSocket#setSoTimeout(int)
   */
  public void setSoTimeout(int timeout) throws SocketException {
    throw new UnsupportedOperationException();
  }

  /**
   * @see java.net.ServerSocket#getSoTimeout()
   */
  public int getSoTimeout() throws IOException {
    return theDelegate.getSoTimeout();
  }

  /**
   * @see java.net.ServerSocket#setReuseAddress(boolean)
   */
  public void setReuseAddress(boolean on) throws SocketException {
    throw new UnsupportedOperationException();
  }

  /**
   * @see java.net.ServerSocket#getReuseAddress()
   */
  public boolean getReuseAddress() throws SocketException {
    return theDelegate.getReuseAddress();
  }

  /**
   * @see java.net.ServerSocket#setReceiveBufferSize(int)
   */
  public void setReceiveBufferSize(int size) throws SocketException {
    throw new UnsupportedOperationException();
  }

  /**
   * @see java.net.ServerSocket#getReceiveBufferSize()
   */
  public int getReceiveBufferSize() throws SocketException {
    return theDelegate.getReceiveBufferSize();
  }
}
