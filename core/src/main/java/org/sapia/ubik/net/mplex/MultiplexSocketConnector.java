package org.sapia.ubik.net.mplex;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;

/**
 * This class is the entry point to get client socket connections in the
 * multiplex logic. Its functionality is similar to the traditional server
 * socket, which gives you the next socket connection upon calling the
 * {@link #accept()} method.
 * <p>
 * 
 * A connector instance can be created using the
 * {@link MultiplexServerSocket#createSocketConnector(StreamSelector)} providing
 * a {@link StreamSelector} object. Upon this call, the server socket will
 * create a new connector instance that will be associated to the server socket.
 * The selector passed in will be used to determine if this connector will
 * handle or not a new client socket connection. After you need to call the
 * <code>accept()</code> to get the next client socket connection (blocking
 * call). Finally when you are done with this socket connector a call to the
 * <code>close()</code> method is required to release all the resource used by
 * this socket connector. Note that closing the socket connector will not close
 * the underlying server socket.
 * <p>
 * 
 * All the accessor methods that returns a state delegates the call to the
 * underlying server socket. For example a call to get the port number on which
 * is connector is bound to, using the <code>getLocalPort()</code> method, will
 * pass the request to the server socket that is associated with this socket
 * connector.
 * 
 * @see MultiplexServerSocket
 * @see StreamSelector
 * @author <a href="mailto:jc@sapia-oss.org">Jean-Cedric Desrochers</a>
 */
public interface MultiplexSocketConnector {
  /**
   * Returns the port on which this socket is listening.
   * 
   * @return The port number to which this socket is listening or -1 if the
   *         socket is not bound yet.
   */
  public int getLocalPort();

  /**
   * Returns the local address of this server socket.
   * 
   * @return The address to which this socket is bound, or <code>null</code> if
   *         the socket is unbound.
   */
  public InetAddress getInetAddress();

  /**
   * Returns the address of the endpoint this socket is bound to, or
   * <code>null</code> if it is not bound yet.
   * 
   * @return A {@link SocketAddress} representing the local endpoint of this
   *         socket, or <code>null</code> if it is not bound yet.
   */
  public SocketAddress getLocalSocketAddress();

  /**
   * Gets the value of the SO_RCVBUF option for this socket interceptor that is
   * the proposed buffer size that will be used for Sockets accepted from this
   * socket interceptor.
   * 
   * @return the value of the SO_RCVBUF option for this Socket.
   * @exception SocketException
   *              if there is an error in the underlying protocol.
   */
  public int getReceiveBufferSize() throws SocketException;

  /**
   * Tests if SO_REUSEADDR is enabled.
   * 
   * @return A <code>boolean</code> indicating whether or not SO_REUSEADDR is
   *         enabled.
   * @exception SocketException
   *              if there is an error in the underlying protocol.
   */
  public boolean getReuseAddress() throws SocketException;

  /**
   * Retrieve setting for SO_TIMEOUT. Zero returns implies that the option is
   * disabled (i.e., timeout of infinity).
   * 
   * @return the SO_TIMEOUT value
   * @exception IOException
   *              if an I/O error occurs
   */
  public int getSoTimeout() throws IOException;

  /**
   * Returns the binding state of the socket.
   * 
   * @return True if the socket succesfuly bound to an address.
   */
  public boolean isBound();

  /**
   * Returns the closed state of the socket.
   * 
   * @return True if the socket has been closed.
   */
  public boolean isClosed();

  /**
   * Listens for a connection to be made to this socket and accepts it. The
   * method blocks until a connection is made.
   * 
   * @return The new Socket
   * @exception IOException
   *              If an I/O error occurs when waiting for a connection.
   */
  public Socket accept() throws IOException;

  /**
   * Closes this multiplex socket handler.
   * 
   * @exception IOException
   *              if an I/O error occurs when closing the socket.
   */
  public void close() throws IOException;
}
