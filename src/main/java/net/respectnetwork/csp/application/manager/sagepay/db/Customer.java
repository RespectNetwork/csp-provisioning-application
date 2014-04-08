package net.respectnetwork.csp.application.manager.sagepay.db;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 * Customer entity.
 * 
 */
@Entity
public class Customer implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue
	long id;
	
	@Column(nullable=false, length=60)
	String email;
	
	@Column(nullable=false, length=32)
	String hashedPassword;
	
	@OneToMany(cascade=CascadeType.ALL, fetch=FetchType.EAGER)
	@JoinColumn(name="customer_id", referencedColumnName="id")
	Set<CustomerCard> customerCards = new HashSet<CustomerCard>();

	@Column(nullable=false)
	@Temporal(TemporalType.TIMESTAMP)
	Date modified;
	
	@Column(nullable=false)
	@Temporal(TemporalType.TIMESTAMP)
	Date created;
	
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

	//-------------------------------------------------------------------------
	// Boilerplate auto-generated getters & setters
	//-------------------------------------------------------------------------
	
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getHashedPassword() {
		return hashedPassword;
	}

	public void setHashedPassword(String p) {
		this.hashedPassword = p;
	}

	public Set<CustomerCard> getCustomerCards() {
		return customerCards;
	}

	public void setCustomerCards(Set<CustomerCard> customerCards) {
		this.customerCards = customerCards;
	}

	public Date getModified() {
		return modified;
	}

	public void setModified(Date modified) {
		this.modified = modified;
	}

	public Date getCreated() {
		return created;
	}

	public void setCreated(Date created) {
		this.created = created;
	}
}