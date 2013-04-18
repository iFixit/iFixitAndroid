package com.dozuki.ifixit.ui.guide.create;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import com.dozuki.ifixit.MainApplication;
import com.dozuki.ifixit.R;
import com.dozuki.ifixit.model.guide.Guide;
import com.dozuki.ifixit.model.guide.GuideStep;
import com.dozuki.ifixit.model.guide.StepLine;
import com.dozuki.ifixit.util.APIError;
import com.dozuki.ifixit.util.APIEvent;
import com.dozuki.ifixit.util.APIService;
import com.marczych.androidimagemanager.ImageManager;
import com.squareup.otto.Subscribe;
import org.holoeverywhere.LayoutInflater;
import org.holoeverywhere.app.Activity;
import org.holoeverywhere.app.Fragment;
import org.holoeverywhere.widget.ListView;
import org.holoeverywhere.widget.TextView;
import org.holoeverywhere.widget.Toast;

import java.util.ArrayList;

public class StepPortalFragment extends Fragment implements StepReorderFragment.StepRearrangeListener {
   public static int STEP_ID = 0;
   private static final String SHOWING_DELETE = "SHOWING_DELETE";
   private static final String STEP_FOR_DELETE = "STEP_FOR_DELETE";
   public static final String DEFAULT_TITLE = "";
   private static final int NO_ID = -1;
   private static final String CURRENT_OPEN_ITEM = "CURRENT_OPEN_ITEM";
   private ListView mStepList;
   private ImageManager mImageManager;
   private StepAdapter mStepAdapter;
   private TextView mAddStepBar;
   private TextView mEditIntroBar;
   private TextView mReorderStepsBar;
   private Guide mGuide;
   private TextView mNoStepsText;
   private StepPortalFragment mSelf;
   private int mCurOpenGuideObjectID;
   private GuideStep mStepForDelete;
   private boolean mShowingDelete;


   @Override
   public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      if (mImageManager == null) {
         mImageManager = ((MainApplication) getActivity().getApplication()).getImageManager();
      }

