package net.respectnetwork.csp.application.form;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;


public class SignUpForm {
    
    private String cloudName;
   
    /** invite code */
    private String inviteCode;
    
    /** gift code */
    private String giftCode;
       
    private String nameAvailabilityCheckURL ;
    /**
     * @return the cloudName
     */
    public String getCloudName() {
        return cloudName;
    }
    /**
     * @param cloudName the cloudName to set
     */
    public void setCloudName(String cloudName) {
        this.cloudName = cloudName;
    }
 

    
    
    
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("MessageForm [cloudName=").append(cloudName)
                .append("]");
        return builder.toString();
    }
    
    /**
     * Hash Implementation using apache-lang
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder()
        .append(cloudName)
        .toHashCode();
    }


    /**
     * Equals Implementation using apache-lang
     */
    @Override
    public boolean equals(Object obj) {
        if(obj instanceof SignUpForm){
            final SignUpForm other = (SignUpForm) obj;
            return new EqualsBuilder()
                .append(cloudName, other.cloudName)
                .isEquals();
        } else{
            return false;
        }
    }
	public String getInviteCode() {
		return inviteCode;
	}
	public void setInviteCode(String inviteCode) {
		this.inviteCode = inviteCode;
	}
	public String getGiftCode() {
		return giftCode;
	}
	public void setGiftCode(String giftCode) {
		this.giftCode = giftCode;
	}

    public void setNameAvailabilityCheckURL(String url) {
        this.nameAvailabilityCheckURL = url;
    }

    public String getNameAvailabilityCheckURL() {
        return this.nameAvailabilityCheckURL;
    }
}
