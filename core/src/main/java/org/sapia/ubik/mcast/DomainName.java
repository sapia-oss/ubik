package org.sapia.ubik.mcast;

import java.util.ArrayList;
import java.util.List;

/**
 * Models a "domain name" that is expressed by a path. Allows to filter
 * multicast events on a per-domain basis. Supports the notion of domain
 * partition (where a domain can have subdomains - or partitions).
 * <p>
 * Partioning is expressed through a path notation, where the parent domain can
 * "contain" subdomains (that necessarily include their parent's path).
 * <p>
 * This notion of containment is used to determine to which domain or partition
 * in a domain a multicast event is targeted. For example, an event that is
 * multicast to domain "parent" will also be received by the following
 * partitions "parent/partition1", "parent/partition2". Yet, the opposite would
 * not be true: an event targeted at "parent/partition2" would not be received
 * by the others - since the latter do not "contain" or "include" the former.
 * <p>
 * An instance of this class is created as follows:
 * 
 * <pre>
 * DomainName dn = DomainName.parse(&quot;domain/partition&quot;);
 * </pre>
 * 
 * Specifying subdomains/partitions is not mandatory.
 * 
 * @author Yanick Duchesne
 */
public class DomainName implements java.io.Serializable {

  static final long serialVersionUID = 1L;

  public static final char DELIM = '/';
  private List<String> segments;

  private DomainName(List<String> segments) {
    this.segments = segments;
  }

  /**
   * Returns the number of "components" in this instance's name.
   * 
   * @return the number of "components" in this instance's name.
   */
  public int size() {
    return segments.size();
  }

  /**
   * Returns the component whose index is given.
   * 
   * @param i
   *          an index.
   * @return a domain name component.
   */
  public String get(int i) {
    return segments.get(i);
  }

  /**
   * Creates an instance of this class out of the given name/path.
   * 
   * @param name
   *          a domain name/path.
   * @return a <code>DomainName</code> object.
   */
  public static DomainName parse(String name) {
    List<String> _segments = new ArrayList<String>(5);

    int idx = 0;
    int lastIdx = 0;

    while ((idx = name.indexOf(DELIM, idx)) > -1) {
      if (idx > 0) {
        _segments.add(name.substring(lastIdx, idx));
      }

      lastIdx = ++idx;
    }

    if (idx < name.length()) {
      _segments.add(name.substring(lastIdx));
    }

    return new DomainName(_segments);
  }

  /**
   * Returns <code>true</code> if this instance "contains" or "includes" the
   * domain name passed in.
   * 
   * @param other
   *          a <code>DomainName</code>.
   */
  public boolean contains(DomainName other) {
    if (segments.size() >= other.size()) {
      for (int i = 0; i < other.size(); i++) {
        if (!other.get(i).equals(get(i))) {
          return false;
        }
      }

      return true;
    } else {
      return false;
    }
  }

  /**
   * Compares this instance with another <code>DomainName</code>.
   * 
   * @param other
   *          an {@link Object}.
   * 
   * @return <code>true</code> if the passed in object is a {@link DomainName}
   *         instance and if it has the same domain name string as this
   *         instance's.
   */
  public boolean equals(Object other) {
    try {
      DomainName dn = (DomainName) other;

      if (segments.size() == dn.size()) {
        for (int i = 0; i < dn.size(); i++) {
          if (!dn.get(i).equals(get(i))) {
            return false;
          }
        }

        return true;
      } else {
        return false;
      }
    } catch (ClassCastException e) {
      return false;
    }
  }

  /**
   * Returns a string representation of this instance - or, more precisely, this
   * instance's path representation, where subdomains/partitions are separated
   * by '/' characters.
   * 
   * @return this instance's string representation.
   */
  public String toString() {
    StringBuffer s = new StringBuffer(segments.size() * 8);

    for (int i = 0; i < segments.size(); i++) {
      s.append((String) segments.get(i));

      if (i < (segments.size() - 1)) {
        s.append(DELIM);
      }
    }

    return s.toString();
  }
}
