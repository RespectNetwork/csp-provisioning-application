package net.respectnetwork.csp.application.model;

import java.util.Date;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

public class GiftCodeModel
{
	private String		giftCodeId;
	private String		inviteId;
	private String		paymentId;
	private Date		timeCreated;

	public GiftCodeModel()
	{
		this.giftCodeId = null;
		this.inviteId = null;
		this.paymentId = null;
		this.timeCreated = null;
	}

	public String getGiftCodeId()
	{
		return this.giftCodeId;
	}

	public void setGiftCodeId( String giftCodeId )
	{
		this.giftCodeId = giftCodeId;
	}

	public String getInviteId()
	{
		return this.inviteId;
	}

	public void setInviteId( String inviteId )
	{
		this.inviteId = inviteId;
	}

	public String getPaymentId()
	{
		return this.paymentId;
	}

	public void setPaymentId( String paymentId )
	{
		this.paymentId = paymentId;
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
		GiftCodeModel other = (GiftCodeModel) object;
		return new EqualsBuilder()
				.appendSuper(super.equals(other))
				.append(this.giftCodeId, other.giftCodeId)
				.append(this.inviteId, other.inviteId)
				.append(this.paymentId, other.paymentId)
				.append(this.timeCreated, other.timeCreated)
				.isEquals();
	}

	public int hashCode()
	{
		return new HashCodeBuilder(17, 37)
				.appendSuper(super.hashCode())
				.append(this.giftCodeId)
				.append(this.inviteId)
				.append(this.paymentId)
				.append(this.timeCreated)
				.toHashCode();
	}

	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		builder.append("GiftCodeModel ");
		builder.append("[giftCodeId=")
		    .append(this.giftCodeId).append(']');
		builder.append("[inviteId=")
		    .append(this.inviteId).append(']');
		builder.append("[paymentId=")
		    .append(this.paymentId).append(']');
		builder.append("[timeCreated=")
		    .append(this.timeCreated).append(']');
		builder.append(' ');
		builder.append(super.toString());
		return builder.toString();
	}
}
