package net.respectnetwork.csp.application.controller;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import net.respectnetwork.csp.application.form.InviterForm;
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
	@RequestMapping(value = "/", method = RequestMethod.GET)
	public ModelAndView homeForm(HttpServletRequest request, Model model,
	        @Valid @ModelAttribute("inviteInfo") InviterForm inviterForm) {
	    
		logger.info("Welcome to CSP Home");
		   
        String inviter = null;
        String cloudName = null;
        
        ModelAndView mv = null; 
        mv = new ModelAndView("csphome");
        
        try {
           
            inviter = (String)request.getParameter("inviter");      
            if (inviter !=null){
                cloudName = invitationManager.getCloudNameFromInviterCode(inviter, cspName);
                
                if (cloudName == null) {
                    mv.addObject("error", "Invalid Inviter Code: " + inviter);  
                    inviterForm.setInviterCloudName(cloudName);
                } else {
                    inviterForm.setInviterCloudName(cloudName);
                }    
            }
            
        } catch (Exception e) {
            logger.warn("Problem Processing", e.getMessage());
        }
        
        mv.addObject("inviteInfo", inviterForm);
        
        

		return mv;
	}
	

}
