package net.respectnetwork.csp.application.util;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import javax.servlet.http.HttpServletRequest;

import net.respectnetwork.csp.application.constants.LicenceKeyEnum;
import net.respectnetwork.csp.application.dao.DAOException;
import net.respectnetwork.csp.application.dao.DAOFactory;
import net.respectnetwork.csp.application.dao.DependentCloudDAO;
import net.respectnetwork.csp.application.exception.CSPException;
import net.respectnetwork.csp.application.manager.RegistrationManager;
import net.respectnetwork.csp.application.model.DependentCloudModel;
import net.respectnetwork.csp.application.model.LicenseKeyModel;
import net.respectnetwork.csp.application.model.LicenseKeyResponse;
import net.respectnetwork.csp.application.rest.client.RestServiceHttpClient;
import net.respectnetwork.sdk.csp.CSP;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.ModelAndView;

public class CSPHelper {
    /** Class Logger */
    private static final Logger logger = LoggerFactory
            .getLogger(CSPHelper.class);
    private static RegistrationManager registrationManager;
    private static CSP cspRegistrar;
    private static final String licenceKeyPath = "/api/getLicenseKey";
    /**
     * RN endpoint for social safe licence key
     */
    private static String rnSocialSafeEndpoint;
    /**
     * Social safe secret token for CSP
     */
    private static String rnSocialSafeToken;

    public CSPHelper(RegistrationManager registrationManager, CSP cspRegistrar) {
        CSPHelper.registrationManager = registrationManager;
        CSPHelper.cspRegistrar = cspRegistrar;
    }

    public static String getRnSocialSafeEndpoint() {
        return rnSocialSafeEndpoint;
    }

    public static void setRnSocialSafeEndpoint(String rnSocialSafeEndpoint) {
        CSPHelper.rnSocialSafeEndpoint = rnSocialSafeEndpoint;
    }

    public static String getRnSocialSafeToken() {
        return rnSocialSafeToken;
    }

    public static void setRnSocialSafeToken(String rnSocialSafeToken) {
        CSPHelper.rnSocialSafeToken = rnSocialSafeToken;
    }

    public static ModelAndView getCloudPage(HttpServletRequest request,
            String cloudName) {

        // logger.debug("Request servlet path " + request.getServletPath());
        // logger.debug("Paths " + request.getPathInfo() + "-" +
        // request.getRequestURI() + "-" + request.getPathTranslated() );
        ModelAndView mv = new ModelAndView("cloudPage");

        String cspHomeURL = request.getContextPath();
        // logger.debug("getCloudPage :: cspHomeURL " + cspHomeURL);
        mv.addObject("logoutURL", cspHomeURL + "/logout");
        mv.addObject("cloudName", cloudName);
        String queryStr = "";
        try {
            queryStr = "name=" + URLEncoder.encode(cloudName, "UTF-8");

            queryStr += "&csp="
                    + URLEncoder
                            .encode(request.getContextPath().replace("/", "+"),
                                    "UTF-8");

            queryStr += "&inviter=" + URLEncoder.encode(cloudName, "UTF-8");
        } catch (UnsupportedEncodingException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        mv.addObject("queryStr", queryStr);
        mv.addObject("postURL", getPostRegistrationURL());
        return mv;
    }

    public static String getPostRegistrationURL() {
        String postRegistrationURL;
        if (RegistrationManager.getPostRegistrationURL() != null
                && !RegistrationManager.getPostRegistrationURL().equals("")) {
            postRegistrationURL = RegistrationManager.getPostRegistrationURL();
        } else {
            postRegistrationURL = registrationManager.getEndpointURI(
                    RegistrationManager.RNpostRegistrationLandingPageURIKey,
                    registrationManager.getCspRegistrar().getCspInformation()
                            .getCspCloudNumber());
        }
        return postRegistrationURL;
    }

    /**
     * @param emailHelper
     * @param dao
     */
    public static String generateSocialSafeKey(boolean isLicenceKeyApplicable,
            String cloudNumber) {
        String licenceKey = null;
        DAOFactory dao = DAOFactory.getInstance();
        if (isLicenceKeyApplicable) {
            logger.info("Get licence key for user cloud number : {}",
                    cloudNumber);
            String cspCloudNumber = cspRegistrar.getCspInformation()
                    .getCspCloudNumber().toString();
            LicenseKeyModel licenceKeyModel = new LicenseKeyModel(
                    cspCloudNumber, cloudNumber, rnSocialSafeToken);
            RestServiceHttpClient restServiceHttpClient = new RestServiceHttpClient(
                    rnSocialSafeEndpoint, licenceKeyPath, licenceKeyModel);
            LicenseKeyResponse licenceKeyResponse = null;
            try {
                licenceKeyResponse = restServiceHttpClient.deserialize(
                        restServiceHttpClient.postRequest(),
                        LicenseKeyResponse.class);
            } catch (CSPException ex) {
                logger.error("Error while parsing the licence key response: ",
                        ex);
            }
            if (licenceKeyResponse != null) {
                if (licenceKeyResponse.getKeyResponse() != null) {
                    licenceKeyModel
                            .setKeyName(LicenceKeyEnum.SOCIALSAFE.name());
                    licenceKeyModel.setKeyValue(licenceKeyResponse
                            .getKeyResponse().getKeyData());
                    try {
                        dao.getLicenseKeyDAO().insert(licenceKeyModel);
                    } catch (DAOException ex) {
                        logger.error(
                                "Error while inserting into licence key: ", ex);
                    }
                    licenceKey = licenceKeyModel.getKeyValue();
                } else {
                    logger.error("Failed to get the license key. Error code is: "
                            + licenceKeyResponse.getErrorCode()
                            + " and reason : "
                            + licenceKeyResponse.getErrorMessage());
                }
            }
        }
        return licenceKey;
    }

    public static boolean isDependentCloud(String cloudName) {
        boolean isDependentCloud = false;
        DependentCloudDAO dependentCloudDAO = DAOFactory.getInstance()
                .getDependentCloudDAO();
        try {
            DependentCloudModel dependentCloudModel = dependentCloudDAO
                    .getDependent(cloudName);
            if (dependentCloudModel != null) {
                isDependentCloud = true;
            }
        } catch (DAOException e) {
            logger.error("Error getting dependent: ", e);
        }
        return isDependentCloud;
    }
}
