package net.respectnetwork.csp.application.manager;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Currency;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.respectnetwork.csp.application.exception.CSPException;
import net.respectnetwork.csp.application.exception.UserRegistrationException;
import net.respectnetwork.csp.application.model.AvailabilityResponse;
import net.respectnetwork.csp.application.util.EmailHelper;
import net.respectnetwork.sdk.csp.BasicCSPInformation;
import net.respectnetwork.sdk.csp.CSP;
import net.respectnetwork.sdk.csp.UserValidator;
import net.respectnetwork.sdk.csp.discount.NeustarRNDiscountCode;
import net.respectnetwork.sdk.csp.discount.RespectNetworkRNDiscountCode;
import net.respectnetwork.sdk.csp.exception.CSPRegistrationException;
import net.respectnetwork.sdk.csp.payment.PaymentProcessingException;
import net.respectnetwork.sdk.csp.payment.PaymentProcessor;
import net.respectnetwork.sdk.csp.payment.PaymentStatusCode;
import net.respectnetwork.sdk.csp.validation.CSPValidationException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Required;

import xdi2.client.exceptions.Xdi2ClientException;
import xdi2.core.xri3.CloudName;
import xdi2.core.xri3.CloudNumber;
import xdi2.core.xri3.XDI3Segment;
import xdi2.discovery.XDIDiscoveryClient;
import xdi2.discovery.XDIDiscoveryResult;

