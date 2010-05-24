package org.sapia.ubik.rmi.naming.remote.archie;

import org.sapia.archie.Entry;
import org.sapia.archie.NamePart;
import org.sapia.archie.ProcessingException;
import org.sapia.archie.impl.AttributeNode;
import org.sapia.archie.impl.Offer;
import org.sapia.archie.sync.SynchronizedNode;

import org.sapia.ubik.rmi.naming.remote.StubTweaker;
import org.sapia.ubik.rmi.server.HealthCheck;
import org.sapia.ubik.rmi.server.Stub;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

import java.rmi.Remote;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;


/**
 * @author Yanick Duchesne
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class UbikSyncNode extends SynchronizedNode implements java.rmi.Remote {
  public UbikSyncNode(UbikNodeFactory fac) throws ProcessingException {
    super(new UbikReliableNode(fac));
  }

  static final class UbikReliableNode extends AttributeNode implements Remote {
    public UbikReliableNode(UbikNodeFactory fac) throws ProcessingException {
      super(new HashMap(), new HashMap(), fac);
    }

    /**
     * @see org.sapia.archie.impl.MultiValueNode#onWrite(org.sapia.archie.NamePart, java.lang.Object)
     */
    protected Object onWrite(NamePart name, Object obj) {
      Iterator itr     = getEntries();
      List     entries = new ArrayList();
      Entry    entry;

      while (itr.hasNext()) {
        entry = (Entry) itr.next();
        entries.add(entry.getValue());
      }
      
      return StubTweaker.tweak(entries.iterator(), obj);
    }

    /**
     * @see org.sapia.archie.impl.AttributeNode#onSelectOffer(java.util.List)
     */
    protected Offer onSelectOffer(List offers) {
      return super.onSelectOffer(offers);
    }

    /**
     * @see org.sapia.archie.impl.AttributeNode#isValid(org.sapia.archie.impl.Offer)
     */
    protected boolean isValid(Offer offer) {
      Object stub = offer.getObject();

      if (stub instanceof Stub && Proxy.isProxyClass(stub.getClass())) {
        InvocationHandler handler = Proxy.getInvocationHandler(stub);

        if (handler instanceof HealthCheck) {
          try {
            return ((HealthCheck) handler).isValid();
          } catch (Exception e) {
            return false;
          }
        }

        return true;
      } else if (stub instanceof HealthCheck) {
        try {
          boolean valid = ((HealthCheck) stub).isValid();

          return valid;
        } catch (Exception e) {
          return false;
        }
      } else {
        return true;
      }
    }
  }
}
