package net.respectnetwork.csp.application.manager.sagepay;

import java.io.Serializable;
import java.util.Date;

import com.sagepay.sdk.api.messages.IPayment;
import com.sagepay.sdk.api.util.Basket;
import net.respectnetwork.csp.application.manager.sagepay.db.Customer;


/**
 * Stores the payment and checkout information collected during the checkout process. 
 *   
 */
public class CheckoutData implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private IPayment paymentMessage;
	private boolean useToken = false;
	private Basket basket = new Basket();
	private String loggedInCustomerEmail;
	
	//fields to build customerxml 
	private Date dateOfBirth;
	private String customerWorkPhone;
	private String customerMobilePhone;
	
	public Date getDateOfBirth() {
		return dateOfBirth;
	}

	public void setDateOfBirth(Date dateOfBirth) {
		this.dateOfBirth = dateOfBirth;
	}

	public String getCustomerWorkPhone() {
		return customerWorkPhone;
	}

	public void setCustomerWorkPhone(String customerWorkPhone) {
		this.customerWorkPhone = customerWorkPhone;
	}

	public String getCustomerMobilePhone() {
		return customerMobilePhone;
	}

	public void setCustomerMobilePhone(String customerMobilePhone) {
		this.customerMobilePhone = customerMobilePhone;
	}

	public Basket getBasket() {
		return basket;
	}

	public void setBasket(Basket basket) {
		this.basket = basket;
	}

	public boolean isUseToken() {
		return useToken;
	}

	public void setUseToken(boolean useToken) {
		this.useToken = useToken;
	}

	public IPayment getPaymentMessage() {
		return paymentMessage;
	}

	public void setPaymentMessage(IPayment paymentMessage) {
		this.paymentMessage = paymentMessage;
	}

	public boolean isLoggedIn() {
		return loggedInCustomerEmail != null;
	}

	public String getLoggedInCustomerEmail() {
		return loggedInCustomerEmail;
	}

	public void setLoggedInCustomerEmail(String loggedInCustomerEmail) {
		this.loggedInCustomerEmail = loggedInCustomerEmail;
	}
	
}
