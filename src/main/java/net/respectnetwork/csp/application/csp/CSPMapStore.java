/**
 * 
 */
package net.respectnetwork.csp.application.csp;

import java.util.HashMap;
import java.util.Map;

/**
 * CSP Store Using 
 *
 */
public class CSPMapStore implements CSPStore, Cloneable {
    
    /** CSP Store Map */
    private Map<String, CSP> cspMap = new HashMap<String, CSP>();

    /**
     * CSPMap Getter
     * @return
     */
    public Map<String, CSP> getCspMap() {
        return cspMap;
    }

    /**
     * CSPMap Setter
     * @param cspMap
     */
    public void setCspMap(Map<String, CSP> cspMap) {
        this.cspMap = cspMap;
    }

    /**
     * Get CSP By Identifier
     * @see net.respectnetwork.csp.application.csp.application.csp.CSPStore#getCSPbyIdentifier(java.lang.String)
     */
    @Override
    public CSP getCSPbyIdentifier(String id) {
        
        CSP theCSP = cspMap.get(id);   
        
        return theCSP;
    }

    
    @Override
    public CSPMapStore clone(){
        Map<String, CSP> clonedMap = new HashMap<String, CSP>(cspMap);
        CSPMapStore clonedMapStore = new CSPMapStore();
        clonedMapStore.setCspMap(clonedMap);
        return clonedMapStore;        
    }

}
