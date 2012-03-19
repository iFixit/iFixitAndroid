package com.dozuki.ifixit;

import java.util.LinkedList;

import android.content.Intent;
import android.os.Bundle;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager.OnBackStackChangedListener;
import android.support.v4.app.FragmentTransaction;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;

public class TopicsActivity extends SherlockFragmentActivity implements
 TopicSelectedListener, OnBackStackChangedListener {
   private static final String ROOT_TOPIC = "ROOT_TOPIC";
   private static final String TOPIC_HISTORY = "TOPIC_HISTORY";
   protected static final int REQUEST_RETURN_TOPIC = 1;
   protected static final int TOPIC_RESULT = 2;
   protected static final int NO_TOPIC_RESULT = 3;

   private boolean mDualPane;
   private TopicViewFragment mTopicView;
   private TopicNode mRootTopic;
   private LinkedList<String> mTopicHistory;
   private int mBackStackSize = 0;

   @Override
   public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      
      setContentView(R.layout.topics);
      
      mTopicView = (TopicViewFragment)getSupportFragmentManager()
       .findFragmentById(R.id.topic_view_fragment);
      mDualPane = mTopicView != null && mTopicView.isInLayout();
      mTopicHistory = new LinkedList<String>();

      if (savedInstanceState != null) {
         mRootTopic = (TopicNode)savedInstanceState.getSerializable(ROOT_TOPIC);
         mTopicHistory = (LinkedList<String>)savedInstanceState.
          getSerializable(TOPIC_HISTORY);
         
         if (mTopicHistory.size() != 0) {
            setActionBarTitle(mTopicHistory.getFirst());
         }
      } else {
         APIHelper.getCategories(new APIHelper.APIResponder<TopicNode>() {
            public void setResult(TopicNode result) {
               mRootTopic = result;
               onTopicSelected(mRootTopic);
            }
         });
      }

      if (mDualPane) {
         mTopicView.setActionBar(getSupportActionBar());
      }

      getSupportFragmentManager().addOnBackStackChangedListener(this);
      mBackStackSize = getSupportFragmentManager().getBackStackEntryCount();
   }
   
   @Override
   public void onSaveInstanceState(Bundle outState) {
      super.onSaveInstanceState(outState);

      outState.putSerializable(ROOT_TOPIC, mRootTopic);
      outState.putSerializable(TOPIC_HISTORY, mTopicHistory);
   }

   public void onBackStackChanged() {
      int backStackSize = getSupportFragmentManager().getBackStackEntryCount();

      if (mBackStackSize > backStackSize) {
         mTopicHistory.removeFirst();
         if (mTopicHistory.size() != 0) {
            setActionBarTitle(mTopicHistory.getFirst());
         }
      }

      mBackStackSize = backStackSize;
   }

   private void setActionBarTitle(String topic) {
      boolean setBack;

      if (!topic.equals("ROOT")) {
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
      mTopicHistory.addFirst(topic.getName());

      if (topic.isLeaf()) {
         if (mDualPane) {
            mTopicView.setTopicNode(topic);
            changeTopicListView(new Fragment(), true);
         } else {
            Intent intent = new Intent(this, TopicViewActivity.class);
            Bundle bundle = new Bundle();

            bundle.putSerializable(TopicViewActivity.TOPIC_KEY, topic);
            intent.putExtras(bundle);
            startActivityForResult(intent, REQUEST_RETURN_TOPIC);
         }
      } else {
         changeTopicListView(new TopicListFragment(topic), !topic.isRoot());
      }
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
