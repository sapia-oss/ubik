package org.sapia.ubik.rmi.server.stub;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.lang.reflect.Method;
import java.rmi.RemoteException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.sapia.ubik.rmi.server.ClientRuntime;
import org.sapia.ubik.rmi.server.CommandPing;
import org.sapia.ubik.rmi.server.Hub;
import org.sapia.ubik.rmi.server.VmId;
import org.sapia.ubik.rmi.server.invocation.InvocationDispatcher;
import org.sapia.ubik.util.Collections2;
import org.sapia.ubik.util.Function;

/**
 * This class implements the basic behavior of dynamic proxies that implement
 * the logic for performing RPC on remote objects.
 *
 * @author Yanick Duchesne
 */
public abstract class RemoteRef implements StubInvocationHandler, Externalizable {

  static final long serialVersionUID = 1L;

  private VmId vmid = VmId.getInstance();
  protected RemoteRefContext context;
  private transient InvocationDispatcher dispatcher;
  private transient ClientRuntime clientRuntime;


  /**
   * Do not use: meant for externalization.
   */
  public RemoteRef() {
  }

  /**
   * Creates an instance of this class, with the given context.
   *
   * @param context
   *          a {@link RemoteRefContext}.
   */
  public RemoteRef(RemoteRefContext context) {
    this.context = context;
  }

  @Override
  public Collection<RemoteRefContext> getContexts() {
    return Collections.singletonList(context);
  }

  /**
   * @see java.lang.reflect.InvocationHandler#invoke(Object, Method, Object[])
   */
  @Override
  public abstract Object invoke(Object obj, Method toCall, Object[] params) throws Throwable;

  /**
   * Tests the connection between this handler and its server; returns false if
   * connection is invalid.
   *
   * @return <code>false</code> if connection is invalid.
   */
  @Override
  public boolean isValid() {
    try {
      Stubs.trySendCommand(new CommandPing(), context);
    } catch (RemoteException e) {
      return false;
    } catch (Exception e) {
      // noop
    }
    return true;
  }

  /**
   * @see StubInvocationHandler#toStubContainer(Object)
   */
  @Override
  public StubContainer toStubContainer(Object proxy) {
    Set<Class<?>> interfaces = new HashSet<Class<?>>();
    Hub.getModules().getServerTable().getTypeCache().collectInterfaces(proxy.getClass(), interfaces);
    String[] names = Collections2.convertAsArray(interfaces, String.class, new Function<String, Class<?>>() {
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
  public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
    vmid = (VmId) in.readObject();
    context = (RemoteRefContext) in.readObject();
  }

  /**
   * @see java.io.Externalizable#writeExternal(ObjectOutput)
   */
  @Override
  public void writeExternal(ObjectOutput out) throws IOException {
    out.writeObject(vmid);
    out.writeObject(context);
  }

  protected ClientRuntime clientRuntime() {
    if (clientRuntime == null) {
      clientRuntime = Hub.getModules().getClientRuntime();
    }
    return clientRuntime;
  }

  protected InvocationDispatcher dispatcher() {
    if (dispatcher == null) {
      dispatcher = Hub.getModules().getInvocationDispatcher();
    }
    return dispatcher;
  }

  @Override
  public String toString() {
    return context.toString();
  }
}
