package org.sapia.ubik.rmi.server.command;

import junit.framework.TestCase;

import org.sapia.ubik.net.ServerAddress;
import org.sapia.ubik.rmi.server.VmId;


/**
 * @author Yanick Duchesne
 * 10-Sep-2003
 */
public class OutQueueTest extends TestCase {
  /**
   * Constructor for OutQueueTest.
   */
  public OutQueueTest(String name) {
    super(name);
  }

  public void testShutdownAll() throws Exception {
    OutQueue.setResponseSender(new TestResponseSender());

    CommandProcessor proc = new CommandProcessor(1);
    VmId             vmid = VmId.getInstance();
    ServerAddress    addr = new TestAddress("testAddress");
    proc.processAsyncCommand("cmdId", vmid, addr,
      new TestCommand() {
        public Object execute() throws Throwable {
          try {
            Thread.sleep(500);
          } catch (InterruptedException e) {
          }

          return null;
        }
      });
    proc.shutdown(2000);

    OutQueue queue = OutQueue.getQueueFor(new Destination(addr, vmid));
    super.assertEquals(0, queue.size());
  }

  public static class TestAddress implements ServerAddress {
    String _id;

    TestAddress(String id) {
      _id = id;
    }

    /**
     * @see org.sapia.ubik.net.ServerAddress#getTransportType()
     */
    public String getTransportType() {
      return "testTransport";
    }

    public int hashCode() {
      return _id.hashCode();
    }

    /**
     * @see org.sapia.ubik.net.ServerAddress#equals(Object)
     */
    public boolean equals(Object o) {
      return _id.equals(o);
    }
  }

  static class TestCommand extends Command {
    /**
     * Constructor for TestCommand.
     */
    public TestCommand() {
      super();
    }

    /**
     * @see org.sapia.ubik.rmi.server.command.Command#execute()
     */
    public Object execute() throws Throwable {
      return null;
    }
  }
}
