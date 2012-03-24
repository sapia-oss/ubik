package org.sapia.ubik.rmi.server.stub.enrichment;

import java.lang.reflect.Proxy;
import java.rmi.RemoteException;
import java.util.Properties;

import org.sapia.ubik.module.ModuleContext;
import org.sapia.ubik.rmi.Consts;
import org.sapia.ubik.rmi.server.Hub;
import org.sapia.ubik.rmi.server.ObjectTable;
import org.sapia.ubik.rmi.server.ServerTable;
import org.sapia.ubik.rmi.server.Stateless;
import org.sapia.ubik.rmi.server.oid.OID;
import org.sapia.ubik.rmi.server.stub.RemoteRefStateless;
import org.sapia.ubik.rmi.server.stub.StubInvocationHandler;
import org.sapia.ubik.rmi.server.stub.Stubs;

/**
 * Converts a stub to a stateless one (see {@link RemoteRefStateless}). 
 * 
 * @author yduchesne
 *
 */
public class StatelessStubEnrichmentStrategy implements StubEnrichmentStrategy {
  
  private ServerTable serverTable;
  private ObjectTable objectTable;
  
  @Override
  public void init(ModuleContext context) {
    serverTable = context.lookup(ServerTable.class);
    objectTable = context.lookup(ObjectTable.class);
  }
  
  @Override
  public boolean apply(Object stub, JndiBindingInfo info) {
  	return stub instanceof Stateless;
  }
  
  /**
   * Returns a stateless stub for the given object. The dynamically generated
   * proxy (created with Java's reflection API) wraps a {@link StubInvocationHandler}
   * of the {@link RemoteRefStateless} class.
   * <p>
   * If the passed in object is already a stub, then it is simply converted.
   *
   * @param name the name of the object for which a stateless stub should be returned.
   * @param domain the name of the domain to which the  object "belongs".
   * @param obj the {@link Object} for which a stateless stub will be created.
   * @return a stateless stub.
   * 
   * @param stub the {@link Object} to convert to a reliable stub.
   * @param info the {@link JndiBindingInfo} holding the JNDI parameters used to bind the stub
   * to Ubik's JNDI.
   *
   * @see Stateless
   * @see RemoteRefStateless
   */  
  @Override
  public Object enrich(Object stub, JndiBindingInfo info) throws RemoteException {
    if(!Stubs.isStub(stub)) {
      Properties props = new Properties();
      props.setProperty(Consts.TRANSPORT_TYPE, Hub.DEFAULT_TRANSPORT_TYPE);
      stub = serverTable.exportObject(stub, props);
    } 
      
    StubInvocationHandler handler   = Stubs.getStubInvocationHandler(stub);
    if(handler instanceof RemoteRefStateless) {
      return stub;
    }
    
    RemoteRefStateless    stateless = Stubs.convertToStatelessRemoteRef(info.getName(), info.getDomainName().toString(), handler);
    OID                   oid       = handler.getContexts().iterator().next().getOid();
    Object                exported  = objectTable.getObjectFor(oid);

    Object proxy = Proxy.newProxyInstance(
        Thread.currentThread().getContextClassLoader(),
        serverTable.getTypeCache().getInterfaceArrayFor(exported.getClass()), 
        stateless
    );
    return proxy;    
  }

}
