package com.dozuki.ifixit.ui.guide.create;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.Toast;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.MenuItem;
import com.dozuki.ifixit.MainApplication;
import com.dozuki.ifixit.R;
import com.dozuki.ifixit.model.guide.Guide;
import com.dozuki.ifixit.model.guide.GuideStep;
import com.dozuki.ifixit.model.guide.StepLine;
import com.dozuki.ifixit.model.guide.wizard.EditTextPage;
import com.dozuki.ifixit.model.guide.wizard.GuideTitlePage;
import com.dozuki.ifixit.model.guide.wizard.Page;
import com.dozuki.ifixit.model.guide.wizard.TopicNamePage;
import com.dozuki.ifixit.ui.guide.view.GuideViewActivity;
import com.dozuki.ifixit.util.APIError;
import com.dozuki.ifixit.util.APIEvent;
import com.dozuki.ifixit.util.APIService;
import com.squareup.otto.Subscribe;

public class StepPortalFragment extends SherlockFragment implements StepReorderFragment.StepRearrangeListener {
   public static int STEP_ID = 0;
   public static final String DEFAULT_TITLE = "";

   private static final String SHOWING_DELETE = "SHOWING_DELETE";
   private static final String STEP_FOR_DELETE = "STEP_FOR_DELETE";
   private static final int NO_ID = -1;
   private static final String CURRENT_OPEN_ITEM = "CURRENT_OPEN_ITEM";

   private ListView mStepList;
   private StepAdapter mStepAdapter;
   private Guide mGuide;
   private GuideStep mStepForDelete;
   private StepPortalFragment mSelf;
   private int mCurOpenGuideObjectID;
   private boolean mShowingDelete;
   private ActionBar mActionBar;

   /////////////////////////////////////////////////////
   // LIFECYCLE
   /////////////////////////////////////////////////////

