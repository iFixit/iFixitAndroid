package com.dozuki.ifixit.ui.topic_view;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.actionbarsherlock.app.SherlockFragment;
import com.dozuki.ifixit.MainApplication;
import com.dozuki.ifixit.R;
import com.dozuki.ifixit.model.dozuki.Site;
import com.dozuki.ifixit.model.topic.TopicLeaf;
import com.dozuki.ifixit.model.topic.TopicNode;
import com.dozuki.ifixit.ui.BaseActivity;
import com.dozuki.ifixit.ui.guide.view.GuideViewActivity;
import com.dozuki.ifixit.ui.guide.view.NoGuidesFragment;
import com.dozuki.ifixit.ui.guide.view.WebViewFragment;
import com.dozuki.ifixit.util.APIEvent;
import com.dozuki.ifixit.util.APIService;
import com.google.analytics.tracking.android.EasyTracker;
import com.squareup.otto.Subscribe;
import com.viewpagerindicator.TitlePageIndicator;

import java.net.URLEncoder;

public class TopicViewFragment extends SherlockFragment {
   private static final int GUIDES_TAB = 0;
   private static final int MORE_INFO_TAB = 1;
   private static final int ANSWERS_TAB = 2;
   private static final String CURRENT_PAGE = "CURRENT_PAGE";
   private static final String CURRENT_TOPIC_LEAF = "CURRENT_TOPIC_LEAF";
   private static final String CURRENT_TOPIC_NODE = "CURRENT_TOPIC_NODE";

   private TopicNode mTopicNode;
   private TopicLeaf mTopicLeaf;
   private Site mSite;
   private PageAdapter mPageAdapter;
   private ViewPager mPager;
   private TitlePageIndicator mTitleIndicator;

   private int mSelectedTab = -1;

   @Subscribe
   public void onTopic(APIEvent.Topic event) {
      if (!event.hasError()) {
         setTopicLeaf(event.getResult());
      } else {
         APIService.getErrorDialog(getActivity(), event).show();
      }
   }

   public boolean isDisplayingTopic() {
      return mTopicLeaf != null;
   }

