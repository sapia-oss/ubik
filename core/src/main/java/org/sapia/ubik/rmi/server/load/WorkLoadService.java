package org.sapia.ubik.rmi.server.load;

import java.util.Random;

import org.sapia.ubik.util.Conf;

public class WorkLoadService implements LoadService {

  public static final String MIN_PAUSE = "ubik.rmi.server.load.client.minPause";
  public static final String MAX_PAUSE = "ubik.rmi.server.load.client.maxPause";
  public static final String NUMBER_OF_LOOPS = "ubik.rmi.server.load.client.numberOfLoopss";

  public static final int DEFAULT_MIN_PAUSE = 5;
  public static final int DEFAULT_MAX_PAUSE = 25;
  public static final int DEFAULT_NUMBER_OF_LOOPS = 25;

  private int minPause, maxPause;
  private int numberOfLoops;

  public WorkLoadService() {
    Conf props = Conf.getSystemProperties();
    minPause = props.getIntProperty(MIN_PAUSE, DEFAULT_MIN_PAUSE);
    maxPause = props.getIntProperty(MAX_PAUSE, DEFAULT_MAX_PAUSE);
    numberOfLoops = props.getIntProperty(NUMBER_OF_LOOPS, DEFAULT_NUMBER_OF_LOOPS);
  }

  @Override
  public void perform() {

    for (int i = 0; i < numberOfLoops; i++) {
      int pause = minPause + new Random().nextInt(maxPause);
      try {
        doWork();
        Thread.sleep(pause);
      } catch (InterruptedException e) {
        break;
      }
    }

  }

  private void doWork() {

  }

}
