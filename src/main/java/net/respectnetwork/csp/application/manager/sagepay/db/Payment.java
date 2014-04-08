package net.respectnetwork.csp.application.manager.sagepay.db;

import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import com.sagepay.sdk.api.CheckResult;
import com.sagepay.sdk.api.ThreeDSecureStatus;
import com.sagepay.sdk.api.TransactionType;
import com.sagepay.util.lang.Utils;

/**
 * Payment entity.
 * 
 */
@Entity
public class Payment {

	@Id
	@Column(nullable=false, length=40) 
	String vendorTxCode;
	
	@Enumerated(javax.persistence.EnumType.STRING)
	@Column(nullable=false, length=32)
	TransactionType transactionType;

	@Column(nullable=false)
	BigDecimal amount;

    @Column(nullable=true)
    BigDecimal surcharge;

	@Column(length=3)
	String currency;

	@Column(length=20)
	String billingSurname;
	@Column(length=20)
	String billingFirstnames;
	@Column(length=100)
	String billingAddress1;
	@Column(length=100)
	String billingAddress2;
	@Column(length=40)
	String billingCity;
	@Column(length=2)
	String billingCountry;
	@Column(length=2)
	String billingState;
	@Column(length=20)
	String billingPhone;
	@Column(length=10)
	String billingPostCode;
	
	@Column(length=20)
	String deliverySurname;
	@Column(length=20)
	String deliveryFirstnames;
	@Column(length=100)
	String deliveryAddress1;
	@Column(length=100)
	String deliveryAddress2;
	@Column(length=40)
	String deliveryCity;
	@Column(length=2)
	String deliveryCountry;
	@Column(length=2)
	String deliveryState;
	@Column(length=20)
	String deliveryPhone;
	@Column(length=10)
	String deliveryPostCode;
	
	
	@Column(length=200)
	String customerEmail;

	@Column(length=64)
	String vpsTxId;

	@Column(length=10)
	String securityKey;
	
	@Column(nullable=true)
	Integer txAuthNo;
	
	@Column(length=50)
	String avsCv2;

	@Enumerated(javax.persistence.EnumType.STRING)
	@Column(length=20)
	CheckResult addressResult;
	
	@Enumerated(javax.persistence.EnumType.STRING)
	@Column(length=20)
	CheckResult postCodeResult;

	@Enumerated(javax.persistence.EnumType.STRING)
	@Column(length=20)
	CheckResult cv2Result;
	
	@Column
	Integer giftAid;

	@Enumerated(javax.persistence.EnumType.STRING)
	@Column(length=50)
	ThreeDSecureStatus threeDSecureStatus;

	@Column(length=32)
	String cavv;
	
	@Column(length=40)
	String relatedVendorTxCode;
	
	@Column(length=20)
	String status;
	
	@Column(length=255)
	String statusMessage;

	@Column(length=20)
	String addressStatus;
	
	@Column(length=20)
	String payerStatus;
	
	@Column(length=15)
	String cardType;

	@Column(length=4)
	String last4Digits;

	@Column(length=4)
	String expiryDate;

	@Column(length=15) // PayPal payer id
	String payerId;

	/*
	 * Basket is a legacy format with colon separated fields
	 */
	@Column(length=7500)
	String basket;

	/*
	 * BasketXml is new from Protocol 3.0 onwards 
	 */
	@Column(length=7500)
	String basketXml;

	/* 
	 * After a DEFERRED or AUTHENTICATE has been successfully released or authorised 
	 * this should be set to the Amount actually captured via a release or authorise 
	 * (always set to same as original amount for PAYMENTs) 
	 */
	@Column
	BigDecimal capturedAmount;  

	/*
	 * Sage Pay token identifier for a payment made with a token.  
	 */
	@Column(length=40) 
	String token;
	
	/*
	 * flat set to create a token from transaction
	 */
	@Column(length=1)
	Integer createToken;
	
	/*
	 * Set on creation time only 
	 */
	@Column(nullable=false)
	@Temporal(TemporalType.TIMESTAMP)
	Date created;

	/*
	 * Updated on every modification (update) 
	 */
	@Column(nullable=false)
	@Temporal(TemporalType.TIMESTAMP)
	Date modified;
	
	
	//-------------------------------------------------------------------------
	// Business logic
	//-------------------------------------------------------------------------

