package org.sapia.ubik.net.mplex;

import java.io.IOException;

import java.net.Socket;
import java.net.SocketException;

import java.util.Iterator;
import java.util.LinkedList;


/**
 * This class implements a simple asynchronous, thread safe, FIFO queue of
 * <code>Socket</code> objects. A producer actor can add a new socket to the
 * queue by calling the <code>add(Socket)</code> method. A consumer actor can
 * retrieve the next available socket from the queue using the <code>getSocket()</code>
 * method.<p>
 *
 * Upon creation a <code>SocketQueue</code> is considered active or open; therefore
 * producers and consumers can use the queue. When the queue is no longer necessary
 * the <code>close()</clode> method will put the queue in a closed state where:
 * <ul>
 *   <li>All the pending <code>Socket</code> objects in the queue will be removed
 *       from the queue and then explicitly closed.</li>
 *   <li>All the consumers that are blocked on the <code>getSocket()</code> method
 *       will return with a <code>SocketException</code>.</li>
 *   <li>Any future call to the <code>add(Socket)</code> or <code>getSocket()</code>
 *       method will result in a <code>SocketException</code>.</li>
 *   <li></li>
 * </ul><p>
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
public class SocketQueue {
  /** The list that act as a queue. */
  private LinkedList<Socket> _theSockets;

  /** The close indicator of the queue. */
  private boolean _isClosed;

  /** The reference over the last exception that occured. */
  private IOException _theLastException;

  /**
   * Creates a new SocketQueue instance.
   */
  public SocketQueue() {
    _theSockets   = new LinkedList<Socket>();
    _isClosed     = false;
  }

  /**
   * Asynchronously add the socket passed in to the internal queue.
   *
   * @param aSocket The socket to add.
   */
  public synchronized void add(Socket aSocket) throws SocketException {
    if (_isClosed) {
      throw new SocketException(
        "Could not add socket - the socket queue is closed");
    }

    _theLastException = null;
    _theSockets.addLast(aSocket);
    notify();
  }

  /**
   * Returns the next available socket. This method will block until
   * a socket is available.
   *
   * @return The next available socket.
   */
  public synchronized Socket getSocket() throws IOException {
    if (_isClosed) {
      throw new SocketException(
        "No socket available - the socket queue is closed");
    } else if (_theSockets.isEmpty()) {
      try {
        wait();
      } catch (InterruptedException ie) {
        // noop
      }
    }

    if (_isClosed) {
      throw new SocketException(
        "No socket available - the socket queue is closed");
    } else if (_theLastException != null) {
      IOException ioe = _theLastException;
      _theLastException = null;
      throw ioe;
    } else if (_theSockets.isEmpty()) {
      return getSocket();
    } else {
      return (Socket) _theSockets.removeFirst();
    }
  }

  /**
   * Close this queue.
   */
  public synchronized void close() {
    _isClosed = true;

    for (Iterator<Socket> it = _theSockets.iterator(); it.hasNext();) {
      Socket socket = (Socket) it.next();

      try {
        socket.close();
      } catch (IOException ioe) {
        // noop
      }
    }

    _theSockets.clear();
    notifyAll();
  }

  /**
   * Sets the exception to this queue.
   *
   * @param anException The exception to send to the client.
   */
  public synchronized void setException(IOException anException) {
    _theLastException = anException;
    notify();
  }

  /**
   * Returns the current exception of this queue.
   *
   * @return The current exception of this queue.
   */
  public IOException getException() {
    return _theLastException;
  }

  /**
   * Returns the number of socket pending in the internal queue.
   *
   * @return The number of socket pending in the internal queue.
   */
  public int size() {
    return _theSockets.size();
  }
}
