package com.dozuki.ifixit.ui.guide.create;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.dozuki.ifixit.MainApplication;
import com.dozuki.ifixit.R;
import com.dozuki.ifixit.model.Image;
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

import java.util.ArrayList;
import java.util.Iterator;

public class StepEditActivity extends BaseActivity implements OnClickListener {
   public static final int MENU_VIEW_GUIDE = 12;
   private static final int STEP_VIEW = 1;
   private static final int FOR_RESULT = 2;
   private static final int HOME_UP = 3;

   private enum ConfirmSave {
      NEW_STEP,
      NEXT_STEP
   }

   public static final int GALLERY_REQUEST_CODE = 1;
   public static final int CAMERA_REQUEST_CODE = 1888;

   public static final String TEMP_FILE_NAME_KEY = "TEMP_FILE_NAME_KEY";
   public static final String EXIT_CODE = "EXIT_CODE_KEY";
   public static final String GUIDE_PUBLIC_KEY = "GUIDE_PUBLIC_KEY";

   public static String GUIDE_STEP_NUM_KEY = "GUIDE_STEP_NUM_KEY";
   public static String DELETE_GUIDE_DIALOG_KEY = "DeleteGuideDialog";
   public static final String GUIDE_ID_KEY = "GUIDE_ID_KEY";
   public static final String GUIDE_STEP_ID = "GUIDE_STEP_ID";
   public static final String PARENT_GUIDE_ID_KEY = "PARENT_GUIDE_ID_KEY";
   public static final int NO_PARENT_GUIDE = -1;

   private static final String SHOWING_HELP = "SHOWING_HELP";

   private static final String IS_GUIDE_DIRTY_KEY = "IS_GUIDE_DIRTY_KEY";
   private static final String SHOWING_SAVE = "SHOWING_SAVE";
   private static final String LOCK_SAVE = "LOCK_SAVE";

   private Guide mGuide;
   private StepEditFragment mCurStepFragment;
   private ImageButton mAddStepButton;
   private Button mSaveStep;
   private ImageButton mDeleteStepButton;
   private StepAdapter mStepAdapter;
   private LockableViewPager mPager;
   private LockableTitlePageIndicator mTitleIndicator;
   private int mPagePosition = 0;
   private int mSavePosition;

   // Necessary for editing prerequisite guides from the view interface in order to navigate back to the parent guide.
   private int mParentGuideId = NO_PARENT_GUIDE;

   // Used to navigate to the correct step when coming from GuideViewActivity.
   private int mInboundStepId;
   private int mGuideid;

   private boolean mConfirmDelete = false;
   private boolean mIsStepDirty = false;
   private boolean mShowingHelp = false;
   private boolean mShowingSave = false;
   private boolean mIsLoading;

   // Should a new step be created after a step POST response (creating a new step)
   private boolean mAddStepAfterSave = false;

   // Flag to prevent saving a guide while we're waiting for an image to upload and return
   private boolean mLockSave;

   private int mExitCode;

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

      extractExtras(getIntent().getExtras());

