package com.ifixit.android.ifixit;

import java.net.URLEncoder;

import org.apache.http.client.ResponseHandler;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class TopicViewFragment extends Fragment {
   private static final String RESPONSE = "RESPONSE";
   private static final String TOPIC_API_URL =
    "http://www.ifixit.com/api/0.1/device/";

   private TopicNode mTopicNode;
   private TopicLeaf mTopicLeaf;
   private TextView mTopicText;

   private final Handler mTopicHandler = new Handler() {
      public void handleMessage(Message message) {
         String response = message.getData().getString(RESPONSE);

         mTopicText.setText(response);

         setTopicLeaf(JSONHelper.parseTopicLeaf(response));
      }
   };

   @Override
   public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
   }

   @Override
   public View onCreateView(LayoutInflater inflater, ViewGroup container,
    Bundle savedInstanceState) {
      View view = inflater.inflate(R.layout.topic_view_fragment, container, false);

      mTopicText = (TextView)view.findViewById(R.id.topicName);

      return view;
   }

   public void setTopicNode(TopicNode topicNode) {
      mTopicNode = topicNode;

      mTopicText.setText(mTopicNode.getName());
      getTopicLeaf(mTopicNode.getName());
   }

   public void setTopicLeaf(TopicLeaf topicLeaf) {
      mTopicLeaf = topicLeaf;
   }

   private void getTopicLeaf(final String topicName) {
      final ResponseHandler<String> responseHandler =
       HTTPRequestHelper.getResponseHandlerInstance(mTopicHandler);

      new Thread() {
         public void run() {
            HTTPRequestHelper helper = new HTTPRequestHelper(responseHandler);

            try {
               helper.performGet(TOPIC_API_URL + URLEncoder.encode(topicName,
                "UTF-8"));
            } catch (Exception e) {
               Log.w("iFixit", "Encoding error: " + e.getMessage());
            }
         }
      }.start();
   }
}
