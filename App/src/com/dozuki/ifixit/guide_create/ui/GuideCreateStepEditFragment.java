package com.dozuki.ifixit.guide_create.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageView;

import com.actionbarsherlock.app.SherlockFragment;
import com.dozuki.ifixit.R;
import com.dozuki.ifixit.gallery.ui.GalleryActivity;
import com.dozuki.ifixit.guide_create.model.GuideCreateStepObject;

public class GuideCreateStepEditFragment extends SherlockFragment implements
		OnClickListener {
	public static int FetchImageKey = 1;
	public static String ThumbPositionKey = "ThumbPositionKey";
	private static String GuideEditKey = "GuideEditKey";
	GuideCreateStepObject mStepObject;
	EditText mStepTitle;
	ImageView mLargeImage;
	ImageView mImageOne;
	ImageView mImageTwo;
	ImageView mImageThree;
	ImageView mMediaIcon;

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
		mLargeImage = (ImageView) view.findViewById(R.id.step_edit_large_image);
		mImageOne = (ImageView) view.findViewById(R.id.step_edit_thumb_1);
		mImageOne.setOnClickListener(this);
		mImageTwo = (ImageView) view.findViewById(R.id.step_edit_thumb_2);
		mImageTwo.setOnClickListener(this);
		mImageThree = (ImageView) view.findViewById(R.id.step_edit_thumb_3);
		mImageThree.setOnClickListener(this);
		mMediaIcon = (ImageView) view.findViewById(R.id.step_edit_thumb_media);
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

	@Override
	public void onClick(View v) {
		Intent intent;
		switch (v.getId()) {
		case R.id.step_edit_thumb_1:
			intent = new Intent(getActivity(), GalleryActivity.class);
			intent.putExtra(ThumbPositionKey, 1);
			startActivityForResult(intent, FetchImageKey);
			break;
		case R.id.step_edit_thumb_2:
			intent = new Intent(getActivity(), GalleryActivity.class);
			intent.putExtra(ThumbPositionKey, 2);
			startActivityForResult(intent, FetchImageKey);
			break;
		case R.id.step_edit_thumb_3:
			intent = new Intent(getActivity(), GalleryActivity.class);
			intent.putExtra(ThumbPositionKey, 3);
			startActivityForResult(intent, FetchImageKey);
			break;
		case R.id.step_edit_thumb_media:
			break;
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {

	}
}
