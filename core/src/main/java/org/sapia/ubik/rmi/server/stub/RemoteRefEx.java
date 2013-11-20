package org.sapia.ubik.rmi.server.stub;

import java.lang.reflect.Method;

import org.sapia.ubik.log.Log;
import org.sapia.ubik.rmi.server.ShutdownException;
import org.sapia.ubik.rmi.server.command.CallbackInvokeCommand;
import org.sapia.ubik.rmi.server.command.InvokeCommand;
import org.sapia.ubik.rmi.server.stub.LocalMethod.LocalMethodMap;

/**
 * This class implements a basic stub handler (no fail over, no load balancing,
 * no replication).
 * 
 * @author Yanick Duchesne
 */
public class RemoteRefEx extends RemoteRef {

  static final long serialVersionUID = 1L;

  private static final LocalMethodMap LOCAL_METHODS = LocalMethodMap.buildDefaultMap();

  /**
   * Do not use: meant for externalization.
   */
  public RemoteRefEx() {
  }

  /**
   * Creates an instance of this class, with the given context.
   * 
   * @param context
   *          a {@link RemoteRefContext}.
   * 
   */
  public RemoteRefEx(RemoteRefContext context) {
    super(context);
  }

  /**
   * @see java.lang.reflect.InvocationHandler#invoke(Object, Method, Object[])
   */
  public Object invoke(Object proxy, Method toCall, Object[] params) throws Throwable {
    Object toReturn = null;

    LocalMethodInvoker invoker = LOCAL_METHODS.getInvokerFor(toCall);
    if (invoker != null) {
      return invoker.invoke(this, params);
    }

    if (context.isCallback()) {
      if (Log.isDebug()) {
        Log.debug(getClass(), "invoking (callback) method " + toCall + " on vmId: " + context.getVmId());
      }

      toReturn = dispatcher().dispatchInvocation(context.getVmId(), context.getConnections(),
          new CallbackInvokeCommand(context.getOid(), toCall.getName(), params, toCall.getParameterTypes(), context.getAddress().getTransportType()));
    } else {
      if (Log.isDebug()) {
        Log.debug(getClass(), "invoking (no callback) method " + toCall + " on vmId: " + context.getVmId());
      }

      toReturn = dispatcher().dispatchInvocation(context.getVmId(), context.getConnections(),
          new InvokeCommand(context.getOid(), toCall.getName(), params, toCall.getParameterTypes(), context.getAddress().getTransportType()));
    }

    if (toReturn == null) {
      return toReturn;
    } else if (toReturn instanceof Throwable) {
      if (toReturn instanceof ShutdownException) {
        onShutdown(proxy, toCall, params);
      }

      throw (Throwable) toReturn;
    }

    return toReturn;
  }

  protected Object onShutdown(Object proxy, Method toCall, Object[] params) throws Throwable {
    throw new ShutdownException();
  }
}
