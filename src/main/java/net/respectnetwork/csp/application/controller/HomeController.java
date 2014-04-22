package net.respectnetwork.csp.application.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import net.respectnetwork.csp.application.dao.DAOException;
import net.respectnetwork.csp.application.dao.DAOFactory;
import net.respectnetwork.csp.application.form.SignUpForm;
import net.respectnetwork.csp.application.invite.InvitationManager;
import net.respectnetwork.csp.application.model.GiftCodeModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

/**
 * Handles requests for the application home page.
 */
@Controller
public class HomeController {

    /** CLass Logger */
	private static final Logger logger = LoggerFactory
			.getLogger(HomeController.class);
	
    public static final String URL_PARAM_NAME_INVITE_CODE = "invitecode";
    public static final String URL_PARAM_NAME_GIFT_CODE   = "giftcode"  ;
    public static final String URL_PARAM_NAME_INVITER     = "inviter"   ;

	/** 
     * Invitation Service
     */
    private InvitationManager invitationManager;
    
    
    /** 
     * CSP Name
     */
    private String cspName;
    
    /**
     * @return the invitationManager
     */
    public InvitationManager getInvitationManager() {
        return invitationManager;
    }


    /**
     * @param invitationManager the invitationManager to set
     */
    @Autowired
    @Required
    public void setInvitationManager(InvitationManager invitationManager) {
        this.invitationManager = invitationManager;
    }


	/**
     * @return the csp
     */
    public String getCspName() {
        return cspName;
    }


    /**
     * @param csp the cspName to set
     */
    @Autowired
    @Qualifier("cspName")
    public void setCspName(String cspName) {
        this.cspName = cspName;
    }


    /**
	 * This is the page the user lands if s/he came from the CSP site. There will be no invite code (ref code in the wire diagrams) when they come here.
	 * The processing on submit of this form is done in RegistrationController.
	 */
	@RequestMapping(value = "/signup", method = RequestMethod.GET)
	public ModelAndView homeForm(HttpServletRequest request, Model model,
	        @Valid @ModelAttribute("signUpInfo") SignUpForm signUpForm) {
	    
	    
	    
		logger.info("Alice signs up by invite flow");
		   
        String inviteCode = null;
        String cloudName = null;
        String inviterCloudName = null;
        String giftCode = null;
        String selfInviteCode = null;
        
        ModelAndView mv = null; 
        mv = new ModelAndView("signup");
        boolean errors = false;
        	inviteCode = (String)request.getParameter(URL_PARAM_NAME_INVITE_CODE);
        	giftCode = (String)request.getParameter(URL_PARAM_NAME_GIFT_CODE);
        	cloudName = (String)request.getParameter("name");

        	logger.debug("Invite Code = " + inviteCode);
         logger.debug("Gift Code = " + giftCode);
         logger.debug("Cloud Name : " + cloudName);
         
         if(signUpForm == null)
         {
            signUpForm = new SignUpForm();
         }
         if(giftCode != null && !giftCode.isEmpty())
         {
            try
            {
               if(DAOFactory.getInstance().getGiftCodeRedemptionDAO().get(giftCode) != null)
               {
                  logger.debug("Invalid gift code. This gift code has already been redeemed. Id=" + giftCode);
                  mv = new ModelAndView("generalErrorPage");
                  mv.addObject("error", "This gift code has already been redeemed. So, a new personal cloud cannot be registered using this gift code. Id=" + giftCode);
                  return mv;
               }
            } catch (DAOException e)
            {
               // TODO Auto-generated catch block
               e.printStackTrace();
               logger.debug("System error");
               mv = new ModelAndView("generalErrorPage");              
               return mv;
            }
            signUpForm.setGiftCode(giftCode);
         }
         if(cloudName != null && !cloudName.isEmpty())
         {
            signUpForm.setCloudName(cloudName);
         }
         
        mv.addObject("signUpInfo", signUpForm);
        return mv;
	}
	

}
