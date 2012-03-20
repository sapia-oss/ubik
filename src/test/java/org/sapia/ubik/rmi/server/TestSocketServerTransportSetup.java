package org.sapia.ubik.rmi.server;

import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;

import org.sapia.ubik.rmi.Consts;
import org.sapia.ubik.rmi.naming.remote.EmbeddableJNDIServer;
import org.sapia.ubik.rmi.naming.remote.RemoteInitialContextFactory;

public class TestSocketServerTransportSetup {
  
  EmbeddableJNDIServer jndi;  
  Context context;
  
  public void setUp() throws Exception{
    System.setProperty(Consts.ENABLE_COLOCATED_CALLS, "false");
    jndi = new EmbeddableJNDIServer("testDomain", 1099);
    jndi.start(true);
    Thread.sleep(10000);
    Properties props = new Properties();
    props.setProperty(Consts.UBIK_DOMAIN_NAME, "testDomain");
    props.setProperty(Context.PROVIDER_URL, "ubik://localhost:1099/");
    props.setProperty(Context.INITIAL_CONTEXT_FACTORY, RemoteInitialContextFactory.class.getName());
    context = new InitialContext(props);
    
  }
  
  public void tearDown() {
    System.setProperty(Consts.ENABLE_COLOCATED_CALLS, "true");
    if(jndi != null) {
      jndi.stop();
    }
    try {
    	context.close();
    } catch (Exception e) {
    	
    }
    Hub.shutdown();
  }
  
  public Object exportObject(Object toExport) throws Exception {
    return Hub.exportObject(toExport);
  }
  
  public void bind(String name, Object remote) throws Exception {
    context.bind(name, Hub.exportObject(remote));
  }
  
  public Object lookup(String name) throws Exception {
    return context.lookup(name);
  }
  
  
}
