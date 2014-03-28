package net.respectnetwork.csp.application.form;

import java.util.Vector;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

public class DependentForm
{
	private String dependentCloudName;
	private String dependentCloudPassword;

	private boolean paymentByCC ;
	
	private String giftCode;
	
	private boolean consentFlag;
	private String consentText;
	
	
	public DependentForm()
	{
		this.dependentCloudName = null;
		this.dependentCloudName = null;
		this.giftCode = null;
		this.paymentByCC = true;
		this.consentFlag = false;
		this.consentText = "I agree to the T&C for Dependent Personal Clouds.";
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
		DependentForm other = (DependentForm) object;
		return new EqualsBuilder()
				.appendSuper(super.equals(other))
				.append(this.dependentCloudName, other.dependentCloudName)
				.append(this.dependentCloudPassword, other.dependentCloudPassword)
				.append(this.paymentByCC, other.paymentByCC)
				.append(this.giftCode, other.giftCode)			
				.isEquals();
	}

	public int hashCode()
	{
		return new HashCodeBuilder(17, 37)
				.appendSuper(super.hashCode())
				.append(this.dependentCloudName)
				.append(this.dependentCloudPassword)
				.append(this.paymentByCC)
				.append(this.giftCode)			
				.toHashCode();
	}

	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		builder.append("DependentForm ");
		builder.append("[dependentCloudName=")
		    .append(this.dependentCloudName).append(']');
		builder.append("[paymentByCCFlag=")
		    .append(this.paymentByCC).append(']');
		builder.append("[giftCode=")
		    .append(this.giftCode).append(']');
		;
		builder.append(' ');
		builder.append(super.toString());
		return builder.toString();
	}


	public String getDependentCloudName() {
		return dependentCloudName;
	}


	public void setDependentCloudName(String dependentCloudName) {
		this.dependentCloudName = dependentCloudName;
	}


	public String getDependentCloudPassword() {
		return dependentCloudPassword;
	}


	public void setDependentCloudPassword(String dependentCloudPassword) {
		this.dependentCloudPassword = dependentCloudPassword;
	}


	public boolean isPaymentByCC() {
		return paymentByCC;
	}


	public void setPaymentByCC(boolean paymentByCC) {
		this.paymentByCC = paymentByCC;
	}


	public String getGiftCode() {
		return giftCode;
	}


	public void setGiftCode(String giftCode) {
		this.giftCode = giftCode;
	}


	public boolean isConsentFlag() {
		return consentFlag;
	}


	public void setConsentFlag(boolean consentFlag) {
		this.consentFlag = consentFlag;
	}


	public String getConsentText() {
		return consentText;
	}


	public void setConsentText(String consentText) {
		this.consentText = consentText;
	}
}
