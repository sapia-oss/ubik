package org.sapia.ubik.rmi.server.stub.enrichment;

import java.lang.reflect.Proxy;
import java.rmi.RemoteException;
import java.util.Map;
import java.util.Properties;

import org.sapia.ubik.module.ModuleContext;
import org.sapia.ubik.net.QueryString;
import org.sapia.ubik.net.Uri;
import org.sapia.ubik.rmi.Consts;
import org.sapia.ubik.rmi.naming.remote.RemoteInitialContextFactory;
import org.sapia.ubik.rmi.server.Hub;
import org.sapia.ubik.rmi.server.ObjectTable;
import org.sapia.ubik.rmi.server.ServerTable;
import org.sapia.ubik.rmi.server.Stateless;
import org.sapia.ubik.rmi.server.stub.RemoteRefContext;
import org.sapia.ubik.rmi.server.stub.RemoteRefReliable;
import org.sapia.ubik.rmi.server.stub.StubInvocationHandler;
import org.sapia.ubik.rmi.server.stub.Stubs;

/**
 * Converts a stub to a reliable one (see {@link RemoteRefReliable}).
 * 
 * @author yduchesne
 * 
 */
public class ReliableStubEnrichmentStrategy implements StubEnrichmentStrategy {

  private ServerTable serverTable;
  private ObjectTable objectTable;

  @Override
  public void init(ModuleContext context) {
    serverTable = context.lookup(ServerTable.class);
    objectTable = context.lookup(ObjectTable.class);
  }

  @Override
  public boolean apply(Object stub, JndiBindingInfo info) {
    return !(stub instanceof Stateless);
  }

  /**
   * Returns a "reliable" stub for the given passed in object. The dynamically
   * generated proxy (created with Java's reflection API) wraps a
   * {@link StubInvocationHandler} of the {@link RemoteRefReliable} class.
   * <p>
   * If the passed in object is already a stub, then it is simply converted.
   * 
   * @param stub
   *          the {@link Object} to convert to a reliable stub.
   * @param info
   *          the {@link JndiBindingInfo} holding the JNDI parameters used to
   *          bind the stub to Ubik's JNDI.
   * 
   * @see RemoteRefReliable
   * @see org.sapia.ubik.rmi.server.stub.RemoteRef
   * 
   * @return a "reliable" stub for the given {@link Object}.
   */
  @Override
  public Object enrich(Object stub, JndiBindingInfo info) throws RemoteException {

    Uri uri = Uri.parse(info.getBaseUrl());
    StringBuffer newUri = new StringBuffer();
    newUri.append(uri.getScheme()).append("://").append(uri.getHost());

    if (uri.getPort() != Uri.UNDEFINED_PORT) {
      newUri.append(':').append("" + uri.getPort());
    }

    newUri.append('/').append(info.getName());

    Uri newUriObj = Uri.parse(newUri.toString());
    QueryString.Builder qsBuilder = QueryString.builder();


    qsBuilder.param(RemoteInitialContextFactory.UBIK_DOMAIN_NAME, info.getDomainName().toString());

    Map<String, String> mcastParams = info.getMcastAddress().toParameters();
    for (String paramName : mcastParams.keySet()) {
      qsBuilder.param(paramName, mcastParams.get(paramName));
    }
    newUriObj = newUriObj.append(qsBuilder.build());

    if (!Stubs.isStub(stub)) {
      Properties props = new Properties();
      props.setProperty(Consts.TRANSPORT_TYPE, Hub.DEFAULT_TRANSPORT_TYPE);
      stub = serverTable.exportObject(stub, props);
    }

    StubInvocationHandler handler = Stubs.getStubInvocationHandler(stub);

    if (handler instanceof RemoteRefReliable) {
      return stub;
    }

    RemoteRefContext context = handler.getContexts().iterator().next();
    RemoteRefReliable reliable = createRemoteRef(context, newUriObj.toString());
    Object exported = objectTable.getRefFor(context.getOid()).getObject();

    Object enriched = Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(),
        serverTable.getTypeCache().getInterfaceArrayFor(exported.getClass()), reliable);

    return enriched;
  }

  protected RemoteRefReliable createRemoteRef(RemoteRefContext context, String uri) {
    return new RemoteRefReliable(context, uri);
  }
}
