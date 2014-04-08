package net.respectnetwork.csp.application.manager.sagepay;

import java.io.IOException;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sagepay.sdk.api.ICommonApi;
import com.sagepay.sdk.api.TransactionType;
import com.sagepay.sdk.api.messages.IPayment;
import com.sagepay.sdk.api.util.ApiFormHelper;
import com.sagepay.sdk.api.util.Basket;
import com.sagepay.sdk.api.util.Basket.TripsInfo;
import com.sagepay.sdk.api.util.BasketXmlFormatter;
import com.sagepay.sdk.api.util.LineItem;
import com.sagepay.sdk.api.util.Products;
import com.sagepay.sdk.api.xml.CustomerXml;
import net.respectnetwork.csp.application.manager.sagepay.db.Customer;
import net.respectnetwork.csp.application.manager.sagepay.db.CustomerCard;
import com.sagepay.sdk.impl.ProtocolType;
import com.sagepay.util.lang.ConfigProperties;
import com.sagepay.util.lang.UncheckedException;
import com.sagepay.util.lang.Utils;
import com.sagepay.util.validation.IValidationResult;
import com.sagepay.util.validation.ValidationErrorCode;
import com.sagepay.util.validation.ValidationException;
import com.sagepay.util.web.Form;
import com.sagepay.util.web.FormInput;
import com.sagepay.util.web.IRequestHandler;
import com.sagepay.util.web.RequestState;
import com.sagepay.util.web.WebUtils;

/*
 * Provide common request handler methods and utilities.   
 */
public abstract class BaseHandler implements IRequestHandler {

	private static final int MAX_ITEM_QTY = 50;

	// The key prefix used in the JEE session to store the customer's checkout data
	public static final String SESSION_KEY_CHECKOUT_DATA = "sagepay_checkout_";

	// The path to the JSP view files
	public static final String VIEWS_PATH = "/WEB-INF/views/";

	private static final String[] CAR_FIELDS = new String[] {
				"carFrom", "carTo"
		};

	private static final String[] TOUR_FIELDS = new String[] {
				"tourFrom", "tourTo"
		};

	private static final String[] CRUISE_FIELDS = new String[] {
				"cruiseFrom", "cruiseTo"
		};

	private static final String[] HOTEL_FIELDS = new String[] {
				"hotelFrom", "hotelTo", "numberInParty", "guestName", "referenceNumber", "roomRate"
		};

	private static final Set<String> FI_RECIPIENT_DETAILS_FIELD_NAMES = fiRecipientDetailsFieldNames();

	private static Map<String, String> MESSAGES;
	private static Logger LOG = LoggerFactory.getLogger(BaseHandler.class);

	static {
		MESSAGES = new LinkedHashMap<String, String>();
		MESSAGES.put("session-timeout", "Sorry your session has timed out");
		MESSAGES.put("empty-basket", "Sorry your basket appears to have no items in it");
		MESSAGES.put("unknown-id", "Sorry we could not find the transaction details");
		MESSAGES.put("logout", "Thanks, you are now logged out");
	}
	
	// store our unique database manager
	private EntityManager manager;

	// store the state of the request cycle
	protected final RequestState rs;

	/*
	 * Subclasses must tell us their integration type.
	 */
	public abstract IntegrationType getIntegrationType();

	/*
	 * A handler is associated with a single State object for its entire life.
	 */
	public BaseHandler(final RequestState rs) {
		this.rs = rs;
	}

	/*-----------------------------------------------------------------------------
	 * 
	 * Public page handler methods
	 * 
	 *----------------------------------------------------------------------------- 
	 */

	public void error(String msg) {
		error(msg, null);
	}

	public void error(Throwable e) {
		error(null, e);
	}

	public void error(String msg, Throwable e) {
		rs.res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
		LOG.debug("error() msg={} throwable={} display={}", new Object[] {
				msg, e, isDisplayDebug()
		});
		if (null != e && isDisplayDebug()) {
			Throwable exception = ExceptionUtils.getRootCause(e);
			if (exception == null)
				exception = e;
			rs.set("stackTrace", ExceptionUtils.getStackTrace(exception));
			rs.set("exception", exception);
		}
		if (msg == null)
			msg = e.getMessage();
		rs.set("errorMessage", msg);
		render("common/error");
	}

	public void missing() {
		rs.res.setStatus(HttpServletResponse.SC_NOT_FOUND);
		render("common/missing");
	}

	public void message() {
		String m = rs.req.getParameter("m");
		String disp = getDisplayMessage(m);
		if (null == disp)
			disp = "Sorry, no message specified.";
		rs.set("message", disp);
		render("common/message");
	}

