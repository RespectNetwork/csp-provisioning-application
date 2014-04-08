package net.respectnetwork.csp.application.manager.sagepay;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sagepay.sdk.api.ApiFactory;
import com.sagepay.sdk.api.IApiConstants;
import com.sagepay.sdk.api.IFormApi;
import com.sagepay.sdk.api.ResponseStatus;
import com.sagepay.sdk.api.messages.IFormPayment;
import com.sagepay.sdk.api.messages.IFormPaymentResult;
import com.sagepay.sdk.api.util.Basket;
import com.sagepay.sdk.api.util.CryptographyHelper;
import net.respectnetwork.csp.application.manager.sagepay.db.Payment;
import com.sagepay.util.lang.Utils;
import com.sagepay.util.web.RequestState;


/**
 * Handler for the "Form" Sage Pay integration method. 
 *   
 */
public class FormKitHandler extends BaseHandler {

	private static Logger LOG = LoggerFactory.getLogger(FormKitHandler.class);

	public static final String SESSION_KEY_FORM_MESSAGE = "vsp_form_msg";

	private IFormApi api;
	
	public FormKitHandler(RequestState rs) {
		super(rs);
		/*
		 * API Call
		 * 
		 * Obtain and store an instance of the appropriate Sage Pay API.
		 */
		api = ApiFactory.getFormApi();
		/*
		 * API Call
		 * 
		 * Optionally configure the API object.
		 * 
		 * For demo purposes we configure the API on every request although
		 * you are unlikely to do that in production.
		 */
		api.configure(getConfig().getRawProperties());
	}
	
	@Override
	public IntegrationType getIntegrationType() {
		return IntegrationType.FORM;
	}
	
	/**
	 * The main entry point for a request.
	 * Call appropriate handler method based on URL path.
	 * Exceptions are caught here and handled generically. 
	 */
	@Override
	public final void dispatch() {
		try {
			String path = rs.localPath1;
			if ("".equals(path)) {
				path = "welcome";
			}
			if ("welcome".equals(path)) {
				welcome();
			} else if ("basket".equals(path)) {
				basket();
			} else if ("details".equals(path)) {
				String nextUrl;

				BigDecimal version = new BigDecimal(this.getConfigValue(KitConstants.PROTOCOL_VERSION));
				if (!isBasketXMLDisabled() && version.compareTo(new BigDecimal("3.0")) >= 0) {
					nextUrl = "/extra-information";
				} else {
					nextUrl = "/confirm/";
				}
				details(getMsg(), nextUrl);
			} else if ("confirm".equals(path)) {
				confirm();
			} else if ("message".equals(path)) {
				message();
			} else if ("success".equals(path)) {
				success();
			} else if ("failure".equals(path)) {
				failure();
			} else if ("extra-information".equals(path)) {
				extraInformation(getMsg(), "/confirm/", null); 
			} else  {
				missing();
			}
		} catch (Exception e) {
			handleException(e);
		}
	}
	
	@Override
	public final void cleanup() {
		// Empty: no resources need destroying
	}
	
	/*-----------------------------------------------------------------------------
	 * 
	 * Public page handler methods
	 * 
	 *----------------------------------------------------------------------------- 
	 */
	
	void welcome() {
		Map<String,Object> data = new HashMap<String,Object>();
		data.put("purchaseUrl", getApiEndpointUrl(api, "sagepay.api.formPaymentUrl"));
		data.put("vendorName", getConfigValue(KitConstants.VENDOR_NAME));
		data.put("fullUrl", getFullUrl("/"));
		data.put("currency", getConfigValue(KitConstants.CURRENCY));
		data.put("siteFqdn", getConfigValue(KitConstants.SITE_FQDN));
		data.put("isEncryptionPasswordOk", isEncryptionPasswordOk());
		rs.set("data", data);
		render("form/welcome");
	}

	/*
	 * A little utility method to check encryption password is valid.
	 */
	boolean isEncryptionPasswordOk() {
		final String password = getConfigValue(KitConstants.ENCRYPTION_PASSWORD);
		Exception ex = null;
		try {
			String test = "a test string to verify encryption password is working";
			byte[] enc = CryptographyHelper.AESEncrypt(test, "ISO-8859-1", password);
			byte[] dec = CryptographyHelper.AESDecrypt(enc, password);
			String comp = new String(dec, "ISO-8859-1");
			return test.equals(comp);
		} catch (InvalidKeyException e) {
			ex = e;
		} catch (NoSuchAlgorithmException e) {
			ex = e;
		} catch (NoSuchPaddingException e) {
			ex = e;
		} catch (IllegalBlockSizeException e) {
			ex = e;
		} catch (BadPaddingException e) {
			ex = e;
		} catch (InvalidAlgorithmParameterException e) {
			ex = e;
		} catch (UnsupportedEncodingException e) {
			ex = e;
		} catch (IllegalArgumentException e) {
			ex = e;
		}
		LOG.error(ex.getMessage(), ex);
		return false;
		
	}
	
