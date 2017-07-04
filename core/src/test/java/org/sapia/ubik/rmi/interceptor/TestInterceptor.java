package org.sapia.ubik.rmi.interceptor;

public class TestInterceptor {
  int count;

  public void onTestEvent(TestEvent evt) {
    count++;
  }
  
  public void onUnregisteredEvent(TestUnregisteredEvent evt) {
    count++;
  }
}
