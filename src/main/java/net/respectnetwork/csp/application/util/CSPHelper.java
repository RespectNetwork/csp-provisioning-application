package net.respectnetwork.csp.application.util;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import javax.servlet.http.HttpServletRequest;

import net.respectnetwork.csp.application.manager.RegistrationManager;

import org.springframework.web.servlet.ModelAndView;

public class CSPHelper {

    private static RegistrationManager registrationManager;

    public CSPHelper(RegistrationManager registrationManager) {
        CSPHelper.registrationManager = registrationManager;
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
}
