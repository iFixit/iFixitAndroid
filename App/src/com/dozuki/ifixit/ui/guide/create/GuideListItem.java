package com.dozuki.ifixit.ui.guide.create;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.dozuki.ifixit.MainApplication;
import com.dozuki.ifixit.R;
import com.dozuki.ifixit.model.guide.GuideInfo;
import com.dozuki.ifixit.ui.guide.view.GuideViewActivity;
import com.dozuki.ifixit.util.api.Api;
import com.dozuki.ifixit.util.PicassoUtils;
import com.dozuki.ifixit.util.api.ApiCall;
import com.google.analytics.tracking.android.MapBuilder;
import com.squareup.picasso.Picasso;

public class GuideListItem extends LinearLayout {
   private static final int ANIMATION_DURATION = 300;

   private static final boolean STATE_CLOSED = false;
   private Context mContext;

   private TextView mTitleView;
   private ImageView mThumbnail;
   private TextView mDeleteButton;
   private TextView mEditButton;
   private TextView mPublishText;
   private TextView mPublishButton;
   private ToggleButton mToggleEdit;
   private LinearLayout mEditBar;
   private Activity mActivity;
   private final RelativeLayout mUpperSection;
   private GuideInfo mGuideInfo;

   public GuideListItem(Context context, Activity activity) {
      super(context);
      mActivity = activity;
      mContext = context;

      LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
      inflater.inflate(R.layout.guide_create_item, this, true);

      mTitleView = (TextView) findViewById(R.id.guide_create_item_title);
      mThumbnail = (ImageView) findViewById(R.id.guide_create_item_thumbnail);
      mToggleEdit = (ToggleButton) findViewById(R.id.guide_create_toggle_edit);
      mUpperSection = (RelativeLayout) findViewById(R.id.guide_create_upper_section);
      mDeleteButton = (TextView) findViewById(R.id.guide_create_item_delete);
      mEditBar = (LinearLayout) findViewById(R.id.guide_create_item_edit_section);
      mEditButton = (TextView) findViewById(R.id.guide_create_item_edit);
      mPublishText = (TextView) findViewById(R.id.guide_create_item_publish_status);
      mPublishButton = (TextView) findViewById(R.id.guide_create_item_publish);

      findViewById(R.id.guide_create_item_view).setOnClickListener(new OnClickListener() {
         @Override
         public void onClick(View v) {
            Intent intent = new Intent(mActivity, GuideViewActivity.class);
            intent.putExtra(GuideViewActivity.GUIDEID, mGuideInfo.mGuideid);
            intent.putExtra(GuideViewActivity.CURRENT_PAGE, 0);
            mActivity.startActivity(intent);
         }
      });

      mToggleEdit.setOnCheckedChangeListener(null);
      mToggleEdit.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
         @Override
         public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            mGuideInfo.mEditMode = isChecked;
            ((GuideCreateActivity) mActivity).onItemSelected(mGuideInfo.mGuideid, isChecked);
            toggleListItem(isChecked, true, mToggleEdit, mEditBar);
         }
      });

      OnClickListener upperSectionListener = new OnClickListener() {
         @Override
         public void onClick(View v) {
            MainApplication.getGaTracker().send(MapBuilder.createEvent("ui_action", "button_press",
             "toggle_guide_item", null).build());

            mToggleEdit.toggle();
         }
      };

      setOnClickListener(upperSectionListener);

      mUpperSection.setOnClickListener(upperSectionListener);
      if (MainApplication.get().getSite().mName.equals("ifixit")) {
         mDeleteButton.setVisibility(View.GONE);
      } else {
         mDeleteButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
               MainApplication.getGaTracker().send(MapBuilder.createEvent("ui_action", "button_press", "delete_guide", null).build());

               ((GuideCreateActivity) mActivity).createDeleteDialog(mGuideInfo).show();
            }
         });
      }
      mEditButton.setOnClickListener(new OnClickListener() {
         @Override
         public void onClick(View v) {
            MainApplication.getGaTracker().send(MapBuilder.createEvent("ui_action", "button_press", "edit_guide", null).build());

            Intent intent = new Intent(mActivity, StepsActivity.class);
            intent.putExtra(StepsActivity.GUIDE_ID_KEY, mGuideInfo.mGuideid);
            intent.putExtra(StepsActivity.GUIDE_PUBLIC_KEY, mGuideInfo.mPublic);
            mActivity.startActivityForResult(intent, GuideCreateActivity.GUIDE_STEP_LIST_REQUEST);
         }
      });
      mPublishButton.setOnClickListener(new OnClickListener() {
         @Override
         public void onClick(View v) {
            MainApplication.getGaTracker().send(MapBuilder.createEvent("ui_action", "button_press", "publish_guide",
             null).build());

            // Ignore button press if we are already (un)publishing the guide.
            if (mGuideInfo.mIsPublishing) {
               return;
            }

            mGuideInfo.mIsPublishing = true;
            mPublishButton.setText(mGuideInfo.mPublic ? R.string.unpublishing : R.string.publishing);

            if (!mGuideInfo.mPublic) {
               Api.call(mActivity,
                ApiCall.publishGuide(mGuideInfo.mGuideid, mGuideInfo.mRevisionid));
            } else {
               Api.call(mActivity,
                ApiCall.unpublishGuide(mGuideInfo.mGuideid, mGuideInfo.mRevisionid));
            }
         }
      });
   }

   public void setRowData(GuideInfo guideInfo) {
      mGuideInfo = guideInfo;
      setTag(mGuideInfo.mGuideid);

      mTitleView.setText(Html.fromHtml(mGuideInfo.mTitle));
      mToggleEdit.setChecked(mGuideInfo.mEditMode);

      if (mThumbnail != null) {
         Picasso picasso = PicassoUtils.with(mContext);

         if (mGuideInfo.hasImage()) {
            picasso
             .load(mGuideInfo.getImagePath(".standard"))
             .noFade()
             .error(R.drawable.no_image)
             .into(mThumbnail);
         } else {
            picasso
             .load(R.drawable.no_image)
             .noFade()
             .into(mThumbnail);
         }
      }

      setPublished(mGuideInfo.mPublic);
      toggleListItem(mGuideInfo.mEditMode, false, mToggleEdit, mEditBar);
   }

   public void setPublished(boolean published) {
      if (published) {
         buildPublishView(R.drawable.ic_list_item_unpublish, Color.rgb(0, 191, 0),
          R.string.published, mGuideInfo.mIsPublishing ? R.string.unpublishing : R.string.unpublish);
      } else {
         buildPublishView(R.drawable.ic_list_item_publish, Color.RED,
          R.string.unpublished, mGuideInfo.mIsPublishing ? R.string.publishing : R.string.publish);
      }
   }

   private void buildPublishView(int drawable, int color, int textString, int buttonString) {
      Drawable img = getContext().getResources().getDrawable(drawable);
      img.setBounds(0, 0, img.getMinimumWidth(), img.getMinimumHeight());

      mPublishText.setText(textString);
      mPublishText.setTextColor(color);

      mPublishButton.setCompoundDrawables(img, null, null, null);
      mPublishButton.setText(buttonString);
   }

   public void toggleListItem(boolean isChecked, boolean animate, final ToggleButton mToggleEdit,
    final LinearLayout mEditBar) {
      if (isChecked) {
         if (animate) {
            Animation rotateAnimation = AnimationUtils.loadAnimation(getContext(), R.anim.rotate_clockwise);
            mToggleEdit.startAnimation(rotateAnimation);
            // Creating the expand animation for the item
            ExpandAnimation expandAni = new ExpandAnimation(mEditBar, ANIMATION_DURATION);
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
            mEditBar.startAnimation(expandAni);
         } else {
            mEditBar.setVisibility(View.GONE);
            ((LinearLayout.LayoutParams) mEditBar.getLayoutParams()).bottomMargin = -50;
         }
      }
   }

   public void setChecked(boolean check) {
      mToggleEdit.setChecked(check);
   }

}
