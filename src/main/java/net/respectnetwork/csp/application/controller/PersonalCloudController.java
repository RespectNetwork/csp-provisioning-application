package net.respectnetwork.csp.application.controller;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import net.respectnetwork.csp.application.dao.DAOException;
import net.respectnetwork.csp.application.dao.DAOFactory;
import net.respectnetwork.csp.application.form.AccountDetailsForm;
import net.respectnetwork.csp.application.form.DependentForm;
import net.respectnetwork.csp.application.form.InviteForm;
import net.respectnetwork.csp.application.form.PaymentForm;
import net.respectnetwork.csp.application.invite.InvitationManager;
import net.respectnetwork.csp.application.manager.BrainTreePaymentProcessor;
import net.respectnetwork.csp.application.manager.PersonalCloudManager;
import net.respectnetwork.csp.application.manager.RegistrationManager;
import net.respectnetwork.csp.application.manager.SagePayPaymentProcessor;
import net.respectnetwork.csp.application.manager.StripePaymentProcessor;
import net.respectnetwork.csp.application.model.CSPModel;
import net.respectnetwork.csp.application.model.GiftCodeModel;
import net.respectnetwork.csp.application.model.GiftCodeRedemptionModel;
import net.respectnetwork.csp.application.model.InviteModel;
import net.respectnetwork.csp.application.model.PaymentModel;
import net.respectnetwork.csp.application.session.RegistrationSession;
import net.respectnetwork.sdk.csp.exception.CSPRegistrationException;

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

import xdi2.client.exceptions.Xdi2ClientException;
import xdi2.core.xri3.CloudName;
import xdi2.core.xri3.CloudNumber;

import com.sagepay.sdk.api.ApiFactory;
import com.sagepay.sdk.api.IFormApi;
import com.sagepay.sdk.api.ResponseStatus;
import com.sagepay.sdk.api.messages.IFormPayment;
import com.sagepay.sdk.api.messages.IFormPaymentResult;
import com.sagepay.util.web.RequestState;

@Controller
public class PersonalCloudController
{

   /** CLass Logger */
   private static final Logger  logger = LoggerFactory
                                             .getLogger(PersonalCloudController.class);

   /**
    * Invitation Service : to create invites and gift codes
    */
   private InvitationManager    invitationManager;

   /**
    * Registration Service : to register dependent clouds
    */
   private  RegistrationManager  registrationManager;

   /**
    * Personal Cloud Service : to authenticate to personal cloud and get/set
    * information from/to the personal cloud
    */
   private PersonalCloudManager personalCloudManager;

   

   /** Registration Session */
   private RegistrationSession  regSession;

   /**
    * CSP Cloud Name
    */
   private String               cspCloudName;
   
   

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
    * @return the invitationManager
    */
   public InvitationManager getInvitationManager()
   {
      return invitationManager;
   }

