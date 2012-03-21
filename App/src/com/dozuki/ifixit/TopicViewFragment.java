package com.dozuki.ifixit;

import java.net.URLEncoder;

import android.app.Activity;

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

public class TopicViewFragment extends SherlockFragment
 implements ActionBar.TabListener {
   private static final int GUIDES_TAB = 0;
   private static final int ANSWERS_TAB = 1;
   private static final int MORE_INFO_TAB = 2;
   private static final String CURRENT_PAGE = "CURRENT_PAGE";
   private static final String CURRENT_TOPIC_LEAF = "CURRENT_TOPIC_LEAF";
   private static final String CURRENT_TOPIC_NODE = "CURRENT_TOPIC_NODE";

   private TopicNode mTopicNode;
   private TopicLeaf mTopicLeaf;
   private ImageManager mImageManager;
   private ActionBar mActionBar;
   private int mSelectedTab = -1;

   public boolean isDisplayingTopic() {
      return mTopicLeaf != null;
   }

   @Override
   public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);

      if (mImageManager == null) {
         mImageManager = ((MainApplication)getActivity().getApplication()).
          getImageManager();
      }

      if (savedInstanceState != null) {
         mSelectedTab = savedInstanceState.getInt(CURRENT_PAGE);
         setTopicLeaf((TopicLeaf)savedInstanceState.getSerializable(
          CURRENT_TOPIC_LEAF));
         mTopicNode = (TopicNode)savedInstanceState.getSerializable(
          CURRENT_TOPIC_NODE);
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
      if (mTopicLeaf != null && topicLeaf != null &&
       mTopicLeaf.equals(topicLeaf)) {
         selectDefaultTab();
         return;
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
      mActionBar.addTab(tab);

      tab = mActionBar.newTab();
      tab.setText(getActivity().getString(R.string.answers));
      tab.setTabListener(this);
      mActionBar.addTab(tab);

      tab = mActionBar.newTab();
      tab.setText(getActivity().getString(R.string.moreInfo));
      tab.setTabListener(this);
      mActionBar.addTab(tab);

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

   private void getTopicLeaf(final String topicName) {
      displayLoading();
      mTopicLeaf = null;
      mSelectedTab = -1;

      APIHelper.getTopic(topicName, new APIHelper.APIResponder<TopicLeaf>() {
         public void setResult(TopicLeaf result) {
            setTopicLeaf(result);
         }
      });
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
         selectedFragment = new TopicGuideListFragment(mImageManager, mTopicLeaf);
      } else if (position == ANSWERS_TAB) {
         WebViewFragment webView = new WebViewFragment();

         webView.loadUrl(mTopicLeaf.getSolutionsUrl());

         selectedFragment = webView;
      } else if (position == MORE_INFO_TAB) {
         WebViewFragment webView = new WebViewFragment();

         try {
            webView.loadUrl("http://www.ifixit.com/c/" +
             URLEncoder.encode(mTopicLeaf.getName(), "UTF-8"));
         } catch (Exception e) {
            Log.w("iFixit", "Encoding error: " + e.getMessage());
         }

         selectedFragment = webView;
      } else {
         Log.w("iFixit", "Too many tabs!");
         return;
      }

      ft.replace(R.id.topic_view_page_fragment, selectedFragment);
      ft.commit();
   }

   @Override
   public void onTabUnselected(Tab tab, FragmentTransaction ft) {
   }

   @Override
   public void onTabReselected(Tab tab, FragmentTransaction ft) {
   }
}
