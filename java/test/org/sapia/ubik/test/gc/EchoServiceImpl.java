package org.sapia.ubik.test.gc;

import java.rmi.Remote;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.sapia.ubik.rmi.server.Log;

public class EchoServiceImpl implements Remote, EchoService {

  private HashMap<String, CallbackService> _callbacks;
  private Executor _executor;
  
  /**
   * Creates a new {@link EchoServiceImpl} instance.
   */
  public EchoServiceImpl() {
    _callbacks = new HashMap<String, CallbackService>();
    _executor = Executors.newFixedThreadPool(3);
  }

  /* (non-Javadoc)
   * @see org.sapia.ubik.test.gc.EchoService#echo(java.lang.String, org.sapia.ubik.test.gc.EchoService.EchoMode)
   */
  public void echo(String aValue, EchoMode aMode) {
    System.out.println("EchoServiceImpl ===> called to echo the value '" + aValue + "' to registered callbacks");
    PerformEchoTask task = new PerformEchoTask(aValue, _callbacks.values());
    if (EchoMode.ASYNCHRONOUS == aMode) {
      _executor.execute(task);
    } else {
      task.run();
    }
  }

  /* (non-Javadoc)
   * @see org.sapia.ubik.test.gc.EchoService#registerCallback(org.sapia.ubik.test.gc.CallbackService)
   */
  public void registerCallback(CallbackService aCallback) {
    synchronized (_callbacks) {
      _callbacks.put(aCallback.getServiceId(), aCallback);
    }
  }

  /* (non-Javadoc)
   * @see org.sapia.ubik.test.gc.EchoService#unregisterCallback(org.sapia.ubik.test.gc.CallbackService)
   */
  public void unregisterCallback(CallbackService aCallback) {
    synchronized (_callbacks) {
      _callbacks.remove(aCallback.getServiceId());
    }
  }
  
  
  /**
   * 
   * @author jcdesrochers
   */
  public class PerformEchoTask implements Runnable {
    private String _value;
    private ArrayList<CallbackService> _toCall;
    public PerformEchoTask(String aValue, Collection<CallbackService> someCallbacks) {
      _value = aValue;
      _toCall = new ArrayList<CallbackService>(someCallbacks);
    }
    /* (non-Javadoc)
     * @see java.lang.Runnable#run()
     */
    public void run() {
      try {
        for (CallbackService callback: _toCall) {
          try {
            callback.callback(_value);
          } catch (Exception e) {
            Log.error(EchoServiceImpl.class, "Error calling back service with value: " + _value + "\n\t" + callback, e);
          }
        }
      } catch (Exception e) {
        Log.error(EchoServiceImpl.class, "System error executing echo task", e);
      }
    }
    
  }
}
