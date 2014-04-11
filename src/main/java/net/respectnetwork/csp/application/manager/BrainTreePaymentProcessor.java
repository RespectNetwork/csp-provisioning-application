package net.respectnetwork.csp.application.manager;

import java.math.BigDecimal;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;



import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.respectnetwork.csp.application.controller.PersonalCloudController;
import net.respectnetwork.csp.application.model.CSPModel;
import net.respectnetwork.csp.application.model.PaymentModel;

import com.braintreegateway.BraintreeGateway;
import com.braintreegateway.Environment;
import com.braintreegateway.Result;
import com.braintreegateway.Transaction;
import com.braintreegateway.TransactionRequest;

public class BrainTreePaymentProcessor
{
   
   private  static BraintreeGateway gateway = null;
   /** CLass Logger */
   private static final Logger  logger = LoggerFactory
                                             .getLogger(BrainTreePaymentProcessor.class);
   
   private static BraintreeGateway getGateway(CSPModel cspModel)
   {
      
         Environment env = null;
         if(cspModel.getEnv().equals("SANDBOX"))
         {
            env = Environment.SANDBOX;  
         } else if(cspModel.getEnv().equals("DEVELOPMENT"))
         {
            env = Environment.DEVELOPMENT;  
         }else if(cspModel.getEnv().equals("PRODUCTION"))
         {
            env = Environment.PRODUCTION;  
         }
         logger.debug("BT ENV" + env.toString());
         if (gateway == null) { 
            gateway = new BraintreeGateway(
                env,
                cspModel.getUsername(),
                cspModel.getPassword(),
                cspModel.getUser_key()
            );
            //logger.debug("BT fields " + cspModel.getUsername() + "::" + cspModel.getPassword() + "::" + cspModel.getUser_key());
         }
      return gateway;
   }
//   private static BraintreeGateway gateway = new BraintreeGateway(
//         Environment.SANDBOX,
//         "dcq4j958kgwrtvkp",
//         "gtgqhw37rwm24qjx",
//         "676293fe617a7cb9991c17bdc963ddfd"
//     );
   
   /**
    * 
    * @param csp
    * @param amount
    * @param desc
    * @return
    */
   /*
    * <script src="https://js.braintreegateway.com/v1/braintree.js"></script>
                  <script>
                        var braintree = Braintree.create('MIIBCgKCAQEArworyo4oER+csDsp5Lx0HRyHILcih2jWbKWIMENOn89fYChCno5r5SXvAT7JaxPTd7fbKxQZEFjIPA3rZkJmlCpW8+ko2FrDg6PZAF2I1PREa5ENCXk5JXX1HyJcHOOB7LzogPsuewQHGWwQeCT8Y5CcN7g6cjfAdINcJCsZyuWgIB4SrX0mx0m8a2pwv4Ztj5KgOuoX4jQQNYYXhkOAK+rqsJ3JW8hsykU/E38l3CWzV7ki5CSlN/o1G6wfTxzlyf3YgDto+LNp0PwqM2QnlFrAcbGFTNQ+KNQzLt27n3PG2dL42CBosSzm5UxVJk6Px85ufs5IM6W4ecJmJKw1XQIDAQAB');
                        braintree.onSubmitEncryptForm('cc_payment_form');
                  </script>
    */
   public static String getJavaScript( CSPModel csp )
   {
      
      StringBuilder builder = new StringBuilder();

      builder.append(csp.getEnc_key())
             
             ;

      //logger.debug("BT enc " + builder.toString());
      return builder.toString();
   }
   public static PaymentModel makePayment(CSPModel csp ,BigDecimal amount , HttpServletRequest request)
   {
      PaymentModel payment = new PaymentModel();
      payment.setPaymentId(UUID.randomUUID().toString());
      payment.setCspCloudName(csp.getCspCloudName());
      
      payment.setAmount(amount);
      payment.setCurrency(csp.getCurrency());
       
      TransactionRequest transactionRequest = new TransactionRequest()
      .amount(amount)
      .creditCard()
          .number(request.getParameter("number"))
          .cvv(request.getParameter("cvv"))
          .expirationMonth(request.getParameter("month"))
          .expirationYear(request.getParameter("year"))
          .done()
          .options()
          .submitForSettlement(true)
          .done();

      
      Result<Transaction> result = getGateway(csp).transaction().sale(transactionRequest);
      //logger.debug(" BT Result " + result.getErrors());
      if (result != null && result.isSuccess()) {
         payment.setPaymentReferenceId(result.getTarget().getId());
         payment.setPaymentResponseCode("OK");
         return payment;
         
       } else {
          logger.debug("BT transaction didn't go through");
          return null;
         
       }
   }
   
   
}
