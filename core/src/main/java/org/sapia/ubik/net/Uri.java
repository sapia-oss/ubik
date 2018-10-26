package org.sapia.ubik.net;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Objects;
import java.util.Optional;

import org.sapia.ubik.util.Equals;
import org.sapia.ubik.util.Strings;
import org.sapia.ubik.util.tokens.Delimiter;
import org.sapia.ubik.util.tokens.DelimiterSequence;
import org.sapia.ubik.util.tokens.Token;
import org.sapia.ubik.util.tokens.Tokenizer;

/**
 * This class models a URI.
 * <p>
 * Usage:
 * 
 * <pre>
 * Uri httpURI = Uri.parse(&quot;http://www.sapia-oss.org:80/index.html&quot;);
 * 
 * // will print: http
 * System.out.println(httpURI.getScheme());
 * 
 * // will print: www.sapia-oss.org
 * System.out.println(httpURI.getHost());
 * 
 * // will print: 80
 * System.out.println(httpURI.getPort());
 * 
 * // will print: /index.html
 * System.out.println(httpURI.getPath());
 * 
 * Uri fileURI = Uri.parse(&quot;file:/some/directory/foo.txt&quot;);
 * 
 * // will print: file
 * System.out.println(fileURI.getScheme());
 * 
 * // these calls don't make sense:
 * System.out.println(fileURI.getHost());
 * System.out.println(fileURI.getHost());
 * 
 * // will print: /some/directory/foo.txt
 * System.out.println(fileURI.getPath());
 * 
 * </pre>
 * 
 * 
 * 
 * @author yduchesne
 */
public class Uri implements Externalizable {
  public static final int    UNDEFINED_PORT = -1;
  public static final String UNDEFINED_HOST = "";
  public static final String EMPTY_PATH     = "";
  private static final String DEFAULT_SCHEME_DELIMS = "";

  //private static final QueryStringParser PARSER = new QueryStringParser();

  private UriStructure structure;

  /**
   * DO NOT CALL: meant for externalization only.
   */
  public Uri() {

  }

  private Uri(UriStructure structure) {
    this.structure = structure;
  }

  /**
   * Returns the path of this URI.
   * 
   * @return a path.
   */
  public String getPath() {
    return structure.path;
  }

  /**
   * @param path a path.
   * @return a copy of this instance, but with the given path.
   */
  public Uri withPath(String path) {
    UriStructure copy = structure.getCopy();
    copy.path = path;
    return new Uri(copy);
  }

  /**
   * Returns the scheme of this URI.
   * 
   * @return a scheme.
   */
  public String getScheme() {
    return structure.scheme;
  }

  /**
   * @param scheme a URI scheme.
   * @return a copy of this instance, but with the given scheme.
   */
  public Uri withScheme(String scheme) {
    UriStructure copy = structure.getCopy();
    copy.scheme = scheme;
    return new Uri(copy);
  }

  /**
   * Returns the host of this URI.
   * 
   * @return a host - if no host was specified, the returned value corresponds
   *         to the UNDEFINED_HOST constant of this class.
   */
  public String getHost() {
    return structure.host;
  }

  /**
   * @param host a host.
   * @return a copy of this instance, but with the given path.
   */
  public Uri withHost(String host) {
    UriStructure copy = structure.getCopy();
    copy.host = host;
    return new Uri(copy);
  }

  /**
   * Returns the port of this URI.
   * 
   * @return a port - if no port was specified, the returned value corresponds
   *         to the UNDEFINED_PORT constant of this class.
   */
  public int getPort() {
    return structure.port;
  }

  /**
   * @param port a port.
   * @return a copy of this instance, but with the given path.
   */
  public Uri withPort(int port) {
    UriStructure copy = structure.getCopy();
    copy.port = port;
    return new Uri(copy);
  }

  /**
   * @return this instance's {@link Optional} user.
   */
  public Optional<String> getUser() {
    return Optional.ofNullable(structure.user);
  }

  /**
   * @param user a user.
   * @return a copy of this instance, but with the given user.
   */
  public Uri withUser(String user) {
    UriStructure copy = structure.getCopy();
    copy.user = user;
    return new Uri(copy);
  }

  /**
   * @return this instance's {@link Optional} password.
   */
  public Optional<String> getPassword() {
    return Optional.ofNullable(structure.password);
  }

  /**
   * @param password a password.
   * @return a copy of this instance, but with the given password.
   */
  public Uri withPassword(String password) {
    UriStructure copy = structure.getCopy();
    copy.password = password;
    return new Uri(copy);
  }

