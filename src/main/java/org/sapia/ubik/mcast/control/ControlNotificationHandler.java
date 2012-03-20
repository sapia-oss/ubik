package org.sapia.ubik.mcast.control;

/**
 * A handler of {@link ControlNotificationHandler}.
 * 
 * @author yduchesne
 *
 */
public interface ControlNotificationHandler {
	
	/**
	 * @param originNode the identifier of the node from which the notification
	 * originates.
	 * @param notif the {@link ControlNotification} to handle.
	 */
	public void handle(String originNode, ControlNotification notif);
	
}
