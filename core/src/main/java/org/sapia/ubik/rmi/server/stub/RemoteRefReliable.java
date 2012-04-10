package org.sapia.ubik.rmi.server.stub;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.lang.reflect.Method;

import javax.naming.NamingException;

import org.sapia.ubik.log.Category;
import org.sapia.ubik.log.Log;
import org.sapia.ubik.rmi.naming.ServiceLocator;
import org.sapia.ubik.rmi.server.ShutdownException;


/**
 * A stub handler that manages reconnecting to another server instance
 * provided a method call fails.
 *
 */
public class RemoteRefReliable extends RemoteRefEx {
  
  static final long serialVersionUID = 1L;
  
  private           String   url;
  private transient Category log = Log.createCategory(getClass());
  private transient Object   lock = new Object();

  /**
   * Do not use: meant for externalization.
   */
  public RemoteRefReliable() {
    super();
  }

  /**
   * Creates an instance of this class, with the given context.
   *
   * @param context a {@link RemoteRefContext}.
   */
  public RemoteRefReliable(RemoteRefContext context, String url) {
    super(context);
    this.url = url;
  }

  /**
   * @see java.lang.reflect.InvocationHandler#invoke(Object, Method, Object[])
   */
  public Object invoke(Object obj, Method toCall, Object[] params)
    throws Throwable {
    try {
      return super.invoke(obj, toCall, params);
    } catch (java.rmi.RemoteException e) {
      if (url != null) {
      	log.info("RemoteException caught, performing failover");
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
    url = (String) in.readObject();
  }

  /**
   * @see java.io.Externalizable#writeExternal(ObjectOutput)
   */
  public void writeExternal(ObjectOutput out) throws IOException {
    super.writeExternal(out);
    out.writeObject(url);
  }

  /**
   * @see org.sapia.ubik.rmi.server.stub.RemoteRefEx#onShutdown(Object, Method, Object[])
   */
  protected Object onShutdown(Object proxy, Method toCall, Object[] params)
    throws Throwable {
    return doFailOver(proxy, toCall, params, new ShutdownException());
  }

  /**
   * Implements fail-over logic.
   */
  protected synchronized Object doFailOver(Object obj, Method toCall, Object[] params, Throwable err) throws Throwable {
    try {
      Object remote = ServiceLocator.lookup(url);

      log.debug("Looked up remote object %s", url);
      
      synchronized (lock) {
        log.debug("Performing failover for %s", url);
        RemoteRefContext newContext = Stubs.getStubInvocationHandler(remote).getContexts().iterator().next();
        newContext.getConnections().clear();
        context = newContext;
      }

      return super.invoke(obj, toCall, params);
    } catch (NamingException e) {
      throw err;
    }
  }
}
