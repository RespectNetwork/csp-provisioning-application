package net.respectnetwork.csp.application.manager;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.util.Currency;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import net.respectnetwork.csp.application.exception.UserRegistrationException;
import net.respectnetwork.sdk.csp.CSP;
import net.respectnetwork.sdk.csp.UserValidator;
import net.respectnetwork.sdk.csp.discount.CloudNameDiscountCode;
import net.respectnetwork.sdk.csp.discount.RespectNetworkMembershipDiscountCode;
import net.respectnetwork.sdk.csp.exception.CSPRegistrationException;
import net.respectnetwork.sdk.csp.payment.PaymentProcessingException;
import net.respectnetwork.sdk.csp.payment.PaymentProcessor;
import net.respectnetwork.sdk.csp.payment.PaymentStatusCode;
import net.respectnetwork.sdk.csp.validation.CSPValidationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import xdi2.client.exceptions.Xdi2ClientException;
import xdi2.core.xri3.CloudName;
import xdi2.core.xri3.CloudNumber;
import xdi2.core.xri3.XDI3Segment;

public class RegistrationManager {
    
    /** Class Logger */
    private static final Logger logger = LoggerFactory
            .getLogger(RegistrationManager.class);
    
    /**
     * Class for Registering User in the Respect Network
     */
    private CSP cspRegistrar;
    
    /**
     * Used for validating User details.
     */
    private UserValidator userValidator;
    
    /**
     * Used for processingPayment.
     */
    private PaymentProcessor paymentProcesser;
    
    /**
     * Charge for Registration
     */
    private String registrationAmount; 
    
    /**
     * Registration Currency
     */
    private String registrationCurrencyCode;
    
    /**
     * Registration Discount Code
     */
    private CloudNameDiscountCode cloudNameDiscountCode = CloudNameDiscountCode.OnePersonOneName;;
    
    /** RN Discount Code */
    private  RespectNetworkMembershipDiscountCode respectNetworkMembershipDiscountCode = RespectNetworkMembershipDiscountCode.IIW17;
    
    /** Personal Cloud EndPoint */
    private String personalCloudEndPoint  = "http://mycloud-ote.neustar.biz:8085/personalclouds/"; 
    
    /** Debug Mode */
    private boolean runInTest = false;
    
    /**
    
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
     * Get User Validaor
     * @return
     */
    public UserValidator getUserValidator() {
        return userValidator;
    }

    /**
     * Set User Validator
     * @param userValidator
     */
    public void setUserValidator(UserValidator userValidator) {
        this.userValidator = userValidator;
    }

    /**
     * @return the paymentProcesser
     */
    public PaymentProcessor getPaymentProcesser() {
        return paymentProcesser;
    }

    /**
     * @param paymentProcesser the paymentProcesser to set
     */
    public void setPaymentProcesser(PaymentProcessor paymentProcesser) {
        this.paymentProcesser = paymentProcesser;
    }

    /**
     * @return the registrationAmount
     */
    public String getRegistrationAmount() {
        return registrationAmount;
    }

    /**
     * @param registrationAmount the registrationAmount to set
     */
    public void setRegistrationAmount(String registrationAmount) {
        this.registrationAmount = registrationAmount;
    }

    /**
     * @return the registrationCurrencyCode
     */
    public String getRegistrationCurrencyCode() {
        return registrationCurrencyCode;
    }

    /**
     * @param registrationCurrencyCode the registrationCurrencyCode to set
     */
    public void setRegistrationCurrencyCode(String registrationCurrencyCode) {
        this.registrationCurrencyCode = registrationCurrencyCode;
    }


    

    

    /**
     * @return the runInTest
     */
    public boolean isRunInTest() {
        return runInTest;
    }

    /**
     * @param runInTest the runInTest to set
     */
    public void setRunInTest(boolean runInTest) {
        this.runInTest = runInTest;
    }

    /**
     * Check CloudName Availability
     * 
     * @param cloudName
     * @return
     * @throws UserRegistrationException
     */
    public boolean isClouldNameAvailable(String cloudName)
        throws UserRegistrationException {
        
        boolean availability = false;
        
        try {
            if (cspRegistrar.checkCloudNameAvailableInRN(CloudName.create(cloudName)) == null) {
                availability = true;
            }
        } catch (Exception e) {
            String error = "Problem checking Clould Number Avaliability: {} " +  e.getMessage();
            logger.warn(error);
            throw new UserRegistrationException(error);
        }
        
        return availability;
    }
    
