package net.respectnetwork.csp.application.manager.sagepay;


/**
 * Simple utils.
 *   
 */
public class KitUtils {
	
	public static String getLast4Digits(String num) {
		return num == null || num.length() <= 4 ? null : num.substring(num.length() - 4);
	}
	
}