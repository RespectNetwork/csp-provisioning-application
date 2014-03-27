package net.respectnetwork.csp.application.invite;

import java.util.Map;

import net.respectnetwork.csp.application.csp.CSP;
import net.respectnetwork.csp.application.csp.CSPStore;
import net.respectnetwork.csp.application.dao.DAOException;
import net.respectnetwork.csp.application.dao.DAOFactory;
import net.respectnetwork.csp.application.model.InviteModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InvitationManager  {
    
    /** Class Logger */
    private static final Logger logger = LoggerFactory
            .getLogger(InvitationManager.class);
    
    /** CSP Store */
    private CSPStore cspStore;
    
        
    /**
     *  Invitation Service
     */
    private Inviter invitationService;
        

    /**
     * @return the invitationService
     */
    public Inviter getInvitationService() {
        return invitationService;
    }
    
    public CSPStore getCspStore() {
        return cspStore;
    }
    
    public CSPStore getCspStore(boolean includeRN) {
        
        CSPStore newStore = cspStore.clone() ;
        if(!includeRN) {
            Map<String, CSP> tmpMap = newStore.getCspMap();
            tmpMap.remove("respectnetwork");
            newStore.setCspMap(tmpMap);
        }
        
        return newStore;
    }
    


    public void setCspStore(CSPStore cspStore) {
        this.cspStore = cspStore;
    }

    /**
     * @param invitationService the invitationService to set
     */
    
    public void setInvitationService(Inviter invitationService) {
        this.invitationService = invitationService;
    }
    
     
    /**
     * Create Invitation URL
     * 
     * @param cloudName
     * @return
     */
    public String createCSPInvitationURL(String cloudName, String csp, boolean full){
        String fullURL = null;
        CSP theCSP = cspStore.getCSPbyIdentifier(csp);
        String inviterCode = invitationService.createInviterCode(csp, cloudName); 
        if ( full) {
            fullURL =  theCSP.getInviteURL() +  "?inviter=" + theCSP.getInviteCode() + "~" + inviterCode; 
        } else {
            fullURL = theCSP.getInviteURL() +  "?inviter=" + inviterCode;     
        }
        return fullURL;
    }
    
    /**
     * Create Invitation URL
     * 
     * @param cloudName
     * @return
     */
    public String createInvitationURLAtRN(String cloudName, String csp, boolean full){
        
        String inviterCode = invitationService.createInviterCode(csp, cloudName); 
        CSP theCSP = cspStore.getCSPbyIdentifier(csp);
        CSP rnCSP = cspStore.getCSPbyIdentifier("respectnetwork");
        
        String fullURL = "";
        if ( full) {
            fullURL =  rnCSP.getInviteURL() +  "?inviter=" + theCSP.getInviteCode() + "~" + inviterCode; 
        } else {
            fullURL =  rnCSP.getInviteURL() +  "?inviter=" + inviterCode; 
        }
        return fullURL;
    }
    
    /**
     * Get Inviter Code Details
     * 
     * @param cloudName
     * @return
     */
    public String getCloudNameFromInviterCode(String inviterCode, String csp) {
             
        String cloudName = null;
        
        if ( inviterCode != null) {
            if ( inviterCode.contains("~")) { //long Form
                String cspcode = getCSPCode(inviterCode);
                cloudName = getCSPNameFromInviteCode(cspcode);
            } else  { //Short form
                //Look for RN or other CSP Codes
                cloudName = getCSPNameFromInviteCode(inviterCode);
                if (cloudName == null) {
                    //Look in the local Invitation Service
                    cloudName = invitationService.getCloudNameFromInviterCode(inviterCode, csp); 
                }
            }
        }

        return cloudName;
    }
    
    
    /**
     * 
     * @param identifier
     * @return
     */
    public CSP getCSP(String identifier){
        CSP theCSP = cspStore.getCSPbyIdentifier(identifier);
        return theCSP;       
    }
    
    /**
     * Is Valid RN Code
     * @return
     */
    public boolean isValidRNCode(String code) {
        boolean valid = false;
        CSP theCSP = cspStore.getCSPbyIdentifier("respectnetwork");
        
        if (code.equalsIgnoreCase(theCSP.getInviteCode())){
            valid = true;
        } else if(code.contains("~")){
            String[] codes = code.split("~");
            if ( codes[0] != null && codes[0].equalsIgnoreCase(theCSP.getInviteCode())) {
                valid = true;
            }
        }       
        return valid;
                        
    }
      
    /**
     * Get CSP Code: Get the First  ~ delimited segment.
     * @return
     */
    public String getCSPCode(String code) {
        String cspCode = null;
        if(code.contains("~")){
            String[] codes = code.split("~");
            if ( codes[0] != null ) {
                cspCode = codes[0] ;
            }
        } else {
            cspCode = code;
        }      
        return cspCode;                       
    }
    
    /**
     * Is Valid RN Code
     * @return
     */
    public String getUserCode(String code) {
        String userCode = null;
        if(code.contains("~")){
            String[] codes = code.split("~");
            if ( codes[(codes.length)-1] != null ) {
                userCode = codes[(codes.length)-1] ;
            }
        } else {
            userCode = code;
        }            
        return userCode;                      
    }
    
    
    /**
     * Is Valid RN Code
     * @return
     */
    public String getCSPNameFromInviteCode(String code) {
        //@TODO Very Inefficient: Need something better.
        
        String name = null;
        for (@SuppressWarnings("rawtypes") Map.Entry entry : cspStore.getCspMap().entrySet()) {
            logger.debug("Key/Value: {} -> {}", entry.getKey(), entry.getValue() );
            String cspCode = ((CSP)entry.getValue()).getInviteCode();
            if (cspCode.equalsIgnoreCase(code)){
                logger.debug("Match for {} = {}", code ,entry.getKey() );
                name = ((CSP)entry.getValue()).getDisplayName();              
            }
        } 
        
        return name;       
    }

    /**
     * get inviter name from invite code when the invitation is created by a person
     */
      public String getInviterName(String inviterCode){
    	  
    	  logger.debug("Looking up inviter name for " + inviterCode);
    	  String inviterName = null;
    	  //lookup the inviter name from DB
    	  try {
    		  InviteModel invite = DAOFactory.getInstance().getInviteDAO().get(inviterCode);
    		  if(invite != null){
    			  inviterName = invite.getInviterCloudName();
    			  logger.debug("Inviter name " + inviterName);
    		  }
		} catch (DAOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	return inviterName;
      }
    
              
}
