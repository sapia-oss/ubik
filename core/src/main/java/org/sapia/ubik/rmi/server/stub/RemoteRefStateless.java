package org.sapia.ubik.rmi.server.stub;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.lang.reflect.Method;
import java.rmi.RemoteException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.sapia.ubik.log.Category;
import org.sapia.ubik.log.Log;
import org.sapia.ubik.mcast.MulticastAddress;
import org.sapia.ubik.rmi.server.CommandPing;
import org.sapia.ubik.rmi.server.Hub;
import org.sapia.ubik.rmi.server.RuntimeRemoteException;
import org.sapia.ubik.rmi.server.ShutdownException;
import org.sapia.ubik.rmi.server.UIDGenerator;
import org.sapia.ubik.rmi.server.command.CallbackInvokeCommand;
import org.sapia.ubik.rmi.server.command.InvokeCommand;
import org.sapia.ubik.rmi.server.invocation.InvocationDispatcher;
import org.sapia.ubik.rmi.server.oid.DefaultOID;
import org.sapia.ubik.rmi.server.oid.OID;
import org.sapia.ubik.rmi.server.stub.LocalMethod.LocalMethodMap;
import org.sapia.ubik.util.Collects;
import org.sapia.ubik.util.Exceptions;
import org.sapia.ubik.util.Func;
import org.sapia.ubik.util.Strings;

/**
 * A {@link StubInvocationHandler} that manages reconnecting to another server
 * instance provided a method call fails.
 * <p>
 * Note that this class does NOT inherit from {@link RemoteRef} - despite what
 * the name might suggest. This is because this class, by definition, does not
 * correspond to a single server endpoint, but to multiple server endpoints.
 * <p>
 * Indeed, an instance of this class corresponds to all servers that were bound
 * under a given name.
 * <p>
 * This design might be reviewed eventually, to provide a more consistent class
 * hierarchy.
 *
 */
public class RemoteRefStateless implements StubInvocationHandler, Externalizable, HealthCheck {

  static final long serialVersionUID = 1L;

  private static final LocalMethodMap LOCAL_METHODS = LocalMethodMap.buildDefaultMap();

  private static Category log = Log.createCategory(RemoteRefStateless.class);

  private String name;
  private String domain;
  private MulticastAddress multicastAddress;
  private OID oid = new DefaultOID(UIDGenerator.createUID());
  private transient ContextList contexts = new ContextList();
  private transient ThreadLocal<ThreadSpecificContextList> threadContexts = new ThreadLocal<>();
  private transient volatile InvocationDispatcher dispatcher;

  /**
   * Do not use: meant for externalization.
   */
  public RemoteRefStateless() {
    super();
  }

  /**
   * Creates an instance of this class
   */
  public RemoteRefStateless(String name, String domain, MulticastAddress multicastAddress) {
    this.name = name;
    this.domain = domain;
    this.multicastAddress = multicastAddress;
  }

  /**
   * @return this instance's domain.
   */
  public String getDomain() {
    return domain;
  }

  /**
   * @return this instance's {@link MulticastAddress}.
   */
  public MulticastAddress getMulticastAddress() {
    return multicastAddress;
  }

  /**
   * @return the name under which this instance's corresponding remote
   *         object was bound.
   */
  public String getName() {
    return name;
  }

  @Override
  public Collection<RemoteRefContext> getContexts() {
    return contexts.getAll();
  }

  /**
   * @see java.lang.reflect.InvocationHandler#invoke(Object, Method, Object[])
   */
  @Override
  public Object invoke(Object obj, Method toCall, Object[] params) throws Throwable {
    Object toReturn = null;

    LocalMethodInvoker invoker = LOCAL_METHODS.getInvokerFor(toCall);
    if (invoker != null) {
      return invoker.invoke(this, params);
    }

    RemoteRefContext context = acquire();
    log.debug("Performing invocation using context: %s", context);
    try {
      toReturn = doInvoke(context, obj, toCall, params);
    } catch (ShutdownException e) {
      toReturn = handleError(context, obj, toCall, params, e);
    } catch (RemoteException | RuntimeRemoteException e) {
      toReturn = handleError(context, obj, toCall, params, e);
    }

    return toReturn;
  }

  /**
   * @see org.sapia.ubik.rmi.server.stub.HealthCheck#isValid()
   */
  @Override
  public boolean isValid() {
    try {
      return clean();
    } catch (Throwable t) {
      log.warning("Stub not valid", t);
      return false;
    }
  }

