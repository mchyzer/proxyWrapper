/*
 * @author mchyzer
 * $Id: ProxyServletRequestWrapper.java,v 1.7 2014/04/23 06:37:18 mchyzer Exp $
 */
package edu.upenn.isc.proxyWrapper;

import java.net.InetAddress;
import java.sql.Timestamp;
import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import edu.upenn.isc.proxyWrapper.util.ExpirableCache;
import edu.upenn.isc.proxyWrapper.util.ProxyUtils;


/**
 * wrap a request to pretend like a proxy is not there...
 */
public class ProxyServletRequestWrapper extends HttpServletRequestWrapper {

  /**
   * if the SSL is offloaded
   */
  private boolean sslOffloaded = false;
  
  
  /**
   * if the SSL is offloaded
   * @return the sslOffloaded
   */
  public boolean isSslOffloaded() {
    return this.sslOffloaded;
  }

  /**
   * 
   */
  private static final String X_FORWARDED_PROTO = "X-Forwarded-Proto";

  /**
   * 
   */
  private static final String X_FORWARDED_FOR = "X-Forwarded-For";


  /**
   * reverse lookup a dns address
   * @param ipAddress
   * @return the dns address
   */
  public static String reverseDnsLookup(String ipAddress) {
    //cache this so we dont lookup every request
    String theHost = remoteHosts.get(ipAddress);
    if (ProxyUtils.isBlank(theHost)) {
      //default to ip address
      theHost = ipAddress;
      try {
        InetAddress addr = InetAddress.getByName(ipAddress);          
        // Get the host name
        theHost = addr.getCanonicalHostName();
      } catch (Exception e) {
        //only logging
        if (ProxyWrapperFilter.isDebug()) {
          String timestamp = new Timestamp(System.currentTimeMillis()).toString();
          System.out.println(timestamp + ": Problem with address: " + ipAddress);
          e.printStackTrace();
        }
      }
      remoteHosts.put(ipAddress, theHost);
    }        
    return theHost;
  }
  
  /** cache ip address to hosts.  note, the dns resolution isnt that important, not used for anything exact if
   * anything at all, so 60 minute cache */
  private static ExpirableCache<String, String> remoteHosts = new ExpirableCache<String, String>(60);
  
  /** http servlet request */
  private HttpServletRequest httpServletRequest = null;
  
  /**
   * <pre>
   * problem with tomcat and request wrappers, so stash this and
   * get back later:
   * http://brian.pontarelli.com/2008/01/16/httpservletrequestwrapper-tomcat-and-forwards/
   * </pre>
   */
  private StringBuffer requestURL = null;
  
  /** scheme, https ot http */
  private String scheme;
  
  /** if ssl */
  private boolean secure;
  
  
  /**
   * @return the secure
   */
  @Override
  public boolean isSecure() {
    return this.secure;
  }

  /**
   * scheme, https ot http
   * @return the scheme
   */
  @Override
  public String getScheme() {
    return this.scheme;
  }

  /** remote address */
  private String remoteAddr;
  
  /**
   * @return the remoteHost
   */
  @Override
  public String getRemoteHost() {
    
    //see if we are passing in through filter
    String xForwardedForOrig = this.httpServletRequest.getHeader(X_FORWARDED_FOR);
    if (!ProxyUtils.isBlank(xForwardedForOrig)) {
      
      return reverseDnsLookup(this.remoteAddr);
    }
    
    //pass through to other
    return this.httpServletRequest.getRemoteHost();
  }

  /**
   * remote address
   * @return the remoteAddr
   */
  @Override
  public String getRemoteAddr() {
    return this.remoteAddr;
  }

