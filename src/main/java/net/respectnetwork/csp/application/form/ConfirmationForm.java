package net.respectnetwork.csp.application.form;


import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;


/**
 * Class for Data used in Confirmation and Payments Form 
 *
 */
public class ConfirmationForm {
	
    /** Email Code */
    private String emailCode;

    /** SMS Code */
    private String smsCode;

    /** CSP Terms and Conditions */
    private boolean cspTandC;
    
    /** RN Terms and Conditions */
    private boolean rnTandC;

    /** CardNumber */
    private String cardNumber;

    /** CCV */
    private String cvv;

    /** Exp. Month */
    private String expMonth;
    
    /** Exp. Year */
    private String expYear;
    
    /** Password */
    private String password;  
    
	
    
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
     * @return the cspTandC
     */
    public boolean isCspTandC() {
        return cspTandC;
    }

    /**
     * @param cspTandC the cspTandC to set
     */
    public void setCspTandC(boolean cspTandC) {
        this.cspTandC = cspTandC;
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
     * @return the cardNumber
     */
    public String getCardNumber() {
        return cardNumber;
    }

    /**
     * @param cardNumber the cardNumber to set
     */
    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    /**
     * @return the cvv
     */
    public String getCvv() {
        return cvv;
    }

    /**
     * @param ccv the cvv to set
     */
    public void setCvv(String cvv) {
        this.cvv = cvv;
    }

    /**
     * @return the expMonth
     */
    public String getExpMonth() {
        return expMonth;
    }

    /**
     * @param expMonth the expMonth to set
     */
    public void setExpMonth(String expMonth) {
        this.expMonth = expMonth;
    }

    /**
     * @return the expYear
     */
    public String getExpYear() {
        return expYear;
    }

    /**
     * @param expYear the expYear to set
     */
    public void setExpYear(String expYear) {
        this.expYear = expYear;
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
     * To String Implementation.
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("[emailCode=").append(emailCode)
            .append(", smsCode=").append(smsCode)
            .append(", cspTandC=").append(cspTandC)
            .append(", rnTandC=").append(rnTandC)
            .append(", cardNumber=").append(cardNumber)
            .append(", ccv=").append(cvv)
            .append(", expMonth=").append(expMonth)
            .append(", expYear=").append(expYear)
            .append(", password=").append(password)
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
        .append(cspTandC)
        .append(rnTandC)
        .append(cardNumber)
        .append(cvv)
        .append(expMonth)
        .append(expYear)
        .append(password)
        .toHashCode();
    }
    
    /**
     * Equals Implementation using apache-lang
     */
    @Override
    public boolean equals(Object obj) {
        if(obj instanceof ConfirmationForm){
            final ConfirmationForm other = (ConfirmationForm) obj;
            return new EqualsBuilder()
                .append(emailCode, other.emailCode)
                .append(smsCode, other.smsCode)
                .append(cspTandC, other.cspTandC)
                .append(rnTandC, other.rnTandC)
                .append(cardNumber, other.cardNumber)
                .append(cvv, other.cvv)
                .append(expMonth, other.expMonth)
                .append(expYear, other.expYear)
                .append(password, other.password)
                .isEquals();
        } else{
            return false;
        }
    }
	
}
