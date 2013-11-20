package org.sapia.ubik.rmi.naming.remote.discovery;

import java.rmi.RemoteException;
import java.util.Properties;

import org.sapia.ubik.rmi.server.stub.StubContainer;

/**
 * This class models an event that is triggered by the binding of a new object
 * in a JNDI node.
 * <p>
 * Client-side {@link ServiceDiscoListener}s are used to trap instances of this
 * class.
 * <p>
 * This allows client apps to be notified when new services are bound into the
 * JNDI.
 * 
 * @author Yanick Duchesne
 */
public class ServiceDiscoveryEvent {
  private Properties attributes;
  private String name;
  private Object service;

  public ServiceDiscoveryEvent(Properties attributes, String name, Object service) {
    this.name = name;
    this.service = service;
    this.attributes = attributes;
  }

  /**
   * Returns the name under which the service was bound.
   * 
   * @return a name.
   */
  public String getName() {
    return name;
  }

  /**
   * Returns the service that was bound.
   * 
   * @return an <code>Object</code>.
   */
  public Object getService() throws RemoteException {
    if (service instanceof StubContainer) {
      return ((StubContainer) service).toStub(Thread.currentThread().getContextClassLoader());
    }

    return service;
  }

  /**
   * @return the <code>Properties</code> corresponding to the attributes used to
   *         bind the service within this instance.
   */
  public Properties getAttributes() {
    return attributes;
  }

  public String toString() {
    return "[ name=" + name + ", service=" + service + ", attributes=" + attributes + " ]";
  }
}
