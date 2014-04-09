package net.respectnetwork.csp.application.model;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

public class SignupInfoModel
{
   private String cloudName;
   private String email;
   private String phone;
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
            
            .isEquals();
   }

   public int hashCode()
   {
      return new HashCodeBuilder(17, 37)
            .appendSuper(super.hashCode())
            .append(this.cloudName)
            .append(this.email)
            .append(this.phone)
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
      
      
      builder.append(' ');
      builder.append(super.toString());
      return builder.toString();
   }


}
