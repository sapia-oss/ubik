package org.sapia.ubik.mcast;

import org.sapia.ubik.util.throttle.Throttle;

class NodeInfoWrapper {

  private NodeInfo nodeInfo;
  private Throttle throttle;
  
  NodeInfoWrapper(NodeInfo nodeInfo, Throttle throttle) {
    this.nodeInfo = nodeInfo;
    this.throttle = throttle;
  }
  
  NodeInfo getNodeInfo() {
    return nodeInfo;
  }
  
  Throttle getThrottle() {
    return throttle;
  }
  
}
