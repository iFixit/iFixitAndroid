package com.dozuki.ifixit.guide_create.ui;

import com.dozuki.ifixit.R;
import com.dozuki.ifixit.guide_create.model.GuideCreateObject;
import com.ifixit.android.imagemanager.ImageManager;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

public class GuideCreateListItem extends RelativeLayout {
	private TextView mTitleView;
	private ImageView mThumbnail;
	private Button mDeleteButton;
	private Button mEditButton;
	private ToggleButton mToggleEdit;
	private LinearLayout mEditBar;
	private ImageManager mImageManager;
	private GuidePortalFragment mPortalRef;
	private Context mContext;
	private boolean editBarVisible = false;
	private GuideCreateObject mGuideCreateObject;

	public GuideCreateListItem(Context context, ImageManager imageManager,
			final GuidePortalFragment portalRef, GuideCreateObject gObject) {
		super(context);
		mContext = context;
		mPortalRef = portalRef;
		mImageManager = imageManager;
		mGuideCreateObject = gObject;
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.guide_create_item, this, true);
		mTitleView = (TextView) findViewById(R.id.guide_create_item_title);
		mThumbnail = (ImageView) findViewById(R.id.guide_create_item_thumbnail);
		mEditBar = (LinearLayout) findViewById(R.id.guide_create_item_edit_section);
		mToggleEdit = (ToggleButton) findViewById(R.id.guide_create_toggle_edit);
		mToggleEdit.setChecked(mGuideCreateObject.getEditMode());
		mToggleEdit.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				mGuideCreateObject.setEditMode(isChecked);
				setEditMode(isChecked, true);
			}
		});
		mDeleteButton = (Button) findViewById(R.id.guide_create_delete_button);
		mDeleteButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				mPortalRef .deleteGuide(mGuideCreateObject);
			}

		});
		mEditButton = (Button) findViewById(R.id.guide_create_edit_button);
		mEditButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(mPortalRef.getActivity(), GuideCreateStepsActivity.class);
				intent.putExtra(GuideCreateStepsActivity.GuideKey, mGuideCreateObject);
				mPortalRef.getActivity().startActivity(intent);
			}

		});
		
		if(mGuideCreateObject.getEditMode())
			setEditMode(true, false);
	}
	
	public void setGuideObject(GuideCreateObject obj)
	{
		mGuideCreateObject = obj;
	}

	public void setEditMode(boolean isChecked, boolean animate) {
		if (isChecked) {
			if (animate) {
				Animation rotateAnimation = AnimationUtils.loadAnimation(
						mContext, R.anim.rotate_clockwise);

				mToggleEdit.startAnimation(rotateAnimation);
				
				Animation slideDownAnimation = AnimationUtils.loadAnimation(
						mContext, R.anim.slide_down);
				mEditBar.setVisibility(VISIBLE);
				mEditBar.startAnimation(slideDownAnimation);
			}
			else
			{
				mEditBar.setVisibility(VISIBLE);
			}

			editBarVisible = true;
		} else {
			if (animate) {
				Animation rotateAnimation = AnimationUtils.loadAnimation(
						mContext, R.anim.rotate_counterclockwise);

				mToggleEdit.startAnimation(rotateAnimation);
				Animation slideUpAnimation = AnimationUtils.loadAnimation(mContext,
						R.anim.slide_up);
				slideUpAnimation.setAnimationListener(new AnimationListener() {

					@Override
					public void onAnimationEnd(Animation animation) {
						mEditBar.setVisibility(GONE);

					}

					@Override
					public void onAnimationRepeat(Animation animation) {
						// TODO Auto-generated method stub

					}

					@Override
					public void onAnimationStart(Animation animation) {
						// TODO Auto-generated method stub

					}
				});
				mEditBar.startAnimation(slideUpAnimation);
			}
			else
			{
				mEditBar.setVisibility(GONE);
			}
			
			editBarVisible = false;
		}
	}

	public boolean editEnabled() {
		return editBarVisible;
	}

	public void setGuideItem(String title, String image) {
		mTitleView.setText(title);

		mImageManager.displayImage(image, mPortalRef.getActivity(), mThumbnail);
	}
	

	public void setTitleText(final String title) {
		Log.i("GuideItem", " title: " + title);
		mTitleView.setText(title);
		//mTitleView.invalidate();
		/*mPortalRef.getActivity().runOnUiThread(new Runnable() {

			public void run() {

				mTitleView.setText(title);

			}
		});*/

	}
}
