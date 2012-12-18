package com.dozuki.ifixit.guide_create.ui;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.actionbarsherlock.app.SherlockFragment;
import com.dozuki.ifixit.R;
import com.dozuki.ifixit.guide_create.model.GuideCreateStepObject;

public class GuideCreateStepEditFragment extends SherlockFragment {
	private static String GuideEditKey = "GuideEditKey";
	GuideCreateStepObject mStepObject;
	EditText mStepTitle;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.guide_create_step_edit_body,
				container, false);
		mStepTitle = (EditText) view.findViewById(R.id.step_edit_title_text);
		if (savedInstanceState != null) {
			mStepObject = (GuideCreateStepObject) savedInstanceState
					.getSerializable(GuideCreateStepEditFragment.GuideEditKey);
		}
		mStepTitle.setText(mStepObject.getTitle());
		mStepTitle.addTextChangedListener(new TextWatcher() {

			@Override
			public void afterTextChanged(Editable s) {

			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				Log.i("GuideCreateStepEditFragment", "GuideTitle changed to: "
						+ s.toString());
				mStepObject.setTitle(s.toString());
			}

		});
		return view;
	}

	public void setStepObject(GuideCreateStepObject stepObject) {
		mStepObject = stepObject;
	}

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		super.onSaveInstanceState(savedInstanceState);
		savedInstanceState.putSerializable(
				GuideCreateStepEditFragment.GuideEditKey, mStepObject);
	}
}
