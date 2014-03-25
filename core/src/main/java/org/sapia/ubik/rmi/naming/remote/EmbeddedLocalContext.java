package org.sapia.ubik.rmi.naming.remote;

import java.io.IOException;
import java.lang.reflect.UndeclaredThrowableException;
import java.rmi.RemoteException;
import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NameParser;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;

import org.sapia.archie.jndi.proxy.ContextProxy;
import org.sapia.ubik.log.Category;
import org.sapia.ubik.log.Log;
import org.sapia.ubik.mcast.EventChannelRef;
import org.sapia.ubik.rmi.naming.remote.proxy.LocalNamingEnum;
import org.sapia.ubik.rmi.server.Hub;
import org.sapia.ubik.rmi.server.stub.RemoteRefStateless;
import org.sapia.ubik.rmi.server.stub.StubContainer;
import org.sapia.ubik.rmi.server.stub.StubInvocationHandler;
import org.sapia.ubik.rmi.server.stub.Stubs;
import org.sapia.ubik.rmi.server.stub.enrichment.StubEnrichmentStrategy.JndiBindingInfo;

/**
 * @author Yanick Duchesne
 */
@SuppressWarnings(value = "unchecked")
class EmbeddedLocalContext extends ContextProxy {
  
  private static final Category LOG = Log.createCategory(EmbeddedLocalContext.class);
 
  private EventChannelRef channel;
  private String          baseUrl;
  
  EmbeddedLocalContext(EventChannelRef channel, String baseUrl, Context delegate) throws NamingException {
    super(delegate);
    this.baseUrl = baseUrl;
  }

  /**
   * @see javax.naming.Context#bind(Name, Object)
   */
  public void bind(Name n, Object o) throws NamingException {
    rebind(n, o);
  }

  /**
   * @see javax.naming.Context#bind(String, Object)
   */
  public void bind(String n, Object o) throws NamingException {
    rebind(n, o);
  }

  /**
   * @see javax.naming.Context#close()
   */
  public void close() throws NamingException {
  }

  /**
   * @see javax.naming.Context#composeName(Name, Name)
   */
  public Name composeName(Name n1, Name n2) throws NamingException {
    try {
      return super.composeName(n1, n2);
    } catch (UndeclaredThrowableException e) {
      doFailOver(e);

      return super.composeName(n1, n2);
    }
  }

  /**
   * @see javax.naming.Context#composeName(String, String)
   */
  public String composeName(String n1, String n2) throws NamingException {
    try {
      return super.composeName(n1, n2);
    } catch (UndeclaredThrowableException e) {
      doFailOver(e);

      return super.composeName(n1, n2);
    }
  }

  /**
   * @see javax.naming.Context#createSubcontext(Name)
   */
  public Context createSubcontext(Name n) throws NamingException {
    try {
      return super.createSubcontext(n);
    } catch (UndeclaredThrowableException e) {
      doFailOver(e);

      return super.createSubcontext(n);
    }
  }

  /**
   * @see javax.naming.Context#createSubcontext(String)
   */
  public Context createSubcontext(String name) throws NamingException {
    try {
      return super.createSubcontext(name);
    } catch (UndeclaredThrowableException e) {
      doFailOver(e);

      return super.createSubcontext(name);
    }
  }

  /**
   * @see javax.naming.Context#destroySubcontext(Name)
   */
  public void destroySubcontext(Name n) throws NamingException {
    try {
      super.destroySubcontext(n);
    } catch (UndeclaredThrowableException e) {
      doFailOver(e);
      super.destroySubcontext(n);
    }
  }

  /**
   * @see javax.naming.Context#destroySubcontext(String)
   */
  public void destroySubcontext(String name) throws NamingException {
    try {
      super.destroySubcontext(name);
    } catch (UndeclaredThrowableException e) {
      doFailOver(e);
      super.destroySubcontext(name);
    }
  }

  /**
   * @see javax.naming.Context#getEnvironment()
   */
  public Hashtable<?, ?> getEnvironment() throws NamingException {
    try {
      return super.getEnvironment();
    } catch (UndeclaredThrowableException e) {
      doFailOver(e);

      return super.getEnvironment();
    }
  }

  /**
   * @see javax.naming.Context#getNameInNamespace()
   */
  public String getNameInNamespace() throws NamingException {
    try {
      return super.getNameInNamespace();
    } catch (UndeclaredThrowableException e) {
      doFailOver(e);

      return super.getNameInNamespace();
    }
  }

