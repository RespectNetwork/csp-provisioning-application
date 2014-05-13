package net.respectnetwork.csp.application.model;

import java.util.Date;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

public class SignupInfoModel
{
   private String cloudName;
   private String email;
   private String phone;
   private String paymentType;
   private String paymentRefId;
   private Date timeCreated;
   public String getCloudName()
   {
      return cloudName;
   }
   public void setCloudName(String cloudName)
   {
      this.cloudName = cloudName;
   }
   public String getEmail()
   {
      return email;
   }
   public void setEmail(String email)
   {
      this.email = email;
   }
   public String getPhone()
   {
      return phone;
   }
   public void setPhone(String phone)
   {
      this.phone = phone;
   }
   public SignupInfoModel()
   {
      this.cloudName = null;
      this.email = null;
      this.phone = null;
      this.paymentType = null;
      this.paymentRefId = null;
      this.timeCreated = null;
   }
   
   public boolean equals( Object object )
   {
      if( object == null )
      {
         return false;
      }
      if( object == this )
      {
         return true;
      }
      if( this.getClass().equals(object.getClass()) == false )
      {
         return false;
      }
      SignupInfoModel other = (SignupInfoModel) object;
      return new EqualsBuilder()
            .appendSuper(super.equals(other))
            .append(this.cloudName, other.cloudName)
            .append(this.email, other.email)
            .append(this.phone, other.phone)
            .append(this.paymentType, other.paymentType)
            .append(this.paymentRefId, other.paymentRefId)
            .append(this.timeCreated, other.timeCreated)
            
            .isEquals();
   }

   public int hashCode()
   {
      return new HashCodeBuilder(17, 37)
            .appendSuper(super.hashCode())
            .append(this.cloudName)
            .append(this.email)
            .append(this.phone)
            .append(this.paymentType)
            .append(this.paymentRefId)
            .append(this.timeCreated)
            .toHashCode();
   }

   public String toString()
   {
      StringBuilder builder = new StringBuilder();
      builder.append("SignupInfoModel ");
      builder.append("[cloudName=")
          .append(this.cloudName).append(']');
      builder.append("[email=")
          .append(this.email).append(']');
      builder.append("[phone=")
          .append(this.phone).append(']');
      builder.append("[paymentType=")
      .append(this.paymentType).append(']');
      builder.append("[paymentRefId=")
      .append(this.paymentRefId).append(']');
      builder.append("[timeCreated=")
      .append(this.timeCreated).append(']');
      
      
      builder.append(' ');
      builder.append(super.toString());
      return builder.toString();
   }

   /**
    * Method to get payment type.
    *
    * @return paymentType. It could be either giftCode, creditCard or
    *         promoCode.
  */
  public String getPaymentType()
  {
      return this.paymentType;
  }

  /**
   * Method to set payment type.
   *
   * @param paymentType. It could be either of giftCode, creditCard or
   *         promoCode.
  */
  public void setPaymentType( String paymentType )
  {
      this.paymentType = paymentType;
  }

  /**
   * Method to get payment reference id.
   *
   * @return paymemtRefId. It could be either giftcode_id or promo_id or
   *          payment_id.
   */
  public String getPaymentRefId()
  {
      return this.paymentRefId;
  }

  /**
   * Method to set payment reference id.
   *
   * @param paymentRefId. It could be either either giftcode_id or promo_id or
   *          payment_id.
  */
  public void setPaymentRefId( String paymentRefId )
  {
      this.paymentRefId = paymentRefId;
  }

  /**
   * Method to get the date when personal cloud was created.
   *
   * @return timeCreated. Date when personal cloud was created.
   */
  public Date getTimeCreated()
  {
      return this.timeCreated;
  }

  /**
   * Method to set the date when personal cloud was created.
   *
   * @param timeCreated Date created.
   */
  public void setTimeCreated( Date timeCreated )
  {
      this.timeCreated = timeCreated;
  }
}
