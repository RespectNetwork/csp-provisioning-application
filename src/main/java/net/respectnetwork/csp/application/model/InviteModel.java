package net.respectnetwork.csp.application.model;

import java.util.Date;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

public class InviteModel
{
	private String		inviteId;
	private String		cspCloudName;
	private String		inviterCloudName;
	private String		invitedEmailAddress;
	private String		emailSubject;
	private String		emailMessage;
	private Date		timeCreated;

	public InviteModel()
	{
		this.inviteId = null;
		this.cspCloudName = null;
		this.inviterCloudName = null;
		this.invitedEmailAddress = null;
		this.emailSubject = null;
		this.emailMessage = null;
		this.timeCreated = null;
	}

	public String getInviteId()
	{
		return this.inviteId;
	}

	public void setInviteId( String inviteId )
	{
		this.inviteId = inviteId;
	}

	public String getCspCloudName()
	{
		return this.cspCloudName;
	}

	public void setCspCloudName( String cspCloudName )
	{
		this.cspCloudName = cspCloudName;
	}

	public String getInviterCloudName()
	{
		return this.inviterCloudName;
	}

	public void setInviterCloudName( String inviterCloudName )
	{
		this.inviterCloudName = inviterCloudName;
	}

	public String getInvitedEmailAddress()
	{
		return this.invitedEmailAddress;
	}

	public void setInvitedEmailAddress( String invitedEmailAddress )
	{
		this.invitedEmailAddress = invitedEmailAddress;
	}

	public String getEmailSubject()
	{
		return this.emailSubject;
	}

	public void setEmailSubject( String emailSubject )
	{
		this.emailSubject = emailSubject;
	}

	public String getEmailMessage()
	{
		return this.emailMessage;
	}

	public void setEmailMessage( String emailMessage )
	{
		this.emailMessage = emailMessage;
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
		InviteModel other = (InviteModel) object;
		return new EqualsBuilder()
				.appendSuper(super.equals(other))
				.append(this.inviteId, other.inviteId)
				.append(this.cspCloudName, other.cspCloudName)
				.append(this.inviterCloudName, other.inviterCloudName)
				.append(this.invitedEmailAddress, other.invitedEmailAddress)
				.append(this.emailSubject, other.emailSubject)
				.append(this.emailMessage, other.emailMessage)
				.append(this.timeCreated, other.timeCreated)
				.isEquals();
	}

	public int hashCode()
	{
		return new HashCodeBuilder(17, 37)
				.appendSuper(super.hashCode())
				.append(this.inviteId)
				.append(this.cspCloudName)
				.append(this.inviterCloudName)
				.append(this.invitedEmailAddress)
				.append(this.emailSubject)
				.append(this.emailMessage)
				.append(this.timeCreated)
				.toHashCode();
	}

	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		builder.append("InviteModel ");
		builder.append("[inviteId=")
		    .append(this.inviteId).append(']');
		builder.append("[cspCloudName=")
		    .append(this.cspCloudName).append(']');
		builder.append("[inviterCloudName=")
		    .append(this.inviterCloudName).append(']');
		builder.append("[invitedEmailAddress=")
		    .append(this.invitedEmailAddress).append(']');
		builder.append("[emailSubject=")
		    .append(this.emailSubject).append(']');
		builder.append("[emailMessage=")
		    .append(this.emailMessage).append(']');
		builder.append("[timeCreated=")
		    .append(this.timeCreated).append(']');
		builder.append(' ');
		builder.append(super.toString());
		return builder.toString();
	}
}