  /**
   * @see javax.naming.Context#getNameParser(Name)
   */
  public NameParser getNameParser(Name n) throws NamingException {
    try {
      return super.getNameParser(n);
    } catch (UndeclaredThrowableException e) {
      doFailOver(e);

      return super.getNameParser(n);
    }
  }

  /**
   * @see javax.naming.Context#getNameParser(String)
   */
  public NameParser getNameParser(String name) throws NamingException {
    try {
      return super.getNameParser(name);
    } catch (UndeclaredThrowableException e) {
      doFailOver(e);

      return super.getNameParser(name);
    }
  }

  /**
   * @see javax.naming.Context#list(Name)
   */
  @SuppressWarnings("rawtypes")
  public NamingEnumeration list(Name n) throws NamingException {
    try {
      return super.list(n);
    } catch (UndeclaredThrowableException e) {
      doFailOver(e);

      return super.list(n);
    }
  }

  /**
   * @see javax.naming.Context#list(String)
   */
  @SuppressWarnings("rawtypes")
  public NamingEnumeration list(String name) throws NamingException {
    try {
      return super.list(name);
    } catch (UndeclaredThrowableException e) {
      doFailOver(e);

      return super.list(name);
    }
  }

  /**
   * @see javax.naming.Context#listBindings(Name)
   */
  @SuppressWarnings("rawtypes")
  public NamingEnumeration listBindings(Name n) throws NamingException {
    try {
      return super.listBindings(n);
    } catch (UndeclaredThrowableException e) {
      doFailOver(e);

      return super.listBindings(n);
    }
  }

  /**
   * @see javax.naming.Context#listBindings(String)
   */
  @SuppressWarnings("rawtypes")
  public NamingEnumeration listBindings(String name) throws NamingException {
    try {
      return super.listBindings(name);
    } catch (UndeclaredThrowableException e) {
      doFailOver(e);

      return super.listBindings(name);
    }
  }

  /**
   * @see javax.naming.Context#lookup(Name)
   */
  public Object lookup(Name n) throws NamingException {
    try {
      return super.lookup(n);
    } catch (UndeclaredThrowableException e) {
      doFailOver(e);

      return super.lookup(n);
    }
  }

  /**
   * @see javax.naming.Context#lookup(String)
   */
  public Object lookup(String name) throws NamingException {
    try {
      return super.lookup(name);
    } catch (UndeclaredThrowableException e) {
      doFailOver(e);

      return super.lookup(name);
    }
  }

  /**
   * @see javax.naming.Context#lookupLink(Name)
   */
  public Object lookupLink(Name n) throws NamingException {
    try {
      return super.lookupLink(n);
    } catch (UndeclaredThrowableException e) {
      doFailOver(e);

      return super.lookupLink(n);
    }
  }

  /**
   * @see javax.naming.Context#lookupLink(String)
   */
  public Object lookupLink(String name) throws NamingException {
    try {
      return super.lookup(name);
    } catch (UndeclaredThrowableException e) {
      doFailOver(e);

      return super.lookup(name);
    }
  }

  /**
   * @see javax.naming.Context#rebind(Name, Object)
   */
  public void rebind(Name n, Object o) throws NamingException {
    try {
      super.rebind(n, o);
    } catch (UndeclaredThrowableException e) {
      doFailOver(e);
      super.bind(n, o);
    }
  }

  /**
   * @see javax.naming.Context#rebind(String, Object)
   */
  public void rebind(String n, Object o) throws NamingException {
    try {
      super.rebind(n, o);
    } catch (UndeclaredThrowableException e) {
      doFailOver(e);
      super.rebind(n, o);
    }
  }

  /**
   * @see javax.naming.Context#removeFromEnvironment(String)
   */
  public Object removeFromEnvironment(String name) throws NamingException {
    try {
      return super.removeFromEnvironment(name);
    } catch (UndeclaredThrowableException e) {
      doFailOver(e);

      return super.removeFromEnvironment(name);
    }
  }

  /**
   * @see javax.naming.Context#rename(Name, Name)
   */
  public void rename(Name n1, Name n2) throws NamingException {
    try {
      super.rename(n1, n2);
    } catch (UndeclaredThrowableException e) {
      doFailOver(e);
      super.rename(n1, n2);
    }
  }

  /**
   * @see javax.naming.Context#rename(String, String)
   */
  public void rename(String n1, String n2) throws NamingException {
    try {
      super.rename(n1, n2);
    } catch (UndeclaredThrowableException e) {
      doFailOver(e);
      super.rename(n1, n2);
    }
  }

