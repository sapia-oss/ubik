package org.sapia.ubik.rmi.server.command;

import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.List;


/**
 * @author Yanick Duchesne
 *
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class ResponseQueueTest extends TestCase {
  public ResponseQueueTest(String name) {
    super(name);
  }

  public void testShutdown() throws Exception {
    ResponseQueue queue = new ResponseQueue();
    ResponseLock  lock  = queue.createResponseLock();
    long          start = System.currentTimeMillis();
    queue.shutdown(1000);
    super.assertTrue((System.currentTimeMillis() - start) > 700);
    super.assertEquals(1, queue.size());
  }

  public void testShutdownWithResponse() throws Exception {
    final ResponseQueue queue = new ResponseQueue();
    final ResponseLock  lock  = queue.createResponseLock();
    long                start = System.currentTimeMillis();
    Thread              adder = new Thread(new Runnable() {
          public void run() {
            try {
              Thread.sleep(1000);

              List responses = new ArrayList();
              responses.add(new Response(lock.getId(), null));
              queue.onResponses(responses);
              lock.waitResponse(1000);
            } catch (Exception e) {
              e.printStackTrace();
            }
          }
        });
    adder.start();
    queue.shutdown(2000);
    super.assertEquals(0, queue.size());
  }
}
