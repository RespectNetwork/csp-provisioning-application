package net.respectnetwork.csp.application.csp;

import java.math.BigDecimal;

/**
 * Represents a currency and cost.
 */
public class CurrencyCost {

   // TODO - this should probably be a java.util.Currency, but we currently use Strings everywhere else
   private String currencyCode;

   private BigDecimal amount;


   public CurrencyCost(String currencyCode, BigDecimal amount) {
      if (currencyCode == null) {
         throw new IllegalArgumentException("currencyCode cannot be null");
      }

      if (amount == null) {
         throw new IllegalArgumentException("amount cannot be null");
      }

      this.currencyCode = currencyCode;
      this.amount = amount;
   }


   public String getCurrencyCode() {
      return currencyCode;
   }

   public BigDecimal getAmount() {
      return amount;
   }

   public CurrencyCost multiply(BigDecimal multiplicand) {
      return new CurrencyCost(currencyCode, amount.multiply(multiplicand));
   }

   public CurrencyCost multiply(int multiplicand) {
      if (multiplicand == 1) {
         return this;
      } else
      {
         return multiply(new BigDecimal(multiplicand));
      }
   }
}
