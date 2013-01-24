package com.dozuki.ifixit.util;

import android.app.ActionBar;
import android.app.ActivityManager;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.SpinnerAdapter;

import com.actionbarsherlock.app.ActionBar.OnNavigationListener;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.dozuki.ifixit.MainApplication;
import com.dozuki.ifixit.R;
import com.dozuki.ifixit.gallery.ui.GalleryActivity;
import com.dozuki.ifixit.guide_create.ui.GuideCreateActivity;
import com.dozuki.ifixit.login.model.LoginEvent;
import com.dozuki.ifixit.login.ui.LogoutDialog;
import com.dozuki.ifixit.topic_view.ui.TopicsActivity;
import com.squareup.otto.Subscribe;

import org.holoeverywhere.app.Activity;

/**
 * Base Activity that performs various functions that all Activities in this app
 * should do. Such as:
 * 
 * Registering for the event bus. Setting the current site's theme. Finishing
 * the Activity if the user logs out but the Activity requires authentication.
 * Displaying various menu icons.
 */
public abstract class IfixitActivity extends Activity {
	/**
	 * This is incredibly hacky. The issue is that Otto does not search for @Subscribed
	 * methods in parent classes because the performance hit is far too big for
	 * Android because of the deep inheritance with the framework and views.
	 * Because of this
	 * 
	 * @Subscribed methods on IfixitActivity itself don't get registered. The
	 *             workaround is to make an anonymous object that is registered
	 *             on behalf of the parent class.
	 * 
	 *             Workaround courtesy of:
	 *             https://github.com/square/otto/issues/26
	 * 
	 *             Note: The '@SuppressWarnings("unused")' is to prevent
	 *             warnings that are incorrect (the methods *are* actually used.
	 */
	private Object loginEventListener = new Object() {
		@SuppressWarnings("unused")
		@Subscribe
		public void onLogin(LoginEvent.Login event) {
			// Update menu icons.
			supportInvalidateOptionsMenu();
		}

		@SuppressWarnings("unused")
		@Subscribe
		public void onLogout(LoginEvent.Logout event) {
			finishActivityIfPermissionDenied();
			// Update menu icons.
			supportInvalidateOptionsMenu();
		}

		@SuppressWarnings("unused")
		@Subscribe
		public void onCancel(LoginEvent.Cancel event) {
			finishActivityIfPermissionDenied();
		}
	};

	@Override
	public void onCreate(Bundle savedState) {
		/**
		 * Set the current site's theme. Must be before onCreate because of
		 * inflating views.
		 */
		setTheme(MainApplication.get().getSiteTheme());
		super.onCreate(savedState);
		
	}

	/**
	 * If the user is coming back to this Activity make sure they still have
	 * permission to view it. onRestoreInstanceState is for Activities that are
	 * being recreated and onRestart is for Activities who are merely being
	 * restarted. Unfortunately both are needed.
	 */
	@Override
	public void onRestoreInstanceState(Bundle savedState) {
		super.onRestoreInstanceState(savedState);
		finishActivityIfPermissionDenied();
	}

	@Override
	public void onRestart() {
		super.onRestart();
		finishActivityIfPermissionDenied();
	}

	@Override
	public void onResume() {
		super.onResume();

		// Invalidate the options menu in case the user has logged in or out.
		supportInvalidateOptionsMenu();

		MainApplication.getBus().register(this);
		MainApplication.getBus().register(loginEventListener);
	}

	@Override
	public void onPause() {
		super.onPause();

		MainApplication.getBus().unregister(this);
		MainApplication.getBus().unregister(loginEventListener);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent intent = null;
		switch (item.getItemId()) {
		case R.id.gallery_button:
			intent = new Intent(this, GalleryActivity.class);
			startActivity(intent);
			return true;
		case R.id.logout_button:
			LogoutDialog.create(this).show();
			return true;
	
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	
	public void prepareNavigationSpinner(com.actionbarsherlock.app.ActionBar actionbar)
	{
		  actionbar.setTitle(MainApplication.get().getSiteDisplayTitle());
		  actionbar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
		  SpinnerAdapter mSpinnerAdapter = ArrayAdapter.createFromResource(this, R.array.action_list,
		          android.R.layout.simple_spinner_dropdown_item);
		  OnNavigationListener mOnNavigationListener = new OnNavigationListener() {
			   //String[] strings = getResources().getStringArray(R.array.action_list);
			   @Override
			   public boolean onNavigationItemSelected(int position, long itemId) {
				 onSpinnerItemSelected(position);
			     return true;
			   }
			 };
		  actionbar.setListNavigationCallbacks(mSpinnerAdapter, mOnNavigationListener);
	}

   public void onSpinnerItemSelected(int position) {
      Intent intent = null;
      switch (position) {
      // view
         case 0:
            if (Build.VERSION.SDK_INT > 10 && TopicsActivity.TASK_ID != -1) {
               ActivityManager activityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
               activityManager.moveTaskToFront(TopicsActivity.TASK_ID, ActivityManager.MOVE_TASK_WITH_HOME);
               return;
            }
            intent = new Intent(this, TopicsActivity.class);
            startActivity(intent);
            return;
         case 1:
            if (Build.VERSION.SDK_INT > 10 && GuideCreateActivity.TASK_ID != -1) {
               ActivityManager activityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
               activityManager.moveTaskToFront(GuideCreateActivity.TASK_ID, ActivityManager.MOVE_TASK_WITH_HOME);
               return;
            }
            intent = new Intent(this, GuideCreateActivity.class);
            startActivity(intent);
            return;
      }
   }

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		MenuItem logout = menu.findItem(R.id.logout_button);

		if (logout != null) {
			logout.setVisible(MainApplication.get().isUserLoggedIn());
		}

		return super.onPrepareOptionsMenu(menu);
	}

	/**
	 * Finishes the Activity if the user should be logged in but isn't.
	 */
	private void finishActivityIfPermissionDenied() {
		MainApplication app = MainApplication.get();

		/**
		 * Never finish if user is logged in or is logging in.
		 */
		if (app.isUserLoggedIn() || app.isLoggingIn()) {
			return;
		}

		/**
		 * Finish if the site is private or activity requires authentication.
		 */
		if (!neverFinishActivityOnLogout()
				&& (finishActivityIfLoggedOut() || !app.getSite().mPublic)) {
			finish();
		}
	}

	/**
	 * "Settings" methods for derived classes are found below. Decides when to
	 * finish the Activity, what icons to display etc.
	 */

	/**
	 * Return true if the gallery launcher should be in the options menu.
	 */
	public boolean showGalleryIcon() {
		return true;
	}

	/**
	 * Returns true if the Activity should be finished if the user logs out or
	 * cancels authentication.
	 */
	public boolean finishActivityIfLoggedOut() {
		return false;
	}

	/**
	 * Returns true if the Activity should never be finished despite meeting
	 * other conditions.
	 * 
	 * This exists because of a race condition of sorts involving logging out of
	 * private Dozuki sites. SiteListActivity can't reset the current site to
	 * one that is public so it is erroneously finished unless flagged
	 * otherwise.
	 */
	public boolean neverFinishActivityOnLogout() {
		return false;
	}
}
