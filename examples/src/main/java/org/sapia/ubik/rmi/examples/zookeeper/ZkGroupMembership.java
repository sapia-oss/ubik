package org.sapia.ubik.rmi.examples.zookeeper;

import java.util.UUID;

import org.sapia.ubik.log.Log;
import org.sapia.ubik.mcast.group.GroupMembershipListener;
import org.sapia.ubik.mcast.group.GroupMembershipService;
import org.sapia.ubik.mcast.group.GroupMembershipServiceFactory;
import org.sapia.ubik.mcast.zookeeper.ZkGroupMembershipService;
import org.sapia.ubik.rmi.Consts;
import org.sapia.ubik.util.Conf;

public class ZkGroupMembership {
  
  private ZkGroupMembership() {
  }

  public static void main(String[] args) throws Exception {
    Log.setDebug();
    Conf config = Conf.newInstance()
      .addProperties(Consts.UNICAST_PROVIDER, Consts.UNICAST_PROVIDER_TCP_NIO)
      .addProperties(Consts.GROUP_MEMBERSHIP_PROVIDER, Consts.GROUP_MEMBERSHIP_PROVIDER_ZOOKEEPER)
      .addProperties(ZkGroupMembershipService.SERVER_LIST, "localhost:2181");
    final GroupMembershipService svc = GroupMembershipServiceFactory.createGroupMemberShipService(config);
    svc.start();
    svc.joinGroup("test-group", UUID.randomUUID().toString(), "payload-1".getBytes(), new GroupMembershipListener() {
      
      @Override
      public void onMemberLeft(String memberId) {
        System.out.println("Member left:" + memberId);
      }
      
      @Override
      public void onMemberDiscovered(String memberId, byte[] payload) {
        System.out.println("Member joined:" + memberId + ". Payload = " + new String(payload));
      }
    });
    
   Runtime.getRuntime().addShutdownHook(new Thread() {
     @Override
    public void run() {
       svc.close();
    }
   });
    
    while(true) {
      Thread.sleep(100000);
    }
    
  }
}
