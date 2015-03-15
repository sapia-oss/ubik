package org.sapia.ubik.mcast.control;

import org.sapia.ubik.mcast.Defaults;
import org.sapia.ubik.rmi.Consts;
import org.sapia.ubik.util.TimeRange;
import org.sapia.ubik.util.TimeValue;

/**
 * Holds the configuration for an {@link EventChannelController}.
 *
 * @author yduchesne
 *
 */
public class ControllerConfiguration {

  private long responseTimeout = Defaults.DEFAULT_CONTROL_RESPONSE_TIMEOUT.getValueInMillis();
  private long heartbeatInterval = Defaults.DEFAULT_HEARTBEAT_INTERVAL.getValueInMillis();
  private long heartbeatTimeout = Defaults.DEFAULT_HEARTBEAT_TIMEOUT.getValueInMillis();
  private TimeRange heartbeatResponseDelay = Defaults.DEFAULT_HEARTBEAT_RESPONSE_DELAY;

  
  private long resyncInterval = Defaults.DEFAULT_RESYNC_INTERVAL.getValueInMillis();
  private long resyncNodeCount = Defaults.DEFAULT_RESYNC_NODE_COUNT;
  private int forceResyncBatchSize = Defaults.DEFAULT_FORCE_RESYNC_BATCH_SIZE;
  private int forceResyncAttempts = Defaults.DEFAULT_FORCE_RESYNC_ATTEMPTS;
  private long masterBroadcastInterval = Defaults.DEFAULT_MASTER_BROADCAST_INTERVAL.getValueInMillis();
  private int syncHeartbeatMessageSplitSize = Defaults.DEFAULT_SYNC_HEARTBEAT_SPLIT_SIZE;
  private boolean syncHeartBeatEnabled = Defaults.DEFAULT_SYNC_HEARTBEAT_ENABLED;
  private TimeValue syncHeartBeatResponseTimeout = Defaults.DEFAULT_SYNC_HEARTBEAT_RESPONSE_TIMEOUT;
  private TimeValue syncResponseTimeout = Defaults.DEFAULT_SYNC_RESPONSE_TIMEOUT;
  private int maxPingAttempts = Defaults.DEFAULT_PING_ATTEMPTS;
  private TimeValue pingInterval = Defaults.DEFAULT_PING_INTERVAL;
  private boolean pingDisabled = false;
  private boolean forceResync = true;
  private boolean ignoreHeartbeatRequests = false;
 
  /**
   * Sets the maximum number of ping attempts.
   * 
   * @param maxPingAttempts the number of ping attempts to set.
   * 
   * @see Consts#MCAST_MAX_PING_ATTEMPTS
   */
  public void setMaxPingAttempts(int maxPingAttempts) {
    this.maxPingAttempts = maxPingAttempts;
  }
  
  /**
   * @return the max number of ping attempts.
   */
  public int getMaxPingAttempts() {
    return maxPingAttempts;
  }
  
  /**
   * Sets the "forced resync" flag.
   * 
   * @param forceResync <code>true</code> if forced resync is enabled.
   * 
   * @see Consts#MCAST_HEARTBEAT_FORCE_RESYNC
   */
  public void setForceResync(boolean forceResync) {
    this.forceResync = forceResync;
  }
  
  /**
   * @return <code>true</code> if forced resync is enabled.
   */
  public boolean isForceResync() {
    return forceResync;
  }

  /**
   * Sets the ping interval time.
   * 
   * @param pingInterval the {@link TimeValue} to use as a ping interval.
   * 
   * @see Consts#MCAST_PING_INTERVAL
   */
  public void setPingInterval(TimeValue pingInterval) {
    this.pingInterval = pingInterval;
  }
  
  /**
   * @return the ping interval time.
   */
  public TimeValue getPingInterval() {
    return pingInterval;
  }
  
  /**
   * Indicates if ping is disabled or not (false by default). THIS SHOULD BE USED FOR TESTING ONLY.
   * 
   * @param pingDisabled the flag indicating if ping is disabled or not.
   * 
   * @see Consts#MCAST_PING_DISABLED
   */
  public void setPingDisabled(boolean pingDisabled) {
    this.pingDisabled = pingDisabled;
  }

  /**
   * @return the flag indicating if ping is disabled or not.
   */
  public boolean isPingDisabled() {
    return pingDisabled;
  }
  
  /**
   * Sets the heartbeat interval.
   *
   * @param heartbeatInterval
   *          an interval, in millis.
   * @see Consts#MCAST_HEARTBEAT_INTERVAL
   */
  public void setHeartbeatInterval(long heartbeatInterval) {
    this.heartbeatInterval = heartbeatInterval;
  }

  /**
   * @return the interval (in millis) at which heartbeat requests should be
   *         sent.
   */
  public long getHeartbeatInterval() {
    return heartbeatInterval;
  }

  /**
   * Sets the heartbeat timeout.
   *
   * @param heartbeatTimeout
   *          an interval, in millis.
   * @see Consts#MCAST_HEARTBEAT_TIMEOUT
   */
  public void setHeartbeatTimeout(long heartbeatTimeout) {
    this.heartbeatTimeout = heartbeatTimeout;
  }

  /**
   * @return the amount of time (in millis) after which a node should be deemed
   *         "down".
   */
  public long getHeartbeatTimeout() {
    return heartbeatTimeout;
  }
  
  /**
   * Sets the heartbeat response delay.
   *
   * @param heartbeatResponseDelay
   *          an interval, in millis.
   * @see Consts#MCAST_HEARTBEAT_RESPONSE_DELAY
   */
  public void setHeartbeatResponseDelay(TimeRange heartbeatResponseDelay) {
    this.heartbeatResponseDelay = heartbeatResponseDelay;
  }
  
