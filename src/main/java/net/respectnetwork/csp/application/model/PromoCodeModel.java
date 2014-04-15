package net.respectnetwork.csp.application.model;

import java.util.Date;

public class PromoCodeModel
{
   private String promo_id ;
   private Date start_date;
   private Date end_date;
   private int promo_limit;
   public String getPromo_id()
   {
      return promo_id;
   }
   public void setPromo_id(String promo_id)
   {
      this.promo_id = promo_id;
   }
   public Date getStart_date()
   {
      return start_date;
   }
   public void setStart_date(Date start_date)
   {
      this.start_date = start_date;
   }
   public Date getEnd_date()
   {
      return end_date;
   }
   public void setEnd_date(Date end_date)
   {
      this.end_date = end_date;
   }
   public int getPromo_limit()
   {
      return promo_limit;
   }
   public void setPromo_limit(int promo_limit)
   {
      this.promo_limit = promo_limit;
   }
   @Override
   public int hashCode()
   {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((end_date == null) ? 0 : end_date.hashCode());
      result = prime * result + ((promo_id == null) ? 0 : promo_id.hashCode());
      result = prime * result + promo_limit;
      result = prime * result
            + ((start_date == null) ? 0 : start_date.hashCode());
      return result;
   }
   @Override
   public boolean equals(Object obj)
   {
      if (this == obj)
         return true;
      if (obj == null)
         return false;
      if (getClass() != obj.getClass())
         return false;
      PromoCodeModel other = (PromoCodeModel) obj;
      if (end_date == null)
      {
         if (other.end_date != null)
            return false;
      } else if (!end_date.equals(other.end_date))
         return false;
      if (promo_id == null)
      {
         if (other.promo_id != null)
            return false;
      } else if (!promo_id.equals(other.promo_id))
         return false;
      if (promo_limit != other.promo_limit)
         return false;
      if (start_date == null)
      {
         if (other.start_date != null)
            return false;
      } else if (!start_date.equals(other.start_date))
         return false;
      return true;
   }
   @Override
   public String toString()
   {
      return "PromoCodeModel [promo_id=" + promo_id + ", start_date="
            + start_date + ", end_date=" + end_date + ", promo_limit="
            + promo_limit + "]";
   }
   
   
}
