package org.sapia.ubik.rmi.server.invocation;

import org.sapia.ubik.log.Category;
import org.sapia.ubik.log.Log;
import org.sapia.ubik.module.ModuleContext;
import org.sapia.ubik.rmi.Consts;
import org.sapia.ubik.rmi.server.VmId;
import org.sapia.ubik.rmi.server.invocation.InvocationDispatcher.InvocationStrategy;
import org.sapia.ubik.util.Props;

class InvocationStrategyFactory {
  
  private Category                    log                    = Log.createCategory(getClass());
  private boolean                     supportsColocatedCalls = true;
  private ColocatedInvocationStrategy colocated;
  private RemoteInvocationStrategy    remote;

  InvocationStrategyFactory(ModuleContext modules) {
    colocated = new ColocatedInvocationStrategy();
    remote    = new RemoteInvocationStrategy();
    colocated.init(modules);
    remote.init(modules);
    
    supportsColocatedCalls = Props.getSystemProperties().getBooleanProperty(Consts.COLOCATED_CALLS_ENABLED, true);
    log.debug("Supports colocated calls: %s", Boolean.toString(supportsColocatedCalls));
  }
  
  InvocationStrategy getInvocationStrategy(VmId clientVmId) {
    if(VmId.getInstance().equals(clientVmId) && supportsColocatedCalls) {
      return colocated;
    } else {
      return remote;
    }   
  }

}