	@PrePersist
	public void prePersist() {
		this.created = this.modified = new Date();
	}
	
	@PreUpdate
	public void preUpdate() {
		this.modified = new Date();
	}	

	@Override
	public String toString() {
		return Utils.toString(this, false);
	}
	
	//-------------------------------------------------------------------------
	// Boilerplate auto-generated getters & setters
	//-------------------------------------------------------------------------
	
	public BigDecimal getCapturedAmount() {
		return capturedAmount;
	}

	public void setCapturedAmount(BigDecimal capturedAmount) {
		this.capturedAmount = capturedAmount;
	}

	public String getVendorTxCode() {
		return vendorTxCode;
	}

	public void setVendorTxCode(String vendorTxCode) {
		this.vendorTxCode = vendorTxCode;
	}

	public TransactionType getTransactionType() {
		return transactionType;
	}

	public void setTransactionType(TransactionType txType) {
		this.transactionType = txType;
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}

    public BigDecimal getSurcharge() {
        return surcharge;
    }

    public void setSurcharge(BigDecimal surcharge) {
        this.surcharge = surcharge;
    }

	public String getCurrency() {
		return currency;
	}

	public void setCurrency(String currency) {
		this.currency = currency;
	}

	public String getBillingSurname() {
		return billingSurname;
	}

	public void setBillingSurname(String billingSurname) {
		this.billingSurname = billingSurname;
	}

	public String getBillingFirstnames() {
		return billingFirstnames;
	}

	public void setBillingFirstnames(String billingFirstnames) {
		this.billingFirstnames = billingFirstnames;
	}

	public String getBillingAddress1() {
		return billingAddress1;
	}

	public void setBillingAddress1(String billingAddress1) {
		this.billingAddress1 = billingAddress1;
	}

	public String getBillingAddress2() {
		return billingAddress2;
	}

	public void setBillingAddress2(String billingAddress2) {
		this.billingAddress2 = billingAddress2;
	}

	public String getBillingCity() {
		return billingCity;
	}

	public void setBillingCity(String billingCity) {
		this.billingCity = billingCity;
	}

	public String getBillingCountry() {
		return billingCountry;
	}

	public void setBillingCountry(String billingCountry) {
		this.billingCountry = billingCountry;
	}

	public String getBillingState() {
		return billingState;
	}

	public void setBillingState(String billingState) {
		this.billingState = billingState;
	}

	public String getBillingPhone() {
		return billingPhone;
	}

	public void setBillingPhone(String billingPhone) {
		this.billingPhone = billingPhone;
	}

	public String getBillingPostCode() {
		return billingPostCode;
	}

	public void setBillingPostCode(String billingPostCode) {
		this.billingPostCode = billingPostCode;
	}

	public String getDeliverySurname() {
		return deliverySurname;
	}

	public void setDeliverySurname(String deliverySurname) {
		this.deliverySurname = deliverySurname;
	}

	public String getDeliveryFirstnames() {
		return deliveryFirstnames;
	}

	public void setDeliveryFirstnames(String deliveryFirstnames) {
		this.deliveryFirstnames = deliveryFirstnames;
	}

	public String getDeliveryAddress1() {
		return deliveryAddress1;
	}

	public void setDeliveryAddress1(String deliveryAddress1) {
		this.deliveryAddress1 = deliveryAddress1;
	}

	public String getDeliveryAddress2() {
		return deliveryAddress2;
	}

	public void setDeliveryAddress2(String deliveryAddress2) {
		this.deliveryAddress2 = deliveryAddress2;
	}

	public String getDeliveryCity() {
		return deliveryCity;
	}

	public void setDeliveryCity(String deliveryCity) {
		this.deliveryCity = deliveryCity;
	}

	public String getDeliveryCountry() {
		return deliveryCountry;
	}

	public void setDeliveryCountry(String deliveryCountry) {
		this.deliveryCountry = deliveryCountry;
	}

	public String getDeliveryState() {
		return deliveryState;
	}

	public void setDeliveryState(String deliveryState) {
		this.deliveryState = deliveryState;
	}

	public String getDeliveryPhone() {
		return deliveryPhone;
	}

