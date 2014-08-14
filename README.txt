 j2ee filter to set a backdoor username with a password.  Setup in web.xml
 
 <filter>
   <filter-name>usernameFilterForTesting</filter-name>
   <filter-class>org.hyzer.usernameFilterForTesting.UsernameFilterForTesting</filter-class>
 </filter>
 <filter-mapping>
   <filter-name>usernameFilterForTesting</filter-name>
   <url-pattern>/*</url-pattern>
 </filter-mapping>
 
 <context-param>
  <param-name>usernameFilterForTestingEnabled</param-name>
  <param-value>true</param-value>
 </context-param>
 <context-param>
  <param-name>usernameFilterForTestingPassword</param-name>
  <param-value>abc123XYZ789</param-value>
 </context-param>

Go to the app:

https://app.whatever/path?usernameFilterForTestingUsername=something&usernameFilterForTestingPassword=******* 
 