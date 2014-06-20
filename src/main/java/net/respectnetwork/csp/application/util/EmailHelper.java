package net.respectnetwork.csp.application.util;

import java.util.Locale;

import net.respectnetwork.csp.application.dao.DAOContextProvider;
import net.respectnetwork.csp.application.dao.DAOException;
import net.respectnetwork.csp.application.dao.DAOFactory;
import net.respectnetwork.csp.application.invite.GiftEmailSenderThread;
import net.respectnetwork.csp.application.manager.PasswordManager;
import net.respectnetwork.csp.application.model.CSPModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class to send the email notifications to user in case of success or failure
 * while registering cloudname.
 *
 * @author psharma2
 */
public class EmailHelper {
    /** Class Logger */
    private static final Logger logger = LoggerFactory
            .getLogger(PasswordManager.class);
    private static final String DEFAILT_REGISTER_MAIL_TXT_1  = "You are now officially a lifetime member of the Respect Network.<br/><br/>";
    private static final String DEFAILT_REGISTER_MAIL_TXT_2  = "As a member, you're an integral part of crowd-funding and growing the world\u2019s first trusted, personal data network.<br/><br/>";
    private String licenceKey;
    
    public String getLicenceKey() {
        return licenceKey;
    }

    public void setLicenceKey(String licenceKey) {
        this.licenceKey = licenceKey;
    }

    /**
     * Method to send notification email for successful registration.
     * @param emailAddress Registered email address of cloud name purchased.
     * @param bccEmailAddress csp contact email address. 
     * @param cloudName cloud name to be registered in case of signup. Or guardian's cloudname in case of dependent registration. 
     * @param locale
     * @param cspCloudName cspcloudname
     * @param homePage csp homepage.
     */
    public void sendRegistrationSuccessNotificaionEmail(String emailAddress, String bccEmailAddress, String cloudName, Locale locale, String cspCloudName, String homePage, boolean isAdditionalCloud)
    {
        logger.info("Sending notificaion email for successful registration of cloudname: " + cloudName);

        CSPModel cspModel = null;

        try
        {
           cspModel = DAOFactory.getInstance().getCSPDAO()
                 .get(cspCloudName);
        } catch (DAOException e)
        {
           // TODO Auto-generated catch block
           e.printStackTrace();
        }
        
        Object[] cloudNames = new Object[] { cloudName};
        Object[] cloudAndCSP = new Object[] { cloudName, cspCloudName};
        Object[] cspName = new Object[] { cspCloudName };
        Object[] cspHomePage = new Object[] { homePage };
        Object[] licenceKeyValue = new Object[] { licenceKey };
        
        String subject = getMessageFromResource("register.mail.subject", cloudNames, null, locale);
        StringBuilder builder = new StringBuilder();
        if(isAdditionalCloud) {
            builder.append(getMessageFromResource("register.additionalcloud.mail.text.0" , cloudNames, null , locale));
        } else {
            builder.append(getMessageFromResource("register.cloud.mail.text.0" , cloudAndCSP, null , locale));
            builder.append(getMessageFromResource("register.cloud.mail.text.1" , null, DEFAILT_REGISTER_MAIL_TXT_1 , locale));
        }
        builder.append(getMessageFromResource("register.cloud.mail.text.2" , null, DEFAILT_REGISTER_MAIL_TXT_2 , locale));
        builder.append(getMessageFromResource("register.cloud.mail.faq" , cspHomePage, null , locale));
        if(licenceKey != null) {
            builder.append(getMessageFromResource("register.mail.licenceKey" , licenceKeyValue, null , locale));
        }
        
        builder.append(getMessageFromResource("register.mail.footer" , cspName, null , locale));

        sendMail(builder, subject, emailAddress, bccEmailAddress);
    }

    /**
     * Method to send notification email for registration failure.
     * @param emailAddress Email address provided while registration and contact support email address configured in csp.properties file. 
     * @param cloudName cloud name to be registered in case of signup. Or guardian's cloudname in case of dependent registration. 
     * @param locale
     * @param cspCloudName cspcloudname
     */
    public void sendRegistrationFailureNotificaionEmail(String emailAddress, String cloudName, Locale locale, String cspCloudName, String paymentType, String paymentRefId, String userEmail, String verifiedPhone, boolean isAdditionalCloud)
    {
        logger.info("Sending notificaion email for registration failure of cloudname: " + cloudName);

        CSPModel cspModel = null;

        try
        {
           cspModel = DAOFactory.getInstance().getCSPDAO()
                 .get(cspCloudName);
        } catch (DAOException e)
        {
           // TODO Auto-generated catch block
           e.printStackTrace();
        }

        Object[] cloudNames = new Object[] { cloudName };
        Object[] cspName = new Object[] { cspCloudName };
        Object[] email = new Object[] { userEmail };
        Object[] phone = new Object[] { verifiedPhone };
        Object[] payType = new Object[] { paymentType };
        Object[] payRefId = new Object[] { paymentRefId };

        String subject = getMessageFromResource("registerFailure.mail.subject", cloudNames, null, locale);

        StringBuilder builder = new StringBuilder();
        if(isAdditionalCloud) {
            builder.append(getMessageFromResource("registerFailure.additionalcloud.mail.text.0" , cloudNames, null , locale));
        } else {
            builder.append(getMessageFromResource("registerFailure.cloud.mail.text.0" , cloudNames, null , locale));
        }
        builder.append(getMessageFromResource("registerFailure.mail.text.1" , cspName, null , locale));
        builder.append(getMessageFromResource("registerFailure.mail.customer.detail" , null, null , locale));
        builder.append(getMessageFromResource("registerFailure.mail.address" , email, null , locale));
        builder.append(getMessageFromResource("registerFailure.mail.phone" , phone, null , locale));
        builder.append(getMessageFromResource("registerFailure.mail.payment.type" , payType, null , locale));
        builder.append(getMessageFromResource("registerFailure.mail.payment.refid" , payRefId, null , locale));
        builder.append(getMessageFromResource("registerFailure.mail.footer" , cspName, null , locale));
        sendMail(builder, subject, emailAddress, null);
    }

    /**
     * Method to send the email.
     * @param builder content of email.
     * @param subject email subject.
     * @param emailAddress email TO address.
     */
    private void sendMail(StringBuilder builder, String subject, String emailToAddress, String emailBccAddress) {
        String emailAddresses = emailToAddress;
        try
        {
           if(emailBccAddress != null) {
               emailAddresses = emailAddresses + "," + emailBccAddress;
           }
           GiftEmailSenderThread get = new GiftEmailSenderThread();
           get.setContent(builder.toString());
           get.setSubject(subject);
           get.setToAddress(emailToAddress);
           get.setBccAddress(emailBccAddress);

           Thread t = new Thread(get);
           t.start();
        }
        catch( Exception e )
        {
           logger.error("Failed to send invite email to " + emailAddresses, e);
        }
    }

    private static String getMessageFromResource( String name, Object[] objs, String def, Locale locale )
    {
        String rtn = DAOContextProvider.getApplicationContext().getMessage(name, objs, def, locale);

        return rtn;
    }
}
