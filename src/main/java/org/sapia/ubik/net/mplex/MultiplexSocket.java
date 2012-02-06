package org.sapia.ubik.net.mplex;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketImpl;


/**
 * This class is the implementation of client socket that is
 * used in Ubik's multiplex logic. It extends the traditionnal {@link Socket}
 * of the <code>java.net</code> package to add a read-ahead functionality on the
 * input stream of the socket.<p>
 *
 * This class has the following limitation were a maximum number of bytes that
 * can be read in advanced and put back in the input stream of the socket has to
 * be set when creating a new {@link MultiplexSocket} instance.
 *
 * @author <a href="mailto:jc@sapia-oss.org">Jean-Cedric Desrochers</a>
 */
public class MultiplexSocket extends Socket {
  
  private static final int PUSHBACK_BUFZ = 256;
  
  /** The input stream to provides the read-ahead logic. */
  private PushbackInputStream _theInput;

  /** The size of the number of the maximum number of bytes that can be read-ahead. */
  private int _thePushbackBufferSize = PUSHBACK_BUFZ;

  /**
   * Creates a new MultiplexSocket instance. The socket will not be connected.
   *
   * @param impl The socket impl to use.
   * @param bufferSize The maximum read-ahead buffer size.
   * @throws SocketException If an error occurs with the socket impl.
   */
  public MultiplexSocket(SocketImpl impl, int bufferSize)
    throws SocketException {
    super(impl);
    _thePushbackBufferSize = bufferSize;
  }

  /**
   * Returns the input stream of this socket.
   *
   * @return The input stream of this socket.
   * @exception IOException If an I/O error occurs when creating the input stream,
   *            the socket is closed, the socket is not connected, or the socket
   *            input has been shutdown using {@link #shutdownInput()}.
   */
  public InputStream getInputStream() throws IOException {
    if (_theInput == null) {
      _theInput = new PushbackInputStream(new BufferedInputStream(
            super.getInputStream()), _thePushbackBufferSize);
    }

    return _theInput;
  }

  /**
   * Returns the input stream of this socket as a <code>PushbackInputStream</code>.
   *
   * @return The input stream of this socket as a <code>PushbackInputStream</code>.
   */
  public PushbackInputStream getPushbackInputStream() throws IOException {
    return (PushbackInputStream) getInputStream();
  }

  /**
   * Returns the implementation address and implementation port of
   * this socket as a <code>String</code>.
   *
   * @return A string representation of this socket.
   */
  public String toString() {
    StringBuffer aBuffer = new StringBuffer();
    aBuffer.append("MultiplexSocket[").append(super.toString()).append("]");

    return aBuffer.toString();
  }
}