   /**
    * @param invitationManager
    *           the invitationManager to set
    */
   @Autowired
   @Required
   public void setInvitationManager(InvitationManager invitationManager)
   {
      this.invitationManager = invitationManager;
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

   @RequestMapping(value = "/login", method = RequestMethod.GET)
   public ModelAndView showLoginForm(HttpServletRequest request, Model model)
   {
      logger.info("showing login form");

      ModelAndView mv = null;

      String cspHomeURL = request.getContextPath();
      String formPostURL = cspHomeURL + "/cloudPage";

      mv = new ModelAndView("login");
      mv.addObject("postURL", formPostURL);
      return mv;
   }

   @RequestMapping(value = "/cloudPage", method =
   { RequestMethod.POST, RequestMethod.GET })
   public ModelAndView showCloudPage(HttpServletRequest request, Model model)
   {
      logger.info("showing cloudPage form");

      ModelAndView mv = null;
      CloudName cloudName = null;
      boolean errors = false;
      logger.info("Cloudname from request parameter "
            + request.getParameter("cloudname"));
      if (request.getParameter("cloudname") != null)
      {
         cloudName = CloudName.create(request.getParameter("cloudname"));
      } else if (regSession.getCloudName() != null)
      {
         cloudName = CloudName.create(regSession.getCloudName());
      }
      if (cloudName == null)
      {
         return processLogout(request, model);
      }
      logger.info("Logging in for cloudname " + cloudName.toString());
      net.respectnetwork.sdk.csp.CSP myCSP = registrationManager
            .getCspRegistrar();
      if (myCSP == null)
      {
         logger.info("myCSP is null!");
      }
      if (myCSP != null)
      {
         logger.info("CSP Info:" + myCSP.toString());
         CloudNumber cloudNumber;
         try
         {
            cloudNumber = myCSP.checkCloudNameAvailableInRN(cloudName);
            String secretToken = null;
            if (regSession != null)
            {
               secretToken = regSession.getPassword();
            }
            if (secretToken == null || secretToken.isEmpty())
            {
               secretToken = request.getParameter("secrettoken");
            }
            if (cloudNumber != null)
            {
               myCSP.authenticateInCloud(cloudNumber, secretToken);
               if (regSession != null && regSession.getCloudName() == null)
               {

                  if (regSession.getSessionId() == null
                        || regSession.getSessionId().isEmpty())
                  {
                     String sessionId = UUID.randomUUID().toString();
                     regSession.setSessionId(sessionId);
                     logger.info("Creating a new regSession with session id ="
                           + sessionId);
                  }
                  logger.info("Setting cloudname as  "
                        + cloudName);
                  if (request.getParameter("cloudname") != null)
                  {
                     regSession.setCloudName(request.getParameter("cloudname"));
                  }
                  // logger.info("Setting secret token as  " +
                  // request.getParameter("secrettoken"));
                  if (request.getParameter("secrettoken") != null)
                  {
                     regSession
                           .setPassword(request.getParameter("secrettoken"));
                  }
               }
               mv = getCloudPage(request, regSession.getCloudName());
               logger.info("Successfully authenticated to the personal cloud for "
                     + cloudName);
            } else
            {
               errors = true;
            }
         } catch (Xdi2ClientException e)
         {
            // TODO Auto-generated catch block
            e.printStackTrace();
            errors = true;
            logger.debug("Authenticating to personal cloud failed for "
                  + request.getParameter("cloudname"));
         }

      } else
      {
         logger.info("CSP Object is null. ");
         errors = true;
      }
      if (errors)
      {
         String cspHomeURL = request.getContextPath();
         String formPostURL = cspHomeURL + "/cloudPage";
         mv = new ModelAndView("login");
         mv.addObject("postURL", formPostURL);

      }

      return mv;
   }

   public RegistrationManager getRegistrationManager()
   {
      return registrationManager;
   }

   @Autowired
   public void setRegistrationManager(RegistrationManager registrationManager)
   {
      this.registrationManager = registrationManager;
   }

   public PersonalCloudManager getPersonalCloudManager()
   {
      return personalCloudManager;
   }

   @Autowired
   public void setPersonalCloudManager(PersonalCloudManager personalCloudManager)
   {
      this.personalCloudManager = personalCloudManager;
   }

   @RequestMapping(value = "/logout", method =
   { RequestMethod.GET, RequestMethod.POST })
   public ModelAndView processLogout(HttpServletRequest request, Model model)
   {
      logger.info("processing logout");

      // nullify password from the session object
      if (regSession != null)
      {
         regSession.setCloudName(null);
         regSession.setPassword(null);
         regSession.setVerifiedEmail(null);
         regSession.setDependentForm(null);
         regSession.setGiftCode(null);
         regSession.setInviteCode(null);
         regSession.setInviteForm(null);
         regSession.setSessionId(null);
         regSession.setVerifiedMobilePhone(null);
      }
      ModelAndView mv = null;

      String cspHomeURL = request.getContextPath();
      String formPostURL = cspHomeURL + "/cloudPage";

      mv = new ModelAndView("login");
      mv.addObject("postURL", formPostURL);
      return mv;
   }

   @RequestMapping(value = "/ccpayment", method = {RequestMethod.POST, RequestMethod.GET})
   public ModelAndView processCCPayment(
         @Valid @ModelAttribute("paymentInfo") PaymentForm paymentForm,
         HttpServletRequest request, HttpServletResponse response , Model model, BindingResult result)
   {
      logger.info("processing CC payment");

      boolean errors = false;
      String errorText = "";

      String cloudName = regSession.getCloudName();
      

      ModelAndView mv = null; 

      String sessionIdentifier = regSession.getSessionId();
      String email = regSession.getVerifiedEmail();
      String phone = regSession.getVerifiedMobilePhone();
      String password = regSession.getPassword();
      logger.debug(sessionIdentifier + "--" + cloudName + "--" + email + "--"
            + phone + "--" + password);
      
      String txnType = paymentForm.getTxnType();
      if(txnType == null || txnType.isEmpty())
      {
         txnType = regSession.getTransactionType();
      }
       
      logger.debug("Transaction type = " + txnType);
      
      logger.debug("Number of clouds being purchased "
            + paymentForm.getNumberOfClouds());
      
      // Check Session
      if (sessionIdentifier == null || cloudName == null || password == null)
      {
         errors = true;
         errorText = "Invalid Session";
         
         logger.debug("Invalid Session ...");
      }
      CSPModel cspModel = null;
      DAOFactory dao = DAOFactory.getInstance();
      try
      {
         cspModel = dao.getCSPDAO().get(this.getCspCloudName());
      } catch (DAOException e1)
      {
         // TODO Auto-generated catch block
         e1.printStackTrace();
         errors = true;
         errorText = "Cannot connect to DB to lookup information";
         
         logger.debug("Cannot connect to DB to lookup info...");
      }

      String forwardingPage = request.getContextPath();
      
      String statusText = "";
      
      if (!errors)
      {
         
         BigDecimal amount = null;
         if (cspModel.getPaymentGatewayName().equals("STRIPE") || cspModel.getPaymentGatewayName().equals("BRAINTREE"))
         {
            amount = cspModel.getCostPerCloudName().multiply(
               new BigDecimal(paymentForm.getNumberOfClouds()));
            logger.debug("Charging CC for " + amount.toPlainString());
         }

         
         PaymentModel payment = null;
         if(cspModel.getPaymentGatewayName().equals("STRIPE"))
         {
            String desc = "";
            if(txnType.equals(PaymentForm.TXN_TYPE_SIGNUP))
            {
               desc = "Personal cloud for " + cloudName;
            } else if (txnType.equals(PaymentForm.TXN_TYPE_BUY_GC))
            {
               desc = paymentForm.getNumberOfClouds() + " giftcodes for " + cloudName;
            }
            String token = StripePaymentProcessor.getToken(request); 
            payment = StripePaymentProcessor.makePayment(cspModel,
                  amount, desc, token);
         } else if (cspModel.getPaymentGatewayName().equals("BRAINTREE"))
         {
            payment = BrainTreePaymentProcessor.makePayment(cspModel, amount, request);
         } else if (cspModel.getPaymentGatewayName().equals("SAGEPAY"))
         {
            payment = SagePayPaymentProcessor.processSagePayCallback(request, response, cspModel);
         }
            
            if (payment != null)
            {

               try
               {
                  dao.getPaymentDAO().insert(payment);
               } catch (DAOException e1)
               {
                  logger.error("Could not insert payment record in the DB "
                        + e1.getMessage());
                  logger.info("Payment record info \n" + payment.toString());
               }
               
               
               if(txnType.equals(PaymentForm.TXN_TYPE_SIGNUP))
               {     
                  if (this.registerCloudName(cloudName, phone, email, password))
                  {
                     forwardingPage += "/cloudPage";
                     statusText = "Congratulations " + cloudName + "! You have successfully purchased a cloudname.";
                  } else
                  {
                     forwardingPage += "/signup";
                     statusText = "Sorry! The system encountered an error while registering your cloudname.";
                     
                  }
               }
               else if (txnType.equals(PaymentForm.TXN_TYPE_DEP))
               {
                  if((mv = createDependentClouds(cloudName,payment,null,request)) != null)
                  {
                     forwardingPage += "/cloudPage";
                     statusText = "Congratulations " + cloudName + "! You have successfully purchased dependent clouds.";
                  } else
                  {
                     forwardingPage += "/cloudPage";
                     statusText = "Sorry! The system encountered an error while registering dependent clouds.";
                     
                  }

               }
               else if(txnType.equals(PaymentForm.TXN_TYPE_BUY_GC))
               {
                  if((mv = this.createGiftCards(request, cloudName, payment, cspModel)) != null)
                  {
                     forwardingPage += "/cloudPage";
                     statusText = "Congratulations " + cloudName + "! You have successfully purchased giftcodes.";
                  } else
                  {
                     forwardingPage += "/cloudPage";
                     statusText = "Sorry! The system encountered an error while purchasing giftcodes.";                     
                  }
               }
               else
               {
                  forwardingPage += "/login";
                  statusText = "Sorry. Something bad happened while processing your request. Returning you to login page. Please try again.";
               }
               
            } else {
               statusText = "Sorry ! Payment Processing Error";
            }

         
      } 
      mv = new ModelAndView("AutoSubmitForm");
      
      mv.addObject("URL", request.getContextPath() + "/transactionSuccessFailure");
      mv.addObject("cloudName", cloudName);
      
      mv.addObject("statusText", statusText);
      mv.addObject("nextHop", forwardingPage);
      
      return mv;
   }

   public static ModelAndView getCloudPage(HttpServletRequest request,
         String cloudName)
   {
      
      logger.debug("Request servlet path " + request.getServletPath());
      logger.debug("Paths " + request.getPathInfo()  + "-" + request.getRequestURI() + "-" + request.getPathTranslated() );
      ModelAndView mv = new ModelAndView("cloudPage");
      
      String cspHomeURL =   request.getContextPath();
      logger.debug("getCloudPage :: cspHomeURL " + cspHomeURL);
      mv.addObject("logoutURL", cspHomeURL + "/logout");
      mv.addObject("cloudName", cloudName);
      
      try
      {
         mv.addObject("rnInvitePage",  RegistrationManager.getCspInviteURL() + "?cloudName" +  URLEncoder.encode(cloudName,"UTF-8"));
      } catch (UnsupportedEncodingException e)
      {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }
      return mv;
   }

   @RequestMapping(value = "/makePayment", method = RequestMethod.POST)
   public ModelAndView makePayment(
         @Valid @ModelAttribute("paymentInfo") PaymentForm paymentFormIn,
         HttpServletRequest request, Model model, BindingResult result)
   {
      logger.info("processing makePayment");

      logger.debug("payment form binding result " + result.toString());

      boolean errors = false;
      String errorText = "";

      ModelAndView mv = null;
      PaymentForm paymentForm = new PaymentForm(paymentFormIn);

      String cloudName = regSession.getCloudName();
      String sessionIdentifier = regSession.getSessionId();
      String email = regSession.getVerifiedEmail();
      String phone = regSession.getVerifiedMobilePhone();
      String password = regSession.getPassword();
      logger.debug(sessionIdentifier + "--" + cloudName + "--" + email + "--"
            + phone + "--" + password);

      // Check Session
      if (sessionIdentifier == null || cloudName == null || password == null)
      {
         errors = true;
         mv = new ModelAndView("signup");
         mv.addObject("error", "Invalid Session");
         logger.debug("Invalid Session ...");
         return mv;
      }

      String paymentType = request.getParameter("paymentType");
      
      
      if (paymentType != null && paymentType.contains("giftCard") && request.getParameter("giftCodes") == null)
      {
         mv = new ModelAndView("payment");
         errors = true;
         mv.addObject(
               "error",
               "Payment with gift card is checked. However, no gift card has been provided. Please provide one.");
         logger.debug("Invalid choice for gift card ...");
         return mv;

      } 

      String txnType = paymentForm.getTxnType();

      logger.debug("Number of clouds being purchased "
            + paymentForm.getNumberOfClouds());
      logger.debug("Transaction type " + txnType);
      DAOFactory dao = DAOFactory.getInstance();
      String giftCodesVal = request.getParameter("giftCodes");
      logger.debug("Giftcodes " + giftCodesVal);
      
      String forwardingPage = request.getContextPath();
      String statusText = "";
      
      //check if its a promo
      //check for valid promo codes in promo_code table
      if(giftCodesVal != null && giftCodesVal.equalsIgnoreCase("NSR-IIS-MAY") && txnType.equals(PaymentForm.TXN_TYPE_SIGNUP))
      {
         if (this.registerCloudName(cloudName, phone, email, password))
         {
            forwardingPage += "/cloudPage";
            statusText = "Congratulations " + cloudName + "! You have successfully purchased a cloudname.";
            
            //make an entry in promo_cloud table
         } else
         {
            forwardingPage += "/signup";
            statusText = "Sorry! The system encountered an error while registering your cloudname.";           
         }
         
         mv = new ModelAndView("AutoSubmitForm");
         
         mv.addObject("URL", request.getContextPath() + "/transactionSuccessFailure");
         mv.addObject("cloudName", cloudName);
         
         mv.addObject("statusText", statusText);
         mv.addObject("nextHop", forwardingPage);
         
         return mv;
      }

      // process gift card payments first

      boolean validGiftCard = false;

      
      String[] giftCodes = null;
      if (giftCodesVal != null && !giftCodesVal.isEmpty())
      {
         giftCodes = giftCodesVal.split(" ");
         
         regSession.setGiftCode(giftCodesVal);
         
         if (giftCodes != null
               && paymentForm.getNumberOfClouds() < giftCodes.length)
         {
            errors = true;
            errorText = "Number of clouds being purchased is less than the number of gift codes provided";

         } else
         {
            int i = 0;

            for (String giftCode : giftCodes)
            {
               logger.debug("Processing giftcode " + giftCode);
               try
               {
                  GiftCodeModel giftCodeObj = dao.getGiftCodeDAO()
                        .get(giftCode);
                  if (giftCodeObj != null)
                  {
                     GiftCodeRedemptionModel gcrObj = dao
                           .getGiftCodeRedemptionDAO().get(giftCode);
                     if (gcrObj != null)
                     {
                        errors = true;
                        errorText += "This giftcode , id "
                              + giftCode
                              + " has already been redeemed. Please remove it from the list. \n";

                     }

                  } else
                  {
                     errors = true;
                     errorText += "The giftcode , id " + giftCode
                           + " is not valid. Please check the giftcode id.\n";
                  }
               } catch (DAOException e2)
               {
                  // TODO Auto-generated catch block
                  e2.printStackTrace();
               }

               i++;
            }
         }
      }

      if (errors)
      {
         mv = new ModelAndView("payment");
         mv.addObject("error", errorText);
         return mv;
      }

      if (!errors && giftCodes != null && giftCodes.length > 0)
      {
         String giftcode = giftCodes[0];
         validGiftCard = true;

         // need a new unique response id
         String responseId = UUID.randomUUID().toString();
         // make entries in the giftcode_redemption
         // table that a new cloud has been registered against a gift code
         if (giftcode != null)
         {
            if (txnType.equals(PaymentForm.TXN_TYPE_SIGNUP))
            {
               if (this.registerCloudName(cloudName, phone, email, password))
               {
                  logger.debug("Going to create the personal cloud now for gift code path...");
                  /*
                  mv = getCloudPage(request, cloudName);
                  AccountDetailsForm accountForm = new AccountDetailsForm();
                  accountForm.setCloudName(cloudName);
                  mv.addObject("accountInfo", accountForm);
                  */
                  forwardingPage += "/cloudPage";
                  statusText = "Congratulations " + cloudName + "! You have successfully purchased a cloudname.";
                  
                  // make a new record in the giftcode_redemption table
                  GiftCodeRedemptionModel giftCodeRedemption = new GiftCodeRedemptionModel();
                  giftCodeRedemption.setCloudNameCreated(cloudName);
                  giftCodeRedemption.setGiftCodeId(regSession.getGiftCode());
                  giftCodeRedemption.setRedemptionId(responseId);
                  giftCodeRedemption.setTimeCreated(new Date());
                  try
                  {
                     dao.getGiftCodeRedemptionDAO().insert(giftCodeRedemption);

                  } catch (DAOException e)
                  {
                     logger.debug("Giftcode redemption entry failed - " + cloudName + ", giftcode=" + regSession.getGiftCode());
                     logger.debug("DB error " + e.getMessage());
                  }
               }
               else
               {
                  forwardingPage += "/signup";
                  statusText = "Sorry! The system encountered an error while registering your cloudname.";
                  
               }
            } 
            else if (txnType.equals(PaymentForm.TXN_TYPE_DEP))
            {
               if ((mv = createDependentClouds(cloudName,null,giftCodes,request)) != null )
               {
                  forwardingPage += "/cloudPage";
                  statusText = "Congratulations " + cloudName + "! You have successfully purchased dependent clouds.";
               } else
               {
                  forwardingPage += "/cloudPage";
                  statusText = "Sorry! The system encountered an error while registering dependent clouds.";
                  
               }
            }
            mv = new ModelAndView("AutoSubmitForm");
            
            mv.addObject("URL", request.getContextPath() + "/transactionSuccessFailure");
            mv.addObject("cloudName", cloudName);
            
            mv.addObject("statusText", statusText);
            mv.addObject("nextHop", forwardingPage);

         }
         

      }

      if (validGiftCard && txnType.equals(PaymentForm.TXN_TYPE_SIGNUP))
      {
         return mv;
      }

      if (giftCodes != null
            && (paymentForm.getNumberOfClouds() == giftCodes.length))
      {
         return mv;
      }

      // reduce the purchase quantity by the number of valid gift codes that
      // have been processed above
      if (validGiftCard)
      {
         paymentForm.setNumberOfClouds(paymentForm.getNumberOfClouds()
               - giftCodes.length);
      }
      if (paymentType != null && paymentType.contains("creditCard") && paymentForm.getNumberOfClouds() > 0)
      {
         mv = new ModelAndView("creditCardPayment");
         String cspHomeURL = request.getContextPath();
         CSPModel cspModel = null;

         try
         {
            cspModel = DAOFactory.getInstance().getCSPDAO()
                  .get(this.getCspCloudName());
         } catch (DAOException e)
         {
            // TODO Auto-generated catch block
            e.printStackTrace();
         }

         BigDecimal amount = cspModel.getCostPerCloudName().multiply(
               new BigDecimal(paymentForm.getNumberOfClouds()));

         String desc = "Personal cloud  " + regSession.getCloudName();
         
         if (cspModel.getPaymentGatewayName().equals("STRIPE"))
         {           
            logger.debug("Payment gateway is STRIPE");
            mv.addObject("StripeJavaScript",
                  StripePaymentProcessor.getJavaScript(cspModel, amount, desc));
            mv.addObject("postURL",
                  cspHomeURL + "/ccpayment");
            mv.addObject("amount",amount.toPlainString());
         } else if (cspModel.getPaymentGatewayName().equals("SAGEPAY"))
         {
            
            mv.addObject("postURL",
                  cspHomeURL +"/submitCustomerDetail");
            mv.addObject("SagePay","SAGEPAY");
            mv.addObject("amount",amount.toPlainString());
            return mv;
         } else if (cspModel.getPaymentGatewayName().equals("BRAINTREE"))
         {
            logger.debug("Payment gateway is BRAINTREE");
            mv.addObject("BrainTree" , BrainTreePaymentProcessor.getJavaScript(cspModel));
            mv.addObject("postURL",
                  cspHomeURL + "/ccpayment");
            
         }
         mv.addObject("cspModel", cspModel);
         mv.addObject("paymentInfo", paymentForm);
         mv.addObject("amount",amount.toPlainString());
         return mv;
      } 
      

      return mv;
   }

   private ModelAndView createDependentClouds(String cloudName , PaymentModel payment , String [] giftCodes , HttpServletRequest request)
   {
      ModelAndView mv = null;
      boolean errors = false;
      DAOFactory dao = DAOFactory.getInstance();
      
      DependentForm dependentForm = regSession.getDependentForm();
      String[] arrDependentCloudName = dependentForm.getDependentCloudName().split(",");
      String[] arrDependentCloudPasswords = dependentForm
            .getDependentCloudPassword().split(",");
      String[] arrDependentCloudBirthDates = dependentForm
            .getDependentBirthDate().split(",");
      

      int cloudsPurchasedWithGiftCodes = 0;
      if(payment != null) //payment via CC
      {
         String giftCodeStr = regSession.getGiftCode();
         if(giftCodeStr != null && !giftCodeStr.isEmpty())
         {
            cloudsPurchasedWithGiftCodes = giftCodeStr.split(" ").length;
         }
      }
      // register the dependent cloudnames

      int i = 0;
      for(String dependentCloudName : arrDependentCloudName)
      {
         if (giftCodes != null && i == giftCodes.length)
         {
            break;
         }
         if (i < cloudsPurchasedWithGiftCodes)
         {
            i++;
            continue;
         }
         if (i >= arrDependentCloudName.length)
         {
            break;
         }
         logger.debug("Creating dependent cloud for " + dependentCloudName);
         CloudNumber dependentCloudNumber = registrationManager.registerDependent(
               CloudName.create(cloudName), regSession.getPassword(),
               CloudName.create(dependentCloudName),
               arrDependentCloudPasswords[i],
               arrDependentCloudBirthDates[i]);
         if (dependentCloudNumber != null)
         {
            logger.info("Dependent Cloud Number "
                  + dependentCloudNumber.toString());
            if(payment != null) 
            {
               PersonalCloudDependentController.saveDependent(dependentCloudName, payment, cloudName,null);
            } else
            {
               PersonalCloudDependentController.saveDependent(dependentCloudName, null, cloudName,giftCodes[i]);
            }
         } else
         {
            logger.error("Dependent Cloud Could not be registered");
            errors = true;
         }
         if(payment == null && giftCodes != null && giftCodes.length > 0)
         {
            String responseId = UUID.randomUUID().toString();
            // make a new record in the giftcode_redemption table
            GiftCodeRedemptionModel giftCodeRedemption = new GiftCodeRedemptionModel();
            giftCodeRedemption.setCloudNameCreated(dependentCloudName);
            giftCodeRedemption.setGiftCodeId(giftCodes[i]);
            giftCodeRedemption.setRedemptionId(responseId);
            giftCodeRedemption.setTimeCreated(new Date());
            try
            {
               dao.getGiftCodeRedemptionDAO().insert(giftCodeRedemption);

            } catch (DAOException e)
            {
               logger.error("Error in updating giftcode redemption information " + e.getMessage());
               errors = true;
            }
         }
         if (errors)
         {
            return null;
         }
         i++;
      }
      if(i < arrDependentCloudName.length)
      {
         mv = new ModelAndView("creditCardPayment");
         CSPModel cspModel = null;

         try
         {
            cspModel = DAOFactory.getInstance().getCSPDAO()
                  .get(this.getCspCloudName());
         } catch (DAOException e)
         {
            // TODO Auto-generated catch block
            e.printStackTrace();
         }

         PaymentForm paymentForm = new PaymentForm();
         paymentForm.setTxnType(PaymentForm.TXN_TYPE_DEP);
         paymentForm.setNumberOfClouds(arrDependentCloudName.length - i);
         mv.addObject("paymentInfo", paymentForm);
         
         BigDecimal amount = cspModel.getCostPerCloudName().multiply(
               new BigDecimal(arrDependentCloudName.length - i));

         String desc = "Personal cloud  " + regSession.getCloudName();
         mv.addObject("cspModel", cspModel);
         if (cspModel.getPaymentGatewayName().equals("STRIPE"))
         {
            logger.debug("Payment gateway is STRIPE");
            mv.addObject("StripeJavaScript",
                  StripePaymentProcessor.getJavaScript(cspModel, amount, desc));
         } else if (cspModel.getPaymentGatewayName().equals("SAGEPAY"))
         {
            
            mv.addObject("postURL",
                  request.getContextPath() +"/submitCustomerDetail");
            mv.addObject("SagePay","SAGEPAY");
            mv.addObject("amount",amount.toPlainString());        
         } else if (cspModel.getPaymentGatewayName().equals("BRAINTREE"))
         {
            logger.debug("Payment gateway is BRAINTREE");
            mv.addObject("BrainTree" , BrainTreePaymentProcessor.getJavaScript(cspModel));
            mv.addObject("postURL",
                  request.getContextPath() + "/ccpayment");
            mv.addObject("amount",amount.toPlainString());
         }
         mv.addObject("paymentInfo", paymentForm);
         return mv;
        
      }
      regSession.setDependentForm(null);
      mv = new ModelAndView("dependentDone");
      
      return mv;
   }

   private boolean registerCloudName(String cloudName, String phone,
         String email, String password)
   {
      try
      {
         registrationManager.registerUser(CloudName.create(cloudName), phone,
               email, password);

         logger.debug("Sucessfully Registered {}", cloudName);
         return true;
      } catch (Xdi2ClientException e1)
      {

         logger.debug("Xdi2ClientException in registering cloud "
               + e1.getMessage());
      } catch (CSPRegistrationException e1)
      {

         logger.debug("CSPRegistrationException in registering cloud "
               + e1.getMessage());
      }
      return false;

   }
   
   private ModelAndView createGiftCards(HttpServletRequest request , String cloudName , PaymentModel payment , CSPModel cspModel)
   {
      ModelAndView mv = getCloudPage(request, cloudName);
      boolean errors = false;
      String errorText = "";
      
      InviteForm   inviteForm = regSession.getInviteForm();
      if(inviteForm == null)
      {
         logger.debug("createGiftCards :: inviteForm is null!");
      }
      InviteModel inviteModel = null;
      try
      {
         inviteModel = DAOFactory.getInstance().getInviteDAO().get(inviteForm.getInviteId());
         if( inviteModel != null )
         {
             logger.error("InviteModel already exist - " + inviteModel);
             errors = true;
             errorText = "Invite id has already been used before !";
         }
         
         List<GiftCodeModel> giftCardList = new ArrayList<GiftCodeModel>();

         InviteModel invite = PersonalCloudInviteController.saveInvite(inviteForm, payment, giftCardList, request.getLocale(),cspModel.getCspCloudName(),cloudName);
         mv = new ModelAndView("inviteDone");
         mv.addObject("cspModel"    , cspModel);
         mv.addObject("inviteModel" , invite);
         mv.addObject("giftCardList", giftCardList);
         
      } catch (DAOException e)
      {
         logger.debug(e.getMessage());
         errors = true;
         errorText = "System error";
      }
      regSession.setInviteForm(null);

      if(errors)
      {
         mv.addObject("error", errorText);
      }
      return mv;
   }
   
   @RequestMapping(value = "/submitCustomerDetail", method = RequestMethod.POST)
   public ModelAndView processBillingDetail(
         
         HttpServletRequest request, HttpServletResponse response)
   {
      CSPModel cspModel = null;

      try
      {
         cspModel = DAOFactory.getInstance().getCSPDAO()
               .get(this.getCspCloudName());
      } catch (DAOException e)
      {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }
      ModelAndView mv =  new ModelAndView("submitCustomerDetail");
      mv.addObject("postURL",
            cspModel.getPaymentUrlTemplate());
      mv.addObject("SagePay","SAGEPAY");
      mv.addObject("vendor",cspModel.getUsername());
      mv.addObject("crypt",SagePayPaymentProcessor.getSagePayCrypt(request, new BigDecimal(request.getParameter("amount")),cspModel.getCurrency(),cspModel.getPassword()));
      return mv;
   }
   
   @RequestMapping(value = "/rnInvite", method = RequestMethod.GET)
   public ModelAndView goToRNInvitePage(HttpServletRequest request, Model model)
   {
      logger.info("Going to RN Invite page");

      //TBD
      ModelAndView mv = null;
      mv = new ModelAndView("AutoSubmitForm");
      mv.addObject("URL", "www.respectnetwork.net");
      mv.addObject("cloudName", regSession.getCloudName());
      
      return mv;
   }
   
   @RequestMapping(value = "/transactionSuccessFailure", method = RequestMethod.POST)
   public ModelAndView showTransactionSuccessFailureForm(HttpServletRequest request, Model model)
   {
      logger.info("showing transactionSuccessFailure form " + request.getParameter("nextHop") + "::" + request.getParameter("cloudname"));

      ModelAndView mv = null;
      String formPostURL = request.getParameter("nextHop");

      mv = new ModelAndView("postTxn");
      mv.addObject("postURL", formPostURL);
      mv.addObject("cloudName", request.getParameter("cloudname"));
      mv.addObject("statusText", request.getParameter("statusText"));
      return mv;
   }
}
