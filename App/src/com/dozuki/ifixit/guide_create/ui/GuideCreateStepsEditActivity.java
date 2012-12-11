package com.dozuki.ifixit.guide_create.ui;

import java.util.ArrayList;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.ListFragment;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

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

public class GuideCreateStepsEditActivity extends SherlockFragmentActivity {
	public static String GuideKey = "GuideKey";
	public static String GuideStepKey = "GuideStepObject";
	private ActionBar mActionBar;
	private GuideCreateObject mGuide;

	private StepAdapter mStepAdapter;
	private ViewPager mPager;

	//TODO: Add "swipey tabs" to top bar
	
	@SuppressWarnings("unchecked")
	public void onCreate(Bundle savedInstanceState) {
		setTheme(((MainApplication) getApplication()).getSiteTheme());
		getSupportActionBar().setTitle(
				((MainApplication) getApplication()).getSite().mTitle);
		mActionBar = getSupportActionBar();
		mActionBar.setTitle("");

		Bundle extras = getIntent().getExtras();
		int startStep = 0;
		if (extras != null) {

			mGuide = (GuideCreateObject) extras
					.getSerializable( GuideCreateStepsEditActivity.GuideKey);
			startStep = extras.getInt( GuideCreateStepsEditActivity.GuideStepKey);
		}

		if (savedInstanceState != null) {
			mGuide = (GuideCreateObject) savedInstanceState
					.getSerializable(GuideKey);
		}

		super.onCreate(savedInstanceState);

		setContentView(R.layout.guide_create_step_edit);

		mStepAdapter = new StepAdapter(this.getSupportFragmentManager());
		mPager = (ViewPager) findViewById(R.id.guide_edit_body_pager);
		mPager.setAdapter(mStepAdapter);
		mPager.setCurrentItem(startStep);

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
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
	public void finish()
	{
		Intent returnIntent = new Intent();
		returnIntent.putExtra(GuideCreateStepsEditActivity.GuideKey, mGuide);
		setResult(RESULT_OK, returnIntent);
		super.finish();
	}

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		super.onSaveInstanceState(savedInstanceState);
		savedInstanceState.putSerializable(GuideCreateStepsActivity.GuideKey,
				mGuide);
	}

	public class StepAdapter extends FragmentStatePagerAdapter {

		public StepAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public int getCount() {
			return mGuide.getSteps().size();
		}

		@Override
		public Fragment getItem(int position) {
			GuideCreateStepEditFragment frag = new GuideCreateStepEditFragment();
			frag.setStepObject(mGuide.getSteps().get(position));
			return frag;
		}

	}
}
