/**
 * 
 */
package net.respectnetwork.csp.application.controller;

import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import net.respectnetwork.csp.application.exception.PasswordValidationException;
import net.respectnetwork.csp.application.form.ChangePasswordForm;
import net.respectnetwork.csp.application.form.ForgotPasswordForm;
import net.respectnetwork.csp.application.form.ValidateForm;
import net.respectnetwork.csp.application.manager.PasswordManager;
import net.respectnetwork.csp.application.session.RegistrationSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

/**
 * Controller class to manage change and recover password flow.
 * 
 */
@Controller
public class PasswordController {

    /** Class Logger */
    private static final Logger logger = LoggerFactory
            .getLogger(PasswordController.class);

    /** Password Management Manager */
    private PasswordManager passwordManager;

    /** Registration Session */
    private RegistrationSession regSession;

    public static final String URL_PARAM_NAME_REQ_CLOUDNAME = "name";

    /**
     * 
     * @return
     */
    public PasswordManager getPasswordManager() {
        return passwordManager;
    }

    /**
     * 
     * @param passwordManagementManager
     */
    @Autowired
    public void setPasswordManager(PasswordManager passwordManager) {
        this.passwordManager = passwordManager;
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
     * This method render change password page.
     * 
     * @param request
     * @param model
     * @return
     */
    @RequestMapping(value = "/changePassword", method = RequestMethod.GET)
    public ModelAndView changePassword(HttpServletRequest request, Model model) {
        logger.info("showing change password form");
        boolean errors = false;
        String error = "";
        String cloudName = regSession.getCloudName();
        ModelAndView mv = new ModelAndView("changePassword");
        logger.debug("Referer URL " + request.getHeader("referer"));
        if (errors) {
            mv = new ModelAndView("changePassword");
            mv.addObject("error", error);
            return mv;
        }
        mv.addObject("cloudName", cloudName);
        return mv;
    }

    /**
     * This method update the password for a cloud name and on success send
     * control to the next page in flow.
     * 
     * @param changePasswordForm
     * @param request
     * @param response
     * @param result
     * @return
     */
    @RequestMapping(value = "/saveChangePassword", method = RequestMethod.POST)
    public ModelAndView updatePassword(
            @Valid @ModelAttribute("changePasswordInfo") ChangePasswordForm changePasswordForm,
            HttpServletRequest request, HttpServletResponse response,
            BindingResult result) {
        ModelAndView mv = null;
        boolean errors = false;
        String errorTxt = null;
        String forwardingPage = request.getContextPath();
        String method = "post";
        String statusText = "";

        mv = new ModelAndView("changePassword");
        mv.addObject("changePasswordInfo", changePasswordForm);

        String cloudName = regSession.getCloudName();
        String sessionId = regSession.getSessionId();
        if (cloudName == null) {
            cloudName = request
                    .getParameter(RegistrationController.URL_PARAM_NAME_REQ_CLOUDNAME);
        }
        // Session Check
        if (sessionId == null || cloudName == null) {
            mv.addObject("error", "Invalid Session");
            return mv;
        }
        try {
            passwordManager.changePassword(cloudName,
                    changePasswordForm.getCurrentPassword(),
                    changePasswordForm.getNewPassword());
            regSession.setPassword(changePasswordForm.getNewPassword());
        } catch (PasswordValidationException ex) {
            logger.error("Error while change password.", ex);
            errors = true;
            errorTxt = ex.getMessage();
        }
        if (errors) {
            mv.addObject("cloudName", cloudName);
            mv.addObject("error", errorTxt);
            return mv;
        }
        mv = new ModelAndView("postTxn");
        mv.addObject("cloudName", cloudName);
        forwardingPage += "/cloudPage";
        statusText = "Your password has been changed successfully.\n";
        mv.addObject("statusText", statusText);
        mv.addObject("postURL", forwardingPage);
        mv.addObject("submitMethod", method);
        mv.addObject("queryStr", "");
        return mv;
    }

    /**
     * This method render forgot password page.
     * 
     * @param request
     * @param model
     * @return
     */
    @RequestMapping(value = "/forgotPassword", method = RequestMethod.GET)
    public ModelAndView forgotPassword(HttpServletRequest request, Model model) {
        logger.info("showing forgot password page");
        ModelAndView mv = new ModelAndView("forgotPassword");

        String sessionId = UUID.randomUUID().toString();
        regSession.setSessionId(sessionId);

        return mv;
    }

    /**
     * This method render forgot password page.
     * 
     * @param request
     * @param model
     * @return
     */
    @RequestMapping(value = "/processResetPassword", method = RequestMethod.POST)
    public ModelAndView processResetPasswordDetails(
            @Valid @ModelAttribute("forgotPasswordInfo") ForgotPasswordForm forgotPasswordForm,
            BindingResult result, HttpServletRequest request,
            HttpServletResponse response) {

        logger.debug("Process recover password user details");

        ModelAndView mv = null;
        boolean errors = false;
        mv = new ModelAndView("forgotPassword");
        String cloudName = forgotPasswordForm.getCloudName();
        mv.addObject("forgotPasswordInfo", forgotPasswordForm);
        if (cloudName != null && !cloudName.startsWith("=")) {
            cloudName = "=" + cloudName;
        }
        try {
            passwordManager.verifyRecoverPasswordDetails(cloudName,
                    forgotPasswordForm.getEmailAddress(),
                    forgotPasswordForm.getPhoneNumber(), regSession.getSessionId());

        } catch (PasswordValidationException ex) {
            String errorStr = "System Error checking Email/Phone Number Uniqueness";
            logger.warn(errorStr + " : {}", ex.getMessage());
            mv.addObject("error", ex.getMessage());
            errors = true;
        }

        if (!errors) {
            mv = new ModelAndView("validate");
            ValidateForm validateForm = new ValidateForm();
            mv.addObject("validateInfo", validateForm);
            mv.addObject("cloudName", cloudName);
            mv.addObject("verifyingEmail", forgotPasswordForm.getEmailAddress());
            mv.addObject("verifyingPhone", forgotPasswordForm.getPhoneNumber());
            mv.addObject("resetPwd", true);
            // Add CloudName/ Email / Phone to Session

            logger.debug("Setting verified email "
                    + regSession.getVerifiedEmail());
            regSession.setCloudName(cloudName);
            regSession.setVerifiedEmail(forgotPasswordForm.getEmailAddress());
            regSession.setVerifiedMobilePhone(forgotPasswordForm
                    .getPhoneNumber());
        }
        return mv;
    }

    /**
     * This method update the password for a cloud name and on success send
     * control to the next page in flow.
     * 
     * @param changePasswordForm
     * @param request
     * @param response
     * @param result
     * @return
     */
    @RequestMapping(value = "/resetPassword", method = RequestMethod.POST)
    public ModelAndView resetPassword(
            @Valid @ModelAttribute("resetPasswordInfo") ChangePasswordForm changePasswordForm,
            HttpServletRequest request, HttpServletResponse response,
            BindingResult result) {
        ModelAndView mv = null;
        boolean errors = false;
        String errorTxt = null;
        String forwardingPage = request.getContextPath();
        String method = "post";
        String statusText = "";

        mv = new ModelAndView("resetPassword");
        mv.addObject("resetPasswordInfo", changePasswordForm);

        String cloudName = regSession.getCloudName();
        String sessionId = regSession.getSessionId();

        // Session Check
        if (sessionId == null || cloudName == null) {
            mv.addObject("error", "Invalid Session");
            return mv;
        }
        try {
            passwordManager.resetPassword(cloudName,
                    changePasswordForm.getNewPassword());
            regSession.setPassword(changePasswordForm.getNewPassword());
        } catch (PasswordValidationException ex) {
            logger.error("Error while reset password.", ex);
            errors = true;
            errorTxt = ex.getMessage();
        }
        if (errors) {
            mv.addObject("error", errorTxt);
            return mv;
        }
        mv = new ModelAndView("postTxn");
        forwardingPage += "/login";
        statusText = "Your password has been changed successfully.\n";
        mv.addObject("statusText", statusText);
        mv.addObject("postURL", forwardingPage);
        mv.addObject("submitMethod", method);
        mv.addObject("queryStr", "");
        return mv;
    }

}
