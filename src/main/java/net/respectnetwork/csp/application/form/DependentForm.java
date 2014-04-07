package net.respectnetwork.csp.application.form;

import java.util.Vector;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

public class DependentForm
{
	
	private boolean consentFlag;
	private String consentText;
	
	private String dependentCloudName , dependentCloudPassword , dependentBirthDate ;
	
	
	public DependentForm()
	{
		this.consentFlag = false;
		this.consentText = "I agree to the Terms and Conditions for Dependent Personal Clouds.";

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
				.append(this.dependentBirthDate, other.dependentBirthDate)
		
				.isEquals();
	}

	public int hashCode()
	{
		return new HashCodeBuilder(17, 37)
				.appendSuper(super.hashCode())
				.append(this.dependentCloudName)
				.append(this.dependentCloudPassword)
				.toHashCode();
	}

	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		builder.append("DependentForm ");
		builder.append("[dependentCloudName=")
		    .append(this.dependentCloudName).append(']');

		;
		builder.append(' ');
		builder.append(super.toString());
		return builder.toString();
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




   public String getDependentCloudName()
   {
      return dependentCloudName;
   }


   public void setDependentCloudName(String dependentCloudName)
   {
      this.dependentCloudName = dependentCloudName;
   }


   public String getDependentCloudPassword()
   {
      return dependentCloudPassword;
   }


   public void setDependentCloudPassword(String dependentCloudPassword)
   {
      this.dependentCloudPassword = dependentCloudPassword;
   }


   public String getDependentBirthDate()
   {
      return dependentBirthDate;
   }


   public void setDependentBirthDate(String dependentBirthDate)
   {
      this.dependentBirthDate = dependentBirthDate;
   }


	
}
