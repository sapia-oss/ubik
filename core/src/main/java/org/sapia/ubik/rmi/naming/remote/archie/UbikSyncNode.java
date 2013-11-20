package org.sapia.ubik.rmi.naming.remote.archie;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.rmi.Remote;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.sapia.archie.ProcessingException;
import org.sapia.archie.impl.AttributeNode;
import org.sapia.archie.impl.Offer;
import org.sapia.archie.sync.SynchronizedNode;
import org.sapia.ubik.log.Category;
import org.sapia.ubik.log.Log;
import org.sapia.ubik.rmi.server.stub.HealthCheck;
import org.sapia.ubik.rmi.server.stub.Stub;

/**
 * @author Yanick Duchesne
 */
public class UbikSyncNode extends SynchronizedNode implements java.rmi.Remote {

  public UbikSyncNode(UbikNodeFactory fac) throws ProcessingException {
    super(new UbikReliableNode(fac));
  }

  static final class UbikReliableNode extends AttributeNode implements Remote {

    private Category log = Log.createCategory(getClass());

    @SuppressWarnings("rawtypes")
    UbikReliableNode(UbikNodeFactory fac) throws ProcessingException {
      super(new ConcurrentHashMap(), new ConcurrentHashMap(), fac);
    }

    /**
     * @see org.sapia.archie.impl.AttributeNode#onSelectOffer(java.util.List)
     */
    @SuppressWarnings("rawtypes")
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
            log.debug("Performing health check for %s", handler);
            boolean valid = ((HealthCheck) handler).isValid();
            log.debug("Health check for %s, valid = %s", handler, valid);
          } catch (Exception e) {
            return false;
          }
        }

        return true;
      } else if (stub instanceof HealthCheck) {
        try {
          log.debug("Performing health check for %s", stub);
          boolean valid = ((HealthCheck) stub).isValid();
          log.debug("Health check for %s, valid = %s", stub, valid);
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
