package com.dozuki.ifixit.util;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.ProtocolVersion;
import org.apache.http.StatusLine;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.cookie.ClientCookie;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.entity.FileEntity;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

/**
 * Wrapper to help make HTTP requests easier - after all, we want to make it
 * nice for the people.
 *
 *
 * @author charliecollins
 *
 */
public class HTTPRequestHelper {

   private static final String CLASSTAG =
    HTTPRequestHelper.class.getSimpleName();

   private static final int POST_TYPE = 1;
   private static final int GET_TYPE = 2;
   private static final String CONTENT_TYPE = "Content-Type";

   public static final String MIME_FORM_ENCODED =
    "application/x-www-form-urlencoded";
   public static final String MIME_TEXT_PLAIN = "text/plain";
   private static final DefaultHttpClient client;

   static {
      HttpParams params = new BasicHttpParams();
      SchemeRegistry schemeRegistry = new SchemeRegistry();
      ThreadSafeClientConnManager cm;

      params.setParameter(CoreProtocolPNames.PROTOCOL_VERSION,
       HttpVersion.HTTP_1_1);
      params.setParameter(CoreProtocolPNames.HTTP_CONTENT_CHARSET, HTTP.UTF_8);
      params.setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 15000);
      params.setParameter(CoreConnectionPNames.STALE_CONNECTION_CHECK, false);

      schemeRegistry.register(new Scheme("http",
       PlainSocketFactory.getSocketFactory(), 80));
      schemeRegistry.register(new Scheme("https",
       SSLSocketFactory.getSocketFactory(), 443));

