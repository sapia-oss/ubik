package org.sapia.ubik.rmi.naming.remote;

import java.lang.reflect.Method;
import java.rmi.RemoteException;
import java.util.Collection;
import java.util.Collections;

import javax.naming.Context;

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
import org.sapia.ubik.util.Conf;
import org.sapia.ubik.util.Strings;

/**
 * This {@link StubInvocationHandler} implementation is used on the client side, when a lookup on
 * a given remote object has failed because it not yet bound to the JNDI tree.
 * <p>
 * In such a case, a dynamic proxy wrapping this implementation can be used, which lazily looks
 * up the JNDI tree for retrieving the
 *
 * @author yduchesne
 *
 */
public class LazyStubInvocationHandler implements StubInvocationHandler, ServiceDiscoListener {

  private static final Category LOG = Log.createCategory(LazyStubInvocationHandler.class);

  private DiscoveryHelper                discoHelper;
  private String                         name;
  private Context                        context;
  private volatile StubInvocationHandler delegate;
  private TimeIntervalBarrier            lookupBarrier = TimeIntervalBarrier.forMillis(
      Conf.newInstance().getLongProperty(Consts.JNDI_LAZY_LOOKUP_INTERVAL, Defaults.DEFAULT_LAZY_LOOKUP_INTERVAL)
  );

  /**
   * @param helper the {@link DiscoveryHelper} with which this instance should register itself.
   * @param name the name of the remote object to look up.
   * @param context the {@link Context} to use when performing the lookup.
   */
  public LazyStubInvocationHandler(DiscoveryHelper helper, String name, Context context) {
    this.discoHelper = helper;
    this.name        = name.startsWith("/") ? name : "/" + name;
    this.context     = context;
    helper.addServiceDiscoListener(this);
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
    return delegate.invoke(proxy, method, args);
  }

  @Override
  public boolean isValid() throws RemoteException {
    if (delegate == null) {
      return false;
    }
    return delegate.isValid();
  }

  @Override
  public void onServiceDiscovered(ServiceDiscoveryEvent evt) {
    if (evt.getName().equals(name.toString()) && delegate != null) {
      try {
        delegate = ((StubContainer) evt.getService()).getStubInvocationHandler();
      } catch (RemoteException e) {
        LOG.error("Remote object could not be looked up", e);
      }
      Spawn.run(new Runnable() {
        @Override
        public void run() {
          discoHelper.removeServiceDiscoListener(LazyStubInvocationHandler.this);
        }
      });
    }
  }

  // --------------------------------------------------------------------------
  // Restricted

  protected StubInvocationHandler getDelegate() {
    return delegate;
  }

  protected DiscoveryHelper getDiscoHelper() {
    return discoHelper;
  }

  @Override
  public String toString() {
    if (delegate != null) {
      return delegate.toString();
    }
    return Strings.toStringFor(this, "name", name);
  }

}
