package org.sapia.ubik.mcast;

import org.sapia.ubik.net.ServerAddress;

import java.io.IOException;
import java.util.List;


/**
 * Implementations of this interface dispatch objects over the wire in a
 * point-to-point fashion. It is important to note that implementations
 * are expected to behave in a peer-to-peer fashion - they are at once client
 * and server (for their siblings).
 *
 * @author Yanick Duchesne
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public interface UnicastDispatcher {
  /**
   * Dispatches the given data to the node whose address is given.
   *
   * @param addr a {@link ServerAddress} that corresponds to the
   * destination node for the data passed in.
   * @param type the logical type of the data that is sent - allows the
   * receiver to perform logic according to the "type".
   * @param data the {@link Object} to send.
   */
  public void dispatch(ServerAddress addr, String type, Object data)
    throws IOException;

  /**
   * Sends the given data to the node whose address is given, returning the
   * corresponding response - received from the destination.
   *
   * @param addr a {@link ServerAddress} that corresponds to the
   * destination node for the data passed in.
   * @param type the logical type of the data that is sent - allows the
   * receiver to perform logic according to the "type".
   * @param data the {@link Object} to send.
   *
   * @return a {@link Response}.
   */
  public Response send(ServerAddress addr, String type, Object data)
    throws IOException;

  /**
   * Sends the given data to the list of destinations specified, and returning
   * the responses received from each destination.
   *
   * @param addresses a {@link List} of {@link ServerAddress} instances.
   * @param type the logical type of the data that is sent - allows the
   * receiver to perform logic according to the "type".
   * @param data the {@link Object} to send.
   *
   * @return a <code>RespList</code>.
   */
  public RespList send(java.util.List<ServerAddress> addresses, String type, Object data)
    throws IOException;

  /**
   * Starts this instance - should be called prior to using this instance.
   */
  public void start();

  /**
   * Closes this instance - which should not be used thereafter.
   */
  public void close();

  /**
   * Sets this instance's "buffer size". The size is specified in bytes, and can
   * be interpreted differently from one implementation to another - for example, for
   * UDP-based implementation, it can correspond to the datagram packet size.
   *
   * @param the size of this instance's internal buffer, in bytes.
   */
  public void setBufsize(int size);

  /**
   * Allows implementations to notify the passed in listener when a socket timeout occurs - this
   * applies if the underlying implementation uses a server restricted to blocking IO.
   *
   * @see java.net.DatagramSocket#setSoTimeout(int)
   * @see java.net.ServerSocket#setSoTimeout(int)
   */
  public void setSoTimeoutListener(SocketTimeoutListener listener);

  /**
   * Returns the address of this instance.
   *
   * @return a {@link ServerAddress}.
   *
   * @throws IllegalStateException if the address of this instance is not yet available.
   * This can be the case if the {@link #start()} method has not yet been called;
   * therefore, always call {@link #start()} before calling this method.
   *
   * @see #start()
   */
  public ServerAddress getAddress() throws IllegalStateException;
}
