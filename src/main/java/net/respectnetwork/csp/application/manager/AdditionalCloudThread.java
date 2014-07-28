package net.respectnetwork.csp.application.manager;

import java.util.Locale;

import net.respectnetwork.csp.application.util.EmailHelper;
import net.respectnetwork.sdk.csp.CSP;
import net.respectnetwork.sdk.csp.discount.NeustarRNCampaignCode;
import net.respectnetwork.sdk.csp.discount.NeustarRNDiscountCode;
import net.respectnetwork.sdk.csp.exception.CSPRegistrationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xdi2.client.exceptions.Xdi2ClientException;
import xdi2.core.xri3.CloudName;
import xdi2.core.xri3.CloudNumber;

/**
 * @author psharma2
 * 
 */
public class AdditionalCloudThread implements Runnable {
    /** Class Logger */
    private static final Logger logger = LoggerFactory
            .getLogger(AdditionalCloudThread.class);

    private CloudName cloudName = null;
    private CloudNumber cloudNumber = null;
    private CSP myCSP = null;
    private String secretToken = "";
    private String paymentType = "";
    private String paymentRefId = "";
    private Locale locale = null;
    private String contactSupportEmail = null;
    private String cspCloudName = null;
    private String cspHomePage = null;
    private String cspContactEmail = null;
    private String verifiedPhone = "";
    private String verifiedEmail = "";
    private RegistrationManager registrationManager = null;

    @Override
    public void run() {
        EmailHelper emailHelper = new EmailHelper();
        cspCloudName = registrationManager.getCspCloudName();
        cspHomePage = registrationManager.getCspHomePage();
        contactSupportEmail = registrationManager.getCspContactSupportEmail();
        cspContactEmail = registrationManager.getCspContactEmail();
        boolean isAdditionalCloud = true;

            try {
                // Return if cloud is already registered.
                if(!registrationManager.isCloudNameAvailableInRegistry(cloudName.toString())) {
                    throw new CSPRegistrationException("Cloud Name " + cloudName
                            + " is already registered.");
                }
                // Step 1: Add an additional Cloud Name to an Existing Respect
                // Network Account.
                myCSP.registerAdditionalCloudNameInRN(cloudName, cloudNumber,
                        NeustarRNDiscountCode.FirstFiveNames,
                        NeustarRNCampaignCode.FirstMillion);

                // Step 2: Register an additional of New CloudName to Existing
                // CloudNumber in CSP Graph
                myCSP.registerAdditionalCloudNameInCSP(cloudName, cloudNumber);

                // Step 3: Register Addition of New CloudName to Existing
                // CloudNumber in User Graph
                myCSP.registerAdditionalCloudNameInCloud(cloudName,
                        cloudNumber, this.getSecretToken());

                // Step 4: Send the email at email address registered for the
                // cloud name.
                emailHelper.sendRegistrationSuccessNotificaionEmail(
                        verifiedEmail, cspContactEmail, cloudName.toString(),
                        locale, cspCloudName, cspHomePage, isAdditionalCloud);
            } catch (Xdi2ClientException ex2) {
                    logger.error("Failed to register cloudname. CloudName : "
                            + cloudName.toString() + " , CloudNumber : "
                            + cloudNumber.toString());
                    logger.error("Xdi2ClientException from AdditionalCloudThread "
                            + ex2.getMessage());
                    // Send the notification email for registration failure of
                    // cloudname.
                    // Send email to configured contact support address.
                    emailHelper.sendRegistrationFailureNotificaionEmail(
                            contactSupportEmail, cloudName.toString(), locale,
                            cspCloudName, this.getPaymentType(),
                            this.getPaymentRefId(), verifiedEmail,
                            verifiedPhone, isAdditionalCloud);
            } catch (Exception ex1) {
                    logger.error("Failed to register cloudname. CloudName : "
                            + cloudName.toString() + " , CloudNumber : "
                            + cloudNumber.toString());
                    logger.error("Exception from AdditionalCloudThread "
                            + ex1.getMessage());
                    // Send the notification email for registration failure of
                    // cloudname.
                    // Send email to configured contact support address.
                    emailHelper.sendRegistrationFailureNotificaionEmail(
                            contactSupportEmail, cloudName.toString(), locale,
                            cspCloudName, this.getPaymentType(),
                            this.getPaymentRefId(), verifiedEmail,
                            verifiedPhone, isAdditionalCloud);
            }
    }

