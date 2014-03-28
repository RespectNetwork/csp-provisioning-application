package net.respectnetwork.csp.application.controller;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import net.respectnetwork.csp.application.form.SignUpForm;
import net.respectnetwork.csp.application.invite.InvitationManager;

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
	 * Simply selects the home view to render by returning its name.
	 */
	@RequestMapping(value = "/signup", method = RequestMethod.GET)
	public ModelAndView homeForm(HttpServletRequest request, Model model,
	        @Valid @ModelAttribute("signUpInfo") SignUpForm SignUpForm) {
	    
	    
	    
		logger.info("Alice signs up by invite flow");
		   
        String inviteCode = null;
        String cloudName = null;
        String inviterCloudName = null;
        String giftCode = null;
        String selfInviteCode = null;
        
        ModelAndView mv = null; 
        mv = new ModelAndView("signup");
        
        try {
           
        	inviteCode = (String)request.getParameter(URL_PARAM_NAME_INVITE_CODE);
        	giftCode = (String)request.getParameter(URL_PARAM_NAME_GIFT_CODE);
        	selfInviteCode = (String)request.getParameter(URL_PARAM_NAME_INVITER);
        	
            
        	//if both invite code and gift code is present. This is the case when Alice is invited by Roger
            
            if (inviteCode !=null){
            	inviterCloudName = invitationManager.getInviterName(inviteCode);
            	
                if (inviterCloudName == null) {
                    mv.addObject("error", "Invalid Invite Code: " + inviteCode);  
                } else {
                	SignUpForm.setInviteCode(inviteCode);
                	if(giftCode != null) {
                		SignUpForm.setGiftCode(giftCode);
                	}
                	
                }    
            } // the else case happens when Roger was not invited by anyone. He created an invite from RespectNetwork website
            // this flow will happen in "public launch"
            else if(selfInviteCode != null){
            	cloudName = invitationManager.getCloudNameFromInviterCode(selfInviteCode, cspName);
                
                if (cloudName == null) {
                    mv.addObject("error", "Invalid Inviter Code: " + selfInviteCode);  
                } else {
                    SignUpForm.setCloudName(cloudName);
                } 
            }
            
        } catch (Exception e) {
            logger.warn("Problem Processing", e.getMessage());
        }
        
        mv.addObject("signUpInfo", SignUpForm);
        
        

		return mv;
	}
	

}
