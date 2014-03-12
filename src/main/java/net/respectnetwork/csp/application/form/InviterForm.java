package net.respectnetwork.csp.application.form;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;


public class InviterForm {
    
    /** Inviter */
    private String inviterCloudName;

     
    /**
     * @return the cloudName
     */
    public String getInviterCloudName() {
        return inviterCloudName;
    }
    /**
     * @param inviter the inviter to set
     */
    public void setInviterCloudName(String inviterCloudName) {
        this.inviterCloudName = inviterCloudName;
    }

    
    
    
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("MessageForm [inviterCloudName=").append(inviterCloudName)
                .append("]");
        return builder.toString();
    }
    
    /**
     * Hash Implementation using apache-lang
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder()
        .append(inviterCloudName)
        .toHashCode();
    }


    /**
     * Equals Implementation using apache-lang
     */
    @Override
    public boolean equals(Object obj) {
        if(obj instanceof InviterForm){
            final InviterForm other = (InviterForm) obj;
            return new EqualsBuilder()
                .append(inviterCloudName, other.inviterCloudName)
                .isEquals();
        } else{
            return false;
        }
    }
    
    
    
}