      int guidid = this.getArguments().getInt(StepsActivity.GUIDE_KEY);
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
         APIService.call((Activity) getActivity(), APIService.getGuideForEditAPICall(guidid));
      }
   }

   @Override
   public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
      View view = inflater.inflate(R.layout.guide_create_steps_portal, container, false);
      mStepList = (ListView) view.findViewById(R.id.steps_portal_list);
      mAddStepBar = (TextView) view.findViewById(R.id.add_step_bar);
      mAddStepBar.setOnClickListener(new OnClickListener() {
         @Override
         public void onClick(View v) {
            if (mNoStepsText.getVisibility() == View.VISIBLE)
               mNoStepsText.setVisibility(View.GONE);
            GuideStep item = new GuideStep(STEP_ID++);
            item.setStepNum(mGuide.getSteps().size());
            item.setTitle(DEFAULT_TITLE);
            item.addLine(new StepLine());
            launchStepEdit(item);
         }
      });
      mEditIntroBar = (TextView) view.findViewById(R.id.edit_intro_bar);
      mEditIntroBar.setOnClickListener(new OnClickListener() {
         @Override
         public void onClick(View v) {
            launchGuideEditIntro();
         }
      });
      mReorderStepsBar = (TextView) view.findViewById(R.id.reorder_steps_bar);
      mReorderStepsBar.setOnClickListener(new OnClickListener() {
         @Override
         public void onClick(View v) {
            if(mGuide.getSteps().size() < 2) {
               Toast.makeText(getActivity(), R.string.step_reorder_insufficient_steps, Toast.LENGTH_SHORT).show();
               return;
            }
            closeSelectedStep();
            launchStepReorder();
         }
      });

      mNoStepsText = (TextView) view.findViewById(R.id.no_steps_text);
      if (mGuide == null || mGuide.getSteps().isEmpty()) {
         toggleNoStepsText(true);
      }
      mStepList.setAdapter(mStepAdapter);
      if( mShowingDelete) {
         createDeleteDialog(mStepForDelete).show();
      }
      return view;
   }

   @Subscribe
   public void onGuideForEdit(APIEvent.GuideForEdit event) {
      if (!event.hasError()) {
         mGuide = event.getResult();
         mStepAdapter.notifyDataSetChanged();
         if (!mGuide.getSteps().isEmpty()) {
            toggleNoStepsText(false);
         } else {
            toggleNoStepsText(true);
         }
         ((StepsActivity) getActivity()).hideLoading();
      } else {
         event.setError(APIError.getFatalError(getActivity()));
         APIService.getErrorDialog(getActivity(), event.getError(), null).show();
      }
   }

   @Subscribe
   public void onIntroSavedGuide(APIEvent.EditGuide event) {
      if (!event.hasError()) {
         mGuide = event.getResult();
      }
   }

   @Subscribe
   public void onGuideStepDeleted(APIEvent.StepRemove event) {
      if (!event.hasError()) {
         mGuide.deleteStep(mStepForDelete);
         for (int i = 0; i < mGuide.getSteps().size(); i++) {
            mGuide.getSteps().get(i).setStepNum(i);
         }
         if (mGuide.getSteps().isEmpty()) {
            toggleNoStepsText(true);
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
         mGuide.setRevisionid(event.getResult().getRevisionid());
         ((StepsActivity) getActivity()).hideLoading();
      } else {
         event.setError(APIError.getFatalError(getActivity()));
         APIService.getErrorDialog(getActivity(), event.getError(), null).show();
      }
   }

   @Override
   public void onResume() {
      super.onResume();
      MainApplication.getBus().register(this);
   }

   @Override
   public void onPause() {
      super.onPause();
      MainApplication.getBus().unregister(this);
   }

   public void invalidateViews() {
      mStepList.invalidateViews();
   }

   private class StepAdapter extends BaseAdapter {

      @Override
      public int getCount() {
          if(mGuide == null) {
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
         StepListItem itemView = (StepListItem) convertView;
         GuideStep step = (GuideStep)getItem(position);
         itemView = new StepListItem(getActivity(), mImageManager, mSelf, step, position);
         itemView.setTag(step.getStepid());
         return itemView;
      }
   }

   private void launchGuideEditIntro() {
      GuideIntroFragment newFragment = new GuideIntroFragment();
      newFragment.setGuideObject(mGuide);
      FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
      transaction.replace(R.id.guide_create_fragment_steps_container, newFragment);
      transaction.addToBackStack(null);
      transaction.commitAllowingStateLoss();
   }

   void launchStepReorder() {
      StepReorderFragment mGuideCreateReOrder = new StepReorderFragment();
      mGuideCreateReOrder.setGuide(mGuide);
      FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
      transaction.add(R.id.guide_create_fragment_steps_container, mGuideCreateReOrder);
      transaction.addToBackStack(null);
      transaction.commitAllowingStateLoss();
   }

   @Override
   public void onActivityResult(int requestCode, int resultCode, Intent data) {
      if (requestCode == StepsActivity.GUIDE_EDIT_STEP_REQUEST) {
         if (resultCode == Activity.RESULT_OK) {
            Guide guide = (Guide)data.getSerializableExtra(GuideCreateActivity.GUIDE_KEY);
            if (guide != null) {
               mGuide = guide;
               mStepAdapter = new StepAdapter();
               mStepList.setAdapter(mStepAdapter);
               if (mGuide.getSteps().isEmpty()) {
                  mNoStepsText.setVisibility(View.VISIBLE);
               }
               mStepList.invalidateViews();
            }
         }
      }
   }

   void deleteStep(GuideStep step) {
      createDeleteDialog(step).show();
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

   public void onItemSelected(int id, boolean sel) {
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

   public AlertDialog createDeleteDialog(GuideStep item) {
      mStepForDelete = item;
      mShowingDelete = true;
      AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
      builder.setTitle(getString(R.string.confirm_delete_title))
         .setMessage(getString(R.string.confirm_delete_body) + " Step " + (mStepForDelete.getStepNum() + 1) + "?")
         .setPositiveButton(getString(R.string.confirm_delete_confirm), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
               mShowingDelete = false;

               ((StepsActivity)getActivity()).showLoading();
               APIService.call((Activity) getActivity(),
                  APIService.getRemoveStepAPICall(mGuide.getGuideid(), mGuide.getRevisionid(), mStepForDelete));
               dialog.cancel();

            }
         }).setNegativeButton(getString(R.string.confirm_delete_cancel), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
               mShowingDelete = false;
               mStepForDelete = null;
            }
         });

      AlertDialog dialog = builder.create();
      dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
         @Override
         public void onDismiss(DialogInterface dialog) {
            mShowingDelete = false;
         }
      });

      return dialog;
   }

   @Override
   public void onReorderComplete(boolean reOrdered) {
      if (reOrdered) {
         mStepAdapter.notifyDataSetChanged();
         ((StepsActivity)getActivity()).showLoading();
         APIService.call((Activity) getActivity(), APIService.getStepReorderAPICall(mGuide));
      }
   }

   void launchStepEdit(ArrayList<GuideStep> stepList, int curStep) {
      Intent intent = new Intent(getActivity(), StepsEditActivity.class);
      intent.putExtra(GuideCreateActivity.GUIDE_KEY, mGuide);
      intent.putExtra(StepsEditActivity.GUIDE_STEP_LIST_KEY, stepList);
      intent.putExtra(StepsEditActivity.GUIDE_STEP_KEY, curStep);
      startActivityForResult(intent, StepsActivity.GUIDE_EDIT_STEP_REQUEST);
   }

   void launchStepEdit(GuideStep curStep) {
      ArrayList<GuideStep> stepList = new ArrayList<GuideStep>();
      stepList.addAll(mGuide.getSteps());
      stepList.add(curStep);
      launchStepEdit(stepList, stepList.size() - 1);
   }

   void launchStepEdit(int curStep) {
      ArrayList<GuideStep> stepList = new ArrayList<GuideStep>();
      stepList.addAll(mGuide.getSteps());
      launchStepEdit(stepList, curStep);
   }


   public void toggleNoStepsText(boolean show) {
      if (mNoStepsText == null) {
         return;
      }

      if (show) {
         mNoStepsText.setVisibility(View.VISIBLE);
      } else {
         mNoStepsText.setVisibility(View.GONE);
      }
   }
}
