package net.respectnetwork.csp.application.session;

import java.io.Serializable;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

public class RegistrationSession implements Serializable {

    /** Generated Serial ID */
    private static final long serialVersionUID = -5040056689025758642L;
    
    
    /** Session Id used in Registration */
    private String sessionId;
    
    /** CloudName used in Registration */
    private String cloudName;

    /** Verified Email used in Registration */
    private String verifiedEmail;

    /** Verified Mobile Phone used in Registration */
    private String verifiedMobilePhone;
    
    /** Password */
    private String password;


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

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("MessageForm [sessionId=").append(sessionId)
                .append(", cloudName=").append(cloudName)
                .append(", verifiedEmail=").append(verifiedEmail)
                .append(", verifiedMobilePhone=").append(verifiedMobilePhone)
                .append(", password=").append(password)                
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
                    .isEquals();
        } else {
            return false;
        }
    }

}
