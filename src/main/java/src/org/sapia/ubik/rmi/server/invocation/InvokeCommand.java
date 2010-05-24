package org.sapia.ubik.rmi.server.invocation;

import org.sapia.ubik.rmi.Consts;
import org.sapia.ubik.rmi.server.Hub;
import org.sapia.ubik.rmi.server.Log;
import org.sapia.ubik.rmi.server.OID;
import org.sapia.ubik.rmi.server.RMICommand;
import org.sapia.ubik.rmi.server.RmiUtils;
import org.sapia.ubik.rmi.server.ShutdownException;
import org.sapia.ubik.rmi.server.VmId;
import org.sapia.ubik.rmi.server.transport.ClassDescriptor;
import org.sapia.ubik.rmi.server.transport.MarshalledObject;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import java.lang.reflect.*;


/**
 * This commands performs a remote method invocation.
 *
 * @author Yanick Duchesne
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class InvokeCommand extends RMICommand implements Externalizable {
  static final long            serialVersionUID         = 1L;
  private static final boolean _vmUsesMarshalledObjects = (System.getProperty(Consts.MARSHALLING) != null) &&
    System.getProperty(Consts.MARSHALLING).equals("true");
  private transient Class[] _paramTypes;
  private transient String  _transportType;
  private OID               _oid;
  private String            _methodName;
  private Object[]          _params;
  private ClassDescriptor[] _paramClasses;
  private boolean           _usesMarshalledObjects = _vmUsesMarshalledObjects;

  /**
   * Do not call; used for externalization only.
   */
  public InvokeCommand() {
  }

  /**
   * @param oid the <code>OID<code> (unique object identifier) of the
   * object on which the method call should be performed.
   * @param methodName the name of the method to call.
   * @param params the method's parameters.
   * @param paramClasses the method's signature, as a class array.
   */
  public InvokeCommand(OID oid, String methodName, Object[] params,
    Class[] paramClasses, String transportType) {
    _oid             = oid;
    _methodName      = methodName;
    _params          = params;
    _paramClasses    = new ClassDescriptor[paramClasses.length];

    for (int i = 0; i < paramClasses.length; i++) {
      _paramClasses[i] = new ClassDescriptor(paramClasses[i]);
    }

    _paramTypes      = paramClasses;
    _transportType   = transportType;
  }

  /**
   * Returns the object identifier of the object on which
   * the invocation will be performed.
   *
   * @return an <code>OID</code>.
   */
  public OID getOID() {
    return _oid;
  }

  /**
   * Returns the name of the method to invoke.
   *
   * @return a method name.
   */
  public String getMethodName() {
    return _methodName;
  }

  /**
   * Returns the signature (the types of the method's parameters) of the method
   * to call.
   *
   * @return an array of <code>Class</code> instances.
   */
  public Class[] getParameterTypes() {
    return _paramTypes;
  }

  public void setParams(Object[] params) {
    _params = params;
  }

  /**
   * Returns true if this instance encapsulates method call parameters and
   * return value in <code>MarshalledObject</code> instances.
   *
   * @return <code>true</code> if this instance uses <code>MarshalledObject</code>s.
   *
   * @see MarshalledObject
   */
  public boolean usesMarshalledObjects() {
    return _usesMarshalledObjects;
  }

  /**
   * Returns the parameters of the method to call.
   */
  public Object[] getParams() {
    return _params;
  }

  /**
   * @see org.sapia.ubik.rmi.server.RMICommand#execute()
   */
  public Object execute() throws Throwable {
    if (Hub.isShutdown()) {
      throw new ShutdownException();
    }

    Object obj = Hub.serverRuntime.objectTable.getObjectFor(_oid);

    if (_paramTypes == null) {
      if(obj.getClass().getClassLoader() == null){
        convertParams(Thread.currentThread().getContextClassLoader());
      }
      else{
        convertParams(obj.getClass().getClassLoader());
      } 
    }

    Method mt = obj.getClass().getMethod(_methodName, _paramTypes);

    ServerPreInvokeEvent preEvt = new ServerPreInvokeEvent(this, obj);
    
    try {
      if (Log.isDebug()) {
        Log.debug(getClass(),
          "invoking " + mt.getName() + " on " + _oid + "(" + obj + ")");
      }

      Hub.serverRuntime.dispatchEvent(preEvt);

      Object toReturn = mt.invoke(preEvt.getTarget(),
          preEvt.getInvokeCommand().getParams());

      ServerPostInvokeEvent postEvt = new ServerPostInvokeEvent(preEvt.getTarget(),
          preEvt.getInvokeCommand(),
          System.currentTimeMillis() - preEvt.getInvokeTime());
      
      postEvt.setResultObject(toReturn);

      Hub.serverRuntime.dispatchEvent(postEvt);

      if (_usesMarshalledObjects) {
        toReturn = new MarshalledObject(toReturn, VmId.getInstance(),
            _config.getServerAddress().getTransportType(), _oid.getCodebase());
      }

      return toReturn;
    } catch (Throwable e) {
      
      // dispatching post invocation event
      
      if (_usesMarshalledObjects) {
        return new MarshalledObject(e, VmId.getInstance(),
          _config.getServerAddress().getTransportType(), _oid.getCodebase());
      }
      if(e instanceof InvocationTargetException){
        e = ((InvocationTargetException)e).getTargetException();
      }
      ServerPostInvokeEvent postEvt = new ServerPostInvokeEvent(preEvt.getTarget(),
          preEvt.getInvokeCommand(),
          System.currentTimeMillis() - preEvt.getInvokeTime(), e);
      
      Hub.serverRuntime.dispatchEvent(postEvt);      
      throw e;
    }
  }

  /**
   * @see java.io.Externalizable#readExternal(ObjectInput)
   */
  public void readExternal(ObjectInput in)
    throws IOException, ClassNotFoundException {
    super.readExternal(in);
    _oid                     = (OID) in.readObject();
    _methodName              = (String) in.readObject();
    _paramClasses            = (ClassDescriptor[]) in.readObject();
    _params                  = (Object[]) in.readObject();
    _usesMarshalledObjects   = in.readBoolean();
  }

  /**
   * @see java.io.Externalizable#writeExternal(ObjectOutput)
   */
  public void writeExternal(ObjectOutput out) throws IOException {
    super.writeExternal(out);
    out.writeObject(_oid);
    out.writeObject(_methodName);

    if (_usesMarshalledObjects) {
      if ((_params != null) && (_params.length > 0) &&
            !(_params[0] instanceof MarshalledObject)) {
        for (int i = 0; i < _params.length; i++) {
          _params[i] = new MarshalledObject(_params[i], _vmId, _transportType, RmiUtils.CODE_BASE);
        }
      }
    }

    out.writeObject(_paramClasses);
    out.writeObject(_params);
    out.writeBoolean(_usesMarshalledObjects);
  }

  /**
   * Internally converts the parameters of the method to call. Internally unmarshals
   * the parameters if they are instances of <code>MarshalledObject</code>.
   *
   * @see MarshalledObject
   */
  protected void convertParams(ClassLoader loader)
    throws IOException, ClassNotFoundException {
    _paramTypes = new Class[_paramClasses.length];

    for (int i = 0; i < _paramClasses.length; i++) {
      _paramTypes[i] = _paramClasses[i].resolve(loader);
    }

    if (_usesMarshalledObjects && (_params != null)) {
      for (int i = 0; i < _params.length; i++) {
        _params[i] = ((MarshalledObject) _params[i]).get(loader);
      }
    }
  }
}
