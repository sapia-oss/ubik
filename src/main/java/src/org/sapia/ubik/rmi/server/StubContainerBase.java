package org.sapia.ubik.rmi.server;

import java.lang.reflect.Proxy;
import java.rmi.RemoteException;

import org.sapia.ubik.rmi.Consts;


/**
 * Base implementation of the <code>StubContainer</code> interface.
 *
 * @author Yanick Duchesne
 *
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class StubContainerBase implements StubContainer, HealthCheck {
  private StubInvocationHandler _ref;
  private String[]              _interfaceNames;
  private static final boolean CODE_DOWNLOAD = 
    System.getProperty(Consts.ALLOW_CODE_DOWNLOAD) != null &&
    System.getProperty(Consts.ALLOW_CODE_DOWNLOAD).equals("true");

  protected StubContainerBase(String[] interfaceNames,
    StubInvocationHandler handler) {
    _ref              = handler;
    _interfaceNames   = interfaceNames;
  }

  /**
   * Returns <code>true</code> if the connection to the remote server of the stub
   * that this instance wraps is still valid.
   *
   * @throws RemoteException if a problem occurs performing the check; if such an
   * error occurs, this instance should be treated as invalid.
   */
  public boolean isValid() throws RemoteException {
    if (_ref instanceof HealthCheck) {
      return ((HealthCheck) _ref).isValid();
    }

    return true;
  }

  /**
   * @see StubContainer#toStub(ClassLoader)
   */
  public Object toStub(ClassLoader loader) throws RemoteException {
    Class[] interfaces = new Class[_interfaceNames.length];

    if(_ref.getOID().getCodebase() != null && CODE_DOWNLOAD){
      loader = new RmiClassLoader(loader, _ref.getOID().getCodebase());
    }
    for (int i = 0; i < _interfaceNames.length; i++) {
      try {
        interfaces[i] = loader.loadClass(_interfaceNames[i]);
      } catch (ClassNotFoundException e) {
        throw new RemoteException("Could not find interface definition: " +
          _interfaceNames[i], e);
      }
    }

    return Proxy.newProxyInstance(loader, interfaces, _ref);
  }

  /**
   * @see StubContainer#getStubInvocationHandler()
   */
  public StubInvocationHandler getStubInvocationHandler() {
    return _ref;
  }

  public String toString() {
    return _ref.toString();
  }
}
