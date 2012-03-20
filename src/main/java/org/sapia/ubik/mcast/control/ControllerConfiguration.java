package org.sapia.ubik.mcast.control;

import org.sapia.ubik.mcast.Defaults;

/**
 * Holds the configuration for an {@link EventChannelStateController}.
 * 
 * @author yduchesne
 *
 */
public class ControllerConfiguration {
	
	private long responseTimeout   = Defaults.DEFAULT_CONTROL_RESPONSE_TIMEOUT;
	private long heartbeatInterval = Defaults.DEFAULT_HEARTBEAT_INTERVAL;
	private long heartbeatTimeout  = Defaults.DEFAULT_HEARTBEAT_TIMEOUT;
	
	/**
	 * @return the interval (in millis) at which heartbeat requests should be sent.
	 */
	public long getHeartbeatInterval() {
    return heartbeatInterval;
  }
	
	/**
	 * Sets the heartbeat interval.
	 * 
	 * @param heartbeatInterval an interval, in millis.
	 * @see #getHeartbeatInterval()
	 */
	public void setHeartbeatInterval(long heartbeatInterval) {
    this.heartbeatInterval = heartbeatInterval;
  }
	
	/**
	 * @return the amount of time (in millis) after which a node should be deemed "down".
	 */
	public long getHeartbeatTimeout() {
    return heartbeatTimeout;
  }
	
	/**
	 * Sets the heartbeat timeout.
	 * 
	 * @param heartbeatTimeout an interval, in millis.
	 * @see #getHeartbeatTimeout()
	 */
	public void setHeartbeatTimeout(long heartbeatTimeout) {
    this.heartbeatTimeout = heartbeatTimeout;
  }
	
	/**
	 * @return the timeout (in millis) of {@link ControlResponse}s.
	 */
	public long getResponseTimeout() {
    return responseTimeout;
  }
	
	/**
	 * Sets the control response timeout.s
	 * 
	 * @param responseTimeout a response timeout, in millis.
	 */
	public void setResponseTimeout(long responseTimeout) {
    this.responseTimeout = responseTimeout;
  }

}