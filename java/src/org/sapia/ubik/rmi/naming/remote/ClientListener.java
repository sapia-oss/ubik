package org.sapia.ubik.rmi.naming.remote;

import java.io.IOException;

import org.sapia.ubik.mcast.AsyncEventListener;
import org.sapia.ubik.mcast.EventChannel;
import org.sapia.ubik.mcast.RemoteEvent;
import org.sapia.ubik.net.ServerAddress;
import org.sapia.ubik.rmi.server.Log;


/**
 * @author Yanick Duchesne
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2004 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class ClientListener implements AsyncEventListener {
  private EventChannel  _channel;
  private ServerAddress _addr;

  public ClientListener(EventChannel channel, ServerAddress addr) {
    _channel   = channel;
    _addr      = addr;
  }

  /**
   * @see org.sapia.ubik.mcast.AsyncEventListener#onAsyncEvent(RemoteEvent)
   */
  public void onAsyncEvent(RemoteEvent evt) {
    if (evt.getType().equals(Consts.JNDI_CLIENT_PUBLISH)) {
      ServerAddress addr = _channel.getView().getAddressFor(evt.getNode());


      try {
        if (addr != null) {        
          if(Log.isDebug()){
            Log.debug(getClass(), "Dispatching JNDI discovery event to expecting client: " + addr);
          }
          _channel.dispatch(addr, Consts.JNDI_SERVER_DISCO, _addr);          
        }
        else{
          if(Log.isDebug()){
            Log.debug(getClass(), "Dispatching JNDI discovery event to whole domain");
          }          
          _channel.dispatch(Consts.JNDI_SERVER_DISCO, _addr);
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }
}