	public void basket() {
		Basket basket = getCurrentBasket();
		if (rs.isPost()) {
			if (!checkSession())
				return;
			basket.clear();
			for (int i = 1; i <= 3; i++) {
				String qs = rs.req.getParameter("quantity" + i);
				if (!Utils.empty(qs)) {
					int qty = Integer.valueOf(qs);
					if (qty > MAX_ITEM_QTY || qty < 0) {
						throw new ValidationException("Quantity must be between 0 and " + MAX_ITEM_QTY);
					} else if (qty > 0) {
						LineItem item = new LineItem();
						item.qty = qty;
						item.product = Products.getInstance().getProductById(i);
						basket.addItem(item);
					}
				}
			}
			if (basket.getNumItems() > 0) {
				LineItem delivery = new LineItem();
				delivery.qty = 1;
				delivery.product = Products.getInstance().getDeliveryProduct();
				basket.setDeliveryItem(delivery);
				if (IntegrationType.DIRECT.equals(getIntegrationType()))
					localRedirect("/basket-checkout/");
				else
					localRedirect("/details/");
				return;
			} else {
				rs.set("pageError", "You did not select any items to buy. Please select at least 1 item.");
			}
		}
		rs.set("quantities", basket.getQuantitiesMap());
		rs.set("products", Products.getInstance().getAllProducts());
		render("common/basket");
	}

	public void details(IPayment msg, String nextUrl) {
		if (!checkSession())
			return;
		if (!checkBasket())
			return;
		
		CheckoutData cd = getCurrentCheckoutData();
		if (rs.isPost()) {
			Map<String, String> map = new HashMap<String, String>(rs.params);
			boolean delSame = (null != map.get("isDeliverySame"));
			getCurrentBasket().setDeliverToBillingAddress(delSame);
			Form form = getDetailsForm();
			boolean isUsaBilling = "US".equals(map.get("billingCountry"));
			boolean isUsaDel = delSame && isUsaBilling || !delSame && "US".equals(map.get("deliveryCountry"));
			if (isUsaBilling)
				form.getInput("billingState").setMandatory(true);
			if (!delSame)
				setDeliveryMandatoryFields(form, isUsaDel);
			form.validate(map);
			IValidationResult val = form.getResult();
			if (!isUsaBilling && !Utils.empty(map.get("billingState"))) {
				val.setStatus("billingState", ValidationErrorCode.ILLEGAL);
				map.put("billingState", null);
			}
			if (!isUsaDel && !Utils.empty(map.get("deliveryState"))) {
				val.setStatus("deliveryState", ValidationErrorCode.ILLEGAL);
				map.put("deliveryState", null);
			}
			String dob = map.get("dateOfBirth");
			Date parsedDob = null;
			if(dob != null && !dob.isEmpty()) {
				try {
					parsedDob = new SimpleDateFormat("dd-MM-yyyy").parse(dob);
				} catch (ParseException e) {
					val.setStatus("dateOfBirth", ValidationErrorCode.INVALID);
					
				}
			}
			if(map.get("token") !=null && !Utils.empty(map.get("token"))) {
				cd.setUseToken(true);
			}
			if (val.isOk()) {
				
				//store dateOfBirth to later build customer xml;
				cd.setDateOfBirth(parsedDob);
				Collection<String> allowed = form.getInputNames();
				LOG.debug("Allowed={} MapKeys={}", allowed, map.keySet());
				Utils.setBeanProperties(msg, map, allowed);
				if (delSame)
					deliverToBilling(msg);
				localRedirect(nextUrl);
				return;
			} else {
				rs.set("allTokens", getAllTokensForCustomer(cd));
				rs.set("form", form);
				rs.set("formVal", val);
				rs.set("curr", rs.params);
				rs.set("dateOfBirth", map.get("dateOfBirth"));
				
			}
		} else {
			rs.set("allTokens", getAllTokensForCustomer(cd));
			rs.set("curr", msg);
		}
		rs.set("deliverToBillingAddress", getCurrentBasket().isDeliverToBillingAddress());
		render("common/details");
	}

	private Set<CustomerCard> getAllTokensForCustomer(CheckoutData cd) {
		Set<CustomerCard> tokens = null;
		if(cd.isLoggedIn()) {
			Customer customer = findCustomerByEmail(cd.getLoggedInCustomerEmail(), true);
			tokens = customer.getCustomerCards();
		}
		return tokens;
	}

	/*-----------------------------------------------------------------------------
	 * 
	 * Utility & framework methods.
	 * 
	 *----------------------------------------------------------------------------- 
	 */

