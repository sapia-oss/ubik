package org.sapia.ubik.rmi.examples.nio;

import org.sapia.ubik.rmi.examples.Bar;
import org.sapia.ubik.rmi.examples.Foo;
import org.sapia.ubik.rmi.server.Hub;
import org.sapia.ubik.rmi.server.transport.mina.NioAddress;
import org.sapia.ubik.util.Localhost;

public class NioFooClient {

  public static void main(String[] args) {
    try {
      
      Foo foo = (Foo) Hub.connect(
        new NioAddress(Localhost.getAnyLocalAddress().getHostAddress(), 6060)
      );
      
      long start = System.currentTimeMillis();
      
      Bar bar  = foo.getBar();
      
      for(int i = 0; i < 200; i++){
        bar.getMsg();
      }
      
      long end = System.currentTimeMillis();
      
      System.out.println("Took: " + ( ((double)end - (double)start)/1000 ) + " secs.");
      
    } catch (Throwable t) {
      t.printStackTrace();
    }
  }
}