	public void setDeliveryPhone(String deliveryPhone) {
		this.deliveryPhone = deliveryPhone;
	}

	public String getDeliveryPostCode() {
		return deliveryPostCode;
	}

	public void setDeliveryPostCode(String deliveryPostCode) {
		this.deliveryPostCode = deliveryPostCode;
	}

	public String getCustomerEmail() {
		return customerEmail;
	}

	public void setCustomerEmail(String customerEmail) {
		this.customerEmail = customerEmail;
	}

	public String getVpsTxId() {
		return vpsTxId;
	}

	public void setVpsTxId(String vpsTxId) {
		this.vpsTxId = vpsTxId;
	}

	public String getSecurityKey() {
		return securityKey;
	}

	public void setSecurityKey(String securityKey) {
		this.securityKey = securityKey;
	}

	public Integer getTxAuthNo() {
		return txAuthNo;
	}

	public void setTxAuthNo(Integer txAuthNo) {
		this.txAuthNo = txAuthNo;
	}

	public String getAvsCv2() {
		return avsCv2;
	}

	public void setAvsCv2(String avsCv2) {
		this.avsCv2 = avsCv2;
	}

	public Integer getGiftAid() {
		return giftAid;
	}

	public void setGiftAid(Integer giftAid) {
		this.giftAid = giftAid;
	}

	public String getCavv() {
		return cavv;
	}

	public void setCavv(String cavv) {
		this.cavv = cavv;
	}

	public String getRelatedVendorTxCode() {
		return relatedVendorTxCode;
	}

	public void setRelatedVendorTxCode(String relatedVendorTxCode) {
		this.relatedVendorTxCode = relatedVendorTxCode;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getAddressStatus() {
		return addressStatus;
	}

	public void setAddressStatus(String addressStatus) {
		this.addressStatus = addressStatus;
	}

	public String getPayerStatus() {
		return payerStatus;
	}

	public void setPayerStatus(String payerStatus) {
		this.payerStatus = payerStatus;
	}

	public String getCardType() {
		return cardType;
	}

	public void setCardType(String cardType) {
		this.cardType = cardType;
	}

	public String getLast4Digits() {
		return last4Digits;
	}

	public void setLast4Digits(String last4Digits) {
		this.last4Digits = last4Digits;
	}

	public String getStatusMessage() {
		return statusMessage;
	}

	public void setStatusMessage(String statusMessage) {
		this.statusMessage = statusMessage;
	}

	public String getPayerId() {
		return payerId;
	}

	public void setPayerId(String payerId) {
		this.payerId = payerId;
	}

	public String getBasket() {
		return basket;
	}

	public void setBasket(String basket) {
		this.basket = basket;
	}

	public CheckResult getAddressResult() {
		return addressResult;
	}

	public void setAddressResult(CheckResult addressResult) {
		this.addressResult = addressResult;
	}

	public CheckResult getPostCodeResult() {
		return postCodeResult;
	}

	public void setPostCodeResult(CheckResult postCodeResult) {
		this.postCodeResult = postCodeResult;
	}

	public CheckResult getCv2Result() {
		return cv2Result;
	}

	public void setCv2Result(CheckResult cv2Result) {
		this.cv2Result = cv2Result;
	}

	public ThreeDSecureStatus getThreeDSecureStatus() {
		return threeDSecureStatus;
	}

	public void setThreeDSecureStatus(ThreeDSecureStatus threeDSecureStatus) {
		this.threeDSecureStatus = threeDSecureStatus;
	}

	public String getExpiryDate() {
		return expiryDate;
	}

	public void setExpiryDate(String expiryDate) {
		this.expiryDate = expiryDate;
	}

	public String getBasketXml() {
		return basketXml;
	}

	public void setBasketXml(String basketXml) {
		this.basketXml = basketXml;
	}
	
	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public Date getCreated() {
		return created;
	}

	public void setCreated(Date created) {
		this.created = created;
	}

	public Date getModified() {
		return modified;
	}

	public void setModified(Date modified) {
		this.modified = modified;
	}

	public void setCreateToken(Integer createToken) {
		this.createToken = createToken;
	}
	
	public Integer getCreateToken() {
		return createToken;
	}
		
}
