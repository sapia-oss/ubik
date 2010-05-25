package org.sapia.ubik.rmi.server.transport.http;

import java.util.HashMap;
import java.util.Map;

import org.simpleframework.http.Request;
import org.simpleframework.http.Response;
import org.simpleframework.http.core.Container;
/**
 * An instance of this class maps context paths to {@link Container}s. (as specified by the Simple API).
 *
 * @author Yanick Duchesne
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2010 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class ContainerMap implements Container {
  
  private Map<String, Container> _services = new HashMap<String, Container>();

  public ContainerMap() {
  }

  /**
   * Adds the given service to this object; internally maps it to the given
   * context path.
   *
   * @param contextPath the path that follows the host:port in a HTTP URL.
   * @param svc a <code>Service</code> instance, as specified by the Simple API.
   */
  public void addService(String contextPath, Container svc) {
    if (contextPath == null) {
      contextPath = "/";
    }

    if (!contextPath.startsWith("/")) {
      contextPath = "/" + contextPath;
    }

    _services.put(contextPath, svc);
  }

  public void handle(Request req, Response res) {
    if(req.getPath() != null){
      String[] segments = req.getPath().getSegments();
      if(segments.length > 0){
        Container c = (Container) _services.get(segments[0]);
        if (c != null) {
          c.handle(req, res);
        }
        
      }
      
    }
  }
}
