package org.sapia.ubik.rmi.server.stub;

import java.lang.reflect.Proxy;
import java.rmi.RemoteException;


/**
 * Base implementation of the {@link StubContainer} interface.
 *
 * @author Yanick Duchesne
 *
 */
public class StubContainerBase implements StubContainer, HealthCheck {
  
  static final long serialVersionUID = 1L;
  
  private StubInvocationHandler ref;
  private String[]              interfaceNames;
  
  protected StubContainerBase(String[] interfaceNames, StubInvocationHandler handler) {
    ref                 = handler;
    this.interfaceNames = interfaceNames;
  }

  /**
   * Returns <code>true</code> if the connection to the remote server of the stub
   * that this instance wraps is still valid.
   *
   * @throws RemoteException if a problem occurs performing the check; if such an
   * error occurs, this instance should be treated as invalid.
   */
  public boolean isValid() throws RemoteException {
    if (ref instanceof HealthCheck) {
      return ((HealthCheck)  ref).isValid();
    }

    return true;
  }

  /**
   * @see StubContainer#toStub(ClassLoader)
   */
  public Object toStub(ClassLoader loader) throws RemoteException {
    Class<?>[] interfaces = new Class[interfaceNames.length];
    for (int i = 0; i < interfaceNames.length; i++) {
      try {
        interfaces[i] = loader.loadClass(interfaceNames[i]);
      } catch (ClassNotFoundException e) {
        throw new RemoteException("Could not find interface definition: " + interfaceNames[i], e);
      }
    }
    return Proxy.newProxyInstance(loader, interfaces, ref);
  }

  /**
   * @see StubContainer#getStubInvocationHandler()
   */
  public StubInvocationHandler getStubInvocationHandler() {
    return ref;
  }

  public String toString() {
    return ref.toString();
  }
}
