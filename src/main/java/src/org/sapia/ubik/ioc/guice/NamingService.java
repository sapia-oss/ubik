package org.sapia.ubik.ioc.guice;

import javax.naming.NameNotFoundException;
import javax.naming.NamingException;

import org.sapia.ubik.rmi.naming.remote.discovery.ServiceDiscoListener;

/**
 * Specifies the behavior of a service that serves as a broker between
 * a remote Ubik JNDI provider and in-VM Guice-instantiated objects.
 * 
 * @see RemoteServiceExporter
 * @see RemoteServiceImporter
 *
 * @author yduchesne
 */
public interface NamingService {
  
  /**
   * Binds the given object to the remote Ubik JNDI provider to which this instance
   * corresponds.
   * 
   * @param name the name under which to bind the object.
   * @param o the object to bind.
   */
  public void bind(String name, Object o) throws NamingException;

  /**
   * Looks up the object under the given name.
   * 
   * @param name the name of the remote object to lookup.
   */
  public Object lookup(String name) throws NamingException,
      NameNotFoundException;
  
  /**
   * Registers the given listener with this instance.
   * 
   * @param listener a <code>ServiceDiscoListener</code>.
   */
  public void register(ServiceDiscoListener listener);
  
  /**
   * Unregisters the given listener from this instance.
   * 
   * @param listener a <code>ServiceDiscoListener</code>.
   */
  public void unregister(ServiceDiscoListener listener);  


  /**
   * Shuts down this instance. 
   */
  public void shutdown();
}
