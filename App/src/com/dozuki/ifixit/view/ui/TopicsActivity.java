package com.dozuki.ifixit.view.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager.OnBackStackChangedListener;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar.Tab;
import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.*;
import com.dozuki.ifixit.MainApplication;
import com.dozuki.ifixit.R;
import com.dozuki.ifixit.util.APIService;
import com.dozuki.ifixit.view.model.LoginListener;
import com.dozuki.ifixit.view.model.TopicNode;
import com.dozuki.ifixit.view.model.TopicSelectedListener;
import com.dozuki.ifixit.view.model.User;

public class TopicsActivity extends SherlockFragmentActivity implements
 TopicSelectedListener, OnBackStackChangedListener, LoginListener {
   private static final String ROOT_TOPIC = "ROOT_TOPIC";
   private static final String TOPIC_LIST_VISIBLE = "TOPIC_LIST_VISIBLE";
   private static final String LOGIN_VISIBLE = "LOGIN_VISIBLE";
   protected static final long TOPIC_LIST_HIDE_DELAY = 1;

   private TopicViewFragment mTopicView;
   private MediaFragment mMediaView;
   private FrameLayout mTopicViewOverlay;
   private TopicNode mRootTopic;
   private int mBackStackSize = 0;
   private boolean mDualPane;
   private boolean mHideTopicList;
   private boolean mTopicListVisible;
   private boolean mLoginVisible;

   private BroadcastReceiver mApiReceiver = new BroadcastReceiver() {
      @Override
      public void onReceive(Context context, Intent intent) {
         APIService.Result result = (APIService.Result)
          intent.getExtras().getSerializable(APIService.RESULT);

         if (!result.hasError()) {
            if (mRootTopic == null) {
            	
            	if(result.getResult() instanceof User)
            	{
            		Log.e("logged in ", ((User)result.getResult()).getUsername());
            	
            	}else
            	{
                  mRootTopic = (TopicNode)result.getResult();   
                  onTopicSelected(mRootTopic);
            	}
            }
         } else {
            APIService.getErrorDialog(TopicsActivity.this, result.getError(),
             APIService.getCategoriesIntent(TopicsActivity.this)).show();
         }
      }
   };

   @Override
   public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);

      getSupportActionBar().setTitle("");
      setContentView(R.layout.topics);
      
      com.actionbarsherlock.app.ActionBar actionBar =  getSupportActionBar();
      
      
      mTopicView = (TopicViewFragment)getSupportFragmentManager()
       .findFragmentById(R.id.topic_view_fragment);
      mTopicViewOverlay = (FrameLayout)findViewById(R.id.topic_view_overlay);
      mHideTopicList = mTopicViewOverlay != null;
      mDualPane = mTopicView != null && mTopicView.isInLayout();

      if (savedInstanceState != null) {
         mRootTopic = (TopicNode)savedInstanceState.getSerializable(ROOT_TOPIC);
         mTopicListVisible = savedInstanceState.getBoolean(TOPIC_LIST_VISIBLE);
         mLoginVisible = savedInstanceState.getBoolean(LOGIN_VISIBLE);
      } else {
         mTopicListVisible = true;
         mLoginVisible= false;
      }

      if (mRootTopic == null) {
         fetchCategories();
      }

      if (!mTopicListVisible && !mHideTopicList) {
         getSupportFragmentManager().popBackStack();
      }

      if (mTopicListVisible && mHideTopicList &&
       mTopicView.isDisplayingTopic()) {
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
   
   @Override
   public boolean onCreateOptionsMenu(com.actionbarsherlock.view.Menu menu)
   {
	 com.actionbarsherlock.view.MenuInflater inflater = getSupportMenuInflater();
	 inflater.inflate(R.menu.menu_bar, menu);
	  return super.onCreateOptionsMenu(menu);
   }

   @Override
   public void onResume() {
      super.onResume();

      IntentFilter filter = new IntentFilter();
      filter.addAction(APIService.ACTION_CATEGORIES);
      registerReceiver(mApiReceiver, filter);
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
      outState.putBoolean(LOGIN_VISIBLE, mLoginVisible);
   }

   // Load categories from the API.
   private void fetchCategories() {
      startService(APIService.getCategoriesIntent(this));
   }

   public void onBackStackChanged() {
	   
	 
      int backStackSize = getSupportFragmentManager().getBackStackEntryCount();

      if (mBackStackSize > backStackSize) {
         setTopicListVisible();
	     if (mLoginVisible) {
	        mLoginVisible = false;
		 }
      }

      mBackStackSize = backStackSize;

      getSupportActionBar().setDisplayHomeAsUpEnabled(mBackStackSize != 0);
   }

   @Override
   public void onTopicSelected(TopicNode topic) {
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
      
      
      if(mLoginVisible)
      {
         ft.setCustomAnimations(R.anim.slide_in_bottom, R.anim.slide_out_left,
        		 R.anim.slide_in_left, R.anim.slide_out_bottom);
      }else
      {
         ft.setCustomAnimations(inAnim, outAnim,
            R.anim.slide_in_left, R.anim.slide_out_right);
      }
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
   
   private void changeMainView(Fragment fragment, boolean addToBack,
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
		      
		      ft.replace(R.id.topic_view_fragment, fragment);

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
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		
		super.onActivityResult(requestCode, resultCode, data);
	   
		

	}

   @Override
   public boolean onOptionsItemSelected(MenuItem item) {
      switch (item.getItemId()) {
         case android.R.id.home:
            getSupportFragmentManager().popBackStack();
            return true;
         case R.id.gallery_button:
        	 MainApplication mainApp = (MainApplication) getApplication();
        	 if(mainApp.getUser() == null)
        	 {
			if (mDualPane) {
				if (!mLoginVisible) {
					LoginFragment fg = LoginFragment.newInstance();
					fg.registerOnLoginListener(this);
					mLoginVisible = true;
					changeTopicListView(fg, true);
				}
			}    		 
        	 else
        	 {
        	   // Intent i = new Intent(this,LoginActivity.class);
        	  //  startActivity(i);
        		 if (!mLoginVisible) {
        		 LoginFragment fg = LoginFragment.newInstance();
					fg.registerOnLoginListener(this);
					mLoginVisible = true;
					changeTopicListView(fg, true);
        		 }
        	 }
        	 }else
        	 {
        		 if (mMediaView == null) {
        				mMediaView = MediaFragment.newInstance();

        			}
        			if (mDualPane) {

        				changeMainView(mMediaView, true, false);
        				mTopicViewOverlay.setVisibility(View.INVISIBLE);
        				hideTopicList();
        			} else {
        				this.changeTopicListView(mMediaView, true, false);
        			}
        	 }
        	 return true;
         default:
            return super.onOptionsItemSelected(item);
      }
   }
    
   @Override
   protected void onDestroy() {
     super.onDestroy();
   }

	@Override
	public void onLogin(User user) {

		if (mLoginVisible) {
			getSupportFragmentManager().popBackStack();
		}

		if (mMediaView == null) {
			mMediaView = MediaFragment.newInstance();

		}
		if (mDualPane) {

			changeMainView(mMediaView, true, false);
			mTopicViewOverlay.setVisibility(View.INVISIBLE);
			hideTopicList();
		} else {
			this.changeTopicListView(mMediaView, true, false);
		}
		// Intent i = new Intent(this, MediaActivity.class);
		// startActivity(i);
	}
}
