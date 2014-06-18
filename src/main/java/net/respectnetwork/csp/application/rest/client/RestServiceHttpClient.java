package net.respectnetwork.csp.application.rest.client;

import java.io.EOFException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import net.respectnetwork.csp.application.exception.CSPException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RestServiceHttpClient {
    /** CLass Logger */
    private static final Logger LOGGER = LoggerFactory
            .getLogger(RestServiceHttpClient.class);
    private final String url;
    private final String path;
    private final Object entity;

    public RestServiceHttpClient(String url, String path, Object entity) {
        this.url = url;
        this.path = path;
        this.entity = entity;
    }

    @SuppressWarnings("unchecked")
    public <T extends HttpResponse> T postRequest() {
        LOGGER.info("Executing post request method.");
        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(getURL());
        LOGGER.debug("Post url is: " + getURL());
        CloseableHttpResponse response = null;
        try {
            List<NameValuePair> nvps = new ArrayList<NameValuePair>();
            nvps.add(new BasicNameValuePair("keyRequest", new ObjectMapper()
                    .writeValueAsString(entity)));
            httpPost.setEntity(new UrlEncodedFormEntity(nvps));
            response = httpclient.execute(httpPost);
        } catch (UnsupportedEncodingException e) {
            LOGGER.error("Error while processing post request.", e);
        } catch (ClientProtocolException e) {
            LOGGER.error("Error while processing post request.", e);
        } catch (IOException e) {
            LOGGER.error("Error while processing post request.", e);
        }
        return (T) response;
    }

    @SuppressWarnings("unchecked")
    public <T extends HttpResponse> T getRequest() {
        LOGGER.info("Processing GET request: " + url);
        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpGet method = new HttpGet(getURL());
        method.setHeader("content-type", "application/json");
        CloseableHttpResponse response = null;
        // Send Get request
        try {
            response = httpclient.execute(method);
        } catch (ClientProtocolException e) {
            LOGGER.error("Error while processing get request.", e);
            e.printStackTrace();
        } catch (IOException e) {
            LOGGER.error("Error while processing get request.", e);
            e.printStackTrace();
        }
        return (T) response;
    }

    protected <S> S processResponse(HttpResponse resp, Class<S> responseClass)
            throws CSPException {
        try {
            HttpEntity entity = resp.getEntity();
            byte[] bytes = EntityUtils.toByteArray(entity);
            if (bytes.length == 0) {
                return null;
            }
            return new ObjectMapper().readValue(bytes, responseClass);
        } catch (EOFException e) {
            LOGGER.error("Cannot deserialize", e);
            return null;
        } catch (IOException e) {
            LOGGER.error("Cannot deserialize", e);
            throw new CSPException("", "Cannot deserialize", e);
        }
    }

    protected final <S> S checkForErrors(HttpResponse resp, int successCode,
            Class<S> responseClass) throws CSPException {
        S response;
        try {
            if (successCode == resp.getStatusLine().getStatusCode()) {
                response = processResponse(resp, responseClass);
            } else {
                throw new CSPException("Failed with status code "
                        + resp.getStatusLine().getStatusCode() + ", expected "
                        + successCode);
            }

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(String.format("deserialized %s - : %s",
                        responseClass, getJson(response)));
            }
            return response;
        } catch (CSPException ex) {
            throw ex;
        } catch (Exception e) {
            LOGGER.error("Failed to de-serialize response for license key", e);
            LOGGER.error("Failed Status: "
                    + resp.getStatusLine().getStatusCode());
            throw new CSPException(
                    "Failed to de-serialize response for license key", e);
        }
    }

    public final <T> T deserialize(HttpResponse resp, Class<T> clazz)
            throws CSPException {
        LOGGER.debug("Status: " + resp.getStatusLine().getStatusCode());
        // check for errors
        return checkForErrors(resp, 200, clazz);
    }

    protected <S> String getJson(S response) {
        try {
            return new ObjectMapper().writeValueAsString(response);
        } catch (IOException e) {
            // this shouldn't be fatal, as it's only use for logging
            LOGGER.error("Cannot generate JSON", e);
            return "";
        }
    }

    private String getURL() {
        return url + path;
    }

}
