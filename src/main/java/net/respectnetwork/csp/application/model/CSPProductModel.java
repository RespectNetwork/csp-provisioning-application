package net.respectnetwork.csp.application.model;

import java.math.BigDecimal;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

/**
 * @author psharma2
 *
 */
public class CSPProductModel {
    private String product_id;
    private BigDecimal product_cost;
    private String product_currency;
    private int duration_unit_years;
    private String csp_name;

    public String getProductId()
    {
       return product_id;
    }

    public void setProductId(String product_id)
    {
       this.product_id = product_id;
    }

    public BigDecimal getProductCost()
    {
       return product_cost;
    }

    public void setProductCost(BigDecimal product_cost)
    {
       this.product_cost = product_cost;
    }

    public String getProductCurrency()
    {
       return product_currency;
    }

    public void setProductCurrency(String product_currency)
    {
       this.product_currency = product_currency;
    }

    public int getProductDuration()
    {
       return duration_unit_years;
    }

    public void setProductDuration(int duration_unit_years)
    {
       this.duration_unit_years = duration_unit_years;
    }

    public String getCspName()
    {
       return csp_name;
    }

    public void setCspName(String csp_name)
    {
       this.csp_name = csp_name;
    }

    public int hashCode()
    {
        return new HashCodeBuilder(17, 37)
                .appendSuper(super.hashCode())
                .append(this.product_id)
                .append(this.product_cost)
                .append(this.product_currency)
                .append(this.duration_unit_years)
                .append(this.csp_name)
                .toHashCode();
    }

    public boolean equals( Object object )
    {
        if( object == null )
        {
            return false;
        }
        if( object == this )
        {
            return true;
        }
        if( this.getClass().equals(object.getClass()) == false )
        {
            return false;
        }
        CSPProductModel other = (CSPProductModel) object;
        return new EqualsBuilder()
                .appendSuper(super.equals(other))
                .append(this.product_id, other.product_id)
                .append(this.product_cost, other.product_cost)
                .append(this.product_currency, other.product_currency)
                .append(this.duration_unit_years, other.duration_unit_years)
                .append(this.csp_name, other.csp_name)
                .isEquals();
    }

    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("CSPProductModel ");
        builder.append("[product_id=")
            .append(this.product_id).append(']');
        builder.append("[product_cost=")
            .append(this.product_cost).append(']');
        builder.append("[product_currency=")
            .append(this.product_currency).append(']');
        builder.append("[duration_unit_years=")
            .append(this.duration_unit_years).append(']');
        builder.append("[csp_name=")
        .append(this.csp_name).append(']');
        builder.append(' ');
        builder.append(super.toString());
        return builder.toString();
    }
}