	/*
	 * Return or create the entity manager for this handler object. As there is a handler per thread we don't need any
	 * synchronisation.
	 */
	protected final EntityManager getManager() {
		if (this.manager == null) {
			this.manager = JpaFactory.getInstance().getFactory(KitConstants.PERSISTENCE_UNIT_NAME).createEntityManager();
		}
		return this.manager;
	}

	/*
	 * Close the database manager at the end of the request.
	 */
	@Override
	public void cleanup() {
		if (null != this.manager)
			this.manager.close();
	}
	
	/*
	 * Find the customer entity by email address, and if mand is true then throw an exception if not found.
	 */
	protected Customer findCustomerByEmail(String email, boolean mand) {
		TypedQuery<Customer> q = getManager().createQuery("select c from Customer c where email = ?", Customer.class);
		q.setParameter(1, email);
		int size = q.getResultList().size();
		if (size == 0 && mand)
			throw new UncheckedException("Customer not found in database");
		return size > 0 ? q.getResultList().get(0) : null;
	}

	/*
	 * Log full error details and if possible display an error page.
	 */
	protected void handleException(Exception e) {
		LOG.error(e.getMessage(), e);
		if (!rs.res.isCommitted())
			error(e);
	}

	/*
	 * Set a few commonly needed view variables as request attributes.
	 */
	protected void preRender() {
		// The deployment path of this webapp, beginning with a slash but with
		// no trailing slash.
		// e.g. "/webapp"
		rs.set("WEBAPP", rs.contextPath);

		// The path of the webapp followed by the first path component, beginning
		// with a slash but with no trailing slash.
		// e.g. "/webapp/path0"
		rs.set("WEBPATH", getHandlerPath());

		// The full absolute URI (including protocol, servername, etc) up to and
		// including the webapp context path, but not including any further path.
		// e.g. "http://server:837/webapp/"
		rs.set("BASE_URI", WebUtils.getAbsoluteContextUri(rs.req) + "/");

		// The current configuration environment is used by some view pages for
		// conditional display of information.
		// e.g. "SIMULATOR"
		rs.set("ENV", getConfigEnv());

		rs.set("CURRENCY", getConfigValue(KitConstants.CURRENCY));
		rs.set("VENDOR_NAME", getConfigValue(KitConstants.VENDOR_NAME));
		rs.set("DISPLAY_ERRORS", isDisplayDebug());
		rs.set("INTEGRATION_TYPE", getIntegrationType().toString());
		rs.set("CHECKOUT_DATA", getCurrentCheckoutData());
	}

	/*
	 * Render a view JSP enclosed by the default main template JSP.
	 */
	protected void render(String viewName) {
		final String mainTemplate = VIEWS_PATH + "common/template.jsp";
		render(viewName, mainTemplate);
	}

	/*
	 * Render a view JSP enclosed by the given template JSP.
	 */
	protected void render(String viewName, String template) {
		preRender();
		WebUtils.setNoHttpCache(rs.res);
		try {
			String viewPath = VIEWS_PATH + viewName + ".jsp";
			RequestDispatcher r;
			if (template != null) {
				rs.set("mainViewName", viewPath);
				r = rs.req.getRequestDispatcher(template);
			} else {
				r = rs.req.getRequestDispatcher(viewPath);
			}
			r.forward(rs.req, rs.res);
		} catch (ServletException e) {
			throw new UncheckedException(e);
		} catch (IOException e) {
			throw new UncheckedException(e);
		}
	}

	/*
	 * Redirect the client to a relative or absolute URI.
	 * 
	 * @see javax.servlet.http.HttpServletResponse#sendRedirect
	 */
	protected void redirect(String url) {
		try {
			LOG.debug("Sending redirect to " + url);
			rs.res.sendRedirect(url);
		} catch (IOException e) {
			throw new UncheckedException(e);
		}
	}

	/*
	 * Redirect to a path relative to the current integration kit URL. e.g. "/welcome/".
	 */
	protected void localRedirect(String methodPath) {
		if (methodPath.charAt(0) != '/')
			throw new IllegalArgumentException("Local redirects to path must begin with a /");
		redirect(getHandlerPath() + methodPath);
	}

	/*
	 * Gets the relative URI path to the current handler (begins with '/', and does not end with '/'). e.g.
	 * "/sagepay-java-kit/server"
	 */
	protected String getHandlerPath() {
		final String path = !Utils.empty(rs.localPath0) ? (rs.contextPath + "/" + rs.localPath0) : rs.contextPath;
		return path;
	}