  /**
   * @see StubInvocationHandler#toStubContainer(Object)
   */
  @Override
  public StubContainer toStubContainer(Object proxy) {
    Set<Class<?>> interfaces = new HashSet<Class<?>>();
    Hub.getModules().getServerTable().getTypeCache().collectInterfaces(proxy.getClass(), interfaces);
    String[] names = Collects.convertAsArray(interfaces, String.class, new Func<String, Class<?>>() {
      @Override
      public String call(Class<?> interf) {
        return interf.getName();
      }
    });
    return new StubContainerBase(names, this);
  }

  /**
   * @see java.io.Externalizable#readExternal(ObjectInput)
   */
  @Override
  @SuppressWarnings(value = "unchecked")
  public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
    name = in.readUTF();
    domain = in.readUTF();
    multicastAddress = (MulticastAddress) in.readObject();
    oid = (OID) in.readObject();
    Collection<RemoteRefContext> remoteContexts = (Collection<RemoteRefContext>) in.readObject();
    contexts = new ContextList();

    log.debug("Deserializing stateless stub (%s); endpoints: %s", name, remoteContexts);

    Hub.getModules().getStatelessStubTable().registerStatelessRef(this, remoteContexts);
  }

  /**
   * @see java.io.Externalizable#writeExternal(ObjectOutput)
   */
  @Override
  public void writeExternal(ObjectOutput out) throws IOException {
    out.writeUTF(name);
    out.writeUTF(domain);
    out.writeObject(multicastAddress);
    out.writeObject(oid);
    out.writeObject(contexts.getAllAsSet());
  }

  /**
   * Returns this instance's underlying connection information.
   *
   * @return this instance's {@link Contexts}.
   */
  ContextList getContextList() {
    return contexts;
  }

  private boolean clean() {
    synchronized (contexts) {
      for (RemoteRefContext context : contexts.getAll()) {
        try {
          Stubs.trySendCommand(new CommandPing(), context);
        } catch (RemoteException e) {
          log.info("Cleaning up invalid endpoint:  %s", context.getAddress());
          contexts.remove(context);
        } catch (Exception e) {
          log.warning("Unknown error trying to clean endpoint " + context, e);
        }
      }

    }
    return contexts.count() > 0;
  }

  private Object doInvoke(RemoteRefContext context, Object obj, Method toCall, Object[] params) throws Throwable {
    Object toReturn = null;

    if (context.isCallback()) {
      log.debug("Invoking (callback): %s", toCall);

      toReturn = dispatcher().dispatchInvocation(context.getVmId(), context.getConnections(),
          new CallbackInvokeCommand(context.getOid(), toCall.getName(), params, toCall.getParameterTypes(), context.getAddress().getTransportType()));
    } else {
      log.debug("Invoking (no callback): %s", toCall);

      toReturn = dispatcher().dispatchInvocation(context.getVmId(), context.getConnections(),
          new InvokeCommand(context.getOid(), toCall.getName(), params, toCall.getParameterTypes(), context.getAddress().getTransportType()));
    }

    if (toReturn == null) {
      return toReturn;
    } else if (toReturn instanceof Throwable) {
      Throwable err = (Throwable) toReturn;
      Exceptions.fillInStackTrace(err);
      throw err;
    }

    return toReturn;
  }

  private RemoteRefContext acquire() throws RemoteException {
    log.debug("Proceeding to round-robin");
    ThreadSpecificContextList tsl = threadContexts.get();
    if (tsl == null) {
      tsl = contexts.getThreadSpecificContextList();
      threadContexts.set(tsl);
    }
    return tsl.roundrobin();
  }

  private RemoteRefContext removeAcquire(RemoteRefContext toRemove) throws RemoteException {
    log.info("Removing invalid instance: %s", toRemove.getAddress());
    contexts.remove(toRemove);
    log.debug("Remaining: %s", contexts);
    return acquire();
  }

  private Object handleError(RemoteRefContext context, Object obj, Method toCall, Object[] params, Throwable err) throws Throwable {
    do {
      context = removeAcquire(context);

      try {
        return doInvoke(context, obj, toCall, params);
      } catch (Throwable t) {
        if (Exceptions.isRemoteException(t)) {
          err = t;
        }
      }
    } while (Exceptions.isRemoteException(err) && (contexts.count() > 0));

    throw err;
  }

  private InvocationDispatcher dispatcher() {
    if (dispatcher == null) {
      dispatcher = Hub.getModules().getInvocationDispatcher();
    }
    return dispatcher;
  }

  @Override
  public int hashCode() {
    return oid.hashCode();
  }

  @Override
  public boolean equals(Object o) {
    if (o instanceof RemoteRefStateless) {
      RemoteRefStateless other = (RemoteRefStateless) o;
      return this == other || other.oid.equals(oid);
    }
    return false;
  }

  @Override
  public String toString() {
    return Strings.toString("name", name, "domain", domain, "multicastAddress", multicastAddress, "contexts", contexts);
  }
}
