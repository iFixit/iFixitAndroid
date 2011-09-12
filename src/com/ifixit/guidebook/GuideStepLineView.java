package com.ifixit.guidebook;

import android.content.Context;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

public class GuideStepLineView extends LinearLayout {
   private static final int LINE_INDENT = 40;
   private static final int MARGIN = 8;
   
   private TextView mStepText;
   private LinearLayout mRow;
   
   public GuideStepLineView(Context context) {
      super(context);
      
      LayoutInflater inflater = (LayoutInflater) context.getSystemService(
            Context.LAYOUT_INFLATER_SERVICE);

      inflater.inflate(R.layout.step_row, this, true);  
      
      mRow = (LinearLayout) findViewById(R.id.step_row);
   }   

   public void setLine(StepLine line) {

      mStepText = (TextView) findViewById(R.id.step_text);
      mStepText.setText(line.getText());

      LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
       LinearLayout.LayoutParams.FILL_PARENT, 
       LinearLayout.LayoutParams.WRAP_CONTENT);
      
      layoutParams.setMargins(LINE_INDENT*line.getLevel(), MARGIN, 0, MARGIN);    
      mRow.setLayoutParams(layoutParams);
      
      BulletView bV = (BulletView) findViewById(R.id.bullet);
      bV.setBullet(line.getColor());

   }
}
