package org.sapia.ubik.ioc.spring;

import java.io.IOException;
import java.lang.ref.SoftReference;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;

import org.sapia.ubik.ioc.NamingService;
import org.sapia.ubik.log.Log;
import org.sapia.ubik.mcast.EventChannel;
import org.sapia.ubik.rmi.Consts;
import org.sapia.ubik.rmi.naming.remote.RemoteInitialContextFactory;
import org.sapia.ubik.rmi.naming.remote.discovery.DiscoveryHelper;
import org.sapia.ubik.rmi.naming.remote.discovery.JndiDiscoListener;
import org.sapia.ubik.rmi.naming.remote.discovery.ServiceDiscoListener;
import org.sapia.ubik.rmi.naming.remote.proxy.ReliableLocalContext;
import org.sapia.ubik.util.Localhost;
import org.sapia.ubik.util.Props;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

/**
 * This singleton bean acts as a bridge to the Ubik JNDI server. It is
 * internally used by the {@link BeanExporterPostProcessor} and
 * {@link BeanImporterPostProcessor} to respectively bind and lookup remote
 * services.
 * <p>
 * This bean robustly discovers Ubik JNDI servers that appear in the domain if
 * none are present at initialization time. The bound services are thus cached
 * until a JNDI server is available.
 * 
 * @author yduchesne
 * 
 */
public class NamingServiceBean implements NamingService, JndiDiscoListener, InitializingBean, DisposableBean {

  private String domain;
  private String host, addr;
  private int port, mcastPort;
  private String avisUrl;
  private Context ctx;
  private Set<Object> toBind = Collections.synchronizedSet(new HashSet<Object>());
  private DiscoveryHelper helper;

  /**
   * @param host
   *          the host of the remote Ubik JNDI server.
   */
  public NamingServiceBean setJndiHost(String host) {
    this.host = host;
    return this;
  }

  /**
   * @param port
   *          the port of the remote Ubik JNDI server.
   */
  public NamingServiceBean setJndiPort(int port) {
    this.port = port;
    return this;
  }

  /**
   * @param domain
   *          the domain of the remote Ubik JNDI server.
   */
  public void setDomain(String domain) {
    this.domain = domain;
  }

  /**
   * @param addr
   *          the multicast address to use for discovering remote JNDI servers.
   */
  public void setMulticastAddress(String addr) {
    this.addr = addr;
  }

  /**
   * @param port
   *          the multicast port to use for discovering remote JNDI servers.
   */
  public void setMulticastPort(int port) {
    this.mcastPort = port;
  }

  /**
   * @param avisUrl
   *          the Avis URL, if Avis should be used as discovery mechanism.
   */
  public void seAvisUrl(String avisUrl) {
    this.avisUrl = avisUrl;
  }

  /**
   * @return this instance's {@link EventChannel}.
   */
  public EventChannel getEventChannel() {
    return helper.getChannel();
  }

  public synchronized void bind(String name, Object o) throws NamingException {
    if (ctx == null) {
      toBind.add(new Binding(name, o));
      return;
    }
    if (Log.isInfo()) {
      Log.info(getClass(), "Binding: " + name + ", " + o + " - to context: " + ctx);
    }
    ctx.bind(name, o);
  }

  public synchronized Object lookup(String name) throws NamingException, NameNotFoundException {
    if (ctx == null) {
      throw new NamingException("No connection to JNDI");
    }
    return ctx.lookup(name);
  }

  public void register(ServiceDiscoListener listener) {
    if (helper != null) {
      helper.addServiceDiscoListener(listener);
    }
  }

  public void unregister(ServiceDiscoListener listener) {
    if (helper != null) {
      helper.removeServiceDiscoListener(listener);
    }
  }

  @Override
  public void destroy() throws Exception {
    try {
      if (ctx != null)
        ctx.close();
      if (helper != null)
        helper.close();
    } catch (NamingException e) {
      // noop
    }
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    if (port == 0) {
      port = 1099;
    }
    if (host == null) {
      host = Localhost.getAnyLocalAddress().getHostAddress();
    }

    Properties props = createProperties();
    try {
      ctx = new InitialContext(props);
      Log.info(getClass(), "Got JNDI initial context");
      EventChannel channel = ReliableLocalContext.currentContext().getEventChannel();
      if (channel != null) {
        helper = new DiscoveryHelper(createEventChannel(props));
        Log.info(getClass(), "Got discovery helper");
      }
    } catch (NamingException e) {
      Log.info(getClass(), "Could not get JNDI initial context for (domain:host:port):" + domain + ":" + host + ":" + port);
      helper = new DiscoveryHelper(createEventChannel(props));
      helper.addJndiDiscoListener(this);
    }
  }

  public void onJndiDiscovered(Context context) {
    Log.info(getClass(), "Discovered JNDI");
    ctx = context;
    helper.removeJndiDiscoListener(this);
    synchronized (this) {
      Iterator<Object> entries = toBind.iterator();
      while (entries.hasNext()) {
        Binding entry = (Binding) entries.next();
        if (entry.getObject() != null) {
          try {
            ctx.bind(entry.getName(), entry.getObject());
            entries.remove();
          } catch (NamingException e) {
          }
        }
      }
    }
  }

  private EventChannel createEventChannel(Properties props) throws IOException {
    EventChannel channel = new EventChannel(domain, new Props().addProperties(props));
    channel.start();
    return channel;
  }

  private Properties createProperties() {
    Properties props = new Properties();
    props.setProperty(Context.INITIAL_CONTEXT_FACTORY, RemoteInitialContextFactory.class.getName());
    props.setProperty(RemoteInitialContextFactory.UBIK_DOMAIN_NAME, domain);
    props.setProperty(Context.PROVIDER_URL, "ubik://" + host + ":" + port);
    if (avisUrl != null) {
      props.setProperty(Consts.BROADCAST_PROVIDER, Consts.BROADCAST_PROVIDER_AVIS);
      props.setProperty(Consts.BROADCAST_AVIS_URL, avisUrl);
    } else {
      props.setProperty(Consts.MCAST_ADDR_KEY, addr == null ? Consts.DEFAULT_MCAST_ADDR : addr);
      props.setProperty(Consts.MCAST_PORT_KEY, mcastPort == 0 ? Integer.toString(Consts.DEFAULT_MCAST_PORT) : Integer.toString(mcastPort));
    }
    return props;
  }

  static class Binding {
    SoftReference<Object> _object;
    String _name;

    Binding(String name, Object o) {
      _name = name;
      _object = new SoftReference<Object>(o);
    }

    Object getObject() {
      return _object.get();
    }

    String getName() {
      return _name;
    }
  }

}
