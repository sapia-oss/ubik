package org.sapia.ubik.rmi.naming.remote;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.naming.spi.InitialContextFactory;

import org.sapia.ubik.log.Category;
import org.sapia.ubik.log.Log;
import org.sapia.ubik.mcast.AsyncEventListener;
import org.sapia.ubik.mcast.Defaults;
import org.sapia.ubik.mcast.DomainName;
import org.sapia.ubik.mcast.EventChannel;
import org.sapia.ubik.mcast.EventChannelRef;
import org.sapia.ubik.mcast.RemoteEvent;
import org.sapia.ubik.net.TCPAddress;
import org.sapia.ubik.net.Uri;
import org.sapia.ubik.net.UriSyntaxException;
import org.sapia.ubik.rmi.Consts;
import org.sapia.ubik.rmi.naming.ServiceLocator;
import org.sapia.ubik.rmi.naming.remote.discovery.ServiceDiscoListener;
import org.sapia.ubik.rmi.naming.remote.proxy.ContextResolver;
import org.sapia.ubik.rmi.naming.remote.proxy.DefaultContextResolver;
import org.sapia.ubik.rmi.naming.remote.proxy.ReliableLocalContext;
import org.sapia.ubik.util.Condition;
import org.sapia.ubik.util.Pause;
import org.sapia.ubik.util.Conf;

/**
 * Implements a factory that allows to register {@link ServiceDiscoListener}s
 * that are notified when a service is bound to the JNDI servers.
 *
 * <p>
 * Usage:
 * <p>
 *
 * <pre>
 * Properties props = new Properties();
 *
 * // IMPORTANT: the following line of code sets the domain name
 * // to which this client (or rather, to which the JNDI context
 * // thereafter acquired) belongs. Service discovery notifications
 * // will only be received from the JNDI servers that belong to the
 * // same domain as the thus acquired JNDI context.
 *
 * props.setProperty(&quot;ubik.jndi.domain&quot;, &quot;someDomain&quot;);
 *
 * // If the above is not specified, the domain will be 'default'.
 *
 * props.setProperty(InitialContext.PROVIDER_URL, &quot;ubik://localhost:1098/&quot;);
 *
 * // IMPORTANT: do not set the wrong factory, otherwise the code
 * // further below will break...
 *
 * props.setProperty(InitialContext.INITIAL_CONTEXT_FACTORY, RemoteInitialContextFactory.class.getName());
 *
 * InitialContext ctx = new InitialContext(props);
 *
 * MyServiceDiscoveryListener listener = new MyServiceDiscoveryListener();
 *
 * ReliableLocalContext rctx = ReliableLocalContext.currentContext();
 * rctx.addServiceDiscoListener(listener);
 *
 * // IMPORTANT: from then on, the listener will be notified if a new service
 * // is bound to the domain, for AS LONG AS THE close() METHOD IS NOT CALLED
 * // ON THE CONTEXT!!!
 *
 * // ... So do the following only when the acquired initial context must
 * // be disposed of - the added listeners will not receive notifications
 * // anymore:
 * ctx.close();
 * </pre>
 *
 * An instance of this class instantiates an {@link EventChannel} to receive
 * notifications from the Ubik JNDI servers on the network, through UDP
 * multicast. The multicast address and port can be specified through the
 * following properties:
 *
 * <ul>
 * <li>ubik.rmi.naming.mcast.address
 * <li>ubik.rmi.naming.mcast.port
 * </ul>
 *
 * These properties must be specified (and passed to an instance of this class)
 * as demonstrated below:
 *
 * <pre>
 * Properties props = new Properties();
 *
 * props.setProperty(&quot;ubik.rmi.naming.mcast.address&quot;, &quot;231.173.5.7&quot;);
 *
 * props.setProperty(&quot;ubik.rmi.naming.mcast.port&quot;, &quot;6565&quot;);
 *
 * props.setProperty(InitialContext.PROVIDER_URL, &quot;ubik://localhost:1098/&quot;);
 *
 * props.setProperty(InitialContext.INITIAL_CONTEXT_FACTORY, ReliableInitialContextFactory.class.getName());
 *
 * InitialContext ctx = new InitialContext(props);
 * </pre>
 *
 * If not specified, the following are used for multicast address and port,
 * respectively:
 *
 * <ul>
 * <li>231.173.5.7
 * <li>5454
 * </ul>
 *
 *
 * @see org.sapia.ubik.rmi.naming.remote.proxy.ReliableLocalContext
 *
 * @author Yanick Duchesne
 */

