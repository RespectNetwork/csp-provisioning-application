package net.respectnetwork.csp.application.manager.sagepay;

/**
 *  The ways of integrating with SagePay.
 *  
 */
public enum IntegrationType {

	DIRECT("Direct"),
	SERVER("Server"),
	FORM("Form"),
	;	
	
	final private String name;
	
	private IntegrationType(String s) {
		this.name = s;
	}

	public String getName() {
		return name;
	}
	
}
