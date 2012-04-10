package org.sapia.ubik.mcast.control;

/**
 * A handler of {@link ControlRequest}s. 
 * 
 * @author yduchesne
 *
 */
public interface ControlRequestHandler {
	
	/**
	 * Handles the given request.
	 * 
	 * @param originNode the node from which the request originates.
	 * @param request a {@link ControlRequest}.
	 */
	public void handle(String originNode, ControlRequest request);
	
}