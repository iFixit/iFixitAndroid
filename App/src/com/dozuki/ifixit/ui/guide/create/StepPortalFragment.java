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

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.dozuki.ifixit.R;
import com.dozuki.ifixit.model.guide.Guide;
import com.dozuki.ifixit.model.guide.GuideStep;
import com.dozuki.ifixit.model.guide.StepLine;
import com.dozuki.ifixit.ui.BaseFragment;
import com.dozuki.ifixit.ui.guide.view.GuideViewActivity;
import com.dozuki.ifixit.util.api.ApiCall;
import com.dozuki.ifixit.util.api.ApiError;
import com.dozuki.ifixit.util.api.ApiEvent;
import com.dozuki.ifixit.util.api.Api;
import com.squareup.otto.Subscribe;

public class StepPortalFragment extends BaseFragment implements
 StepReorderFragment.StepRearrangeListener {
   public static int STEP_ID = 0;

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
         Api.call(getActivity(), ApiCall.unpatrolledGuide(guideid));
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
   public void onSaveInstanceState(Bundle savedInstanceState) {
      super.onSaveInstanceState(savedInstanceState);

      savedInstanceState.putInt(CURRENT_OPEN_ITEM, mCurOpenGuideObjectID);
      savedInstanceState.putSerializable(STEP_FOR_DELETE, mStepForDelete);
      savedInstanceState.putBoolean(SHOWING_DELETE, mShowingDelete);
      savedInstanceState.putSerializable(StepsActivity.GUIDE_KEY, mGuide);
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
   public void onPrepareOptionsMenu(Menu menu) {
      if (mGuide != null) {
         menu.findItem(R.id.reorder_steps).setVisible(mGuide.getSteps().size() >= 2);
      }

      super.onPrepareOptionsMenu(menu);
   }

   @Override
   public boolean onOptionsItemSelected(MenuItem item) {
      switch (item.getItemId()) {
         case android.R.id.home:
            getActivity().finish();
            return true;
         case R.id.edit_guide_details:
            launchGuideIntroEdit();
            return true;
         case R.id.reorder_steps:
            closeSelectedStep();
            launchStepReorderFragment();
            return true;
         case R.id.add_step:
            GuideStep newStep = new GuideStep(mGuide.getSteps().size() + 1);

            newStep.addLine(new StepLine());
            mGuide.addStep(newStep);

            launchStepEdit(mGuide.getSteps().size());
            return true;
         case R.id.view_guide:
            Intent intent = new Intent(getActivity(), GuideViewActivity.class);
            intent.putExtra(GuideViewActivity.GUIDEID, mGuide.getGuideid());
            intent.putExtra(GuideViewActivity.CURRENT_PAGE, 0);
            startActivity(intent);
            return true;
         default:
            return super.onOptionsItemSelected(item);
      }
   }

   /////////////////////////////////////////////////////
   // NOTIFICATION LISTENERS
   /////////////////////////////////////////////////////

   @Subscribe
   public void onGuideForEdit(ApiEvent.GuideForEdit event) {
      if (!event.hasError()) {
         mGuide = event.getResult();

         // Set the page title to the guide name
         mActionBar.setTitle(mGuide.getTitle());

         mStepAdapter.notifyDataSetChanged();

         ((StepsActivity) getActivity()).hideLoading();
      } else {
         Api.getErrorDialog(getActivity(), event).show();
      }
   }

   @Subscribe
   public void onGuideIntroSaved(ApiEvent.EditGuide event) {
      if (!event.hasError()) {
         mGuide = event.getResult();

         // Update the page title if the title changed
         if (!mActionBar.getTitle().equals(mGuide.getTitle())) {
            mActionBar.setTitle(mGuide.getTitle());
         }
      } else {
         Api.getErrorDialog(getActivity(), event).show();
      }
   }

   @Subscribe
   public void onGuideStepDeleted(ApiEvent.StepRemove event) {
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
         Api.getErrorDialog(getActivity(), event).show();
      }
   }

   @Subscribe
   public void onStepReorder(ApiEvent.StepReorder event) {
      ((StepsActivity)getActivity()).hideLoading();

      if (!event.hasError() || event.getError().mType == ApiError.Type.CONFLICT) {
         mGuide = event.getResult();

         mStepAdapter.notifyDataSetChanged();
         invalidateViews();
      }

      if (event.hasError()) {
         Api.getErrorDialog(getActivity(), event).show();
      }
   }

   /////////////////////////////////////////////////////
   // EVENT LISTENERS
   /////////////////////////////////////////////////////

   @Override
   public void onReorderComplete(boolean reodered) {
      if (reodered) {
         mStepAdapter.notifyDataSetChanged();
         ((StepsActivity) getActivity()).showLoading();
         Api.call(getActivity(), ApiCall.reorderSteps(mGuide));
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
             Api.call(getActivity(),
              ApiCall.deleteStep(mGuide.getGuideid(), mStepForDelete));
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
         for (GuideStep step : mGuide.getSteps()) {
            if (step.getStepid() == mCurOpenGuideObjectID) {
               step.setEditMode(false);
            }
         }
      }
      mCurOpenGuideObjectID = NO_ID;
   }
}
