package org.sapia.ubik.test.gc;

public interface EchoService {

  public enum EchoMode {
    SYNCHRONOUS,
    ASYNCHRONOUS;
  }
  
  public void registerCallback(CallbackService aCallback);
  
  public void unregisterCallback(CallbackService aCallback);
  
  public void echo(String aValue, EchoMode aMode);
  
}
