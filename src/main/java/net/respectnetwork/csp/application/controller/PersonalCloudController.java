package net.respectnetwork.csp.application.controller;


import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import net.respectnetwork.csp.application.dao.DAOException;
import net.respectnetwork.csp.application.dao.DAOFactory;
import net.respectnetwork.csp.application.dao.InviteResponseDAO;
import net.respectnetwork.csp.application.form.AccountDetailsForm;
import net.respectnetwork.csp.application.form.PaymentForm;
import net.respectnetwork.csp.application.invite.InvitationManager;
import net.respectnetwork.csp.application.manager.PersonalCloudManager;
import net.respectnetwork.csp.application.manager.RegistrationManager;
import net.respectnetwork.csp.application.manager.StripePaymentProcessor;
import net.respectnetwork.csp.application.model.CSPModel;
import net.respectnetwork.csp.application.model.GiftCodeRedemptionModel;
import net.respectnetwork.csp.application.model.InviteResponseModel;
import net.respectnetwork.csp.application.model.PaymentModel;
import net.respectnetwork.csp.application.session.RegistrationSession;
import net.respectnetwork.sdk.csp.exception.CSPRegistrationException;

import net.respectnetwork.csp.application.dao.DAOFactory;
import net.respectnetwork.csp.application.model.InviteModel;
import net.respectnetwork.csp.application.model.DependentCloudModel;

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

import com.stripe.Stripe;
import com.stripe.exception.APIConnectionException;
import com.stripe.exception.APIException;
import com.stripe.exception.AuthenticationException;
import com.stripe.exception.CardException;
import com.stripe.exception.InvalidRequestException;
import com.stripe.model.Charge;

@Controller
public class PersonalCloudController {

	/** CLass Logger */
	private static final Logger logger = LoggerFactory
			.getLogger(PersonalCloudController.class);
	
	/** 
     * Invitation Service : to create invites and gift codes
     */
    private InvitationManager invitationManager;
    
    /**
     * Registration Service : to register dependent clouds
     */
    private RegistrationManager registrationManager;
    
    /**
     * Personal Cloud Service : to authenticate to personal cloud and get/set information from/to the personal cloud
     */
    private PersonalCloudManager personalCloudManager;
    
    
    /** 
     * CSP Name
     */
    private String cspName;
    
    /** Registration Session */
    private RegistrationSession regSession;
    
    /** 
     * CSP Cloud Name
     */
    private String cspCloudName;

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

    /**
     * @return the invitationManager
     */
    public InvitationManager getInvitationManager() {
        return invitationManager;
    }


    /**
     * @param invitationManager the invitationManager to set
     */
    @Autowired
    @Required
    public void setInvitationManager(InvitationManager invitationManager) {
        this.invitationManager = invitationManager;
    }


	/**
     * @return the csp
     */
    public String getCspName() {
        return cspName;
    }


    /**
     * @param csp the cspName to set
     */
    @Autowired
    @Qualifier("cspName")
    public void setCspName(String cspName) {
        this.cspName = cspName;
    }

    /**
     * @return the regSession
     */
    public RegistrationSession getRegSession() {
        return regSession;
    }

    /**
     * @param regSession
     *            the regSession to set
     */
    @Autowired
    public void setRegSession(RegistrationSession regSession) {
        this.regSession = regSession;
    }

	
	@RequestMapping(value = "/login", method = RequestMethod.GET)
	public ModelAndView showLoginForm(HttpServletRequest request, Model model) {
		logger.info("showing login form");
        
        ModelAndView mv = null; 
        
        String cspHomeURL = request.getContextPath();
        String formPostURL = cspHomeURL + "/cloudPage";
       
        mv = new ModelAndView("login");
        mv.addObject("postURL", formPostURL);
		return mv;
	}
    
