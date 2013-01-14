package com.dozuki.ifixit.guide_create.ui;

import java.util.ArrayList;

import org.holoeverywhere.app.Activity;

import android.content.Intent;
import android.os.Bundle;
import org.holoeverywhere.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.FragmentManager.OnBackStackChangedListener;
import android.util.Log;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.dozuki.ifixit.MainApplication;
import com.dozuki.ifixit.R;
import com.dozuki.ifixit.guide_create.model.GuideCreateObject;
import com.dozuki.ifixit.guide_create.ui.GuideIntroFragment.GuideCreateIntroListener;

public class GuideCreateActivity extends Activity implements GuideCreateIntroListener {
	static final int GUIDE_STEP_LIST_REQUEST = 0;
	private static String GuideObjectKey = "GuideCreateObject";
	public static int GuideItemID = 0;
	private ActionBar mActionBar;
	private GuidePortalFragment mGuidePortal;

	private ArrayList<GuideCreateObject> mGuideList;

	private OnBackStackChangedListener getListener() {
		OnBackStackChangedListener result = new OnBackStackChangedListener() {
			public void onBackStackChanged() {
				FragmentManager manager = getSupportFragmentManager();

				if (manager != null) {
					Log.i("GuideCreateActivity", "onbacklistenerfragment");
					Fragment currFrag = (Fragment) manager
							.findFragmentById(R.id.guide_create_fragment_container);

					currFrag.onResume();
				}
			}
		};

		return result;
	}

	public ArrayList<GuideCreateObject> getGuideList() {
		return mGuideList;
	}
	
	public void addGuide(GuideCreateObject guide)
	{
		mGuideList.add(guide);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		mGuideList = new ArrayList<GuideCreateObject>();

		setTheme(((MainApplication) getApplication()).getSiteTheme());
		getSupportActionBar().setTitle(
				((MainApplication) getApplication()).getSite().mTitle);
		mActionBar = getSupportActionBar();
		mActionBar.setTitle("");

		if (savedInstanceState != null) {
			mGuideList = (ArrayList<GuideCreateObject>) savedInstanceState
					.getSerializable(GuideObjectKey);
		}
		
		super.onCreate(savedInstanceState);

		setContentView(R.layout.guide_create);

		getSupportFragmentManager()
				.addOnBackStackChangedListener(getListener());
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		String tag = "guide_portal_fragment";
		if (findViewById(R.id.guide_create_fragment_container) != null && getSupportFragmentManager().findFragmentByTag(tag) == null) {	
			mGuidePortal = new GuidePortalFragment();
		//	mGuidePortal.setRetainInstance(true);
			getSupportFragmentManager().beginTransaction()
					.add(R.id.guide_create_fragment_container, mGuidePortal, tag)
					.commit();
		}
	}

   @Override
   public boolean onCreateOptionsMenu(Menu menu) {

      MenuInflater inflater = getSupportMenuInflater();
      inflater.inflate(R.menu.guide_create_menu, menu);

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
		savedInstanceState.putSerializable(GuideObjectKey, mGuideList);
		super.onSaveInstanceState(savedInstanceState);
	}
	
	public void createGuide() {
		if (mGuideList == null)
			return;
		
		launchGuideCreateIntro();
	}
	
	private void launchGuideCreateIntro()
	{

		String tag = "guide_intro_fragment";
		GuideIntroFragment newFragment = new GuideIntroFragment();
		newFragment.setGuideOBject(null);
		FragmentTransaction transaction =  getSupportFragmentManager().beginTransaction();
		transaction.replace(R.id.guide_create_fragment_container, newFragment);
		transaction.addToBackStack(tag);
		transaction.commitAllowingStateLoss();	
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == GUIDE_STEP_LIST_REQUEST) {
			if (resultCode == RESULT_OK) {
				GuideCreateObject guide = (GuideCreateObject) data
						.getSerializableExtra(GuideCreateStepsEditActivity.GuideKey);
				if (guide != null) {
					mGuideList.set(mGuideList.indexOf(guide),guide);				
				}
			}
		}
	}

   @Override
   public void onFinishIntroInput(String device, String title, String summary, String intro, String guideType,
      String thing) {
      
      GuideCreateObject guideObject =  new GuideCreateObject(GuideItemID++);
      guideObject.setTitle(title);
      guideObject.setTopic(device);
      guideObject.setSummary(summary);
      guideObject.setIntroduction(intro);
      
      getGuideList().add(guideObject);
      
     // APIService.call((Activity) getActivity(),
      //   APIService.getCreateGuideAPICall(device, title, summary, intro, guideType, thing));

      getSupportFragmentManager().popBackStack();
      
   }
}
