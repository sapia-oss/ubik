package org.sapia.ubik.rmi.naming.remote;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.HashMap;
import java.util.Map;

public class JndiSyncRequest implements Externalizable {

  private String[] names;
  private int[]    counts;

  /**
   * Meant for externalization only.
   */
  public JndiSyncRequest() {
  }

  JndiSyncRequest(String[] names, int[] counts) {
    this.names  = names;
    this.counts = counts;
  }

  /**
   * @return the {@link Map} of name-to-count entries that this instance holds.
   */
  public Map<String, Integer> asMap() {
    Map<String, Integer> elements = new HashMap<String, Integer>();
    for (int i = 0; i < names.length; i++) {
      elements.put(names[i], counts[i]);
    }
    return elements;
  }

  /**
   * @param other a {@link Map} holding name-to-count entries to compare with this instance.
   * @return the {@link Map} of name-to-diff entries that this method as computed.
   */
  public Map<String, Integer> diff(Map<String, Integer> other) {
    Map<String, Integer> diff         = new HashMap<>();
    Map<String, Integer> thisInstance = asMap();
    for (Map.Entry<String, Integer> e: other.entrySet()) {
      Integer thisCount = thisInstance.get(e.getKey());
      if (thisCount == null) {
        thisCount = 0;
      }
      diff.put(e.getKey(), thisCount - other.get(e.getKey()));
    }

    for (Map.Entry<String, Integer> e: thisInstance.entrySet()) {
      Integer otherCount = other.get(e.getKey());
      if (otherCount == null) {
        otherCount = 0;
      }
      diff.put(e.getKey(), thisInstance.get(e.getKey()) - otherCount);
    }

    return diff;
  }

  @Override
  public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
    names = (String[]) in.readObject();
    counts = (int[]) in.readObject();
  }

  @Override
  public void writeExternal(ObjectOutput out) throws IOException {
    out.writeObject(names);
    out.writeObject(counts);
  }

  /**
   * @param countsByNames a {@link Map} of JNDI binding names vs the number of remote
   * references per binding.
   *
   * @return a new {@link JndiSyncRequest}.
   */
  public static JndiSyncRequest newInstance(Map<String, Integer> countsByNames) {
    String[] names  = new String[countsByNames.size()];
    int[]    counts = new int[countsByNames.size()];
    int index = 0;
    for (String name : countsByNames.keySet()) {
      names[index] = name;
      counts[index] = countsByNames.get(name);
      index++;
    }
    return new JndiSyncRequest(names, counts);
  }

}