	@RequestMapping(value = "/cloudPage", method = {RequestMethod.POST,RequestMethod.GET})
	public ModelAndView showCloudPage(HttpServletRequest request, Model model) {
		logger.info("showing cloudPage form");
		
        ModelAndView mv = null; 
        CloudName cloudName = null;
        
        if(request.getParameter("cloudname") != null){
        	cloudName = CloudName.create(request.getParameter("cloudname"));
        } else if (regSession.getCloudName() != null){
        	cloudName = CloudName.create(regSession.getCloudName());
        }
	if( cloudName == null )
	{
		return processLogout(request, model);
	}
        
        net.respectnetwork.sdk.csp.CSP myCSP = registrationManager.getCspRegistrar();
        if(myCSP != null){
        	CloudNumber cloudNumber;
			try {
				cloudNumber = myCSP.checkCloudNameAvailableInRN(cloudName);
				String secretToken = null;
				if(regSession != null){
					secretToken = regSession.getPassword();
				}
				if(secretToken == null || secretToken.isEmpty()){
						secretToken = request.getParameter("secrettoken") ;
				}
				myCSP.authenticateInCloud(cloudNumber,secretToken);
				
		        if(regSession != null && regSession.getCloudName() == null){
		        	
		        	if (regSession.getSessionId() == null || regSession.getSessionId().isEmpty() ) {
		        		String sessionId =  UUID.randomUUID().toString();
		        		regSession.setSessionId(sessionId);
		        		logger.info("Creating a new regSession with session id =" + sessionId);
		        	}
			        logger.info("Setting cloudname as  " + request.getParameter("cloudname"));
			        if(request.getParameter("cloudname") != null) {
			        	regSession.setCloudName(request.getParameter("cloudname"));
			        }
			        //logger.info("Setting secret token as  " + request.getParameter("secrettoken"));
			        if(request.getParameter("secrettoken") != null){
			        	regSession.setPassword(request.getParameter("secrettoken"));
			        }
		        }
		        mv = getCloudPage(request, regSession.getCloudName());
		        logger.info("Successfully authenticated to the personal cloud for " + request.getParameter("cloudname") );
			} catch (Xdi2ClientException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				logger.info("Authenticating to personal cloud failed for " + request.getParameter("cloudname") );
				String cspHomeURL = request.getContextPath();
	            String formPostURL = cspHomeURL + "/cloudPage";          
	            mv = new ModelAndView("login");
	            mv.addObject("postURL", formPostURL);
			}
        	
        } else {
        	logger.info("CSP Object is null. " );
        	String cspHomeURL = request.getContextPath();
            String formPostURL = cspHomeURL + "/cloudPage";          
            mv = new ModelAndView("login");
            mv.addObject("postURL", formPostURL);
        }
        
        
		return mv;
	}


	public RegistrationManager getRegistrationManager() {
		return registrationManager;
	}

	@Autowired
	public void setRegistrationManager(RegistrationManager registrationManager) {
		this.registrationManager = registrationManager;
	}


	public PersonalCloudManager getPersonalCloudManager() {
		return personalCloudManager;
	}


	@Autowired
	public void setPersonalCloudManager(PersonalCloudManager personalCloudManager) {
		this.personalCloudManager = personalCloudManager;
	}
	
