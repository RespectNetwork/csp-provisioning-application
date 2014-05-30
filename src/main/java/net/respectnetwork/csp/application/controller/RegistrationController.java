package net.respectnetwork.csp.application.controller;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Enumeration;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import net.respectnetwork.csp.application.csp.CurrencyCost;
import net.respectnetwork.csp.application.dao.DAOException;
import net.respectnetwork.csp.application.dao.DAOFactory;
import net.respectnetwork.csp.application.exception.UserRegistrationException;
import net.respectnetwork.csp.application.form.PaymentForm;
import net.respectnetwork.csp.application.form.SignUpForm;
import net.respectnetwork.csp.application.form.UserDetailsForm;
import net.respectnetwork.csp.application.form.ValidateForm;
import net.respectnetwork.csp.application.manager.RegistrationManager;
import net.respectnetwork.csp.application.model.CSPCostOverrideModel;
import net.respectnetwork.csp.application.model.CSPModel;
import net.respectnetwork.csp.application.session.RegistrationSession;
import net.respectnetwork.sdk.csp.validation.CSPValidationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import xdi2.core.xri3.CloudNumber;

import com.maxmind.geoip.Location;
import com.maxmind.geoip.LookupService;

/**
 * Handles requests for the application home page.
 */
@Controller
public class RegistrationController
{

   /** Class Logger */
   private static final Logger logger = LoggerFactory
                                            .getLogger(RegistrationController.class);

   /** Registration Manager */
   private RegistrationManager theManager;

   /** Registration Session */
   private RegistrationSession regSession;

   private String              cspCloudName;
   
   public static final String URL_PARAM_NAME_REQ_CLOUDNAME     = "name"   ;

   private static LookupService        geoIpLookupService = null;

   private static synchronized void geoIpLookupServiceInit()
   {
           if( geoIpLookupService != null )
           {
                   return;
           }
           URL fileResource = RegistrationController.class.getClassLoader()
                 .getResource("GeoLiteCity.dat"); 
           if(fileResource != null)
           {
              String fileName = fileResource.getFile() ; 
              logger.info("GeoIpLookupServiceInit - " + fileName);
              try
              {
                      geoIpLookupService = new LookupService(fileName, LookupService.GEOIP_MEMORY_CACHE);
                      logger.info("GeoIpLookupServiceInit - " + fileName + " Done " + geoIpLookupService);
              }
              catch( java.io.IOException e )
              {
                      logger.error("Cannot initialize GeoIpLookupService - " + fileName, e);
              }
           } else
           {
              logger.error("Cannot initialize GeoIpLookupService - " + "GeoLiteCity.dat");
           }
           
   }

   public String getCspCloudName()
   {
      return this.cspCloudName;
   }

   @Autowired
   @Qualifier("cspCloudName")
   public void setCspCloudName(String cspCloudName)
   {
      this.cspCloudName = cspCloudName;
   }

   /**
    * 
    * @return
    */
   public RegistrationManager getTheManager()
   {
      return theManager;
   }

   /**
    * 
    * @param theManager
    */
   @Autowired
   @Qualifier("active")
   @Required
   public void setTheManager(RegistrationManager theManager)
   {
      this.theManager = theManager;
   }

   /**
    * @return the regSession
    */
   public RegistrationSession getRegSession()
   {
      return regSession;
   }

   /**
    * @param regSession
    *           the regSession to set
    */
   @Autowired
   public void setRegSession(RegistrationSession regSession)
   {
      this.regSession = regSession;
   }
    /**
     * Flag to check if referer required or not to register
     */
    @Value("${csp.refererRequired}")
    private String refererRequired;
    /**
     * Referer URL
     */
    @Value("${csp.refererURL}")
    private String refererURL;

