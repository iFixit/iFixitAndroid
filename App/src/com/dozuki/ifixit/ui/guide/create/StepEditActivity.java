package com.dozuki.ifixit.ui.guide.create;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.*;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognizerIntent;
import android.support.v4.app.*;
import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CompoundButton;
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
import com.dozuki.ifixit.ui.BaseMenuDrawerActivity;
import com.dozuki.ifixit.ui.gallery.GalleryActivity;
import com.dozuki.ifixit.ui.guide.view.GuideViewActivity;
import com.dozuki.ifixit.util.APIError;
import com.dozuki.ifixit.util.APIEvent;
import com.dozuki.ifixit.util.APIService;
import com.dozuki.ifixit.util.JSONHelper;
import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.Tracker;
import com.squareup.otto.Subscribe;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;

public class StepEditActivity extends BaseMenuDrawerActivity implements OnClickListener {
   private static final int STEP_VIEW = 1;
   private static final int FOR_RESULT = 2;
   private static final int HOME_UP = 3;
   private static final String TAG = "StepEditActivity";

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
   private ImageButton mAddStepButton;
   private Button mSaveStep;
   private ImageButton mDeleteStepButton;
   private StepAdapter mStepAdapter;
   private LockableViewPager mPager;
   private LockableTitlePageIndicator mTitleIndicator;
   private int mPagePosition = 0;
   private int mSavePosition;

   /**
    * Necessary for editing prerequisite guides from the view interface in order
    * to navigate back to the parent guide.
    */
   private int mParentGuideId = NO_PARENT_GUIDE;

   // Used to navigate to the correct step when coming from GuideViewActivity.
   private int mInboundStepId;

   private boolean mConfirmDelete = false;
   private boolean mIsStepDirty = false;
   private boolean mShowingHelp = false;
   private boolean mShowingSave = false;
   private boolean mIsLoading = false;

   // Should a new step be created after a step POST response (creating a new step)
   private boolean mAddStepAfterSave = false;

   // Flag to prevent saving a guide while we're waiting for an image to upload and return
   private boolean mLockSave = false;

   private int mExitCode;

   private static int mLoadingContainer = R.id.step_edit_loading_screen;

   private SharedPreferences mSharedPreferences;

   /////////////////////////////////////////////////////
   // LIFECYCLE
   /////////////////////////////////////////////////////

