package org.sapia.ubik.ioc.spring;

import java.io.IOException;
import java.lang.ref.SoftReference;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;

import org.sapia.ubik.ioc.NamingService;
import org.sapia.ubik.mcast.EventChannel;
import org.sapia.ubik.rmi.Consts;
import org.sapia.ubik.rmi.naming.remote.RemoteInitialContextFactory;
import org.sapia.ubik.rmi.naming.remote.discovery.DiscoveryHelper;
import org.sapia.ubik.rmi.naming.remote.discovery.JndiDiscoListener;
import org.sapia.ubik.rmi.naming.remote.discovery.ServiceDiscoListener;
import org.sapia.ubik.rmi.naming.remote.proxy.ReliableLocalContext;
import org.sapia.ubik.rmi.server.Log;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

/**
 * This singleton bean acts as a bridge to the Ubik JNDI server. It is internally used by the {@link BeanExporterPostProcessor}
 * and {@link BeanImporterPostProcessor} to respectively bind and lookup remote services.
 * <p>
 * This bean robustly discovers Ubik JNDI servers that appear in the domain if none are present at initialization time.
 * The bound services are thus cached until a JNDI server is available.
 * 
 * @author yduchesne
 *
 */
public class NamingServiceBean implements NamingService, JndiDiscoListener, InitializingBean, DisposableBean{
  
  private String         _domain;
  private String         _host, _addr;
  private int            _port, _mcastPort;
  private Context        _ctx;
  private Set<Object>    _toBind = Collections.synchronizedSet(new HashSet<Object>());
  private DiscoveryHelper _helper;

  /**
   * @param host the host of the remote Ubik JNDI server.
   */
  public NamingServiceBean setJndiHost(String host) {
    _host = host;
    return this;
  }

  /**
   * @param port the port of the remote Ubik JNDI server.
   */
  public NamingServiceBean setJndiPort(int port) {
    _port = port;
    return this;
  }

  /**
   * @param domain the domain of the remote Ubik JNDI server.
   */
  public void setDomain(String domain) {
    _domain = domain;
  }
  
  /**
   * @param addr the multicast address to use for discovering remote
   * JNDI servers.
   */
  public void setMulticastAddress(String addr){
    _addr = addr;
  }

  /**
   * @param port the multicast port to use for discovering remote
   * JNDI servers.
   */
  public void setMulticastPort(int port){
    _mcastPort = port;
  }
  
  public EventChannel getEventChannel() {
    return _helper.getChannel();
  }

  public synchronized void bind(String name, Object o) throws NamingException {
    if(_ctx == null){
      _toBind.add(new Binding(name, o));
      return;
    }
    if(Log.isInfo()){
      Log.info(getClass(), "Binding: " + name + ", " + o + " - to context: " + _ctx);
    }
    _ctx.bind(name, o);
  }

  public synchronized Object lookup(String name) throws NamingException,
      NameNotFoundException {
    if(_ctx == null){
      throw new NamingException("No connection to JNDI");
    }
    return _ctx.lookup(name);
  }
  
  public void register(ServiceDiscoListener listener){
    if(_helper != null){
      _helper.addServiceDiscoListener(listener);
    }
  }
  
  public void unregister(ServiceDiscoListener listener){
    if(_helper != null){
      _helper.removeServiceDiscoListener(listener);
    }    
  }
  
  @Override
  public void destroy() throws Exception {
    try {
      if(_ctx != null)
        _ctx.close();
      if(_helper != null)
        _helper.close();
    } catch(NamingException e) {
      //noop
    }
  }
  
  @Override
  public void afterPropertiesSet() throws Exception {
    if(_port == 0){
      _port = 1099;
    }
    if(_host == null){
      _host = "localhost";
    }
    if(_mcastPort == 0){
      _mcastPort = Consts.DEFAULT_MCAST_PORT;
    }
    if(_addr == null){
      _addr = Consts.DEFAULT_MCAST_ADDR;
    }
    try{
      _ctx = getInitialContext(_domain, _host, _port);      
      Log.info(getClass(), "Got JNDI initial context");
      EventChannel channel = ReliableLocalContext.currentContext().getEventChannel();
      if(channel != null){
        _helper = getDiscoHelper(channel);
        Log.info(getClass(), "Got discovery helper");
      }
    }catch(NamingException e){
      Log.info(getClass(), "Could not get JNDI initial context for (domain:host:port):" + 
        _domain + ":" + _host + ":" + _port);
      _helper = getDiscoHelper(_domain, _addr, _mcastPort);
      _helper.addJndiDiscoListener(this);
    }
  }

  public void onJndiDiscovered(Context context) {
    Log.info(getClass(), "Discovered JNDI");
    _ctx = context;
    _helper.removeJndiDiscoListener(this);
    synchronized(this){
      Iterator<Object> entries = _toBind.iterator();
      while(entries.hasNext()){
        Binding entry = (Binding)entries.next();
        if(entry.getObject() != null){
          try{
            _ctx.bind(entry.getName(), entry.getObject());
            entries.remove();
          }catch(NamingException e){}
        }
      }
    }
  }
  
  protected DiscoveryHelper getDiscoHelper(String domain, 
                                           String mcastHost, 
                                           int mcastPort) throws IOException{
    return new DiscoveryHelper(domain, mcastHost, mcastPort);
  }
  
  protected DiscoveryHelper getDiscoHelper(EventChannel channel) throws IOException{
    return new DiscoveryHelper(channel);
  }  
  
  protected InitialContext getInitialContext(String domain, String host, int port)
    throws NamingException{
    Properties props = new Properties();
    props.setProperty(Context.INITIAL_CONTEXT_FACTORY,
        RemoteInitialContextFactory.class.getName());
    props.setProperty(RemoteInitialContextFactory.UBIK_DOMAIN_NAME, _domain);
    props.setProperty(Context.PROVIDER_URL, "ubik://" + _host + ":" + _port);
    return new InitialContext(props);          
  }
  
  static class Binding{
    SoftReference<Object> _object;
    String _name;
    Binding(String name, Object o){
      _name = name;
      _object = new SoftReference<Object>(o);
    }
    Object getObject(){
      return _object.get();
    }
    String getName(){
      return _name;
    }
  }  

}
