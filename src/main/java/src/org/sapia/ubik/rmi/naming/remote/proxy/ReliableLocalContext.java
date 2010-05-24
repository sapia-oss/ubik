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

import org.sapia.ubik.mcast.AsyncEventListener;
import org.sapia.ubik.mcast.EventChannel;
import org.sapia.ubik.mcast.RemoteEvent;
import org.sapia.ubik.net.TCPAddress;
import org.sapia.ubik.rmi.naming.remote.Consts;
import org.sapia.ubik.rmi.naming.remote.RemoteContext;
import org.sapia.ubik.rmi.naming.remote.discovery.DiscoveryHelper;
import org.sapia.ubik.rmi.naming.remote.discovery.JndiDiscoListener;
import org.sapia.ubik.rmi.naming.remote.discovery.ServiceDiscoListener;
import org.sapia.ubik.rmi.server.Log;


/**
 * An instance of this class is created by a <code>ReliableInitialContextFactory</code>. It allows
 * clients to register <code>ServiceDiscoveryListener</code>s that are notified when new services
 * are bound to the JNDI servers on the network.
 *
 * @see org.sapia.ubik.rmi.naming.remote.RemoteInitialContextFactory
 * @see org.sapia.ubik.rmi.naming.remote.discovery.ServiceDiscoListener
 * @see org.sapia.ubik.rmi.naming.remote.discovery.ServiceDiscoveryEvent
 *
 * @author Yanick Duchesne
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class ReliableLocalContext extends LocalContext
  implements AsyncEventListener {
  private static ThreadLocal _currentContext = new ThreadLocal();
  private static String      PING          = "ubik/rmi/naming/ping/test";
  private BindingCache       _bindings     = new BindingCache();
  private DiscoveryHelper    _helper;
  private List               _servers      = Collections.synchronizedList(new ArrayList());
  private ContextResolver    _resolver;

  /**
   * Constructor for ReliableLocalContext.
   */
  public ReliableLocalContext(EventChannel channel, 
                              String url,
                              RemoteContext ctx,
                              boolean publish,
                              ContextResolver resolver) throws NamingException, IOException {
    super(url, ctx);
    _helper = new DiscoveryHelper(channel);
    _resolver = resolver;
    channel.registerAsyncListener(Consts.JNDI_SERVER_DISCO, this);
    channel.registerAsyncListener(Consts.JNDI_SERVER_PUBLISH, this);

    if (publish) {
      if (!channel.isClosed()) {
        channel.dispatch(Consts.JNDI_CLIENT_PUBLISH, "");
      }
    }
    
    _currentContext.set(this);
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

    if (!_helper.getChannel().isClosed()) {
      _bindings.add(_helper.getChannel().getDomainName().toString(),
        n, o);
    }
  }

  /**
   * @see org.sapia.ubik.rmi.naming.remote.proxy.LocalContext#rebind(String, Object)
   */
  public void rebind(String n, Object o) throws NamingException {
    super.rebind(n, o);

    if (!_helper.getChannel().isClosed()) {
      _bindings.add(_helper.getChannel().getDomainName().toString(), super.getNameParser().parse(n), o);
    }
  }

  /**
   * Adds a service discovery listener to this instance.
   *
   * @param a <code>ServiceDiscoListener</code>.
   */
  public void addServiceDiscoListener(ServiceDiscoListener listener) {
    if (!_helper.getChannel().isClosed()) {
      _helper.addServiceDiscoListener(listener);
    }
  }

  /**
   * Adds a JNDI discovery listener to this instance.
   *
   * @param a <code>JndiDiscoListener</code>.
   */
  public void addJndiDiscoListener(JndiDiscoListener listener) {
    if (!_helper.getChannel().isClosed()) {
      _helper.addJndiDiscoListener(new JndiListenerWrapper(listener));
    }
  }

  /**
   * @see org.sapia.ubik.mcast.AsyncEventListener#onAsyncEvent(RemoteEvent)
   */
  public void onAsyncEvent(RemoteEvent evt) {
    TCPAddress tcp;

    try {
      if (evt.getType().equals(Consts.JNDI_SERVER_DISCO)) {
        tcp = (TCPAddress) evt.getData();

        Context remoteCtx = (Context) _resolver.resolve(tcp);
        _servers.add(remoteCtx);
        _bindings.copyTo(remoteCtx, _domainName, super._url,
            _helper.getChannel().getMulticastHost(),
            _helper.getChannel().getMulticastPort());

      } else if (evt.getType().equals(Consts.JNDI_SERVER_PUBLISH) &&
            (getInternalContext() != null)) {
        Log.info(getClass(), "Discovered naming service; binding cached stubs...");
        tcp = (TCPAddress) evt.getData();

        Context remoteCtx = (Context) _resolver.resolve(tcp);
        _servers.add(remoteCtx);
        _bindings.copyTo(remoteCtx, _domainName, super._url,
        _helper.getChannel().getMulticastHost(),
        _helper.getChannel().getMulticastPort());
      }
    } catch (RemoteException e) {
      Log.error(getClass(), "Could not connect to naming service", e);
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

    synchronized (_servers) {
      for (int i = 0; i < _servers.size(); i++) {
        server = (RemoteContext) _servers.get(i);

        try {
          server.lookup(PING);
          _ctx = server;

          return;
        } catch (UndeclaredThrowableException udte) {
          if (udte.getUndeclaredThrowable() instanceof RemoteException) {
            _servers.remove(i);
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
    _helper.close();
  }

  /**
   * Returns the instance of this class that is currently registered with the calling thread.
   *
   * @throws IllegalStateException if no instance of this class is currently registered.
   */
  public static ReliableLocalContext currentContext()
    throws IllegalStateException {
    ReliableLocalContext ctx = (ReliableLocalContext) _currentContext.get();

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
    return _helper.getChannel();
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
      synchronized (_servers) {
        _servers.add(ctx);
      }

      _wrapped.onJndiDiscovered(ctx);
    }
  }
}
