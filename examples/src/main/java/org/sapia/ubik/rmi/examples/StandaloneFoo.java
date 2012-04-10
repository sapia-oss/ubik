package org.sapia.ubik.rmi.examples;

import java.rmi.RemoteException;

import org.sapia.ubik.rmi.Consts;
import org.sapia.ubik.rmi.server.Hub;

public class StandaloneFoo implements Foo{
  
  public Bar getBar() throws RemoteException {
    return new UbikBar();
  }
  
  public static void main(String[] args) throws Exception{
    
    try{
      System.setProperty(Consts.STATS_ENABLED, "true");
      System.setProperty(Consts.SERVER_RESET_INTERVAL, "0");
      System.setProperty(Consts.MARSHALLING, "true");
            
      StandaloneFoo foo = new StandaloneFoo();
      
      Hub.exportObject(foo, 9090);
      while(true){
        Thread.sleep(100000);
      }
    }finally{
      Hub.shutdown(30000);
    }
    
  }

}
