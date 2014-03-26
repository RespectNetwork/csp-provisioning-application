package net.respectnetwork.csp.application.model;

import java.util.Date;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

public class GiftCodeRedemptionModel
{
	private String		redemptionId;
	private String		giftCodeId;
	private String		cloudNameCreated;
	private Date		timeCreated;

	public GiftCodeRedemptionModel()
	{
		this.redemptionId = null;
		this.giftCodeId = null;
		this.cloudNameCreated = null;
		this.timeCreated = null;
	}

	public String getRedemptionId()
	{
		return this.redemptionId;
	}

	public void setRedemptionId( String redemptionId )
	{
		this.redemptionId = redemptionId;
	}

	public String getGiftCodeId()
	{
		return this.giftCodeId;
	}

	public void setGiftCodeId( String giftCodeId )
	{
		this.giftCodeId = giftCodeId;
	}

	public String getCloudNameCreated()
	{
		return this.cloudNameCreated;
	}

	public void setCloudNameCreated( String cloudNameCreated )
	{
		this.cloudNameCreated = cloudNameCreated;
	}

	public Date getTimeCreated()
	{
		return this.timeCreated;
	}

	public void setTimeCreated( Date timeCreated )
	{
		this.timeCreated = timeCreated;
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
		GiftCodeRedemptionModel other = (GiftCodeRedemptionModel) object;
		return new EqualsBuilder()
				.appendSuper(super.equals(other))
				.append(this.redemptionId, other.redemptionId)
				.append(this.giftCodeId, other.giftCodeId)
				.append(this.cloudNameCreated, other.cloudNameCreated)
				.append(this.timeCreated, other.timeCreated)
				.isEquals();
	}

	public int hashCode()
	{
		return new HashCodeBuilder(17, 37)
				.appendSuper(super.hashCode())
				.append(this.redemptionId)
				.append(this.giftCodeId)
				.append(this.cloudNameCreated)
				.append(this.timeCreated)
				.toHashCode();
	}

	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		builder.append("GiftCodeRedemptionModel ");
		builder.append("[redemptionId=")
		    .append(this.redemptionId).append(']');
		builder.append("[giftCodeId=")
		    .append(this.giftCodeId).append(']');
		builder.append("[cloudNameCreated=")
		    .append(this.cloudNameCreated).append(']');
		builder.append("[timeCreated=")
		    .append(this.timeCreated).append(']');
		builder.append(' ');
		builder.append(super.toString());
		return builder.toString();
	}
}
