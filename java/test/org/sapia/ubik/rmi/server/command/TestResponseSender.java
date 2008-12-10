package org.sapia.ubik.rmi.server.command;

import java.util.List;


/**
 * @author Yanick Duchesne
 * 10-Sep-2003
 */
public class TestResponseSender implements ResponseSender {
  /**
   * Constructor for TestResponseSender.
   */
  public TestResponseSender() {
    super();
  }

  /**
   * @see org.sapia.ubik.rmi.server.command.ResponseSender#sendResponses(Destination, List)
   */
  public void sendResponses(Destination destination, List responses) {
    try {
      Thread.sleep(1000);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }
}