   /**
    * Initial Sign-Up Page
    */
   @RequestMapping(value = "/signup", method = RequestMethod.POST)
   public ModelAndView signup(
         @Valid @ModelAttribute("signUpInfo") SignUpForm signUpForm,
         HttpServletRequest request, HttpServletResponse response,
         BindingResult result)
   {

      logger.debug("Starting the Sign Up Process");

      ModelAndView mv = null;
      boolean errors = false;

      String cloudName = signUpForm.getCloudName();
      
      String inviteCode = signUpForm.getInviteCode();
      String giftCode = signUpForm.getGiftCode();

      logger.debug("Invite Code = " + inviteCode);
      logger.debug("Gift Code = " + giftCode);
      logger.debug("Cloud Name : " + cloudName);

      mv = new ModelAndView("signup");

      if (cloudName != null)
      {
         // Start Check that the Cloud Number is Available.
         if(!cloudName.startsWith("="))
         {
            cloudName = "=" + cloudName; 
         }
         try
         {
            if (theManager.isRequireInviteCode()
                  && ((inviteCode == null) || (inviteCode.trim().isEmpty())))
            {
               errors = true;
               logger.debug("Invite code is required and it has not been passed in the input query parameters(inviteCode=)");
            } else if (!theManager.isCloudNameAvailable(cloudName))
            {
               String errorStr = "CloudName not Available";
               mv.addObject("cloudNameError", errorStr);
               errors = true;
            }
         } catch (UserRegistrationException e)
         {
            String errorStr = "System Error checking CloudName";
            logger.warn(errorStr + " : {}", e.getMessage());
            mv.addObject("error", errorStr);
            errors = true;
         }
         UserDetailsForm userDetailsForm = new UserDetailsForm();
         userDetailsForm.setCloudName(cloudName);
         
         /*
         InviteModel invite = null;
         // add the email address of the person who was invited in the
         // userDetailsForm object, the user shouldn't be able to change it
         DAOFactory dao = DAOFactory.getInstance();
         UserDetailsForm userDetailsForm = new UserDetailsForm();
         try
         {
            invite = dao.getInviteDAO().get(inviteCode);
            if (invite != null
                  && (invite.getInvitedEmailAddress() != null && !invite
                        .getInvitedEmailAddress().trim().isEmpty()))
            {
               userDetailsForm.setEmail(invite.getInvitedEmailAddress());
            } else
            {
               logger.error("This invite object does not have an email address or another valid identifier associated to it. Sending user to the signup page.");
               errors = true;
            }
         } catch (DAOException e)
         {
            logger.error("Could not get invite information from DB. Sending user to the signup page.");
            errors = true;
         }
         */
         if (!errors)
         {
            mv = new ModelAndView("userdetails");

            mv.addObject("userInfo", userDetailsForm);

            // Add CloudName to Session

            String sessionId = UUID.randomUUID().toString();
            regSession.setSessionId(sessionId);
            regSession.setCloudName(cloudName);
            //regSession.setInviteCode(inviteCode);
            regSession.setGiftCode(giftCode);
            //regSession.setVerifiedEmail(invite.getInvitedEmailAddress());

         } else
         {
            mv = new ModelAndView("signup");
         }

      }
      mv.addObject("signupInfo", signUpForm);

      return mv;
   }

