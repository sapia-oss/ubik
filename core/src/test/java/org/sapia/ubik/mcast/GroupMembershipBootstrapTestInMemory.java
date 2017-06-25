package org.sapia.ubik.mcast;

import org.sapia.ubik.mcast.group.GroupMembershipService;
import org.sapia.ubik.mcast.memory.InMemoryGroupMembershipService;
import org.sapia.ubik.mcast.memory.InMemoryUnicastDispatcher;

public class GroupMembershipBootstrapTestInMemory extends GroupMembershipBootstrapTestSupport {

  @Override
  protected GroupMembershipService createGroupMembershipService() {
    return new InMemoryGroupMembershipService();
  }
  
  @Override
  protected UnicastDispatcher createUnicastDispatcher(EventConsumer consumer) {
    InMemoryUnicastDispatcher ud = new InMemoryUnicastDispatcher();
    ud.initialize(new DispatcherContext(consumer));
    return ud;
  }

  

}
