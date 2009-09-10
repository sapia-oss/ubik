package org.sapia.ubik.test.gc;

import java.rmi.Remote;

public class CallbackServiceImpl implements Remote, CallbackService {

  private String _serviceId;
  private CallbackService _delegate;
  
  /**
   * Creates a new {@link CallbackServiceImpl} instance.
   */
  public CallbackServiceImpl(String aServiceId) {
    _serviceId = aServiceId;
  }

  /**
   * Changes the delegate.
   *
   * @param aDelegate The new delegate value.
   */
  public void setDelegate(CallbackService aDelegate) {
    _delegate = aDelegate;
  }

  /* (non-Javadoc)
   * @see org.sapia.ubik.test.gc.CallbackService#callback(java.lang.String)
   */
  public void callback(String aValue) {
    System.out.println("CallbackServiceImpl ==> called back with value: " + aValue);
    if (_delegate != null) _delegate.callback(aValue);
  }

  /* (non-Javadoc)
   * @see org.sapia.ubik.test.gc.CallbackService#getServiceId()
   */
  public String getServiceId() {
    return _serviceId;
  }
}
