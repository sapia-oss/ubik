package org.sapia.ubik.net.mplex;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;


/**
 * This utility class adapts a <code>MultiplexSocketConnector</code> to a
 * <code>java.net.ServerSocket<code>. That means that a socket connector, that
 * was previously created with a multiplex server socket, can be used as
 * a traditionnal server socket. This adapter can be useful when integrating
 * the multiplex module into existing code that rely on the <code>java.net.ServerSocket<code>
 * object.<p>
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
 * Calling any of these methods will result in an <code>UnsupportedOperationException</code>
 * beign thrown.
 *
 *
 * @author <a href="mailto:jc@sapia-oss.org">Jean-Cedric Desrochers</a>
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2004 <a href="http://www.sapia-oss.org">
 *     Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *     <a href="http://www.sapia-oss.org/license.html" target="sapia-license">license page</a>
 *     at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class ServerSocketAdapter extends ServerSocket {
  /** The socket connector wrapped by this adapter. */
  private MultiplexSocketConnector _theDelegate;

  /**
   * Creates a new ServerSocketAdapter instance.
   */
  public ServerSocketAdapter(MultiplexSocketConnector anInterceptor)
    throws IOException {
    super();
    _theDelegate = anInterceptor;
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
    return _theDelegate.getInetAddress();
  }

  /**
   * @see java.net.ServerSocket#getLocalPort()
   */
  public int getLocalPort() {
    return _theDelegate.getLocalPort();
  }

  /**
   * @see java.net.ServerSocket#getLocalSocketAddress()
   */
  public SocketAddress getLocalSocketAddress() {
    return _theDelegate.getLocalSocketAddress();
  }

  /**
   * @see java.net.ServerSocket#accept()
   */
  public Socket accept() throws IOException {
    return _theDelegate.accept();
  }

  /**
   * @see java.net.ServerSocket#close()
   */
  public void close() throws IOException {
    _theDelegate.close();
  }

  /**
   * @see java.net.ServerSocket#isBound()
   */
  public boolean isBound() {
    return _theDelegate.isBound();
  }

  /**
   * @see java.net.ServerSocket#isClosed()
   */
  public boolean isClosed() {
    return _theDelegate.isClosed();
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
    return _theDelegate.getSoTimeout();
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
    return _theDelegate.getReuseAddress();
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
    return _theDelegate.getReceiveBufferSize();
  }
}
