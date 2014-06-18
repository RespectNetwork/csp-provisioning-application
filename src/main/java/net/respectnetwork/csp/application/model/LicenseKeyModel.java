package net.respectnetwork.csp.application.model;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonTypeInfo;
import org.codehaus.jackson.annotate.JsonTypeName;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.WRAPPER_OBJECT)
@JsonTypeName("keyRequest")
@JsonIgnoreProperties(ignoreUnknown = true)
public class LicenseKeyModel {

    @JsonProperty("csp")
    private String cspCloudNumber;
    @JsonProperty("user")
    private String userCloudNumber;
    @JsonIgnore
    private String keyName;
    @JsonIgnore
    private String keyValue;
    private String token;

    public LicenseKeyModel() {
        this.cspCloudNumber = null;
        this.userCloudNumber = null;
        this.token = null;
    }

    public LicenseKeyModel(String cspCloudNumber, String userCloudNumber,
            String token) {
        this.cspCloudNumber = cspCloudNumber;
        this.userCloudNumber = userCloudNumber;
        this.token = token;
    }

    public String getCspCloudNumber() {
        return cspCloudNumber;
    }

    public void setCspCloudNumber(String cspCloudNumber) {
        this.cspCloudNumber = cspCloudNumber;
    }

    public String getUserCloudNumber() {
        return userCloudNumber;
    }

    public void setUserCloudNumber(String userCloudNumber) {
        this.userCloudNumber = userCloudNumber;
    }

    public String getKeyName() {
        return keyName;
    }

    public void setKeyName(String keyName) {
        this.keyName = keyName;
    }

    public String getKeyValue() {
        return keyValue;
    }

    public void setKeyValue(String keyValue) {
        this.keyValue = keyValue;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

}
