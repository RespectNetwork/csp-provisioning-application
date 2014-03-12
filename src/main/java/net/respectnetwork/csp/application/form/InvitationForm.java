package net.respectnetwork.csp.application.form;


import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;


/**
 * Form Class for Invitation Page
 * 
 */
public class InvitationForm {
	
    /**  Cloud Name */
	private String cloudName;
	
	/** Invite URL */
	private String inviteURL;
	
	/** Invite URL */
    private String rnInviteURL;
	
    /** Registration URL */
    private String regURL;
    
    /** Invite URL */
    private String inviteCode;
    
    /** CSP */
    private String csp;
	
	public String getCloudName() {
		return cloudName;
	}
	public void setCloudName(String cloudName) {
		this.cloudName = cloudName;
	}
	public String getInviteURL() {
		return inviteURL;
	}
	public void setInviteURL(String inviteURL) {
		this.inviteURL = inviteURL;
	}
	
	public String getInviteCode() {
        return inviteCode;
    }
    public void setInviteCode(String inviteCode) {
        this.inviteCode = inviteCode;
    }
    public String getRnInviteURL() {
        return rnInviteURL;
    }
    public void setRnInviteURL(String rnInviteURL) {
        this.rnInviteURL = rnInviteURL;
    }
    public String getRegURL() {
        return regURL;
    }
    public void setRegURL(String regURL) {
        this.regURL = regURL;
    }
    public String getCsp() {
        return csp;
    }
    public void setCsp(String csp) {
        this.csp = csp;
    }
    /**
     * To String Implementation.
     */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("InvitationForm [cloudName=").append(cloudName)
		       .append(", inviteURL=").append(inviteURL)
		       .append(", rnInviteURL=").append(rnInviteURL)
		       .append(", regURL=").append(regURL)
		       .append(", inviteCode=").append(inviteCode)
		       .append(", csp=").append(csp)
		       .append("]");
		return builder.toString();
	}
	
    /**
     * HashCode Implementation using apache-lang
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder()
        .append(cloudName)
        .append(inviteURL)
        .append(rnInviteURL)
        .append(regURL)
        .append(inviteCode)
        .append(csp)
        .toHashCode();
    }
    
    /**
     * Equals Implementation using apache-lang
     */
    @Override
    public boolean equals(Object obj) {
        if(obj instanceof InvitationForm){
            final InvitationForm other = (InvitationForm) obj;
            return new EqualsBuilder()
                .append(cloudName, other.cloudName)
                .append(inviteURL, other.inviteURL)
                .append(rnInviteURL, other.rnInviteURL)
                .append(regURL, other.regURL)
                .append(inviteCode, other.inviteCode)
                .append(csp, other.csp)
                .isEquals();
        } else{
            return false;
        }
    }
	
	
	
}
