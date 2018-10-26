package org.sapia.ubik.net;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * This class models a query string. A query string has a format similar to the
 * following:
 * 
 * <pre>
 *   name1=value1&name2=value2...&name-N=value-N
 * </pre>
 * 
 * @author yduchesne
 */
public class QueryString implements Externalizable {

  private Map<String, List<String>> parameters;

  private QueryString(Map<String, List<String>> parameters) {
    this.parameters = parameters;
  }

  public QueryString() {
    this(new HashMap<>());
  }

  /**
   * @return a {@link QueryString} {@link Builder}.
   */
  public static Builder builder() {
    return new Builder();
  }

  /**
   * Returns this instance's parameters.
   * 
   * @return a {@link Map} containing this instance's parameters.
   */
  public Map<String, List<String>> getParameters() {
    return Collections.unmodifiableMap(parameters);
  }

  /**
   * @return a {@link Map} of unique parameters.
   */
  public Map<String, String> getUniqueParameters() {
    Map<String, String> toReturn = new HashMap<>();
    parameters.entrySet().forEach(e -> {
      if (e.getValue().isEmpty()) {
        toReturn.put(e.getKey(), "");
      } else {
        toReturn.put(e.getKey(), e.getValue().get(0));
      }
    });
    return toReturn;
  }

  /**
   * @param name
   *          the name of the parameter whose value should be returned.
   * @return the value of the given parameter, or <code>null</code> if no such
   *         value exists.
   */
  public String getParameterValue(String name) {
    List<String> values = parameters.get(name);
    if (values == null || values.isEmpty()) {
      return null;
    }
    return values.get(0);
  }

  /**
   * @param name the name of the parameter for which to return the values.
   * @return the {@link List} of values corresponding to the given parameter name.
   */
  public List<String> getParameterValues(String name) {
    List<String> values = parameters.get(name);
    if (values == null) {
      return Collections.emptyList();
    }
    return values;
  }

  /**
   * @param toAppend a {@link Map} of parameters to append to this instance's parameters.
   * @return a new instance of this class, containing this instance's parameters plus the ones
   * given.
   */
  public QueryString append(Map<String, String> toAppend) {
    QueryString qs = new QueryString();
    qs.parameters.putAll(this.parameters);
    toAppend.entrySet().forEach(p -> qs.getOrCreateParameterValues(p.getKey()).add(p.getValue()));
    return qs;
  }

  /**
   * @param toAppend a {@link QueryString} of parameters to append.
   * @return a new instance of this class, containing this instance's parameters plus the ones
   * given.
   */
  public QueryString append(QueryString toAppend) {
    QueryString qs = new QueryString();
    qs.parameters.putAll(this.parameters);
    qs.parameters.putAll(toAppend.parameters);
    return  qs;
  }

  /**
   * @return <code>true</code> if this instance has no parameters.
   */
  public boolean isEmpty() {
    return parameters.isEmpty();
  }

  /**
   * @return this instance's string representation, with the parameters ordered by name.
   */
  public String toOrderedString() {
    return doToString(new TreeMap<>(parameters));
  }

  @Override
  public String toString() {
    return doToString(parameters);
  }
  
  @Override
  public boolean equals(Object obj) {
    if (obj instanceof QueryString) {
      QueryString other = (QueryString) obj;
      if (parameters.size() == other.parameters.size()) {
    	for (String n : parameters.keySet()) {
    	  List<String> values      = parameters.get(n);
    	  List<String> otherValues = other.parameters.get(n);
    	  if (values != null && otherValues != null) {
    		if(!doEquals(values, otherValues)) {
    		  return false;
    		}
    	  } else if (values == null && otherValues != null){
    		return false;
    	  } else if (values != null && otherValues == null) {
    		return false;
    	  }  
    	}
    	return true;
      }
      return false;
    } 
    return false;
  }
  
  private boolean doEquals(List<String> l1, List<String> l2) {
    if (l1.size() == l2.size()) {
	  for (int i = 0; i < l1.size(); i++) {
		  if(!l2.contains(l1.get(i))) {
			  return false;
		  }
	  }
	  return true;
    }
	return false;
  }

  // --------------------------------------------------------------------------

  @Override
  public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
    parameters = (Map<String, List<String>>) in.readObject();
  }

  @Override
  public void writeExternal(ObjectOutput out) throws IOException {
    out.writeObject(parameters);
  }

  // --------------------------------------------------------------------------
  // Restricted

  // Visible fo testing
  List<String> getOrCreateParameterValues(String name) {
    List<String> values = parameters.get(name);
    if (values == null) {
      values = new ArrayList<>();
      parameters.put(name, values);
    }
    return values;
  }

  private static String doToString(Map<String, List<String>> parameters) {
    StringBuffer buf = new StringBuffer();
    if (!parameters.isEmpty()) {

      final AtomicInteger count = new AtomicInteger(0);

      parameters.entrySet().forEach(entry -> {
        if (count.get() > 0) {
          buf.append('&');
        }

        String       name   = entry.getKey();
        List<String> values = entry.getValue();
        values.forEach(v -> {
          buf.append(name).append('=').append(v);
          count.incrementAndGet();
        });

      });
    }
    return buf.toString();
  }

  // ==========================================================================
  // Builder

  public final static class Builder {

    private Map<String, List<String>> parameters = new HashMap<>();

    private Builder() {

    }

    /**
     * @param name a parameter name.
     * @param value a parameter value.
     * @return this instance.
     */
    public Builder param(String name, String value) {
      getOrCreateParameterValues(name).add(value);
      return this;
    }

    /**
     * @return a new {@link QueryString}, containing the configured parameters.
     */
    public QueryString build() {
      return new QueryString(parameters);
    }

    private List<String> getOrCreateParameterValues(String name) {
      List<String> values = parameters.get(name);
      if (values == null) {
        values = new ArrayList<>();
        parameters.put(name, values);
      }
      return values;
    }

  }
}
