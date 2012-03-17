package com.dozuki.ifixit;

import java.net.URLEncoder;

import org.apache.http.client.ResponseHandler;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class APIHelper {
   private static final String RESPONSE = "RESPONSE";
   private static final String TOPIC_API_URL =
    "http://www.ifixit.com/api/1.0/topic/";

   public interface APIResponder<T> {
      public void setResult(T result);
   }

   private interface StringHandler {
      public void handleString(String string);
   }

   public static void getTopic(String topic,
    final APIResponder<TopicLeaf> responder) {
      String url;

      try {
         url = TOPIC_API_URL + URLEncoder.encode(topic, "UTF-8");
         performRequest(url, new StringHandler() {
            public void handleString(String response) {
               responder.setResult(JSONHelper.parseTopicLeaf(response));
            }
         });
      } catch (Exception e) {
         Log.w("iFixit", "Encoding error: " + e.getMessage());
         responder.setResult(null);
      }
   }

   private static void performRequest(final String url,
    final StringHandler stringHandler) {
      final Handler handler = new Handler() {
         public void handleMessage(Message message) {
            String response = message.getData().getString(RESPONSE);
            stringHandler.handleString(response);
         }
      };

      final ResponseHandler<String> responseHandler =
       HTTPRequestHelper.getResponseHandlerInstance(handler);

      new Thread() {
         public void run() {
            HTTPRequestHelper helper = new HTTPRequestHelper(responseHandler);

            try {
               helper.performGet(url);
            } catch (Exception e) {
               Log.w("iFixit", "Encoding error: " + e.getMessage());
            }
         }
      }.start();
   }
}
