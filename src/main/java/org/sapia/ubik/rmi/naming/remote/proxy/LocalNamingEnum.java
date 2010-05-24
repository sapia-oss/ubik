package org.sapia.ubik.rmi.naming.remote.proxy;

import javax.naming.Binding;
import javax.naming.Name;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;

import org.sapia.archie.jndi.proxy.EnumProxy;
import org.sapia.ubik.rmi.naming.remote.RemoteContext;


/**
 * @author Yanick Duchesne
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class LocalNamingEnum extends EnumProxy {
  private String _url;

  public LocalNamingEnum(String url, Name ctxName, NamingEnumeration en) {
    super(ctxName, en);
    _url = url;
  }

  public Object onNextElement(Name contextName, Object next)
    throws NamingException {
    if (next instanceof Binding &&
          ((Binding) next).getObject() instanceof RemoteContext) {
      Binding b = (Binding) next;
      b.setObject(new LocalContext(_url, (RemoteContext) b.getObject()));

      return b;
    } else if (next instanceof RemoteContext) {
      return new LocalContext(_url, (RemoteContext) next);
    } else {
      return next;
    }
  }
}