	/*
	 * Get full abosulte http/https url relative to the current handler for the supplied method path.
	 */
	String getFullUrl(String methodPath) {
		if (methodPath.charAt(0) != '/')
			throw new IllegalArgumentException("Argument path should start with '/'");
		return getAbsoluteSiteUrl() + getHandlerPath() + methodPath;
	}

	/*
	 * Return the full absolute site URL from configuration property or else attempt to infer it from the current
	 * request details (protocol, host and port). Note the returned value does NOT include a trailing '/'. e.g.
	 * "https://www.example.com:8443"
	 */
	String getAbsoluteSiteUrl() {
		String site = getConfigValue(KitConstants.SITE_FQDN);
		if (!Utils.empty(site)) {
			site = site.replaceFirst("/$", "");
			return site;
		}
		StringBuilder sb = new StringBuilder();
		if (rs.req.isSecure())
			sb.append("https://");
		else
			sb.append("http://");
		sb.append(rs.req.getServerName());
		int port = rs.req.getServerPort();
		if (rs.req.isSecure() && port != 443)
			sb.append(":" + port);
		else if (!rs.req.isSecure() && port != 80)
			sb.append(":" + port);
		return sb.toString();
	}

	String getApiEndpointUrl(ICommonApi api, String key) {
		return api.getConfiguredProperty(key);
	}

	boolean inSimulatorMode() {
		return "SIMULATOR".equals(getConfigEnv());
	}

	String getConfigEnv() {
		return getConfig().getEnv();
	}

	String getConfigValue(String key) {
		return getConfig().getString(key);
	}

	Boolean getConfigBoolean(String key) {
		return getConfig().getBoolean(key);
	}

	ConfigProperties getConfig() {
		return (ConfigProperties) rs.ctx.getAttribute(KitConstants.CTX_KEY_CONF);
	}

	String getDisplayMessage(String m) {
		return MESSAGES.get(m);
	}

	boolean useTestData() {
        return getConfig().getBoolean(KitConstants.POPULATE_TEST_DATA);
    }

    boolean isBasketXMLDisabled() {
        Boolean disableBasketXML = getConfig().getBoolean(KitConstants.DISABLE_BASKET_XML);
        return disableBasketXML != null ? disableBasketXML : false;
    }

	// speedup testing for development purposes only
	void setTestData(IPayment msg) {
		if (!useTestData())
			return;
		LOG.warn("Test data mode is enabled. Pre-populating forms with dummy data");
		msg.setBillingFirstnames("Fname Mname");
		msg.setBillingSurname("Surname");
		msg.setBillingAddress1("BillAddress Line 1");
		msg.setBillingAddress2("BillAddress Line 2");
		msg.setBillingCity("BillCity");
		msg.setBillingPostCode("W1A 1BL");
		msg.setBillingCountry("GB");
		msg.setBillingPhone("44 (0)7933 000 000");
		CheckoutData cd = getCurrentCheckoutData();
		if (cd.isLoggedIn()) {
			msg.setCustomerEmail(cd.getLoggedInCustomerEmail());
		} else {
			msg.setCustomerEmail("customer@example.com");
		}
	}

	void setCommonPaymentFields(Basket basket, IPayment msg) {
		msg.setTransactionType(TransactionType.valueOf(getConfigValue(KitConstants.DEFAULT_TRANSACTION_TYPE))); // REQUIRED
		msg.setVendor(getConfigValue(KitConstants.VENDOR_NAME)); // REQUIRED
		msg.setAmount(basket.getTotalGrossPrice()); // REQUIRED
		msg.setCurrency(basket.getCurrency()); // REQUIRED
		msg.setReferrerId(getConfigValue(KitConstants.PARTNER_ID));
		msg.setSurchargeXml(getConfigValue(KitConstants.SURCHARGE_XML));

		// set the basket string, which has a max of 7500 characters,
		// set to basket xml instead of colonSeparated basket if present.
		if (msg.getBasketXml() == null) {
			msg.setBasket(basket.toColonSeparatedBasketString());
		} else {
			msg.setBasket(null);
		}
		// up to 100 chars of description
		msg.setDescription(StringUtils.abbreviate("The best DVDs from " + msg.getVendor(), 100));
		if (!TransactionType.AUTHENTICATE.equals(msg.getTransactionType())) {
			// Allow fine control over AVS/CV2 checks and rules by changing this value.
			// See the Protocol documentation.
			msg.setApplyAvsCv2(getConfig().getMandatoryInteger(KitConstants.APPLY_AVS_CV2));
		} else {
			msg.setApplyAvsCv2(null);
		}
		// Allow fine control over 3D-Secure checks and rules by changing this value.
		// It can be changed dynamically, per transaction, if you wish.
		// See the Protocol documentation
		msg.setApply3dSecure(getConfig().getMandatoryInteger(KitConstants.APPLY_3DS));
	}

