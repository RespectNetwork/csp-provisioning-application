package net.respectnetwork.csp.application.model;

import java.math.BigDecimal;
import java.util.Date;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

public class CSPModel
{
	private String		cspCloudName;
	private String		paymentGatewayName;
	private String		paymentUrlTemplate;
	private String		username;
	private String		password;
	private BigDecimal	costPerCloudName;
	private String		currency;
	private Date		timeCreated;

	public CSPModel()
	{
		this.cspCloudName = null;
		this.paymentGatewayName = null;
		this.paymentUrlTemplate = null;
		this.username = null;
		this.password = null;
		this.costPerCloudName = null;
		this.currency = null;
		this.timeCreated = null;
	}

	public String getCspCloudName()
	{
		return this.cspCloudName;
	}

	public void setCspCloudName( String cspCloudName )
	{
		this.cspCloudName = cspCloudName;
	}

	public String getPaymentGatewayName()
	{
		return this.paymentGatewayName;
	}

	public void setPaymentGatewayName( String paymentGatewayName )
	{
		this.paymentGatewayName = paymentGatewayName;
	}

	public String getPaymentUrlTemplate()
	{
		return this.paymentUrlTemplate;
	}

	public void setPaymentUrlTemplate( String paymentUrlTemplate )
	{
		this.paymentUrlTemplate = paymentUrlTemplate;
	}

	public String getUsername()
	{
		return this.username;
	}

	public void setUsername( String username )
	{
		this.username = username;
	}

	public String getPassword()
	{
		return this.password;
	}

	public void setPassword( String password )
	{
		this.password = password;
	}

	public BigDecimal getCostPerCloudName()
	{
		return this.costPerCloudName;
	}

	public void setCostPerCloudName( BigDecimal costPerCloudName )
	{
		this.costPerCloudName = costPerCloudName;
	}

	public String getCurrency()
	{
		return this.currency;
	}

	public void setCurrency( String currency )
	{
		this.currency = currency;
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
		CSPModel other = (CSPModel) object;
		return new EqualsBuilder()
				.appendSuper(super.equals(other))
				.append(this.cspCloudName, other.cspCloudName)
				.append(this.paymentGatewayName, other.paymentGatewayName)
				.append(this.paymentUrlTemplate, other.paymentUrlTemplate)
				.append(this.username, other.username)
				.append(this.password, other.password)
				.append(this.costPerCloudName, other.costPerCloudName)
				.append(this.currency, other.currency)
				.append(this.timeCreated, other.timeCreated)
				.isEquals();
	}

	public int hashCode()
	{
		return new HashCodeBuilder(17, 37)
				.appendSuper(super.hashCode())
				.append(this.cspCloudName)
				.append(this.paymentGatewayName)
				.append(this.paymentUrlTemplate)
				.append(this.username)
				.append(this.password)
				.append(this.costPerCloudName)
				.append(this.currency)
				.append(this.timeCreated)
				.toHashCode();
	}

	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		builder.append("CSPModel ");
		builder.append("[cspCloudName=")
		    .append(this.cspCloudName).append(']');
		builder.append("[paymentGatewayName=")
		    .append(this.paymentGatewayName).append(']');
		builder.append("[paymentUrlTemplate=")
		    .append(this.paymentUrlTemplate).append(']');
		builder.append("[username=")
		    .append(this.username).append(']');
		builder.append("[password=")
		    .append(this.password).append(']');
		builder.append("[costPerCloudName=")
		    .append(this.costPerCloudName).append(']');
		builder.append("[currency=")
		    .append(this.currency).append(']');
		builder.append("[timeCreated=")
		    .append(this.timeCreated).append(']');
		builder.append(' ');
		builder.append(super.toString());
		return builder.toString();
	}
}
