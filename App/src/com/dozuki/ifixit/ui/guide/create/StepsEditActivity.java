package com.dozuki.ifixit.ui.guide.create;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import com.dozuki.ifixit.MainApplication;
import com.dozuki.ifixit.R;
import com.dozuki.ifixit.model.gallery.MediaInfo;
import com.dozuki.ifixit.model.guide.Guide;
import com.dozuki.ifixit.model.guide.GuideStep;
import com.dozuki.ifixit.model.guide.StepLine;
import com.dozuki.ifixit.ui.IfixitActivity;
import com.dozuki.ifixit.ui.gallery.GalleryActivity;
import com.dozuki.ifixit.ui.guide.view.LoadingFragment;
import com.dozuki.ifixit.util.APIError;
import com.dozuki.ifixit.util.APIEvent;
import com.dozuki.ifixit.util.APIImage;
import com.dozuki.ifixit.util.APIService;
import com.squareup.otto.Subscribe;
import com.viewpagerindicator.TitlePageIndicator;
import org.holoeverywhere.app.AlertDialog;
import org.holoeverywhere.widget.Toast;

import java.util.ArrayList;

public class StepsEditActivity extends IfixitActivity implements OnClickListener, StepChangedListener {
   public static String TAG = "StepsEditActivity";
   public static String GUIDE_STEP_KEY = "GUIDE_STEP_KEY";
   public static String MEDIA_SLOT_RETURN_KEY = "MediaSlotReturnKey";
   public static String DeleteGuideDialogKey = "DeleteGuideDialog";
   private static final String SHOWING_HELP = "SHOWING_HELP";

   private static final String IS_GUIDE_DIRTY_KEY = "IS_GUIDE_DIRTY_KEY";
   public static final String GUIDE_STEP_LIST_KEY = "GUIDE_STEP_LIST_KEY";
   private static final String SHOWING_SAVE = "SHOWING_SAVE";
   private static final String LOADING = "LOADING";

   private Guide mGuide;
   private StepEditFragment mCurStepFragment;
   private ArrayList<GuideStep> mStepList;
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
   private boolean mIsLoading;

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
      getSupportActionBar().setTitle("");

      mConfirmDelete = false;
      Bundle extras = getIntent().getExtras();
      mPagePosition = 0;
      if (extras != null) {
         mGuide = (Guide) extras.getSerializable(GuideCreateActivity.GUIDE_KEY);
         mPagePosition = extras.getInt(StepsEditActivity.GUIDE_STEP_KEY);
         mStepList = (ArrayList<GuideStep>) extras.getSerializable(GUIDE_STEP_LIST_KEY);
      }
      if (savedInstanceState != null) {
         mGuide = (Guide) savedInstanceState.getSerializable(StepsActivity.GUIDE_KEY);
         mPagePosition = savedInstanceState.getInt(StepsEditActivity.GUIDE_STEP_KEY);
         mConfirmDelete = savedInstanceState.getBoolean(DeleteGuideDialogKey);
         mIsStepDirty = savedInstanceState.getBoolean(IS_GUIDE_DIRTY_KEY);
         mShowingHelp = savedInstanceState.getBoolean(SHOWING_HELP);
         mShowingSave = savedInstanceState.getBoolean(SHOWING_SAVE);
         mStepList = (ArrayList<GuideStep>) savedInstanceState.getSerializable(GUIDE_STEP_LIST_KEY);
         mIsLoading = savedInstanceState.getBoolean(LOADING);
         if (mShowingHelp) {
            createHelpDialog().show();
         }

         if (mShowingSave) {
            createExitWarningDialog().show();
         }
      }
      setContentView(R.layout.guide_create_step_edit);
      mSaveStep = (Button) findViewById(R.id.step_edit_view_save);

      toggleSave(mIsStepDirty);

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
      if (mConfirmDelete) {
         createDeleteDialog(this).show();
      }

      getSupportActionBar().setDisplayHomeAsUpEnabled(true);

