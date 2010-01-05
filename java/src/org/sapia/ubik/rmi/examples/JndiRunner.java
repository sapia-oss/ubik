package org.sapia.ubik.rmi.examples;

import org.sapia.ubik.rmi.naming.remote.EmbeddableJNDIServer;

public class JndiRunner {
  
  public static void main(String[] args) throws Exception{
    
    EmbeddableJNDIServer jndi = new EmbeddableJNDIServer("default", 1099);
    jndi.start(true);
    
    while(true){
      Thread.sleep(100000);
    }
  }

}
