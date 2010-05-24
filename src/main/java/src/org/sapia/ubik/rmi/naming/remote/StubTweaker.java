package org.sapia.ubik.rmi.naming.remote;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.Iterator;

import javax.naming.Name;
import javax.naming.NamingException;

import org.sapia.ubik.mcast.DomainName;
import org.sapia.ubik.net.Uri;
import org.sapia.ubik.net.UriSyntaxException;
import org.sapia.ubik.rmi.Consts;
import org.sapia.ubik.rmi.server.Hub;
import org.sapia.ubik.rmi.server.RemoteRefReliable;
import org.sapia.ubik.rmi.server.RemoteRefStateless;
import org.sapia.ubik.rmi.server.Stateless;
import org.sapia.ubik.rmi.server.StatelessStubTable;
import org.sapia.ubik.rmi.server.Stub;
import org.sapia.ubik.rmi.server.StubContainer;
import org.sapia.ubik.rmi.server.StubInvocationHandler;


/**
 * @author Yanick Duchesne
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class StubTweaker {
  public static Object tweak(Iterator boundObjects, Object toBind) {
    RemoteRefStateless newRef = null;

    if (toBind instanceof StubContainer &&
          ((StubContainer) toBind).getStubInvocationHandler() instanceof RemoteRefStateless) {
      newRef = (RemoteRefStateless) ((StubContainer) toBind).getStubInvocationHandler();

      RemoteRefStateless oldRef;
      Object             bound;

      while (boundObjects.hasNext()) {
        bound = boundObjects.next();

        if (bound instanceof StubContainer &&
              ((StubContainer) bound).getStubInvocationHandler() instanceof RemoteRefStateless) {
          oldRef = (RemoteRefStateless) ((StubContainer) bound).getStubInvocationHandler();
          newRef.addSibling(oldRef);
        }
      }
    }

    if (newRef != null) {
      StatelessStubTable.registerStatelessRef(newRef);
    }

    return toBind;
  }

  public static Object tweak(String baseUrl, Name name, DomainName domainName,
    String mcastAddress, int mcastPort, Object toBind)
    throws NamingException {
    try {
      if (!(toBind instanceof Stub && Proxy.isProxyClass(toBind.getClass()))) {
        Stub stub = null;

        try {
          if (toBind instanceof Stateless) {
            stub = (Stub) Hub.toStatelessStub(name, domainName.toString(),
                (Stub) Hub.toStub(toBind));
          } else {
            stub   = (Stub) Hub.toStub(toBind);
            stub   = (Stub) Hub.toReliableStub(stub);

            Uri          uri = Uri.parse(baseUrl);
            StringBuffer newUri = new StringBuffer();
            newUri.append(uri.getScheme()).append("://").append(uri.getHost());

            if (uri.getPort() != Uri.UNDEFINED_PORT) {
              newUri.append(':').append("" + uri.getPort());
            }

            newUri.append('/').append(name);

            Uri newUriObj = Uri.parse(newUri.toString());
            newUriObj.getQueryString().addParameter(RemoteInitialContextFactory.UBIK_DOMAIN_NAME,
              domainName.toString());
            newUriObj.getQueryString().addParameter(Consts.MCAST_ADDR_KEY,
              mcastAddress);
            newUriObj.getQueryString().addParameter(Consts.MCAST_PORT_KEY,
              "" + mcastPort);

            if (Proxy.isProxyClass(stub.getClass())) {
              ((RemoteRefReliable) Proxy.getInvocationHandler(stub)).setUp(newUriObj.toString());
            }
          }

          if (Proxy.isProxyClass(stub.getClass())) {
            InvocationHandler handler = Proxy.getInvocationHandler(stub);

            if (handler instanceof StubInvocationHandler) {
              toBind = ((StubInvocationHandler) handler).toStubContainer(stub);
            }
          } else {
            toBind = stub;
          }
        } catch (UriSyntaxException e) {
          NamingException ne = new NamingException("Invalid URI");
          ne.setRootCause(ne);
          throw ne;
        } catch (ClassCastException e) {
          // noop
        }

        return toBind;
      } else {
        return toBind;
      }
    } catch (UndeclaredThrowableException e) {
      NamingException ne = new NamingException("Could not bind object");
      ne.setRootCause(e.getUndeclaredThrowable());
      throw ne;
    } catch (java.rmi.RemoteException e) {
      NamingException ne = new NamingException("Could not bind object");
      ne.setRootCause(e);
      throw ne;
    }
  }
}
