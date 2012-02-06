package org.sapia.ubik.ioc.spring;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.rmi.NoSuchObjectException;

import org.sapia.ubik.ioc.NamingService;
import org.sapia.ubik.log.Log;
import org.sapia.ubik.rmi.naming.remote.discovery.ServiceDiscoListener;
import org.sapia.ubik.rmi.naming.remote.discovery.ServiceDiscoveryEvent;
import org.springframework.beans.BeansException;
import org.springframework.beans.FatalBeanException;
import org.springframework.beans.factory.config.BeanPostProcessor;

/**
 * This bean post processor is used in order to lookup remote objects from remote Ubik JNDI servers. 
 * It processes {@link RemoteBeanRef} instances, returning in their place the the actual remote beans 
 * that these bean references point to.
 * <p>
 * This post processor supports lazy lookups of services: it allows for Ubik services to appear on
 * the network in an ad-hoc manner. More precisely, given services must not necessarily appear on the network
 * before the applications that depend on them.
 * <p>
 * This allows client applications to startup without risking abrupt termination because of failed service lookups:
 * if a given service is not found, a lazy proxy is created and returned at its place. A discovery mechanism 
 * insures that the missing service is eventually retrieved (if it appears on the network). 
 * 
 * Note that a {@link NamingServiceBean} must have been configurued beforehand (so that it is used by
 * this post processor when performing lookups).
 * 
 * @see NamingService
 * 
 * @author yduchesne
 */
public class BeanImporterPostProcessor implements BeanPostProcessor{

  private NamingService naming;

  @Override
  public Object postProcessAfterInitialization(Object bean, String name)
      throws BeansException {
    
    if(bean instanceof NamingService){
      naming = (NamingService)bean;
    }
    else if(bean instanceof RemoteBeanRef){
      RemoteBeanRef ref = (RemoteBeanRef)bean;
      if(ref.getName() == null){
        throw new FatalBeanException("'name' property not set on remote bean reference");
      }
      if (naming == null) {
        throw new IllegalStateException(NamingService.class.getName()
            + " not passed to this post processor; make sure you declare a " 
            + NamingServiceBean.class.getName());
      }
      Object remote = null;
      try {
        remote = naming.lookup(ref.getName());
        if (Log.isInfo()) {
          Log.info(getClass(), "Resolved remote service: " + ref.getName());
        }
        
        // returning the remote object that was found
        return remote;
      } catch (Exception e) {
        Log.warning(getClass(), "Could not resolve remote service: " + ref.getName()
            + "; attempting discovery");
        Log.error(getClass(), e);
        
        ProxyInvocationHandler handler = new ProxyInvocationHandler(ref.getName());
        naming.register(new ServiceDiscoListenerImpl(handler));

        if(ref.getInterfaces() == null){
          throw new FatalBeanException("'interfaces' property not set on remote bean reference: " 
              + ref.getName());
        }

        // not found remote object, returning lazy proxy
        return Proxy.newProxyInstance(
            Thread.currentThread().getContextClassLoader(), 
            ref.getInterfaces(),
            handler);
      }
    }
    return null;
  }
  
  @Override
  public Object postProcessBeforeInitialization(Object bean, String arg1)
      throws BeansException {
    return bean;
  }
  
  ////////////////// INNER CLASSES ///////////////////
  
  class ServiceDiscoListenerImpl implements ServiceDiscoListener{ 
    
    private ProxyInvocationHandler handler;
    
    public ServiceDiscoListenerImpl(ProxyInvocationHandler handler) {
      this.handler = handler;
    }

    public void onServiceDiscovered(ServiceDiscoveryEvent anEvent) {
      // Matching service name as-is or without the initial '/' character
      if (anEvent.getName().equals(handler.getName())
          || (anEvent.getName().charAt(0) == '/' && anEvent.getName()
              .substring(1).equals(handler.getName()))) {
        try {
          handler.setRemoteObject(anEvent.getService());
          naming.unregister(this);
        } catch (Exception e) {
          Log.error(getClass(), "Could not resolve remote service: " + handler.getName(), e);
        }
      }
    }
  }

  /**
   * Internal proxy class to handle invocations
   */
  public class ProxyInvocationHandler implements InvocationHandler {

    Object remote;
    String name;

    ProxyInvocationHandler(String name) {
      this.name = name;
    }
    
    public void setRemoteObject(Object remote) {
      this.remote = remote;
    }
    
    public String getName() {
      return name;
    }

    public Object invoke(Object proxy, Method method, Object[] args)
        throws Throwable {
      // Forward Service life-cycle calls to the proxy service
      if (remote != null) {
        try {
          Object result = method.invoke(remote, args);
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

        throw new NoSuchObjectException(name);
      }
    }
  }

}