	// Generate a simple unique TxCode
	// Ensure TxCodes are no longer than 40 characters
	String generateTransactionCode() {
		String vendorName = getConfigValue(KitConstants.VENDOR_NAME);
		String txCode = vendorName.substring(0, Math.min(18, vendorName.length())) + // 18 chars
				"-" + // 1 char
				System.currentTimeMillis() + // 13 chars
				"-" + // 1 char
				(int) Math.abs(Math.random() * 1000000); // 6 chars
		return txCode;
	}

	String getSessionKeyName() {
		return SESSION_KEY_CHECKOUT_DATA + getIntegrationType();
	}

	CheckoutData getCurrentCheckoutData() {
		String key = getSessionKeyName();
		CheckoutData ret = (CheckoutData) rs.req.getSession().getAttribute(key);
		if (ret == null) {
			ret = new CheckoutData();
			ret.getBasket().setCurrency(getConfigValue(KitConstants.CURRENCY));
			rs.req.getSession().setAttribute(key, ret);
		}
		// for demo purposes only, set the currency every time to
		// immediately detect any props file changes
		ret.getBasket().setCurrency(getConfigValue(KitConstants.CURRENCY));
		return ret;
	}

	void clearCheckoutData() {
		LOG.debug("Clearing checkout data from session");
		rs.req.getSession(true).removeAttribute(getSessionKeyName());
	}

	Basket getCurrentBasket() {
		return getCurrentCheckoutData().getBasket();
	}

	void clearCurrentBasket() {
		LOG.debug("Clearing basket");
		CheckoutData cd = getCurrentCheckoutData();
		cd.getBasket().clear();
	}

	void deliverToBilling(IPayment msg) {
		msg.setDeliverySurname(msg.getBillingSurname());
		msg.setDeliveryFirstnames(msg.getBillingFirstnames());
		msg.setDeliveryAddress1(msg.getBillingAddress1());
		msg.setDeliveryAddress2(msg.getBillingAddress2());
		msg.setDeliveryCity(msg.getBillingCity());
		msg.setDeliveryCountry(msg.getBillingCountry());
		msg.setDeliveryPostCode(msg.getBillingPostCode());
		msg.setDeliveryState(msg.getBillingState());
		msg.setDeliveryPhone(msg.getBillingPhone());
	}

	Form getDetailsForm() {
		Form f = new Form("CustomerDetails");
		ApiFormHelper h = new ApiFormHelper();
		f.add(h.buildFormInput("text", "token", "Previous Payment Method", false));
		f.add(h.buildFormInput("text", "billingFirstnames", "Billing First Name(s)", true));
		f.add(h.buildFormInput("text", "billingSurname", "Billing Surname", true));
		f.add(h.buildFormInput("text", "middleInitial",ProtocolType.INITIAL.getDataType() ,"Middle Initial", false));
		f.add(h.buildFormInput("text", "billingAddress1", "Billing Address Line 1", true));
		f.add(h.buildFormInput("text", "billingAddress2", "Billing Address Line 2", false));
		f.add(h.buildFormInput("text", "billingCity", "Billing City", true));
		f.add(h.buildFormInput("text", "billingPostCode", "Billing Post/Zip Code", false));
		f.add(h.buildFormInput("text", "billingCountry", "Billing Country", true));
		f.add(h.buildFormInput("text", "billingState", "Billing State Code (U.S. only)", false));
		f.add(h.buildFormInput("text", "billingPhone", "Phone", false));
		f.add(h.buildFormInput("text", "customerEmail", "Email Address", false));
		f.add(h.buildFormInput("text", "dateOfBirth", ProtocolType.DATE.getDataType(), "Date of Birth", false));
		f.add(new FormInput("text", "isDeliverySame", "Same as billing details?", false));
		f.add(h.buildFormInput("text", "deliveryFirstnames", "Delivery First Name(s)", false));
		f.add(h.buildFormInput("text", "deliverySurname", "Delivery Surname", false));
		f.add(h.buildFormInput("text", "deliveryAddress1", "Delivery Address Line 1", false));
		f.add(h.buildFormInput("text", "deliveryAddress2", "Delivery Address Line 2", false));
		f.add(h.buildFormInput("text", "deliveryCity", "Delivery City", false));
		f.add(h.buildFormInput("text", "deliveryPostCode", "Delivery Post/Zip Code", false));
		f.add(h.buildFormInput("text", "deliveryCountry", "Delivery Country", false));
		f.add(h.buildFormInput("text", "deliveryState", "Delivery State Code (U.S. only)", false));
		f.add(h.buildFormInput("text", "deliveryPhone", "Delivery Phone", false));
		return f;
	}

