package net.respectnetwork.csp.application.form;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
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
    // List of DependentFormIndividual, it contains the cloudname, password,
    // birthdate of all the dependents.
    private List<DependentFormIndividual> dependentFormIndividual = new ArrayList<DependentFormIndividual>();

	private boolean consentFlag;
	private String consentText;
	
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
				.append(this.dependentFormIndividual, other.dependentFormIndividual)
				.append(this.consentFlag, other.consentFlag)
				.append(this.consentText, other.consentText)
				.isEquals();
	}

	public int hashCode()
	{
		return new HashCodeBuilder(17, 37)
				.appendSuper(super.hashCode())
				.append(this.dependentFormIndividual)
				.append(this.consentFlag)
				.append(this.consentText)
				
				.toHashCode();
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

    /**
     * This method returns the list of all the dependents cloud names.
     * @return All dependents cloud names.
     */
    public ArrayList<String> getDependentCloudName() {
        ArrayList<String> allDependentCloudName = new ArrayList<String>();
        Iterator<DependentFormIndividual> iterator = dependentFormIndividual.iterator();
        while (iterator.hasNext()) {
            String dependentCloudName = iterator.next().getDependentCloudName();
            if(dependentCloudName != null && !dependentCloudName.isEmpty()) {
                allDependentCloudName.add(dependentCloudName);
            }
        }
        return allDependentCloudName;
    }

    /**
     * This method returns the list of all the dependents passwords.
     * @return All dependents passwords.
     */
    public ArrayList<String> getDependentCloudPassword() {
        ArrayList<String> allDependentPassword = new ArrayList<String>();
        Iterator<DependentFormIndividual> iterator = dependentFormIndividual.iterator();
        while (iterator.hasNext()) {
            String dependentCloudPassword = iterator.next().getDependentPassword();
            if(dependentCloudPassword != null && !dependentCloudPassword.isEmpty()) {
                allDependentPassword.add(dependentCloudPassword);
            }
        }
        return allDependentPassword;
    }

    /**
     * This method returns the list of all the dependents birth dates.
     * @return All dependents birth date.
     */
    public ArrayList<String> getDependentBirthDate() {
        ArrayList<String> allDependentBirthDate = new ArrayList<String>();
        Iterator<DependentFormIndividual> iterator = dependentFormIndividual.iterator();
        while (iterator.hasNext()) {
            String dependentCloudDOB = iterator.next().getDependentBirthDate();
            if(dependentCloudDOB != null && !dependentCloudDOB.isEmpty()) {
                allDependentBirthDate.add(dependentCloudDOB);
            }
        }
        return allDependentBirthDate;
    }

   public void setNameAvailabilityCheckURL(String url)
   {
      this.nameAvailabilityCheckURL = url;
   }

   public String getNameAvailabilityCheckURL()
   {
      return this.nameAvailabilityCheckURL;
   }

   public void setDependentFormIndividual(List<DependentFormIndividual> dependentFormIndividual)
   {
      this.dependentFormIndividual = dependentFormIndividual;
   }
   
   public List<DependentFormIndividual> getDependentFormIndividual()
   {
      return this.dependentFormIndividual;
   }
}