   @Override
   public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);

      EasyTracker.getInstance().setContext(getActivity());

      if (mSite == null) {
         mSite = ((MainApplication) getActivity().getApplication()).getSite();
      }
   }

   @Override
   public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
      View view = inflater.inflate(R.layout.topic_view_fragment, container, false);

      Bundle args = getArguments();

      mPager = (ViewPager) view.findViewById(R.id.topic_view_view_pager);
      mTitleIndicator = (TitlePageIndicator) view.findViewById(R.id.topic_view_indicator);

      if (savedInstanceState != null) {
         mSelectedTab = savedInstanceState.getInt(CURRENT_PAGE, 0); // Default to Guide page
         mTopicNode = (TopicNode) savedInstanceState.getSerializable(CURRENT_TOPIC_NODE);
         TopicLeaf topicLeaf = (TopicLeaf) savedInstanceState.getSerializable(CURRENT_TOPIC_LEAF);

         if (topicLeaf != null) {
            setTopicLeaf(topicLeaf);
         } else if (mTopicNode != null) {
            getTopicLeaf(mTopicNode.getName());
         }
      } else if (args != null) {
         if (args.containsKey(GuideViewActivity.TOPIC_NAME_KEY)) {
            getTopicLeaf(args.getString(GuideViewActivity.TOPIC_NAME_KEY));
         }
      }
      return view;
   }

   @Override
   public void onResume() {
      super.onResume();

      MainApplication.getBus().register(this);
   }

   @Override
   public void onPause() {
      super.onPause();

      MainApplication.getBus().unregister(this);
   }

   @Override
   public void onSaveInstanceState(Bundle outState) {
      super.onSaveInstanceState(outState);

      outState.putInt(CURRENT_PAGE, mSelectedTab);
      outState.putSerializable(CURRENT_TOPIC_LEAF, mTopicLeaf);
      outState.putSerializable(CURRENT_TOPIC_NODE, mTopicNode);
   }

   public void setTopicNode(TopicNode topicNode) {
      if (topicNode == null) {
         mTopicNode = null;
         mTopicLeaf = null;
         return;
      }

      if (mTopicNode == null || !mTopicNode.equals(topicNode)) {
         getTopicLeaf(topicNode.getName());
      } else {
         selectDefaultTab();
      }

      mTopicNode = topicNode;
   }

   public void setTopicLeaf(TopicLeaf topicLeaf) {
      if (getActivity() == null) {
         return;
      }
      ((BaseActivity)getActivity()).hideLoading();

      if (mTopicLeaf != null && topicLeaf != null) {
         if (mTopicLeaf.equals(topicLeaf)) {
            selectDefaultTab();
            return;
         } else if (!topicLeaf.getName().equals(mTopicNode.getName())) {
            // Not the most recently selected topic... wait for another.
            return;
         }
      }

      mTopicLeaf = topicLeaf;

      if (mTopicLeaf == null) {
         // display error message
         return;
      }

      mTitleIndicator.setVisibility(View.VISIBLE);
      mPageAdapter = new PageAdapter(getChildFragmentManager());
      mPager.setAdapter(mPageAdapter);
      mTitleIndicator.setViewPager(mPager);
      mPager.setOffscreenPageLimit(2);
      selectDefaultTab();
   }

   private void selectDefaultTab() {

      if (mTopicLeaf == null) {
         return;
      }

      boolean noGuides = (mTopicLeaf.getGuides().size() == 0);
      int defaultTab = noGuides ? MORE_INFO_TAB : GUIDES_TAB;

      mPager.setCurrentItem(defaultTab, false);
      mTitleIndicator.setCurrentItem(defaultTab);
      mPager.invalidate();
      mTitleIndicator.invalidate();
   }

   private void getTopicLeaf(String topicName) {
      mTopicLeaf = null;
      mSelectedTab = -1;

      if (mTitleIndicator != null) {
         mTitleIndicator.setVisibility(View.VISIBLE);
      }

      APIService.call(getActivity(), APIService.getTopicAPICall(topicName));
   }

   public TopicLeaf getTopicLeaf() {
      return mTopicLeaf;
   }

   public TopicNode getTopicNode() {
      return mTopicNode;
   }

   public class PageAdapter extends FragmentStatePagerAdapter {

      public PageAdapter(FragmentManager fm) {
         super(fm);
      }

      @Override
      public int getCount() {
         if (mSite.mAnswers) {
            return 3;
         } else {
            return 2;
         }
      }

      @Override
      public CharSequence getPageTitle(int position) {
         switch (position) {
            case GUIDES_TAB:
               return getActivity().getString(R.string.guides);

            case MORE_INFO_TAB:
               return getActivity().getString(R.string.info);

            case ANSWERS_TAB:
               if (mSite.mAnswers) {
                  return getActivity().getString(R.string.answers);
               } else {
                  return getActivity().getString(R.string.info);
               }
         }
         return "";
      }

      @Override
      public Fragment getItem(int position) {

         Fragment selectedFragment;
         switch (position) {
            case GUIDES_TAB:
               if (mTopicLeaf.getGuides().size() == 0) {
                  selectedFragment = new NoGuidesFragment();
               } else {
                  selectedFragment = new TopicGuideListFragment(mTopicLeaf);
               }
               mSelectedTab = GUIDES_TAB;
               return selectedFragment;
            case MORE_INFO_TAB:
               selectedFragment = new TopicInfoFragment(mTopicLeaf);
               mSelectedTab = MORE_INFO_TAB;
               return selectedFragment;
            case ANSWERS_TAB:
               WebViewFragment webView = new WebViewFragment();

               EasyTracker.getTracker().sendView(mTopicLeaf.getName() + " Answers");

               if (mSite.mAnswers) {
                  webView.loadUrl(mTopicLeaf.getSolutionsUrl());

                  selectedFragment = webView;
                  mSelectedTab = ANSWERS_TAB;
               } else {

                  try {
                     webView.loadUrl("http://" + mSite.mDomain + "/c/"
                      + URLEncoder.encode(mTopicLeaf.getName(), "UTF-8"));
                  } catch (Exception e) {
                     Log.w("iFixit", "Encoding error: " + e.getMessage());
                  }

                  selectedFragment = webView;
                  mSelectedTab = MORE_INFO_TAB;
               }
               return selectedFragment;
            default:
               return null;
         }
      }

      @Override
      public void setPrimaryItem(ViewGroup container, int position, Object object) {
         super.setPrimaryItem(container, position, object);
         mSelectedTab = position;
      }
   }
}
