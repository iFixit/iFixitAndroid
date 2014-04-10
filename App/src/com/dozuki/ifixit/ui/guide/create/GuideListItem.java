package com.dozuki.ifixit.ui.guide.create;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.dozuki.ifixit.App;
import com.dozuki.ifixit.R;
import com.dozuki.ifixit.model.guide.GuideInfo;
import com.dozuki.ifixit.ui.RoundedTransformation;
import com.dozuki.ifixit.ui.TouchableRelativeLayout;
import com.dozuki.ifixit.ui.guide.view.GuideViewActivity;
import com.dozuki.ifixit.util.ImageSizes;
import com.dozuki.ifixit.util.PicassoUtils;
import com.dozuki.ifixit.util.api.Api;
import com.dozuki.ifixit.util.api.ApiCall;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

public class GuideListItem extends TouchableRelativeLayout {
   private static final int DELETE_OPTION = 3;
   private static final int EDIT_OPTION = 2;
   private static final int PUBLISH_OPTION = 1;
   private static final int VIEW_OPTION = 0;
   private Context mContext;

   private TextView mTitleView;
   private ImageView mThumbnail;
   private TextView mPublishText;
   private Activity mActivity;
   private GuideInfo mGuideInfo;

   public GuideListItem(Context context, Activity activity) {
      super(context);
      mActivity = activity;
      mContext = context;

      LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
      inflater.inflate(R.layout.guide_create_item, this, true);

      mTitleView = (TextView) findViewById(R.id.guide_create_item_title);
      mThumbnail = (ImageView) findViewById(R.id.guide_create_item_thumbnail);
      mPublishText = (TextView) findViewById(R.id.guide_create_item_publish_status);

      setOnClickListener(new OnClickListener() {
         @Override
         public void onClick(View v) {
            editGuide();
         }
      });

      final View menuButton = findViewById(R.id.guide_item_menu_button);

      menuButton.setOnClickListener(new OnClickListener() {
         @Override
         public void onClick(View v) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
               PopupMenu itemMenu = new PopupMenu(mContext, menuButton);

               itemMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                  @Override
                  public boolean onMenuItemClick(MenuItem item) {
                     switch (item.getItemId()) {
                        case R.id.guide_create_item_view:
                           viewGuide();
                           break;
                        case R.id.guide_create_item_edit:
                           editGuide();
                           break;
                        case R.id.guide_create_item_publish:
                           publishGuide();
                           break;
                        case R.id.guide_create_item_delete:
                           deleteGuide();
                           break;
                     }

                     return true;
                  }
               });

               MenuInflater menuInflater = itemMenu.getMenuInflater();
               menuInflater.inflate(R.menu.guide_item_popup, itemMenu.getMenu());
               Menu menu = itemMenu.getMenu();

               if (mGuideInfo.mPublic) {
                  menu.findItem(R.id.guide_create_item_publish).setTitle(R.string.unpublish);
               }

               // Disable deleting guides on iFixit for now.
               if (App.get().getSite().isIfixit()) {
                  menu.findItem(R.id.guide_create_item_delete).setVisible(false);
               }

               itemMenu.show();
            } else {
               // PopupMenu was added in API 11, so let's use an AlertDialog instead.
               AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
               builder.setItems(App.get().getSite().isIfixit() ? R.array.guide_list_item_options
                : R.array.guide_list_item_options_with_delete, new DialogInterface.OnClickListener() {
                  public void onClick(DialogInterface dialog, int which) {
                     switch (which) {
                        case VIEW_OPTION:
                           viewGuide();
                           break;
                        case PUBLISH_OPTION:
                           publishGuide();
                           break;
                        case EDIT_OPTION:
                           editGuide();
                           break;
                        case DELETE_OPTION:
                           deleteGuide();
                           break;
                     }
                  }
               });
               builder.create();
               builder.show();
            }
         }
      });
   }

   private void viewGuide() {
      Intent intent = new Intent(mActivity, GuideViewActivity.class);
      intent.putExtra(GuideViewActivity.GUIDEID, mGuideInfo.mGuideid);
      intent.putExtra(GuideViewActivity.CURRENT_PAGE, 0);
      mActivity.startActivity(intent);
   }

   private void publishGuide() {
      App.sendEvent("ui_action", "button_press", "publish_guide", (long)mGuideInfo.mGuideid);

      // Ignore button press if we are already (un)publishing the guide.
      if (mGuideInfo.mIsPublishing) {
         return;
      }

      mGuideInfo.mIsPublishing = true;
      mPublishText.setText(mGuideInfo.mPublic ? R.string.unpublishing : R.string.publishing);
      mPublishText.setTextColor(getResources().getColor(R.color.text_light));

      if (!mGuideInfo.mPublic) {
         Api.call(mActivity,
          ApiCall.publishGuide(mGuideInfo.mGuideid, mGuideInfo.mRevisionid));
      } else {
         Api.call(mActivity,
          ApiCall.unpublishGuide(mGuideInfo.mGuideid, mGuideInfo.mRevisionid));
      }
   }

   private void deleteGuide() {
      App.sendEvent("ui_action", "button_press", "delete_guide", (long)mGuideInfo.mGuideid);

      ((GuideCreateActivity) mActivity).createDeleteDialog(mGuideInfo).show();
   }

   public void setRowData(GuideInfo guideInfo) {
      mGuideInfo = guideInfo;
      setTag(mGuideInfo.mGuideid);

      mTitleView.setText(Html.fromHtml(mGuideInfo.mTitle));

      if (mThumbnail != null) {
         Picasso picasso = PicassoUtils.with(mContext);

         Transformation transform = new RoundedTransformation(4, 0);

         if (mGuideInfo.hasImage()) {
            picasso
             .load(mGuideInfo.getImagePath(ImageSizes.guideList))
             .noFade()
             .fit()
             .transform(transform)
             .error(R.drawable.no_image)
             .into(mThumbnail);
         } else {
            picasso
             .load(R.drawable.no_image)
             .noFade()
             .fit()
             .transform(transform)
             .into(mThumbnail);
         }
      }

      setPublished(mGuideInfo.mPublic);
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

   private void editGuide() {
      App.sendEvent("ui_action", "button_press", "edit_guide", (long)mGuideInfo.mGuideid);

      Intent intent = new Intent(mActivity, StepsActivity.class);
      intent.putExtra(StepsActivity.GUIDE_ID_KEY, mGuideInfo.mGuideid);
      intent.putExtra(StepsActivity.GUIDE_PUBLIC_KEY, mGuideInfo.mPublic);
      mActivity.startActivityForResult(intent, GuideCreateActivity.GUIDE_STEP_LIST_REQUEST);
   }

   private void buildPublishView(int drawable, int color, int textString, int buttonString) {
      Drawable img = getContext().getResources().getDrawable(drawable);
      img.setBounds(0, 0, img.getMinimumWidth(), img.getMinimumHeight());

      mPublishText.setText(textString);
      mPublishText.setTextColor(color);
   }
}
