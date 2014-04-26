package org.sapia.ubik.rmi.naming.remote;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.sapia.archie.Name;
import org.sapia.archie.NamePart;
import org.sapia.archie.Node;
import org.sapia.archie.NodeVisitor;
import org.sapia.ubik.rmi.server.stub.StubContainer;

class JndiSyncVisitor implements NodeVisitor {

  private Map<String, Integer> countsByNames = new HashMap<String, Integer>();

  /**
   * @return a new {@link JndiSyncRequest}.
   */
  public JndiSyncRequest getSyncRequest() {
    return JndiSyncRequest.newInstance(countsByNames);
  }

  /**
   * @return the map of name-to-count bindings.
   */
  public Map<String, Integer> asMap() {
    return countsByNames;
  }

  @Override
  public boolean visit(Node node) {
    Iterator<NamePart> valueNames = node.getValueNames();
    while (valueNames.hasNext()) {
      NamePart valueName = valueNames.next();
      Name fullName = node.getAbsolutePath().add(valueName);
      Object value = node.getValue(valueName);
      if (value instanceof StubContainer) {
        StubContainer container = (StubContainer) value;
        countsByNames.put(node.getNameParser().asString(fullName), container.getStubInvocationHandler().getContexts().size());
      }
    }
    return true;
  }
}
