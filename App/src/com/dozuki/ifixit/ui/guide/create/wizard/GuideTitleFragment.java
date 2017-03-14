package com.dozuki.ifixit.ui.guide.create.wizard;

import android.app.Activity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.dozuki.ifixit.App;
import com.dozuki.ifixit.R;
import com.dozuki.ifixit.model.guide.wizard.EditTextPage;
import com.dozuki.ifixit.model.guide.wizard.GuideTitlePage;
import com.dozuki.ifixit.model.guide.wizard.Page;
import com.dozuki.ifixit.model.guide.wizard.SingleFixedChoicePage;
import com.dozuki.ifixit.model.guide.wizard.TopicNamePage;
import com.dozuki.ifixit.ui.guide.create.GuideIntroWizardModel;

public class GuideTitleFragment extends EditTextFragment {

   private static final String ARG_KEY = "key";

   private PageFragmentCallbacks mCallbacks;
   private String mKey;
   private GuideTitlePage mPage;
   private EditText mField;

   public GuideTitleFragment() { }

   public static GuideTitleFragment create(String key) {
      Bundle args = new Bundle();
      args.putString(ARG_KEY, key);

      GuideTitleFragment fragment = new GuideTitleFragment();
      fragment.setArguments(args);
      return fragment;
   }

   @Override
   public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);

      Bundle args = getArguments();
      mKey = args.getString(ARG_KEY);
   }

   @Override
   public View onCreateView(LayoutInflater inflater, ViewGroup container,
    Bundle savedInstanceState) {
      View rootView = super.onCreateView(inflater, container, savedInstanceState);

      mPage = (GuideTitlePage) mCallbacks.onGetPage(mKey);

      mField = (EditText) rootView.findViewById(R.id.edit_text_field);

      // If there isn't a title already, suggest one.  Example: {{Guide Type}} {{Guide Topic}} {{Guide Subject}}
      if (mField.getText().toString().length() == 0) {
         mField.setText(buildTitle());
      }

      // Move cursor to end of text field
      mField.setSelection(mField.getText().length());

      return rootView;
   }

   @Override
   public void onAttach(Activity activity) {
      super.onAttach(activity);

      if (!(activity instanceof PageFragmentCallbacks)) {
         throw new ClassCastException("Activity must implement PageFragmentCallbacks");
      }

      mCallbacks = (PageFragmentCallbacks) activity;


   }

   @Override
   public void onDetach() {
      super.onDetach();
      mCallbacks = null;
   }

   private String buildTitle() {
      String title;

      String guideType = getPageData(getString(R.string.guide_intro_wizard_guide_type_title),
       SingleFixedChoicePage.SIMPLE_DATA_KEY).toLowerCase();

      String guideTopic = getPageData(getString(R.string.guide_intro_wizard_guide_topic_title,
       App.get().getTopicName()), TopicNamePage.TOPIC_DATA_KEY);

      if (guideType.equals("technique") || guideType.equals("maintenance") || guideType.equals("teardown")) {
         title = String.format("%s %s", guideTopic, guideType.substring(0, 1).toUpperCase() + guideType.substring(1));
      } else {
         guideType = transformTypeToAction(guideType);
         String subjectKey = GuideIntroWizardModel.HAS_SUBJECT_KEY + ":" + getString(R.string
          .guide_intro_wizard_guide_subject_title);
         String guideSubject = getPageData(subjectKey, EditTextPage.TEXT_DATA_KEY);

         title = String.format("%s %s %s", guideType.substring(0, 1).toUpperCase() + guideType.substring(1),
          guideTopic, guideSubject);
      }
      Bundle titleBundle = new Bundle();
      titleBundle.putString(GuideTitlePage.TITLE_DATA_KEY, title);

      mPage.resetData(titleBundle);
      mPage.notifyDataChanged();

      return title;
   }


   @Override
   public void onViewCreated(View view, Bundle savedInstanceState) {
      super.onViewCreated(view, savedInstanceState);

      mField.addTextChangedListener(new TextWatcher() {
         @Override
         public void beforeTextChanged(CharSequence charSequence, int i, int i1,
          int i2) {
         }

         @Override
         public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
         }

         @Override
         public void afterTextChanged(Editable editable) {
            mPage.getData().putString(GuideTitlePage.TITLE_DATA_KEY, (editable != null) ? editable.toString() : null);
            mPage.notifyDataChanged();
         }
      });
   }

   private String getPageData(String pageKey, String dataKey) {
      Page page = mCallbacks.onGetPage(pageKey);

      if (page != null) {
         Bundle pageData = page.getData();
         if (pageData.containsKey(dataKey)) {
            return pageData.getString(dataKey);
         }
      }
      return "";
   }

   private String transformTypeToAction(String type) {
      if (type.equals("installation")) {
         return "Installing";
      } else if (type.equals("disassembly")) {
         return "Disassembling";
      } else if (type.equals("repair")) {
         return "Repairing";
      } else {
         return type;
      }

   }
}
