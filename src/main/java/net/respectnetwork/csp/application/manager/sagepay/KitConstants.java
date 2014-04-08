package net.respectnetwork.csp.application.manager.sagepay;

import com.sagepay.sdk.api.IApiConstants;

/**
 *  Keys that can be defined in the kit's main properties configuration file.
 */
public interface KitConstants {

	/** Class path location of the config properties file */
	public static final String PROPS_FILE = "/sagepay-java-kit.properties";
	
	/** Name of our environment flag */
	public static final String VSP_ENV = IApiConstants.VSP_ENV;

    /** Version of protocol in use */
    public static final String PROTOCOL_VERSION = "sagepay.api.protocolVersion";
	
	/** Key to store config against in the JEE Servlet Context */
	public static final String CTX_KEY_CONF = "sagepay.kit.config";
	
	/** name of the JPA persistence unit for Server/Direct kits */
	public static final String PERSISTENCE_UNIT_NAME = "SagePayKitPersistenceUnit";

	public static final String OVERRIDES_FILE_CONTEXT_PARAM_NAME = "configurationPropertiesHostOverridesFile";

	public static final String VENDOR_NAME = "sagepay.kit.vendorName";
	public static final String CURRENCY = "sagepay.kit.currency";
	public static final String PARTNER_ID = "sagepay.kit.partnerId";
	public static final String SITE_FQDN = "sagepay.kit.siteFqdn";
	public static final String ACCOUNT_TYPE = "sagepay.kit.direct.accountType";
	public static final String CUST_PASS_SALT = "sagepay.kit.direct.customerPasswordSalt";
	public static final String DEFAULT_TRANSACTION_TYPE = "sagepay.kit.defaultTransactionType";
	public static final String APPLY_AVS_CV2 = "sagepay.kit.applyAvsCv2";
	public static final String APPLY_3DS = "sagepay.kit.apply3dSecure";
	public static final String DISPLAY_DEBUG_ERRORS = "sagepay.kit.displayDebugErrors";
	public static final String POPULATE_TEST_DATA = "sagepay.kit.populateFormsWithTestData";
	public static final String LOAD_PROPS_EVERY_REQUEST = "sagepay.kit.loadPropsFileEveryRequest";
	
	/** Allow Gift Aid (only applies to Server & Form protocols) */
	public static final String ALLOW_GIFT_AID = "sagepay.kit.allowGiftAid";

	public static final String VENDOR_EMAIL = "sagepay.kit.form.vendorEmail";
	public static final String ENCRYPTION_PASSWORD = "sagepay.kit.form.encryptionPassword";
	public static final String SEND_EMAIL = "sagepay.kit.form.sendEmail";
	public static final String EMAIL_MESSAGE = "sagepay.kit.form.emailMessage";
	
	public static final String SURCHARGE_XML = "sagepay.kit.surcharge.xml";

	public static final String COLLECT_RECIPIENT_DETAILS = "sagepay.kit.collectRecipientDetails";

    public static final String DISABLE_BASKET_XML = "sagepay.kit.basketxml.disable";
    public static final String BILLING_AGREEMENT = "sagepay.kit.billingAgreement";
}
