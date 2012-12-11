package com.dozuki.ifixit.topic_view.ui;

import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager.OnBackStackChangedListener;
import android.support.v4.app.FragmentTransaction;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.dozuki.ifixit.MainApplication;
import com.dozuki.ifixit.R;
import com.dozuki.ifixit.dozuki.model.Site;
import com.dozuki.ifixit.gallery.ui.GalleryActivity;
import com.dozuki.ifixit.login.model.LoginListener;
import com.dozuki.ifixit.login.model.User;
import com.dozuki.ifixit.login.ui.LoginFragment;
import com.dozuki.ifixit.topic_view.model.TopicNode;
import com.dozuki.ifixit.topic_view.model.TopicSelectedListener;
import com.dozuki.ifixit.util.APIEndpoint;
import com.dozuki.ifixit.util.APIError;
import com.dozuki.ifixit.util.APIReceiver;
import com.dozuki.ifixit.util.APIService;
import com.google.analytics.tracking.android.EasyTracker;

import org.holoeverywhere.app.Activity;

public class TopicsActivity extends Activity
 implements TopicSelectedListener, OnBackStackChangedListener, LoginListener {
   private static final String ROOT_TOPIC = "ROOT_TOPIC";
   private static final String TOPIC_LIST_VISIBLE = "TOPIC_LIST_VISIBLE";
   protected static final long TOPIC_LIST_HIDE_DELAY = 1;

   /**
    * Used for Dozuki app. Enables the up navigation button to finish the
    * activity and go back to the sites list.
    */
   private static final boolean UP_NAVIGATION_FINISH_ACTIVITY = false;

   private TopicViewFragment mTopicView;
   private FrameLayout mTopicViewOverlay;
   private TopicNode mRootTopic;
   private int mBackStackSize = 0;
   private boolean mDualPane;
   private boolean mHideTopicList;
   private boolean mTopicListVisible;
   private boolean mVerifiedUser;
   private boolean mRequireLogin;
   private Site mSite;

   private APIReceiver mApiReceiver = new APIReceiver() {
      public void onSuccess(Object result, Intent intent) {
         if (mRootTopic == null) {
            mRootTopic = (TopicNode)result;
            onTopicSelected(mRootTopic);
         }
      }

      public void onFailure(APIError error, Intent intent) {
         if (mRequireLogin && !mVerifiedUser) {
            triggerLogin();
         } else {
            APIService.getErrorDialog(TopicsActivity.this, error,
             APIService.getCategoriesIntent(TopicsActivity.this)).show();
         }
      }
   };

   @Override
   public void onCreate(Bundle savedInstanceState) {
      setTheme(((MainApplication)getApplication()).getSiteTheme());
      getSupportActionBar().setTitle(((MainApplication)getApplication()).
       getSiteDisplayTitle());

      super.onCreate(savedInstanceState);

      setContentView(R.layout.topics);

      if (mSite == null) {
         mSite = ((MainApplication)getApplication())
          .getSite();
      }   

      mTopicView = (TopicViewFragment)getSupportFragmentManager()
       .findFragmentById(R.id.topic_view_fragment);
      mTopicViewOverlay = (FrameLayout)findViewById(R.id.topic_view_overlay);
      mHideTopicList = mTopicViewOverlay != null;
      mDualPane = mTopicView != null && mTopicView.isInLayout();

      if (savedInstanceState != null) {
         mRootTopic = (TopicNode)savedInstanceState.getSerializable(ROOT_TOPIC);
         mTopicListVisible = savedInstanceState.getBoolean(TOPIC_LIST_VISIBLE);
      } else {
         mTopicListVisible = true;
      }

      if (!mTopicListVisible && !mHideTopicList) {
         getSupportFragmentManager().popBackStack();
      }

      if (mTopicListVisible && mHideTopicList && mTopicView.isDisplayingTopic())
         hideTopicListWithDelay();

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
      
      mRequireLogin = !mSite.mPublic;
      
      mVerifiedUser = ((MainApplication)getApplication()).getUserFromPreferenceFile() != null;

      APIService.setRequireAuthentication(mVerifiedUser);
      
      // If a site is private, and the user has not yet logged in, promt to log in
      if (mRequireLogin && !mVerifiedUser) {
         triggerLogin();
      }
   }
   
   public void triggerLogin() {
      LoginFragment.newInstance().show(getSupportFragmentManager());
   }
   
   @Override
   public void onStart() {
      super.onStart();
      
      EasyTracker.getInstance().activityStart(this);
   }
   
   @Override
   public void onStop() {
      super.onStop();
      
      EasyTracker.getInstance().activityStop(this);
   } 
   
   @Override
   public void onResume() {
      super.onResume();

      if (mVerifiedUser || !mRequireLogin) {
         initApiReceiver();
      }
   }

   @Override
   public void onLogin(User user) {
      mVerifiedUser = (user != null);
            
      initApiReceiver();
   }

   @Override
   public void onLogout() {
      ((MainApplication)getApplication()).logout();
      mVerifiedUser = false;
      
      finish();
   }

   @Override
   public void onCancel() {
      mVerifiedUser = false;

      finish();
   }
   
   @Override
   public void onPause() {
      super.onPause();

      try {
         unregisterReceiver(mApiReceiver);
      } catch (IllegalArgumentException e) {
         // Do nothing. This happens in the unlikely event that
         // unregisterReceiver has been called already.
      }
   }

   @Override
   public void onSaveInstanceState(Bundle outState) {
      super.onSaveInstanceState(outState);

      outState.putSerializable(ROOT_TOPIC, mRootTopic);
      outState.putBoolean(TOPIC_LIST_VISIBLE, mTopicListVisible);
   }

   public void initApiReceiver() {
      IntentFilter filter = new IntentFilter();
      filter.addAction(APIEndpoint.CATEGORIES.mAction);
      registerReceiver(mApiReceiver, filter);

      // Fetch categories if necessary.
      if (mRootTopic == null) {
         // Load categories from the API.
         startService(APIService.getCategoriesIntent(this));
      }      
   }

   public void onBackStackChanged() {
      int backStackSize = getSupportFragmentManager().getBackStackEntryCount();

      if (mBackStackSize > backStackSize) {
         setTopicListVisible();
      }

      mBackStackSize = backStackSize;

      getSupportActionBar().setDisplayHomeAsUpEnabled(mBackStackSize != 0 ||
       UP_NAVIGATION_FINISH_ACTIVITY);
   }

   @Override
   public void onTopicSelected(TopicNode topic) {
      EasyTracker.getTracker().trackEvent("ui_navigation", "list_item_selected", "topic_list", null);
      
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
            startActivity(intent);
         }
      } else {
         changeTopicListView(new TopicListFragment(topic), !topic.isRoot());
      }
   }

   private void hideTopicList() {
      hideTopicList(false);
   }

   @Override
   public boolean onCreateOptionsMenu(Menu menu) {
      MenuInflater inflater = getSupportMenuInflater();
      inflater.inflate(R.menu.menu_bar, menu);

      return super.onCreateOptionsMenu(menu);
   }

   private void hideTopicList(boolean delay) {
      getSupportActionBar().setDisplayHomeAsUpEnabled(true);
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

   private void changeTopicListView(Fragment fragment, boolean addToBack,
    boolean delay) {
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
   
   @Override
   public boolean onPrepareOptionsMenu(Menu menu) {
      MenuItem logout = menu.findItem(R.id.logout_button);
      logout.setVisible(mRequireLogin);
      
      return super.onPrepareOptionsMenu(menu);
   }

   @Override
   public boolean onOptionsItemSelected(MenuItem item) {
      switch (item.getItemId()) {
         case android.R.id.home:
            // Go up in the hierarchy by popping the back stack.
            boolean poppedStack = getSupportFragmentManager().
             popBackStackImmediate();

            /**
             *  If there is not a previous category to go to and the up navigation
             *  button should finish the activity, finish the activity.
             *  Note: Although this is a warning for iFixit, it is not for Dozuki.
             */
            if (!poppedStack && UP_NAVIGATION_FINISH_ACTIVITY) {
               finish();
            }

            return true;

         case R.id.gallery_button:
            Intent intent = new Intent(this, GalleryActivity.class);
            startActivity(intent);
            return true;
         case R.id.logout_button:
            LoginFragment.getLogoutDialog(this).show();
         default:
            return super.onOptionsItemSelected(item);
      }
   }
}
