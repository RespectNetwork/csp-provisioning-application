package net.respectnetwork.csp.application.form;


import java.math.BigDecimal;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;


/**
 * Class for Data used in Confirmation and Payments Form 
 *
 */
public class PaymentForm {
	
   public final static String GIFTCARD_PAYMENT = "giftCard";
   public final static String CC_PAYMENT = "creditCard";
   
   public final static String TXN_TYPE_SIGNUP = "signup"; 
   public final static String TXN_TYPE_BUY_GC = "buyGiftCard";
   public final static String TXN_TYPE_DEP = "buyDependentCloud";
   

   private String giftCodes;
   
    /** CSP Terms and Conditions */
    private boolean cspTandC;

    
    /** customer email */    
    String customerEmail ;
    
    /** customer name */
    String customerName ;

    
    /** signup or buyGiftCard or buyDependentCloud */
    String txnType ;
    
    /** number of clouds being purchased */
    int numberOfClouds;
    
    public PaymentForm()
    {
       this.giftCodes = "";
       this.cspTandC = false;
       this.txnType = null;
       this.customerEmail = null;
       this.customerName = null;
       this.numberOfClouds = 0;
    }

   public PaymentForm(PaymentForm paymentFormIn)
   {
      this.giftCodes = paymentFormIn.giftCodes;
      this.cspTandC = paymentFormIn.cspTandC;
      this.txnType = paymentFormIn.txnType;
      this.customerEmail = paymentFormIn.customerEmail;
      this.customerName = paymentFormIn.customerName;
      this.numberOfClouds = paymentFormIn.numberOfClouds;
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
     * To String Implementation.
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("[giftCodes=").append(giftCodes)
            .append(", cspTandC=").append(cspTandC)
            .append("]");
        return builder.toString();
    }
    
    /**
     * HashCode Implementation using apache-lang
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder()
        .append(giftCodes)
        .append(cspTandC)
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
                .append(giftCodes, other.giftCodes)
                .append(cspTandC, other.cspTandC)
                .isEquals();
        } else{
            return false;
        }
    }

   public String getGiftCodes()
   {
      return giftCodes;
   }

   public void setGiftCodes(String giftCodes)
   {
      this.giftCodes = giftCodes;
   }

   public String getCustomerEmail()
   {
      return customerEmail;
   }

   public void setCustomerEmail(String customerEmail)
   {
      this.customerEmail = customerEmail;
   }

   public String getCustomerName()
   {
      return customerName;
   }

   public void setCustomerName(String customerName)
   {
      this.customerName = customerName;
   }

   public String getTxnType()
   {
      return txnType;
   }

   public void setTxnType(String txnType)
   {
      this.txnType = txnType;
   }

   public int getNumberOfClouds()
   {
      return numberOfClouds;
   }

   public void setNumberOfClouds(int numberOfClouds)
   {
      this.numberOfClouds = numberOfClouds;
   }

	
}
