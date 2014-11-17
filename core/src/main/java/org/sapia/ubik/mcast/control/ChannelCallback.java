package org.sapia.ubik.mcast.control;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import org.sapia.ubik.mcast.EventChannel;
import org.sapia.ubik.mcast.NodeInfo;
import org.sapia.ubik.net.ServerAddress;

/**
 * This interface is meant to abstract the {@link EventChannel} class from the
 * {@link EventChannelController}. To the controller, an instance of this class
 * represents its event channel.
 * 
 * @author yduchesne
 * 
 */
public interface ChannelCallback {

  /**
   * @return the {@link String} identifier of the node corresponding to the
   *         underlying event channel.
   */
  public String getNode();

  /**
   * @return the unicast {@link ServerAddress} of the underlying event channel.
   */
  public ServerAddress getAddress();

  /**
   * @return the {@link Set} of node identifiers corresponding to the nodes in
   *         the domain/cluster.
   */
  public Set<String> getNodes();
  
  /**
   * @return the number of nodes that the underlying {@link EventChannel} "sees".
   */
  public int getNodeCount();
  
  /**
   * @param nodes a {@link List} of {@link NodeInfo} instances corresponding to the master's view.
   */
  public void updateView(List<NodeInfo> nodes);

  /**
   * Triggers a resync with the cluster.
   */
  public void resync();

  /**
   * Sends the given {@link ControlRequest}.
   * 
   * @param req
   *          a {@link ControlRequest} to send.
   */
  public void sendRequest(ControlRequest req);

  /**
   * @param targetedNodes
   *          the set of node identifiers corresponding to the targeted nodes.
   * @param request
   *          the {@link SynchronousControlRequest} to send.
   * @return the {@link Set} of {@link SynchronousControlResponse} that are
   *         returned in response to the request.
   * @throws InterruptedException
   *           if the calling thread is interrupted while sending the request.
   * @throws IOException
   *           if an IO problem occurred while sending the request.
   */
  public Set<SynchronousControlResponse> sendSynchronousRequest(Set<String> targetedNodes, SynchronousControlRequest request)
      throws InterruptedException, IOException;

  /**
   * Sends a {@link ControlResponse} back to the master node - following a
   * corresponding request.
   * 
   * @param masterNode
   *          the {@link String} corresponding to the identifier of the master
   *          node.
   * @param res
   *          the {@link ControlResponse} to send.
   */
  public void sendResponse(String masterNode, ControlResponse res);

  /**
   * Sends the given {@link ControlNotification}.
   * 
   * @param notif
   *          a {@link ControlNotification}.
   */
  public void sendNotification(ControlNotification notif);
  
  /**
   * @param destination the {@link ServerAddress} to which to even should be sent.
   * @param event the {@link ControlEvent} to send.
   */
  public void sendUnicastEvent(ServerAddress destination, ControlEvent event);
  
  /**
   * @param event a {@link ControlEvent} to broadcast across the domain.
   */
  public void sendBroadcastEvent(ControlEvent event);
  
  /**
   * This method is meant to notify the underlying event channel that a given
   * node has provided its heartbeat.
   * 
   * @param a
   *          {@link String} corresponding to the identifier of the node that
   *          provided its heartbeat.
   * @param unicastAddress
   *          the unicast {@link ServerAddress} of the given node.
   */
  public void heartbeatResponse(String node, ServerAddress unicastAddress);
  
  /**
   * This method is meant to notify the underlying event channel that a given
   * node has received a heartbeat request from its master.
   * 
   * @param a
   *          {@link String} corresponding to the identifier of the master node 
   *          that sent the request.
   * @param unicastAddress
   *          the unicast {@link ServerAddress} of the given node.
   */
  public void heartbeatRequest(String node, ServerAddress unicastAddress);

  /**
   * This method is meant to notify the underlying event channel that a given
   * node has been detected as being down.
   * 
   * @param node
   *          a {@link String} corresponding to the identifier of the node that
   *          was detected as being down.
   */
  public void down(String node);
  
  /**
   * @param node 
   *          a {@link String} corresponding to the identifier of the node that
   *          was disovered.
   * @param addr
   *          the {@link ServerAddress} corresponding to the unicast address of the discovered node.
   * @return <code>true</code> if the given data effectively corresponds to a new node.
   */
  public boolean addNewNode(String node, ServerAddress addr);
  
  /**
   * @param node 
   *          a {@link String} corresponding to the identifier of the node to test for.
   * @return <code>true</code> if the node is known to the underlying {@link EventChannel}.
   */
  public boolean containsNode(String node);
  
  /**
   * @return the {@link List} of {@link NodeInfo} instances corresponding to the nodes that
   * the underlying {@link EventChannel} "sees".
   */
  public List<NodeInfo> getView();

  /**
   * @param targetedNodes
   *          a {@link Set} of nodes that are targeted for a forced resync.
   */
  public void forceResyncOf(Set<String> targetedNodes);

  /**
   * Triggers a "master broadcast".
   */
  public void triggerMasterBroadcast();
}
