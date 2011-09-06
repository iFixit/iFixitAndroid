package com.ifixit.guidebook;


import android.app.Activity;
import android.content.Context;
import android.support.v4.view.ViewPager;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.ifixit.guidebook.GuideView;

public class GuideStepView extends LinearLayout {

   public static final double IMG_RESIZE = .80;
   
   private Context mContext;
   private TextView mTitle;
   private TextView mText;
   private ImageView mImage;
   private Gallery mThumbs;
   private GuideStep mStep;
   private ImageManager mImageManager;
   
   public GuideStepView(Context context, GuideStep step) {
      super(context);      
      this.mContext = context;
      this.mStep = step;
      
      this.mImageManager = new ImageManager(mContext);

      LayoutInflater inflater = (LayoutInflater) context.getSystemService(
            Context.LAYOUT_INFLATER_SERVICE);

      inflater.inflate(R.layout.guide_step, this, true);        

      mTitle = (TextView) findViewById(R.id.step_title);
      if (step.getTitle().isEmpty()) {
         mTitle.setText("Step " + step.getStepNum());
      } else {
         mTitle.setText(step.getTitle());
      }
      mText = (TextView) findViewById(R.id.step_text);
      mText.setText(Html.fromHtml(step.getText()).toString());
          
      mImage = (ImageView)findViewById(R.id.image);
      mImage.setAdjustViewBounds(true);

      int height = ((GuideView) context).getScreenHeight();
      int width = ((GuideView) context).getScreenWidth();

      mImage.setMaxHeight((int) Math.floor(height * IMG_RESIZE));
      mImage.setMaxWidth((int) Math.floor(width * IMG_RESIZE));
      mImage.setMinimumHeight((int) Math.floor(height * IMG_RESIZE));
      mImage.setMinimumWidth((int) Math.floor(width * IMG_RESIZE));
      
      mImageManager.displayImage(mStep.mImages.get(0).mText + ".large",
       (Activity)mContext, mImage);

      mThumbs = (Gallery) findViewById(R.id.thumbnail_gallery);
      mThumbs.setAdapter(new ThumbnailImageAdapter((Activity)mContext, step));
      
      mThumbs.setSpacing(30);
      mThumbs.setOnItemClickListener(new OnItemClickListener() {

         @Override
         public void onItemClick(AdapterView parent, View v, int position,
          long id) {            
            mImageManager.displayImage(mStep.mImages.get(position).mText + ".large",
             (Activity)mContext, mImage);
         }
      });
            
   }

   public ImageView getImageView() {
      return mImage;
   }
      
}
