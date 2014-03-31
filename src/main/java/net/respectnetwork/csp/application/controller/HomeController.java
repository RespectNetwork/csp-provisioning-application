package net.respectnetwork.csp.application.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

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
        boolean errors = false;
        
        try {
           
        	inviteCode = (String)request.getParameter(URL_PARAM_NAME_INVITE_CODE);
        	giftCode = (String)request.getParameter(URL_PARAM_NAME_GIFT_CODE);
        	selfInviteCode = (String)request.getParameter(URL_PARAM_NAME_INVITER);
        	
        	logger.debug("Invite Code = " + inviteCode);
            logger.debug("Gift Code = " + giftCode);
            logger.debug("Cloud Name : " + cloudName);
            
        	//if both invite code and gift code is present. This is the case when Alice is invited by Roger
            
            if (inviteCode !=null){
            	inviterCloudName = invitationManager.getInviterName(inviteCode);
            	
                if (inviterCloudName == null) {
                	mv = new ModelAndView("generalErrorPage");
                    mv.addObject("error", "Invalid Invite Code: " + inviteCode);  
                    logger.debug("Inviter cloud name is null. So, cannot proceed.");
                } else {
                	logger.debug("Valid invite code found.");
                	SignUpForm.setInviteCode(inviteCode);
                	if(giftCode != null) {
                		if ( DAOFactory.getInstance().getGiftCodeRedemptionDAO().get(giftCode) != null )
                		{
                			logger.debug("Invalid gift code. This gift code has already been redeemed. Id=" + giftCode);
                			mv = new ModelAndView("generalErrorPage");
                			mv.addObject("error", "This gift code has already been redeemed. So, a new personal cloud cannot be registered using this gift code. Id=" + giftCode); 
                		} else {
                			boolean codeMatch = false;
                			List<GiftCodeModel> giftCodes = DAOFactory.getInstance().getGiftCodeDAO().list(inviteCode);
                			for(GiftCodeModel gift : giftCodes){
                				if(gift.getGiftCodeId().equals(giftCode)){
                					codeMatch = true;
                					break;
                				}
                			}
                			if(codeMatch) {
                				SignUpForm.setGiftCode(giftCode);
                			} else {
                				logger.debug("Invalid gift code. Please check the URL. Id=" + giftCode);
                    			mv = new ModelAndView("generalErrorPage");
                    			mv.addObject("error", "Invalid gift code. Please check the URL. Id=" + giftCode);                				
                			}
                		}
                	}
                	
                }    
            } // the else case happens when Roger was not invited by anyone. He created an invite from RespectNetwork website
            // this flow will happen in "public launch"
            else if(selfInviteCode != null){
            	cloudName = invitationManager.getCloudNameFromInviterCode(selfInviteCode, cspName);
                
                if (cloudName == null) {
                	mv = new ModelAndView("generalErrorPage");
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
