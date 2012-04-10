package org.sapia.ubik.net.mplex;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;

import org.sapia.ubik.net.ThreadInterruptedException;


/**
 * Class documentation
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
public class SocketConnectorImpl implements MultiplexSocketConnector {
  /** The multiplex server socket to which this handled is registered. */
  private MultiplexServerSocket _theServerSocket;

  /** The stream selector that was registered with this handler. */
  private StreamSelector _theSelector;

  /** The queue of incoming socket connection. */
  private SocketQueue _theQueue;

  /** Indicates if this socket connector is closed. */
  private boolean _isClosed;

  /**
   * Creates a new SocketConnectorImpl instance.
   */
  public SocketConnectorImpl(MultiplexServerSocket aServerSocket,
    StreamSelector aSelector, SocketQueue aQueue) {
    _theServerSocket   = aServerSocket;
    _theSelector       = aSelector;
    _theQueue          = aQueue;
    _isClosed          = false;
  }

  /**
   * Returns the selector associated to this handler.
   *
   * @return The selector associated to this handler.
   */
  public StreamSelector getSelector() {
    return _theSelector;
  }

  /**
   * Returns the socket queue of this handler.
   *
   * @return The socket queue of this handler.
   */
  public SocketQueue getQueue() {
    return _theQueue;
  }

  /**
   * Returns the port on which this socket is listening.
   *
   * @return The port number to which this socket is listening or
   *          -1 if the socket is not bound yet.
   */
  public int getLocalPort() {
    return _theServerSocket.getLocalPort();
  }

  /**
   * Returns the local address of this server socket.
   *
   * @return The address to which this socket is bound, or
   *         <code>null</code> if the socket is unbound.
   */
  public InetAddress getInetAddress() {
    return _theServerSocket.getInetAddress();
  }

  /**
   * Returns the address of the endpoint this socket is bound to, or
   * <code>null</code> if it is not bound yet.
   *
   * @return A <code>SocketAddress</code> representing the local endpoint of this
   *         socket, or <code>null</code> if it is not bound yet.
   */
  public SocketAddress getLocalSocketAddress() {
    return _theServerSocket.getLocalSocketAddress();
  }

  /**
   * Gets the value of the SO_RCVBUF option for this socket interceptor
   * that is the proposed buffer size that will be used for Sockets accepted
   * from this socket interceptor.
   *
   * @return the value of the SO_RCVBUF option for this Socket.
   * @exception SocketException if there is an error in the underlying protocol.
   */
  public int getReceiveBufferSize() throws SocketException {
    return _theServerSocket.getReceiveBufferSize();
  }

  /**
   * Tests if SO_REUSEADDR is enabled.
   *
   * @return A <code>boolean</code> indicating whether or not SO_REUSEADDR is enabled.
   * @exception SocketException if there is an error in the underlying protocol.
   */
  public boolean getReuseAddress() throws SocketException {
    return _theServerSocket.getReuseAddress();
  }

  /**
   * (i.e., timeout of infinity).
   *
   * @return the SO_TIMEOUT value
   * @exception IOException if an I/O error occurs
   */
  public int getSoTimeout() throws IOException {
    return _theServerSocket.getSoTimeout();
  }

  /**
   * Returns the binding state of the socket.
   *
   * @return True if the socket succesfuly bound to an address.
   */
  public boolean isBound() {
    return _theServerSocket.isBound();
  }

  /**
   * Returns the closed state of the socket.
   *
   * @return True if the socket has been closed.
   */
  public boolean isClosed() {
    return _isClosed || _theServerSocket.isClosed();
  }

  /**
   * Listens for a connection to be made to this socket and accepts
   * it. The method blocks until a connection is made.
   *
   * @return The new Socket
   * @exception IOException If an I/O error occurs when waiting for a connection.
   */
  public Socket accept() throws IOException {
    try {
      return _theQueue.getSocket();
    } catch (ThreadInterruptedException e) {
      SocketException se = new SocketException("Thread interrupted while waiting for socket");
      se.fillInStackTrace();
      throw e;
    }
  }

  /**
   * Closes this multiplex socket handler.
   *
   * @exception  IOException  if an I/O error occurs when closing the socket.
   */
  public void close() throws IOException {
    _theServerSocket.removeSocketConnector(this);
    _theQueue.close();
    _isClosed = true;
  }
}
