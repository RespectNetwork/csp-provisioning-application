package net.respectnetwork.csp.application.manager;

import java.security.GeneralSecurityException;
import java.security.PrivateKey;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.respectnetwork.sdk.csp.BasicCSPInformation;
import net.respectnetwork.sdk.csp.CSP;
import net.respectnetwork.sdk.csp.CSPInformation;
import xdi2.client.XDIClient;
import xdi2.client.exceptions.Xdi2ClientException;
import xdi2.client.http.XDIHttpClient;
import xdi2.client.util.XDIClientUtil;
import xdi2.core.xri3.CloudName;
import xdi2.core.xri3.CloudNumber;
import xdi2.core.xri3.XDI3Segment;
import xdi2.core.xri3.XDI3Statement;
import xdi2.discovery.XDIDiscoveryClient;
import xdi2.discovery.XDIDiscoveryResult;
import xdi2.messaging.Message;
import xdi2.messaging.MessageCollection;
import xdi2.messaging.MessageEnvelope;
import xdi2.messaging.error.ErrorMessageResult;

public class RegisterDependentCloudThread implements Runnable
{

   /** Class Logger */
   private static final Logger logger = LoggerFactory
           .getLogger(RegisterDependentCloudThread.class);
   
   private CloudName dependentCloudName = null;
   private CloudNumber depCloudNumber = null;
   private CSP                                  cspRegistrar                         = null;
   private String                               rcBaseURL                            = "";
   private String dependentToken = "";
   private String s_dependentBirthDate = "";
   private CloudName guardianCloudName = null;
   private String guardianToken = "";
   private String paymentType = "";
   private String paymentRefId = "";
   private String guardianEmail = "";
   private String guardianPhone = "";
   private String cspHomePage = "";
   private String contactSupportEmail = "";
   private String cspCloudName = "";
   private Locale locale = null;
   private String cspContactEmail = "";
   
   @Override
   public void run()
   {
      CloudNumber depCloudNumber = CloudNumber.createRandom(dependentCloudName.getCs());
      RegisterUserThread rut = new RegisterUserThread();
      rut.setCloudName(dependentCloudName);
      rut.setCloudNumber(depCloudNumber);
      rut.setCspRegistrar(cspRegistrar);
      rut.setRcBaseURL(rcBaseURL);
      rut.setUserPassword(dependentToken);
      rut.setVerifiedEmail(" ");
      rut.setVerifiedPhone(" ");
      rut.setPaymentType(paymentType);
      rut.setPaymentRefId(paymentRefId);
      rut.setCdc(RegistrationManager.depCloudNameDiscountCode);
      rut.setUserEmail(guardianEmail);
      rut.setUserPhone(guardianPhone);
      rut.setCspCloudName(cspCloudName);
      rut.setCspHomePage(cspHomePage);
      rut.setCspContactSupportEmail(contactSupportEmail);
      rut.setLocale(locale);
      rut.setCspContactEmail(cspContactEmail);
      Thread t = new Thread(rut);
      t.start();
      try
       {
          t.join();
       } catch (InterruptedException e2)
       {
          // TODO Auto-generated catch block
          e2.printStackTrace();
       }
      logger.debug("Dependent cloudname registered...");  
       
    
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
           
      }
      
      discovery.setAuthorityCache(null);
      discovery.setRegistryCache(null);

