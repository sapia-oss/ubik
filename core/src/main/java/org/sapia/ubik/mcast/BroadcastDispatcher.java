package org.sapia.ubik.mcast;

import java.io.IOException;

import org.sapia.ubik.net.ConnectionStateListener;
import org.sapia.ubik.net.ServerAddress;
import org.sapia.ubik.util.Conf;

/**
 * An instance of this interface broadcasts remote events to potentially
 * multiple nodes.
 * 
 * @author Yanick Duchesne
 */
public interface BroadcastDispatcher {
  
  /**
   * @param context the {@link DispatcherContext} holding this instance's initialization data.
   */
  public void initialize(DispatcherContext context);
  
  /**
   * Dispatches a multicast event holding the given parameters.
   * 
   * @param unicastAddr
   *          the unicast {@link ServerAddress} of the caller, or
   *          <code>null</code> if such an address does not exist.
   * @param alldomains
   *          if <code>true</code> sends an event to all domains.
   * @param type
   *          the logical type of the event.
   * @param data
   *          the data that is encapsulated within the event.
   * @throws IOException
   *           if an IO problem occurs.
   */
  public void dispatch(ServerAddress unicastAddr, boolean alldomains, String type, Object data) throws IOException;

  /**
   * Dispatches a multicast event to the given domain.
   * 
   * @param unicastAddr
   *          the unicast {@link ServerAddress} of the caller, or
   *          <code>null</code> if such an address does not exist.
   * @param domain
   *          the domain to dispatch the event to.
   * @param type
   *          the logical type of the event.
   * @param data
   *          the data that is encapsulated within the event.
   * @throws IOException
   *           if an IO problem occurs.
   */
  public void dispatch(ServerAddress unicastAddr, String domain, String type, Object data) throws IOException;

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
   * @return this instance's {@link MulticastAddress}.
   */
  public MulticastAddress getMulticastAddress();
  
  /**
   * @param listener a {@link ConnectionStateListener} to add.
   */
  public void addConnectionStateListener(ConnectionStateListener listener);
  
  /**
   * @param listener a {@link ConnectionStateListener} to remove.
   */
  public void removeConnectionStateListener(ConnectionStateListener listener);
  
  /**
   * @param props the {@link Conf} instance to use to retrieve the multicast address in properties form. 
   * @return a {@link MulticastAddress} corresponding to the resolved multicast address properties.
   */
  public MulticastAddress getMulticastAddressFrom(Conf props);
}
