package org.sapia.ubik.rmi.examples;

import org.sapia.ubik.rmi.naming.remote.EmbeddableJNDIServer;

public class CustomMulticastJndiRunner {
  
  public static final String CUSTOM_MCAST_ADDRESS = "231.173.5.7";
  public static final int    CUSTOM_MCAST_PORT    = 5555;
  
  public static void main(String[] args) throws Exception{
    
    EmbeddableJNDIServer jndi = new EmbeddableJNDIServer(
        "default", 
        2099,
        CUSTOM_MCAST_ADDRESS,
        CUSTOM_MCAST_PORT);
    
    jndi.start(true);
    
    while(true){
      Thread.sleep(100000);
    }
  }

}
