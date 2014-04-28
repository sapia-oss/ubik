package org.sapia.ubik.rmi.naming.remote;

import org.sapia.ubik.log.Category;
import org.sapia.ubik.log.Log;
import org.sapia.ubik.mcast.Defaults;
import org.sapia.ubik.rmi.Consts;
import org.sapia.ubik.rmi.naming.remote.archie.UbikRemoteContext;
import org.sapia.ubik.taskman.Task;
import org.sapia.ubik.util.Conf;
import org.sapia.ubik.util.Func;

/**
 * Task that periodically triggers synchronization of a JNDI tree with the others in the cluster.
 *
 * @author yduchesne
 *
 */
public class JndiSyncTask implements Task {

  private Category log = Log.createCategory(getClass());

  private UbikRemoteContext root;
  Func<Void, JndiSyncRequest> dispatchFunc;
  private int syncMaxCount  = Conf.newInstance().getIntProperty(Consts.JNDI_SYNC_MAX_COUNT, Defaults.DEFAULT_JNDI_SYNC_MAX_COUNT);
  private int execCount;

  JndiSyncTask(UbikRemoteContext root, Func<Void, JndiSyncRequest> dispatchFunc) {
    this.root = root;
    this.dispatchFunc = dispatchFunc;

    if (syncMaxCount > 0) {
      log.info("JNDI sync will be performed %s times after startup", syncMaxCount);
    } else {
      log.info("JNDI sync will be performed indefinitely");
    }
  }

  @Override
  public void exec(org.sapia.ubik.taskman.TaskContext ctx) {
    JndiSyncVisitor visitor = new JndiSyncVisitor();
    root.accept(visitor);
    JndiSyncRequest request = JndiSyncRequest.newInstance(visitor.asMap());
    dispatchFunc.call(request);
    execCount++;
    if (syncMaxCount > 0 && execCount >= syncMaxCount) {
      log.debug("Max number of executions reached: %s, aborting", execCount);
      ctx.abort();
    }
  }

}
