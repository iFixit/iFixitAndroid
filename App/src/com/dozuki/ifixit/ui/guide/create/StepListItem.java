package com.dozuki.ifixit.ui.guide.create;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import com.dozuki.ifixit.MainApplication;
import com.dozuki.ifixit.R;
import com.dozuki.ifixit.model.Image;
import com.dozuki.ifixit.model.VideoThumbnail;
import com.dozuki.ifixit.model.guide.GuideStep;
import com.dozuki.ifixit.ui.TouchableRelativeLayout;
import com.google.analytics.tracking.android.MapBuilder;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class StepListItem extends TouchableRelativeLayout {
   private static final int EDIT_OPTION = 0;
   private static final int DELETE_OPTION = 1;
   private TextView mStepsView;
   private TextView mStepNumber;
   private ImageView mImageView;
   private Context mContext;
   private StepPortalFragment mPortalRef;
   private GuideStep mStepObject;
   private int mStepPosition;

   public StepListItem(Context context, final StepPortalFragment portalRef) {
      super(context);
      mContext = context;
      mPortalRef = portalRef;

      LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
      inflater.inflate(R.layout.guide_create_step_list_item, this, true);
      mStepsView = (TextView) findViewById(R.id.step_line_text_view);
      mStepNumber = (TextView) findViewById(R.id.guide_create_step_item_number);
      mImageView = (ImageView) findViewById(R.id.guide_step_item_thumbnail);

      setOnClickListener(new OnClickListener() {
         @Override
         public void onClick(View v) {
            editStep();
         }
      });

      final View menuButton = findViewById(R.id.step_item_menu_button);

      menuButton.setOnClickListener(new OnClickListener() {
         @Override
         public void onClick(View v) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {

               PopupMenu itemMenu = new PopupMenu(mContext, menuButton);

               itemMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                  @Override
                  public boolean onMenuItemClick(MenuItem item) {
                     switch (item.getItemId()) {
                        case R.id.step_create_item_edit:
                           editStep();
                           break;
                        case R.id.step_create_item_delete:
                           deleteStep();
                           break;
                     }

                     return true;
                  }
               });

               MenuInflater menuInflater = itemMenu.getMenuInflater();
               menuInflater.inflate(R.menu.step_item_popup, itemMenu.getMenu());
               itemMenu.show();
            } else {
               // PopupMenu was added in API 11, so let's use an AlertDialog instead.
               AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
               builder.setItems(R.array.step_list_item_options, new DialogInterface.OnClickListener() {
                  public void onClick(DialogInterface dialog, int which) {
                     switch (which) {
                        case EDIT_OPTION:
                           editStep();
                           break;
                        case DELETE_OPTION:
                           deleteStep();
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

   private void deleteStep() {
      MainApplication.getGaTracker()
       .send(MapBuilder.createEvent("ui_action", "button_press", "delete_step", null).build());

      mPortalRef.createDeleteDialog(mStepObject).show();
   }

   public void setRowData(GuideStep step, int position) {
      mStepObject = step;
      mStepPosition = position;

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
   }

   private void editStep() {
      MainApplication.getGaTracker()
       .send(MapBuilder.createEvent("ui_action", "button_press", "edit_step", null).build());

      mPortalRef.launchStepEdit(mStepPosition);
   }

   private void setStepThumbnail(ArrayList<Image> imageList, ImageView imageView) {
      if (imageList.size() == 0) {
         Picasso
          .with(mContext)
          .load(R.drawable.no_image)
          .noFade()
          .into(imageView);
      } else {
         for (Image imageInfo : imageList) {
            if (imageInfo.getId() > 0) {
               String url = imageInfo.getPath(".standard");
               setStepThumbnail(url, imageView);
               return;
            }
         }
      }
   }

   private void setStepThumbnail(VideoThumbnail thumb, ImageView imageView) {
      String url = thumb.getPath(".standard");

      // Videos are not guaranteed to be 4:3 ratio, so let's fake it.
      imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

      setStepThumbnail(url, imageView);
   }

   private void setStepThumbnail(String url, ImageView imageView) {
      imageView.setTag(url);

      Picasso
       .with(mContext)
       .load(url)
       .noFade()
       .error(R.drawable.no_image)
       .into(imageView);

      imageView.invalidate();
   }
}
