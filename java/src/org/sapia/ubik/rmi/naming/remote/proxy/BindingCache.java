package org.sapia.ubik.rmi.naming.remote.proxy;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.lang.ref.SoftReference;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.naming.Context;
import javax.naming.NamingException;

import org.sapia.ubik.mcast.DomainName;
import org.sapia.ubik.rmi.server.Stub;
import org.sapia.ubik.rmi.server.StubInvocationHandler;


/**
 * @author Yanick Duchesne
 *
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class BindingCache implements Externalizable {
  private List _services = Collections.synchronizedList(new ArrayList());

  public BindingCache() {
  }

  public synchronized void add(String domainName, String name, Object o) {
    _services.add(new BoundRef(domainName, name, o));
  }

  public synchronized List cachedRefs() {
    return _services;
  }

  public void copyTo(Context ctx, DomainName domain) {
    BoundRef ref;

    for (int i = 0; i < _services.size(); i++) {
      ref = (BoundRef) _services.get(i);

      if (ref.obj == null) {
        continue;
      }

      try {
        if ((ref.domainName != null) && domain.contains(ref.domainName)) {
          ctx.rebind(ref.name, ref.obj);
        }
      } catch (NamingException e) {
        //noop;
      }
    }
  }

  public void readExternal(ObjectInput in)
    throws IOException, ClassNotFoundException {
    _services = (List) in.readObject();
  }

  public void writeExternal(ObjectOutput out) throws IOException {
    BoundRef ref;

    synchronized (_services) {
      for (int i = 0; i < _services.size(); i++) {
        ref = (BoundRef) _services.get(i);

        if (ref.isNull()) {
          _services.remove(i);
          --i;
        }
      }
    }

    out.writeObject(_services);
  }

  public static class BoundRef implements Externalizable {
    public String     name;
    public Object     obj;
    public DomainName domainName;

    /**
     * Do not use this constructor; meant for externalization.
     */
    public BoundRef() {
    }

    BoundRef(String domainName, String name, Object o) {
      this.domainName   = DomainName.parse(domainName);
      this.name         = name;
      this.obj          = new SoftReference(o);
    }

    public void readExternal(ObjectInput in)
      throws IOException, ClassNotFoundException {
      this.name         = in.readUTF();
      this.obj          = in.readObject();
      this.domainName   = (DomainName) in.readObject();
    }

    public void writeExternal(ObjectOutput out) throws IOException {
      out.writeUTF(name);

      Object toWrite;

      if (obj instanceof SoftReference) {
        toWrite = ((SoftReference) obj).get();
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

      if (obj instanceof SoftReference) {
        return ((SoftReference) obj).get() == null;
      }

      return false;
    }
  }
}
