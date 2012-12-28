package com.dozuki.ifixit.gallery.ui;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AnimationUtils;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.ActionMode;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.dozuki.ifixit.MainApplication;
import com.dozuki.ifixit.R;
import com.dozuki.ifixit.gallery.ui.PhotoMediaFragment.ModeCallback;
import com.dozuki.ifixit.guide_create.ui.GuideCreateStepEditFragment;
import com.dozuki.ifixit.guide_create.ui.GuideCreateStepsEditActivity.StepAdapter;
import com.dozuki.ifixit.login.model.LoginListener;
import com.dozuki.ifixit.login.model.User;
import com.dozuki.ifixit.login.ui.LoginFragment;
import com.dozuki.ifixit.util.APIEndpoint;
import com.dozuki.ifixit.util.APIError;
import com.dozuki.ifixit.util.APIReceiver;
import com.viewpagerindicator.TitlePageIndicator;

public class GalleryActivity extends SherlockFragmentActivity implements
		LoginListener, OnClickListener {

	public static final String MEDIA_FRAGMENT_PHOTOS = "MEDIA_FRAGMENT_PHOTOS";
	public static final String MEDIA_FRAGMENT_VIDEOS = "MEDIA_FRAGMENT_VIDEOS";
	public static final String MEDIA_FRAGMENT_EMBEDS = "MEDIA_FRAGMENT_EMBEDS";

	private static final String LOGIN_VISIBLE = "LOGIN_VISIBLE";
	private static final String LOGIN_FRAGMENT = "LOGIN_FRAGMENT";

	private static final String SHOWING_HELP = "SHOWING_HELP";
	private static final String SHOWING_LOGOUT = "SHOWING_LOGOUT";
	private static final String SHOWING_DELETE = "SHOWING_DELETE";

	public static boolean showingLogout;
	public static boolean showingHelp;
	public static boolean showingDelete;

	private ActionBar mActionBar;
	private boolean mLoginVisible;
	private boolean mIconsHidden;
	
	private HashMap<String, MediaFragment> mMediaCategoryFragments;
	private MediaFragment mCurrentMediaFragment;
	
	private StepAdapter mStepAdapter;
	private ViewPager mPager;
	private TitlePageIndicator titleIndicator;
	private RelativeLayout mButtons;
	private TextView mLoginText;
	private String mUserName;
	public TextView noImagesText;
	
	private boolean mGetMediaItemForReturn;
	private int mMediaReturnValue;
	private ActionMode mMode;

	private APIReceiver mApiReceiver = new APIReceiver() {
		public void onSuccess(Object result, Intent intent) {
			/**
			 * The success are handled by the media fragment. This is here to
			 * catch if the user has an invalid session.
			 */
		}

		public void onFailure(APIError error, Intent intent) {
			if (error.mType == APIError.ErrorType.INVALID_USER) {
				LoginFragment editNameDialog = new LoginFragment();
				editNameDialog.show(getSupportFragmentManager(), "");
			}
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		setTheme(((MainApplication) getApplication()).getSiteTheme());
		getSupportActionBar().setTitle(
				((MainApplication) getApplication()).getSite().mTitle);

		mActionBar = getSupportActionBar();
		mActionBar.setTitle("");

		mMediaCategoryFragments = new HashMap<String, MediaFragment>();
		mMediaCategoryFragments.put(MEDIA_FRAGMENT_PHOTOS, new PhotoMediaFragment());
		mMediaCategoryFragments.put(MEDIA_FRAGMENT_VIDEOS, new VideoMediaFragment());
		mMediaCategoryFragments.put(MEDIA_FRAGMENT_EMBEDS, new EmbedMediaFragment());
		mCurrentMediaFragment = mMediaCategoryFragments.get(MEDIA_FRAGMENT_PHOTOS);
		
		showingHelp = false;
		showingLogout = false;
		showingDelete = false;
		
		mGetMediaItemForReturn = false;
		mMediaReturnValue = -1;
		mMode = null;
		
		if(getIntent().getExtras() != null)
		{
			Bundle bundle = getIntent().getExtras();
			mMediaReturnValue = bundle.getInt(GuideCreateStepEditFragment.ThumbPositionKey, -1);
			if(mMediaReturnValue != -1)
				mGetMediaItemForReturn = true;
			mMode = startActionMode(new ContextualMediaSelect(this));
		}

		if (savedInstanceState != null) {
			showingHelp = savedInstanceState.getBoolean(SHOWING_HELP);
			if (showingHelp)
				createHelpDialog(this).show();
			showingLogout = savedInstanceState.getBoolean(SHOWING_LOGOUT);
			if (showingLogout)
				LoginFragment.getLogoutDialog(this).show();
			showingDelete = savedInstanceState.getBoolean(SHOWING_DELETE);
			/*
			 * if (showingDelete) { createDeleteConfirmDialog(this).show(); }
			 */
		}

		super.onCreate(savedInstanceState);

		setContentView(R.layout.gallery_root);
		mButtons = (RelativeLayout) findViewById(R.id.button_holder);
		mLoginText = ((TextView) findViewById(R.id.login_text));
		mStepAdapter = new StepAdapter(this.getSupportFragmentManager());
		mPager = (ViewPager) findViewById(R.id.gallery_view_body_pager);
		mPager.setAdapter(mStepAdapter);
		titleIndicator = (TitlePageIndicator) findViewById(R.id.gallery_view_top_bar);
		titleIndicator.setViewPager(mPager);
		mPager.setCurrentItem(1);

		/*
		 * mMediaView = (MediaFragment) getSupportFragmentManager()
		 * .findFragmentById(R.id.gallery_view_fragment);
		 * 
		 * mMediaView.noImagesText.setVisibility(View.GONE);
		 */
		LoginFragment mLogin = (LoginFragment) getSupportFragmentManager()
				.findFragmentByTag(LOGIN_FRAGMENT);

		User user = ((MainApplication) getApplication())
				.getUserFromPreferenceFile();

		if (user != null) {
			mIconsHidden = false;
			supportInvalidateOptionsMenu();
		} else {
			mIconsHidden = true;
			if (mLogin == null) {
				displayLogin();
			}
		}

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
	}

	@Override
	public void onStart() {
		if (!((MainApplication) this.getApplication()).isUserLoggedIn()) {
			mButtons.setVisibility(View.GONE);
		} else {
			mUserName = ((MainApplication) (this).getApplication()).getUser()
					.getUsername();
			mLoginText.setText(this.getString(R.string.logged_in_as) + " "
					+ mUserName);
			mButtons.setOnClickListener(this);

		}

		super.onStart();
	}
	
	

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.button_holder:
			showingLogout = true;
			LoginFragment.getLogoutDialog(this).show();
			break;
		}
	}

	private void displayLogin() {
		mIconsHidden = true;
		supportInvalidateOptionsMenu();
		LoginFragment editNameDialog = new LoginFragment();
		editNameDialog.show(getSupportFragmentManager(), LOGIN_FRAGMENT);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putBoolean(LOGIN_VISIBLE, mLoginVisible);
		outState.putBoolean(SHOWING_HELP, showingHelp);
		outState.putBoolean(SHOWING_LOGOUT, showingLogout);
		outState.putBoolean(SHOWING_DELETE, showingDelete);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		boolean isLoggedIn = ((MainApplication) getApplication())
				.isUserLoggedIn();
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			return true;
		case R.id.top_camera_button:
			if (!isLoggedIn) {
				return false;
			}
			mCurrentMediaFragment.launchCamera();
			return true;
		case R.id.top_gallery_button:
			if (!isLoggedIn) {
				return false;
			}
			mCurrentMediaFragment.launchGallery();
			return true;
		case R.id.top_question_button:
			if (!isLoggedIn) {
				return false;
			}
			createHelpDialog(this).show();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onLogin(User user) {
		mIconsHidden = false;
		supportInvalidateOptionsMenu();
		mMediaCategoryFragments.get(MEDIA_FRAGMENT_PHOTOS).clearMediaList();
		mUserName = ((MainApplication) (this).getApplication()).getUser()
				.getUsername();
		mLoginText.setText(getString(R.string.logged_in_as) + " " + mUserName);
		mButtons.setOnClickListener(this);
		mMediaCategoryFragments.get(MEDIA_FRAGMENT_PHOTOS).retrieveUserMedia();
		mButtons.setVisibility(View.VISIBLE);
		mButtons.setAnimation(AnimationUtils.loadAnimation(this,
				R.anim.slide_in_bottom));

		if (((MainApplication) getApplication()).isFirstTimeGalleryUser()) {
			createHelpDialog(this).show();
			((MainApplication) getApplication()).setFirstTimeGalleryUser(false);
		}
	}

	@Override
	public void onLogout() {
		((MainApplication) getApplication()).logout();
		finish();
	}

	@Override
	public void onCancel() {
		finish();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		if (!mIconsHidden) {
			MenuInflater inflater = getSupportMenuInflater();
			inflater.inflate(R.menu.gallery_menu, menu);
		}

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public void onResume() {
		IntentFilter filter = new IntentFilter();
		filter.addAction(APIEndpoint.USER_IMAGES.mAction);
		filter.addAction(APIEndpoint.UPLOAD_IMAGE.mAction);
		filter.addAction(APIEndpoint.DELETE_IMAGE.mAction);
		registerReceiver(mApiReceiver, filter);
		super.onResume();
	}

	@Override
	public void onPause() {
		try {
			unregisterReceiver(mApiReceiver);
		} catch (IllegalArgumentException e) {
		}
		super.onPause();
	}

	public class StepAdapter extends FragmentStatePagerAdapter {

		public StepAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public int getCount() {
			return mMediaCategoryFragments.size();
		}

		@Override
		public CharSequence getPageTitle(int position) {
			switch (position) {
			case 0:
				return "Videos";
			case 1:
				return "Photos";
			case 2:
				return "Embeds";
			default:
				return "Photos";
			}	
		}

		@Override
		public Fragment getItem(int position) {
			switch (position) {
			case 0:
				return (VideoMediaFragment)mMediaCategoryFragments.get(MEDIA_FRAGMENT_VIDEOS);
			case 1:
				return (PhotoMediaFragment)mMediaCategoryFragments.get(MEDIA_FRAGMENT_PHOTOS);
			case 2:
				return (EmbedMediaFragment)mMediaCategoryFragments.get(MEDIA_FRAGMENT_EMBEDS);
			default:
				return (PhotoMediaFragment)mMediaCategoryFragments.get(MEDIA_FRAGMENT_PHOTOS);
			}	
		}

		@Override
		public void setPrimaryItem(ViewGroup container, int position,
				Object object) {
			super.setPrimaryItem(container, position, object);
			// mPagePosition = position;
			mCurrentMediaFragment = (MediaFragment) object;
			// Log.i(TAG, "page selected: " + mPagePosition);
		}
	}

	public static AlertDialog createHelpDialog(final Context context) {
		showingHelp = true;
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setTitle(context.getString(R.string.media_help_title))
				.setMessage(context.getString(R.string.media_help_messege))
				.setPositiveButton(
						context.getString(R.string.media_help_confirm),
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								showingHelp = false;
								dialog.cancel();
							}
						});

		AlertDialog dialog = builder.create();
		dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
			@Override
			public void onDismiss(DialogInterface dialog) {
				showingHelp = false;
			}
		});

		return dialog;
	}
	
	public final class ContextualMediaSelect implements ActionMode.Callback {
		private Context mParentContext;

		public ContextualMediaSelect(Context parentContext) {
			mParentContext = parentContext;
		}

		@Override
		public boolean onCreateActionMode(ActionMode mode, Menu menu) {
			// Create the menu from the xml file
			//MenuInflater inflater = getSupportMenuInflater();
			//inflater.inflate(R.menu.contextual_delete, menu);
			return true;
		}

		@Override
		public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
			return false;
		}

		@Override
		public void onDestroyActionMode(ActionMode mode) {
			finish();
		}

		@Override
		public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
			

			return true;
		}
	};

}
