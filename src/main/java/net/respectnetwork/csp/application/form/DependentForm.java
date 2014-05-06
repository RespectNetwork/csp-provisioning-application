package net.respectnetwork.csp.application.form;

import java.util.Vector;

import net.respectnetwork.csp.application.controller.PersonalCloudDependentController;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

public class DependentForm
{
	
	private boolean consentFlag;
	private String consentText;
	
	private String dependentCloudName , dependentCloudPassword , dependentBirthDate ;
	
	private String dependentCloudName1  ;
	private String dependentCloudName2 ;
	private String dependentCloudName3 ;
	private String dependentCloudName4 ;
	private String dependentCloudName5 ;
	private String dependentCloudName6 ;
	private String dependentCloudName7 ;
	private String dependentCloudName8 ;
	private String dependentCloudName9 ;
	private String dependentCloudName10 ;
	
	private String nameAvailabilityCheckURL ; //= "https://registration-stage.respectnetwork.net/rn-checkavailability-service/";
	
	private static final Logger logger = LoggerFactory
         .getLogger(DependentForm.class);
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
				.append(this.consentFlag, other.consentFlag)
				.append(this.consentText, other.consentText)
				.isEquals();
	}

	public int hashCode()
	{
		return new HashCodeBuilder(17, 37)
				.appendSuper(super.hashCode())
				.append(this.dependentCloudName)
				.append(this.dependentCloudPassword)
				.append(this.dependentBirthDate)
				.append(this.consentFlag)
				.append(this.consentText)
				
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
      String allDependentCloudNames = dependentCloudName;
      logger.debug("allDependentCloudNames..." + allDependentCloudNames);
      if (this.getDependentCloudName1() != null && !this.getDependentCloudName1().isEmpty())
      {
         allDependentCloudNames += "," + this.getDependentCloudName1();
      }
      if (this.getDependentCloudName2() != null && !this.getDependentCloudName2().isEmpty())
      {
         allDependentCloudNames += "," + this.getDependentCloudName2();
      }
      if (this.getDependentCloudName3() != null && !this.getDependentCloudName3().isEmpty())
      {
         allDependentCloudNames += "," + this.getDependentCloudName3();
      }
      if (this.getDependentCloudName4() != null && !this.getDependentCloudName4().isEmpty())
      {
         allDependentCloudNames += "," + this.getDependentCloudName4();
      }
      if (this.getDependentCloudName5() != null && !this.getDependentCloudName5().isEmpty())
      {
         allDependentCloudNames += "," + this.getDependentCloudName5();
      }
      if (this.getDependentCloudName6() != null && !this.getDependentCloudName6().isEmpty())
      {
         allDependentCloudNames += "," + this.getDependentCloudName6();
      }
      if (this.getDependentCloudName7() != null && !this.getDependentCloudName7().isEmpty())
      {
         allDependentCloudNames += "," + this.getDependentCloudName7();
      }
      if (this.getDependentCloudName8() != null && !this.getDependentCloudName8().isEmpty())
      {
         allDependentCloudNames += "," + this.getDependentCloudName8();
      }
      if (this.getDependentCloudName9() != null && !this.getDependentCloudName9().isEmpty())
      {
         allDependentCloudNames += "," + this.getDependentCloudName9();
      }
      if (this.getDependentCloudName10() != null && !this.getDependentCloudName10().isEmpty())
      {
         allDependentCloudNames += "," + this.getDependentCloudName10();
      }
      logger.debug("allDependentCloudNames..." + allDependentCloudNames);
      return allDependentCloudNames;
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


   public String getDependentCloudName1()
   {
      return dependentCloudName1;
   }


   public void setDependentCloudName1(String dependentCloudName1)
   {
      this.dependentCloudName1 = dependentCloudName1;
   }


   public String getDependentCloudName2()
   {
      return dependentCloudName2;
   }


   public void setDependentCloudName2(String dependentCloudName2)
   {
      this.dependentCloudName2 = dependentCloudName2;
   }


   public String getDependentCloudName3()
   {
      return dependentCloudName3;
   }


   public void setDependentCloudName3(String dependentCloudName3)
   {
      this.dependentCloudName3 = dependentCloudName3;
   }


   public String getDependentCloudName4()
   {
      return dependentCloudName4;
   }


   public void setDependentCloudName4(String dependentCloudName4)
   {
      this.dependentCloudName4 = dependentCloudName4;
   }


   public String getDependentCloudName5()
   {
      return dependentCloudName5;
   }


   public void setDependentCloudName5(String dependentCloudName5)
   {
      this.dependentCloudName5 = dependentCloudName5;
   }


   public String getDependentCloudName6()
   {
      return dependentCloudName6;
   }


   public void setDependentCloudName6(String dependentCloudName6)
   {
      this.dependentCloudName6 = dependentCloudName6;
   }


   public String getDependentCloudName7()
   {
      return dependentCloudName7;
   }


   public void setDependentCloudName7(String dependentCloudName7)
   {
      this.dependentCloudName7 = dependentCloudName7;
   }


   public String getDependentCloudName8()
   {
      return dependentCloudName8;
   }


   public void setDependentCloudName8(String dependentCloudName8)
   {
      this.dependentCloudName8 = dependentCloudName8;
   }


   public String getDependentCloudName9()
   {
      return dependentCloudName9;
   }


   public void setDependentCloudName9(String dependentCloudName9)
   {
      this.dependentCloudName9 = dependentCloudName9;
   }


   public String getDependentCloudName10()
   {
      return dependentCloudName10;
   }


   public void setDependentCloudName10(String dependentCloudName10)
   {
      this.dependentCloudName10 = dependentCloudName10;
   }

   
   public void setNameAvailabilityCheckURL(String url)
   {
      this.nameAvailabilityCheckURL = url;
   }
   
   public String getNameAvailabilityCheckURL()
   {
      return this.nameAvailabilityCheckURL;
   }
	
}
