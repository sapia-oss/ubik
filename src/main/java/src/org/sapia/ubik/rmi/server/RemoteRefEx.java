package org.sapia.ubik.rmi.server;

import java.lang.reflect.Method;

import org.sapia.ubik.net.ServerAddress;
import org.sapia.ubik.rmi.server.invocation.CallBackInvokeCommand;
import org.sapia.ubik.rmi.server.invocation.InvokeCommand;


/**
 * This class implements a basic stub handler (no fail over,
 * no load balancing, no replication).
 *
 * @author Yanick Duchesne
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class RemoteRefEx extends RemoteRef {
  static final long serialVersionUID = 1L;

  public RemoteRefEx() {
  }

  /**
   * Creates an instance of this class, with the given object and host
   * identifiers.
   *
   * @param oid an <code>OID</code>
   * @param serverAddress a <code>ServerAddress</code>
   *
   */
  public RemoteRefEx(OID oid, ServerAddress serverAddress) {
    super(oid, serverAddress);
  }

  /**
   * @see java.lang.reflect.InvocationHandler#invoke(Object, Method, Object[])
   */
  public Object invoke(Object proxy, Method toCall, Object[] params)
    throws Throwable {
    Object toReturn = null;

    if (_callBack &&
          Hub.clientRuntime.isCallback(_serverAddress.getTransportType())) {
      if (Log.isDebug()) {
        Log.debug(getClass(), "invoking (callback) method " + toCall + " on vmId: " + _vmId);
      }

      if (_pool == null) {
        super.initPool(false);
      }

      toReturn = ClientRuntime.invoker.dispatchInvocation(_vmId, _pool,
          new CallBackInvokeCommand(_oid, toCall.getName(), params,
            toCall.getParameterTypes(), _serverAddress.getTransportType()));
    } else {
      if (Log.isDebug()) {
        Log.debug(getClass(), "invoking (no callback) method " + toCall + " on vmId: " + _vmId);
      }

      if (_pool == null) {
        super.initPool(false);
      }

      toReturn = ClientRuntime.invoker.dispatchInvocation(_vmId, _pool,
          new InvokeCommand(_oid, toCall.getName(), params,
            toCall.getParameterTypes(), _serverAddress.getTransportType()));
    }

    if (toReturn == null) {
      return toReturn;
    } else if (toReturn instanceof Throwable) {
      if (toReturn instanceof ShutdownException) {
        onShutdown(proxy, toCall, params);
      }

      Throwable err = (Throwable) toReturn;
      err.fillInStackTrace();
      throw err;
    }

    return toReturn;
  }

  protected Object onShutdown(Object proxy, Method toCall, Object[] params)
    throws Throwable {
    throw new ShutdownException();
  }
}
