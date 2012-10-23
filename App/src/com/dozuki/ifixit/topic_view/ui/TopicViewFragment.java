package com.dozuki.ifixit.topic_view.ui;

import java.net.URLEncoder;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.Tab;
import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.dozuki.ifixit.MainApplication;
import com.dozuki.ifixit.R;
import com.dozuki.ifixit.dozuki.model.Site;
import com.dozuki.ifixit.guide_view.ui.LoadingFragment;
import com.dozuki.ifixit.guide_view.ui.NoGuidesFragment;
import com.dozuki.ifixit.guide_view.ui.WebViewFragment;
import com.dozuki.ifixit.topic_view.model.TopicLeaf;
import com.dozuki.ifixit.topic_view.model.TopicNode;
import com.dozuki.ifixit.util.APIEndpoint;
import com.dozuki.ifixit.util.APIReceiver;
import com.dozuki.ifixit.util.APIService;
import com.ifixit.android.imagemanager.ImageManager;
import com.dozuki.ifixit.util.Error;

public class TopicViewFragment extends SherlockFragment
 implements ActionBar.TabListener {
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
   private ActionBar mActionBar;
   private int mSelectedTab = -1;

   private APIReceiver mApiReceiver = new APIReceiver() {
      public void onSuccess(Object result, Intent intent) {
         setTopicLeaf((TopicLeaf)result);
      }

      public void onFailure(Error error, Intent intent) {
         APIService.getErrorDialog(getActivity(), error,
          APIService.getTopicIntent(getActivity(), mTopicNode.getName()))
          .show();
      }
   };

   public boolean isDisplayingTopic() {
      return mTopicLeaf != null;
   }

   @Override
   public void onCreate(Bundle savedInstanceState) {
      getActivity().setTheme(((MainApplication)getActivity().getApplication()).getSiteTheme());
      getActivity().setTitle("");
      super.onCreate(savedInstanceState);

      if (mImageManager == null) {
         mImageManager = ((MainApplication)getActivity().getApplication()).
          getImageManager();
      }

      if (mSite == null) {
         mSite = ((MainApplication)getActivity().getApplication())
          .getSite();
      }

      if (savedInstanceState != null) {
         mSelectedTab = savedInstanceState.getInt(CURRENT_PAGE);
         mTopicNode = (TopicNode)savedInstanceState.getSerializable(
          CURRENT_TOPIC_NODE);
         TopicLeaf topicLeaf = (TopicLeaf)savedInstanceState.getSerializable(
          CURRENT_TOPIC_LEAF);

         if (topicLeaf != null) {
            setTopicLeaf(topicLeaf);
         } else if (mTopicNode != null) {
            getTopicLeaf(mTopicNode.getName());
         }
      }
   }

   @Override
   public View onCreateView(LayoutInflater inflater, ViewGroup container,
    Bundle savedInstanceState) {
      View view = inflater.inflate(R.layout.topic_view_fragment, container,
       false);

      return view;
   }

   @Override
   public void onResume() {
      super.onResume();

      IntentFilter filter = new IntentFilter();
      filter.addAction(APIEndpoint.TOPIC.mAction);
      getActivity().registerReceiver(mApiReceiver, filter);
   }

   @Override
   public void onPause() {
      super.onPause();

      try {
         getActivity().unregisterReceiver(mApiReceiver);
      } catch (IllegalArgumentException e) {
         // Do nothing. This happens in the unlikely event that
         // unregisterReceiver has been called already.
      }
   }

   @Override
   public void onAttach(Activity activity) {
      super.onAttach(activity);
      mActionBar = ((SherlockFragmentActivity)activity).getSupportActionBar();
   }

   @Override
   public void onSaveInstanceState(Bundle outState) {
      super.onSaveInstanceState(outState);

      outState.putInt(CURRENT_PAGE, mActionBar.getSelectedNavigationIndex());
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
      mActionBar.removeAllTabs();

      if (mTopicLeaf == null) {
         // display error message
         return;
      }

      mActionBar.setTitle(mTopicLeaf.getName());

      mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
      ActionBar.Tab tab = mActionBar.newTab();
      tab.setText(getActivity().getString(R.string.guides));
      tab.setTabListener(this);
      mActionBar.addTab(tab, false);

      tab = mActionBar.newTab();
      tab.setText(getActivity().getString(R.string.info));
      tab.setTabListener(this);
      mActionBar.addTab(tab, false);

      if (mSite.mAnswers) {
         tab = mActionBar.newTab();
         tab.setText(getActivity().getString(R.string.answers));
         tab.setTabListener(this);
         mActionBar.addTab(tab, false);
      }

      if (mSelectedTab != -1) {
         mActionBar.setSelectedNavigationItem(mSelectedTab);
      } else {
         selectDefaultTab();
      }
   }

   private void displayLoading() {
      mActionBar.removeAllTabs();
      FragmentTransaction ft = getActivity().getSupportFragmentManager().
       beginTransaction();
      ft.replace(R.id.topic_view_page_fragment, new LoadingFragment());
      ft.commit();
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

      mActionBar.setSelectedNavigationItem(tab);
   }

   private void getTopicLeaf(String topicName) {
      displayLoading();
      mTopicLeaf = null;
      mSelectedTab = -1;

      getActivity().startService(APIService.getTopicIntent(getActivity(),
       topicName));
   }

   public TopicLeaf getTopicLeaf() {
      return mTopicLeaf;
   }

   public TopicNode getTopicNode() {
      return mTopicNode;
   }

   @Override
   public void onTabSelected(Tab tab, FragmentTransaction ft) {
      int position = tab.getPosition();
      Fragment selectedFragment;
      ft = getActivity().getSupportFragmentManager().beginTransaction();

      if (mTopicLeaf == null) {
         Log.w("iFixit", "Trying to get Fragment at bad position");
         return;
      }

      if (position == GUIDES_TAB) {
         if (mTopicLeaf.getGuides().size() == 0) {
            selectedFragment = new NoGuidesFragment();
         } else {
            selectedFragment = new TopicGuideListFragment(mImageManager,
             mTopicLeaf);
         }
         mSelectedTab = GUIDES_TAB;
      } else if (position == ANSWERS_TAB && mSite.mAnswers) {
         WebViewFragment webView = new WebViewFragment();

         webView.loadUrl(mTopicLeaf.getSolutionsUrl());

         selectedFragment = webView;
         mSelectedTab = ANSWERS_TAB;
      } else if (position == MORE_INFO_TAB) {
         WebViewFragment webView = new WebViewFragment();

         try {
            webView.loadUrl("http://" + mSite.mDomain + "/c/" +
             URLEncoder.encode(mTopicLeaf.getName(), "UTF-8"));
         } catch (Exception e) {
            Log.w("iFixit", "Encoding error: " + e.getMessage());
         }

         selectedFragment = webView;
         mSelectedTab = MORE_INFO_TAB;
      } else {
         Log.w("iFixit", "Too many tabs!");
         return;
      }

      ft.replace(R.id.topic_view_page_fragment, selectedFragment);
      ft.commit();
   }

   @Override
   public void onTabUnselected(Tab tab, FragmentTransaction ft) {}

   @Override
   public void onTabReselected(Tab tab, FragmentTransaction ft) {}
}
