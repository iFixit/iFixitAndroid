package com.dozuki.ifixit.guide_create.ui;

import com.dozuki.ifixit.R;
import com.dozuki.ifixit.guide_create.model.GuideCreateObject;
import com.ifixit.android.imagemanager.ImageManager;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

public class GuideCreateListItem extends RelativeLayout {
	private static final int ANIMATION_DURATION = 300;
   private TextView mTitleView;
	private ImageView mThumbnail;
	private TextView mDeleteButton;
	private TextView mEditButton;
	private TextView mPublishText;
	private TextView mPublishButtonText;
	private ToggleButton mToggleEdit;
	private LinearLayout mEditBar;
	private ImageManager mImageManager;
	private GuidePortalFragment mPortalRef;
	private Context mContext;
	private boolean editBarVisible = false;
	private GuideCreateObject mGuideCreateObject;
	private boolean mHasAnimated;

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
         public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            mGuideCreateObject.setEditMode(isChecked);
            portalRef.onItemSelected(mGuideCreateObject.getGuideid(), isChecked);
            setEditMode(isChecked, true);
         }
      });
      FrameLayout frame = (FrameLayout) findViewById(R.id.guide_create_frame);
      frame.setOnClickListener(new OnClickListener(){

		@Override
		public void onClick(View v) {
			mToggleEdit.toggle();
		}
    	  
      });
      mDeleteButton = (TextView) findViewById(R.id.guide_create_item_delete);
      mDeleteButton.setOnClickListener(new OnClickListener() {
         @Override
         public void onClick(View v) {
            mPortalRef.deleteGuide(mGuideCreateObject);
         }
      });
      mEditButton = (TextView) findViewById(R.id.guide_create_item_edit);
      mEditButton.setOnClickListener(new OnClickListener() {
         @Override
         public void onClick(View v) {
            Intent intent = new Intent(mPortalRef.getActivity(), GuideCreateStepsActivity.class);
            intent.putExtra(GuideCreateStepsActivity.GuideKey, mGuideCreateObject);
            mPortalRef.getActivity().startActivityForResult(intent, GuideCreateActivity.GUIDE_STEP_LIST_REQUEST);
         }
      });
		mPublishText = (TextView) findViewById(R.id.guide_create_item_publish_status);
		mPublishButtonText = (TextView) findViewById(R.id.guide_create_item_publish);
		mPublishButtonText.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
			    setPublished(!mGuideCreateObject.getPublished());
					}
				});
		if (mGuideCreateObject.getPublished())
		   setPublished(true);
		if (mGuideCreateObject.getEditMode())
			setEditMode(true, false);
	}

	public void setGuideObject(GuideCreateObject obj) {
		mGuideCreateObject = obj;
	}
	
   public void setPublished(boolean published) {
      if (published) {
         Drawable img = getContext().getResources().getDrawable(R.drawable.ic_list_item_unpublish);
         img.setBounds(0, 0, img.getMinimumWidth(),  img.getMinimumHeight());
         mPublishButtonText.setCompoundDrawables(img, null, null, null);
         mPublishText.setText(R.string.published);
         mPublishButtonText.setText(R.string.unpublish);
         mPublishText.setTextColor(Color.GREEN);
      } else {
         Drawable img = getContext().getResources().getDrawable(R.drawable.ic_list_item_publish);
         img.setBounds(0, 0, img.getMinimumWidth(), img.getMinimumHeight());
         mPublishButtonText.setCompoundDrawables(img, null, null, null);
         mPublishText.setText(R.string.unpublished);
         mPublishButtonText.setText(R.string.publish);
         mPublishText.setTextColor(Color.RED);
      }
      mGuideCreateObject.setPublished(published);
   }

	public void setEditMode(boolean isChecked, boolean animate) {
		if (isChecked) {
         if (animate) {
            Animation rotateAnimation = AnimationUtils.loadAnimation(mContext, R.anim.rotate_clockwise);
            rotateAnimation.setInterpolator(new DecelerateInterpolator());
            mToggleEdit.startAnimation(rotateAnimation);

            // Creating the expand animation for the item
            ExpandAnimation expandAni = new ExpandAnimation(mEditBar, ANIMATION_DURATION);

            // Start the animation on the toolbar
            mEditBar.startAnimation(expandAni);
            
         } else {
            mEditBar.setVisibility(VISIBLE);
            ((LinearLayout.LayoutParams) mEditBar.getLayoutParams()).bottomMargin = 0;

         }

         editBarVisible = true;
      } else {
         if (animate) {
            Animation rotateAnimation = AnimationUtils.loadAnimation(mContext, R.anim.rotate_counterclockwise);
            rotateAnimation.setInterpolator(new AccelerateInterpolator());
            mToggleEdit.startAnimation(rotateAnimation);
            // Creating the expand animation for the item
            ExpandAnimation expandAni = new ExpandAnimation(mEditBar, ANIMATION_DURATION);

            // Start the animation on the toolbar
            mEditBar.startAnimation(expandAni);
			} else {
				mEditBar.setVisibility(GONE);
            ((LinearLayout.LayoutParams) mEditBar.getLayoutParams()).bottomMargin = -50;
			}

			editBarVisible = false;
		}
	}

	public boolean editEnabled() {
		return editBarVisible;
	}
	
	public void setChecked(boolean check) {
	   mToggleEdit.setChecked(check);
	}

	public void setGuideItem(String title, String image) {
		mTitleView.setText(title);

		mImageManager.displayImage(image, mPortalRef.getActivity(), mThumbnail);
	}

	public void setTitleText(final String title) {
		Log.i("GuideItem", " title: " + title);
		mTitleView.setText(title);
		// mTitleView.invalidate();
		/*
		 * mPortalRef.getActivity().runOnUiThread(new Runnable() {
		 * 
		 * public void run() {
		 * 
		 * mTitleView.setText(title);
		 * 
		 * } });
		 */

	}

}
