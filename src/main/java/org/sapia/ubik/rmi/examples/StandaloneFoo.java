package org.sapia.ubik.rmi.examples;

import java.io.File;
import java.rmi.RemoteException;

import org.sapia.ubik.rmi.Consts;
import org.sapia.ubik.rmi.server.Hub;
import org.sapia.ubik.rmi.server.stats.CsvStatDumper;
import org.sapia.ubik.taskman.TaskContext;

public class StandaloneFoo implements Foo{
  
  public Bar getBar() throws RemoteException {
    return new UbikBar();
  }
  
  public static void main(String[] args) throws Exception{
    
    try{
      System.setProperty(Consts.STATS_ENABLED, "true");
      //System.setProperty(Consts.STATS_DUMP_INTERVAL, "10");
      System.setProperty(Consts.SERVER_RESET_INTERVAL, "0");
      System.setProperty(Consts.MARSHALLING, "true");
            
      StandaloneFoo foo = new StandaloneFoo();
      
      TaskContext ctx = new TaskContext("CsvDumper", 1000*60*2);
      CsvStatDumper dumper = new CsvStatDumper(new File("etc/serverDump.csv"), Hub.getModules().getStatsCollector());
      Hub.getModules().getTaskManager().addTask(ctx, dumper);
      
      Hub.exportObject(foo, 9090);
      while(true){
        Thread.sleep(100000);
      }
    }finally{
      Hub.shutdown(30000);
    }
    
  }

}