  /**
   * @return the amount of time to wait for, befored sending back a heartbeat response.
   */
  public TimeRange getHeartbeatResponseDelay() {
    return heartbeatResponseDelay;
  }

  /**
   * Sets the control response timeout.
   *
   * @param responseTimeout
   *          a response timeout, in millis.
   * @see Consts#MCAST_CONTROL_RESPONSE_TIMEOUT
   */
  public void setResponseTimeout(long responseTimeout) {
    this.responseTimeout = responseTimeout;
  }

  /**
   * @return the timeout (in millis) of {@link ControlResponse}s.
   */
  public long getResponseTimeout() {
    return responseTimeout;
  }

  /**
   * @param resyncInterval
   *          a resync interval, in millis.
   * @see Consts#MCAST_RESYNC_INTERVAL
   */
  public void setResyncInterval(long resyncInterval) {
    this.resyncInterval = resyncInterval;
  }

  /**
   * @return the resync interval, in millis.
   */
  public long getResyncInterval() {
    return resyncInterval;
  }

  /**
   * @param resyncNodeCount
   *          a node count.
   * @see Consts#MCAST_RESYNC_NODE_COUNT
   */
  public void setResyncNodeCount(long resyncNodeCount) {
    this.resyncNodeCount = resyncNodeCount;
  }

  /**
   * @return the resync node count.
   */
  public long getResyncNodeCount() {
    return resyncNodeCount;
  }

  /**
   * @param forceResyncBatchSize
   *          a batch size.
   *
   * @see Consts#MCAST_HEARTBEAT_FORCE_RESYNC_BATCH_SIZE
   */
  public void setForceResyncBatchSize(int forceResyncBatchSize) {
    this.forceResyncBatchSize = forceResyncBatchSize;
  }

  /**
   * @return the force-resync batch size.
   */
  public int getForceResyncBatchSize() {
    return forceResyncBatchSize;
  }

  /**
   * @param attempts
   *          the maximum number of force-resync attempts.
   *
   * @see Consts#MCAST_HEARTBEAT_FORCE_RESYNC_ATTEMPTS
   */
  public void setForceResyncAttempts(int attempts) {
    this.forceResyncAttempts = attempts;
  }

  /**
   * @return the maximum number of force-resync attempts.
   */
  public int getForceResyncAttempts() {
    return this.forceResyncAttempts;
  }

  /**
   * @param masterBroadcastInterval
   *          the master broadcast interval.
   *
   * @see Consts#MCAST_MASTER_BROADCAST_INTERVAL
   */
  public void setMasterBroadcastInterval(long masterBroadcastInterval) {
    this.masterBroadcastInterval = masterBroadcastInterval;
  }

  public long getMasterBroadcastInterval() {
    return masterBroadcastInterval;
  }
  
  /**
   * @param controlMessageSplitSize the number of copies into which control messages should be split.
   * 
   * @see Consts#MCAST_CONTROL_SYNC_HEARTBEAT_SPLIT_SIZE
   */
  public void setHeartbeatControllMessageSplitSize(int controlMessageSplitSize) {
    this.syncHeartbeatMessageSplitSize = controlMessageSplitSize;
  }
  
  /**
   * @return  the number of copies into which control messages should be split.
   */
  public int getHeartbeatControlMessageSplitSize() {
    return syncHeartbeatMessageSplitSize;
  }
  
  /**
   * @param syncHeartBeatEnabled <code>true</code> if synchronous heartbeats should be enabled.
   */
  public void setSyncHeartBeatEnabled(boolean syncHeartBeatEnabled) {
    this.syncHeartBeatEnabled = syncHeartBeatEnabled;
  }
  
  /**
   * @return <code>true</code> if synchronous heartbeats should be enabled.
   */
  public boolean isSyncHeartBeatEnabled() {
    return syncHeartBeatEnabled;
  }
  
  /**
   * @param ignore sets if heartbeat requests should be enabled or not.
   */
  public void setIgnoreHeartbeatRequests(boolean ignore) {
    this.ignoreHeartbeatRequests = ignore;
  }
  
  /**
   * @return <code>true</code> if heartbeat requests should be ignored or not.
   */
  public boolean isIgnoreHeartbeatRequests() {
    return ignoreHeartbeatRequests;
  }

  /**
   * @param syncHeartBeatResponseTimeout the timeout to observe when blocking
   * on synchronous heartbeat responses.
   * 
   * @see Consts#MCAST_CONTROL_SYNC_HEARTBEAT_RESPONSE_TIMEOUT
   */
  public void setSyncHeartBeatResponseTimeout(
      TimeValue syncHeartBeatResponseTimeout) {
    this.syncHeartBeatResponseTimeout = syncHeartBeatResponseTimeout;
  }
  
  /**
   * @return the timeout to observe when blocking on synchronous heartbeat responses.
   */
  public TimeValue getSyncHeartBeatResponseTimeout() {
    return syncHeartBeatResponseTimeout;
  }
  
  /**
   * @param syncResponseTimeout the timeout to observe when blocking on synchronous
   * remote event responses.
   * 
   * @see Consts#MCAST_SYNC_RESPONSE_TIMEOUT
   */
  public void setSyncResponseTimeout(TimeValue syncResponseTimeout) {
    this.syncResponseTimeout = syncResponseTimeout;
  }

  /**
   * @return the timeout to observe when blocking on synchronous
   * remote event responses.
   */
  public TimeValue getSyncResponseTimeout() {
    return syncResponseTimeout;
  }
  
  
}