package org.sapia.ubik.rmi.server.invocation;

import org.sapia.ubik.rmi.Consts;
import org.sapia.ubik.rmi.server.*;
import org.sapia.ubik.rmi.server.command.ResponseLock;
import org.sapia.ubik.rmi.server.command.ResponseQueue;
import org.sapia.ubik.rmi.server.command.ResponseTimeOutException;
import org.sapia.ubik.rmi.server.perf.PerfAnalyzer;
import org.sapia.ubik.rmi.server.perf.Topic;
import org.sapia.ubik.rmi.server.transport.Connections;
import org.sapia.ubik.rmi.server.transport.MarshalledObject;
import org.sapia.ubik.rmi.server.transport.RmiConnection;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;


/**
 * This class handles remote method invocations on the client-side.
 *
 * @author Yanick Duchesne
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class InvocationDispatcher {
  static final long   DEFAULT_CALLBACK_TIMEOUT = 30000;
  private static long _timeout = DEFAULT_CALLBACK_TIMEOUT;
  
  static {
    if (System.getProperty(Consts.CLIENT_CALLBACK_TIMEOUT) != null) {
      try {
        _timeout = Long.parseLong(Consts.CLIENT_CALLBACK_TIMEOUT);
      } catch (NumberFormatException e) {
        //noop
      }
    }
  }
  
  private Perf _perf = new Perf();
  private ResponseQueue _responses = ResponseQueue.getInstance();
  
  /**
   * Constructor for InvocationDispatcher.
   */
  public InvocationDispatcher() {
    super();
  }
  
  /**
   * Dispatches the given invocation command using the passed in
   * connection pool (which holds connections to a remote server).
   *
   * @param vmId the <code>VmId</code> of the stub that is performing the call.
   * @param pool <code>Connections</code> to the server to call.
   * @param cmd an <code>InvokeCommand</code> representing the remote
   * method invocation to be performed.
   *
   * @return the return value of the invocation.
   *
   * @throws java.io.IOException if an IO problem occurs while performing this
   * operation.
   *
   * @throws ClassNotFoundException if the class of the invocation's return value
   * could not be found in the deserialization process.
   *
   * @throws Throwable if the invocation is colocated and an exception is throws by the
   * colocated call.
   */
  public Object dispatchInvocation(VmId vmId, Connections pool,
    InvokeCommand cmd)
    throws java.io.IOException, ClassNotFoundException, Throwable {
    Object toReturn;
    
    if (Log.isDebug()) {
      Log.debug(getClass(), "sending invocation for object: " + cmd.getOID() + " on vmId " + vmId);
    }
    
    // CLIENT pre invoke event dispatch
    ClientPreInvokeEvent pre = new ClientPreInvokeEvent(cmd);
    Hub.clientRuntime.dispatcher.dispatch(pre);
    
    
    if (VmId.getInstance().equals(vmId)) {
      Object target = Hub.serverRuntime.objectTable.getObjectFor(cmd.getOID());
      Method toCall = target.getClass().getMethod(pre.getCommand().getMethodName(),
              pre.getCommand().getParameterTypes());

      if (Log.isInfo()) {
        Log.info(getClass(), "performing colocated direct call ==> invoking " + toCall.getName() + " on " + cmd.getOID() + "(" + target + ")");
      }

      // SERVER pre invoke event dispatch
      ServerPreInvokeEvent serverPreEvent = new ServerPreInvokeEvent(pre.getCommand(), target);
      Hub.serverRuntime.dispatcher.dispatch(serverPreEvent);
      
      try {
        toReturn = toCall.invoke(serverPreEvent.getTarget(), serverPreEvent.getInvokeCommand().getParams());

        // SERVER post invoke event dispatch
        ServerPostInvokeEvent serverPostEvent = new ServerPostInvokeEvent(serverPreEvent.getTarget(),
                serverPreEvent.getInvokeCommand(), System.currentTimeMillis() - serverPreEvent.getInvokeTime());
        serverPostEvent.setResultObject(toReturn);
        Hub.serverRuntime.dispatchEvent(serverPostEvent);
        
      } catch (Throwable e) {
        if(e instanceof InvocationTargetException){
          e = ((InvocationTargetException)e).getTargetException();
        }
        ServerPostInvokeEvent postEvt = new ServerPostInvokeEvent(serverPreEvent.getTarget(),
                serverPreEvent.getInvokeCommand(), System.currentTimeMillis() - serverPreEvent.getInvokeTime(), e);
        Hub.serverRuntime.dispatchEvent(postEvt);      

        throw e;
      }
      
    } else {
      if (_perf.acquireCon.isEnabled()) {
        _perf.acquireCon.start();
      }
      
      RmiConnection conn = pool.acquire();
      
      if (_perf.acquireCon.isEnabled()) {
        _perf.acquireCon.end();
      }
      
      try {
        //    	VmId.getInstance().register(cmd.getVmId());
        if (_perf.invokeSend.isEnabled()) {
          _perf.invokeSend.start();
        }
        
//        conn.send(pre.getCommand(), cmd.getVmId(),
//      Changed the vmId in case the invocation contains remote objects (to be referencized)
        conn.send(pre.getCommand(), vmId,
          conn.getServerAddress().getTransportType());
        
        if (_perf.invokeSend.isEnabled()) {
          _perf.invokeSend.end();
        }
        
        if (_perf.invokeReceive.isEnabled()) {
          _perf.invokeReceive.start();
        }
        
        toReturn = conn.receive();
        
        if (_perf.invokeReceive.isEnabled()) {
          _perf.invokeReceive.end();
        }
      } finally {
        pool.release(conn);
      }
      
      if (cmd.usesMarshalledObjects() && (toReturn != null)) {
        try {
          toReturn = ((MarshalledObject) toReturn).get(Thread.currentThread()
          .getContextClassLoader());
        } catch (ClassCastException e) {
          String aMessage = "Could not cast to MarshalledObject: " +
            toReturn.getClass() + "\n" + toReturn;
          Log.error(getClass(), aMessage);
          throw new ClassCastException(aMessage);
        }
      }
    }
    
    ClientPostInvokeEvent post = new ClientPostInvokeEvent(pre.getCommand(),
      toReturn);
    
    if (Log.isDebug()) {
      Log.debug(getClass(), "dispatching post-invocation event");
    }
    
    Hub.clientRuntime.dispatcher.dispatch(post);
    
    if (Log.isDebug()) {
      Log.debug(getClass(), "returning invocation response");
    }
    
    return toReturn;
  }
  
  /**
   * Dispatches the given asynchronous invocation command using the passed in
   * connection pool (which holds connections to a remote server).
   *
   * @param vmId the <code>VmId</code> of the stub that is performing the call.
   * @param pool <code>Connections</code> to the server to call.
   * @param cmd an <code>InvokeCommand</code> representing the remote
   * method invocation to be performed.
   *
   * @return the return value of the invocation.
   *
   * @throws java.io.IOException if an IO problem occurs while performing this
   * operation.
   * @throws ClassNotFoundException if the class of the invocation's return value
   * could not be found in the deserialization process.
   * @throws ResponseTimeOutException if no response is received before the time-out
   * specified by the <code>ubik.rmi.client.callback.timeout</code> property.
   * @throws Throwable if the invocation is colocated and an exception is throws by the
   * colocated call.
   */
  public Object dispatchInvocation(VmId vmId, Connections pool,
    CallBackInvokeCommand cmd)
    throws java.io.IOException, ClassNotFoundException,
    ResponseTimeOutException, Throwable {
    RmiConnection        conn;
    Object               toReturn;
    
    ClientPreInvokeEvent pre = new ClientPreInvokeEvent(cmd);
    
    if (VmId.getInstance().equals(vmId)) {
      Object target = Hub.serverRuntime.objectTable.getObjectFor(cmd.getOID());
      Method toCall = target.getClass().getMethod(cmd.getMethodName(),
        cmd.getParameterTypes());

      if (Log.isInfo()) {
        Log.info(getClass(), "performing colocated callback call ==> invoking " + toCall.getName() + " on " + cmd.getOID() + "(" + target + ")");
      }
      Hub.clientRuntime.dispatcher.dispatch(pre);
      
      // SERVER pre invoke event dispatch
      ServerPreInvokeEvent serverPreEvent = new ServerPreInvokeEvent(pre.getCommand(), target);
      Hub.serverRuntime.dispatcher.dispatch(serverPreEvent);
      
      try {
        toReturn = toCall.invoke(serverPreEvent.getTarget(), serverPreEvent.getInvokeCommand().getParams());

        // SERVER post invoke event dispatch
        ServerPostInvokeEvent serverPostEvent = new ServerPostInvokeEvent(serverPreEvent.getTarget(),
                serverPreEvent.getInvokeCommand(), System.currentTimeMillis() - serverPreEvent.getInvokeTime());
        serverPostEvent.setResultObject(toReturn);
        Hub.serverRuntime.dispatchEvent(serverPostEvent);
        
      } catch (Throwable e) {
        if(e instanceof InvocationTargetException){
          e = ((InvocationTargetException)e).getTargetException();
        }
        ServerPostInvokeEvent postEvt = new ServerPostInvokeEvent(serverPreEvent.getTarget(),
                serverPreEvent.getInvokeCommand(), System.currentTimeMillis() - serverPreEvent.getInvokeTime(), e);
        Hub.serverRuntime.dispatchEvent(postEvt);      

        throw e;
      }
      
    } else {
      ResponseLock lock = _responses.createResponseLock();
      
      if (Log.isDebug()) {
        Log.debug(getClass(), "sending callback invocation " + lock.getId());
      }
      
      cmd.setUp(lock.getId(),
        Hub.clientRuntime.getCallbackAddress(pool.getTransportType()));
      Hub.clientRuntime.dispatcher.dispatch(pre);
      
      conn = pool.acquire();
      
      try {
        //    	VmId.getInstance().register(cmd.getVmId());
        conn.send(pre.getCommand(), vmId,
          conn.getServerAddress().getTransportType());
        
        if (Log.isDebug()) {
          Log.debug(getClass(), "receiving ACK");
        }
        
        conn.receive();
      } finally {
        pool.release(conn);
      }
      
      try {
        if (Log.isDebug()) {
          Log.debug(getClass(), "waiting for response...");
        }
        
        toReturn = lock.waitResponse(_timeout);
      } catch (ResponseTimeOutException e) {
        throw e;
      } catch (InterruptedException e) {
        lock.release();
        throw new java.io.IOException("response queue thread interrupted");
      }
      
      if (cmd.usesMarshalledObjects() && (toReturn != null)) {
        try {
          toReturn = ((MarshalledObject) toReturn).get(Thread.currentThread()
          .getContextClassLoader());
        } catch (ClassCastException e) {
          String aMessage = "Could not cast to MarshalledObject: " +
            toReturn.getClass() + "\n" + toReturn;
          Log.error(getClass(), aMessage);
          throw new ClassCastException(aMessage);
        }
      }
    }
    
    ClientPostInvokeEvent post = new ClientPostInvokeEvent(pre.getCommand(),
      toReturn);
    Hub.clientRuntime.dispatcher.dispatch(post);
    
    return toReturn;
  }
  
  private String className(){
    return getClass().getName();
  }
  
  //// Inner classes
  
  class Perf{
    Topic acquireCon = PerfAnalyzer.getInstance().getTopic(className()+".AcquireConnection");
    Topic invokeSend = PerfAnalyzer.getInstance().getTopic(className()+".InvokeSend");    
    Topic invokeReceive = PerfAnalyzer.getInstance().getTopic(className()+".InvokeReceive");    
  }
}
