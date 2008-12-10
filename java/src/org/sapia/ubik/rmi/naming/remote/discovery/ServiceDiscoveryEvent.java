package org.sapia.ubik.rmi.naming.remote.discovery;

import java.rmi.RemoteException;
import java.util.Properties;

import org.sapia.ubik.rmi.server.StubContainer;


/**
 * This class models an event that is triggered by the binding of a new object in
 * the <code>ReliableRemoteContext</code>.
 * <p>
 * Client-side <code>ServiceDiscoListener</code>s are used to trap instances of this
 * class.
 * <p>
 * This allows client apps to be notified when new services are bound into the JNDI.
 *
 * @author Yanick Duchesne
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class ServiceDiscoveryEvent {
  private Properties _attributes;
  private String     _name;
  private Object     _service;

  /**
   * Constructor for ServiceDiscoveryEvent.
   */
  public ServiceDiscoveryEvent(Properties attributes, String name, Object service) {
    _name         = name;
    _service      = service;
    _attributes   = attributes;
  }

  /**
   * Returns the name under which the service was bound.
   *
   * @return a name.
   */
  public String getName() {
    return _name;
  }

  /**
   * Returns the service that was bound.
   *
   * @return an <code>Object</code>.
   */
  public Object getService() throws RemoteException {
    if (_service instanceof StubContainer) {
      return ((StubContainer) _service).toStub(Thread.currentThread()
                                                     .getContextClassLoader());
    }

    return _service;
  }

  /**
   * @return the <code>Properties</code> corresponding to the attributes
   * used to bind the service within this instance.
   */
  public Properties getAttributes() {
    return _attributes;
  }

  public String toString() {
    return "[ name=" + _name + ", service=" + _service + ", attributes=" +
    _attributes + " ]";
  }
}
