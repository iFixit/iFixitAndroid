package com.ifixit.guidebook;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class GuideStepView extends LinearLayout {

   private Context mContext;
   private TextView mTitle;
   private TextView mText;
   private ImageView mImage;
   
   public GuideStepView(Context context, GuideStep step) {
      super(context);      
      this.mContext = context;

      LayoutInflater inflater = (LayoutInflater) context.getSystemService(
            Context.LAYOUT_INFLATER_SERVICE);

      inflater.inflate(R.layout.guide_step, this, true);        

      mTitle = (TextView) findViewById(R.id.step_title);
      mTitle.setText(step.getTitle() + "Step " + step.getStepNum());
      
      mText = (TextView) findViewById(R.id.step_text);
      mText.setText(step.getText());

      mImage = (ImageView)findViewById(R.id.image);
   }

   public ImageView getImageView() {
      return mImage;
   }

}
