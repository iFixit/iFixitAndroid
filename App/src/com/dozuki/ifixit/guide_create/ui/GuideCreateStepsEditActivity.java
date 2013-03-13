package com.dozuki.ifixit.guide_create.ui;

import java.util.ArrayList;

import com.dozuki.ifixit.guide_create.model.UserGuide;
import com.dozuki.ifixit.guide_view.ui.LoadingFragment;
import com.dozuki.ifixit.util.APIEvent;
import com.dozuki.ifixit.util.APIService;
import com.squareup.otto.Subscribe;
import org.holoeverywhere.app.Activity;
import org.holoeverywhere.app.AlertDialog;
import org.holoeverywhere.widget.ProgressBar;
import org.holoeverywhere.widget.Toast;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RelativeLayout;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.dozuki.ifixit.MainApplication;
import com.dozuki.ifixit.R;
import com.dozuki.ifixit.gallery.model.MediaInfo;
import com.dozuki.ifixit.gallery.ui.GalleryActivity;
import com.dozuki.ifixit.guide_create.model.GuideCreateObject;
import com.dozuki.ifixit.guide_create.model.GuideCreateStepObject;
import com.dozuki.ifixit.guide_create.ui.GuideCreateStepEditFragment.GuideStepChangedListener;
import com.dozuki.ifixit.guide_view.model.StepImage;
import com.dozuki.ifixit.guide_view.model.StepLine;
import com.dozuki.ifixit.util.IfixitActivity;
import com.viewpagerindicator.TitlePageIndicator;

public class GuideCreateStepsEditActivity extends IfixitActivity implements OnClickListener, GuideStepChangedListener {
   public static String TAG = "GuideCreateStepsEditActivity";
   public static String GUIDE_STEP_KEY = "GUIDE_STEP_KEY";
   public static String MEDIA_SLOT_RETURN_KEY = "MediaSlotReturnKey";
   public static String DeleteGuideDialogKey = "DeleteGuideDialog";
   private static final String SHOWING_HELP = "SHOWING_HELP";

   private static final String IS_GUIDE_DIRTY_KEY = "IS_GUIDE_DIRTY_KEY";
   public static final String GUIDE_STEP_LIST_KEY = "GUIDE_STEP_LIST_KEY";
   private static final String SHOWING_SAVE = "SHOWING_SAVE";

   private ActionBar mActionBar;
   private GuideCreateObject mGuide;
   private GuideCreateStepEditFragment mCurStepFragment;
   private ArrayList<GuideCreateStepObject> mStepList;
   private ImageButton mAddStepButton;
   private Button mSaveStep;
   private ImageButton mDeleteStepButton;
   private StepAdapter mStepAdapter;
   private LockableViewPager mPager;
   private TitlePageIndicator titleIndicator;
   private RelativeLayout mBottomBar;
   private int mPagePosition;
   private int mSavePosition;
   private boolean mConfirmDelete;
   private boolean mIsStepDirty;
   private boolean mShowingHelp;
   private boolean mShowingSave;

