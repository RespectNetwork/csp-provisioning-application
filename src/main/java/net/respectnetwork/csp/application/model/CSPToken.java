package net.respectnetwork.csp.application.model;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("session")
public class CSPToken {
    
    private String secretToken;

    /**
     * @return the secretToken
     */
    public String getSecretToken() {
        return secretToken;
    }
    
    public CSPToken() {
        this.secretToken = null;
    }

    public CSPToken(String secretToken) {
        this.secretToken = secretToken;
    }

    /**
     * @param secretToken the secretToken to set
     */
    public void setSecretToken(String secretToken) {
        this.secretToken = secretToken;
    }
    

}
