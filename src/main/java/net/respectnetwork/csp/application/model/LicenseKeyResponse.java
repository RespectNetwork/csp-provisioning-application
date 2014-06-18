package net.respectnetwork.csp.application.model;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class LicenseKeyResponse {

    private LicenseKeyData keyResponse;
    private String errorCode;
    private String errorMessage;

    public LicenseKeyData getKeyResponse() {
        return keyResponse;
    }

    public void setKeyResponse(LicenseKeyData keyResponse) {
        this.keyResponse = keyResponse;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

}
