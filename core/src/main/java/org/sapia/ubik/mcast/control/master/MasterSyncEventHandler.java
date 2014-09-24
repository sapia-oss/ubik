package org.sapia.ubik.mcast.control.master;

import org.sapia.ubik.mcast.control.ControlEvent;
import org.sapia.ubik.mcast.control.ControlEventHandler;
import org.sapia.ubik.mcast.control.ControllerContext;
import org.sapia.ubik.net.ServerAddress;

/**
 * Handles {@link MasterSyncEvent}s.
 * 
 * @author yduchesne
 *
 */
public class MasterSyncEventHandler implements ControlEventHandler {
  
  private ControllerContext context;
  
  public MasterSyncEventHandler(ControllerContext context) {
    this.context = context;
  }
  
  @Override
  public void handle(String originNode, ServerAddress originAddress,
      ControlEvent event) {
    MasterSyncEvent sync = (MasterSyncEvent) event;
    context.getChannelCallback().updateView(sync.getView());
  }

}
