package net.respectnetwork.csp.application.util;

import java.util.Locale;
import javax.servlet.http.HttpServletRequest;

import net.respectnetwork.csp.application.dao.DAOContextProvider;
import net.respectnetwork.csp.application.dao.DAOException;
import net.respectnetwork.csp.application.dao.DAOFactory;
import net.respectnetwork.csp.application.dao.SignupInfoDAO;
import net.respectnetwork.csp.application.invite.GiftEmailSenderThread;
import net.respectnetwork.csp.application.manager.PasswordManager;
import net.respectnetwork.csp.application.model.CSPModel;
import net.respectnetwork.csp.application.model.SignupInfoModel;

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

    /**
     * Method to send notification email for successful registration.
     * @param emailAddress Registered email address of cloud name purchased. 
     * @param cloudName cloud name to be registered in case of signup. Or guardian's cloudname in case of dependent registration. 
     * @param locale
     * @param cspCloudName cspcloudname
     * @param homePage csp homepage.
     */
    public void sendRegistrationSuccessNotificaionEmail(String emailAddress, String cloudName, Locale locale, String cspCloudName, String homePage)
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

        Object[] cloudNames = new Object[] { cloudName };
        Object[] cspName = new Object[] { cspCloudName };
        Object[] cspHomePage = new Object[] { homePage };

        String subject = getMessageFromResource("register.mail.subject", cloudNames, null, locale);

        StringBuilder builder = new StringBuilder();
        builder.append(getMessageFromResource("register.mail.text.0" , cloudNames, null , locale));
        builder.append(getMessageFromResource("register.mail.text.1" , cspName, null , locale));
        builder.append(getMessageFromResource("register.mail.faq" , cspHomePage, null , locale));
        builder.append(getMessageFromResource("register.mail.footer" , cspName, null , locale));

        sendMail(builder, subject, emailAddress);
    }

    /**
     * Method to send notification email for registration failure.
     * @param emailAddress Email address provided while registration and contact support email address configured in csp.properties file. 
     * @param cloudName cloud name to be registered in case of signup. Or guardian's cloudname in case of dependent registration. 
     * @param locale
     * @param cspCloudName cspcloudname
     */
    public void sendRegistrationFailureNotificaionEmail(String emailAddress, String cloudName, Locale locale, String cspCloudName)
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

        String subject = getMessageFromResource("registerFailure.mail.subject", cloudNames, null, locale);

        StringBuilder builder = new StringBuilder();
        builder.append(getMessageFromResource("registerFailure.mail.text.0" , cloudNames, null , locale));
        builder.append(getMessageFromResource("registerFailure.mail.text.1" , cspName, null , locale));
        builder.append(getMessageFromResource("registerFailure.mail.footer" , null, null , locale));

        sendMail(builder, subject, emailAddress);
    }

    /**
     * Method to send the email.
     * @param builder content of email.
     * @param subject email subject.
     * @param emailAddress email TO address.
     */
    private void sendMail(StringBuilder builder, String subject, String emailAddress) {
        try
        {
           GiftEmailSenderThread get = new GiftEmailSenderThread();
           get.setContent(builder.toString());
           get.setSubject(subject);
           get.setToAddress(emailAddress);

           Thread t = new Thread(get);
           t.start();
        }
        catch( Exception e )
        {
           logger.error("Failed to send invite email to " + emailAddress, e);
        }
    }

    private static String getMessageFromResource( String name, Object[] objs, String def, Locale locale )
    {
        String rtn = DAOContextProvider.getApplicationContext().getMessage(name, objs, def, locale);

        return rtn;
    }
}
