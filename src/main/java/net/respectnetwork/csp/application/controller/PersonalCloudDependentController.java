package net.respectnetwork.csp.application.controller;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import net.respectnetwork.csp.application.csp.CurrencyCost;
import net.respectnetwork.csp.application.dao.DAOException;
import net.respectnetwork.csp.application.dao.DAOFactory;
import net.respectnetwork.csp.application.exception.UserRegistrationException;
import net.respectnetwork.csp.application.form.DependentForm;
import net.respectnetwork.csp.application.form.PaymentForm;
import net.respectnetwork.csp.application.manager.RegistrationManager;
import net.respectnetwork.csp.application.manager.StripePaymentProcessor;
import net.respectnetwork.csp.application.model.CSPCostOverrideModel;
import net.respectnetwork.csp.application.model.CSPModel;
import net.respectnetwork.csp.application.model.DependentCloudModel;
import net.respectnetwork.csp.application.model.PaymentModel;
import net.respectnetwork.csp.application.session.RegistrationSession;
import net.respectnetwork.sdk.csp.exception.CSPRegistrationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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

@Controller
public class PersonalCloudDependentController
{
   private static final Logger logger = LoggerFactory
                                            .getLogger(PersonalCloudDependentController.class);
   private String              cspCloudName;
   private RegistrationSession regSession;

   /**
    * Registration Service : to register dependent clouds
    */
   private RegistrationManager theManager;

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

   public RegistrationSession getRegSession()
   {
      return regSession;
   }

   @Autowired
   public void setRegSession(RegistrationSession regSession)
   {
      this.regSession = regSession;
   }

   private String getCloudName()
   {
      String rtn = regSession.getCloudName();
      return rtn;
   }

   private DependentForm getDependentForm()
   {
      DependentForm rtn = regSession.getDependentForm();

      return rtn;
   }

   private void setDependentForm(DependentForm dependentForm)
   {
      regSession.setDependentForm(dependentForm);
   }

   @RequestMapping(value = "/dependent", method = RequestMethod.GET)
   public ModelAndView showDependentForm(HttpServletRequest request, Model model)
         throws DAOException
   {
      Enumeration<String> paramNames = request.getParameterNames(); 
      while(paramNames.hasMoreElements())
      {
         String paramName = paramNames.nextElement();
         logger.debug("p name " + paramName);
         String[] paramValues = request.getParameterValues(paramName);
         for(int i = 0 ; i < paramValues.length ; i++)
         {
            logger.debug("p value " + paramValues[i]);
         }
      }
      String rnQueryString = "" ;
      if (request.getQueryString() != null && !request.getQueryString().isEmpty())
      {
         rnQueryString = rnQueryString.substring(rnQueryString.indexOf("&")+1);
         logger.debug("Query String " + rnQueryString);
      }
      
      
      String cloudName = request.getParameter(RegistrationController.URL_PARAM_NAME_REQ_CLOUDNAME); //this.getCloudName();

      logger.info("showing dependent page - " + cloudName);

      String cspHomeURL = request.getContextPath();
      ModelAndView mv = null;
      DependentForm dependentForm = null;
      CSPModel cspModel = null;

      if (cloudName == null || cloudName.isEmpty() || regSession== null || !cloudName.equalsIgnoreCase(regSession.getCloudName()))
      {
         mv = new ModelAndView("login");
         mv.addObject("postURL", cspHomeURL + "/cloudPage");
         return mv;
      }

      cspModel = DAOFactory.getInstance().getCSPDAO()
            .get(this.getCspCloudName());

      // Get cost override info
      CurrencyCost costPerCloudName = RegistrationController.getCostIncludingOverride(cspModel, regSession.getVerifiedMobilePhone(), 1);
      regSession.setCurrency(costPerCloudName.getCurrencyCode());
      regSession.setCostPerCloudName(costPerCloudName.getAmount());

      mv = new ModelAndView("dependent");
      mv.addObject("cspModel", cspModel);
      dependentForm = new DependentForm();
      model.addAttribute("dependentForm", dependentForm);
      model.addAttribute("cloudName", cloudName);
      if(regSession != null)
      {
         regSession.setTransactionType(PaymentForm.TXN_TYPE_DEP);
         regSession.setCloudName(cloudName);
         regSession.setRnQueryString(rnQueryString);
      }

      return mv;
   }

   @RequestMapping(value = "/dependentDone", method = {RequestMethod.GET,RequestMethod.POST} )
   public ModelAndView showDependentDoneForm(Model model,
         HttpServletRequest request) throws DAOException
   {
      ModelAndView mv = PersonalCloudController.getCloudPage(request,
            this.regSession.getCloudName());
      return mv;
   }

