package net.respectnetwork.csp.application.form;


import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;


/**
 * Class for Data used in Confirmation and Payments Form 
 *
 */
public class PaymentForm {
	
    /** Gift Code */
    private String giftCode;

    /** CSP Terms and Conditions */
    private boolean cspTandC;
    
    /** CardNumber */
    private String cardNumber;

    /** CCV */
    private String cvv;

    /** Exp. Month */
    private String expMonth;
    
    /** Exp. Year */
    private String expYear;

    
    
	/**
     * @return the Gift Code
     */
    public String getGiftCode() {
        return giftCode;
    }

    /**
     * @param giftCode the giftCode to set
     */
    public void setGiftCode(String giftCode) {
        this.giftCode = giftCode;
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
     * To String Implementation.
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("[giftCode=").append(giftCode)
            .append(", cspTandC=").append(cspTandC)
            .append(", cardNumber=").append(cardNumber)
            .append(", ccv=").append(cvv)
            .append(", expMonth=").append(expMonth)
            .append(", expYear=").append(expYear)
            .append("]");
        return builder.toString();
    }
    
    /**
     * HashCode Implementation using apache-lang
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder()
        .append(giftCode)
        .append(cspTandC)
        .append(cardNumber)
        .append(cvv)
        .append(expMonth)
        .append(expYear)
        .toHashCode();
    }
    
    /**
     * Equals Implementation using apache-lang
     */
    @Override
    public boolean equals(Object obj) {
        if(obj instanceof PaymentForm){
            final PaymentForm other = (PaymentForm) obj;
            return new EqualsBuilder()
                .append(giftCode, other.giftCode)
                .append(cspTandC, other.cspTandC)
                .append(cardNumber, other.cardNumber)
                .append(cvv, other.cvv)
                .append(expMonth, other.expMonth)
                .append(expYear, other.expYear)
                .isEquals();
        } else{
            return false;
        }
    }
	
}
