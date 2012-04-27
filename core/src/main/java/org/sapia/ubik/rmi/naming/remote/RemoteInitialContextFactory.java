package org.sapia.ubik.rmi.naming.remote;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.Hashtable;
import java.util.Map;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.naming.spi.InitialContextFactory;

import org.sapia.ubik.log.Log;
import org.sapia.ubik.mcast.AsyncEventListener;
import org.sapia.ubik.mcast.EventChannel;
import org.sapia.ubik.mcast.RemoteEvent;
import org.sapia.ubik.net.TCPAddress;
import org.sapia.ubik.net.Uri;
import org.sapia.ubik.net.UriSyntaxException;
import org.sapia.ubik.rmi.naming.ServiceLocator;
import org.sapia.ubik.rmi.naming.remote.discovery.ServiceDiscoListener;
import org.sapia.ubik.rmi.naming.remote.proxy.ContextResolver;
import org.sapia.ubik.rmi.naming.remote.proxy.DefaultContextResolver;
import org.sapia.ubik.rmi.naming.remote.proxy.ReliableLocalContext;
import org.sapia.ubik.util.Props;


/**
 * Implements a factory that allows to register {@link ServiceDiscoListener}s
 * that are notified when a service is bound to the JNDI servers.
 *
 * <p>
 * Usage:
 * <p>
 * <pre>
 *   Properties props = new Properties();
 *
 *   // IMPORTANT: the following line of code sets the domain name
 *   // to which this client (or rather, to which the JNDI context
 *   // thereafter acquired) belongs. Service discovery notifications
 *   // will only be received from the JNDI servers that belong to the
 *   // same domain as the thus acquired JNDI context.
 *
 *   props.setProperty("ubik.jndi.domain", "someDomain");
 *
 *   // If the above is not specified, the domain will be 'default'.
 *
 *
 *   props.setProperty(InitialContext.PROVIDER_URL, "ubik://localhost:1098/");
 *
 *   // IMPORTANT: do not set the wrong factory, otherwise the code
 *   // further below will break...
 *
 *   props.setProperty(InitialContext.INITIAL_CONTEXT_FACTORY,
 *                      RemoteInitialContextFactory.class.getName());
 *
 *   InitialContext         ctx = new InitialContext(props);
 *
 *   MyServiceDiscoveryListener listener = new MyServiceDiscoveryListener();
 *
 *   ReliableLocalContext   rctx = ReliableLocalContext.currentContext();
 *   rctx.addServiceDiscoListener(listener);
 *
 *   // IMPORTANT: from then on, the listener will be notified if a new service
 *   // is bound to the domain, for AS LONG AS THE close() METHOD IS NOT CALLED
 *   // ON THE CONTEXT!!!
 *
 *   // ... So do the following only when the acquired initial context must
 *   // be disposed of - the added listeners will not receive notifications
 *   // anymore:
 *   ctx.close();
 * </pre>
 *
 * An instance of this class instantiates an {@link EventChannel} to receive
 * notifications from the Ubik JNDI servers on the network, through UDP multicast.
 * The multicast address and port can be specified through the following properties:
 *
 * <ul>
 *   <li>ubik.rmi.naming.mcast.address
 *   <li>ubik.rmi.naming.mcast.port
 * </ul>
 *
 * These properties must be specified (and passed to an instance of this class) as
 * demonstrated below:
 *
 * <pre>
 *   Properties props = new Properties();
 *
 *   props.setProperty("ubik.rmi.naming.mcast.address", "231.173.5.7");
 *
 *   props.setProperty("ubik.rmi.naming.mcast.port", "6565");
 *
 *   props.setProperty(InitialContext.PROVIDER_URL, "ubik://localhost:1098/");
 *
 *   props.setProperty(InitialContext.INITIAL_CONTEXT_FACTORY,
 *                      ReliableInitialContextFactory.class.getName());
 *
 *   InitialContext         ctx = new InitialContext(props);
 * </pre>
 *
 * If not specified, the following are used for multicast address and port, respectively:
 *
 * <ul>
 *   <li>231.173.5.7
 *   <li>5454
 * </ul>
 *
 *
 * @see org.sapia.ubik.rmi.naming.remote.proxy.ReliableLocalContext
 *
 * @author Yanick Duchesne
 */

