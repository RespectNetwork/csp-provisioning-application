package net.respectnetwork.csp.application.form;

import javax.validation.constraints.*;
import org.hibernate.validator.constraints.*;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

public class InviteForm
{
	@NotNull @Size(min=6, max=64) @Email
	private String        emailAddress;

	@NotNull @Size(min=1, max=1024)
	private String        emailMessage;

	@NotNull
	private Boolean       giftCardFlag;

	@NotNull @Min(0) @Max(10)
	private Integer       giftCardQuantity;

	public InviteForm()
	{
		this.emailAddress = "";
		this.emailMessage = "";
		this.giftCardFlag = Boolean.FALSE;
		this.giftCardQuantity = Integer.valueOf(1);
	}

	public String getEmailAddress()
	{
		return this.emailAddress;
	}

	public void setEmailAddress( String emailAddress )
	{
		this.emailAddress = emailAddress;
	}

	public String getEmailMessage()
	{
		return this.emailMessage;
	}

	public void setEmailMessage( String emailMessage )
	{
		this.emailMessage = emailMessage;
	}

	public Boolean getGiftCardFlag()
	{
		return this.giftCardFlag;
	}

	public void setGiftCardFlag( Boolean giftCardFlag )
	{
		this.giftCardFlag = giftCardFlag;
	}

	public Integer getGiftCardQuantity()
	{
		return this.giftCardQuantity;
	}

	public void setGiftCardQuantity( Integer giftCardQuantity )
	{
		this.giftCardQuantity = giftCardQuantity;
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
		InviteForm other = (InviteForm) object;
		return new EqualsBuilder()
				.appendSuper(super.equals(other))
				.append(this.emailAddress, other.emailAddress)
				.append(this.emailMessage, other.emailMessage)
				.append(this.giftCardFlag, other.giftCardFlag)
				.append(this.giftCardQuantity, other.giftCardQuantity)
				.isEquals();
	}

	public int hashCode()
	{
		return new HashCodeBuilder(17, 37)
				.appendSuper(super.hashCode())
				.append(this.emailAddress)
				.append(this.emailMessage)
				.append(this.giftCardFlag)
				.append(this.giftCardQuantity)
				.toHashCode();
	}

	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		builder.append("InviteForm ");
		builder.append("[emailAddress=")
		    .append(this.emailAddress).append(']');
		builder.append("[emailMessage=")
		    .append(this.emailMessage).append(']');
		builder.append("[giftCardFlag=")
		    .append(this.giftCardFlag).append(']');
		builder.append("[giftCardQuantity=")
		    .append(this.giftCardQuantity).append(']');
		builder.append(' ');
		builder.append(super.toString());
		return builder.toString();
	}
}
