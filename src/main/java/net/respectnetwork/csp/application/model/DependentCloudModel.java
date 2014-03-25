package net.respectnetwork.csp.application.model;

import java.util.Date;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

public class DependentCloudModel
{
	private String		guardianCloudName;
	private String		dependentCloudName;
	private String		paymentId;
	private Date		timeCreated;

	public DependentCloudModel()
	{
		this.guardianCloudName = null;
		this.dependentCloudName = null;
		this.paymentId = null;
		this.timeCreated = null;
	}

	public String getGuardianCloudName()
	{
		return this.guardianCloudName;
	}

	public void setGuardianCloudName( String guardianCloudName )
	{
		this.guardianCloudName = guardianCloudName;
	}

	public String getDependentCloudName()
	{
		return this.dependentCloudName;
	}

	public void setDependentCloudName( String dependentCloudName )
	{
		this.dependentCloudName = dependentCloudName;
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
		DependentCloudModel other = (DependentCloudModel) object;
		return new EqualsBuilder()
				.appendSuper(super.equals(other))
				.append(this.guardianCloudName, other.guardianCloudName)
				.append(this.dependentCloudName, other.dependentCloudName)
				.append(this.paymentId, other.paymentId)
				.append(this.timeCreated, other.timeCreated)
				.isEquals();
	}

	public int hashCode()
	{
		return new HashCodeBuilder(17, 37)
				.appendSuper(super.hashCode())
				.append(this.guardianCloudName)
				.append(this.dependentCloudName)
				.append(this.paymentId)
				.append(this.timeCreated)
				.toHashCode();
	}

	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		builder.append("DependentCloudModel ");
		builder.append("[guardianCloudName=")
		    .append(this.guardianCloudName).append(']');
		builder.append("[dependentCloudName=")
		    .append(this.dependentCloudName).append(']');
		builder.append("[paymentId=")
		    .append(this.paymentId).append(']');
		builder.append("[timeCreated=")
		    .append(this.timeCreated).append(']');
		builder.append(' ');
		builder.append(super.toString());
		return builder.toString();
	}
}