	@RequestMapping(value = "/logout", method = { RequestMethod.GET, RequestMethod.POST})
	public ModelAndView processLogout(HttpServletRequest request, Model model) {
		logger.info("processing logout");
        
		//nullify password from the session object
		if(regSession != null){
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
	
	@RequestMapping(value = "/stripeConnect", method = RequestMethod.POST)
	public ModelAndView processStripeResponse(@Valid @ModelAttribute("paymentInfo") PaymentForm paymentForm, HttpServletRequest request, Model model , BindingResult result) {
		logger.info("processing STRIPE Response");
		
		boolean errors = false;
        
        String cloudName = regSession.getCloudName();

		ModelAndView mv = getCloudPage(request, cloudName);
		
        String sessionIdentifier = regSession.getSessionId(); 
        String email = regSession.getVerifiedEmail();
        String phone = regSession.getVerifiedMobilePhone();
        String password = regSession.getPassword();
        logger.debug(sessionIdentifier + "--" + cloudName + "--" + email + "--" + phone + "--" + password);
        logger.debug("Invite Code =" + regSession.getInviteCode() + " and gift code = " + regSession.getGiftCode());
        
		//Check Session      
        if (sessionIdentifier == null || cloudName == null || email == null
                || phone == null || password == null) {
            errors = true;    
            mv.addObject("error", "Invalid Session"); 
            logger.debug("Invalid Session ...");
        }
        CSPModel cspModel = null;
        DAOFactory dao = DAOFactory.getInstance();
		try {
			cspModel = dao.getCSPDAO().get(this.getCspName());
		} catch (DAOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			errors = true;    
            mv.addObject("error", "Cannot connect to DB to lookup information");
            logger.debug("Cannot connect to DB to lookup info...");
		}
		logger.debug("Reached here - 5 , errors = " + errors);
			if(!errors) {
				logger.debug("Reached here - 10");
				
				//need a new unique response id
                String responseId = UUID.randomUUID().toString();
              //make entries in the invite_response table or giftcode_redemption table that a new cloud has been registered against an invite code and optionally a gif code
                if((regSession.getInviteCode() != null) && (regSession.getGiftCode() != null)) {
                	logger.debug("Reached here - 20");
			    	 logger.debug("Going to create the personal cloud now for gift code path...");
		                try {
							registrationManager.registerUser(CloudName.create(cloudName), phone,
							        email, password);
							logger.debug("Reached here - 30");
							AccountDetailsForm accountForm = new AccountDetailsForm();
			                accountForm.setCloudName(cloudName);
			                mv.addObject("accountInfo", accountForm);   
			                
			                logger.debug("Sucessfully Registered {}", cloudName );
						} catch (Xdi2ClientException e1) {
							logger.debug("Reached here - 40");
							// TODO Auto-generated catch block
							e1.printStackTrace();
						} catch (CSPRegistrationException e1) {
							logger.debug("Reached here - 50");
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}

		                logger.debug("Reached here - 60");
                	//make a new record in the giftcode_redemption table
                	GiftCodeRedemptionModel giftCodeRedemption = new GiftCodeRedemptionModel();
                	giftCodeRedemption.setCloudNameCreated(cloudName);
                	giftCodeRedemption.setGiftCodeId(regSession.getGiftCode());
                	giftCodeRedemption.setRedemptionId(responseId);
                	giftCodeRedemption.setTimeCreated(new Date());
                	try {
						dao.getGiftCodeRedemptionDAO().insert(giftCodeRedemption);
						logger.debug("Reached here - 70");
					} catch (DAOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
                	logger.debug("Reached here - 80");
                	
                } else { //CC path
                	logger.debug("Reached here - 90");
					BigDecimal amount   = cspModel.getCostPerCloudName();
					String     desc     = "A personal cloud for " + cloudName;
	
					String     token    = StripePaymentProcessor.getToken(request);
					
					
					if(token != null && !token.isEmpty()) { //this is a CC charge request
						PaymentModel payment = StripePaymentProcessor.makePayment(cspModel, amount, desc, token);
						if(payment != null){
							
							try {
								dao.getPaymentDAO().insert(payment);
							} catch (DAOException e1) {
								logger.error("Could not insert payment record in the DB " + e1.getMessage());
								logger.info("Payment record info \n" + payment.toString());
							}
						     try {
						    	 logger.debug("Going to create the personal cloud now for CC path ...");
					                registrationManager.registerUser(CloudName.create(cloudName), phone,
					                        email, password);
					                
					                 if (regSession.getInviteCode() != null){
					                	//make a new record in the invite_response table
					                	InviteResponseModel inviteResponse = new InviteResponseModel();
					                	inviteResponse.setCloudNameCreated(cloudName);
					                	inviteResponse.setInviteId(regSession.getInviteCode());
					                	inviteResponse.setResponseId(responseId);
					                	inviteResponse.setPaymentId(payment.getPaymentId());
					                	
					                	InviteResponseDAO inviteResponseDAO = DAOFactory.getInstance().getInviteResponseDAO();
					                	inviteResponseDAO.insert(inviteResponse);
					                }
					                
					                AccountDetailsForm accountForm = new AccountDetailsForm();
					                accountForm.setCloudName(cloudName);
					                mv.addObject("accountInfo", accountForm);   
					                
					                logger.debug("Sucessfully Registered {}", cloudName );
					                
					            } catch (Exception e) {
					                logger.warn("Registration Error {}", e.getMessage());
					                mv.addObject("error", e.getMessage());
					                
					            }
						 
						}   
					}
                }
		}

        return mv;
	}
	
	public static ModelAndView getCloudPage( HttpServletRequest request, String cloudName )
	{
		ModelAndView mv         = new ModelAndView("cloudPage");
		String       cspHomeURL = request.getContextPath();

		mv.addObject("logoutURL", cspHomeURL + "/logout");
		mv.addObject("cloudName", cloudName);

		try
		{
			DAOFactory dao = DAOFactory.getInstance();

			List<InviteModel>         invList = dao.getInviteDAO().listGroupByInvited(cloudName);
			List<DependentCloudModel> depList = dao.getDependentCloudDAO().list(cloudName);

			mv.addObject("inviteList"   , invList);
			mv.addObject("dependentList", depList);
		}
		catch( DAOException e )
		{
			logger.error("Failed to perform DAO opertations", e);
		}

		return mv;
	}
}
