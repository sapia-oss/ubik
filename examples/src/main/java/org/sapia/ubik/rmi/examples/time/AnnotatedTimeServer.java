package org.sapia.ubik.rmi.examples.time;

import org.sapia.ubik.rmi.server.Hub;

public class AnnotatedTimeServer {

  public static void main(String[] args) throws Exception{
    AnnotatedTimeService service = new AnnotatedTimeService();

    Hub.exportObject(service, 25000);
    while(true){
      Thread.sleep(100000);
    }
  }
}
