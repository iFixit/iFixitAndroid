package com.dozuki.ifixit.ui.guide.create;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.support.v7.widget.AppCompatAutoCompleteTextView;
import android.support.v7.widget.AppCompatSpinner;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import com.dozuki.ifixit.App;
import com.dozuki.ifixit.R;
import com.dozuki.ifixit.model.guide.Guide;
import com.dozuki.ifixit.ui.BaseDialogFragment;
import com.dozuki.ifixit.util.api.Api;
import com.dozuki.ifixit.util.api.ApiCall;
import com.dozuki.ifixit.util.api.ApiEvent;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.regex.Pattern;

public class NewGuideDialogFragment extends BaseDialogFragment {
   private static final String INVALID_DEVICE_NAME_PATTERN = "[^#<>\\[\\]\\|\\{\\},\\+\\?&\\/\\\\\\%:;]+";

   private static final String GUIDE_KEY = "GUIDE_KEY";
   private static final String TOPIC_LIST_KEY = "TOPIC_LIST_KEY";
   private static String mTopicName;
   private Guide mGuide;
   private AppCompatSpinner mType;
   private TextInputEditText mSubject;
   private AppCompatAutoCompleteTextView mTopic;
   private ArrayList<String> mTopics;
   private ArrayAdapter<String> mAdapter;

   public static NewGuideDialogFragment newInstance(Guide guide) {
      mTopicName = App.get().getTopicName();
      NewGuideDialogFragment frag = new NewGuideDialogFragment();

      Bundle bundle = new Bundle();
      bundle.putSerializable(GUIDE_KEY, guide);
      frag.setArguments(bundle);

      frag.setStyle(STYLE_NO_TITLE, android.R.style.Theme_Holo_Light_Dialog);

      return frag;
   }

   @Override
   public Dialog onCreateDialog(Bundle savedInstanceState) {
      if (savedInstanceState != null) {
         mGuide = (Guide) savedInstanceState.getSerializable(GUIDE_KEY);
         mTopics = (ArrayList<String>) savedInstanceState.getSerializable(TOPIC_LIST_KEY);
      } else {
         mGuide = (Guide) getArguments().getSerializable(GUIDE_KEY);
      }

      AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.Base_Dialog_Alert)
       .setTitle(getResources().getString(R.string.new_guide_details_title))
       .setPositiveButton(getResources().getString(R.string.done), new DialogInterface.OnClickListener() {
          public void onClick(DialogInterface dialog, int whichButton) {
             boolean error = false;
             String type = (String) mType.getItemAtPosition(mType.getSelectedItemPosition());
             Editable topic = mTopic.getText();

             if (App.get().getSite().hasSubject(type)) {
                Editable subject = mSubject.getText();

                if (subject.length() == 0) {
                   error = true;
                   mSubject.setError(getString(R.string.guide_subject_required_error));
                } else {
                   mGuide.setSubject(subject.toString());
                }
             }

             if (topic.length() == 0) {
                error = true;
                mTopic.setError(getString(R.string.topic_name_required_error, mTopicName));
             }

             if (!error) {
                mGuide.setType(type);
                mGuide.setTopic(topic.toString());

                App.getBus().post(new GuideDetailsChangedEvent(mGuide));
                getDialog().dismiss();
             }
          }
       })
       .setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
             dialog.dismiss();
          }
       });


       View v = getActivity().getLayoutInflater().inflate(R.layout.new_guide_fragment_dialog, null);
      mTopic = (AppCompatAutoCompleteTextView) v.findViewById(R.id.topic_name_field);
      mTopic.setHint(getString(R.string.guide_intro_wizard_guide_topic_title, mTopicName));
      mTopic.addTextChangedListener(new TextWatcher() {
         @Override
         public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

         @Override
         public void afterTextChanged(Editable editable) { }

         @Override
         public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            if (!Pattern.matches(INVALID_DEVICE_NAME_PATTERN, charSequence)) {
               mTopic.setError(getString(R.string.device_invalid_characters_error_message));
            }
         }
      });

      String topic = mGuide.getTopic();
      if (topic != null && topic.length() != 0) {
         mTopic.setText(topic);
      }

      if (mTopics != null) {
         setTopicArrayAdapter();
      } else {
         Api.call(getActivity(), ApiCall.allTopics());
      }

      mSubject = (TextInputEditText) v.findViewById(R.id.subject_field);

      String subject = mGuide.getSubject();

      if (subject != null && subject.length() != 0) {
         mSubject.setText(subject);
      }

      mType = (AppCompatSpinner) v.findViewById(R.id.guide_types_spinner);

      // Create an ArrayAdapter using the string array and a default spinner layout
      ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item,
       App.get().getSite().getGuideTypesArray());

      // Specify the layout to use when the list of choices appears
      adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

      // Apply the adapter to the spinner
      mType.setAdapter(adapter);

      mType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
         @Override
         public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            String[] guideTypes = App.get().getSite().getGuideTypesArray();
            int visibility;

            if (!App.get().getSite().hasSubject(guideTypes[position])) {
               visibility = View.GONE;
               mTopic.setImeOptions(EditorInfo.IME_ACTION_DONE);
            } else {
               visibility = View.VISIBLE;
               mTopic.setImeOptions(EditorInfo.IME_ACTION_NEXT);
            }

            mSubject.setVisibility(visibility);
         }

         @Override
         public void onNothingSelected(AdapterView<?> parent) {
            // No need to do anything
         }
      });

      String type = mGuide.getType();
      if (type != null && type.length() > 0) {
         mType.setSelection(adapter.getPosition(type));
      }

      builder.setView(v);

       return builder.create();
   }

   @Override
   public void onSaveInstanceState(Bundle outState) {
      super.onSaveInstanceState(outState);

      Editable topic = mTopic.getText();
      if (topic != null)
         mGuide.setTopic(topic.toString());

      if (mSubject != null) {
         Editable subject = mSubject.getText();
         if (subject != null) {
            mGuide.setSubject(subject.toString());
         }
      }

      outState.putSerializable(GUIDE_KEY, mGuide);
      outState.putStringArrayList(TOPIC_LIST_KEY, mTopics);
   }

   @Subscribe
   public void onTopicList(ApiEvent.TopicList event) {
      if (!event.hasError()) {
         mTopics = new ArrayList<String>(event.getResult());

         setTopicArrayAdapter();
      } else {
         Api.getErrorDialog(getActivity(), event).show();
      }
   }

   private void setTopicArrayAdapter() {
      mAdapter = new ArrayAdapter<String>(getActivity(), R.layout.topic_name_autocomplete_dropdown_item,
       mTopics);

      mTopic.setAdapter(mAdapter);
   }
}
