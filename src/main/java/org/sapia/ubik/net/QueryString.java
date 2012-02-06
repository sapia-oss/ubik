package org.sapia.ubik.net;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


/**
 * This class models a query string. A query string has a format similar
 * to the following:
 *
 * <pre>
 *   /some/path?name1=value1&name2=value2
 * </pre>
 *
 * @author Yanick Duchesne
 */
public class QueryString {
  private String              path = "/";
  private Map<String, String> properties = new HashMap<String, String>();

  /**
   * Constructor for QueryString.
   */
  QueryString() {
  }

  /**
   * Constructor for QueryString.
   *
   * This constructor takes the path of the query string.
   *
   * @param path a path
   */
  public QueryString(String path) {
    this.path = path;
  }

  /**
   * Returns this instance's path.
   *
   * @return a path.
   */
  public String getPath() {
    return this.path;
  }

  /**
   * Returns this instance's parameters.
   *
   * @return a {@link Map} containing name/value pairs.
   */
  public Map<String,String> getParameters() {
    return properties;
  }

  /**
   * Adds the passed in name/value pair as parameter.
   *
   * @param name an object attribute name
   * @param value an object attribute value.
   */
  public void addParameter(String name, String value) {
    properties.put(name, value);
  }

  /**
   * Returns the value for the parameter with the passed
   * in name.
   *
   * @param name the name of the parameter whose value should be returned.
   * @return the value of the given parameter, or <code>null</code>
   * if no such value exists.
   */
  public String getParameter(String name) {
    return (String) properties.get(name);
  }

  /**
   * Sets this instance's path.
   *
   * @param path a path.
   */
  void setPath(String path) {
    this.path = path;
  }

  /**
   * Parses a query string and returns its object representation.
   *
   * @return a <code>QueryString</code>
   */
  public static QueryString parse(String queryStr) {
    QueryStringParser p = new QueryStringParser();

    return p.parseQueryString(queryStr);
  }

  public String toString() {
    StringBuffer buf = new StringBuffer(path);

    if (properties.size() > 0) {
      Map.Entry<String, String> entry;
      buf.append(QueryStringParser.QMARK);

      Iterator<Map.Entry<String, String>> itr  = properties.entrySet().iterator();
      int      count = 0;

      while (itr.hasNext()) {
        entry = (Map.Entry<String, String>) itr.next();

        if (count > 0) {
          buf.append(QueryStringParser.AMP);
        }

        buf.append(entry.getKey().toString()).append(QueryStringParser.EQ)
           .append(entry.getValue());
        count++;
      }
    }

    return buf.toString();
  }
}
