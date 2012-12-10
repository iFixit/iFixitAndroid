package com.dozuki.ifixit.guide_create.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragment;
import com.dozuki.ifixit.R;
import com.dozuki.ifixit.guide_create.model.GuideCreateObject;

public class GuideIntroFragment extends SherlockFragment {
	
	private static String DeviceTypeKey = "DeviceType";
	private static String TitleKey = "DeviceType";
	private static String SummaryKey = "DeviceType";
	private static String IntroductionKey = "DeviceType";
	EditText mDeviceType;
	EditText mTitle;
	EditText mSummary;
	EditText mIntroduction;
	TextView mErrorText;
	Button mSubmitGuideButton;
	GuideCreateObject mGuideObject;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}
	
	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		super.onSaveInstanceState(savedInstanceState);
		savedInstanceState.putString(DeviceTypeKey, mDeviceType.getText().toString());
		savedInstanceState.putString(TitleKey , mTitle.getText().toString());
		savedInstanceState.putString(SummaryKey, mSummary.getText().toString());
		savedInstanceState.putString(IntroductionKey, mIntroduction.getText().toString());
	}
	
	public void setGuideOBject(GuideCreateObject obj)
	{
		mGuideObject = obj;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.guide_create_intro, container,
				false);
		mSubmitGuideButton = (Button)view.findViewById(R.id.confirm_create_guide_button);
		mDeviceType = (EditText)view.findViewById(R.id.edit_guide_intro_device_id);
		mTitle = (EditText)view.findViewById(R.id.edit_guide_intro_title);
		mSummary = (EditText)view.findViewById(R.id.edit_guide_intro_summary);
		mIntroduction = (EditText)view.findViewById(R.id.edit_guide_intro_introduction_text);
		mErrorText = (TextView)view.findViewById(R.id.guide_intro_error_text);
		
		if(savedInstanceState != null)
		{
			mDeviceType.setText(savedInstanceState.getString(DeviceTypeKey));
			mTitle.setText(savedInstanceState.getString(TitleKey));
			mSummary.setText(savedInstanceState.getString(SummaryKey));
			mIntroduction.setText(savedInstanceState.getString(IntroductionKey));
		}
		
		mSubmitGuideButton.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				/*if( device.length() == 0)
				{
					mErrorText.setVisibility(View.VISIBLE);
				}
				else*/
				
					Log.i("TITLE", mTitle.getText().toString());
					confirmCreateGuide(mDeviceType.getText().toString(), mTitle.getText().toString(), mSummary.getText().toString(), mIntroduction.getText().toString());
				
			}
			
		});
		
		if(mGuideObject != null)
		{
		   mDeviceType.setText(mGuideObject.getTopic());
		   mTitle.setText(mGuideObject.getTitle());
		   mSummary.setText(mGuideObject.getSummary());
		   mIntroduction.setText(mGuideObject.getIntroduction());
		}
		
		return view;
	}
	
	private void confirmCreateGuide(String device, String title, String summary, String intro)
	{
		if(mGuideObject == null) return;
		mGuideObject.setTitle(title);
		mGuideObject.setTopic(device);
		mGuideObject.setSummary(summary);
		mGuideObject.setIntroduction(intro);
		getActivity().getSupportFragmentManager().popBackStack();
	}
}
