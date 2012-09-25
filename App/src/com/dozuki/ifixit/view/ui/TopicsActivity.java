package com.dozuki.ifixit.view.ui;

import android.content.BroadcastReceiver;
import android.content.ClipData.Item;
import android.content.SharedPreferences.Editor;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentManager.OnBackStackChangedListener;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

import com.actionbarsherlock.app.ActionBar;
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
	private static final String GALLERY_VISIBLE = "LOGIN_VISIBLE";
	protected static final long TOPIC_LIST_HIDE_DELAY = 1;
	private static final String BACK_STACK_STATE = "BACK_STACK_STATE";
	private static final String BACK_STACK_HIDDEN_STATE = "BACK_STACK_HIDDEN_STATE";
	private static final String GALLERY_FRAGMENT_ID = "GALLERY_FRAGMENT_ID";
	private static final String TOPIC_VIEW_FRAGMENT_ID = "TOPIC_VIEW_FRAGMENT_ID";

	private TopicViewFragment mTopicView;
	private MediaFragment mMediaView;
	private ActionBar mActionBar;
	private FrameLayout mTopicViewOverlay;
	private TopicNode mRootTopic;
	private int mBackStackSize = 0;
	private boolean mDualPane;
	private boolean mHideTopicList;
	private boolean mTopicListVisible;
	private boolean mLoginVisible;
	private boolean mGalleryVisible;
	private com.actionbarsherlock.view.Menu mMenu;
	View mTopicListView;

	private BroadcastReceiver mApiReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			APIService.Result result = (APIService.Result) intent.getExtras()
					.getSerializable(APIService.RESULT);

			if (!result.hasError()) {
				if (mRootTopic == null) {
					mRootTopic = (TopicNode) result.getResult();
					onTopicSelected(mRootTopic);
				}
			} else {
				APIService.getErrorDialog(TopicsActivity.this,
						result.getError(),
						APIService.getCategoriesIntent(TopicsActivity.this))
						.show();
			}
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.topics);

		mActionBar = getSupportActionBar();
		mActionBar.setTitle("");

		mTopicListView = (View) findViewById(R.id.topic_list_fragment);
		View galleryTopicView = (View) findViewById(R.id.topic_view_fragment);

		mTopicViewOverlay = (FrameLayout) findViewById(R.id.topic_view_overlay);
		mHideTopicList = mTopicViewOverlay != null;
		mDualPane = galleryTopicView != null; // && mTopicView.isInLayout();

		if (savedInstanceState != null) {
			mRootTopic = (TopicNode) savedInstanceState
					.getSerializable(ROOT_TOPIC);
			mTopicListVisible = savedInstanceState
					.getBoolean(TOPIC_LIST_VISIBLE);
			mLoginVisible = savedInstanceState.getBoolean(LOGIN_VISIBLE);
			mGalleryVisible = savedInstanceState.getBoolean(GALLERY_VISIBLE);
			mMediaView = (MediaFragment) getSupportFragmentManager()
					.findFragmentByTag(GALLERY_FRAGMENT_ID);
			mTopicView = (TopicViewFragment) getSupportFragmentManager()
					.findFragmentByTag(TOPIC_VIEW_FRAGMENT_ID);

		} else {
			mTopicListVisible = true;
			mLoginVisible = false;
			mGalleryVisible = false;
			mTopicView = new TopicViewFragment();
			mMediaView = new MediaFragment((Context) this);
			setUpGalleryAndTopicFragments();
		}

		if (mRootTopic == null) {
			fetchCategories();
		}

		if (!mTopicListVisible && !mHideTopicList) {
			getSupportFragmentManager().popBackStack(BACK_STACK_HIDDEN_STATE,
					FragmentManager.POP_BACK_STACK_INCLUSIVE);
		}

		// this brings back the gallery state on an orientation change or when
		// the
		if (mGalleryVisible)
			toggleGalleryView(mGalleryVisible);
		else {
			if (!mMediaView.isHidden()) {
				FragmentTransaction ft = getSupportFragmentManager()
						.beginTransaction();
				ft.hide(mMediaView);
				ft.commitAllowingStateLoss();
			}
		}
		// hide topic list so the allery get the entire screen
		if (mGalleryVisible && mDualPane) {
			mTopicListView.setVisibility(View.GONE);
		}

		if (mTopicListVisible && mHideTopicList
				&& (mTopicView.isDisplayingTopic() || mGalleryVisible)) {
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
						hideTopicList(BACK_STACK_HIDDEN_STATE);
						return true;

					} else {
						return false;
					}
				}
			});
		}

		// retrieves login information from user preferences
		if (((MainApplication) this.getApplication()).getUser() == null) {
			SharedPreferences preferenceFile = this.getSharedPreferences(
					LoginFragment.PREFERENCE_FILE, MODE_PRIVATE);
			User user = new User();
			String session = preferenceFile.getString(
					LoginFragment.SESSION_KEY, null);
			String username = preferenceFile.getString(
					LoginFragment.USERNAME_KEY, null);
			if (session != null) {
				user.setSession(session);
				user.setUsername(username);
				((MainApplication) this.getApplication()).setUser(user);
				mMediaView.retrieveUserImages();
			}
		}
		
		LoginFragment.clearLoginListeners();
		LoginFragment.registerOnLoginListener(this);
		LoginFragment.registerOnLoginListener(mMediaView);

	}

	private void setUpGalleryAndTopicFragments() {
		// add gallery and topic fragment
		if (mDualPane) {
			FragmentTransaction ft = getSupportFragmentManager()
					.beginTransaction();
			ft.add(R.id.topic_view_fragment, mMediaView, GALLERY_FRAGMENT_ID);
			ft.hide(mMediaView);
			ft.add(R.id.topic_view_fragment, mTopicView, TOPIC_VIEW_FRAGMENT_ID);
			ft.commitAllowingStateLoss();
		} else {
			FragmentTransaction ft = getSupportFragmentManager()
					.beginTransaction();
			ft.add(R.id.topic_media_fragment, mMediaView, GALLERY_FRAGMENT_ID);
			ft.hide(mMediaView);
			ft.commitAllowingStateLoss();
		}

	}

	@Override
	public boolean onCreateOptionsMenu(com.actionbarsherlock.view.Menu menu) {
		com.actionbarsherlock.view.MenuInflater inflater = getSupportMenuInflater();
		mMenu = menu;
		inflater.inflate(R.menu.menu_bar, menu);
		MenuItem galleryIcon = mMenu.findItem(R.id.gallery_button);
		MenuItem guideIcon = mMenu.findItem(R.id.guides_button);
		if (mGalleryVisible) {
			galleryIcon.setIcon(R.drawable.ic_menu_gallery_active);
			guideIcon.setIcon(R.drawable.ic_menu_guides_inactive);
		}
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
		outState.putBoolean(GALLERY_VISIBLE, mGalleryVisible);
	}

	// Load categories from the API.
	private void fetchCategories() {
		startService(APIService.getCategoriesIntent(this));
	}

	public void onBackStackChanged() {

		int backStackSize = getSupportFragmentManager()
				.getBackStackEntryCount();

		if (mBackStackSize > backStackSize) {

			setTopicListVisible();

			if (mLoginVisible) {
				// login popped off
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
					hideTopicList(BACK_STACK_HIDDEN_STATE);
				}
			} else {
				Intent intent = new Intent(this, TopicViewActivity.class);
				Bundle bundle = new Bundle();

				bundle.putSerializable(TopicViewActivity.TOPIC_KEY, topic);
				intent.putExtras(bundle);
				startActivity(intent);
			}
		} else {
			changeTopicListView(new TopicListFragment(topic), !topic.isRoot(),
					null);
		}
	}

	private void hideTopicList(String state) {
		hideTopicList(false, state);
	}

	private void hideTopicList(boolean delay, String state) {
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		mTopicViewOverlay.setVisibility(View.INVISIBLE);
		mTopicListVisible = false;
		changeTopicListView(new Fragment(), true, delay, state);
	}

	private void hideTopicListWithDelay() {
		mTopicListVisible = false;
		// Delay this slightly to make sure the animation is played.
		new Handler().postAtTime(new Runnable() {
			public void run() {
				hideTopicList(true, BACK_STACK_HIDDEN_STATE);
			}
		}, SystemClock.uptimeMillis() + TOPIC_LIST_HIDE_DELAY);
	}

	private void setTopicListVisible() {
		if (mTopicViewOverlay != null) {
			mTopicViewOverlay.setVisibility(View.VISIBLE);
		}
		mTopicListVisible = true;
	}

	private void changeTopicListView(Fragment fragment, boolean addToBack,
			String state) {
		changeTopicListView(fragment, addToBack, false, state);
	}

	private void changeTopicListView(Fragment fragment, boolean addToBack,
			boolean delay, String state) {
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		int inAnim, outAnim;

		if (delay) {
			inAnim = R.anim.slide_in_right_delay;
			outAnim = R.anim.slide_out_left_delay;
		} else {
			inAnim = R.anim.slide_in_right;
			outAnim = R.anim.slide_out_left;
		}

		if (mLoginVisible) {
			// login fragment is being put on the stack
			ft.setCustomAnimations(R.anim.slide_in_bottom,
					R.anim.slide_out_left, R.anim.slide_in_left,
					R.anim.slide_out_bottom);
		} else {
			ft.setCustomAnimations(inAnim, outAnim, R.anim.slide_in_left,
					R.anim.slide_out_right);
		}
		ft.replace(R.id.topic_list_fragment, fragment);

		if (addToBack) {
			ft.addToBackStack(state);
		}

		// ft.commit();

		// commitAllowingStateLoss doesn't throw an exception if commit() is
		// run after the fragments parent already saved its state. Possibly
		// fixes the IllegalStateException crash in
		// FragmentManagerImpl.checkStateLoss()
		ft.commitAllowingStateLoss();
	}

	private void toggleGalleryView(boolean showGallery) {

		if (showGallery) {
			// if some how the gallery is visible we
			// pop the stack so it wont put on the
			// stack twice
			if (mMenu != null) {
				MenuItem galleryIcon = mMenu.findItem(R.id.gallery_button);
				MenuItem guideIcon = mMenu.findItem(R.id.guides_button);
				galleryIcon.setIcon(R.drawable.ic_menu_gallery_active);
				guideIcon.setIcon(R.drawable.ic_menu_guides_inactive);
			}
			if (mGalleryVisible) {
				getSupportFragmentManager().popBackStack(BACK_STACK_STATE,
						FragmentManager.POP_BACK_STACK_INCLUSIVE);

			}
			// save state right before we add the gallery
			FragmentTransaction ft = getSupportFragmentManager()
					.beginTransaction();
			ft.addToBackStack(BACK_STACK_STATE);
			ft.commitAllowingStateLoss();
			if (mDualPane) {

				// swap out the topic fragment
				ft = getSupportFragmentManager().beginTransaction();
				ft.setCustomAnimations(R.anim.slide_in_right,
						R.anim.slide_out_left, R.anim.slide_in_left,
						R.anim.slide_out_right);
				ft.show((MediaFragment) getSupportFragmentManager()
						.findFragmentByTag(GALLERY_FRAGMENT_ID));
				ft.hide((TopicViewFragment) getSupportFragmentManager()
						.findFragmentByTag(TOPIC_VIEW_FRAGMENT_ID));
				ft.addToBackStack(null);
				ft.commitAllowingStateLoss();
				if (mHideTopicList) {
					hideTopicList(false, null);
				} else {
					ft = getSupportFragmentManager().beginTransaction();
					ft.addToBackStack(null);
					ft.commitAllowingStateLoss();
				}
				if (!mHideTopicList)
					mTopicListView.setVisibility(View.GONE);
			} else {
				// pop in the media fragment
				ft = getSupportFragmentManager().beginTransaction();
				ft.setCustomAnimations(R.anim.slide_in_right,
						R.anim.slide_out_left, R.anim.slide_in_left,
						R.anim.slide_out_right);
				ft.show((MediaFragment) getSupportFragmentManager()
						.findFragmentByTag(GALLERY_FRAGMENT_ID));
				ft.addToBackStack(null);
				ft.commitAllowingStateLoss();
			}
			// managing the bar
			mActionBar.setTitle(MediaFragment.GALLERY_TITLE);
			mActionBar.removeAllTabs();
			mGalleryVisible = true;
		} else {

			if (mDualPane) {
				getSupportFragmentManager().popBackStack(BACK_STACK_STATE,
						FragmentManager.POP_BACK_STACK_INCLUSIVE);
				if (mTopicListView.getVisibility() == View.GONE)
					mTopicListView.setVisibility(View.VISIBLE);
				mTopicView.reDisplayActionBar();
			} else {

				getSupportFragmentManager().popBackStack(BACK_STACK_STATE,
						FragmentManager.POP_BACK_STACK_INCLUSIVE);
				mActionBar.setTitle("");

			}
			if (mMenu != null) {
				MenuItem galleryIcon = mMenu.findItem(R.id.gallery_button);
				MenuItem guideIcon = mMenu.findItem(R.id.guides_button);
				galleryIcon.setIcon(R.drawable.ic_menu_gallery_inactive);
				guideIcon.setIcon(R.drawable.ic_menu_guides_active);
				mGalleryVisible = false;
			}
			// managing the bar
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			if (mGalleryVisible) {
				toggleGalleryView(false);
				return true;
			}
			getSupportFragmentManager().popBackStack();
			return true;
		case R.id.gallery_button:
			MainApplication mainApp = (MainApplication) getApplication();
			// /don't allow the anything to happin if topics havent loaded
			if (mRootTopic == null)
				return true;
			
			if (mainApp.getUser() == null) {
				if (!mLoginVisible) {
					LoginFragment fg = LoginFragment.newInstance();
					mLoginVisible = true;
					changeTopicListView(fg, true, null);
				}

			} else if (mGalleryVisible == false) {
				toggleGalleryView(true);
			}
			return true;

		case R.id.guides_button:
			if (mGalleryVisible) {
				toggleGalleryView(false);
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

		toggleGalleryView(true);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (mGalleryVisible) {
				toggleGalleryView(false);
				return true;
			}
		}
		return super.onKeyDown(keyCode, event);
	}

	public void onClick(View arg0) {
		switch (arg0.getId()) {
		case R.id.login_text:
			Log.i("TopicsActivity", "Clicked logout!");
			break;
		}
	}

	@Override
	public void onLogout() {
		if (mGalleryVisible) {
			toggleGalleryView(false);

		
				final SharedPreferences prefs = this.getSharedPreferences(LoginFragment.PREFERENCE_FILE, Context.MODE_PRIVATE);
				Editor editor = prefs.edit();
				editor.remove(LoginFragment.SESSION_KEY);
				editor.remove(LoginFragment.USERNAME_KEY);
				editor.commit();
				((MainApplication) this.getApplication()).setUser(null);
			//we will let topics activity deal with the logging out 
		}
	}

}
