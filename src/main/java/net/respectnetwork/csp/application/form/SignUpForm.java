package net.respectnetwork.csp.application.form;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;


public class SignUpForm {
    
    private String cloudName;
    private String email;
    private String mobilePhone;
    
       
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
    /**
     * @return the email
     */
    public String getEmail() {
        return email;
    }
    /**
     * @param email the email to set
     */
    public void setEmail(String email) {
        this.email = email;
    }
    /**
     * @return the mobilePhone
     */
    public String getMobilePhone() {
        return mobilePhone;
    }
    /**
     * @param mobilePhone the mobilePhone to set
     */
    public void setMobilePhone(String mobilePhone) {
        this.mobilePhone = mobilePhone;
    }

    
    
    
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("MessageForm [cloudName=").append(cloudName)
                .append(", email=").append(email)
                .append(", mobilePhone=").append(mobilePhone)
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
        .append(email)
        .append(mobilePhone)
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
                .append(email, other.email)
                .append(mobilePhone, other.mobilePhone)
                .isEquals();
        } else{
            return false;
        }
    }
    
    
    
}
