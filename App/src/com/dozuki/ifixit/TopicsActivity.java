package com.dozuki.ifixit;

import java.util.LinkedList;

import android.content.Intent;
import android.os.Bundle;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager.OnBackStackChangedListener;
import android.support.v4.app.FragmentTransaction;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;

import android.view.MotionEvent;
import android.view.View;

import android.widget.FrameLayout;

public class TopicsActivity extends SherlockFragmentActivity implements
 TopicSelectedListener, OnBackStackChangedListener {
   private static final String ROOT_TOPIC = "ROOT_TOPIC";
   private static final String TOPIC_HISTORY = "TOPIC_HISTORY";
   private static final String TOPIC_LIST_VISIBLE = "TOPIC_LIST_VISIBLE";
   protected static final int REQUEST_RETURN_TOPIC = 1;
   protected static final int TOPIC_RESULT = 2;
   protected static final int NO_TOPIC_RESULT = 3;

   private TopicViewFragment mTopicView;
   private FrameLayout mTopicViewOverlay;
   private TopicNode mRootTopic;
   private LinkedList<String> mTopicHistory;
   private int mBackStackSize = 0;
   private boolean mDualPane;
   private boolean mHideTopicList;
   private boolean mTopicListVisible;

   @Override
   public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      
      setContentView(R.layout.topics);
      
      mTopicView = (TopicViewFragment)getSupportFragmentManager()
       .findFragmentById(R.id.topic_view_fragment);
      mTopicViewOverlay = (FrameLayout)findViewById(R.id.topic_view_overlay);
      mHideTopicList = mTopicViewOverlay != null;
      mDualPane = mTopicView != null && mTopicView.isInLayout();

      if (savedInstanceState != null) {
         mRootTopic = (TopicNode)savedInstanceState.getSerializable(ROOT_TOPIC);
         mTopicHistory = (LinkedList<String>)savedInstanceState.
          getSerializable(TOPIC_HISTORY);
         mTopicListVisible = savedInstanceState.getBoolean(TOPIC_LIST_VISIBLE);
         
         if (mTopicHistory.size() != 0) {
            setActionBarTitle(mTopicHistory.getFirst());
         }
      } else {
         mTopicListVisible = true;
         mTopicHistory = new LinkedList<String>();
         APIHelper.getCategories(new APIHelper.APIResponder<TopicNode>() {
            public void setResult(TopicNode result) {
               mRootTopic = result;
               onTopicSelected(mRootTopic);
            }
         });
      }

      if (!mTopicListVisible && !mHideTopicList) {
         getSupportFragmentManager().popBackStack();
      }

      getSupportFragmentManager().addOnBackStackChangedListener(this);
      mBackStackSize = getSupportFragmentManager().getBackStackEntryCount();

      if (mTopicViewOverlay != null) {
         mTopicViewOverlay.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
               if (mTopicListVisible && mTopicView.isDisplayingTopic()) {
                  hideTopicList();
                  return true;
               } else {
                  return false;
               }
            }
         });
      }
   }
   
   @Override
   public void onSaveInstanceState(Bundle outState) {
      super.onSaveInstanceState(outState);

      outState.putSerializable(ROOT_TOPIC, mRootTopic);
      outState.putSerializable(TOPIC_HISTORY, mTopicHistory);
      outState.putBoolean(TOPIC_LIST_VISIBLE, mTopicListVisible);
   }

   public void onBackStackChanged() {
      int backStackSize = getSupportFragmentManager().getBackStackEntryCount();

      if (mBackStackSize > backStackSize) {
         setTopicListVisible();
         mTopicHistory.removeFirst();
         if (mTopicHistory.size() != 0) {
            setActionBarTitle(mTopicHistory.getFirst());
         }
      }

      mBackStackSize = backStackSize;
   }

   private void setActionBarTitle(String topic) {
      boolean setBack;

      if (!TopicNode.isRootName(topic)) {
         getSupportActionBar().setTitle(topic);
         setBack = true;
      } else {
         getSupportActionBar().setTitle("");
         setBack = false;
      }

      getSupportActionBar().setDisplayHomeAsUpEnabled(setBack);
   }
   
   @Override
   public void onTopicSelected(TopicNode topic) {
      setActionBarTitle(topic.getName());

      if (topic.isLeaf()) {
         if (mDualPane) {
            mTopicView.setTopicNode(topic);

            if (mHideTopicList) {
               hideTopicList();
            }
         } else {
            Intent intent = new Intent(this, TopicViewActivity.class);
            Bundle bundle = new Bundle();

            bundle.putSerializable(TopicViewActivity.TOPIC_KEY, topic);
            intent.putExtras(bundle);
            startActivityForResult(intent, REQUEST_RETURN_TOPIC);
         }
      } else {
         changeTopicListView(new TopicListFragment(topic), !topic.isRoot());
         mTopicHistory.addFirst(topic.getName());
      }
   }

   private void hideTopicList() {
      getSupportActionBar().setDisplayHomeAsUpEnabled(true);
      mTopicHistory.addFirst("");
      mTopicViewOverlay.setVisibility(View.INVISIBLE);
      mTopicListVisible = false;
      changeTopicListView(new Fragment(), true);
   }

   private void setTopicListVisible() {
      if (mTopicViewOverlay != null) {
         mTopicViewOverlay.setVisibility(View.VISIBLE);
      }
      mTopicListVisible = true;
   }

   private void changeTopicListView(Fragment fragment, boolean addToBack) {
      FragmentTransaction ft = getSupportFragmentManager().beginTransaction();

      ft.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left,
            R.anim.slide_in_left, R.anim.slide_out_right);
      ft.replace(R.id.topic_list_fragment, fragment);

      if (addToBack) {
         ft.addToBackStack(null);
      }

      ft.commit();
   }

   @Override
   public boolean onOptionsItemSelected(MenuItem item) {
      switch (item.getItemId()) {
         case android.R.id.home:
            getSupportFragmentManager().popBackStack();

            return true;
         default:
            return super.onOptionsItemSelected(item);
      }
   }
}
