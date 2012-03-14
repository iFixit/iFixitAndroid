package com.dozuki.ifixit;

import java.net.URLEncoder;
import java.util.ArrayList;

import org.apache.http.client.ResponseHandler;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TabHost;
import android.widget.TabWidget;

import com.actionbarsherlock.app.SherlockFragment;
import com.viewpagerindicator.TitleProvider;


public class TopicViewFragment extends SherlockFragment {
   private static final int GUIDES_TAB = 0;
   private static final int ANSWERS_TAB = 1;
   private static final int MORE_INFO_TAB = 2;
   private static final String RESPONSE = "RESPONSE";
   private static final String TOPIC_API_URL =
    "http://www.ifixit.com/api/0.1/device/";
   private TabHost mTabHost;

   private TopicNode mTopicNode;
   private TopicLeaf mTopicLeaf;
   private ViewPager mPager;
   private TabsAdapter mTabsAdapter;
   private ImageManager mImageManager;

   private final Handler mTopicHandler = new Handler() {
      public void handleMessage(Message message) {
         String response = message.getData().getString(RESPONSE);

         setTopicLeaf(JSONHelper.parseTopicLeaf(response));
      }
   };

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

      mTabHost = (TabHost)view.findViewById(android.R.id.tabhost);
      mTabHost.setup();

      mPager = (ViewPager)view.findViewById(R.id.pager);
      mTabsAdapter = new TabsAdapter(getActivity(), mTabHost, mPager,
       mImageManager);

      mTabsAdapter.addTab(mTabHost.newTabSpec("guides").setIndicator(
       getActivity().getString(R.string.guides)), TopicGuideItemView.class,
       null);
      mTabsAdapter.addTab(mTabHost.newTabSpec("answers").setIndicator(
       getActivity().getString(R.string.answers)), WebViewFragment.class,
       null);
      mTabsAdapter.addTab(mTabHost.newTabSpec("moreInfo").setIndicator(
       getActivity().getString(R.string.moreInfo)), WebViewFragment.class,
       null);

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

      if (mTopicLeaf.getGuides().size() == 0) {
         mTabHost.setCurrentTab(MORE_INFO_TAB);
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

   public static class TabsAdapter extends FragmentStatePagerAdapter
    implements TitleProvider, TabHost.OnTabChangeListener,
    ViewPager.OnPageChangeListener {
      private TopicLeaf mTopicLeaf;
      private final Context mContext;
      private final TabHost mTabHost;
      private final ViewPager mViewPager;
      private final ImageManager mImageManager;
      private final ArrayList<TabInfo> mTabs = new ArrayList<TabInfo>();

      static final class TabInfo {
         private final String tag;
         private final Class<?> clss;
         private final Bundle args;

         TabInfo(String _tag, Class<?> _class, Bundle _args) {
            tag = _tag;
            clss = _class;
            args = _args;
         }
      }

      static class DummyTabFactory implements TabHost.TabContentFactory {
         private final Context mContext;

         public DummyTabFactory(Context context) {
            mContext = context;
         }

         @Override
         public View createTabContent(String tag) {
            View v = new View(mContext);
            v.setMinimumWidth(0);
            v.setMinimumHeight(0);
            return v;
         }
      }

      public TabsAdapter(FragmentActivity activity, TabHost tabHost,
       ViewPager pager, ImageManager imageManager) {
         super(activity.getSupportFragmentManager());
         mContext = activity;
         mTabHost = tabHost;
         mViewPager = pager;
         mTabHost.setOnTabChangedListener(this);
         mViewPager.setAdapter(this);
         mViewPager.setOnPageChangeListener(this);
         mImageManager = imageManager;
      }

      public void addTab(TabHost.TabSpec tabSpec, Class<?> clss, Bundle args) {
         tabSpec.setContent(new DummyTabFactory(mContext));
         String tag = tabSpec.getTag();

         TabInfo info = new TabInfo(tag, clss, args);
         mTabs.add(info);
         mTabHost.addTab(tabSpec);
         notifyDataSetChanged();
      }

      @Override
      public int getCount() {
         if (mTopicLeaf == null) {
            return 0;
         } else {
            return mTabs.size();
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
      public void onTabChanged(String tabId) {
         int position = mTabHost.getCurrentTab();
         mViewPager.setCurrentItem(position);
      }

      @Override
      public void onPageScrolled(int position, float positionOffset,
       int positionOffsetPixels) {
      }

      @Override
      public void onPageSelected(int position) {
         // Unfortunately when TabHost changes the current tab, it kindly
         // also takes care of putting focus on it when not in touch mode.
         // The jerk.
         // This hack tries to prevent this from pulling focus out of our
         // ViewPager.
         TabWidget widget = mTabHost.getTabWidget();
         int oldFocusability = widget.getDescendantFocusability();
         widget.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
         mTabHost.setCurrentTab(position);
         widget.setDescendantFocusability(oldFocusability);
      }

      @Override
      public void onPageScrollStateChanged(int state) {
      }

      public void setTopicLeaf(TopicLeaf topicLeaf) {
         mTopicLeaf = topicLeaf;
         notifyDataSetChanged();
      }

      @Override
      public String getTitle(int position) {
         if (position == GUIDES_TAB) {
            return mContext.getString(R.string.guides);
         } else if (position == ANSWERS_TAB) {
            return mContext.getString(R.string.answers);
         } else if (position == MORE_INFO_TAB) {
            return mContext.getString(R.string.moreInfo);
         } else {
            Log.w("iFixit", "Too many tabs!");
            return null;
         }
      }
   }
}
