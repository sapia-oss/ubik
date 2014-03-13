package org.sapia.ubik.rmi.server;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.sapia.ubik.log.Category;
import org.sapia.ubik.log.Log;
import org.sapia.ubik.mcast.DomainName;
import org.sapia.ubik.mcast.EventChannel;
import org.sapia.ubik.mcast.EventChannelRef;
import org.sapia.ubik.mcast.MulticastAddress;
import org.sapia.ubik.module.Module;
import org.sapia.ubik.module.ModuleContext;
import org.sapia.ubik.rmi.RemoteRuntimeException;
import org.sapia.ubik.util.Condition;
import org.sapia.ubik.util.Props;

/**
 * Keeps event channels on a per-domain basis.
 *
 * @author Yanick Duchesne
 *
 */
public class EventChannelTable implements Module, EventChannelTableMBean {

  private Category log = Log.createCategory(getClass());

  private Map<ChannelKey, EventChannelRef> channels = new ConcurrentHashMap<ChannelKey, EventChannelRef>();
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
    for (EventChannelRef r : channels.values()) {
      r.close();
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
  public EventChannel getEventChannelFor(final String domain, final MulticastAddress address) throws RemoteRuntimeException {
    EventChannelRef ref;
    ChannelKey key = new ChannelKey(domain, address);

    if ((ref = channels.get(key)) == null) {
      synchronized (lock) {
        if ((ref = channels.get(key)) == null) {
          // checking in the static active channels first: we want to avoid creating
          // a new EventChannel if one already exists in same JVM.
          ref = EventChannel.selectActiveChannel(new Condition<EventChannel>() {
            DomainName n = DomainName.parse(domain);
            @Override
            public boolean apply(EventChannel item) {
              return item.getDomainName().equals(n) && item.getMulticastAddress().equals(address);
            }
          });

          if (ref != null) {
            log.debug("EventChannel already active for domain %s. Returning it", domain);
            channels.put(key, ref);
          } else {

            try {
              log.debug("Creating EventChannel for domain %s", domain);
              Props props = new Props().addMap(address.toParameters()).addProperties(System.getProperties());
              ref = new EventChannel(domain, props).getReference();
              ref.get().start();
              channels.put(key, ref);
            } catch (IOException e) {
              throw new RemoteRuntimeException("Could not create event channel for domain: " + domain, e);
            }
          }
        }
      }
    }

    return ref.get();
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
