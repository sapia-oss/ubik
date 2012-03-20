package org.sapia.ubik.rmi.server.command;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.sapia.ubik.log.Log;
import org.sapia.ubik.rmi.Consts;
import org.sapia.ubik.rmi.server.Hub;
import org.sapia.ubik.rmi.server.ShutdownException;
import org.sapia.ubik.rmi.server.VmId;
import org.sapia.ubik.rmi.server.invocation.ServerPostInvokeEvent;
import org.sapia.ubik.rmi.server.invocation.ServerPreInvokeEvent;
import org.sapia.ubik.rmi.server.oid.DefaultOID;
import org.sapia.ubik.rmi.server.oid.OID;
import org.sapia.ubik.rmi.server.transport.ClassDescriptor;
import org.sapia.ubik.rmi.server.transport.MarshalledObject;


/**
 * This commands performs a remote method invocation.
 *
 * @author Yanick Duchesne
 */
public class InvokeCommand extends RMICommand implements Externalizable {
  static final long            serialVersionUID         = 1L;
  
  private static final boolean vmUsesMarshalledObjects = 
    System.getProperty(Consts.MARSHALLING) != null &&
    System.getProperty(Consts.MARSHALLING).equals("true");
  
  private transient Class<?>[] paramTypes;
  private transient String     transportType;
  private OID                  oid;
  private String               methodName;
  private Object[]             params;
  private ClassDescriptor[]    paramClasses;
  private boolean              usesMarshalledObjects = vmUsesMarshalledObjects;

  /**
   * Do not call; used for externalization only.
   */
  public InvokeCommand() {
  }

  /**
   * @param oid the {@link DefaultOID} (unique object identifier) of the
   * object on which the method call should be performed.
   * @param methodName the name of the method to call.
   * @param params the method's parameters.
   * @param paramClasses the method's signature, as a class array.
   */
  public InvokeCommand(OID oid, String methodName, Object[] params,
    Class<?>[] paramClasses, String transportType) {
    this.oid             = oid;
    this.methodName      = methodName;
    this.params          = params;
    this.paramClasses    = new ClassDescriptor[paramClasses.length];

    for (int i = 0; i < paramClasses.length; i++) {
      this.paramClasses[i] = new ClassDescriptor(paramClasses[i]);
    }

    this.paramTypes      = paramClasses;
    this.transportType   = transportType;
  }

  /**
   * Returns the object identifier of the object on which
   * the invocation will be performed.
   *
   * @return an {@link DefaultOID}.
   */
  public OID getOID() {
    return oid;
  }

  /**
   * Returns the name of the method to invoke.
   *
   * @return a method name.
   */
  public String getMethodName() {
    return methodName;
  }

  /**
   * Returns the signature (the types of the method's parameters) of the method
   * to call.
   *
   * @return an array of {@link Class} instances.
   */
  public Class<?>[] getParameterTypes() {
    return paramTypes;
  }

  public void setParams(Object[] params) {
    this.params = params;
  }

  /**
   * Returns true if this instance encapsulates method call parameters and
   * return value in {@link MarshalledObject} instances.
   *
   * @return <code>true</code> if this instance uses {@link MarshalledObject}s.
   *
   * @see MarshalledObject
   */
  public boolean usesMarshalledObjects() {
    return usesMarshalledObjects;
  }

  /**
   * Returns the parameters of the method to call.
   */
  public Object[] getParams() {
    return params;
  }

  /**
   * @see org.sapia.ubik.rmi.server.command.RMICommand#execute()
   */
  public Object execute() throws Throwable {
    if (Hub.isShutdown()) {
      throw new ShutdownException();
    }

    Object obj = Hub.getModules().getObjectTable().getObjectFor(oid);

    if (paramTypes == null) {
      if(obj.getClass().getClassLoader() == null){
        convertParams(Thread.currentThread().getContextClassLoader());
      }
      else{
        convertParams(obj.getClass().getClassLoader());
      } 
    }

    Method mt = obj.getClass().getMethod(methodName, paramTypes);

    ServerPreInvokeEvent preEvt = new ServerPreInvokeEvent(this, obj);
    
    try {
      if (Log.isDebug()) {
        Log.debug(getClass(), "invoking " + mt.getName() + " on " + oid + "(" + obj + ")");
      }

      Hub.getModules().getServerRuntime().getDispatcher().dispatch(preEvt);

      Object toReturn = mt.invoke(preEvt.getTarget(),
          preEvt.getInvokeCommand().getParams());

      ServerPostInvokeEvent postEvt = new ServerPostInvokeEvent(preEvt.getTarget(),
          preEvt.getInvokeCommand(),
          System.currentTimeMillis() - preEvt.getInvokeTime());
      
      postEvt.setResultObject(toReturn);

      Hub.getModules().getServerRuntime().getDispatcher().dispatch(postEvt);

      if (usesMarshalledObjects) {
        toReturn = new MarshalledObject(
            toReturn, 
            VmId.getInstance(),
            config.getServerAddress().getTransportType());
      }

      return toReturn;
    } catch (Throwable e) {
      
      // dispatching post invocation event
      
      if (usesMarshalledObjects) {
        return new MarshalledObject(e, VmId.getInstance(),config.getServerAddress().getTransportType());
      }
      
      if(e instanceof InvocationTargetException){
        e = ((InvocationTargetException)e).getTargetException();
      }
      ServerPostInvokeEvent postEvt = new ServerPostInvokeEvent(
          preEvt.getTarget(),
          preEvt.getInvokeCommand(),
          System.currentTimeMillis() - preEvt.getInvokeTime(), 
          e
      );
      
      Hub.getModules().getServerRuntime().getDispatcher().dispatch(postEvt);      
      throw e;
    }
  }

  /**
   * @see java.io.Externalizable#readExternal(ObjectInput)
   */
  public void readExternal(ObjectInput in)
    throws IOException, ClassNotFoundException {
    super.readExternal(in);
    oid                     = (OID) in.readObject();
    methodName              = (String) in.readObject();
    paramClasses            = (ClassDescriptor[]) in.readObject();
    params                  = (Object[]) in.readObject();
    usesMarshalledObjects   = in.readBoolean();
  }

  /**
   * @see java.io.Externalizable#writeExternal(ObjectOutput)
   */
  public void writeExternal(ObjectOutput out) throws IOException {
    super.writeExternal(out);
    out.writeObject(oid);
    out.writeObject(methodName);

    if (usesMarshalledObjects) {
      if ((params != null) && (params.length > 0) && !(params[0] instanceof MarshalledObject)) {
        for (int i = 0; i < params.length; i++) {
          params[i] = new MarshalledObject(params[i], vmId, transportType);
        }
      }
    }

    out.writeObject(paramClasses);
    out.writeObject(params);
    out.writeBoolean(usesMarshalledObjects);
  }

  /**
   * Internally converts the parameters of the method to call. Internally unmarshals
   * the parameters if they are instances of {@link MarshalledObject}.
   *
   * @see MarshalledObject
   */
  protected void convertParams(ClassLoader loader)
    throws IOException, ClassNotFoundException {
    paramTypes = new Class[paramClasses.length];

    for (int i = 0; i < paramClasses.length; i++) {
      paramTypes[i] = paramClasses[i].resolve(loader);
    }

    if (usesMarshalledObjects && (params != null)) {
      for (int i = 0; i < params.length; i++) {
        params[i] = ((MarshalledObject) params[i]).get(loader);
      }
    }
  }
}