	void setDeliveryMandatoryFields(Form f, boolean isUsa) {
		f.getInput("deliveryFirstnames").setMandatory(true);
		f.getInput("deliverySurname").setMandatory(true);
		f.getInput("deliveryAddress1").setMandatory(true);
		f.getInput("deliveryCity").setMandatory(true);
		f.getInput("deliveryPostCode").setMandatory(false);
		f.getInput("deliveryCountry").setMandatory(true);
		if (isUsa) {
			f.getInput("deliveryState").setMandatory(true);
		}
	}

	boolean checkSession() {
		if (rs.isNewSession()) {
			localRedirect("/message/?m=session-timeout");
			return false;
		}
		return true;
	}

	boolean checkBasket() {
		Basket basket = getCurrentBasket();
		if (basket.getNumItems() == 0) {
			localRedirect("/message/?m=empty-basket");
			return false;
		}
		return true;
	}

	void requirePost() {
		if (!this.rs.isPost())
			throw new UncheckedException("HTTP POST ONLY");
	}

	void requireGet() {
		if (!this.rs.isGet())
			throw new UncheckedException("HTTP GET ONLY");
	}

	boolean isDisplayDebug() {
		return getConfigBoolean(KitConstants.DISPLAY_DEBUG_ERRORS);
	}

	String getCharset() {
		return Charset.defaultCharset().name();
	}

	String getPaymentErrorStatusDescription(String status) {
		String reason;
		// Determine the reason this transaction was unsuccessful
		if (null == status) {
			reason = "Unknown, no status field returned";
		} else if (status.equals("DECLINED"))
			reason = "You payment was declined by the bank.  This could be due to insufficient funds, or incorrect card details.";
		else if (status.equals("NOTAUTHED"))
			reason = "You payment was declined by the bank.  This could be due to insufficient funds, or incorrect card details.";
		else if (status.equals("ABORT"))
			reason = "You chose to Cancel your order on the payment pages.";
		else if (status.equals("REJECTED"))
			reason = "Your order did not meet our minimum fraud screening requirements.";
		else if (status.equals("INVALID") || status.equals("MALFORMED"))
			reason = "The Payment Gateway rejected some of the information provided without forwarding it to the bank.";
		else if (status.equals("ERROR"))
			reason = "We could not process your order because our Payment Gateway service was experiencing difficulties.";
		else
			reason = "The transaction process failed.";
		return reason;
	}