   /**
    * Get User Details
    */
   @RequestMapping(value = "/processuserdetails", method = RequestMethod.POST)
   public ModelAndView getDetails(
         @Valid @ModelAttribute("userInfo") UserDetailsForm userDetailsForm,
         HttpServletRequest request, HttpServletResponse response,
         BindingResult result)
   {

      logger.debug("Get User Details");

      ModelAndView mv = null;
      boolean errors = false;
      mv = new ModelAndView("userdetails");
      mv.addObject("userInfo", userDetailsForm);

      String cn = regSession.getCloudName();
      String sessionId = regSession.getSessionId();

      // Session Check
      if (sessionId == null || cn == null)
      {
         errors = true;
         mv.addObject("error", "Invalid Session");
      }

      if (!errors)
      {
         
         try
         {
         // validate email address entered by user
         if (!org.apache.commons.validator.routines.EmailValidator
                  .getInstance().isValid(userDetailsForm.getEmail())) 
         {
              String errorStr = "Invalid Email Address.";
              logger.debug("Invalid Email address entered..."
                      + userDetailsForm.getEmail());
              mv.addObject("error", errorStr);
              errors = true;
              return mv;
          }
           if(!RegistrationManager.validatePhoneNumber(userDetailsForm.getPhone()))
           {
              String errorStr = "Invalid Phone Number. Please provide your phone number in international format (+ccnnnnnnnnn)";
              logger.debug("Invalid Phone Number entered..."
                      + userDetailsForm.getPhone());
              mv.addObject("error", errorStr);
              errors = true;
              return mv;  
           }
           if(!RegistrationManager.validatePassword(userDetailsForm.getPassword()))
           {
              String errorStr = "Invalid password. Please provide a password that is at least 8 characters, have at least 2 letters, 2 numbers and at least one special character, e.g. @, #, $ etc.";
              logger.debug("Invalid password."
                      + userDetailsForm.getPassword());
              mv.addObject("error", errorStr);
              errors = true;
              return mv;  
           }
           if(!userDetailsForm.getPassword().equals(userDetailsForm.getConfirmPassword()))
           {
              String errorStr = "Password and confirm password fields do not match.";
              logger.debug("Password and confirm password fields do not match"
                      + userDetailsForm.getPassword() + ":" + userDetailsForm.getConfirmPassword());
              mv.addObject("error", errorStr);
              errors = true;
              return mv;
           }
            CloudNumber[] existingUsers = theManager
                  .checkEmailAndMobilePhoneUniqueness(
                        userDetailsForm.getPhone(),
                        userDetailsForm.getEmail());
            
            if (existingUsers[0] != null)
            {
               // Communicate back to Form phone is already taken
               String errorStr = "Phone number has already been used for a cloud name";
               mv.addObject("error", errorStr);
               logger.debug("Phone {} already used by {}",
                     userDetailsForm.getPhone(), existingUsers[0]);
               errors = true;
            }
            if (existingUsers[1] != null)
            {
               String errorStr = "Email has already been used for a cloud name";
               mv.addObject("error", errorStr);
               logger.debug("Email {} already used by {}",
                     userDetailsForm.getEmail(), existingUsers[1]);
               errors = true;
            }
            
            /*
            if (existingUsers[0] != null && existingUsers[1] != null)
            {
               String errorStr = "The Email and phone combination has already been used for a cloud name";
               mv.addObject("error", errorStr);
               logger.debug("Email {} already used by {} , phone {} already used by {} ",
                     userDetailsForm.getEmail(), existingUsers[1], userDetailsForm.getMobilePhone(),existingUsers[0]);
               errors = true;
            }
            */
         } catch (UserRegistrationException e)
         {
            String errorStr = "System Error checking Email/Phone Number Uniqueness";
            logger.warn(errorStr + " : {}", e.getMessage());
            mv.addObject("error", errorStr);
            errors = true;
         }
      }

      if (!errors)
      {

         // If all is okay send out the validation messages.
         try
         {
            theManager.sendValidationCodes(sessionId,
                  userDetailsForm.getEmail(), userDetailsForm.getPhone());
         } catch (CSPValidationException e)
         {
            String errorStr = "System Error sending validation messages. Please check email and mobile phone number.";
            logger.warn(errorStr + " : {}", e.getMessage());
            mv.addObject("error", errorStr);
            errors = true;
         }
      }

      if (!errors)
      {
         mv = new ModelAndView("validate");
         ValidateForm validateForm = new ValidateForm();
         mv.addObject("validateInfo", validateForm);
         mv.addObject("cloudName", regSession.getCloudName());
         mv.addObject("verifyingEmail", userDetailsForm.getEmail());
         mv.addObject("verifyingPhone", userDetailsForm.getPhone());

         // Add CloudName/ Email / Password and Phone to Session

         logger.debug("Setting verified email " + regSession.getVerifiedEmail());
         regSession.setVerifiedEmail(userDetailsForm.getEmail());
         regSession.setVerifiedMobilePhone(userDetailsForm.getPhone());
         regSession.setPassword(userDetailsForm.getPassword());
      }

      mv.addObject("userInfo", userDetailsForm);

      return mv;
   }

