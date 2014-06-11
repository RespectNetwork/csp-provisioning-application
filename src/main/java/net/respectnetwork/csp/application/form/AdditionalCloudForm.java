package net.respectnetwork.csp.application.form;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.respectnetwork.csp.application.manager.RegistrationManager;
import net.respectnetwork.csp.application.session.RegistrationSession;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

/**
 * @author psharma2
 *
 */
public class AdditionalCloudForm {
    // List of additional cloud names.
    private List<String> additionalCloudNames = new ArrayList<String>();

    private String nameAvailabilityCheckURL ;

    public void setAdditionalCloudNames(List<String> additionalCloudNames)
    {
       this.additionalCloudNames = additionalCloudNames;
    }

    public List<String> getAdditionalCloudNames()
    {
       return this.additionalCloudNames;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("MessageForm [additionalCloudNames=").append(additionalCloudNames)
        .append("]");
        return builder.toString();
    }

    /**
     * Hash Implementation using apache-lang
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder()
        .append(additionalCloudNames)
        .toHashCode();
    }

    /**
     * Equals Implementation using apache-lang
     */
    @Override
    public boolean equals(Object obj) {
        if(obj instanceof AdditionalCloudForm){
            final AdditionalCloudForm other = (AdditionalCloudForm) obj;
            return new EqualsBuilder()
                .append(additionalCloudNames, other.additionalCloudNames)
                .isEquals();
        } else{
            return false;
        }
    }

    /**
     * Method to set name availability check URL.
     * @param url
     */
    public void setNameAvailabilityCheckURL(String url) {
        this.nameAvailabilityCheckURL = url;
    }

    /**
     * Method to get name availability check URL.
     * @return
     */
    public String getNameAvailabilityCheckURL() {
        return this.nameAvailabilityCheckURL;
    }
}