package org.sapia.ubik.rmi.naming.remote.proxy;

import java.lang.reflect.UndeclaredThrowableException;
import java.rmi.RemoteException;
import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NameParser;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;

import org.sapia.archie.jndi.proxy.ContextProxy;
import org.sapia.ubik.mcast.DomainName;
import org.sapia.ubik.rmi.naming.remote.DomainInfo;
import org.sapia.ubik.rmi.naming.remote.RemoteContext;
import org.sapia.ubik.rmi.naming.remote.StubTweaker;
import org.sapia.ubik.rmi.server.StubContainer;


/**
 * @author Yanick Duchesne
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class LocalContext extends ContextProxy implements java.rmi.Remote {
  protected DomainName _domainName;
  protected String     _mcastAddress;
  protected int        _mcastPort;
  protected String     _url;

  public LocalContext(String url, RemoteContext remote) throws NamingException {
    super(remote);
    _url = url;

    DomainInfo info = remote.getDomainInfo();
    _domainName     = info.getDomainName();
    _mcastAddress   = info.getMulticastAddress();
    _mcastPort      = info.getMulticastPort();
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
  public Hashtable getEnvironment() throws NamingException {
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
        return ((StubContainer) obj).toStub(Thread.currentThread()
                                                  .getContextClassLoader());
      } catch (RemoteException e) {
        NamingException ne = new NamingException("Could not acquire stub");
        ne.setRootCause(e);
      }
    }

    return obj;
  }

  /**
   * @see ContextProxy#onBind(javax.naming.Name, java.lang.Object)
   */
  protected Object onBind(Name n, Object toBind) throws NamingException {
    toBind = StubTweaker.tweak(_url, n, _domainName, _mcastAddress, _mcastPort,
             toBind);
    return toBind;
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
  protected NamingEnumeration onEnum(Name n, NamingEnumeration en) {
    return new LocalNamingEnum(_url, n, en);
  }

  /**
   * "see ContextProxy#onSubContext(javax.naming.Name, javax.naming.Context)
   */
  protected Context onSubContext(Name name, Context ctx)
    throws NamingException {
    return new LocalContext(_url, (RemoteContext) ctx);
  }

  protected void doFailOver(UndeclaredThrowableException e)
    throws NamingException {
    NamingException ne = new NamingException("Unavailable naming service for " +
        _url);
    ne.setRootCause(e.getUndeclaredThrowable());
  }
}