@SuppressWarnings(value = "unchecked")
public class RemoteInitialContextFactory implements InitialContextFactory, JNDIConsts {
  private String scheme = ServiceLocator.UBIK_SCHEME;
  private Category log = Log.createCategory(getClass());

  public RemoteInitialContextFactory() {
  }

  public RemoteInitialContextFactory(String serviceLocatorScheme) {
    scheme = serviceLocatorScheme;
  }

  /**
   * @see javax.naming.spi.InitialContextFactory#getInitialContext(Hashtable)
   */
  @Override
  @SuppressWarnings("rawtypes")
  public Context getInitialContext(Hashtable props) throws NamingException {

    Conf allProps = new Conf().addMap(props).addSystemProperties();

    String url = (String) props.get(InitialContext.PROVIDER_URL);

    if (url == null) {
      throw new NamingException(InitialContext.PROVIDER_URL + " property not specified");
    }

    Uri uri = null;
    try {
      uri = Uri.parse(url);
    } catch (UriSyntaxException e) {
      NamingException ne = new NamingException("Invalid URL:" + url);
      ne.setRootCause(e);
      throw ne;
    }

    uri.setScheme(scheme);

    RemoteContext ctx = null;
    ContextResolver resolver = doGetResolver();
    EventChannelRef channel = getEventChannel(allProps);
    try {
      ctx = resolver.resolve(uri.getHost(), uri.getPort());
    } catch (RemoteException e) {

      log.warning("Could not find JNDI server for  %s; trying to discover ", uri.toString());

      BlockingEventListener listener = new BlockingEventListener();
      channel.get().registerAsyncListener(JNDI_SERVER_DISCO, listener);
      TCPAddress addr = null;
      try {
        channel.get().dispatch(JNDI_CLIENT_PUBLISH, "");

        long discoTimeout = allProps.getLongProperty(Consts.JNDI_CLIENT_DISCO_TIMEOUT, Defaults.DEFAULT_JNDI_CLIENT_DISCO_TIMEOUT);
        RemoteEvent evt = listener.waitForEvent(discoTimeout);
        channel.get().unregisterAsyncListener(listener);

        if (evt == null) {
          NamingException ne = new NamingException("Could not connect to JNDI server");
          ne.setRootCause(e);
          throw ne;
        }

        addr = (TCPAddress) evt.getData();

        log.warning("Discovered JNDI server at : %s", addr);
        ctx = resolver.resolve(addr);
        return new ReliableLocalContext(channel, uri.toString(), ctx, false, resolver);
      } catch (Exception e2) {
        NamingException ne = new NamingException("Could not connect to remote JNDI server: " + addr);
        ne.setRootCause(e2);
        channel.close();
        throw ne;
      }
    }
    try {
      return new ReliableLocalContext(channel, uri.toString(), ctx, true, resolver);
    } catch (IOException e) {
       NamingException ne = new NamingException("Could not instantiate local context");
      ne.setRootCause(e);
      throw ne;
    }

  }

  private EventChannelRef getEventChannel(Conf props) throws NamingException {
    final String domain = props.getProperty(UBIK_DOMAIN_NAME, JNDIConsts.DEFAULT_DOMAIN);

    EventChannelRef ref = EventChannel.selectActiveChannel(new Condition<EventChannel>() {
      DomainName dn = DomainName.parse(domain);
      @Override
      public boolean apply(EventChannel item) {
        return item.getDomainName().equals(dn);
      }
    });

    if (ref == null) {
      log.debug("Did not find active event channel for domain: %s. Creating new one", domain);
      try {
        EventChannel ec = new EventChannel(domain, props);
        ec.start();
        ref = ec.getReference();
      } catch (IOException e) {
        NamingException ne = new NamingException("Could not start event channel");
        ne.setRootCause(e);
        throw ne;
      }
    } else {
      log.debug("Reusing existing event channel");
    }
    return ref;
  }

  protected ContextResolver doGetResolver() {
    return new DefaultContextResolver();
  }

  // ==========================================================================

  static final class BlockingEventListener implements AsyncEventListener {
    private RemoteEvent event;

    BlockingEventListener() {
    }

    @Override
    public synchronized void onAsyncEvent(RemoteEvent event) {
      this.event = event;
      notify();
    }

    public synchronized RemoteEvent waitForEvent(long timeout) throws InterruptedException {
      Pause delay = new Pause(timeout);
      while (!delay.isOver() && event == null) {
          wait(delay.remainingNotZero());
      }
      return event;
    }
  }
}
