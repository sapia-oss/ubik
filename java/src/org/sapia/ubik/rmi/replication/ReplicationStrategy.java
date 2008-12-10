package org.sapia.ubik.rmi.replication;

import org.sapia.ubik.net.ServerAddress;

import java.util.Set;


/**
 * This class implements the logic that selects the next server to which a replicated command
 * should be dispatched.
 *
 * @author Yanick Duchesne
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2004 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class ReplicationStrategy {
  private Set _visited;
  private Set _targets;
  private Set _siblings;

  /**
   * @param visited the <code>Set</code> of <code>ServerAddress</code>es of the hosts
   * that have already been visited.
   * @param targets the <code>Set</code> of <code>ServerAddress</code>es corresponding to
   * targeted hosts - if <code>null</code>, then the strategy assumes that all hosts must
   * be visited.
   * @param existing the <code>Set</code> of <code>ServerAddress</code>es corresponding to
   * the existing hosts.
   */
  public ReplicationStrategy(Set visited, Set targets, Set existing) {
    _visited    = visited;
    _targets    = targets;
    _siblings   = existing;
  }

  /**
   * @return the <code>ServerAddress</code> of the next sibling host to which replication
   * should be made. <code>null</code> is returned if there is no "next" host to which to
   * replicate - all hosts have been visited. <b>Note</b>: if not null, the returned address
   * is added to the set of visited ones.
   */
  public ServerAddress selectNextSibling() {
    ServerAddress toReturn;

    if (_targets == null) {
      _siblings.removeAll(_visited);

      if (_siblings.size() == 0) {
        return null;
      }

      toReturn = (ServerAddress) _siblings.iterator().next();
    } else {
      _siblings.retainAll(_targets);
      _siblings.removeAll(_visited);

      if (_siblings.size() == 0) {
        return null;
      }

      toReturn = (ServerAddress) _siblings.iterator().next();
    }

    _visited.add(toReturn);

    return toReturn;
  }
}