	/**
	 * Handle extra customer information. Will take the data in the request and format for the basketxml field in
	 * the protocol.
	 * 
	 * @param payment
	 * @param string 
	 */
	public void extraInformation(IPayment payment, String nextUrl, Customer c) {
		rs.set("collectRecipientDetails", getConfigBoolean(KitConstants.COLLECT_RECIPIENT_DETAILS));
		if (rs.isPost()) {
	
			Map<String, String> map = new HashMap<String, String>(rs.params);
			DateFormat formatter = new SimpleDateFormat("dd-MM-yy");
	
			// update basket with hotel info
			Form form = getExtraInformationForm();
			form.validate(map);
			IValidationResult val = form.getResult();
	
			// validate if all fields are supplied for a trip type.
			// Must supply all or none for a trip type
			if (!validateAllOrNothing(HOTEL_FIELDS, map)) {
				populateValidationResultWithMissingFields(val, HOTEL_FIELDS, map);
			}
			if (!validateAllOrNothing(CRUISE_FIELDS, map)) {
				populateValidationResultWithMissingFields(val, CRUISE_FIELDS, map);
			}
			if (!validateAllOrNothing(TOUR_FIELDS, map)) {
				populateValidationResultWithMissingFields(val, TOUR_FIELDS, map);
			}
			if (!validateAllOrNothing(CAR_FIELDS, map)) {
				populateValidationResultWithMissingFields(val, CAR_FIELDS, map);
			}
			
			
			//passed valiation, start building xml
			if (val.isOk()) {
	
				// validation passed, create basket xml
	
				Date checkIn = parseDate("hotelFrom", formatter, map, val);
				Date checkOut = parseDate("hotelTo", formatter, map, val);
	
				if (checkIn != null && checkOut != null) {
					Basket.HotelInfo hotel = new Basket.HotelInfo();
					hotel.checkIn = checkIn;
					hotel.checkOut = checkOut;
					hotel.numberInParty = (map.get("numberInParty") == null || map.get("numberInParty").isEmpty() ? null : new Integer(map.get("numberInParty")));
					hotel.folioRefNumber = (map.get("referenceNumber"));
					hotel.dailyRoomRate = map.get("roomRate") == null ? new BigDecimal(map.get("roomRate")) : null;
					hotel.guestName = map.get("guestName");
					String confirmed = map.get("confirmedReservation");
					hotel.confirmedReservation = confirmed == null || confirmed.equals("no") ? false : true;
					getCurrentBasket().setHotelInfo(hotel);
				}
	
				getCurrentBasket().setCruiseInfo(getTripInfo("cruiseFrom", "cruiseTo", formatter, map, val));
				getCurrentBasket().setCarRentalInfo(getTripInfo("carFrom", "carTo", formatter, map, val));
				getCurrentBasket().setTourOperatorInfo(getTripInfo("tourFrom", "tourTo", formatter, map, val));
	
				BasketXmlFormatter xmlFormatter = new BasketXmlFormatter();
				String basketXml = xmlFormatter.toXml(payment, getCurrentBasket());
	
				if (basketXml != null && !basketXml.isEmpty()) {
					payment.setBasketXml(basketXml);
				}
				
				//create customer xml
				CheckoutData cd = getCurrentCheckoutData();
				CustomerXml customerXml = new CustomerXml();
				//if existing customer, supply number of days, previous customer field in customer xml
				if(c!= null) {
					Days d = Days.daysBetween(new DateTime(c.getCreated().getTime()),new DateTime());
					customerXml.setTimeOnFile(d.getDays());
					customerXml.setPreviousCust(d.getDays() != 0);
				} else {
					customerXml.setPreviousCust(false);
				}
				
				//get middle initial from second name if supplied in firstnames field
				String[] names = payment.getBillingFirstnames().split("\\s");
				if(names != null && names.length > 1 && !names[1].isEmpty()) {
					customerXml.setCustomerMiddleInitial(names[1].substring(0,1));
				}
				
				customerXml.setCustomerBirth(cd.getDateOfBirth() == null ? null : cd.getDateOfBirth());
				customerXml.setCustomerMobilePhone(cd.getCustomerMobilePhone());
				customerXml.setCustomerWorkPhone(cd.getCustomerWorkPhone());
				
				//marshal customerxml 
				JAXBContext jaxbContext;
				try {
					jaxbContext = JAXBContext.newInstance(com.sagepay.sdk.api.xml.CustomerXml.class);
					javax.xml.bind.Marshaller marshaller = jaxbContext.createMarshaller();
					marshaller.setProperty("jaxb.fragment", Boolean.TRUE);
					StringWriter writer = new StringWriter();
					marshaller.marshal(customerXml,  writer);
					payment.setCustomerXml(writer.toString());
				} catch (JAXBException e) {
					e.printStackTrace();
				}

				setFiRecipientDetailsOnPaymentMsg(payment, map);

				localRedirect(nextUrl);
			} else {
				// back in page with validation errors
				rs.set("form", form);
				rs.set("formVal", val);
				rs.set("curr", rs.params);
				render("common/extra-information");
			}
	
		} else {
			render("common/extra-information");
		}
	}

	private void setFiRecipientDetailsOnPaymentMsg(final IPayment payment, final Map<String, String> parameterMap) {
		Utils.setBeanProperties(payment, parameterMap, FI_RECIPIENT_DETAILS_FIELD_NAMES);
	}

