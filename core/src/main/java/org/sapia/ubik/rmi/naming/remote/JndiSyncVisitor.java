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

  @SuppressWarnings("unchecked")
  @Override
  public boolean visit(Node paramNode) {
    Iterator<NamePart> valueNames = paramNode.getValueNames();
    while (valueNames.hasNext()) {
      NamePart valueName = valueNames.next();
      Name fullName = paramNode.getAbsolutePath().add(valueName);
      Object value = paramNode.getValue(valueNames.next());
      if (value instanceof StubContainer) {
        StubContainer container = (StubContainer) value;
        countsByNames.put(fullName.toString(), container.getStubInvocationHandler().getContexts().size());
      }
    }
    return true;
  }
}
