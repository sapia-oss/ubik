package org.sapia.ubik.ioc.guice;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.rmi.NoSuchObjectException;

import org.sapia.ubik.ioc.NamingService;
import org.sapia.ubik.log.Log;
import org.sapia.ubik.rmi.naming.remote.discovery.ServiceDiscoListener;
import org.sapia.ubik.rmi.naming.remote.discovery.ServiceDiscoveryEvent;

import com.google.inject.Inject;
import com.google.inject.Provider;

/**
 * This provider is used in order to lookup remote objects from remote Ubik JNDI servers. 
 * <p>
 * This provider supports lazy lookups of services: it allows for Ubik services to appear on
 * the network in an ad-hoc manner. More precisely, given services must not necessarily appear on the network
 * before the applications that depend on them.
 * <p>
 * This allows client applications to startup without risking abrupt termination because of failed service lookups:
 * if a given service is not found, a lazy proxy is created and returned at its place. A discovery mechanism 
 * insures that the missing service is eventually retrieved (if it appears on the network). 
 * <p>
 * Example:
 * <pre>
 *   
 *   final NamingService naming = new NamingServiceImpl("default")
 *     .setJndiHost(Localhost.getLocalAddress().getHostAddress())
 *     .setJndiPort(1099);    
 *   
 *   Injector injector = Guice.createInjector(new AbstractModule(){
 *     
 *     @Override
 *    protected void configure() {
 *      bind(NamingService.class).toInstance(naming);        
 *      bind(TimeServiceIF.class).toProvider(new RemoteServiceImporter<TimeServiceIF>(TimeServiceIF.class, "services/time"));
 *    }
 *     
 *  });
 *   
 *  TimeServiceIF service = injector.getInstance(TimeServiceIF.class);
 *  System.out.println("Got time: " + service.getTime());
 * </pre>
 * 
 * Note that a {@link NamingService} must have been bound beforehand (so that it can be injected into 
 * this instance).
 * 
 * @see NamingService
 * @see RemoteServiceExporter
 * 
 * @author yduchesne
 */
public class RemoteServiceImporter<T> implements Provider<T>{

  @Inject
  private NamingService naming;
  private String        name;
  private T             remote;
  private Class<T>      type;

  /**
   * Creates a new instance of {@link RemoteServiceImporter}.
   * 
   * @param type the {@link Class} of the service to retrieve.
   * @param jndiName the name under which the expected service is bound into
   * Ubik's JNDI server(s).
   */
  public RemoteServiceImporter(Class<T> type, String jndiName) {
    this.type = type;
    this.name = jndiName;
  }

  public T get() {
    if (name == null) {
      throw new IllegalStateException("JNDI name not set on this provider");
    }
    if (naming == null) {
      throw new IllegalStateException(NamingService.class.getName()
          + " instance not set on this provider");
    }

    try {
      remote = type.cast(naming.lookup(name));
      if (Log.isInfo()) {
        Log.info(getClass(), "Resolved remote service: " + name);
      }
      
      // returning the remote object that was found
      return remote;
    } catch (Exception e) {
      Log.warning(getClass(), "Could not resolve remote service: " + name + "; attempting discovery");
      Log.error(getClass(), e);
      naming.register(new ServiceDiscoListenerImpl());
      
      // not found remote object, returning lazy proxy
      return type.cast(Proxy.newProxyInstance(Thread.currentThread()
          .getContextClassLoader(), new Class[] { type },
          new ProxyInvocationHandler(this)));
    }
  }

  ////////////////// INNER CLASSES ///////////////////
  
  class ServiceDiscoListenerImpl implements ServiceDiscoListener{ 

    public void onServiceDiscovered(ServiceDiscoveryEvent anEvent) {
      // Matching service name as-is or without the initial '/' character
      if (anEvent.getName().equals(name)
          || (anEvent.getName().charAt(0) == '/' && anEvent.getName()
              .substring(1).equals(name))) {
        try {
          remote = type.cast(anEvent.getService());
          naming.unregister(this);
        } catch (Exception e) {
          Log.error(getClass(), "Could not resolve remote service: " + name, e);
        }
      }
    }
  }

  /**
   * Internal proxy class to handle invocations
   */
  public class ProxyInvocationHandler implements InvocationHandler {

    private RemoteServiceImporter<T> owner;

    ProxyInvocationHandler(RemoteServiceImporter<T> owner) {
      this.owner = owner;
    }

    public Object invoke(Object proxy, Method method, Object[] args)
        throws Throwable {
      // Forward Service life-cycle calls to the proxy service
      if (owner.remote != null) {
        try {
          Object result = method.invoke(owner.remote, args);
          return result;
        } catch (InvocationTargetException e) {
          throw e.getTargetException();
        }
      } else {
        try {
          // attempting calling on this (in case of toString(), equals()...)
          return method.invoke(this, args);
        } catch (InvocationTargetException e) {
          throw e.getTargetException();
        } catch (Exception e) {
        }

        throw new NoSuchObjectException(owner.name);
      }
    }
  }

}