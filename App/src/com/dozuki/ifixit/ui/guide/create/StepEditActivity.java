package com.dozuki.ifixit.ui.guide.create;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.Toast;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.dozuki.ifixit.MainApplication;
import com.dozuki.ifixit.R;
import com.dozuki.ifixit.model.APIImage;
import com.dozuki.ifixit.model.gallery.MediaInfo;
import com.dozuki.ifixit.model.guide.Guide;
import com.dozuki.ifixit.model.guide.GuideStep;
import com.dozuki.ifixit.model.guide.StepLine;
import com.dozuki.ifixit.ui.BaseActivity;
import com.dozuki.ifixit.ui.gallery.GalleryActivity;
import com.dozuki.ifixit.ui.guide.view.GuideViewActivity;
import com.dozuki.ifixit.util.APIError;
import com.dozuki.ifixit.util.APIEvent;
import com.dozuki.ifixit.util.APIService;
import com.squareup.otto.Subscribe;
import com.viewpagerindicator.TitlePageIndicator;

import java.util.ArrayList;
import java.util.Iterator;

public class StepEditActivity extends BaseActivity implements OnClickListener {
   public static final int MENU_VIEW_GUIDE = 12;
   private static final int STEP_VIEW = 1;
   private static final int FOR_RESULT = 2;
   private static final int HOME_UP = 3;

   public static final String EXIT_CODE = "EXIT_CODE_KEY";
   public static final String GUIDE_PUBLIC_KEY = "GUIDE_PUBLIC_KEY";

   public static String TAG = "StepEditActivity";
   public static String GUIDE_STEP_NUM_KEY = "GUIDE_STEP_NUM_KEY";
   public static String MEDIA_SLOT_RETURN_KEY = "MediaSlotReturnKey";
   public static String DELETE_GUIDE_DIALOG_KEY = "DeleteGuideDialog";
   public static final String GUIDE_ID_KEY = "GUIDE_ID_KEY";
   public static final String GUIDE_STEP_ID = "GUIDE_STEP_ID";
   public static final String PARENT_GUIDE_ID_KEY = "PARENT_GUIDE_ID_KEY";
   public static final int NO_PARENT_GUIDE = -1;

   private static final String SHOWING_HELP = "SHOWING_HELP";

   private static final String IS_GUIDE_DIRTY_KEY = "IS_GUIDE_DIRTY_KEY";
   private static final String SHOWING_SAVE = "SHOWING_SAVE";
   private static final String LOCK_SAVE = "LOCK_SAVE";
   private static final String LOADING = "LOADING";

   private Guide mGuide;
   private StepEditFragment mCurStepFragment;
   private ImageButton mAddStepButton;
   private Button mSaveStep;
   private ImageButton mDeleteStepButton;
   private StepAdapter mStepAdapter;
   private LockableViewPager mPager;
   private TitlePageIndicator titleIndicator;
   private RelativeLayout mBottomBar;
   private int mPagePosition;
   private int mSavePosition;

   // Necessary for editing prerequisite guides from the view interface in order to navigate back to the parent guide.
   private int mParentGuideId = NO_PARENT_GUIDE;

   // Used to navigate to the correct step when coming from GuideViewActivity.
   private int mInboundStepId;

   private boolean mConfirmDelete;
   private boolean mIsStepDirty;
   private boolean mShowingHelp;
   private boolean mShowingSave;
   private boolean mIsLoading;

   // Flag to prevent saving a guide while we're waiting for an image to upload and return
   private boolean mLockSave;

   private int mExitCode;
   private boolean mGuidePublic;

   private static int mLoadingContainer = R.id.step_edit_loading_screen;


   /////////////////////////////////////////////////////
   // LIFECYCLE
   /////////////////////////////////////////////////////

