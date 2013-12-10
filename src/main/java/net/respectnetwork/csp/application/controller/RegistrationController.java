package net.respectnetwork.csp.application.controller;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import net.respectnetwork.csp.application.form.CloudForm;
import net.respectnetwork.csp.application.form.CodesForm;
import net.respectnetwork.csp.application.form.UserForm;
import net.respectnetwork.csp.application.manager.RegistrationManager;
import net.respectnetwork.csp.application.model.CSPToken;
import net.respectnetwork.sdk.csp.exception.CSPValidationException;
import net.respectnetwork.sdk.csp.model.CSPUserCredential;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

/**
 * Handles requests for the application home page.
 */
@Controller
public class RegistrationController {
    
    /** Class Logger */
    private static final Logger logger = LoggerFactory
            .getLogger(RegistrationController.class);
    
    /** Registration Manager */
    private RegistrationManager theManager;
    
    /** Registration Failure View */
    private String failureView;
    
    /** Registration Success View */
    private String successView;
    
    /** Secret Token */
    private CSPToken secretToken;
    
    /** Session Cookie */
    private String sessionCookieName;
    
    /** Session Length (in seconds)*/  
    private Integer sessionLength = 600;
    
    /** Whether the cookie can only be sent via SSL */  
    private Boolean secureSession = true;
    
    
    /**
     * @return the sessionCookieName
     */
    public String getSessionCookieName() {
        return sessionCookieName;
    }

    /**
     * @param sessionCookieName the sessionCookieName to set
     */
    @Autowired
    @Qualifier("sessionCookieName")
    @Required
    public void setSessionCookieName(String sessionCookieName) {
        this.sessionCookieName = sessionCookieName;
    }

    /**
     * @return the failureView
     */
    public String getFailureView() {
        return failureView;
    }

    /**
     * @param failureView the failureView to set
     */
    @Autowired
    @Qualifier("failureView")
    @Required
    public void setFailureView(String failureView) {
        this.failureView = failureView;
    }
    

    /**
     * @return the successView
     */
    public String getSuccessView() {
        return successView;
    }

    /**
     * @param successView the successView to set
     */
    @Autowired
    @Qualifier("successView")
    @Required
    public void setSuccessView(String successView) {
        this.successView = successView;
    }

    /**
     * 
     * @return
     */
    public RegistrationManager getTheManager() {
        return theManager;
    }

    /**
     * 
     * @param theManager
     */
    @Autowired
    @Qualifier("active")
    @Required
    public void setTheManager(RegistrationManager theManager) {
        this.theManager = theManager;
    }
    
   
    /**
     * Get Session Length
     * @return
     */
    public Integer getSessionLength() {
        return sessionLength;
    }

 
    /**
     * Set the length of the session
     * 
     * @param Length of session
     */
    @Autowired
    @Qualifier("sessionLength")
    public void setSessionLength(Integer sessionLength) {
        this.sessionLength = sessionLength;
    }

    /**
     * 
     * @return
     */
    public Boolean isSecureSession() {
        return secureSession;
    }

   /**
    * 
    * @param secureSession
    */
    @Autowired
    @Qualifier("secureSession")
    public void setSecureSession(Boolean secureSession) {
        this.secureSession = secureSession;
    }

    /**
     * @return the secretToken
     */
    @Autowired
    @Required
    public CSPToken getSecretToken() {
        return secretToken;
    }

    /**
     * @param secretToken
     *            the secretToken to set
     */
    public void setSecretToken(CSPToken secretToken) {
        this.secretToken = secretToken;
    }

    /**
     * Process the Initial SignUp Request
     */
    @RequestMapping(value = "/signup", method = RequestMethod.POST)
    public String signup(Model model, HttpServletResponse response) {
        logger.debug("Starting SignUp Process");
        
        String returnView = "";
              
        try {
            CSPUserCredential theCred = theManager.startSignUpProcess();
            String cloudNumber =  theCred.getCloudNumber().toString();
            logger.debug("Created new CloudNumber: {}", cloudNumber);
            this.setSecretToken(new CSPToken(theCred.getSecretToken()));
            
            createSession(response, cloudNumber); 
            
            returnView = "userdetails";
            model.addAttribute("userInfo", new UserForm());
            model.addAttribute("cloudNumber", cloudNumber);
        } catch (Exception e) {
            String error = "Problem Signing Up User: " + e.getMessage();
            logger.warn(error);
            returnView = this.failureView;
            model.addAttribute("error", error);         
        }
        
        return returnView;        
    }

    
    /**
     * Get the user's basic Information and being the
     * email and phone number (sms) validation processes.
     * This will  create the user's initial graph.
     * 
     * @param userForm Form with User's details
     * @param result Binding Result for Validation or errors
     * @return ModelandView of next  travel location
     */
    @RequestMapping(value = "/getuserdetails", method = RequestMethod.POST)
    public ModelAndView createAndValidateUser(
            @Valid @ModelAttribute("userInfo") UserForm userForm,
            HttpServletRequest request,
            BindingResult result) {
        
                
        logger.debug("Starting Creation/Validation Process");
        logger.debug("Processing User Data: {}", userForm.toString());
        
        ModelAndView mv = null;
        String cloudNumber = getCloudNumberFromSession(request); 
 
        //Go back to destination if errors occur. 
        if (result.hasErrors()) {
            mv = new ModelAndView("userdetails");
            mv.addObject("userInfo", userForm);
        } else {  
            try {  
                if (cloudNumber == null) {
                    throw new CSPValidationException("Invalid Session");
                }
                theManager.createAndValidateUser(cloudNumber, userForm, this.secretToken.getSecretToken());
                mv = new ModelAndView("validatecodes");
                mv.addObject("codeInfo", new CodesForm());
            } catch (CSPValidationException e) {
                String error = "Problem Creating/Validating User: " + e.getMessage();
                logger.debug(error);
                mv = new ModelAndView(this.failureView);                
                mv.addObject("error", error);
            }
        }

        return mv;

    }
    
    
    /**
     * Enter Validation Codes
     */
    @RequestMapping(value = "/enterValidationCodes", method = RequestMethod.GET)
    public ModelAndView enterValidationCodes(
            @Valid @ModelAttribute("codeInfo") CodesForm codesForm,
            HttpServletRequest request,
            BindingResult result) {
        
                
        logger.debug("Processing Validation Codes");
        
        String cloudNumber = null;
              
        ModelAndView mv = null;
        try {
            cloudNumber = (String)request.getParameter("cloudNumber");
            if (cloudNumber != null) {
                cloudNumber = URLDecoder.decode(cloudNumber, "UTF-8");
            }
        } catch (UnsupportedEncodingException e) {
                logger.warn("Problem Encoding Cloud Number: {}", e.getMessage());
        }
        
        logger.debug("Received cloudNumber {} in request paramater", cloudNumber);
 
        //Go back to destination if errors occur. 
        if (result.hasErrors()) {
            mv = new ModelAndView("validatecodes");
        } else {  
   
                if (cloudNumber == null) {
                    mv = new ModelAndView(failureView);
                    mv.addObject("error", "Validation failed. No Clould Number Provided.");
                } else {
                    mv = new ModelAndView("validatecodes");
                    mv.addObject("codeInfo", new CodesForm());
                }

        }

        return mv;

    }
    
    
    
