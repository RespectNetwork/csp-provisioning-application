package net.respectnetwork.csp.application.controller;

import java.math.BigDecimal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
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

	private String              cspCloudName;
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

		// rtn = "=animesh.test15";

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
		ModelAndView mv = new ModelAndView("cspdashboard");
		AccountDetailsForm acct = new AccountDetailsForm();
		acct.setCloudName(this.getCloudName());
		mv.addObject("accountInfo", acct);
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
			InviteModel invite = this.saveInvite(inviteForm, null, null);
			mv = new ModelAndView("inviteDone");
			mv.addObject("inviteModel" , invite);
			mv.addObject("giftCardList", null);
			return mv;
		}

		cspModel = DAOFactory.getInstance().getCSPDAO().get(this.getCspCloudName());
		BigDecimal quantity = BigDecimal.valueOf((long) inviteForm.getGiftCardQuantity().intValue());
		BigDecimal amount   = cspModel.getCostPerCloudName().multiply(quantity);
		String     desc     = this.getPaymentDescription(inviteForm, request);

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
		String     desc     = this.getPaymentDescription(inviteForm, request);

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

		InviteModel invite = this.saveInvite(inviteForm, payment, giftCardList);
		mv = new ModelAndView("inviteDone");
		mv.addObject("cspModel"    , cspModel);
		mv.addObject("inviteModel" , invite);
		mv.addObject("giftCardList", giftCardList);

		return mv;
	}

	private InviteModel saveInvite( InviteForm inviteForm, PaymentModel payment, List<GiftCodeModel> giftCodeList ) throws DAOException
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

		this.sendInviteEmail(rtn, giftCodeList);

		return rtn;
	}

	private void sendInviteEmail( InviteModel invite, List<GiftCodeModel> giftCodeList )
	{
		logger.info("sendInviteEmail - " + invite);
		logger.info("sendInviteEmail - " + giftCodeList);
	}

	private String getPaymentDescription( InviteForm inviteForm, HttpServletRequest request )
	{
		String rtn = "CloudName Gift Cards for {0}";
		rtn = DAOContextProvider.getApplicationContext().getMessage("invite.text.paydesc", 
			new Object[] { inviteForm.getEmailAddress() }, rtn, request.getLocale());
		return rtn;
	}
}