    /**
     * Check Email and Mobile Phone Number Uniqueness
     * 
     * @param email
     * @param mobilePhone
     * @return
     * @throws UserRegistrationException
     */
    public CloudNumber[] checkEmailAndMobilePhoneUniqueness(String email, String mobilePhone)
        throws UserRegistrationException {
              
        try {
            CloudNumber[] existingCloudNumbers = cspRegistrar.checkPhoneAndEmailAvailableInRN(email, mobilePhone); 
            if (runInTest) { 
                logger.debug("Overriding uniqueness check.");
                existingCloudNumbers = new CloudNumber[2];
            }
            return existingCloudNumbers;

        } catch (Xdi2ClientException e) {
            logger.debug("Problem checking Phone/eMail Uniqueness");
            throw new UserRegistrationException("Problem checking Phone/eMail Uniqueness: " + e.getMessage());
        }    

    }
    
    /**
     * Validate Confirmation Codes
     * 
     * @param cloudNumber
     * @param emailCode
     * @param smsCode
     * @return
     */
    public boolean validateCodes(String sessionIdentifier, String emailCode, String smsCode) {
        
        boolean validated = false;
        try { 
                     
           validated = userValidator.validateCodes(sessionIdentifier, emailCode, smsCode);
                   
        } catch (CSPValidationException e) {
            logger.warn("Problem Validating SMS and/or Email Codes: {}", e.getMessage());
            validated = false;
        }
        
        return validated;
    }
    
    /**
     * Process Payment
     * 
     * @param cardNumber
     * @param cvv
     * @param expMonth
     * @param expYear
     * @param amount
     * @param currency
     * @return
     */
    public PaymentStatusCode processPayment(String cardNumber, String cvv, String expMonth, String expYear) {
        
       
        BigDecimal amount = new BigDecimal(registrationAmount);
        Currency currency = Currency.getInstance(registrationCurrencyCode);
        
        PaymentStatusCode returnCode = null;
        try {
            returnCode = paymentProcesser.processPayment(cardNumber, cvv, expMonth, expYear, amount, currency);
        } catch (PaymentProcessingException e) {
            returnCode = PaymentStatusCode.FAILURE;
        }
        return returnCode;
    }
    
    
    /**
     * Send Validation Codes
     * 
     * @param sessionId
     * @param email
     * @param mobilePhone
     * @throws CSPValidationException
     */
    public void sendValidationCodes(String sessionId, String email, String mobilePhone)
        throws CSPValidationException{
        
        userValidator.sendValidationMessages(sessionId, email, mobilePhone);
        
    }
    
    public CloudNumber registerUser(CloudName cloudName, String verifiedPhone, String verifiedEmail, String userPassword) throws CSPRegistrationException, Xdi2ClientException {
        
        
        CloudNumber cloudNumber = CloudNumber.createRandom(cloudName.getCs());
        

        // Unset for Message Signing.
        //cspInformation.retrieveCspSignaturePrivateKey();
        //cspInformation.setRnCspSecretToken(null);


        // Step 1: Register Cloud with Cloud Number and Shared Secret
        
        String cspSecretToken = cspRegistrar.getCspInformation().getCspSecretToken();

        cspRegistrar.registerCloudInCSP(cloudNumber, cspSecretToken);

        // step 2: Set Cloud Services in Cloud

        Map<XDI3Segment, String> services = new HashMap<XDI3Segment, String> ();
        
        try {
            services.put(XDI3Segment.create("<$https><$connect><$xdi>"), personalCloudEndPoint +
                URLEncoder.encode(cloudNumber.toString(), "UTF-8") + "/connect/request");
        } catch (UnsupportedEncodingException e) {
            throw new CSPRegistrationException(e);
        }
        
        cspRegistrar.setCloudServicesInCloud(cloudNumber, cspSecretToken, services);

        // step 3: Check if the Cloud Name is available

        CloudNumber existingCloudNumber = cspRegistrar.checkCloudNameAvailableInRN(cloudName);

        if (existingCloudNumber != null) {
            throw new CSPRegistrationException("Cloud Name " + cloudName + " is already registered with Cloud Number " + existingCloudNumber + ".");
        }

       
        // step 5: Register Cloud Name

        cspRegistrar.registerCloudNameInRN(cloudName, cloudNumber, verifiedPhone, verifiedEmail, cloudNameDiscountCode);
        cspRegistrar.registerCloudNameInCSP(cloudName, cloudNumber);
        cspRegistrar.registerCloudNameInCloud(cloudName, cloudNumber, cspSecretToken);

        // step 6: Set phone number and e-mail address

        cspRegistrar.setPhoneAndEmailInCloud(cloudNumber, cspSecretToken, verifiedPhone, verifiedEmail);

        // step 7: Set RN/RF membership

        cspRegistrar.setRespectNetworkMembershipInRN(cloudNumber, new Date(), respectNetworkMembershipDiscountCode);
        cspRegistrar.setRespectFirstMembershipInRN(cloudNumber);
        
        // Step 8: Change Secret Token

        cspRegistrar.setCloudSecretTokenInCSP(cloudNumber, userPassword);
        
        return cloudNumber;

    }

}
