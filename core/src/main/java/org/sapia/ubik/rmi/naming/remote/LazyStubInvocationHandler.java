package org.sapia.ubik.rmi.naming.remote;

import java.lang.reflect.Method;
import java.rmi.RemoteException;
import java.util.Collection;
import java.util.Collections;

import javax.naming.Context;
import javax.naming.NamingException;

import org.sapia.ubik.concurrent.Spawn;
import org.sapia.ubik.concurrent.TimeIntervalBarrier;
import org.sapia.ubik.log.Category;
import org.sapia.ubik.log.Log;
import org.sapia.ubik.mcast.Defaults;
import org.sapia.ubik.rmi.Consts;
import org.sapia.ubik.rmi.naming.remote.discovery.DiscoveryHelper;
import org.sapia.ubik.rmi.naming.remote.discovery.ServiceDiscoListener;
import org.sapia.ubik.rmi.naming.remote.discovery.ServiceDiscoveryEvent;
import org.sapia.ubik.rmi.server.stub.RemoteRefContext;
import org.sapia.ubik.rmi.server.stub.StubContainer;
import org.sapia.ubik.rmi.server.stub.StubInvocationHandler;
import org.sapia.ubik.rmi.server.stub.Stubs;
import org.sapia.ubik.util.Assertions;
import org.sapia.ubik.util.Conf;
import org.sapia.ubik.util.Func;
import org.sapia.ubik.util.Strings;

/**
 * This {@link StubInvocationHandler} implementation is used on the client side, when a lookup on
 * a given remote object has failed because it not yet bound to the JNDI tree.
 * <p>
 * In such a case, a dynamic proxy wrapping this implementation can be used, which lazily looks
 * up the JNDI tree for retrieving the remote object that is expected.
 * <p>
 * This class implements the {@link ServiceDiscoListener} interface, allowing an instance of it
 * to be registered with a {@link DiscoveryHelper}.
 *
 * @author yduchesne
 *
 */
public class LazyStubInvocationHandler implements StubInvocationHandler, ServiceDiscoListener {

  private static final Category LOG = Log.createCategory(LazyStubInvocationHandler.class);
  
  /**
   * A builder of {@link LazyStubInvocationHandler}s.
   */
  public static class Builder {
    private String        name;
    private RemoteContext context;
    private Func<Void, LazyStubInvocationHandler> matchFunction;
    
    private Builder() {
    }
    
    /**
     * @param name the name of the remote object to lookup.
     * @return this instance.
     */
    public Builder name(String name) {
      this.name = name;
      return this;
    }
    
    /**
     * @param context the {@link Context} to use to perform lazy lookup.
     * @return this instance.
     */
    public Builder context(RemoteContext context) {
      this.context = context;
      return this;
    }
  
    /**
     * @param func the {@link Func} that is invoked when a service corresponding to the
     * @return this instance.
     */
    public Builder matchFunction(Func<Void, LazyStubInvocationHandler> func) {
      this.matchFunction = func;
      return this;
    }
    
    /**
     * @return the {@link LazyStubInvocationHandler}.
     */
    public LazyStubInvocationHandler build() {
      Assertions.notNull(name, "Name not set");
      Assertions.notNull(context, "RemoteContext to use for lookup not set");
      Assertions.notNull(matchFunction, "Match function not set");
      return new LazyStubInvocationHandler(name, context, matchFunction);
    }
    
    /**
     * @return a new instance of this class.
     */
    public static Builder newInstance() {
      return new Builder();
    }
  }
  
  // ==========================================================================

  private String                         name;
  private Context                        context;
  private volatile StubInvocationHandler delegate;
  private TimeIntervalBarrier            lookupBarrier = TimeIntervalBarrier.forMillis(
      Conf.newInstance().getLongProperty(Consts.JNDI_LAZY_LOOKUP_INTERVAL, Defaults.DEFAULT_LAZY_LOOKUP_INTERVAL)
  );
  private Func<Void, LazyStubInvocationHandler> discoveryMatchFunction;
  
  /**
   * @param name the name of the remote object to look up.
   * @param context the {@link Context} to use when performing the lookup.
   */
  public LazyStubInvocationHandler(String name, Context context) {
    this(name, context, new Func<Void, LazyStubInvocationHandler>() {
      @Override
      public Void call(LazyStubInvocationHandler arg) {
        return null;
      }
    });
  }

  /**
   * This constructor allows passing a {@link Func} which is invoked when a remote object is found
   * that matches the name of this instance's intended stub.
   * 
   * @param name the name of the remote object to look up.
   * @param context the {@link Context} to use when performing the lookup.
   * @param discoveryMatchFunction the {@link Func} that this invoked when the {@link #onServiceDiscovered(ServiceDiscoveryEvent)} 
   * of this instance is called, and the remote object that was found matches this instance's name. 
   */
  public LazyStubInvocationHandler(String name, Context context, Func<Void, LazyStubInvocationHandler> discoveryMatchFunction) {
    this.name    = name.startsWith("/") ? name : "/" + name;
    this.context = context;
    this.discoveryMatchFunction = discoveryMatchFunction;
  }
  
  @Override
  public StubContainer toStubContainer(Object proxy) {
    return delegate.toStubContainer(proxy);
  }

  @Override
  public Collection<RemoteRefContext> getContexts() {
    if (delegate == null) {
      return Collections.emptyList();
    }
    return delegate.getContexts();
  }

  @Override
  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    if (delegate == null) {
      doLookup();
    }
    return delegate.invoke(proxy, method, args);
  }

  @Override
  public boolean isValid() throws RemoteException {
    if (delegate == null) {
      try {
        doLookup();
        return delegate.isValid();
      } catch (Exception e) {
        LOG.info("Could not perform lookup", e);
        return false;
      }
    }
    return delegate.isValid();
  }

  @Override
  public void onServiceDiscovered(final ServiceDiscoveryEvent evt) {
    if (evt.getName().equals(name.toString()) && delegate == null) {
      try {
        delegate = Stubs.getStubInvocationHandler(evt.getService());
      } catch (RemoteException e) {
        LOG.error("Remote object %s could not be looked up", e, evt.getName());
      }
      Spawn.run(new Runnable() {
        @Override
        public void run() {
          discoveryMatchFunction.call(LazyStubInvocationHandler.this);
        }
      });
    }
  }

  /**
   * @return the {@link StubInvocationHandler} that this instance wraps, or <code>null</code> if 
   * no corresponding remote object has yet been discovered.
   */
  public StubInvocationHandler getDelegate() {
    return delegate;
  }

  // --------------------------------------------------------------------------
  // Restricted
  
  void setDelegate(StubInvocationHandler delegate) {
    this.delegate = delegate;
  }
  
  private void doLookup() throws RemoteException, NamingException {
    synchronized (lookupBarrier) {
      if (delegate == null) {
        if (lookupBarrier.tryAcquire()) {
          delegate = ((StubContainer) context.lookup(name)).getStubInvocationHandler();
        } else {
          throw new RemoteException("Remote object not yet present on the network: " + name);
        }
      }
    }
  }

  @Override
  public String toString() {
    if (delegate != null) {
      return delegate.toString();
    }
    return Strings.toStringFor(this, "name", name);
  }
}