import com.ibm.icu.text.UTF16;

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
    public static  NeustarRNDiscountCode cloudNameDiscountCode = NeustarRNDiscountCode.OnePersonOneName;
    
    /**
     * Dependent Cloud Discount Code
     */
    public static NeustarRNDiscountCode depCloudNameDiscountCode = NeustarRNDiscountCode.OnePersonOneName;
    
    /** RN Discount Code */
    public static RespectNetworkRNDiscountCode respectNetworkMembershipDiscountCode = RespectNetworkRNDiscountCode.IIW17;
    
    /** Personal Cloud EndPoint */
    private String personalCloudEndPoint; 
    
    /** Debug Mode: Require Uniqueness in the eMail/SMS */
    private boolean requireUniqueness = true;
    
    /** Debug Mode: Send eMail/SMS as part of validation */
    private boolean sendMailAndSMS = true;
      
    /** Debug Mode: Require Codes to be validate to proceed with registration */
    private boolean validateCodes = true;
    
    /** Controls whether an invite code is required for registration or not */
    private boolean requireInviteCode = true;
    
    /** 
     * CSP phone
     */
    private String cspContactPhone;
    
    /** 
     * CSP email
     */
    private String cspContactEmail;
    
    private String cspHomeURL;
    
    private static String cspInviteURL;
    
    private String cspRespectConnectBaseURL;
    
    private static String latLongPostURL ;
    
    private static String postRegistrationURL ;
    
    private String cspTCURL;
    
    private String nameAvailabilityCheckURL;
    
    private String cspHomePage;
    
    private String contactSupportEmail;
    
    private String cspCloudName;
    
    public static final String GeoLocationPostURIKey = "<$https><#network.globe><$set>";
    public static final String RNpostRegistrationLandingPageURIKey = "<$https><#post><#registration>";
    public static final String CSPCloudRegistrationURIKey = "<$https><#registration>";
    public static final String CSPDependentCloudRegistrationURIKey = "<$https><#dependent><#registration>";
    public static final String CSPGiftCardPurchaseURIKey = "<$https><#giftcard><#registration>";
    public static final String CloudNameRegEx = "^=[a-z\\d]+((.|-)[a-z\\d]+)*$";
    public static final String validINameFormat = "Personal cloud names must start with an = sign and business cloud names with a + sign. After that, they may contain up to 64 characters in any supported character set, plus dots or dashes. They may not start or end with a dot or a dash nor contain consecutive dots or dashes. The supported character sets include Latin (which covers many European languages such as German, Swedish and Spanish), Chinese, Japanese, and Korean.";

    // don't forget to change client-side validation regex in userdetails.html
    public static final String phoneNumberRegEx = "^\\+[0-9]{1,3}\\.[0-9]{4,14}(?:x.+)?$"; // todo possibly send to client to guarantee in sync?

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
    public boolean isRequireUniqueness() {
        return requireUniqueness;
    }

    /**
     * @param runInTest the runInTest to set
     */
    public void setRequireUniqueness(boolean requireUniqueness) {
        this.requireUniqueness = requireUniqueness;
    }
    
    /**
     * @return the personalCloudEndPoint
     */
    public String getPersonalCloudEndPoint() {
        return personalCloudEndPoint;
    }

    /**
     * @param personalCloudEndPoint the personalCloudEndPoint to set
     */
    @Required
    public void setPersonalCloudEndPoint(String personalCloudEndPoint) {
        this.personalCloudEndPoint = personalCloudEndPoint;
    }

    /**
     * @return the sendMailAndSMS
     */
    public boolean isSendMailAndSMS() {
        return sendMailAndSMS;
    }

    /**
     * @param sendMailAndSMS the sendMailAndSMS to set
     */
    public void setSendMailAndSMS(boolean sendMailAndSMS) {
        this.sendMailAndSMS = sendMailAndSMS;
    }

    /**
     * @return the validateCodes
     */
    public boolean isValidateCodes() {
        return validateCodes;
    }

    /**
     * @param validateCodes the validateCodes to set
     */
    public void setValidateCodes(boolean validateCodes) {
        this.validateCodes = validateCodes;
    }

    /**
     * Constructor Used for Getting CSP Private Key
     */
    public RegistrationManager(CSP cspRegistrar) {
        
        this.cspRegistrar = cspRegistrar;
        
        BasicCSPInformation theCSPInfo = (BasicCSPInformation)cspRegistrar.getCspInformation();
        
        // Getting the CSP  Private Key  and Setting it in  BasicCSPInformation.cspSignaturePrivateKey
              
         try {
            theCSPInfo.retrieveCspSignaturePrivateKey();
            logger.debug("Private Key Algo. = {}", theCSPInfo.getCspSignaturePrivateKey().getAlgorithm() );
        } catch (Exception e) {
            logger.warn("Cannot get CSP Private Key", e.getMessage());
        }
        if ( theCSPInfo.getCspSignaturePrivateKey() != null) {
            logger.debug("CSP Private Key Found: Setting setRnCspSecretToken = null");
            theCSPInfo.setRnCspSecretToken(null);
        } 
        //set the registration endpoints
        //setEndpointURI(CSPCloudRegistrationURIKey, this.cspHomeURL + "/register");
        //setEndpointURI(CSPDependentCloudRegistrationURIKey, this.cspHomeURL + "/dependent");
        //setEndpointURI(CSPGiftCardPurchaseURIKey, this.cspHomeURL + "/invite");
        postRegistrationURL = getEndpointURI(RegistrationManager.RNpostRegistrationLandingPageURIKey, theCSPInfo.getRnCloudNumber());
        latLongPostURL = getEndpointURI(RegistrationManager.GeoLocationPostURIKey, theCSPInfo.getRnCloudNumber());
        
    }

    /**
     * Check CloudName Availability
     * 
     * @param cloudName
     * @return
     * @throws UserRegistrationException
     */
    public boolean isCloudNameAvailable(String cloudName)
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
            CloudNumber[] existingCloudNumbers = cspRegistrar.checkPhoneAndEmailAvailableInRN(mobilePhone, email); 
            if (!requireUniqueness) { 
                logger.warn("Overriding eMail/SMS uniqueness check.");
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
            if (validateCodes) {
                logger.debug("Validating codes: validateCodes = true ");
                validated = userValidator.validateCodes(sessionIdentifier, emailCode, smsCode);
            } else {
                logger.debug("Not Validating codes: validateCodes = false");
                validated = true;
            }
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
        if (sendMailAndSMS) {
            logger.debug("Sending Validation messages: sendMailAndSMS = true ");
            userValidator.sendValidationMessages(sessionId, email, mobilePhone);
        } else {
            logger.debug("Not sending Validation messages: sendMailAndSMS = false ");
        }
        
    }
    
    public CloudNumber registerUser(final CloudName cloudName, String verifiedPhone, final String verifiedEmail, String userPassword , NeustarRNDiscountCode cdc, String paymentType, String paymentRefId, final Locale locale) throws CSPRegistrationException, Xdi2ClientException {
        
        CloudNumber cloudNumber = CloudNumber.createRandom(cloudName.getCs());
        if(cloudNumber != null)
        {
           RegisterUserThread rut = new RegisterUserThread();
           rut.setCloudName(cloudName);
           rut.setCloudNumber(cloudNumber);
           rut.setCspRegistrar(cspRegistrar);
           rut.setRcBaseURL(this.getCspRespectConnectBaseURL());
           rut.setUserPassword(userPassword);
           rut.setVerifiedEmail(verifiedEmail);
           rut.setVerifiedPhone(verifiedPhone);
           rut.setCdc(cdc);
           rut.setPaymentType(paymentType);
           rut.setPaymentRefId(paymentRefId);
           rut.setLocale(locale);
           rut.setCspCloudName(this.cspCloudName);
           rut.setCspHomePage(this.cspHomePage);
           rut.setCspContactSupportEmail(this.contactSupportEmail);
           rut.setUserEmail(verifiedEmail);
           rut.setCspContactEmail(cspContactEmail);
           Thread t = new Thread(rut);
           t.start();
           
           /*
        // Step 1: Register Cloud with Cloud Number and Shared Secret
        
        String cspSecretToken = cspRegistrar.getCspInformation().getCspSecretToken();

        cspRegistrar.registerCloudInCSP(cloudNumber, cspSecretToken);

        // step 2: Set Cloud Services in Cloud

        Map<XDI3Segment, String> services = new HashMap<XDI3Segment, String> ();
        
        try {
            services.put(XDI3Segment.create("<$https><$connect><$xdi>"), this.getCspRespectConnectBaseURL() +
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
        if(cdc != null)
        {
           cspRegistrar.registerCloudNameInRN(cloudName, cloudNumber, verifiedPhone, verifiedEmail, cdc);
        } else
        {
           cspRegistrar.registerCloudNameInRN(cloudName, cloudNumber, verifiedPhone, verifiedEmail, cloudNameDiscountCode);
        }
        cspRegistrar.registerCloudNameInCSP(cloudName, cloudNumber);
        cspRegistrar.registerCloudNameInCloud(cloudName, cloudNumber, cspSecretToken);

        // step 6: Set phone number and e-mail address

        cspRegistrar.setPhoneAndEmailInCloud(cloudNumber, cspSecretToken, verifiedPhone, verifiedEmail);

        // step 7: Set RN/RF membership

        cspRegistrar.setRespectNetworkMembershipInRN(cloudNumber, new Date(), respectNetworkMembershipDiscountCode);
        cspRegistrar.setRespectFirstMembershipInRN(cloudNumber);
        
        // Step 8: Change Secret Token

        cspRegistrar.setCloudSecretTokenInCSP(cloudNumber, userPassword);
        
        //Step 9 : save the email and phone in local DB
        
        DAOFactory dao = DAOFactory.getInstance();
        SignupInfoModel signupInfo  = new SignupInfoModel();
        signupInfo.setCloudName(cloudName.toString());
        signupInfo.setEmail(verifiedEmail);
        signupInfo.setPhone(verifiedPhone);
        
        try
      {
         dao.getSignupInfoDAO().insert(signupInfo);
      } catch (DAOException e)
      {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }
      */
        }
        return cloudNumber;

    }

	public boolean isRequireInviteCode() {
		return requireInviteCode;
	}

	public void setRequireInviteCode(boolean requireInviteCode) {
		this.requireInviteCode = requireInviteCode;
	}
	
	public CloudNumber registerDependent(CloudName guardianCloudName , String guardianToken , CloudName dependentCloudName,  String dependentToken , String s_dependentBirthDate, String paymentType, String paymentRefId, String guardianEmailAddress, String guardianPhone, Locale locale){
				  CloudNumber depCloudNumber = CloudNumber.createRandom(dependentCloudName.getCs());
				  
				  RegisterDependentCloudThread rdct = new RegisterDependentCloudThread();
				  rdct.setCspRegistrar(cspRegistrar);
				  rdct.setDepCloudNumber(depCloudNumber);
				  rdct.setDependentCloudName(dependentCloudName);
				  rdct.setDependentToken(dependentToken);
				  rdct.setGuardianCloudName(guardianCloudName);
				  rdct.setGuardianToken(guardianToken);
				  rdct.setRcBaseURL(this.getCspRespectConnectBaseURL());
				  rdct.setS_dependentBirthDate(s_dependentBirthDate);
				  rdct.setPaymentType(paymentType);
				  rdct.setPaymentRefId(paymentRefId);
				  rdct.setGuardianEmail(guardianEmailAddress);
				  rdct.setGuardianPhone(guardianPhone);
				  rdct.setCspCloudName(this.cspCloudName);
				  rdct.setCspHomePage(this.cspHomePage);
				  rdct.setCspContactSupportEmail(this.contactSupportEmail);
				  rdct.setLocale(locale);
				  rdct.setCspContactEmail(cspContactEmail);
				  Thread t = new Thread(rdct);
				  t.start();
					
				/*
				// Common Data

				CloudNumber guardianCloudNumber = null;
		        CloudNumber dependentCloudNumber = null;
		        PrivateKey guardianPrivateKey = null;
		        PrivateKey dependentPrivateKey = null;
				boolean withConsent = true;
				
				Date dependentBirthDate = null;
				
				BasicCSPInformation cspInformation = (BasicCSPInformation)cspRegistrar.getCspInformation();
				
				// Resolve Cloud Numbers from Name
				
		        XDIDiscoveryClient discovery = cspInformation.getXdiDiscoveryClient();

		        try {
		            SimpleDateFormat format = 
		                    new SimpleDateFormat("MM/dd/yyyy");
		            dependentBirthDate = format.parse(s_dependentBirthDate);
		        } catch (ParseException e) {
	                logger.debug("Invalid Dependent BirthDate.");
	                return null;
		        }

				for (int tries = 0 ; tries < 5 ; tries++) {

					try {
						logger.debug("Waiting for five seconds to allow for the newly registered dependent name in discovery");
						Thread.sleep(5000);
					} catch (InterruptedException e1) {
						
					}
		        
			        try {
			            XDIDiscoveryResult guardianRegistry = discovery.discoverFromRegistry(
			                    XDI3Segment.create(guardianCloudName.toString()), null);
			            
			            XDIDiscoveryResult dependentRegistry = discovery.discoverFromRegistry(
			                    XDI3Segment.create(dependentCloudName.toString()), null);
			            
			            guardianCloudNumber = guardianRegistry.getCloudNumber();
			            dependentCloudNumber = dependentRegistry.getCloudNumber();
			            
			            String guardianXdiEndpoint = guardianRegistry.getXdiEndpointUri();
			            String dependentXdiEndpoint = dependentRegistry.getXdiEndpointUri();
	
			            guardianPrivateKey = XDIClientUtil.retrieveSignaturePrivateKey(guardianCloudNumber, guardianXdiEndpoint, guardianToken);
			            logger.debug("GuardianPrivateKey Algo: " + guardianPrivateKey.getAlgorithm());
	
			            dependentPrivateKey = XDIClientUtil.retrieveSignaturePrivateKey(dependentCloudNumber, dependentXdiEndpoint, dependentToken);
			            logger.debug("DependentPrivateKey Algo: " + dependentPrivateKey.getAlgorithm());
			            
			            if (guardianCloudNumber == null || dependentCloudNumber == null) {
			                logger.debug("Un-registered Cloud Name.");
			                continue;
			            }
			            break;
	
			        } catch (Xdi2ClientException e) {
			            logger.debug("Problem with Cloud Name Provided.");
			            e.printStackTrace();
			            logger.debug(e.getMessage());
			            continue;
			        } catch (GeneralSecurityException gse){
			        	logger.debug("Problem retrieving signatures.");
			        	gse.printStackTrace();
			        	logger.debug(gse.getMessage());
			            continue;
			        }
			        
				}
				if(guardianCloudNumber != null && dependentCloudNumber != null) {
			        try {
			            // Set User Cloud Data
			        	cspRegistrar.setGuardianshipInCloud(cspInformation, guardianCloudNumber, dependentCloudNumber, dependentBirthDate, withConsent, guardianToken, guardianPrivateKey);
			    		
			    		// Set CSP Cloud Data
			        	cspRegistrar.setGuardianshipInCSP(cspInformation, guardianCloudNumber, dependentCloudNumber, dependentBirthDate, withConsent, guardianPrivateKey);
			    	    
			    	    // Set MemberGraph Data
			        	cspRegistrar.setGuardianshipInRN(cspInformation, guardianCloudNumber, dependentCloudNumber, dependentBirthDate, withConsent, guardianPrivateKey);
			    	    
			        } catch (Xdi2ClientException e) {
			        	logger.debug("Xdi2ClientException: " + e.getMessage());
			            e.printStackTrace();
			        }
				}
				*/
		return depCloudNumber;
	}

	/**
     * @param phone to set
     */
    @Autowired
    @Qualifier("cspContactPhone")
    public void setCspContactPhone(String cspPhone) {
        this.cspContactPhone = cspPhone;
    }
    
    /**
     * @param email to set
     */
    @Autowired
    @Qualifier("cspContactEmail")
    public void setCspContactEmail(String cspEmail) {
        this.cspContactEmail = cspEmail;
    }
    
    public String getCSPContactInfo() {
    	
    	return "Please contact the CSP at " + this.cspContactPhone + " or via email at " + this.cspContactEmail;
    }

   public String getCspHomeURL()
   {
      return cspHomeURL;
   }

   public void setCspHomeURL(String cspHomeURL)
   {
      this.cspHomeURL = cspHomeURL;
   }

   public static String getCspInviteURL()
   {
      return cspInviteURL;
   }

   public static void setCspInviteURL(String url)
   {
      cspInviteURL = url;
   }
   public  String getEndpointURI(String key , CloudNumber cloudNumber)
   {
      
      logger.debug("getEndpointURI key=" + key + " , cloudNumber = " + cloudNumber.toString());
      BasicCSPInformation cspInformation = (BasicCSPInformation)cspRegistrar.getCspInformation();
      XDIDiscoveryClient discovery = cspInformation.getXdiDiscoveryClient();

      discovery.setAuthorityCache(null);
      try
      {
         XDI3Segment[] uriType = new XDI3Segment[1];
         uriType[0] = XDI3Segment.create(key);
         XDIDiscoveryResult discResult = discovery.discover(
               XDI3Segment.create(cloudNumber.toString()), uriType);
         //XDIDiscoveryResult discResult = discovery.discoverFromAuthority("https://mycloud-ote.neustar.biz/registry", cloudNumber, uriType);
         Map<XDI3Segment,String> endpointURIs = discResult.getEndpointUris();
         for (Map.Entry<XDI3Segment, String> epURI : endpointURIs.entrySet())
         {
            logger.debug("Looping ... Endpoint key = " + epURI.getKey().toString() + " ,value=" + epURI.getValue());
            if(epURI.getKey().toString().equals(key))
            {
               logger.debug("Found match for Endpoint key = " + key);
               return epURI.getValue();
            }
         }
         
      } catch (Xdi2ClientException e)
      {
         
         logger.debug("Error in getEndpointURI " + e.getMessage());
      }
      logger.debug("Did not find match for Endpoint key = " + key);
      return "";
      
   }
   
   public  boolean setEndpointURI(String key , String endpointURI ) 
   {
      BasicCSPInformation cspInformation = (BasicCSPInformation)cspRegistrar.getCspInformation();
      Map<XDI3Segment,String> endpointURIMap = new HashMap<XDI3Segment,String>();
      endpointURIMap.put(XDI3Segment.create(key), endpointURI);
      try
      {
         cspRegistrar.setCloudServicesForCSPInCSP(cspInformation.getCspCloudNumber(), cspInformation.getCspSecretToken(), cspInformation.getCspRegistryXdiEndpoint(), endpointURIMap);
      } catch (Xdi2ClientException e)
      {        
         e.printStackTrace();
         return false;
      }
      
      
      return true;
      
   }

   public String getCspRespectConnectBaseURL()
   {
      return cspRespectConnectBaseURL;
   }

   public void setCspRespectConnectBaseURL(String cspRespectConnectBaseURL)
   {
      this.cspRespectConnectBaseURL = cspRespectConnectBaseURL;
   }

   public static String getLatLongPostURL()
   {
      return latLongPostURL;
   }

   public static void setLatLongPostURL(String latLongPostURL)
   {
      RegistrationManager.latLongPostURL = latLongPostURL;
   }

   public static String getPostRegistrationURL()
   {
      return postRegistrationURL;
   }

   public static void setPostRegistrationURL(String postRegistrationURL)
   {
      RegistrationManager.postRegistrationURL = postRegistrationURL;
   }

   public String getCspTCURL()
   {
      return cspTCURL;
   }

   public void setCspTCURL(String cspTCURL)
   {
      this.cspTCURL = cspTCURL;
   }

   @Autowired
   @Qualifier("contactSupportEmail")
   public void setCspContactSupportEmail(String contactSupportEmail) {
       this.contactSupportEmail = contactSupportEmail;
   }

   @Autowired
   @Qualifier("cspCloudName")
   public void setCspCloudName(String cspCloudName) {
       this.cspCloudName = cspCloudName;
   }

   @Autowired
   @Qualifier("cspHomePage")
   public void setCspHomePage(String cspHomePage) {
       this.cspHomePage = cspHomePage;
   }

   public static boolean validateCloudName(String iname) {
      
         boolean retval = false;
         if (iname == null || iname.length() < 2)
            return false;
         if (iname.length() > 254)
         {
            
            return false;
         }
         int index=0;
         int c;
         if (!iname.startsWith("=") || !iname.startsWith("+"))
         {
            index=1;
            
            //only do this check if this is not a Global registry meaning that the GRS IName is not ! = or +.
            if (index > 1)
            {
               //length has to be greater than length of GRS I-Name plus "*" thus index+1
               if (iname.length() <= index+1)
               {
                  return false;
               }
               
               c = UTF16.charAt(iname, index);
               if  (c != '*')
               {
                  return false;
               }
               //take into account of the '*'
               index++;
            }
         }
         
         int next_c;
         for ( ; index < iname.length(); index += UTF16.getCharCount(c))
         {
            c = UTF16.charAt(iname, index);
            //cannot have "." or "-" be 2nd or last character
            if (index==1 || (index==iname.length()-1))
            {
               if (c == '.' || c == '-')
               {
                  return false;
               }
            }
            
            // Global I-Name or a Delegated I-Name MUST NOT contain two or more consecutive
            // dots (".", or hyphens ("-").
            
            if (c == '.' || c == '-')
            {
               next_c = UTF16.charAt(iname, index + 1);
               if (next_c == '.' || next_c == '-')
               {
                  return false;
               }
            }
            
            if (!isAlphaNumeric(c) && c != '-' && c != '.'
               && !(c >= 0xA0 && c <= 0xD7FF)
               && !(c >= 0xF900 && c <= 0xD7FF)
               && !(c >= 0xFDF0 && c <= 0xFFEF)
               && !(c >= 0x10000 && c <= 0x1FFFD)
               && !(c >= 0x20000 && c <= 0x2FFFD)
               && !(c >= 0x30000 && c <= 0x3FFFD)
               && !(c >= 0x40000 && c <= 0x4FFFD)
               && !(c >= 0x50000 && c <= 0x5FFFD)
               && !(c >= 0x60000 && c <= 0x6FFFD)
               && !(c >= 0x70000 && c <= 0x7FFFD)
               && !(c >= 0x80000 && c <= 0x8FFFD)
               && !(c >= 0x90000 && c <= 0x9FFFD)
               && !(c >= 0xA0000 && c <= 0xAFFFD)
               && !(c >= 0xB0000 && c <= 0xBFFFD)
               && !(c >= 0xC0000 && c <= 0xCFFFD)
               && !(c >= 0xD0000 && c <= 0xDFFFD)
               && !(c >= 0xE0000 && c <= 0xEFFFD)
            )
            {
               
               return false;
            }
         }
               
         return true;
   }
   public static boolean validatePhoneNumber(String phone)
   {
      if(phone == null || phone.isEmpty())
      {
         return false;
      }
      Pattern pattern = Pattern.compile(phoneNumberRegEx);
      Matcher matcher = pattern.matcher(phone);
      if (matcher.find()) {
          return true;
      } else {
          return false;
      }
   }
   /*
    * Check for Medium Password: Must be at least 8 characters, have at least 2 letters, 2 numbers and at least one special character, e.g. @, #, $ etc.
    */
   public static boolean validatePassword(String password)
   {
      if(password == null || password.isEmpty())
      {
         return false;
      }
      if(password.length() < 8)
      {
         return false;
      }
      int letterCount = 0;
      int digitCount = 0;
      int specialCharCount = 0;
      for(int i = 0 ; i < password.length() ; i++)
      {
         if(password.charAt(i) >= '0' && password.charAt(i) <= '9')
         {
            digitCount++;
         }
         if((password.charAt(i) >= 'A' && password.charAt(i) <= 'Z') || ((password.charAt(i) >= 'a' && password.charAt(i) <= 'z')))
         {
            letterCount++;
         }
         //!@#$%^&*()_+|~-=\‘{}[]:";’<>?,./

         if((password.charAt(i) ==  '!') ||
               (password.charAt(i) ==  '@') ||
               (password.charAt(i) ==  '#') ||
               (password.charAt(i) ==  '$') ||
               (password.charAt(i) ==  '%') ||
               (password.charAt(i) ==  '^') ||
               (password.charAt(i) ==  '*') ||
               (password.charAt(i) ==  '(') ||
               (password.charAt(i) ==  ')') ||
               (password.charAt(i) ==  '_') ||
               (password.charAt(i) ==  '~') ||
               (password.charAt(i) ==  '-') ||
               (password.charAt(i) ==  '=') ||
               (password.charAt(i) ==  '\\') ||
               (password.charAt(i) ==  '`') ||
               (password.charAt(i) ==  '{') ||
               (password.charAt(i) ==  '}') ||
               (password.charAt(i) ==  '[') ||
               (password.charAt(i) ==  ']') ||
               (password.charAt(i) ==  ':') ||
               (password.charAt(i) ==  '"') ||
               (password.charAt(i) ==  ';') ||
               (password.charAt(i) ==  '\'') ||               
               (password.charAt(i) ==  '<') ||
               (password.charAt(i) ==  '>') ||
               (password.charAt(i) ==  '?') ||
               //(password.charAt(i) ==  ',') || //treats "," as a delimiter !
               (password.charAt(i) ==  '.') ||
               (password.charAt(i) ==  '&') ||
               (password.charAt(i) ==  '/') 
               )
         {
            specialCharCount++;
         }
         
      }
      if(letterCount >= 2 && digitCount >= 2 && specialCharCount >= 1)
      {
         return true;
      }
      return false;
   }

   public static boolean isAlphaNumeric(int c)
   {
      boolean retval = false;
      if (c >= 48 && c <= 57)
      {
         //number
         retval = true;
      }
      else if (c >= 65 && c <= 90)
      {
         //uppercase letter
         retval = true;
      }
      else if (c >= 97 && c <= 122)
      {
         //lowercase letter
         retval = true;
      }
      
      return retval;
   }

   public String getNameAvailabilityCheckURL()
   {
      return nameAvailabilityCheckURL;
   }

   public void setNameAvailabilityCheckURL(String nameAvailabilityCheckURL)
   {
      this.nameAvailabilityCheckURL = nameAvailabilityCheckURL;
   }
   // TODO:
    // 1. We should write Rest client to make a call to AvailabilityAPI.
    // That way we can reuse it whereever it is required in application.
    // 2. Url to AvailabilityAPI needs to modify to cater both Plus and Equal
    /**
     * This method checks the availability of cloud name into the registry.
     * 
     * @param cloudName
     * @return
     * @throws Exception
     */
    public boolean isCloudNameAvailableInRegistry(String cloudName)
             throws CSPException {
        logger.info("Going to get cloud name availability from registry "
                + nameAvailabilityCheckURL);

        boolean available = false;
        CloseableHttpClient httpclient = HttpClients.createDefault();
        if (cloudName.startsWith("=")) {
            cloudName = cloudName.substring(cloudName.indexOf("=") + 1);
        }
        String url = nameAvailabilityCheckURL + "/api/availability/equals/"
                + cloudName;
        logger.debug("Final cloud name availability check URL : "
                + nameAvailabilityCheckURL);
        CloseableHttpResponse response = null;
        try {
            HttpGet httpGet = new HttpGet(url);
            response = httpclient.execute(httpGet);
            // TODO: in case Http status code is not 200, what we should do.
            // Right now returning false in case status not OK
            if (HttpStatus.SC_OK == response.getStatusLine().getStatusCode()) {
                HttpEntity entity = response.getEntity();
                byte[] bytes = EntityUtils.toByteArray(entity);
                if (bytes.length == 0) {
                    return available;
                }
                ObjectMapper objectMapper = new ObjectMapper();
                AvailabilityResponse availabilityResponse = objectMapper
                        .readValue(bytes, AvailabilityResponse.class);
                available = availabilityResponse.isAvailable();
                logger.info("Is cloud name {} available: " + available,
                        cloudName);
            }
        } catch (ClientProtocolException e) {
            logger.error("Error while checking cloud name.", e);
            throw new CSPException("System error while checking cloud name.");
        } catch (IOException e) {
            logger.error("Error while checking cloud name.", e);
            throw new CSPException("System error while checking cloud name.");
        } finally {
            try {
            if (response != null) {
                response.close();
            }
            httpclient.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return available;
    }
}
