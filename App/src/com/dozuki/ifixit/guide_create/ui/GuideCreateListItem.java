package com.dozuki.ifixit.guide_create.ui;

import com.dozuki.ifixit.R;
import com.ifixit.android.imagemanager.ImageManager;

import android.app.Activity;
import android.content.Context;
import android.text.Html;
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
	private GuideCreateListItem mSelf;
	private boolean editBarVisible = false;
	private int mID;

	public GuideCreateListItem(Context context, ImageManager imageManager,
			final GuidePortalFragment portalRef, int id) {
		super(context);
		mContext = context;
		mPortalRef = portalRef;
		mImageManager = imageManager;
		mID = id;
		mSelf = this;
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.guide_create_item, this, true);

		mTitleView = (TextView) findViewById(R.id.guide_create_item_title);
		mThumbnail = (ImageView) findViewById(R.id.guide_create_item_thumbnail);
		mEditBar = (LinearLayout) findViewById(R.id.guide_create_item_edit_section);
		mToggleEdit = (ToggleButton) findViewById(R.id.guide_create_toggle_edit);
		mToggleEdit.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				setEditMode(isChecked, true);
			}
		});
		mDeleteButton = (Button) findViewById(R.id.guide_create_delete_button);
		mDeleteButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				portalRef.deleteGuide(mSelf);
				// mDeleteButton.setOnClickListener(null);
			}

		});
		mEditButton = (Button) findViewById(R.id.guide_create_edit_button);
		mEditButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

			}

		});
	}

	public void setEditMode(boolean isChecked, boolean animate) {
		if (isChecked) {
			if (animate) {
				Animation rotateAnimation = AnimationUtils.loadAnimation(
						mContext, R.anim.rotate_clockwise);

				mToggleEdit.startAnimation(rotateAnimation);
			}

			Animation slideDownAnimation = AnimationUtils.loadAnimation(
					mContext, R.anim.slide_down);
			mEditBar.setVisibility(VISIBLE);
			mEditBar.startAnimation(slideDownAnimation);

			editBarVisible = true;
		} else {
			if (animate) {
				Animation rotateAnimation = AnimationUtils.loadAnimation(
						mContext, R.anim.rotate_counterclockwise);

				mToggleEdit.startAnimation(rotateAnimation);
			}
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
			editBarVisible = false;
		}
	}

	public boolean editEnabled() {
		return editBarVisible;
	}

	public void setGuideItem(String title, String image, Context context) {
		mContext = context;

		mTitleView.setText(Html.fromHtml(title));

		mImageManager.displayImage(image, (Activity) mContext, mThumbnail);
	}

	public void setText(final String title) {
		mPortalRef.getActivity().runOnUiThread(new Runnable() {

			public void run() {

				mTitleView.setText(title);

			}
		});

	}

	public int getId() {
		return mID;
	}

	public void setId(int id) {
		mID = id;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof GuideCreateListItem)) {
			return false;
		}
		GuideCreateListItem lhs = (GuideCreateListItem) o;
		return mID == lhs.mID;
	}
}
