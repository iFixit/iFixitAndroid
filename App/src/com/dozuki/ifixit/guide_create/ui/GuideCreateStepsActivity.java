package com.dozuki.ifixit.guide_create.ui;

import java.util.ArrayList;

import org.holoeverywhere.app.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.SubMenu;
import com.dozuki.ifixit.MainApplication;
import com.dozuki.ifixit.R;
import com.dozuki.ifixit.gallery.ui.GalleryActivity;
import com.dozuki.ifixit.guide_create.model.GuideCreateObject;
import com.dozuki.ifixit.guide_create.model.GuideCreateStepObject;
import com.dozuki.ifixit.guide_create.ui.GuideIntroFragment.GuideCreateIntroListener;
import com.dozuki.ifixit.topic_view.ui.TopicsActivity;

public class GuideCreateStepsActivity extends Activity implements GuideCreateIntroListener {
	static final int GUIDE_EDIT_STEP_REQUEST = 0;
	public static String GuideKey = "GuideKey";
	private ActionBar mActionBar;
	private GuideCreateStepPortalFragment mStepPortalFragment;
	private ArrayList<GuideCreateStepObject> mStepList;
	private GuideCreateObject mGuide;

	public ArrayList<GuideCreateStepObject> getStepList() {
		return mStepList;
	}

	public void deleteStep(GuideCreateStepObject step) {
		mStepList.remove(step);
	}

	public void addStep(GuideCreateStepObject step, int index) {
		mStepList.add(index, step);
	}

	public GuideCreateObject getGuide() {
		return mGuide;
	}

	@SuppressWarnings("unchecked")
	public void onCreate(Bundle savedInstanceState) {
		setTheme(((MainApplication) getApplication()).getSiteTheme());
		getSupportActionBar().setTitle(
				((MainApplication) getApplication()).getSite().mTitle);
		mActionBar = getSupportActionBar();
		mActionBar.setTitle("");

		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			mGuide = (GuideCreateObject) extras
					.getSerializable(GuideCreateStepsActivity.GuideKey);
			if (mGuide.getSteps() == null)
				mGuide.setStepList(new ArrayList<GuideCreateStepObject>());
			mStepList = mGuide.getSteps();
		} else if (savedInstanceState != null) {
			mStepList = mGuide.getSteps();
		}

		super.onCreate(savedInstanceState);

		setContentView(R.layout.guide_create_steps_root);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		String tag = "guide_steeps_portal_fragment";
		if (findViewById(R.id.guide_create_fragment_steps_container) != null
				&& getSupportFragmentManager().findFragmentByTag(tag) == null) {
			mStepPortalFragment = new GuideCreateStepPortalFragment(mGuide);
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
      inflater.inflate(R.menu.step_create_menu, menu);

      return super.onCreateOptionsMenu(menu);
   }

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override 
	public void onSaveInstanceState(Bundle savedInstanceState) {
		super.onSaveInstanceState(savedInstanceState);
		savedInstanceState.putSerializable(GuideCreateStepsActivity.GuideKey,
				mGuide);
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
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == GUIDE_EDIT_STEP_REQUEST) {
			if (resultCode == RESULT_OK) {
				GuideCreateObject guide = (GuideCreateObject) data
						.getSerializableExtra(GuideCreateStepsEditActivity.GuideKey);
				if (guide != null) {
					mGuide = guide;
					mStepList = mGuide.getSteps();
				}
			}
		}
	}
	
	
	  
   @Override
   public void onFinishIntroInput(String device, String title, String summary, String intro, String guideType,
      String thing) {
      
      mGuide.setTitle(title);
      mGuide.setTopic(device);
      mGuide.setSummary(summary);
      mGuide.setIntroduction(intro);
            
      //TODO PATCH GUIDE
     // APIService.call((Activity) getActivity(),
      //   APIService.getCreateGuideAPICall(device, title, summary, intro, guideType, thing));

      getSupportFragmentManager().popBackStack();
      
   }
}
