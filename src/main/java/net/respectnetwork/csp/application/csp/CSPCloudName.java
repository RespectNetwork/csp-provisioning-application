package net.respectnetwork.csp.application.csp;

import java.io.Serializable;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

/**
 * Container for CSP and CloudName
 */
public class CSPCloudName implements Serializable{

    private static final long serialVersionUID = -8667770285083067368L;
    private String csp;
    private String cloudname;
    
    public CSPCloudName(String csp, String cloudname) {
        super();
        this.csp = csp;
        this.cloudname = cloudname;
    }

    public String getCsp() {
        return csp;
    }

    public void setCsp(String csp) {
        this.csp = csp;
    }

    public String getCloudName() {
        return cloudname;
    }

    public void setCloudName(String cloudname) {
        this.cloudname = cloudname;
    }
    
    /**
     * HashCode Implementation using apache-lang
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder()
        .append(csp)
        .append(cloudname)
        .toHashCode();
    }
    
    /**
     * Equals Implementation using apache-lang
     */
    @Override
    public boolean equals(Object obj) {
        if(obj instanceof CSPCloudName){
            final CSPCloudName other = (CSPCloudName) obj;
            return new EqualsBuilder()
                .append(csp, other.csp)
                .append(cloudname.toLowerCase(), other.cloudname.toLowerCase())       
                .isEquals();
        } else{
            return false;
        }
    }
    
    
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("MessageForm [csp=").append(csp)
                .append(", cloudname=").append(cloudname)
                .append("]");
        return builder.toString();
    }
    
}
