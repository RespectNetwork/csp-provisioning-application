package net.respectnetwork.csp.application.form;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;


public class AccountDetailsForm {
	
	private String cloudName;
  
    /**
     * @return the cloudName
     */
    public String getCloudName() {
        return cloudName;
    }
    /**
     * @param cloudName the cloudName to set
     */
    public void setCloudName(String cloudName) {
        this.cloudName = cloudName;
    }
  
	
	
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("MessageForm [cloudName=").append(cloudName)
				.append("]");
		return builder.toString();
	}

    /**
     * Hash Implementation using apache-lang
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder()
        .append(cloudName)
        .toHashCode();
    }


    /**
     * Equals Implementation using apache-lang
     */
    @Override
    public boolean equals(Object obj) {
        if(obj instanceof AccountDetailsForm){
            final AccountDetailsForm other = (AccountDetailsForm) obj;
            return new EqualsBuilder()
                .append(cloudName, other.cloudName)
                .isEquals();
        } else{
            return false;
        }
    }
	
	
	
}