      if (mIsLoading) {
         mPager.setVisibility(View.GONE);
      }
   }

   public void showLoading() {
      if (mPager != null) {
         mPager.setVisibility(View.GONE);
      }
      getSupportFragmentManager().beginTransaction()
       .add(R.id.step_edit_loading_screen, new LoadingFragment(), "loading").addToBackStack("loading").commit();
      mIsLoading = true;

   }

   public void hideLoading() {
      if (mPager != null) {
         mPager.setVisibility(View.VISIBLE);
      }
      getSupportFragmentManager().popBackStack("loading", FragmentManager.POP_BACK_STACK_INCLUSIVE);
      mIsLoading = false;
   }

   @Subscribe
   public void onStepSave(APIEvent.StepSave event) {
      if (!event.hasError()) {
         mGuide.getSteps().get(mSavePosition).setRevisionid(event.getResult().getRevisionid());
         hideLoading();
      } else {
         event.setError(APIError.getFatalError(this));
         APIService.getErrorDialog(StepsEditActivity.this, event.getError(), null).show();
      }
   }

   @Subscribe
   public void onStepAdd(APIEvent.StepAdd event) {
      if (!event.hasError()) {
         mGuide.getSteps().get(mSavePosition).setStepid(event.getResult().getSteps().get(mSavePosition).getStepid());
         mGuide.getSteps().get(mSavePosition)
          .setRevisionid((event.getResult().getSteps().get(mSavePosition).getRevisionid()));
         mGuide.setRevisionid(event.getResult().getRevisionid());
         hideLoading();
      } else {
         event.setError(APIError.getFatalError(this));
         APIService.getErrorDialog(StepsEditActivity.this, event.getError(), null).show();
      }
   }

   @Subscribe
   public void onGuideStepDeleted(APIEvent.StepRemove event) {
      if (!event.hasError()) {
         mGuide.setRevisionid(event.getResult().getRevisionid());
         deleteStep(false);
         hideLoading();
      } else {
         event.setError(APIError.getFatalError(this));
         APIService.getErrorDialog(StepsEditActivity.this, event.getError(), null).show();
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
            APIImage mImageInfo = new APIImage();
            mImageInfo.mBaseUrl = media.getGuid();
            mImageInfo.mId = Integer.valueOf(media.getItemId());
            ArrayList<APIImage> list = mStepList.get(mPagePosition).getImages();
            /*if (requestCode == StepEditMediaFragment.IMAGE_KEY_1) {
               if (list.size() > 0) {
                  list.get(0).mBaseUrl = media.getGuid();
                  list.get(0).mId = Integer.valueOf(media.getItemId());
               } else {
                  list.add(mImageInfo);
               }
            } else if (requestCode == StepEditMediaFragment.IMAGE_KEY_2) {
               if (list.size() > 1) {
                  list.get(0).mBaseUrl = media.getGuid();
                  list.get(0).mId = Integer.valueOf(media.getItemId());
               } else {
                  list.add(mImageInfo);
               }

            } else if (requestCode == StepEditMediaFragment.IMAGE_KEY_3) {
               if (list.size() > 2) {
                  list.get(0).mBaseUrl = media.getGuid();
                  list.get(0).mId = Integer.valueOf(media.getItemId());
               } else {
                  list.add(mImageInfo);
               }
            }*/
            if (list.size() > 0) {
               list.get(0).mBaseUrl = media.getGuid();
               list.get(0).mId = Integer.valueOf(media.getItemId());
            } else {
               list.add(mImageInfo);
            }
            mStepList.get(mPagePosition).setImages(list);
            toggleSave(true);
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
      savedInstanceState.putSerializable(StepsActivity.GUIDE_KEY, mGuide);
      savedInstanceState.putBoolean(DeleteGuideDialogKey, mConfirmDelete);
      savedInstanceState.putInt(StepsEditActivity.GUIDE_STEP_KEY, mPagePosition);
      savedInstanceState.putBoolean(IS_GUIDE_DIRTY_KEY, mIsStepDirty);
      savedInstanceState.putBoolean(SHOWING_HELP, mShowingHelp);
      savedInstanceState.putBoolean(SHOWING_SAVE, mShowingSave);
      savedInstanceState.putSerializable(GUIDE_STEP_LIST_KEY, mStepList);
      savedInstanceState.putBoolean(LOADING, mIsLoading);
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
         StepEditFragment frag = new StepEditFragment();
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
            mIsStepDirty = false;
            toggleSave(mIsStepDirty);
         }
         mPagePosition = position;
         mCurStepFragment = (StepEditFragment) object;
      }
   }

   private void save(int savePosition) {
      GuideStep obj = mCurStepFragment.getGuideChanges();
      if (obj.getLines().size() == 0) {
         Toast.makeText(this, getResources().getString(R.string.guide_create_edit_must_add_line_content),
          Toast.LENGTH_SHORT).show();
         return;
      }

      for (StepLine l : obj.getLines()) {
         if (l.getTextRaw().length() == 0) {
            Toast.makeText(this, getResources().getString(R.string.guide_create_edit_must_add_line_content),
             Toast.LENGTH_SHORT).show();
            return;
         }
      }

      if (!mIsStepDirty) {
         return;
      }
      mIsStepDirty = false;

      mSavePosition = savePosition;
      for (int i = 0; i < mStepList.size(); i++) {
         mStepList.get(i).setStepNum(i);
      }
      toggleSave(mIsStepDirty);
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
               createDeleteDialog(StepsEditActivity.this).show();
            }
            break;
         case R.id.step_edit_view_save:
            save(mPagePosition);
            break;
         case R.id.step_edit_add_step:

            if ((mGuide.getSteps().size() == (mPagePosition)) && mIsStepDirty) {
               save(mPagePosition);
            } else if (mGuide.getSteps().size() < mPagePosition + 1) {
               Toast.makeText(this, getResources().getString(R.string.guide_create_edit_step_media_cannot_add_step),
                Toast.LENGTH_SHORT).show();
               return;
            }

            GuideStep item = new GuideStep(StepPortalFragment.STEP_ID++);
            item.setTitle(StepPortalFragment.DEFAULT_TITLE);
            item.addLine(new StepLine());
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

   @Override
   public boolean finishActivityIfLoggedOut() {
      return true;
   }

   public void finishEdit() {
      if (mIsStepDirty) {
         createExitWarningDialog().show();
      } else {
         finish();
      }
   }

   private void deleteStep(boolean unsaved) {
      int curStep = mPagePosition;
      mStepList.remove(mPagePosition);
      if (mPagePosition < mGuide.getSteps().size()) {
         if (!unsaved) {
            mGuide.getSteps().remove(mPagePosition);
         }
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
             mIsStepDirty = false;

             if (mPagePosition >= mGuide.getSteps().size()) {
                //step has not been saved and is at end of list
                deleteStep(true);
             } else if (mStepList.get(mPagePosition).getRevisionid() == null) {
                //step in the middle of the list and has not been saved
                deleteStep(true);
             } else {
                showLoading();
                APIService.call(StepsEditActivity.this, APIService.getRemoveStepAPICall(
                 mGuide.getGuideid(), mGuide.getRevisionid(), mGuide.getSteps().get(mPagePosition)));
             }
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
   public void onStepChanged() {
      mIsStepDirty = true;
      toggleSave(mIsStepDirty);
   }

   public void toggleSave(boolean toggle) {
      int buttonBackgroundColor = toggle ? R.color.blue : R.color.dark;
      int buttonTextColor = toggle ? R.color.white : R.color.blue;

      mSaveStep.setBackgroundColor(getResources().getColor(buttonBackgroundColor));
      mSaveStep.setTextColor(getResources().getColor(buttonTextColor));
      mSaveStep.setText(R.string.guide_create_save);
      mSaveStep.setEnabled(toggle);
   }

   private AlertDialog createHelpDialog() {
      AlertDialog.Builder builder = new AlertDialog.Builder(this);
      builder.setTitle(getString(R.string.media_help_title))
       .setMessage(getString(R.string.guide_create_edit_steps_help))
       .setPositiveButton(getString(R.string.media_help_confirm), new DialogInterface.OnClickListener() {

          public void onClick(DialogInterface dialog, int id) {
             dialog.cancel();
          }
       });

      return builder.create();
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
