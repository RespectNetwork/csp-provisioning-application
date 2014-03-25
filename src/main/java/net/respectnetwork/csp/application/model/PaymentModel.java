package net.respectnetwork.csp.application.model;

import java.math.BigDecimal;
import java.util.Date;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

public class PaymentModel
{
	private String		paymentId;
	private String		cspCloudName;
	private String		paymentReferenceId;
	private String		paymentResponseCode;
	private BigDecimal	amount;
	private String		currency;
	private Date		timeCreated;

	public PaymentModel()
	{
		this.paymentId = null;
		this.cspCloudName = null;
		this.paymentReferenceId = null;
		this.paymentResponseCode = null;
		this.amount = null;
		this.currency = null;
		this.timeCreated = null;
	}

	public String getPaymentId()
	{
		return this.paymentId;
	}

	public void setPaymentId( String paymentId )
	{
		this.paymentId = paymentId;
	}

	public String getCspCloudName()
	{
		return this.cspCloudName;
	}

	public void setCspCloudName( String cspCloudName )
	{
		this.cspCloudName = cspCloudName;
	}

	public String getPaymentReferenceId()
	{
		return this.paymentReferenceId;
	}

	public void setPaymentReferenceId( String paymentReferenceId )
	{
		this.paymentReferenceId = paymentReferenceId;
	}

	public String getPaymentResponseCode()
	{
		return this.paymentResponseCode;
	}

	public void setPaymentResponseCode( String paymentResponseCode )
	{
		this.paymentResponseCode = paymentResponseCode;
	}

	public BigDecimal getAmount()
	{
		return this.amount;
	}

	public void setAmount( BigDecimal amount )
	{
		this.amount = amount;
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
		PaymentModel other = (PaymentModel) object;
		return new EqualsBuilder()
				.appendSuper(super.equals(other))
				.append(this.paymentId, other.paymentId)
				.append(this.cspCloudName, other.cspCloudName)
				.append(this.paymentReferenceId, other.paymentReferenceId)
				.append(this.paymentResponseCode, other.paymentResponseCode)
				.append(this.amount, other.amount)
				.append(this.currency, other.currency)
				.append(this.timeCreated, other.timeCreated)
				.isEquals();
	}

	public int hashCode()
	{
		return new HashCodeBuilder(17, 37)
				.appendSuper(super.hashCode())
				.append(this.paymentId)
				.append(this.cspCloudName)
				.append(this.paymentReferenceId)
				.append(this.paymentResponseCode)
				.append(this.amount)
				.append(this.currency)
				.append(this.timeCreated)
				.toHashCode();
	}

	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		builder.append("PaymentModel ");
		builder.append("[paymentId=")
		    .append(this.paymentId).append(']');
		builder.append("[cspCloudName=")
		    .append(this.cspCloudName).append(']');
		builder.append("[paymentReferenceId=")
		    .append(this.paymentReferenceId).append(']');
		builder.append("[paymentResponseCode=")
		    .append(this.paymentResponseCode).append(']');
		builder.append("[amount=")
		    .append(this.amount).append(']');
		builder.append("[currency=")
		    .append(this.currency).append(']');
		builder.append("[timeCreated=")
		    .append(this.timeCreated).append(']');
		builder.append(' ');
		builder.append(super.toString());
		return builder.toString();
	}
}
