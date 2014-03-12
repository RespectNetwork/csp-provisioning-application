/**
 * 
 */
package net.respectnetwork.csp.application.invite;

/**
 * Inviter Service Interface
 */
public interface Inviter {
    
    /**
     * Create Inviter Code 
     *
     */
    public String createInviterCode(String cloudName, String csp);
    
    /**
     * Get Inviter Code Details
     */   
    public String getCloudNameFromInviterCode(String inviterCode, String csp);
   
}
