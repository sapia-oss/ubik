package org.sapia.ubik.mcast.control;

import static org.junit.Assert.assertEquals;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.junit.Test;
import org.sapia.ubik.mcast.McastUtil;
import org.sapia.ubik.rmi.server.transport.socket.MultiplexSocketAddress;
import org.sapia.ubik.util.SysClock;
import org.sapia.ubik.util.Collects;

public class ControlRequestTest {

  @Test
  public void testSplit() {
    Set<String> targetedNodes = new HashSet<String>();
    for (int i = 0; i < 100; i++) {
      targetedNodes.add("node_" + i);
    }
    TestControlRequest req = new TestControlRequest(targetedNodes);

    List<SplitteableMessage> splits = req.split(10);
    int total = 0;
    for (int i = 0; i < splits.size(); i++) {
      assertEquals(10, splits.get(i).getTargetedNodes().size());
      List<SplitteableMessage> nestedSplits = splits.get(i).split(5);
      for (int j = 0; j < nestedSplits.size(); j++) {
        assertEquals(2, nestedSplits.get(j).getTargetedNodes().size());
        total += nestedSplits.get(j).getTargetedNodes().size();
      }
    }
    assertEquals(100, total);
  }

  @Test
  public void testBatchSizeLargerSplit() {
    Set<String> targetedNodes = Collects.arrayToSet("1", "2");
    TestControlRequest req = new TestControlRequest(targetedNodes);
    assertEquals(2, req.split(3).size());
  }
  
  @Test
  public void testBatchSizeSmallerSplit() {
    Set<String> targetedNodes = Collects.arrayToSet("1", "2", "3", "4", "5");
    TestControlRequest req = new TestControlRequest(targetedNodes);
    assertEquals(3, req.split(3).size());
  }

  @Test
  public void testSizeFor10Nodes() throws Exception {
    System.out.println("Size with 10 nodes: " + McastUtil.getSizeInBytes(createWith(10)));
  }

  @Test
  public void testSizeFor20Nodes() throws Exception {
    System.out.println("Size with 20 nodes: " + McastUtil.getSizeInBytes(createWith(20)));
  }

  @Test
  public void testSizeFor40Nodes() throws Exception {
    System.out.println("Size with 40 nodes: " + McastUtil.getSizeInBytes(createWith(40)));
  }

  @Test
  public void testSizeFor50Nodes() throws Exception {
    System.out.println("Size with 50 nodes: " + McastUtil.getSizeInBytes(createWith(50)));
  }

  @Test
  public void testSizeFor100Nodes() throws Exception {
    System.out.println("Size with 100 nodes: " + McastUtil.getSizeInBytes(createWith(100)));
  }

  @Test
  public void testSizeFor200Nodes() throws Exception {
    System.out.println("Size with 200 nodes: " + McastUtil.getSizeInBytes(createWith(200)));
  }

  private TestControlRequest createWith(int numberOfNodes) {
    Set<String> nodes = new HashSet<String>();
    for (int i = 0; i < numberOfNodes; i++) {
      nodes.add(UUID.randomUUID().toString());
    }
    return new TestControlRequest(0, nodes);
  }

  public static class TestControlRequest extends ControlRequest {

    public TestControlRequest() {
    }

    public TestControlRequest(long requestId, Set<String> targetedNodes) {
      super(SysClock.RealtimeClock.getInstance(), requestId, "master", new MultiplexSocketAddress("test", 1), targetedNodes);
    }

    public TestControlRequest(Set<String> targetedNodes) {
      super(targetedNodes);
    }

    @Override
    protected ControlRequest getCopy(Set<String> targetedNodes) {
      return new TestControlRequest(targetedNodes);
    }
  }

}
