package net.respectnetwork.csp.application.form;

public class ForgotPasswordForm {
    /**
     * cloud name of the user.
     */
    private String cloudName;

    /**
     * Registered email address of the user.
     */
    private String emailAddress;

    /**
     * Registered phone number of user.
     */
    private String phoneNumber;
    /**
     * Country code
     */
    private String countryCode;

    public String getCloudName() {
        return cloudName;
    }

    public void setCloudName(String cloudName) {
        this.cloudName = cloudName;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public String getPhoneNumber() {
        return countryCode + "." + phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("ForgotePasswordForm [cloudName=" + cloudName)
                .append(", emailAddress=" + emailAddress)
                .append(", phoneNumber=" + phoneNumber).append("]");
        return builder.toString();
    }
}
