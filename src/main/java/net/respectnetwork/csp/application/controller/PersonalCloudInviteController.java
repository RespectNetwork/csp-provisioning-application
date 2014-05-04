package net.respectnetwork.csp.application.controller;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import net.respectnetwork.csp.application.csp.CurrencyCost;
import net.respectnetwork.csp.application.dao.DAOContextProvider;
import net.respectnetwork.csp.application.dao.DAOException;
import net.respectnetwork.csp.application.dao.DAOFactory;
import net.respectnetwork.csp.application.form.InviteForm;
import net.respectnetwork.csp.application.form.PaymentForm;
import net.respectnetwork.csp.application.invite.GiftEmailSenderThread;
import net.respectnetwork.csp.application.manager.BrainTreePaymentProcessor;
import net.respectnetwork.csp.application.manager.PinNetAuPaymentProcessor;
import net.respectnetwork.csp.application.manager.RegistrationManager;
import net.respectnetwork.csp.application.manager.StripePaymentProcessor;
import net.respectnetwork.csp.application.model.CSPCostOverrideModel;
import net.respectnetwork.csp.application.model.CSPModel;
import net.respectnetwork.csp.application.model.GiftCodeModel;
import net.respectnetwork.csp.application.model.InviteModel;
import net.respectnetwork.csp.application.model.PaymentModel;
import net.respectnetwork.csp.application.session.RegistrationSession;
import net.respectnetwork.sdk.csp.notification.BasicNotificationService;

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

@Controller
public class PersonalCloudInviteController
{
	private static final Logger logger = LoggerFactory.getLogger(PersonalCloudInviteController.class);