   /**
    * Validate Confirmation Codes,
    * 
    * 
    * @param userForm
    *           Form with User's details
    * @param result
    *           Binding Result for Validation or errors
    * @return ModelandView of next travel location
    */
   @RequestMapping(value = "/validatecodes", method = RequestMethod.POST)
   public ModelAndView validateCodes(
         @Valid @ModelAttribute("validateInfo") ValidateForm validateForm,
         HttpServletRequest request, BindingResult result)
   {

      logger.debug("Starting Validation Process");
      logger.debug("Processing Validation Data: {}", validateForm.toString());

      boolean errors = false;
      String errorStr = "";
      ModelAndView mv = new ModelAndView("validate");
      String sessionIdentifier = regSession.getSessionId();
      String verifyingEmail = request.getParameter("verifyingEmail");
      String verifyingPhone = request.getParameter("verifyingPhone");
	  // To check if request comes from reset password 
      boolean resetPwd = Boolean.parseBoolean(request.getParameter("resetPwd"));
      mv.addObject("validateInfo", validateForm);
      mv.addObject("cloudName", regSession.getCloudName());
      mv.addObject("verifyingEmail", verifyingEmail);
      mv.addObject("verifyingPhone", verifyingPhone);
      mv.addObject("resetPwd", resetPwd);
      if(request.getParameter("resendCodes") != null)
      {
         try
         {
            theManager.sendValidationCodes(sessionIdentifier,
                  verifyingEmail, verifyingPhone);
            
            
            return mv;
         } catch (CSPValidationException e)
         {
            errorStr = "System Error sending validation messages. Please check email and phone number.";
            logger.warn(errorStr + " : {}", e.getMessage());
            mv.addObject("error", errorStr);
            errors = true;
            return mv;
         }
      }
      logger.debug("RN Terms checkbox ..." + request.getParameter("terms"));
      if(request.getParameter("terms") == null || !request.getParameter("terms").equalsIgnoreCase("on"))
      {
         errorStr = "Please agree to the Respect Trust Framework to continue";
         logger.debug("Respect Trust Framework not checked ...."
                 + request.getParameter("terms"));
         mv.addObject("error", errorStr);
         errors = true;
         return mv;
         
      }
      

      // Validate Codes
      if (!theManager.validateCodes(sessionIdentifier,
            validateForm.getEmailCode().trim().toUpperCase(), validateForm.getSmsCode().trim().toUpperCase()))
      {
         errorStr = "Email and/or phone code validation failed. Please enter the codes correctly.";
         logger.debug(errorStr);
         mv.addObject("error", errorStr);
         

         errors = true;
      }

      CSPModel cspModel = null;

      if (!errors)
      {

         try
         {
            cspModel = DAOFactory.getInstance().getCSPDAO()
                  .get(this.getCspCloudName());
         } catch (DAOException e)
         {
            // TODO Auto-generated catch block
            e.printStackTrace();
            errors = true;
         }

      }

      if (!errors) {
         if (!resetPwd) {
			 mv = new ModelAndView("payment");
			 mv.addObject("cspTCURL", this.getTheManager().getCspTCURL());
			 PaymentForm paymentForm = new PaymentForm();
			 paymentForm.setTxnType(PaymentForm.TXN_TYPE_SIGNUP);
			 if(regSession != null)
			 {
				regSession.setTransactionType(PaymentForm.TXN_TYPE_SIGNUP);
			 }
			 paymentForm.setNumberOfClouds(1);
			 if(regSession.getGiftCode() != null && !regSession.getGiftCode().isEmpty())
			 {
				logger.debug("Setting giftcode from session " + regSession.getGiftCode());
				paymentForm.setGiftCodes(regSession.getGiftCode());
			 }
			 if(cspModel.getPaymentGatewayName().equals("GIFT_CODE_ONLY"))
			 {
				paymentForm.setGiftCodesOnly(true);
			 }
			 mv.addObject("paymentInfo", paymentForm);

			 // Check for cost override based on phone number
			 CurrencyCost totalCost = getCostIncludingOverride(cspModel,
					 regSession.getVerifiedMobilePhone(),
					 paymentForm.getNumberOfClouds());

			 regSession.setCurrency(totalCost.getCurrencyCode());
			 regSession.setCostPerCloudName(totalCost.getAmount());

			 mv.addObject("totalAmountText", formatCurrencyAmount(totalCost));
			 mv.addObject("paymentInfo", paymentForm);
		 } else {
		     mv = new ModelAndView("resetPassword");
             mv.addObject("cloudName", regSession.getCloudName());
		 }
      }

      return mv;
   }

