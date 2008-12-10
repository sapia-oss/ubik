package org.sapia.ubik.rmi.server;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import javax.naming.NamingException;

import org.sapia.ubik.net.ServerAddress;
import org.sapia.ubik.rmi.naming.ServiceLocator;


/**
 * A stub handler that manages reconnecting to another server instance
 * provided a method call fails.
 *
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class RemoteRefReliable extends RemoteRefEx implements HealthCheck {
  static final long serialVersionUID = 1L;
  protected String  _url;

  /**
   * Constructor for StubHandlerReliable.
   */
  public RemoteRefReliable() {
    super();
  }

  /**
   * Creates an instance of this class, with the given object and host
   * identifiers.
   *
   * @param oid an <code>OID</code>
   * @param serverAddress a <code>ServerAddress</code>
   *
   */
  public RemoteRefReliable(OID oid, ServerAddress serverAddress) {
    super(oid, serverAddress);
  }

  /**
   * Sets this handler's server URL.
   *
   * @param url a URL string.
   */
  public void setUp(String url) {
    _url = url;
  }

  /**
   * @see java.lang.reflect.InvocationHandler#invoke(Object, Method, Object[])
   */
  public Object invoke(Object obj, Method toCall, Object[] params)
    throws Throwable {
    try {
      return super.invoke(obj, toCall, params);
    } catch (java.rmi.RemoteException e) {
      if (_url != null) {
        return doFailOver(obj, toCall, params, e);
      } else {
        throw e;
      }
    }
  }

  /**
   * @see java.io.Externalizable#readExternal(ObjectInput)
   */
  public void readExternal(ObjectInput in)
    throws IOException, ClassNotFoundException {
    super.readExternal(in);
    _url = (String) in.readObject();
  }

  /**
   * @see java.io.Externalizable#writeExternal(ObjectOutput)
   */
  public void writeExternal(ObjectOutput out) throws IOException {
    super.writeExternal(out);
    out.writeObject(_url);
  }

  /**
   * @see org.sapia.ubik.rmi.server.RemoteRefEx#onShutdown(Object, Method, Object[])
   */
  protected Object onShutdown(Object proxy, Method toCall, Object[] params)
    throws Throwable {
    return doFailOver(proxy, toCall, params, new ShutdownException());
  }

  /**
   * Implements fail-over logic.
   */
  protected synchronized Object doFailOver(Object obj, Method toCall,
    Object[] params, Throwable err) throws Throwable {
    try {
      Object remote = ServiceLocator.lookup(_url);

      if (remote instanceof Stub && Proxy.isProxyClass(remote.getClass())) {
        try {
          ///////////
          //This section might need a better synchronization pattern...
          synchronized (_lock) {
            ServerAddress newAddr = ((RemoteRef) Proxy.getInvocationHandler(remote)).getServerAddress();
            _oid = ((RemoteRef) Proxy.getInvocationHandler(remote)).getOid();

            if (!newAddr.equals(_serverAddress)) {
              _serverAddress = newAddr;
              initPool(true);
            } else {
              _pool.clear();
            }
          }

          Hub.clientRuntime.gc.register(_serverAddress, _oid, this);
        } catch (ClassCastException e) {
          throw err;
        }

        return super.invoke(obj, toCall, params);
      } else {
        throw err;
      }
    } catch (NamingException e) {
      throw err;
    }
  }
}
