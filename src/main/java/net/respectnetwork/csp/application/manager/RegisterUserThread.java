package net.respectnetwork.csp.application.manager;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import net.respectnetwork.csp.application.dao.DAOException;
import net.respectnetwork.csp.application.dao.DAOFactory;
import net.respectnetwork.csp.application.model.SignupInfoModel;
import net.respectnetwork.csp.application.util.EmailHelper;
import net.respectnetwork.sdk.csp.CSP;
import net.respectnetwork.sdk.csp.discount.NeustarRNDiscountCode;
import net.respectnetwork.sdk.csp.exception.CSPRegistrationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xdi2.client.exceptions.Xdi2ClientException;
import xdi2.core.xri3.CloudName;
import xdi2.core.xri3.CloudNumber;
import xdi2.core.xri3.XDI3Segment;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

public class RegisterUserThread implements Runnable
{   
   /** Class Logger */
   private static final Logger logger = LoggerFactory
           .getLogger(RegisterUserThread.class);
   
   private CloudNumber                          cloudNumber                          = null;
   private CSP                                  cspRegistrar                         = null;
   private String                               rcBaseURL                            = "";
   private CloudName                            cloudName                            = null;
   private String                               verifiedPhone                        = "";
   private String                               verifiedEmail                        = "";
   private String                               userPassword                         = "";
   private String                               paymentType                          = "";
   private String                               paymentRefId                         = "";

   private NeustarRNDiscountCode                cdc                                  = null;
   private String                               userEmail                            = null;
   private String                               userPhone                            = null;
   private String                               contactSupportEmail                  = null;
   private Locale                               locale                               = null;
   private String                               cspCloudName                         = null;
   private String                               cspHomePage                          = null;
   private String                               cspContactEmail                      = null;
   @Override
   public void run()
   {
      SignupInfoModel signupInfo = new SignupInfoModel();
      signupInfo.setCloudName(cloudName.toString());
      signupInfo.setEmail(verifiedEmail);
      signupInfo.setPhone(verifiedPhone);
      signupInfo.setPaymentType(paymentType);
      signupInfo.setPaymentRefId(paymentRefId);
      int retryCount = 0;
      EmailHelper emailHelper = new EmailHelper();
      cspCloudName = this.getCspCloudName();
      cspHomePage = this.getCspHomePage();
      contactSupportEmail = this.getCspContactSupportEmail();
      userEmail = this.getUserEmail();
      if(verifiedPhone != " ") {
          userPhone = verifiedPhone;
      } else {
          userPhone = this.getUserPhone();
      }
      cspContactEmail = this.getCspContactEmail();
      // 5 times retry registration in case of failure. Retry interval is 2 minutes.
      while (retryCount < 5) {
      try
      {
         // Step 1: Register Cloud with Cloud Number and Shared Secret

         String cspSecretToken = cspRegistrar.getCspInformation()
               .getCspSecretToken();

         cspRegistrar.registerCloudInCSP(cloudNumber, cspSecretToken);

         // step 2: Set Cloud Services in Cloud

         Map<XDI3Segment, String> services = new HashMap<XDI3Segment, String>();

         try
         {
            services.put(
                  XDI3Segment.create("<$https><$connect><$xdi>"),
                  rcBaseURL
                        + URLEncoder.encode(cloudNumber.toString(), "UTF-8")
                        + "/connect/request");
         } catch (UnsupportedEncodingException e)
         {
            throw new CSPRegistrationException(e);
         }

         cspRegistrar.setCloudServicesInCloud(cloudNumber, cspSecretToken,
               services);

         // step 3: Check if the Cloud Name is available

         CloudNumber existingCloudNumber = cspRegistrar
               .checkCloudNameInRN(cloudName);

         if (existingCloudNumber != null)
         {
            throw new CSPRegistrationException("Cloud Name " + cloudName
                  + " is already registered with Cloud Number "
                  + existingCloudNumber + ".");
         }

         // step 5: Register Cloud Name
         if (cdc != null)
         {
            cspRegistrar.registerCloudNameInRN(cloudName, cloudNumber,
                  verifiedPhone, verifiedEmail, cdc);
         } else
         {
            cspRegistrar.registerCloudNameInRN(cloudName, cloudNumber,
                  verifiedPhone, verifiedEmail, RegistrationManager.cloudNameDiscountCode);
         }
         cspRegistrar.registerCloudNameInCSP(cloudName, cloudNumber);
         cspRegistrar.registerCloudNameInCloud(cloudName, cloudNumber,
               cspSecretToken);

         // step 6: Set phone number and e-mail address

         cspRegistrar.setPhoneAndEmailInCloud(cloudNumber, cspSecretToken,
               verifiedPhone, verifiedEmail);

         // step 7: Set RN/RF membership

         cspRegistrar.setRespectNetworkMembershipInRN(cloudNumber, new Date(),
               null);
//         if(cdc != null && cdc.equals(NeustarRNDiscountCode.OnePersonOneName))
//         {
//            cspRegistrar.setRespectFirstMembershipInRN(cloudNumber);
//         }
         // Step 8: Change Secret Token

         cspRegistrar.setCloudSecretTokenInCSP(cloudNumber, userPassword);

         // Step 9 : save the email and phone in local DB

         DAOFactory dao = DAOFactory.getInstance();
         try
         {
            dao.getSignupInfoDAO().insert(signupInfo);
         } catch (DAOException e)
         {
            logger.error("Problem inserting record in signupInfo table. Data : " + signupInfo.toString());
            logger.error("DB Exception : " + e.getMessage());
         }

         // Step 10 : send the notification email for successful registration of cloudname.
         // Send the email at email address registered for the cloud name.
         emailHelper.sendRegistrationSuccessNotificaionEmail(userEmail, cspContactEmail, cloudName.toString(), locale, cspCloudName, cspHomePage);
         break;
      } catch (CSPRegistrationException ex1)
      {
          try {
              retryCount++;
              logger.error("Failed to register cloudname with CSP. CloudName : " + cloudName.toString() + " , CloudNumber : " + cloudNumber.toString());
              logger.error("SignupInfo : " + signupInfo.toString());
              logger.error("CSPRegistrationException from RegisterUserThread " + ex1.getMessage());
              // Send the notification email for registration failure of cloudname.
              // Send email to configured contact support address.
              emailHelper.sendRegistrationFailureNotificaionEmail(contactSupportEmail, cloudName.toString(), locale, cspCloudName, paymentType, paymentRefId, userEmail, userPhone);
              // Wait for 2 minutes before retry
              Thread.sleep(120000);
          } catch (InterruptedException e) {
              logger.error("Interrupted while waiting for cloud name registration {}.", e.getLocalizedMessage());
          }
      } catch (Xdi2ClientException ex2)
      {
          try {
              retryCount++;
              logger.error("Failed to register cloudname. CloudName : " + cloudName.toString() + " , CloudNumber : " + cloudNumber.toString());
              logger.error("SignupInfo : " + signupInfo.toString());
              logger.error("Xdi2ClientException from RegisterUserThread " + ex2.getMessage());
              // Send the notification email for registration failure of cloudname.
              // Send email to configured contact support address.
              emailHelper.sendRegistrationFailureNotificaionEmail(contactSupportEmail, cloudName.toString(), locale, cspCloudName, paymentType, paymentRefId, userEmail, userPhone);
              // Wait for 2 minutes before retry
              Thread.sleep(120000);
          } catch (InterruptedException e) {
              logger.error("Interrupted while waiting for cloud name registration {}.", e.getLocalizedMessage());
          }
      }
      }
   }

