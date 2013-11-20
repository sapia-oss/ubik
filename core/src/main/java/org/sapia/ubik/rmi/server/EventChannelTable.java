package org.sapia.ubik.rmi.server;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.sapia.ubik.mcast.EventChannel;
import org.sapia.ubik.mcast.MulticastAddress;
import org.sapia.ubik.module.Module;
import org.sapia.ubik.module.ModuleContext;
import org.sapia.ubik.rmi.RemoteRuntimeException;
import org.sapia.ubik.util.Props;

/**
 * Keeps event channels on a per-domain basis.
 * 
 * @author Yanick Duchesne
 * 
 */
public class EventChannelTable implements Module, EventChannelTableMBean {

  private Map<ChannelKey, EventChannel> channels = new ConcurrentHashMap<ChannelKey, EventChannel>();
  private Object lock = new Object();

  @Override
  public void init(ModuleContext context) {
    context.registerMbean(this);
  }

  @Override
  public void start(ModuleContext context) {

  }

  @Override
  public void stop() {
    for (EventChannel channel : channels.values()) {
      channel.close();
    }
    channels.clear();
  }

  /**
   * @see EventChannelTableMBean#getEventChannelCount().
   */
  @Override
  public int getEventChannelCount() {
    return channels.size();
  }

  /**
   * Returns an event channel corresponding to the given domain.
   * 
   * @param domain
   *          a domain name.
   * @param address
   *          a {@link MulticastAddress}.
   * @return an <code>EventChannel</code>.
   * @throws RemoteRuntimeException
   *           if a channel could not be returned/created.
   */
  public EventChannel getEventChannelFor(String domain, MulticastAddress address) throws RemoteRuntimeException {
    EventChannel channel;

    ChannelKey key = new ChannelKey(domain, address);

    if ((channel = channels.get(key)) == null) {
      synchronized (lock) {
        if ((channel = channels.get(key)) == null) {
          try {
            Props props = new Props().addMap(address.toParameters()).addProperties(System.getProperties());
            channel = new EventChannel(domain, props);
            channel.start();
          } catch (IOException e) {
            throw new RemoteRuntimeException("Could not create event channel for domain: " + domain, e);
          }

          channels.put(key, channel);
        }
      }
    }

    return channel;
  }

  // --------------------------------------------------------------------------

  static class ChannelKey {
    private String domain;
    private MulticastAddress address;

    ChannelKey(String domain, MulticastAddress address) {
      this.domain = domain;
      this.address = address;
    }

    @Override
    public int hashCode() {
      return domain.hashCode() * 31 ^ address.hashCode() * 31;
    }

    @Override
    public boolean equals(Object obj) {
      if (obj instanceof ChannelKey) {
        ChannelKey other = (ChannelKey) obj;
        return other.domain.equals(domain) && other.address.equals(address);
      }
      return false;
    }

  }
}
