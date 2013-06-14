package com.dozuki.ifixit.ui.guide.create;

import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.*;
import android.widget.CompoundButton.OnCheckedChangeListener;
import com.dozuki.ifixit.MainApplication;
import com.dozuki.ifixit.R;
import com.dozuki.ifixit.model.APIImage;
import com.dozuki.ifixit.model.guide.GuideStep;
import com.dozuki.ifixit.model.guide.StepVideoThumbnail;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class StepListItem extends RelativeLayout implements AnimationListener {
   private static int ANIMATION_DURATION = 300;
   private TextView mStepsView;
   private TextView mStepNumber;
   private ToggleButton mToggleEdit;
   private TextView mDeleteButton;
   private TextView mEditButton;
   private LinearLayout mEditBar;
   private ImageView mImageView;
   private RelativeLayout mStepFrame;
   private Context mContext;
   private StepPortalFragment mPortalRef;
   private GuideStep mStepObject;
   private int mStepPosition;

   public StepListItem(Context context, final StepPortalFragment portalRef, GuideStep sObject, int position) {
      super(context);
      mContext = context;
      mPortalRef = portalRef;

      mStepObject = sObject;
      mStepPosition = position;
      LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
      inflater.inflate(R.layout.guide_create_step_list_item, this, true);
      mStepsView = (TextView) findViewById(R.id.step_line_text_view);
      mStepNumber = (TextView) findViewById(R.id.guide_create_step_item_number);
      mToggleEdit = (ToggleButton) findViewById(R.id.step_item_toggle_edit);
      mImageView = (ImageView) findViewById(R.id.guide_step_item_thumbnail);
      mDeleteButton = (TextView) findViewById(R.id.step_create_item_delete);
      mEditButton = (TextView) findViewById(R.id.step_create_item_edit);
      mEditBar = (LinearLayout) findViewById(R.id.step_create_item_edit_section);
      mStepFrame = (RelativeLayout) findViewById(R.id.guide_step_edit_frame);
      boolean isEdit = sObject.getEditMode();
      mToggleEdit.setOnCheckedChangeListener(null);
      mToggleEdit.setChecked(isEdit);
      mToggleEdit.setOnCheckedChangeListener(new OnCheckedChangeListener() {
         @Override
         public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            mStepObject.setEditMode(isChecked);
            portalRef.onItemSelected(mStepObject.getStepid(), isChecked);
            setEditMode(isChecked, true, mToggleEdit, mEditBar);
         }
      });
      mStepFrame.setOnClickListener(new OnClickListener() {
         @Override
         public void onClick(View v) {
            mToggleEdit.toggle();
         }
      });
      mDeleteButton.setOnClickListener(new OnClickListener() {
         @Override
         public void onClick(View v) {
            mPortalRef.createDeleteDialog(mStepObject).show();
         }
      });
      mEditButton.setOnClickListener(new OnClickListener() {
         @Override
         public void onClick(View v) {
            mPortalRef.launchStepEdit(mStepPosition);
         }
      });

      String stepText = MainApplication.get().getString(R.string.step_number, mStepPosition + 1);
      if (mStepObject.getTitle().equals("")) {
         mStepsView.setText(stepText);
         mStepNumber.setVisibility(View.GONE);
      } else {
         mStepsView.setText(Html.fromHtml(mStepObject.getTitle()));
         mStepNumber.setText(stepText);
         mStepNumber.setVisibility(View.VISIBLE);
      }

      if (mStepObject.hasVideo()) {
         setStepThumbnail(mStepObject.getVideo().getThumbnail(), mImageView);
      } else {
         setStepThumbnail(mStepObject.getImages(), mImageView);
      }
      setEditMode(isEdit, false, mToggleEdit, mEditBar);
   }

   public void setEditMode(boolean isChecked, boolean animate, final ToggleButton mToggleEdit,
    final LinearLayout mEditBar) {
      if (isChecked) {
         if (animate) {
            Animation rotateAnimation = AnimationUtils.loadAnimation(mContext, R.anim.rotate_clockwise);
            mToggleEdit.startAnimation(rotateAnimation);
            // Creating the expand animation for the item
            ExpandAnimation expandAni = new ExpandAnimation(mEditBar, ANIMATION_DURATION);
            expandAni.setAnimationListener(this);
            // Start the animation on the toolbar
            mEditBar.startAnimation(expandAni);
         } else {
            mEditBar.setVisibility(View.VISIBLE);
            ((LinearLayout.LayoutParams) mEditBar.getLayoutParams()).bottomMargin = 0;
         }
      } else {
         if (animate) {
            Animation rotateAnimation = AnimationUtils.loadAnimation(mContext, R.anim.rotate_counterclockwise);
            mToggleEdit.startAnimation(rotateAnimation);
            ExpandAnimation expandAni = new ExpandAnimation(mEditBar, ANIMATION_DURATION);
            expandAni.setAnimationListener(this);
            mEditBar.startAnimation(expandAni);
         } else {
            mEditBar.setVisibility(View.GONE);
            ((LinearLayout.LayoutParams) mEditBar.getLayoutParams()).bottomMargin = -50;
         }
      }
   }

   private void setStepThumbnail(ArrayList<APIImage> imageList, ImageView imageView) {
      for (APIImage imageInfo : imageList) {
         if (imageInfo.mId > 0) {
            String url = imageInfo.getPath(".standard");
            setStepThumbnail(url, imageView);
            return;
         }
      }
   }

   private void setStepThumbnail(StepVideoThumbnail thumb, ImageView imageView) {
      String url = thumb.getUrl(".standard");

      // Videos are not guaranteed to be 4:3 ratio, so let's fake it.
      imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

      setStepThumbnail(url, imageView);
   }

   private void setStepThumbnail(String url, ImageView imageView) {
      imageView.setTag(url);

      Picasso
       .with(mContext)
       .load(url)
       .error(R.drawable.no_image)
       .into(imageView);

      imageView.invalidate();
   }

   public void setChecked(boolean check) {
      mToggleEdit.setChecked(check);
   }

   @Override
   public void onAnimationEnd(Animation animation) {
      mPortalRef.invalidateViews();
   }

   @Override
   public void onAnimationRepeat(Animation animation) {

   }

   @Override
   public void onAnimationStart(Animation animation) {

   }
}
