package com.dozuki.ifixit.view.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import com.dozuki.ifixit.MainApplication;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.SubMenu;
import com.dozuki.ifixit.R;
import com.dozuki.ifixit.util.APIService;
import com.dozuki.ifixit.view.model.LoginListener;
import com.dozuki.ifixit.view.model.TopicNode;
import com.dozuki.ifixit.view.model.User;

public class GalleryActivity extends
		SherlockFragmentActivity implements LoginListener {

	private static final String LOGIN_VISIBLE = "LOGIN_VISIBLE";
	private static final String LOGIN_FRAGMENT = "LOGIN_FRAGMENT";
	private static final String FIRST_TIME_USER = "FIRST_TIME_USER";
	private String GALLERY_FRAGMENT = "GALLERY_FRAGMENT";
	private MediaFragment mMediaView;
	private FrameLayout mTopicViewOverlay;

	private boolean mDualPane;
	private ActionBar mActionBar;
	private boolean mLoginVisible;
	private View mLoginView;

	private MenuItem galleryIcon;
	private MenuItem cameraIcon;
	private MenuItem helpIcon;
	boolean iconsHidden;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// setTheme(((MainApplication)getApplication()).getSiteTheme());
		// getSupportActionBar().setTitle(((MainApplication)getApplication())
		// .getSite().mTitle);

		mActionBar = getSupportActionBar();
		mActionBar.setTitle("");

		super.onCreate(savedInstanceState);

		setContentView(R.layout.gallery);

		mMediaView = (MediaFragment) getSupportFragmentManager()
				.findFragmentById(
						R.id.gallery_view_fragment);
		mLoginView = findViewById(R.id.login_fragment);
		LoginFragment mLogin = (LoginFragment) getSupportFragmentManager()
				.findFragmentByTag(LOGIN_FRAGMENT);
		// mTopicViewOverlay = (FrameLayout)
		// findViewById(R.id.gallery_view_overlay);
		// mHideTopicList = mTopicViewOverlay != null;
		mDualPane = mTopicViewOverlay != null;

		SharedPreferences preferenceFile;
		if (((MainApplication) this.getApplication())
				.getUser() == null) {
			preferenceFile = this.getSharedPreferences(
					LoginFragment.PREFERENCE_FILE,
					MODE_PRIVATE);
			User user = new User();
			String session = preferenceFile.getString(
					LoginFragment.SESSION_KEY, null);
			String username = preferenceFile.getString(
					LoginFragment.USERNAME_KEY, null);
			if (session != null) {
				user.setSession(session);
				user.setUsername(username);
				mLoginView.setVisibility(View.INVISIBLE);
				((MainApplication) this.getApplication())
						.setUser(user);
				mMediaView.onLogin(user);
				iconsHidden = false;
				supportInvalidateOptionsMenu();
			} else {
				iconsHidden = true;
				if (mLogin == null) {
					displayLogin();
				}
			}
		} else {
			mMediaView.onLogin(((MainApplication) this
					.getApplication()).getUser());
		}

		LoginFragment.clearLoginListeners();
		LoginFragment.registerOnLoginListener(this);
		LoginFragment.registerOnLoginListener(mMediaView);

		getSupportActionBar().setDisplayHomeAsUpEnabled(
				true);

	}

	private void displayLogin() {

		//hideMenuBarIcons();
		iconsHidden = true;
		supportInvalidateOptionsMenu();

		FragmentTransaction ft = getSupportFragmentManager()
				.beginTransaction();
		int inAnim, outAnim;
		LoginFragment fg = new LoginFragment();
		;
		// mLoginVisible = true;

		inAnim = R.anim.fade_in;
		outAnim = R.anim.fade_out;

		ft.setCustomAnimations(inAnim, outAnim);
		if (mDualPane) {
			ft.add(R.id.login_fragment, fg, LOGIN_FRAGMENT);
		} else {
			ft.add(R.id.login_fragment, fg, LOGIN_FRAGMENT);
		}
		ft.addToBackStack(null);
		ft.commitAllowingStateLoss();

	}

	@Override
	public void onResume() {
		super.onResume();
		if (((MainApplication) this.getApplication())
				.getUser() == null)
			hideMenuBarIcons();
	}

	@Override
	public void onPause() {
		super.onPause();

		try {

		} catch (IllegalArgumentException e) {
			// Do nothing. This happens in the unlikely event that
			// unregisterReceiver has been called already.
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putBoolean(LOGIN_VISIBLE, mLoginVisible);

	}

	public void onBackStackChanged() {

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			return true;
		case R.id.top_camera_button:
			if (((MainApplication) this.getApplication())
					.getUser() == null)
				return false;
			mMediaView.launchCamera();
			return true;
		case R.id.top_gallery_button:
			if (((MainApplication) this.getApplication())
					.getUser() == null)
				return false;
			mMediaView.launchGallery();
			return true;
		case R.id.top_question_button:
			if (((MainApplication) this.getApplication())
					.getUser() == null)
				return false;
			mMediaView.createHelpDialog(this).show();
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
		iconsHidden = false;
		supportInvalidateOptionsMenu();
		
		getSupportFragmentManager().popBackStack();
		mLoginView.setVisibility(View.INVISIBLE);

		SharedPreferences preferenceFile = this
				.getSharedPreferences(
						LoginFragment.PREFERENCE_FILE,
						MODE_PRIVATE);
		boolean firstTimeUser = preferenceFile.getBoolean(
				FIRST_TIME_USER, true);

		if (firstTimeUser) {
			mMediaView.createHelpDialog(this).show();
			Editor e = preferenceFile.edit();
			e.putBoolean(FIRST_TIME_USER, false);
			e.commit();
		}

	}

	@Override
	public void onLogout() {

		final SharedPreferences prefs = this
				.getSharedPreferences(
						LoginFragment.PREFERENCE_FILE,
						Context.MODE_WORLD_READABLE);
		Editor editor = prefs.edit();
		editor.remove(LoginFragment.SESSION_KEY);
		editor.remove(LoginFragment.USERNAME_KEY);
		editor.commit();
		((MainApplication) this.getApplication())
				.setUser(null);
		finish();

	}

	@Override
	public boolean onCreateOptionsMenu(
			com.actionbarsherlock.view.Menu menu) {
		 if(!iconsHidden)
		 {
		com.actionbarsherlock.view.MenuInflater inflater = getSupportMenuInflater();
		inflater.inflate(R.menu.gallery_menu, menu);
		 }//else
		// {

		//galleryIcon = menu
		///		.findItem(R.id.top_gallery_button);
		//cameraIcon = menu.findItem(R.id.top_camera_button);
		//helpIcon = menu.findItem(R.id.top_question_button);

        //if(iconsHidden)
		 //  supportInvalidateOptionsMenu();
        //else*/
        	
        	
        	
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			this.finish();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	public void hideMenuBarIcons() {
		if (galleryIcon != null)
			galleryIcon.setVisible(false);
		if (cameraIcon != null)
			cameraIcon.setVisible(false);
		if (helpIcon != null)
			helpIcon.setVisible(false);

	}

	public void showMenuBarIcons() {

		if (galleryIcon != null) {
			galleryIcon.setVisible(true);
			// galleryIcon.invalidate();
		}
		if (cameraIcon != null) {
			cameraIcon.setVisible(true);
		}
		if (helpIcon != null) {
			helpIcon.setVisible(true);
		}
		
	}

}
