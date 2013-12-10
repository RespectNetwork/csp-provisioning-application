package net.respectnetwork.csp.application.form;


public class CodesForm {
	
	private String emailCode;
	private String SMSCode;
	
	   
    /**
     * @return the emailCode
     */
    public String getEmailCode() {
        return emailCode;
    }
    /**
     * @param emailCode the emailCode to set
     */
    public void setEmailCode(String emailCode) {
        this.emailCode = emailCode;
    }
    /**
     * @return the smsCode
     */
    public String getSMSCode() {
        return SMSCode;
    }
    /**
     * @param smsCode the smsCode to set
     */
    public void setSMSCode(String smsCode) {
        this.SMSCode = smsCode;
    }

	
	
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("MessageForm [emailCode=").append(emailCode)
		        .append(", smsCode=").append(SMSCode)
				.append("]");
		return builder.toString();
	}

    @Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((emailCode == null) ? 0 : emailCode.hashCode());
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
		CodesForm other = (CodesForm) obj;
		if (emailCode == null) {
			if (other.emailCode != null)
				return false;
		} else if (!emailCode.equals(other.emailCode))
			return false;
		return true;
	}
	
	
	
}
