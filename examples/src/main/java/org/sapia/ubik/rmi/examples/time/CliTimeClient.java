package org.sapia.ubik.rmi.examples.time;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.sapia.ubik.log.Log;
import org.sapia.ubik.rmi.server.Hub;


/**
 * @
 */
public class CliTimeClient {
  public static void main(String[] args) {
    Log.setDebug();

    try {
      BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));      
      int i = 0;
      String prompt;
      TimeClient aClient = new TimeClient();
      System.out.println("-- press enter --");
      System.out.println();
      while (true) {
        System.out.print((++i) + ". >>");
        prompt = reader.readLine();
        if(prompt == null){
          break;
        }
        aClient.execute();
        Thread.sleep(1000);
      }
    } catch (InterruptedException ie) {
      System.err.println("The endless time client is interrupted, exiting...");
      ie.printStackTrace();
    } catch (IOException e){
      System.err.println("System prompting input, exiting...");      
    } catch (RuntimeException re) {
      System.err.println("System error running the time client, exiting...");
      re.printStackTrace();
    } finally {
      try {
        Hub.shutdown(30000);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
  }
}
