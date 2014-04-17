package net.respectnetwork.csp.application.session;

import java.io.Serializable;
import java.util.Map;

import net.respectnetwork.csp.application.form.DependentForm;
import net.respectnetwork.csp.application.form.InviteForm;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

public class RegistrationSession implements Serializable {

    /** Generated Serial ID */
    private static final long serialVersionUID = -5040056689025758642L;
    
    
    /** Session Id used in Registration */
    private String sessionId = null;
    
    /** CloudName used in Registration */
    private String cloudName = null;

    /** Verified Email used in Registration */
    private String verifiedEmail = null;

    /** Verified Mobile Phone used in Registration */
    private String verifiedMobilePhone ;
    
    /** Password */
    private String password = null;
    
    /** invite code */
    private String inviteCode = null;
    
    /** gift code */
    private String giftCode  = null;

    /** invite form */
    private InviteForm inviteForm = null;

    /** invite form */
    private DependentForm dependentForm = null;
    
    /** URL parameter list that comes from RN*/
    
    private Map<String,String[]> RNParamMap = null;
    
    /** Query String from RN to be relayed back */
    private String rnQueryString = null;
    
    /** RN post registration URL */
    private String RNPostRegistrationURL = null;
    
    /** Transaction Type */
    private String transactionType = null;
    
    /**
     * @return the SessionId
     */
    public String getSessionId() {
        return sessionId;
    }

    /**
     * @param sessionId  the sessionId to set
     */
    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }
    
    /**
     * @return the cloudName
     */
    public String getCloudName() {
        return cloudName;
    }

    /**
     * @param cloudName  the cloudName to set
     */
    public void setCloudName(String cloudName) {
        this.cloudName = cloudName;
    }

    /**
     * @return the email
     */
    public String getVerifiedEmail() {
        return verifiedEmail;
    }

    /**
     * @param email the email to set
     */
    public void setVerifiedEmail(String verifiedEmail) {
        this.verifiedEmail = verifiedEmail;
    }

    /**
     * @return the mobilePhone
     */
    public String getVerifiedMobilePhone() {
        return verifiedMobilePhone;
    }

    /**
     * @param mobilePhone the mobilePhone to set
     */
    public void setVerifiedMobilePhone(String verifiedMobilePhone) {
        this.verifiedMobilePhone = verifiedMobilePhone;
    }

    /**
     * @return the password
     */
    public String getPassword() {
        return password;
    }

    /**
     * @param password the password to set
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * @return the inviteForm
     */
    public InviteForm getInviteForm() {
        return inviteForm;
    }

    /**
     * @param inviteForm the inviteForm to set
     */
    public void setInviteForm(InviteForm inviteForm) {
        this.inviteForm = inviteForm;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("MessageForm [sessionId=").append(sessionId)
                .append(", cloudName=").append(cloudName)
                .append(", verifiedEmail=").append(verifiedEmail)
                .append(", verifiedMobilePhone=").append(verifiedMobilePhone)
                .append(", password=").append(password)                
                .append(", inviteForm=").append(inviteForm)                
                .append("]");
        return builder.toString();
    }

    /**
     * Hash Implementation using apache-lang
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(sessionId)
                .append(cloudName)
                .append(verifiedEmail)
                .append(verifiedMobilePhone)
                .append(password)                
                .append(inviteForm)                
                .toHashCode();
    }

    /**
     * Equals Implementation using apache-lang
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof RegistrationSession) {
            final RegistrationSession other = (RegistrationSession) obj;
            return new EqualsBuilder().append(sessionId, other.sessionId)
                    .append(cloudName, other.cloudName)
                    .append(verifiedEmail, other.verifiedEmail)
                    .append(verifiedMobilePhone, other.verifiedMobilePhone)
                    .append(password, other.password)
                    .append(inviteForm, other.inviteForm)
                    .isEquals();
        } else {
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

	public DependentForm getDependentForm() {
		return dependentForm;
	}

	public void setDependentForm(DependentForm dependentForm) {
		this.dependentForm = dependentForm;
	}

   public Map<String, String[]> getRNParamMap()
   {
      return RNParamMap;
   }

   public void setRNParamMap(Map<String, String[]> rNParamMap)
   {
      RNParamMap = rNParamMap;
   }

   public String getRNPostRegistrationURL()
   {
      return RNPostRegistrationURL;
   }

   public void setRNPostRegistrationURL(String rNPostRegistrationURL)
   {
      RNPostRegistrationURL = rNPostRegistrationURL;
   }

   public String getTransactionType()
   {
      return transactionType;
   }

   public void setTransactionType(String transactionType)
   {
      this.transactionType = transactionType;
   }

   public String getRnQueryString()
   {
      return rnQueryString;
   }

   public void setRnQueryString(String rnQueryString)
   {
      this.rnQueryString = rnQueryString;
   }

}
