package org.sapia.ubik.rmi.server.invocation;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.sapia.ubik.log.Category;
import org.sapia.ubik.log.Log;
import org.sapia.ubik.module.ModuleContext;
import org.sapia.ubik.rmi.server.ObjectTable;
import org.sapia.ubik.rmi.server.ServerRuntime;
import org.sapia.ubik.rmi.server.command.InvokeCommand;
import org.sapia.ubik.rmi.server.invocation.InvocationDispatcher.InvocationStats;
import org.sapia.ubik.rmi.server.invocation.InvocationDispatcher.InvocationStrategy;
import org.sapia.ubik.rmi.server.transport.Connections;

public class ColocatedInvocationStrategy implements InvocationStrategy {

  private Category log = Log.createCategory(getClass());
  private ServerRuntime serverRuntime;
  private ObjectTable objectTable;

  @Override
  public void init(ModuleContext modules) {
    this.serverRuntime = modules.lookup(ServerRuntime.class);
    this.objectTable = modules.lookup(ObjectTable.class);
  }

  @Override
  public Object dispatchInvocation(InvocationStats stats, Connections pool, InvokeCommand cmd) throws IOException, ClassNotFoundException, Throwable {

    Object toReturn;
    Object target = objectTable.getObjectFor(cmd.getOID());
    Method toCall = target.getClass().getMethod(cmd.getMethodName(), cmd.getParameterTypes());

    log.debug("Performing colocated call ==> invoking %s on %s (%s)", toCall.getName(), cmd.getOID(), target);

    // SERVER pre invoke event dispatch
    ServerPreInvokeEvent serverPreEvent = new ServerPreInvokeEvent(cmd, target);
    serverRuntime.getDispatcher().dispatch(serverPreEvent);

    try {
      toCall.setAccessible(true);
      toReturn = toCall.invoke(serverPreEvent.getTarget(), serverPreEvent.getInvokeCommand().getParams());

      // SERVER post invoke event dispatch
      ServerPostInvokeEvent serverPostEvent = new ServerPostInvokeEvent(serverPreEvent.getTarget(), serverPreEvent.getInvokeCommand(),
          System.currentTimeMillis() - serverPreEvent.getInvokeTime());
      serverPostEvent.setResultObject(toReturn);
      serverRuntime.getDispatcher().dispatch(serverPostEvent);

    } catch (Throwable e) {
      if (e instanceof InvocationTargetException) {
        e = ((InvocationTargetException) e).getTargetException();
      }
      ServerPostInvokeEvent postEvt = new ServerPostInvokeEvent(serverPreEvent.getTarget(), serverPreEvent.getInvokeCommand(),
          System.currentTimeMillis() - serverPreEvent.getInvokeTime(), e);
      serverRuntime.getDispatcher().dispatch(postEvt);
      e.fillInStackTrace();
      toReturn = e;
    }
    return toReturn;
  }

}
