package net.respectnetwork.csp.application.session;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;




public final class CustomHeaderHttpServletRequestWrapper extends
      HttpServletRequestWrapper
{
   
  

   public CustomHeaderHttpServletRequestWrapper(HttpServletRequest request)
   {
      super(request);
      
   }
   
   

   

}
