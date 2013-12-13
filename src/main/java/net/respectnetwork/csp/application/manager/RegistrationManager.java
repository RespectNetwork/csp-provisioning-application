package net.respectnetwork.csp.application.manager;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import net.respectnetwork.csp.application.exception.UserRegistrationException;
import net.respectnetwork.csp.application.form.UserForm;
import net.respectnetwork.sdk.csp.CSP;
import net.respectnetwork.sdk.csp.exception.CSPRegistrationException;
import net.respectnetwork.sdk.csp.exception.CSPValidationException;
import net.respectnetwork.sdk.csp.model.UserProfile;
import net.respectnetwork.sdk.csp.model.CSPUserCredential;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import xdi2.client.exceptions.Xdi2ClientException;
import xdi2.core.xri3.CloudName;
import xdi2.core.xri3.CloudNumber;

public class RegistrationManager {
    
    /** Class Logger */
    private static final Logger logger = LoggerFactory
            .getLogger(RegistrationManager.class);
    
    /**
     * Class for Registering User in the Respect Network
     */
    private CSP cspRegistrar;
    
    /**
     * Get CSP Registrar
     * @return 
     */
    public CSP getCspRegistrar() {
        return cspRegistrar;
    }

    /**
     * Set CSP Registrar
     * @param cspRegistrar
     */
    @Required
    public void setCspRegistrar(CSP cspRegistrar) {
        this.cspRegistrar = cspRegistrar;
    }
    
    
       
    /**
     * Section 2.1.1
     * https://wiki.respectnetwork.net/wiki/Alice_Signs_Up
     * 
     * @return CloudNumber
     */
    public CSPUserCredential startSignUpProcess()
            throws CSPRegistrationException {
        
        logger.debug("Starting SignUp Process");                    
        CSPUserCredential cspCredential = cspRegistrar.signUpNewUser(); 
        return cspCredential;
            
    }
    
    
    /**
     * 
     * Section 3.1.1
     */
    public void createAndValidateUser(String cloudNumber, UserForm theUser, String secretToken)
        throws CSPValidationException{
           
           UserProfile theCSPUser = new UserProfile();
           theCSPUser.setFirstName(theUser.getName());
           theCSPUser.setNickName(theUser.getNickName());
           
           theCSPUser.setEmail(theUser.getEmail());
           theCSPUser.setPhone(theUser.getPhone());
           
           theCSPUser.setStreet(theUser.getStreet());
           theCSPUser.setCity(theUser.getCity());
           theCSPUser.setState(theUser.getState());
           theCSPUser.setPostalcode(theUser.getPostalcode());
           
           cspRegistrar.setUpAndValidateUserProfileInCloud(CloudNumber.create(cloudNumber), theCSPUser, secretToken);
    }
    
    
    /**
     * Section 4.1.1
     */
    public boolean validateCodes(String cloudNumber, String emailCode, String smsCode, String secretToken) {
        
        boolean validated = false;
        try { 
                     
           validated = cspRegistrar.validateCodes(CloudNumber.create(cloudNumber), emailCode, smsCode, secretToken);
           
           //4.1.1.1
           if (validated) {
               // Update EMAIL and Phone Number in CloudGraph with validation Information
               // e.g. <=me><+email><+validation>...
              
           }
            
        } catch (CSPValidationException e) {
            logger.warn("Problem Validating SMS and/or Email Codes");
            validated = false;
        }
        
        return validated;
    }

    /**
     * Register a new User.
     * 
     * Section 5.1.1
     * 
     * @param cloudNumber
     * @param cloudName
     * @param secretToken
     */
    public void registerNewUser(String cloudNumber, String theCloudName,
            String secretToken) throws UserRegistrationException {

        try {
            
            CloudName cloudName = CloudName.create(theCloudName);
            
            //@TODO: Will we use this instead.?
           //cspRegistrar.registerUserCloud(cloudNumber, cloudName, secretToken);
            

            
            try {
                if (cspRegistrar.checkCloudNameAvailableInRN(cloudName) != null){
                    throw new UserRegistrationException("Clould Name " + cloudName.toString() + " not available");
                }
            } catch (Xdi2ClientException e) {
                String error = "Problem checking Clould Number Avaliability: {} " +  e.getMessage();
                throw new UserRegistrationException(error);
            }
            // 5.1.1.1 Write Password to CSP Graph.
            cspRegistrar.setCloudSecretTokenInCSP(
                    CloudNumber.create(cloudNumber), secretToken);

            // 5.1.1.3
            

            
            //@TODO: This assumes uniqueness Check has already  been done
            // To Be implemented
            String email = ""; //Get Email from Cloud Graph;
            String phone = ""; //Get Phone from Cloud Graph;
            
            //cspRegistrar.setVerifiedContactInformationInRN(CloudNumber.create(cloudNumber), email, phone);
            
            cspRegistrar.registerCloudNameInRN(cloudName,
                    CloudNumber.create(cloudNumber));

            // Create XDI EndPoint (for use in registry)
            String cloudXdiEndpoint = null;
            String cloudXdiBase = cspRegistrar.getCspInformation()
                    .getCspCloudBaseXdiEndpoint();

            cloudXdiEndpoint = cloudXdiBase
                    + URLEncoder.encode(cloudNumber, "UTF-8");
            
            logger.debug("Creating cloudXdiEndpoint : {}", cloudXdiEndpoint);

            // No need to  do  this
            //cspRegistrar.setCloudXdiEndpointInRN(
              //      CloudNumber.create(cloudNumber), cloudXdiEndpoint);

            // 5.1.1.5 ( User Graph )
            cspRegistrar.registerCloudNameInCloud(cloudName,
                    CloudNumber.create(cloudNumber), secretToken);

            // 5.1.1.7
            cspRegistrar.registerCloudNameInCSP(cloudName,
                    CloudNumber.create(cloudNumber));

        } catch (Xdi2ClientException e) {
            String error = "Problem Completing User Registration"
                    + e.getMessage();
            logger.warn(error);
            throw new UserRegistrationException(error);

        } catch (UnsupportedEncodingException e) {
            String error = "Problem encoding cloudXdiEndpoint";
            logger.warn(error);
            throw new UserRegistrationException(error);

        }

    }
}