   @Override
   public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);

      int guideid = getArguments().getInt(StepsActivity.GUIDE_ID_KEY);
      mActionBar = getSherlockActivity().getSupportActionBar();
      setHasOptionsMenu(true);
      mSelf = this;
      mStepAdapter = new StepAdapter();
      mCurOpenGuideObjectID = NO_ID;

      if (savedInstanceState != null) {
         mCurOpenGuideObjectID = savedInstanceState.getInt(CURRENT_OPEN_ITEM);
         mShowingDelete = savedInstanceState.getBoolean(SHOWING_DELETE);
         mStepForDelete = (GuideStep) savedInstanceState.getSerializable(STEP_FOR_DELETE);
         mGuide = (Guide) savedInstanceState.getSerializable(StepsActivity.GUIDE_KEY);
      }

      if (mGuide == null) {
         ((StepsActivity) getActivity()).showLoading();
         APIService.call(getActivity(), APIService.getGuideForEditAPICall(guideid));
      } else {
         mActionBar.setTitle(mGuide.getTitle());
      }
   }

   @Override
   public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
      View view = inflater.inflate(R.layout.guide_create_steps_portal, container, false);

      mStepList = (ListView) view.findViewById(R.id.steps_portal_list);
      mStepList.setEmptyView(view.findViewById(R.id.no_steps_text));
      mStepList.setAdapter(mStepAdapter);

      if (mShowingDelete) {
         createDeleteDialog(mStepForDelete).show();
      }

      return view;
   }

   @Override
   public void onPause() {
      super.onPause();
      MainApplication.getBus().unregister(this);
   }

   @Override
   public void onResume() {
      super.onResume();
      MainApplication.getBus().register(this);
   }

   @Override
   public void onSaveInstanceState(Bundle savedInstanceState) {
      savedInstanceState.putInt(CURRENT_OPEN_ITEM, mCurOpenGuideObjectID);
      savedInstanceState.putSerializable(STEP_FOR_DELETE, mStepForDelete);
      savedInstanceState.putBoolean(SHOWING_DELETE, mShowingDelete);
      savedInstanceState.putSerializable(StepsActivity.GUIDE_KEY,
       mGuide);

      super.onSaveInstanceState(savedInstanceState);
   }

   @Override
   public void onActivityResult(int requestCode, int resultCode, Intent data) {
      super.onActivityResult(requestCode, resultCode, data);

      if (requestCode == StepsActivity.GUIDE_EDIT_STEP_REQUEST && resultCode == Activity.RESULT_OK) {
         Guide guide = (Guide) data.getSerializableExtra(GuideCreateActivity.GUIDE_KEY);

         if (guide != null) {
            mGuide = guide;
            mStepAdapter = new StepAdapter();
            mStepList.setAdapter(mStepAdapter);
            mStepList.invalidateViews();
         }
      }
   }

   @Override
   public boolean onOptionsItemSelected(MenuItem item) {
      switch (item.getItemId()) {
         case android.R.id.home:
            getActivity().finish();
            break;
         case StepsActivity.MENU_EDIT_INTRO:
            launchGuideIntroEdit();
            break;
         case StepsActivity.MENU_REARRANGE_STEPS:
            if (mGuide.getSteps().size() < 2) {
               Toast.makeText(getActivity(), R.string.step_reorder_insufficient_steps, Toast.LENGTH_SHORT).show();
               break;
            }
            closeSelectedStep();
            launchStepReorderFragment();
            break;
         case StepsActivity.MENU_STEP_ADD:
            GuideStep newStep = new GuideStep(mGuide.getSteps().size() + 1);

            newStep.addLine(new StepLine());
            mGuide.addStep(newStep);

            launchStepEdit(mGuide.getSteps().size());
            break;
         case StepEditActivity.MENU_VIEW_GUIDE:
            Intent intent = new Intent(getActivity(), GuideViewActivity.class);
            intent.putExtra(GuideViewActivity.SAVED_GUIDEID, mGuide.getGuideid());
            intent.putExtra(GuideViewActivity.CURRENT_PAGE, 0);
            startActivity(intent);
            break;
         default:
            return super.onOptionsItemSelected(item);

      }
      return true;
}

   /////////////////////////////////////////////////////
   // NOTIFICATION LISTENERS
   /////////////////////////////////////////////////////

   @Subscribe
   public void onGuideForEdit(APIEvent.GuideForEdit event) {
      if (!event.hasError()) {
         mGuide = event.getResult();

         // Set the page title to the guide name
         mActionBar.setTitle(mGuide.getTitle());

         mStepAdapter.notifyDataSetChanged();

         ((StepsActivity) getActivity()).hideLoading();
      } else {
         event.setError(APIError.getFatalError(getActivity()));
         APIService.getErrorDialog(getActivity(), event.getError(), null).show();
      }
   }

   @Subscribe
   public void onGuideIntroSaved(APIEvent.EditGuide event) {
      if (!event.hasError()) {
         mGuide = event.getResult();

         // Update the page title if the title changed
         if (!mActionBar.getTitle().equals(mGuide.getTitle())) {
            mActionBar.setTitle(mGuide.getTitle());
         }

      } else {
         event.setError(APIError.getFatalError(getActivity()));
         APIService.getErrorDialog(getActivity(), event.getError(), null).show();
      }
   }

   @Subscribe
   public void onGuideStepDeleted(APIEvent.StepRemove event) {
      if (!event.hasError()) {
         mGuide.deleteStep(mStepForDelete);
         for (int i = 0; i < mGuide.getSteps().size(); i++) {
            mGuide.getSteps().get(i).setStepNum(i);
         }

         mStepForDelete = null;
         mGuide.setRevisionid(event.getResult().getRevisionid());
         invalidateViews();
         ((StepsActivity) getActivity()).hideLoading();
      } else {
         event.setError(APIError.getFatalError(getActivity()));
         APIService.getErrorDialog(getActivity(), event.getError(), null).show();
      }
   }

   @Subscribe
   public void onStepReorder(APIEvent.StepReorder event) {
      if (!event.hasError()) {
         mGuide = event.getResult();

         mStepAdapter.notifyDataSetChanged();
         invalidateViews();
         ((StepsActivity) getActivity()).hideLoading();
      } else {
         event.setError(APIError.getFatalError(getActivity()));
         APIService.getErrorDialog(getActivity(), event.getError(), null).show();
      }
   }

   /////////////////////////////////////////////////////
   // EVENT LISTENERS
   /////////////////////////////////////////////////////

   @Override
   public void onReorderComplete(boolean reOrdered) {
      if (reOrdered) {
         mStepAdapter.notifyDataSetChanged();
         ((StepsActivity) getActivity()).showLoading();
         APIService.call((Activity) getActivity(), APIService.getStepReorderAPICall(mGuide));
      }
   }

   /////////////////////////////////////////////////////
   // NAVIGATION
   /////////////////////////////////////////////////////

   protected void launchStepEdit(int curStep) {
      Intent intent = new Intent(getActivity(), StepEditActivity.class);
      intent.putExtra(GuideCreateActivity.GUIDE_KEY, mGuide);
      intent.putExtra(StepEditActivity.GUIDE_STEP_NUM_KEY, curStep);
      startActivityForResult(intent, StepsActivity.GUIDE_EDIT_STEP_REQUEST);
   }

   private void launchGuideIntroEdit() {
      Intent intent = new Intent(getActivity(), GuideIntroActivity.class);
      intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
      intent.putExtra("model", buildIntroBundle());
      intent.putExtra(GuideIntroActivity.STATE_KEY, true);
      intent.putExtra(StepsActivity.GUIDE_KEY, mGuide);
      startActivityForResult(intent, GuideCreateActivity.GUIDE_STEP_EDIT_REQUEST);
   }

   private void launchStepReorderFragment() {
      // Launch the Step reorder fragment
      StepReorderFragment mGuideCreateReOrder = new StepReorderFragment();
      mGuideCreateReOrder.setGuide(mGuide);
      FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
      transaction.add(R.id.guide_create_fragment_steps_container, mGuideCreateReOrder);
      transaction.addToBackStack(null);
      transaction.commitAllowingStateLoss();
   }

   private Bundle buildIntroBundle() {
      Bundle bundle = new Bundle();
      MainApplication app = MainApplication.get();
      String type = mGuide.getType().toLowerCase();
      String subjectBundleKey;

      Bundle topicBundle = new Bundle();
      topicBundle.putString(TopicNamePage.TOPIC_DATA_KEY, mGuide.getTopic());

      Bundle typeBundle = new Bundle();
      typeBundle.putString(Page.SIMPLE_DATA_KEY, type);

      Bundle titleBundle = new Bundle();
      titleBundle.putString(GuideTitlePage.TITLE_DATA_KEY, mGuide.getTitle());

      Bundle summaryBundle = new Bundle();
      summaryBundle.putString(EditTextPage.TEXT_DATA_KEY, mGuide.getSummary());

      Bundle introductionBundle = new Bundle();
      introductionBundle.putString(EditTextPage.TEXT_DATA_KEY, mGuide.getIntroductionRaw());

      Bundle subjectBundle = new Bundle();
      subjectBundle.putString(EditTextPage.TEXT_DATA_KEY, mGuide.getSubject());

      if (type.equals("installation") || type.equals("disassembly") || type.equals("repair")) {
         subjectBundleKey = GuideIntroWizardModel.HAS_SUBJECT_KEY + ":" + app.getString(R.string
          .guide_intro_wizard_guide_subject_title);
      } else {
         subjectBundleKey = GuideIntroWizardModel.NO_SUBJECT_KEY + ":" + app.getString(R.string
          .guide_intro_wizard_guide_subject_title);
      }

      String topicBundleKey = app.getString(R.string.guide_intro_wizard_guide_topic_title, app.getTopicName());

      bundle.putBundle(subjectBundleKey, subjectBundle);
      bundle.putBundle(app.getString(R.string.guide_intro_wizard_guide_type_title), typeBundle);
      bundle.putBundle(topicBundleKey, topicBundle);
      bundle.putBundle(app.getString(R.string.guide_intro_wizard_guide_title_title), titleBundle);
      bundle.putBundle(app.getString(R.string.guide_intro_wizard_guide_introduction_title), introductionBundle);
      bundle.putBundle(app.getString(R.string.guide_intro_wizard_guide_summary_title), summaryBundle);

      return bundle;
   }

   /////////////////////////////////////////////////////
   // ADAPTERS
   /////////////////////////////////////////////////////

   private class StepAdapter extends BaseAdapter {

      @Override
      public int getCount() {
         if (mGuide == null) {
            return 0;
         }
         return mGuide.getSteps().size();
      }

      @Override
      public Object getItem(int position) {
         return mGuide.getSteps().get(position);
      }

      @Override
      public long getItemId(int position) {
         return position;
      }

      @Override
      public View getView(int position, View convertView, ViewGroup parent) {
         StepListItem itemView;
         GuideStep step = (GuideStep) getItem(position);
         if (convertView != null) {
            itemView = (StepListItem) convertView;
         } else {
            itemView = new StepListItem(getActivity(), mSelf);
         }

         itemView.setRowData(step, position);
         itemView.setTag(step.getStepid());

         return itemView;
      }
   }

   /////////////////////////////////////////////////////
   // DIALOGS
   /////////////////////////////////////////////////////

   protected AlertDialog createDeleteDialog(GuideStep item) {
      mStepForDelete = item;
      mShowingDelete = true;
      AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
      builder.setTitle(getString(R.string.confirm_delete_title))
       .setMessage(getString(R.string.step_edit_confirm_delete_message, mStepForDelete.getStepNum()))
       .setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
          public void onClick(DialogInterface dialog, int id) {
             mShowingDelete = false;

             ((StepsActivity) getActivity()).showLoading();
             APIService.call((Activity) getActivity(),
              APIService.getRemoveStepAPICall(mGuide.getGuideid(), mGuide.getRevisionid(), mStepForDelete));
             dialog.cancel();

          }
       }).setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
         public void onClick(DialogInterface dialog, int id) {
            mShowingDelete = false;
            mStepForDelete = null;
         }
      }).setOnDismissListener(new DialogInterface.OnDismissListener() {
         @Override
         public void onDismiss(DialogInterface dialog) {
            mShowingDelete = false;
         }
      });

      return builder.create();
   }

   /////////////////////////////////////////////////////
   // HELPERS
   /////////////////////////////////////////////////////

   protected void invalidateViews() {
      mStepList.invalidateViews();
   }

   protected void onItemSelected(int id, boolean sel) {
      if (!sel) {
         mCurOpenGuideObjectID = NO_ID;
         return;
      }
      closeSelectedStep();
      mCurOpenGuideObjectID = id;
   }

   private void closeSelectedStep() {
      if (mCurOpenGuideObjectID != NO_ID) {
         StepListItem view = ((StepListItem) mStepList.findViewWithTag(mCurOpenGuideObjectID));
         if (view != null) {
            view.setChecked(false);
         }
         for (int i = 0; i < mGuide.getSteps().size(); i++) {
            if (mGuide.getSteps().get(i).getStepid() == mCurOpenGuideObjectID) {
               mGuide.getSteps().get(i).setEditMode(false);
            }
         }
      }
      mCurOpenGuideObjectID = NO_ID;
   }
}