    /**
     * 
     * @param codesForm
     * @param result
     * @return
     */
    @RequestMapping(value = "/validatecodes", method = RequestMethod.POST)
    public ModelAndView validateCodes(
            @Valid @ModelAttribute("codeInfo") CodesForm codesForm,
            HttpServletRequest request,
            BindingResult result) {
        
        logger.debug("Starting Code Checking Process");
        
        ModelAndView mv = null;
        String cloudNumber = getCloudNumberFromSession(request); 

        
        if (result.hasErrors()) {
            mv = new ModelAndView("validatecodes");
            mv.addObject("codeInfo", codesForm);
        }
        
        logger.debug("Processing Codes: {}", codesForm.toString());

        
        if (cloudNumber != null) {         
            if (theManager.validateCodes(cloudNumber, codesForm.getEmailCode(), codesForm.getSMSCode())) {
                mv = new ModelAndView("registercloudname");
                mv.addObject("cloudInfo", new CloudForm());
            } else {
                mv = new ModelAndView("validatecodes");
                mv.addObject("codeInfo", new CodesForm());
                mv.addObject("error", "Validation failed. Please Try again.");
            }
        } else {
            mv = new ModelAndView(failureView);
            mv.addObject("error", "Validation failed. Invalid Session.");
        }
        return mv;        
    }
    
    /**
     * Finish Registration of Cloud with provided Cloud Number and Session Token
     * 
     * @param cloudForm
     * @param result
     * @return Success or Failure Model and View.
     */
    @RequestMapping(value = "/registercloud", method = RequestMethod.POST)
    public ModelAndView registerCloud(
            @Valid @ModelAttribute("cloudInfo") CloudForm cloudForm,
            HttpServletRequest request,
            BindingResult result) {
        
        logger.debug("Starting Cloud Creation Process");
        
        ModelAndView mv = null;
        String cloudNumber = getCloudNumberFromSession(request); 

      
        if (result.hasErrors()) {
            mv = new ModelAndView("registercloudname");
            mv.addObject("codeInfo", cloudForm);
        }
        
        logger.debug("Processing Cloud Creation: {}", cloudForm.toString());
        
        
        if (cloudNumber == null) {
            mv = new ModelAndView(failureView);
            mv.addObject("error", "Registration Failed: Invalid Session");
        } else {
            try {
               String cloudName = cloudForm.getCloudName();
               theManager.registerNewUser(cloudNumber, cloudName, cloudForm.getSecretToken());
               mv = new ModelAndView(successView);
            } catch (Exception e) {
               String errorMsg = "Registration Failed: " + e.getMessage();
               mv = new ModelAndView(failureView);
               mv.addObject("error", errorMsg);
            }
        }
                              
        return mv;        
    }
    
    /**
     * Extract Cloud Number from Session
     * 
     * @param cloudNumber
     * @return
     */
    private String getCloudNumberFromSession(HttpServletRequest request) {
        
        String cloudNumber = null;
        
        Cookie[] theCookies = request.getCookies();
        if (theCookies != null) {
            for (int i = 0; i < theCookies.length; i++) {
                if ( theCookies[i].getName().equalsIgnoreCase(sessionCookieName)){
                    cloudNumber = theCookies[i].getValue();
                }
            }
        }
        
        return cloudNumber;
      
    }
    
    /**
     * Create Session by adding Cookies to the response
     * @param response
     */
    private void createSession( HttpServletResponse response, String cloudNumber) {
                
        Cookie sessionCookie = new Cookie(sessionCookieName, cloudNumber);
        sessionCookie.setMaxAge(sessionLength);
        sessionCookie.setSecure(secureSession);
        response.addCookie(sessionCookie);
        
        return;
    }

}