	void confirm() {
		if (!checkSession()) return;
		if (!checkBasket()) return;
		Basket basket = getCurrentBasket();
		IFormApi api = ApiFactory.getFormApi();
		IFormPayment msg = getMsg();
		setCommonPaymentFields(basket, msg);
		setFormApiSpecificFields(msg);
		
		// generate a new vendor tx code for each 
		msg.setVendorTxCode(generateTransactionCode()); // REQUIRED
		
		// Now we've finished populating, we can build the "Crypt" field
		final String key = getConfigValue(KitConstants.ENCRYPTION_PASSWORD);
		api.encrypt(key, msg);

		String purchaseUrl = getApiEndpointUrl(api, "sagepay.api.formPaymentUrl");
		assert(purchaseUrl != null && !purchaseUrl.isEmpty());
		
		rs.set("msg", msg);
		rs.set("basket", getCurrentBasket());
		rs.set("purchaseUrl", purchaseUrl);
		rs.set("displayQueryString", buildQueryString(msg));
		render("form/confirm");
	}

	void success() {
		result("success");
	}

	void failure() {
		result("failure");
	}

	void result(String result) {
		if (!"success".equals(result) && !"failure".equals(result)) 
			throw new IllegalArgumentException("Result must be success or failure");
		String crypt= rs.params.getMandatoryString("crypt");
		IFormApi api = ApiFactory.getFormApi();
		/*
		 * API Call 
		 * 
		 * Decrypt the returned parameter into a result object.
		 */
		IFormPaymentResult fps = 
			api.decrypt(getConfigValue(KitConstants.ENCRYPTION_PASSWORD), crypt);
		LOG.info("Form Payment Result: " + fps.toString());
		ResponseStatus status = fps.getStatus();
		String reason;
		boolean ok = result.equals("success");
		rs.set("msg", getMsg());
		if (ok) {
			final Payment ord = getManager().find(Payment.class, fps.getVendorTxCode());
			rs.set("basket",getCurrentCheckoutData().getBasket());
			//clearCurrentBasket();
		}
		else {
			// Determine the reason this transaction was unsuccessful
			if (status.equals(ResponseStatus.NOTAUTHED))
				reason = "You payment was declined by the bank.  This could be due to insufficient funds, or incorrect card details.";
			else if (status.equals(ResponseStatus.ABORT))
				reason = "You chose to Cancel your order on the payment pages.";
			else if (status.equals(ResponseStatus.REJECTED)) 
				reason = "Your order did not meet our minimum fraud screening requirements.";
			else if (status.equals(ResponseStatus.INVALID) || status.equals(ResponseStatus.MALFORMED))
				reason = "We could not process your order because we have been unable to register your transaction with our Payment Gateway.";
			else if (status.equals(ResponseStatus.ERROR))
				reason = "We could not process your order because our Payment Gateway service was experiencing difficulties.";
			else
				reason = "The transaction process failed. Please contact us with the date and time of your order and we will investigate.";
			rs.set("reason", reason);
		}	
		rs.set("res", fps);
		rs.set("isSuccess", ok);
		render("form/result");
	}	
	
	/*
	 * Marshal the message object into a query string ready for encryption.
	 * Use the toMap method of the API to produce a string map representation.  
	 */
	private String buildQueryString(IFormPayment msg) {
		Map<String,String> map = api.toMap(IFormPayment.class, msg);
		map.remove("Crypt");
		return Utils.toQueryString(map, false, false, IApiConstants.ISO_8859_1);
	}
	
	/*
	 * Create or return the current session's payment request
	 */
	private IFormPayment getMsg() {
		IFormPayment msg = (IFormPayment) rs.req.getSession().getAttribute(SESSION_KEY_FORM_MESSAGE);
		if (msg == null) {
			msg = ApiFactory.getFormApi().newFormPaymentRequest();
			rs.req.getSession().setAttribute(SESSION_KEY_FORM_MESSAGE, msg);
			setTestData(msg);
		}
		return msg;
	}

	/*
	 * Set fields specific to this integration type
	 */
	private void setFormApiSpecificFields(IFormPayment msg) {
		// Set mandatory fields.
		msg.setSuccessUrl(getFullUrl("/success/")); 
		msg.setFailureUrl(getFullUrl("/failure/")); 
		// Optionally set the customer name in a single field.
		msg.setCustomerName(msg.getBillingFirstnames() + " " + msg.getBillingSurname());
		msg.setVendorEmail(getConfigValue(KitConstants.VENDOR_EMAIL));
		msg.setSendEmail(new Integer(getConfigValue(KitConstants.SEND_EMAIL)));
		msg.setEmailMessage(getConfigValue(KitConstants.EMAIL_MESSAGE));
		String allowGiftAid = getConfigValue(KitConstants.ALLOW_GIFT_AID);
		msg.setAllowGiftAid(Utils.empty(allowGiftAid) ? 0 : new Integer(allowGiftAid));
	}
	
}
