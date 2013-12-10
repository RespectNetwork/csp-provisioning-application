package net.respectnetwork.csp.application.form;


import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.hibernate.validator.constraints.Length;


/**
 * Class for Data used in UserDatails Form 
 *
 */
public class UserForm {
	
    /** Name */
    private String name;

    /** NickName */
    private String nickName;

    /** Street */
    private String street;

    /** City */
    private String city;

    /** State */
    private String state;

    /** PostalCode */
    private String postalcode;
    
    /** Email */
    private String email;  
    
    /** Mobile Phone */
    @Length(min=9)
    private String phone;

	/**
     * @return the name
     */
    public String getName() {
        return name;
    }
    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the nickName
     */
    public String getNickName() {
        return nickName;
    }
    /**
     * @param nickName the nickName to set
     */
    public void setNickName(String nickName) {
        this.nickName = nickName;
    }  
    
    /**
     * @return the Street
     */
    public String getStreet() {
        return street;
    }
    /**
     * @param Street the Street to set
     */
    public void setStreet(String street) {
        this.street = street;
    }
    
    /**
     * 
     * @return
     */
    public String getCity() {
        return city;
    }
    
    /**
     * 
     * @param city
     */
    public void setCity(String city) {
        this.city = city;
    }
    
    /**
     * 
     * @return
     */
    public String getState() {
        return state;
    }
    
    /**
     * 
     * @param state
     */
    public void setState(String state) {
        this.state = state;
    }
    
    /**
     * 
     * @return
     */
    public String getPostalcode() {
        return postalcode;
    }
    
    /**
     * 
     * @param postalcode
     */
    public void setPostalcode(String postalcode) {
        this.postalcode = postalcode;
    }
    
    /**
     * 
     * @return
     */
	public String getEmail() {
		return email;
	}
	
	/**
	 * 
	 * @param email
	 */
	public void setEmail(String email) {
		this.email = email;
	}
	
	/**
	 * 
	 * @return
	 */
	public String getPhone() {
		return phone;
	}
	
    /**
     * 
     * @param phone
     */
	public void setPhone(String phone) {
		this.phone = phone;
	}

	
    
	   /**
     * To String Implementation.
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("CSPCredential [name=").append(name)
            .append(", nickName=").append(nickName)
            .append(", phone=").append(phone)
            .append(", email=").append(email)
            .append(", street=").append(street)
            .append(", city=").append(city)
            .append(", state=").append(state)
            .append(", postcode=").append(postalcode)
            .append("]");
        return builder.toString();
    }
    
    /**
     * HashCode Implementation using apache-lang
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder()
        .append(name)
        .append(nickName)
        .append(phone)
        .append(email)
        .append(street)
        .append(city)
        .append(state)
        .append(postalcode)
        .toHashCode();
    }
    
    /**
     * Equals Implementation using apache-lang
     */
    @Override
    public boolean equals(Object obj) {
        if(obj instanceof UserForm){
            final UserForm other = (UserForm) obj;
            return new EqualsBuilder()
                .append(name, other.name)
                .append(nickName, other.nickName)
                .append(phone, other.phone)
                .append(email, other.email)
                .append(street, other.street)
                .append(city, other.city)
                .append(state, other.state)
                .append(postalcode, other.postalcode)
                .isEquals();
        } else{
            return false;
        }
    }
	
}
