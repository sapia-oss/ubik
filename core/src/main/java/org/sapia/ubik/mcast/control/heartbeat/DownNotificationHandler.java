package org.sapia.ubik.mcast.control.heartbeat;

import org.sapia.ubik.log.Category;
import org.sapia.ubik.log.Log;
import org.sapia.ubik.mcast.control.ControlNotification;
import org.sapia.ubik.mcast.control.ControlNotificationHandler;
import org.sapia.ubik.mcast.control.ControllerContext;

/**
 * This class holds logic for handling {@link DownNotification}s.
 * 
 * @author yduchesne
 * 
 */
public class DownNotificationHandler implements ControlNotificationHandler {

  private Category log = Log.createCategory(getClass());

  private ControllerContext context;

  /**
   * @param context
   *          the {@link ControllerContext}.
   */
  public DownNotificationHandler(ControllerContext context) {
    this.context = context;
  }

  /**
   * @param originNode
   *          the node from which the given notification has cascaded.
   * @param notification
   *          a {@link ControlNotification} - expected to be a
   *          {@link DownNotification}.
   */
  @Override
  public void handle(String originNode, ControlNotification notification) {
    DownNotification downNotif = (DownNotification) notification;
    log.info("Notified of nodes that are down: %s", downNotif.getDownNodes());

    for (String down : downNotif.getDownNodes()) {
      context.getChannelCallback().down(down);
    }
  }
}
