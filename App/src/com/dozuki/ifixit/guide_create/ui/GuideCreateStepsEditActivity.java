package com.dozuki.ifixit.guide_create.ui;

import java.util.ArrayList;

import net.londatiga.android.ActionItem;
import net.londatiga.android.QuickAction;

import org.holoeverywhere.app.Activity;
import org.holoeverywhere.widget.ProgressBar;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import android.util.Log;
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
import com.dozuki.ifixit.guide_create.ui.GuideCreateStepEditFragmentNew.GuideStepChangedListener;
import com.viewpagerindicator.TitlePageIndicator;

public class GuideCreateStepsEditActivity extends Activity
		implements OnClickListener, GuideStepChangedListener {
	public static String TAG = "GuideCreateStepsEditActivity";
	public static String GuideKey = "GuideKey";
	public static String GUIDE_STEP_KEY = "GuideStepObject";
	public static String MEDIA_SLOT_RETURN_KEY = "MediaSlotReturnKey";
	public static String DeleteGuideDialogKey = "DeleteGuideDialog";
   private static final String SHOWING_HELP = "SHOWING_HELP";
	
	
	private static final int  NEW_STEP_ID = 1;
	private static final int DELETE_STEP_ID = 2;
   private static final String IS_GUIDE_DIRTY_KEY = "IS_GUIDE_DIRTY_KEY";
   public static final String GUIDE_STEP_LIST_KEY = "GUIDE_STEP_LIST_KEY";
	private ActionBar mActionBar;
	private GuideCreateObject mGuide;
	private GuideCreateStepEditFragmentNew mCurStepFragment;
	private ArrayList<GuideCreateStepObject> mStepList;
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
   private ProgressBar mSavingIndicator;
   private boolean mIsStepDirty;
   private boolean mShowingHelp;

	// TODO: Add "swipey tabs" to top bar

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
         mGuide = (GuideCreateObject) extras.getSerializable(GuideCreateStepsEditActivity.GuideKey);
         mPagePosition = extras.getInt(GuideCreateStepsEditActivity.GUIDE_STEP_KEY);
         mStepList = (ArrayList<GuideCreateStepObject>) extras.getSerializable(GUIDE_STEP_LIST_KEY);
      }
		if (savedInstanceState != null) {
         mGuide = (GuideCreateObject) savedInstanceState.getSerializable(GuideKey);
         mPagePosition = savedInstanceState.getInt(GuideCreateStepsEditActivity.GUIDE_STEP_KEY);
         mConfirmDelete = savedInstanceState.getBoolean(DeleteGuideDialogKey);
         mIsStepDirty = savedInstanceState.getBoolean(IS_GUIDE_DIRTY_KEY);
         mShowingHelp = savedInstanceState.getBoolean(SHOWING_HELP);
         mStepList = (ArrayList<GuideCreateStepObject>) savedInstanceState.getSerializable(GUIDE_STEP_LIST_KEY);
        // mStepAdapter.restoreState(arg0, arg1)
         
         if (mShowingHelp)
            createHelpDialog().show();
      }

      super.onCreate(savedInstanceState);

      setContentView(R.layout.guide_create_step_edit);

      mSaveStep = (Button) findViewById(R.id.step_edit_view_save);
      if (!mIsStepDirty) {
         disableSave();
      }else
      {
         enableSave();
      }
      mSpinnerMenu = (ImageView) findViewById(R.id.step_edit_spinner);
      mViewSteps = (ImageView) findViewById(R.id.step_edit_view_steps);
      mBottomBar = (RelativeLayout) findViewById(R.id.guide_create_edit_bottom_bar);
      mSavingIndicator = (ProgressBar) findViewById(R.id.step_edit_save_progress_bar);

      mStepAdapter = new StepAdapter(this.getSupportFragmentManager());
      mPager = (LockableViewPager) findViewById(R.id.guide_edit_body_pager);
      mPager.setAdapter(mStepAdapter);
      mPager.setCurrentItem(mPagePosition);
      

      titleIndicator = (TitlePageIndicator) findViewById(R.id.step_edit_top_bar);
      titleIndicator.setViewPager(mPager);
      mSaveStep.setOnClickListener(this);

      ActionItem addAction =
         new ActionItem(NEW_STEP_ID, "Add Step", getResources().getDrawable(R.drawable.ic_menu_bot_step_add));
      ActionItem delAction =
         new ActionItem(DELETE_STEP_ID, "Delete Step", getResources().getDrawable(R.drawable.ic_menu_bot_step_delete));

      mQuickAction = new QuickAction(this);
      mQuickAction.addActionItem(addAction);
      mQuickAction.addActionItem(delAction);
		mQuickAction.setOnActionItemClickListener(new QuickAction.OnActionItemClickListener() {
		   
         @Override
         public void onItemClick(QuickAction source, int pos, int actionId) {
            switch(actionId)
            {
               case NEW_STEP_ID:
                  GuideCreateStepObject item = new GuideCreateStepObject(GuideCreateStepPortalFragment.StepID++);
                  item.setTitle("Test Step " + GuideCreateStepPortalFragment.StepID);
                  mStepList.add(mPagePosition + 1, item);
                  mPager.invalidate();
                  titleIndicator.invalidate();
                  mPager.setCurrentItem(mPagePosition + 1, true);
                  break;
               case DELETE_STEP_ID:
                  if (!mStepList.isEmpty())
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
	      switch (item.getItemId()) {
	      case android.R.id.home:
	         finishEdit();
	         return true;
	      case R.id.help_button:
	         createHelpDialog().show();
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
	   public void onActivityResult(int requestCode, int resultCode, Intent data) {
	     if(mCurStepFragment != null) {
	         mCurStepFragment.setMediaResult(requestCode, resultCode, data);
	     }
	   }

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
      //getSupportFragmentManager().putFragment(savedInstanceState, "step_frag", mCurStepFragment);
		super.onSaveInstanceState(savedInstanceState);
		savedInstanceState.putSerializable(GuideCreateStepsActivity.GuideKey,
				mGuide);
		savedInstanceState.putBoolean(DeleteGuideDialogKey, mConfirmDelete);
		savedInstanceState.putInt(GuideCreateStepsEditActivity.GUIDE_STEP_KEY,
				mPagePosition);
		savedInstanceState.putBoolean(IS_GUIDE_DIRTY_KEY, mIsStepDirty);
		savedInstanceState.putBoolean(SHOWING_HELP, mShowingHelp);
		savedInstanceState.putSerializable(GUIDE_STEP_LIST_KEY, mStepList);
	}

	public class StepAdapter extends FragmentStatePagerAdapter {
		
		public StepAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public int getCount() {
			return mStepList.size();
		}

		@Override
		public CharSequence getPageTitle(int position) {
			return mStepList.get(position).getTitle();
		}

		@Override
		public Fragment getItem(int position) {
			GuideCreateStepEditFragmentNew frag = new GuideCreateStepEditFragmentNew();
			Bundle args = new Bundle();
			args.putSerializable(GUIDE_STEP_KEY, mStepList.get(position));
			frag.setArguments(args);
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
			if(mPagePosition != position) {
			   if(mIsStepDirty) {
		        save();
			   }
			   disableSave();
			   mIsStepDirty = false;
			}
			mPagePosition = position;
			mCurStepFragment = (GuideCreateStepEditFragmentNew) object;
			 Log.i(TAG, "page selected: " + mPagePosition);
		}
	}
	
	
	private void save()
	{
      disableSave();
      mSavingIndicator.setVisibility(View.VISIBLE);
      mGuide.sync(mCurStepFragment.syncGuideChanges(), mPagePosition);
      mSavingIndicator.setVisibility(View.INVISIBLE);
      mIsStepDirty = false;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.step_edit_view_steps:
		   finishEdit();
			break;
		case R.id.step_edit_view_save:
		   save();
			break;
		case R.id.step_edit_spinner:
			mQuickAction.show(v);
			break;
		case android.R.id.home:
		   finishEdit();
			break;
		}
	}

	
	public void finishEdit() {
	   if(mIsStepDirty) {
         createExitWarningDialog().show();
      } else {
         finish();
      }
	}

	private void deleteStep() {
	   int curStep = mPagePosition;
	   mStepList.remove(mPagePosition);
	   //remove from mGuide
	   if(mPagePosition < mGuide.getSteps().size()) {
	      mGuide.getSteps().remove(mPagePosition);
	   }
	   
      if(mStepList.size() == 0) {
         finish();
      }
      mStepAdapter = new StepAdapter(this.getSupportFragmentManager());
      mPager.setAdapter(mStepAdapter);
      mPager.setCurrentItem(curStep);
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
								+ mStepList.get(mPagePosition)
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

   @Override
   public void onGuideStepChanged() {
        mIsStepDirty = true;    
        enableSave();
   }
   
   public void enableSave() {
      mSaveStep.setBackgroundColor(getResources().getColor(R.color.fireswing_blue));
      mSaveStep.setEnabled(true);
   }
   
   public void disableSave() {
      mSaveStep.setBackgroundColor(getResources().getColor(R.color.fireswing_grey));
      mSaveStep.setEnabled(false);
   }
   
   private AlertDialog createHelpDialog() {
      mShowingHelp = true;
      AlertDialog.Builder builder = new AlertDialog.Builder(this);
      builder
            .setTitle(getString(R.string.media_help_title))
            .setMessage(getString(R.string.guide_create_edit_steps_help))
            .setPositiveButton(getString(R.string.media_help_confirm),
               new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int id) {
                     mShowingHelp = false;
                     dialog.cancel();
                  }
               });

      AlertDialog dialog = builder.create();
      dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
         @Override
         public void onDismiss(DialogInterface dialog) {
            mShowingHelp = false;
         }
      });

      return dialog;
   }
   
   
   
   private AlertDialog createExitWarningDialog() {
      mShowingHelp = true;
      AlertDialog.Builder builder = new AlertDialog.Builder(this);
      builder
            .setTitle(getString(R.string.guide_create_confirm_leave_without_save_title))
            .setMessage(getString(R.string.guide_create_confirm_leave_without_save_body))
            .setPositiveButton(getString(R.string.guide_create_confirm_leave_without_save_confirm),
               new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int id) {
                   //  mShowingHelp = false;
                     finish();
                     dialog.dismiss();
                  }
               }).setNegativeButton(R.string.guide_create_confirm_leave_without_save_cancel, new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int id) {
                    // mShowingHelp = false;
               
                     dialog.dismiss();
                  }
               });

      AlertDialog dialog = builder.create();
      dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
         @Override
         public void onDismiss(DialogInterface dialog) {
            mShowingHelp = false;
         }
      });

      return dialog;
   }
   
   
   
   public void enableViewPager(boolean unlocked) {
      mPager.setPagingEnabled(unlocked);
   }
   
   @Override
   public void onBackPressed() {
      finishEdit();
   }
	
}
