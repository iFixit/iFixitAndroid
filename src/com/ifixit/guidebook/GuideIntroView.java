package com.ifixit.guidebook;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class GuideIntroView extends LinearLayout {
   private Context mContext;
   private TextView mTitle;
   private ImageView mImage;

   public GuideIntroView(Context context, Guide guide) {
      super(context);      
      this.mContext = context;

      LayoutInflater inflater = (LayoutInflater) context.getSystemService(
            Context.LAYOUT_INFLATER_SERVICE);

      inflater.inflate(R.layout.guide_intro, this, true);        

      mTitle = (TextView) findViewById(R.id.guide_title);
      mTitle.setText(guide.getTitle());
      mImage = (ImageView)findViewById(R.id.introImage);


   }

   public ImageView getImageView() {
      return mImage;
   }

}
