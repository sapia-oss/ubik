package org.sapia.ubik.rmi.examples;

import java.util.ArrayList;
import java.util.List;

import org.sapia.ubik.rmi.Consts;
import org.sapia.ubik.rmi.server.Hub;
import org.sapia.ubik.util.Localhost;

public class StandaloneFooClient{
  
  
  public static void main(String[] args) throws Exception{
    System.setProperty(Consts.STATS_ENABLED, "true");
    System.setProperty(Consts.SERVER_RESET_INTERVAL, "0");    
    //System.setProperty(Consts.CLIENT_GC_THRESHOLD, "200");
    System.setProperty(Consts.MARSHALLING, "true");    
    
    
    List<Worker> workers = new ArrayList<Worker>();
    for(int i = 0; i < 50; i++){
      Foo f = (Foo)Hub.connect(Localhost.getPreferredLocalAddress().getHostAddress(), 
          9090);
      
      Worker w = new Worker(i, f, 1000);
      w.setDaemon(true);
      w.start();
      workers.add(w);
    }
    
    while(true){
      Thread.sleep(10000);
    }
    
  }
  
  static class Worker extends Thread{
    
    long sleep;
    Foo foo;
    int index;
    
    Worker(int index, Foo f, long sleep){
      super("Worker-" + index);
      this.index = index;
      this.foo = f;
      this.sleep = sleep;
    }
    
    public void run() {
      while(true){
        try {
          doRun();
          Thread.sleep(sleep);
        } catch (Exception e) {
          break;
        }
      }
    }
    
    private void doRun() throws Exception{
      
      for(int i = 0; i < 200; i++){
        Bar b = foo.getBar();
        b.getMsg();
        b = null;
      }      
    }
    
  }

}
