package org.sapia.ubik.net.mplex;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.sapia.ubik.rmi.server.Log;


/**
 * This class is the main server socket that multiplexes the traditionnal
 * <code>java.net.ServerSocket</code>. That means that it listens on the port of the
 * server socket and it provides a mechanism to register different handlers for
 * these new incoming socket connections. These handlers are called socket connectors
 * and they can be used to process multiple types of data streams over the same socket.<p>
 *
 * For instance, using this <code>MultiplexServerSocket</code> you could handle serialized
 * Java objects and HTTP requests using two different connectors (two distinct handling
 * logic), same host/port. This is achieved through a read-ahead on the incoming stream of data
 * from a new client socket connection. When a new incoming connection is requested by a client,
 * all the registered <code>StreamSelector</code>s are used to determine which socket connector
 * will handle the new connection.<p>
 *
 * The internals of this <code>MultiplexServerSocket</code> are simple. An instance of this class
 * works on an asynchronous process were new socket connections are first accepted, and then selected.
 * These two steps are described as follows:
 * <ol>
 *   <li><b>acceptor</b>: This first step involves the low-level logic of virtual machine that resides
 *        under the Java I/O package. When a client connects to the server, a new <code>MultiplexSocket</code> 
 *        is created on the server-side; the acceptor adds that new connection/socket to the internal
 *        pending queue.
 *   </li>
 *   <li><b>selector</b>: The selector listens on the queue of accepted connections and performs
 *       the logic of going through all the candidate socket connectors to determine which one of them
 *       handles the new socket connection; if none of the registered connectors is.
 *   </li>
 * </ol>
 * To perform this process, the <code>MultiplexServerSocket</code> contains two distinct pools of
 * threads that are configurable using the <code>setAcceptorDaemonThread(int)</code> and
 * <code>setSelectorDaemonThread(int)</code> methods.<p>
 *
 * The primary goal of the <code>MultiplexServerSocket</code> was to keep the "standard" (non-NIO)
 * programming model of the JDK regarding socket handling. Whether you use this server socket
 * directly or you use a socket connector, the logic is still the same for the client that
 * receives new incoming connections: it calls an <code>accept()</code> method that blocks
 * until a new connection is made. For integration with actual running systems, an adapter
 * is available to make a socket connector look like a server socket. We used that strategy
 * with the open-source Simple HTTP server and it worked transparently and fluidly.
 *
 * @see MultiplexSocket
 * @see MultiplexSocketConnector
 * @see ServerSocketAdapter
 * @see StreamSelector
 * @author <a href="mailto:jc@sapia-oss.org">Jean-Cedric Desrochers</a>
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2004 <a href="http://www.sapia-oss.org">
 *     Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *     <a href="http://www.sapia-oss.org/license.html" target="sapia-license">license page</a>
 *     at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class MultiplexServerSocket extends ServerSocket implements Runnable {
  /**
   * The default number of bytes read ahead fom incoming socket connections.
   */
  public static final short DEFAULT_READ_AHEAD_BUFFER_SIZE = 64;

  /**
   * The default number of acceptor daemon thread used to accept connections.
   */
  public static final short DEFAULT_ACCEPTOR_DAEMON_THREAD = 3;

  /**
   * The default number of selector daemon thread used to accept connections.
   */
  public static final short DEFAULT_SELECTOR_DAEMON_THREAD = 3;

  /** The list of created connectors that can handle new connections on this server. */
  private List<MultiplexSocketConnector> _theConnectors = new ArrayList<MultiplexSocketConnector>();

  /** The default connector that handles incoming connections. */
  private SocketConnectorImpl _theDefaultConnector;

  /** The list of running acceptor daemon threads. */
  private List<Thread> _theAcceptorDaemons = new ArrayList<Thread>();

  /** The list of running selector daemon threads. */
  private List<Thread> _theSelectorDaemons = new ArrayList<Thread>();

  /** The socket queue of accepted connection. */
  private SocketQueue _theAcceptedQueue = new SocketQueue();

  /** The number of acceptor daemon threads of this server. */
  private int _theAcceptorDaemonThread = DEFAULT_ACCEPTOR_DAEMON_THREAD;

  /** The number of selector daemon threads of this server. */
  private int _theSelectorDaemonThread = DEFAULT_SELECTOR_DAEMON_THREAD;

  /** The number of bytes read ahead when accepting a new connection. */
  private int _theReadAheadBufferSize = DEFAULT_READ_AHEAD_BUFFER_SIZE;
  
  /**
   * Creates a new MultiplexServerSocket instance. The server
   * socket is unbound
   *
   * @throws IOException If an error occurs opening the socket.
   */
  public MultiplexServerSocket() throws IOException {
    super();
  }

  /**
   * Creates a new MultiplexServerSocket instance. The new server will
   * be bound on the speficied port, or to a free port if the value passed
   * in is 0 (zero).
   *
   * The maximum queue length for incoming connection indications (a
   * request to connect) is set to <code>50</code>. If a connection
   * indication arrives when the queue is full, the connection is refused.
   *
   * @param port The port number to bind the server or 0 to use any port.
   * @throws IOException If an error occurs when opening the socket.
   */
  public MultiplexServerSocket(int port) throws IOException {
    super(port, 50);
  }

  /**
   * Creates a new MultiplexServerSocket instance. The new server will
   * be bound on the speficied port, or to a free port if the value passed
   * in is 0 (zero).
   *
   * The maximum queue length for incoming connection indications (a
   * request to connect) is set to the <code>backlog</code> parameter. If
   * a connection indication arrives when the queue is full, the
   * connection is refused.
   *
   * @param port The port number to bind the server or 0 oi use any port.
   * @param backlog The maximum length of the queue.
   * @throws IOException If an error occurs when opening the socket.
   */
  public MultiplexServerSocket(int port, int backlog) throws IOException {
    super(port, backlog);
  }

  /**
   * Creates a new MultiplexServerSocket instance. The new server will
   * be bound on the speficied port, or to a free port if the value passed
   * in is 0 (zero). The server will use the local IP address represented
   * by the <code>bindAddr</code> passed in.
   *
   * The maximum queue length for incoming connection indications (a
   * request to connect) is set to the <code>backlog</code> parameter. If
   * a connection indication arrives when the queue is full, the
   * connection is refused.
   *
   * @param port The port number to bind the server or 0 oi use any port.
   * @param backlog The maximum length of the queue.
   * @param bindAddr The local TCP address the server will bind to. If <code>null</code>
   *        the server will accept connections on any/all local addresses.
   * @throws IOException If an error occurs when opening the socket.
   */
  public MultiplexServerSocket(int port, int backlog, InetAddress bindAddr)
    throws IOException {
    super(port, backlog, bindAddr);
  }

  /**
   * Returns the size of the buffer used to pre-read the incoming bytes of the
   * accepted connection.
   *
   * @return The size of the read ahead buffer size.
   */
  public int getReadAheadBufferSize() {
    return _theReadAheadBufferSize;
  }

  /**
   * Changes the size of the read ahead buffer size. This method can only be called before starting
   * the server (ie. the initial call to the method <code>accept()</code>.
   *
   * @param aSize The new size.
   * @exception IllegalStateException If the server is already running.
   */
  public void setReadAheadBufferSize(int aSize) {
    if (aSize <= 0) {
      throw new IllegalArgumentException("The size is less than zero");
    } else if (_theAcceptorDaemons.size() > 0) {
      throw new IllegalStateException(
        "Cannot change the read ahead buffer size on a running server socket");
    }

    _theReadAheadBufferSize = aSize;
  }

  /**
   * Returns the number of daemon threads used to accept incoming connections.
   *
   * @return The number of daemon threads used to accept incoming connections.
   */
  public int getAcceptorDaemonThread() {
    return _theAcceptorDaemonThread;
  }

  /**
   * Returns the number of daemon threads used to select an connector for incoming connections.
   *
   * @return The number of daemon threads used to select an connector for incoming connections.
   */
  public int getSelectorDaemonThread() {
    return _theSelectorDaemonThread;
  }

  /**
   * Changes the number of daemon threads used to accept incoming connections.
   *
   * @param maxThread The new numbe of running daemon.
   * @exception IllegalStateException If the server is already running.
   */
  public void setAcceptorDaemonThread(int maxThread) {
    if (maxThread <= 0) {
      throw new IllegalArgumentException("The size is less than zero");
    } else if (_theAcceptorDaemons.size() > 0) {
      throw new IllegalStateException(
        "Cannot change the number of acceptor daemons on a running server socket");
    }

    _theAcceptorDaemonThread = maxThread;
  }

  /**
   * Changes the number of daemon threads used to select connectors for incoming connections.
   *
   * @param maxThread The new numbe of running daemon.
   * @exception IllegalStateException If the server is already running.
   */
  public void setSelectorDaemonThread(int maxThread) {
    if (maxThread <= 0) {
      throw new IllegalArgumentException("The size is less than zero");
    } else if (_theSelectorDaemons.size() > 0) {
      throw new IllegalStateException(
        "Cannot change the number of selector daemons on a running server socket");
    }

    _theSelectorDaemonThread = maxThread;
  }

  /**
   * This factory method creates a socket connector through which a client will be able
   * to receive incoming socket connections. The stream selector passed in will be used
   * y this multiplex server socket to determine if an incoming socket connection must
   * be handled by the created socket connector.
   *
   * @param aSelector The stream selector to assign to the created socket connector.
   * @return The created socket connector.
   * @throws IllegalStateException If this server socket is closed.
   */
  public synchronized MultiplexSocketConnector createSocketConnector(
    StreamSelector aSelector) {
    if (aSelector == null) {
      throw new IllegalArgumentException("The selector passed in is null");
    } else if (isClosed()) {
      throw new IllegalStateException(
        "Could not create a socket connector, the server socket is closed");
    }

    MultiplexSocketConnector aConnector = new SocketConnectorImpl(this,
        aSelector, new SocketQueue());
    _theConnectors.add(aConnector);

    return aConnector;
  }

  /**
   * Internal method that initializes the default connector that gets all the incoming socket
   * connections. It also creates all the acceptor and selector thread pools according to the
   * configuration.
   */
  private synchronized void initializeDefaultConnector() {
    if (_theDefaultConnector == null) {
      // Create the default handler
      _theDefaultConnector = new SocketConnectorImpl(this,
          new PositiveStreamSelector(), new SocketQueue());

      // Create the selector daemons
      for (int i = 1; i <= _theAcceptorDaemonThread; i++) {
        Thread aDaemon = new Thread(new SelectorTask(),
            "MultiplexServerSocket-Selector" + i);
        aDaemon.setDaemon(true);
        _theSelectorDaemons.add(aDaemon);
        aDaemon.start();
      }

      // Create the accceptor daemons
      for (int i = 1; i <= _theAcceptorDaemonThread; i++) {
        Thread aDaemon = new Thread(this, "MultiplexServerSocket-Acceptor" + i);
        aDaemon.setDaemon(true);
        _theAcceptorDaemons.add(aDaemon);
        aDaemon.start();
      }
    }
  }

  /**
   * Removes the socket connector passed in from the list of available connectors
   * associated with this server socket. After this method the multiplex server socket
   * will no longer send new incoming socket connection to the socket connector.
   *
   * @param aConnector The socket connector to remove from this server socket.
   */
  public synchronized void removeSocketConnector(
    MultiplexSocketConnector aConnector) {
    if (aConnector == null) {
      throw new IllegalArgumentException("The connector passed in is null");
    }

    _theConnectors.remove(aConnector);
  }

  /**
   * Extract the first bytes of the multiplex socket passed.
   *
   * @param aSocket The socket from which to get the data.
   * @param aMaxLength The maximum number of bytes to extract.
   * @return The array of bytes representingthe header.
   * @throws IOException If an error occurs extracting the header.
   */
  private byte[] extractHeader(MultiplexSocket aSocket, int aMaxLength)
    throws IOException {
    // Extract the first bytes of the input stream
    byte[] preview = new byte[aMaxLength];
    int    length = aSocket.getPushbackInputStream().read(preview, 0,
        preview.length);

    // Put back the headers in the stream
    aSocket.getPushbackInputStream().unread(preview, 0, length);

    // Trim the array of bytes
    byte[] header;

    if (length < preview.length) {
      header = new byte[length];
      System.arraycopy(preview, 0, header, 0, length);
    } else {
      header = preview;
    }

    return header;
  }

  /**
   * Listens for a connection to be made to this socket and accepts it. The default
   * connector  of this server socket will be used to act as the main port of entry
   * for clients directly using the server socket instead of the default socket
   * connector. The method blocks until a connection is made.
   *
   * @return The new Socket
   * @exception IOException If an I/O error occurs when waiting for a connection.
   * @exception SocketTimeoutException if a timeout was previously set with setSoTimeout and
   *            the timeout has been reached.
   */
  public Socket accept() throws IOException {
    if (isClosed()) {
      throw new SocketException("Socket is closed");
    } else if (!isBound()) {
      throw new SocketException("Socket is not bound yet");
    } else if (_theDefaultConnector == null) {
      initializeDefaultConnector();
    }

    return _theDefaultConnector.getQueue().getSocket();
  }

  /**
   * Closes this multiplex server socket. If socket connectors are associated to
   * this server scket then they are closed as well.
   *
   * @exception IOException If an I/O error occurs when closing the socket.
   */
  public synchronized void close() throws IOException {
    try {
      super.close();
    } finally {
      if (_theDefaultConnector != null) {
        _theDefaultConnector.close();
      }

      // May close the acceptor and selector threads here!!!
      if (_theConnectors != null) {
        // To avoid concurrent modifs when removing
        for (MultiplexSocketConnector connector: new ArrayList<MultiplexSocketConnector>(_theConnectors)) {
          connector.close();
        }
      }
    }
  }

  /**
   * Returns the implementation address and implementation port of
   * this socket as a <code>String</code>.
   *
   * @return A string representation of this socket.
   */
  public String toString() {
    StringBuffer aBuffer = new StringBuffer();
    aBuffer.append("MultiplexServerSocket[").append(super.toString()).append("]");

    return aBuffer.toString();
  }

  /**
   * Implements the Runnable interface and it performs the asynchronous acceptor logic
   * of the multiplex. Used by the acceptor threads, it listens on the underlying
   * server socket and dispacthes all the incoming socket connections to the queue
   * of accepted socket connection. The client socket connections put in that queue are
   * then processed asynchronously by the selector threads.
   */
  public void run() {
    try {
      Log.warning(getClass(),
        new Date() + " [" + Thread.currentThread().getName() +
        "] MultiplexServerSocket * REPORT * Starting this acceptor thread");

      // Loop for accepting incoming client socket connection  
      while (!isClosed() && !Thread.interrupted()) {
        try {
          // Wait for a connection
          MultiplexSocket aClient = new MultiplexSocket(null,
              _theReadAheadBufferSize);
          implAccept(aClient);

          _theAcceptedQueue.add(aClient);
          
        } catch (IOException ioe) {
          _theDefaultConnector.getQueue().setException(ioe);
        } catch (RuntimeException re) {
          _theDefaultConnector.getQueue().setException(new IOException(re.getLocalizedMessage()));
        }
      }
      
    } catch (Exception e) {
      Log.error(getClass(),
        new Date() + " [" + Thread.currentThread().getName() +
        "] MultiplexServerSocket * ERROR * An unhandled exception occured in this acceptor thread... EXITING LOOP",
        e);
    } finally {
      Log.warning(getClass(),
        new Date() + " [" + Thread.currentThread().getName() +
        "] MultiplexServerSocket * REPORT * Stopping this acceptor thread");
    }
  }

  /**
   * This utility method is used to select the socket connector that requests the new
   * client socket connection passed in. Note that this method will only use as candidate
   * selectors the one that are registered with this server socket (and not the default one).
   *
   * @param aClient The client socket connection for which to select a connector.
   * @return The first connector that selects the socket connection, or <code>null</code>
   *         if no connector selects the client socket r if there is no registered connector.
   * @exception IOException If an error occurs selecting a connector for the socket.
   */
  private SocketConnectorImpl selectConnector(MultiplexSocket aClient)
    throws IOException {
    SocketConnectorImpl aConnector = null;

    if (_theConnectors.size() > 0) {
      // Extract the first bytes of the socket input stream
      byte[] header = extractHeader(aClient, _theReadAheadBufferSize);

      // Select the right connector
      synchronized (this) {
        for (Iterator<MultiplexSocketConnector> it = _theConnectors.iterator(); it.hasNext();) {
          SocketConnectorImpl aCandidate = (SocketConnectorImpl) it.next();

          if (aCandidate.getSelector().selectStream(header)) {
            aConnector = aCandidate;

            break;
          }
        }
      }
    }

    return aConnector;
  }

  /**
   * This class is responsible of the selection logic of the multiplex.
   */
  public class SelectorTask implements Runnable {
    /**
     * This method waits on the queue of accepted client socket connection
     * and will processes asynchonously, using the selector threads, to the election
     * of the appropriate socket selector for the new accepted socket connection.
     */
    public void run() {
      try {
        Log.warning(getClass(),
          new Date() + " [" + Thread.currentThread().getName() +
          "] MultiplexServerSocket * REPORT * Starting this selector thread");

        // Loop for selecting new client socket connections
        while (!isClosed() && !Thread.interrupted()) {
          MultiplexSocket     aSocket;
          SocketConnectorImpl aConnector;

          try {
            // Get the next accepted client socket
            aSocket   = (MultiplexSocket) _theAcceptedQueue.getSocket();

            // Selects a registered connector
            aConnector = selectConnector(aSocket);

            // If no connector selected, use the default one
            if (aConnector == null) {
              _theDefaultConnector.getQueue().add(aSocket);
            } else {
              aConnector.getQueue().add(aSocket);
            }
            
          } catch (IOException ioe) {
            _theDefaultConnector.getQueue().setException(ioe);
          } catch (RuntimeException re) {
            _theDefaultConnector.getQueue().setException(new IOException(re.getLocalizedMessage()));
          }
        }
        
      } catch (Exception e) {
        Log.error(new Date() + " [" + Thread.currentThread().getName() +
          "] MultiplexServerSocket * ERROR * An unhandled exception occured in this selector thread... EXITING LOOP",
          e);
      } finally {
        Log.warning(getClass(),
          new Date() + " [" + Thread.currentThread().getName() +
          "] MultiplexServerSocket * REPORT * Stopping this selector thread");
      }
    }
  }
}
