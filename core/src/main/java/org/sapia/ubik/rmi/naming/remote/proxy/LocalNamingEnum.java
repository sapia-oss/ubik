package org.sapia.ubik.rmi.naming.remote.proxy;

import javax.naming.Binding;
import javax.naming.Name;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;

import org.sapia.archie.jndi.proxy.EnumProxy;
import org.sapia.ubik.rmi.naming.remote.RemoteContext;


/**
 * @author Yanick Duchesne
 */
public class LocalNamingEnum extends EnumProxy {
  private String url;

  public LocalNamingEnum(String url, Name ctxName, NamingEnumeration en) {
    super(ctxName, en);
    this.url = url;
  }

  public Object onNextElement(Name contextName, Object next)
    throws NamingException {
    if (next instanceof Binding &&
          ((Binding) next).getObject() instanceof RemoteContext) {
      Binding b = (Binding) next;
      b.setObject(new LocalContext(url, (RemoteContext) b.getObject()));

      return b;
    } else if (next instanceof RemoteContext) {
      return new LocalContext(url, (RemoteContext) next);
    } else {
      return next;
    }
  }
}