      if (savedInstanceState != null) {
         mGuide = (Guide) savedInstanceState.getSerializable(StepsActivity.GUIDE_KEY);

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

      mPager = (LockableViewPager) findViewById(R.id.guide_edit_body_pager);
      initPager();
      mPager.setCurrentItem(startPage);

      mTitleIndicator = (LockableTitlePageIndicator) findViewById(R.id.step_edit_top_bar);
      mTitleIndicator.setViewPager(mPager);

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

   private void initPager() {
      mStepAdapter = new StepAdapter(this.getSupportFragmentManager());
      mPager.setAdapter(mStepAdapter);
   }

   private void extractExtras(Bundle extras) {
      if (extras != null) {
         mGuide = (Guide) extras.getSerializable(GuideCreateActivity.GUIDE_KEY);
         mPagePosition = extras.getInt(GUIDE_STEP_NUM_KEY, 0);

         if (mGuide == null) {
            mParentGuideId = extras.getInt(PARENT_GUIDE_ID_KEY, NO_PARENT_GUIDE);
            mGuideid = extras.getInt(GUIDE_ID_KEY);
            mInboundStepId = extras.getInt(GUIDE_STEP_ID);

            showLoading(mLoadingContainer);
            APIService.call(StepEditActivity.this,
             APIService.getGuideForEditAPICall(mGuideid));
         }
      }
   }

   @Override
   public void onNewIntent(Intent intent) {
      super.onNewIntent(intent);

      mGuide = null;
      mPagePosition = 0;

      extractExtras(intent.getExtras());
      if (mGuide != null) {
         initPage(mPagePosition);
      }
   }

   @Override
   public void onActivityResult(int requestCode, int resultCode, Intent data) {
      MainApplication.getBus().register(this);

      super.onActivityResult(requestCode, resultCode, data);

      if (mCurStepFragment != null) {
         Image newThumb;

         switch (requestCode) {
            case GALLERY_REQUEST_CODE:
               if (data != null) {
                  newThumb = (Image) data.getSerializableExtra(GalleryActivity.MEDIA_RETURN_KEY);
                  mGuide.getStep(mPagePosition).addImage(newThumb);
                  mCurStepFragment.setImages(mGuide.getStep(mPagePosition).getImages());
                  MainApplication.getBus().post(new StepChangedEvent());
               } else {
                  Log.e("StepEditActivity", "Error data is null!");
                  return;
               }

               break;
            case CAMERA_REQUEST_CODE:
               if (resultCode == Activity.RESULT_OK) {

                  SharedPreferences prefs = getSharedPreferences("com.dozuki.ifixit", Context.MODE_PRIVATE);
                  String tempFileName = prefs.getString(TEMP_FILE_NAME_KEY, null);

                  if (tempFileName == null) {
                     Log.e("StepEditActivity", "Error cameraTempFile is null!");
                     return;
                  }

                  // Prevent a save from being called until the image uploads and returns with the imageid
                  lockSave();

                  newThumb = new Image();
                  newThumb.setLocalImage(tempFileName);

                  mGuide.getStep(mPagePosition).addImage(newThumb);
                  mCurStepFragment.setImages(mGuide.getStep(mPagePosition).getImages());

                  APIService.call(this, APIService.getUploadImageToStepAPICall(tempFileName));
               }
               break;
         }

      } else {
         if (resultCode == RESULT_OK) {

            // we dont have a reference the the fragment managing the media, so we make the changes to the step manually
            Image image = (Image) data.getSerializableExtra(GalleryActivity.MEDIA_RETURN_KEY);
            ArrayList<Image> list = mGuide.getStep(mPagePosition).getImages();

            if (list.size() > 0) {
               list.set(0, image);
            } else {
               list.add(image);
            }

            mGuide.getStep(mPagePosition).setImages(list);
            toggleSave(true);
            // recreate pager with updated step:
            initPager();
            mPager.invalidate();
            mTitleIndicator.invalidate();
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

   public void navigateBack() {
      Intent returnIntent = new Intent();
      returnIntent.putExtra(GuideCreateActivity.GUIDE_KEY, mGuide);
      setResult(RESULT_OK, returnIntent);
      finish();
   }

   @Override
   public boolean finishActivityIfLoggedOut() {
      return true;
   }

   @Override
   public boolean alertOnNavigation() {
      return mIsStepDirty;
   }

   @Override
   public AlertDialog navigationAlertDialog(final String tag, final Context context) {
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

              navigateMenuDrawer(tag, context);
           }
        })
       .setPositiveButton(R.string.guide_create_confirm_leave_without_save_cancel,
        new DialogInterface.OnClickListener() {
           public void onClick(DialogInterface dialog, int id) {
              mIsStepDirty = false;
              dialog.dismiss();

              navigateMenuDrawer(tag, context);
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

   @Override
   public boolean onCreateOptionsMenu(Menu menu) {
      menu.add(1, MENU_VIEW_GUIDE, 0, R.string.view_guide)
       .setIcon(R.drawable.ic_action_book)
       .setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS | MenuItem.SHOW_AS_ACTION_WITH_TEXT);

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
         APIService.getErrorDialog(this, event).show();
      }
   }

   @Subscribe
   public void onStepSave(APIEvent.StepSave event) {
      hideLoading();

      if (!event.hasError() || event.getError().mType == APIError.Type.CONFLICT) {
         // Update the guide on successful save or conflict.
         mGuide.getSteps().set(mSavePosition, event.getResult());

         // The view pager does not recreate the item in the current position unless we force it
         initPager();
         mPager.invalidate();
         mTitleIndicator.invalidate();

         mPager.setCurrentItem(mSavePosition, false);
      }

      if (event.hasError()) {
         mIsStepDirty = true;
         toggleSave(mIsStepDirty);

         APIService.getErrorDialog(this, event).show();
      }
   }

   @Subscribe
   public void onUploadStepImage(APIEvent.UploadStepImage event) {
      if (!event.hasError()) {
         Image newThumb = event.getResult();

         // Find the temporarily stored image object to update the filename to
         // the image path and imageid.
         if (newThumb != null) {
            ArrayList<Image> images = new ArrayList<Image>(mGuide.getStep(mPagePosition).getImages());

            for (Image image : images) {
               if (image.isLocal()) {
                  images.set(images.indexOf(image), newThumb);
                  break;
               }
            }

            mCurStepFragment.setImages(images);
            mGuide.getStep(mPagePosition).setImages(images);
         }

         if (!mGuide.getStep(mPagePosition).hasLocalImages()) {
            unlockSave();

            // Set guide dirty after the image is uploaded so the user can't
            // save the guide before we have the imageid.
            MainApplication.getBus().post(new StepChangedEvent());
         }
      } else {
         APIService.getErrorDialog(this, event).show();
      }
   }

   @Subscribe
   public void onStepImageDelete(StepImageDeleteEvent event) {
      mGuide.getStep(mPagePosition).getImages().remove(event.image);
   }

   @Subscribe
   public void onStepAdd(APIEvent.StepAdd event) {
      hideLoading();

      if (!event.hasError()) {
         mGuide = event.getResult();

         mStepAdapter.notifyDataSetChanged();
         mPager.setCurrentItem(mSavePosition);

         if (mAddStepAfterSave) {
            addNewStep(mSavePosition + 1);
            mAddStepAfterSave = false;
         }
      } else {
         mAddStepAfterSave = false;
         mIsStepDirty = true;
         toggleSave(mIsStepDirty);

         APIService.getErrorDialog(this, event).show();
      }
   }

   @Subscribe
   public void onGuideStepDeleted(APIEvent.StepRemove event) {
      hideLoading();

      if (!event.hasError()) {
         mGuide.setRevisionid(event.getResult().getRevisionid());
         deleteStep();
      } else {
         APIService.getErrorDialog(this, event).show();
      }
   }

   @Subscribe
   public void onImageCopy(APIEvent.CopyImage event) {
      if (!event.hasError()) {
         Toast.makeText(this, getString(R.string.image_saved_to_media_manager_toast),
          Toast.LENGTH_LONG).show();
      } else {
         APIService.getErrorDialog(this, event).show();
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

            // If the step has changes, prompt the user to save or continue editing.
            if (mIsStepDirty) {
               createSaveChangesDialog(ConfirmSave.NEW_STEP).show();
            // If the step doesn't have any bullet content, prompt them to add some.
            } else if (!stepHasLineContent(mGuide.getStep(mPagePosition))) {
               Toast.makeText(this, getResources().getString(R.string.guide_create_edit_step_media_cannot_add_step),
                Toast.LENGTH_SHORT).show();
               return;
            } else {
               addNewStep(newPosition);
            }

            break;
      }
   }

   public void addNewStep(int newPosition) {
      if (!mGuide.hasNewStep()) {
         GuideStep item = new GuideStep(StepPortalFragment.STEP_ID++);
         item.setTitle(StepPortalFragment.DEFAULT_TITLE);
         item.addLine(new StepLine());
         item.setStepNum(newPosition);

         mGuide.addStep(item, newPosition);

         // The view pager does not recreate the item in the current position unless we force it
         initPager();
         mPager.invalidate();
         mTitleIndicator.invalidate();

         mPager.setCurrentItem(newPosition, false);
      } else {
         // Show "Must add content to step" toast
         Toast.makeText(this, getResources().getString(R.string.guide_create_edit_step_media_cannot_add_step),
          Toast.LENGTH_SHORT).show();
      }
   }

   @Override
   public void onBackPressed() {
      finishEdit(HOME_UP);
   }

   @Override
   public boolean onOptionsItemSelected(MenuItem item) {
      switch (item.getItemId()) {
         case MENU_VIEW_GUIDE:
            finishEdit(STEP_VIEW);
      }

      return super.onOptionsItemSelected(item);
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
         return PagerAdapter.POSITION_NONE;
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
                deleteStep();
             } else {
                showLoading(mLoadingContainer, getString(R.string.deleting));
                APIService.call(StepEditActivity.this, APIService.getRemoveStepAPICall(
                 mGuide.getGuideid(), mGuide.getSteps().get(mPagePosition)));
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

   protected AlertDialog createSaveChangesDialog(final ConfirmSave dialogType) {
      mShowingSave = true;
      AlertDialog.Builder builder = new AlertDialog.Builder(this);
      builder
       .setTitle(getString(R.string.save_changes_to_step))
       .setPositiveButton(getString(R.string.yes),
        new DialogInterface.OnClickListener() {
           public void onClick(DialogInterface dialog, int id) {
              save(mPagePosition);
              dialog.dismiss();
              switch (dialogType) {
                 case NEW_STEP:
                    addNewStep(mPagePosition + 1);
                    mAddStepAfterSave = true;
                    break;
                 case NEXT_STEP:
                    break;
              }
           }
        })
       .setNegativeButton(getString(R.string.cancel),
        new DialogInterface.OnClickListener() {
           public void onClick(DialogInterface dialog, int id) {
              mIsStepDirty = true;
              dialog.dismiss();
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
                 navigateBack();
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

      showLoading(mLoadingContainer, getString(R.string.saving));
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
   public void showLoading(int container, String message) {
      if (mPager != null) {
         mPager.setVisibility(View.GONE);
      }
      mIsLoading = true;

      super.showLoading(container, message);
   }

   @Override
   public void hideLoading() {
      if (mPager != null) {
         mPager.setVisibility(View.VISIBLE);
      }
      getSupportFragmentManager().popBackStack(LOADING, FragmentManager.POP_BACK_STACK_INCLUSIVE);
      mIsLoading = false;
   }

   protected void navigateToStepView() {
      Intent intent = new Intent(this, GuideViewActivity.class);
      if (mParentGuideId != NO_PARENT_GUIDE) {
         intent.putExtra(GuideViewActivity.GUIDEID, mParentGuideId);
      } else {
         intent.putExtra(GuideViewActivity.GUIDEID, mGuide.getGuideid());
      }
      intent.putExtra(GuideViewActivity.CURRENT_PAGE, mPagePosition + 1);
      intent.putExtra(GuideViewActivity.INBOUND_STEP_ID, mGuide.getStep(mPagePosition).getStepid());
      intent.putExtra(StepEditActivity.GUIDE_PUBLIC_KEY, mGuide.isPublic());
      intent.putExtra(GuideViewActivity.FROM_EDIT, true);
      intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
      startActivity(intent);
   }

   protected void finishEdit(int exitCode) {
      mExitCode = exitCode;
      if (mIsStepDirty || mLockSave) {
         createExitWarningDialog(exitCode).show();
      } else {

         // Clean out unsaved, new steps.
         for (Iterator<GuideStep> it = mGuide.getSteps().iterator(); it.hasNext(); ) {
            if (it.next().getRevisionid() == null) {
               it.remove();
            }
         }

         int guideSize = mGuide.getSteps().size();

         // Make sure the step numbers are correct after removing steps.
         for (int i = 1; i <= guideSize; i++) {
            mGuide.getStep(i - 1).setStepNum(i);
         }

         // If the current position is equal to or greater than the number of steps in the guide,
         // there was a new step at the end of the guide and that position no longer exists.  Set the page position
         // to the new last step.
         if (mPagePosition >= guideSize) {
            mPagePosition = guideSize - 1;
         }

         Intent data;
         switch (exitCode) {
            case HOME_UP:
               data = new Intent(this, StepsActivity.class);
               data.putExtra(StepsActivity.GUIDE_ID_KEY, mGuide.getGuideid());
               data.putExtra(GuideCreateActivity.GUIDE_KEY, mGuide);
               data.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_NO_ANIMATION);

               startActivity(data);
               finish();
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
   }

   protected void deleteStep() {
      mGuide.getSteps().remove(mPagePosition);

      // If it's the last step in the guide, finish the activity.
      if (mGuide.getSteps().size() == 0) {
         mStepAdapter.notifyDataSetChanged();

         navigateBack();
         return;
      }

      int guideSize = mGuide.getSteps().size();

      for (int i = 0; i < guideSize; i++) {
         mGuide.getStep(i).setStepNum(i);
      }

      int newPosition = mPagePosition - 1;

      // The view pager does not recreate the item in the current position unless we force it to.
      initPager();
      mPager.invalidate();
      mTitleIndicator.notifyDataSetChanged();
      mStepAdapter.notifyDataSetChanged();
      mPager.setCurrentItem(newPosition, false);
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

      if (mTitleIndicator != null) {
         mTitleIndicator.setPagingEnabled(unlocked);
      }
   }

   public void lockSave() {
      mLockSave = true;

      mSaveStep.setText(getString(R.string.loading_image));
      mSaveStep.setBackgroundColor(getResources().getColor(R.color.fireswing_dark_grey));
      mSaveStep.setTextColor(getResources().getColor(R.color.fireswing_grey));

      enableViewPager(false);
   }

   public void unlockSave() {
      mLockSave = false;
      enableViewPager(true);
   }
}
