package org.sapia.ubik.mcast.control;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Future;

import org.sapia.ubik.mcast.EventChannel;
import org.sapia.ubik.mcast.NodeInfo;
import org.sapia.ubik.net.ServerAddress;
import org.sapia.ubik.util.Condition;
import org.sapia.ubik.util.TimeValue;

/**
 * This interface is meant to abstract the {@link EventChannel} class from the
 * {@link EventChannelController}. To the controller, an instance of this class
 * represents its event channel.
 * 
 * @author yduchesne
 * 
 */
public interface EventChannelFacade {

  /**
   * @param node the identifier of the {@link NodeInfo} to return.
   * @return the corresponding {@link NodeInfo}, or <code>null</code> if no such instance exists.
   */
  public NodeInfo getNodeInfoFor(String node);
  
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
   * Triggers a resync with the cluster.
   */
  public void resync();

  /**
   * @param targetedNodes
   *          the set of node identifiers corresponding to the targeted nodes.
   * @param request
   *          the {@link SynchronousControlRequest} to send.
   * @param timeout
   *          the maximum time to block waiting for responses.
   * @return the {@link Set} of {@link SynchronousControlResponse}s that are
   *         returned in response to the request.
   * @throws InterruptedException
   *           if the calling thread is interrupted while sending the request.
   * @throws IOException
   *           if an IO problem occurred while sending the request.
   */
  public Set<SynchronousControlResponse> sendSynchronousRequest(Set<String> targetedNodes, SynchronousControlRequest request, TimeValue timeout)
      throws InterruptedException, IOException;
  
  /**
   * @param targetedNodes 
   *          the array of node identifiers corresponding to the nodes to target.
   * @param requests 
   *          the {@link SynchronousControlRequest}s to send to each node, respectively.
   * @param timeout
   *          the maximum time to block waiting for responses.
   * @return the {@link Set} of {@link SynchronousControlResponse}s that are returned in response to the requests.
   * @throws InterruptedException
   *           if the calling thread is interrupted while sending the request.
   * @throws IOException
   *           if an IO problem occurred while sending the request.   
   */
  public Set<SynchronousControlResponse> sendSynchronousRequests(String[] targetedNodes, SynchronousControlRequest[] requests, TimeValue timeout)
      throws InterruptedException, IOException;

  /**
   * Sends the given {@link ControlNotification}.
   * 
   * @param notif
   *          a {@link ControlNotification}.
   */
  public void sendNotification(ControlNotification notif);
  
  /**
   * Sends the given {@link GossipNotification}.
   * 
   * @param notif
   *          a {@link ControlNotification}.
   */
  public void sendGossipNotification(GossipNotification notif);
  
  /**
   * @param destination the {@link ServerAddress} to which to even should be sent.
   * @param event the {@link ControlEvent} to send.
   */
  public Future<Void> sendUnicastEvent(ServerAddress destination, ControlEvent event);
  
  /**
   * @param event a {@link ControlEvent} to broadcast across the domain.
   */
  public Future<Void> sendBroadcastEvent(ControlEvent event);
  
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
  public void heartbeat(String node, ServerAddress unicastAddress);

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
   * Method to request curation of the dead nodes list of this event channel.
   */
  public void cleanDeadNodes(long gracePeriodMillis);
  
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
   * @param filter a {@link Condition} to use as filter.
   * @return the filter {@link List} of {@link NodeInfo} instances corresponding to the nodes that
   * the underlying {@link EventChannel} "sees".
   */
  public List<NodeInfo> getView(Condition<NodeInfo> filter);

}
