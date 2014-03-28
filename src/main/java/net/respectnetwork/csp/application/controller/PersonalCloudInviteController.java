package net.respectnetwork.csp.application.controller;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import net.respectnetwork.csp.application.form.AccountDetailsForm;
import net.respectnetwork.csp.application.invite.InvitationManager;
import net.respectnetwork.csp.application.manager.PersonalCloudManager;
import net.respectnetwork.csp.application.manager.RegistrationManager;
import net.respectnetwork.csp.application.manager.StripePaymentProcessor;
import net.respectnetwork.csp.application.session.RegistrationSession;

import net.respectnetwork.sdk.csp.notification.BasicNotificationService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import xdi2.client.exceptions.Xdi2ClientException;
import xdi2.core.xri3.CloudName;
import xdi2.core.xri3.CloudNumber;
import net.respectnetwork.csp.application.form.InviteForm;
import net.respectnetwork.csp.application.model.*;
import net.respectnetwork.csp.application.dao.*;

import javax.validation.Valid;

import org.springframework.validation.*;

@Controller
public class PersonalCloudInviteController
{
	private static final Logger logger = LoggerFactory.getLogger(PersonalCloudInviteController.class);

	private static final List<Integer> quantityList = new ArrayList<Integer>(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10));

	private static final String RES_DEFAULT_INVITE_TEXT_PAYDESC = "CloudName Gift Cards for {0}";

	private static final String RES_DEFAILT_INVITE_MAIL_SUBJECT = "Invitation from {0} to join Respect Network";
	private static final String RES_DEFAILT_INVITE_MAIL_HEADER  = "{0} has invited you to join Respect Network at http://respectnetwork.com/\n\n";
	private static final String RES_DEFAILT_INVITE_MAIL_GIFT_0  = "Please click the following URL to sign up:\n\n";
	private static final String RES_DEFAILT_INVITE_MAIL_GIFT_1  = "Please click the following URL to sign up and redeem the gift card given by {0}:\n\n";
	private static final String RES_DEFAILT_INVITE_MAIL_GIFT_2  = "Please click the following URLs to sign up and redeem the gift cards given by {0}:\n\n";
	private static final String RES_DEFAILT_INVITE_MAIL_URL     = "   {0}\n\n";
	private static final String RES_DEFAILT_INVITE_MAIL_FOOTER  = "\n\nSincerely,\n\nRespect Network\n\nhttp://respectnetwork.com/\n\nThe Personal Cloud Network";

	private String              cspCloudName;
	private String		    cspInviteURL;
	private RegistrationSession regSession;
    
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
		String       cloudName  = this.getCloudName();

		logger.info("showing invite page - " + cloudName);

		String       cspHomeURL = request.getContextPath();
		ModelAndView mv         = null;
		InviteForm   inviteForm = null;
		CSPModel     cspModel   = null;

		if( cloudName == null )
		{
			mv = new ModelAndView("login");
			mv.addObject("postURL", cspHomeURL + "/cloudPage");
			return mv;
		}

		cspModel = DAOFactory.getInstance().getCSPDAO().get(this.getCspCloudName());

		mv = new ModelAndView("invite");
		mv.addObject("cspModel"    , cspModel);
		mv.addObject("quantityList", quantityList);

		inviteForm = new InviteForm();
		model.addAttribute("inviteForm", inviteForm);

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

		if( cloudName == null )
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
		}
		else
		{
			mv = new ModelAndView("inviteReview");
		}

		cspModel = DAOFactory.getInstance().getCSPDAO().get(this.getCspCloudName());
		mv.addObject("cspModel"    , cspModel);

		model.addAttribute("inviteForm", inviteForm);
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

		if( cloudName == null )
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
		else if(    (inviteForm.getGiftCardFlag() == null)
		         || Boolean.FALSE.equals(inviteForm.getGiftCardFlag())
		         || (inviteForm.getGiftCardQuantity() == null)
		         || (inviteForm.getGiftCardQuantity().intValue() <= 0) )
		{
			logger.info("Without payment");
			InviteModel invite = this.saveInvite(inviteForm, null, null, request.getLocale());
			mv = new ModelAndView("inviteDone");
			mv.addObject("inviteModel" , invite);
			mv.addObject("giftCardList", null);
			return mv;
		}

		cspModel = DAOFactory.getInstance().getCSPDAO().get(this.getCspCloudName());
		BigDecimal quantity = BigDecimal.valueOf((long) inviteForm.getGiftCardQuantity().intValue());
		BigDecimal amount   = cspModel.getCostPerCloudName().multiply(quantity);
		String     desc     = this.getPaymentDescription(inviteForm, request.getLocale());

		inviteForm.setInviteId(UUID.randomUUID().toString());

		mv = new ModelAndView("inviteSubmit");

		mv.addObject("cspModel"    , cspModel);
		mv.addObject("javaScript"  , StripePaymentProcessor.getJavaScript(cspModel, amount, desc));

		model.addAttribute("inviteForm", inviteForm);
		this.setInviteForm(inviteForm);

		return mv;
	}

	@RequestMapping(value = "/invitePayment", method = RequestMethod.POST)
	public ModelAndView showInvitePaymentForm( Model model, HttpServletRequest request ) throws DAOException
	{
		String       cloudName  = this.getCloudName();
		InviteForm   inviteForm = this.getInviteForm();

		logger.info("showing invite payment page - " + cloudName + " : " + inviteForm);

		String       cspHomeURL = request.getContextPath();
		ModelAndView mv         = null;
		CSPModel     cspModel   = null;

		if( cloudName == null )
		{
			mv = new ModelAndView("login");
			mv.addObject("postURL", cspHomeURL + "/cloudPage");
			return mv;
		}

		if( inviteForm == null )
		{
			mv = showInviteDoneForm(model, request);
			return mv;
		}

		// nullify session variable

		this.setInviteForm(null);

		InviteModel inviteModel = DAOFactory.getInstance().getInviteDAO().get(inviteForm.getInviteId());
		if( inviteModel != null )
		{
			logger.error("InviteModel already exist - " + inviteModel);
			mv = showInviteDoneForm(model, request);
			return mv;
		}

		cspModel = DAOFactory.getInstance().getCSPDAO().get(this.getCspCloudName());
		BigDecimal quantity = BigDecimal.valueOf((long) inviteForm.getGiftCardQuantity().intValue());
		BigDecimal amount   = cspModel.getCostPerCloudName().multiply(quantity);
		String     desc     = this.getPaymentDescription(inviteForm, request.getLocale());

		String     token    = StripePaymentProcessor.getToken(request);
		if( token == null )
		{
			mv = new ModelAndView("inviteSubmit");

			mv.addObject("error"       , "Failed to obtain payment token. Please try again!");
			mv.addObject("cspModel"    , cspModel);
			mv.addObject("javaScript"  , StripePaymentProcessor.getJavaScript(cspModel, amount, desc));

			model.addAttribute("inviteForm", inviteForm);
			this.setInviteForm(inviteForm);

                        return mv;
		}

		PaymentModel payment = StripePaymentProcessor.makePayment(cspModel, amount, desc, token);

		if( payment == null )
		{
			mv = new ModelAndView("inviteSubmit");

			mv.addObject("error"       , "Failed to process payment. Please try again!");
			mv.addObject("cspModel"    , cspModel);
			mv.addObject("javaScript"  , StripePaymentProcessor.getJavaScript(cspModel, amount, desc));

			model.addAttribute("inviteForm", inviteForm);
			this.setInviteForm(inviteForm);

                        return mv;
		}

		List<GiftCodeModel> giftCardList = new ArrayList<GiftCodeModel>();

		InviteModel invite = this.saveInvite(inviteForm, payment, giftCardList, request.getLocale());
		mv = new ModelAndView("inviteDone");
		mv.addObject("cspModel"    , cspModel);
		mv.addObject("inviteModel" , invite);
		mv.addObject("giftCardList", giftCardList);

		return mv;
	}

	private InviteModel saveInvite( InviteForm inviteForm, PaymentModel payment, List<GiftCodeModel> giftCodeList, Locale locale ) throws DAOException
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
		rtn.setCspCloudName(this.getCspCloudName());
		rtn.setInviterCloudName(this.getCloudName());
		rtn.setInvitedEmailAddress(inviteForm.getEmailAddress());
		rtn.setEmailSubject("Invitation to Join Respect Network");
		rtn.setEmailMessage(inviteForm.getEmailMessage());

		if( payment != null )
		{
			dao.getPaymentDAO().insert(payment);
		}

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

		this.sendInviteEmail(rtn, giftCodeList, locale);

		return rtn;
	}

	private void sendInviteEmail( InviteModel invite, List<GiftCodeModel> giftCodeList, Locale locale )
	{
		logger.info("sendInviteEmail - " + invite + " : " + giftCodeList);

		Object[]      inviter = new Object[] { invite.getInviterCloudName() };
		String        subject = this.getMessageFromResource("invite.mail.subject", inviter, RES_DEFAILT_INVITE_MAIL_SUBJECT, locale);
		StringBuilder builder = new StringBuilder();

		builder.append(this.getMessageFromResource("invite.mail.header" , inviter, RES_DEFAILT_INVITE_MAIL_HEADER , locale));
		builder.append(invite.getEmailMessage().trim());
		builder.append('\n');
		builder.append('\n');
		if( (giftCodeList == null) || (giftCodeList.size() == 0) )
		{
			builder.append(this.getMessageFromResource("invite.mail.gift.0", inviter, RES_DEFAILT_INVITE_MAIL_GIFT_0, locale));
		}
		else if( giftCodeList.size() == 1 )
		{
			builder.append(this.getMessageFromResource("invite.mail.gift.1", inviter, RES_DEFAILT_INVITE_MAIL_GIFT_1, locale));
		}
		else
		{
			builder.append(this.getMessageFromResource("invite.mail.gift.2", inviter, RES_DEFAILT_INVITE_MAIL_GIFT_2, locale));
		}
		String url = this.getCspInviteURL();
		if( url.endsWith("/") == false )
		{
			url = url + "/";
		}
		url = url + "signup?" + HomeController.URL_PARAM_NAME_INVITE_CODE + "=" + invite.getInviteId();

		if( (giftCodeList == null) || (giftCodeList.size() == 0) )
		{
			builder.append(this.getMessageFromResource("invite.mail.url", new Object[] { url }, RES_DEFAILT_INVITE_MAIL_URL, locale));
		}
		else
		{
			for( GiftCodeModel gift : giftCodeList )
			{
				Object[] obj = new Object[] { url + "&" + HomeController.URL_PARAM_NAME_GIFT_CODE + "=" + gift.getGiftCodeId() };
				builder.append(this.getMessageFromResource("invite.mail.url", obj, RES_DEFAILT_INVITE_MAIL_URL, locale));
			}
		}

		builder.append(this.getMessageFromResource("invite.mail.footer", inviter, RES_DEFAILT_INVITE_MAIL_FOOTER, locale));

		try
		{
			BasicNotificationService svc = (BasicNotificationService) DAOContextProvider.getApplicationContext().getBean("basicNotifier");
			logger.info("basicNotifier = " + svc + " " + svc.getEmailSubject());
			svc.setEmailSubject(subject);
			svc.sendEmailNotification(invite.getInvitedEmailAddress(), builder.toString());
			logger.info("invite email has been sent to " + invite.getInvitedEmailAddress());
			logger.debug("Subject: " + subject + "\n\n" + builder.toString());
		}
		catch( Exception e )
		{
			logger.error("Failed to send invite email to " + invite.getInvitedEmailAddress(), e);
		}
	}

	private String getMessageFromResource( String name, Object[] objs, String def, Locale locale )
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
}