	private static final List<Integer> quantityList = new ArrayList<Integer>(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10));

	private static final String RES_DEFAULT_INVITE_TEXT_PAYDESC = "CloudName Gift Cards for {0}";

	private static final String RES_DEFAILT_INVITE_MAIL_SUBJECT = "Gift Code from {0} to join Respect Network";
	private static final String RES_DEFAILT_INVITE_MAIL_HEADER  = "{0} has given you a gift code to join Respect Network <br/><br/>";
	private static final String RES_DEFAILT_INVITE_MAIL_GIFT_0  = "Please click the following URL to sign up:<br/><br/>";
	private static final String RES_DEFAILT_INVITE_MAIL_GIFT_1  = "Please click the following URL to sign up and redeem the gift card given by {0}:<br/><br/>";
	private static final String RES_DEFAILT_INVITE_MAIL_GIFT_2  = "Please click the following URLs to sign up and redeem the gift cards given by {0}:<br/><br/>";
	private static final String RES_DEFAILT_INVITE_MAIL_URL     = "   {0}<br/><br/>";
	private static final String RES_DEFAILT_INVITE_MAIL_FOOTER  = "<br/><br/>Sincerely,<br/><br/>Respect Network<br/><br/>http://respectnetwork.com/<br/><br/>The Personal Cloud Network";

	private String              cspCloudName;
	private String		    cspInviteURL;
	private RegistrationSession regSession;
	private RegistrationManager registrationManager ;
    
	public String getCspCloudName()
	{
		return this.cspCloudName;
	}

	@Autowired
	@Qualifier("cspCloudName")
	public void setCspCloudName( String cspCloudName )
	{
		this.cspCloudName = cspCloudName;
	}

	public String getCspInviteURL()
	{
		return this.cspInviteURL;
	}

	@Autowired
	@Qualifier("cspInviteURL")
	public void setCspInviteURL( String cspInviteURL )
	{
		this.cspInviteURL = cspInviteURL;
	}

	public RegistrationSession getRegSession()
	{
		return this.regSession;
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

	private void setInviteForm( InviteForm inviteForm )
	{
		regSession.setInviteForm(inviteForm);
	}

	private InviteForm getInviteForm()
	{
		InviteForm rtn = regSession.getInviteForm();

		return rtn;
	}

	@RequestMapping(value = "/invite", method = RequestMethod.GET)
	public ModelAndView showInviteForm(HttpServletRequest request, Model model) throws DAOException
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
      String cloudName = request.getParameter(RegistrationController.URL_PARAM_NAME_REQ_CLOUDNAME); // this.getCloudName();

		logger.info("showing invite page - " + cloudName );

		String       cspHomeURL = request.getContextPath();
		ModelAndView mv         = null;
		InviteForm   inviteForm = null;
		CSPModel     cspModel   = null;

		if( cloudName == null || cloudName.isEmpty() || regSession== null || !cloudName.equalsIgnoreCase(regSession.getCloudName()))
		{
			mv = new ModelAndView("login");
			mv.addObject("postURL", cspHomeURL + "/cloudPage");
			return mv;
		}

		cspModel = DAOFactory.getInstance().getCSPDAO().get(this.getCspCloudName());
		logger.debug("showInviteForm :: CSP Cloud Name " + cspModel.getCspCloudName() );

		mv = new ModelAndView("invite");
		mv.addObject("cspModel"    , cspModel);
		mv.addObject("quantityList", quantityList);
		mv.addObject("cloudName", cloudName);

      // Format cost per cloud (include override if applicable)
      CurrencyCost costPerCloudName = RegistrationController.getCostIncludingOverride(cspModel, regSession.getVerifiedMobilePhone(), 1);
      regSession.setCurrency(costPerCloudName.getCurrencyCode());
      regSession.setCostPerCloudName(costPerCloudName.getAmount());

      mv.addObject("costPerCloudNameText", RegistrationController.formatCurrencyAmount(costPerCloudName));

      inviteForm = new InviteForm();
      model.addAttribute("inviteForm", inviteForm);
      if (regSession != null)
      {
         regSession.setTransactionType(PaymentForm.TXN_TYPE_BUY_GC);
         regSession.setCloudName(cloudName);
         regSession.setRnQueryString(rnQueryString);
      }

      return mv;
   }

	@RequestMapping(value = "/inviteReview", method = RequestMethod.POST)
	public ModelAndView showInviteReviewForm( @Valid @ModelAttribute("inviteForm") InviteForm inviteForm, BindingResult result, Model model, HttpServletRequest request ) throws DAOException
	{
		String       cloudName  = this.getCloudName();

		logger.info("showing invite review page - " + cloudName + " : " + inviteForm);

		String       cspHomeURL = request.getContextPath();
		ModelAndView mv         = null;
		CSPModel     cspModel   = null;

		if( cloudName == null)
		{
			mv = new ModelAndView("login");
			mv.addObject("postURL", cspHomeURL + "/cloudPage");
			return mv;
		}

	
      
		if( result.hasErrors() )
		{
			logger.error("result - " + result);
			mv = new ModelAndView("invite");
			String errorStr = "Invalid values for email and/or gift card quantity";
			cspModel = DAOFactory.getInstance().getCSPDAO().get(this.getCspCloudName());
         mv = new ModelAndView("invite");
         mv.addObject("cspModel"    , cspModel);
         mv.addObject("quantityList", quantityList);
         mv.addObject("cloudName", regSession.getCloudName());
         model.addAttribute("inviteForm", inviteForm);
         mv.addObject("error", errorStr);
         
         return mv;
		}
		else
		{
		   // validate email address entered by user
	      if (!org.apache.commons.validator.routines.EmailValidator
	               .getInstance().isValid(inviteForm.getEmailAddress())) 
	      {
	           String errorStr = "Invalid Email Address.";
	           logger.debug("Invalid Email address entered..."
	                   + inviteForm.getEmailAddress());
	           cspModel = DAOFactory.getInstance().getCSPDAO().get(this.getCspCloudName());
	           mv = new ModelAndView("invite");
	           mv.addObject("cspModel"    , cspModel);
	           mv.addObject("quantityList", quantityList);
	           mv.addObject("cloudName", regSession.getCloudName());
	           model.addAttribute("inviteForm", inviteForm);
	           mv.addObject("error", errorStr);
	           
	           return mv;
	      }
		}
		mv = new ModelAndView("inviteReview");
		cspModel = DAOFactory.getInstance().getCSPDAO().get(this.getCspCloudName());
		mv.addObject("cspTCURL", registrationManager.getCspTCURL());
		model.addAttribute("inviteForm", inviteForm);
		mv.addObject("cloudName", regSession.getCloudName());

      // Format total cost (including override if applicable)
      String currency = regSession.getCurrency();
      BigDecimal totalCost = regSession.getCostPerCloudName().multiply(new BigDecimal(inviteForm.getGiftCardQuantity()));
      mv.addObject("totalAmountText", RegistrationController.formatCurrencyAmount(currency, totalCost));

      return mv;
	}

	@RequestMapping(value = "/inviteDone", method = RequestMethod.GET)
	public ModelAndView showInviteDoneForm( Model model, HttpServletRequest request ) throws DAOException
	{
		ModelAndView mv = PersonalCloudController.getCloudPage(request, this.getCloudName());
		return mv;
	}

	@RequestMapping(value = "/inviteSubmit", method = RequestMethod.POST)
	public ModelAndView showInviteSubmitForm( @Valid @ModelAttribute("inviteForm") InviteForm inviteForm, BindingResult result, Model model, HttpServletRequest request ) throws DAOException
	{
		String       cloudName  = this.getCloudName();

		logger.info("showing invite submit page - " + cloudName + " : " + inviteForm);

		String       cspHomeURL = request.getContextPath();
		ModelAndView mv         = null;
		CSPModel     cspModel   = null;

		if( cloudName == null || regSession== null || !cloudName.equalsIgnoreCase(regSession.getCloudName()))
		{
			mv = new ModelAndView("login");
			mv.addObject("postURL", cspHomeURL + "/cloudPage");
			return mv;
		}

		if( result.hasErrors() )
		{
			logger.error("result - " + result);
			mv = new ModelAndView("invite");
			mv.addObject("quantityList", quantityList);

			return mv;
		}
		

		cspModel = DAOFactory.getInstance().getCSPDAO().get(this.getCspCloudName());
		BigDecimal quantity = BigDecimal.valueOf((long) inviteForm.getGiftCardQuantity().intValue());
		String     desc     = this.getPaymentDescription(inviteForm, request.getLocale());

		inviteForm.setInviteId(UUID.randomUUID().toString());

      // Cost including override is stored in regSession
      String currency = regSession.getCurrency();
      BigDecimal totalCost = regSession.getCostPerCloudName().multiply(quantity);

      
		mv = new ModelAndView("creditCardPayment");
		PaymentForm paymentForm = new PaymentForm();
		if(cspModel.getPaymentGatewayName().equals("GIFT_CODE_ONLY"))
      {
         paymentForm.setGiftCodesOnly(true);
      }
      paymentForm.setTxnType(PaymentForm.TXN_TYPE_BUY_GC);
      if(regSession != null)
      {
         regSession.setTransactionType(PaymentForm.TXN_TYPE_BUY_GC);
      }
      paymentForm.setNumberOfClouds(inviteForm.getGiftCardQuantity().intValue());
      mv.addObject("paymentInfo", paymentForm);
      
		if(cspModel.getPaymentGatewayName().equals("STRIPE")) 
		{
		   logger.debug("Payment gateway is STRIPE");
		   mv.addObject("StripeJavaScript"  , StripePaymentProcessor.getJavaScript(cspModel, totalCost, desc));
		   
		   mv.addObject("postURL",
               cspHomeURL + "/ccpayment");
		} 
		else if (cspModel.getPaymentGatewayName().equals("SAGEPAY"))
      {
         
         mv.addObject("postURL", cspHomeURL +"/submitCustomerDetail");
         mv.addObject("SagePay", "SAGEPAY");
                 
      } else if (cspModel.getPaymentGatewayName().equals("BRAINTREE"))
      {
         logger.debug("Payment gateway is BRAINTREE");
         mv.addObject("BrainTree", BrainTreePaymentProcessor.getJavaScript(cspModel));
         mv.addObject("postURL", cspHomeURL + "/ccpayment");
         
      } else if (cspModel.getPaymentGatewayName().equals(
            PinNetAuPaymentProcessor.DB_PAYMENT_GATEWAY_NAME))
      {
         logger.debug("Payment gateway is PIN");
         mv.addObject("PinNetAu",
               PinNetAuPaymentProcessor.DB_PAYMENT_GATEWAY_NAME);
         mv.addObject("publishableKey",
               PinNetAuPaymentProcessor.getPublishableApiKey(cspModel));
         mv.addObject("environment",
               PinNetAuPaymentProcessor.getEnvironment(cspModel));
         mv.addObject("postURL", cspHomeURL + "/ccpayment");
      }
		mv.addObject("amount",totalCost.toPlainString());
		mv.addObject("totalAmountText", RegistrationController.formatCurrencyAmount(regSession.getCurrency(), totalCost));
		this.setInviteForm(inviteForm);

		return mv;
	}


	public static InviteModel saveInvite( InviteForm inviteForm, PaymentModel payment, List<GiftCodeModel> giftCodeList, Locale locale , String cspCloudName , String cloudName, HttpServletRequest request) throws DAOException
	{
		DAOFactory dao = DAOFactory.getInstance();

		InviteModel rtn = new InviteModel();

		if( inviteForm.getInviteId() != null )
		{
			rtn.setInviteId(inviteForm.getInviteId());
		}
		else
		{
			rtn.setInviteId(UUID.randomUUID().toString());
		}
		rtn.setCspCloudName(cspCloudName);
		rtn.setInviterCloudName(cloudName);
		rtn.setInvitedEmailAddress(inviteForm.getEmailAddress());
		rtn.setEmailSubject("Invitation to Join Respect Network");
		rtn.setEmailMessage(inviteForm.getEmailMessage());

//		if( payment != null )
//		{
//			dao.getPaymentDAO().insert(payment);
//		}

		dao.getInviteDAO().insert(rtn);

		if( payment != null )
		{
			int quantity = inviteForm.getGiftCardQuantity().intValue();
			for( int i = 0; i < quantity; i++ )
			{
				GiftCodeModel gift = new GiftCodeModel();
				gift.setGiftCodeId(UUID.randomUUID().toString());
				gift.setInviteId(rtn.getInviteId());
				gift.setPaymentId(payment.getPaymentId());
				dao.getGiftCodeDAO().insert(gift);
				giftCodeList.add(gift);
			}
		}

		sendInviteEmail(rtn, giftCodeList, locale,request);

		return rtn;
	}

	public static void sendInviteEmail( InviteModel invite, List<GiftCodeModel> giftCodeList, Locale locale , HttpServletRequest request )
	{
		logger.info("sendInviteEmail - " + invite + " : " + giftCodeList);

		Object[]      inviter = new Object[] { invite.getInviterCloudName() };
		Object[]      csp = new Object[] {invite.getCspCloudName() };
		String        subject = getMessageFromResource("invite.mail.subject", inviter, RES_DEFAILT_INVITE_MAIL_SUBJECT, locale);
		StringBuilder builder = new StringBuilder();

      builder.append(getMessageFromResource("invite.mail.header" , inviter, RES_DEFAILT_INVITE_MAIL_HEADER , locale));
      builder.append(invite.getEmailMessage().trim());
      builder.append("<br/>");
      builder.append("<br/>");
      // wmartin 4/24 get msutton's wording in. bloat.
      
      builder.append(getMessageFromResource("invite.mail.giftText.0", csp, null, locale));
      builder.append("<br/><br/>");
      builder.append(getMessageFromResource("invite.mail.giftText.1", csp, null, locale));
      builder.append("<br/><br>");
      if( (giftCodeList == null) || (giftCodeList.size() == 0) )
      {
         builder.append(getMessageFromResource("invite.mail.gift.0", inviter, RES_DEFAILT_INVITE_MAIL_GIFT_0, locale));
      }
      else if( giftCodeList.size() == 1 )
      {
         builder.append(getMessageFromResource("invite.mail.gift.1", inviter, RES_DEFAILT_INVITE_MAIL_GIFT_1, locale));
      }
      else
      {
         builder.append(getMessageFromResource("invite.mail.gift.2", inviter, RES_DEFAILT_INVITE_MAIL_GIFT_2, locale));
      }
      
      String baseURL = "";
      if(request.getServerName().equalsIgnoreCase("localhost"))
      {
         baseURL = "http://" + request.getServerName() +  ":" + request.getServerPort() +request.getContextPath() + "/";
      }
      else 
      {
         baseURL = "https://" + request.getServerName() + request.getContextPath() + "/";
      }
      if( baseURL.endsWith("/") == false )
      {
         baseURL = baseURL + "/";
      }
      String url = "";

      if( (giftCodeList == null) || (giftCodeList.size() == 0) )
      {
         builder.append(getMessageFromResource("invite.mail.url", new Object[] { baseURL }, RES_DEFAILT_INVITE_MAIL_URL, locale));
      }
      else
      {
         for( GiftCodeModel gift : giftCodeList )
         {
            StringBuilder builder2 = new StringBuilder(builder);
            Object[] obj = new Object[] { "Gift Code = " + gift.getGiftCodeId() };
            url = baseURL + "signup?" + HomeController.URL_PARAM_NAME_GIFT_CODE + "=" + gift.getGiftCodeId();
            builder2.append(url);
            builder2.append("<br></br>");
            builder2.append(getMessageFromResource("invite.mail.url", obj, RES_DEFAILT_INVITE_MAIL_URL, locale));
            builder2.append(getMessageFromResource("invite.mail.footer", inviter, RES_DEFAILT_INVITE_MAIL_FOOTER, locale));
            
            try
            {
               /*
               BasicNotificationService svc = (BasicNotificationService) DAOContextProvider.getApplicationContext().getBean("basicNotifier");
               logger.info("basicNotifier = " + svc + " " + svc.getEmailSubject());
               svc.setEmailSubject(subject);
               svc.sendEmailNotification(invite.getInvitedEmailAddress(), builder2.toString());
               */
               //logger.info("invite email has been sent to " + invite.getInvitedEmailAddress());
               //logger.debug("Subject: " + subject + "\n\n" + builder2.toString());
               GiftEmailSenderThread get = new GiftEmailSenderThread();
               get.setContent(builder2.toString());
               get.setSubject(subject);
               get.setToAddress(invite.getInvitedEmailAddress());
               Thread t = new Thread(get);
               t.start();
               
            }
            catch( Exception e )
            {
               logger.error("Failed to send invite email to " + invite.getInvitedEmailAddress(), e);
            }
            
         }
      }

      builder.append(getMessageFromResource("invite.mail.footer", inviter, RES_DEFAILT_INVITE_MAIL_FOOTER, locale));

		
	}

	private static String getMessageFromResource( String name, Object[] objs, String def, Locale locale )
	{
		String rtn = DAOContextProvider.getApplicationContext().getMessage(name, objs, def, locale);

		return rtn;
	}

	private String getPaymentDescription( InviteForm inviteForm, Locale locale )
	{
		Object[] obj = new Object[] { inviteForm.getEmailAddress() };
		String   rtn = getMessageFromResource("invite.text.paydesc", obj, RES_DEFAULT_INVITE_TEXT_PAYDESC, locale);
		return rtn;
	}
	@Autowired
   public void setRegistrationManager(RegistrationManager registrationManager)
   {
      this.registrationManager = registrationManager;
   }
}