  /**
   * @see javax.naming.Context#unbind(Name)
   */
  public void unbind(Name n) throws NamingException {
    try {
      super.unbind(n);
    } catch (UndeclaredThrowableException e) {
      doFailOver(e);
      super.unbind(n);
    }
  }

  /**
   * @see javax.naming.Context#unbind(String)
   */
  public void unbind(String name) throws NamingException {
    try {
      super.unbind(name);
    } catch (UndeclaredThrowableException e) {
      doFailOver(e);
      super.unbind(name);
    }
  }

  /**
   * @see ContextProxy#onLookup(javax.naming.Name, java.lang.Object)
   */
  protected Object onLookup(Name name, Object obj) throws NamingException {
    if (obj instanceof StubContainer) {
      try {
        return ((StubContainer) obj).toStub(Thread.currentThread().getContextClassLoader());
      } catch (RemoteException e) {
        NamingException ne = new NamingException("Could not acquire stub");
        ne.setRootCause(e);
        throw ne;
      }
    }

    return obj;
  }

  /**
   * @see ContextProxy#onBind(javax.naming.Name, java.lang.Object)
   */
  protected Object onBind(Name n, Object toBind) throws NamingException {
    try {
      return makeStub(n, toBind);
    } catch (IOException e) {
      NamingException ne = new NamingException("Could not enrich stub for binding");
      ne.setRootCause(e);
      ne.fillInStackTrace();
      throw ne;
    }
  }

  /**
   * @see ContextProxy#onRebind(javax.naming.Name, java.lang.Object)
   */
  protected Object onRebind(Name n, Object toBind) throws NamingException {
    return onBind(n, toBind);
  }

  /**
   * @see ContextProxy#onEnum(javax.naming.Name, javax.naming.NamingEnumeration)
   */
  @SuppressWarnings("rawtypes")
  protected NamingEnumeration onEnum(Name n, NamingEnumeration en) {
    return new LocalNamingEnum(baseUrl, n, en);
  }

  /**
   * "see ContextProxy#onSubContext(javax.naming.Name, javax.naming.Context)
   */
  protected Context onSubContext(Name name, Context ctx) throws NamingException {
    return new EmbeddedLocalContext(this.channel, baseUrl, (RemoteContext) ctx);
  }

  protected void doFailOver(UndeclaredThrowableException e) throws NamingException {
    NamingException ne = new NamingException("Unavailable naming service for " + baseUrl);
    ne.setRootCause(e.getUndeclaredThrowable());
  }
  
  /**
   *  This method insures that objects bound to the JNDI tree are indeed stubs (it performs the conversion
   *  to stub if required). This allows using this instance in-JVM without having to do stub conversion
   *  calling {@link Hub#exportObject(Object)} with the object intended for binding to the JNDI beforehand.
   */
  private Object makeStub(Name name, Object toBind) throws RemoteException, IOException {
    Object remote;

    // Object is already in storable form.
    if (toBind instanceof StubContainer) {
      LOG.debug("Object %s already transformed to StubContainer, so not making stub", name);
      return toBind;
    }

    // Object not already a stub, do stubbing
    if (!Stubs.isStub(toBind)) {
      LOG.debug("Making stub for object %s", name);
      remote = Hub.exportObject(toBind);

    // Object is a stub,  using as is.
    } else {
      LOG.debug("Object %s already a stub", name);
      remote = toBind;
    }

    // performing stub enrichment
    LOG.debug("Performing stub enrichment for object %s", name);
    JndiBindingInfo info    = new JndiBindingInfo(baseUrl, name, channel.get().getDomainName(), channel.get().getMulticastAddress());
    remote                  = Hub.getModules().getStubProcessor().enrichForJndiBinding(remote, info);

    // converting to StubContainer for storage in JNDI
    if (Stubs.isStub(remote)) {
      StubInvocationHandler handler = Stubs.getStubInvocationHandler(remote);
      LOG.debug("Got stub invocation handler %s for %s", handler, name);
      // stateless stub: register so that it is updated with new
      // endpoints appearing on the network

      if (handler instanceof RemoteRefStateless) {
        LOG.debug("Registering %s named %s with stateless stub table", handler, name);
        RemoteRefStateless ref = (RemoteRefStateless) handler;
        Hub.getModules().getStatelessStubTable().registerStatelessRef(ref, ref.getContexts());
      }
      return handler.toStubContainer(remote);
    }
    return remote;
  }
}
