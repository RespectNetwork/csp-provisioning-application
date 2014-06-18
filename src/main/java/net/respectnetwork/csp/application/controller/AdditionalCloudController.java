package net.respectnetwork.csp.application.controller;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import net.respectnetwork.csp.application.csp.CurrencyCost;
import net.respectnetwork.csp.application.dao.DAOException;
import net.respectnetwork.csp.application.dao.DAOFactory;
import net.respectnetwork.csp.application.exception.UserRegistrationException;
import net.respectnetwork.csp.application.form.AdditionalCloudForm;
import net.respectnetwork.csp.application.form.PaymentForm;
import net.respectnetwork.csp.application.manager.AdditionalCloudManager;
import net.respectnetwork.csp.application.manager.RegistrationManager;
import net.respectnetwork.csp.application.model.CSPModel;
import net.respectnetwork.csp.application.session.RegistrationSession;
import net.respectnetwork.csp.application.util.CSPHelper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import xdi2.client.exceptions.Xdi2ClientException;
import xdi2.core.xri3.CloudName;

/**
 * @author psharma2
 * 
 */
@Controller
public class AdditionalCloudController {
    // Class Logger
    private static final Logger logger = LoggerFactory
            .getLogger(AdditionalCloudController.class);

    private RegistrationManager theManager;
    private RegistrationSession regSession;
    private String cspCloudName;

    public RegistrationSession getRegSession() {
        return regSession;
    }

    @Autowired
    public void setRegSession(RegistrationSession regSession) {
        this.regSession = regSession;
    }

    public String getCspCloudName() {
        return this.cspCloudName;
    }

    @Autowired
    @Qualifier("cspCloudName")
    public void setCspCloudName(String cspCloudName) {
        this.cspCloudName = cspCloudName;
    }

