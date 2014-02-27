package com.dozuki.ifixit.ui.topic_view;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.dozuki.ifixit.App;
import com.dozuki.ifixit.R;
import com.dozuki.ifixit.model.dozuki.Site;
import com.dozuki.ifixit.model.topic.TopicLeaf;
import com.dozuki.ifixit.model.topic.TopicNode;
import com.dozuki.ifixit.ui.BaseActivity;
import com.dozuki.ifixit.ui.BaseFragment;
import com.dozuki.ifixit.ui.guide.view.GuideViewActivity;
import com.dozuki.ifixit.ui.guide.view.NoGuidesFragment;
import com.dozuki.ifixit.ui.WebViewFragment;
import com.dozuki.ifixit.util.api.ApiCall;
import com.dozuki.ifixit.util.api.ApiEvent;
import com.dozuki.ifixit.util.api.Api;
import com.google.analytics.tracking.android.Fields;
import com.google.analytics.tracking.android.MapBuilder;
import com.google.analytics.tracking.android.Tracker;
import com.squareup.otto.Subscribe;
import com.viewpagerindicator.TitlePageIndicator;

import java.util.HashMap;
import java.util.Map;

public class TopicViewFragment extends BaseFragment implements ViewPager.OnPageChangeListener {
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

   @Override
   public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);

      if (mSite == null) {
         mSite = ((App) getActivity().getApplication()).getSite();
      }
   }

   @Override
   public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
      View view = inflater.inflate(R.layout.topic_view_fragment, container, false);

      Bundle args = getArguments();

      mPager = (ViewPager) view.findViewById(R.id.topic_view_view_pager);
      mTitleIndicator = (TitlePageIndicator) view.findViewById(R.id.topic_view_indicator);
      mTitleIndicator.setOnPageChangeListener(this);

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
   public void onSaveInstanceState(Bundle outState) {
      super.onSaveInstanceState(outState);

      outState.putInt(CURRENT_PAGE, mSelectedTab);
      outState.putSerializable(CURRENT_TOPIC_LEAF, mTopicLeaf);
      outState.putSerializable(CURRENT_TOPIC_NODE, mTopicNode);
   }

   @Subscribe
   public void onTopic(ApiEvent.Topic event) {
      if (!event.hasError()) {
         setTopicLeaf(event.getResult());
      } else {
         Api.getErrorDialog(getActivity(), event).show();
      }
   }

   public boolean isDisplayingTopic() {
      return mTopicLeaf != null;
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

      if (topicLeaf != null) {
         if (mTopicNode != null && !topicLeaf.getName().equals(mTopicNode.getName())) {
            // Not the most recently selected topic... wait for another.
            return;
         } else if (mTopicLeaf != null && mTopicLeaf.equals(topicLeaf)) {
            selectDefaultTab();
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

   @Override
   public void onPageScrolled(int i, float v, int i2) {

   }

   @Override
   public void onPageSelected(int position) {
      String label = mPageAdapter.getFragmentScreenLabel(position);
      Tracker tracker = App.getGaTracker();
      tracker.set(Fields.SCREEN_NAME, label);
      tracker.send(MapBuilder.createAppView().build());
   }

   @Override
   public void onPageScrollStateChanged(int i) {

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

      ((BaseActivity) getActivity()).hideLoading();
   }

   private void getTopicLeaf(String topicName) {
      mTopicLeaf = null;
      mSelectedTab = -1;

      Api.call(getActivity(), ApiCall.topic(topicName));
   }

   public TopicLeaf getTopicLeaf() {
      return mTopicLeaf;
   }

   public TopicNode getTopicNode() {
      return mTopicNode;
   }

   public class PageAdapter extends FragmentStatePagerAdapter {
      private Map<Integer, String> mPageLabelMap;

      public PageAdapter(FragmentManager fm) {
         super(fm);

         mPageLabelMap = new HashMap<Integer, String>();
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
         String label = "/category/" + mTopicLeaf.getName();
         Fragment selectedFragment;

         switch (position) {
            case GUIDES_TAB:
               if (mTopicLeaf.getGuides().size() == 0) {
                  selectedFragment = new NoGuidesFragment();
               } else {
                  selectedFragment = new TopicGuideListFragment(mTopicLeaf);
               }
               mSelectedTab = GUIDES_TAB;
               label += "/guides";
               break;
            case MORE_INFO_TAB:
               selectedFragment = new TopicInfoFragment(mTopicLeaf);
               label += "/info";

               mSelectedTab = MORE_INFO_TAB;
               break;
            case ANSWERS_TAB:
               WebViewFragment webView = new WebViewFragment();

               label += "/answers";

               webView.loadUrl(mTopicLeaf.getSolutionsUrl());

               selectedFragment = webView;
               mSelectedTab = ANSWERS_TAB;

               break;
            default:
               return null;
         }

         mPageLabelMap.put(position, label);

         return selectedFragment;
      }

      @Override
      public void setPrimaryItem(ViewGroup container, int position, Object object) {
         super.setPrimaryItem(container, position, object);
         mSelectedTab = position;
      }

      public String getFragmentScreenLabel(int key) {
         return mPageLabelMap.get(key);
      }

      @Override
      public void destroyItem(View container, int position, Object object) {
         super.destroyItem(container, position, object);

         mPageLabelMap.remove(position);
      }
   }
}
