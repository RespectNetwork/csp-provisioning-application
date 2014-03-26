package net.respectnetwork.csp.application.model;

import java.util.Date;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

public class InviteResponseModel
{
	private String		responseId;
	private String		inviteId;
	private String		paymentId;
	private String		cloudNameCreated;
	private Date		timeCreated;

	public InviteResponseModel()
	{
		this.responseId = null;
		this.inviteId = null;
		this.paymentId = null;
		this.cloudNameCreated = null;
		this.timeCreated = null;
	}

	public String getResponseId()
	{
		return this.responseId;
	}

	public void setResponseId( String responseId )
	{
		this.responseId = responseId;
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
		InviteResponseModel other = (InviteResponseModel) object;
		return new EqualsBuilder()
				.appendSuper(super.equals(other))
				.append(this.responseId, other.responseId)
				.append(this.inviteId, other.inviteId)
				.append(this.paymentId, other.paymentId)
				.append(this.cloudNameCreated, other.cloudNameCreated)
				.append(this.timeCreated, other.timeCreated)
				.isEquals();
	}

	public int hashCode()
	{
		return new HashCodeBuilder(17, 37)
				.appendSuper(super.hashCode())
				.append(this.responseId)
				.append(this.inviteId)
				.append(this.paymentId)
				.append(this.cloudNameCreated)
				.append(this.timeCreated)
				.toHashCode();
	}

	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		builder.append("InviteResponseModel ");
		builder.append("[responseId=")
		    .append(this.responseId).append(']');
		builder.append("[inviteId=")
		    .append(this.inviteId).append(']');
		builder.append("[paymentId=")
		    .append(this.paymentId).append(']');
		builder.append("[cloudNameCreated=")
		    .append(this.cloudNameCreated).append(']');
		builder.append("[timeCreated=")
		    .append(this.timeCreated).append(']');
		builder.append(' ');
		builder.append(super.toString());
		return builder.toString();
	}
}