      cm = new ThreadSafeClientConnManager(params, schemeRegistry);
      client = new DefaultHttpClient(cm, params);
   }

   private final ResponseHandler<String> responseHandler;

   /**
    * Constructor that accepts ResponseHandler parameter,
    * you can define your own ResponseHandler and do whatever you need with it.
    *
    * Note: you can also use the default String based response handler
    * with the static <code>HTTPRequestHelper.getResponseHandlerInstance()
    * </code> method.
    *
    * @param responseHandler
    */
   public HTTPRequestHelper(final ResponseHandler<String> responseHandler) {
      this.responseHandler = responseHandler;
   }

   // ctor that automatically uses String based ResponseHandler
   public HTTPRequestHelper(final Handler handler) {
      this(HTTPRequestHelper.getResponseHandlerInstance(handler));
   }

   /**
    * Perform a simple HTTP GET operation.
    *
    */
   public void performGet(final String url) {
      performRequest(null, url, null, null, null, null, 
    		  HTTPRequestHelper.GET_TYPE);
   }

   /**
    * Perform an HTTP GET operation with user/pass and headers.
    *
    */
   public void performGet(final String url, final String user,
    final String pass, final Map<String, String> additionalHeaders) {
      performRequest(null, url, user, pass, additionalHeaders, 
       null, HTTPRequestHelper.GET_TYPE);
   }

   /**
    * Perform an HTTP POST operation with specified content type.
    *
    */
   public void performPost(final String contentType, final String url,
    final String user, final String pass, final Map<String,
    String> additionalHeaders, final Map<String, String> params, final File file) {
      performRequest(contentType, url, user, pass, additionalHeaders, params, 
       HTTPRequestHelper.POST_TYPE);
   }
   

   
   
	/**
	 * Perform an HTTP POST operation with specified content type.
	 * 
	 */
	public void performPostWithSessionCookie(final String url,
			final String user, final String pass, final String session,
			final String domain, final Map<String, String> additionalHeaders,
			final Map<String, String> params, File file) {

		// clearing all old cookies
		client.getCookieStore().clear();
		// create and addd sessionCookie
		final BasicClientCookie cookie = new BasicClientCookie("session",
				session);

		cookie.setExpiryDate(new Date(System.currentTimeMillis() + 120000));
		cookie.setDomain(domain);
		cookie.setAttribute(ClientCookie.VERSION_ATTR, "0");
		cookie.setAttribute(ClientCookie.DOMAIN_ATTR, domain);
		cookie.setPath("/");

		client.getCookieStore().addCookie(cookie);
		
		//URLConnection used for file uploads
		if (file == null)
			performRequest(HTTPRequestHelper.MIME_FORM_ENCODED, url, user,
					pass, additionalHeaders, params,
					HTTPRequestHelper.POST_TYPE);
		else
			fileUpload(url, file, "session=" + session + ";"
					+ ClientCookie.VERSION_ATTR);
	}

   /**
    * Perform an HTTP POST operation with a default conent-type of
    * "application/x-www-form-urlencoded."
    *
    */
   public void performPost(final String url, final String user,
    final String pass, final Map<String, String> additionalHeaders,
    final Map<String, String> params) {
      performRequest(HTTPRequestHelper.MIME_FORM_ENCODED, url, user, pass,
       additionalHeaders, params,  HTTPRequestHelper.POST_TYPE);
   }
   
  

   /**
    * Private heavy lifting method that performs GET or POST with supplied
    * url, user, pass, data, and headers.
    *
    * @param contentType
    * @param url
    * @param user
    * @param pass
    * @param headers
    * @param params
    * @param requestType
    */
   private void performRequest(final String contentType, final String url,
    final String user, final String pass, final Map<String, String> headers,
    final Map<String, String> params, final int requestType) {
      final Map<String, String> sendHeaders = new HashMap<String, String>();

      Log.d(CLASSTAG, " " + HTTPRequestHelper.CLASSTAG +
       " making HTTP request to url - " + url);

      // add user and pass to client credentials if present
      if ((user != null) && (pass != null)) {
         Log.d(CLASSTAG, " " + HTTPRequestHelper.CLASSTAG +
          " user and pass present, adding credentials to request");
         client.getCredentialsProvider().setCredentials(AuthScope.ANY,
          new UsernamePasswordCredentials(user, pass));
      }

      // process headers using request interceptor
      if ((headers != null) && (headers.size() > 0)) {
         sendHeaders.putAll(headers);
      }
      if (requestType == HTTPRequestHelper.POST_TYPE) {
         sendHeaders.put(HTTPRequestHelper.CONTENT_TYPE, contentType);
      }
      if (sendHeaders.size() > 0) {
         client.addRequestInterceptor(new HttpRequestInterceptor() {
            public void process(final HttpRequest request,
             final HttpContext context) throws HttpException, IOException {
               for (String key : sendHeaders.keySet()) {
                  if (!request.containsHeader(key)) {
                     Log.d(CLASSTAG, " " + HTTPRequestHelper.CLASSTAG +
                      " adding header: " + key + " | " + sendHeaders.get(key));
                     request.addHeader(key, sendHeaders.get(key));
                  }
               }
               sendHeaders.clear();
            }
         });
      }

      // handle POST or GET request respectively
      if (requestType == HTTPRequestHelper.POST_TYPE) {
         HttpPost method = new HttpPost(url);

         Log.d(CLASSTAG, " " + HTTPRequestHelper.CLASSTAG +
          " performRequest POST");

         // data - name/value params
         List<NameValuePair> nvps = null;
         if ((params != null) && (params.size() > 0)) {
            nvps = new ArrayList<NameValuePair>();
            for (String key : params.keySet()) {
               Log.d(CLASSTAG, " " + HTTPRequestHelper.CLASSTAG +
                " adding param: " + key + " | " + params.get(key));
               nvps.add(new BasicNameValuePair(key, params.get(key)));
            }
         }
         if (nvps != null) {
            try {
               method.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));
            } catch (UnsupportedEncodingException e) {
               Log.e(CLASSTAG, " " + HTTPRequestHelper.CLASSTAG, e);
            }
         }
         
         execute(client, method);
      } else if (requestType == HTTPRequestHelper.GET_TYPE) {
         Log.d(CLASSTAG, " " + HTTPRequestHelper.CLASSTAG +
          " performRequest GET");
         execute(client, new HttpGet(url));
      }
   }

	protected void fileUpload(String sURL, File file, String cookie) {

		HttpURLConnection connection = null;
		DataOutputStream outputStream = null;
		
		String boundary = "*****";

		int bytesRead, bytesAvailable, bufferSize;
		byte[] buffer;
		int maxBufferSize = 1 * 1024 * 1024;
		try {
			FileInputStream fileInputStream = new FileInputStream(file);

			URL url = new URL(sURL);
			connection = (HttpURLConnection) url.openConnection();		
			connection.setDoInput(true);
			connection.setDoOutput(true);
			connection.setUseCaches(false);

			int length;
			length = ((int) (file.length()));

			//create cookie for URL connection
			connection.setRequestMethod("POST");
			connection.setRequestProperty("Cookie", cookie);
			connection.setRequestProperty("Connection", "Keep-Alive");
			connection.setRequestProperty("Content-Length", length + "");
			connection.setRequestProperty("Content-Type",
					"binary/octet-stream;boundary=" + boundary);

			outputStream = new DataOutputStream(connection.getOutputStream());

			bytesAvailable = fileInputStream.available();
			bufferSize = Math.min(bytesAvailable, maxBufferSize);
			buffer = new byte[bufferSize];

			// Read file
			bytesRead = fileInputStream.read(buffer, 0, bufferSize);

			while (bytesRead > 0) {
				outputStream.write(buffer, 0, bufferSize);
				bytesAvailable = fileInputStream.available();
				bufferSize = Math.min(bytesAvailable, maxBufferSize);
				bytesRead = fileInputStream.read(buffer, 0, bufferSize);
			}

			int serverResponseCode = connection.getResponseCode();

			String serverResponseMessage = connection.getResponseMessage();
			StringBuilder sb = new StringBuilder();

			BufferedReader br = new BufferedReader(new InputStreamReader(
					connection.getInputStream()));
			sb = new StringBuilder();
			String line;
			while ((line = br.readLine()) != null) {
				sb.append(line + "\n");
			}
			br.close();

			BasicHttpResponse responseObj = new BasicHttpResponse(
					new ProtocolVersion("HTTP", 1, 1), serverResponseCode,
					serverResponseMessage);

			String response = sb.toString();
			StringEntity entity = new StringEntity(response);
			responseObj.setEntity(entity);
			responseHandler.handleResponse(responseObj);
			fileInputStream.close();
			outputStream.flush();
			outputStream.close();
		} catch (Exception e) {
			 Log.e(CLASSTAG, " " + HTTPRequestHelper.CLASSTAG, e);
			return;
		}
		return;
	}

   /**
    * Once the client and method are established, execute the request.
    *
    * @param client
    * @param method
    */
   private void execute(HttpClient client, HttpRequestBase method) {
      // create a response specifically for errors (in case)
      BasicHttpResponse errorResponse = new BasicHttpResponse(
       new ProtocolVersion("HTTP_ERROR", 1, 1), 500, "ERROR");

      Log.d(CLASSTAG, " " + HTTPRequestHelper.CLASSTAG + " execute invoked");

      try {
         client.execute(method, this.responseHandler);
         Log.d(CLASSTAG, " " + HTTPRequestHelper.CLASSTAG +
          " request completed");
      } catch (Exception e) {
         Log.e(CLASSTAG, " " + HTTPRequestHelper.CLASSTAG, e);
         errorResponse.setReasonPhrase(e.getMessage());
         try {
            this.responseHandler.handleResponse(errorResponse);
         } catch (Exception ex) {
            Log.e(CLASSTAG, " " + HTTPRequestHelper.CLASSTAG, ex);
         }
      }
   }
   


   /**
    * Static utility method to create a default ResponseHandler that sends a
    * Message to the passed * in Handler with the response as a String, after
    * the request completes.
    *
    * @param handler
    * @return
    */
   public static ResponseHandler<String> getResponseHandlerInstance(
    final Handler handler) {
      final ResponseHandler<String> responseHandler =
       new ResponseHandler<String>() {
         public String handleResponse(final HttpResponse response) {
            HttpEntity entity = response.getEntity();
            String result = null;
            Message message = handler.obtainMessage();
            Bundle bundle = new Bundle();
            StatusLine status = response.getStatusLine();

            Log.d(CLASSTAG, " " + HTTPRequestHelper.CLASSTAG +
             " statusCode - " + status.getStatusCode());
            Log.d(CLASSTAG, " " + HTTPRequestHelper.CLASSTAG +
             " statusReasonPhrase - " + status.getReasonPhrase());

            if (entity != null) {
               try {
                  result = HTTPRequestHelper.inputStreamToString(
                   entity.getContent());
                  bundle.putString("RESPONSE", result);
                  message.setData(bundle);
                  handler.sendMessage(message);
               } catch (IOException e) {
                  Log.e(CLASSTAG, " " + HTTPRequestHelper.CLASSTAG, e);
                  bundle.putString("RESPONSE", "Error - " + e.getMessage());
                  message.setData(bundle);
                  handler.sendMessage(message);
               }
            } else {
               Log.w(CLASSTAG, " " + HTTPRequestHelper.CLASSTAG +
                " empty response entity, HTTP error occurred");
               bundle.putString("RESPONSE", "Error - " +
                response.getStatusLine().getReasonPhrase());
               message.setData(bundle);
               handler.sendMessage(message);
            }
            return result;
         }

      };
      return responseHandler;
   }

   private static String inputStreamToString(final InputStream stream)
    throws IOException {
      BufferedReader br = new BufferedReader(new InputStreamReader(stream));
      StringBuilder sb = new StringBuilder();
      String line = null;

      while ((line = br.readLine()) != null) {
         sb.append(line + "\n");
      }
      br.close();

      return sb.toString();
   }
}
