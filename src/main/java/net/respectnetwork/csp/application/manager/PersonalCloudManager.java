package net.respectnetwork.csp.application.manager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import xdi2.discovery.XDIDiscoveryClient;

public class PersonalCloudManager {
	
    /** Class Logger */
    private static final Logger logger = LoggerFactory
            .getLogger(PersonalCloudManager.class);
    
    private XDIDiscoveryClient personalCloudDiscoveryClient;

	public XDIDiscoveryClient getPersonalCloudDiscoveryClient() {
		return personalCloudDiscoveryClient;
	}
	@Autowired
	public void setPersonalCloudDiscoveryClient(
			XDIDiscoveryClient personalCloudDiscoveryClient) {
		this.personalCloudDiscoveryClient = personalCloudDiscoveryClient;
	}
    
    


}
