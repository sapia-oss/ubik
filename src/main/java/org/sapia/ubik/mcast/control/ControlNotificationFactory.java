package org.sapia.ubik.mcast.control;

import java.util.Set;

import org.sapia.ubik.mcast.control.heartbeat.DownNotification;

/**
 * A factory of {@link ControlNotification}s.
 * 
 * @author yduchesne
 *
 */
public class ControlNotificationFactory {

	/**
	 * Creates a {@link DownNotification} and returns it.
	 * @param targetedNodes the {@link Set} of identifiers corresponding to the nodes that are targeted
	 * by the notification.
	 * @param downNodes the {@link Set} of node identifiers that have been detected as being down.
	 * @return a {@link ControlNotification}.
	 */
	public static ControlNotification createDownNotification(Set<String> targetedNodes, Set<String> downNodes) {
		return new DownNotification(targetedNodes, downNodes);
	}

}