   /**
    * Calculate the cost of buying cloudnames, taking cost overrides into account
    */
   static CurrencyCost getCostIncludingOverride(CSPModel cspModel, String phoneNumber, int numberOfClouds) {
      String currency = cspModel.getCurrency();
      BigDecimal costPerCloud = cspModel.getCostPerCloudName();

      CSPCostOverrideModel cspCostOverrideModel = null;
      try
      {
         cspCostOverrideModel = DAOFactory.getInstance().getcSPCostOverrideDAO()
                 .get(cspModel.getCspCloudName(), phoneNumber);
         if (cspCostOverrideModel != null)
         {
            logger.debug("Cost override found: " + cspCostOverrideModel.toString());
            currency = cspCostOverrideModel.getCurrency();
            costPerCloud = cspCostOverrideModel.getCostPerCloudName();
         } else
         {
            logger.debug("No cost override found (using default cost)");
         }
      } catch (DAOException e)
      {
         logger.error(e.toString());
      }

      CurrencyCost costOneCloud = new CurrencyCost(currency, costPerCloud);
      return costOneCloud.multiply(numberOfClouds);
   }

   /**
    * Format a currency and amount for human display.
    */
   static String formatCurrencyAmount(String currency, BigDecimal amount)
   {
      // Hack - JDK doesn't seem to have an easy locale-independent way to get this symbol
      String currencySymbol = "";
      if (currency.equals("USD") || currency.equals("AUD"))
      {
         currencySymbol = "$";
      }
      return String.format("%s%04.2f %s", currencySymbol, amount, currency);
   }

   /**
    * Format a currency and amount for human display.
    */
   static String formatCurrencyAmount(CurrencyCost currencyCost) {
      return formatCurrencyAmount(currencyCost.getCurrencyCode(), currencyCost.getAmount());
   }


