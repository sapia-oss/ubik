package org.sapia.ubik.rmi.server;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.sapia.ubik.mcast.EventChannel;
import org.sapia.ubik.rmi.Consts;
import org.sapia.ubik.rmi.RemoteRuntimeException;


/**
 * Keeps event channels on a per-domain basis.
 *
 * @author Yanick Duchesne
 *
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
class EventChannelSingleton {
  private static Map<String, EventChannel>    _channels     = new ConcurrentHashMap<String, EventChannel>();
  private static String _mcastAddress = Consts.DEFAULT_MCAST_ADDR;
  private static int    _mcastPort    = Consts.DEFAULT_MCAST_PORT;

  static {
    try {
      if (System.getProperty(org.sapia.ubik.rmi.Consts.MCAST_PORT_KEY) != null) {
        _mcastPort = Integer.parseInt(System.getProperty(
              org.sapia.ubik.rmi.Consts.MCAST_PORT_KEY));
      }
    } catch (NumberFormatException e) {
      // noop: using default
    }

    if (System.getProperty(org.sapia.ubik.rmi.Consts.MCAST_ADDR_KEY) != null) {
      _mcastAddress = System.getProperty(org.sapia.ubik.rmi.Consts.MCAST_ADDR_KEY);
    }
  }

  /**
   * Returns an event channel corresponding to the given domain.
   *
   * @param domain a domain name.
   * @return an <code>EventChannel</code>.
   * @throws RemoteRuntimeException if a channel could not be returned/created.
   */
  static synchronized EventChannel getEventChannelFor(String domain)
    throws RemoteRuntimeException {
    return getEventChannelFor(domain, _mcastAddress, _mcastPort);
  }

  /**
   * Returns an event channel corresponding to the given domain.
   *
   * @param domain a domain name.
   * @return an <code>EventChannel</code>.
   * @throws RemoteRuntimeException if a channel could not be returned/created.
   */
  static synchronized EventChannel getEventChannelFor(String domain,
    String mcastAddress, int mcastPort) throws RemoteRuntimeException {
    EventChannel channel;

    String       key = domain + ":" + mcastAddress + ":" + mcastPort;

    if ((channel = _channels.get(key)) == null) {
      try {
        channel = new EventChannel(domain, mcastAddress, mcastPort);
        channel.start();
      } catch (IOException e) {
        throw new RemoteRuntimeException(
          "Could not create event channel for domain: " + domain, e);
      }

      _channels.put(key, channel);
    }

    return channel;
  }

  /**
   * Shuts down all event channels that this instance keeps.
   */
  static synchronized void shutdown() {
    for (EventChannel channel : _channels.values()) {
      channel.close();
    }
    _channels.clear();
  }
}
