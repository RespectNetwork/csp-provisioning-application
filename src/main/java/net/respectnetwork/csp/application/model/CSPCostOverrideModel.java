package net.respectnetwork.csp.application.model;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import java.math.BigDecimal;

public class CSPCostOverrideModel
{
   private String		cspCloudName;
   private String		phonePrefix;
   private BigDecimal costPerCloudName;
   private String		currency;
   private String		merchantAccountId;

   public CSPCostOverrideModel() {
      this.cspCloudName = null;
      this.phonePrefix = null;
      this.costPerCloudName = null;
      this.currency = null;
      this.merchantAccountId = null;
   }

   public String getCspCloudName()
   {
      return cspCloudName;
   }

   public void setCspCloudName(String cspCloudName)
   {
      this.cspCloudName = cspCloudName;
   }

   public String getPhonePrefix()
   {
      return phonePrefix;
   }

   public void setPhonePrefix(String phonePrefix)
   {
      this.phonePrefix = phonePrefix;
   }

   public BigDecimal getCostPerCloudName()
   {
      return costPerCloudName;
   }

   public void setCostPerCloudName(BigDecimal costPerCloudName)
   {
      this.costPerCloudName = costPerCloudName;
   }

   public String getCurrency()
   {
      return currency;
   }

   public void setCurrency(String currency)
   {
      this.currency = currency;
   }

   public String getMerchantAccountId()
   {
      return merchantAccountId;
   }

   public void setMerchantAccountId(String merchantAccountId)
   {
      this.merchantAccountId = merchantAccountId;
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
		CSPCostOverrideModel other = (CSPCostOverrideModel) object;
		return new EqualsBuilder()
				.appendSuper(super.equals(other))
				.append(this.cspCloudName, other.cspCloudName)
				.append(this.phonePrefix, other.phonePrefix)
				.append(this.costPerCloudName, other.costPerCloudName)
				.append(this.currency, other.currency)
				.append(this.merchantAccountId, other.merchantAccountId)
				.isEquals();
	}

	public int hashCode()
	{
		return new HashCodeBuilder(17, 37)
				.appendSuper(super.hashCode())
				.append(this.cspCloudName)
				.append(this.phonePrefix)
				.append(this.costPerCloudName)
				.append(this.currency)
				.append(this.merchantAccountId)
				.toHashCode();
	}

   public String toString()
	{
		StringBuilder builder = new StringBuilder();
		builder.append("CSPCostOverrideModel ");
		builder.append("[cspCloudName=")
		    .append(this.cspCloudName).append(']');
		builder.append("[phonePrefix=")
		    .append(this.phonePrefix).append(']');
		builder.append("[costPerCloudName=")
		    .append(this.costPerCloudName).append(']');
		builder.append("[currency=")
		    .append(this.currency).append(']');
      builder.append("[merchantAccountId=")
		    .append(this.merchantAccountId).append(']');
		builder.append(' ');
		builder.append(super.toString());
		return builder.toString();
	}
}