   /**
    * This is the endpoint where the user lands in the CSP website from an
    * invite
    * 
    * @param request
    *           : some query parameters that come from RN which are to be echoed
    *           back and a query parameter called "name" which has the cloudname
    * @param response
    * @return
    */
   @RequestMapping(value = "/register", method = RequestMethod.GET)
   public ModelAndView registerCloudName(HttpServletRequest request,
         HttpServletResponse response,
         @Valid @ModelAttribute("signUpInfo") SignUpForm signUpForm,
         BindingResult result)
   {
      boolean errors = false;
      String error = "";
      ModelAndView mv = null;
      String rnQueryString = "";
      //SignUpForm signUpForm = new SignUpForm();
      logger.debug("Referer URL " + request.getHeader("referer"));
      // check for referer URL and if it does not match with the configured
      // one in the
      // properties, then re-direct  to referer URL mentioned in the config file.
      if (Boolean.parseBoolean(refererRequired)
              && (request.getHeader("referer") == null || !request.getHeader(
                      "referer").equals(refererURL))) {
            return new ModelAndView("redirect:"+refererURL);
      }
      Enumeration<String> paramNames = request.getParameterNames(); 
      while(paramNames.hasMoreElements())
      {
         String paramName = paramNames.nextElement();
         logger.debug("p name " + paramName);
         String[] paramValues = request.getParameterValues(paramName);
         for(int i = 0 ; i < paramValues.length ; i++)
         {
            logger.debug("p value " + paramValues[i]);
          //ignore the "name" parameter. Capture rest of it
            if(!paramName.equalsIgnoreCase(URL_PARAM_NAME_REQ_CLOUDNAME))
            {
               try
               {
                  rnQueryString = rnQueryString + "&" + paramName + "=" + URLEncoder.encode(paramValues[i], "UTF-8");
               } catch (UnsupportedEncodingException e)
               {
                  // TODO Auto-generated catch block
                  e.printStackTrace();
               }
            }
         }
      }
      
      String remoteIPAddr = request.getHeader("X-FORWARDED-FOR");
      
      logger.debug("User agent " + request.getHeader("User-Agent"));

      if(remoteIPAddr == null)
      {
         remoteIPAddr = request.getRemoteAddr();
         
      }
      logger.debug("Client IP " + remoteIPAddr);
      logger.debug("Referer URL " + request.getHeader("referer"));
      //TODO : check for referer URL and if it does not match with the configured one in the 
      //properties, then bail out
      
      logger.info("getLocation - " + remoteIPAddr);

      if( geoIpLookupService == null )
      {
              geoIpLookupServiceInit();
      }
      Location loc = geoIpLookupService.getLocation(remoteIPAddr);
      if( loc == null )
      {
              logger.info("Cannot find location for IP address - " + remoteIPAddr);
              remoteIPAddr = "209.173.53.233";
              loc = geoIpLookupService.getLocation(remoteIPAddr);
      }

      logger.info("getLocation - " + remoteIPAddr + " LAT = " + loc.latitude + " LNG = " + loc.longitude);
      theManager.getEndpointURI(RegistrationManager.GeoLocationPostURIKey, theManager.getCspRegistrar().getCspInformation().getRnCloudNumber());     

      String cloudName = null;
      if (signUpForm != null && signUpForm.getCloudName() != null)
      {
         cloudName = signUpForm.getCloudName();
         try
         {
            cloudName = URLDecoder.decode(cloudName,"UTF-8");
         } catch (UnsupportedEncodingException e)
         {
            logger.debug("Exception for cloudname " + cloudName);
            logger.debug(e.getMessage());
            errors = true;
            error = "Sorry ! The system has encountered an error. Please try again.";
         }
      } else
      {
         cloudName = request.getParameter(URL_PARAM_NAME_REQ_CLOUDNAME);
      }
      
      logger.info("registerCloudName : registration request for cloudname " + cloudName);
      
      if (cloudName != null)
      {
         if(!RegistrationManager.validateCloudName(cloudName))
         {
            errors = true;
            error = RegistrationManager.validINameFormat;
         }
         try
         {
			 //Added one more condition to check registry too using AvailabilityAPI for cloud name. 
            if (theManager.isCloudNameAvailableInRegistry(cloudName) && theManager.isCloudNameAvailable(cloudName))
            {
               logger.info(cloudName + " is available, so going to show the validation screen");
               mv = new ModelAndView("userdetails");
               UserDetailsForm userDetailsForm = new UserDetailsForm();
               userDetailsForm.setCloudName(cloudName);
               mv.addObject("userInfo", userDetailsForm);
               // Add CloudName to Session
               String sessionId = UUID.randomUUID().toString();
               regSession.setSessionId(sessionId);
               regSession.setCloudName(cloudName);
               regSession.setRnQueryString(rnQueryString);
               regSession.setLongitude((long)loc.longitude);
               regSession.setLatitude((long)loc.latitude);
               regSession.setGiftCode(null);;
            } else 
            {
               errors = true;
               error = "CloudName is not available. Please choose another valid CloudName";
            }
         } catch (Exception e) {
            logger.info("Exception in registerCloudName " + e.getMessage());
            errors = true;
            error = "Sorry ! The system has encountered an error. Please try again.";
            // e.printStackTrace();
         }

      } else
      {
         errors = true;
         error = "Please provide a valid cloudname";
      }
      if(errors)
      {
         mv = new ModelAndView("signup");
         mv.addObject("error", error);
         return mv;
      }
      mv.addObject("signupInfo", signUpForm);
      mv.addObject("cloudName", cloudName);
      return mv;
   }

}
