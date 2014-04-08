package net.respectnetwork.csp.application.manager.sagepay.db;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import com.sagepay.util.lang.Utils;

/**
 * Entity for a tokenised Card.
 * 
 */
@Entity
public class CustomerCard implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue
	long id;
	
	@Column(nullable=false, length=40)
	String token;

	@Column(nullable=false, length=4)
	String last4digits;

	@ManyToOne(optional=false)
	Customer customer;

	/* Set on creation only */
	@Column(nullable=false)
	@Temporal(TemporalType.TIMESTAMP)
	Date created;

	/* Updated on every modification (update) */
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
	
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public Customer getCustomer() {
		return customer;
	}

	public void setCustomer(Customer customer) {
		this.customer = customer;
	}

	public String getLast4digits() {
		return last4digits;
	}

	public void setLast4digits(String last4digits) {
		this.last4digits = last4digits;
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
}
