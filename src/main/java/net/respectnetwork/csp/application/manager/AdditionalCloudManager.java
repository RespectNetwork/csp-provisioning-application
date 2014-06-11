package net.respectnetwork.csp.application.manager;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import net.respectnetwork.sdk.csp.CSP;
import net.respectnetwork.csp.application.dao.DAOException;
import net.respectnetwork.csp.application.dao.DAOFactory;
import net.respectnetwork.csp.application.form.AdditionalCloudForm;
import net.respectnetwork.csp.application.model.PaymentModel;
import net.respectnetwork.csp.application.session.RegistrationSession;
import net.respectnetwork.csp.application.model.GiftCodeRedemptionModel;
import net.respectnetwork.csp.application.types.PaymentType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.ModelAndView;

import xdi2.client.exceptions.Xdi2ClientException;
import xdi2.core.xri3.CloudName;
import xdi2.core.xri3.CloudNumber;

/**
 * @author psharma2
 * 
 */
public class AdditionalCloudManager {

    /** CLass Logger */
    private static final Logger logger = LoggerFactory
            .getLogger(AdditionalCloudManager.class);

    /** Registration Session */
    private RegistrationSession regSession;

    /** Registration Service : to register dependent clouds */
    private RegistrationManager registrationManager;

    /**
     * Constructor
     */
    public AdditionalCloudManager(RegistrationSession regSession,
            RegistrationManager registrationManager) {
        this.regSession = regSession;
        this.registrationManager = registrationManager;
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
     * Method to get registration manager.
     * @return
     */
    public RegistrationManager getRegistrationManager() {
        return registrationManager;
    }

    /**
     * Method to set registration manager.
     * @param registrationManager
     */
    @Autowired
    public void setRegistrationManager(RegistrationManager registrationManager) {
        this.registrationManager = registrationManager;
    }

    /**
     * Method to create additional cloud. This method checks and process
     * the payment type, whether it is credit card or giftcodes or promocode.
     * Redirects to additional cloud thread for handling the creation process.
     *
     * @param cloudName cloudname of additional cloud to be created.
     * @param payment contains payment detail, paymentType and paymentRefId.
     * @param giftCodes If cloud to be purchased with gift card, otherwise null.
     * @param promoCode If cloud to be purchased with promo card, otherwise null.
     * @param guardianEmailAddress email address of loggedin user.
     * @param guardianPhoneNumber phone number of loggedin user.
     * @param request
     * @return
     */
    public ModelAndView createAdditionalClouds(String cloudName,
            PaymentModel payment, String[] giftCodes, String promoCode,
            String guardianEmailAddress, String guardianPhoneNumber,
            HttpServletRequest request) {
        ModelAndView mv = null;
        boolean errors = false;
        DAOFactory dao = DAOFactory.getInstance();

        AdditionalCloudForm additionalCloudForm = regSession
                .getAdditionalCloudForm();
        if (additionalCloudForm == null) {
            return null;
        }
        List<String> arrAdditionalCloudName = additionalCloudForm
                .getAdditionalCloudNames();

        int cloudsPurchasedWithGiftCodes = 0;
        if (payment != null) // payment via CC
        {
            String giftCodeStr = regSession.getGiftCode();
            if (giftCodeStr != null && !giftCodeStr.isEmpty()) {
                cloudsPurchasedWithGiftCodes = giftCodeStr.split(" ").length;
            }
        }
        int validCloudName = 0;
        int i = 0;

        CSP myCSP = registrationManager.getCspRegistrar();
        if (myCSP == null) {
            logger.info("CSP Object is null. ");
            errors = true;
        } else {
            CloudName loggedinCloudName = CloudName.create(cloudName);
            CloudNumber cloudNumber = null;
            try {
                cloudNumber = myCSP.checkCloudNameInRN(loggedinCloudName);
            } catch (Xdi2ClientException ex2) {
                logger.error("Failed to register cloudname. CloudName : "
                        + cloudName + " , CloudNumber : "
                        + cloudNumber.toString());
                logger.error("Xdi2ClientException from RegisterUserThread "
                        + ex2.getMessage());
                errors = true;
            }
            if (cloudNumber != null) {
                for (String additionalCloudName : arrAdditionalCloudName) {
                    if (!(additionalCloudName == null)) {
                        if (!additionalCloudName.trim().isEmpty()) {
                            validCloudName++;
                            if (promoCode == null || promoCode.isEmpty()) {
                                if (giftCodes != null && i == giftCodes.length) {
                                    break;
                                }
                                if (i < cloudsPurchasedWithGiftCodes) {
                                    i++;
                                    continue;
                                }
                            }
                            if (i >= arrAdditionalCloudName.size()) {
                                break;
                            }
                            String paymentType = "";
                            String paymentRefId = "";
                            if (payment != null) {
                                paymentType = PaymentType.CreditCard.toString();
                                paymentRefId = payment.getPaymentId();
                            } else if (giftCodes != null
                                    && giftCodes.length > 0) {
                                paymentType = PaymentType.GiftCode.toString();
                                paymentRefId = giftCodes[i];
                            } else if (promoCode != null
                                    && !promoCode.isEmpty()) {
                                paymentType = PaymentType.PromoCode.toString();
                                paymentRefId = promoCode;
                            }
                            logger.debug("Creating additional cloud "
                                    + additionalCloudName);

                            try {
                                registerAdditionalCloudName(
                                        registrationManager,
                                        additionalCloudName, cloudNumber,
                                        regSession.getPassword(), myCSP,
                                        paymentType, paymentRefId,
                                        request.getLocale(),
                                        guardianEmailAddress,
                                        guardianPhoneNumber);
                                logger.debug("Sucessfully Registered {}",
                                        additionalCloudName);
                            } catch (Xdi2ClientException e1) {
                                logger.debug("Xdi2ClientException in registering cloud "
                                        + e1.getMessage());
                            }
                            if (payment == null && giftCodes != null
                                    && giftCodes.length > 0) {
                                String responseId = UUID.randomUUID()
                                        .toString();
                                // make a new record in the giftcode_redemption
                                // table
                                GiftCodeRedemptionModel giftCodeRedemption = new GiftCodeRedemptionModel();
                                giftCodeRedemption
                                        .setCloudNameCreated(additionalCloudName);
                                giftCodeRedemption.setGiftCodeId(giftCodes[i]);
                                giftCodeRedemption.setRedemptionId(responseId);
                                giftCodeRedemption.setTimeCreated(new Date());
                                try {
                                    dao.getGiftCodeRedemptionDAO().insert(
                                            giftCodeRedemption);
                                } catch (DAOException e) {
                                    logger.error("Error in updating giftcode redemption information "
                                            + e.getMessage());
                                    errors = true;
                                }
                            }
                            // return null if errors or if there is no valid
                            // additional cloud name.
                            if (errors) {
                                return null;
                            }
                            i++;
                        }
                    }
                }
            } else {
                errors = true;
                logger.info("Authenticating to personal cloud failed for "
                        + cloudName);
            }
        }
        // If cloud name purchased with gift i.e. i, is less then
        // validCloudName, pay rest with credit card.
        if (i < validCloudName) {
            mv = new ModelAndView("creditCardPayment");
            return mv;
        }
        regSession.setAdditionalCloudForm(null);
        mv = new ModelAndView("additionalCloudDone");
        return mv;
    }

    /**
     * Method to register additional cloudname.
     * 
     * @param cloudNumber
     *            cloud number of cloudname user is logged in with.
     * @param myCSP
     *            contains csp related functionality.
     * @param additionalCloudName
     *            previously unRegistered ClouldName
     */
    private void registerAdditionalCloudName(
            RegistrationManager registrationManager,
            String additionalCloudName, CloudNumber cloudNumber,
            String secretToken, CSP myCSP, String paymentType,
            String paymentRefId, Locale locale, String guardianEmailAddress,
            String guardianPhoneNumber) throws Xdi2ClientException {
        CloudName cloudName = CloudName.create(additionalCloudName);
        AdditionalCloudThread act = new AdditionalCloudThread();
        act.setCloudName(cloudName);
        act.setCloudNumber(cloudNumber);
        act.setCSP(myCSP);
        act.setPaymentType(paymentType);
        act.setPaymentRefId(paymentRefId);
        act.setVerifiedEmail(guardianEmailAddress);
        act.setVerifiedPhone(guardianPhoneNumber);
        act.setRegistrationManager(registrationManager);
        act.setSecretToken(secretToken);

        Thread t = new Thread(act);
        t.start();
    }

    /**
     * This method returns Cloud Names exist for the Cloud Number associated
     * with given cloud name in the CSP Cloud.
     * 
     * @param cloudName
     * @return list of cloud names associated with same cloud number of given
     *         cloud name.
     * @throws Xdi2ClientException
     */
    public List<String> getAdditionalCloudNamesInCSP(CloudName loggedinCloudName)
            throws Xdi2ClientException {
        List<String> additionalCloudNames = Arrays.asList(new String[5]);
        CSP mycsp = registrationManager.getCspRegistrar();
        // Get cloud number of given cloud name
        CloudNumber cloudNumber = mycsp.checkCloudNameInRN(loggedinCloudName);
        // Get all the cloud names associated with given cloud number in CSP
        CloudName[] cloudNameArr = mycsp.checkCloudNamesInCSP(cloudNumber);

        // Cloud name array size greater then one signifies there are additional
        // cloud names in CSP.
        // Return the list of additional cloud names.
        if (cloudNameArr != null && cloudNameArr.length > 1) {
            int j = 0;
            for (int i = 0; i < cloudNameArr.length; i++) {
                if (!cloudNameArr[i].equals(loggedinCloudName)) {
                    additionalCloudNames.set(j, cloudNameArr[i].toString());
                    j++;
                }
            }
        }
        return additionalCloudNames;
    }
}