@SuppressWarnings(value="unchecked")
public class RemoteInitialContextFactory implements InitialContextFactory, JNDIConsts {
  private String scheme = ServiceLocator.UBIK_SCHEME;
  
  public RemoteInitialContextFactory() {
  }
  
  public RemoteInitialContextFactory(String serviceLocatorScheme) {
    scheme = serviceLocatorScheme;
  }
  
  /**
   * @see javax.naming.spi.InitialContextFactory#getInitialContext(Hashtable)
   */
  @SuppressWarnings("rawtypes")
  public Context getInitialContext(Hashtable props) throws NamingException {
    
    Props allProps = new Props().addMap((Map<String, String>)props).addSystemProperties();
    
    String url = (String) props.get(InitialContext.PROVIDER_URL);

    
    if (url == null) {
      throw new NamingException(InitialContext.PROVIDER_URL +
        " property not specified");
    }
    
    Uri uri = null;
    try{
      uri = Uri.parse(url);
    }catch(UriSyntaxException e){
      NamingException ne = new NamingException("Invalid URL:" + url);
      ne.setRootCause(e);
      throw ne;
    }
    
    uri.setScheme(scheme);
    
    String domain = allProps.getProperty(UBIK_DOMAIN_NAME, JNDIConsts.DEFAULT_DOMAIN);
    
    EventChannel ec = null;
    RemoteContext ctx = null;
    ContextResolver resolver = doGetResolver();    
    
    try{
      ec = new EventChannel(domain, allProps);
      ec.start();
    }catch(IOException e){
      NamingException ne = new NamingException("Could not start event channel");
      ne.setRootCause(e);
      throw ne;
    }
    
    try {
      ctx = doGetResolver().resolve(uri.getHost(), uri.getPort());
    } catch (RemoteException e) {
      
      Log.warning(getClass(), "Could not find JNDI server for : " + uri.toString() + "; trying to discover");
      BlockingEventListener listener = new BlockingEventListener();
      ec.registerAsyncListener(JNDI_SERVER_DISCO, listener);
      TCPAddress addr = null;
      try{
        ec.dispatch(JNDI_CLIENT_PUBLISH, "");
        
        RemoteEvent evt = listener.waitForEvent(10000);
        ec.unregisterAsyncListener(listener);
        
        if (evt == null) {
          NamingException ne = new NamingException("Could not connect to JNDI server");
          ne.setRootCause(e);
          throw ne;
        }
        
        addr = (TCPAddress) evt.getData();
        
        Log.warning(getClass(), "Discovered JNDI server at : " + addr);        
        ctx = resolver.resolve(addr);
        return new ReliableLocalContext(ec, uri.toString(), ctx, false, resolver);
      }catch(Exception e2){
        NamingException ne = new NamingException("Could not connect to remote JNDI server: " + addr);
        ne.setRootCause(e2);
        throw ne;
      }
    }
    try{
      return new ReliableLocalContext(ec, uri.toString(), ctx, true, resolver);
    }catch(IOException e){
      NamingException ne = new NamingException("Could not instantiate local context");
      ne.setRootCause(e);
      throw ne;
    }
    
  }
  
  protected ContextResolver doGetResolver(){
    return new DefaultContextResolver();
  }
  
  static final class BlockingEventListener implements AsyncEventListener {
    private RemoteEvent  _evt;
    
    BlockingEventListener() {
    }
    
    /**
     * @see org.sapia.ubik.mcast.AsyncEventListener#onAsyncEvent(RemoteEvent)
     */
    public synchronized void onAsyncEvent(RemoteEvent evt) {
      _evt = evt;
      notify();
    }
    
    public synchronized RemoteEvent waitForEvent(long timeout) {
      long start = System.currentTimeMillis();
      
      try {
        while (((System.currentTimeMillis() - start) < timeout) &&
          (_evt == null)) {
          wait(timeout);
        }
      } catch (InterruptedException e) {
        return null;
      }
      
      return _evt;
    }
  }
}
