package org.sapia.ubik.ioc.guice;

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

import com.google.inject.Singleton;

@Singleton
public class NamingServiceImpl implements NamingService, JndiDiscoListener {

  private String _domain;
  private String _host, _addr;
  private int _port, _mcastPort;
  private String _avisUrl;
  private Context _ctx;
  private Set<Object> _toBind = Collections.synchronizedSet(new HashSet<Object>());
  private DiscoveryHelper _helper;

  public NamingServiceImpl(String domain) throws IOException {
    _domain = domain;
    init();
  }

  /**
   * @param host
   *          the host of the remote Ubik JNDI server.
   */
  public NamingServiceImpl setJndiHost(String host) {
    _host = host;
    return this;
  }

  /**
   * @param port
   *          the port of the remote Ubik JNDI server.
   */
  public NamingServiceImpl setJndiPort(int port) {
    _port = port;
    return this;
  }

  /**
   * @param domain
   *          the domain of the remote Ubik JNDI server.
   */
  public void setDomain(String domain) {
    _domain = domain;
  }

  /**
   * @param addr
   *          the multicast address to use for discovering remote JNDI servers.
   */
  public void setMulticastAddress(String addr) {
    _addr = addr;
  }

  /**
   * @param port
   *          the multicast port to use for discovering remote JNDI servers.
   */
  public void setMulticastPort(int port) {
    _mcastPort = port;
  }

  /**
   * @param avisUrl
   *          the Avis URL, if Avis should be used as discovery mechanism.
   */
  public void setAvisUrl(String avisUrl) {
    _avisUrl = avisUrl;
  }

  /**
   * @return this instance's {@link EventChannel}.
   */
  public EventChannel getEventChannel() {
    return _helper.getChannel();
  }

  public synchronized void bind(String name, Object o) throws NamingException {
    if (_ctx == null) {
      _toBind.add(new Binding(name, o));
      return;
    }
    if (Log.isInfo()) {
      Log.info(getClass(), "Binding: " + name + ", " + o + " - to context: " + _ctx);
    }
    _ctx.bind(name, o);
  }

  public synchronized Object lookup(String name) throws NamingException, NameNotFoundException {
    if (_ctx == null) {
      throw new NamingException("No connection to JNDI");
    }
    return _ctx.lookup(name);
  }

  public void register(ServiceDiscoListener listener) {
    if (_helper != null) {
      _helper.addServiceDiscoListener(listener);
    }
  }

  public void unregister(ServiceDiscoListener listener) {
    if (_helper != null) {
      _helper.removeServiceDiscoListener(listener);
    }
  }

  public void shutdown() {
    try {
      if (_ctx != null)
        _ctx.close();
      if (_helper != null)
        _helper.close();
    } catch (NamingException e) {
      // noop
    }
  }

  private void init() throws IOException {
    if (_port == 0) {
      _port = 1099;
    }
    if (_host == null) {
      _host = Localhost.getAnyLocalAddress().getHostAddress();
    }

    Properties props = createProperties();
    try {
      _ctx = new InitialContext(props);
      Log.info(getClass(), "Got JNDI initial context");
      EventChannel channel = ReliableLocalContext.currentContext().getEventChannel();
      if (channel != null) {
        _helper = new DiscoveryHelper(channel);
        Log.info(getClass(), "Got discovery helper");
      }
    } catch (NamingException e) {
      Log.info(getClass(), "Could not get JNDI initial context for (domain:host:port):" + _domain + ":" + _host + ":" + _port);
      _helper = new DiscoveryHelper(createEventChannel(createProperties()));
      _helper.addJndiDiscoListener(this);
    }
  }

  public void onJndiDiscovered(Context context) {
    Log.info(getClass(), "Discovered JNDI");
    _ctx = context;
    _helper.removeJndiDiscoListener(this);
    synchronized (this) {
      Iterator<Object> entries = _toBind.iterator();
      while (entries.hasNext()) {
        Binding entry = (Binding) entries.next();
        if (entry.getObject() != null) {
          try {
            _ctx.bind(entry.getName(), entry.getObject());
            entries.remove();
          } catch (NamingException e) {
          }
        }
      }
    }
  }

  private EventChannel createEventChannel(Properties props) throws IOException {
    EventChannel channel = new EventChannel(_domain, new Props().addProperties(props));
    channel.start();
    return channel;
  }

  private Properties createProperties() {
    Properties props = new Properties();
    props.setProperty(Context.INITIAL_CONTEXT_FACTORY, RemoteInitialContextFactory.class.getName());
    props.setProperty(RemoteInitialContextFactory.UBIK_DOMAIN_NAME, _domain);
    props.setProperty(Context.PROVIDER_URL, "ubik://" + _host + ":" + _port);
    if (_avisUrl != null) {
      props.setProperty(Consts.BROADCAST_PROVIDER, Consts.BROADCAST_PROVIDER_AVIS);
      props.setProperty(Consts.BROADCAST_AVIS_URL, _avisUrl);
    } else {
      props.setProperty(Consts.MCAST_ADDR_KEY, _addr == null ? Consts.DEFAULT_MCAST_ADDR : _addr);
      props.setProperty(Consts.MCAST_PORT_KEY, _mcastPort == 0 ? Integer.toString(Consts.DEFAULT_MCAST_PORT) : Integer.toString(_mcastPort));
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
