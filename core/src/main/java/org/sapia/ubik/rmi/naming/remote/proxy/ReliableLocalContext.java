package org.sapia.ubik.rmi.naming.remote.proxy;

import java.io.IOException;
import java.lang.reflect.UndeclaredThrowableException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NamingException;

import org.sapia.ubik.log.Category;
import org.sapia.ubik.log.Log;
import org.sapia.ubik.mcast.EventChannel;
import org.sapia.ubik.mcast.EventChannelRef;
import org.sapia.ubik.rmi.naming.remote.JNDIConsts;
import org.sapia.ubik.rmi.naming.remote.RemoteContext;
import org.sapia.ubik.rmi.naming.remote.RemoteInitialContextFactory;
import org.sapia.ubik.rmi.naming.remote.discovery.DiscoveryHelper;
import org.sapia.ubik.rmi.naming.remote.discovery.JndiDiscoListener;
import org.sapia.ubik.rmi.naming.remote.discovery.ServiceDiscoListener;
import org.sapia.ubik.rmi.naming.remote.discovery.ServiceDiscoveryEvent;

/**
 * An instance of this class is created by a {@link RemoteInitialContextFactory}
 * . It allows clients to register ServiceDiscoveryListener that are notified
 * when new services are bound to the JNDI servers on the network.
 *
 * @see RemoteInitialContextFactory
 * @see ServiceDiscoListener
 * @see ServiceDiscoveryEvent
 *
 * @author Yanick Duchesne
 */
@SuppressWarnings(value = "unchecked")
public class ReliableLocalContext extends LocalContext implements JndiDiscoListener {

  private static ThreadLocal<Context> currentContext = new ThreadLocal<Context>();
  private static String PING = "ubik/rmi/naming/ping/test";
  private Category log = Log.createCategory(getClass());
  private BindingCache bindings = new BindingCache();
  private DiscoveryHelper helper;
  private List<Context> servers = Collections.synchronizedList(new ArrayList<Context>());

  /**
   * Constructor for ReliableLocalContext.
   */
  public ReliableLocalContext(EventChannelRef channel, String url, RemoteContext ctx, boolean publish, ContextResolver resolver) throws NamingException,
      IOException {
    super(url, ctx);
    helper = new DiscoveryHelper(channel);
    helper.addJndiDiscoListener(this);
    helper.setContextResolver(resolver);

    if (publish) {
      if (!channel.get().isClosed()) {
        channel.get().dispatch(JNDIConsts.JNDI_CLIENT_PUBLISH, "");
      }
    }

    currentContext.set(this);
  }

  @Override
  public void bind(Name n, Object o) throws NamingException {
    rebind(n, o);
  }

  @Override
  public void bind(String n, Object o) throws NamingException {
    rebind(n, o);
  }

  @Override
  public void rebind(Name n, Object o) throws NamingException {
    super.rebind(n, o);
    if (!helper.getChannel().get().isClosed()) {
      bindings.add(helper.getChannel().get().getDomainName().toString(), n, o);
    }
  }

  @Override
  public void rebind(String n, Object o) throws NamingException {
    super.rebind(n, o);

    if (!helper.getChannel().get().isClosed()) {
      bindings.add(helper.getChannel().get().getDomainName().toString(), super.getNameParser().parse(n), o);
    }
  }

  public void addServiceDiscoListener(ServiceDiscoListener listener) {
    if (!helper.getChannel().get().isClosed()) {
      helper.addServiceDiscoListener(listener);
    }
  }

  public void addJndiDiscoListener(JndiDiscoListener listener) {
    if (!helper.getChannel().get().isClosed()) {
      helper.addJndiDiscoListener(new JndiListenerWrapper(listener));
    }
  }

  @Override
  public void onJndiDiscovered(Context ctx) {
    log.info("Discovered naming service; binding cached stubs...");
    servers.add(ctx);
    bindings.copyTo(ctx, domainInfo.getDomainName(), super.url, helper.getChannel().get().getMulticastAddress());
  }

  /*
  @Override
  public void onAsyncEvent(RemoteEvent evt) {
    try {
      ServerAddress serverAddress;
      if (evt.getType().equals(JNDIConsts.JNDI_SERVER_DISCO)) {
        serverAddress = (TCPAddress) evt.getData();
        Context remoteCtx = resolver.resolve(serverAddress);
        servers.add(remoteCtx);
        bindings.copyTo(remoteCtx, domainInfo.getDomainName(), super.url, helper.getChannel().get().getMulticastAddress());

      } else if (evt.getType().equals(JNDIConsts.JNDI_SERVER_PUBLISH) && (getInternalContext() != null)) {
      }
    } catch (RemoteException e) {
      log.warning("Could not connect to naming service; JNDI server probably down", e);
    } catch (IOException e) {
      log.error("IO problem trying to bind services", e);
    }
  }*/

  @Override
  protected void doFailOver(UndeclaredThrowableException e) throws NamingException {
    if (!(e.getUndeclaredThrowable() instanceof RemoteException)) {
      super.doFailOver(e);
    }

    RemoteContext server;

    synchronized (servers) {
      for (int i = 0; i < servers.size(); i++) {
        server = (RemoteContext) servers.get(i);

        try {
          server.lookup(PING);
          _ctx = server;

          return;
        } catch (UndeclaredThrowableException udte) {
          if (udte.getUndeclaredThrowable() instanceof RemoteException) {
            servers.remove(i);
          }
        } catch (NamingException ne) {
          _ctx = server;

          return;
        }
      }
    }

    super.doFailOver(e);
  }

  @Override
  public void close() throws NamingException {
    super.close();
    helper.close();
  }

  /**
   * Returns the instance of this class that is currently registered with the
   * calling thread.
   *
   * @throws IllegalStateException
   *           if no instance of this class is currently registered.
   */
  public static ReliableLocalContext currentContext() throws IllegalStateException {
    ReliableLocalContext ctx = (ReliableLocalContext) currentContext.get();

    if (ctx == null) {
      throw new IllegalStateException("No " + ReliableLocalContext.class.getName() + " registered with current thread");
    }

    return ctx;
  }

  /**
   * @return the {@link EventChannelRef} pointing to the {@link EventChannel}
   * used by this instance to perform discovery.
   */
  public EventChannelRef getEventChannel() {
    return helper.getChannel();
  }

  // ==========================================================================
  // Inner classes
  class JndiListenerWrapper implements JndiDiscoListener {
    private JndiDiscoListener wrapped;

    JndiListenerWrapper(JndiDiscoListener toWrap) {
      wrapped = toWrap;
    }

    @Override
    public void onJndiDiscovered(Context ctx) {
      synchronized (servers) {
        servers.add(ctx);
      }

      wrapped.onJndiDiscovered(ctx);
    }
  }
}
