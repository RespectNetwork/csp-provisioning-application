package net.respectnetwork.csp.application.csp;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

/**
 *  Representation of a CSP 
 *
 */
public class CSP {
    
    /** Identifier, also  used as path selector*/
    String identifier;
    
    /** Display Name*/
    String displayName;
    
    /** Invite URL*/
    String inviteURL;
    
    /** Home URL */
    String homeURL;
    
    /** InviteCode */
    String inviteCode;
    
    /** Registration URL */
    String regURL;

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getInviteURL() {
        return inviteURL;
    }

    public void setInviteURL(String inviteURL) {
        this.inviteURL = inviteURL;
    }

    public String getHomeURL() {
        return homeURL;
    }

    public void setHomeURL(String homeURL) {
        this.homeURL = homeURL;
    }

    public String getInviteCode() {
        return inviteCode;
    }

    public void setInviteCode(String inviteCode) {
        this.inviteCode = inviteCode;
    }
    
    public String getRegURL() {
        return regURL;
    }

    public void setRegURL(String regURL) {
        this.regURL = regURL;
    }

    /**
     * To String Implementation.
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("CSP [identifier=").append(identifier)
            .append("][displayName=").append(displayName)
            .append("][inviteCode=").append(inviteCode)
            .append("][inviteURL=").append(inviteURL)
            .append("][homeURL=").append(homeURL)
            .append("][regURL=").append(regURL)
            .append("]");
        return builder.toString();
    }
    
    /**
     * HashCode Implementation using apache-lang
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder()
        .append(identifier)
        .append(displayName)
        .append(inviteCode)
        .append(inviteURL)
        .append(homeURL)
        .append(regURL)
        .toHashCode();
    }
    
    /**
     * Equals Implementation using apache-lang
     */
    @Override
    public boolean equals(Object obj) {
        if(obj instanceof CSP){
            final CSP other = (CSP) obj;
            return new EqualsBuilder()
                .append(identifier, other.identifier)
                .append(displayName, other.displayName)
                .append(inviteCode, other.inviteCode)       
                .append(inviteURL, other.inviteURL)       
                .append(homeURL, other.homeURL)    
                .append(regURL, other.regURL)
                .isEquals();
        } else{
            return false;
        }
    }

}
