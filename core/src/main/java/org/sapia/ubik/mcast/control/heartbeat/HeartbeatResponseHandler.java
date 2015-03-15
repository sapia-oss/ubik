package org.sapia.ubik.mcast.control.heartbeat;

import java.util.Set;

import org.sapia.ubik.mcast.NodeInfo;
import org.sapia.ubik.mcast.control.ControlResponse;
import org.sapia.ubik.mcast.control.ControlResponseHandler;
import org.sapia.ubik.mcast.control.ControllerContext;
import org.sapia.ubik.util.Collects;

/**
 * This class holds logic for handling {@link HeartbeatResponse}s.
 *
 * @see HeartbeatRequest
 * @author yduchesne
 *
 */
public class HeartbeatResponseHandler extends HeartbeatResponseHandlerSupport implements ControlResponseHandler {

  /**
   * @param context
   *          the {@link ControllerContext}
   * @param targetedNodes
   *          the identifiers of the nodes that had originally targeted by the
   *          heartbeat request.
   */
  public HeartbeatResponseHandler(ControllerContext context, Set<String> targetedNodes) {
    super(context, targetedNodes);
  }

  /**
   * @returns <code>true</code> if the given response is a
   *          {@link HeartbeatResponse}.
   */
  @Override
  public boolean accepts(ControlResponse response) {
    return response instanceof HeartbeatResponse;
  }

  /**
   * @param originNode
   *          the identifier of the node from which the response originates.
   * @param response
   *          a {@link ControlResponse}, expected to be a
   *          {@link HeartbeatResponse}.
   * @return <code>true</code> if all expected responses have been received,
   *         false otherwise.
   */
  @Override
  public synchronized boolean handle(String originNode, ControlResponse response) {
    if (response instanceof HeartbeatResponse) {
      HeartbeatResponse hr = (HeartbeatResponse) response;
      return doHandleResponse(originNode, ((HeartbeatResponse) response).getUnicastAddress(), Collects.arrayToSet(new NodeInfo(hr.getUnicastAddress(), originNode)));
    } else {
      log.debug("Expected a heartbeat response, got %s", response);
      return false;
    }
  }

  @Override
  public synchronized void onResponseTimeOut() {
    doHandleTimeout();
  }
}
