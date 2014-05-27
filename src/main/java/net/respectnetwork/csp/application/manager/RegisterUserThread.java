package net.respectnetwork.csp.application.manager;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import net.respectnetwork.csp.application.dao.DAOException;
import net.respectnetwork.csp.application.dao.DAOFactory;
import net.respectnetwork.csp.application.model.SignupInfoModel;
import net.respectnetwork.sdk.csp.CSP;
import net.respectnetwork.sdk.csp.discount.NeustarRNDiscountCode;
import net.respectnetwork.sdk.csp.exception.CSPRegistrationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xdi2.client.exceptions.Xdi2ClientException;
import xdi2.core.xri3.CloudName;
import xdi2.core.xri3.CloudNumber;
import xdi2.core.xri3.XDI3Segment;

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

   @Override
   public void run()
   {
      SignupInfoModel signupInfo = new SignupInfoModel();
      signupInfo.setCloudName(cloudName.toString());
      signupInfo.setEmail(verifiedEmail);
      signupInfo.setPhone(verifiedPhone);
      signupInfo.setPaymentType(paymentType);
      signupInfo.setPaymentRefId(paymentRefId);
      
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
               .checkCloudNameAvailableInRN(cloudName);

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

      } catch (CSPRegistrationException ex1)
      {
         logger.error("Failed to register cloudname with CSP. CloudName : " + cloudName.toString() + " , CloudNumber : " + cloudNumber.toString());
         logger.error("SignupInfo : " + signupInfo.toString());
         logger.error("CSPRegistrationException from RegisterUserThread " + ex1.getMessage());
      } catch (Xdi2ClientException ex2)
      {
         logger.error("Failed to register cloudname. CloudName : " + cloudName.toString() + " , CloudNumber : " + cloudNumber.toString());
         logger.error("SignupInfo : " + signupInfo.toString());
         logger.error("Xdi2ClientException from RegisterUserThread " + ex2.getMessage());
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
}
