package org.sapia.ubik.rmi.examples.time;

import org.sapia.ubik.rmi.server.Hub;

public class AnnotatedTimeServiceClient {
  
  public static void main(String[] args) throws Exception{
    TimeServiceIF time = (TimeServiceIF)Hub.connect("localhost", 25000);
    System.out.println(time.getTime());
  }

}
