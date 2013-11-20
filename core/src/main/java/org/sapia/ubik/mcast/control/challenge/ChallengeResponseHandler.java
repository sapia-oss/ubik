package org.sapia.ubik.mcast.control.challenge;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import org.sapia.ubik.concurrent.NamedThreadFactory;
import org.sapia.ubik.log.Category;
import org.sapia.ubik.log.Log;
import org.sapia.ubik.mcast.EventChannel.Role;
import org.sapia.ubik.mcast.control.ControlNotificationFactory;
import org.sapia.ubik.mcast.control.ControlResponse;
import org.sapia.ubik.mcast.control.ControlResponseHandler;
import org.sapia.ubik.mcast.control.ControllerContext;
import org.sapia.ubik.mcast.control.challenge.ChallengeResponse.Code;

public class ChallengeResponseHandler implements ControlResponseHandler {

  private static final int MIN_PAUSE = 200;
  private static final int RANDOM_PAUSE = 1800;

  private Category log = Log.createCategory(getClass());
  private ControllerContext context;
  private Set<String> targetedNodes;
  private Set<String> replyingNodes = new HashSet<String>();

  public ChallengeResponseHandler(ControllerContext context, Set<String> targetedNodes) {
    this.context = context;
    this.targetedNodes = targetedNodes;
  }

  @Override
  public boolean accepts(ControlResponse response) {
    return response instanceof ChallengeResponse;
  }

  @Override
  public synchronized boolean handle(String originNode, ControlResponse response) {
    if (response instanceof ChallengeResponse) {
      ChallengeResponse challengeRs = (ChallengeResponse) response;
      log.debug("Received challenge response from %s", originNode);
      context.getChannelCallback().heartbeat(originNode, challengeRs.getUnicastAddress());
      replyingNodes.add(originNode);

      if (challengeRs.getCode() == Code.DENIED) {
        log.debug("Challenge was denied by node %s. Setting state of this node to SLAVE and aborting response handling", originNode);
        context.setRole(Role.SLAVE);
        notifyChallengeCompleted();
        return true;
      }

      log.debug("Challenge was accepted by %s", originNode);

      if (replyingNodes.size() >= targetedNodes.size()) {
        log.debug("All expected challenge responses received. Upgrading role to MASTER");
        context.setRole(Role.MASTER);
        notifyChallengeCompleted();
        return true;
      }
      log.debug("Received %s/%s responses thus far...", replyingNodes.size(), targetedNodes.size());
      return false;
    } else {
      log.debug("Expected a challenge response, got %s", response);
      return false;
    }
  }

  @Override
  public synchronized void onResponseTimeOut() {
    log.debug("Challenge response timeout detected, assuming role is MASTER");
    context.setRole(Role.MASTER);
    notifyChallengeCompleted();
  }

  private void notifyChallengeCompleted() {
    context.notifyChallengeCompleted();
    if (context.getRole() == Role.MASTER) {
      notifyOtherNodes();
    }
  }

  private void notifyOtherNodes() {
    Thread notifier = NamedThreadFactory.createWith("Ubik.EventChannel.ChallengeConfirmationNotifier").setDaemon(true).newThread(new Thread() {

      public void run() {

        try {

          Thread.sleep(MIN_PAUSE + new Random().nextInt(RANDOM_PAUSE));

          // this shouldn't happen but testing anyway
          if (context.getRole() == Role.SLAVE) {
            log.debug("Role downgraded to SLAVE");
          } else {
            context.getChannelCallback().sendNotification(
                ControlNotificationFactory.createChallengeCompletionNotification(context.getNode(), context.getChannelCallback().getNodes()));
          }
        } catch (InterruptedException e) {
          log.debug("Thread interrupted, exiting", log.noArgs());
        }
      }
    });

    notifier.start();
  }

}