    /**
     * Get additional cloud name.
     * 
     * @return
     */
    public CloudName getCloudName() {
        return this.cloudName;
    }

    /**
     * Set additional cloud name.
     * 
     * @param cloudName
     */
    public void setCloudName(CloudName cloudName) {
        this.cloudName = cloudName;
    }

    /**
     * Method to get cloud number. The cloud number of additional cloud will be
     * same as cloudnumber of logged in cloudname.
     * 
     * @return
     */
    public CloudNumber getCloudNumber() {
        return this.cloudNumber;
    }

    /**
     * Method to set cloudnumber
     * 
     * @param cloudNumber
     */
    public void setCloudNumber(CloudNumber cloudNumber) {
        this.cloudNumber = cloudNumber;
    }

    /**
     * Method to get CSP object. It contains csp related functionality.
     * 
     * @return CSP
     */
    public CSP getCSP() {
        return this.myCSP;
    }

    /**
     * Method to set CSP object. It contains csp related functionality.
     * 
     * @param myCSP
     */
    public void setCSP(CSP myCSP) {
        this.myCSP = myCSP;
    }

    /**
     * Method to set additional cloudname's secret token. It will be same as
     * logged in cloud names secret token.
     * 
     * @return secretToken
     */
    public String getSecretToken() {
        return this.secretToken;
    }

    /**
     * Method to set secret token in additional cloudname created.
     * 
     * @param secretToken
     *            secret token of additional cloudname.
     */
    public void setSecretToken(String secretToken) {
        this.secretToken = secretToken;
    }

    /**
     * Method to get payment type.
     * 
     * @return paymentType. It could be either giftCode, creditCard or
     *         promoCode.
     */
    public String getPaymentType() {
        return this.paymentType;
    }

    /**
     * Method to set payment type.
     * 
     * @param paymentType
     *            . It could be either of giftCode, creditCard or promoCode.
     */
    public void setPaymentType(String paymentType) {
        this.paymentType = paymentType;
    }

    /**
     * Method to get payment reference id.
     * 
     * @return paymemtRefId. It could be either giftcode_id or promo_id or
     *         payment_id.
     */
    public String getPaymentRefId() {
        return this.paymentRefId;
    }

    /**
     * Method to set payment reference id.
     * 
     * @param paymentRefId
     *            . It could be either either giftcode_id or promo_id or
     *            payment_id.
     */
    public void setPaymentRefId(String paymentRefId) {
        this.paymentRefId = paymentRefId;
    }

    /**
     * Method to set locale.
     * 
     * @param locale
     */
    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    /**
     * Method to get locale.
     * 
     * @return locale
     */
    public Locale getLocale() {
        return this.locale;
    }

    /**
     * Method to get verified phone number.
     * 
     * @return verifiedPhone
     */
    public String getVerifiedPhone() {
        return verifiedPhone;
    }

    /**
     * Method to set verified phone number.
     * 
     * @param verifiedPhone
     */
    public void setVerifiedPhone(String verifiedPhone) {
        this.verifiedPhone = verifiedPhone;
    }

    /**
     * Method to get verified email address.
     * 
     * @return verifiedEmail
     */
    public String getVerifiedEmail() {
        return verifiedEmail;
    }

    /**
     * Method to set verified email address.
     * 
     * @param verifiedEmail
     */
    public void setVerifiedEmail(String verifiedEmail) {
        this.verifiedEmail = verifiedEmail;
    }

    /**
     * Method to get registration manager object.
     * 
     * @return registrationManager
     */
    public RegistrationManager getRegistrationManager() {
        return this.registrationManager;
    }

    /**
     * Method to set registration manager object.
     * 
     * @param registrationManager
     */
    public void setRegistrationManager(RegistrationManager regMgr) {
        this.registrationManager = regMgr;
    }
}
