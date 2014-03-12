package net.respectnetwork.csp.application.controller;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import net.respectnetwork.csp.application.csp.CSP;
import net.respectnetwork.csp.application.form.InvitationForm;
import net.respectnetwork.csp.application.invite.InvitationManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

/**
 * Handles requests for the application home page.
 */
@Controller
public class InviteController {

	private static final Logger logger = LoggerFactory
			.getLogger(InviteController.class);
	
	
	/** 
	 * Invitation Service
	 */
	private InvitationManager invitationManager;

	
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
     * Simply selects the home view to render by returning its name.
     */
    @RequestMapping(value = "/createInvitation/{csp}/**", method = RequestMethod.GET)
    public ModelAndView createInvitation(@PathVariable("csp") String csp, Model model) {
        logger.info("Create Invitation Page");
        
        ModelAndView mv = new ModelAndView("createInvitation");

        InvitationForm theForm = new InvitationForm();
        if (invitationManager.getCSP(csp) != null) {
            theForm.setCsp(csp);
            model.addAttribute("inviteInfo", theForm);
        } else {
            model.addAttribute("inviteInfo", theForm);
            mv.addObject("error", "No Such CSP configured");
        }

        return mv;
    }
    
    
    /**
     * Simply selects the home view to render by returning its name.
     */        
    @RequestMapping(value = "/processInvitation/{csp}/**", method = RequestMethod.POST)
    public ModelAndView processInvitation(
            @PathVariable("csp") String csp,
            @Valid @ModelAttribute("inviteInfo") InvitationForm invitationForm,
            HttpServletRequest request,
            BindingResult result) {
        
        logger.info("Create Invitation Page");
        
        ModelAndView mv = new ModelAndView("createInvitation");
        
        // Check if this is a form
        String cloudName = invitationForm.getCloudName();
        
        if (cloudName != null) {
            //Process Create Invitation Request
            logger.debug("Creating Invitation for {} : CSP {}", cloudName, csp);
            
            
            String inviteURL = invitationManager.createCSPInvitationURL(cloudName, csp, false);
            String rnInviteURL = invitationManager.createInvitationURLAtRN(cloudName, csp, true);
            invitationForm.setInviteURL(inviteURL);
            invitationForm.setRnInviteURL(rnInviteURL);
            
            mv.addObject("inviteInfo", invitationForm);
           
        }
        
        return mv;
    }
	
	 /**
     * Process the Invitation Request
     */
    @RequestMapping(value = "/radInvite", method = RequestMethod.GET)
    public ModelAndView invite(
            @Valid @ModelAttribute("inviteInfo") InvitationForm invitationForm,
            HttpServletRequest request,
            BindingResult result) {
        logger.info("Invitation Processing Page");
        
        String inviter = null;
        ModelAndView mv = new ModelAndView("cspregistration");
        
        String csp = "radiator";
        
        CSP theRNCSP = invitationManager.getCSP("respectnetwork");
        invitationForm.setRnInviteURL(theRNCSP.getInviteURL());
        CSP theCSP = invitationManager.getCSP("radiator");
        invitationForm.setRegURL(theCSP.getRegURL());
        
        
        try {
            inviter = (String)request.getParameter("inviter"); 
            String cloudName = "";
            if (invitationManager.isValidRNCode(inviter)){
                cloudName = "Respect Network";
            } else {
                cloudName = invitationManager.getCloudNameFromInviterCode(inviter, csp);
            }
            
            if (cloudName == null) { //No Match For Code.
                mv.addObject("error", "Invalid Inviter Code");
            } else {
                invitationForm.setCloudName(cloudName);
                invitationForm.setInviteCode(theCSP.getInviteCode() + "~" + inviter);
                mv.addObject("inviteInfo", invitationForm);          
            }               
            
        } catch (Exception e) {
            logger.warn("Problem Processing", e.getMessage());
        }

        return mv;
    }
    



}
