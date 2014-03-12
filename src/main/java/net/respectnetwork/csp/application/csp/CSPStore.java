package net.respectnetwork.csp.application.csp;

import java.util.Map;

/**
 * Interface for Getting CSP Information
 */
public interface CSPStore {
    
    
    /**
     * Get CSP  Details by Unique Identifier
     * 
     * @param id CSP  Identifier aka  path Selector
     * @return CSP selected
     */
    public CSP getCSPbyIdentifier(String id);
    
    /**
     * Get Full CSP Map
     * 
     * @return map
     */
    public Map<String, CSP> getCspMap();
    
    /**
     * CSPMap Setter
     * 
     * @param cspMap
     */   
    public void setCspMap(Map<String, CSP> cspMap);
    
    /**
     * CSPMap Cloner
     * 
     * @param cspMap
     */   
    public CSPStore clone();

}