   public CloudNumber getCloudNumber()
   {
      return cloudNumber;
   }

   public void setCloudNumber(CloudNumber cloudNumber)
   {
      this.cloudNumber = cloudNumber;
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

   public CloudName getCloudName()
   {
      return cloudName;
   }

   public void setCloudName(CloudName cloudName)
   {
      this.cloudName = cloudName;
   }

   public String getVerifiedPhone()
   {
      return verifiedPhone;
   }

   public void setVerifiedPhone(String verifiedPhone)
   {
      this.verifiedPhone = verifiedPhone;
   }

   public String getVerifiedEmail()
   {
      return verifiedEmail;
   }

   public void setVerifiedEmail(String verifiedEmail)
   {
      this.verifiedEmail = verifiedEmail;
   }

   public String getUserPassword()
   {
      return userPassword;
   }

   public void setUserPassword(String userPassword)
   {
      this.userPassword = userPassword;
   }

   

   public NeustarRNDiscountCode getCdc()
   {
      return cdc;
   }

   public void setCdc(NeustarRNDiscountCode cdc)
   {
      this.cdc = cdc;
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

  public void setCspContactSupportEmail(String contactSupportEmail) {
      this.contactSupportEmail = contactSupportEmail;
  }

  public String getCspContactSupportEmail()
  {
     return this.contactSupportEmail;
  }

  public void setCspCloudName(String cspCloudName) {
      this.cspCloudName = cspCloudName;
  }

  public String getCspCloudName()
  {
     return this.cspCloudName;
  }

  public void setCspHomePage(String cspHomePage) {
      this.cspHomePage = cspHomePage;
  }

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

  public void setUserEmail(String email) {
      this.userEmail = email;
  }

  public String getUserEmail() {
      return this.userEmail;
  }

  public void setUserPhone(String userPhone) {
      this.userPhone = userPhone;
  }

  public String getUserPhone() {
      return this.userPhone;
  }

  public void setCspContactEmail(String cspContactEmail) {
      this.cspContactEmail = cspContactEmail;
  }

  public String getCspContactEmail()
  {
     return this.cspContactEmail;
  }
}
