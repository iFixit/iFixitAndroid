package com.dozuki.ifixit;

import java.net.URLEncoder;

import org.apache.http.client.ResponseHandler;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.Tab;
import com.actionbarsherlock.app.SherlockFragment;

public class TopicViewFragment extends SherlockFragment {
   private static final int GUIDES_TAB = 0;
   private static final int ANSWERS_TAB = 1;
   private static final int MORE_INFO_TAB = 2;
   private static final int TAB_COUNT = 3;
   private static final String RESPONSE = "RESPONSE";
   private static final String TOPIC_API_URL =
    "http://www.ifixit.com/api/1.0/topic/";

   private TopicNode mTopicNode;
   private TopicLeaf mTopicLeaf;
   private ViewPager mPager;
   private TopicAdapter mTabsAdapter;
   private ImageManager mImageManager;
   private ActionBar mActionBar;

   private final Handler mTopicHandler = new Handler() {
      public void handleMessage(Message message) {
         String response = message.getData().getString(RESPONSE);

         setTopicLeaf(JSONHelper.parseTopicLeaf(response));
      }
   };

   public void setActionBar(ActionBar actionBar) {
      mActionBar = actionBar;
   }

   public TopicLeaf getTopicLeaf() {
      return mTopicLeaf;
   }

   @Override
   public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);

      if (mImageManager == null) {
         mImageManager = ((MainApplication)getActivity().getApplication()).
          getImageManager();
      }
      
   }

   @Override
   public View onCreateView(LayoutInflater inflater, ViewGroup container,
    Bundle savedInstanceState) {
      View view = inflater.inflate(R.layout.topic_view_fragment, container,
       false);

      mPager = (ViewPager)view.findViewById(R.id.pager);
      mTabsAdapter = new TopicAdapter(getActivity(), mPager, mImageManager);

      return view;
   }

   public void setTopicNode(TopicNode topicNode) {
      mTopicNode = topicNode;

      getTopicLeaf(mTopicNode.getName());
      
   }

   public void setTopicLeaf(TopicLeaf topicLeaf) {
      mTopicLeaf = topicLeaf;

      mTabsAdapter.setTopicLeaf(mTopicLeaf);
      mPager.setAdapter(mTabsAdapter);

      mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
      ActionBar.Tab tab = mActionBar.newTab();
      tab.setText(getActivity().getString(R.string.guides));
      tab.setTabListener(mTabsAdapter);
      mActionBar.addTab(tab);

      tab = mActionBar.newTab();
      tab.setText(getActivity().getString(R.string.answers));
      tab.setTabListener(mTabsAdapter);
      mActionBar.addTab(tab);

      tab = mActionBar.newTab();
      tab.setText(getActivity().getString(R.string.moreInfo));
      tab.setTabListener(mTabsAdapter);
      mActionBar.addTab(tab);

      if (mTopicLeaf.getGuides().size() == 0) {
         mActionBar.setSelectedNavigationItem(MORE_INFO_TAB);
      }
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

   public class TopicAdapter extends FragmentPagerAdapter
    implements ActionBar.TabListener, ViewPager.OnPageChangeListener {
      private TopicLeaf mTopicLeaf;
      private final ViewPager mViewPager;
      private final ImageManager mImageManager;

      public TopicAdapter(FragmentActivity activity,
       ViewPager pager, ImageManager imageManager) {
         super(activity.getSupportFragmentManager());
         mViewPager = pager;
         mViewPager.setAdapter(this);
         mViewPager.setOnPageChangeListener(this);
         mImageManager = imageManager;
      }

      @Override
      public int getCount() {
         if (mTopicLeaf == null) {
            return 0;
         } else {
            return TAB_COUNT;
         }
      }

      @Override
      public Fragment getItem(int position) {
         if (mTopicLeaf == null) {
            Log.w("iFixit", "Trying to get Fragment at bad position");
            return null;
         }

         if (position == GUIDES_TAB) {
            return new TopicGuideListFragment(mImageManager, mTopicLeaf);
         } else if (position == ANSWERS_TAB) {
            WebViewFragment webView = new WebViewFragment();

            webView.loadUrl(mTopicLeaf.getSolutionsUrl());

            return webView;
         } else if (position == MORE_INFO_TAB) {
            WebViewFragment webView = new WebViewFragment();

            try {
               webView.loadUrl("http://www.ifixit.com/c/" +
                URLEncoder.encode(mTopicLeaf.getName(), "UTF-8"));
            } catch (Exception e) {
               Log.w("iFixit", "Encoding error: " + e.getMessage());
            }

            return webView;
         } else {
            Log.w("iFixit", "Too many tabs!");
            return null;
         }
      }

      @Override
      public void onPageScrolled(int position, float positionOffset,
       int positionOffsetPixels) {
      }

      @Override
      public void onPageSelected(int position) {
         mActionBar.setSelectedNavigationItem(position);
      }

      @Override
      public void onPageScrollStateChanged(int state) {
      }

      public void setTopicLeaf(TopicLeaf topicLeaf) {
         mTopicLeaf = topicLeaf;
         notifyDataSetChanged();
      }

      @Override
      public void onTabSelected(Tab tab, FragmentTransaction ft) {
         mViewPager.setCurrentItem(tab.getPosition());
      }

      @Override
      public void onTabUnselected(Tab tab, FragmentTransaction ft) {
      }

      @Override
      public void onTabReselected(Tab tab, FragmentTransaction ft) {
      }
   }
}
