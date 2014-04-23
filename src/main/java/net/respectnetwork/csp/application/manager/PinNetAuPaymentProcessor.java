package net.respectnetwork.csp.application.manager;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import net.respectnetwork.csp.application.model.CSPModel;
import net.respectnetwork.csp.application.model.PaymentModel;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

/**
 * Payment processor for Pin.net.au.
 * API uses these fields in csp table:
 * username - publishable key
 * password - secret key
 * env - {test, live}
 */
public class PinNetAuPaymentProcessor
{
   public static final String DB_PAYMENT_GATEWAY_NAME = "PIN_NET_AU";

	private static final Logger logger = LoggerFactory.getLogger(PinNetAuPaymentProcessor.class);

   private static final HashMap<String, URI> ENVIRONMENT_URIS;

   static {
      ENVIRONMENT_URIS = new HashMap<String, URI>();
      try
      {
         ENVIRONMENT_URIS.put("live", new URI("https://api.pin.net.au/1/"));
         ENVIRONMENT_URIS.put("test", new URI("https://test-api.pin.net.au/1/"));
      } catch (URISyntaxException e)
      {
         logger.error("Error creating ENVIRONMENT_URIS");
      }
   }


	private static long getAmountInCents(BigDecimal amount)
	{
		long rtn = amount.multiply(BigDecimal.valueOf(100.0)).setScale(0, BigDecimal.ROUND_HALF_UP).longValue();
		logger.info("getAmountInCents(" + amount + ") = " + rtn);
		return rtn;
	}

   public static String getPublishableApiKey(CSPModel cspModel) {
      return cspModel.getUsername();
   }

   private static String getSecretApiKey(CSPModel csp)
   {
      return csp.getPassword();
   }

   public static String getEnvironment(CSPModel cspModel) {
      return cspModel.getEnv();
   }


   /**
    * Complete a payment given a card_token created from JavaScript client library.
    */
	public static PaymentModel makePayment(CSPModel csp, BigDecimal amount, String description, String email, String clientIpAddress, String cardToken)
	{
      if(description == null || description.isEmpty())
      {
         description = "Purchase personal clouds in Respect Network";
      }

      String currency = csp.getCurrency();

      URI apiBaseUri = ENVIRONMENT_URIS.get(csp.getEnv());
      if (apiBaseUri == null)
      {
         throw new IllegalArgumentException("Could not find environment " + csp.getEnv());
      }
      URI chargeEndpoint = apiBaseUri.resolve("charges");

      // API key is sent as username using HTTP basic auth (blank password)
      HttpHost target = new HttpHost(chargeEndpoint.getHost(), chargeEndpoint.getPort(), chargeEndpoint.getScheme());
      String apiKey = getSecretApiKey(csp);
      CredentialsProvider credsProvider = new BasicCredentialsProvider();
      credsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(apiKey, ""));

      // We need to preemptivly send the authentication header (HttpClient normally waits for a 401)
      AuthCache authCache = new BasicAuthCache();
      BasicScheme basicAuth = new BasicScheme();
      authCache.put(target, basicAuth);

      HttpClientContext localContext = HttpClientContext.create();
      localContext.setAuthCache(authCache);

      CloseableHttpClient httpClient = HttpClients.custom()
              .setDefaultCredentialsProvider(credsProvider)
              .build();

      HttpPost httpPost = new HttpPost(chargeEndpoint);

      // Add params
      List<NameValuePair> nvps = new ArrayList<NameValuePair>();
      nvps.add(new BasicNameValuePair("email", email));
      nvps.add(new BasicNameValuePair("description", description));
      nvps.add(new BasicNameValuePair("amount", Long.toString(getAmountInCents(amount))));
      nvps.add(new BasicNameValuePair("ip_address", clientIpAddress));
      nvps.add(new BasicNameValuePair("currency", currency));
      nvps.add(new BasicNameValuePair("capture", "true"));
      nvps.add(new BasicNameValuePair("card_token", cardToken));
      try
      {
         httpPost.setEntity(new UrlEncodedFormEntity(nvps));
      } catch (UnsupportedEncodingException e)
      {
         logger.error(e.getMessage());
         return null;
      }

      CloseableHttpResponse response = null;
      try
      {
         response = httpClient.execute(httpPost, localContext);
         logger.debug("Response status: " + response.getStatusLine());
         HttpEntity entity = response.getEntity();

         int statusCode = response.getStatusLine().getStatusCode();
         if (statusCode >= 200 && statusCode < 300)
         {
            String responseText = EntityUtils.toString(entity);
            EntityUtils.consume(entity);
            logger.debug("Response text: " + responseText);
            Gson gson = new Gson();


            JsonElement responseObject = extractMemberElementFromJsonString("response", responseText);
            ChargeResponse chargeResponse = gson.fromJson(responseObject, ChargeResponse.class);

            if (chargeResponse != null && chargeResponse.success) {
               logger.debug("charge response success");
               PaymentModel paymentModel = new PaymentModel();
               paymentModel.setPaymentId(UUID.randomUUID().toString());
               paymentModel.setAmount(amount);
               paymentModel.setCurrency(currency);
               paymentModel.setCspCloudName(csp.getCspCloudName());
               paymentModel.setPaymentReferenceId(chargeResponse.token);
               paymentModel.setPaymentResponseCode(chargeResponse.status_message);
               return paymentModel;
            } else
            {
               if (chargeResponse == null) {
                  logger.error("Null charge response");
               } else
               {
                  logger.error("Error in charge response: " + chargeResponse.error_message);
               }
               return null;
            }
         } else
         {
            logger.error("Non-success HTTP status code");
            return null;
         }
      } catch (IOException e)
      {
            logger.error(e.getMessage());
            return null;
      } finally {

         if (response != null)
         {
            try
            {
               response.close();
            } catch (IOException e)
            {
               // Failure during close - nothing we can do
            }
         }
      }
	}



   private static JsonElement extractMemberElementFromJsonString(String memberName, String json) {
      JsonParser parser = new JsonParser();
      JsonElement jsonElement = parser.parse(json);
      return jsonElement.getAsJsonObject().get(memberName);
   }

   /**
    * Class to model some relevant fields from the JSON response.
    */
   private static class ChargeResponse {
      String token;
      boolean success;
      String error_message;
      String status_message;
   }
}
