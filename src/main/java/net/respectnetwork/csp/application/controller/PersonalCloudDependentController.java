package net.respectnetwork.csp.application.controller;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import net.respectnetwork.csp.application.dao.DAOException;
import net.respectnetwork.csp.application.dao.DAOFactory;
import net.respectnetwork.csp.application.exception.UserRegistrationException;
import net.respectnetwork.csp.application.form.DependentForm;
import net.respectnetwork.csp.application.form.PaymentForm;
import net.respectnetwork.csp.application.manager.RegistrationManager;
import net.respectnetwork.csp.application.manager.StripePaymentProcessor;
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
	private static final Logger logger = LoggerFactory.getLogger(PersonalCloudDependentController.class);
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
		return rtn;
	}

	private DependentForm getDependentForm()
	{
		DependentForm rtn = regSession.getDependentForm();

		return rtn;
	}
	private void setDependentForm( DependentForm dependentForm )
	{
		regSession.setDependentForm(dependentForm);
	}

	@RequestMapping(value = "/dependent", method = RequestMethod.GET)
	public ModelAndView showDependentForm(HttpServletRequest request, Model model) throws DAOException
	{
		String       cloudName  = this.getCloudName();

		logger.info("showing dependent page - " + cloudName);

		String       cspHomeURL = request.getContextPath();
		ModelAndView mv         = null;
		DependentForm dependentForm = null;
		CSPModel     cspModel   = null;

		if( cloudName == null )
		{
			mv = new ModelAndView("login");
			mv.addObject("postURL", cspHomeURL + "/cloudPage");
			return mv;
		}

		cspModel = DAOFactory.getInstance().getCSPDAO().get(this.getCspCloudName());

		mv = new ModelAndView("dependent");
		mv.addObject("cspModel"    , cspModel);
		dependentForm = new DependentForm();
		model.addAttribute("dependentForm", dependentForm);

		return mv;
	}
/*
	@RequestMapping(value = "/dependentReview", method = RequestMethod.POST)
	public ModelAndView showInviteReviewForm( @Valid @ModelAttribute("dependentForm") DependentForm dependentForm, BindingResult result, Model model, HttpServletRequest request ) throws DAOException
	{
		String       cloudName  = this.getCloudName();

		logger.info("showing dependent review page - " + cloudName + " : " + dependentForm);

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
			mv = new ModelAndView("cloudPage");        
	        mv.addObject("logoutURL", cspHomeURL + "/logout");
	        return mv;
		}
		else
		{
			mv = new ModelAndView("dependentReview");
		}

		cspModel = DAOFactory.getInstance().getCSPDAO().get(this.getCspCloudName());
		
		mv.addObject("cspModel"    , cspModel);
		
		model.addAttribute("dependentForm", dependentForm);

		
		return mv;
	}
*/
	@RequestMapping(value = "/dependentDone", method = RequestMethod.GET)
	public ModelAndView showDependentDoneForm( Model model, HttpServletRequest request ) throws DAOException
	{
		ModelAndView mv = PersonalCloudController.getCloudPage(request, this.regSession.getCloudName());
		return mv;
	}

	@RequestMapping(value = "/dependentSubmit", method = RequestMethod.POST)
	public ModelAndView showDependentSubmitForm( @Valid @ModelAttribute("dependentForm") DependentForm dependentForm, BindingResult result, Model model, HttpServletRequest request ) throws DAOException
	{
		String       cloudName  = this.getCloudName();
		boolean errors = false; 

		logger.info("showing dependent submit page - " + cloudName + " : " + dependentForm);

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
			mv = PersonalCloudController.getCloudPage(request, cloudName);
			return mv;
		}
		
		cspModel = DAOFactory.getInstance().getCSPDAO().get(this.getCspCloudName());
		
		
		
		String 		dependentCloudName = dependentForm.getDependentCloudName();
		
		String errorStr = "";
		
		try	{
			logger.info("Checking for dependent cloudname :" + dependentCloudName);
			if (! theManager.isCloudNameAvailable(dependentCloudName)) {
                errorStr = "CloudName not Available";
                errors = true;
            }
		} catch (UserRegistrationException e) {
			errorStr = "System Error checking CloudName";
            logger.warn(errorStr + " : {}", e.getMessage());
            errors = true;
		}
		if(errors){
            mv = new ModelAndView("dependent");
    		mv.addObject("cspModel"    , cspModel);
    		dependentForm = new DependentForm();
    		model.addAttribute("dependentForm", dependentForm);
            mv.addObject("error", errorStr);
            errors = true;
            return mv;
			
		}
		/*
		BigDecimal amount   = cspModel.getCostPerCloudName();
		String     desc     = this.getPaymentDescription(dependentForm, request);
		
		mv = new ModelAndView("dependentSubmit");

		mv.addObject("cspModel"    , cspModel);
		mv.addObject("javaScript"  , StripePaymentProcessor.getJavaScript(cspModel, amount, desc));

		model.addAttribute("dependentForm", dependentForm);
		*/
		mv = new ModelAndView("payment");
		PaymentForm paymentForm = new PaymentForm();
      paymentForm.setTxnType(PaymentForm.TXN_TYPE_DEP);
      paymentForm.setNumberOfClouds(1);
      mv.addObject("paymentInfo", paymentForm);
		this.setDependentForm(dependentForm);
		return mv;
	}


	public  static DependentCloudModel saveDependent( DependentForm dependentForm, PaymentModel payment , String cloudName) 
	{
		DependentCloudModel dependentCloud = null;
		DAOFactory dao = DAOFactory.getInstance();

		if( payment != null )
		{
			dependentCloud = new DependentCloudModel();
			dependentCloud.setDependentCloudName(dependentForm.getDependentCloudName());
			dependentCloud.setGuardianCloudName(cloudName);
			if(payment != null)
			{
			   dependentCloud.setPaymentId(payment.getPaymentId());
			}
			dependentCloud.setTimeCreated(new Date());
			try {
			   dao.getDependentCloudDAO().insert(dependentCloud);
			}
			catch(DAOException ex)
			{
			   logger.debug("Error while saving dependent record in DB " + ex.getMessage());
			   return null;
			}
		}
		return dependentCloud;
	}

	private String getPaymentDescription( DependentForm dependentForm, HttpServletRequest request )
	{
		String rtn = "Personal Cloud For " + dependentForm.getDependentCloudName();
		
		return rtn;
	}

	public RegistrationManager getTheManager() {
		return theManager;
	}
	@Autowired
	public void setTheManager(RegistrationManager theManager) {
		this.theManager = theManager;
	}
}
