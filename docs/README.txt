- to make the jar, just export from eclipse into the dist dir.  Include the source too.

- here is the doc:

https://flash.isc-seo.upenn.edu/confluence/display/FAST/Proxy+filter


  <context-param>
    <param-name>proxyDebug</param-name>
    <param-value>true</param-value>
  </context-param>


  <!-- The patterns here should match the servlet mappings below -->
  <filter>
    <filter-name>proxyWrapper</filter-name>
    <filter-class>edu.upenn.isc.proxyWrapper.ProxyWrapperFilter</filter-class>
  </filter>
  <filter-mapping>
    <filter-name>proxyWrapper</filter-name>
    <url-pattern>/jsp/fast.do</url-pattern>
  </filter-mapping>
  <filter-mapping>
    <filter-name>proxyWrapper</filter-name>
    <url-pattern>/jsp/fast2.do</url-pattern>
  </filter-mapping>
  <filter-mapping>
    <filter-name>proxyWrapper</filter-name>
    <url-pattern>/jsp/fast3.do</url-pattern>
  </filter-mapping>
  <filter-mapping>
    <filter-name>proxyWrapper</filter-name>
    <url-pattern>/jsp/fast4.do</url-pattern>
  </filter-mapping>
  <filter-mapping>
    <filter-name>proxyWrapper</filter-name>
    <url-pattern>/jsp/fast.png</url-pattern>
  </filter-mapping>
  <filter-mapping>
    <filter-name>proxyWrapper</filter-name>
    <url-pattern>/jsp/fast.jpeg</url-pattern>
  </filter-mapping>
  <filter-mapping>
    <filter-name>proxyWrapper</filter-name>
    <url-pattern>/jsp/fast.jpg</url-pattern>
  </filter-mapping>
  <filter-mapping>
    <filter-name>proxyWrapper</filter-name>
    <url-pattern>/jsp/fast.bmp</url-pattern>
  </filter-mapping>
  <filter-mapping>
    <filter-name>proxyWrapper</filter-name>
    <url-pattern>/jsp/fast.gif</url-pattern>
  </filter-mapping>

  
  
##################
## Tamper data for X-Forwarded-Proto (https), X-Forwarded-For (1.2.3.4, or your own ip address)