  /**
   * @param theRequest
   */
  public ProxyServletRequestWrapper(HttpServletRequest theRequest) {
    super(theRequest);
    
    this.httpServletRequest = theRequest;
    
    String timestamp = new Timestamp(System.currentTimeMillis()).toString();
    //String requestURI = request.getRequestURI();
    //System.out.println(requestURI);
    this.requestURL = theRequest.getRequestURL();
    
    String xForwardedProtoHeaderName = null;
    String xForwardedForHeaderName = null;
    
    {
      //see what the real header names are (case)
      @SuppressWarnings("unchecked")
      Enumeration<String> headerNames = theRequest.getHeaderNames();
      
      while (headerNames != null && headerNames.hasMoreElements()) {
        String headerName = headerNames.nextElement();
        if (ProxyUtils.equalsIgnoreCase(headerName, X_FORWARDED_FOR)) {
          xForwardedForHeaderName = headerName;
        }
        if (ProxyUtils.equalsIgnoreCase(headerName, X_FORWARDED_PROTO)) {
          xForwardedProtoHeaderName = headerName;
        }
      }
    }    
    //either https or not.  Get the headers passed from the F5
    String xForwardedProtoOrig = theRequest.getHeader(X_FORWARDED_PROTO);
    String xForwardedForOrig = theRequest.getHeader(X_FORWARDED_FOR);
    
    String xForwardedProtoFirst = null;
    
    String xForwardedForFirst = null;
    boolean foundException = false;
    try {
      
      //if the browser or upstream server sends headers, they will end up comma separated... if thats the case, get the first one
      xForwardedProtoFirst = ProxyUtils.isBlank(xForwardedProtoOrig) ? null : ProxyUtils.splitTrim(xForwardedProtoOrig, ",").get(0);
      xForwardedForFirst = ProxyUtils.isBlank(xForwardedForOrig) ? null : ProxyUtils.splitTrim(xForwardedForOrig, ",").get(0);

      //secure means if we have SSL
      this.secure = theRequest.isSecure();
      
      //if we arent sending in a header for protocol, use the underlying request one
      if (ProxyUtils.isBlank(xForwardedProtoFirst)) {
        this.scheme = theRequest.getScheme();
      } else {
        //if we are sending the HTTPS in a header, then we have to adjust the scheme, request URL, and secure flag

        this.scheme = xForwardedProtoFirst.toLowerCase();

        String requestUrlString = this.requestURL.toString();
        if ("https".equals(this.scheme) && requestUrlString.toLowerCase().startsWith("http:")) {
          this.requestURL = new StringBuffer("https:" + requestUrlString.substring(5));
          this.secure = true;
          this.sslOffloaded = true;
        }
        
      }
      
      //if didnt send in remote address in header, use the underlying
      if (ProxyUtils.isBlank(xForwardedForFirst)) {
        this.remoteAddr = theRequest.getRemoteAddr();
      } else {
        this.remoteAddr = xForwardedForFirst;
      }
      
    } catch (RuntimeException re) {
      foundException = true;
      re.printStackTrace();
      throw re;
    } finally {
      
      //logging is if set in web.xml, see the docs in sourcecontrol/docs or confluence
      //we use system.out since this jar doesnt depend on anything (fast)
      if (foundException || ProxyWrapperFilter.isDebug()) {
        System.out.println(timestamp + ": Proxy log: xForwardedProtoOrig: " + xForwardedProtoOrig 
            + ", xForwardedForOrig: " + xForwardedForOrig 
            + ", xForwardedProtoFirst: " + xForwardedProtoFirst + ", xForwardedForFirst: " + xForwardedForFirst
            + ", requestURL: " + this.requestURL + ", scheme: " + this.scheme + ", remoteAddr: " + this.remoteAddr
            + ", secure: " + this.secure + ", xForwardedForHeader: " + xForwardedForHeaderName 
            + ", xForwardedProtoHeader: " + xForwardedProtoHeaderName);
      }
    }
  }

  /**
   * <pre>
   * problem with tomcat and request wrappers, so stash this and
   * get back later:
   * http://brian.pontarelli.com/2008/01/16/httpservletrequestwrapper-tomcat-and-forwards/
   * </pre>
   * @see javax.servlet.http.HttpServletRequestWrapper#getRequestURL()
   */
  @Override
  public StringBuffer getRequestURL() {
    //StringBuffer requestURL = super.getRequestURL();
    //System.out.println("URL: " + requestURL.toString());
    return this.requestURL;
  }

  
  
  
}
