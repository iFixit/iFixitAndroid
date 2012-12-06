package com.dozuki.ifixit.guide_create.ui;

import java.util.ArrayList;

import android.content.Intent;
import android.os.Bundle;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.SubMenu;
import com.dozuki.ifixit.MainApplication;
import com.dozuki.ifixit.R;
import com.dozuki.ifixit.gallery.ui.GalleryActivity;
import com.dozuki.ifixit.guide_create.model.GuideCreateObject;
import com.dozuki.ifixit.guide_create.model.GuideCreateStepObject;
import com.dozuki.ifixit.topic_view.ui.TopicsActivity;

public class GuideCreateStepsActivity extends SherlockFragmentActivity {
	public static String GuideKey = "GuideKey";
	private static String GuideStepKey = "GuideStepObject";
	private ActionBar mActionBar;
	private GuideCreateStepPortalFragment mStepPortalFragment;
	private ArrayList<GuideCreateStepObject> mStepList;

	public ArrayList<GuideCreateStepObject> getStepList() {
		return mStepList;
	}

	@SuppressWarnings("unchecked")
	public void onCreate(Bundle savedInstanceState) {
		mStepList = new ArrayList<GuideCreateStepObject>();
		setTheme(((MainApplication) getApplication()).getSiteTheme());
		getSupportActionBar().setTitle(
				((MainApplication) getApplication()).getSite().mTitle);
		mActionBar = getSupportActionBar();
		mActionBar.setTitle("");

		if (savedInstanceState != null) {
			mStepList = (ArrayList<GuideCreateStepObject>) savedInstanceState
					.getSerializable(GuideStepKey);
		}

		super.onCreate(savedInstanceState);

		setContentView(R.layout.guide_create_steps_root);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		String tag = "guide_steeps_portal_fragment";
		if (findViewById(R.id.guide_create_fragment_steps_container) != null
				&& getSupportFragmentManager().findFragmentByTag(tag) == null) {
			mStepPortalFragment = new GuideCreateStepPortalFragment();
			mStepPortalFragment.setRetainInstance(true);
			getSupportFragmentManager()
					.beginTransaction()
					.add(R.id.guide_create_fragment_steps_container,
							mStepPortalFragment, tag).commit();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getSupportMenuInflater();
		SubMenu subMenu = menu.addSubMenu("");
		subMenu.setIcon(R.drawable.ic_menu_spinner);
		inflater.inflate(R.menu.menu_bar, subMenu);

		MenuItem subMenuItem = subMenu.getItem();
		subMenuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS
				| MenuItem.SHOW_AS_ACTION_WITH_TEXT);

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent intent;
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			return true;
		case R.id.gallery_button:
			intent = new Intent(this, GalleryActivity.class);
			startActivity(intent);
			return true;
		case R.id.my_guides_button:
			return true;
		case R.id.browse_button:
			intent = new Intent(this, TopicsActivity.class);
			startActivity(intent);
			return true;
		case R.id.new_step_button:
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		super.onSaveInstanceState(savedInstanceState);
		savedInstanceState.putSerializable(GuideStepKey, mStepList);
	}
}