	private Form getExtraInformationForm() {
		Form f = new Form("extraInformationForm");
		ApiFormHelper h = new ApiFormHelper();
		f.add(h.buildFormInput("text", "hotelFrom", ProtocolType.DATE.getDataType(), "Hotel Check In Date", false));
		f.add(h.buildFormInput("text", "hotelTo", ProtocolType.DATE.getDataType(), "Hotel Check Out Date", false));
		f.add(h.buildFormInput("text", "numberInParty", ProtocolType.HOTE_NUMBER_IN_PARTY.getDataType(), "Number In Party", false));
		f.add(h.buildFormInput("text", "guestName", ProtocolType.STRING29.getDataType(), "Guest Name", false));
		f.add(h.buildFormInput("text", "referenceNumber", ProtocolType.HOTEL_REFERENCE.getDataType(), "Reference Number", false));
		f.add(h.buildFormInput("text", "roomRate", ProtocolType.AMOUNT.getDataType(), "Room Rate", false));
		f.add(h.buildFormInput("text", "confirmedReservation", ProtocolType.CHECKBOX.getDataType(), "Confirmed Reservation", false));
		f.add(h.buildFormInput("text", "cruiseFrom", ProtocolType.DATE.getDataType(), "Cruise Check In Date", false));
		f.add(h.buildFormInput("text", "cruiseTo", ProtocolType.DATE.getDataType(), "Cruise Check Out Date", false));
		f.add(h.buildFormInput("text", "carFrom", ProtocolType.DATE.getDataType(), "Car Rental Check In Date", false));
		f.add(h.buildFormInput("text", "carTo", ProtocolType.DATE.getDataType(), "Car Rental Check Out Date", false));
		f.add(h.buildFormInput("text", "tourFrom", ProtocolType.DATE.getDataType(), "Tour Check In Date", false));
		f.add(h.buildFormInput("text", "tourTo", ProtocolType.DATE.getDataType(), "Tour Check Out Date", false));
		// recipient details
		f.add(h.buildFormInput("text", "fiRecipientAcctNumber", ProtocolType.FI_RECIPIENT_ACCT_NUMBER.getDataType(), "Recipient account number", false));
		f.add(h.buildFormInput("text", "fiRecipientDob", ProtocolType.FI_RECIPIENT_DOB.getDataType(), "Recipient date of birth", false));
		f.add(h.buildFormInput("text", "fiRecipientPostCode", ProtocolType.FI_RECIPIENT_POST_CODE.getDataType(), "Recipient post code", false));
		f.add(h.buildFormInput("text", "fiRecipientSurname", ProtocolType.FI_RECIPIENT_SURNAME.getDataType(), "Recipient surname", false));
		return f;
	}

	/**
	 * Tries to parse a date with the formatter specified, if unable, set error in validation result
	 * 
	 * @param fieldName the fieldName in the request to parse
	 * @param formatter date formatter
	 * @param map map of all request elements
	 * @param val validation result to store potential errors to report back to user
	 * @return
	 */
	private Date parseDate(String fieldName, DateFormat formatter, Map<String, String> map, IValidationResult val) {
		try {
			return (map.get(fieldName) == null || map.get(fieldName).isEmpty() ? null : formatter.parse(map.get(fieldName)));
		} catch (ParseException e) {
			val.setStatus(fieldName, ValidationErrorCode.INVALID, fieldName + " must follow dd-mm-yyy format");
		}
	
		return null;
	}

	/**
	 * Build a tripsInfo object from the 2 date fields
	 * 
	 * @param fromField name of from field to parse
	 * @param toField name to from field to parse
	 * @param formatter date formatter
	 * @param map map of all request elements
	 * @param val validation result to store potential errors to report back to user
	 * @return
	 */
	private TripsInfo getTripInfo(String fromField, String toField, DateFormat formatter, Map<String, String> map, IValidationResult val) {
		Date checkIn = parseDate(fromField, formatter, map, val);
		Date checkOut = parseDate(toField, formatter, map, val);
	
		TripsInfo trip = null;
	
		if (checkIn != null && checkOut != null) {
			trip = new TripsInfo();
			trip.checkIn = checkIn;
			trip.checkOut = checkOut;
		}
	
		return trip;
	}

	/**Determines if one or field of the group of fields has been filled in and therefore
	 * all fields need to be populated. 
	 * 
	 * @param fields array of fieldNames which all need to be filled in or none need to be filled in. Cannot have a subset of them populated
	 * @param map map of request parameters
	 * @return boolean to indicate whether the fields need validating.  
	 */
	private boolean validateAllOrNothing(String[] fields, Map<String, String> map) {
	
		int count = 0;
	
		for (String fieldName : fields) {
			String param = map.get(fieldName);
			if (param != null && !param.isEmpty()) {
				count++;
			}
		}
	
		return count == 0 || count == fields.length;
	}

	/**
	 * Populates a validation result will all the missing fields in the group.
	 * 
	 * @param val validation result to populate with missing field errors
	 * @param fields name of fields to check
	 * @param map map of all request parameters
	 */
	private void populateValidationResultWithMissingFields(IValidationResult val, String[] fields, Map<String, String> map) {
		for (String f : fields) {
			String value = map.get(f);
			if (value == null || value.isEmpty()) {
				val.setStatus(f, ValidationErrorCode.MISSING);
			}
		}
	}

	/**
	 * The field names that make up recipient details for use by financial institutions.
	 */
	private static Set<String> fiRecipientDetailsFieldNames() {
		Set<String> names = new HashSet<String>();
		names.add("fiRecipientAcctNumber");
		names.add("fiRecipientDob");
		names.add("fiRecipientPostCode");
		names.add("fiRecipientSurname");
		return names;
	}

}