  /**
   * @return this instance's {@link Optional} fragment.
   */
  public Optional<String> getFragment() {
    return Optional.ofNullable(structure.fragment);
  }

  public Uri withFragment(String fragment) {
    UriStructure copy = structure.getCopy();
    copy.fragment = fragment;
    return new Uri(copy);
  }

  /**
   * Returns this instance's query string.
   * 
   * @return a <code>QueryString</code>, or <code>null</code> if this instance
   *         has no query string.
   */
  public QueryString getQueryString() {
    return structure.queryString;
  }

  /**
   * @param qs a {@link QueryString}.
   * @return copy of this instance, but with the given {@link QueryString}.
   */
  public Uri withQueryString(QueryString qs) {
    UriStructure copy = structure.getCopy();
    copy.queryString = qs;
    return new Uri(copy);
  }

  /**
   * Creates a new instance of this class, which is a copy of this instance. Appends to the new instance the
   * {@link QueryString} passed in.
   *
   * @param qs a {@link QueryString} to append.
   * @return a copy of this instance, with the given {@link QueryString} appended to it.
   *
   * @see QueryString#append(QueryString)
   */
  public Uri append(QueryString qs) {
    UriStructure newStructure     = structure.getCopy();
    newStructure.queryString      = structure.queryString.append(qs);
    return new Uri(newStructure);
  }

  /**
   * Returns this instance's string format.
   * 
   * @return a <code>String</code>.
   */
  @Override
  public String toString() {

    StringBuffer buf = new StringBuffer();
    if (structure.scheme != null) {
    	buf.append(structure.scheme).append(":");
    }
    
    // condition accounts for windows drive letters
    if (structure.schemeDelimiters.equals("//") || (structure.path.length() > 0 && Character.isAlphabetic(structure.path.charAt(0)))) {
      buf.append(structure.schemeDelimiters);
    }

    if (structure.user != null) {
      buf.append(structure.user);
      if (structure.password != null) {
        buf.append(':').append(structure.password);
      }
      buf.append('@');
    }

    if (structure.host != null) {
      buf.append(structure.host);
    }
    if (structure.port != UNDEFINED_PORT) {
      buf.append(':').append(structure.port);
    }
    if (structure.path != null) {
      buf.append(structure.path);
    }
    if (!structure.queryString.isEmpty()) {
      buf.append('?').append(structure.queryString.toString());
    }
    if (structure.fragment != null) {
      buf.append('#').append(structure.fragment);
    }
    return buf.toString();
  }

  /**
   * Parses the given URI string and returns its object representation. The provided input must match the following format:
   * <pre>
   *  scheme:[//[user[:password]@]host[:port]][/path][?query][#fragment]
   * </pre>
   * 
   * @return a <code>Uri</code>.
   *
   * @throws UriSyntaxException if the provided input does not correspond to a valid URI.
   */
  public static Uri parse(String uriStr) throws UriSyntaxException {
    UriParser parser = new UriParser(uriStr);
    return parser.parse();
  }
  
  @Override
  public boolean equals(Object obj) {
    if (obj instanceof Uri) {
      return structure.doEquals( ((Uri) obj).structure );
    }
    return false;
  }
  
  @Override
  public int hashCode() {
    return structure.doHashCode();
  }

  // --------------------------------------------------------------------------
  // Externalizable interface

