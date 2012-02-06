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

import org.sapia.ubik.log.Log;
import org.sapia.ubik.mcast.AsyncEventListener;
import org.sapia.ubik.mcast.EventChannel;
import org.sapia.ubik.mcast.RemoteEvent;
import org.sapia.ubik.net.ServerAddress;
import org.sapia.ubik.net.TCPAddress;
import org.sapia.ubik.rmi.naming.remote.JndiConsts;
import org.sapia.ubik.rmi.naming.remote.RemoteContext;
import org.sapia.ubik.rmi.naming.remote.RemoteInitialContextFactory;
import org.sapia.ubik.rmi.naming.remote.discovery.DiscoveryHelper;
import org.sapia.ubik.rmi.naming.remote.discovery.JndiDiscoListener;
import org.sapia.ubik.rmi.naming.remote.discovery.ServiceDiscoListener;
import org.sapia.ubik.rmi.naming.remote.discovery.ServiceDiscoveryEvent;


/**
 * An instance of this class is created by a {@link RemoteInitialContextFactory}. It allows
 * clients to register ServiceDiscoveryListener that are notified when new services
 * are bound to the JNDI servers on the network.
 *
 * @see RemoteInitialContextFactory
 * @see ServiceDiscoListener
 * @see ServiceDiscoveryEvent
 *
 * @author Yanick Duchesne
 */
@SuppressWarnings(value="unchecked")
public class ReliableLocalContext extends LocalContext
  implements AsyncEventListener {
  private static ThreadLocal currentContext = new ThreadLocal();
  private static String      PING           = "ubik/rmi/naming/ping/test";
  private BindingCache       bindings       = new BindingCache();
  private DiscoveryHelper    helper;
  private List               servers        = Collections.synchronizedList(new ArrayList());
  private ContextResolver    resolver;

  /**
   * Constructor for ReliableLocalContext.
   */
  public ReliableLocalContext(EventChannel channel, 
                              String url,
                              RemoteContext ctx,
                              boolean publish,
                              ContextResolver resolver) throws NamingException, IOException {
    super(url, ctx);
    helper        = new DiscoveryHelper(channel);
    this.resolver = resolver;
    channel.registerAsyncListener(JndiConsts.JNDI_SERVER_DISCO, this);
    channel.registerAsyncListener(JndiConsts.JNDI_SERVER_PUBLISH, this);

    if (publish) {
      if (!channel.isClosed()) {
        channel.dispatch(JndiConsts.JNDI_CLIENT_PUBLISH, "");
      }
    }
    
    currentContext.set(this);
  }

  /**
   * @see org.sapia.ubik.rmi.naming.remote.proxy.LocalContext#bind(Name, Object)
   */
  public void bind(Name n, Object o) throws NamingException {
    rebind(n, o);
  }

  /**
   * @see org.sapia.ubik.rmi.naming.remote.proxy.LocalContext#bind(String, Object)
   */
  public void bind(String n, Object o) throws NamingException {
    rebind(n, o);
  }

  /**
   * @see org.sapia.ubik.rmi.naming.remote.proxy.LocalContext#rebind(Name, Object)
   */
  public void rebind(Name n, Object o) throws NamingException {
    super.rebind(n, o);

    if (!helper.getChannel().isClosed()) {
      bindings.add(helper.getChannel().getDomainName().toString(), n, o);
    }
  }

  /**
   * @see org.sapia.ubik.rmi.naming.remote.proxy.LocalContext#rebind(String, Object)
   */
  public void rebind(String n, Object o) throws NamingException {
    super.rebind(n, o);

    if (!helper.getChannel().isClosed()) {
      bindings.add(helper.getChannel().getDomainName().toString(), super.getNameParser().parse(n), o);
    }
  }

  /**
   * Adds a service discovery listener to this instance.
   *
   * @param listener a {@link ServiceDiscoListener}.
   */
  public void addServiceDiscoListener(ServiceDiscoListener listener) {
    if (!helper.getChannel().isClosed()) {
      helper.addServiceDiscoListener(listener);
    }
  }

  /**
   * Adds a JNDI discovery listener to this instance.
   *
   * @param listener a {@link JndiDiscoListener}.
   */
  public void addJndiDiscoListener(JndiDiscoListener listener) {
    if (!helper.getChannel().isClosed()) {
      helper.addJndiDiscoListener(new JndiListenerWrapper(listener));
    }
  }

  /**
   * @see org.sapia.ubik.mcast.AsyncEventListener#onAsyncEvent(RemoteEvent)
   */
  public void onAsyncEvent(RemoteEvent evt) {
    try {
      ServerAddress serverAddress;      
      if (evt.getType().equals(JndiConsts.JNDI_SERVER_DISCO)) {
        serverAddress = (TCPAddress) evt.getData();
        Context remoteCtx = (Context) resolver.resolve(serverAddress);
        servers.add(remoteCtx);
        bindings.copyTo(remoteCtx, domainInfo.getDomainName(), super.url, helper.getChannel().getMulticastAddress());

      } else if (evt.getType().equals(JndiConsts.JNDI_SERVER_PUBLISH) && (getInternalContext() != null)) {
        Log.info(getClass(), "Discovered naming service; binding cached stubs...");
        serverAddress = (TCPAddress) evt.getData();

        Context remoteCtx = (Context) resolver.resolve(serverAddress);
        servers.add(remoteCtx);
        bindings.copyTo(remoteCtx, domainInfo.getDomainName(), super.url, helper.getChannel().getMulticastAddress());
      }
    } catch (RemoteException e) {
      Log.warning(getClass(), "Could not connect to naming service; JNDI server probably down");
    } catch (IOException e) {
      Log.error(getClass(), "IO problem trying to bind services", e);
    }
  }

  /**
   * @see org.sapia.ubik.rmi.naming.remote.proxy.LocalContext#doFailOver(UndeclaredThrowableException)
   */
  protected void doFailOver(UndeclaredThrowableException e)
    throws NamingException {
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

  /**
   * @see org.sapia.ubik.rmi.naming.remote.proxy.LocalContext#close()
   */
  public void close() throws NamingException {
    super.close();
    helper.close();
  }

  /**
   * Returns the instance of this class that is currently registered with the calling thread.
   *
   * @throws IllegalStateException if no instance of this class is currently registered.
   */
  public static ReliableLocalContext currentContext()
    throws IllegalStateException {
    ReliableLocalContext ctx = (ReliableLocalContext) currentContext.get();

    if (ctx == null) {
      throw new IllegalStateException("No " + ReliableLocalContext.class.getName() + " registered with current thread");
    }

    return ctx;
  }
  
  /**
   * @return the <code>EventChannel</code> used by this instance to perform
   * discovery.
   */
  public EventChannel getEventChannel(){
    return helper.getChannel();
  }

  /*////////////////////////////////////////////////////////////////////
                              INNER CLASSES
  ////////////////////////////////////////////////////////////////////*/
  
  class JndiListenerWrapper implements JndiDiscoListener {
    private JndiDiscoListener _wrapped;

    JndiListenerWrapper(JndiDiscoListener toWrap) {
      _wrapped   = toWrap;
    }

    /**
     * @see org.sapia.ubik.rmi.naming.remote.discovery.JndiDiscoListener#onJndiDiscovered(Context)
     */
    public void onJndiDiscovered(Context ctx) {
      synchronized (servers) {
        servers.add(ctx);
      }

      _wrapped.onJndiDiscovered(ctx);
    }
  }
}
