/**
 * 
 */
package net.respectnetwork.csp.application.manager;

import net.respectnetwork.csp.application.constants.CSPErrorsEnum;
import net.respectnetwork.csp.application.exception.PasswordValidationException;
import net.respectnetwork.sdk.csp.CSP;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xdi2.client.exceptions.Xdi2ClientException;
import xdi2.core.xri3.CloudName;
import xdi2.core.xri3.CloudNumber;

/**
 * @author kvats
 * 
 */
public class PasswordManager {

    /** Class Logger */
    private static final Logger logger = LoggerFactory
            .getLogger(PasswordManager.class);

    /**
     * Class for Registering User in the Respect Network
     */
    private CSP cspRegistrar;

    /**
     * Constructor
     */
    public PasswordManager(CSP cspRegistrar) {
        this.cspRegistrar = cspRegistrar;
    }

    /**
     * This method checks if cloud name for which password needs to be changed
     * exist. Then check if format for new password is correct and finally
     * change the password.
     * 
     * @param clName
     *            cloud name for which password is to be changed.
     * @param currentPassword
     *            existing password for the cloud name.
     * @param newPassword
     *            new value for the password to be updated.
     * @throws PasswordValidationException
     */
    public void changePassword(String clName, String currentPassword,
            String newPassword) throws PasswordValidationException {
        logger.info("Change user password");
        CloudName cloudName = CloudName.create(clName);
        CloudNumber cloudNumber;
        try {
            cloudNumber = cspRegistrar.checkCloudNameAvailableInRN(cloudName);
            if (cloudNumber != null) {
                cspRegistrar.authenticateInCloud(cloudNumber, currentPassword);

            } else {
                logger.error("Authenticating to personal cloud failed for "
                        + cloudName);
                throw new PasswordValidationException(
                        CSPErrorsEnum.VE_ERROR_CLOUD_NAME_NOT_EXIST.code(),
                        CSPErrorsEnum.VE_ERROR_CLOUD_NAME_NOT_EXIST.message());

            }
        } catch (Xdi2ClientException ex) {
            logger.error("Failed to authenticate to personal cloud: ", ex);
            throw new PasswordValidationException(
                    CSPErrorsEnum.VE_INVALID_CURRENT_PASSWORD.code(),
                    CSPErrorsEnum.VE_INVALID_CURRENT_PASSWORD.message());
        }
        validatePasswordFormat(newPassword);
        // update the new password for CSP user
        logger.info("going to change password for cloud name: " + clName);
        try {
            cspRegistrar.setCloudSecretTokenInCSP(cloudNumber, newPassword);
            logger.info("Successfully changed the passord.");
        } catch (Xdi2ClientException ex) {
            logger.error("Failed to change the password for CloudName: "
                    + clName + " , CloudNumber : " + cloudNumber.toString());
            logger.error("Error while updating password: ", ex);
            // TODO: In case error while updating password should we just
            // continue by logging the message or display a message to user to
            // contact
            // customer
            // care or try later
        }

    }

    /**
     * Validate format of the password.
     * 
     * @param newPassword
     *            password provided
     * @throws PasswordValidationException
     *             if password provided does not compliance to standard.
     */
    private void validatePasswordFormat(String newPassword)
            throws PasswordValidationException {
        if (!RegistrationManager.validatePassword(newPassword)) {
            logger.debug("The password provided is invalid.");
            throw new PasswordValidationException(
                    CSPErrorsEnum.VE_INVALID_PASSWORD_FORMAT.code(),
                    CSPErrorsEnum.VE_INVALID_PASSWORD_FORMAT.message());
        }
    }

}