  @Override
  public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
    structure = (UriStructure) in.readObject();
  }

  @Override
  public void writeExternal(ObjectOutput out) throws IOException {
    out.writeObject(structure);
  }

  // ==========================================================================
  // Inner classes

  public static class UriStructure implements Externalizable {
	  
	static long serialVersionUID = 1L;

    private String      scheme;
    private String      schemeDelimiters  = "//";
    private String      user;
    private String      password;
    private String      host              = UNDEFINED_HOST;
    private int         port              = UNDEFINED_PORT;
    private String      path              = EMPTY_PATH;
    private QueryString queryString       = new QueryString();
    private String      fragment;
    
    // Do not call: meant for serialization only
    public UriStructure() {
    }
    
    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
    	out.writeObject(scheme);
    	out.writeObject(schemeDelimiters);
    	out.writeObject(user);
    	out.writeObject(password);
    	out.writeObject(host);
    	out.writeInt(port);
    	out.writeObject(path);
    	out.writeObject(queryString);
    	out.writeObject(fragment);
    }
    
    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
    	scheme = (String) in.readObject();
    	schemeDelimiters = (String) in.readObject();
    	user = (String) in.readObject();
    	password = (String) in.readObject();
    	host = (String) in.readObject();
    	port = in.readInt();
    	path = (String) in.readObject();
    	queryString = (QueryString) in.readObject();
    	fragment = (String) in.readObject();
    }

    private UriStructure getCopy() {
      UriStructure copy     = new UriStructure();
      copy.scheme           = scheme;
      copy.schemeDelimiters = schemeDelimiters;
      copy.user             = user;
      copy.password         = password;
      copy.host             = host;
      copy.port             = port;
      copy.path             = path;
      copy.queryString      = queryString;
      copy.fragment         = fragment;
      copy.validate();
      return copy;
    }

    private UriStructure validate() {
      doAssert(Strings.isNotBlank(scheme), "URI scheme cannot be null or empty");
      doAssert(Strings.isNotBlank(schemeDelimiters), "URI scheme delimiters cannot be null or empty");
      return this;
    }

    private void doAssert(boolean condition, String msg, Object...args) throws UriSyntaxException {
      if (!condition) {
        throw new UriSyntaxException(String.format(msg, args));
      }
    }
    
    private boolean doEquals(UriStructure other) {
      return Equals.isTrue(scheme, other.scheme)
      && Equals.isTrue(schemeDelimiters, other.schemeDelimiters)
      && Equals.isTrue(user, other.user)
      && Equals.isTrue(password, other.password)
      && Equals.isTrue(host, other.host)
      && port == other.port
      && Equals.isTrue(path, other.path)
      && Equals.isTrue(queryString, other.queryString)
      && Equals.isTrue(fragment, other.fragment);
    }
    
    private int doHashCode() {
    	return Objects.hash(scheme, schemeDelimiters, user, password, host, port, path, queryString, fragment);
    }
  }

  // --------------------------------------------------------------------------

  static class UriParser {

    private enum ParsingState {
      SCHEME,
      SCHEME_DELIMITERS,
      USER,
      PASSWORD,
      HOST,
      PORT,
      PATH,
      QUERY,
      FRAGMENT,
      END
    }

    private UriStructure struc      = new UriStructure();
    private Tokenizer    tokenizer;
    private ParsingState state      = ParsingState.SCHEME;

    UriParser(String input) {
      tokenizer = new Tokenizer(input, ":", "///", "//", "@", "/", "?", "&", "=", "#");
    }

    Uri buildUri() {
      return new Uri(struc);
    }

    UriParser advanceTo(int index) {
      tokenizer = tokenizer.withIndex(index);
      return this;
    }

    void parseScheme() {
      Token scheme = tokenizer.tokenizeNext();
      if (tokenizer.isEos()) {
        throw new UriSyntaxException(String.format("Invalid URI %s. Premature end of string reached when trying to extract scheme",
            tokenizer.getInput()));
      }
      if (scheme.isNull()) {
        throw new UriSyntaxException(String.format("Invalid URI %s. Scheme not specified", tokenizer.getInput()));
      }
      struc.scheme = scheme.getValue();
      state = ParsingState.SCHEME_DELIMITERS;
    }

    void parseSchemeDelims() {
      Token delims = tokenizer.tokenizeNext();
      if (tokenizer.isEos()) {
        throw new UriSyntaxException(String.format("Invalid URI %s. Premature end of string reached when trying to extract scheme or user info/host",
            tokenizer.getInput()));
      }
      if (delims.isNull()) {
        throw new UriSyntaxException(String.format("Invalid URI %s. Expected to find / or // after %s:", tokenizer.getInput(), struc.scheme));
      }
      Delimiter matchedSchemeDelim = delims.getMatchedDelimOrThrow(() -> {
        throw new UriSyntaxException(String.format("Invalid URI %s. Expected to find / or // after %s:", tokenizer.getInput(), struc.scheme));
      });
      if (matchedSchemeDelim.isEqualTo("//")) {
        state = ParsingState.USER;
      } else if (matchedSchemeDelim.isEqualTo("/")) {
        state = ParsingState.PATH;
      } else if (matchedSchemeDelim.isEqualTo("///")) {
        state = ParsingState.PATH;
      } else {
        throw new UriSyntaxException(String.format("Invalid URI %s. Expected to find / or // after %s:", tokenizer.getInput(), struc.scheme));
      }
      struc.schemeDelimiters = matchedSchemeDelim.getValue();
    }

    void parseUser() {
      DelimiterSequence delimiterSequence = tokenizer.peekNextDelims();
      if (delimiterSequence.contains(":", "@") || delimiterSequence.contains("@")) {
        // found user
        Token user = tokenizer.tokenizeNext();
        if (user.isNull()) {
          throw new UriSyntaxException(
              String.format("Invalid URI %s. Premature end of string reached when trying to extract user info/host/path",
                  tokenizer.getInput())
          );
        }
        Delimiter matchedUserDelim = user.getMatchedDelimOrThrow(() -> {
          throw new UriSyntaxException(String.format("Invalid URI %s. Expected to find either user info, host or path", tokenizer.getInput()));
        });

        if (matchedUserDelim.isEqualTo(":")) {
          struc.user = user.getValue();
          state = ParsingState.PASSWORD;
        } else if (matchedUserDelim.isEqualTo("@")) {
          struc.user = user.getValue();
          state = ParsingState.HOST;
        } else {
          // This should not happen since we have checked that we have a sequence of delimiters
          // corresponding to user:password@host or user@host
          throw new UriSyntaxException(String.format("Invalid URI %s. Expected to find either user info, host or path", tokenizer.getInput()));
        }

      } else {
        // if we fall here, it means there's no user info in the URI, so we're going to skip that part.
        state = ParsingState.HOST;
      }
    }

    void parsePassword() {
      Token pwd = tokenizer.tokenizeNext();
      if (tokenizer.isEos() || pwd.isNull()) {
        throw new UriSyntaxException(
            String.format("Invalid URI %s. Premature end of string reached when trying to extract user info/host/path",
                tokenizer.getInput())
        );
      }
      struc.password = pwd.getValue();
      state = ParsingState.HOST;
    }

    void parseHost() {
      Token host = tokenizer.tokenizeNext();
      if (host.isNull()) {
        throw new UriSyntaxException(
            String.format("Invalid URI %s. Premature end of string reached when trying to extract host",
                tokenizer.getInput())
        );
      }
      struc.host = host.getValue();
      if (tokenizer.isEos()) {
        state = ParsingState.END;
      } else {
        Delimiter matchedHostDelim = host.getMatchedDelimOrThrow(() -> {
          throw new UriSyntaxException(String.format("Invalid URI %s. Expected port or path", tokenizer.getInput()));
        });
        if (matchedHostDelim.isEqualTo(":")) {
          state = ParsingState.PORT;
        } else if (matchedHostDelim.isEqualTo("/")) {
          state = ParsingState.PATH;
        } else if (matchedHostDelim.isEqualTo("?")) {
          state = ParsingState.QUERY;
        } else if (matchedHostDelim.isEqualTo("#")) {
          state = ParsingState.FRAGMENT;
        } else {
          throw new UriSyntaxException(String.format("Invalid URI %s. Unexpected delimiter at index %s: %s",
              tokenizer.getInput(), matchedHostDelim.getIndex(), matchedHostDelim.getValue()));
        }
      }

    }

    void parsePort() {
      Token port = tokenizer.tokenizeNext();
      if (port.isNull() || Strings.isBlank(port.getValue())) {
        throw new UriSyntaxException(
            String.format("Invalid URI %s. Premature end of string reached when trying to extract port",
                tokenizer.getInput())
        );
      }
      try {
        struc.port = Integer.parseInt(port.getValue());
      } catch (NumberFormatException e) {
        throw new UriSyntaxException(String.format("Invalid port in URI %s. Got %s, expected integer value", tokenizer.getInput(), port.getValue()));
      }
      if (tokenizer.isEos()) {
        state = ParsingState.END;
      } else {
        Delimiter matchedPortDelim = port.getMatchedDelimOrThrow(() -> {
          throw new UriSyntaxException(String.format("Invalid URI %s. Expected path, query string, or fragment after index %s",
              tokenizer.getInput(), port.getIndex() + port.getValue().length()));
        });
        if (matchedPortDelim.isEqualTo("/")) {
          state = ParsingState.PATH;
        } else if (matchedPortDelim.isEqualTo("?")) {
          state = ParsingState.QUERY;
        } else if (matchedPortDelim.isEqualTo("#")) {
          state = ParsingState.FRAGMENT;
        } else {
          throw new UriSyntaxException(String.format("Invalid URI %s. Expected path, query string, or fragment at index %s",
              tokenizer.getInput(), matchedPortDelim.getIndex()));
        }
      }
    }

    void parsePath() {
      Token path = tokenizer.tokenizeNext();
      if (tokenizer.isEos()) {
        if (path.isSet()) {
          struc.path = struc.path + (struc.path.endsWith("/") ? path.getValue() : "/" + path.getValue());
        }
        state = ParsingState.END;
      } else {
        Delimiter matchedPathDelim = path.getMatchedDelimOrThrow(() -> {
          throw new UriSyntaxException(String.format("Invalid URI %s. Expected query string or fragment after index %s",
              tokenizer.getInput(), path.getIndex()));
        });

        if (matchedPathDelim.isEqualTo("?")) {
          struc.path = struc.path + (struc.path.endsWith("/") ? path.getValue() : "/" + path.getValue());
          state = ParsingState.QUERY;
        } else if (matchedPathDelim.isEqualTo("#")) {
          struc.path = struc.path + (struc.path.endsWith("/") ? path.getValue() : "/" + path.getValue());
          state = ParsingState.FRAGMENT;
        } else if (matchedPathDelim.isEqualTo("/") || matchedPathDelim.isEqualTo("\\")) {
          struc.path = struc.path + (struc.path.endsWith("/") ? path.getValue() : "/" + path.getValue());
          state = ParsingState.PATH;

        // accounting for windows drive letter path
        } else if (matchedPathDelim.isEqualTo(":")) {
          struc.path = struc.path + path.getValue() + ":";
          state = ParsingState.PATH;
        } else {
          throw new UriSyntaxException(
            String.format("Invalid URI %s. Expected path, query string, or fragment at index %s (%s)",
              tokenizer.getInput(),
              matchedPathDelim.getIndex(),
              tokenizer.getInput().substring(0, matchedPathDelim.getIndex() + matchedPathDelim.getValue().length())
            )
          );
        }
      }
    }

    void parseQuery() {
      Token     paramName       = tokenizer.tokenizeNext();
      Delimiter matchParamDelim = paramName.getMatchedDelimOrThrow(() -> {
        throw new UriSyntaxException(String.format("Invalid URI %s. Expected '=' at index %s",
            tokenizer.getInput(), paramName.getIndex()));
      });
      if (!matchParamDelim.isEqualTo("=")) {
        throw new UriSyntaxException(
          String.format(
            "Invalid URI %s. Expected '=' at index %s (%s). Got '%s'",
            tokenizer.getInput(),
            matchParamDelim.getIndex() + matchParamDelim.getValue().length(),
            tokenizer.getInput().substring(0, matchParamDelim.getIndex() + matchParamDelim.getValue().length()),
            matchParamDelim.getValue()
          )
        );
      }
      Token paramValue = tokenizer.tokenizeNext();
      if (paramValue.isSet()) {
        struc.queryString.getOrCreateParameterValues(paramName.getValue()).add(paramValue.getValue());
      } else {
        struc.queryString.getOrCreateParameterValues(paramName.getValue()).add("");
      }

      if (tokenizer.isEos()) {
        state = ParsingState.END;
      } else {
        Delimiter nextParamDelim = paramValue.getMatchedDelimOrThrow(() -> {
          throw new UriSyntaxException(
            String.format(
              "Invalid URI %s. Expected fragment or other parameter after index %s (%s)",
              tokenizer.getInput(),
              paramName.getIndex(),
              tokenizer.getInput().substring(0, paramValue.getIndex() + paramValue.getValue().length())
            )
          );
        });
        if (nextParamDelim.isEqualTo("#")) {
          state = ParsingState.FRAGMENT;
        } else if (!nextParamDelim.isEqualTo("&")) {
          throw new UriSyntaxException(
              String.format(
                "Invalid URI %s. Expected another parameter at index %s (%s). Got '%s', when '&' was expected",
                tokenizer.getInput(),
                nextParamDelim.getIndex(),
                tokenizer.getInput().substring(0, nextParamDelim.getIndex()) + nextParamDelim.getValue(),
                nextParamDelim.getValue()
              )
          );
        } else {
          state = ParsingState.QUERY;
        }
      }
    }

    void parseFragment() {
      Token fragment = tokenizer.tokenizeNext();
      if (fragment.isSet()) {
        struc.fragment = fragment.getValue();
      } else if (!tokenizer.isEos()) {
        throw new UriSyntaxException(String.format("Not content expected after fragment. Got %s", tokenizer.remaining()));
      }
      state = ParsingState.END;
    }

    Uri parse() {

      while (!tokenizer.isEos() && state != ParsingState.END) {

        switch (state) {
          case SCHEME:
            parseScheme();
            break;
          case SCHEME_DELIMITERS:
            parseSchemeDelims();
            break;
          case USER:
            parseUser();
            break;
          case PASSWORD:
            parsePassword();
            break;
          case HOST:
            parseHost();
            break;
          case PORT:
            parsePort();
            break;
          case PATH:
            parsePath();
            break;
          case FRAGMENT:
            parseFragment();
            break;
          case QUERY:
            parseQuery();
            break;
          case END:
            break;
          default:
            throw new IllegalStateException("State not handled: " + state);
        }
      }

      Uri uri = buildUri();
      uri.structure.validate();
      return uri;
    }
  }

}