   @RequestMapping(value = "/dependentSubmit", method = RequestMethod.POST)
   public ModelAndView showDependentSubmitForm(
         @Valid @ModelAttribute("dependentForm") DependentForm dependentForm,
         BindingResult result, Model model, HttpServletRequest request)
         throws DAOException
   {
      String cloudName = this.getCloudName();
      boolean errors = false;

      logger.info("showing dependent submit page - " + cloudName + " : "
            + dependentForm);

      String cspHomeURL = request.getContextPath();
      ModelAndView mv = null;
      CSPModel cspModel = null;

      if (cloudName == null)
      {
         mv = new ModelAndView("login");
         mv.addObject("postURL", cspHomeURL + "/cloudPage");
         return mv;
      }

      if (result.hasErrors())
      {
         logger.error("result - " + result);
         mv = PersonalCloudController.getCloudPage(request, cloudName);
         return mv;
      }

      cspModel = DAOFactory.getInstance().getCSPDAO()
            .get(this.getCspCloudName());

      logger.debug("All dependents ..." + dependentForm.getDependentCloudName() );
      String[] arrDependentCloudName = dependentForm.getDependentCloudName()
            .split(",");
      String[] arrPasswd = dependentForm.getDependentCloudPassword()
            .split(",");

      String errorStr = "";

      int i = 0 ;
      for (String dependentCloudName : arrDependentCloudName)
      {
         try
         {
            logger.info("Checking for dependent cloudname :"
                  + dependentCloudName);
            if (!theManager.isCloudNameAvailable(dependentCloudName))
            {
               errorStr += "\nCloudName not Available " + dependentCloudName;
               errors = true;
               break;
            }
            if(!RegistrationManager.validatePassword(arrPasswd[i]))
            {
               errorStr += "\nInvalid password. Please provide a password that is at least 8 characters, have at least 2 letters, 2 numbers and at least one special character, e.g. @, #, $ etc.";;
               errors = true;
               break;
            }
         } catch (UserRegistrationException e)
         {
            errorStr = "System Error checking CloudName";
            logger.warn(errorStr + " : {}", e.getMessage());
            errors = true;
         }
         i++;
      }
      if (errors)
      {
         mv = new ModelAndView("dependent");
         mv.addObject("cspModel", cspModel);
         //dependentForm = new DependentForm();
         model.addAttribute("dependentForm", dependentForm);
         model.addAttribute("cloudName", cloudName);
         mv.addObject("cspModel", cspModel);
         mv.addObject("error", errorStr);
         errors = true;
         return mv;

      }
      cspModel = DAOFactory.getInstance().getCSPDAO().get(this.getCspCloudName());
      BigDecimal quantity = BigDecimal.valueOf((long) arrDependentCloudName.length);
      
      

      // Cost (overriden if applicable) is stored in regSession
      String currency = regSession.getCurrency();
      BigDecimal totalCost = regSession.getCostPerCloudName().multiply(quantity);

      mv = new ModelAndView("payment");
      PaymentForm paymentForm = new PaymentForm();
      paymentForm.setTxnType(PaymentForm.TXN_TYPE_DEP);
      if(regSession != null)
      {
         regSession.setTransactionType(PaymentForm.TXN_TYPE_DEP);
      }
      paymentForm.setNumberOfClouds(arrDependentCloudName.length);
      if(cspModel.getPaymentGatewayName().equals("GIFT_CODE_ONLY"))
      {
         paymentForm.setGiftCodesOnly(true);
      }
      mv.addObject("paymentInfo", paymentForm);
      mv.addObject("totalAmountText", RegistrationController.formatCurrencyAmount(regSession.getCurrency(), totalCost));
      mv.addObject("cspTCURL", this.getTheManager().getCspTCURL());
      
      this.setDependentForm(dependentForm);
      return mv;
   }

   public static DependentCloudModel saveDependent(String dependentCloudName,
         PaymentModel payment, String cloudName , String giftCode)
   {
      DependentCloudModel dependentCloud = null;
      DAOFactory dao = DAOFactory.getInstance();

      dependentCloud = new DependentCloudModel();
      dependentCloud.setDependentCloudName(dependentCloudName);
      dependentCloud.setGuardianCloudName(cloudName);
      if (payment != null)
      {
         dependentCloud.setPaymentId(payment.getPaymentId());
      } else
      {
         dependentCloud.setPaymentId(giftCode);
      }
      dependentCloud.setTimeCreated(new Date());
      try
      {
         dao.getDependentCloudDAO().insert(dependentCloud);
      } catch (DAOException ex)
      {
         logger.debug("Error while saving dependent record in DB "
               + ex.getMessage());
         return null;
      }

      return dependentCloud;
   }

   private String getPaymentDescription(DependentForm dependentForm,
         HttpServletRequest request)
   {
      String rtn = "Personal Cloud For "
            + dependentForm.getDependentCloudName();

      return rtn;
   }

   public RegistrationManager getTheManager()
   {
      return theManager;
   }

   @Autowired
   public void setTheManager(RegistrationManager theManager)
   {
      this.theManager = theManager;
   }
}
