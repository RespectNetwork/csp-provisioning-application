package net.respectnetwork.csp.application.controller;

import java.math.BigDecimal;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import net.respectnetwork.csp.application.dao.DAOException;
import net.respectnetwork.csp.application.dao.DAOFactory;
import net.respectnetwork.csp.application.dao.InviteResponseDAO;
import net.respectnetwork.csp.application.exception.UserRegistrationException;
import net.respectnetwork.csp.application.form.AccountDetailsForm;
import net.respectnetwork.csp.application.form.PaymentForm;
import net.respectnetwork.csp.application.form.SignUpForm;
import net.respectnetwork.csp.application.form.UserDetailsForm;
import net.respectnetwork.csp.application.form.ValidateForm;
import net.respectnetwork.csp.application.manager.RegistrationManager;
import net.respectnetwork.csp.application.manager.StripePaymentProcessor;
import net.respectnetwork.csp.application.model.CSPModel;
import net.respectnetwork.csp.application.model.InviteModel;
import net.respectnetwork.csp.application.model.InviteResponseModel;
import net.respectnetwork.csp.application.session.RegistrationSession;
import net.respectnetwork.sdk.csp.payment.PaymentStatusCode;
import net.respectnetwork.sdk.csp.validation.CSPValidationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import xdi2.core.xri3.CloudName;
import xdi2.core.xri3.CloudNumber;

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
    
    /** Registration Session */
    private RegistrationSession regSession;
    
    private String              cspCloudName;
    
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


    /**
     * Initial Sign-Up Page
     */
    @RequestMapping(value = "/signup", method = RequestMethod.POST)
    public ModelAndView signup(
            @Valid @ModelAttribute("signUpInfo") SignUpForm signUpForm,
            HttpServletRequest request, HttpServletResponse response,
            BindingResult result) {
        
        logger.debug("Starting the Sign Up Process");
        
        ModelAndView mv = null; 
        boolean errors = false;
        
        String cloudName = signUpForm.getCloudName();
        String inviteCode = signUpForm.getInviteCode();
        String giftCode = signUpForm.getGiftCode();
        
        logger.debug("Invite Code = " + inviteCode);
        logger.debug("Gift Code = " + giftCode);
        logger.debug("Cloud Name : " + cloudName);
        
        if (cloudName != null) {           
        // Start Check that the Cloud Number is Available.
            try {
            	if(theManager.isRequireInviteCode() && ((inviteCode == null) || (inviteCode.trim().isEmpty())) ){
            		errors = true;
            		logger.debug("Invite code is required and it has not been passed in the input query parameters(inviteCode=)");
            	}
            	else if (! theManager.isCloudNameAvailable(cloudName)) {
                    String errorStr = "CloudName not Available";
                    mv.addObject("cloudNameError", errorStr); 
                    errors = true;
                }
            } catch (UserRegistrationException e) {
                String errorStr = "System Error checking CloudName";
                logger.warn(errorStr + " : {}", e.getMessage());
                mv.addObject("error", errorStr);
                errors = true;
            }
            InviteModel invite = null;
           //add the email address of the person who was invited in the userDetailsForm object, the user shouldn't be able to change it
            DAOFactory dao = DAOFactory.getInstance();
            UserDetailsForm userDetailsForm = new UserDetailsForm();
            try {
				invite = dao.getInviteDAO().get(inviteCode);
				if(invite != null && (invite.getInvitedEmailAddress() != null || invite.getInvitedEmailAddress().trim().isEmpty() )) {
					userDetailsForm.setEmail(invite.getInvitedEmailAddress());
				} else {
					logger.error("This invite object does not have an email address or another valid identifier associated to it. Sending user to the signup page.");
					errors = true;
				}
			} catch (DAOException e) {
				logger.error("Could not get invite information from DB. Sending user to the signup page.");
				errors = true;
			}
            if (!errors) {
                mv = new ModelAndView("userdetails");
                
                mv.addObject("userInfo", userDetailsForm);

                // Add CloudName to Session

                String sessionId =  UUID.randomUUID().toString();
                regSession.setSessionId(sessionId);
                regSession.setCloudName(signUpForm.getCloudName());
                regSession.setInviteCode(inviteCode);
                regSession.setGiftCode(giftCode);
                regSession.setVerifiedEmail(invite.getInvitedEmailAddress());

            } else {
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
            BindingResult result) {
        
        logger.debug("Get User Details");
        
        ModelAndView mv = null; 
        boolean errors = false;
        mv = new ModelAndView("userdetails");
        mv.addObject("userInfo", userDetailsForm);
        
        String cn = regSession.getCloudName();
        String sessionId = regSession.getSessionId();
        
        //Session Check
        if (sessionId == null || cn == null){
            errors = true;
            mv.addObject("error", "Invalid Session");          
        }
 
        if (!errors) {
            try {
                CloudNumber[] existingUsers = theManager
                        .checkEmailAndMobilePhoneUniqueness(
                                userDetailsForm.getMobilePhone(),
                                userDetailsForm.getEmail());
                if (existingUsers[0] != null) {
                    // Communicate back to Form phone is already taken
                    String errorStr = "Phone Number not Unique";
                    mv.addObject("phoneError", errorStr);
                    logger.debug("Phone {} already used by {}",
                            userDetailsForm.getMobilePhone(), existingUsers[0]);
                    errors = true;
                }
                if (existingUsers[1] != null) {
                    String errorStr = "Email not Unique";
                    mv.addObject("emailError", errorStr);
                    logger.debug("Phone {} already used by {}",
                            userDetailsForm.getEmail(), existingUsers[1]);
                    errors = true;
                }
            } catch (UserRegistrationException e) {
                String errorStr = "System Error checking Email/Phone Number Uniqueness";
                logger.warn(errorStr + " : {}", e.getMessage());
                mv.addObject("error", errorStr);
                errors = true;
            }
        }

        if (!errors) {

            // If all is okay send out the validation messages.
            try {
                theManager.sendValidationCodes(sessionId,
                        userDetailsForm.getEmail(),
                        userDetailsForm.getMobilePhone());
            } catch (CSPValidationException e) {
                String errorStr = "System Error sending validation messages";
                logger.warn(errorStr + " : {}", e.getMessage());
                mv.addObject("error", errorStr);
                errors = true;
            }
        }

        if (!errors) {
            mv = new ModelAndView("validate");
            ValidateForm validateForm = new ValidateForm();
            mv.addObject("validateInfo", validateForm);

            // Add CloudName/ Email / Password and Phone to Session

            logger.debug("Setting verified email " + regSession.getVerifiedEmail());
            //regSession.setVerifiedEmail(userDetailsForm.getEmail());
            regSession.setVerifiedMobilePhone(userDetailsForm.getMobilePhone());
            regSession.setPassword(userDetailsForm.getPassword());
        }
           
                                               
        mv.addObject("userInfo", userDetailsForm);
                                    
        return mv;        
    }
    
    
    /**
     * Validate Confirmation Codes,
     * 
     * 
     * @param userForm Form with User's details
     * @param result Binding Result for Validation or errors
     * @return ModelandView of next  travel location
     */
    @RequestMapping(value = "/validatecodes", method = RequestMethod.POST)
    public ModelAndView validateCodes(
            @Valid @ModelAttribute("validateInfo") ValidateForm validateForm,
            HttpServletRequest request,
            BindingResult result) {
        
                
        logger.debug("Starting Validation Process");
        logger.debug("Processing Validation Data: {}", validateForm.toString());
        
        ModelAndView mv = new ModelAndView("validate");
        String sessionIdentifier = regSession.getSessionId(); 
       
        boolean errors = false;
        
        //Validate Codes
        if (!theManager.validateCodes(sessionIdentifier, validateForm.getEmailCode(), validateForm.getSmsCode())) {
            String errorStr = "Code(s) Validation Failed";
            logger.debug(errorStr);
            mv.addObject("codeValidationError", errorStr); 
            errors = true;
        }
        
        if (!errors) {
        	
        	CSPModel     cspModel   = null;
        	
        	try {
				cspModel = DAOFactory.getInstance().getCSPDAO().get(this.getCspCloudName());
			} catch (DAOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        	mv = new ModelAndView("payment");
        	
    		BigDecimal amount   = cspModel.getCostPerCloudName();
    		String     desc     = "Personal cloud  " + regSession.getCloudName();
    		mv.addObject("cspModel"    , cspModel);
    		if(regSession.getGiftCode() != null){
    			mv.addObject("giftCode"  ,regSession.getGiftCode());
    		}
    		else {
    			mv.addObject("javaScript"  , StripePaymentProcessor.getJavaScript(cspModel, amount, desc));
    		}
			
/*            
            PaymentForm paymentForm = new PaymentForm();
            String paymentId = UUID.randomUUID().toString();
            paymentForm.setPaymentId(paymentId);
            mv.addObject("paymentInfo", paymentForm);
*/            
        }
            
        
        return mv;
        
    }
    
    
    /**
     * Process Payment
     * 
     * @param userForm Form with User's details
     * @param result Binding Result for Validation or errors
     * @return ModelandView of next  travel location
     */
    //This code should not be used anymore. The same functionality is now in PersonalCloudController - /stripeConnect
    /*
     
    @RequestMapping(value = "/processpayment", method = RequestMethod.POST)
    public ModelAndView processPayment(
            @Valid @ModelAttribute("paymentInfo") PaymentForm paymentForm,
            HttpServletRequest request,
            BindingResult result) {
        
                
        logger.debug("Payment Processing");
        logger.debug("Processing Payment Data: {}", paymentForm.toString());
        
        ModelAndView mv = new ModelAndView("payment");


       
        boolean errors = false;
        
        String sessionIdentifier = regSession.getSessionId(); 
        String cloudName = regSession.getCloudName();
        String email = regSession.getVerifiedEmail();
        String phone = regSession.getVerifiedMobilePhone();
        String password = regSession.getPassword();
        
              
        //Check Session      
        if (sessionIdentifier == null || cloudName == null || email == null
                || phone == null || password == null) {
            errors = true;    
            mv.addObject("error", "Invalid Session"); 
        }
        

        
        // If Validation Has Succeeded.
        if (!errors) {
        
            //Process Payment           
            if (theManager.processPayment(paymentForm.getCardNumber(), paymentForm.getCvv(),
                    paymentForm.getExpMonth(), paymentForm.getExpYear()) != PaymentStatusCode.SUCCESS) {
                String errorStr = "Payment Processing Failed";
                logger.warn(errorStr + "for " + paymentForm.getCardNumber() );
                mv.addObject("paymentProcessingError", errorStr); 
                errors = true;
            }
            
        }
        
        if (!errors) {
            try {
                theManager.registerUser(CloudName.create(cloudName), phone,
                        email, password);
                
                //need a new unique response id
                String responseId = UUID.randomUUID().toString();
                
                //make entries in the invite_response table or giftcode_redemption table that a new cloud has been registered against an invite code and optionally a gif code
                if((regSession.getInviteCode() != null) && (regSession.getGiftCode() != null)){
                	//make a new record in the giftcode_redemption table
                	
                } else if (regSession.getInviteCode() != null){
                	//make a new record in the invite_response table
                	InviteResponseModel inviteResponse = new InviteResponseModel();
                	inviteResponse.setCloudNameCreated(cloudName);
                	inviteResponse.setInviteId(regSession.getInviteCode());
                	inviteResponse.setResponseId(responseId);
                	inviteResponse.setPaymentId(paymentForm.getPaymentId());
                	
                	InviteResponseDAO inviteResponseDAO = DAOFactory.getInstance().getInviteResponseDAO();
                	inviteResponseDAO.insert(inviteResponse);
                }
            } catch (Exception e) {
                logger.warn("Registration Error {}", e.getMessage());
                mv.addObject("error", e.getMessage());
                errors = true;
            }
        } 
                             
        if (!errors) {
            mv = new ModelAndView("cspdashboard"); 
            AccountDetailsForm accountForm = new AccountDetailsForm();
            accountForm.setCloudName(cloudName);
            mv.addObject("accountInfo", accountForm);   
            
            logger.debug("Sucessfully Registered {}", cloudName );
        }
             
        return mv;

    }
 */
}