   @Override
   public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);

      /**
       * lock screen for small sizes to portrait.
       * Courtesy:
       * http://stackoverflow.com/questions/10491531/android-restrict-activity-orientation-based-on-screen-size
       **/
      if (MainApplication.get().isScreenLarge()) {
         setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
      } else {
         setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
      }

      mConfirmDelete = false;
      Bundle extras = getIntent().getExtras();
      mPagePosition = 0;
      if (extras != null) {
         mGuide = (Guide) extras.getSerializable(GuideCreateActivity.GUIDE_KEY);
         mPagePosition = extras.getInt(GUIDE_STEP_NUM_KEY, 0);

         if (mGuide == null) {
            int guideid = extras.getInt(GUIDE_ID_KEY);
            mGuidePublic = extras.getBoolean(GUIDE_PUBLIC_KEY);
            mParentGuideId = extras.getInt(PARENT_GUIDE_ID_KEY, NO_PARENT_GUIDE);
            mInboundStepId = extras.getInt(GUIDE_STEP_ID);

            APIService.call(StepEditActivity.this, APIService.getGuideForEditAPICall(guideid));
            showLoading(mLoadingContainer);
         } else {
            mGuidePublic = mGuide.isPublic();
         }
      }

      if (savedInstanceState != null) {
         mGuide = (Guide) savedInstanceState.getSerializable(StepsActivity.GUIDE_KEY);

         if (mGuide != null) {
            mGuidePublic = mGuide.isPublic();
         }

         mPagePosition = savedInstanceState.getInt(GUIDE_STEP_NUM_KEY);
         mConfirmDelete = savedInstanceState.getBoolean(DELETE_GUIDE_DIALOG_KEY);
         mIsStepDirty = savedInstanceState.getBoolean(IS_GUIDE_DIRTY_KEY);
         mShowingHelp = savedInstanceState.getBoolean(SHOWING_HELP);
         mShowingSave = savedInstanceState.getBoolean(SHOWING_SAVE);
         mLockSave = savedInstanceState.getBoolean(LOCK_SAVE);
         mIsLoading = savedInstanceState.getBoolean(LOADING);
         mExitCode = savedInstanceState.getInt(EXIT_CODE);
         if (mShowingHelp) {
            createHelpDialog().show();
         }

         if (mShowingSave) {
            createExitWarningDialog(mExitCode).show();
         }
      }

      setContentView(R.layout.guide_create_step_edit);

      mSaveStep = (Button) findViewById(R.id.step_edit_save);
      toggleSave(mIsStepDirty);

      if (mGuide != null) {
         initPage(mPagePosition);
      }
   }

   private void initPage(int startPage) {
      getSupportActionBar().setTitle(mGuide.getTitle());

      mAddStepButton = (ImageButton) findViewById(R.id.step_edit_add_step);
      mDeleteStepButton = (ImageButton) findViewById(R.id.step_edit_delete_step);
      mBottomBar = (RelativeLayout) findViewById(R.id.guide_create_edit_bottom_bar);

      mStepAdapter = new StepAdapter(this.getSupportFragmentManager());
      mPager = (LockableViewPager) findViewById(R.id.guide_edit_body_pager);
      mPager.setAdapter(mStepAdapter);
      mPager.setCurrentItem(startPage);

      titleIndicator = (TitlePageIndicator) findViewById(R.id.step_edit_top_bar);
      titleIndicator.setViewPager(mPager);
      mSaveStep.setOnClickListener(this);
      mAddStepButton.setOnClickListener(this);
      mDeleteStepButton.setOnClickListener(this);
      if (mConfirmDelete) {
         createDeleteDialog(this).show();
      }

      //getSupportActionBar().setDisplayHomeAsUpEnabled(true);

      if (mIsLoading) {
         mPager.setVisibility(View.GONE);
      }
   }

   @Override
   public void onActivityResult(int requestCode, int resultCode, Intent data) {
      MainApplication.getBus().register(this);

      super.onActivityResult(requestCode, resultCode, data);

      if (mCurStepFragment != null) {
         mCurStepFragment.setMediaResult(requestCode, resultCode, data);
      } else {
         if (resultCode == RESULT_OK) {
            // we dont have a reference the the fragment managing the media, so we make the changes to the step manually
            MediaInfo media = (MediaInfo) data.getSerializableExtra(GalleryActivity.MEDIA_RETURN_KEY);
            APIImage mImageInfo = new APIImage();
            mImageInfo.mBaseUrl = media.getGuid();
            mImageInfo.mId = Integer.valueOf(media.getItemId());
            ArrayList<APIImage> list = mGuide.getStep(mPagePosition).getImages();

            if (list.size() > 0) {
               list.get(0).mBaseUrl = media.getGuid();
               list.get(0).mId = Integer.valueOf(media.getItemId());
            } else {
               list.add(mImageInfo);
            }
            mGuide.getStep(mPagePosition).setImages(list);
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
      savedInstanceState.putBoolean(DELETE_GUIDE_DIALOG_KEY, mConfirmDelete);
      savedInstanceState.putInt(StepEditActivity.GUIDE_STEP_NUM_KEY, mPagePosition);
      savedInstanceState.putBoolean(IS_GUIDE_DIRTY_KEY, mIsStepDirty);
      savedInstanceState.putBoolean(SHOWING_HELP, mShowingHelp);
      savedInstanceState.putBoolean(SHOWING_SAVE, mShowingSave);
      savedInstanceState.putBoolean(LOADING, mIsLoading);
      savedInstanceState.putBoolean(LOCK_SAVE, mLockSave);
      savedInstanceState.putInt(EXIT_CODE, mExitCode);
   }

   @Override
   public void finish() {
      Intent returnIntent = new Intent();
      returnIntent.putExtra(GuideCreateActivity.GUIDE_KEY, mGuide);
      setResult(RESULT_OK, returnIntent);
      super.finish();
   }

   @Override
   public boolean finishActivityIfLoggedOut() {
      return true;
   }

   @Override
   public boolean onCreateOptionsMenu(Menu menu) {
      if (mGuidePublic) {
         menu.add(1, MENU_VIEW_GUIDE, 0, R.string.view_guide)
          .setIcon(R.drawable.ic_action_book)
          .setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
      }
      return super.onCreateOptionsMenu(menu);
   }

   /////////////////////////////////////////////////////
   // NOTIFICATION LISTENERS
   /////////////////////////////////////////////////////

   @Subscribe
   public void onGuideGet(APIEvent.GuideForEdit event) {
      hideLoading();

      if (!event.hasError()) {
         int startPagePosition = 0;
         mGuide = event.getResult();
         for (int i = 0; i < mGuide.getSteps().size(); i++) {
            if (mGuide.getStep(i).getStepid() == mInboundStepId) {
               startPagePosition = i;
               break;
            }
         }
         initPage(startPagePosition);
      } else {
         event.setError(APIError.getFatalError(this));
         APIService.getErrorDialog(StepEditActivity.this, event.getError(), null).show();
      }
   }

   @Subscribe
   public void onStepSave(APIEvent.StepSave event) {
      hideLoading();

      if (!event.hasError()) {

         GuideStep step = event.getResult();

         mGuide.getSteps().set(mSavePosition, step);

         mStepAdapter.notifyDataSetChanged();
      } else {

         mIsStepDirty = true;
         toggleSave(mIsStepDirty);

         event.setError(APIError.getFatalError(this));
         APIService.getErrorDialog(StepEditActivity.this, event.getError(), null).show();
      }
   }

   @Subscribe
   public void onStepAdd(APIEvent.StepAdd event) {

      hideLoading();

      if (!event.hasError()) {
         mGuide = event.getResult();

         mStepAdapter.notifyDataSetChanged();
         mPager.setCurrentItem(mPagePosition);
      } else {
         mIsStepDirty = true;
         toggleSave(mIsStepDirty);

         event.setError(APIError.getFatalError(this));
         APIService.getErrorDialog(StepEditActivity.this, event.getError(), null).show();
      }
   }

   @Subscribe
   public void onGuideStepDeleted(APIEvent.StepRemove event) {
      hideLoading();

      if (!event.hasError()) {
         mGuide.setRevisionid(event.getResult().getRevisionid());
         deleteStep(false);
      } else {
         event.setError(APIError.getFatalError(this));
         APIService.getErrorDialog(StepEditActivity.this, event.getError(), null).show();
      }
   }

   @Subscribe
   public void onImageCopy(APIEvent.CopyImage event) {
      if (!event.hasError()) {
         Toast.makeText(this, getString(R.string.image_saved_to_media_manager_toast),
          Toast.LENGTH_LONG).show();

      } else {
         event.setError(APIError.getFatalError(this));
         APIService.getErrorDialog(StepEditActivity.this, event.getError(), null).show();
      }
   }

   @Subscribe
   public void onGuideChanged(StepChangedEvent event) {
      mIsStepDirty = true;
      toggleSave(mIsStepDirty);
   }

   /////////////////////////////////////////////////////
   // UI EVENT LISTENERS
   /////////////////////////////////////////////////////

   @Override
   public void onClick(View v) {
      switch (v.getId()) {
         case R.id.step_edit_delete_step:
            if (!mGuide.getSteps().isEmpty()) {
               createDeleteDialog(StepEditActivity.this).show();
            }
            break;
         case R.id.step_edit_save:
            save(mPagePosition);
            break;
         case R.id.step_edit_add_step:
            int newPosition = mPagePosition + 1;

            // If the step has changes, save it first.
            if (mIsStepDirty) {
               save(mPagePosition);
            } else if (!stepHasLineContent(mGuide.getStep(mPagePosition))) {
               Toast.makeText(this, getResources().getString(R.string.guide_create_edit_step_media_cannot_add_step),
                Toast.LENGTH_SHORT).show();
               return;
            } else {
               GuideStep item = new GuideStep(StepPortalFragment.STEP_ID++);
               item.setTitle(StepPortalFragment.DEFAULT_TITLE);
               item.addLine(new StepLine());
               item.setStepNum(newPosition);

               mGuide.addStep(item, newPosition);

               for (int i = 1; i < mGuide.getSteps().size(); i++) {
                  mGuide.getStep(i).setStepNum(i);
               }

               // The view pager does not recreate the item in the current position unless we force it
               mStepAdapter = new StepAdapter(this.getSupportFragmentManager());
               mPager.setAdapter(mStepAdapter);
               mPager.invalidate();
               titleIndicator.invalidate();

               mPager.setCurrentItem(newPosition, false);
            }

            break;
      }
   }

   @Override
   public void onBackPressed() {
      finishEdit(FOR_RESULT);
   }

   @Override
   public boolean onOptionsItemSelected(MenuItem item) {
      switch (item.getItemId()) {
         case android.R.id.home:
            if (mParentGuideId == NO_PARENT_GUIDE) {
               finishEdit(HOME_UP);
            } else {
               finishEdit(STEP_VIEW);
            }
            return true;
         case MENU_VIEW_GUIDE:
            finishEdit(STEP_VIEW);
      }

      return (super.onOptionsItemSelected(item));
   }

   /////////////////////////////////////////////////////
   // ADAPTERS and PRIVATE CLASSES
   /////////////////////////////////////////////////////

   public class StepAdapter extends FragmentStatePagerAdapter {

      public StepAdapter(FragmentManager fm) {
         super(fm);
      }

      @Override
      public int getCount() {
         return mGuide.getSteps().size();
      }

      @Override
      public CharSequence getPageTitle(int position) {
         return getString(R.string.step_number, position + 1);
      }

      @Override
      public Fragment getItem(int position) {
         return StepEditFragment.newInstance(mGuide.getStep(position));
      }

      /**
       * When you call notifyDataSetChanged(), if it's set to POSITION_NONE, the view pager will remove all views and
       * reload them all.
       */
      @Override
      public int getItemPosition(Object object) {
         /*StepEditFragment page = (StepEditFragment)object;
         GuideStep step = page.getStepObject();
         int position = mGuide.getSteps().indexOf(step);

         if (position >= 0) {
            return position;
         } else {*/
         return POSITION_NONE;
         // }
      }

      @Override
      public void setPrimaryItem(ViewGroup container, int position, Object object) {
         super.setPrimaryItem(container, position, object);

         mPagePosition = position;
         mCurStepFragment = (StepEditFragment) object;
      }
   }

   /////////////////////////////////////////////////////
   // DIALOGS
   /////////////////////////////////////////////////////

   protected AlertDialog createDeleteDialog(final Context context) {
      mConfirmDelete = true;
      AlertDialog.Builder builder = new AlertDialog.Builder(context);
      builder
       .setTitle(context.getString(R.string.step_edit_confirm_delete_title))
       .setMessage(context.getString(R.string.step_edit_confirm_delete_message, mPagePosition + 1))
       .setPositiveButton(context.getString(R.string.yes), new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int id) {
             mConfirmDelete = false;
             mIsStepDirty = false;

             //step is at end of list
             if (mPagePosition >= mGuide.getSteps().size()
              // or it's a new step
              || mGuide.getStep(mPagePosition).getRevisionid() == null) {
                deleteStep(mIsStepDirty);
             } else {
                showLoading(mLoadingContainer);
                APIService.call(StepEditActivity.this, APIService.getRemoveStepAPICall(
                 mGuide.getGuideid(), mGuide.getRevisionid(), mGuide.getSteps().get(mPagePosition)));
             }
             dialog.cancel();
          }
       }).setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
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

   protected AlertDialog createHelpDialog() {
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

   protected AlertDialog createExitWarningDialog(final int exitCode) {
      mShowingSave = true;
      AlertDialog.Builder builder = new AlertDialog.Builder(this);
      builder
       .setTitle(getString(R.string.guide_create_confirm_leave_without_save_title))
       .setMessage(getString(R.string.guide_create_confirm_leave_without_save_body))
       .setNegativeButton(getString(R.string.save),
        new DialogInterface.OnClickListener() {
           public void onClick(DialogInterface dialog, int id) {
              mIsStepDirty = true;
              save(mPagePosition);
              dialog.dismiss();

              if (mExitCode == STEP_VIEW) {
                 navigateToStepView();
              } else {
                 finish();
              }
           }
        })
       .setPositiveButton(R.string.guide_create_confirm_leave_without_save_cancel,
        new DialogInterface.OnClickListener() {
           public void onClick(DialogInterface dialog, int id) {
              mIsStepDirty = false;
              dialog.dismiss();
              finishEdit(exitCode);
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

   /////////////////////////////////////////////////////
   // HELPERS
   /////////////////////////////////////////////////////

   protected void save(int savePosition) {
      GuideStep obj = mGuide.getStep(savePosition);
      obj.setLines(mCurStepFragment.getLines());

      if (!obj.hasVideo() && !obj.hasEmbed()) {
         obj.setImages(mCurStepFragment.getImages());
      }

      obj.setTitle(mCurStepFragment.getTitle());

      mGuide.getSteps().set(savePosition, obj);

      if (!stepHasLineContent(obj)) {
         Toast.makeText(this, getResources().getString(R.string.guide_create_edit_must_add_line_content),
          Toast.LENGTH_SHORT).show();

         return;
      }

      if (!mIsStepDirty || mLockSave) {
         return;
      }

      mSavePosition = savePosition;
      mIsStepDirty = false;

      showLoading(mLoadingContainer);
      toggleSave(mIsStepDirty);

      if (obj.getRevisionid() != null) {
         APIService.call(this, APIService.getEditStepAPICall(obj, mGuide.getGuideid()));
      } else {
         APIService.call(this, APIService.getAddStepAPICall(obj, mGuide.getGuideid(),
          mPagePosition + 1, mGuide.getRevisionid()));
      }
   }

   private boolean stepHasLineContent(GuideStep obj) {
      if (obj.getLines().size() == 0) {
         return false;
      }

      for (StepLine l : obj.getLines()) {
         if (l.getTextRaw().length() == 0) {
            return false;
         }
      }


      return true;
   }

   @Override
   public void showLoading(int container) {
      if (mPager != null) {
         mPager.setVisibility(View.GONE);
      }
      mIsLoading = true;

      super.showLoading(container);
   }

   @Override
   public void hideLoading() {
      if (mPager != null) {
         mPager.setVisibility(View.VISIBLE);
      }
      getSupportFragmentManager().popBackStack("loading", FragmentManager.POP_BACK_STACK_INCLUSIVE);
      mIsLoading = false;
   }

   protected void navigateToStepView() {
      Intent intent = new Intent(this, GuideViewActivity.class);
      if (mParentGuideId != NO_PARENT_GUIDE) {
         intent.putExtra(GuideViewActivity.SAVED_GUIDEID, mParentGuideId);
      } else {
         intent.putExtra(GuideViewActivity.SAVED_GUIDEID, mGuide.getGuideid());
      }
      intent.putExtra(GuideViewActivity.CURRENT_PAGE, mPagePosition + 1);
      intent.putExtra(GuideViewActivity.INBOUND_STEP_ID, mGuide.getStep(mPagePosition).getStepid());
      intent.putExtra(StepEditActivity.GUIDE_PUBLIC_KEY, mGuide.isPublic());
      intent.putExtra(GuideViewActivity.FROM_EDIT, true);
      startActivity(intent);
   }

   protected void finishEdit(int exitCode) {
      mExitCode = exitCode;
      if (mIsStepDirty) {
         createExitWarningDialog(exitCode).show();
      } else {

         // Clean out unsaved, new steps.
         for (Iterator<GuideStep> it = mGuide.getSteps().iterator(); it.hasNext(); ) {
            if (it.next().getRevisionid() == null) {
               it.remove();
            }
         }

         // Make sure the step numbers are correct after removing steps.
         for (int i = 1; i <= mGuide.getSteps().size(); i++) {
            mGuide.getStep(i - 1).setStepNum(i);
         }

         Intent data;
         switch (exitCode) {
            case HOME_UP:
               data = new Intent(this, StepsActivity.class);
               data.putExtra(StepsActivity.GUIDE_ID_KEY, mGuide.getGuideid());
               data.putExtra(GuideCreateActivity.GUIDE_KEY, mGuide);
               data.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);

               startActivity(data);
               break;
            case FOR_RESULT:
               data = new Intent();
               data.putExtra(GuideCreateActivity.GUIDE_KEY, mGuide);

               if (getParent() == null) {
                  setResult(Activity.RESULT_OK, data);
               } else {
                  getParent().setResult(Activity.RESULT_OK, data);
               }

               finish();
               break;
            case STEP_VIEW:
               navigateToStepView();
               break;

         }
      }
      return;
   }

   protected void deleteStep(boolean unsaved) {

      if (mPagePosition < mGuide.getSteps().size()) {
         if (!unsaved) {
            mGuide.getSteps().remove(mPagePosition);
         }
      }

      if (mGuide.getSteps().size() == 0) {
         finish();
      }

      for (int i = 0; i < mGuide.getSteps().size(); i++) {
         mGuide.getStep(i).setStepNum(i);
      }

      // The view pager does not recreate the item in the current position unless we force it to:
      mStepAdapter = new StepAdapter(this.getSupportFragmentManager());
      mPager.setAdapter(mStepAdapter);
      mPager.setCurrentItem(mPagePosition);
      mPager.invalidate();
      titleIndicator.invalidate();

   }

   protected void invalidateStepAdapter() {
      mStepAdapter.notifyDataSetChanged();
   }

   protected int getIndicatorHeight() {
      return titleIndicator.getHeight() + mBottomBar.getHeight();
   }

   public void toggleSave(boolean toggle) {
      if (!mLockSave) {
         int buttonBackgroundColor = toggle ? R.color.fireswing_blue : R.color.fireswing_dark_grey;
         int buttonTextColor = toggle ? R.color.white : R.color.fireswing_grey;

         mSaveStep.setBackgroundColor(getResources().getColor(buttonBackgroundColor));
         mSaveStep.setTextColor(getResources().getColor(buttonTextColor));
         mSaveStep.setText(R.string.save);
         mSaveStep.setEnabled(toggle);
         // Lock the pager if save is enabled
         enableViewPager(!toggle);
      }
   }

   protected void enableViewPager(boolean unlocked) {
      if (mPager != null) {
         mPager.setPagingEnabled(unlocked);
      }
   }

   public void lockSave() {
      mLockSave = true;
      mSaveStep.setText(R.string.loading_image);
      enableViewPager(false);
   }

   public void unlockSave() {
      mLockSave = false;
      enableViewPager(true);
   }
}
