package com.ifixit.android.ifixit;

import java.net.URLEncoder;

import org.apache.http.client.ResponseHandler;

import com.viewpagerindicator.TabPageIndicator;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.viewpagerindicator.TitleProvider;

public class TopicViewFragment extends Fragment {
   private static final String RESPONSE = "RESPONSE";
   private static final String TOPIC_API_URL =
    "http://www.ifixit.com/api/0.1/device/";

   private TopicNode mTopicNode;
   private TopicLeaf mTopicLeaf;
   private ViewPager mPager;
   private TabPageIndicator mTabIndicator;
   private TopicViewAdapter mAdapter;

   private final Handler mTopicHandler = new Handler() {
      public void handleMessage(Message message) {
         String response = message.getData().getString(RESPONSE);

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

      mTabIndicator = (TabPageIndicator)view.findViewById(R.id.indicator);
      mPager = (ViewPager)view.findViewById(R.id.pager);
      mAdapter = new TopicViewAdapter(getActivity().
       getSupportFragmentManager());
      mPager.setAdapter(mAdapter);
      mTabIndicator.setViewPager(mPager);

      return view;
   }

   public void setTopicNode(TopicNode topicNode) {
      mTopicNode = topicNode;

      getTopicLeaf(mTopicNode.getName());
   }

   public void setTopicLeaf(TopicLeaf topicLeaf) {
      mTopicLeaf = topicLeaf;

      mAdapter.setTopicLeaf(mTopicLeaf);
      mPager.setAdapter(mAdapter);
      mTabIndicator.notifyDataSetChanged();
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

   private class TopicViewAdapter extends FragmentPagerAdapter
    implements TitleProvider {
      private TopicLeaf mTopicLeaf;

      public TopicViewAdapter(FragmentManager fm) {
         super(fm);
      }

      public void setTopicLeaf(TopicLeaf topicLeaf) {
         mTopicLeaf = topicLeaf;
      }

      @Override
      public Fragment getItem(int position) {
         if (mTopicLeaf == null) {
            Log.w("iFixit", "RuhRoh!");
            return null;
         }

         WebViewFragment webView = new WebViewFragment();

         webView.loadUrl("http://www.ifixit.com/Wiki/" + mTopicLeaf.getName() +
          position);

         return webView;
      }

      @Override
      public int getCount() {
         if (mTopicLeaf == null) {
            return 0;
         } else {
            return 9;
         }
      }

      @Override
      public String getTitle(int position) {
         return "Pos: " + position;
      }
   }
}
