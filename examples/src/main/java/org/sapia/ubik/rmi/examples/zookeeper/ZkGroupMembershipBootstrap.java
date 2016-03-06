package org.sapia.ubik.rmi.examples.zookeeper;

import java.util.UUID;

import org.sapia.ubik.log.Log;
import org.sapia.ubik.mcast.AsyncEventListener;
import org.sapia.ubik.mcast.GroupMembershipBootstrap;
import org.sapia.ubik.mcast.RemoteEvent;
import org.sapia.ubik.mcast.zookeeper.ZkGroupMembershipService;
import org.sapia.ubik.rmi.Consts;
import org.sapia.ubik.util.Conf;

public class ZkGroupMembershipBootstrap {
  
  public static void main(String[] args) throws Exception {
    Log.setDebug();
    String nodeId = UUID.randomUUID().toString();
    Conf config = Conf.newInstance()
      .addProperties(Consts.UNICAST_PROVIDER, Consts.UNICAST_PROVIDER_TCP_NIO)
      .addProperties(Consts.MCAST_CONTROL_SPLIT_SIZE, "2")
      .addProperties(ZkGroupMembershipService.CONNECTION_RETRY_MAX_TIME, "5s")
      .addProperties(Consts.GROUP_MEMBERSHIP_PROVIDER, Consts.GROUP_MEMBERSHIP_PROVIDER_ZOOKEEPER)
      .addProperties(ZkGroupMembershipService.SERVER_LIST, "localhost:2181");
    
    
    AsyncEventListener listener = new AsyncEventListener() {
      @Override
      public void onAsyncEvent(RemoteEvent evt) {
        try {
          System.out.println("Got event: " + evt.getType() + ", payload = " + evt.getData());
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    };
    
    final GroupMembershipBootstrap bootstrap = new GroupMembershipBootstrap("test-domain", config);
    bootstrap.startWith(new RemoteEvent("test-domain", "initial-event", "initial-payload-" + nodeId), listener);
    System.out.println("This instance is: " + bootstrap.getEventChannel().getUnicastAddress());
    bootstrap.getEventChannel().registerAsyncListener("test-event", listener);
    
    Runtime.getRuntime().addShutdownHook(new Thread() {
      @Override
      public void run() {
        bootstrap.close();
      }
    });
    
    while(true) {
      bootstrap.getEventChannel().dispatch("test-event", "test-payload-" + bootstrap.getEventChannel().getUnicastAddress());
      Thread.sleep(5000);
    }
  }

}
