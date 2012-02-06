package org.sapia.ubik.rmi.examples.replication;

import java.util.Set;

import org.sapia.ubik.rmi.examples.Foo;
import org.sapia.ubik.rmi.interceptor.Interceptor;
import org.sapia.ubik.rmi.replication.ReplicationEvent;


/**
 * @author Yanick Duchesne
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2004 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class ServerSideInterceptor implements Interceptor {
  private Foo _target;
  private Set _siblings;

  /**
   *
   */
  public ServerSideInterceptor(Set siblings, Foo targetInstance) {
    _siblings   = siblings;
    _target     = targetInstance;
  }

  public void onReplicationEvent(ReplicationEvent evt) {
    if (evt.getReplicatedCommand() instanceof ReplicatedCommandEx) {
      System.out.println("Replicated command intercepted...");

      ReplicatedInvokerImpl inv = (ReplicatedInvokerImpl) evt.getReplicatedCommand()
                                                             .getReplicatedInvoker();
      inv.setTargetInstance(_target);
      inv.setSiblings(_siblings);
    }
  }
}