   @SuppressWarnings("unchecked")
   public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);

      /**
       * lock screen for small sizes to portrait.
       * Courtesy:
       * http://stackoverflow.com/questions/10491531/android-restrict-activity-orientation-based-on-screen-size
       **/
      if (isScreenLarge()) {
         setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
      } else {
         setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
      }

      setTheme(((MainApplication) getApplication()).getSiteTheme());
      getSupportActionBar().setTitle(((MainApplication) getApplication()).getSite().mTitle);
      mActionBar = getSupportActionBar();
      mActionBar.setTitle("");
      mConfirmDelete = false;
      Bundle extras = getIntent().getExtras();
      mPagePosition = 0;
      if (extras != null) {
         mGuide = (GuideCreateObject) extras.getSerializable(GuideCreateActivity.GUIDE_KEY);
         mPagePosition = extras.getInt(GuideCreateStepsEditActivity.GUIDE_STEP_KEY);
         mStepList = (ArrayList<GuideCreateStepObject>) extras.getSerializable(GUIDE_STEP_LIST_KEY);
      }
      if (savedInstanceState != null) {
         mGuide = (GuideCreateObject) savedInstanceState.getSerializable(GuideCreateStepsActivity.GUIDE_KEY);
         mPagePosition = savedInstanceState.getInt(GuideCreateStepsEditActivity.GUIDE_STEP_KEY);
         mConfirmDelete = savedInstanceState.getBoolean(DeleteGuideDialogKey);
         mIsStepDirty = savedInstanceState.getBoolean(IS_GUIDE_DIRTY_KEY);
         mShowingHelp = savedInstanceState.getBoolean(SHOWING_HELP);
         mShowingSave = savedInstanceState.getBoolean(SHOWING_SAVE);
         mStepList = (ArrayList<GuideCreateStepObject>) savedInstanceState.getSerializable(GUIDE_STEP_LIST_KEY);
         if (mShowingHelp) {
            createHelpDialog().show();
         }

         if (mShowingSave) {
            createExitWarningDialog().show();
         }
      }
      setContentView(R.layout.guide_create_step_edit);
      mSaveStep = (Button) findViewById(R.id.step_edit_view_save);
      if (!mIsStepDirty) {
         disableSave();
      } else {
         enableSave();
      }
      mAddStepButton = (ImageButton) findViewById(R.id.step_edit_add_step);
      mDeleteStepButton = (ImageButton) findViewById(R.id.step_edit_delete_step);
      mBottomBar = (RelativeLayout) findViewById(R.id.guide_create_edit_bottom_bar);

      mStepAdapter = new StepAdapter(this.getSupportFragmentManager());
      mPager = (LockableViewPager) findViewById(R.id.guide_edit_body_pager);
      mPager.setAdapter(mStepAdapter);
      mPager.setCurrentItem(mPagePosition);

      titleIndicator = (TitlePageIndicator) findViewById(R.id.step_edit_top_bar);
      titleIndicator.setViewPager(mPager);
      mSaveStep.setOnClickListener(this);
      mAddStepButton.setOnClickListener(this);
      mDeleteStepButton.setOnClickListener(this);
      if (mConfirmDelete)
         createDeleteDialog(this).show();

      getSupportActionBar().setDisplayHomeAsUpEnabled(true);
   }

   public void showLoading() {
      if (mPager != null) {
         mPager.setVisibility(View.GONE);
      }
      getSupportFragmentManager().beginTransaction()
         .add(R.id.step_edit_loading_screen, new LoadingFragment(), "loading").addToBackStack("loading").commit();
   }

   public void hideLoading() {
      if (mPager != null) {
         mPager.setVisibility(View.VISIBLE);
      }
      getSupportFragmentManager().popBackStack("loading", FragmentManager.POP_BACK_STACK_INCLUSIVE);
   }

   @Subscribe
   public void onStepSave(APIEvent.StepSave event) {
      if (!event.hasError()) {
         hideLoading();
         mIsStepDirty = false;
      } else {
         // TODO handle errors hideLoading();
      }
   }

   @Subscribe
   public void onStepAdd(APIEvent.StepAdd event) {
      if (!event.hasError()) {
         mIsStepDirty = false;
         mGuide.getSteps().get(mSavePosition).setStepId(event.getResult().getSteps().get(mSavePosition).getStepId());
         mGuide.getSteps().get(mSavePosition)
            .setRevisionid((event.getResult().getSteps().get(mSavePosition).getRevisionid()));
         hideLoading();
      } else {
         // TODO handle errors hideLoading();
      }
   }

   @Subscribe
   public void onGuideStepDeleted(APIEvent.StepRemove event) {
      if (!event.hasError()) {
         mIsStepDirty = false;
         deleteStep();
         hideLoading();
      } else {
         // todo error
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
      returnIntent.putExtra(GuideCreateActivity.GUIDE_KEY, mGuide);
      setResult(RESULT_OK, returnIntent);
      super.finish();
   }

   @Override
   public void onActivityResult(int requestCode, int resultCode, Intent data) {
      if (mCurStepFragment != null) {
         mCurStepFragment.setMediaResult(requestCode, resultCode, data);
      } else {
         if (resultCode == RESULT_OK) {
            // we dont have a reference the the fragment managing the media, so we make the changes to the step manually
            MediaInfo media = (MediaInfo) data.getSerializableExtra(GalleryActivity.MEDIA_RETURN_KEY);
            StepImage mImageInfo = new StepImage(0);
            mImageInfo.setText(media.getGuid());
            mImageInfo.setImageId(Integer.valueOf(media.getItemId()));
            ArrayList<StepImage> list = mStepList.get(mPagePosition).getImages();
            if (requestCode == GuideCreateEditMediaFragment.IMAGE_KEY_1) {
               if (list.size() > 0) {
                  list.get(0).setText(media.getGuid());
                  list.get(0).setImageId(Integer.valueOf(media.getItemId()));
               } else {
                  list.add(mImageInfo);
               }
            } else if (requestCode == GuideCreateEditMediaFragment.IMAGE_KEY_2) {
               if (list.size() > 1) {
                  list.get(1).setText(media.getGuid());
                  list.get(1).setImageId(Integer.valueOf(media.getItemId()));
               } else {
                  list.add(mImageInfo);
               }

            } else if (requestCode == GuideCreateEditMediaFragment.IMAGE_KEY_3) {
               if (list.size() > 2) {
                  list.get(2).setText(media.getGuid());
                  list.get(2).setImageId(Integer.valueOf(media.getItemId()));
               } else {
                  list.add(mImageInfo);
               }
            }
            mStepList.get(mPagePosition).setImages(list);
            enableSave();
            // recreate pager with updated step:
            mStepAdapter = new StepAdapter(this.getSupportFragmentManager());
            mPager.setAdapter(mStepAdapter);
            mPager.invalidate();
            titleIndicator.invalidate();
            mPager.setCurrentItem(mPagePosition, false);
         }
      }
   }

   @Override
   public void onSaveInstanceState(Bundle savedInstanceState) {
      super.onSaveInstanceState(savedInstanceState);
      savedInstanceState.putSerializable(GuideCreateStepsActivity.GUIDE_KEY, mGuide);
      savedInstanceState.putBoolean(DeleteGuideDialogKey, mConfirmDelete);
      savedInstanceState.putInt(GuideCreateStepsEditActivity.GUIDE_STEP_KEY, mPagePosition);
      savedInstanceState.putBoolean(IS_GUIDE_DIRTY_KEY, mIsStepDirty);
      savedInstanceState.putBoolean(SHOWING_HELP, mShowingHelp);
      savedInstanceState.putBoolean(SHOWING_SAVE, mShowingSave);
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
         return "Step " + (position + 1);
      }

      @Override
      public Fragment getItem(int position) {
         GuideCreateStepEditFragment frag = new GuideCreateStepEditFragment();
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
      public void setPrimaryItem(ViewGroup container, int position, Object object) {
         super.setPrimaryItem(container, position, object);

         if (mPagePosition != position) {
            /** keyboard */
            final InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(container.getWindowToken(), 0);
            if (mIsStepDirty) {
               save(mPagePosition);
            }
            disableSave();
            mIsStepDirty = false;
         }
         mPagePosition = position;
         mCurStepFragment = (GuideCreateStepEditFragment) object;
      }
   }

   private void save(int savePosition) {
      mSavePosition = savePosition;
      for (int i = 0; i < mStepList.size(); i++) {
         mStepList.get(i).setStepNum(i);
      }
      disableSave();
      mGuide.sync(mCurStepFragment.getGuideChanges(), mSavePosition);
      showLoading();
      if (mGuide.getSteps().get(mSavePosition).getRevisionid() != null) {
         APIService
            .call(this, APIService.getEditStepAPICall(mGuide.getSteps().get(mSavePosition), mGuide.getGuideid()));
      } else {
         APIService.call(this, APIService.getAddStepAPICall(mGuide.getSteps().get(mSavePosition), mGuide.getGuideid(),
                 mPagePosition + 1, mGuide.getRevisionid()));
      }
   }


   @Override
   public void onClick(View v) {
      switch (v.getId()) {
         case R.id.step_edit_delete_step:
            if (!mStepList.isEmpty()) {
               createDeleteDialog(GuideCreateStepsEditActivity.this).show();
            }
            break;
         case R.id.step_edit_view_save:
            save(mPagePosition);
            break;
         case R.id.step_edit_add_step:

            if ((mGuide.getSteps().size() == (mPagePosition)) && mIsStepDirty) {
               // a convenience for the user
               // TODO: see if it can work with the API
               save(mPagePosition);
            } else if (mGuide.getSteps().size() < mPagePosition + 1) {
               Toast.makeText(this, getResources().getString(R.string.guide_create_edit_step_media_cannot_add_step),
                  Toast.LENGTH_SHORT).show();
               return;
            }

            GuideCreateStepObject item = new GuideCreateStepObject(GuideCreateStepPortalFragment.STEP_ID++);
            item.setTitle(GuideCreateStepPortalFragment.DEFAULT_TITLE);
            item.addLine(new StepLine(null, "black", 0, ""));
            item.setStepNum(mPagePosition + 1);
            mStepList.add(mPagePosition + 1, item);
            int pos = mPagePosition;
            for (int i = 0; i < mStepList.size(); i++) {
               mStepList.get(i).setStepNum(i);
            }
            // The view pager does not recreate the item in the current position unless we force it to:
            mStepAdapter = new StepAdapter(this.getSupportFragmentManager());
            mPager.setAdapter(mStepAdapter);
            mPager.invalidate();
            titleIndicator.invalidate();

            mPager.setCurrentItem(pos + 1, false);
            break;
         case android.R.id.home:
            finishEdit();
            break;
      }
   }

   public void finishEdit() {
      if (mIsStepDirty) {
         createExitWarningDialog().show();
      } else {
         finish();
      }
   }

   private void deleteStep() {
      int curStep = mPagePosition;
      mStepList.remove(mPagePosition);
      if (mPagePosition < mGuide.getSteps().size()) {
         mGuide.getSteps().remove(mPagePosition);
      }

      if (mStepList.size() == 0) {
         finish();
      }

      for (int i = 0; i < mStepList.size(); i++) {
         mStepList.get(i).setStepNum(i);
      }
      // The view pager does not recreate the item in the current position unless we force it to:
      mStepAdapter = new StepAdapter(this.getSupportFragmentManager());
      mPager.setAdapter(mStepAdapter);
      mPager.setCurrentItem(curStep);
      mPager.invalidate();
      titleIndicator.invalidate();

   }

   public void invalidateStepAdapter() {
      mStepAdapter.notifyDataSetChanged();
   }

   public AlertDialog createDeleteDialog(final Context context) {
      mConfirmDelete = true;
      AlertDialog.Builder builder = new AlertDialog.Builder(context);
      builder
         .setTitle(context.getString(R.string.step_edit_confirm_delete_title))
         .setMessage(
            context.getString(R.string.step_edit_confirm_delete_message) + " Step "
               + (mStepList.get(mPagePosition).getStepNum() + 1) + "?")
         .setPositiveButton(context.getString(R.string.logout_confirm), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
               mConfirmDelete = false;
               showLoading();
               APIService.call(
                       GuideCreateStepsEditActivity.this,
                       APIService.getRemoveStepAPICall(mGuide.getGuideid(), mGuide.getRevisionid(),
                               mGuide.getSteps().get(mPagePosition)));
               dialog.cancel();


            }
         }).setNegativeButton(R.string.logout_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
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

   public int getIndicatorHeight() {
      return titleIndicator.getHeight() + mBottomBar.getHeight();

   }

   @Override
   public void onGuideStepChanged() {
      mIsStepDirty = true;
      enableSave();
   }

   public void enableSave() {
      mSaveStep.setBackgroundColor(getResources().getColor(R.color.fireswing_blue));
      mSaveStep.setTextColor(getResources().getColor(R.color.white));
      mSaveStep.setText(R.string.guide_create_save);
      mSaveStep.setEnabled(true);
   }

   public void disableSave() {
      mSaveStep.setBackgroundColor(getResources().getColor(R.color.dark));
      mSaveStep.setTextColor(getResources().getColor(R.color.fireswing_disabled));
      mSaveStep.setText(R.string.guide_create_saved);
      mSaveStep.setEnabled(false);
   }

   private AlertDialog createHelpDialog() {
      mShowingHelp = true;
      AlertDialog.Builder builder = new AlertDialog.Builder(this);
      builder.setTitle(getString(R.string.media_help_title))
         .setMessage(getString(R.string.guide_create_edit_steps_help))
         .setPositiveButton(getString(R.string.media_help_confirm), new DialogInterface.OnClickListener() {

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
      mShowingSave = true;
      AlertDialog.Builder builder = new AlertDialog.Builder(this);
      builder
         .setTitle(getString(R.string.guide_create_confirm_leave_without_save_title))
         .setMessage(getString(R.string.guide_create_confirm_leave_without_save_body))
         .setNegativeButton(getString(R.string.guide_create_confirm_leave_without_save_confirm),
            new DialogInterface.OnClickListener() {
               public void onClick(DialogInterface dialog, int id) {
                  save(mPagePosition);
                  dialog.dismiss();
                  finish();
               }
            })
         .setPositiveButton(R.string.guide_create_confirm_leave_without_save_cancel,
            new DialogInterface.OnClickListener() {

               public void onClick(DialogInterface dialog, int id) {
                  dialog.dismiss();
                  finish();
               }
            });

      AlertDialog dialog = builder.create();
      dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
         @Override
         public void onDismiss(DialogInterface dialog) {
            mShowingSave = false;
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

   public boolean isScreenLarge() {
      final int screenSize = getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK;
      return screenSize == Configuration.SCREENLAYOUT_SIZE_LARGE
         || screenSize == Configuration.SCREENLAYOUT_SIZE_XLARGE;
   }
}
