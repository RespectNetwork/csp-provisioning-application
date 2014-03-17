package net.respectnetwork.csp.application.form;


import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;


/**
 * Class for Data used in Confirmation and Payments Form 
 *
 */
public class ValidateForm {
	
    /** Email Code */
    private String emailCode;

    /** SMS Code */
    private String smsCode;

 
    
	/**
     * @return the emailCode
     */
    public String getEmailCode() {
        return emailCode;
    }

    /**
     * @param emailCode the emailCode to set
     */
    public void setEmailCode(String emailCode) {
        this.emailCode = emailCode;
    }

    /**
     * @return the smsCode
     */
    public String getSmsCode() {
        return smsCode;
    }

    /**
     * @param smsCode the smsCode to set
     */
    public void setSmsCode(String smsCode) {
        this.smsCode = smsCode;
    }

  

    /**
     * To String Implementation.
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("[emailCode=").append(emailCode)
            .append(", smsCode=").append(smsCode)
            .append("]");
        return builder.toString();
    }
    
    /**
     * HashCode Implementation using apache-lang
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder()
        .append(emailCode)
        .append(smsCode)
        .toHashCode();
    }
    
    /**
     * Equals Implementation using apache-lang
     */
    @Override
    public boolean equals(Object obj) {
        if(obj instanceof ValidateForm){
            final ValidateForm other = (ValidateForm) obj;
            return new EqualsBuilder()
                .append(emailCode, other.emailCode)
                .append(smsCode, other.smsCode)
                .isEquals();
        } else{
            return false;
        }
    }
	
}
