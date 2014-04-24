/**
 * @author mchyzer
 * $Id: ProxyServletResponseWrapper.java,v 1.2 2014/04/23 07:00:55 mchyzer Exp $
 */
package edu.upenn.isc.proxyWrapper;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;


/**
 *
 */
public class ProxyServletResponseWrapper extends HttpServletResponseWrapper {

  /**
   * @param response
   */
  public ProxyServletResponseWrapper(HttpServletResponse response) {
    super(response);
    
  }

  /**
   * for a url, get the protocol and domain, e.g. for url https://a.b/path, will return https://a.b
   * @param url
   * @return the protocol and path
   */
  public static String httpProtocolAndDomain(String url) {
    int firstSlashAfterProtocol = url.indexOf('/', 8);
    if (firstSlashAfterProtocol < 0) {
      //must not have a path
      return url;
    }

    return url.substring(0, firstSlashAfterProtocol);
  }

  /**
   * @see javax.servlet.http.HttpServletResponseWrapper#sendRedirect(java.lang.String)
   */
  @Override
  public void sendRedirect(String location) throws IOException {
    
    String originalLocation = location;
    
    if (location != null) {
      //this can be a /something url and it does not take into account if we are proxied
      //note, this didnt work with proxies in front of the j2ee container (e.g. offloading SSL)
      ProxyServletRequestWrapper proxyServletRequestWrapper = ProxyWrapperFilter.retrieveProxyServletRequestWrapper();
      if (proxyServletRequestWrapper.isSslOffloaded()) {
        String requestUrl = proxyServletRequestWrapper.getRequestURL().toString();
        boolean changedUrl = false;
        String protocolAndDomain = httpProtocolAndDomain(requestUrl);
        if (location.startsWith("/")) {
            location = protocolAndDomain + location;
            changedUrl = true;
        } else if (!location.toLowerCase().startsWith("http:") && !location.toLowerCase().startsWith("https:")) {
          //this is a relative URL
          int questionIndex = requestUrl.indexOf('?');
          if (questionIndex >= 0) {
            requestUrl = requestUrl.substring(0, questionIndex);
          }
          
          //cases:
          // https://some.thing
          // https://some.thing/
          // https://some.thing/abc
          // https://some.thing/abc/def/
          // https://some.thing/abc/def/efg

          int slashIndex = requestUrl.lastIndexOf('/');
          //if it matches https://
          if (slashIndex <= 8) {
            slashIndex = -1;
          }
          if (slashIndex == -1) {
            // https://some.thing
            location = requestUrl + "/" + location;
          } else if (slashIndex == requestUrl.length()-1) {
            // https://some.thing/
            // https://some.thing/abc/def/
            location = requestUrl + location;
          } else {
            // https://some.thing/abc
            // https://some.thing/abc/def/efg
            location = requestUrl.substring(0, slashIndex+1) + location;
          }
          
          changedUrl = true;
        }
        
        if (changedUrl && ProxyWrapperFilter.isDebug()) {
          System.out.println("Changed redirect for request: " + proxyServletRequestWrapper.getRequestURL() 
              + ", from: " + originalLocation + ", to: " + location);
        }
      }
    }

    
    super.sendRedirect(location);
  }

}
