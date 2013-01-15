package com.dozuki.ifixit.guide_create.ui;

import java.util.ArrayList;


import net.londatiga.android.ActionItem;
import net.londatiga.android.QuickAction;

import org.holoeverywhere.app.Activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;

import android.widget.ImageView;


import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.dozuki.ifixit.MainApplication;
import com.dozuki.ifixit.R;
import com.dozuki.ifixit.guide_create.model.GuideCreateObject;
import com.dozuki.ifixit.guide_create.model.GuideCreateStepObject;
import com.viewpagerindicator.TitlePageIndicator;

public class GuideCreateStepsEditActivity extends Activity
		implements OnClickListener {
	public static String TAG = "GuideCreateStepsEditActivity";
	public static String GuideKey = "GuideKey";
	public static String GUIDE_STEP_KEY = "GuideStepObject";
	public static String MEDIA_SLOT_RETURN_KEY = "MediaSlotReturnKey";
	public static String DeleteGuideDialogKey = "DeleteGuideDialog";
	private static final int  NEW_STEP_ID = 1;
	private static final int DELETE_STEP_ID = 2;
	private ActionBar mActionBar;
	private GuideCreateObject mGuide;
	private GuideCreateStepEditFragmentNew mCurStepFragment;
	//private TextView mAddStep;
	//private TextView mDeleteStep;
	private ImageView mSpinnerMenu;
	private Button mSaveStep;
	private ImageView mViewSteps;
	private StepAdapter mStepAdapter;
	private LockableViewPager mPager;
	private TitlePageIndicator titleIndicator;
	RelativeLayout mBottomBar;
	private int mPagePosition;
	private boolean mConfirmDelete;
   private QuickAction mQuickAction;

	// TODO: Add "swipey tabs" to top bar

	@SuppressWarnings("unchecked")
	public void onCreate(Bundle savedInstanceState) {
		setTheme(((MainApplication) getApplication()).getSiteTheme());
		getSupportActionBar().setTitle(
				((MainApplication) getApplication()).getSite().mTitle);
		mActionBar = getSupportActionBar();
		mActionBar.setTitle("");
		mConfirmDelete = false;
		Bundle extras = getIntent().getExtras();
		mPagePosition = 0;
		if (extras != null) {
			mGuide = (GuideCreateObject) extras
					.getSerializable(GuideCreateStepsEditActivity.GuideKey);
			mPagePosition = extras
					.getInt(GuideCreateStepsEditActivity.GUIDE_STEP_KEY);
		}else if (savedInstanceState != null) {
			mGuide = (GuideCreateObject) savedInstanceState
					.getSerializable(GuideKey);
			mPagePosition = savedInstanceState
					.getInt(GuideCreateStepsEditActivity.GUIDE_STEP_KEY);
			mConfirmDelete = savedInstanceState
					.getBoolean(DeleteGuideDialogKey);
		}

		super.onCreate(savedInstanceState);

		setContentView(R.layout.guide_create_step_edit);
		//mAddStep = (TextView) findViewById(R.id.step_edit_add_step);

		//mDeleteStep = (TextView) findViewById(R.id.step_edit_delete_step);

		mSaveStep = (Button) findViewById(R.id.step_edit_view_save);
		mSpinnerMenu = (ImageView) findViewById(R.id.step_edit_spinner);
		mViewSteps = (ImageView) findViewById(R.id.step_edit_view_steps);
		mBottomBar = (RelativeLayout)findViewById(R.id.guide_create_edit_bottom_bar);

		mStepAdapter = new StepAdapter(this.getSupportFragmentManager());
		mPager = (LockableViewPager) findViewById(R.id.guide_edit_body_pager);
		mPager.setAdapter(mStepAdapter);
		mPager.setCurrentItem(mPagePosition);

		titleIndicator = (TitlePageIndicator) findViewById(R.id.step_edit_top_bar);
		titleIndicator.setViewPager(mPager);
		mSaveStep.setOnClickListener(this);
		
		ActionItem addAction = new ActionItem(NEW_STEP_ID, "Add Step", getResources().getDrawable(R.drawable.ic_menu_bot_step_add));
		
	
		ActionItem delAction = new ActionItem(DELETE_STEP_ID, "Delete Step", getResources().getDrawable(R.drawable.ic_menu_bot_step_delete)); 

	   mQuickAction  = new QuickAction(this);
		 
		mQuickAction.addActionItem(addAction);
		mQuickAction.addActionItem(delAction);
	
		//setup the action item click listener
		mQuickAction.setOnActionItemClickListener(new QuickAction.OnActionItemClickListener() {
		   
         @Override
         public void onItemClick(QuickAction source, int pos, int actionId) {
            switch(actionId)
            {
               case NEW_STEP_ID:
                  GuideCreateStepObject item = new GuideCreateStepObject(GuideCreateStepPortalFragment.StepID++);
                  item.setTitle("Test Step " + GuideCreateStepPortalFragment.StepID);
                  mGuide.getSteps().add(mPagePosition + 1, item);
                  mPager.invalidate();
                  titleIndicator.invalidate();
                  mPager.setCurrentItem(mPagePosition + 1, true);
                  break;
               case DELETE_STEP_ID:
                  if (!mGuide.getSteps().isEmpty())
                     createDeleteDialog(GuideCreateStepsEditActivity.this).show();
                  break;
            }
            
         }
		});

		mSpinnerMenu.setOnClickListener(this);

		mViewSteps.setOnClickListener(this);

		if (mConfirmDelete)
			createDeleteDialog(this).show();

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
	}

	 @Override
	   public boolean onCreateOptionsMenu(Menu menu) {

	      MenuInflater inflater = getSupportMenuInflater();
	      inflater.inflate(R.menu.step_create_menu, menu);

	      return super.onCreateOptionsMenu(menu);
	   }

	   @Override
	   public boolean onOptionsItemSelected(MenuItem item) {
	      Intent intent;
	      switch (item.getItemId()) {
	      case android.R.id.home:
	         finish();
	         return true;
	      default:
	         return super.onOptionsItemSelected(item);
	      }
	   }

	@Override
	public void finish() {
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
		savedInstanceState.putBoolean(DeleteGuideDialogKey, mConfirmDelete);
		savedInstanceState.putInt(GuideCreateStepsEditActivity.GUIDE_STEP_KEY,
				mPagePosition);
	}

	public class StepAdapter extends FragmentStatePagerAdapter {

		private boolean isPagingEnabled;
		
		public StepAdapter(FragmentManager fm) {
			super(fm);
			isPagingEnabled = true;
		}

		@Override
		public int getCount() {
			return mGuide.getSteps().size();
		}

		@Override
		public CharSequence getPageTitle(int position) {
			return mGuide.getSteps().get(position).getTitle();
		}

		@Override
		public Fragment getItem(int position) {
			GuideCreateStepEditFragmentNew frag = new GuideCreateStepEditFragmentNew();
			Bundle args = new Bundle();
			args.putSerializable(GUIDE_STEP_KEY, mGuide.getSteps().get(position));
			frag.setArguments(args);
			mCurStepFragment = frag;
			return frag;
		}
		
		@Override
		public int getItemPosition(Object object) {
		    return POSITION_NONE;
		}

		@Override
		public void setPrimaryItem(ViewGroup container, int position,
				Object object) {
			super.setPrimaryItem(container, position, object);
			mPagePosition = position;
			// Log.i(TAG, "page selected: " + mPagePosition);
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		/*case R.id.step_edit_add_step:
			GuideCreateStepObject item = new GuideCreateStepObject(
					GuideCreateStepPortalFragment.StepID++);
			item.setTitle("Test Step " + GuideCreateStepPortalFragment.StepID);
			mGuide.getSteps().add(mPagePosition + 1, item);
			mPager.invalidate();
			titleIndicator.invalidate();
			mPager.setCurrentItem(mPagePosition + 1, true);
			break;
		case R.id.step_edit_delete_step:
			if (!mGuide.getSteps().isEmpty())
				createDeleteDialog(this).show();
			break;*/
		case R.id.step_edit_view_steps:
			finish();
			break;
		case R.id.step_edit_view_save:
			//
		   mCurStepFragment.syncGuideChanges();
			break;
		case R.id.step_edit_spinner:
			mQuickAction.show(v);
			break;
		}
	}

	private void deleteStep() {
		mGuide.getSteps().remove(mPagePosition);
		mPager.invalidate();
		titleIndicator.invalidate();
	}
	
	public void invalidateStepAdapter()
	{
		mStepAdapter.notifyDataSetChanged();
	}

	public AlertDialog createDeleteDialog(final Context context) {
		mConfirmDelete = true;
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setTitle(
				context.getString(R.string.step_edit_confirm_delete_title))
				.setMessage(
						context.getString(R.string.step_edit_confirm_delete_message)
								+ " "
								+ mGuide.getSteps().get(mPagePosition)
										.getTitle() + "?")
				.setPositiveButton(context.getString(R.string.logout_confirm),
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int id) {
								mConfirmDelete = false;
								deleteStep();
								dialog.cancel();
							}
						})
				.setNegativeButton(R.string.logout_cancel,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								mConfirmDelete = false;
								dialog.cancel();
							}
						});

		AlertDialog dialog = builder.create();
		dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
			@Override
			public void onDismiss(DialogInterface dialog) {
				mConfirmDelete = false;
			}
		});

		return dialog;
	}

	int getIndicatorHeight()
	{
      return titleIndicator.getHeight() + mBottomBar.getHeight();
	   
	}
}
