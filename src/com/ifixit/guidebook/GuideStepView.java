package com.ifixit.guidebook;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

public class GuideStepView extends LinearLayout {

   private Context mContext;
   private TextView mTitle;
   private TextView mText;
   
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
      
   }
}
