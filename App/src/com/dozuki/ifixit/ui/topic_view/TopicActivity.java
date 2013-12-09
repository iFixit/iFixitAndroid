package com.dozuki.ifixit.ui.topic_view;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import com.dozuki.ifixit.ui.BaseSearchMenuDrawerActivity;
import com.dozuki.ifixit.R;
import com.dozuki.ifixit.model.topic.TopicNode;
import com.dozuki.ifixit.ui.LoadingFragment;
import com.dozuki.ifixit.util.api.ApiCall;
import com.dozuki.ifixit.util.api.ApiEvent;
import com.dozuki.ifixit.util.api.Api;
import com.squareup.otto.Subscribe;

public class TopicActivity extends BaseSearchMenuDrawerActivity
 implements TopicSelectedListener, FragmentManager.OnBackStackChangedListener {
   private static final String ROOT_TOPIC = "ROOT_TOPIC";
   private static final String TOPIC_LIST_VISIBLE = "TOPIC_LIST_VISIBLE";
   protected static final long TOPIC_LIST_HIDE_DELAY = 1;
   private static final String TOPIC_TAG = "TOPIC_TAG";
   private static final String TOPIC_LOADING = "TOPIC_LOADING_TAG";

   private TopicViewFragment mTopicView;
   private FrameLayout mTopicViewOverlay;
   private TopicNode mRootTopic;
   private int mBackStackSize = 0;
   private boolean mDualPane;
   private boolean mHideTopicList;
   private boolean mTopicListVisible;

   @Override
   public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);

      setContentView(R.layout.topics);

      FrameLayout topicViewContainer = (FrameLayout) findViewById(R.id.topic_view_fragment_container);
      mDualPane = topicViewContainer != null;
      mTopicViewOverlay = (FrameLayout) findViewById(R.id.topic_view_overlay);
      mHideTopicList = mTopicViewOverlay != null;

      if (savedInstanceState != null) {
         mRootTopic = (TopicNode) savedInstanceState.getSerializable(ROOT_TOPIC);
         mTopicListVisible = savedInstanceState.getBoolean(TOPIC_LIST_VISIBLE);
      } else {
         mTopicListVisible = true;
      }

      if (mDualPane) {
         mTopicView = (TopicViewFragment) getSupportFragmentManager().findFragmentByTag(TOPIC_TAG);

         if (mTopicView == null) {
            mTopicView = new TopicViewFragment();
            getSupportFragmentManager().beginTransaction()
             .add(R.id.topic_view_fragment_container, mTopicView, TOPIC_TAG)
             .commit();
         }
      }

      if (mRootTopic == null) {
         showLoading(R.id.topic_list_fragment);
         Api.call(this, ApiCall.categories());
      }

      if (!mTopicListVisible && !mHideTopicList) {
         getSupportFragmentManager().popBackStack();
      }

      if (mTopicView != null && mTopicListVisible && mHideTopicList && mTopicView.isDisplayingTopic()) {
         hideTopicListWithDelay();
      }

      getSupportFragmentManager().addOnBackStackChangedListener(this);

      // Reset backstack size
      mBackStackSize = -1;
      onBackStackChanged();

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

   @Subscribe
   public void onCategories(ApiEvent.Categories event) {
      hideLoading();
      if (!event.hasError()) {
         if (mRootTopic == null) {
            mRootTopic = event.getResult();
            onTopicSelected(mRootTopic);
         }
      } else {
         Api.getErrorDialog(this, event).show();
      }
   }

   @Subscribe
   public void onTopic(ApiEvent.Topic event) {
      hideTopicLoading();
   }

   @Override
   public void onSaveInstanceState(Bundle outState) {
      super.onSaveInstanceState(outState);

      outState.putSerializable(ROOT_TOPIC, mRootTopic);
      outState.putBoolean(TOPIC_LIST_VISIBLE, mTopicListVisible);
   }

   @Override
   public void onBackStackChanged() {
      int backStackSize = getSupportFragmentManager().getBackStackEntryCount();

      if (mBackStackSize > backStackSize) {
         setTopicListVisible();
      }

      mBackStackSize = backStackSize;
   }

   @Override
   public void onTopicSelected(TopicNode topic) {
      if (topic.isLeaf()) {
         if (mDualPane) {
            showTopicLoading(topic.getDisplayName());
            mTopicView.setTopicNode(topic);

            if (mHideTopicList) {
               hideTopicList();
            }
         } else {
            Intent intent = new Intent(this, TopicViewActivity.class);
            Bundle bundle = new Bundle();

            bundle.putSerializable(TopicViewActivity.TOPIC_KEY, topic);
            intent.putExtras(bundle);
            startActivity(intent);
         }
      } else {
         if (mDualPane && !topic.isRoot()) {
            showTopicLoading(topic.getDisplayName());

            mTopicView.setTopicNode(topic);
         }
         changeTopicListView(new TopicListFragment(topic), !topic.isRoot());
      }
   }

   protected boolean isDualPane() {
      return mDualPane;
   }

   private void hideTopicList() {
      hideTopicList(false);
   }

   private void hideTopicList(boolean delay) {
      mTopicViewOverlay.setVisibility(View.INVISIBLE);
      mTopicListVisible = false;
      changeTopicListView(new Fragment(), true, delay);
   }

   private void hideTopicListWithDelay() {
      // Delay this slightly to make sure the animation is played.
      new Handler().postAtTime(new Runnable() {
         public void run() {
            hideTopicList(true);
         }
      }, SystemClock.uptimeMillis() + TOPIC_LIST_HIDE_DELAY);
   }

   private void setTopicListVisible() {
      if (mTopicViewOverlay != null) {
         mTopicViewOverlay.setVisibility(View.VISIBLE);
      }
      mTopicListVisible = true;
   }

   private void changeTopicListView(Fragment fragment, boolean addToBack) {
      changeTopicListView(fragment, addToBack, false);
   }

   private void changeTopicListView(Fragment fragment, boolean addToBack, boolean delay) {
      FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
      int inAnim, outAnim;

      if (delay) {
         inAnim = R.anim.slide_in_right_delay;
         outAnim = R.anim.slide_out_left_delay;
      } else {
         inAnim = R.anim.slide_in_right;
         outAnim = R.anim.slide_out_left;
      }

      ft.setCustomAnimations(inAnim, outAnim,
       R.anim.slide_in_left, R.anim.slide_out_right);
      ft.replace(R.id.topic_list_fragment, fragment);

      if (addToBack) {
         ft.addToBackStack(null);
      }

      // ft.commit();
      // commitAllowingStateLoss doesn't throw an exception if commit() is
      // run after the fragments parent already saved its state.  Possibly
      // fixes the IllegalStateException crash in FragmentManagerImpl.checkStateLoss()
      ft.commitAllowingStateLoss();
   }

   /////////////////////////////////////////////////////
   // HELPERS
   /////////////////////////////////////////////////////

   public void showTopicLoading(String topicName) {
      mTopicView = (TopicViewFragment) getSupportFragmentManager().findFragmentByTag(TOPIC_TAG);
      if (mTopicView != null) {
         FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
         ft.hide(mTopicView);
         ft.add(R.id.topic_view_fragment_container,
          new LoadingFragment(getString(R.string.loading_topic, topicName)), TOPIC_LOADING);
         ft.commit();
      }
   }

   public void hideTopicLoading() {
      // Do not re-set mTopicView with this fragment reference
      Fragment frag = getSupportFragmentManager().findFragmentByTag(TOPIC_TAG);
      if (frag != null) {
         FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
         Fragment loading = getSupportFragmentManager().findFragmentByTag(TOPIC_LOADING);

         if (loading != null) {
            ft.remove(loading);
         }

         ft.show(frag);
         ft.commit();
      }
   }
}
