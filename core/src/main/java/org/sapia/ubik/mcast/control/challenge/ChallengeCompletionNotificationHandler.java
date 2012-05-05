package org.sapia.ubik.mcast.control.challenge;

import org.sapia.ubik.log.Category;
import org.sapia.ubik.log.Log;
import org.sapia.ubik.mcast.EventChannel.Role;
import org.sapia.ubik.mcast.control.ControlNotification;
import org.sapia.ubik.mcast.control.ControlNotificationHandler;
import org.sapia.ubik.mcast.control.ControllerContext;

public class ChallengeCompletionNotificationHandler implements ControlNotificationHandler {
  
  private Category          log      = Log.createCategory(getClass());
  private ControllerContext context;
  
  public ChallengeCompletionNotificationHandler(ControllerContext context) {
    this.context = context;
  }
  
  @Override
  public void handle(String originNode, ControlNotification notif) {
    ChallengeCompletionNotification completionNotif = (ChallengeCompletionNotification) notif;
    if (context.getRole() == Role.MASTER || context.getRole() == Role.MASTER_CANDIDATE) {
      log.debug("Received challenge completion notification from master node: ", completionNotif.getMasterNode());
      log.debug("Current node %s is currently %s. Triggering challenge", context.getNode(), context.getRole());
      context.triggerChallenge();
    } else {
      log.debug("Received challenge completion notification from master node: ", completionNotif.getMasterNode());
      context.setMasterNode(completionNotif.getMasterNode());
    }
  }
  
  

}
