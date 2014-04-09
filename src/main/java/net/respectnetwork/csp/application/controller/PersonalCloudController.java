package net.respectnetwork.csp.application.controller;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.ServletConfig;
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
import net.respectnetwork.csp.application.manager.PersonalCloudManager;
import net.respectnetwork.csp.application.manager.RegistrationManager;
import net.respectnetwork.csp.application.manager.StripePaymentProcessor;

import net.respectnetwork.csp.application.model.CSPModel;
import net.respectnetwork.csp.application.model.DependentCloudModel;
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
   private RegistrationManager  registrationManager;

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
                        + request.getParameter("cloudname"));
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
                     + request.getParameter("cloudname"));
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

   @RequestMapping(value = "/ccpayment", method = RequestMethod.POST)
   public ModelAndView processCCPayment(
         @Valid @ModelAttribute("paymentInfo") PaymentForm paymentForm,
         HttpServletRequest request, Model model, BindingResult result)
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

      if (!errors)
      {
         BigDecimal amount = cspModel.getCostPerCloudName();
         String desc = "A personal cloud for " + cloudName;

         String token = StripePaymentProcessor.getToken(request);

         if (token != null && !token.isEmpty())
         { // this is a CC charge request
            PaymentModel payment = StripePaymentProcessor.makePayment(cspModel,
                  amount, desc, token);
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

               

               if (txnType.equals(PaymentForm.TXN_TYPE_SIGNUP))
               {
                  if (this.registerCloudName(cloudName, phone, email, password))
                  {
                     logger.debug("Going to create the personal cloud now for CC path ...");
                     mv = getCloudPage(request, cloudName);
                     AccountDetailsForm accountForm = new AccountDetailsForm();
                     accountForm.setCloudName(cloudName);
                     mv.addObject("accountInfo", accountForm);

                  } else
                  {
                     errors = true;
                     errorText ="Could not register cloudname";
                  }

               }
               if (txnType.equals(PaymentForm.TXN_TYPE_DEP))
               {
                  if((mv = createDependentClouds(cloudName,payment,null)) != null)
                  {
                     return mv;
                  }
                  else
                  {
                     errors = true;
                     errorText ="Could not register dependent cloud";
                  }
               }
               if(txnType.equals(PaymentForm.TXN_TYPE_BUY_GC))
               {
                  if((mv = this.createGiftCards(request, cloudName, payment, cspModel)) != null)
                  {
                     return mv;
                  }
                  else
                  {
                     errors = true;
                     errorText ="Errors in gift card processing";
                  }
               }
            }

         }
      } 
      if(errors)
      {
         if(txnType.equals(PaymentForm.TXN_TYPE_SIGNUP))
         {
            mv = new ModelAndView("signup");
         }
         else if(cloudName == null || password == null || sessionIdentifier == null)
         {
            mv = new ModelAndView("login");
         } else
         {
            mv = getCloudPage(request, cloudName);
         }
         mv.addObject("error", errorText);
      }

      return mv;
   }

   public static ModelAndView getCloudPage(HttpServletRequest request,
         String cloudName)
   {
      ModelAndView mv = new ModelAndView("cloudPage");
      String cspHomeURL = request.getContextPath();

      mv.addObject("logoutURL", cspHomeURL + "/logout");
      mv.addObject("cloudName", cloudName);

      try
      {
         DAOFactory dao = DAOFactory.getInstance();

         List<InviteModel> invList = dao.getInviteDAO().listGroupByInvited(
               cloudName);
         List<DependentCloudModel> depList = dao.getDependentCloudDAO().list(
               cloudName);

         mv.addObject("inviteList", invList);
         mv.addObject("dependentList", depList);
      } catch (DAOException e)
      {
         logger.error("Failed to perform DAO opertations", e);
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

      String paymentTypeGC = request.getParameter("paymentTypeGC");
      String paymentTypeCC = request.getParameter("paymentTypeCC");
      if (paymentTypeGC != null)
      {

         logger.debug("Payment with GC");
      }
      if (paymentTypeCC != null)
      {

         logger.debug("Payment with CC");
      } else
      {

      }

      if (paymentTypeGC != null && request.getParameter("giftCodes") == null)
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

      // process gift card payments first

      boolean validGiftCard = false;

      DAOFactory dao = DAOFactory.getInstance();
      String giftCodesVal = request.getParameter("giftCodes");
      logger.debug("Giftcodes " + giftCodesVal);
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
                  mv = getCloudPage(request, cloudName);
                  AccountDetailsForm accountForm = new AccountDetailsForm();
                  accountForm.setCloudName(cloudName);
                  mv.addObject("accountInfo", accountForm);

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
                     // TODO Auto-generated catch block
                     e.printStackTrace();
                  }
               }
            }
            if (txnType.equals(PaymentForm.TXN_TYPE_DEP))
            {
               return createDependentClouds(cloudName,null,giftCodes);
            }

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
      if (paymentTypeCC != null && paymentForm.getNumberOfClouds() > 0)
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
         } else if (cspModel.getPaymentGatewayName().equals("SAGEPAY"))
         {
            
            mv.addObject("postURL",
                  cspHomeURL +"/submitCustomerDetail");
            mv.addObject("SagePay","SAGEPAY");
            mv.addObject("amount",amount.toPlainString());
            return mv;
         }
         mv.addObject("cspModel", cspModel);
         mv.addObject("paymentInfo", paymentForm);
         return mv;
      }

      return mv;
   }

   private ModelAndView createDependentClouds(String cloudName , PaymentModel payment , String [] giftCodes)
   {
      ModelAndView mv = null;
      boolean errors = false;
      DAOFactory dao = DAOFactory.getInstance();
      
      DependentForm dependentForm = regSession.getDependentForm();

      //

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
            CSPModel cspModel = null;
           
            try
            {
               cspModel = dao.getCSPDAO().get(this.getCspCloudName());
            } catch (DAOException e1)
            {
               logger.debug("Cannot connect to DB to lookup info...");
            }
            mv = new ModelAndView("dependent");
            mv.addObject("error", "Failed to register dependent cloud. "
                  + registrationManager.getCSPContactInfo());     
            mv.addObject("dependentForm", dependentForm);
            mv.addObject("cspModel", cspModel);
            return mv;
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
            // TBD
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
      
      regSession.setInviteForm(null);

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
      

      if(errors)
      {
         mv.addObject("error", errorText);
      }
      return mv;
   }
   private String getSagePayCrypt(HttpServletRequest request, BigDecimal amount,String currency, String key)
   {
      IFormApi api = ApiFactory.getFormApi();
      IFormPayment msg =  ApiFactory.getFormApi().newFormPaymentRequest();
   
      String cspHomeURL = request.getContextPath();
      
      
      msg.setSuccessUrl(request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + cspHomeURL + "/sagePayCallback/"); 
      msg.setFailureUrl(request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + cspHomeURL + "/sagePayCallback/"); 
      msg.setVendorTxCode(UUID.randomUUID().toString());
      msg.setDescription("Purchase Personal Cloud(s)");
      msg.setCustomerName(request.getParameter("BillingFirstNames") + " " + request.getParameter("BillingSurname"));
      msg.setBillingFirstnames(request.getParameter("BillingFirstNames"));
      msg.setBillingSurname(request.getParameter("BillingSurname"));
      msg.setBillingAddress1(request.getParameter("BillingAddress1"));
      msg.setBillingCity(request.getParameter("BillingCity"));
      msg.setBillingCountry(request.getParameter("BillingCountry"));
      msg.setBillingPostCode(request.getParameter("BillingPostCode"));
      
      msg.setDeliveryFirstnames(request.getParameter("BillingFirstNames"));
      msg.setDeliverySurname(request.getParameter("BillingSurname"));
      msg.setDeliveryAddress1(request.getParameter("BillingAddress1"));
      msg.setDeliveryCity(request.getParameter("BillingCity"));
      msg.setDeliveryCountry(request.getParameter("BillingCountry"));
      msg.setDeliveryPostCode(request.getParameter("BillingPostCode"));

      msg.setCurrency(currency);
      msg.setAmount(amount);
      
      api.encrypt(key, msg);
      
      Map<String,String> map = api.toMap(IFormPayment.class, msg);
      
      String crypt = map.get("Crypt");
      
      logger.debug("Crypt " + crypt);
      
      return crypt;
   }
   @RequestMapping(value = "/sagePayCallback", method = RequestMethod.GET)
   public ModelAndView sagePayCallbackProcessing(
         HttpServletRequest request, Model model , HttpServletResponse response)
   {
      
      String sessionIdentifier = regSession.getSessionId();
      String email = regSession.getVerifiedEmail();
      String phone = regSession.getVerifiedMobilePhone();
      String password = regSession.getPassword();
      String cloudName = regSession.getCloudName();
      logger.debug(sessionIdentifier + "--" + cloudName + "--" + email + "--"
            + phone + "--" + password);
      
      final RequestState rs = new RequestState(request, response, request.getSession().getServletContext());
      
   
      ModelAndView mv = null  ; //
      
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

      String crypt= rs.params.getMandatoryString("crypt");
      IFormApi api = ApiFactory.getFormApi();
      /*
       * API Call 
       * 
       * Decrypt the returned parameter into a result object.
       */
      IFormPaymentResult fps = 
         api.decrypt(cspModel.getPassword(), crypt);
      logger.debug("Form Payment Result: " + fps.toString());
      ResponseStatus status = fps.getStatus();
      if(status.equals(ResponseStatus.OK)) 
      {
         logger.debug("SagePay payment was processed successfully " + status.toString() );
         this.registerCloudName(cloudName, phone, email, password);
         mv = getCloudPage(request, regSession.getCloudName());
         
         
         
      }
      else {
         String reason="";
         // Determine the reason this transaction was unsuccessful
         if (status.equals(ResponseStatus.NOTAUTHED))
            reason = "You payment was declined by the bank.  This could be due to insufficient funds, or incorrect card details.";
         else if (status.equals(ResponseStatus.ABORT))
            reason = "You chose to Cancel your order on the payment pages.";
         else if (status.equals(ResponseStatus.REJECTED)) 
            reason = "Your order did not meet our minimum fraud screening requirements.";
         else if (status.equals(ResponseStatus.INVALID) || status.equals(ResponseStatus.MALFORMED))
            reason = "We could not process your order because we have been unable to register your transaction with our Payment Gateway.";
         else if (status.equals(ResponseStatus.ERROR))
            reason = "We could not process your order because our Payment Gateway service was experiencing difficulties.";
         else
            reason = "The transaction process failed. Please contact us with the date and time of your order and we will investigate.";
         mv = new ModelAndView("signup");
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
      mv.addObject("crypt",getSagePayCrypt(request, new BigDecimal(request.getParameter("amount")),cspModel.getCurrency(),cspModel.getPassword()));
      return mv;
   }
}
