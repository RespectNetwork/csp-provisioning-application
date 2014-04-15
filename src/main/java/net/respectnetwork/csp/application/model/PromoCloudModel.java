package net.respectnetwork.csp.application.model;

import java.util.Date;

public class PromoCloudModel
{
   private String promo_id;
   private String cloudname ;
   private Date creation_date ;
   private String csp_cloudname ;
   @Override
   public int hashCode()
   {
      final int prime = 31;
      int result = 1;
      result = prime * result
            + ((cloudname == null) ? 0 : cloudname.hashCode());
      result = prime * result
            + ((creation_date == null) ? 0 : creation_date.hashCode());
      result = prime * result
            + ((csp_cloudname == null) ? 0 : csp_cloudname.hashCode());
      result = prime * result + ((promo_id == null) ? 0 : promo_id.hashCode());
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
      PromoCloudModel other = (PromoCloudModel) obj;
      if (cloudname == null)
      {
         if (other.cloudname != null)
            return false;
      } else if (!cloudname.equals(other.cloudname))
         return false;
      if (creation_date == null)
      {
         if (other.creation_date != null)
            return false;
      } else if (!creation_date.equals(other.creation_date))
         return false;
      if (csp_cloudname == null)
      {
         if (other.csp_cloudname != null)
            return false;
      } else if (!csp_cloudname.equals(other.csp_cloudname))
         return false;
      if (promo_id == null)
      {
         if (other.promo_id != null)
            return false;
      } else if (!promo_id.equals(other.promo_id))
         return false;
      return true;
   }
   public String getPromo_id()
   {
      return promo_id;
   }
   public void setPromo_id(String promo_id)
   {
      this.promo_id = promo_id;
   }
   public String getCloudname()
   {
      return cloudname;
   }
   public void setCloudname(String cloudname)
   {
      this.cloudname = cloudname;
   }
   public Date getCreation_date()
   {
      return creation_date;
   }
   public void setCreation_date(Date creation_date)
   {
      this.creation_date = creation_date;
   }
   public String getCsp_cloudname()
   {
      return csp_cloudname;
   }
   public void setCsp_cloudname(String csp_cloudname)
   {
      this.csp_cloudname = csp_cloudname;
   }
   @Override
   public String toString()
   {
      return "PromoCloudModel [promo_id=" + promo_id + ", cloudname="
            + cloudname + ", creation_date=" + creation_date
            + ", csp_cloudname=" + csp_cloudname + "]";
   }
   
}
