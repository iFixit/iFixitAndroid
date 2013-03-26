package com.dozuki.ifixit.ui.topic_view;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import com.dozuki.ifixit.MainApplication;
import com.dozuki.ifixit.R;
import com.dozuki.ifixit.model.dozuki.Site;
import com.dozuki.ifixit.model.topic.TopicLeaf;
import com.dozuki.ifixit.model.topic.TopicNode;
import com.dozuki.ifixit.ui.IfixitActivity;
import com.dozuki.ifixit.ui.guide_view.NoGuidesFragment;
import com.dozuki.ifixit.ui.guide_view.WebViewFragment;
import com.dozuki.ifixit.util.APIEvent;
import com.dozuki.ifixit.util.APIService;
import com.marczych.androidimagemanager.ImageManager;
import com.squareup.otto.Subscribe;
import com.viewpagerindicator.TitlePageIndicator;
import org.holoeverywhere.LayoutInflater;
import org.holoeverywhere.app.Activity;
import org.holoeverywhere.app.Fragment;

import java.net.URLEncoder;

public class TopicViewFragment extends Fragment {
   private static final int GUIDES_TAB = 0;
   private static final int MORE_INFO_TAB = 1;
   private static final int ANSWERS_TAB = 2;
   private static final String CURRENT_PAGE = "CURRENT_PAGE";
   private static final String CURRENT_TOPIC_LEAF = "CURRENT_TOPIC_LEAF";
   private static final String CURRENT_TOPIC_NODE = "CURRENT_TOPIC_NODE";

   private TopicNode mTopicNode;
   private TopicLeaf mTopicLeaf;
   private ImageManager mImageManager;
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
         APIService.getErrorDialog(getActivity(), event.getError(), APIService.getTopicAPICall(mTopicNode.getName()))
            .show();
      }
   }

   public boolean isDisplayingTopic() {
      return mTopicLeaf != null;
   }

   @Override
   public void onCreate(Bundle savedInstanceState) {
      getActivity().setTitle("");
      super.onCreate(savedInstanceState);

      if (mImageManager == null) {
         mImageManager = ((MainApplication) getActivity().getApplication()).getImageManager();
      }

      if (mSite == null) {
         mSite = ((MainApplication) getActivity().getApplication()).getSite();
      }
   }

   @Override
   public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
      View view = inflater.inflate(R.layout.topic_view_fragment, container, false);

      mPager = (ViewPager) view.findViewById(R.id.topic_view_view_pager);
      mTitleIndicator = (TitlePageIndicator) view.findViewById(R.id.topic_view_indicator);

      if (savedInstanceState != null) {
         mSelectedTab = savedInstanceState.getInt(CURRENT_PAGE);
         mTopicNode = (TopicNode) savedInstanceState.getSerializable(CURRENT_TOPIC_NODE);
         TopicLeaf topicLeaf = (TopicLeaf) savedInstanceState.getSerializable(CURRENT_TOPIC_LEAF);

         if (topicLeaf != null) {
            setTopicLeaf(topicLeaf);
         } else if (mTopicNode != null) {
            getTopicLeaf(mTopicNode.getName());
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
   public void onAttach(Activity activity) {
      super.onAttach(activity);
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

      ((IfixitActivity) getActivity()).setCustomTitle(mTopicLeaf.getName());
      if (mTopicLeaf == null) {
         // display error message
         return;
      }

      mTitleIndicator.setVisibility(View.VISIBLE);
      mPageAdapter = new PageAdapter(this.getChildFragmentManager());
      mPager.setAdapter(mPageAdapter);
      mTitleIndicator.setViewPager(mPager);
      mPager.setOffscreenPageLimit(2);
      selectDefaultTab();
   }

   private void selectDefaultTab() {
      int tab;

      if (mTopicLeaf == null) {
         return;
      }

      if (mTopicLeaf.getGuides().size() == 0) {
         tab = MORE_INFO_TAB;
      } else {
         tab = GUIDES_TAB;
      }

      mPager.setCurrentItem(tab, false);
      mPager.invalidate();
      mTitleIndicator.invalidate();
   }

   private void getTopicLeaf(String topicName) {
      mTopicLeaf = null;
      mSelectedTab = -1;

      if (mTitleIndicator != null) {
         mTitleIndicator.setVisibility(View.VISIBLE);
      }

      APIService.call((Activity) getActivity(), APIService.getTopicAPICall(topicName));
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
         } else
            return 2;
      }

      @Override
      public CharSequence getPageTitle(int position) {
         switch (position) {
            case 0:
               return getActivity().getString(R.string.guides);
            case 1:

               if (mSite.mAnswers) {
                  return getActivity().getString(R.string.answers);
               } else {
                  return getActivity().getString(R.string.info);
               }

            case 2:
               return getActivity().getString(R.string.info);
         }
         return "";
      }

      @Override
      public Fragment getItem(int position) {

         Fragment selectedFragment;
         switch (position) {
            case 0:
               if (mTopicLeaf.getGuides().size() == 0) {
                  selectedFragment = new NoGuidesFragment();
               } else {
                  selectedFragment = new TopicGuideListFragment(mImageManager, mTopicLeaf);
               }
               mSelectedTab = GUIDES_TAB;
               return selectedFragment;
            case 1:
               if (mSite.mAnswers) {
                  WebViewFragment webView = new WebViewFragment();

                  webView.loadUrl(mTopicLeaf.getSolutionsUrl());

                  selectedFragment = webView;
                  mSelectedTab = ANSWERS_TAB;
               } else {
                  WebViewFragment webView = new WebViewFragment();

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
            case 2:
               WebViewFragment webView = new WebViewFragment();

               try {
                  webView.loadUrl("http://" + mSite.mDomain + "/c/" + URLEncoder.encode(mTopicLeaf.getName(), "UTF-8"));
               } catch (Exception e) {
                  Log.w("iFixit", "Encoding error: " + e.getMessage());
               }

               selectedFragment = webView;
               mSelectedTab = MORE_INFO_TAB;
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
