package com.dozuki.ifixit.guide_create.ui;

import android.os.Bundle;
import android.util.Log;
import org.holoeverywhere.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import org.holoeverywhere.widget.Button;
import org.holoeverywhere.widget.EditText;
import org.holoeverywhere.widget.TextView;
import org.holoeverywhere.widget.Spinner;

import org.holoeverywhere.app.Fragment;
import com.dozuki.ifixit.R;
import com.dozuki.ifixit.guide_create.model.GuideCreateObject;

public class GuideIntroFragment extends Fragment {

   public interface GuideCreateIntroListener {
      void
         onFinishIntroInput(String device, String title, String summary, String intro, String guideType, String thing);
   }

   private static String DEVICE_TYPE_KEY = "DEVICE_TYPE_KEY";
   private static String TITLE_KEY = "TITLE_KEY";
   private static String SUMMARY_KEY = "SUMMARY_KEY";
   private static String INRODUCTION_KEY = "INRODUCTION_KEY";
   private TextView mViewHeader;
   private EditText mDeviceType;
   private EditText mTitle;
   private EditText mSummary;
   private EditText mFocus;
   private EditText mIntroduction;
   private TextView mErrorText;
   private Spinner mGuideTypeSpinner;
   private Button mSubmitGuideButton;
   private GuideCreateObject mGuideObject;

   @Override
   public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
   }

   @Override
   public void onSaveInstanceState(Bundle savedInstanceState) {
      super.onSaveInstanceState(savedInstanceState);
      savedInstanceState.putString(DEVICE_TYPE_KEY, mDeviceType.getText().toString());
      savedInstanceState.putString(TITLE_KEY, mTitle.getText().toString());
      savedInstanceState.putString(SUMMARY_KEY, mSummary.getText().toString());
      savedInstanceState.putString(INRODUCTION_KEY, mIntroduction.getText().toString());
   }

   public void setGuideOBject(GuideCreateObject obj) {
      mGuideObject = obj;
   }

   @Override
   public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
      View view = inflater.inflate(R.layout.guide_create_intro, container, false);
      mViewHeader = (TextView) view.findViewById(R.id.guide_intro_header);
      if(mGuideObject != null)
         mViewHeader.setText(R.string.create_guide_header_edit);
      mSubmitGuideButton = (Button) view.findViewById(R.id.confirm_create_guide_button);
      mDeviceType = (EditText) view.findViewById(R.id.edit_guide_intro_device_id);
      mFocus = (EditText) view.findViewById(R.id.edit_guide_intro_focus);
      mTitle = (EditText) view.findViewById(R.id.edit_guide_intro_title);
      mSummary = (EditText) view.findViewById(R.id.edit_guide_intro_summary);
      mIntroduction = (EditText) view.findViewById(R.id.edit_guide_intro_introduction_text);
      mErrorText = (TextView) view.findViewById(R.id.guide_intro_error_text);
      mGuideTypeSpinner = (Spinner) view.findViewById(R.id.guide_intro_type_spinner);

      if (savedInstanceState != null) {
         mDeviceType.setText(savedInstanceState.getString(DEVICE_TYPE_KEY));
         mTitle.setText(savedInstanceState.getString(TITLE_KEY));
         mSummary.setText(savedInstanceState.getString(SUMMARY_KEY));
         mIntroduction.setText(savedInstanceState.getString(INRODUCTION_KEY));
      }

      mSubmitGuideButton.setOnClickListener(new OnClickListener() {
         @Override
         public void onClick(View v) {
            /*
             * if( device.length() == 0)
             * {
             * mErrorText.setVisibility(View.VISIBLE);
             * }
             * else
             */
            Log.i("TITLE", mTitle.getText().toString());
            confirmCreateGuide(mDeviceType.getText().toString(), mTitle.getText().toString(), mSummary.getText()
               .toString(), mIntroduction.getText().toString(), (String) mGuideTypeSpinner.getSelectedItem(), mFocus
               .getText().toString());
         }
      });

      if (mGuideObject != null) {
         mDeviceType.setText(mGuideObject.getTopic());
         mTitle.setText(mGuideObject.getTitle());
         mSummary.setText(mGuideObject.getSummary());
         mIntroduction.setText(mGuideObject.getIntroduction());
      }

      return view;
   }

   private void confirmCreateGuide(String device, String title, String summary, String intro, String guideType,
      String thing) {
      ((GuideCreateIntroListener) getActivity()).onFinishIntroInput(device, title, summary, intro, guideType, thing);
   }
}
