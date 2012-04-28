package org.sapia.ubik.mcast.control;

import static org.junit.Assert.assertEquals;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;
import org.sapia.ubik.util.Collections2;

public class ControlNotificationTest {
  
  @Test
  public void testNestedSplit() {
    Set<String> targetedNodes = new HashSet<String>();
    for (int i = 0; i < 100; i++) {
      targetedNodes.add("node_" + i);
    }
    TestControlNotification notif = new TestControlNotification(targetedNodes);
    
    List<SplittableMessage> splits = notif.split(10);
    for (int i = 0; i < splits.size(); i++) {
      assertEquals(10, splits.get(i).getTargetedNodes().size());
      List<SplittableMessage> nestedSplits = splits.get(i).split(5);
      for (int j = 0; j < nestedSplits.size(); j++) {
        assertEquals(2, nestedSplits.get(j).getTargetedNodes().size());
      }
    }
  }
  
  @Test
  public void testBatchSizeLargerSplit() {
    Set<String> targetedNodes = Collections2.arrayToSet("1", "2");
    TestControlNotification notif = new TestControlNotification(targetedNodes);
    assertEquals(1, notif.split(3).size());
  }
  
  public static class TestControlNotification extends ControlNotification {
    
    public TestControlNotification(Set<String> targetedNodes) {
      super(targetedNodes);
    }
    
    
    @Override
    protected ControlNotification getCopy(Set<String> targetedNodes) {
      return new TestControlNotification(targetedNodes);
    }
  }
}
