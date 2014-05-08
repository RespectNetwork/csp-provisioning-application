package net.respectnetwork.csp.application.form;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;


public class UserDetailsForm {
    
    /** eMail */
    private String email;
    
    /** Phone */
    private String mobilePhone;
    
    /** RN Terms and Conditions */
    private boolean rnTandC;
    
    /** Password */
    private String password; 
    
    /** Cloudname */
    private String cloudName;   
    
    private String cc;
    
    /** Confirm Password */
    private String confirmPassword;

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
    
    /**
     * @return the rnTandC
     */
    public boolean isRnTandC() {
        return rnTandC;
    }

    /**
     * @param rnTandC the rnTandC to set
     */
    public void setRnTandC(boolean rnTandC) {
        this.rnTandC = rnTandC;
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
        builder.append("MessageForm [email=").append(email)
                .append(", mobilePhone=").append(mobilePhone)
                .append(", rnTandC=").append(rnTandC)
                //.append(", password=").append(password)
                .append(", cloudName=").append(cloudName)
                .append("]");
        return builder.toString();
    }
    
    /**
     * Hash Implementation using apache-lang
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder()
        .append(email)
        .append(mobilePhone)
        .append(rnTandC)
        .append(password)
        .toHashCode();
    }


    /**
     * Equals Implementation using apache-lang
     */
    @Override
    public boolean equals(Object obj) {
        if(obj instanceof UserDetailsForm){
            final UserDetailsForm other = (UserDetailsForm) obj;
            return new EqualsBuilder()
                .append(email, other.email)
                .append(mobilePhone, other.mobilePhone)
                .append(rnTandC, other.rnTandC)
                .append(password, other.password)
                .append(cloudName, other.cloudName)
                .isEquals();
        } else{
            return false;
        }
    }
   public String getCloudName()
   {
      return cloudName;
   }
   public void setCloudName(String cloudName)
   {
      this.cloudName = cloudName;
   }
   public String getCc()
   {
      return cc;
   }
   public void setCc(String cc)
   {
      this.cc = cc;
   }
    
    public String getPhone()
    {
       return cc + "." +  mobilePhone;
    }
   public String getConfirmPassword()
   {
      return confirmPassword;
   }
   public void setConfirmPassword(String confirmPassword)
   {
      this.confirmPassword = confirmPassword;
   }
    
}
