package org.sapia.ubik.rmi.examples.guice;

import org.sapia.ubik.ioc.guice.NamingService;
import org.sapia.ubik.ioc.guice.NamingServiceImpl;
import org.sapia.ubik.ioc.guice.RemoteServiceExporter;
import org.sapia.ubik.rmi.examples.time.TimeServiceIF;
import org.sapia.ubik.rmi.examples.time.TimeServiceImpl;
import org.sapia.ubik.rmi.server.Log;
import org.sapia.ubik.util.Localhost;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;

public class GuiceTimeServer {

  
  public static void main(String[] args) throws Exception{
    
    final NamingService naming = new NamingServiceImpl("default")
      .setJndiHost(Localhost.getLocalAddress().getHostAddress())
      .setJndiPort(1099);    
    
    Injector injector = Guice.createInjector(new AbstractModule(){
      
      @Override
      protected void configure() {
        bind(NamingService.class).toInstance(naming);
        bind(TimeServiceIF.class).toProvider(new RemoteServiceExporter<TimeServiceIF>(new TimeServiceImpl(), "services/time"));
      }
      
    });
    
    // calling getInstance() internally invokes get() on the RemoteServiceExporter, which publishes the service
    // to the JNDI.
    TimeServiceIF server = injector.getInstance(TimeServiceIF.class);
    
    System.out.println("Bound time server");
    while(true){
      Thread.sleep(10000);
    }
    
  }
}
