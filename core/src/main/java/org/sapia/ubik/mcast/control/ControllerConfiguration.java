package org.sapia.ubik.mcast.control;

import org.sapia.ubik.mcast.Defaults;
import org.sapia.ubik.util.TimeValue;

/**
 * Holds the configuration for an {@link EventChannelController}.
 *
 * @author yduchesne
 *
 */
public class ControllerConfiguration {

  private TimeValue heartbeatTimeout           = Defaults.DEFAULT_HEARTBEAT_TIMEOUT;
  private TimeValue healthCheckDelegateTimeout = Defaults.DEFAULT_HEALTCHCHECK_DELEGATE_TIMEOUT;
  private int       healthCheckDelegateCount   = Defaults.DEFAULT_HEALTCHCHECK_DELEGATE_COUNT;
  private TimeValue gossipInterval             = Defaults.DEFAULT_GOSSIP_INTERVAL;
  private int       gossipNodeCount            = Defaults.DEFAULT_GOSSIP_NODE_COUNT;
  
  private boolean gossipEnabled = true;
  
  public void setHeartbeatTimeout(TimeValue heartbeatTimeout) {
    this.heartbeatTimeout = heartbeatTimeout;
  }
  
  public TimeValue getHeartbeatTimeout() {
    return heartbeatTimeout;
  }

  public void setGossipInterval(TimeValue gossipInterval) {
    this.gossipInterval = gossipInterval;
  }
  
  public TimeValue getGossipInterval() {
    return gossipInterval;
  }
  
  public void setHealthCheckDelegateCount(int healthCheckDelegateCount) {
    this.healthCheckDelegateCount = healthCheckDelegateCount;
  }
  
  public int getHealthCheckDelegateCount() {
    return healthCheckDelegateCount;
  }
  
  public void setHealthCheckDelegateTimeout(TimeValue healthCheckDelegateTimeout) {
    this.healthCheckDelegateTimeout = healthCheckDelegateTimeout;
  }
  
  public TimeValue getHealthCheckDelegateTimeout() {
    return healthCheckDelegateTimeout;
  }
  
  public void setGossipEnabled(boolean gossipEnabled) {
    this.gossipEnabled = gossipEnabled;
  }
  
  public boolean isGossipEnabled() {
    return gossipEnabled;
  }
  
  public void setGossipNodeCount(int gossipNodeCount) {
    this.gossipNodeCount = gossipNodeCount;
  }
  
  public int getGossipNodeCount() {
    return gossipNodeCount;
  }
  
}