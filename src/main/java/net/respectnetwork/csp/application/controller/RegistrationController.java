package net.respectnetwork.csp.application.controller;

import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import net.respectnetwork.csp.application.exception.UserRegistrationException;
import net.respectnetwork.csp.application.form.AccountDetailsForm;
import net.respectnetwork.csp.application.form.ConfirmationForm;
import net.respectnetwork.csp.application.form.SignUpForm;
import net.respectnetwork.csp.application.manager.RegistrationManager;
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
        mv = new ModelAndView("signup");
        mv.addObject("signUpInfo", signUpForm);
        
        String cloudName = signUpForm.getCloudName();
        
        if (cloudName != null) {           
        // Start Check that the Cloud Number is Available.
            try {
                if (! theManager.isClouldNameAvailable(cloudName)) {
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
            
            //If the CloudName is Available
            if (errors == false) {
                try {
                    CloudNumber[] existingUsers= theManager.checkEmailAndMobilePhoneUniqueness(signUpForm.getMobilePhone(), signUpForm.getEmail());
                    if (existingUsers[0] != null) {
                       //Communicate back to Form phone is already taken
                        String errorStr = "Phone Number not Unique";
                        mv.addObject("phoneError", errorStr);
                        logger.debug("Phone {} already used by {}", signUpForm.getMobilePhone(), existingUsers[0]  );
                        errors = true;
                    }
                    if (existingUsers[1] != null) {
                        String errorStr = "Email not Unique";
                        mv.addObject("emailError", errorStr);
                        logger.debug("Phone {} already used by {}", signUpForm.getEmail(), existingUsers[1]  );
                        errors = true;
                     }
                } catch (UserRegistrationException e) {
                    String errorStr = "System Error checking Email/Phone Number Uniqueness";
                    logger.warn(errorStr + " : {}", e.getMessage());
                    mv.addObject("error", errorStr);
                    errors = true;
                }
                       
                
                
                if (!errors) {
                    String sessionId =  UUID.randomUUID().toString();
                    regSession.setSessionId(sessionId);
                           
                    // If all is okay send out the validation messages.
                    try {
                        theManager.sendValidationCodes(sessionId, signUpForm.getEmail(), signUpForm.getMobilePhone());
                    } catch (CSPValidationException e) {
                        String errorStr = "System Error sending validation messages";
                        logger.warn(errorStr + " : {}", e.getMessage());
                        mv.addObject("error", errorStr);    
                        errors = true;
                    }
                }
                
                if (!errors) {
                    mv = new ModelAndView("confirmation"); 
                    ConfirmationForm confirmationForm = new ConfirmationForm();
                    mv.addObject("confirmationInfo", confirmationForm);
                    
                    //Add CloudName/ Email and Phone to Session
                    
                    regSession.setCloudName(signUpForm.getCloudName());
                    regSession.setVerifiedEmail(signUpForm.getEmail());
                    regSession.setVerifiedMobilePhone(signUpForm.getMobilePhone());
                   
                }
            }
                                            
        }
        mv.addObject("signupInfo", signUpForm);
                                    
        return mv;        
    }
    
    
    /**
     * Validate Confirmation Codes, Process Terms and Conditions
     * Process Payment, Store Password and the register user Cloud.
     * 
     * @param userForm Form with User's details
     * @param result Binding Result for Validation or errors
     * @return ModelandView of next  travel location
     */
    @RequestMapping(value = "/processRegistration", method = RequestMethod.POST)
    public ModelAndView createAndValidateUser(
            @Valid @ModelAttribute("confirmationInfo") ConfirmationForm confirmationForm,
            HttpServletRequest request,
            BindingResult result) {
        
                
        logger.debug("Starting Creation/Validation Process");
        logger.debug("Processing Confirmation Data: {}", confirmationForm.toString());
        
        ModelAndView mv = new ModelAndView("confirmation");
        String sessionIdentifier = regSession.getSessionId(); 
       
        boolean errors = false;
        
        //Validate Codes
        if (!theManager.validateCodes(sessionIdentifier, confirmationForm.getEmailCode(), confirmationForm.getSmsCode())) {
            String errorStr = "Code(s) Validation Failed";
            logger.debug(errorStr);
            mv.addObject("codeValidationError", errorStr); 
            errors = true;
        }
           
        
        //Get CloudName/ Email and Phone fromSession
        
        String cloudName = regSession.getCloudName();
        String verifiedEmail = regSession.getVerifiedEmail();
        String verifiedPhone = regSession.getVerifiedMobilePhone();
        
        // If Validation Has Succeeded.
        if (!errors) {
        
            //Process Payment           
            if (theManager.processPayment(confirmationForm.getCardNumber(), confirmationForm.getCvv(),
                    confirmationForm.getExpMonth(), confirmationForm.getExpYear()) != PaymentStatusCode.SUCCESS) {
                String errorStr = "Payment Processing Failed";
                logger.warn(errorStr + "for " + confirmationForm.getCardNumber() );
                mv.addObject("paymentProcessingError", errorStr); 
                errors = true;
            }
            
           //Register Personal Cloud
           if (cloudName == null || verifiedEmail == null || verifiedPhone ==null ) {
                mv.addObject("error", "Error retrieving data from session"); 
                errors= true;
            } else {      
                try {
                    theManager.registerUser(CloudName.create(cloudName), verifiedPhone, verifiedEmail, confirmationForm.getPassword());
                } catch (Exception e) {
                    logger.warn("Registration Error {}", e.getMessage() );
                    mv.addObject("error", e.getMessage());
                    errors = true;
                }
            }
        }
                    
        if (!errors) {
            mv = new ModelAndView("accountInformation"); 
            AccountDetailsForm accountForm = new AccountDetailsForm();
            accountForm.setCloudName(cloudName);
            mv.addObject("accountInfo", accountForm);   
            
            logger.debug("Sucessfully Registered {}", cloudName );
        }
             
        return mv;

    }
     
    

    

}
