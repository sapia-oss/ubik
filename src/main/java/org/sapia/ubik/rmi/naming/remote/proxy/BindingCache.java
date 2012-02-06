package org.sapia.ubik.rmi.naming.remote.proxy;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.lang.ref.SoftReference;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.rmi.RemoteException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NamingException;

import org.sapia.ubik.log.Category;
import org.sapia.ubik.log.Log;
import org.sapia.ubik.mcast.DomainName;
import org.sapia.ubik.mcast.MulticastAddress;
import org.sapia.ubik.rmi.server.Hub;
import org.sapia.ubik.rmi.server.stub.Stub;
import org.sapia.ubik.rmi.server.stub.StubInvocationHandler;
import org.sapia.ubik.rmi.server.stub.enrichment.StubEnrichmentStrategy.JndiBindingInfo;


/**
 * A client-side binding cache.
 * 
 * @author Yanick Duchesne
 */
public class BindingCache implements Externalizable {

  private Category       log      = Log.createCategory(getClass());
  private List<BoundRef> services = new CopyOnWriteArrayList<BoundRef>();

  public BindingCache() {
  }

  public synchronized void add(String domainName, Name name, Object o) {
    services.add(new BoundRef(domainName, name, o));
  }

  public synchronized List<BoundRef> cachedRefs() {
    return services;
  }

  public void copyTo(Context ctx, DomainName domain, String baseUrl, MulticastAddress mcastAddress) {
    BoundRef ref;

    for (int i = 0; i < services.size(); i++) {
      ref = (BoundRef) services.get(i);

      if (ref.obj == null) {
        continue;
      }

      try {
        if ((ref.domainName != null) && domain.contains(ref.domainName)) {
          Object toBind;
          if(ref.obj instanceof SoftReference<?>){
            toBind = ((SoftReference<?>)ref.obj).get();
          }
          else{
            toBind = ref.obj;
          }
          if(toBind != null){
            JndiBindingInfo info = new JndiBindingInfo(baseUrl, ref.name, ref.domainName, mcastAddress);
            try {
              toBind = Hub.getModules().getServerTable().getStubProcessor().enrichForJndiBinding(toBind, info);
              ctx.rebind(ref.name, toBind);
            } catch (RemoteException e) {
              log.error("Could not enrich stub", e);
            }
          }
        }
      } catch (NamingException e) {
        //noop;
      }
    }
  }

  @SuppressWarnings(value="unchecked")
  public void readExternal(ObjectInput in)
    throws IOException, ClassNotFoundException {
    services = (List<BoundRef>) in.readObject();
  }

  public void writeExternal(ObjectOutput out) throws IOException {
    BoundRef ref;

    synchronized (services) {
      for (int i = 0; i < services.size(); i++) {
        ref = (BoundRef) services.get(i);

        if (ref.isNull()) {
          services.remove(i);
          --i;
        }
      }
    }

    out.writeObject(services);
  }

  public static class BoundRef implements Externalizable {
    public Name       name;
    public Object     obj;
    public DomainName domainName;

    /**
     * Do not use this constructor; meant for externalization.
     */
    public BoundRef() {
    }

    BoundRef(String domainName, Name name, Object o) {
      this.domainName   = DomainName.parse(domainName);
      this.name         = name;
      this.obj          = new SoftReference<Object>(o);
    }

    public void readExternal(ObjectInput in)
      throws IOException, ClassNotFoundException {
      this.name         = (Name)in.readObject();
      this.obj          = in.readObject();
      this.domainName   = (DomainName) in.readObject();
    }

    public void writeExternal(ObjectOutput out) throws IOException {
      out.writeObject(name);

      Object toWrite;

      if (obj instanceof SoftReference<?>) {
        toWrite = ((SoftReference<?>)obj).get();
      } else {
        toWrite = obj;
      }

      if (toWrite instanceof Stub && Proxy.isProxyClass(toWrite.getClass())) {
        InvocationHandler handler = Proxy.getInvocationHandler(toWrite);

        if (handler instanceof StubInvocationHandler) {
          toWrite = ((StubInvocationHandler) handler).toStubContainer(toWrite);
        }
      }

      out.writeObject(toWrite);
      out.writeObject(domainName);
    }

    public boolean isNull() {
      if (obj == null) {
        return true;
      }

      if (obj instanceof SoftReference<?>) {
        return ((SoftReference<?>) obj).get() == null;
      }

      return false;
    }
  }
}
