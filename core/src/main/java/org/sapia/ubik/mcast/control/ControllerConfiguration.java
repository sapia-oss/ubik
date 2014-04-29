package org.sapia.ubik.mcast.control;

import org.sapia.ubik.mcast.Defaults;
import org.sapia.ubik.rmi.Consts;

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
  private long resyncInterval = Defaults.DEFAULT_RESYNC_INTERVAL.getValueInMillis();
  private long resyncNodeCount = Defaults.DEFAULT_RESYNC_NODE_COUNT;
  private int forceResyncBatchSize = Defaults.DEFAULT_FORCE_RESYNC_BATCH_SIZE;
  private int forceResyncAttempts = Defaults.DEFAULT_FORCE_RESYNC_ATTEMPTS;
  private boolean masterBroadcastEnabled = true;
  private long masterBroadcastInterval = Defaults.DEFAULT_MASTER_BROADCAST_INTERVAL.getValueInMillis();

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
   * @param masterBroadcastEnabled
   *          indicates if master broadcast should be enabled or not.
   *
   * @see Consts#MCAST_MASTER_BROADCAST_ENABLED
   */
  public void setMasterBroadcastEnabled(boolean masterBroadcastEnabled) {
    this.masterBroadcastEnabled = masterBroadcastEnabled;
  }

  public boolean isMasterBroadcastEnabled() {
    return masterBroadcastEnabled;
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

}