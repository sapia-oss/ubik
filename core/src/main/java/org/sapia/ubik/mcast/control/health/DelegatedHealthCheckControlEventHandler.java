package org.sapia.ubik.mcast.control.health;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.sapia.ubik.log.Category;
import org.sapia.ubik.log.Log;
import org.sapia.ubik.mcast.control.ControlEvent;
import org.sapia.ubik.mcast.control.ControlEventHandler;
import org.sapia.ubik.mcast.control.ControllerContext;
import org.sapia.ubik.mcast.control.SynchronousControlResponse;
import org.sapia.ubik.net.ServerAddress;
import org.sapia.ubik.util.Collects;
import org.sapia.ubik.util.SysClock;

/**
 * Handles {@link DelegatedHealthCheckControlEvent}s, which are sent by nodes that need performing
 * health checks on suspect nodes. This delegation mechanism ensures that network issues at the origin
 * nodes won't prevent health-checking of the suspect nodes.
 * <p>
 * An instance of this class will send a {@link HealthCheckConfirmationControlEvent} for any health incoming event,
 * indicating the output of testing the suspect node.
 *
 */
public class DelegatedHealthCheckControlEventHandler implements ControlEventHandler {
  
  private static final Map<String, CachedResponse> CACHED_RESPONSES_BY_NODE = new HashMap<>();
  private static final long RESPONSE_EXPIRATION_DURATION_MILLIS = 10000L;
  private static final long CACHE_CLEANUP_MIN_INTERVAL_MILLIS = 5000L;
  private static long nextCacheCleanupTimestamp = 0;
  
  private Category log = Log.createCategory(getClass());
  
  private ControllerContext context;
  
  public DelegatedHealthCheckControlEventHandler(ControllerContext context) {
    this.context = context;
  }
  
  @Override
  public void handle(String originNode, ServerAddress originAddress, ControlEvent event) {
    DelegatedHealthCheckControlEvent healtchCheckEvent = (DelegatedHealthCheckControlEvent) event;
    log.info("Received event for suspect node: %s", healtchCheckEvent.getTarget());
    context.getMetrics().incrementCounter("eventController.onDelegatedHealthCheck");
    context.getEventChannel().heartbeat(originNode, originAddress);
    String suspectedTargetNode = healtchCheckEvent.getTarget().getNode();
    try {

      boolean cachedResponseFound = false;
      CachedResponse cachedResponse = null;
      synchronized (CACHED_RESPONSES_BY_NODE) {
        if (CACHED_RESPONSES_BY_NODE.containsKey(suspectedTargetNode)) {
          cachedResponse = CACHED_RESPONSES_BY_NODE.get(suspectedTargetNode);
          cachedResponseFound = true;
        } else {
          cachedResponse = new CachedResponse();
          cachedResponse.futureResponses = new CompletableFuture<>();
          CACHED_RESPONSES_BY_NODE.put(suspectedTargetNode, cachedResponse);
        }
      }
      
      Set<SynchronousControlResponse> responses = null;
      if (cachedResponseFound) {
        responses = cachedResponse.futureResponses.get(
            context.getConfig().getHealthCheckDelegateTimeout().getValueInMillis(),
            TimeUnit.MILLISECONDS);
        context.getMetrics().incrementCounter("eventController.onDelegatedHealthCheck.cachedResponse");
      } else {
        try {
          responses = context.getEventChannel().sendSynchronousRequest(
              Collects.arrayToSet(healtchCheckEvent.getTarget().getNode()), 
              new SynchronousHealthCheckRequest(), 
              context.getConfig().getHealthCheckDelegateTimeout());
          context.getMetrics().incrementCounter("eventController.onDelegatedHealthCheck.syncCall");
          cachedResponse.futureResponses.complete(responses);
        } catch (Exception e) {
          log.error("Unexpected error caught during delegated health check of %s (%s)]", healtchCheckEvent.getTarget(), e.getMessage());
          cachedResponse.futureResponses.complete(new HashSet<>());
        } finally {
          cachedResponse.expirationTimestamp = RESPONSE_EXPIRATION_DURATION_MILLIS + context.getClock().currentTimeMillis();
        }
      }
      
      if (responses == null || responses.isEmpty()) {
        log.info("Received no response for health check on %s. Sending negative confirmation", healtchCheckEvent.getTarget());
        context.getEventChannel().sendUnicastEvent(
            originAddress, 
            new HealthCheckConfirmationControlEvent(
                healtchCheckEvent.getTarget(), 
                false));
      } else {
        log.info("Health check successful for %s. Sending positive confirmation", healtchCheckEvent.getTarget());
        context.getEventChannel().sendUnicastEvent(
            originAddress, 
            new HealthCheckConfirmationControlEvent(
                healtchCheckEvent.getTarget(), 
                true));
      }
    } catch (Exception e) {
      log.error("Unexpected error caught during health check of %s (%s). Sending negative confirmation", healtchCheckEvent.getTarget(), e.getMessage());
      context.getEventChannel().sendUnicastEvent(
          originAddress, 
          new HealthCheckConfirmationControlEvent(
              healtchCheckEvent.getTarget(), 
              false)
      );
    } finally {
      // Eventual cleanup of static cache via lazy in-thread strategy
      doCleanupExpiredCacheEntries();
    }
  }
  
  private void doCleanupExpiredCacheEntries() {
    synchronized (CACHED_RESPONSES_BY_NODE) {
      if (context.getClock().currentTimeMillis() > nextCacheCleanupTimestamp) {
        List<String> keys = new ArrayList<>(CACHED_RESPONSES_BY_NODE.keySet());
        for (String node: keys) {
          CachedResponse response = CACHED_RESPONSES_BY_NODE.get(node);
          if (response.isExpired(context.getClock())) {
            CACHED_RESPONSES_BY_NODE.remove(node);
          }
        }
        nextCacheCleanupTimestamp = CACHE_CLEANUP_MIN_INTERVAL_MILLIS + context.getClock().currentTimeMillis();
      }
    }
  }
  
  
  public static class CachedResponse {
    private long expirationTimestamp;
    private CompletableFuture<Set<SynchronousControlResponse>> futureResponses;
    
    public CachedResponse() {
    }

    public boolean isExpired(SysClock clock) {
      return expirationTimestamp > 0 && clock.currentTimeMillis() > expirationTimestamp;
    }
  }
}
