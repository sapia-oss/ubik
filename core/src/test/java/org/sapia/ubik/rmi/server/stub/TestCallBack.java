package org.sapia.ubik.rmi.server.stub;

import java.rmi.Remote;

import org.sapia.ubik.rmi.Callback;

@Callback
public class TestCallBack implements TestCallbackInterface, Remote {

  int counter;

  @Override
  public int callMethod() {
    counter++;
    return counter;
  }

}