    for (int tries = 0 ; tries < 10 ; tries++) {

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
             if(dependentRegistry == null || dependentRegistry.getCloudNumber() == null)
             {
                logger.debug("Dependent name is not in discovery yet. So going back to check again ...");
                continue;
             }
             
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
          cspRegistrar.setGuardianshipInCloud(cspInformation, guardianCloudNumber, dependentCloudNumber, dependentBirthDate, withConsent, guardianToken, guardianPrivateKey, dependentToken);
          
          // Set CSP Cloud Data
          cspRegistrar.setGuardianshipInCSP(cspInformation, guardianCloudNumber, dependentCloudNumber, dependentBirthDate, withConsent, guardianPrivateKey);
           
           // Set MemberGraph Data
          cspRegistrar.setGuardianshipInRN(cspInformation, guardianCloudNumber, dependentCloudNumber, dependentBirthDate, withConsent, guardianPrivateKey);
          
          
           
         } catch (Xdi2ClientException e) {
          logger.debug("Xdi2ClientException: " + e.getMessage());
             e.printStackTrace();
         }
    }


   }

   public CloudName getDependentCloudName()
   {
      return dependentCloudName;
   }

   public void setDependentCloudName(CloudName dependentCloudName)
   {
      this.dependentCloudName = dependentCloudName;
   }

   public CloudNumber getDepCloudNumber()
   {
      return depCloudNumber;
   }

   public void setDepCloudNumber(CloudNumber depCloudNumber)
   {
      this.depCloudNumber = depCloudNumber;
   }

   public CSP getCspRegistrar()
   {
      return cspRegistrar;
   }

   public void setCspRegistrar(CSP cspRegistrar)
   {
      this.cspRegistrar = cspRegistrar;
   }

   public String getRcBaseURL()
   {
      return rcBaseURL;
   }

   public void setRcBaseURL(String rcBaseURL)
   {
      this.rcBaseURL = rcBaseURL;
   }

   public String getDependentToken()
   {
      return dependentToken;
   }

   public void setDependentToken(String dependentToken)
   {
      this.dependentToken = dependentToken;
   }

   public String getS_dependentBirthDate()
   {
      return s_dependentBirthDate;
   }

   public void setS_dependentBirthDate(String s_dependentBirthDate)
   {
      this.s_dependentBirthDate = s_dependentBirthDate;
   }

   public CloudName getGuardianCloudName()
   {
      return guardianCloudName;
   }

   public void setGuardianCloudName(CloudName guardianCloudName)
   {
      this.guardianCloudName = guardianCloudName;
   }

   public String getGuardianToken()
   {
      return guardianToken;
   }

   public void setGuardianToken(String guardianToken)
   {
      this.guardianToken = guardianToken;
   }

   /**
    * Method to get payment type.
    *
    * @return paymentType. It could be either giftCode, creditCard or
    *         promoCode.
  */
  public String getPaymentType()
  {
      return this.paymentType;
  }

  /**
   * Method to set payment type.
   *
   * @param paymentType. It could be either of giftCode, creditCard or
   *         promoCode.
  */
  public void setPaymentType( String paymentType )
  {
      this.paymentType = paymentType;
  }

  /**
   * Method to get payment reference id.
   *
   * @return paymemtRefId. It could be either giftcode_id or promo_id or
   *          payment_id.
   */
  public String getPaymentRefId()
  {
      return this.paymentRefId;
  }

  /**
   * Method to set payment reference id.
   *
   * @param paymentRefId. It could be either either giftcode_id or promo_id or
   *          payment_id.
  */
  public void setPaymentRefId( String paymentRefId )
  {
      this.paymentRefId = paymentRefId;
  }

  /**
   * Method to set the guardian's email address. This detail is required in the email
   * sent in case of dependent cloudname registration failure.
   *
   * @return emailAddress.
   */
  public String getGuardianEmail() {
      return this.guardianEmail;
  }

  /**
   * Set guardian's email address.
   * @param email guardian's email address.
   */
  public void setGuardianEmail(String email) {
      this.guardianEmail = email;
  }

  /**
   * Method to set the guardian's phone number. This detail is required in the email
   * sent in case of dependent cloudname registration failure.
   *
   * @return guardianPhone.
   */
  public String getGuardianPhone() {
      return this.guardianPhone;
  }

  /**
   * Set guardian's phone number.
   * @param phoneNum guardian's phone number.
   */
  public void setGuardianPhone(String guardianPhone) {
      this.guardianPhone = guardianPhone;
  }

  /**
   * Set contact support email address.
   * @param contactSupportEmail, contact support email address.
   */
  public void setCspContactSupportEmail(String contactSupportEmail) {
      this.contactSupportEmail = contactSupportEmail;
  }

  /**
   * Method to get the contact support's email address. It is required to send
   * an email in case of dependent cloudname registration failure.
   * @return email, contact support email address.
   */
  public String getCspContactSupportEmail()
  {
     return this.contactSupportEmail;
  }

  /**
   * Set csp cloudname.
   * @param cspCloudName
   */
  public void setCspCloudName(String cspCloudName) {
      this.cspCloudName = cspCloudName;
  }

  /**
   * Method to get csp cloud name.
   * @return csp cloud name
   */
  public String getCspCloudName()
  {
     return this.cspCloudName;
  }

  /**
   * Method to set csp home page. It will represent csp url.
   * @param cspHomePage csp url.
   */
  public void setCspHomePage(String cspHomePage) {
      this.cspHomePage = cspHomePage;
  }

  /**
   * Method to get the cspHomePage. It will return the configured csp url.
   * @return cspHomePage
   */
  public String getCspHomePage()
  {
     return this.cspHomePage;
  }

  public void setLocale(Locale locale) {
      this.locale = locale;
  }

  public Locale getLocale()
  {
     return this.locale;
  }

  public void setCspContactEmail(String cspContactEmail) {
      this.cspContactEmail = cspContactEmail;
  }

  public String getCspContactEmail()
  {
     return this.cspContactEmail;
  }
}
