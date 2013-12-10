package net.respectnetwork.csp.application.form;


public class CloudForm {
	
	private String cloudName;
	private String secretToken;
	
	   
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
    /**
     * @return the secretToken
     */
    public String getSecretToken() {
        return secretToken;
    }
    /**
     * @param secretToken the secretToken to set
     */
    public void setSecretToken(String secretToken) {
        this.secretToken = secretToken;
    }

	
	
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("MessageForm [cloudName=").append(cloudName)
		        .append(", secretToken=").append(secretToken)
				.append("]");
		return builder.toString();
	}

    @Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((cloudName == null) ? 0 : cloudName.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CloudForm other = (CloudForm) obj;
		if (cloudName == null) {
			if (other.cloudName != null)
				return false;
		} else if (!cloudName.equals(other.cloudName))
			return false;
		return true;
	}
	
	
	
}
