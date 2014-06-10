package net.respectnetwork.csp.application.manager;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.respectnetwork.csp.application.dao.DAOException;
import net.respectnetwork.csp.application.dao.DAOFactory;
import net.respectnetwork.csp.application.model.CSPModel;
import net.respectnetwork.csp.application.model.PaymentModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sagepay.sdk.api.ApiFactory;
import com.sagepay.sdk.api.IFormApi;
import com.sagepay.sdk.api.ResponseStatus;
import com.sagepay.sdk.api.messages.IFormPayment;
import com.sagepay.sdk.api.messages.IFormPaymentResult;
import com.sagepay.util.web.RequestState;

public class SagePayPaymentProcessor
{
   /** CLass Logger */
   private static final Logger  logger = LoggerFactory
                                             .getLogger(SagePayPaymentProcessor.class);
   
   public static String getSagePayCrypt(HttpServletRequest request, BigDecimal amount,String currency, String key)
   {
      logger.debug("SagePayPaymentProcessor::getSagePayCrypt");
      IFormApi api = ApiFactory.getFormApi();
      IFormPayment msg =  ApiFactory.getFormApi().newFormPaymentRequest();
   
      String cspHomeURL = request.getContextPath();
      
      if(request.getServerName().equalsIgnoreCase("localhost"))
      {
         msg.setSuccessUrl("http://" + request.getServerName() + ":" + request.getServerPort() + cspHomeURL + "/ccpayment/"); 
         msg.setFailureUrl("http://" + request.getServerName() + ":" + request.getServerPort() + cspHomeURL + "/ccpayment/"); 
      } else 
      {
         msg.setSuccessUrl("https://" + request.getServerName() +  cspHomeURL + "/ccpayment/"); 
         msg.setFailureUrl("https://" + request.getServerName() +  cspHomeURL + "/ccpayment/");
      }
      msg.setVendorTxCode(UUID.randomUUID().toString());
      msg.setDescription("Purchase Personal Cloud(s)");
      msg.setCustomerName(request.getParameter("BillingFirstNames") + " " + request.getParameter("BillingSurname"));
      msg.setBillingFirstnames(request.getParameter("BillingFirstNames"));
      msg.setBillingSurname(request.getParameter("BillingSurname"));
      msg.setBillingAddress1(request.getParameter("BillingAddress1"));
      msg.setBillingCity(request.getParameter("BillingCity"));
      msg.setBillingCountry(request.getParameter("BillingCountry"));
      msg.setBillingPostCode(request.getParameter("BillingPostCode"));
      if(request.getParameter("BillingState") != null && !request.getParameter("BillingState").isEmpty())
      {
         msg.setBillingState(request.getParameter("BillingState"));
      }
      msg.setDeliveryFirstnames(request.getParameter("BillingFirstNames"));
      msg.setDeliverySurname(request.getParameter("BillingSurname"));
      msg.setDeliveryAddress1(request.getParameter("BillingAddress1"));
      msg.setDeliveryCity(request.getParameter("BillingCity"));
      msg.setDeliveryCountry(request.getParameter("BillingCountry"));
      msg.setDeliveryPostCode(request.getParameter("BillingPostCode"));

      msg.setCurrency(currency);
      msg.setAmount(amount);
      
      api.encrypt(key, msg);
      
      Map<String,String> map = api.toMap(IFormPayment.class, msg);
      
      String crypt = map.get("Crypt");
      return crypt;
   }
   
   public static PaymentModel processSagePayCallback(HttpServletRequest request , HttpServletResponse response , CSPModel cspModel, String currency)
   {
      final RequestState rs = new RequestState(request, response, request.getSession().getServletContext());
      String crypt= rs.params.getMandatoryString("crypt");
      IFormApi api = ApiFactory.getFormApi();
      /*
       * API Call 
       * 
       * Decrypt the returned parameter into a result object.
       */
      IFormPaymentResult fps = 
         api.decrypt(cspModel.getPassword(), crypt);
      logger.debug("Form Payment Result: " + fps.toString());
      ResponseStatus status = fps.getStatus();
      if(status.equals(ResponseStatus.OK)) 
      {
         PaymentModel payment = new PaymentModel();
         payment.setPaymentId(fps.getVendorTxCode());
         payment.setCspCloudName(cspModel.getCspCloudName());
         payment.setPaymentReferenceId(fps.getVpsTxId());
         payment.setPaymentResponseCode(fps.getStatus().toString());
         payment.setAmount(fps.getAmount());
         payment.setCurrency(currency);
         return payment;
      } else
      {
         return null;
      }
   }

}
