package org.sapia.ubik.rmi.examples.guice;

import java.io.IOException;

import org.sapia.ubik.ioc.NamingService;
import org.sapia.ubik.ioc.guice.NamingServiceImpl;
import org.sapia.ubik.ioc.guice.RemoteServiceImporter;
import org.sapia.ubik.rmi.examples.time.TimeServiceIF;
import org.sapia.ubik.util.Localhost;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;

public class GuiceTimeClient {

  public static void main(String[] args) throws IOException {
    
    final NamingService naming = new NamingServiceImpl("default")
      .setJndiHost(Localhost.getAnyLocalAddress().getHostAddress())
      .setJndiPort(1099);    
    
    Injector injector = Guice.createInjector(new AbstractModule(){
      
      @Override
      protected void configure() {
        bind(NamingService.class).toInstance(naming);        
        bind(TimeServiceIF.class).toProvider(new RemoteServiceImporter<TimeServiceIF>(TimeServiceIF.class, "services/time"));
      }
      
    });
    
    TimeServiceIF service = injector.getInstance(TimeServiceIF.class);
    System.out.println("Got time: " + service.getTime());
    
  }
}
