package com.dozuki.ifixit.ui.guide.view;

import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.dozuki.ifixit.App;
import com.dozuki.ifixit.R;
import com.dozuki.ifixit.model.guide.Guide;
import com.dozuki.ifixit.model.user.LoginEvent;
import com.dozuki.ifixit.ui.BaseFragment;
import com.dozuki.ifixit.util.api.Api;
import com.dozuki.ifixit.util.api.ApiCall;
import com.dozuki.ifixit.util.api.ApiEvent;
import com.squareup.otto.Subscribe;

public class GuideConclusionFragment extends BaseFragment {
   private static final String SAVED_GUIDE = "SAVED_GUIDE";
   private Guide mGuide;
   private Button mButton;

   public static GuideConclusionFragment newInstance(Guide guide) {
      GuideConclusionFragment frag = new GuideConclusionFragment();
      Bundle args = new Bundle();
      args.putSerializable(SAVED_GUIDE, guide);
      frag.setArguments(args);
      return frag;
   }

   @Override
   public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);

      if (getArguments() != null) {
         mGuide = (Guide) getArguments().getSerializable(SAVED_GUIDE);
      } else if (savedInstanceState != null && mGuide == null) {
         mGuide = (Guide) savedInstanceState.getSerializable(SAVED_GUIDE);
      }
   }

   @Override
   public View onCreateView(LayoutInflater inflater, ViewGroup container,
    Bundle savedInstanceState) {
      View view = inflater.inflate(R.layout.guide_conclusion, container, false);

      ((TextView) view.findViewById(R.id.guide_conclusion_text)).setText(Html.fromHtml(mGuide.getConclusion()));

      mButton = (Button) view.findViewById(R.id.guide_completed_button);
      mButton.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View v) {
            Api.call(getActivity(), ApiCall.completeGuide(mGuide.getGuideid()));
            mButton.setEnabled(false);
            mButton.setText(R.string.completing);
         }
      });

      setCompletedStatus(mGuide.getCompleted());

      if (mGuide.isTeardown()) mButton.setVisibility(View.GONE);

      return view;
   }

   @Override
   public void onSaveInstanceState(Bundle state) {
      super.onSaveInstanceState(state);

      state.putSerializable(SAVED_GUIDE, mGuide);
   }

   @Subscribe
   public void onGuideComplete(ApiEvent.CompleteGuide event) {
      if (!event.hasError()) {
         boolean completed = event.getResult();
         mGuide.setCompleted(completed);
         setCompletedStatus(completed);
      } else {
         // Reset the button to the current state.
         setCompletedStatus(mGuide.getCompleted());
         Api.getErrorDialog(getActivity(), event).show();
      }
   }

   @Subscribe
   public void onCancelLogin(LoginEvent.Cancel event) {
      setCompletedStatus(false);
   }

   private void setCompletedStatus(boolean completed) {
      mButton.setEnabled(!completed);
      mButton.setText(completed ? R.string.completed :
       (App.get().getSite().isIfixit() ? R.string.i_did_it_success : R.string.complete_this_guide));
   }
}
