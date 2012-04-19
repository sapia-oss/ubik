package org.sapia.ubik.rmi.server;

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.naming.NameNotFoundException;
import javax.naming.NamingException;

import org.sapia.ubik.rmi.Consts;
import org.sapia.ubik.rmi.naming.ServiceHandler;
import org.sapia.ubik.rmi.naming.ServiceLocator;
import org.sapia.ubik.rmi.server.Hub;
import org.sapia.ubik.rmi.server.transport.memory.InMemoryAddress;
import org.sapia.ubik.rmi.server.transport.memory.InMemoryTransportProvider;

public class TestInMemoryTransportSetup {

  private Map<String, Object> services = new HashMap<String, Object>();
  
  public void setUp() {
    // making sure we're shut down
    Hub.shutdown();
    ServiceLocator.registerHandler("ubik-local", new MapServiceHandler());
    System.setProperty(Consts.COLOCATED_CALLS_ENABLED, "false");
    System.setProperty(InMemoryTransportProvider.MARSHALLING, "true");
  }
  
  public void tearDown() {
    ServiceLocator.unregisterHandler("ubik-local");
    System.setProperty(Consts.COLOCATED_CALLS_ENABLED, "true");
    System.setProperty(InMemoryTransportProvider.MARSHALLING, "false");
    Hub.shutdown();
  }

  public void bind(String name, Object object) {
    services.put(name, object);
  }
  
  public Object exportObject(Object toExport) throws RemoteException {
    Properties props = new Properties();
    props.setProperty(Consts.TRANSPORT_TYPE, InMemoryAddress.TRANSPORT_TYPE);
    return Hub.exportObject(toExport, props);
  }
  
  public Object connect() throws RemoteException {
    return Hub.connect(new InMemoryAddress(InMemoryTransportProvider.DEFAULT_SERVER_NAME));
  }
  
  private class MapServiceHandler implements ServiceHandler {
    
    @Override
    public Object handleLookup(String host, int port, String path,
        Map<String, String> attributes) throws NameNotFoundException,
        NamingException {
      
      Object toReturn = services.get(path);
      if(toReturn == null) {
        throw new NameNotFoundException(path);
      }
      return toReturn;
    }
  }
  
}
