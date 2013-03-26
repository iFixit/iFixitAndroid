package com.dozuki.ifixit.ui.guide_create;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.*;
import android.widget.CompoundButton.OnCheckedChangeListener;
import com.dozuki.ifixit.R;
import com.dozuki.ifixit.model.guide.UserGuide;
import com.dozuki.ifixit.util.APIService;
import com.marczych.androidimagemanager.ImageManager;
import org.holoeverywhere.app.Activity;

public class GuideCreateListItem extends RelativeLayout implements AnimationListener {
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
   private boolean mEditBarVisible = false;
   private UserGuide mGuideCreateObject;

   public GuideCreateListItem(Context context, ImageManager imageManager, final GuidePortalFragment portalRef,
      UserGuide gObject) {
      super(context);
      mPortalRef = portalRef;
      mImageManager = imageManager;
      mGuideCreateObject = gObject;
      LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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
            setEditMode(isChecked, true, mToggleEdit, mEditBar);
         }
      });
      FrameLayout frame = (FrameLayout) findViewById(R.id.guide_create_frame);
      frame.setOnClickListener(new OnClickListener() {
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
            intent.putExtra(GuideCreateStepsActivity.GUIDE_KEY, mGuideCreateObject.getGuideid());
            mPortalRef.getActivity().startActivityForResult(intent, GuideCreateActivity.GUIDE_STEP_LIST_REQUEST);
         }
      });
      mPublishText = (TextView) findViewById(R.id.guide_create_item_publish_status);
      mPublishButtonText = (TextView) findViewById(R.id.guide_create_item_publish);
      mPublishButtonText.setOnClickListener(new OnClickListener() {
         @Override
         public void onClick(View v) {
            if(!mGuideCreateObject.getPublished())   {
               ((GuideCreateActivity) mPortalRef.getActivity()).showLoading();
               APIService.call((Activity) mPortalRef.getActivity(), APIService.getPublishGuideAPICall(mGuideCreateObject.getGuideid(), mGuideCreateObject.getRevisionid()));
            }  else {
               ((GuideCreateActivity) mPortalRef.getActivity()).showLoading();
               APIService.call((Activity) mPortalRef.getActivity(), APIService.getUnPublishGuideAPICall(mGuideCreateObject.getGuideid(), mGuideCreateObject.getRevisionid()));
            }
            setPublished(!mGuideCreateObject.getPublished());
         }
      });
      if (mGuideCreateObject.getPublished()) {
         setPublished(true);
      }
      if (mGuideCreateObject.getEditMode()) {
         setEditMode(true, false, mToggleEdit, mEditBar);
      }
   }

   public void setGuideObject(UserGuide obj) {
      mGuideCreateObject = obj;
   }

   public void setPublished(boolean published) {
      if (published) {
         Drawable img = getContext().getResources().getDrawable(R.drawable.ic_list_item_unpublish);
         img.setBounds(0, 0, img.getMinimumWidth(), img.getMinimumHeight());
         setPublishedText(Color.rgb(0, 191, 0), R.string.published);
         setPublishedButton(img, R.string.unpublish);
      } else {
          Drawable img = getContext().getResources().getDrawable(R.drawable.ic_list_item_publish);
         img.setBounds(0, 0, img.getMinimumWidth(), img.getMinimumHeight());
         setPublishedText(Color.RED, R.string.unpublished);
         setPublishedButton(img, R.string.publish);
      }
      mGuideCreateObject.setPublished(published);
   }

   private void setPublishedText(int color, int text) {
      mPublishText.setText(text);
      mPublishText.setTextColor(color);
   }

   private void setPublishedButton(Drawable img, int text) {
      mPublishButtonText.setCompoundDrawables(img, null, null, null);
      mPublishButtonText.setText(text);
   }

   public void setEditMode(boolean isChecked, boolean animate, final ToggleButton mToggleEdit,
      final LinearLayout mEditBar) {
      if (isChecked) {
         if (animate) {
            Animation rotateAnimation = AnimationUtils.loadAnimation(getContext(), R.anim.rotate_clockwise);
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
            Animation rotateAnimation = AnimationUtils.loadAnimation(getContext(), R.anim.rotate_counterclockwise);
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

   public boolean editEnabled() {
      return mEditBarVisible;
   }

   public void setChecked(boolean check) {
      mToggleEdit.setChecked(check);
   }

   public void setGuideItem(String title, String image) {   
      mTitleView.setText(title);
      if(image != null) {
         mImageManager.displayImage(image, mPortalRef.getActivity(), mThumbnail);
      }
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
