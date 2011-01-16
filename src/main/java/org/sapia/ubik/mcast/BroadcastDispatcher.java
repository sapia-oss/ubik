package org.sapia.ubik.mcast;

import java.io.IOException;

import org.sapia.ubik.net.ServerAddress;


/**
 * An instance of this interface dispatches multicast events.
 *
 * @author Yanick Duchesne
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public interface BroadcastDispatcher {
  /**
   * Dispatches a multicast event holding the given parameters.
   *
   * @param unicastAddr the unicast {@link ServerAddress} of the caller, or <code>null</code> if such an address
   * does not exist.
   * @param alldomains if <code>true</code> sends an event to all domains.
   * @param id the logical identifier of the event.
   * @param data the data that is encapsulated within the event.
   * @throws IOException if an IO problem occurs.
   */
  public void dispatch(ServerAddress unicastAddr , boolean alldomains, String id, Object data)
    throws IOException;

  /**
   * Dispatches a multicast event to the given domain.
   *
   * @param domain the domain to dispatch the event to.
   * @param id the logical identifier of the event.
   * @param data the data that is encapsulated within the event.
   * @throws IOException if an IO problem occurs.
   */
  /*public void dispatch(String domain, String id, Object data)
    throws IOException;*/

  /**
   * Dispatches a multicast event to the given domain.
   * 
   * @param unicastAddr the unicast {@link ServerAddress} of the caller, or <code>null</code> if such an address
   * does not exist.
   * @param domain the domain to dispatch the event to.
   * @param id the logical identifier of the event.
   * @param data the data that is encapsulated within the event.
   * @throws IOException if an IO problem occurs.
   */
  public void dispatch(ServerAddress unicastAddr, String domain, String id, Object data)
    throws IOException;
  
  
  /**
   * Sets this instance's "buffer size". The size is specified in bytes, and can
   * be interpreted differently from one implementation to another - for example, for
   * UDP-based implementation, it can correspond to the datagram packet size.
   *
   * @param size the size of this instance's internal buffer, in bytes.
   */
  public void setBufsize(int size);

  /**
   * Starts this instance - must be called prior to using this instance.
   */
  public void start();

  /**
   * Closes this instance, which can't be used thereafter.
   */
  public void close();

  /**
   * Returns the node identifier of this instance.
   *
   * @return this instance's node identifier.
   */
  public String getNode();

  /**
   * Returns this instance's multicast port.
   *
   * @return a multicast port.
   */
  public int getMulticastPort();

  /**
   * Returns this instance's multicast address.
   *
   * @return a multicast address string.
   */
  public String getMulticastAddress();
}