   @Override
   public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.guide_create_step_edit);

      mSharedPreferences = getSharedPreferences("com.dozuki.ifixit", Context.MODE_PRIVATE);

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

         if (mConfirmDelete) {
            createDeleteDialog(this).show();
         }

      } else if (getIntent().getExtras() != null) {
         extractExtras(getIntent().getExtras());
      } else {
         // Creating a new guide
         initializeNewGuide();
      }

      if (MainApplication.get().getSite().mGuideTypes == null) {
         APIService.call(this, APIService.getSiteInfoAPICall());
      }

      mSaveStep = (Button) findViewById(R.id.step_edit_save);

      toggleSave(mIsStepDirty);

      if (mGuide != null) {
         initPage(mPagePosition);
      }
   }

   private void initializeNewGuide() {
      // Creating a new guide
      mGuide = new Guide();

      GuideStep step = new GuideStep();
      step.addLine(new StepLine());

      mGuide.addStep(step);
      mPagePosition = 0;
   }

   private void initPage(int startPage) {

      String guideTitle = mGuide.getTitle();

      getSupportActionBar().setTitle(guideTitle);

      EasyTracker.getTracker().sendView(guideTitle + " Edit View");

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

      if (mIsLoading) {
         mPager.setVisibility(View.GONE);
      }

      // Must be after mPager and mTitleIndicator are initialized, otherwise they aren't locked
      if (mLockSave) {
         lockSave();
      }

      // Finally, reload the action bar to update action states (view guide and public/private toggle)
      supportInvalidateOptionsMenu();
   }

   private void initPager() {
      mStepAdapter = new StepAdapter(getSupportFragmentManager());
      mPager.setAdapter(mStepAdapter);
   }

   private void extractExtras(Bundle extras) {
      if (extras != null) {
         mGuide = (Guide) extras.getSerializable(GuideCreateActivity.GUIDE_KEY);
         mPagePosition = extras.getInt(GUIDE_STEP_NUM_KEY, 0);

         if (mGuide == null) {
            mParentGuideId = extras.getInt(PARENT_GUIDE_ID_KEY, NO_PARENT_GUIDE);
            int guideid = extras.getInt(GUIDE_ID_KEY);
            mInboundStepId = extras.getInt(GUIDE_STEP_ID);

            showLoading(mLoadingContainer);
            APIService.call(StepEditActivity.this,
             APIService.getGuideForEditAPICall(guideid));
         }
      }
   }

   @Override
   public void onNewIntent(Intent intent) {
      super.onNewIntent(intent);

      Bundle extras = intent.getExtras();
      if (extras != null) {
         Log.d(TAG, "onNewIntent has extras");
         mGuide = null;
         mPagePosition = 0;

         extractExtras(intent.getExtras());
      } else {
         initializeNewGuide();
      }

      if (mGuide != null) {
         initPage(mPagePosition);
      }
   }

   @Override
   public void onActivityResult(int requestCode, int resultCode, Intent data) {
      MainApplication.getBus().register(this);

      Image newThumb;

      switch (requestCode) {
         case GALLERY_REQUEST_CODE:
            if (data != null) {
               newThumb = (Image) data.getSerializableExtra(GalleryActivity.MEDIA_RETURN_KEY);
               mGuide.getStep(mPagePosition).addImage(newThumb);
               refreshView(mPagePosition);

               onGuideChanged(null);
            } else {
               Log.e("StepEditActivity", "Error data is null!");
               return;
            }

            break;
         case CAMERA_REQUEST_CODE:
            if (resultCode == Activity.RESULT_OK) {

               String tempFileName = mSharedPreferences.getString(TEMP_FILE_NAME_KEY, null);

               if (tempFileName == null) {
                  Log.e("StepEditActivity", "Error cameraTempFile is null!");
                  return;
               }

               // Prevent a save from being called until the image uploads and returns with the imageid
               lockSave();

               newThumb = new Image();
               newThumb.setLocalImage(tempFileName);

               mGuide.getStep(mPagePosition).addImage(newThumb);
               refreshView(mPagePosition);

               APIService.call(this, APIService.getUploadImageToStepAPICall(tempFileName));
            }
            break;
         case StepEditLinesFragment.MIC_REQUEST_CODE:
            if (resultCode == Activity.RESULT_OK) {
               // Populate the wordsList with the String values the recognition engine thought it heard
               final ArrayList<String> matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

               if (matches == null) {
                  return;
               }

               if (MainApplication.inDebug()) {
                  String debug = "";

                  for (String match : matches) {
                     debug += "   " + match + "\n";
                  }
                  Log.d("StepEditActivity", "Potential Results:  \n\n" + debug);
               }

               if (matches.size() > 0) {
                  Handler handler = new Handler();

                  /**
                   * We have to delay posting the event because this activities
                   * onActivityResult method is called just before the fragments onResume.
                   * Delaying 1/10 of a second gives the fragment enough time to
                   * register its' event bus listener so it can receive the event.
                   */
                  handler.postDelayed(new Runnable() {
                     @Override
                     public void run() {
                        MainApplication.getBus().post(new StepMicCompleteEvent(matches,
                         mGuide.getStep(mPagePosition).getStepid()));
                     }

                  }, 100);
               } else {
                  Log.d("StepEditActivity", "No matches; try again");
                  // TODO: Relaunch mic and try again.
               }
            }
            break;
         default:
            super.onActivityResult(requestCode, resultCode, data);
      }
   }

   @Override
   public void onStop() {
      super.onStop();
   };

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

   private void navigateBack() {
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
      super.onCreateOptionsMenu(menu);

      getSupportMenuInflater().inflate(R.menu.step_edit_menu, menu);
      MenuItem item = menu.findItem(R.id.publish_guide);
      CompoundButton toggle = (CompoundButton)item.getActionView().findViewById(R.id.publish_toggle);
      toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
         @Override
         public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (mGuide != null && !mGuide.isNewGuide() && isChecked != mGuide.isPublic()) {
               Log.d("StepEditActivity", "Toggle: " + (isChecked ? "true" : "false"));

               // Disable the toggle so we don't have multiple presses.
               buttonView.setEnabled(false);

               // Disable the switch / checkbox until the publish response comes back.
               //buttonView.setEnabled(false);
               showLoading(mLoadingContainer, getString(isChecked ? R.string.publishing : R.string.unpublishing));

               if (isChecked) {
                  APIService.call(StepEditActivity.this,
                   APIService.getPublishGuideAPICall(mGuide.getGuideid(), mGuide.getRevisionid()));
               } else {
                  APIService.call(StepEditActivity.this,
                   APIService.getUnPublishGuideAPICall(mGuide.getGuideid(), mGuide.getRevisionid()));
               }
            }
         }
      });

      return true;
   }

   @Override
   public boolean onPrepareOptionsMenu(Menu menu) {
      MenuItem viewGuide = menu.findItem(R.id.view_guide);
      MenuItem visibilityToggle = menu.findItem(R.id.publish_guide);
      CompoundButton toggle = ((CompoundButton) visibilityToggle.getActionView().findViewById(R.id.publish_toggle));
      if (mGuide != null) {
         if (mGuide.getRevisionid() == null) {
            viewGuide.setIcon(R.drawable.ic_action_book_dark);
            viewGuide.setEnabled(false);
            toggle.setEnabled(false);
         } else {
            viewGuide.setIcon(R.drawable.ic_action_book);
            viewGuide.setEnabled(true);
            toggle.setEnabled(true);

            if (toggle.isChecked() != mGuide.isPublic())
               toggle.setChecked(mGuide.isPublic());
         }
      } else {
         viewGuide.setIcon(R.drawable.ic_action_book_dark);
         viewGuide.setEnabled(false);
         toggle.setEnabled(false);
      }
      return super.onPrepareOptionsMenu(menu);
   }

   @Override
   public boolean onOptionsItemSelected(MenuItem item) {
      switch (item.getItemId()) {
         case R.id.view_guide:
            finishEdit(STEP_VIEW);
            break;
         case R.id.discard_changes:
            if (!mIsStepDirty) break; // Bail early if there aren't any changes

            toggleSave(false);
            mIsStepDirty = false;

            // Set the inbound stepid so the Step pager will navigate to the current step after updating
            mInboundStepId = mGuide.getStep(mPagePosition).getStepid();
            APIService.call(StepEditActivity.this, APIService.getGuideForEditAPICall(mGuide.getGuideid()));

            break;
      }

      return super.onOptionsItemSelected(item);
   }

   /////////////////////////////////////////////////////
   // NOTIFICATION LISTENERS
   /////////////////////////////////////////////////////

   @Subscribe
   public void onSiteInfo(APIEvent.SiteInfo event) {
      if (!event.hasError()) {
         MainApplication.get().setSite(event.getResult());
      }
   }

   @Subscribe
   public void onPublishStatus(APIEvent.PublishStatus event) {
      hideLoading();

      // Re-enable the toggle view
      findViewById(R.id.publish_toggle).setEnabled(true);

      // Update guide even if there is a conflict.
      if (!event.hasError() || event.getError().mType == APIError.Type.CONFLICT) {
         Guide result = event.getResult();
         mGuide.setRevisionid(result.getRevisionid());
         mGuide.setPublic(result.isPublic());
      }

      if (event.hasError()) {
         APIService.getErrorDialog(this, event).show();
      }

      // Reload the options menu to reenable the button, regardless of success or failure because we need to update
      // the state if the request failed so the toggle is reset to it's correct position.
      supportInvalidateOptionsMenu();
   }

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
         updateCurrentStep(event.getResult());
      }

      if (event.hasError()) {
         mIsStepDirty = true;
         toggleSave(mIsStepDirty);
         final APIError error = event.getError();

         if (error.mType == APIError.Type.VALIDATION) {

            int positiveButton = R.string.error_confirm;

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(error.mTitle)
             .setMessage(error.mMessage)
             .setPositiveButton(positiveButton,
              new DialogInterface.OnClickListener() {
                 public void onClick(DialogInterface dialog, int id) {
                    dialog.dismiss();

                    if (error.mIndex != -1) {
                       MainApplication.getBus().post(new StepLineValidationEvent(
                        mGuide.getStep(mSavePosition).getStepid(), error.mIndex));
                    }
                 }
              });

            builder.create().show();
         } else {
            APIService.getErrorDialog(this, event).show();
         }
      }
   }

   @Subscribe
   public void onGuideDetailsChanged(GuideDetailsChangedEvent event) {
      showLoading(mLoadingContainer, "Creating New Guide...");
      toggleSave(false);

      mGuide = event.guide;
      APIService.call(StepEditActivity.this, APIService.getCreateGuideAPICall(mGuide));
   }

   @Subscribe
   public void onGuideCreated(APIEvent.CreateGuide event) {
      if (!event.hasError()) {
         mPagePosition = 0;

         Guide guide = event.getResult();

         mGuide.setGuideid(guide.getGuideid());
         mGuide.setRevisionid(guide.getRevisionid());
         mGuide.setAuthor(guide.getAuthor());
         mGuide.setPublic(false);
         mGuide.setTitle(guide.getTitle());
         setTitle(guide.getTitle());
         supportInvalidateOptionsMenu();
         save(mPagePosition);
      } else {
         mIsStepDirty = true;
         toggleSave(mIsStepDirty);

         APIService.getErrorDialog(this, event).show();
      }
   }

   @Subscribe
   public void onUploadStepImage(APIEvent.UploadStepImage event) {
      int position = mPagePosition;

      if (!event.hasError()) {
         Image newThumb = event.getResult();

         // Find the temporarily stored image object to update the filename to
         // the image path and imageid.
         if (newThumb != null) {
            ArrayList<Image> images = new ArrayList<Image>(mGuide.getStep(position).getImages());

            int i = 0;
            for (Image image : images) {
               if (image.isLocal()) {
                  newThumb.setLocalPath(image.getPath());
                  images.set(i, newThumb);
                  break;
               }
               i++;
            }

            mGuide.getStep(position).setImages(images);
            refreshView(position);
         }

         if (!mGuide.getStep(position).hasLocalImages()) {
            unlockSave();

            // Set guide dirty after the image is uploaded so the user can't
            // save the guide before we have the imageid.
            onGuideChanged(null);
         }
      } else {
         APIService.getErrorDialog(this, event).show();
      }
   }

   @Subscribe
   public void onStepImageDelete(StepImageDeleteEvent event) {
      mGuide.getStep(mPagePosition).getImages().remove(event.image);

      refreshView(mPagePosition);
   }

   @Subscribe
   public void onStepAdd(APIEvent.StepAdd event) {
      hideLoading();

      if (!event.hasError()) {
         mGuide = event.getResult();

         refreshView(mSavePosition);

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
         // Try to update the step on a conflict.
         if (event.getError().mType == APIError.Type.CONFLICT) {
            try {
               updateCurrentStep(JSONHelper.parseStep(
                new JSONObject(event.getResponse()), 0));
            } catch (JSONException e) {
               Log.e("StepEditActivity", "Error parsing step delete conflict", e);
            }
         }

         APIService.getErrorDialog(this, event).show();
      }
   }

   @Subscribe
   public void onStepLinesChanged(StepLinesChangedEvent event) {
      mGuide.getStepById(event.stepid).setLines(event.lines);
      onGuideChanged(null);
   }

   @Subscribe
   public void onStepTitleChanged(StepTitleChangedEvent event) {
      mGuide.getStepById(event.stepid).setTitle(event.title);
      onGuideChanged(null);
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
      Tracker gaTracker = EasyTracker.getTracker();
      switch (v.getId()) {
         case R.id.step_edit_delete_step:
            gaTracker.sendEvent("ui_action", "button_press", "step_edit_delete_step",
             (long) mGuide.getStep(mPagePosition).getStepid());
            if (!mGuide.getSteps().isEmpty()) {
               createDeleteDialog(StepEditActivity.this).show();
            }
            break;
         case R.id.step_edit_save:
            gaTracker.sendEvent("ui_action", "button_press", "step_edit_save_step",
             (long) mGuide.getStep(mPagePosition).getStepid());

            if (mGuide.isNewGuide()) {
               if (!stepHasLineContent(mGuide.getStep(mPagePosition).getLines())) {
                  Toast.makeText(this, getResources().getString(R.string.guide_create_edit_step_media_cannot_add_step),
                   Toast.LENGTH_SHORT).show();
                  return;
               }
               // DialogFragment.show() will take care of adding the fragment
               // in a transaction.  We also want to remove any currently showing
               // dialog, so make our own transaction and take care of that here.
               FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
               Fragment prev = getSupportFragmentManager().findFragmentByTag("dialog");
               if (prev != null) {
                  ft.remove(prev);
               }
               ft.addToBackStack(null);

               // Create and show the dialog.
               DialogFragment newFragment = NewGuideDialogFragment.newInstance(mGuide);
               newFragment.show(ft, "dialog");
            } else {
               save(mPagePosition);
            }
            break;
         case R.id.step_edit_add_step:
            gaTracker.sendEvent("ui_action", "button_press", "step_edit_add_step", null);

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

   private void addNewStep(int newPosition) {
      if (!mGuide.hasNewStep()) {
         GuideStep step = new GuideStep(newPosition);
         step.addLine(new StepLine());
         mGuide.addStep(step, newPosition);

         refreshView(newPosition);
      } else {
         // Show "Must add content to step" toast
         Toast.makeText(this, getResources().getString(R.string.guide_create_edit_step_media_cannot_add_step),
          Toast.LENGTH_SHORT).show();
      }
   }

   @Override
   public void onBackPressed() {
      if (mGuide != null && mGuide.getRevisionid() == null) {
         super.onBackPressed();
      } else {
         finishEdit(HOME_UP);
      }
   }

   public void enablePublishToggle(boolean enabled) {
   }

   /////////////////////////////////////////////////////
   // ADAPTERS and PRIVATE CLASSES
   /////////////////////////////////////////////////////

   private void refreshView(int position) {
      // The view pager does not recreate the item in the current position unless we force it to.
      initPager();
      mTitleIndicator.notifyDataSetChanged();
      mStepAdapter.notifyDataSetChanged();
      mPager.setCurrentItem(position, false);
   }

   private class StepAdapter extends FragmentStatePagerAdapter {

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
       .setPositiveButton(getString(R.string.save),
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
       .setNegativeButton(R.string.guide_create_confirm_leave_without_save_cancel,
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

      InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
      imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);

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
      return stepHasLineContent(obj.getLines());
   }

   private boolean stepHasLineContent(ArrayList<StepLine> lines) {
      if (lines.size() == 0) {
         return false;
      }

      for (StepLine l : lines) {
         if (l.getTextRaw().length() == 0) {
            return false;
         }
      }

      return true;
   }

   @Override
   public void showLoading(int container) {
      this.showLoading(container, getString(R.string.loading));
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
      mIsLoading = false;

      super.hideLoading();
   }

   protected void navigateToStepView() {
      // Bail early if somehow the user is able to click view guide before the guide is retrieved.
      if (mGuide == null) {
         return;
      }

      EasyTracker.getTracker().sendEvent("menu_action", "button_press", "view_guide", (long) mGuide.getGuideid());

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

         // Necessary because if there were any new steps that were deleted, we need to let the adapters know about
         // it.  Otherwise we get IllegalStateExceptions.
         mStepAdapter.notifyDataSetChanged();
         mTitleIndicator.notifyDataSetChanged();

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
               mIsStepDirty = false;
               toggleSave(false);
               navigateToStepView();
               break;

         }
      }
   }

   private void updateCurrentStep(GuideStep step) {
      // Update the guide on successful save or conflict.
      mGuide.getSteps().set(mSavePosition, step);

      refreshView(mSavePosition);
   }

   protected void deleteStep() {
      mGuide.getSteps().remove(mPagePosition);

      // If it's the last step in the guide, finish the activity.
      if (mGuide.getSteps().size() == 0) {
         mStepAdapter.notifyDataSetChanged();

         finishEdit(HOME_UP);
         return;
      }

      // Disable the save button.
      toggleSave(false);

      int guideSize = mGuide.getSteps().size();

      for (int i = 0; i < guideSize; i++) {
         mGuide.getStep(i).setStepNum(i);
      }

      int newPosition = mPagePosition - 1;

      // The view pager does not recreate the item in the current position unless we force it to.
      refreshView(newPosition);
   }

   /**
    * Toggle the save button state
    *
    * @param toggle true to enable, false to disable
    */
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
