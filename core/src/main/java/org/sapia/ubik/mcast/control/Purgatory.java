package org.sapia.ubik.mcast.control;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * An instance of this class keeps nodes that have been detected as down by the
 * master.
 * <p>
 * References to such nodes are kept until a given number for "force resync"
 * events has been broadcast to each of them, telling them to resync themselves
 * with the cluster.
 * <p>
 * When nodes resync themselves with the cluster, they are put in the cluster
 * member view of each node in the cluster - and removed from the master's
 * purgatory.
 * 
 * @author yduchesne
 * 
 */
public class Purgatory {

  static class DownNode {

    private String node;
    private AtomicInteger attempts = new AtomicInteger();

    DownNode(String node) {
      this.node = node;
    }

    String getNode() {
      return node;
    }

    int getAttempts() {
      return attempts.get();
    }

    int attempt() {
      return attempts.incrementAndGet();
    }

  }

  // --------------------------------------------------------------------------

  private Map<String, DownNode> nodes = new HashMap<String, DownNode>();

  /**
   * @param node
   *          the identifier of the node to remove from the purgatory.
   * @return <code>true</code> if there was such a node identifier to remove.
   */
  public synchronized boolean remove(String node) {
    if (nodes.containsKey(node)) {
      nodes.remove(node);
      return true;
    }
    return false;
  }

  /**
   * @param node
   *          the identifier of the node to the purgatory.
   */
  public synchronized void add(String node) {
    DownNode dn = new DownNode(node);
    nodes.put(node, dn);
  }

  /**
   * @param nodes
   *          the {@link Set} of identifiers of the nodes to add to the
   *          purgatory.
   */
  public synchronized void addAll(Set<String> nodes) {
    for (String n : nodes) {
      add(n);
    }
  }

  /**
   * @param node
   *          a node identifier to test for.
   * @return <code>true</code> if this instance contains the given node.
   */
  public synchronized boolean contains(String node) {
    return nodes.containsKey(node);
  }

  /**
   * @return the number of nodes currently in the purgatory.
   */
  public synchronized int size() {
    return nodes.size();
  }

  /**
   * @return the {@link List} of {@link DownNode}s that this instance holds.
   */
  synchronized List<DownNode> getDownNodes() {
    return new ArrayList<Purgatory.DownNode>(nodes.values());
  }

  /**
   * @param maxAttempts
   *          the maximum resync attempts beyond which down nodes should be
   *          removed from this instance.
   * @return the {@link Set} of node identifiers corresponding to the nodes that
   *         have been cleared from the purgatory.
   */
  synchronized Set<String> clear(int maxAttempts) {
    Set<String> toReturn = new HashSet<String>();
    for (DownNode n : nodes.values()) {
      if (n.getAttempts() > maxAttempts) {
        nodes.remove(n.node);
        toReturn.add(n.node);
      }
    }
    return toReturn;
  }

}
