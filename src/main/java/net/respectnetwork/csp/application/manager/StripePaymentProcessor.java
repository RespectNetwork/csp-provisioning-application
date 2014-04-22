package net.respectnetwork.csp.application.manager;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.stripe.Stripe;
import com.stripe.exception.APIConnectionException;
import com.stripe.exception.APIException;
import com.stripe.exception.AuthenticationException;
import com.stripe.exception.CardException;
import com.stripe.exception.StripeException;
import com.stripe.exception.InvalidRequestException;
import com.stripe.model.Charge;

import net.respectnetwork.csp.application.model.CSPModel;
import net.respectnetwork.csp.application.model.PaymentModel;

public class StripePaymentProcessor
{
	private static final Logger logger = LoggerFactory.getLogger(StripePaymentProcessor.class);
    
	private static long getAmount( BigDecimal amount )
	{
		long rtn = amount.multiply(BigDecimal.valueOf(100.0)).setScale(0, BigDecimal.ROUND_HALF_UP).longValue();

		logger.info("getAmount(" + amount + ") = " + rtn);

		return rtn;
	}

	public static String getJavaScript( CSPModel csp, BigDecimal amount, String dsc )
	{
		StringBuilder builder = new StringBuilder("\n");
		String desc = "";
		if(dsc == null || dsc.isEmpty())
		{
		   desc = "Purchase personal clouds in Respect Network";
		} else
		{
		   desc = dsc;
		}

		builder.append("<script           class=\"stripe-button\"\n")
		       .append("                    src=\"" + csp.getPaymentUrlTemplate().replaceAll("\"", "&quot;") + "\"\n")
		       .append("               data-key=\"" + csp.getUsername().replaceAll("\"", "&quot;") + "\"\n")
		       .append("            data-amount=\"" + getAmount(amount) + "\"\n")
		       .append("              data-name=\"" + csp.getPaymentGatewayName().replaceAll("\"", "&quot;") + "\"\n")
		       .append("       data-description=\"" + desc.replaceAll("\"", "&quot;") + "\"\n")
		       .append("             data-image=\"img/csp_logo.png\">\n")
		       .append("</script>\n");

		return builder.toString();
	}

	public static String getToken( HttpServletRequest request )
	{
		String rtn   = null;

		String code  = request.getParameter("code");
		String token = request.getParameter("stripeToken");

		logger.info("code = " + code + " token = " + token);
		if( (code != null) && (code.isEmpty() == false) )
		{
			// this is an oAuth connect request
			logger.error("oAuth connect request - code - " + code);
			return rtn;
		}

		if( (token == null) || (token.isEmpty() == true) )
		{
			logger.error("token is null or empty = " + token);
			return rtn;
		}

		rtn = token;
		return rtn;
	}

	public static PaymentModel makePayment( CSPModel csp, BigDecimal amount, String currency, String dsc, String token )
	{
		PaymentModel rtn = null;
		String desc = "";
      if(dsc == null || dsc.isEmpty())
      {
         desc = "Purchase personal clouds in Respect Network";
      } else
      {
         desc = dsc;
      }

		Map<String, Object> chargeParams = new HashMap<String, Object>();
		chargeParams.put("amount"     , "" + getAmount(amount));
		chargeParams.put("currency"   , currency);
		chargeParams.put("card"       , token);
		chargeParams.put("description", desc);

		try
		{
			logger.info("try make payment - " + chargeParams);
			Charge rsp = Charge.create(chargeParams, csp.getPassword());
			logger.info(rsp.toString());
			logger.info("make payment is successful - id = " + rsp.getId() + " invoice = " + rsp.getInvoice());

			PaymentModel payment = new PaymentModel();
			payment.setPaymentId(UUID.randomUUID().toString());
			payment.setCspCloudName(csp.getCspCloudName());
			payment.setPaymentReferenceId(token);
			payment.setPaymentResponseCode(rsp.getId());
			payment.setAmount(amount);
			payment.setCurrency(currency);

			rtn = payment;

			logger.info("payment = " + rtn);
		}
		catch( CardException e )
		{
			// Since it's a decline, CardException will be caught
			logger.info("status is - " + e.getCode());
			logger.info("message is - " + e.getParam());
			logger.error("Payment failure", e);
		}
		catch( InvalidRequestException e )
		{
			// Invalid parameters were supplied to Stripe's API
			logger.error("Payment failure", e);
		}
		catch( AuthenticationException e )
		{
			// Authentication with Stripe's API failed (maybe the API key is wrong)
			logger.error("Payment failure", e);
		}
		catch( APIConnectionException e )
		{
			// Network communication with Stripe failed
			// FIXME - retry?
			logger.error("Payment failure", e);
		}
		catch( APIException e )
		{
			// FIXME - ???
			logger.error("Payment failure", e);
		}
		catch( StripeException e )
		{
			// Display a very generic error to the user, and maybe send a warning email
			logger.error("Payment failure", e);
		}
		catch( Exception e )
		{
			// Something else happened, completely unrelated to Stripe
			logger.error("Payment failure", e);
		}

		return rtn;
	}
}