    /**
     * This method render buy additional cloud page.
     * 
     * @param request
     * @param model
     * @return
     * @throws DAOException
     */
    @RequestMapping(value = "/additionalCloud", method = RequestMethod.GET)
    public ModelAndView showAdditionalCloudForm(HttpServletRequest request,
            Model model) throws DAOException {
        logger.info("Showing buy additional cloud names form.");

        String rnQueryString = "";
        Enumeration<String> paramNames = request.getParameterNames();
        while (paramNames.hasMoreElements()) {
            String paramName = paramNames.nextElement();
            logger.debug("p name " + paramName);
            String[] paramValues = request.getParameterValues(paramName);
            for (int i = 0; i < paramValues.length; i++) {
                logger.debug("p value " + paramValues[i]);
                // ignore the "name" parameter. Capture rest of it
                if (!paramName
                        .equalsIgnoreCase(RegistrationController.URL_PARAM_NAME_REQ_CLOUDNAME)) {
                    try {
                        rnQueryString = rnQueryString + "&" + paramName + "="
                                + URLEncoder.encode(paramValues[i], "UTF-8");
                    } catch (UnsupportedEncodingException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
        }

        String cloudName = request
                .getParameter(RegistrationController.URL_PARAM_NAME_REQ_CLOUDNAME); // this.getCloudName();

        logger.info("showing additional cloud page - " + cloudName);

        String cspHomeURL = request.getContextPath();
        ModelAndView mv = null;
        AdditionalCloudForm additionalCloudForm = null;
        CSPModel cspModel = null;

        if (cloudName == null || cloudName.isEmpty() || regSession == null
                || !cloudName.equalsIgnoreCase(regSession.getCloudName())) {
            mv = new ModelAndView("login");
            mv.addObject("postURL", cspHomeURL + "/cloudPage");
            return mv;
        }

        cspModel = DAOFactory.getInstance().getCSPDAO()
                .get(this.getCspCloudName());

        // Get cost override info
        CurrencyCost costPerCloudName = RegistrationController
                .getCostToBuyAdditionalCloudName(cspModel, "ACN", 1);
        regSession.setCurrency(costPerCloudName.getCurrencyCode());
        regSession.setCostPerCloudName(costPerCloudName.getAmount());

        mv = new ModelAndView("additionalCloud");
        mv.addObject("cspModel", cspModel);

        mv = new ModelAndView("additionalCloud");
        mv.addObject("cspModel", cspModel);

        List<String> additionalCloudNames = null;
        CloudName loggedinCloudName = CloudName.create(cloudName);
        try {
            AdditionalCloudManager acm = new AdditionalCloudManager(regSession,
                    theManager);
            additionalCloudNames = acm
                    .getAdditionalCloudNamesInCSP(loggedinCloudName);
        } catch (Xdi2ClientException ex2) {
            logger.error("Failed to fetch the cloudNumber for cloudName "
                    + cloudName);
            logger.error("Xdi2ClientException while getting cloud number "
                    + ex2.getMessage());

            mv = new ModelAndView("login");
            mv.addObject("postURL", cspHomeURL + "/cloudPage");
            return mv;
        }
        additionalCloudForm = new AdditionalCloudForm();
        if (additionalCloudNames != null) {
            additionalCloudForm.setAdditionalCloudNames(additionalCloudNames);
        }
        additionalCloudForm.setNameAvailabilityCheckURL(theManager
                .getNameAvailabilityCheckURL());
        model.addAttribute("additionalCloudForm", additionalCloudForm);
        model.addAttribute("cloudName", cloudName);

        if (regSession != null) {
            regSession.setTransactionType(PaymentForm.TXN_TYPE_ADD);
            regSession.setCloudName(cloudName);
            regSession.setRnQueryString(rnQueryString);
        }

        return mv;
    }

    /**
     * This method saves the additional cloud names.
     * 
     * @param additionalCloudForm
     * @param request
     * @param response
     * @return
     * @throws DAOException
     */
    @RequestMapping(value = "/saveAdditionalCloud", method = RequestMethod.POST)
    public ModelAndView saveAdditionalCloud(
            @Valid @ModelAttribute("additionalCloudInfo") AdditionalCloudForm additionalCloudForm,
            HttpServletRequest request, HttpServletResponse response)
            throws DAOException {
        logger.info("Save additional cloud.");

        String cloudName = this.getCloudName();
        boolean errors = false;

        ModelAndView mv = new ModelAndView("saveAdditionalCloud");
        mv.addObject("additionalCloudInfo", additionalCloudForm);

        String cspHomeURL = request.getContextPath();
        CSPModel cspModel = null;

        if (cloudName == null) {
            mv.addObject("error", "Invalid Session");
            mv.addObject("postURL", cspHomeURL + "/cloudPage");
            return mv;
        }

        cspModel = DAOFactory.getInstance().getCSPDAO()
                .get(this.getCspCloudName());

        List<String> additionalCloudameList = new ArrayList<String>();
        additionalCloudameList = additionalCloudForm.getAdditionalCloudNames();

        additionalCloudForm.setNameAvailabilityCheckURL(theManager
                .getNameAvailabilityCheckURL());
        String errorStr = "";

        int validNamesCount = 0;
        for (String additionalCloudName : additionalCloudameList) {
            if (!(additionalCloudName == null)) {
                if (!additionalCloudName.trim().isEmpty()) {
                    try {
                        logger.info("Checking for additional cloudname :"
                                + additionalCloudName);
                        if (!theManager
                                .isCloudNameAvailable(additionalCloudName)) {
                            errorStr += "\nCloudName not Available "
                                    + additionalCloudName;
                            errors = true;
                            break;
                        }
                    } catch (UserRegistrationException e) {
                        errorStr = "System Error checking CloudName";
                        logger.warn(errorStr + " : {}", e.getMessage());
                        errors = true;
                    }
                    validNamesCount++;
                }
            }
        }
        if (errors) {
            mv.addObject("cloudName", cloudName);
            mv.addObject("error", errorStr);
            return mv;

        }

        cspModel = DAOFactory.getInstance().getCSPDAO()
                .get(this.getCspCloudName());
        BigDecimal quantity = BigDecimal.valueOf((long) validNamesCount);

        // Cost (overriden if applicable) is stored in regSession
        String currency = regSession.getCurrency();
        BigDecimal totalCost = regSession.getCostPerCloudName().multiply(
                quantity);

        mv = new ModelAndView("payment");
        PaymentForm paymentForm = new PaymentForm();
        paymentForm.setTxnType(PaymentForm.TXN_TYPE_ADD);
        if (regSession != null) {
            regSession.setTransactionType(PaymentForm.TXN_TYPE_ADD);
        }
        paymentForm.setNumberOfClouds(validNamesCount);
        if (cspModel.getPaymentGatewayName().equals("GIFT_CODE_ONLY")) {
            paymentForm.setGiftCodesOnly(true);
        }
        mv.addObject("paymentInfo", paymentForm);
        mv.addObject(
                "totalAmountText",
                RegistrationController.formatCurrencyAmount(
                        regSession.getCurrency(), totalCost));
        mv.addObject("cspTCURL", this.getTheManager().getCspTCURL());

        this.setAdditionalcloudForm(additionalCloudForm);
        return mv;
    }

    /**
     * This method renders the page once all dependents are paid successfully.
     * 
     * @param model
     * @param request
     * @return
     * @throws DAOException
     */
    @RequestMapping(value = "/additionalCloudDone", method = {
            RequestMethod.GET, RequestMethod.POST })
    public ModelAndView showAdditionalCloudDoneForm(Model model,
            HttpServletRequest request) throws DAOException {
        ModelAndView mv = CSPHelper.getCloudPage(request,
                this.regSession.getCloudName());
        return mv;
    }

    public RegistrationManager getTheManager() {
        return theManager;
    }

    @Autowired
    public void setTheManager(RegistrationManager theManager) {
        this.theManager = theManager;
    }

    private String getCloudName() {
        String rtn = regSession.getCloudName();
        return rtn;
    }

    private void setAdditionalcloudForm(AdditionalCloudForm additionalcloudForm) {
        regSession.setAdditionalCloudForm(additionalcloudForm);
    }
}
